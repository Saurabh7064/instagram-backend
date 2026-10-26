# Partitioning, sharding, and Cassandra key design

[Database category](../README.md) · [Concept index](../../index.md) · [Learner progress](../../../learner-progress.md) · [Teaching review / evidence](./verification.md)

These topics belong primarily to **Databases & Data Modeling**, expanding roadmap 84 (sharding) and 85 (partitioning), with 86–90 supplying replication context. Related roadmap topics [33 (consistent hashing) and 49 (hotspots)](../../02-scalability-and-load-balancing/README.md) link here from Scalability & Load Balancing. The canonical lessons explain data placement; cache/request-routing implementations remain separate. A database partition is different from a Kafka topic partition; messaging remains a separate module.

Artifact: `READY` for 11 authored lessons after teaching/document checks. Comprehension: `TODO` throughout. PostgreSQL's isolated exercise was executed; Cassandra CQL syntax and routing were reviewed/model-checked, but no live Cassandra cluster was run. See verification for the exact evidence boundary.

## Start here

Know only that a table has rows/columns and a database server stores them. Lesson 1 defines the storage/distribution vocabulary. Each part has a 10–15 minute core, an initialized example, trade-offs, a failure/recovery exercise, and three explained checkpoints. Study one part at a time, explain/predict/diagnose without notes, and record the attempt before advancing. Creating the whole module does not move learner status forward.

## Where your topics fit

| Your topic or question | Canonical lesson | Important distinction |
|---|---|---|
| Sharding; partition versus node | 1 | Logical subdivision, distributed placement, and copies are different |
| Range-based partitioning | 2 | Neighboring values group together; the active range can be hot |
| Hash-based partitioning | 3 | Different keys can spread; equal keys remain together |
| Hotspot / overloaded partition / hot key | 4 | Traffic rate and stored size need separate diagnosis |
| Consistent hashing | 5 | Reduces membership remapping; does not split an unchanged hot key |
| What is a SQL partition key? How choose it? | 6 | Partition boundary, row identity, and lookup index can differ |
| Cassandra primary-key syntax; composite key | 7 | Inner parentheses define the complete partition tuple |
| Cluster key / clustering key | 8 | Clustering columns order and distinguish rows within a partition |
| How is a partition created? What does CREATE TABLE do? | 9 | Schema declares a rule; writes populate exact-key logical groups |
| Key → hash → token range → node | 10 | Include encoded complete key, replica set, coordinator, and required responses |
| Partition key for Instagram | 11 | One key per access pattern/table, with worst-case capacity and read costs |

## Lesson sequence

| Part | Lesson | Artifact | Comprehension |
|---:|---|---|---|
| 1 | [A partition is a data boundary; a shard is a distribution boundary](./01-partitions-shards-and-replicas.md) | `READY` | `TODO` |
| 2 | [Range partitioning: give each interval an owner](./02-range-partitioning.md) | `READY` | `TODO` |
| 3 | [Hash partitioning: spread different keys, preserve equal keys](./03-hash-partitioning.md) | `READY` | `TODO` |
| 4 | [A hot key is a workload problem, not just uneven storage](./04-hotspots-and-hot-keys.md) | `READY` | `TODO` |
| 5 | [Consistent hashing reduces remapping when membership changes](./05-consistent-hashing.md) | `READY` | `TODO` |
| 6 | [Choose a SQL partition key from operations, not just identity](./06-sql-partition-key-and-table-creation.md) | `READY` | `TODO` |
| 7 | [Cassandra PRIMARY KEY: parentheses define grouping and row identity](./07-cassandra-primary-key-syntax.md) | `READY` | `TODO` |
| 8 | [Clustering columns order rows inside one Cassandra partition](./08-cassandra-clustering-columns.md) | `READY` | `TODO` |
| 9 | [Cassandra CREATE TABLE defines the grouping rule, not one table per user](./09-cassandra-partition-creation.md) | `READY` | `TODO` |
| 10 | [Cassandra query routing: complete key to token to replica set](./10-cassandra-query-routing.md) | `READY` | `TODO` |
| 11 | [Instagram has query-specific partition keys, not one universal key](./11-instagram-key-design.md) | `READY` | `TODO` |

