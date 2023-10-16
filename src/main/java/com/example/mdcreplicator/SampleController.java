package com.example.mdcreplicator;

import io.micrometer.core.instrument.kotlin.AsContextElementKt;
import io.micrometer.observation.ObservationRegistry;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.reactor.MonoKt;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class SampleController {
    private final Logger logger = LoggerFactory.getLogger(SampleController.class);

    private final OkHttpClient okHttpClient;
    private final KotlinDummyClass kotlinDummyClass;
    private final ObservationRegistry observationRegistry;

    public SampleController(
        OkHttpClient okHttpClient,
        KotlinDummyClass kotlinDummyClass,
        ObservationRegistry observationRegistry
    ) {
        this.okHttpClient = okHttpClient;
        this.kotlinDummyClass = kotlinDummyClass;
        this.observationRegistry = observationRegistry;
    }

    // fine
    @GetMapping("/blockingOkHttp")
    public String blockingOkHttp() throws IOException {
        logger.info("blockingOkHttp called");
        Request request = new Request.Builder().url(KotlinDummyClass.URL + "/blockingOkHttp").get().build();
        return okHttpClient.newCall(request).execute().body().string();
    }

    // enqueue seems to forget context
    @GetMapping("/monoOkHttp")
    public String monoOkHttp() {
        logger.info("monoOkHttp called");
        return MonoKt.<String>mono(
                Dispatchers.getIO().plus(AsContextElementKt.asContextElement(observationRegistry)),
                (scope, continuation) -> {
                    logger.info("inside continuation lambda");
                    return kotlinDummyClass.okHttpSuspend(continuation);
                }
            )
            .block();
    }

    // fine
    @GetMapping("/monoBlockingOkHttp")
    public String monoBlockingOkHttp() {
        logger.info("monoBlockingOkHttp called");
        return MonoKt.<String>mono(
                Dispatchers.getIO().plus(AsContextElementKt.asContextElement(observationRegistry)),
                (scope, continuation) -> {
                    logger.info("inside continuation lambda");
                    return kotlinDummyClass.blockingOkHttpSuspend(continuation);
                }
            )
            .block();
    }

    // fine
    @GetMapping("/monoRestTemplate")
    public String restTemplate() {
        logger.info("monoRestTemplate called");
        return MonoKt.<String>mono(
                Dispatchers.getIO().plus(AsContextElementKt.asContextElement(observationRegistry)),
                (scope, continuation) -> {
                    logger.info("inside continuation lambda");
                    return kotlinDummyClass.restTemplateSuspend(continuation);
                }
            )
            .block();
    }

    // fine
    @GetMapping("/monoWebClient")
    public String monoWebClient() {
        logger.info("monoWebClient called");
        return MonoKt.<String>mono(
                Dispatchers.getIO().plus(AsContextElementKt.asContextElement(observationRegistry)),
                (scope, continuation) -> {
                    logger.info("inside continuation lambda");
                    return kotlinDummyClass.webClientSuspend(continuation);
                }
            )
            .block();
    }

}
