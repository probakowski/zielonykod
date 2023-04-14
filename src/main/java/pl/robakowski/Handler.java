package pl.robakowski;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonWriter;
import io.activej.bytebuf.ByteBuf;

import java.io.InputStream;

public abstract class Handler {

    protected static final DslJson<?> json = new DslJson<>();

    private static final ThreadLocal<JsonWriter> writer = ThreadLocal.withInitial(json::newWriter);

    public ByteBuf handle(InputStream is) throws Exception {
        JsonWriter jsonWriter = writer.get();
        jsonWriter.reset();
        handle(is, jsonWriter);
        return ByteBuf.wrap(jsonWriter.getByteBuffer(), 0, jsonWriter.size());
    }

    protected abstract void handle(InputStream is, JsonWriter writer) throws Exception;
}
