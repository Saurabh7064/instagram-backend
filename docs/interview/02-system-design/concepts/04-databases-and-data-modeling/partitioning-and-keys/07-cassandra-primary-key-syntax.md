# Lesson 7 — Cassandra PRIMARY KEY: parentheses define grouping and row identity

[Module](./README.md) · [Previous: SQL key roles](./06-sql-partition-key-and-table-creation.md) · [Next: clustering columns](./08-cassandra-clustering-columns.md)

- Artifact: `READY`; comprehension: `TODO`.
- Core study: 10–15 minutes. Primary idea: read the nested parentheses before deciding what one Cassandra row represents.
- Prerequisites: identity differs from placement; equal partition keys stay together.
- New core terms: CQL, composite partition key, clustering column.

## Start with the question, not a guessed primary key — Must understand

We want all activities for one user on one UTC date, newest first, while preserving multiple events of the same type. A table's primary key must make that query practical *and* distinguish events that should coexist.

**CQL means Cassandra Query Language.** It resembles SQL syntax, but its keys define different access and placement rules. **Cassandra is a product name, not an acronym.** “NoSQL,” commonly expanded as “not only SQL,” is a broad family label; do not assume all those databases use Cassandra's key structure.

A **composite partition key** has more than one column whose values together define one exact logical partition. A **clustering column** distinguishes and orders rows within that partition. The complete Cassandra primary key consists of the partition-key columns plus every clustering column, so it determines row identity as well as grouping.

## Decode the syntax in small steps

```cql
PRIMARY KEY (user_id, activity_date, activity_type)
```

With no inner grouping, the first item `user_id` is the partition key. `activity_date` and `activity_type` are clustering columns. This produces one partition per user across dates, not one per user/day.

```cql
PRIMARY KEY ((user_id, activity_date), activity_type)
```

The inner parentheses group `user_id` and `activity_date` into one composite partition key. The remaining `activity_type` is a clustering column. The partition is one user/day; the row is one user/day/type tuple.

There are not two independent routing stages, “choose user then choose date.” Cassandra hashes the encoded **complete tuple**. The date is part of the grouping identity. `PRIMARY KEY ((a,b),c,d)` means partition tuple `(a,b)` with ordered clustering tuple `(c,d)`.

The [Cassandra data-definition documentation](https://cassandra.apache.org/doc/latest/cassandra/developing/cql/ddl.html) defines this grammar and component roles. Parentheses are functional syntax, not cosmetic formatting.

## Work through the problem in your proposed key

Use the fixed dataset, especially two different events for user 42 on October 25:

| Event | Time | Type | Post |
|---|---|---|---:|
| 1002 | 09:05 UTC | VIEW_POST | 900 |
| 1003 | 09:05 UTC | VIEW_POST | 901 |

Under `PRIMARY KEY ((user_id,activity_date),activity_type)`, both have full key `(42,2026-10-25,VIEW_POST)`. There is no activity_id or time component in the key to distinguish them. A normal Cassandra insert is an **upsert**: it writes the addressed row, inserting if absent or updating columns if it already exists. These writes target one row, not two event-history rows. Under an explicitly later write timestamp for the second mutation, its supplied non-key fields replace the earlier values. Conflicting equal timestamps have resolution rules of their own; do not rely on arrival order as a general guarantee.

This is appropriate if the business wants *one latest summary per activity type per day*. It is incorrect if the business wants every occurrence. The problem is row identity, not a hash collision or a hot partition.

## Choose a key that preserves the requested events

```cql
PRIMARY KEY ((user_id, activity_date), occurred_at, activity_id)
```

The partition still groups one user/day. Event time orders rows, while activity_id distinguishes events sharing the same time. IDs 1002 and 1003 now have different full keys despite matching type and timestamp. The key's purpose is derived from the query and identity requirement, not simply “pick a unique field.” A production event identifier needs a durable uniqueness/generation strategy; the small integer IDs here are fixed teaching identities.

Changing a primary key is not an in-place reinterpretation of existing rows. A new layout requires a new table/migration design. Data already overwritten because identity was too coarse cannot be recreated from the surviving row alone.

## Analogy, choice, and project mapping

Think of a folder labeled user/day and files labeled event time/ID. Labeling files only “VIEW_POST” reuses the same slot. The analogy stops where Cassandra resolves concurrent cell writes and places replica copies.

Use the type-only layout for a one-per-type summary if that is the actual query. Use event identity for a history. Avoid assuming any “unique column” becomes a row key when it is not included in `PRIMARY KEY`.

[Post.id](../../../../../../src/main/java/com/instagram/backend/domain/Post.java) is a relational identity, not a Cassandra partition/clustering declaration. The proposed activity table is not part of the backend. [The CQL example](./examples/cassandra-activity.cql) contains both layouts so the identity distinction can be tested later; runtime status is recorded in verification.

## Failure drill — paper exercise

- **Predict:** insert 1002 then 1003 into the type-only key. How many VIEW_POST rows can this partition hold?
- **Symptom:** one addressed row instead of two; a column called activity_id exists but was not part of identity.
- **Contain:** stop treating the table as complete history; preserve any original event log for recovery.
- **Recover:** design a new event-keyed table, replay recoverable source events, and compare IDs. Do not invent overwritten events.
- **Prevent:** test equal types and equal times before schema acceptance. State whether the table stores events or summaries.

## Recap and stop/go

Read both parenthesis patterns aloud, identify partition/clustering components, and derive why 1002 and 1003 collide in one layout and coexist in the other.

## Questions and explained answers

<details>
<summary>1. What changes when the inner parentheses are removed?</summary>

The partition key becomes user_id alone; the date moves into the clustering tuple. Rows across dates for that user share one partition rather than separate user/day partitions.

</details>

<details>
<summary>2. Why does a non-key activity_id column not protect both events?</summary>

Cassandra addresses the row by the complete primary key. A non-key field is a value at that identity; giving it another value does not create another identity.

</details>

<details>
<summary>3. Is the type-only primary key always wrong?</summary>

No. It fits one summary row per user/day/type. It is wrong for the stated requirement to preserve every event, including repeated types at equal times.

</details>
