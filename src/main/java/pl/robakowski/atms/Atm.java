package pl.robakowski.atms;

import com.dslplatform.json.CompiledJson;

@CompiledJson
public record Atm(int region, int atmId) {

}
