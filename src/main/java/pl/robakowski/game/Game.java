package pl.robakowski.game;

import com.dslplatform.json.CompiledJson;

@CompiledJson
public record Game(int groupCount, Clan[] clans) {

    public Game(int groupCount, Clan[] clans) {
        this.groupCount = groupCount;
        if (clans == null) {
            this.clans = new Clan[0];
        } else {
            this.clans = copy(clans);
        }
    }

    @Override
    public Clan[] clans() {
        return copy(clans);
    }

    private Clan[] copy(Clan[] clans) {
        Clan[] newClans = new Clan[clans.length];
        System.arraycopy(clans, 0, newClans, 0, clans.length);
        return newClans;
    }
}
