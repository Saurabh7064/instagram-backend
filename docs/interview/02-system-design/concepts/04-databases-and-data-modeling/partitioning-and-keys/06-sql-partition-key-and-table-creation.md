# Lesson 6 — Choose a SQL partition key from operations, not just identity

[Module](./README.md) · [Previous: consistent hashing](./05-consistent-hashing.md) · [Next: Cassandra primary keys](./07-cassandra-primary-key-syntax.md)

- Artifact: `READY`; comprehension: `TODO`.
- Core study: 10–15 minutes. Primary idea: identity, lookup, and partition boundaries can use different columns.
- Prerequisites: range bounds, child partitions, pruning; an index helps find rows inside a table.
- New core terms: primary key, partition key, query plan. SQL means Structured Query Language.

## Start with the operations — Must understand

We need to read user 42's October 25 activity and retire old dates efficiently. A unique activity identifier tells us which event it is, but does not automatically group old dates together. A frequent user filter does not by itself tell us which time partition to retain or remove.

A **primary key** is a non-null unique row identity enforced by a table constraint. A **partition key** supplies values used by the partitioning rule. An **index** is an access structure for locating matching rows. A **query plan** describes the database's chosen steps for executing a query. These roles can overlap, but are not synonyms.

## Derive the key choice

Our [fixed dataset](./README.md#fixed-activity-dataset) has five rows across two dates. Choose `activity_date` for range partitioning because date-window queries and lifecycle operations align with it. Choose `(activity_date,user_id,activity_id)` as this learning table's primary key. PostgreSQL requires a partitioned table's primary/unique constraint to include the partition-key columns, since child-local indexes alone cannot enforce an unconstrained uniqueness claim across all date children.

This primary key makes the tuple unique. It does **not** independently enforce that activity_id=1001 can occur nowhere on another date or for another user. If global event-ID uniqueness is a requirement, design an additional registry or another supported architecture; do not claim the chosen composite constraint supplies it.

For the frequent user/time query, add an index on `(user_id,occurred_at DESC,activity_id ASC)`. It helps locate a user's recent rows within surviving children and distinguishes equal event times. It does not choose the date child. A small test table may still use a sequential scan because that is cheaper; a scan on the correct child is not evidence pruning failed.

## Read the schema and insert in order

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

## Trace the query and physical evidence

```sql
SELECT activity_id, activity_type, occurred_at
FROM user_activity
WHERE activity_date = DATE '2026-10-25' AND user_id = 42
ORDER BY occurred_at DESC, activity_id ASC;
```

The date excludes the October 26 child. The user condition excludes activity 2001 belonging to user 84. Three rows remain: 1001 at 09:00 and 1002/1003 at 09:05. Descending time puts the two 09:05 events first; ascending ID breaks their tie as 1002 before 1003. The result is **1002,1003,1001**, derived from both sort columns.

The script queries `tableoid::regclass`, PostgreSQL's physical table identifier rendered as a relation name, to verify four rows in the October 25 child and one in October 26. `EXPLAIN` displays a plan for the date/user query; absence of the October 26 child is the relevant pruning evidence. These are separate from output ordering, which must be requested with `ORDER BY`.

Official references: PostgreSQL's [partition declarations and constraints](https://www.postgresql.org/docs/current/ddl-partitioning.html) and [CREATE TABLE grammar](https://www.postgresql.org/docs/current/sql-createtable.html). SQL syntax is database-specific; do not generalize PostgreSQL's exact rules to every SQL product.

## Comparison, analogy, and project mapping

An archive uses calendar drawers, a receipt number, and a lookup card. Those are placement, identity, and retrieval respectively. The analogy stops where the database automatically routes writes and enforces constraints concurrently.

Use date partitions when pruning/retention justify maintenance. Keep an ordinary indexed table when volume is small or most queries cross all dates. A different PostgreSQL table could hash-partition on user_id, but that changes the lifecycle and range-query trade-off; it does not turn PostgreSQL into Cassandra.

[Post.java](../../../../../../src/main/java/com/instagram/backend/domain/Post.java) currently uses an ID primary key and `createdAt`. [PostRepository](../../../../../../src/main/java/com/instagram/backend/repository/PostRepository.java) does not declare this activity table or a partition migration. The schema here is isolated learning material, not an application database change.

## Failure drill — executed isolated SQL exercise

- **Predict:** only October 25 exists, then the script inserts 1004 at October 26 00:00. There is no matching child.
- **Symptom:** a partition-bound check error is caught as the expected controlled failure; no row is inserted.
- **Contain:** retain the failed event identity and correct timestamp instead of changing its date.
- **Recover:** create the October 26 child, retry 1004, and verify counts 4/1 and its exact physical child.
- **Prevent:** provision upcoming intervals, enforce the UTC-date rule, and validate routing during deployment. The script finally rolls back the schema and checks its absence; see [verification](./verification.md).

## Recap and stop/go

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
