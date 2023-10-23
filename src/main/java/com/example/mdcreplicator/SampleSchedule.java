package com.example.mdcreplicator;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.BaggageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.ExecutionException;

import static com.example.mdcreplicator.filter.CorrelationIdFilter.CORRELATION_ID_HEADER_NAME;
import static com.example.mdcreplicator.filter.CorrelationIdFilter.createCorrelationId;

@ConditionalOnProperty(
    value = "app.scheduling.enable", havingValue = "true", matchIfMissing = true
)
@EnableScheduling
@Configuration
public class SampleSchedule {
    private static final Logger logger = LoggerFactory.getLogger(SampleSchedule.class);
    private final ObservationRegistry observationRegistry;
    private final OkHttpService okHttpService;
    private final BaggageManager baggageManager;

    public SampleSchedule(ObservationRegistry observationRegistry, OkHttpService okHttpService, BaggageManager baggageManager) {
        this.observationRegistry = observationRegistry;
        this.okHttpService = okHttpService;
        this.baggageManager = baggageManager;
    }

    @Scheduled(fixedDelay = 10_000L)
    public void run() throws ExecutionException, InterruptedException {
        // has current observation, but I assume the scheduled pool not instrumented in latest snapshot yet
        logger.info("in schedule {}", observationRegistry.getCurrentObservation());
        try (var ignored = baggageManager.createBaggageInScope(CORRELATION_ID_HEADER_NAME, createCorrelationId())) {
            okHttpService.call();
        }
    }
}
