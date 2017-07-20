package com.viafoura.common.vertx;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.validation.constraints.NotNull;

/**
 * Standardized reusable HTTP server pre-configured with:
 * <tt>
 * - rate limiting
 * - authentication???
 * </tt>
 */
public abstract class BaseAPIServer {
    @NotNull
    protected static final Logger logger = LogManager.getLogger(BaseAPIServer.class);

    @NotNull
    private final RateLimitDecorator limiter;
    private final int serverPort;

    public BaseAPIServer(int serverPort, @NotNull final RateLimiterConfig limiterConfig) {
        logger.info("Initializing {} server.", getClass().getSimpleName());
        this.limiter = new RateLimitDecorator(RateLimiter.of("BaseAPIServer", limiterConfig));
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
        final Vertx vertx = Vertx.vertx();
        final Router router = Router.router(vertx);
        registerRoutes(router);
        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(serverPort, listenResult -> {
                    if (listenResult.failed()) {
                        logger.error("Could not start HTTP server: {}", listenResult.cause().getMessage());
                    } else {
                        logger.info("Server started.");
                    }
                });
    }

    /**
     * Configure each route here (in a a subclass).
     */
    public abstract void registerRoutes(@NotNull Router router);
}
