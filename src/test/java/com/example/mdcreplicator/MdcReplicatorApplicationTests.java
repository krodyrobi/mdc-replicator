package com.example.mdcreplicator;

import io.micrometer.core.instrument.kotlin.AsContextElementKt;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.BaggageInScope;
import io.micrometer.tracing.BaggageManager;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.reactor.MonoKt;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.function.BiConsumer;

import static com.example.mdcreplicator.filter.CorrelationIdFilter.CORRELATION_ID_HEADER_NAME;
import static com.example.mdcreplicator.filter.CorrelationIdFilter.createCorrelationId;

@SpringBootTest
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

    private final Logger logger = LoggerFactory.getLogger(MdcReplicatorApplicationTests.class);

    private void withSpan(BiConsumer<Tracer.SpanInScope, BaggageInScope> block) {
        Span span = this.tracer.nextSpan();
        try (
            var spanInScope = tracer.withSpan(span.start());
            var baggageInScope = baggageManager.createBaggageInScope(CORRELATION_ID_HEADER_NAME, createCorrelationId())
        ) {
            block.accept(spanInScope, baggageInScope);
        } finally {
            span.end();
        }
    }

    @Test
    void sample_test() {
        withSpan((span, baggage) -> {
            logger.info(baggage.get());
            MonoKt.<String>mono(
                    Dispatchers.getIO().plus(AsContextElementKt.asContextElement(observationRegistry)),
                    (scope, continuation) -> kotlinUtilClass.okHttpSuspend(continuation)
                )
                .block();
        });
    }
}
