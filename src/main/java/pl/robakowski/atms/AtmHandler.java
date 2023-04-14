package pl.robakowski.atms;

import com.dslplatform.json.JsonWriter;
import pl.robakowski.Handler;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class AtmHandler extends Handler {

    private static final JsonWriter.WriteObject<Atm> atmWriter = Objects.requireNonNull(json.tryFindWriter(Atm.class));

    @Override
    protected void handle(InputStream is, JsonWriter writer) throws Exception {
        json.registerWriter(Request.RequestType.class, null);
        List<Request> requests = json.deserializeList(Request.class, is);
        if (requests == null) {
            requests = Collections.emptyList();
        }
        Collections.sort(requests);
        List<? extends Atm> atms = requests;
        writer.serialize((List<Atm>) atms, atmWriter);
    }
}
