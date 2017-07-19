package com.viafoura.examples.simpleapi;

/**
 * Simplified application configuration (statically typed).
 */
public interface AppConfig {
    // Server config
    int SERVER_PORT = 7000;
    String GET_WORDS_HANDLER_PATH = "/simpleApiExample";
    String COUNT_WORDS_HANDLER_PATH = "/simpleApiExample/count";
}