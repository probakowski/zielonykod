package pl.robakowski.game;

import com.dslplatform.json.JsonWriter;
import pl.robakowski.Handler;
import pl.robakowski.game._Clan_DslJsonConverter.ObjectFormatConverter;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameHandler extends Handler {

    private static final ObjectFormatConverter CLAN_CONVERTER = new ObjectFormatConverter(json);

    static {
        json.registerWriter(Group.class, GameHandler::writeGroup);
    }

    private static void writeGroup(JsonWriter writer, Group value) {
        writer.serialize(value != null ? value.getClans() : Collections.emptyList(), CLAN_CONVERTER);
    }

    @Override
    protected void handle(InputStream is, JsonWriter writer) throws Exception {
        Game game = json.deserialize(Game.class, is);
        if (game == null) {
            game = new Game(0, Collections.emptyList());
        }

        int groupCount = game.groupCount();
        List<Clan> clans = game.clans();

        ArrayList<Group> groups = new ArrayList<>();
        int[] capacity = new int[clans.size()];
        int firstFree = 0;
        for (Clan clan : clans) {
            boolean shouldAdd = true;
            int size = groups.size();
            for (int i = firstFree; i < size; i++) {
                int numberOfPlayers = clan.numberOfPlayers();
                if (capacity[i] >= numberOfPlayers) {
                    groups.get(i).add(clan);
                    capacity[i] -= numberOfPlayers;
                    //move start to first group with capacity left
                    while (firstFree < size && capacity[firstFree] <= 0) {
                        firstFree++;
                    }
                    shouldAdd = false;
                    break;
                }
            }
            if (shouldAdd) {
                Group group = new Group();
                groups.add(group);
                capacity[size] = groupCount - clan.numberOfPlayers();
                group.add(clan);
            }
        }

        json.serialize(writer, groups);
    }
}
