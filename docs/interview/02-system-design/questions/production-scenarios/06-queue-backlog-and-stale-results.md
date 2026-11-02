# Queue Backlog and Stale Results

## Question

Post creation succeeds, but notifications arrive 30 minutes late and feed updates are increasingly stale. Queue lag keeps growing.

Your manager asks:

**“How would you find the bottleneck, recover safely, and prevent another backlog?”**

> [One-page revision card](../../revision-cards/production-scenarios/06-queue-backlog-and-stale-results.md)

## The write succeeded. The work it promised has not happened

The user publishes a post and receives success. The post is durable, but a worker still needs to update feeds or send notifications. If that worker falls behind, request success and user experience begin telling different stories: Publish works while followers see old information.

A queue lets us accept work before processing it, which helps absorb short bursts. It cannot make the worker complete more per second. Sustained backlog means work is arriving faster than it completes somewhere in the processing path—or that a piece of work has blocked the path entirely.

Those cases need different repairs. More workers may help a uniformly overloaded workload. They may do nothing for one ordered stream stuck retrying the same malformed event. First find which kind of waiting we have.

## A bigger depot does not make sorting faster

A depot can hold parcels during a short delivery rush. If trucks arrive faster than staff sort them all day, the oldest parcel becomes later no matter how large the floor is. One damaged parcel blocking a conveyor differs from every station working too slowly.

The analogy stops because messages can be redelivered, processed out of order, or produce effects in other systems. An “already handled” parcel cannot simply be recognized unless the software records that fact.

## Count pending work, and ask how old it is

**Consumer lag** is work accepted but not yet processed. Its count tells us how much remains; its age tells us how long the oldest accepted work has waited. A short queue with one 30-minute-old post can still violate the product's freshness promise.

In a partitioned broker, a **partition** is an ordered subset of the message stream. Parallelism is limited by how that broker assigns partitions to workers. One blocked partition can leave other partitions healthy; this model is not a promise that every queue product uses partitions in the same way.

A **dead-letter queue (DLQ)** is a quarantine for messages that repeatedly fail. Moving a message there may unblock healthy traffic, but it does not complete that message's business effect. Someone still owns repair and replay.

Messages can be delivered again after a crash. **Idempotent consumption** means that repeated delivery of the same event ID applies the local effect once. That lets us catch up without turning replay into duplicate notifications or feed entries.

## Is every worker slow, or is one stream stuck?

Compare publish rate with successful completion rate and oldest-message age. If arrivals stay above completions, the backlog cannot shrink. Depth alone is ambiguous: a queue of 100 can drain quickly at 10,000 completions per second, while one old event can remain unresolved indefinitely.

Split the pending work by partition, event type, consumer version, and outcome. If one partition stops advancing and repeats the same exception, inspect that event and its ordering rules. If every partition grows older, look for shared slow database work, remote rate limits, larger messages, or insufficient processing capacity. Confirm consumers are making progress rather than merely running or restarting.

Follow receipt, parsing, database updates, remote effects, and acknowledgment in that order. Add workers only where the broker can parallelize them and their dependencies have spare capacity. Otherwise the new workers may add connections and worsen the underlying constraint.

Contain harmful input, quarantine a poison event with its reason, or prioritize critical work. Preserve accepted events. After the fix, replay with duplicate protection and compare authoritative posts with derived feed state. Recovery means the missing business effects exist and their age is acceptable, not simply that lag reached zero.

## Follow event e7 instead of restarting every worker

In this proposed system, `PostCreated(postId=42, eventId=e7)` normally reaches the feed projection within five seconds. A projection is a stored read copy built for rendering the feed; the post service remains authoritative.

1. A producer publishes `e7`. A newly deployed consumer cannot parse one optional field and rejects it.
2. The consumer retries `e7` endlessly, so its ordered partition does not advance. Other partitions continue normally.
3. Partition age and repeated failures identify `e7`, rather than suggesting the whole worker fleet lacks CPU.
4. The team preserves the event in quarantine, repairs the parser, and checks whether later events may safely advance before it. Where order matters, keep the affected key or partition blocked until a correct replay path exists.
5. On replay, the consumer records `e7` as processed and updates post 42's projection in the same local database transaction.
6. It acknowledges only after the commit. If that acknowledgment is lost, redelivery finds the durable processed ID and does not apply the update twice.
7. Reconciliation confirms post 42 and every other required source post appear in the intended projection.

The end condition is restored freshness plus correct state. Restarting a worker that deterministically rejects the same event would only repeat the incident.

## Instagram-backend mapping

The current project has no message broker or asynchronous feed projection. [PostService.create(...)](../../../../../src/main/java/com/instagram/backend/service/PostService.java) saves a post synchronously, and [PostService.feed(...)](../../../../../src/main/java/com/instagram/backend/service/PostService.java) reads PostgreSQL directly. Therefore queue metrics, a DLQ, inbox deduplication, and replay are **proposed**, not implemented.

A future design could save the post and a `PostCreated` outbox record in one transaction, then publish the committed record for feed and notification consumers. The event would need `eventId`, `postId`, `type`, `version`, and `occurredAt`; each consumer would store processed `eventId` with its local update. Existing bounded business metrics in [InstagramMetrics.java](../../../../../src/main/java/com/instagram/backend/observability/InstagramMetrics.java) show the style, but new freshness and lag signals would be required.

## More consumers, more partitions, or quarantine?

- **Add consumers** when partitions and downstream capacity permit parallelism. Avoid it when one partition is blocked or the database is saturated; it adds rebalance and connection pressure.
- **Add partitions** when sustained parallel throughput is needed. Do not do it casually when strict global order is required; repartitioning and per-key ordering become harder.
- **Move a message to a DLQ** when repeated failure would block healthy work. This restores flow but creates a mandatory investigation, repair, replay, and reconciliation task.

## Rehearse the crash between commit and acknowledgment

**Prediction:** after the consumer commits the projection update for `e7` but before its acknowledgment reaches the broker, it crashes. The broker delivers `e7` again.

**Detect:** the stable event ID appears in a second processing attempt, while its durable processed record and projection update already exist.

**Contain:** do not run the effect again simply because delivery repeated. Consult the processed record under the same transactional uniqueness rule.

**Recover:** acknowledge the redelivery without changing the feed again, then verify the projection contains the one intended effect and lag is catching up.

**Prevent:** test crash-before-ack with a processed-event record committed atomically with the local effect. For an external notification provider, a local inbox alone cannot make a remote send atomic; use a stable provider idempotency key or a status/reconciliation design for that additional gap.

## Two-to-three-minute interview answer

I would start with rates and age: producer rate, successful consumer rate, oldest-message age, processing duration, retries, dead letters, and the user-visible freshness objective. Then I would split lag by partition, event type, version, and consumer. One stuck partition points to a poison event or hot key; uniform lag points to insufficient throughput or a shared slow dependency.

I trace processing into validation, database work, remote calls, and acknowledgment before scaling. More consumers help only when partitions and dependencies have headroom. During impact I may pause a harmful producer, quarantine repeatedly failing events with evidence, prioritize critical work, or add tested capacity. I preserve accepted messages. After fixing code, schema, or capacity, I replay using `eventId` for idempotency and reconcile source posts against feed or notification state. Recovery requires oldest-message age and business freshness to return to target. A low error count alone is insufficient because a slow consumer can succeed while falling further behind.

## Before you practise

The written scenario is ready for practice; comprehension remains separate. Explain one-partition versus uniform lag and safe replay, then try the final questions before revealing their answers.

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
