package com.viafoura.examples.simpleapi;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.vavr.CheckedRunnable;
import io.vavr.control.Try;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

/**
 * Decorates a Handler with a rate limiter.
 */
public class LimitRate implements Handler<RoutingContext> {
    private static final Logger logger = LogManager.getLogger(LimitRate.class);
    private final RateLimiter rateLimiter;
    private final Handler<RoutingContext> delegate;

    private LimitRate(final Handler<RoutingContext> delegate, final RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
        this.delegate = delegate;
    }

    public static LimitRate of(final Handler<RoutingContext> delegate, final RateLimiter rateLimiter) {
        return new LimitRate(delegate, rateLimiter);
    }


    @Override
    public void handle(RoutingContext context) {
        // Simplified version.
        final CheckedRunnable delegateCaller = () -> this.delegate.handle(context);
        final CheckedRunnable limitedDelegateCaller = RateLimiter.decorateCheckedRunnable(this.rateLimiter, delegateCaller);
        Try.run(limitedDelegateCaller)
                .onSuccess(aVoid -> {
//                    logger.info("ACCEPTED!");
//                    Optional.ofNullable(context).ifPresent(RoutingContext::next);
//                    context.next();
                })
                .onFailure(throwable -> {
//                    logger.info("REJECTED");
                    Optional.ofNullable(context).ifPresent(ctx -> ctx.fail(420));
                });
    }
}
