package pl.robakowski.it;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonWriter;
import io.activej.bytebuf.ByteBuf;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.robakowski.Launcher;
import pl.robakowski.atms.AtmHandler;
import pl.robakowski.atms.Request;
import pl.robakowski.game.Clan;
import pl.robakowski.game.Game;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class E2ETest {

    private static final Launcher launcher = new Launcher();
    private static final Logger LOGGER = LoggerFactory.getLogger(E2ETest.class);

    private static final Random random = new SecureRandom(new byte[]{35, 32, 56, 45, 62});

    @BeforeAll
    public static void startServer() throws Exception {
        new Thread(() -> {
            try {
                launcher.launch(new String[0]);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
        launcher.getStartFuture().toCompletableFuture().get();
    }

    @AfterAll
    public static void stopServer() {
        launcher.shutdown();
    }

    @ParameterizedTest
    @ValueSource(strings = {"game1", "game_big"})
    public void testGame(String name) throws Exception {
        doRequest("onlinegame/calculate", name + "_request.json", name + "_response.json");
    }

    private static final List<Game> GAMES = IntStream.range(0, 1000).mapToObj(i -> {
        List<Clan> clans = IntStream.range(0, 20000)
                .mapToObj(j -> new Clan(random.nextInt(999) + 1, random.nextInt(1000000)))
                .collect(Collectors.toCollection(() -> new ArrayList<>(20000)));
        return new Game(1000, clans.toArray(Clan[]::new));
    }).toList();

    @Test
    public void testGameMultithreaded() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<Void>> futures = new ArrayList<>();

        ConcurrentLinkedQueue<Long> queue = new ConcurrentLinkedQueue<>();
        DslJson<Object> json = new DslJson<>();
        for (Game game : GAMES) {
            JsonWriter writer = json.newWriter();
            json.serialize(writer, game);
            futures.add(executor.submit(() -> {
                URL url = new URL("http://localhost:8080/onlinegame/calculate");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setDoOutput(true);

                long start = System.currentTimeMillis();

                try (OutputStream os = new BufferedOutputStream(con.getOutputStream())) {
                    writer.toStream(os);
                }

                try (InputStream isr = con.getInputStream()) {
                    isr.readAllBytes();
                    queue.add(System.currentTimeMillis() - start);
                }
                return null;
            }));
        }
        waitForAllTasks(futures);
        ArrayList<Long> longs = new ArrayList<>(queue);
        longs.sort(null);
        LOGGER.info("90% line " + longs.get((int) (longs.size() * 0.9)) + "ms");
    }

    @ParameterizedTest
    @ValueSource(strings = {"atms1", "atms2", "atms_big"})
    public void testAtms(String name) throws Exception {
        doRequest("atms/calculateOrder", name + "_request.json", name + "_response.json");
    }

    @Test
    public void testAtmsMultithreaded() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<Void>> futures = new ArrayList<>();
        InputStream is = getClass().getClassLoader().getResourceAsStream("atms_big_request.json");
        byte[] request = is.readAllBytes();
        is = getClass().getClassLoader().getResourceAsStream("atms_big_response.json");
        byte[] response = is.readAllBytes();
        ConcurrentLinkedQueue<Long> queue = new ConcurrentLinkedQueue<>();
        for (int i = 0; i < 1000; i++) {
            futures.add(executor.submit(() -> {
                URL url = new URL("http://localhost:8080/atms/calculateOrder");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setDoOutput(true);

                long start = System.currentTimeMillis();

                try (OutputStream os = con.getOutputStream()) {
                    os.write(request);
                }

                try (InputStream isr = con.getInputStream()) {
                    byte[] actual = isr.readAllBytes();
                    queue.add(System.currentTimeMillis() - start);
//                    Assertions.assertArrayEquals(actual, response);
                }
                return null;
            }));
        }
        waitForAllTasks(futures);
        ArrayList<Long> longs = new ArrayList<>(queue);
        longs.sort(null);
        LOGGER.info("90% line " + longs.get((int) (longs.size() * 0.9)) + "ms");
    }

    private static void waitForAllTasks(List<Future<Void>> futures) throws InterruptedException, ExecutionException {
        for (Future<Void> future : futures) {
            future.get();
        }
    }

    private void doRequest(String path, String requestFile, String responseFile) throws IOException {
        URL url = new URL(String.format("http://localhost:8080/%s", path));
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);

        try (OutputStream os = con.getOutputStream();
             InputStream is = Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(requestFile))) {
            is.transferTo(os);
        }

        byte[] response;
        try {
            response = con.getInputStream().readAllBytes();
        } catch (Exception e) {
            LOGGER.error(new String(con.getErrorStream().readAllBytes(), StandardCharsets.UTF_8));
            throw e;
        }


        try (InputStream is = Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(responseFile))) {
            byte[] expected = is.readAllBytes();
            Assertions.assertArrayEquals(expected, response);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"transactions1", "transactions_big"})
    public void testTransactions(String name) throws Exception {
        doRequest("transactions/report", name + "_request.json", name + "_response.json");
    }

    @Test
    public void testTransactionsMultithreaded() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<Void>> futures = new ArrayList<>();
        byte[] request;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("transactions_big_request.json")) {
            request = Objects.requireNonNull(is).readAllBytes();
        }
        ConcurrentLinkedQueue<Long> queue = new ConcurrentLinkedQueue<>();

        for (int i = 0; i < 1000; i++) {
            futures.add(executor.submit(() -> {
                URL url = new URL("http://localhost:8080/transactions/report");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setDoOutput(true);

                long start = System.currentTimeMillis();
                try (OutputStream os = con.getOutputStream()) {
                    os.write(request);
                }

                try (InputStream isr = con.getInputStream()) {
                    isr.readAllBytes();
                }
                queue.add(System.currentTimeMillis() - start);
                return null;
            }));
        }
        waitForAllTasks(futures);
        ArrayList<Long> longs = new ArrayList<>(queue);
        longs.sort(null);
        LOGGER.info("90% line " + longs.get((int) (longs.size() * 0.9)) + "ms");
    }

    @Test
    public void generateAtmsBig() throws Exception {
        List<Request> requests = new ArrayList<>();
        Request.RequestType[] values = Request.RequestType.values();
        for (int i = 0; i < 1000; i++) {
            for (int j = 0; j < 1000; j++) {
                int region = random.nextInt(9999) + 1;
                int atmId = random.nextInt(9999) + 1;
                Request.RequestType requestType = values[random.nextInt(4)];
                requests.add(new Request(region, atmId, requestType));
            }
        }
        DslJson<?> json = new DslJson<>();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        json.serialize(requests, baos);
        byte[] bytes = baos.toByteArray();
        ByteBuf buf = new AtmHandler().handle(new ByteArrayInputStream(bytes));
        Files.write(Path.of("src", "integration", "resources", "atms_big_request.json"), bytes, StandardOpenOption.TRUNCATE_EXISTING);
        Files.write(Path.of("src", "integration", "resources", "atms_big_response.json"), buf.asArray(), StandardOpenOption.TRUNCATE_EXISTING);
    }
}
