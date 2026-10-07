# Micro-Lesson 03 — Partitioning, Replication, and Hot Keys

- Status: `READY`; comprehension not yet demonstrated
- Time: 15–20 minutes
- Primary idea: partitioning divides data while replication copies data; a poor key can overload one division
- New terms: partition, replica, hot key
- Prerequisite: [Scalability, Availability, and Reliability](./01-scalability-availability-reliability.md)

## Why does this exist?

One machine eventually runs out of storage, read capacity, write capacity, or recovery tolerance. Distributed databases place different data on different machines and keep copies so one failure does not erase or hide everything.

The placement key matters because it decides which requests compete for the same resources.

## Analogy: warehouse aisles and duplicate warehouses

- **Partitioning:** split products across aisles by a rule so workers do not search one giant pile.
- **Replication:** keep a copy in another warehouse so a fire or outage does not remove the only copy.
- **Hot key:** every customer wants one viral product stored in aisle 7, so aisle 7 is overwhelmed while other aisles are quiet.

The analogy stops working where databases hash keys, rebalance token ranges, coordinate consistency, and store replicas on multiple nodes automatically.

## Plain-language model

- A **partition** is a logical group of rows placed and retrieved together using a partition key.
- A **replica** is another copy of data used for availability, durability, or read capacity.
- A **hot key** is a key receiving disproportionate traffic or accumulating an unmanageably large partition.

## Cassandra mental model

Consider activity events:

```sql
CREATE TABLE user_activity (
    user_id uuid,
    activity_day date,
    occurred_at timestamp,
    activity_type text,
    PRIMARY KEY ((user_id, activity_day), occurred_at)
);
```

- Partition key: `(user_id, activity_day)`
- Clustering key: `occurred_at`
- One partition contains all rows for one user on one day, ordered by time.

```text
Logical partition (user 42, 2026-10-06)
  ├─ 09:00 LOGIN
  ├─ 09:05 VIEW_POST
  └─ 09:08 LIKE_POST
```

A partition is not a node. The partition is a logical data grouping. A node is a server process that stores many partitions and replica copies.

## How Cassandra routes a request

```text
(user_id, day)
      ↓ hash
numeric token
      ↓ token-ring ownership
coordinator routes to replica nodes
```

1. The client or coordinator hashes the complete partition key.
2. The hash produces a token.
3. Token ownership identifies the responsible replica set.
4. The coordinator contacts enough replicas for the requested consistency level.

If there are one million unique user IDs, there can be roughly one million logical partitions for one day when `user_id` and day form the key. That does not mean one million nodes. Each node stores many partitions.

## Predict before reading

An activity table uses only `celebrity_id` as its partition key. One celebrity receives ten million interactions per hour. What happens?

<details>
<summary>Reveal the prediction</summary>

All interactions for that celebrity route to the same logical partition and replica set. Storage, request rate, compaction, and network load concentrate there, producing a hot and potentially oversized partition while other nodes remain underused.

</details>

## Repairing a hot partition

Choose based on query needs:

1. **Time bucket:** `(celebrity_id, activity_day)` or hour bounds partition size over time.
2. **Write shard:** `(celebrity_id, activity_day, shard_number)` spreads one time bucket across several partitions, but reads must fan out and merge.
3. **Separate aggregates:** store counts in a dedicated aggregation path rather than reading every event.
4. **Cache safe reads:** reduce repeated reads but do not solve concentrated writes.
5. **Rate limit or buffer:** protect the datastore during bursts.

More shards are not free: they increase read fan-out, merge work, metadata, and operational complexity.

## SQL versus NoSQL key choice

Start with access patterns in both systems.

For a relational database:

- choose primary keys for stable identity and relationships;
- add indexes for important filters and joins;
- introduce physical partitioning only when table size or operational needs justify it.

For Cassandra-style storage:

- design the primary key around a specific query;
- the partition key must provide routing and bounded distribution;
- clustering columns organize rows inside that partition;
- denormalized tables per query are normal.

Do not choose a Cassandra key because it is “unique.” Choose it because it supports the query and distributes both data volume and traffic.

## Project mapping

[Post.java](../../../../src/main/java/com/instagram/backend/domain/Post.java), [PostLike.java](../../../../src/main/java/com/instagram/backend/domain/PostLike.java), and their repositories currently use relational storage. That is appropriate at the current scale.

At larger scale, partitioning by only `author_id` could create celebrity hot keys. A feed/event store might use a bounded key such as `(author_id, time_bucket, shard)` while a relational source of truth continues to own transactional post metadata.

## Partitioning versus replication

| Question | Partitioning | Replication |
|---|---|---|
| Main purpose | Divide data/load | Copy data |
| Different data per node? | Usually yes | Copies overlap |
| Helps storage capacity? | Yes | No; uses more total storage |
| Helps survive node loss? | Not alone | Yes, with healthy replicas/failover |
| Main new problem | Key distribution and cross-partition queries | Lag, consistency, conflict, failover |

## Failure drill

Scenario: a time-bucketed key uses `(user_id, year)`. Most users are small, but one automated account writes hundreds of millions of rows in a year.

1. **Prediction:** that yearly partition grows too large and hot.
2. **Symptom:** concentrated latency, timeouts, compaction pressure, and uneven node utilization.
3. **Recovery:** throttle or buffer the producer, add a narrower bucket/new write path, and migrate or expire old data deliberately.
4. **Prevention:** size partitions using worst-case users and traffic, not averages; monitor partition size and per-key request rate.

## Interview questions

<details>
<summary>1. Are partitions or nodes actually storing the data?</summary>

A partition is a logical group of rows. Nodes are physical or virtual database servers that store the partition's data and replicas. One node stores many partitions; one partition can be copied to several nodes.

</details>

<details>
<summary>2. How can one partition contain multiple rows?</summary>

Rows share the same partition-key value but have different clustering-key values. In the activity example, one user/day identifies the partition while each timestamp identifies an ordered row inside it.

</details>

<details>
<summary>3. If there are one million users, are there one million partitions?</summary>

If `user_id` alone is the partition key and all users have data, approximately yes—one logical partition per distinct key. With `(user_id, day)`, there can be one partition per active user per day. These logical partitions are distributed across a much smaller number of nodes.

</details>

<details>
<summary>4. Why can a perfectly even data size still have a hot-key problem?</summary>

Traffic can be uneven even when stored bytes are balanced. One key may be read or written far more often than others, concentrating request load on its replica set.

</details>

<details>
<summary>5. Why not add a random shard to every partition key?</summary>

It spreads writes but makes reads query multiple shards and merge results. Use it only when one natural key cannot handle worst-case traffic and the read fan-out remains acceptable.

</details>

## Teach-back

Draw three nodes, place several logical partitions on them, add replica arrows, and explain how the key `(user_id, day)` routes one query.

## Stop/go

Proceed only when you can:

- distinguish a partition from a node and a replica;
- explain why one partition contains many clustering rows;
- route a partition key conceptually through a hash to replicas;
- diagnose the celebrity hot-key scenario and state the cost of sharding it.
