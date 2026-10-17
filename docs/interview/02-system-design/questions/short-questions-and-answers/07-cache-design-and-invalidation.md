# Cache Design and Invalidation

## Question

A celebrity post is requested thousands of times per second. PostgreSQL is overloaded, but users expect edits, deletes, and like state to appear correctly.

**“How would you design the cache, and how would you keep cached data from becoming dangerously stale?”**

## Why this problem exists

Rebuilding the same response wastes database capacity. A cache serves reusable data faster, but creates another copy that can outlive the source value. Incorrect keys or failed invalidation can reveal another viewer’s state or return deleted content, so caching is also a correctness decision.

## Analogy

A restaurant posts its menu near the entrance so customers do not repeatedly ask the kitchen. When an item sells out, the sign needs an update or expiry.

The analogy stops because distributed caches have concurrent readers, evictions, partial failures, and personalized values.

## Terms to establish

- **Cache-aside:** read the cache first and load the database on a miss.
- **TTL:** maximum lifetime of a cached entry.
- **Invalidation:** remove or replace entries affected by a write.
- **Stampede:** many simultaneous misses overload the source.

## What I would do

I would separate reusable public fields from viewer-specific fields. Caption, author, media URL, and stable counts can use `post:v1:42`; `likedByViewer` must be loaded separately or keyed by viewer and post. Each field needs an explicit staleness limit.

For reads, use cache-aside with bounded, jittered TTLs. For edits and deletes, commit PostgreSQL first, then invalidate through an after-commit action or durable outbox event. On a hot miss, single-flight loading or a short lease lets one request rebuild while others wait briefly or receive approved stale public data. I would monitor hit rate, miss amplification, evictions, entry age, rebuild time, and database load.

## Concrete walkthrough

1. Post `42` misses `post:v1:42`.
2. One request obtains a rebuild lease, loads public data, and caches it with TTL jitter.
3. Other viewers reuse that object, but resolve their own like state separately.
4. The author edits the caption; PostgreSQL commits, then invalidation removes the key.
5. The next request loads the new value. If invalidation is delayed, TTL bounds staleness.

Database traffic falls without sharing private state indefinitely.

## Instagram-backend mapping

[PostService.java](../../../../../src/main/java/com/instagram/backend/service/PostService.java) loads every post and calls `existsByPostAndUser(...)` while mapping it. [FeedPostResponse.java](../../../../../src/main/java/com/instagram/backend/dto/FeedPostResponse.java) therefore mixes reusable post fields with personalized `likedByViewer`. Edits and deletes also run through `PostService`.

**Implemented:** PostgreSQL reads and transactional mutations. **Proposed:** a distributed cache, split public and personalized entries, after-commit invalidation, TTL jitter, single-flight rebuilds, and cache metrics. No application cache currently exists.

## Choices and trade-offs

- Choose **cache-aside** for read-heavy data with a database source of truth. Avoid it when every read must be current; miss latency and staleness are the cost.
- Choose **write-through** when all writers share one path and freshness dominates. Cache coupling and write latency are the cost.
- Choose **stale-while-revalidate** for public content during brief failures. Never use stale authorization decisions; controlled staleness is the cost.

## Failure drill

**Prediction:** a hot key expires and thousands of requests hit PostgreSQL. **Detection:** cache misses, connection wait, and one key’s traces spike together. **Containment:** permit one rebuild and serve bounded stale content where safe. **Recovery:** repopulate the key and drain work. **Prevention:** TTL jitter, request coalescing, proactive refresh, and per-key miss alerts.

## Two-to-three-minute interview answer

I would cache only data with a stated staleness policy. Public post fields are reusable, but `likedByViewer` is personalized and cannot safely live in a shared post response. I would use versioned cache-aside keys with bounded, jittered TTLs. Database writes remain authoritative, and an after-commit event invalidates edited or deleted posts.

For hot misses, one caller rebuilds while others wait briefly or receive an approved stale public value. Cache failure should degrade to controlled database access, not data loss. I would monitor hit rate, entry age, miss amplification, rebuild latency, and database load; authorization always uses authoritative data.

## Practice status

The artifact is ready; comprehension requires choosing cache boundaries and diagnosing a stampede without notes.

## Questions and explained answers

<details>
<summary>Why not store `likedByViewer` in a shared post cache entry?</summary>

It depends on the requester. Sharing it can show one user another user’s like state. Separate public data or include the viewer in the key.
</details>

<details>
<summary>Why invalidate after the database commit?</summary>

Invalidating first can let another request reload and cache the old database value before the transaction commits.
</details>

<details>
<summary>Does a longer TTL prevent a stampede?</summary>

It delays the miss but does not prevent synchronized rebuilding. Jitter and request coalescing address simultaneous misses directly.
</details>
