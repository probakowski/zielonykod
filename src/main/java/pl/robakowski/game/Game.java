package pl.robakowski.game;

import com.dslplatform.json.CompiledJson;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@CompiledJson
public record Game(int groupCount, Clan[] clans) {

    public List<Group> getOrder() {
        var groups = new LinkedList<Group>();
        Arrays.sort(clans);
        int size = clans.length;
        while (size > 0) {
            int available = groupCount;
            Group e = new Group(groupCount);
            groups.add(e);
            for (int i = 0; i < size; i++) {
                Clan next = clans[i];
                if (next.numberOfPlayers() <= available) {
                    final int newSize;
                    if ((newSize = size - 1) > i) {
                        System.arraycopy(clans, i + 1, clans, i, newSize - i);
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
