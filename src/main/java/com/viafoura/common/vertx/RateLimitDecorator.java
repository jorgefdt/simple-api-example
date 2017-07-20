package com.viafoura.common.vertx;

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
public class RateLimitDecorator {
    private static final Logger logger = LogManager.getLogger(RateLimitDecorator.class);
    private final RateLimiter rateLimiter;

    public RateLimitDecorator(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }


    public Handler<RoutingContext> of(final Handler<RoutingContext> delegate) {
        return context -> {
            final CheckedRunnable limitedDelegateCaller = RateLimiter.decorateCheckedRunnable(rateLimiter, () -> delegate.handle(context));
            Try.run(limitedDelegateCaller)
                    .onSuccess(aVoid -> {
//                        logger.info("ACCEPTED!");
//                        Optional.ofNullable(context).ifPresent(RoutingContext::next);
//                        context.next();
                    })
                    .onFailure(throwable -> {
//                        logger.info("REJECTED");
                        Optional.ofNullable(context).ifPresent(ctx -> ctx.fail(420));
                    });
        };
    }

}
