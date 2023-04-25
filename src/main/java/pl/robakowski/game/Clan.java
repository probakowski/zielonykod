package pl.robakowski.game;

import com.dslplatform.json.CompiledJson;
import org.jetbrains.annotations.NotNull;

@CompiledJson
public record Clan(int numberOfPlayers, int points) implements Comparable<Clan> {

    @Override
    public int compareTo(@NotNull Clan clan) {
        int diff = clan.points - points;
        if (diff != 0) {
            return diff;
        }
        return numberOfPlayers - clan.numberOfPlayers;
    }
}
