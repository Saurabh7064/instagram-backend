# Lesson 8 — Clustering columns order rows inside one Cassandra partition

[Module](./README.md) · [Previous: primary-key grammar](./07-cassandra-primary-key-syntax.md) · [Next: when partitions appear](./09-cassandra-partition-creation.md)

- New core terms: clustering order, tie-breaker, slice.

Core study: 10–15 minutes. Prerequisites: `(user_id,activity_date)` groups a partition; the full key identifies a row.

## The day is selected. Which two events come first? — Must understand

The `(42,2026-10-25)` group contains activity 1001 at 09:00 and activities 1002 and 1003 at 09:05. We want recent events first. Compare time first: both 09:05 events precede 09:00. They tie on time, so compare their IDs in ascending order. The result is 1002, 1003, 1001; the first two are 1002 and 1003.

The date/user key chose the group. It did not resolve the order or distinguish events with equal timestamps. For those jobs Cassandra uses **clustering columns**. Together, our time and ID form the **clustering key** inside the selected partition.

The declared **clustering order** chooses ascending or descending traversal for each component. An ID that decides between equal times is a **tie-breaker**. We will use descending time, then ascending ID. A contiguous time interval within that ordered group is a **slice**.

Keep the terminology tied to its job: a Cassandra cluster is a group of servers, while clustering columns organize rows within one partition. Relational products also use their own clustered-index terminology, which does not define Cassandra replica placement. Nothing about this time/ID ordering changes the user/day hash.

## Put the comparison rules in the table declaration

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

## Apply LIMIT only after establishing the order

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

## Why skipping the first clustering column changes the lookup

Cassandra's efficient clustering restrictions follow the tuple's order. After selecting the partition, time is the first clustering column. Filtering only `activity_id=1003` without constraining the preceding time component does not become a direct ordered lookup in this layout. Index-enabled or explicitly filtered queries can have different capabilities, but their work and version-specific rules need analysis; adding `ALLOW FILTERING` does not redesign the key.

Likewise, ordering user 42's events across *two dates* requires querying both date partitions and merging in the application or choosing another table. The clustering order is not a global order of every row across the cluster. [Cassandra's SELECT documentation](https://cassandra.apache.org/doc/latest/cassandra/developing/cql/dml.html) explains clustering restrictions and supported ordering.

## An ordered folder does not order the whole database

A dated folder contains files sorted newest first, with an ID deciding equal-time order. It does not reorder files in other folders or choose the building storing them. Real Cassandra storage may span internal files and memory structures; “ordered” describes the logical clustering access model, not a claim of one physically contiguous disk file.

Choose clustering columns around the required within-group ranges and stable pagination. Avoid treating an unordered event type as an occurrence identity. The cost is a specific access pattern: another lookup shape may need another table. [PostRepository's createdAt-desc query](../../../../../../src/main/java/com/instagram/backend/repository/PostRepository.java) requests relational result ordering, not Cassandra clustering storage. The activity table remains proposed learning material.

## Remove the ID tie-breaker — paper exercise

Keep both 09:05 events but remove activity_id from the primary key. Predict whether they still address separate rows.

They now share the same user/day/time identity. Under the write-resolution rules, one row's non-key fields can replace the other's. Timestamp precision is not an event-uniqueness guarantee. Stop writing to the insufficient layout and retain original event input for repair.

Create the table with its tie-breaker, replay recoverable events, and verify the traversal 1002,1003,1001. Include pagination tests that cross equal-time rows. Those checks test both preservation and stable ordering rather than accepting a plausible-looking first page as proof.

## Before moving on

Artifact: `READY`; comprehension: `TODO`.

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
