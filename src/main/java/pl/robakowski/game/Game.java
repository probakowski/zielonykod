package pl.robakowski.game;

import com.dslplatform.json.CompiledJson;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@CompiledJson
public record Game(int groupCount, List<Clan> clans) {

    public List<Group> getOrder() {
        var groups = new LinkedList<Group>();
        clans.sort(null);
        while (!clans.isEmpty()) {
            int available = groupCount;
            Group e = new Group(groupCount);
            groups.add(e);
            Iterator<Clan> it = clans.iterator();
            while (it.hasNext() && available > 0) {
                Clan next = it.next();
                if (next.numberOfPlayers() <= available) {
                    it.remove();
                    e.add(next);
                    available -= next.numberOfPlayers();
                }
            }
        }

        return groups;
    }
}