## Fixed activity dataset

All examples use these event identities. Times are UTC (Coordinated Universal Time); `activity_date` is the UTC calendar date of `occurred_at`. An activity ID distinguishes an event, even when two share a timestamp and type.

| user_id | activity_date | occurred_at (UTC) | activity_id | activity_type | post_id |
|---:|---|---|---:|---|---:|
| 42 | 2026-10-25 | 2026-10-25 09:00:00 | 1001 | LOGIN | absent |
| 42 | 2026-10-25 | 2026-10-25 09:05:00 | 1002 | VIEW_POST | 900 |
| 42 | 2026-10-25 | 2026-10-25 09:05:00 | 1003 | VIEW_POST | 901 |
| 84 | 2026-10-25 | 2026-10-25 09:08:00 | 2001 | LIKE_POST | 900 |
| 42 | 2026-10-26 | 2026-10-26 00:00:00 | 1004 | VIEW_POST | 902 |

PostgreSQL date children group four rows on October 25 and one on October 26. Cassandra `(user_id,activity_date)` groups three rows for (42,Oct25), one for (84,Oct25), and one for (42,Oct26). Those are two different partitioning units on the same dataset.

Newest-first order for user 42 on October 25 is 1002,1003,1001: 09:05 precedes 09:00, then ascending ID breaks the equal-time tie. Across both days, 1004 comes first because October 26 is later. Key/order choices are derived in the lessons rather than assumed from this recap.

## Corrections to preserve from the original notes

1. Consistent hashing helps limit ownership changes when nodes join/leave. It can improve many-key placement with appropriate tokens, but a hot exact key still maps together.
2. One token range has configured replicas; “all its data is on one node” omits the copies. A logical partition is neither an entire token range nor a server.
3. Cassandra's full partition tuple, not only its first column, is encoded and hashed. Clustering columns are not routing inputs.
4. `PRIMARY KEY ((user_id,activity_date),activity_type)` is one row per type per user/day. It stores summaries, not repeated event occurrences.
5. Cassandra clustering columns describe ordered access and identity inside a logical group, not an entire cluster's global order or a guaranteed single physical file.
6. PostgreSQL child partitions are explicitly declared; Cassandra's exact-key groups are populated by writes. Similar vocabulary does not imply identical CREATE TABLE behavior.

## Exercises and implementation follow-up

[PostgreSQL script](./examples/postgresql-partitions.sql) creates an isolated schema, checks missing-child failure/recovery, row counts, order, and physical boundary placement, then rolls back. [Cassandra script](./examples/cassandra-activity.cql) contains the complete proposed single-node setup, five inserts, bounded reads, actual-token query, and deterministic summary-overwrite demonstration; its execution remains unverified here.

Integrated paper exercise: choose between a relational date layout and a Cassandra user/day layout for one user's recent activity. Derive grouping counts, query order, response behavior after a replica failure, and the cost of four-way write sharding over two days. Predict the key problem before choosing a remedy; verify complete event identities after any migration.

Proposed implementation tasks: for F-016 feed expansion and SD-007 query planning, measure the current relational read and compare a suitable index with bounded read models. For F-021 ranking, define ordering/pagination separately. Do not migrate the current database to Cassandra solely because a lesson introduces it.

## Source-note access

The three supplied ChatGPT conversation URLs could not be retrieved in this session. The third URL also includes a trailing `In` after its conversation identifier. Lessons use the pasted material and linked official PostgreSQL/Apache Cassandra documentation; no hidden conversation content is claimed to have been read.

## Stop/go

Start at part 1. Before advancing, explain its idea, derive its example, predict the failure, compare an alternative, and answer its checkpoints without opening the answers. All comprehension remains TODO until that evidence is provided. Recheck after one day and one week.
