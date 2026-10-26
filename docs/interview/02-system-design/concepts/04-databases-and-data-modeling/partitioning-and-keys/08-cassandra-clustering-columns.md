# Lesson 8 — Clustering columns order rows inside one Cassandra partition

[Module](./README.md) · [Previous: primary-key grammar](./07-cassandra-primary-key-syntax.md) · [Next: when partitions appear](./09-cassandra-partition-creation.md)

- Artifact: `READY`; comprehension: `TODO`.
- Core study: 10–15 minutes. Primary idea: choose clustering order for the query inside an already selected partition.
- Prerequisites: `(user_id,activity_date)` groups a partition; the full key identifies a row.
- New core terms: clustering order, tie-breaker, slice.

## Why grouping alone is insufficient — Must understand

Once a read selects user 42 and October 25, it still needs recent events first and must preserve simultaneous activities. Grouping tells us *where* the rows belong. The clustering tuple says how rows are distinguished and traversed within that group.

The correct Cassandra term is **clustering column** or the combined **clustering key**. “Cluster key” is ambiguous: a cluster is a group of servers, not this ordering tuple. This is also different from a relational database's clustered-index terminology, which is product-specific and does not select Cassandra replicas.

**Clustering order** defines ascending or descending traversal for each clustering component. A **tie-breaker** is the next component used when earlier components are equal. A **slice** is a contiguous interval in that ordered clustering space.

## Declare the order we actually want

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

`activity_lessons` is a **keyspace**, Cassandra's namespace for tables and replication settings. Its setup is taught next. `timestamp` stores an event-time value; we supply explicit UTC offsets in the sample inserts. The partition tuple is `(user_id,activity_date)`. The clustering tuple is `(occurred_at,activity_id)`. `DESC` means descending and `ASC` ascending. Non-key fields carry event details; they do not affect node routing or row identity.

Without the explicit clause, clustering columns default to ascending order. This clause changes within-partition ordering, not the partition hash. The [Cassandra key/clustering reference](https://cassandra.apache.org/doc/latest/cassandra/developing/cql/ddl.html) defines these roles.

## Derive the query's exact result

Select the fixed `(42,2026-10-25)` partition. It has:

- 1001 at 09:00, LOGIN.
- 1002 at 09:05, VIEW_POST of post 900.
- 1003 at 09:05, VIEW_POST of post 901.

Descending time compares 09:05 ahead of 09:00. Within 09:05, ascending ID compares 1002 before 1003. Therefore the full traversal is **1002,1003,1001**. `LIMIT 2` takes 1002 and 1003, not an unspecified pair.

```cql
SELECT activity_id, occurred_at, activity_type, post_id
FROM activity_lessons.activity_by_user_day
WHERE user_id = 42 AND activity_date = '2026-10-25'
LIMIT 2;
```

Both partition-key components identify the group. The selected columns describe the returned values, not routing inputs. The stored clustering order supplies this within-partition traversal. For a time slice starting at 09:05, adding `occurred_at >= '2026-10-25 09:05:00+0000'` includes both tied events and excludes 1001. The bound is inclusive, so equality matters.

## Why component order affects supported filters

Cassandra's efficient clustering restrictions follow the tuple's order. After selecting the partition, time is the first clustering column. Filtering only `activity_id=1003` without constraining the preceding time component does not become a direct ordered lookup in this layout. Index-enabled or explicitly filtered queries can have different capabilities, but their work and version-specific rules need analysis; adding `ALLOW FILTERING` does not redesign the key.

Likewise, ordering user 42's events across *two dates* requires querying both date partitions and merging in the application or choosing another table. The clustering order is not a global order of every row across the cluster. [Cassandra's SELECT documentation](https://cassandra.apache.org/doc/latest/cassandra/developing/cql/dml.html) explains clustering restrictions and supported ordering.

## Analogy, trade-off, and project mapping

A dated folder contains files sorted newest first, with an ID deciding equal-time order. It does not reorder files in other folders or choose the building storing them. Real Cassandra storage may span internal files and memory structures; “ordered” describes the logical clustering access model, not a claim of one physically contiguous disk file.

Choose clustering columns around the required within-group ranges and stable pagination. Avoid treating an unordered event type as an occurrence identity. The cost is a specific access pattern: another lookup shape may need another table. [PostRepository's createdAt-desc query](../../../../../../src/main/java/com/instagram/backend/repository/PostRepository.java) requests relational result ordering, not Cassandra clustering storage. The activity table remains proposed learning material.

## Failure drill — paper exercise

- **Predict:** remove activity_id from the primary key while keeping two 09:05 events. Can the partition preserve both as separate rows?
- **Symptom:** both address the same user/day/time key and one row's non-key fields replace the other under write-resolution rules.
- **Contain:** stop writes to the insufficient identity layout; keep original event input for repair.
- **Recover:** use a new table with the tie-breaker, replay both events, and verify 1002,1003,1001 order.
- **Prevent:** test timestamp collisions and pagination across equal-time rows. Ordering precision is not a uniqueness guarantee.

## Recap and stop/go

Identify the partition and clustering tuples, then derive the full order and the LIMIT 2 result. Explain why clustering order does not decide node placement or cross-date global ordering.

## Questions and explained answers

<details>
<summary>1. Why include activity_id if occurred_at already orders events?</summary>

Multiple events can share a timestamp. The ID distinguishes their full keys and provides deterministic tie ordering instead of overwriting one occurrence.

</details>

<details>
<summary>2. Does activity_type determine the replica set here?</summary>

No. Only the complete partition tuple is hashed for routing. Activity type is a non-key value in this table; even the clustering tuple does not determine replica placement.

</details>

<details>
<summary>3. Why does LIMIT 2 return 1002 and 1003?</summary>

The time comparison places 09:05 before 09:00. The two 09:05 rows tie on time, so ascending IDs order 1002 then 1003. Taking the first two follows that explicit ordering.

</details>
