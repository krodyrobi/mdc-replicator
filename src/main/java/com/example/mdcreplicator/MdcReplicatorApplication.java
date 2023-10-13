package com.example.mdcreplicator;

import io.micrometer.core.instrument.binder.okhttp3.OkHttpObservationInterceptor;
import io.micrometer.observation.ObservationRegistry;
import okhttp3.OkHttpClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationProperties;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Duration;

@SpringBootApplication
public class MdcReplicatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(MdcReplicatorApplication.class, args);
    }

    @Bean
    public OkHttpClient rawClient(
        ObservationRegistry observationRegistry,
        ObservationProperties observationProperties
    ) {
        String observationName = observationProperties.getHttp().getClient().getRequests().getName();

        // TODO intercept does not append baggage 'x-request-id', it does however
        var interceptor = OkHttpObservationInterceptor
            .builder(observationRegistry, observationName)
            .build();

        var httpClient = new OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .callTimeout(Duration.ofSeconds(3));

        return httpClient.build();
    }
}


