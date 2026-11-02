# Sharding and Hot Partitions

## Question

A celebrity post receives millions of likes in a few minutes, and one database partition saturates while the rest remain healthy.

**“How would you shard data and prevent celebrity accounts from creating hot partitions?”**


## More shards do not split one popular key

Our celebrity post receives five million likes in one minute. That is `5,000,000 ÷ 60 ≈ 83,333` writes each second. Suppose routing uses the post ID: all writes for post 42 go to the same destination, even if there are many otherwise quiet destinations.

Each independently operated subset of the data is a **shard**. The value used to choose that subset is the **shard key**. A **hot partition** is a subset receiving more work than its owner can safely handle. Spreading different post IDs does not split requests that all carry the same ID.

Imagine many supermarket checkouts with a rule sending every customer holding one popular item to the same lane. The other lanes do not help that line. The limit of the analogy matters: changing database routing requires moving stored data and coordinating writes, not simply asking people to change lanes.

## Separate the relationship from the count

There are two jobs: remember that user 7 liked post 42, and report a total for post 42. Keep an authoritative relationship with an enforceable uniqueness rule. Choosing how to distribute those relationships requires its own query and transaction design.

The total can sometimes be maintained in several counter buckets rather than one contested row. For an illustrative 128-bucket counter, a stable hash of the user selects a bucket. Under an even-distribution assumption, `83,333 ÷ 128 ≈ 651` updates reach each bucket per second. This is an average estimate, not a promised capacity.

Adding a bucket value to the counter key is often called **salting**. It helps physical write distribution only if the storage routing actually places those buckets across sufficient capacity. Keeping all 128 counters behind one saturated owner may reduce a row-lock hotspot without solving that owner's overall limit.

## The read pays for the simpler write

An exact count now requires summing all buckets. A cached aggregate is cheaper to read but can lag. Relationship updates and bucket updates also need a correctness and recovery plan; a uniqueness constraint on a single database does not automatically span independently sharded databases.

Choose this extra complexity only after measuring the hotspot. Monitor individual shards and popular keys: a fleet-wide average can look healthy while every request for post 42 is struggling.

## Where this appears in our backend

[PostLike.java](../../../../../src/main/java/com/instagram/backend/domain/PostLike.java) stores the viewer/post relation, while [Post.java](../../../../../src/main/java/com/instagram/backend/domain/Post.java) stores one `likeCount`. [PostService.java](../../../../../src/main/java/com/instagram/backend/service/PostService.java) updates both in one PostgreSQL transaction. Shard routing, salted counters, rebalancing, and a celebrity path are proposed, not implemented.

## Choosing the approach and paying its costs

- Shard only after one tuned database cannot meet measured needs; distribution adds transaction and operational complexity.
- Choose for the dominant access pattern; other queries may require fan-out or projections.
- Salt hot counters when write distribution outweighs immediate exact reads; aggregation becomes costlier.


## Paper drill: most machines are quiet

Predict why adding shards might leave the celebrity post just as slow. If every request still routes by its one post ID, the hot key still has one owner.

Group latency, waiting work and write load by destination and key. Protect unrelated traffic by bounding or queuing hot-key work. Then test a design that isolates the key or distributes the relevant writes; do not move live data without a routing and reconciliation plan.

After recovery, compare authoritative like relationships with the aggregate count and replay only duplicate-safe work. Prevent recurrence with skew alerts, celebrity-shaped load tests and a rehearsed data-migration procedure. Randomizing a counter key is only one part of that procedure.

## Two-to-three-minute interview answer

I would shard only after one tuned database cannot meet measured storage or throughput. I would list access patterns and invariants, select a high-cardinality key, make routing explicit, and monitor each shard and key because fleet averages hide skew.

Hashing by post ID still sends every celebrity like to one shard. I would preserve the authoritative user/post relation but spread its aggregate count across deterministic salted buckets. Reads can sum or cache those buckets when brief staleness is acceptable; the cost is read fan-in and reconciliation.

Before launch I would prepare versioned routing, bounded migration, retry-safe writes, dual-read verification, rollback, and per-shard alerts. During an incident I would rate-limit or queue the hot operation, protect unrelated keys, isolate the celebrity key, and reconcile relationships with counters after traffic stabilizes. I would treat sharding as an operational program, not only a hash function.


## Before moving on

Artifact: `READY`; comprehension: `TODO`. Explain this scenario in your own words, predict the paper drill, and attempt the questions before opening their answers. Record learning only from your own explanation and predictions.

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
