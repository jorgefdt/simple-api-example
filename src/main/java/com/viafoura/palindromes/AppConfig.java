package com.viafoura.palindromes;

interface AppConfig {
    // Server config
    int SERVER_PORT = 7000;
    String GET_WORDS_HANDLER_PATH = "/palindromes";
    String COUNT_WORDS_HANDLER_PATH = "/palindromes/count";

    // Other
    ServerType DEFAULT_SERVER_TYPE = ServerType.VERTXIO;
}