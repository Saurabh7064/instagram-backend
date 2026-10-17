# Feed Fan-Out Strategy

## Question

Most creators have hundreds of followers, but a celebrity has tens of millions. Users expect a fresh home feed with low read latency.

**“Would you build the feed with fan-out on write, fan-out on read, or a hybrid strategy?”**

## Why this problem exists

Precomputing every follower’s feed makes reads fast, but one celebrity post can trigger millions of writes. Building the feed entirely at read time avoids that write explosion, but each read must query, merge, filter, and rank many creators. A skewed social graph rarely fits one strategy.

## Analogy

A local newspaper can deliver to every doorstep, while national breaking news is fetched from a central broadcast when someone opens the app.

The analogy stops because follows, deletions, personalized ranking, and asynchronous retries continuously change feed membership.

## Terms to establish

- **Fan-out on write:** copy a new post reference into follower feeds at publish time.
- **Fan-out on read:** gather followed creators’ posts when the viewer requests a feed.
- **Hybrid fan-out:** precompute ordinary creators and merge high-fan-out creators on read.
- **Feed store:** a per-user ordered collection of candidate post IDs.

## What I would do

I would use a hybrid. Publishing commits the post and a durable outbox event in one transaction. For ordinary creators, workers append the post ID and ranking metadata to follower feed stores. A uniqueness key such as `(viewerId, postId)` makes retries idempotent.

Creators above a measured delivery-cost threshold use fan-out on read. The feed service loads precomputed candidates, fetches recent posts from followed high-fan-out accounts, merges and deduplicates them, applies privacy and deletion rules, ranks them, then batch-hydrates post details. The threshold should adapt to follower count, active followers, and freshness targets. I would monitor event lag, write amplification, store size, merge latency, and missing candidates.

## Concrete walkthrough

1. Mira, with 800 followers, publishes `P20`; its transaction also writes an outbox event.
2. Workers append `P20` to 800 feed stores in bounded batches.
3. A celebrity publishes `P21`; the system avoids 30 million immediate writes.
4. Viewer `V7` opens the feed. The service merges precomputed IDs including `P20` with recent celebrity candidates including `P21`.
5. It deduplicates, ranks, and hydrates the first page.

Reads stay predictable without letting one creator overwhelm publishing.

## Instagram-backend mapping

[PostController.java](../../../../../src/main/java/com/instagram/backend/controller/PostController.java) exposes the feed endpoint. [PostService.java](../../../../../src/main/java/com/instagram/backend/service/PostService.java) currently asks [PostRepository.java](../../../../../src/main/java/com/instagram/backend/repository/PostRepository.java) for every post globally and maps all results. [Post.java](../../../../../src/main/java/com/instagram/backend/domain/Post.java) records author and creation time.

**Implemented:** a global chronological feed and synchronous post creation. **Proposed:** follows, transactional outbox, durable events, idempotent workers, per-user candidate stores, high-fan-out classification, merge/ranking, and deletion tombstones.

## Choices and trade-offs

- Choose **fan-out on write** for ordinary creators when fast reads dominate. Avoid it for huge audiences; write and storage amplification are the cost.
- Choose **fan-out on read** for high-fan-out creators. Avoid it when viewers follow many active accounts; merge latency is the cost.
- Choose **hybrid** for a skewed graph. Do not add two paths before scale requires them; classification and deduplication complexity are the cost.

## Failure drill

**Prediction:** workers lag, so ordinary posts are missing from feeds. **Detection:** oldest event age, queue depth, and freshness probes rise. **Containment:** pause optional recomputation, scale within datastore limits, and merge recent posts on read. **Recovery:** replay events idempotently and backfill stores. **Prevention:** partitioned consumers, burst tests, backpressure, celebrity isolation, and lag alerts.

## Two-to-three-minute interview answer

I would use a hybrid because follower counts are highly skewed. Ordinary publishes commit an outbox event, and idempotent workers copy only post IDs into follower feed stores for predictable reads. High-fan-out creators avoid millions of immediate writes; their recent posts are pulled at read time.

The read path merges both candidate sets, deduplicates, applies privacy and deletion rules, ranks, and batch-hydrates a cursor page. I would set the threshold from measured delivery cost, monitor freshness and lag, and use bounded read-time fallback during worker delays.

## Practice status

The artifact is ready; comprehension requires selecting strategies for two follower distributions without notes.

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
