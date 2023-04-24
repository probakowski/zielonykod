package pl.robakowski.atms;

import com.dslplatform.json.JsonWriter;
import pl.robakowski.Handler;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;


public class AtmHandler extends Handler {

    private static final JsonWriter.WriteObject<Atm> atmWriter = Objects.requireNonNull(json.tryFindWriter(Atm.class));
    private static final int REQUEST_TYPES_COUNT = 4;
    private static final int MAX_REGION_NUMBER = 9999;
    private static final int BUCKETS_COUNT = REQUEST_TYPES_COUNT * (MAX_REGION_NUMBER + 1);

    @Override
    protected void handle(InputStream is, JsonWriter writer) throws Exception {
        List<Atm>[] buckets = new List[BUCKETS_COUNT];
        Iterator<Request> requests = json.iterateOver(Request.class, is);
        int requestsCount = 0;
        if (requests == null) {
            writer.writeAscii("[]");
            return;
        }
        while (requests.hasNext()) {
            requestsCount++;
            Request request = requests.next();
            int region = request.getRegion() * REQUEST_TYPES_COUNT + request.getRequestType().ordinal();
            if (buckets[region] == null) {
                buckets[region] = new ArrayList<>();
            }
            buckets[region].add(request);
        }
        List<Atm> atms = new ArrayList<>(requestsCount);
        for (int i = 0; i < buckets.length; i += REQUEST_TYPES_COUNT) {
            BitSet visited = new BitSet();
            for (int j = 0; j < REQUEST_TYPES_COUNT; j++) {
                if (buckets[i + j] != null) {
                    for (Atm atm : buckets[i + j]) {
                        if (visited.set(atm.getAtmId())) {
                            atms.add(atm);
                        }
                    }
                }
            }
        }
        writer.serialize(atms, atmWriter);
    }
}
