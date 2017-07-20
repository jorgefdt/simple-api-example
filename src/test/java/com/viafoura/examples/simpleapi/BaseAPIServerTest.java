package com.viafoura.examples.simpleapi;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RepeatRule;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

@RunWith(VertxUnitRunner.class)
public class BaseAPIServerTest {
    private static final Logger logger = LogManager.getLogger(BaseAPIServerTest.class);
    private static final String INPUT_TEST_FILE = "input-sample.txt";

    @Rule
    public RepeatRule repeatRule = new RepeatRule();

    @Rule
    public RunTestOnContext ctxRule = new RunTestOnContext();


    @BeforeClass
    public static void suiteSetUp() throws Exception {
        logger.info("Setting up suite.");
        AppLauncher.startServer(INPUT_TEST_FILE);
    }


    @Test
    public void getPalindromes(TestContext ctx) {
        final Async async = ctx.async();
        ctxRule.vertx()
                .createHttpClient()
                .getNow(AppConfig.SERVER_PORT, "localhost", AppConfig.GET_WORDS_HANDLER_PATH, res -> {
                    res.handler(body -> {
                        ctx.assertEquals("[racecar, abccba]", body.toString());
                        async.complete();
                    });
                });
    }


    @Test
    public void getPalindromesCount(TestContext ctx) {
        final Async async = ctx.async();
        ctxRule.vertx()
                .createHttpClient()
                .getNow(AppConfig.SERVER_PORT, "localhost", AppConfig.COUNT_WORDS_HANDLER_PATH, res -> {
                    res.handler(body -> {
                        ctx.assertEquals("2", body.toString());
                        async.complete();
                    });
                });
    }


    @Test
    public void getPalindromesCountLimited(TestContext ctx) {
        final int NUM_REQUESTS = 200;
        final AtomicLong counter = new AtomicLong();

        final Vertx vertx = Vertx.vertx();
        final Async async = ctx.async(NUM_REQUESTS);
        IntStream.range(0, NUM_REQUESTS).forEach(x -> {
            vertx.createHttpClient()
                    .get(AppConfig.SERVER_PORT, "localhost", AppConfig.COUNT_WORDS_HANDLER_PATH)
                    .handler(res -> {
                        final int status = res.statusCode();
                        if (status == 200) {
                            counter.incrementAndGet();
                            logger.info("OK");
                        } else {
                            logger.info("FAILED: {}", status);
                        }

                        final int count = async.count();
                        if (count == 0) {
                            async.complete();
                        } else {
                            async.countDown();
                        }
                    })
                    .end();
        });

        logger.info("Awaiting...");
        async.awaitSuccess();
        logger.info("END - Accepted {} requests.", counter.get());
    }
}