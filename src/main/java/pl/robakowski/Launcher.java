package pl.robakowski;

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
import pl.robakowski.atms.AtmHandler;
import pl.robakowski.game.GameHandler;
import pl.robakowski.transactions.TransactionsHandler;

import java.io.InputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static io.activej.http.ContentTypes.JSON_UTF_8;
import static io.activej.http.HttpHeaderValue.ofContentType;
import static io.activej.http.HttpHeaders.CONTENT_TYPE;

public class Launcher extends HttpServerLauncher {

    private static final Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    @Provides
    AsyncServlet servlet(Eventloop eventloop) {
        FatalErrorHandlers.setGlobalFatalErrorHandler((e, context) -> logger.error("Error in handler", e));
        AtmHandler atmHandler = new AtmHandler();
        GameHandler gameHandler = new GameHandler();
        TransactionsHandler transactionsHandler = new TransactionsHandler();
        return RoutingServlet.create()
                .map(HttpMethod.POST, "/atms/calculateOrder", handle(atmHandler, eventloop))
                .map(HttpMethod.POST, "/onlinegame/calculate", handle(gameHandler, eventloop))
                .map(HttpMethod.POST, "/transactions/report", handle(transactionsHandler, eventloop));
    }

    private static AsyncServlet handle(Handler handler, Eventloop eventloop) {
        return request -> {
            ChannelSupplier<ByteBuf> body = request.takeBodyStream();
            return Promise.ofBlocking(executor, () -> {
                try (InputStream is = ChannelSuppliers.channelSupplierAsInputStream(eventloop, body)) {
                    return handler.handle(is);
                }
            }).then(buf -> HttpResponse.ok200().withHeader(CONTENT_TYPE, ofContentType(JSON_UTF_8)).withBody(buf).promise());
        };
    }

    public static void main(String[] args) throws Exception {
        Launcher launcher = new Launcher();
        launcher.launch(args);
    }
}