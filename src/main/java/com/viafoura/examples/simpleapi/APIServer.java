package com.viafoura.examples.simpleapi;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

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
        router.route(AppConfig.GET_WORDS_HANDLER_PATH).handler(ctx -> {
            ctx.response().end(service.getPalindromeKeys().toString());
        });
        router.route(AppConfig.COUNT_WORDS_HANDLER_PATH).handler(ctx -> {
            ctx.response().end(Integer.toString(service.getPalindromeKeys().size()));
        });
        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(AppConfig.SERVER_PORT);
    }
}