package com.viafoura.examples.simpleapi;

/**
 * Simplified application configuration (statically typed).
 */
public interface AppConfig {
    int SERVER_PORT = 7000;
    String GET_WORDS_HANDLER_PATH = "/simpleApiExample";
    String COUNT_WORDS_HANDLER_PATH = "/simpleApiExample/count";
}