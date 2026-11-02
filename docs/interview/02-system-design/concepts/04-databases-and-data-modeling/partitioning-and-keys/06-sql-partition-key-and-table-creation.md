# Lesson 6 — Choose a SQL partition key from operations, not just identity

[Module](./README.md) · [Previous: consistent hashing](./05-consistent-hashing.md) · [Next: Cassandra primary keys](./07-cassandra-primary-key-syntax.md)

- New core terms: primary key, partition key, query plan. SQL means Structured Query Language.

Core study: 10–15 minutes. Prerequisites: range bounds, child partitions, pruning; an index helps find rows inside a table.

## One query needs identity, date grouping, and a way to find the rows — Must understand

We want user 42's October 25 activity, newest first. Later we want to remove old days efficiently. The five-row dataset gives us two useful tests: user 84's event must stay out of user 42's result, and event 1004 at October 26 midnight must go into the next day's storage.

Three jobs are involved. First, distinguish one event row from another. Second, choose the day group that owns it. Third, locate matching rows within the groups a query needs. A unique event number addresses only the first job; a date split does not automatically provide the third.

A table's **primary key** is its enforced non-null unique row identity. Its **partition key** supplies values to the subdivision rule. An **index** is an access structure that helps locate matching rows. Those roles can use some of the same columns, but they promise different things.

For this PostgreSQL exercise, use the date to choose child tables, a date/user/event tuple for identity, and a user/time index for the frequent read. We will derive each choice before reading the declaration. When we later inspect a **query plan**, we are looking at the database's selected execution steps, not another key.

## Why these columns serve different jobs

