package pl.robakowski.atms;

import com.dslplatform.json.CompiledJson;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Atm atm = (Atm) o;
        return region == atm.region && atmId == atm.atmId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(region, atmId);
    }
}
