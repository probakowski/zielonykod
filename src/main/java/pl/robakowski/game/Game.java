package pl.robakowski.game;

import com.dslplatform.json.CompiledJson;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@CompiledJson
public record Game(int groupCount, List<Clan> clans) {

    private static final ThreadLocal<ArrayList<Group>> group = ThreadLocal.withInitial(() -> new ArrayList<>(20000));

    public List<Group> getOrder() {
        var groups = new LinkedList<Group>();
        var allGroups = group.get();
        allGroups.clear();
        clans.sort(null);
        for (Clan clan : clans) {
            var shouldCreateGroup = true;
            var it = groups.iterator();
            while (it.hasNext()) {
                Group next = it.next();
                if (next.getCapacity() >= clan.numberOfPlayers()) {
                    shouldCreateGroup = false;
                    next.add(clan);
                    if (next.getCapacity() == 0) {
                        it.remove();
                    }
                    break;
                }
            }
            if (shouldCreateGroup) {
                Group group = new Group(groupCount);
                allGroups.add(group);
                group.add(clan);
                if (group.getCapacity() > 0) {
                    groups.add(group);
                }
            }
        }

        return allGroups;
    }
}
