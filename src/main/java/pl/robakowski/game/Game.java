package pl.robakowski.game;

import com.dslplatform.json.CompiledJson;

import java.util.ArrayList;
import java.util.List;

@CompiledJson
public record Game(int groupCount, List<Clan> clans) {

    public List<? extends List<Clan>> getOrder() {
        var groups = new ArrayList<Group>();
//        clans.stream().sorted().forEach();
        return groups;
    }
}
