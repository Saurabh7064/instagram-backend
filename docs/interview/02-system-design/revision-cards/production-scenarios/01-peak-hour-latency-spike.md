# Peak-Hour Latency Spike — Interview Revision Card

> **Question:** A service works normally, but at peak traffic its response time rises from 200 ms to 3–4 seconds. How would you investigate, and what would you check first?
>
> [Detailed answer](../../questions/production-scenarios/01-peak-hour-latency-spike.md)

## Key requirements

- Bound user impact and protect critical traffic.
- Find the first saturated resource or slow dependency.
- Restore latency without hiding the cause.

## Scale assumptions

- Compare normal and peak request rate, p95/p99 latency, concurrency, and dependency capacity; do not assume traffic alone caused the slowdown.

## Components

- **Saturation** means a finite resource—workers, connections, CPU, or queue slots—is near its limit.
- Gateway/load balancer, service instances, database/cache/downstreams, and metrics/traces/logs.

## Request flow

1. Bound affected routes, users, regions, and time.
2. Compare peak with baseline rate, errors, latency, and saturation.
3. Trace slow requests to the longest wait.
4. Check retry amplification.
5. Repair or contain the bottleneck; verify p95/p99 recovery.

## Data model

- `RequestSample(time, route, region, version, traceId, status, durationMs)`

## Three trade-offs

1. Scaling out is fast but useless when the database is the limit.
2. Larger queues reduce rejection briefly but increase waiting and memory.
3. Caching reduces reads but introduces stale data and invalidation work.

## Three failures and mitigations

1. **Worker saturation:** cap concurrency and shed low-priority work.
2. **Database contention:** fix slow queries/locks before enlarging pools.
3. **Retry storm:** use one bounded retry layer with backoff and jitter.

## Two-minute spoken answer

I first bound the problem by route, region, tenant, and version. I compare baseline with peak request rate, errors, p95/p99 latency, and saturation of CPU, workers, queues, connections, and dependencies. Slow traces reveal the first wait consuming the latency budget. Low CPU may still mean requests are blocked on a pool, lock, or network call.

I check whether retries amplified load. I protect critical traffic by rejecting optional work, limiting concurrency, disabling expensive features, or adding only proven capacity. I verify the original percentile and business outcome recover. Afterwards I reproduce peak load, fix the bottleneck, and alert on the saturation signal that rose before latency.

## Recall questions

1. Why can normal CPU coexist with high latency?
2. Which comparison separates load growth from a code regression?
3. When would scaling out fail to help?
