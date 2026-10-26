# Partitioning and key-design module — teaching and execution evidence

Date: 2026-10-25. Scope: 11 new micro-lessons, module/category navigation, stable legacy entry, example SQL/CQL, feature note, and learner/backlog records. Existing unrelated working-tree edits are excluded. No application storage, API, or UI behavior changed.

## Placement and source boundary

Canonical home: Databases & Data Modeling, primarily roadmap 84/85 with replica context 86–90; scalability topics 33/49 cross-link to these canonical explanations. Consistent hashing has applications to caches/load balancing, but the lessons here concern data ownership. Kafka partitioning is not silently conflated with database partitioning.

The supplied ChatGPT conversation pages were not accessible through retrieval; the third URL includes trailing `In`. The pasted notes supplied the topic scope. Primary sources reviewed: official PostgreSQL table-partition/CREATE TABLE documentation and Apache Cassandra's data definition, data manipulation, data-modeling, and token/replication architecture documentation. Citations appear in the lessons. No hidden conversation contents, production hash numbers, or benchmark capacities are claimed.

## Per-lesson semantic author review

This is an author review of teaching completeness, not a learner assessment or independent review. The [teaching-quality rule](../../../../../../AGENTS.md#concept-lesson-teaching-quality-standard) requires definitions and derivations, not merely headings or valid links.

| Part | Meaning/prerequisites checked | Worked reasoning checked | Limitation and failure checked |
|---|---|---|---|
| [1](./01-partitions-shards-and-replicas.md) | Partition, shard, replica, server, index | Five events split 4/1; physical copies do not add business identities | Two children on one server do not survive its failure by themselves |
| [2](./02-range-partitioning.md) | Ordered ranges, inclusive/exclusive bounds, pruning | October 26 fails P25's strict upper bound and enters P26 | Missing child fails; pruning is not an inside-partition index |
| [3](./03-hash-partitioning.md) | Deterministic hash, collision, modulo, bucket | 35/80 modulo 3 and 4; both sample key mappings move | Toy hashes are not product hashes; routing version must match stored placement |
| [4](./04-hotspots-and-hot-keys.md) | Hot key versus oversized partition versus hot node | 9000−3000=6000 queued/second; 60000 in ten seconds; four-way conditional 2250 | Time buckets do not split active traffic; read fan-out and physical placement cost |
| [5](./05-consistent-hashing.md) | Token, wrapped ownership interval, vnode | 35→B; 80/65→C; D70 takes only (50,70], including 65 | Stable hash differs from replica consistency; unchanged hot key remains together |
| [6](./06-sql-partition-key-and-table-creation.md) | SQL expansion, identity, partition, index, plan | Parent/children, four/one rows, 1002/1003 time tie, boundary evidence | Composite uniqueness is scoped; explicit child creation and date validation |
| [7](./07-cassandra-primary-key-syntax.md) | CQL expansion; grouped versus flat parentheses; upsert | Same VIEW_POST full key for 1002/1003 versus event-distinguishing tuple | Type-only key fits a summary, not event history; write resolution is not arrival order |
| [8](./08-cassandra-clustering-columns.md) | Clustering versus cluster/index; ordered tuple and slice | 09:05 tied IDs precede 09:00; LIMIT 2 and inclusive time slice | Not node placement/global order; timestamp alone can lose equal-time events |
| [9](./09-cassandra-partition-creation.md) | Keyspace/schema/data groups; datacenter and type meanings | Five writes form three exact-key partitions, not five nodes/files | Supplied day is not automatically inferred; key repair needs migration semantics |
| [10](./10-cassandra-query-routing.md) | Encoded complete key, partitioner, coordinator, replicas, RF/level | Fixed three-node/rack toy topology; B/C for 35; local quorum 2 | Coordinator A is not an extra replica; filtering does not supply a missing key |
| [11](./11-instagram-key-design.md) | Query-driven choice and current versus proposed components | User/day grouping, cross-day 1004-first order, two versus eight reads | Worst-case users, duplication, migration, and ranking/pagination remain design costs |

All 11 core paths include a defined problem, initialized example, mechanism reasoning, analogy with a limit, actual code mapping, suitable/unsuitable choices, cost, and prediction/symptom/containment/recovery/prevention. Each ends with three explained checkpoint questions. One lesson at a time remains the teaching rule; comprehension stays TODO.

## Executed PostgreSQL lab

Used installed PostgreSQL 16 binaries to initialize a new temporary cluster under a generated private temporary directory. It listened only on a private Unix-domain socket, with TCP listening disabled. The application database was not contacted. Sandbox shared-memory restrictions required the approved isolated-process execution path; no cloud/container resources were created.

[postgresql-partitions.sql](./examples/postgresql-partitions.sql) was run using `psql -X -v ON_ERROR_STOP=1`. [Recorded output](./examples/postgresql-verification.txt) shows:

- Four October 25 rows inserted.
- October 26 insertion rejected while its child was missing; only the expected partition check failure was caught.
- The October 26 child was created; the same boundary row then inserted successfully.
- Physical child counts 4 and 1; user 42/day25 ordering 1002,1003,1001.
- A plan scanning only the October 25 child. A sequential scan is reasonable for this tiny dataset and does not negate pruning.
- Assertions for exact row count, boundary ownership, and tie order passed.
- Transaction rollback removed the learning schema/data; an additional assertion confirmed the schema was absent.
- The temporary server was stopped and the generated directory removed after execution.

The first SQL check exposed an invalid alias cast in the evidence query's ORDER BY. That query was corrected and the entire isolated script rerun successfully; the saved output is from the successful run.

## Cassandra runtime limitation

[Cassandra example](./examples/cassandra-activity.cql) is complete for a disposable single-node datacenter1 setup, including two table layouts, the five fixed inserts, bounded reads, actual token inspection, and explicit-timestamp summary overwrite. It was reviewed against the CQL schema/query grammar, but **not executed**. Docker could not connect to its daemon; no Cassandra server was available or installed. Thus no actual Cassandra token numbers, row results, tracing, or node-failure evidence is claimed.

The RF=2 three-node routing exercise is a separate toy model with one datacenter and three distinct racks. The CQL lab requests RF=1 on one node. Their assumptions are explicit and must not be combined into a false executed topology claim. Execution and actual tracing are follow-up lab work; current READY status applies to lesson artifacts, not a completed Cassandra implementation milestone.

## Document/model checks

Checked local Markdown paths/anchors, balanced fences and expandable answers, 11 lesson rows, 33 explained checkpoint answers, unchanged TODO comprehension, and Git whitespace. Independently recalculated date grouping, primary-key collapse versus history preservation, order/ties, modulo movement, ring ownership before/after D70, replica sets, quorum requirement, write-rate arithmetic, and two-day shard fan-out. The model checks do not execute Cassandra.

Recorded document/model check output:

```text
PASS: 11 lessons, 33 explained checkpoints, 207 local paths/anchors; TODO comprehension and complete failure drills.
PASS: fixed grouping 4/1 and 3/1/1; exact tie order; summary-key collision; modulo; all 100 ring positions before/after D70; rate/fan-out/quorum arithmetic.
PASS: executed PostgreSQL evidence includes controlled failure, recovery, boundary/order assertions, rollback and server cleanup. Cassandra remains unexecuted.
```

Git whitespace checks passed after the final edits. These checks validate the stated document models, not a live Cassandra protocol or learner mastery.

## Follow-up and completion scope

No product feature or system-design implementation status is upgraded. Proposed F-016/SD-007 measurement and F-021 read-model work remain separate. To assess learning, require the learner's explanation, correct prediction, diagnosed failure, and alternative comparison without notes, then record later recall separately. To verify Cassandra runtime behavior, run the example on a disposable instance and add actual results/traces before claiming that lab complete.
