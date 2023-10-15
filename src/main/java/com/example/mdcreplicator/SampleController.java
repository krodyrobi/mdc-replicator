package com.example.mdcreplicator;

import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.reactor.MonoKt;
import kotlinx.coroutines.slf4j.MDCContext;
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

    public SampleController(OkHttpClient okHttpClient, KotlinDummyClass kotlinDummyClass) {
        this.okHttpClient = okHttpClient;
        this.kotlinDummyClass = kotlinDummyClass;
    }

    @GetMapping("/")
    public String index() {
        logger.info("index called");
        return MonoKt.<String>mono(
                Dispatchers.getIO().plus(new MDCContext()), // removing new MDCContext() stop propagation even for logs
                (scope, continuation) -> {
                    logger.info("inside continuation lambda");
                    return kotlinDummyClass.exampleSuspend(continuation);
                }
            )
            .block();
    }

    @GetMapping("/raw")
    public String raw() throws IOException {
        logger.info("raw called");
        Request request = new Request.Builder().url("https://example.com").get().build();
        return okHttpClient.newCall(request).execute().body().string();
    }
}
