package pl.robakowski.game;

import java.util.LinkedList;
import java.util.List;

public class Group {

    private final List<Clan> clans;

    public Group(int capacity) {
        this.capacity = capacity;
        this.clans = new LinkedList<>();
    }

    public Group(int capacity, List<Clan> clans) {
        this.capacity = capacity;
        this.clans = new LinkedList<>(clans);
    }

    public int getCapacity() {
        return capacity;
    }

    private int capacity;

    public void add(Clan clan) {
        capacity -= clan.numberOfPlayers();
        clans.add(clan);
    }

    public List<Clan> getClans() {
        return clans;
    }
}
