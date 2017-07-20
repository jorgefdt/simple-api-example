package com.viafoura.examples.simpleapi;

import com.viafoura.common.vertx.VertxUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Command line application launcher.
 */
public class AppLauncher {
    private static final Logger logger = LogManager.getLogger(AppLauncher.class);

    public static void main(String[] args) throws IOException {
        if (args.length == 1) {
            startServer(args[0]);
        } else {
            System.err
                    .printf("** ERROR: missing argument!%n")
                    .printf("   USAGE: java %s <file>%n", AppLauncher.class.getName());
        }
    }

    static void startServer(final String fileName) {
        // Force Vertx.io and Netty logging to use the same as the rest of this app: Log4J2
        VertxUtils.customizeVertxLogging();

        try {
            new PalindromesApiServer(fileName).start();
        } catch (IOException e) {
            logger.error("Problem while executing server.", e);
        }
    }
}
