# Downstream Timeout and Retry Storm — Interview Revision Card

> **Question:** A downstream service becomes slow, upstream timeouts rise, and traffic to that dependency triples. How would you stop the cascading failure?
>
> [Detailed answer](../../questions/production-scenarios/05-downstream-timeout-and-retry-storm.md)

## Key requirements

- Bound how long and how much work waits.
- Prevent retries from multiplying overload.
- Preserve safe behavior for uncertain writes.

## Scale assumptions

- Three layers making three attempts can create up to 27 bottom-level attempts for one request.

## Components

- A **deadline** is remaining time; **backoff with jitter** spreads retries; a **circuit breaker** temporarily blocks failing calls.
- Dependency limit, idempotency store, fallback, and telemetry.

## Request flow

1. Propagate the caller deadline.
2. Apply per-hop timeouts and concurrency limits.
3. Retry only transient, idempotent operations at one layer.
4. Open the circuit when failure evidence crosses a threshold.
5. Return an approved fallback or bounded failure; probe recovery gradually.

## Data model

- `Attempt(operationId, dependency, attempt, deadline, outcome, durationMs)`

## Three trade-offs

1. Short timeouts protect resources but may cut off slow successes.
2. Retries hide brief faults but add load and duplicate risk.
3. Fallbacks preserve availability but can be stale or incomplete.

## Three failures and mitigations

1. **Retry amplification:** keep one retry owner and a total attempt budget.
2. **Duplicate write:** require an idempotency key and status lookup.
3. **Circuit never recovers:** use limited half-open probes and clear metrics.

## Two-minute spoken answer

Slow success holds finite resources while requests keep arriving. I allocate the user deadline into smaller downstream budgets and cap concurrency per dependency. Once full, optional work degrades or fails quickly instead of queueing indefinitely.

One layer owns retries, only for transient failures and safe operations, with backoff, jitter, and a total attempt budget. Writes need idempotency because timeout does not prove failure. A circuit breaker blocks a sustained fault and later admits limited probes. I monitor dependency latency, active calls, timeouts, retries, rejections, and fallback use. During an incident I disable redundant retries and optional fan-out, then verify user latency and downstream load fall together.

## Recall questions

1. Why does a longer timeout sometimes worsen availability?
2. When is a retry safe?
3. What signal should control circuit recovery?
