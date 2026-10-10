# Bulkhead Isolation — Interview Revision Card

> **Question:** What is bulkhead isolation, and how would you apply it in microservices?
>
> [Detailed source](../../questions/microservices/05-bulkhead-isolation.md)

## Key requirements

- Contain a slow workload’s resource use.
- Preserve capacity for critical, unrelated operations.
- Reject or degrade predictably when a limit is full.

## Scale assumptions

- Required concurrency grows with arrival rate × time in flight; a slowdown can multiply occupied slots.

## Components

- A **bulkhead** is a bounded resource partition for one workload or dependency.
- Separate concurrency limits/queues, timeouts, circuit breaker, priority policy, and saturation metrics.

## Request flow

1. Classify incoming work by dependency and business priority.
2. Admit it only within that class’s concurrency limit.
3. Use a small or zero bounded queue.
4. On full capacity, reject or return an approved fallback.
5. A timeout releases trapped slots; limited probes test recovery.

## Data model

- `WorkloadLimit(name, maxActive, maxQueued, timeoutMs, fallbackPolicy)`

## Three trade-offs

1. Reserved capacity improves isolation but may sit idle.
2. More pools isolate finely but increase tuning and operational complexity.
3. Larger queues absorb bursts but increase tail latency during sustained overload.

## Three failures and mitigations

1. **Shared database still saturates:** verify the entire resource path.
2. **Retries bypass limit:** route every attempt through the same bulkhead.
3. **Limit too small:** tune with load tests and business priority.

## Two-minute spoken answer

Bulkhead isolation prevents one slow dependency or workload from consuming every shared thread, connection, or worker. I classify work by failure boundary and importance, then give each class a bounded concurrency limit and usually a very small queue. When full, optional work degrades or fails quickly, preserving capacity for critical paths.

For example, slow like-status enrichment should not exhaust feed retrieval or post creation. Calls also need deadlines and a circuit breaker; retries pass through the same limit so they cannot bypass protection. I size limits from arrival rate, tail latency, downstream capacity, and failure tests—not arbitrary percentages. The cost is lower peak utilization and more configuration. Separate executors are insufficient if they still share a saturated database, heap, or CPU, so I verify the full resource path and monitor active work, queue wait, rejections, fallback use, and user outcomes.

## Recall questions

1. Why can a larger bulkhead worsen an incident?
2. What shared resource can defeat isolation?
3. When is a small queue useful?
