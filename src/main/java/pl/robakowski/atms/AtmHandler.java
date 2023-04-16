package pl.robakowski.atms;

import com.dslplatform.json.JsonWriter;
import pl.robakowski.Handler;

import java.io.InputStream;
import java.util.*;

public class AtmHandler extends Handler {

    private static final JsonWriter.WriteObject<Atm> atmWriter = Objects.requireNonNull(json.tryFindWriter(Atm.class));
    private static final int requestTypesCount = Request.RequestType.values().length;
    private static final int maxRegionNumber = 9999;
    private static final int bucketsCount = requestTypesCount * (maxRegionNumber + 1);

    @Override
    protected void handle(InputStream is, JsonWriter writer) throws Exception {
        List<Atm>[] buckets = new List[bucketsCount];
        Iterator<Request> requests = json.iterateOver(Request.class, is);
        int i = 0;
        if (requests == null) {
            requests = Collections.emptyIterator();
        }
        while (requests.hasNext()) {
            i++;
            Request request = requests.next();
            int region = request.getRegion() * requestTypesCount + request.getRequestType().ordinal();
            if (buckets[region] == null) {
                buckets[region] = new ArrayList<>();
            }
            buckets[region].add(request);
        }
        int estimatedAtmsPerRegion = i / maxRegionNumber;
        List<Atm> atms = new ArrayList<>(i);
        for (int j = 0; j < buckets.length; j += requestTypesCount) {
            HashSet<Atm> visited = new HashSet<>(estimatedAtmsPerRegion);
            if (buckets[j] != null) {
                atms.addAll(buckets[j]);
                visited.addAll(buckets[j]);
            }
            for (int k = 1; k < requestTypesCount; k++) {
                if (buckets[j + k] != null) {
                    for (Atm atm : buckets[j + k]) {
                        if (visited.add(atm)) {
                            atms.add(atm);
                        }
                    }
                }
            }
        }
        writer.serialize(atms, atmWriter);
    }
}
