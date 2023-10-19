package com.example.mdcreplicator;

import io.micrometer.core.instrument.kotlin.AsContextElementKt;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.BaggageManager;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.reactor.MonoKt;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Hooks;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import static com.example.mdcreplicator.KotlinUtilClass.URL;
import static com.example.mdcreplicator.filter.CorrelationIdFilter.CORRELATION_ID_HEADER_NAME;
import static com.example.mdcreplicator.filter.CorrelationIdFilter.createCorrelationId;
import static org.springframework.boot.test.context.SpringBootTest.UseMainMethod.ALWAYS;

@SpringBootTest(useMainMethod = ALWAYS)
@AutoConfigureObservability
class MdcReplicatorApplicationTests {
    @Autowired
    Tracer tracer;

    @Autowired
    BaggageManager baggageManager;

    @Autowired
    ObservationRegistry observationRegistry;

    @Autowired
    KotlinUtilClass kotlinUtilClass;

    @Autowired
    OkHttpClient okHttpClient;

    private final Logger logger = LoggerFactory.getLogger(MdcReplicatorApplicationTests.class);

    @BeforeAll
    public static void beforeAll() {
        Hooks.enableAutomaticContextPropagation();
    }

    @Test
    void sample_test() {
        Span span1 = this.tracer.nextSpan();
        try (
            var spanInScope = tracer.withSpan(span1.start());
            var baggageInScope = baggageManager.createBaggageInScope(CORRELATION_ID_HEADER_NAME, createCorrelationId())
        ) {
            logger.info(baggageInScope.get());
            MonoKt.<String>mono(
                    Dispatchers.getIO().plus(AsContextElementKt.asContextElement(observationRegistry)),
                    (scope, continuation) -> kotlinUtilClass.okHttpSuspend(continuation)
                )
                .block();
        } finally {
            span1.end();
        }
    }

    @Test
    void sample_test2() throws InterruptedException {
        Span span = this.tracer.nextSpan();
        try (
            var spanInScope = tracer.withSpan(span.start());
            var baggageInScope = baggageManager.createBaggageInScope(CORRELATION_ID_HEADER_NAME, createCorrelationId())
        ) {
            logger.info(baggageInScope.get());
            CountDownLatch latch = new CountDownLatch(1);
            okHttpClient.newCall(new Request.Builder().url(URL).get().build()).enqueue(
                new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        logger.info("on response");
                        latch.countDown();
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        logger.info("on response");
                        latch.countDown();
                    }
                }
            );
            latch.await();
        } finally {
            span.end();
        }
    }
}
