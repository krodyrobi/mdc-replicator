package com.example.mdcreplicator;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.BaggageManager;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Hooks;

import java.util.concurrent.ExecutionException;

import static com.example.mdcreplicator.filter.CorrelationIdFilter.CORRELATION_ID_HEADER_NAME;
import static com.example.mdcreplicator.filter.CorrelationIdFilter.createCorrelationId;
import static org.springframework.boot.test.context.SpringBootTest.UseMainMethod.ALWAYS;

@SpringBootTest(useMainMethod = ALWAYS, properties = {"app.scheduling.enable=false"})
@AutoConfigureObservability
class MdcReplicatorApplicationTests {
    private final Logger logger = LoggerFactory.getLogger(MdcReplicatorApplicationTests.class);

    @Autowired
    Tracer tracer;

    @Autowired
    BaggageManager baggageManager;

    @Autowired
    ObservationRegistry observationRegistry;

    @Autowired
    OkHttpService okHttpService;

    @BeforeAll
    public static void beforeAll() {
        Hooks.enableAutomaticContextPropagation();
    }

    @Test
    void test1() throws ExecutionException, InterruptedException {
        // no current observation
        logger.info("in test1 {}", observationRegistry.getCurrentObservation());
        try (var ignored = baggageManager.createBaggageInScope(CORRELATION_ID_HEADER_NAME, createCorrelationId())) {
            okHttpService.call();
        }
    }

    @Test
    void test2() {
        logger.info("in test2 {}", observationRegistry.getCurrentObservation());
        Observation.start("test", observationRegistry).observe(() -> {
            try (var ignored = baggageManager.createBaggageInScope(CORRELATION_ID_HEADER_NAME, createCorrelationId())) {
                logger.info("in test2 observe {}", observationRegistry.getCurrentObservation());
                try {
                    okHttpService.call();
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("failed", e);
                }
            }
        });
    }
}
