package com.viafoura.examples.simpleapi;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * Simplified application configuration (statically typed).
 */
public interface AppConfig {
    Config config = ConfigFactory.load();
//    VfmClient vfmClient =  Vfm.factory().build();

    int APP_SERVER_PORT = AppConfig.config.getInt("simpleapi.app.server.port");
    int LIMIT_REFRESH_PERIOD = AppConfig.config.getInt("simpleapi.ratelimit.limitRefreshPeriod");
    int LIMIT_FOR_PERIOD = AppConfig.config.getInt("simpleapi.ratelimit.limitForPeriod");
    int TIMEOUT_DURATION = AppConfig.config.getInt("simpleapi.ratelimit.timeoutDuration");
    int HEARTBEAT_INTERVAL = AppConfig.config.getInt("simpleapi.metrics.heartbeatInterval");

    String GET_WORDS_HANDLER_PATH = "/simpleApiExample";
    String COUNT_WORDS_HANDLER_PATH = "/simpleApiExample/count";
}