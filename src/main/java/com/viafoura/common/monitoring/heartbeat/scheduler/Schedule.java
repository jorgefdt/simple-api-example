package com.viafoura.common.monitoring.heartbeat.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledFuture;

public class Schedule {
    private static final Logger logger = LoggerFactory.getLogger(Schedule.class);

    private static Scheduler theScheduler = null;
    private final long millisToStart;
    private long intervalInMillis;
    private long lifeSpanInMillis;
    private ScheduledFuture<?> scheduledFuture;
    private Runnable task;


    public static void close() {
        synchronized (Schedule.class) {
            if (theScheduler != null) {
                logger.info("Closing Schedule.");
                theScheduler.close();
                theScheduler = null;
            }
        }
    }

    private synchronized Scheduler getScheduler() {
        synchronized (Schedule.class) {
            if (theScheduler == null) {
                logger.info("Creating Scheduler.");
                theScheduler = new Scheduler();
            }
        }

        return theScheduler;
    }

    private Schedule(final long millisToStart) {
        this.millisToStart = millisToStart;
    }

    /**
     * Static factory.
     */
    public static Schedule now() {
        return new Schedule(0);
    }

    /**
     * Static factory.
     */
    public static Schedule inMillis(final long millisToStart) {
        return new Schedule(millisToStart);
    }

    public Schedule interval(long intervalInMillis) {
        this.intervalInMillis = intervalInMillis;
        return this;
    }

    public Schedule stoppingIn(long lifeSpanInMillis) {
        this.lifeSpanInMillis = lifeSpanInMillis;
        return this;
    }

    public Schedule forEach(Runnable task) {
        this.task = task;
        return this;
    }

    public void start() {
        this.scheduledFuture = getScheduler().scheduleAtFixedRate(this.task, (long) 0, this.intervalInMillis);

        // If configured, schedule a stop.
        if (this.lifeSpanInMillis > 0) {
            getScheduler().scheduleTaskStop(scheduledFuture, this.lifeSpanInMillis);
        }
    }
}