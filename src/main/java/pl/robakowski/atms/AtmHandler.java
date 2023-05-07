package pl.robakowski.atms;

import com.dslplatform.json.JsonWriter;
import pl.robakowski.Handler;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class AtmHandler extends Handler {

    private static final int REQUEST_TYPES_COUNT = 4;
    private static final int MAX_REGION_NUMBER = 9999;
    private static final int BUCKETS_COUNT = REQUEST_TYPES_COUNT * (MAX_REGION_NUMBER + 1);

    @Override
    protected void handle(InputStream is, JsonWriter writer) throws Exception {
        Iterator<Request> requests = json.iterateOver(Request.class, is);
        int requestsCount = 0;
        if (requests == null) {
            writer.writeAscii("[]");
            return;
        }

        //sort requests using bucket sort, one bucket per possible region and request type combination
        List<Atm>[] buckets = new List[BUCKETS_COUNT];
        while (requests.hasNext()) {
            requestsCount++;
            Request request = requests.next();
            //sort by region and then request type
            int region = request.region() * REQUEST_TYPES_COUNT + request.requestType().ordinal();
            if (buckets[region] == null) {
                buckets[region] = new ArrayList<>();
            }
            buckets[region].add(new Atm(request.region(), request.atmId()));
        }

        //for each region store only first appearance of any atm id
        List<Atm> atms = new ArrayList<>(requestsCount);
        for (int i = 0; i < buckets.length; i += REQUEST_TYPES_COUNT) {
            BitSet visited = new BitSet();
            for (int j = 0; j < REQUEST_TYPES_COUNT; j++) {
                if (buckets[i + j] != null) {
                    for (Atm atm : buckets[i + j]) {
                        if (visited.set(atm.atmId())) {
                            atms.add(atm);
                        }
                    }
                }
            }
        }
        json.serialize(writer, atms);
    }
}
