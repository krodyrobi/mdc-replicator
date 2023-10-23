package com.example.mdcreplicator;

import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@RestController
public class SampleController {
    private final Logger logger = LoggerFactory.getLogger(SampleController.class);

    private final OkHttpService okHttpService;
    private final ObservationRegistry observationRegistry;

    public SampleController(
        OkHttpService okHttpService,
        ObservationRegistry observationRegistry
    ) {
        this.okHttpService = okHttpService;
        this.observationRegistry = observationRegistry;
    }

    @GetMapping("/")
    public String index() throws ExecutionException, InterruptedException {
        logger.info("in index {}", observationRegistry.getCurrentObservation());
        return okHttpService.call();
    }
}
