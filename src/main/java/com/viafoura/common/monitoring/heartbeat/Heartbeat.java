package com.viafoura.common.monitoring.heartbeat;

import com.viafoura.common.monitoring.heartbeat.scheduler.Schedule;
import com.viafoura.common.vfmetrics.VfmClient;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;


/**
 * Utility class to emit heartbeats.
 */
public class Heartbeat {

    private static final Logger logger = LoggerFactory.getLogger(Heartbeat.class);

    private static final Consumer<VfmClient> reportHeartbeatToMetrics = (metrics) -> {
        logger.info("Emitting heartbeat to metrics id {}", metrics.getInternalId());
        metrics.reportHeartbeat();
    };

    private static final Consumer<VfmClient> defaultReportHeartbeat = reportHeartbeatToMetrics;

    /**
     * Avoid instantiation
     */
    private Heartbeat() {
    }

    /**
     * Emits a heartbeat every <tt>intervalInMillis</tt> milliseconds, forever.
     *
     * @param someMetrics      The metrics.
     * @param intervalInMillis Positive value.
     */
    public static void emitForeverTo(@NonNull final VfmClient someMetrics, final long intervalInMillis) {
        logger.info("Starting forever heartbeat emission for {}.", someMetrics.getResourceName());

        Schedule.now()
                .interval(intervalInMillis)
                .forEach(() -> defaultReportHeartbeat.accept(someMetrics))
                .start();
    }


    /**
     * Emits a heartbeat every <tt>intervalInMillis</tt> milliseconds, stopping after <tt>stoppingInMillis</tt> milliseconds.
     *
     * @param someMetrics      The metrics.
     * @param intervalInMillis Positive value.
     */
    public static void emitTo(@NonNull final VfmClient someMetrics, final long intervalInMillis, final long stoppingInMillis) {
        logger.info("Starting heartbeat emission for {}.", someMetrics.getResourceName());
        Schedule.now()
                .interval(intervalInMillis)
                .stoppingIn(stoppingInMillis)
                .forEach(() -> defaultReportHeartbeat.accept(someMetrics))
                .start();
    }


    /**
     * Emits a heartbeat now.
     */
    public static void emitNowTo(@NonNull final VfmClient someMetrics) {
        logger.info("Starting heartbeat emission for {}.", someMetrics.getResourceName());
        defaultReportHeartbeat.accept(someMetrics);
    }
}