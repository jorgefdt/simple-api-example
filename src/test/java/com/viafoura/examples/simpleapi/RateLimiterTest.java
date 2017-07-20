package com.viafoura.examples.simpleapi;

import com.viafoura.common.vertx.RateLimitDecorator;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.vavr.CheckedRunnable;
import io.vavr.control.Try;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

public class RateLimiterTest {
    private static final Logger logger = LogManager.getLogger(RateLimiterTest.class);

    @Test
    public void tryRateLimiter() {
        // Calling rate not higher than 10 req/ms.
        final RateLimiterConfig config = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofMillis(100))
                .limitForPeriod(5)
                .timeoutDuration(Duration.ofMillis(10))
                .build();

        final RateLimiter rateLimiter = RateLimiter.of("VF1", config);


        // Decorate call.
        final AtomicLong counter = new AtomicLong();
        final CheckedRunnable limitedCounter = RateLimiter.decorateCheckedRunnable(rateLimiter, counter::incrementAndGet);

        final StringBuilder calls = new StringBuilder();
        rateLimiter.getEventPublisher()
                .onSuccess(event -> calls.append("*"))
                .onFailure(event -> calls.append("."));

        // Run
        final int NUM_REQUESTS = 200;
        IntStream.range(0, NUM_REQUESTS).forEach(x -> Try.run(limitedCounter));
        System.out.println();

        // Verification.
        logger.info("Called {} times.", counter.get());
        logger.info("calls: {}", calls);
        Assert.assertTrue(counter.get() < NUM_REQUESTS);
    }

    @Test
    public void tryRateLimiterHandler() {
        // Calling rate not higher than 10 req/ms.
        final RateLimiterConfig config = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofMillis(100))
                .limitForPeriod(5)
                .timeoutDuration(Duration.ofMillis(10))
                .build();

        final RateLimiter rateLimiter = RateLimiter.of("VF1", config);
        final RateLimitDecorator limiter = new RateLimitDecorator(rateLimiter);

        final AtomicLong counter = new AtomicLong();
        final Handler<RoutingContext> limitedHandler = limiter.of(event -> counter.incrementAndGet());

        // Run
        final int NUM_REQUESTS = 200;
        IntStream.range(0, NUM_REQUESTS).forEach(x -> limitedHandler.handle(null));

        // Verification.
        logger.info("Called {} times.", counter.get());
        Assert.assertTrue(counter.get() < NUM_REQUESTS);
    }
}
