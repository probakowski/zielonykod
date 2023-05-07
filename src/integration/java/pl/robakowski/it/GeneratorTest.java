package pl.robakowski.it;

import com.dslplatform.json.DslJson;
import io.activej.bytebuf.ByteBuf;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import pl.robakowski.atms.AtmHandler;
import pl.robakowski.atms.Request;
import pl.robakowski.game.Clan;
import pl.robakowski.game.Game;
import pl.robakowski.game.GameHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class GeneratorTest {

    @Disabled("only used for data generation")
    @Test
    public void generateAtmsBig() throws Exception {
        Random random = new SecureRandom();
        List<Request> requests = new ArrayList<>();
        Request.RequestType[] values = Request.RequestType.values();
        for (int i = 0; i < 500000; i++) {
            int region = random.nextInt(9999) + 1;
            int atmId = random.nextInt(9999) + 1;
            Request.RequestType requestType = values[random.nextInt(4)];
            requests.add(new Request(region, atmId, requestType));
        }
        DslJson<?> json = new DslJson<>();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        json.serialize(requests, baos);
        byte[] bytes = baos.toByteArray();
        ByteBuf buf = new AtmHandler().handle(new ByteArrayInputStream(bytes));
        Files.write(Path.of("src", "integration", "resources", "atms_big2_request.json"), bytes, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        Files.write(Path.of("src", "integration", "resources", "atms_big2_response.json"), buf.asArray(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
    }

    @Disabled("only used for data generation")
    @Test
    public void generateGamesBig() {
        DslJson<Object> json = new DslJson<>();
        Random random = new SecureRandom();
        IntStream.range(0, 1000).parallel().forEach(i -> {
            List<Clan> clans = IntStream.range(0, 20000)
                    .mapToObj($ -> new Clan(random.nextInt(1000), random.nextInt(100000)))
                    .toList();
            Game game = new Game(1000, clans);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                json.serialize(game, baos);
                byte[] bytes = baos.toByteArray();
                ByteBuf buf = new GameHandler().handle(new ByteArrayInputStream(bytes));
                Files.write(Path.of("src", "integration", "resources", "games", "game" + i + "_request.json"), bytes, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
                Files.write(Path.of("src", "integration", "resources", "games", "game" + i + "_response.json"), buf.asArray(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
