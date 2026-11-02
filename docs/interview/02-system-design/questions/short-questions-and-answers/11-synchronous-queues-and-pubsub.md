# Synchronous Calls, Queues, and Pub/Sub

## Question

Creating a post waits for feed updates, notifications, and analytics. Slow analytics now makes the request take four seconds.

**“When should work be synchronous, queued, or published to multiple consumers?”**


## What does “your post was created” promise?

A user submits a post. Before saying it exists, the backend must authenticate the caller, validate the request and durably save the post. The user does not need to wait for an analytics report about that post. If analytics takes four seconds, attaching it to the response makes an optional activity part of the publishing delay.

Work completed before the response is **synchronous**. Work completed later is **asynchronous**. The split follows the API promise: moving the authoritative save into an unreliable background task would make a successful response misleading.

A restaurant records an accepted order before giving the customer a receipt; marketing reports can happen later. Software needs a stronger handoff than a paper ticket, because a process can stop between recording the order and notifying the kitchen.

## Close the gap between saving and announcing

In the proposed flow, one database transaction saves post 42 and an event record `PostCreated(eventId=e7, postId=42)`. That event record is a **transactional outbox**. If the transaction fails, neither record commits. If it succeeds and the process stops, the event remains available for a publisher to find.

The API can now return `201 Created` for the saved post. A separate relay publishes `e7`. It may publish again after an uncertain acknowledgement, so consumers must recognize repeated event IDs. Durable publication makes recovery possible; it does not make every effect happen exactly once.

## Who needs a copy of the event?

Feed preparation, notifications and analytics each need to hear that post 42 exists. **Publish/subscribe**, often shortened to pub/sub, gives these independent subscribers their own event stream or subscription. Analytics can fall behind without taking feed delivery's copy.

Inside feed delivery, several workers may share a **queue** so work is distributed among them. One worker's successful processing should satisfy that subscription's work item. Broker acknowledgement and redelivery details still require duplicate handling.

These are different relationships: independent capabilities each need the fact; workers doing the same capability share its work. Putting feed and analytics on one competing queue could cause one to receive an event the other also needed.

Give later work an explicit freshness target, a bounded retry policy and a way to quarantine malformed events. Otherwise a faster create response can conceal a permanently unfinished workflow.

## Where this appears in our backend

**Implemented:** [PostController.java](../../../../../src/main/java/com/instagram/backend/controller/PostController.java) sends `POST /api/posts` to [PostService.java](../../../../../src/main/java/com/instagram/backend/service/PostService.java), which authenticates and saves the authoritative post synchronously. The feed reads PostgreSQL through [PostRepository.java](../../../../../src/main/java/com/instagram/backend/repository/PostRepository.java).

**Proposed, not implemented:** there is no broker, outbox, relay, asynchronous feed, or notification subscriber. These are future scale-out components, not current behavior.

## Choosing the approach and paying its costs

- Use synchronous calls when the response requires the dependency’s answer, such as authorization; avoid them for optional side effects.
- Use a queue when one successful worker should handle each item, such as image processing. It adds backlog and dead-letter operations.
- Use pub/sub when independent capabilities need the same fact. It reduces producer coupling but multiplies storage, compatibility, and lag monitoring.
- Keep direct database reads while they meet the SLO; a materialized feed improves isolation but introduces staleness.


## Paper drill: the same event keeps failing

Predict the effect of a malformed `e7` that repeatedly crashes a feed worker before acknowledgement. Depending on the broker and ordering policy, it can prevent later work on that partition from progressing.

Detect repeated failure of the same event ID together with increasing oldest-event age. Contain the damage by pausing the affected work or moving the event into a recorded quarantine under an explicit policy; preserve enough evidence to repair it.

Fix the consumer or event handling, replay safely and compare feed entries against committed posts. Prevent recurrence with schema validation, bounded retries and an exercised repair path. An empty queue still needs a business-state check: a worker could have acknowledged an event without creating its intended result.

## Two-to-three-minute interview answer

I separate work by the API promise. Authentication, validation, and the authoritative post write remain synchronous. Feed fanout, notifications, media variants, and analytics move behind a durable event boundary.

I commit the post and a `PostCreated` outbox row in one transaction. A relay publishes the event to independent subscriptions; workers inside each subscription share a queue. Stable event IDs and versions make retry and deduplication possible. I monitor processing latency and oldest-message age, and quarantine poison events rather than block a partition forever.

The benefit is lower request latency and failure isolation; the cost is eventual consistency and broker operations. This project currently writes posts and reads the feed synchronously. The outbox and consumers are proposed, and I would prove recovery by replaying an event and reconciling the derived feed with authoritative posts.


## Before moving on

Artifact: `READY`; comprehension: `TODO`. Explain this scenario in your own words, predict the paper drill, and attempt the questions before opening their answers. Record learning only from your own explanation and predictions.

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
