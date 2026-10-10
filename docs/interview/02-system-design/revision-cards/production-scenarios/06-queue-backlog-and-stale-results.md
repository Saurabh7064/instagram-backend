# Queue Backlog and Stale Results — Interview Revision Card

> **Question:** Post creation succeeds, but notifications or feeds are 30 minutes late and queue lag keeps growing. How would you find and fix the bottleneck?
>
> [Detailed answer](../../questions/production-scenarios/06-queue-backlog-and-stale-results.md)

## Key requirements

- Restore freshness without losing or duplicating effects.
- Find whether arrival rate, processing time, or failures changed.
- Replay safely after repair.

## Scale assumptions

- Lag grows whenever sustained publish rate exceeds completion rate.

## Components

- **Consumer lag** measures unprocessed work; a **dead-letter queue** isolates repeated failures; **idempotency** makes replay safe.
- Broker partitions, consumers, dependencies, and freshness metrics.

## Request flow

1. Compare publish rate, completion rate, lag age, and failures.
2. Split lag by partition, event type, version, and instance.
3. Trace processing; inspect poison messages and dependencies.
4. Pause harmful production, isolate bad events, or add safe parallelism.
5. Repair, replay idempotently, and reconcile projections.

## Data model

- `Event(eventId, aggregateId, type, version, occurredAt, payload)` plus processed `eventId`.

## Three trade-offs

1. More consumers improve throughput only when partitions and dependencies allow it.
2. More partitions add parallelism but complicate ordering and rebalancing.
3. Dead-lettering restores flow but creates an explicit recovery obligation.

## Three failures and mitigations

1. **Poison message:** quarantine with reason; fix and replay.
2. **Slow dependency:** bound calls, batch work, and apply backpressure.
3. **Duplicate replay:** deduplicate by event ID and reconcile outcomes.

## Two-minute spoken answer

Successful writes with stale results mean asynchronous work is behind. I compare publish and completion rates, oldest-message age, retries, and dead letters, then split lag by partition, event type, version, and instance. One stuck partition suggests a poison message or hot key; uniform lag suggests low throughput or a shared dependency.

I inspect each processing stage before adding consumers. They cannot help without partitions or database capacity. I pause harmful production, quarantine repeated failures, or prioritize critical events. After fixing code, schema, capacity, or dependency, I replay by event ID and reconcile source records with derived state. Recovery requires lag age and business freshness to return to target.

## Recall questions

1. What does one lagging partition suggest?
2. When will adding consumers not help?
3. How do you prove replay produced correct state?
