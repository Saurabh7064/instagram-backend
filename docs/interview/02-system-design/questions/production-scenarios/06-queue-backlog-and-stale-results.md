# Queue Backlog and Stale Results

## Question

Post creation succeeds, but notifications arrive 30 minutes late and feed updates are increasingly stale. Queue lag keeps growing.

Your manager asks:

**“How would you find the bottleneck, recover safely, and prevent another backlog?”**

> [One-page revision card](../../revision-cards/production-scenarios/06-queue-backlog-and-stale-results.md)

## Why this problem exists

A queue separates accepting work from processing it. That improves availability during short bursts, but it does not create consumer capacity. When producers publish faster than consumers complete for a sustained period, unprocessed work and its age must grow.

The investigation must distinguish increased input, slower processing, one blocked partition, repeatedly failing messages, and a saturated dependency. Recovery must preserve accepted work and avoid duplicating side effects during replay.

## Analogy: parcels at a sorting depot

A depot can hold parcels during a short delivery rush. If trucks arrive faster than staff sort them all day, the oldest parcel becomes later no matter how large the floor is. One damaged parcel blocking a conveyor differs from every station working too slowly.

The analogy stops because messages can be redelivered, processed out of order, or produce effects in other systems. An “already handled” parcel cannot simply be recognized unless the software records that fact.

## Terms to establish

- **Consumer lag** is unprocessed work, measured by count or by the age of the oldest item.
- A **partition** is an ordered subset of a queue that limits and organizes parallel consumption.
- A **dead-letter queue (DLQ)** holds messages that repeatedly fail so they do not block healthy work.
- **Idempotent consumption** makes redelivery produce one logical effect.

## Investigation and recovery sequence

### 1. Measure arrival, service, and freshness

Compare messages published per second with successful completions per second. Track oldest-message age, processing duration, retry count, DLQ volume, and the user-visible age of a notification or feed projection. A queue depth of 100 may be harmless at 10,000 completions per second; a 30-minute oldest item is not.

### 2. Split the backlog

Break lag down by partition, event type, consumer version, instance, and outcome. One stuck partition suggests a poison message, hot key, or ordering block. Uniform lag suggests a shared dependency or insufficient total throughput. Confirm whether consumers are alive, repeatedly restarting, or rebalancing.

### 3. Find the slow stage

Trace receipt, validation, database work, remote calls, and acknowledgment. Check database connections, locks, API rate limits, and message sizes. Add consumers only when partitions and dependencies have headroom; otherwise parallelism moves the bottleneck.

### 4. Contain and catch up

Pause a harmful producer feature, isolate poison messages with their reason, prioritize critical events, or temporarily add proven capacity. Do not discard accepted events to make the graph green. After repair, replay idempotently and reconcile source records with derived state.

## Concrete event walkthrough

1. `PostCreated(postId=42, eventId=e7)` is published successfully.
2. Feed consumers normally materialize it in under five seconds.
3. A new consumer version cannot parse one optional field and retries the same event endlessly.
4. Its partition stops advancing while other partitions remain current.
5. Metrics show high age on one partition and repeated failures for `e7`.
6. The team quarantines `e7`, deploys a compatible parser, then replays it.
7. The consumer records `e7` as processed in the same transaction as the projection update.
8. A reconciliation job verifies every source post appears in the intended feed projection.

The observable recovery is decreasing oldest-message age plus restored feed freshness, not simply a restarted consumer.

## Instagram-backend mapping

The current project has no message broker or asynchronous feed projection. [PostService.create(...)](../../../../../src/main/java/com/instagram/backend/service/PostService.java) saves a post synchronously, and [PostService.feed(...)](../../../../../src/main/java/com/instagram/backend/service/PostService.java) reads PostgreSQL directly. Therefore queue metrics, a DLQ, inbox deduplication, and replay are **proposed**, not implemented.

A future design could publish `PostCreated` through a transactional outbox after the post commit, then let feed and notification consumers process it. The event would need `eventId`, `postId`, `type`, `version`, and `occurredAt`; each consumer would store processed `eventId` with its local update. Existing bounded business metrics in [InstagramMetrics.java](../../../../../src/main/java/com/instagram/backend/observability/InstagramMetrics.java) show the style, but new freshness and lag signals would be required.

## Choices and trade-offs

- **Add consumers** when partitions and downstream capacity permit parallelism. Avoid it when one partition is blocked or the database is saturated; it adds rebalance and connection pressure.
- **Add partitions** when sustained parallel throughput is needed. Do not do it casually when strict global order is required; repartitioning and per-key ordering become harder.
- **Move a message to a DLQ** when repeated failure would block healthy work. This restores flow but creates a mandatory investigation, repair, replay, and reconciliation task.

## Failure drill

**Prediction:** a consumer crashes after writing a notification but before acknowledging the message. **Symptom and detection:** the broker redelivers the same `eventId`, and duplicate notification attempts appear. **Containment:** stop side effects that cannot deduplicate and retain the message. **Recovery:** look up the recorded outcome and acknowledge without sending twice, or reconcile a duplicate if the provider lacks idempotency. **Prevention:** use a consumer inbox/idempotency key, make acknowledgment follow durable outcome recording, and test crash-before-ack.

## Two-to-three-minute interview answer

I would start with rates and age: producer rate, successful consumer rate, oldest-message age, processing duration, retries, dead letters, and the user-visible freshness objective. Then I would split lag by partition, event type, version, and consumer. One stuck partition points to a poison event or hot key; uniform lag points to insufficient throughput or a shared slow dependency.

I trace processing into validation, database work, remote calls, and acknowledgment before scaling. More consumers help only when partitions and dependencies have headroom. During impact I may pause a harmful producer, quarantine repeatedly failing events with evidence, prioritize critical work, or add tested capacity. I preserve accepted messages. After fixing code, schema, or capacity, I replay using `eventId` for idempotency and reconcile source posts against feed or notification state. Recovery requires oldest-message age and business freshness to return to target. A low error count alone is insufficient because a slow consumer can succeed while falling further behind.

## Practice status

The artifact is `READY`; comprehension remains `TODO`. Mark it practiced only after diagnosing one-partition versus uniform lag, explaining safe replay, and answering the final questions before revealing them.

## Questions and explained answers

<details>
<summary>When will adding more consumers fail to reduce lag?</summary>

It fails when there are not enough partitions, one ordered partition is blocked, or the real constraint is a saturated database or remote dependency. Added consumers then sit idle or intensify the shared bottleneck.

</details>

<details>
<summary>Why track oldest-message age as well as queue depth?</summary>

Depth has no meaning without traffic and processing rates. Age directly reflects user-visible staleness: a small queue containing one blocked 30-minute-old event can violate the product objective.

</details>

<details>
<summary>How do you prove replay restored correct state?</summary>

Use stable event IDs to prevent duplicate effects, then reconcile authoritative source records with the derived feed or notification records. Lag reaching zero does not prove that failed events produced the intended outcome.

</details>
