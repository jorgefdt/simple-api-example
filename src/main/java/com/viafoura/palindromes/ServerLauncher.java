package com.viafoura.palindromes;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import static com.viafoura.palindromes.AppConfig.*;

/**
 * Launches and configures the server.
 */
public class ServerLauncher {
    private static final Logger logger = LogManager.getLogger(ServerLauncher.class);

    /**
     * The service.
     */
    private final PalindromesService service = new PalindromesService();

    /**
     * Launches the server.
     */
    public void launchServer(final String fileName) {
        try {
            this.service.collectPalindromeKeys(fileName);
            this.startVertxServer();
        } catch (IOException e) {
            logger.error("Problem while executing server.", e);
        }
    }


    private void startVertxServer() {
        logger.info("Launching server.");

        final Vertx vertx = Vertx.vertx();
        final Router router = Router.router(vertx);
        router.route(GET_WORDS_HANDLER_PATH).handler(ctx -> {
            ctx.response().end(service.getPalindromeKeys().toString());
        });
        router.route(COUNT_WORDS_HANDLER_PATH).handler(ctx -> {
            ctx.response().end(Integer.toString(service.getPalindromeKeys().size()));
        });
        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(SERVER_PORT);
    }
}
