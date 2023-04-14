package pl.robakowski.atms;

import com.dslplatform.json.CompiledJson;
import org.jetbrains.annotations.NotNull;

@CompiledJson
public class Request extends Atm implements Comparable<Request> {

    private final RequestType requestType;

    public Request(int region, int atmId, RequestType requestType) {
        super(region, atmId);
        this.requestType = requestType;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    @Override
    public int compareTo(@NotNull Request request) {
        int diff = getRegion() - request.getRegion();
        if (diff != 0) {
            return diff;
        }

        diff = requestType.ordinal() - request.requestType.ordinal();

        return diff == 0 ? request.getAtmId() - getAtmId() : diff;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @CompiledJson
    public enum RequestType {
        FAILURE_RESTART, PRIORITY, SIGNAL_LOW, STANDARD
    }
}