Our [fixed dataset](./README.md#fixed-activity-dataset) has five rows across two dates. Choose `activity_date` for range partitioning because date-window queries and lifecycle operations align with it. Choose `(activity_date,user_id,activity_id)` as this learning table's primary key. PostgreSQL requires a partitioned table's primary/unique constraint to include the partition-key columns, since child-local indexes alone cannot enforce an unconstrained uniqueness claim across all date children.

This primary key makes the tuple unique. It does **not** independently enforce that activity_id=1001 can occur nowhere on another date or for another user. If global event-ID uniqueness is a requirement, design an additional registry or another supported architecture; do not claim the chosen composite constraint supplies it.

For the frequent user/time query, add an index on `(user_id,occurred_at DESC,activity_id ASC)`. It helps locate a user's recent rows within surviving children and distinguishes equal event times. It does not choose the date child. A small test table may still use a sequential scan because that is cheaper; a scan on the correct child is not evidence pruning failed.

## Create the routing rule, create one child, then insert

The complete checked script is [postgresql-partitions.sql](./examples/postgresql-partitions.sql). It creates a separate learning schema inside a transaction and rolls it back.

```sql
CREATE TABLE user_activity (
    user_id bigint NOT NULL,
    activity_date date NOT NULL,
    occurred_at timestamptz NOT NULL,
    activity_id bigint NOT NULL,
    activity_type text NOT NULL,
    post_id bigint,
    PRIMARY KEY (activity_date, user_id, activity_id),
    CHECK (activity_date = (occurred_at AT TIME ZONE 'UTC')::date)
) PARTITION BY RANGE (activity_date);

CREATE TABLE user_activity_2026_10_25
    PARTITION OF user_activity
    FOR VALUES FROM ('2026-10-25') TO ('2026-10-26');
```

The parent declaration defines columns, row identity, and routing method. `CHECK` requires the supplied date to match the event timestamp's UTC day: event 1004 at October 26 midnight cannot be mislabeled October 25 merely to fit an existing partition. `::date` converts the UTC calendar value to a date. `PARTITION OF` declares actual child storage; the parent statement alone has not created every future daily child. Bounds include October 25 and exclude October 26.

```sql
INSERT INTO user_activity VALUES
(42, '2026-10-25', '2026-10-25 09:05:00+00', 1002, 'VIEW_POST', 900);
```

This insert targets the parent. The date selects the October 25 child; user 42 and activity 1002 identify the row within the constraint. `+00` states the zero-offset time explicitly. The declared column order matches the values; production code should normally name columns to avoid mistakes when schema order changes.

## Derive the order and check where the rows live

```sql
SELECT activity_id, activity_type, occurred_at
FROM user_activity
WHERE activity_date = DATE '2026-10-25' AND user_id = 42
ORDER BY occurred_at DESC, activity_id ASC;
```

The date excludes the October 26 child. The user condition excludes activity 2001 belonging to user 84. Three rows remain: 1001 at 09:00 and 1002/1003 at 09:05. Descending time puts the two 09:05 events first; ascending ID breaks their tie as 1002 before 1003. The result is **1002,1003,1001**, derived from both sort columns.

The script queries `tableoid::regclass`, PostgreSQL's physical table identifier rendered as a relation name, to verify four rows in the October 25 child and one in October 26. `EXPLAIN` displays a plan for the date/user query; absence of the October 26 child is the relevant pruning evidence. These are separate from output ordering, which must be requested with `ORDER BY`.

Official references: PostgreSQL's [partition declarations and constraints](https://www.postgresql.org/docs/current/ddl-partitioning.html) and [CREATE TABLE grammar](https://www.postgresql.org/docs/current/sql-createtable.html). SQL syntax is database-specific; do not generalize PostgreSQL's exact rules to every SQL product.

## When is this extra table management worthwhile?

An archive uses calendar drawers, a receipt number, and a lookup card. Those are placement, identity, and retrieval respectively. The analogy stops where the database automatically routes writes and enforces constraints concurrently.

Use date partitions when pruning/retention justify maintenance. Keep an ordinary indexed table when volume is small or most queries cross all dates. A different PostgreSQL table could hash-partition on user_id, but that changes the lifecycle and range-query trade-off; it does not turn PostgreSQL into Cassandra.

[Post.java](../../../../../../src/main/java/com/instagram/backend/domain/Post.java) currently uses an ID primary key and `createdAt`. [PostRepository](../../../../../../src/main/java/com/instagram/backend/repository/PostRepository.java) does not declare this activity table or a partition migration. The schema here is isolated learning material, not an application database change.

## Recover the missing-child failure — existing isolated SQL exercise

In the recorded SQL exercise, only October 25 exists when the script attempts event 1004 at October 26 00:00. Before reading the recorded result, predict whether a matching child exists.

The expected partition-bound error is caught and no row is inserted. Keep the event identity and correct timestamp; changing its date would conceal the storage gap by creating incorrect data.

The script creates the October 26 child, retries 1004, and verifies counts of 4/1 plus its physical child. It also rolls back the learning schema and checks its absence. See the [existing verification record](./verification.md) for that execution evidence. Upcoming intervals, the UTC-date check, and deployment routing checks help prevent recurrence. This editorial revision does not rerun the lab.

## Before moving on

Artifact: `READY`; comprehension: `TODO`.

Explain the three key/index roles, walk through parent/child creation, and derive the ordered result. Distinguish executed PostgreSQL evidence from the later unexecuted Cassandra walkthrough.

## Questions and explained answers

<details>
<summary>1. Why is user_id not automatically the partition key just because the query filters it?</summary>

Partition choice serves a specific operation set. This example chooses dates for retention and date pruning, then an index serves user lookup. Another workload could justify a different layout.

</details>

<details>
<summary>2. Does CREATE TABLE ... PARTITION BY RANGE create tomorrow's child automatically?</summary>

No. It declares the parent method. A matching child must exist, or an intentionally configured default partition must handle that value. This lab deliberately demonstrates the missing-child failure.

</details>

<details>
<summary>3. Why are 1002 and 1003 both returned, with 1002 first?</summary>

They are distinct row identities with the same event time. The user/date filter includes both; descending time ties, so the explicit ascending activity ID orders 1002 before 1003.

</details>
