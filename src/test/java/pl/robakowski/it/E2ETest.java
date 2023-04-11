package pl.robakowski.it;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonWriter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.robakowski.Launcher;
import pl.robakowski.atms.Atm;
import pl.robakowski.atms.Request;
import pl.robakowski.game.Clan;
import pl.robakowski.game.Game;
import pl.robakowski.game.Group;
import pl.robakowski.transactions.Amount;
import pl.robakowski.transactions.Transaction;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class E2ETest {

    private static final Launcher launcher = new Launcher();
    public static final Logger LOGGER = LoggerFactory.getLogger(E2ETest.class);

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
        doRequest("/onlinegame/calculate", name + "_request.json", name + "_response.json");
    }

    @Test
    public void testGameMultithreaded() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<Void>> futures = new ArrayList<>();
        InputStream is = getClass().getClassLoader().getResourceAsStream("game_big_request.json");
        byte[] request = is.readAllBytes();
        is = getClass().getClassLoader().getResourceAsStream("game_big_response.json");
        byte[] response = is.readAllBytes();
        ConcurrentLinkedQueue<Long> queue = new ConcurrentLinkedQueue<>();
        for (int i = 0; i < 1000; i++) {
            futures.add(executor.submit(() -> {
                URL url = new URL("http://localhost:8080/onlinegame/calculate");
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
        for (Future<Void> future : futures) {
            future.get();
        }
        ArrayList<Long> longs = new ArrayList<>(queue);
        longs.sort(null);
        LOGGER.info("90% line " + longs.get((int) (longs.size() * 0.9)) + "ms");
    }

    @ParameterizedTest
    @ValueSource(strings = {"atms1", "atms2", "atms_big"})
    public void testAtms(String name) throws Exception {
        doRequest("/atms/calculateOrder", name + "_request.json", name + "_response.json");
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
        for (int i = 0; i < 10000; i++) {
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
                    Assertions.assertArrayEquals(actual, response);
                }
                return null;
            }));
        }
        for (Future<Void> future : futures) {
            future.get();
        }
        ArrayList<Long> longs = new ArrayList<>(queue);
        longs.sort(null);
        LOGGER.info("90% line " + longs.get((int) (longs.size() * 0.9)) + "ms");
    }

    private void doRequest(String path, String requestFile, String responseFile) throws IOException {
        URL url = new URL("http://localhost:8080" + path);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);

        try (OutputStream os = con.getOutputStream();
             InputStream is = getClass().getClassLoader().getResourceAsStream(requestFile)) {
            is.transferTo(os);
        }
        byte[] response = con.getInputStream().readAllBytes();

        try (InputStream is = getClass().getClassLoader().getResourceAsStream(responseFile)) {
            byte[] expected = is.readAllBytes();
            Assertions.assertArrayEquals(expected, response);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"transactions1", "transactions_big"})
    public void testTransactions(String name) throws Exception {
        doRequest("/transactions/report", name + "_request.json", name + "_response.json");
    }

    @Test
    public void testTransactionsMultithreaded() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<Void>> futures = new ArrayList<>();
        InputStream is = getClass().getClassLoader().getResourceAsStream("transactions_big_request.json");
        byte[] request = is.readAllBytes();
        ConcurrentLinkedQueue<Long> queue = new ConcurrentLinkedQueue<>();

        for (int i = 0; i < 1000; i++) {
            futures.add(executor.submit(() -> {
                URL url = new URL("http://localhost:8080/transactions/report");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
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
        for (Future<Void> future : futures) {
            future.get();
        }
        ArrayList<Long> longs = new ArrayList<>(queue);
        longs.sort(null);
        LOGGER.info("90% line " + longs.get((int) (longs.size() * 0.9)) + "ms");
    }

    //@Test
    public void testGenerateAtms() throws Exception {
        DslJson<Object> json = new DslJson<>();
        List<Atm> requests = new ArrayList<>(100000000);
        for (int i = 0; i < 1000; i++) {
            for (int j = 0; j < 100; j++) {
                requests.add(new Request(i, j, Request.RequestType.STANDARD));
            }
        }
        try (OutputStream os = new FileOutputStream("atms_big_request.json")) {
            json.serialize(requests, os);
        }
        requests.clear();
        for (int i = 0; i < 1000; i++) {
            for (int j = 99; j >= 0; j--) {
                requests.add(new Request(i, j, Request.RequestType.STANDARD));
            }
        }
        try (OutputStream os = new FileOutputStream("atms_big_response.json")) {
            JsonWriter jsonWriter = json.newWriter();
            jsonWriter.reset(os);
            jsonWriter.serialize(requests, json.tryFindWriter(Atm.class));
        }
    }

    //    @Test
    public void testGenerateGames() throws Exception {
        DslJson<Object> json = new DslJson<>();
        json.registerWriter(Group.class, (writer, value) -> writer.serialize(value.getClans(), json.tryFindWriter(Clan.class)));
        List<Clan> clans = new ArrayList<>(20000);
        Random random = new Random();
        int groupSize = random.nextInt(1001);
        for (int i = 0; i < 20000; i++) {
            clans.add(new Clan(random.nextInt(groupSize), random.nextInt(1000000)));
        }
        Game game = new Game(groupSize, clans);
        try (OutputStream os = new FileOutputStream("game_big_request.json")) {
            json.serialize(game, os);
        }
        try (OutputStream os = new FileOutputStream("game_big_response.json")) {
            JsonWriter jsonWriter = json.newWriter();
            jsonWriter.reset(os);
            json.serialize(game.getOrder(), os);
        }
    }

    //    @Test
    public void testGenerateGamesTricky() throws Exception {
        DslJson<Object> json = new DslJson<>();
        json.registerWriter(Group.class, (writer, value) -> writer.serialize(value.getClans(), json.tryFindWriter(Clan.class)));
        List<Clan> clans = new ArrayList<>(20000);
        for (int i = 0; i < 20000; i++) {
            clans.add(new Clan(2, 1000000));
        }
        Game game = new Game(3, clans);
        try (OutputStream os = new FileOutputStream("game_big_request.json")) {
            json.serialize(game, os);
        }
        try (OutputStream os = new FileOutputStream("game_big_response.json")) {
            JsonWriter jsonWriter = json.newWriter();
            jsonWriter.reset(os);
            json.serialize(game.getOrder(), os);
        }
    }

    @Test
    public void testGenerateTransaction() throws Exception {
        DslJson<Object> json = new DslJson<>();
        String[] accounts = new String[50000];
        Random random = new Random();
        for (int i = 0; i < 50000; i++) {
            accounts[i] = random.ints('0', '9' + 1)
                    .limit(26)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();
        }
        List<Transaction> transactions = new ArrayList<>(100000);
        for (int i = 0; i < 100000; i++) {
            BigDecimal amount = new BigDecimal(BigInteger.valueOf(random.nextInt(100000)), 2);
            transactions.add(new Transaction(accounts[random.nextInt(50000)], accounts[random.nextInt(50000)], new Amount(0, amount)));
        }
        try (OutputStream os = new FileOutputStream("transactions_big_request.json")) {
            json.serialize(transactions, os);
        }
    }
}
