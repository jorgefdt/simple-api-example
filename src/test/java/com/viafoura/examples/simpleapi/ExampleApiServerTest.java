package com.viafoura.examples.simpleapi;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Repeat;
import io.vertx.ext.unit.junit.RepeatRule;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@RunWith(VertxUnitRunner.class)
public class ExampleApiServerTest {
    private static final Logger logger = LogManager.getLogger(ExampleApiServerTest.class);
    private static final String INPUT_TEST_FILE = "input-sample.txt";

    @Rule
    public RepeatRule repeatRule = new RepeatRule();

    @Rule
    public RunTestOnContext ctxRule = new RunTestOnContext();
    private final AtomicInteger attemptedRequestsCounter = new AtomicInteger();
    private final AtomicInteger acceptedRequestsCounter = new AtomicInteger();

    @BeforeClass
    public static void suiteSetUp() throws Exception {
        logger.info("Setting up suite.");
        AppLauncher.startServer(INPUT_TEST_FILE);
    }


    @Test
    public void getPalindromes(TestContext ctx) {
        doGetExpectingBodyValue(ctx, AppConfig.GET_WORDS_HANDLER_PATH, "[racecar, abccba]");
    }


    @Test
    public void getPalindromesCount(TestContext ctx) {
        doGetExpectingBodyValue(ctx, AppConfig.COUNT_WORDS_HANDLER_PATH, "2");
    }


    @Repeat(value = 500, silent = true)
    @Test
    public void getPalindromesCountRepeated(TestContext ctx) {
        doGetExpectingBodyValue(ctx, AppConfig.COUNT_WORDS_HANDLER_PATH, "2");
    }


    @Test
    public void getPalindromesCountLimited(TestContext ctx) {
        doAsyncGets(ctx, 400, AppConfig.COUNT_WORDS_HANDLER_PATH);
    }


    private void doAsyncGets(TestContext ctx, int numRequests, String path) {
        final Vertx vertx = Vertx.vertx();
        final Async async = ctx.async(numRequests);
        IntStream.range(0, numRequests).forEach(x -> {

            final int thisIndex = this.attemptedRequestsCounter.getAndIncrement();
            vertx.createHttpClient()
                    .get(AppConfig.APP_SERVER_PORT, "localhost", path)
                    .handler(res -> {
                        final int status = res.statusCode();
                        if (status == HttpResponseStatus.OK.code()) {
                            this.acceptedRequestsCounter.incrementAndGet();
                            res.bodyHandler(body -> {
                                final String bodyValue = body.toString();
                                logger.info("doAsyncGets {}: OK - got body: '{}'", thisIndex, bodyValue);
                            });
                        } else if (status == HttpResponseStatus.TOO_MANY_REQUESTS.code()) {
                            logger.warn("doAsyncGets {}: Rejected: {}", thisIndex, res.statusMessage());
                        } else {
                            logger.error("doAsyncGets {}: FAILED: {}", thisIndex, res.statusMessage());
                        }

                        // Sync different threads.
                        final int count = countDownOrComplete(async);
                        logger.info("doAsyncGets {}: Thread count: '{}'", thisIndex, count);
                    })
                    .exceptionHandler(t -> {
                        // NOTE: some requests can fall here because the server side configuration is limiting the max number of simultaneous connections.
                        // Sync different threads.
                        final int count = countDownOrComplete(async);
                        logger.error("doAsyncGets {}: Thread count: {}, EXCEPTION: {}", thisIndex, count, t.getMessage());
                    })
                    .end();

        });

        logger.info("Awaiting...");
        async.awaitSuccess();
        logger.info("doAsyncGets: END - Accepted {} requests.", this.acceptedRequestsCounter.get());
    }

    private int countDownOrComplete(Async async) {
        synchronized (async) {
            final int count = async.count();
            if (count == 0) {
                async.complete();
            } else {
                async.countDown();
            }
            return async.count();
        }
    }


    // == Helpers


    private void doGetExpectingBodyValue(final TestContext ctx, final String path, final String expectedBody) {
        final Async async = ctx.async();
        final Vertx vertx = ctxRule.vertx();

        final int thisIndex = attemptedRequestsCounter.getAndIncrement();
        vertx.createHttpClient()
                .getNow(AppConfig.APP_SERVER_PORT, "localhost", path, res -> {
                    final int status = res.statusCode();
                    if (status == HttpResponseStatus.OK.code()) {
                        res.bodyHandler(body -> {
                            final String bodyValue = body.toString();
                            logger.info("doGet {}: OK - got body: '{}'", thisIndex, bodyValue);
                            ctx.assertEquals(expectedBody, bodyValue);
                        });
                    } else if (status == HttpResponseStatus.TOO_MANY_REQUESTS.code()) {
                        logger.warn("doGet {}: Rejected: {}", thisIndex, res.statusMessage());
                    } else {
                        logger.error("doGet {}: FAILED: {}", thisIndex, res.statusMessage());
                    }
                    async.complete();
                });
    }
}
