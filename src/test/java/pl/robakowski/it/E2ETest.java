package pl.robakowski.it;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonWriter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import pl.robakowski.Launcher;
import pl.robakowski.atms.Atm;
import pl.robakowski.atms.Request;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class E2ETest {

    private static final Launcher launcher = new Launcher();

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

        for (int i = 0; i < 1000; i++) {
            futures.add(executor.submit(() -> {
                URL url = new URL("http://localhost:8080/atms/calculateOrder");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setDoOutput(true);

                try (OutputStream os = con.getOutputStream()) {
                    os.write(request);
                }

                try (InputStream isr = con.getInputStream()) {
                    byte[] actual = isr.readAllBytes();
                    Assertions.assertArrayEquals(actual, response);
                }
                return null;
            }));
        }
        for (Future<Void> future : futures) {
            future.get();
        }
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

    @Test
    public void testGenerate() throws Exception {
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
}
