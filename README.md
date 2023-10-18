Running the same block of code as in the controller inside a test function and manually supplying baggages
does not work.

Expected:
- All logging points should return `x-request-id, traceId and spanId` in the log entry.
- Sends `traceId/spanId` headers
- Sends `x-request-id`

Request bin to debug
https://public.requestbin.com/r/eny7z0f6m5g2
