package com.example.mdcreplicator;

import io.micrometer.context.ContextExecutorService;
import io.micrometer.context.ContextSnapshotFactory;
import io.micrometer.core.instrument.binder.okhttp3.OkHttpObservationInterceptor;
import io.micrometer.observation.ObservationRegistry;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.internal.Util;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationProperties;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Hooks;

import java.time.Duration;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class MdcReplicatorApplication {

    public static void main(String[] args) {
        Hooks.enableAutomaticContextPropagation();
        SpringApplication.run(MdcReplicatorApplication.class, args);
    }

    @Bean
    public OkHttpClient rawClient(
        ObservationRegistry observationRegistry,
        ObservationProperties observationProperties
    ) {
        String observationName = observationProperties.getHttp().getClient().getRequests().getName();

        var interceptor = OkHttpObservationInterceptor
            .builder(observationRegistry, observationName)
            .build();

        var executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
            new SynchronousQueue<>(), Util.threadFactory("OkHttp Dispatcher", false));
        var instrumentedExecutor = ContextExecutorService.wrap(executor, ContextSnapshotFactory.builder().build()::captureAll);

        var httpClient = new OkHttpClient.Builder()
            .dispatcher(new Dispatcher(instrumentedExecutor))
            .addInterceptor(interceptor)
            .callTimeout(Duration.ofSeconds(3));

        return httpClient.build();
    }
}


