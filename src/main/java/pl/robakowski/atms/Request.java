package pl.robakowski.atms;

import com.dslplatform.json.CompiledJson;

@CompiledJson
public class Request extends Atm {

    private final RequestType requestType;

    public Request(int region, int atmId, RequestType requestType) {
        super(region, atmId);
        this.requestType = requestType;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    @CompiledJson
    public enum RequestType {
        FAILURE_RESTART, PRIORITY, SIGNAL_LOW, STANDARD
    }
}
