package com.viafoura.common.vertx;

import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Log4J2LoggerFactory;

/**
 * Utilities associated with the Vertx.iolibrary.
 */
public class VertxUtils {
    private VertxUtils() { // meant to avoid instantiation.
    }

    /**
     * Force Vertx and Netty logging to Log4J2.
     */
    public static void customizeVertxLogging() {
        // Force Vertx logging to Log4J2.
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.Log4j2LogDelegateFactory");

        // Force Netty logging to Log4j.
        InternalLoggerFactory.setDefaultFactory(Log4J2LoggerFactory.INSTANCE);
    }
}
