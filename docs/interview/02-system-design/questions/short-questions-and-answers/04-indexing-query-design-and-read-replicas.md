# Indexing, Query Design, and Read Replicas

## Question

Feed and profile reads become slow as data grows, but application CPU remains healthy.

**“How would you reduce database read latency with query design, indexes, and replicas?”**

## Why this problem exists

Database latency often comes from broad scans, repeated queries, I/O or locks, or an index that does not match filtering and ordering. Replicas copy inefficient queries and may return stale data, so I would reduce work first.

## Analogy: a library catalog

A catalog locates books without scanning every shelf; a branch adds read capacity. The analogy stops because indexes cost work on every write and replicas can temporarily disagree with the leader.

## Terms to establish

- An **execution plan** shows how the database intends to scan, join, sort, and return a query.
- A **composite index** orders multiple columns for a specific filter and sort pattern.
- An **N+1 query** is one initial query followed by an additional query for each returned item.
- **Replication lag** is the delay before a replica applies a leader’s committed change.

## What I would do

1. Measure slow queries, scanned rows, locks, I/O, connection wait, and actual plans.
2. Paginate results, select needed columns, and replace N+1 access with bounded joins or batches.
3. Add indexes matching important filters and order; route only stale-tolerant reads to replicas.

## Concrete walkthrough

A 100-post feed can trigger one list query plus 100 viewer-like checks and possible lazy-author queries. Fetch 20 posts after a `(created_at, id)` cursor, support it with a matching index, and batch related data. If optimized traffic still exceeds leader capacity, send stale-tolerant older pages to a replica. Keep the user’s immediate post-creation read on the leader so lag does not make the post appear lost.

## Instagram-backend mapping

[PostRepository.java](../../../../../src/main/java/com/instagram/backend/repository/PostRepository.java) returns the full ordered feed. [PostService.java](../../../../../src/main/java/com/instagram/backend/service/PostService.java) then calls `existsByPostAndUser(...)` per post, a concrete N+1 candidate. Pagination, batched like lookup, a matching index, plan verification, and replica routing are proposed, not implemented.

## Choices and trade-offs

- Index frequent, selective queries; avoid indexing every column because writes and storage become costlier.
- Cursor pagination handles changing deep feeds better; offset pagination is simpler but can slow and shift.
- Replicas add stale-tolerant capacity, not guaranteed read-your-writes.

## Failure drill

**Prediction:** a user creates a post, then an immediate feed read hits a lagging replica and omits it. **Symptom and detection:** the write succeeds on the leader, replica-lag metrics rise, and leader versus replica queries disagree. **Containment:** route that journey and other freshness-critical reads to the leader. **Recovery:** restore replication health and wait for the replica to catch up before returning it to rotation. **Prevention:** define freshness thresholds, monitor lag, and implement a read-your-writes routing policy.

## Two-to-three-minute interview answer

I would inspect slow-query evidence and actual plans before adding replicas: scanned rows, joins, sorts, locks, I/O, and connection wait. Then I would paginate, select only needed fields, eliminate N+1 access through joins or batches, and add composite indexes matching the real filter and order.

For this feed, returning every post and checking like state per post is the first measurement target. A bounded `(created_at, id)` page and batched liked-ID query should be validated first. I would confirm each index improves the plan enough to justify its storage and write cost.

If optimized reads still exceed leader capacity, replicas can serve stale-tolerant journeys. Writes, authorization, and immediate read-after-write stay on the leader or follow an explicit session-freshness rule. I would monitor lag, stop routing when it exceeds the agreed threshold, and verify failover does not silently weaken consistency.

## Practice status

The artifact is ready; comprehension is not implied. Practice by counting the current feed queries and explaining when one read must remain on the leader.

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
