package pl.robakowski.game;

import com.dslplatform.json.CompiledJson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@CompiledJson
public record Game(int groupCount, List<Clan> clans) {

    public Game(int groupCount, List<Clan> clans) {
        this.groupCount = groupCount;
        this.clans = new ArrayList<>(clans);
        this.clans.sort(null);
    }

    @Override
    public List<Clan> clans() {
        return Collections.unmodifiableList(clans);
    }
}
