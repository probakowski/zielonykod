package pl.robakowski.atms;

import com.dslplatform.json.JsonWriter;
import pl.robakowski.Handler;

import java.io.InputStream;
import java.util.*;

public class AtmHandler extends Handler {

    private static final JsonWriter.WriteObject<Atm> atmWriter = Objects.requireNonNull(json.tryFindWriter(Atm.class));

    @Override
    protected void handle(InputStream is, JsonWriter writer) throws Exception {
        List<Request> requests = json.deserializeList(Request.class, is);
        if (requests == null) {
            requests = Collections.emptyList();
        }
        Collections.sort(requests);
        HashSet<Integer> visited = new HashSet<>();
        List<Atm> atms = new ArrayList<>(requests.size());
        for (Request request : requests) {
            if (visited.add(request.getRegion() * 10000 + request.getAtmId())) {
                atms.add(request);
            }
        }
        writer.serialize(atms, atmWriter);
    }
}
