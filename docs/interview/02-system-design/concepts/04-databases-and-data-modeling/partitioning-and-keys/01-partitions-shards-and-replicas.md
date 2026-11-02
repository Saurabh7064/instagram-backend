# Lesson 1 — A partition is a data boundary; a shard is a distribution boundary

[Module](./README.md) · [Next: range partitioning](./02-range-partitioning.md)

- New core terms: partition, shard, replica.

Core study: 10–15 minutes. Prerequisites: a table contains rows; a database server stores them. No Cassandra knowledge assumed.

## Split the activity history, move it, or copy it? — Must understand

We have five activity events. User 42 has 1001, 1002, and 1003 on October 25, then 1004 on October 26. User 84 has 2001 on October 25. Someone asks for user 42's recent activity. Later, we need to retire an old day. Eventually one database server may run out of capacity.

Those are three different problems. Grouping rows by day can help skip or remove old data. Putting different groups on different servers can spread work. Keeping another copy can help when a server fails. It is easy to say “partition the data” and accidentally assume we gained all three benefits.

A **partition** is a subdivision formed by a rule. For this first picture, put the four October 25 events in P25 and the one October 26 event in P26. If both live on server A, we have divided the table but still depend on the same server.

Moving subsets across independent storage and processing boundaries is **sharding**; a placed subset is a **shard**. Keeping another maintained copy is **replication**, and the copy is a **replica**. Copying P25 creates another representation of its four events, not four new activities.

The terms describe roles rather than identical objects in every product. PostgreSQL can create child tables for date ranges. Cassandra can group rows with one exact partition-key value. We will learn those mechanisms separately; first keep subdivision, placement, and copying distinct.

## Watch what each design actually changes

Follow those same five events through four designs. The [module dataset](./README.md#fixed-activity-dataset) fixes their values for later lessons; all displayed dates and times use Coordinated Universal Time (UTC).

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

## Which row values choose the group?

A **partition key** is the column or tuple of columns whose values determine a row's subdivision under the chosen rule. For a date-range PostgreSQL layout it might be `activity_date`. In a Cassandra table it might be the exact tuple `(user_id, activity_date)`. Both use keys, but one groups many values into a declared interval while the other groups exact tuple equality.

Library shelves make a useful analogy: divide books into sections, place sections in separate branches, and duplicate popular titles. The analogy stops at automated routing and consistency; librarians are not a database's recovery protocol.

## Solve the actual limit before distributing the database

Use local table partitioning for pruning or retention when one server still fits. Use sharding when distributing capacity or ownership justifies routing, cross-shard queries, rebalancing, and harder transactions. Prefer a suitable index before adding either if the real problem is locating a small set of rows. An **index** is an auxiliary structure used to find rows efficiently; it does not by itself distribute the table to new servers.

[Post.java](../../../../../../src/main/java/com/instagram/backend/domain/Post.java) defines a relational `posts` table and [PostRepository](../../../../../../src/main/java/com/instagram/backend/repository/PostRepository.java) reads it. Neither file declares a shard map or activity partitions. All activity placement in this module is a learning design, not the current production layout. PostgreSQL's [partitioning overview](https://www.postgresql.org/docs/current/ddl-partitioning.html) describes its local table-subdivision model.

## Lose server A while both date partitions live there — paper exercise

P25 and P26 have different names but both reside on A. Predict which remains readable if A fails.

Both become unreachable through that server. The two logical subdivisions never supplied redundant hardware, so fail affected operations honestly rather than treating a partition count as an availability guarantee.

Restore A or recover through a correctly maintained replica using its supported protocol, then verify all five event identities. Record placement separately from replication and test actual server loss. That evidence answers the availability question more directly than the number of child tables.

## Before moving on

Artifact: `READY`; comprehension: `TODO`.

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
