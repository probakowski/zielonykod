package pl.robakowski.game;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

import java.util.LinkedList;

public class Group extends LinkedList<Clan> {

    public Group(int capacity) {
        this.capacity = capacity;
    }

    public int getCapacity() {
        return capacity;
    }

    @JsonAttribute(ignore = true)
    private int capacity;

    @Override
    public boolean offer(Clan clan) {
        return super.offer(clan);
    }
}
