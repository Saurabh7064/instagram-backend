# Replication, CAP, and Consistency

## Question

Your Instagram service runs in two regions. A network failure separates them, but users still expect feeds to load and writes not to corrupt data.

**“How would you choose a replication and consistency strategy, and how does CAP affect that decision?”**

## Why this problem exists

Replicas improve durability, read capacity, and regional survival, but updates need communication to reach every copy. During a partition, the system cannot both accept every request and guarantee that both regions agree on the latest value. The right choice depends on each operation’s business invariant.

## Analogy

Two library branches copy one catalog. If their link fails, both can show the last catalog, but they cannot safely promise the final copy of the same reserved book.

The analogy stops because software also has concurrent writes, automatic retries, and clocks that may disagree.

## Terms to establish

- **Replication:** copies data across nodes or regions.
- **Consistency:** defines what a read may observe after writes.
- **Partition:** nodes are running but cannot communicate reliably.
- **Quorum:** enough replicas respond that read and write sets overlap.

## What I would do

I would classify operations first. Authentication, username uniqueness, post ownership, and deletion should not accept conflicting decisions. During a partition, those writes should reach one authoritative leader or fail clearly. Feed reads, public profiles, and approximate counters can often remain available from a regional replica within a stated staleness budget.

I would begin with leader-follower replication per data partition. Cross-region replication can be asynchronous when lower write latency matters more than immediate remote visibility. A client needing read-your-writes can temporarily read from the leader or carry a committed version. I would monitor replication lag, rejected writes, stale-read age, failover time, and reconciliation results.

## Concrete walkthrough

1. A west-region leader commits post `P42` and returns its version.
2. The replication log sends `P42` to the east follower 300 ms later.
3. During a cross-region partition, east continues serving the last replicated feed.
4. An east request to delete `P42` cannot reach the authoritative writer, so it is rejected or delayed rather than creating conflicting ownership state.
5. After recovery, replication catches up before full write traffic returns.

Users may see boundedly stale feeds, while invariant-sensitive writes remain consistent.

## Instagram-backend mapping

The project uses one relational database through [PostRepository.java](../../../../../src/main/java/com/instagram/backend/repository/PostRepository.java), and [PostService.java](../../../../../src/main/java/com/instagram/backend/service/PostService.java) makes ownership decisions. [PostLike.java](../../../../../src/main/java/com/instagram/backend/domain/PostLike.java) prevents duplicate user likes, while [Post.java](../../../../../src/main/java/com/instagram/backend/domain/Post.java) stores a derived `likeCount`.

**Implemented:** transactional single-database writes and local constraints. **Proposed:** regional replicas, lag-aware routing, leader fencing, read-your-writes versions, and counter reconciliation. Multi-region replication is not implemented.

## Choices and trade-offs

- Choose **leader-follower** when one writer simplifies invariants. Do not use lagging followers for freshness-sensitive reads; stale reads and failover work are the cost.
- Choose **multi-leader** only when regional writes justify conflict handling. Avoid it for ownership and uniqueness; reconciliation is the cost.
- Choose **quorums** when stronger latest-value guarantees justify extra coordination. They add latency and can reduce availability.

## Failure drill

**Prediction:** both regions accept ownership writes and create split brain. **Detection:** conflicting versions and invariant alarms appear after reconnection. **Containment:** stop non-authoritative writes but preserve safe reads. **Recovery:** select the valid history, replay valid operations, and repair derived counters. **Prevention:** writer fencing, explicit per-operation policies, partition tests, and lag alerts.

## Two-to-three-minute interview answer

I would not choose one consistency level for the whole product. Ownership, unique identities, and destructive changes need one authoritative decision, so I would sacrifice their write availability during a partition. Feed reads and approximate engagement data can remain available from a replica with a defined staleness limit.

I would start with leader-follower replication, use asynchronous cross-region replication where latency matters, and route read-your-writes requests by leader or version. CAP matters specifically during lost communication: each operation must choose consistent rejection or potentially stale availability. I would test partitions and observe lag, failover, rejected writes, and reconciliation.

## Practice status

The artifact is ready; comprehension requires classifying operations and predicting partition behavior without notes.

## Questions and explained answers

<details>
<summary>Does CAP force a permanent choice between consistency and availability?</summary>

No. The forced trade-off appears during a network partition. Outside it, both are usually possible within normal latency and failure limits.
</details>

<details>
<summary>Why can feed reads and post deletion use different policies?</summary>

A slightly stale feed is often acceptable. A conflicting deletion can expose or destroy data, so it should require authoritative state.
</details>

<details>
<summary>What must happen before promoting a follower?</summary>

Check its replication position and fence the old leader from writing. Without fencing, two leaders can create divergent histories.
</details>
