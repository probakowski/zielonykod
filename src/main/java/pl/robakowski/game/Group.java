package pl.robakowski.game;

import java.util.LinkedList;
import java.util.List;

public class Group {

    private final List<Clan> clans = new LinkedList<>();

    public void add(Clan clan) {
        clans.add(clan);
    }

    public List<Clan> getClans() {
        return clans;
    }
}
