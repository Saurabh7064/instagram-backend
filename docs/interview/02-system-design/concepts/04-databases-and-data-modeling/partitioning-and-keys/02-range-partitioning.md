# Lesson 2 — Range partitioning: give each interval an owner

[Module](./README.md) · [Previous: boundaries](./01-partitions-shards-and-replicas.md) · [Next: hashing](./03-hash-partitioning.md)

- New core terms: range partitioning, boundary, partition pruning.

Core study: 10–15 minutes. Prerequisite: a partition subdivides rows; local partitions need not reside on separate servers.

## October 26 midnight belongs to which day? — Must understand

A user requests October 25 activity. Our five events span two days: 1001, 1002, 1003, and 2001 belong to October 25; 1004 arrives exactly at midnight on October 26. Grouping by day would let the database ignore October 26 for the first request and retire whole old days later.

The boundary needs one clear rule. If October 25 includes midnight on October 26 *and* October 26 starts there, event 1004 fits twice. Instead, include the start of each interval and exclude its end. P25 covers `2026-10-25 <= activity_date < 2026-10-26`; P26 begins where P25 stops.

Assigning rows to ordered intervals is **range partitioning**. The interval endpoints are its **boundaries**. The shorthand `[start,end)` means start included, end excluded. It lets adjacent daily intervals meet without overlap or a gap.

Once a query asks specifically for October 25, the database can exclude a child whose bounds contain only October 26. Skipping impossible partitions is **partition pruning**. It reduces the data groups to inspect; it does not yet find user 42's rows inside the remaining day. We will keep those two jobs separate as we follow the rows and query.

## Apply both date comparisons before creating the children

Use `activity_date` as the partition key. Define P25 as `[2026-10-25,2026-10-26)` and P26 as `[2026-10-26,2026-10-27)`.

- Activity 1001 has date October 25. October 25 >= October 25 and October 25 < October 26, so it belongs to P25.
- Activities 1002,1003,2001 have the same date and satisfy the same bounds. P25 therefore contains four rows.
- Activity 1004 is exactly October 26. It fails P25's strict upper comparison because October 26 < October 26 is false. It satisfies P26's lower comparison, so P26 contains one row.

This is why the boundary row is not stored twice. The date chooses the interval; the activity's user does not determine which date partition owns it.

```sql
CREATE TABLE user_activity (
    user_id bigint NOT NULL,
    activity_date date NOT NULL,
    occurred_at timestamptz NOT NULL,
    activity_id bigint NOT NULL,
    activity_type text NOT NULL,
    post_id bigint,
    PRIMARY KEY (activity_date, user_id, activity_id)
) PARTITION BY RANGE (activity_date);

CREATE TABLE user_activity_2026_10_25
    PARTITION OF user_activity
    FOR VALUES FROM ('2026-10-25') TO ('2026-10-26');

CREATE TABLE user_activity_2026_10_26
    PARTITION OF user_activity
    FOR VALUES FROM ('2026-10-26') TO ('2026-10-27');
```

`CREATE TABLE` declares columns and constraints. `PARTITION BY RANGE` makes the parent route rows using date intervals. Each `PARTITION OF` statement creates one child with its bounds. `FROM` is inclusive; `TO` is exclusive. `timestamptz` is PostgreSQL's timestamp-with-time-zone type; here values use UTC. The primary-key columns identify rows and include the partition key, as required for this partitioned table's uniqueness constraint. It is not declaring that every user/day becomes a child table.

## Skipping a day is different from finding a user

A date predicate `activity_date = DATE '2026-10-25'` excludes P26. Within P25, filtering `user_id=42` still needs a scan or an appropriate index: four rows are in the date group, but only IDs 1001,1002,1003 belong to user 42. Pruning eliminates a whole irrelevant partition; it does not magically find the three rows inside the remaining one.

Without the date predicate, “all events for user 42” can require checking both date partitions. Those dates may still be efficiently searched with indexes, but date pruning cannot infer an omitted time range. The [PostgreSQL partitioning documentation](https://www.postgresql.org/docs/current/ddl-partitioning.html) defines bounds and describes pruning.

## Why a useful date split can still have a busy newest day

Calendar folders help you find one day's receipts and retire one whole folder. The analogy stops at database constraints, concurrent writes, and query plans. The newest folder may receive nearly all current writes; range partitioning is not a guarantee of balanced traffic.

Choose ranges when date windows, retention, or adjacent-key scans matter. Avoid adding daily children blindly to a small table; there is metadata and maintenance overhead. A narrower interval bounds historical size but may leave all current writes in the newest interval. Hashing addresses a different distribution dimension, taught next.

## Project mapping

[Post.createdAt](../../../../../../src/main/java/com/instagram/backend/domain/Post.java) is a real timestamp, but its presence does not partition the `posts` table. These `user_activity` statements are isolated learning objects. The [SQL lesson](./06-sql-partition-key-and-table-creation.md) teaches execution and a missing-child failure in detail; [the example script](./examples/postgresql-partitions.sql) verifies the bounds without changing application tables.

## Insert midnight's event before P26 exists — isolated SQL exercise

Suppose only the P25 child exists. Predict where event 1004, dated October 26, can go. Its date must not be changed merely to fit the available child.

No child matches, so the insert fails; PostgreSQL does not automatically create the next daily child. Retain the event identity and correct timestamp for retry rather than discarding the activity.

Create P26, retry 1004, and check that P25 contains four rows while P26 contains one. Prepare upcoming intervals ahead of time and alert on missing coverage. The date-derivation rule must consistently use UTC, especially at midnight. The existing SQL lesson and verification record describe the lab evidence; this explanation does not claim a new execution.

## Before moving on

Artifact: `READY`; comprehension: `TODO`.

Calculate both comparisons for activity 1004. Explain the difference between choosing a child, pruning a child, and finding rows inside it.

## Questions and explained answers

<details>
<summary>1. Why is the October 26 row excluded from P25?</summary>

P25's upper bound is exclusive. Its predicate requires the date to be less than October 26; equality is false. P26 includes that date at its lower boundary.

</details>

<details>
<summary>2. Does a date partition make user 42's query constant-time?</summary>

No. It can narrow the date groups, but the database still has to locate that user's rows inside the group. Index design, row volume, and the selected range still matter.

</details>

<details>
<summary>3. Why can a date layout still have a write hotspot?</summary>

Most incoming events can share today's date. The active child receives the load even while historical partitions are quiet. Range boundaries distribute values, not necessarily requests.

</details>
