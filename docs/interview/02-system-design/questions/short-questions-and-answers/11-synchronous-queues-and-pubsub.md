# Synchronous Calls, Queues, and Pub/Sub

## Question

Creating a post waits for feed updates, notifications, and analytics. Slow analytics now makes the request take four seconds.

**“When should work be synchronous, queued, or published to multiple consumers?”**

## Why this problem exists

A request should wait only for work required to give a truthful result. Making every side effect synchronous couples user latency to every dependency; making everything asynchronous can return success before the authoritative write exists. The system needs a durable boundary between accepting a post and completing later work.

## Analogy: a restaurant order

The customer waits for the cashier to record an order, not for marketing reports. One kitchen ticket goes to one cook, while several teams may receive an “order completed” notice. Unlike paper tickets, software messages can be duplicated, delayed, or reordered, so consumers need explicit correctness rules.

## Terms to establish

- **Synchronous work** finishes before the caller receives a response.
- A **queue** lets one worker in a group claim each item.
- **Pub/sub** gives independent subscribers separate event copies.
- A **transactional outbox** stores an event with its business change.

## What I would do

Authentication, validation, and the durable post write stay synchronous. Feed fanout, notifications, thumbnails, and analytics can usually be asynchronous, each with a freshness target.

I would commit the post and `PostCreated` outbox row together. A relay publishes it to separate feed, notification, and analytics subscriptions. Workers within one subscription form a consumer group. Events carry a stable ID, post ID, version, and time; consumers deduplicate, bound retries, monitor oldest-message age, and quarantine poison events.

## Concrete walkthrough

1. The client submits a valid post.
2. One transaction commits the post and outbox row.
3. The API returns `201 Created`.
4. The relay publishes `PostCreated(eventId=e7, postId=42)`.
5. Feed, notification, and analytics each receive `e7`.
6. One feed worker applies its subscription copy.
7. Analytics may retry without delaying feed visibility or recreating the post.

## Instagram-backend mapping

**Implemented:** [PostController.java](../../../../../src/main/java/com/instagram/backend/controller/PostController.java) sends `POST /api/posts` to [PostService.java](../../../../../src/main/java/com/instagram/backend/service/PostService.java), which authenticates and saves the authoritative post synchronously. The feed reads PostgreSQL through [PostRepository.java](../../../../../src/main/java/com/instagram/backend/repository/PostRepository.java).

**Proposed, not implemented:** there is no broker, outbox, relay, asynchronous feed, or notification subscriber. These are future scale-out components, not current behavior.

## Choices and trade-offs

- Use synchronous calls when the response requires the dependency’s answer, such as authorization; avoid them for optional side effects.
- Use a queue when one successful worker should handle each item, such as image processing. It adds backlog and dead-letter operations.
- Use pub/sub when independent capabilities need the same fact. It reduces producer coupling but multiplies storage, compatibility, and lag monitoring.
- Keep direct database reads while they meet the SLO; a materialized feed improves isolation but introduces staleness.

## Failure drill

**Prediction:** one malformed event repeatedly crashes the feed consumer and blocks its partition. **Detection:** one partition’s oldest-message age rises while the same `eventId` fails. **Containment:** pause that partition or quarantine the event without deleting evidence. **Recovery:** fix the consumer, replay idempotently, and reconcile posts with feed entries. **Prevention:** validate schemas, bound retries, define dead-letter handling, and test poison events.

## Two-to-three-minute interview answer

I separate work by the API promise. Authentication, validation, and the authoritative post write remain synchronous. Feed fanout, notifications, media variants, and analytics move behind a durable event boundary.

I commit the post and a `PostCreated` outbox row in one transaction. A relay publishes the event to independent subscriptions; workers inside each subscription share a queue. Stable event IDs and versions make retry and deduplication possible. I monitor processing latency and oldest-message age, and quarantine poison events rather than block a partition forever.

The benefit is lower request latency and failure isolation; the cost is eventual consistency and broker operations. This project currently writes posts and reads the feed synchronously. The outbox and consumers are proposed, and I would prove recovery by replaying an event and reconciling the derived feed with authoritative posts.

## Practice status

The artifact is `READY`; comprehension remains `TODO`. Practice requires explaining the durable handoff and diagnosing the failure drill without notes.

## Questions and explained answers

<details>
<summary>1. Why not publish after committing with an ordinary broker call?</summary>

A crash between commit and publish leaves a real post with no event. An outbox makes publication retryable.

</details>

<details>
<summary>2. Why do feed and analytics need separate subscriptions?</summary>

Both need the event. On one competing queue, only one capability might receive it; separate subscriptions isolate progress and failure.

</details>

<details>
<summary>3. Does an empty queue prove correctness?</summary>

No. A consumer may acknowledge without producing the intended result. Reconciliation must compare derived state with authoritative posts.

</details>
