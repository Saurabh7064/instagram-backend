# Lesson 9 — Cassandra CREATE TABLE defines the grouping rule, not one table per user

[Module](./README.md) · [Previous: clustering](./08-cassandra-clustering-columns.md) · [Next: routing to nodes](./10-cassandra-query-routing.md)

- Artifact: `READY`; comprehension: `TODO`.
- Core study: 10–15 minutes. Primary idea: a new exact partition-key value creates a new logical group as data is written.
- Prerequisites: nested primary-key grammar and clustering order; CQL means Cassandra Query Language.
- New core terms: keyspace, schema, logical partition.

## What exactly does CREATE TABLE create? — Must understand

You write a table declaration with `(user_id,activity_date)` as partition key. Did it create one partition, a partition per node, or a partition for every future user/day? None of those interpretations is correct. Separate the rule from the data groups formed by applying it.

A **keyspace** is a namespace containing Cassandra tables and replication settings. A **schema** declares tables, columns, types, and keys. A **logical partition** is the group of rows in one table with the same complete partition-key value. Table identity matters: the same key tuple in two different tables does not make their rows one shared partition.

Cassandra does not require a PostgreSQL-style `CREATE TABLE ... PARTITION OF` for every user/day. Declaring the table establishes a grouping/placement rule. Writing rows with a new complete key gives that table a new logical data group.

## Read the setup with its assumptions

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

## Build the groups from the fixed five writes

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

## Analogy, choice, and project mapping

A filing rule says “folder per user/day”; folders acquire contents when documents arrive. Declaring the rule does not preprint every possible folder. The analogy stops at database storage/replication and deletion metadata.

This grouping suits bounded user/day reads. Avoid user-only history if worst-case volume grows indefinitely; avoid a global day-only key if today's writes overwhelm one group. Choose boundaries from both access patterns and worst-case size/load.

[Post.java](../../../../../../src/main/java/com/instagram/backend/domain/Post.java) is a relational schema declaration. It has no Cassandra keyspace or activity grouping. These CQL objects are learning artifacts; no application storage migration is performed. A running Cassandra server is required to execute the file, and the [verification record](./verification.md) states that it has not been executed in this environment.

## Failure drill — paper exercise

- **Predict:** the application mistakenly sends activity 1004 with date October 25 while its timestamp is October 26. Which group is selected?
- **Symptom:** it routes to `(42,Oct25)`; a correct October 26 query misses it. Cassandra uses the supplied key, not an inferred day.
- **Contain:** reject inconsistent day/time values at ingestion rather than accepting wrong grouping.
- **Recover:** find the event by durable identity, insert the correct key safely, then remove the wrong-key row with a recorded repair. Changing a key is not an ordinary non-key update.
- **Prevent:** derive UTC day consistently and test midnight boundaries. Do not assume the table declaration derives `activity_date` automatically.

## Recap and stop/go

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
