# Feed Fan-Out Strategy

## Question

Most creators have hundreds of followers, but a celebrity has tens of millions. Users expect a fresh home feed with low read latency.

**“Would you build the feed with fan-out on write, fan-out on read, or a hybrid strategy?”**


## When should we do the work of building a feed?

Mira has 800 followers and publishes `P20`. We could place a reference to `P20` into each follower's prepared feed now. That does about 800 feed writes once, so opening the feed later can be cheap. This is **fan-out on write**: distribute references when the post is published.

Now a celebrity with 30 million followers publishes `P21`. The same rule asks for 30 million writes from one publish. Even with background workers, that burst can delay other creators' posts. The follower count changes the cost of the decision.

The alternative is to collect relevant posts when a viewer opens the feed. This is **fan-out on read**. Publishing is cheaper, but the reader waits while the service gathers and combines posts from followed accounts. A viewer following many active creators can make that path expensive.

Newspaper delivery gives a useful picture: prepare and deliver copies in advance, or let readers collect news when they need it. The analogy leaves out changing follows, deletions, privacy and repeated delivery attempts, all of which a software feed must handle.

## Combine the two paths only when the traffic justifies it

In a proposed hybrid, ordinary creators use prepared feeds while high-follower creators are merged at read time. Viewer `V7` opens the app: the service loads prepared IDs including `P20`, fetches eligible recent celebrity IDs including `P21`, combines them and removes duplicates.

These IDs are candidates, not permission to show a post. Check current visibility and deletion rules before returning content. Then rank or order the eligible candidates and fetch their current display fields in batches. Copying IDs instead of full posts reduces the number of captions and counters that need updating after an edit.

## Make delayed delivery repairable

The post transaction can record an event alongside `P20`. Workers later read that durable handoff and update follower feeds in bounded batches. A uniqueness rule such as `(viewerId, postId)` makes repeating the same delivery safe. Track the oldest unfinished event, not just whether workers are running.

Choose the high-follower threshold from measured delivery cost, active audience and freshness needs. A hybrid creates two paths to operate, deduplicate and reconcile. While the existing global chronological feed meets requirements, a simpler bounded database query may be the right starting point.

## Where this appears in our backend

[PostController.java](../../../../../src/main/java/com/instagram/backend/controller/PostController.java) exposes the feed endpoint. [PostService.java](../../../../../src/main/java/com/instagram/backend/service/PostService.java) currently asks [PostRepository.java](../../../../../src/main/java/com/instagram/backend/repository/PostRepository.java) for every post globally and maps all results. [Post.java](../../../../../src/main/java/com/instagram/backend/domain/Post.java) records author and creation time.

**Implemented:** a global chronological feed and synchronous post creation. **Proposed:** follows, transactional outbox, durable events, idempotent workers, per-user candidate stores, high-fan-out classification, merge/ranking, and deletion tombstones.

## Choosing the approach and paying its costs

- Choose **fan-out on write** for ordinary creators when fast reads dominate. Avoid it for huge audiences; write and storage amplification are the cost.
- Choose **fan-out on read** for high-fan-out creators. Avoid it when viewers follow many active accounts; merge latency is the cost.
- Choose **hybrid** for a skewed graph. Do not add two paths before scale requires them; classification and deduplication complexity are the cost.


## Paper drill: Mira's post is missing

Predict what `V7` sees if `P20` commits but its feed-delivery event waits behind a celebrity-sized backlog. The source post exists, while the prepared feed is stale. Compare the publish time with event progress and an actual freshness probe.

Protect ordinary delivery by limiting expensive background work and separating exceptional creators where needed. A bounded read-time merge can fill recent gaps if the source has capacity; an unlimited fallback could overload that source too.

Recover by replaying the pending deliveries with duplicate-safe writes, then compare prepared entries with authoritative posts. Prevent recurrence by testing follower-count skew, measuring event age and enforcing limits on each delivery batch.

## Two-to-three-minute interview answer

I would use a hybrid because follower counts are highly skewed. Ordinary publishes commit an outbox event, and idempotent workers copy only post IDs into follower feed stores for predictable reads. High-fan-out creators avoid millions of immediate writes; their recent posts are pulled at read time.

The read path merges both candidate sets, deduplicates, applies privacy and deletion rules, ranks, and batch-hydrates a cursor page. I would set the threshold from measured delivery cost, monitor freshness and lag, and use bounded read-time fallback during worker delays.


## Before moving on

Artifact: `READY`; comprehension: `TODO`. Explain this scenario in your own words, predict the paper drill, and attempt the questions before opening their answers. Record learning only from your own explanation and predictions.

## Questions and explained answers

<details>
<summary>Why store post IDs instead of full post objects in every feed?</summary>

IDs reduce duplication and allow current captions, counters, and policy to be batch-hydrated at read time.
</details>

<details>
<summary>Why is fan-out on write dangerous for celebrities?</summary>

One publish can create millions of writes, saturating queues and storage while delaying other creators.
</details>

<details>
<summary>How do retried fan-out events avoid duplicate entries?</summary>

Use a stable event ID and uniqueness key such as `(viewerId, postId)`, so reprocessing updates the same candidate.
</details>
