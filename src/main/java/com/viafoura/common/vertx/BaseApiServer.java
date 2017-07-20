package com.viafoura.common.vertx;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.ext.dropwizard.Match;
import io.vertx.ext.dropwizard.MatchType;
import io.vertx.ext.dropwizard.MetricsService;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.validation.constraints.NotNull;

/**
 * Standardized reusable HTTP server pre-configured with:
 * <tt>
 * - rate limiting
 * - metrics
 * - authentication???
 * </tt>
 */
public abstract class BaseApiServer {
    @NotNull
    protected static final Logger logger = LogManager.getLogger(BaseApiServer.class);

    @NotNull
    private final RateLimitDecorator limiter;
    private final int serverPort;

    public BaseApiServer(int serverPort, @NotNull final RateLimiterConfig limiterConfig) {
        logger.info("Initializing {} server.", getClass().getSimpleName());
        this.limiter = new RateLimitDecorator(RateLimiter.of("BaseApiServer", limiterConfig));
        this.serverPort = serverPort;
    }


    /**
     * Wraps the specified handler into a rate-limiting handler.
     *
     * @param handlerToBeLimited A not nullable handler.
     * @return The wrapped handler. Not nullable.
     */
    @NotNull
    public final Handler<RoutingContext> limit(@NotNull final Handler<RoutingContext> handlerToBeLimited) {
        return this.limiter.of(handlerToBeLimited);
    }

    /**
     * Launches the server.
     */
    public void start() {
        logger.info("Launching {} server.", getClass().getSimpleName());
        final Vertx vertx = Vertx.vertx(createMetricsOptions());
        final Router router = Router.router(vertx);
        registerRoutes(router);
        final HttpServer httpServer = vertx.createHttpServer();
        httpServer
                .requestHandler(router::accept)
                .listen(serverPort, listenResult -> {
                    if (listenResult.failed()) {
                        logger.error("Could not start HTTP server: {}", listenResult.cause().getMessage());
                    } else {
                        logger.info("Server started.");
                        setupMetrics(vertx);
                    }
                });
    }

    private VertxOptions createMetricsOptions() {
        return new VertxOptions().setMetricsOptions(
                new DropwizardMetricsOptions()
                        .setEnabled(true)
                        .setJmxEnabled(true)
                        .addMonitoredHttpServerUri(new Match().setValue("/"))
                        .addMonitoredHttpServerUri(new Match().setValue("/palindromes").setType(MatchType.REGEX))
                        .addMonitoredHttpServerUri(new Match().setValue("/palindromes/count").setType(MatchType.REGEX))
        );
    }

    private void setupMetrics(Vertx vertx) {
        final MetricsService metricsService = MetricsService.create(vertx);

        metricsService.metricsNames().stream().forEach(name -> logger.info("# {}", name));
        vertx.setPeriodic(1000, id -> {
            logger.info("========================================================================");
            final JsonObject metrics = metricsService.getMetricsSnapshot("vertx.http.servers.0.0.0.0:7000");
            logger.info(metrics);
        });
    }

    /**
     * Configure each route here (in a a subclass).
     */
    public abstract void registerRoutes(@NotNull Router router);
}
