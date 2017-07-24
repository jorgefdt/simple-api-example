package com.viafoura.common.monitoring.heartbeat.scheduler;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Simple scheduler.
 */
public class Scheduler implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(Scheduler.class);
    private static final int THREAD_POOL_SIZE = 20;
    private static final long GRACE_WAIT_IN_SECONDS = 5;

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(THREAD_POOL_SIZE);
    private final AtomicBoolean closeRequested = new AtomicBoolean(false);

    public ScheduledFuture<?> scheduleWithFixedDelay(@NonNull Runnable task, long initialDelayMillis, long delayMillis) {
        logger.info("Scheduling task with fixed delay every {} ms, with initial delay of {} ms.", delayMillis, initialDelayMillis);
        return executorService.scheduleWithFixedDelay(task, initialDelayMillis, delayMillis, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> scheduleAtFixedRate(@NonNull Runnable task, long initialDelayMillis, long period) {
        logger.info("Scheduling task at fixed rate every {} ms, with initial delay of {} ms.", period, initialDelayMillis);
        return executorService.scheduleAtFixedRate(task, initialDelayMillis, period, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> addSimpleJob(@NonNull final Runnable task, final long delayMillis) {
        return executorService.schedule(task, delayMillis, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> scheduleTaskStop(@NonNull final ScheduledFuture<?> future, final long delayMillis) {
        logger.info("Scheduling task stop in {} ms.", delayMillis);
        return addSimpleJob(() -> future.cancel(false), delayMillis);
    }


    /**
     * Shuts down the executor service and awaits termination of the executing tasks for GRACE_WAIT_IN_SECONDS seconds.
     * After calling this, this class can't be used again to schedule anything.
     */
    @Override
    public void close() {
        if (!closeRequested.getAndSet(true)) {
            if (!executorService.isShutdown() && !executorService.isTerminated()) {
                logger.info("Closing " + Scheduler.class.getSimpleName() + " service...");
                executorService.shutdown();
                try {
                    executorService.awaitTermination(GRACE_WAIT_IN_SECONDS, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    logger.error("Failed while closing " + Scheduler.class.getSimpleName() + " service: " + e, e);
                }
            }
        }
    }
}