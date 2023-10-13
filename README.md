Hit `http://localhost:8080/` and you should see the current CEO.

- All logging points should return `x-request-id, traceId and spanId` in the log entry. (WORKING)
- OkHttp sends `traceId/spanId` headers (WORKING)
- OkHttp forwards `x-request-id` header (NOT WORKING)

To see this breakpoint in `OkHttpObservationInterceptor.intercept` on line `response = chain.proceed(newRequest);`
MDCContext at this point does have the `x-request-id` entry and does NOT have observability keys

