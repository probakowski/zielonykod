package pl.robakowski.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Group {

    private final List<Clan> clans = new ArrayList<>();

    public void add(Clan clan) {
        clans.add(clan);
    }

    public List<Clan> getClans() {
        return Collections.unmodifiableList(clans);
    }
}
