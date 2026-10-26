# Partitioning, Replication, and Hot Keys — module entry

[Database category](./README.md) · [Complete lesson sequence](./partitioning-and-keys/README.md)

This stable URL remains the entry for the older system-design learning plan. The detailed teaching is now split into 11 prerequisite-ordered lessons; the overview itself is not a substitute for them. Comprehension remains TODO.

## Begin with boundaries

A partition subdivides data by a rule. A shard places a subset across a distributed storage boundary. A replica is a maintained copy. One server can store many partitions, and one logical partition can have copies on several servers. [Lesson 1](./partitioning-and-keys/01-partitions-shards-and-replicas.md) derives these differences on five activity events.

## Choose the distribution rule

[Range partitioning](./partitioning-and-keys/02-range-partitioning.md) groups neighboring values; [hash partitioning](./partitioning-and-keys/03-hash-partitioning.md) computes destinations from keys. [Hotspot diagnosis](./partitioning-and-keys/04-hotspots-and-hot-keys.md) distinguishes concentrated request rate from oversized stored history. [Consistent hashing](./partitioning-and-keys/05-consistent-hashing.md) limits ownership movement during membership changes; it does not split one unchanged hot key.

## Build and route the actual table

[SQL key choice](./partitioning-and-keys/06-sql-partition-key-and-table-creation.md) separates identity, date children, and indexes. Cassandra lessons then teach [primary-key parentheses](./partitioning-and-keys/07-cassandra-primary-key-syntax.md), [clustering order and ties](./partitioning-and-keys/08-cassandra-clustering-columns.md), [partition creation by writes](./partitioning-and-keys/09-cassandra-partition-creation.md), and [key-to-replica routing](./partitioning-and-keys/10-cassandra-query-routing.md). Finish with [Instagram query-specific key design](./partitioning-and-keys/11-instagram-key-design.md).

## Evidence and learning state

[Teaching review and verification](./partitioning-and-keys/verification.md) distinguishes executed PostgreSQL checks from unexecuted Cassandra examples and paper routing models. Read one lesson, attempt its checkpoints, and record actual understanding in [learner progress](../../learner-progress.md) before advancing.
