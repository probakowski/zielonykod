package pl.robakowski;

import io.activej.bytebuf.ByteBuf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import pl.robakowski.atms.AtmHandler;
import pl.robakowski.game.GameHandler;
import pl.robakowski.transactions.TransactionsHandler;

import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HandlerTest {

    @ParameterizedTest
    @ValueSource(strings = {"example1", "example2"})
    public void atmHandlerReturnsCorrectValues(String name) throws Exception {
        runTest("atms/" + name, new AtmHandler());
    }

    @ParameterizedTest
    @ValueSource(strings = {"example1"})
    public void transactionsHandlerReturnsCorrectValues(String name) throws Exception {
        runTest("transactions/" + name, new TransactionsHandler());
    }

    @ParameterizedTest
    @ValueSource(strings = {"example1"})
    public void gameHandlerReturnsCorrectValues(String name) throws Exception {
        runTest("game/" + name, new GameHandler());
    }

    private void runTest(String prefix, Handler handler) throws Exception {
        ClassLoader cl = Handler.class.getClassLoader();
        try (InputStream req = cl.getResourceAsStream(prefix + "_request.json");
             InputStream res = cl.getResourceAsStream(prefix + "_response.json")) {
            assertNotNull(req);
            assertNotNull(res);
            ByteBuf buf = handler.handle(req);
            assertEquals(new String(res.readAllBytes(), UTF_8), buf.asString(UTF_8));
        }
    }
}
