# Replication, CAP, and Consistency

## Question

Your Instagram service runs in two regions. A network failure separates them, but users still expect feeds to load and writes not to corrupt data.

**“How would you choose a replication and consistency strategy, and how does CAP affect that decision?”**


## Let the two regions disagree in one specific way

The west-region database accepts post `P42`. The east-region copy normally receives the change 300 ms later. A reader reaching east during that interval may see the previous feed. That delay comes from **replication**, the process of copying accepted changes to another participant.

Now the connection between the regions breaks. Both regions are running, but messages cannot pass between them. This is a **network partition**. East can still return the data it already has; it cannot discover the west region's new changes by waiting a locally chosen 300 ms.

## What promise should each operation make?

For a public feed, the product may accept older data for a specified period. That is a **consistency policy**: a rule about which stored history a read may show. If our policy sets a maximum age, we must stop using the stale copy or return an explicit degraded result when that limit is exceeded. A partition can last longer than any freshness budget.

Ownership changes and deletion need a more careful policy. If east cannot reach the authoritative writer, accepting an independent conflicting decision can leave two incompatible histories. Requiring the authoritative decision may mean delaying or rejecting the operation while communication is broken.

Two library branches with disconnected reservation records face a similar information problem: each can show its old catalog, but both cannot safely promise the last available copy. Software adds automatic retries and concurrent writes, so it needs explicit rules beyond the analogy.

## Where CAP fits

**CAP** names Consistency, Availability and Partition tolerance. In this context, consistency asks reads and writes to behave like one current copy; availability asks requests received by non-failing participants to eventually complete with a valid result for the requested operation, rather than an unavailable error or an indefinite wait; a partition is the communication failure the design must tolerate. During such a split, the system cannot guarantee both of those operation promises.

That is why the choice belongs to a particular operation. Serving an older feed and refusing an uncertain ownership change can be deliberate parts of the same product.

A leader-follower design starts with one authoritative writer and copies its accepted changes. It simplifies ownership but creates lag and failover work. Requiring responses from overlapping sets of replicas, often called quorums, is a coordination tool; overlap alone does not prove a complete latest-value protocol. The read, write, version and membership rules must fit together.

## Where this appears in our backend

The project uses one relational database through [PostRepository.java](../../../../../src/main/java/com/instagram/backend/repository/PostRepository.java), and [PostService.java](../../../../../src/main/java/com/instagram/backend/service/PostService.java) makes ownership decisions. [PostLike.java](../../../../../src/main/java/com/instagram/backend/domain/PostLike.java) prevents duplicate user likes, while [Post.java](../../../../../src/main/java/com/instagram/backend/domain/Post.java) stores a derived `likeCount`.

**Implemented:** transactional single-database writes and local constraints. **Proposed:** regional replicas, lag-aware routing, leader fencing, read-your-writes versions, and counter reconciliation. Multi-region replication is not implemented.

## Choosing the approach and paying its costs

- Choose **leader-follower** when one writer simplifies invariants. Do not use lagging followers for freshness-sensitive reads; stale reads and failover work are the cost.
- Choose **multi-leader** only when regional writes justify conflict handling. Avoid it for ownership and uniqueness; reconciliation is the cost.
- Use **quorums** as part of a specified coordination protocol when the required guarantees justify extra responses and latency. Overlap alone does not prove latest-value reads; version, read/write and membership rules matter.


## Paper drill: two regions both believe they own writes

Predict the result if both regions accept conflicting ownership changes while disconnected. After reconnection, their accepted histories may disagree. An alarm should compare accepted versions and detect violated ownership rules, rather than treating successful local responses as proof of global correctness.

Contain the incident by preventing non-authoritative writes while preserving only reads allowed by the safety policy. Recovery needs an explicit choice of valid history and repair of derived data; reconnecting the network does not choose that history automatically.

Prevent recurrence by making write authority enforceable, testing partitions and failover, and checking replica freshness before serving bounded-staleness reads. The old leader must be fenced, meaning prevented from continuing authoritative writes, before a replacement can safely take over.

## Two-to-three-minute interview answer

I would not choose one consistency level for the whole product. Ownership, unique identities, and destructive changes need one authoritative decision, so I would sacrifice their write availability during a partition. Feed reads and approximate engagement data can use a replica within a defined staleness limit; when that limit is exceeded, the service must follow its explicit unavailable or degraded policy.

I would start with leader-follower replication, use asynchronous cross-region replication where latency matters, and route read-your-writes requests by leader or version. CAP matters specifically during lost communication: each operation must choose consistent rejection or potentially stale availability. I would test partitions and observe lag, failover, rejected writes, and reconciliation.


## Before moving on

Artifact: `READY`; comprehension: `TODO`. Explain this scenario in your own words, predict the paper drill, and attempt the questions before opening their answers. Record learning only from your own explanation and predictions.

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
