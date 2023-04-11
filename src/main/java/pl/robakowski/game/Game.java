package pl.robakowski.game;

import com.dslplatform.json.CompiledJson;

import java.util.LinkedList;
import java.util.List;

@CompiledJson
public record Game(int groupCount, List<Clan> clans) {

    public List<Group> getOrder() {
        var groups = new LinkedList<Group>();
        clans.sort(null);
        Clan[] ac = clans.toArray(Clan[]::new);
        int size = ac.length;
        while (size > 0) {
            int available = groupCount;
            Group e = new Group(groupCount);
            groups.add(e);
            for (int i = 0; i < size; i++) {
                Clan next = ac[i];
                if (next.numberOfPlayers() <= available) {
                    final int newSize;
                    if ((newSize = size - 1) > i) {
                        System.arraycopy(ac, i + 1, ac, i, newSize - i);
                    }
                    size = newSize;
                    i--;
                    e.add(next);
                    available -= next.numberOfPlayers();
                }
            }
        }

        return groups;
    }
}
