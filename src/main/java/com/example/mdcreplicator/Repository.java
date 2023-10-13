package com.example.mdcreplicator;


import com.apollographql.apollo3.ApolloCall;
import com.apollographql.apollo3.ApolloClient;
import com.apollographql.apollo3.api.ApolloResponse;
import com.apollographql.apollo3.api.Query;
import com.example.mdcreplicator.graphql.ExampleQuery;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.reactor.MonoKt;
import kotlinx.coroutines.slf4j.MDCContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

@Component
public class Repository {
    private final Logger logger = LoggerFactory.getLogger(Repository.class);
    private final ApolloClient client;

    public Repository(ApolloClient client) {
        this.client = client;
    }

    public String getCeo() {
        logger.info("before");
        Mono<ApolloResponse<ExampleQuery.Data>> result =
            apolloCallToMono(() -> {
                logger.info("in supplier");
                return client.query(new ExampleQuery());
            });

        var output = result.flatMap(response -> {
                logger.info("in mono flatmap");

                var exception = response.exception;
                if (exception != null) {
                    return Mono.error(exception);
                }

                // Should not happen as exception will not be null
                if (response.data == null) {
                    return Mono.just("N/A");
                }

                if (response.data.company == null) {
                    return Mono.just("N/A");
                }
                return Mono.just(response.data.company.ceo != null ? response.data.company.ceo : "N/A");
            })
            .doOnEach(it -> logger.info("on each"))
            .block();
        logger.info("after");
        return output;
    }

    private <T extends Query.Data> Mono<ApolloResponse<T>> apolloCallToMono(Supplier<ApolloCall<T>> callSupplier) {
        logger.info("inside apolloCallToMono");
        return MonoKt.mono(
            Dispatchers.getIO().plus(new MDCContext()), // removing new MDCContext() stop propagation even for logs
            (scope, continuation) -> {
                logger.info("inside apolloCallToMono continuation lambda");
                return callSupplier.get().execute(continuation);
            }
        );
    }
}
