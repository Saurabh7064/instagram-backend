# High p99 with a Normal Average — Interview Revision Card

> **Question:** Average latency looks normal, but p99 is several seconds and some users complain. How would you find and fix the slow cohort?
>
> [Detailed answer](../../questions/production-scenarios/03-high-p99-with-normal-average.md)

## Key requirements

- Identify which requests form the slow tail.
- Protect affected users without degrading everyone.
- Verify the tail, not only the average, recovers.

## Scale assumptions

- At high volume, 1% can represent thousands of requests; preserve histograms by route and bounded dimensions.

## Components

- **p99 latency** is the duration at or below which 99% of requests finish.
- Histograms, cohort dimensions, representative traces, dependency spans, and change markers.

## Request flow

1. Validate the time window and sample count.
2. Split p99 by bounded cohort dimensions.
3. Compare fast and slow traces for one operation.
4. Find the first divergent wait, query, retry, or fan-out.
5. Mitigate that cohort; confirm p95/p99 recovery.

## Data model

- `LatencySample(time, route, region, version, cohort, durationMs, traceId)`

## Three trade-offs

1. More dimensions isolate cohorts but raise metric cardinality.
2. More tracing finds rare tails but raises cost and privacy risk.
3. Fallbacks lower latency but may be stale or partial.

## Three failures and mitigations

1. **Hot tenant/key:** isolate capacity or split the key.
2. **Cache miss tail:** coalesce misses and refresh before expiry.
3. **Runtime pause:** inspect allocation/GC evidence; reduce churn and tune from measurements.

## Two-minute spoken answer

An average can hide a small but large-in-absolute-numbers slow cohort. I validate p99 and sample volume, then split the histogram by bounded dimensions such as route, region, version, payload class, and cache result. I want “large uncached feed requests in one region are slow,” not “the service is slow.”

I compare fast and slow traces for that operation and find the first divergent span: connection wait, lock, slow shard, retry, fan-out, runtime pause, or cold cache. I contain only the affected path through fallback, traffic shift, or concurrency limits, then verify its percentile and user outcome. Request IDs stay in traces, not metric labels. Finally, I add a cohort-aware alert and a test recreating the tail.

## Recall questions

1. How can a normal average hide user pain?
2. Which dimensions avoid unbounded cardinality?
3. What distinguishes cause from downstream symptom?
