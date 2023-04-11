package pl.robakowski;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonWriter;
import io.activej.bytebuf.ByteBuf;
import io.activej.common.exception.FatalErrorHandlers;
import io.activej.csp.ChannelSupplier;
import io.activej.csp.ChannelSuppliers;
import io.activej.eventloop.Eventloop;
import io.activej.http.AsyncServlet;
import io.activej.http.HttpMethod;
import io.activej.http.HttpResponse;
import io.activej.http.RoutingServlet;
import io.activej.inject.annotation.Provides;
import io.activej.launchers.http.HttpServerLauncher;
import io.activej.promise.Promise;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import pl.robakowski.atms.Atm;
import pl.robakowski.atms.Request;
import pl.robakowski.game.Clan;
import pl.robakowski.game.Game;
import pl.robakowski.game.Group;
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
        DslJson<?> json = new DslJson<>();
        json.registerWriter(Group.class, (writer, value) -> writer.serialize(value.getClans(), json.tryFindWriter(Clan.class)));
        return json;
    }

    @Provides
    ThreadLocal<JsonWriter> writer(DslJson<?> json) {
        return ThreadLocal.withInitial(json::newWriter);
    }

    @Provides
    AsyncServlet servlet(Executor executor, Eventloop eventloop, DslJson<?> json, ThreadLocal<JsonWriter> writer) {
        FatalErrorHandlers.setGlobalFatalErrorHandler((e, context) -> logger.error("Error in handler", e));
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
                    var is = new ByteArrayInputStream(body.array(), body.head(), body.tail() - body.head());
                    Game game = json.deserialize(Game.class, is);
                    JsonWriter jsonWriter = writer.get();
                    jsonWriter.reset();
                    json.serialize(jsonWriter, game.getOrder());
                    return HttpResponse.ok200()
                            .withHeader(CONTENT_TYPE, ofContentType(JSON_UTF_8))
                            .withBody(ByteBuf.wrap(jsonWriter.getByteBuffer(), 0, jsonWriter.size()));
                }))
                .map(HttpMethod.POST, "/transactions/report", request -> {
                    ChannelSupplier<ByteBuf> body = request.takeBodyStream();
                    return Promise.ofBlocking(executor, () -> handleTransactions(eventloop, json, body, writer));
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