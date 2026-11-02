# Cache Design and Invalidation

## Question

A celebrity post is requested thousands of times per second. PostgreSQL is overloaded, but users expect edits, deletes, and like state to appear correctly.

**“How would you design the cache, and how would you keep cached data from becoming dangerously stale?”**


## Which part of this response belongs to everyone?

Thousands of viewers ask for post 42. They all need its caption and media URL, so rebuilding those fields from PostgreSQL for every request repeats work. But each viewer also needs an answer to “have I liked this post?” User 7 and user 8 may need different answers.

A **cache** is a reusable copy of a result. Before deciding where to put it, decide whose result it is. A shared key such as `post:v1:42` can hold approved public fields. It must not accidentally hold the entire response after it has been personalized for user 7. Keep viewer-specific state separate, or include the viewer in the key and accept the larger number of entries.

## Walk through the first read and the edit

With **cache-aside**, the application looks in the cache first. If post 42 is absent, it reads PostgreSQL and stores a reusable copy. Later reads can reuse that copy. Like a menu posted outside a restaurant, it saves repeated questions to the source. Unlike a physical sign, several software requests can rebuild and change the copy concurrently.

Now the author edits the caption. Commit the database change before removing the old cached entry; removal is called **invalidation**. The next miss can load the accepted caption. A durable change record can make a failed invalidation retryable.

Ordering the edit this way closes one race, but it does not close every race. A reader may load the old caption before the edit, then place that value in the cache after invalidation. If the product needs stronger freshness, use a version-aware design or another explicitly verified strategy. Simply saying “invalidate after commit” is not a proof that stale values cannot reappear.

## How long may an old value survive?

**TTL** means time to live: the expiry duration attached to an entry. It limits how long that particular cached copy remains eligible without refresh. The end-to-end freshness limit also depends on where the copy came from, racing fills, and any policy for serving stale values.

For an ordinary caption, a short documented delay may be acceptable. For permission revocation or removed private content, the same delay may expose data. Keep authoritative eligibility checks where the safety contract requires them.

Finally, thousands of simultaneous misses can all reload PostgreSQL. Let one request rebuild a hot key while others wait briefly or receive an approved older public value. That request coalescing protects the source; spreading expiry times helps with many different keys but cannot alone coalesce one hot key's callers.

## Where this appears in our backend

[PostService.java](../../../../../src/main/java/com/instagram/backend/service/PostService.java) loads every post and calls `existsByPostAndUser(...)` while mapping it. [FeedPostResponse.java](../../../../../src/main/java/com/instagram/backend/dto/FeedPostResponse.java) therefore mixes reusable post fields with personalized `likedByViewer`. Edits and deletes also run through `PostService`.

**Implemented:** PostgreSQL reads and transactional mutations. **Proposed:** a distributed cache, split public and personalized entries, after-commit invalidation, TTL jitter, single-flight rebuilds, and cache metrics. No application cache currently exists.

## Choosing the approach and paying its costs

- Choose **cache-aside** for read-heavy data with a database source of truth. Avoid it when every read must be current; miss latency and staleness are the cost.
- Choose **write-through** when all writers share one path and freshness dominates. Cache coupling and write latency are the cost.
- Choose **stale-while-revalidate** for public content during brief failures. Never use stale authorization decisions; controlled staleness is the cost.


## Paper drill: the hot post expires

Predict the database load if every waiting viewer independently rebuilds post 42. Cache misses and database-connection waits can rise together even though ordinary cache hits were fast.

Contain the burst by coalescing the rebuild and bounding waiters. Use an older value only if its age and content policy permit it. Recover by verifying the rebuilt version, repopulating the key and allowing database queues to drain.

Prevent recurrence with a tested hot-key rebuild policy, bounded fallbacks and per-key investigation evidence. Also test a reader racing an edit; high hit rate does not establish freshness or correct personalization.

## Two-to-three-minute interview answer

I would cache only data with a stated staleness policy. Public post fields are reusable, but `likedByViewer` is personalized and cannot safely live in a shared post response. I would use versioned cache-aside keys with bounded, jittered TTLs. Database writes remain authoritative, and an after-commit event invalidates edited or deleted posts.

For hot misses, one caller rebuilds while others wait briefly or receive an approved stale public value. Cache failure should degrade to controlled database access, not data loss. I would monitor hit rate, entry age, miss amplification, rebuild latency, and database load; authorization always uses authoritative data.


## Before moving on

Artifact: `READY`; comprehension: `TODO`. Explain this scenario in your own words, predict the paper drill, and attempt the questions before opening their answers. Record learning only from your own explanation and predictions.

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

It delays expiry but does not prevent many callers rebuilding the same expired key. Request coalescing directly combines that key's concurrent rebuilds. Jitter spreads expiry across different keys; it does not alone prevent a stampede on one hot key.
</details>
