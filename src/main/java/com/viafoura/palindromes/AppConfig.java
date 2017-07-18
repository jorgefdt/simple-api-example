package com.viafoura.palindromes;

/**
 * Simplified application configuration (statically typed).
 */
public interface AppConfig {
    // Server config
    int SERVER_PORT = 7000;
    String GET_WORDS_HANDLER_PATH = "/palindromes";
    String COUNT_WORDS_HANDLER_PATH = "/palindromes/count";
}