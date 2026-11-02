# Lesson 7 — Cassandra PRIMARY KEY: parentheses define grouping and row identity

[Module](./README.md) · [Previous: SQL key roles](./06-sql-partition-key-and-table-creation.md) · [Next: clustering columns](./08-cassandra-clustering-columns.md)

- New core terms: CQL, composite partition key, clustering column.

Core study: 10–15 minutes. Prerequisites: identity differs from placement; equal partition keys stay together.

## Two VIEW_POST events must remain two events — Must understand

User 42 viewed post 900 and post 901 at 09:05 on October 25. These are activities 1002 and 1003. Our history must preserve both and return recent activity for one user/day. A key that says only “user 42, October 25, VIEW_POST” cannot distinguish the occurrences.

This is a row-identity question as well as a grouping question. Which values name the group, and which values distinguish rows inside it? We will use those two questions to read Cassandra's parentheses rather than memorize their appearance.

**CQL** means **Cassandra Query Language**. Its declarations resemble SQL, but the keys define different placement and access rules. Cassandra is a product name, not an acronym. “NoSQL,” commonly expanded as “not only SQL,” is a broad label and does not give every database Cassandra's key behavior.

In Cassandra, several columns grouped as a **composite partition key** name one exact logical partition. A **clustering column** distinguishes and orders rows inside that partition. The complete primary key contains both sets of columns, so leaving activity_id out of that complete identity matters even if it exists as an ordinary column.

## Read the inner parentheses as one group

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

## The type-only key addresses the same row twice

Use the fixed dataset, especially two different events for user 42 on October 25:

| Event | Time | Type | Post |
|---|---|---|---:|
| 1002 | 09:05 UTC | VIEW_POST | 900 |
| 1003 | 09:05 UTC | VIEW_POST | 901 |

Under `PRIMARY KEY ((user_id,activity_date),activity_type)`, both have full key `(42,2026-10-25,VIEW_POST)`. There is no activity_id or time component in the key to distinguish them. A normal Cassandra insert is an **upsert**: it writes the addressed row, inserting if absent or updating columns if it already exists. These writes target one row, not two event-history rows. Under an explicitly later write timestamp for the second mutation, its supplied non-key fields replace the earlier values. Conflicting equal timestamps have resolution rules of their own; do not rely on arrival order as a general guarantee.

This is appropriate if the business wants *one latest summary per activity type per day*. It is incorrect if the business wants every occurrence. The problem is row identity, not a hash collision or a hot partition.

## Keep the user/day group; distinguish each occurrence

```cql
PRIMARY KEY ((user_id, activity_date), occurred_at, activity_id)
```

The partition still groups one user/day. Event time orders rows, while activity_id distinguishes events sharing the same time. IDs 1002 and 1003 now have different full keys despite matching type and timestamp. The key's purpose is derived from the query and identity requirement, not simply “pick a unique field.” A production event identifier needs a durable uniqueness/generation strategy; the small integer IDs here are fixed teaching identities.

Changing a primary key is not an in-place reinterpretation of existing rows. A new layout requires a new table/migration design. Data already overwritten because identity was too coarse cannot be recreated from the surviving row alone.

## A summary key and an event-history key serve different purposes

Think of a folder labeled user/day and files labeled event time/ID. Labeling files only “VIEW_POST” reuses the same slot. The analogy stops where Cassandra resolves concurrent cell writes and places replica copies.

Use the type-only layout for a one-per-type summary if that is the actual query. Use event identity for a history. Avoid assuming any “unique column” becomes a row key when it is not included in `PRIMARY KEY`.

[Post.id](../../../../../../src/main/java/com/instagram/backend/domain/Post.java) is a relational identity, not a Cassandra partition/clustering declaration. The proposed activity table is not part of the backend. [The CQL example](./examples/cassandra-activity.cql) contains both layouts so the identity distinction can be tested later; runtime status is recorded in verification.

## Write both views with the type-only identity — paper exercise

Insert 1002, then 1003, under the type-only key. Predict how many addressed VIEW_POST rows can remain in that user/day group.

Both writes name one row. Having a separate activity_id column does not help if the column is outside the primary key. Stop treating that table as a complete occurrence history and preserve any original event log.

Recover into a new event-keyed table by replaying the source events you still have, then compare their IDs. The surviving summary row cannot recreate overwritten occurrences on its own. Test equal types and equal times before accepting the schema, and explicitly decide whether the table represents a history or a one-per-type summary.

## Before moving on

Artifact: `READY`; comprehension: `TODO`.

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
