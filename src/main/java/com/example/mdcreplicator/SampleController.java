package com.example.mdcreplicator;


import com.apollographql.apollo3.ApolloCall;
import com.apollographql.apollo3.ApolloClient;
import com.apollographql.apollo3.api.ApolloResponse;
import com.apollographql.apollo3.api.Query;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.reactor.MonoKt;
import kotlinx.coroutines.slf4j.MDCContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

@RestController
public class SampleController {
    private Logger logger = LoggerFactory.getLogger(SampleController.class);

    private final Repository repository;

    public SampleController(Repository client) {
        this.repository = client;
    }

    @GetMapping("/")
    public String getCeo() {
        logger.info("getCeo called");
        return repository.getCeo();
    }

}
