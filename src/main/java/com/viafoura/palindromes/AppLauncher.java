package com.viafoura.palindromes;

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
        new ServerLauncher().launchServer(fileName);
    }
}
