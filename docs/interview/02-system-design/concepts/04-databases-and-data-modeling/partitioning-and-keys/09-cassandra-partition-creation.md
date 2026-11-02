# Lesson 9 — Cassandra CREATE TABLE defines the grouping rule, not one table per user

[Module](./README.md) · [Previous: clustering](./08-cassandra-clustering-columns.md) · [Next: routing to nodes](./10-cassandra-query-routing.md)

- New core terms: keyspace, schema, logical partition.

Core study: 10–15 minutes. Prerequisites: nested primary-key grammar and clustering order; CQL means Cassandra Query Language.

## The table is empty. Have we already created every user/day? — Must understand

Our rule is “group activity by user and UTC day.” The five events will form three groups: user 42 on October 25, user 84 on October 25, and user 42 on October 26. But declaring the table does not pre-create a group for every possible user and future day.

The declaration establishes a rule. When we write the first event for `(42,2026-10-25)`, that key names a nonempty data group. Later writes with the same complete key add rows inside it; changing either user or date selects another group. This differs from PostgreSQL, where we explicitly created date child tables before those dates could receive rows.

A **logical partition** is the group of rows in one Cassandra table sharing a complete partition-key value. The table is part of that identity: the same tuple in another table does not combine their rows. The **schema** declares columns, types, and keys; it is the rule rather than all the possible data groups.

A **keyspace** supplies a namespace for tables and their replication settings. We need one before declaring the table. The setup below is for a disposable single-node lab, while the later routing lesson separately reasons about three nodes. Keep those scenarios separate so a copy-count setting is not mistaken for creation of extra servers.

## Create the namespace and grouping rule

The [complete CQL example](./examples/cassandra-activity.cql) targets a disposable **single-node** lab whose datacenter name is `datacenter1`:

```cql
CREATE KEYSPACE activity_lessons
WITH replication = {
    'class': 'NetworkTopologyStrategy',
    'datacenter1': 1
};
```

The keyspace name is a namespace, not a partition key. The strategy controls placement of copies according to topology. Here `1` requests one replica in the named datacenter; the datacenter label must match the actual lab. It provides no node-failure redundancy. It is deliberately not a claim that this script creates a three-node cluster. Do not reuse a guessed datacenter label or this copy count as a production policy.

```cql
CREATE TABLE activity_lessons.activity_by_user_day (
    user_id bigint,
    activity_date date,
    occurred_at timestamp,
    activity_id bigint,
    activity_type text,
    post_id bigint,
    PRIMARY KEY ((user_id, activity_date), occurred_at, activity_id)
) WITH CLUSTERING ORDER BY (occurred_at DESC, activity_id ASC);
```

The qualified name chooses the keyspace/table. `bigint` is a signed 64-bit integer, `date` a calendar day, `timestamp` an instant represented at millisecond precision, and `text` a string. The two inner columns define grouping; the last two define within-group identity/order. Merely creating this schema does not materialize all possible user/day combinations.

Cassandra's [data-definition reference](https://cassandra.apache.org/doc/latest/cassandra/developing/cql/ddl.html) explains keyspace/table declarations. Compare it with PostgreSQL's explicit child declarations from lesson 6: the similar phrase “create a partition” names different mechanisms.

## Watch the five writes form exactly three groups

Apply the dataset in insertion order:

1. Event 1001 writes `(42,Oct25)` with clustering key `(09:00,1001)`. This is the first nonempty logical group for that tuple.
2. Event 1002 writes `(42,Oct25)` with `(09:05,1002)`. The partition-key tuple matches, so it adds a row inside the existing group.
3. Event 1003 writes the same tuple with `(09:05,1003)`. Same partition, distinct row.
4. Event 2001 writes `(84,Oct25)`. The user changed, so it belongs to another partition even though the date matches.
5. Event 1004 writes `(42,Oct26)`. The date changed, so it belongs to a third partition even though the user matches.

| Complete partition key | Activity IDs | Row count |
|---|---|---:|
| (42,2026-10-25) | 1001,1002,1003 | 3 |
| (84,2026-10-25) | 2001 | 1 |
| (42,2026-10-26) | 1004 | 1 |

Three distinct tuple values produce three logical groups containing 3+1+1=5 rows. A million possible users do not create a million physical nodes. A node stores many such groups and copies. Hash collisions can give different groups the same token without making their actual keys equal.

There is no required “one disk file per partition.” Cassandra persists many groups using its storage engine's shared structures. A table, logical partition, token range, node, and file are different layers. Deleting the last row can leave physical deletion metadata until maintenance; logical group counts are not a reliable count of current files or allocated bytes.

## A logical group is not a physical file

A filing rule says “folder per user/day”; folders acquire contents when documents arrive. Declaring the rule does not preprint every possible folder. The analogy stops at database storage/replication and deletion metadata.

This grouping suits bounded user/day reads. Avoid user-only history if worst-case volume grows indefinitely; avoid a global day-only key if today's writes overwhelm one group. Choose boundaries from both access patterns and worst-case size/load.

[Post.java](../../../../../../src/main/java/com/instagram/backend/domain/Post.java) is a relational schema declaration. It has no Cassandra keyspace or activity grouping. These CQL objects are learning artifacts; no application storage migration is performed. A running Cassandra server is required to execute the file, and the [verification record](./verification.md) states that it has not been executed in this environment.

## Send midnight's event with yesterday's date — paper exercise

Event 1004 has an October 26 timestamp, but the application sends October 25 as activity_date. Predict which group receives it.

Cassandra uses the supplied key, so the row goes to `(42,Oct25)` and a correct October 26 query misses it. Reject inconsistent day/time values at ingestion; the table declaration does not infer the date from the timestamp.

Find the event by durable identity, safely insert it under the correct key, then remove the wrong-key row through a recorded repair. Changing a key is a move between identities rather than an ordinary non-key update. Derive UTC day consistently and test midnight boundaries so the same defect does not keep creating wrongly grouped events.

## Before moving on

Artifact: `READY`; comprehension: `TODO`.

Explain what keyspace creation, table creation, and each write do separately. Derive three groups from five rows and compare that with PostgreSQL's two date child tables.

## Questions and explained answers

<details>
<summary>1. Does CREATE TABLE pre-create an empty partition for every user/date?</summary>

No. It establishes the schema and grouping rule. Data written under a complete key forms that table's logical group; there is no per-user/day child-table creation statement.

</details>

<details>
<summary>2. Why are there three logical partitions rather than five?</summary>

The first three rows share the same user/date tuple. User 84 on October 25 and user 42 on October 26 add two more distinct tuples. Partition count follows distinct complete keys, not row count.

</details>

<details>
<summary>3. Will Cassandra infer the UTC date from occurred_at?</summary>

Not from this schema. The application supplies both fields. A wrong date changes the partition key and therefore grouping/routing, so ingestion must enforce that relationship.

</details>
