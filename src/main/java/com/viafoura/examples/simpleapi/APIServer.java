package com.viafoura.examples.simpleapi;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.Instant;

/**
 * Serves a REST API on a PalindromesService.
 */
public class APIServer {
    private static final Logger logger = LogManager.getLogger(APIServer.class);

    /**
     * The service.
     */
    final PalindromesService service = new PalindromesService();

    public APIServer(final String fileName) throws IOException {
        this.service.collectPalindromeKeys(fileName);
    }

    /**
     * Launches the server.
     */
    public void start() {
        logger.info("Launching server.");

        final Vertx vertx = Vertx.vertx();
        final Router router = Router.router(vertx);
        registerRoutes(router);
        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(AppConfig.SERVER_PORT, listenResult -> {
                    if (listenResult.failed()) {
                        logger.error("Could not start HTTP server: {}", listenResult.cause().getMessage());
                    } else {
                        logger.info("Server started.");
                    }
                });
    }


    /**
     * Configure each route here.
     */
    private void registerRoutes(Router router) {
        router.route(AppConfig.GET_WORDS_HANDLER_PATH).handler(ctx -> {
            ctx.response().end(service.getPalindromeKeys().toString());
        });

        router.route(AppConfig.COUNT_WORDS_HANDLER_PATH).handler(ctx -> {
            ctx.response().end(Integer.toString(service.getPalindromeKeys().size()));
        });

        router.route("/").handler(new SomeComplexHandler());
    }
}


class SomeComplexHandler implements Handler<RoutingContext> {
    @Override
    public void handle(RoutingContext ctx) {
        // lot of code here
        final long seconds = Instant.now().getEpochSecond();
        ctx.response().end("" + seconds);
    }
}