package pl.robakowski.atms;

import com.dslplatform.json.CompiledJson;

@CompiledJson
public class Atm {

    private final int region;
    private final int atmId;

    public Atm(int region, int atmId) {
        this.region = region;
        this.atmId = atmId;
    }

    public int getRegion() {
        return region;
    }

    public int getAtmId() {
        return atmId;
    }
}
