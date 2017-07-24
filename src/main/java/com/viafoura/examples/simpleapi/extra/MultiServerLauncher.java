package com.viafoura.examples.simpleapi.extra;

import com.viafoura.examples.simpleapi.ExampleService;
import io.javalin.Javalin;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rapidoid.setup.On;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import static com.viafoura.examples.simpleapi.AppConfig.*;


/**
 * Launches servers implemented with different technologies.
 */
public class MultiServerLauncher {
    private static final Logger logger = LogManager.getLogger(MultiServerLauncher.class);

    enum ServerType {
        JAVALIN,
        VERTXIO,
        RAPIDOID
    }

    /**
     * All registered server launchers.
     */
    private final Map<ServerType, Runnable> launchers = new EnumMap<>(ServerType.class);

    /**
     * The service.
     */
    private final ExampleService service = new ExampleService();

    public MultiServerLauncher() {
        registerLaunchers();
    }

    /**
     * Launches a server of the specified type.
     */
    public void launchServer(ServerType type, final String fileName) {
        try {
            this.service.collectPalindromeKeys(fileName);
            logger.info("Launching {} server.", type);
            this.launchers.getOrDefault(type, this::startVertxServer).run();
        } catch (IOException e) {
            logger.error("Problem while executing server {}.", type, e);
        }
    }


    private void registerLaunchers() {
        this.launchers.put(ServerType.JAVALIN, this::startJavalinServer);
        this.launchers.put(ServerType.VERTXIO, this::startVertxServer);
        this.launchers.put(ServerType.RAPIDOID, this::startRapidoidServer);
    }


    //
    // -- Server Implementations.
    //

    private void startJavalinServer() {
        Javalin.create()
                .port(APP_SERVER_PORT)
                .get(GET_WORDS_HANDLER_PATH, ctx -> {
                    ctx.result(service.getPalindromeKeys().toString());
                })
                .get(COUNT_WORDS_HANDLER_PATH, ctx -> {
                    ctx.result(Integer.toString(service.getPalindromeKeys().size()));
                })
                .before(ctx -> logger.info("Serving {}", ctx.uri()));
    }


    private void startVertxServer() {
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
                .listen(APP_SERVER_PORT);
    }


    private void startRapidoidServer() {
        On.port(APP_SERVER_PORT);
        On.get(GET_WORDS_HANDLER_PATH).json(msg -> service.getPalindromeKeys());
        On.get(COUNT_WORDS_HANDLER_PATH).json(msg -> service.getPalindromeKeys().size());
    }
}
