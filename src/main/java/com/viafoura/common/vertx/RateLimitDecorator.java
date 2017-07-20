package com.viafoura.common.vertx;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vavr.CheckedRunnable;
import io.vavr.control.Try;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Optional;

/**
 * Decorates a Handler with a rate limiter.
 */
public class RateLimitDecorator {
    private static final Logger logger = LogManager.getLogger(RateLimitDecorator.class);

    private final RateLimiter rateLimiter; // Nullable

    /**
     * Initializes an instance with a null RateLimiter. It is expected that subsequent calls
     * to {@link #of(Handler, RateLimiter)} specify a rate limiter.
     */
    public RateLimitDecorator() {
        this.rateLimiter = null;
    }

    /**
     * Initializes an instance with a not null RateLimiter.
     *
     * @param rateLimiter A global RateLimiter to use in all decorations that do not specify a RateLimiter.
     */
    public RateLimitDecorator(@NotNull RateLimiter rateLimiter) {
        this.rateLimiter = Objects.requireNonNull(rateLimiter);
    }

    /**
     * Wraps the specified handler with a new one that will forward or reject requests
     * (with HttpResponseStatus.TOO_MANY_REQUESTS code), depending on the RateLimiter configuration.
     * <p>
     * This uses the RateLimiter specified in the constructor (has to be not null).
     *
     * @param delegate The handler to limit.
     * @return The decorated version of delegate.
     */
    @NotNull
    public Handler<RoutingContext> of(@NotNull final Handler<RoutingContext> delegate) {
        return of(delegate, this.rateLimiter);
    }

    /**
     * Wraps the specified handler with a new one that will forward or reject requests
     * (with HttpResponseStatus.TOO_MANY_REQUESTS code), depending on the RateLimiter configuration.
     *
     * @param delegate    The Handler to limit.
     * @param rateLimiter The RateLimiter to use.
     * @return The decorated version of delegate.
     */
    @NotNull
    public static Handler<RoutingContext> of(@NotNull final Handler<RoutingContext> delegate, @NotNull final RateLimiter rateLimiter) {
        Objects.requireNonNull(delegate);
        Objects.requireNonNull(rateLimiter);

        return context -> {
            final CheckedRunnable limitedDelegateCaller = RateLimiter.decorateCheckedRunnable(rateLimiter, () -> delegate.handle(context));
            Try.run(limitedDelegateCaller)
                    .onSuccess(aVoid -> {
                        // Request processing was accepted and executed. Do nothing else.
                        // Optional.ofNullable(context).ifPresent(RoutingContext::next);
                    })
                    .onFailure(throwable -> {
                        // Request processing was denied. Fail request with standard HTTP status code.
                        Optional.ofNullable(context).ifPresent(ctx -> ctx.fail(HttpResponseStatus.TOO_MANY_REQUESTS.code()));
                    });

        };
    }
}
