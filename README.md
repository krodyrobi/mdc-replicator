Some endpoints work completely some don't when wrapped with `MonoKt`
From examples in the repo seems like okhttp enqueue forgets about mdc

Expected:
- All logging points should return `x-request-id, traceId and spanId` in the log entry.
- Forwards/Sends `traceId/spanId` headers
- Forwards/sends `x-request-id`

Request bin to debug
https://public.requestbin.com/r/eny7z0f6m5g2
