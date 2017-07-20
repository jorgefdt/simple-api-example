package com.viafoura.examples.simpleapi;

import com.viafoura.common.vertx.BaseAPIServer;
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
 * HTTP Server that serves a REST API on PalindromesService.
 */
public class PalindromesAPIServer extends BaseAPIServer {
    /**
     * The service.
     */
    private final PalindromesService service = new PalindromesService();

    public PalindromesAPIServer(final String fileName) throws IOException {
        super(AppConfig.SERVER_PORT, makeRateLimiterConfig());
        this.service.collectPalindromeKeys(fileName);
    }

    private static RateLimiterConfig makeRateLimiterConfig() {
        // Calling rate not higher than 10 req/ms.
        return RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofMillis(100))
                .limitForPeriod(5)
                .timeoutDuration(Duration.ofMillis(10))
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
                ctx.response().end(Integer.toString(service.getPalindromeKeys().size()))
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
