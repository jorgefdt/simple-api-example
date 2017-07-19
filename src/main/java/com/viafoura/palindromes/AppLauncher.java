package com.viafoura.palindromes;

import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Log4J2LoggerFactory;

import java.io.IOException;

/**
 * Command line application launcher.
 */
public class AppLauncher {
    public static void main(String[] args) throws IOException {
        if (args.length == 1) {
            startServer(args[0]);
        } else {
            System.err
                    .printf("** ERROR: missing argument!%n")
                    .printf("   USAGE: java %s <file>%n", AppLauncher.class.getName());
        }
    }

    private static void startServer(final String fileName) {
        customizeVertxLogging();
        new ServerLauncher().launchServer(fileName);
    }

    private static void customizeVertxLogging() {
        // Force Vertx logging to Log4J2.
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.Log4j2LogDelegateFactory");

        // Force Netty logging to Log4j.
        InternalLoggerFactory.setDefaultFactory(Log4J2LoggerFactory.INSTANCE);
    }
}
