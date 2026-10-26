# Lesson 1 — A partition is a data boundary; a shard is a distribution boundary

[Module](./README.md) · [Next: range partitioning](./02-range-partitioning.md)

- Artifact: `READY`; comprehension: `TODO`.
- Core study: 10–15 minutes. Primary idea: distinguish splitting data from placing it on machines and making copies.
- Prerequisites: a table contains rows; a database server stores them. No Cassandra knowledge assumed.
- New core terms: partition, shard, replica.

## Why one large table becomes a problem — Must understand

Suppose we store user activity. A query asks for one user's recent events, but the database contains years of everyone's events. We want to skip irrelevant data, manage retention, and eventually spread work when one server cannot keep up. Those are related goals, but they are not the same operation.

A **partition** is a subdivision of a dataset according to a rule. The exact unit depends on the database: PostgreSQL can divide one logical table into child tables covering date ranges; Cassandra groups rows sharing an exact partition-key value. Do not assume the word names an identical storage object in both systems.

A **shard** is a separately placed subset of data in a distributed design, commonly managed by a database server or server group. **Sharding** distributes subsets across those independent storage/processing boundaries. Products vary in terminology; always ask “what is divided, and where is it placed?”

A **replica** is another maintained copy of data. Replication copies a subset; partitioning divides it. A replicated shard can have several copies of the same rows on different servers.

## Start with the same five activity events

All lessons use the [fixed activity dataset](./README.md#fixed-activity-dataset). User 42 has activity IDs 1001,1002,1003 on October 25 and 1004 on October 26. User 84 has activity 2001 on October 25. Dates and displayed times use Coordinated Universal Time (UTC).

1. Put October 25 in partition P25: it has four rows, IDs 1001,1002,1003,2001. Put October 26 in P26: it has one row, 1004.
2. Store both child partitions on database server A. We have split the table, but every request still shares A's CPU, storage, and failure boundary. CPU means the processor executing work.
3. In a proposed sharded design, place P25 on server group A and P26 on server group B. A routing rule must now direct each operation to the right group.
4. Keep another copy of P25 on group A's replica. We now have two physical copies of those four rows, not eight distinct activities.

| Design | Distinct events | What changed? | What remains shared? |
|---|---:|---|---|
| One table on A | 5 | Nothing divided | All server resources |
| P25/P26 on A | 4+1=5 | Storage/maintenance units | Same server capacity/failure boundary |
| P25 on A, P26 on B | 5 | Placement across groups | Cross-group operations need coordination |
| P25 copied to A's replica | Still 5 | Redundant storage | Both copies represent the same four activities |

More partitions on the same server do not automatically add server capacity. More replicas consume storage and can help recovery/read availability depending on the replication protocol, but do not automatically split one hot writer's work.

## What a key contributes

A **partition key** is the column or tuple of columns whose values determine a row's subdivision under the chosen rule. For a date-range PostgreSQL layout it might be `activity_date`. In a Cassandra table it might be the exact tuple `(user_id, activity_date)`. Both use keys, but one groups many values into a declared interval while the other groups exact tuple equality.

Library shelves make a useful analogy: divide books into sections, place sections in separate branches, and duplicate popular titles. The analogy stops at automated routing and consistency; librarians are not a database's recovery protocol.

## Choose the smallest needed boundary

Use local table partitioning for pruning or retention when one server still fits. Use sharding when distributing capacity or ownership justifies routing, cross-shard queries, rebalancing, and harder transactions. Prefer a suitable index before adding either if the real problem is locating a small set of rows. An **index** is an auxiliary structure used to find rows efficiently; it does not by itself distribute the table to new servers.

[Post.java](../../../../../../src/main/java/com/instagram/backend/domain/Post.java) defines a relational `posts` table and [PostRepository](../../../../../../src/main/java/com/instagram/backend/repository/PostRepository.java) reads it. Neither file declares a shard map or activity partitions. All activity placement in this module is a learning design, not the current production layout. PostgreSQL's [partitioning overview](https://www.postgresql.org/docs/current/ddl-partitioning.html) describes its local table-subdivision model.

## Failure drill — paper exercise

- **Predict:** both P25/P26 are on A and A fails. Does having two partitions keep either available?
- **Symptom:** both become unreachable through that server despite separate child names.
- **Contain:** avoid treating logical subdivision as redundancy; fail operations honestly.
- **Recover:** restore A or use a correctly maintained replica under its recovery protocol, then verify all five event identities.
- **Prevent:** document placement and replication separately. Test server loss rather than inferring availability from partition count.

## Recap and stop/go

Draw the five events, split them 4/1, distribute the subsets, then add a copy. Explain what each step changes and why row identity counts do not increase with replication.

## Questions and explained answers

<details>
<summary>1. Are two date partitions on one PostgreSQL server two shards?</summary>

Not in the distributed-placement sense used here. They are separate table subdivisions sharing one server. Sharding adds independent placement and routing boundaries.

</details>

<details>
<summary>2. Why does copying P25 not create four additional business events?</summary>

The copies retain the same event identities. Replication increases physical storage and recovery options, not the number of distinct activities.

</details>

<details>
<summary>3. Can partitioning and replication coexist?</summary>

Yes. Divide the dataset into subsets, then maintain multiple copies of each subset. They solve different problems and introduce different routing/consistency costs.

</details>
