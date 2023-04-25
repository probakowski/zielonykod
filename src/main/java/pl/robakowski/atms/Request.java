package pl.robakowski.atms;

import com.dslplatform.json.CompiledJson;

@CompiledJson
public record Request(int region, int atmId, RequestType requestType) {

    @CompiledJson
    public enum RequestType {
        FAILURE_RESTART, PRIORITY, SIGNAL_LOW, STANDARD
    }
}
