# Lesson 2 — Range partitioning: give each interval an owner

[Module](./README.md) · [Previous: boundaries](./01-partitions-shards-and-replicas.md) · [Next: hashing](./03-hash-partitioning.md)

- Artifact: `READY`; comprehension: `TODO`.
- Core study: 10–15 minutes. Primary idea: grouping neighboring values makes range operations manageable but can concentrate recent traffic.
- Prerequisite: a partition subdivides rows; local partitions need not reside on separate servers.
- New core terms: range partitioning, boundary, partition pruning.

## Why dates are a useful split — Must understand

A user asks for October 25 activity. A retention job later needs to retire old days. If rows are grouped by date intervals, each operation can reason about a relevant group instead of treating the table as one undifferentiated history.

**Range partitioning** assigns each row to an interval of ordered key values. A **boundary** is an interval endpoint. In this PostgreSQL example, lower bounds are included and upper bounds excluded: `[start,end)` means `start <= value < end`. One interval ending exactly where the next begins avoids overlap and gaps.

**Partition pruning** is the query planner/executor skipping partitions whose bounds cannot contain matching rows. It is not an index and does not guarantee that every query is fast. The query must supply information that lets the database exclude irrelevant intervals.

## Derive row placement on the fixed dataset

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

## Derive query work

A date predicate `activity_date = DATE '2026-10-25'` excludes P26. Within P25, filtering `user_id=42` still needs a scan or an appropriate index: four rows are in the date group, but only IDs 1001,1002,1003 belong to user 42. Pruning eliminates a whole irrelevant partition; it does not magically find the three rows inside the remaining one.

Without the date predicate, “all events for user 42” can require checking both date partitions. Those dates may still be efficiently searched with indexes, but date pruning cannot infer an omitted time range. The [PostgreSQL partitioning documentation](https://www.postgresql.org/docs/current/ddl-partitioning.html) defines bounds and describes pruning.

## Trade-off and analogy

Calendar folders help you find one day's receipts and retire one whole folder. The analogy stops at database constraints, concurrent writes, and query plans. The newest folder may receive nearly all current writes; range partitioning is not a guarantee of balanced traffic.

Choose ranges when date windows, retention, or adjacent-key scans matter. Avoid adding daily children blindly to a small table; there is metadata and maintenance overhead. A narrower interval bounds historical size but may leave all current writes in the newest interval. Hashing addresses a different distribution dimension, taught next.

## Project mapping

[Post.createdAt](../../../../../../src/main/java/com/instagram/backend/domain/Post.java) is a real timestamp, but its presence does not partition the `posts` table. These `user_activity` statements are isolated learning objects. The [SQL lesson](./06-sql-partition-key-and-table-creation.md) teaches execution and a missing-child failure in detail; [the example script](./examples/postgresql-partitions.sql) verifies the bounds without changing application tables.

## Failure drill — isolated SQL lab / prediction first

- **Predict:** insert activity 1004 when only the P25 child exists. Where can October 26 go?
- **Symptom:** no child matches; the insertion fails instead of creating a date child automatically.
- **Contain:** stop/retry the affected writes with their event identities; do not discard them or falsify their date.
- **Recover:** create the correct P26 child and retry 1004. Verify four rows in P25 and one in P26.
- **Prevent:** create upcoming intervals ahead of time, alert on missing coverage, and keep date derivation consistent with UTC.

## Recap and stop/go

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
