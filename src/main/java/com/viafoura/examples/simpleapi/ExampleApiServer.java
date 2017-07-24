package com.viafoura.examples.simpleapi;

import com.viafoura.common.vertx.BaseApiServer;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.vertx.core.Handler;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import static com.viafoura.examples.simpleapi.AppConfig.COUNT_WORDS_HANDLER_PATH;
import static com.viafoura.examples.simpleapi.AppConfig.GET_WORDS_HANDLER_PATH;

/**
 * HTTP Server that serves a REST API on ExampleService.
 */
public class ExampleApiServer extends BaseApiServer {
    /**
     * The service.
     */
    private final ExampleService service = new ExampleService();

    public ExampleApiServer(final String fileName) throws IOException {
        super(AppConfig.APP_SERVER_PORT, makeRateLimiterConfig());
        this.service.collectPalindromeKeys(fileName);
    }

    private static RateLimiterConfig makeRateLimiterConfig() {
        // Calling rate not higher than 10 req/ms.
        return RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofMillis(AppConfig.LIMIT_REFRESH_PERIOD))
                .limitForPeriod(AppConfig.LIMIT_FOR_PERIOD)
                .timeoutDuration(Duration.ofMillis(AppConfig.TIMEOUT_DURATION))
                .build();
    }


    /**
     * Configure each route here.
     */
    @Override
    public void registerRoutes(@NotNull final Router router) {
        router.route(GET_WORDS_HANDLER_PATH).handler(limit(ctx ->
                ctx.response().end(service.getPalindromeKeys().toString())
        ));

        router.route(COUNT_WORDS_HANDLER_PATH).handler(limit(ctx ->
                ctx.response().end(Integer.toString(service.getPalindromeKeysSize()))
        ));

        router.route("/").handler(limit(new SomeComplexHandler()));
    }

}

class SomeComplexHandler implements Handler<RoutingContext> {
    @Override
    public void handle(RoutingContext ctx) {
        ctx.response().end(Long.toString(Instant.now().getEpochSecond()));
    }
}
