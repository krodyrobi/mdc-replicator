package com.example.mdcreplicator.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

public class CorrelationIdFilter extends OncePerRequestFilter {
    public static final String CORRELATION_ID_HEADER_NAME = "x-request-id";

    public static String createCorrelationId() {
        return UUID.randomUUID().toString();
    }

    @Override
    protected void doFilterInternal(
        @NotNull HttpServletRequest request,
        @NotNull HttpServletResponse response,
        @NotNull FilterChain filterChain
    ) throws ServletException, IOException {
        MutableHeaderHttpServletRequest newRequest = new MutableHeaderHttpServletRequest(request);

        String correlationId = newRequest.getHeader(CORRELATION_ID_HEADER_NAME);
        if (correlationId == null) {
            correlationId = createCorrelationId();
            newRequest.putHeader(CORRELATION_ID_HEADER_NAME, correlationId);
        }

        response.addHeader(CORRELATION_ID_HEADER_NAME, correlationId);
        filterChain.doFilter(newRequest, response);
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }
}
