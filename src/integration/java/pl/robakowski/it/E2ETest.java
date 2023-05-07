package pl.robakowski.it;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.robakowski.Launcher;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class E2ETest {

    private static final Launcher launcher = new Launcher();
    private static final Logger LOGGER = LoggerFactory.getLogger(E2ETest.class);

    @BeforeAll
    public static void startServer() throws Exception {
        if (!System.getProperty("start_server", "true").equals("false")) {
            new Thread(() -> {
                try {
                    launcher.launch(new String[0]);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).start();
            launcher.getStartFuture().toCompletableFuture().get();
        }
    }

    @AfterAll
    public static void stopServer() {
        launcher.shutdown();
    }

    @ParameterizedTest
    @ValueSource(strings = {"game1", "game_big"})
    public void testGame(String name) throws Exception {
        doRequest("onlinegame/calculate", name);
    }

    @Test
    public void testGameMultithreaded() throws Exception {
        testMultithreaded("onlinegame/calculate", "game_big");
    }

    @ParameterizedTest
    @ValueSource(strings = {"atms1", "atms2", "atms_big"})
    public void testAtms(String name) throws Exception {
        doRequest("atms/calculateOrder", name);
    }

    @Test
    public void testAtmsMultithreaded() throws Exception {
        testMultithreaded("atms/calculateOrder", "atms_big");
    }

    @Test
    public void testAtmsMultithreaded2() throws Exception {
        testMultithreaded("atms/calculateOrder", "atms_big2");
    }

    @ParameterizedTest
    @ValueSource(strings = {"transactions1", "transactions_big"})
    public void testTransactions(String name) throws Exception {
        doRequest("transactions/report", name);
    }

    @Test
    public void testTransactionsMultithreaded() throws Exception {
        testMultithreaded("transactions/report", "transactions_big");
    }

    private void testMultithreaded(String path, String name) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<Long>> futures = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            futures.add(executor.submit(() -> doRequest(path, name)));
        }
        List<Long> longs = waitForAllTasks(futures);
        LOGGER.info("90% line for " + name + ":" + longs.get((int) (longs.size() * 0.9)) + "ms");
    }

    private static List<Long> waitForAllTasks(List<Future<Long>> futures) throws Exception {
        List<Long> timings = new ArrayList<>(futures.size());
        for (Future<Long> future : futures) {
            timings.add(future.get());
        }
        timings.sort(null);
        return timings;
    }

    private long doRequest(String path, String name) throws IOException {
        String requestFile = name + "_request.json";
        String responseFile = name + "_response.json";
        URL url = new URL(String.format("http://localhost:8080/%s", path));
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);

        long start;
        try (OutputStream os = con.getOutputStream();
             InputStream is = Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(requestFile))) {
            byte[] req = is.readAllBytes();
            start = System.currentTimeMillis();
            os.write(req);
        }

        byte[] response;
        try {
            response = con.getInputStream().readAllBytes();
        } catch (Exception e) {
            LOGGER.error(new String(con.getErrorStream().readAllBytes(), StandardCharsets.UTF_8));
            throw e;
        }
        long time = System.currentTimeMillis() - start;


        try (InputStream is = Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(responseFile))) {
            byte[] expected = is.readAllBytes();
            if(Arrays.compare(expected, response)!=0){
                Files.write(Path.of("build/expected.json"), expected, StandardOpenOption.CREATE_NEW);
                Files.write(Path.of("build/response.json"), response, StandardOpenOption.CREATE_NEW);
            }
            Assertions.assertArrayEquals(expected, response);
        }
        return time;
    }


}
