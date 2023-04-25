package pl.robakowski.game;

import com.dslplatform.json.JsonWriter;
import pl.robakowski.Handler;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class GameHandler extends Handler {

    private static final JsonWriter.WriteObject<Clan> clanWriter = Objects.requireNonNull(json.tryFindWriter(Clan.class));

    static {
        json.registerWriter(Group.class, GameHandler::writeGroup);
    }

    private static void writeGroup(JsonWriter writer, Group value) {
        writer.serialize(value != null ? value.getClans() : Collections.emptyList(), clanWriter);
    }

    @Override
    protected void handle(InputStream is, JsonWriter writer) throws Exception {
        Game game = json.deserialize(Game.class, is);
        if (game == null) {
            game = new Game(0, new Clan[0]);
        }

        List<Group> groups = new LinkedList<>();
        Clan[] clans = game.clans();
        Arrays.sort(clans);
        int size = clans.length;
        while (size > 0) {
            int available = game.groupCount();
            Group group = new Group();
            groups.add(group);
            for (int i = 0; i < size; i++) {
                Clan next = clans[i];
                if (next.numberOfPlayers() <= available) {
                    final int newSize;
                    if ((newSize = size - 1) > i) {
                        System.arraycopy(clans, i + 1, clans, i, newSize - i);
                    }
                    size = newSize;
                    i--;
                    group.add(next);
                    available -= next.numberOfPlayers();
                }
            }
        }

        json.serialize(writer, groups);
    }
}
