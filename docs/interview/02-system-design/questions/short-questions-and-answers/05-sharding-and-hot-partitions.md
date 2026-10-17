# Sharding and Hot Partitions

## Question

A celebrity post receives millions of likes in a few minutes, and one database partition saturates while the rest remain healthy.

**“How would you shard data and prevent celebrity accounts from creating hot partitions?”**

## Why this problem exists

Sharding spreads data when one database cannot meet storage or throughput needs. A popular account can still send most requests to one shard, which queues and times out while fleet averages look healthy.

## Analogy: supermarket checkout lanes

Many checkout lanes add capacity, but sending everyone to one special lane recreates a bottleneck. The analogy stops because changing a shard key requires moving durable data while preserving uniqueness and in-flight writes.

## Terms to establish

- A **shard** is one independently operated subset of the data.
- A **shard key** determines which shard owns a record or request.
- A **hot partition** receives disproportionately high traffic or storage.
- **Salting** adds a controlled bucket value so one logical key can spread writes across several physical keys.

## What I would do

1. Start from queries and invariants, then choose a high-cardinality key and explicit routing scheme.
2. Measure load per shard and key; isolate exceptions with buckets or a celebrity path.
3. Plan online rebalancing, retry-safe writes, verification, and reconciliation before an emergency.

## Concrete walkthrough

Five million likes in one minute is about 83,000 writes per second. Routing by `post_id` sends them all to one shard. Keep the authoritative like relation under a key supporting viewer/post lookup, but replace one counter row with 128 buckets chosen from a stable user hash. Writes average about 650 per second per bucket; reads sum or cache them and accept brief staleness.

## Instagram-backend mapping

[PostLike.java](../../../../../src/main/java/com/instagram/backend/domain/PostLike.java) stores the viewer/post relation, while [Post.java](../../../../../src/main/java/com/instagram/backend/domain/Post.java) stores one `likeCount`. [PostService.java](../../../../../src/main/java/com/instagram/backend/service/PostService.java) updates both in one PostgreSQL transaction. Shard routing, salted counters, rebalancing, and a celebrity path are proposed, not implemented.

## Choices and trade-offs

- Shard only after one tuned database cannot meet measured needs; distribution adds transaction and operational complexity.
- Choose for the dominant access pattern; other queries may require fan-out or projections.
- Salt hot counters when write distribution outweighs immediate exact reads; aggregation becomes costlier.

## Failure drill

**Prediction:** a celebrity post saturates one shard’s CPU, locks, or write queue while fleet averages look normal. **Symptom and detection:** per-shard and per-key metrics show concentrated latency, throttling, and queue depth. **Containment:** rate-limit or queue the hot operation and protect unrelated keys. **Recovery:** split or isolate the key, drain queued idempotent writes, and reconcile relationships with aggregate counters. **Prevention:** alert on skew, load-test celebrity traffic, pre-create buckets, and rehearse online rebalancing.

## Two-to-three-minute interview answer

I would shard only after one tuned database cannot meet measured storage or throughput. I would list access patterns and invariants, select a high-cardinality key, make routing explicit, and monitor each shard and key because fleet averages hide skew.

Hashing by post ID still sends every celebrity like to one shard. I would preserve the authoritative user/post relation but spread its aggregate count across deterministic salted buckets. Reads can sum or cache those buckets when brief staleness is acceptable; the cost is read fan-in and reconciliation.

Before launch I would prepare versioned routing, bounded migration, retry-safe writes, dual-read verification, rollback, and per-shard alerts. During an incident I would rate-limit or queue the hot operation, protect unrelated keys, isolate the celebrity key, and reconcile relationships with counters after traffic stabilizes. I would treat sharding as an operational program, not only a hash function.

## Practice status

The artifact is ready; comprehension is not implied. Practice by choosing a shard key, predicting its hottest key, and explaining how a salted counter changes both writes and reads.

## Questions and explained answers

<details>
<summary>Why does hashing by post ID not solve a celebrity-post hotspot?</summary>

Hashing distributes different posts, but every request for the same post still produces the same shard key and reaches one owner. High cardinality across keys does not prevent one key from becoming extremely hot.
</details>

<details>
<summary>What cost does salting a counter introduce?</summary>

The write load spreads across buckets, but an exact read must aggregate several values or trust a cached total. Recovery and reconciliation must also account for every bucket.
</details>

<details>
<summary>Why are fleet-wide averages insufficient after sharding?</summary>

Healthy shards can hide one saturated shard. Per-shard and per-key latency, throughput, queue, and storage metrics reveal skew that directly affects users routed to the hotspot.
</details>
