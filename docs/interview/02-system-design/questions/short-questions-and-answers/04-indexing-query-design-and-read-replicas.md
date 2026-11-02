# Indexing, Query Design, and Read Replicas

## Question

Feed and profile reads become slow as data grows, but application CPU remains healthy.

**“How would you reduce database read latency with query design, indexes, and replicas?”**


## Count the work behind one feed response

Suppose a request returns 100 posts. One query loads that list, then response mapping asks the database whether the viewer liked each post. That makes `1 + 100 = 101` queries before considering any additional author lookups. This is an **N+1 query pattern**: one initial query followed by another for each returned item.

Adding a database copy would distribute these queries, but each response would still require the same repeated work. First return a bounded page and load its related like state in a batch. A 20-post page needs only the likes relevant to those 20 post IDs; it does not need 20 separate round trips merely because the response has 20 entries.

## Help the database find the page

An **execution plan** describes how the database finds rows, combines them and orders the result. Read the actual plan for the measured slow query. A broad scan or large sort suggests different work from a lock wait or a wait for a connection.

If the query continues a feed ordered by `(created_at, id)`, consider a **composite index**, which orders several columns together. Its usefulness depends on the actual filter and sort. Verify the plan and timing after adding it; an index costs storage and maintenance on writes.

The library-catalog analogy helps here: the catalog avoids inspecting every shelf. Its limit is that a database index must also be updated as records change, so more indexes are not free.

## When would a read replica help?

After reducing the work per request, remaining read traffic may exceed the main database's capacity. A read replica is a maintained copy that can answer some reads. With asynchronous copying, it can temporarily lack a change already accepted by the primary; that delay is **replication lag**.

For this feed, use the primary when the creator immediately checks their new post, unless the product explicitly permits a delay. Older, freshness-tolerant pages may use a replica within an agreed lag policy. Like another library branch, it adds places to read; unlike a perfectly synchronized catalog, its newest information may differ.

This gives us an order of decisions: measure the slow work, reduce repeated and unbounded work, verify the query plan, then decide whether another read-serving copy is justified.

## Where this appears in our backend

[PostRepository.java](../../../../../src/main/java/com/instagram/backend/repository/PostRepository.java) returns the full ordered feed. [PostService.java](../../../../../src/main/java/com/instagram/backend/service/PostService.java) then calls `existsByPostAndUser(...)` per post, a concrete N+1 candidate. Pagination, batched like lookup, a matching index, plan verification, and replica routing are proposed, not implemented.

## Choosing the approach and paying its costs

- Index frequent, selective queries; avoid indexing every column because writes and storage become costlier.
- Cursor pagination handles changing deep feeds better; offset pagination is simpler but can slow and shift.
- Replicas add stale-tolerant capacity, not guaranteed read-your-writes.


## Paper drill: “my new post disappeared”

Predict what happens when creation commits on the primary and the immediate feed read reaches a replica that has not copied it. The write succeeds, but the new post is absent from that read.

Compare the same post on primary and replica and inspect replication progress. Keep immediate read-after-write journeys on the primary while restoring copying health. Resume replica traffic only after verifying that the copy meets the freshness policy.

Prevent the user-visible failure with an explicit routing rule for recent writes, lag monitoring, and a test that pauses replication during the create-then-read journey. A fast replica query is not sufficient evidence that the response is fresh enough.

## Two-to-three-minute interview answer

I would inspect slow-query evidence and actual plans before adding replicas: scanned rows, joins, sorts, locks, I/O, and connection wait. Then I would paginate, select only needed fields, eliminate N+1 access through joins or batches, and add composite indexes matching the real filter and order.

For this feed, returning every post and checking like state per post is the first measurement target. A bounded `(created_at, id)` page and batched liked-ID query should be validated first. I would confirm each index improves the plan enough to justify its storage and write cost.

If optimized reads still exceed leader capacity, replicas can serve stale-tolerant journeys. Writes, authorization, and immediate read-after-write stay on the leader or follow an explicit session-freshness rule. I would monitor lag, stop routing when it exceeds the agreed threshold, and verify failover does not silently weaken consistency.


## Before moving on

Artifact: `READY`; comprehension: `TODO`. Explain this scenario in your own words, predict the paper drill, and attempt the questions before opening their answers. Record learning only from your own explanation and predictions.

## Questions and explained answers

<details>
<summary>Why should query shape be fixed before adding replicas?</summary>

Replicas multiply read capacity, but they also execute the same wasteful scans or N+1 pattern. Reducing work per request lowers load on every copy and may avoid unnecessary topology.
</details>

<details>
<summary>Why does a composite index need to match filter and ordering?</summary>

Column order determines which lookups and sort order the database can satisfy efficiently. An index containing the right columns in an unusable order may still require a broad scan or sort.
</details>

<details>
<summary>Which feed read should not go to an asynchronous replica?</summary>

An immediate read after the same user creates or updates a post, unless the product accepts that delay. Otherwise a successful write can appear lost because the replica has not applied it yet.
</details>
