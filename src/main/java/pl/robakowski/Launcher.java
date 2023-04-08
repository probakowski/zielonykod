package pl.robakowski;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonWriter;
import io.activej.bytebuf.ByteBuf;
import io.activej.csp.ChannelSupplier;
import io.activej.csp.ChannelSuppliers;
import io.activej.eventloop.Eventloop;
import io.activej.http.AsyncServlet;
import io.activej.http.HttpMethod;
import io.activej.http.HttpResponse;
import io.activej.http.RoutingServlet;
import io.activej.inject.annotation.Provides;
import io.activej.launchers.http.HttpServerLauncher;
import io.activej.promise.SettablePromise;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import pl.robakowski.atms.Atm;
import pl.robakowski.atms.Request;
import pl.robakowski.game.Game;
import pl.robakowski.transactions.Report;
import pl.robakowski.transactions.Transaction;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static io.activej.http.ContentTypes.JSON_UTF_8;
import static io.activej.http.HttpHeaderValue.ofContentType;
import static io.activej.http.HttpHeaders.CONTENT_TYPE;

public class Launcher extends HttpServerLauncher {
    @Provides
    Executor executor() {
        return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @Provides
    DslJson<?> json() {
        return new DslJson<>();
    }

    @Provides
    ThreadLocal<JsonWriter> writer(DslJson<?> json) {
        return ThreadLocal.withInitial(json::newWriter);
    }

    @Provides
    AsyncServlet servlet(Executor executor, Eventloop eventloop, DslJson<?> json, ThreadLocal<JsonWriter> writer) {
        return RoutingServlet.create()
                .map(HttpMethod.POST, "/atms/calculateOrder", AsyncServlet.ofBlocking(executor, request -> {
                    ByteBuf body = request.getBody();
                    var is = new ByteArrayInputStream(body.array(), body.head(), body.tail() - body.head());
                    List<Request> requests = json.deserializeList(Request.class, is);
                    Collection<Atm> atms = (Collection<Atm>) Request.calculateOrder(requests);
                    JsonWriter jsonWriter = writer.get();
                    jsonWriter.reset();
                    jsonWriter.serialize(atms, json.tryFindWriter(Atm.class));
                    return HttpResponse.ok200()
                            .withHeader(CONTENT_TYPE, ofContentType(JSON_UTF_8))
                            .withBody(ByteBuf.wrap(jsonWriter.getByteBuffer(), 0, jsonWriter.size()));
                }))
                .map(HttpMethod.POST, "/onlinegame/calculate", AsyncServlet.ofBlocking(executor, request -> {
                    ByteBuf body = request.getBody();
                    Game game = json.deserialize(Game.class, body.array(), body.tail());
                    return HttpResponse.ok200()
                            .withHtml("calculateOrder");
                }))
                .map(HttpMethod.POST, "/transactions/report", request -> {
                    ChannelSupplier<ByteBuf> body = request.takeBodyStream();
                    SettablePromise<HttpResponse> response = new SettablePromise<>();
                    executor.execute(() -> {
                        try {
                            HttpResponse result = handleTransactions(eventloop, json, body, writer);
                            eventloop.execute(() -> response.set(result));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    return response;
                });
    }

    @NotNull
    private static HttpResponse handleTransactions(Eventloop eventloop, DslJson<?> json, ChannelSupplier<ByteBuf> body, ThreadLocal<JsonWriter> writer) throws IOException {
        try (InputStream is = ChannelSuppliers.channelSupplierAsInputStream(eventloop, body)) {
            Iterator<Transaction> it = json.iterateOver(Transaction.class, is);
            Report report = new Report();
            report.processTransactions(it);
            JsonWriter jsonWriter = writer.get();
            jsonWriter.reset();
            json.serialize(jsonWriter, report.getAccounts());
            return HttpResponse.ok200()
                    .withHeader(CONTENT_TYPE, ofContentType(JSON_UTF_8))
                    .withBody(ByteBuf.wrap(jsonWriter.getByteBuffer(), 0, jsonWriter.size()));
        }
    }

    public static void main(String[] args) throws Exception {
        LoggerFactory.getLogger(Launcher.class).info("Starting");
        Launcher example = new Launcher();
        example.launch(args);
    }
}