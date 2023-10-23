package com.example.mdcreplicator;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.BaggageManager;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class OkHttpService {
    private final Logger logger = LoggerFactory.getLogger(OkHttpService.class);
    public static final String URL = "https://eny7z0f6m5g2.x.pipedream.net";
    private final OkHttpClient okHttpClient;
    private final ObservationRegistry observationRegistry;
    private final Tracer tracer;
    private final BaggageManager baggageManager;

    public OkHttpService(
        OkHttpClient okHttpClient,
        ObservationRegistry observationRegistry,
        Tracer tracer,
        BaggageManager baggageManager
    ) {
        this.okHttpClient = okHttpClient;
        this.observationRegistry = observationRegistry;
        this.tracer = tracer;
        this.baggageManager = baggageManager;
    }

    public String call() throws InterruptedException, ExecutionException {
        logger.info("in call {}", observationRegistry.getCurrentObservation());
        CompletableFuture<String> result = new CompletableFuture<>();

        Span span = this.tracer.nextSpan();
        try (var ignored = tracer.withSpan(span.start())) {
            logger.info("in call baggage {}", baggageManager.getAllBaggage());

            Request request = new Request.Builder().url(URL).get().build();
            okHttpClient.newCall(request).enqueue(
                new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        logger.info("on failure");
                        result.completeExceptionally(e);
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) {
                        logger.info("on response");
                        try {
                            ResponseBody body = response.body();
                            result.complete(body == null ? "N/A" : body.string());
                        } catch (IOException e) {
                            result.completeExceptionally(e);
                        }
                    }
                }
            );
            return result.get();
        } finally {
            span.end();
        }
    }

}
