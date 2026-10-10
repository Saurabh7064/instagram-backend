# Inter-Service Communication Resilience — Interview Revision Card

> **Question:** How would you handle latency, timeouts, and cascading failures between microservices?
>
> [Detailed source](../../questions/microservices/03-inter-service-communication-resilience.md)

## Key requirements

- Bound end-to-end waiting and resource use.
- Keep one slow dependency from taking down unrelated work.
- Make ambiguous writes and retries safe.

## Scale assumptions

- Fan-out and layered retries multiply attempts, so budgets are per user request, not per library call.

## Components

- A **deadline** bounds useful time; a **bulkhead** caps dependency concurrency; a **circuit breaker** stops likely failures.
- Idempotency record, bounded retry policy, fallback, and dependency telemetry.

## Request flow

1. Propagate the user deadline and trace context.
2. Give each hop a smaller timeout and concurrency limit.
3. Retry transient, idempotent work at one layer with backoff and jitter.
4. Open the circuit during sustained failure.
5. Fail fast or return an approved fallback; probe recovery gradually.

## Data model

- `Attempt(operationId, dependency, number, deadline, outcome, durationMs)`

## Three trade-offs

1. Short timeouts protect capacity but may cut off legitimate slow work.
2. Retries hide brief faults but add load and duplicate risk.
3. Async events decouple availability but add lag and reconciliation.

## Three failures and mitigations

1. **Retry storm:** one retry owner and a total attempt budget.
2. **Lost response after commit:** idempotency key plus status lookup.
3. **Fallback corruption:** allow stale data only for explicitly safe fields.

## Two-minute spoken answer

I start with the user deadline and decide which calls must be synchronous. Each downstream hop gets a smaller timeout and bounded concurrency, so slow success cannot consume every worker. One layer owns a small retry budget for transient failures, using backoff and jitter. Writes require idempotency because a timeout does not prove the server failed.

For sustained failure, a circuit breaker stops normal calls and later permits limited probes. Optional reads may use a documented stale fallback; correctness-critical operations fail safely or remain pending. Noncritical side effects such as notifications can move to durable events. I monitor latency, active calls, queue wait, attempts, rejections, circuit state, and fallback use. The design trades some fast rejection or staleness for a bounded blast radius and predictable recovery.

## Recall questions

1. Why can increasing a timeout reduce availability?
2. Which layer should own retries?
3. When is a fallback unsafe?
