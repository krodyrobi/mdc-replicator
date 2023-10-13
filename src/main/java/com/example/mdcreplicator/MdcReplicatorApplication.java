package com.example.mdcreplicator;

import com.apollographql.apollo3.ApolloClient;
import com.apollographql.apollo3.network.OkHttpExtensionsKt;
import io.micrometer.core.instrument.binder.okhttp3.OkHttpObservationInterceptor;
import io.micrometer.observation.ObservationRegistry;
import okhttp3.OkHttpClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationProperties;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Hooks;

import java.time.Duration;

@SpringBootApplication
public class MdcReplicatorApplication {

    public static void main(String[] args) {
        Hooks.enableAutomaticContextPropagation();
        SpringApplication.run(MdcReplicatorApplication.class, args);
    }

    @Bean
    public ApolloClient spaceXClient(
        ObservationRegistry observationRegistry,
        ObservationProperties observationProperties
    ) {
        var graphQlClient = new ApolloClient.Builder();

        String observationName = observationProperties.getHttp().getClient().getRequests().getName();

        // TODO intercept does not append baggage 'x-request-id', it does however
        var interceptor = OkHttpObservationInterceptor
            .builder(observationRegistry, observationName)
            .build();

        var httpClient = new OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .callTimeout(Duration.ofSeconds(3));

        OkHttpExtensionsKt.okHttpClient(graphQlClient, httpClient.build());

        return graphQlClient
            .serverUrl("https://spacex-production.up.railway.app/graphql")
            .canBeBatched(false)
            .build();
    }
}


