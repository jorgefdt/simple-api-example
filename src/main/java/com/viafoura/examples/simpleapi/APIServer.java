package com.viafoura.examples.simpleapi;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

/**
 * Serves a REST API on a PalindromesService.
 */
public class APIServer {
    private static final Logger logger = LogManager.getLogger(APIServer.class);

    /**
     * The service.
     */
    private final PalindromesService service = new PalindromesService();
    private RateLimiter rateLimiter;


    public APIServer(final String fileName) throws IOException {
        this.service.collectPalindromeKeys(fileName);
        initRateLimiter();
    }

    /**
     * Launches the server.
     */
    public void start() {
        logger.info("Launching server.");

        final Vertx vertx = Vertx.vertx();
        final Router router = Router.router(vertx);
//        registerRoutes(router);
        registerRoutesWithLimit(router);
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


    private void initRateLimiter() {
        // Calling rate not higher than 10 req/ms.
        final RateLimiterConfig config = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofMillis(100))
                .limitForPeriod(5)
                .timeoutDuration(Duration.ofMillis(10))
                .build();

        this.rateLimiter = RateLimiter.of("VF1", config);
    }


    /**
     * Configure each route here.
     */
    private void registerRoutesWithLimit(Router router) {
        final Handler<RoutingContext> handler = ctx -> {
            ctx.response().end(service.getPalindromeKeys().toString());
        };
        router.route(AppConfig.GET_WORDS_HANDLER_PATH).handler(LimitRate.of(handler, this.rateLimiter));

        router.route(AppConfig.COUNT_WORDS_HANDLER_PATH).handler(LimitRate.of(ctx -> {
            ctx.response().end(Integer.toString(service.getPalindromeKeys().size()));
        }, this.rateLimiter));

        router.route("/").handler(LimitRate.of(new SomeComplexHandler(), this.rateLimiter));
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


