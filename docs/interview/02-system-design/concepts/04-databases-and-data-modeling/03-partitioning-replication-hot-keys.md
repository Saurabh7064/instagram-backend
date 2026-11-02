# Partitioning, Replication, and Hot Keys — module entry

[Database category](./README.md) · [Complete lesson sequence](./partitioning-and-keys/README.md)

Suppose a user's activity history keeps growing. A date split might help remove old records. Another server might provide more capacity. A second copy might keep reads working after a failure. These moves sound similar when described as “distributing data,” but each changes a different part of the system.

This page helps you choose a starting point in the 11-lesson sequence. It remains the entry used by the older learning plan; the teaching itself lives in the linked lessons. Comprehension remains TODO.

## First separate dividing, placing, and copying

Start with [five activity events in lesson 1](./partitioning-and-keys/01-partitions-shards-and-replicas.md). Divide them into groups, move the groups, then add copies. The lesson makes the distinction visible before naming partitions, shards, and replicas. Two date groups on one server still share that server's failure and capacity limits.

## Then ask why one group is busy

[Range partitioning](./partitioning-and-keys/02-range-partitioning.md) keeps neighboring values together, which suits dates and retention. [Hash partitioning](./partitioning-and-keys/03-hash-partitioning.md) calculates a repeatable destination from a key, which can spread many distinct users.

Neither promises balanced requests. [Hotspot diagnosis](./partitioning-and-keys/04-hotspots-and-hot-keys.md) separates a large stored history from a key receiving too much current traffic. [Consistent hashing](./partitioning-and-keys/05-consistent-hashing.md) then explains how ownership can change when servers join. Moving an unchanged hot key moves its load; it does not divide it.

## Finally make the key answer a real query

Follow [the SQL exercise](./partitioning-and-keys/06-sql-partition-key-and-table-creation.md) to distinguish row identity, date children, and an index for user/time reads. The Cassandra sequence uses the same activity data while changing the storage model: [read the primary-key parentheses](./partitioning-and-keys/07-cassandra-primary-key-syntax.md), [order equal-time events](./partitioning-and-keys/08-cassandra-clustering-columns.md), [form groups through writes](./partitioning-and-keys/09-cassandra-partition-creation.md), and [route a complete key to its replicas](./partitioning-and-keys/10-cassandra-query-routing.md).

Finish by [choosing keys for Instagram queries](./partitioning-and-keys/11-instagram-key-design.md). A user's activity day and a viewer's home feed ask for different groupings, so the goal is to justify a key from its query and worst-case load rather than memorize one universal key.

## Read one lesson and check what you can explain

The [verification record](./partitioning-and-keys/verification.md) separates previously executed PostgreSQL checks from Cassandra examples and routing models that remain unexecuted. Complete one lesson's checkpoints, then record demonstrated understanding in [learner progress](../../learner-progress.md) before advancing. A working script and an explanation you can reproduce are different kinds of evidence.
