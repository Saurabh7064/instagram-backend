# Partitioning and Cassandra key-design lessons

## 1) What I built

Created an [11-lesson module](../interview/02-system-design/concepts/04-databases-and-data-modeling/partitioning-and-keys/README.md) under Databases & Data Modeling, covering the learner's range/hash/hotspot/consistent-hashing/sharding/key/CREATE TABLE/routing questions. Preserved the old entry URL, updated category counts and per-lesson learner progress, and provided PostgreSQL and CQL examples. Artifacts READY; comprehension TODO.

## 2) Concepts learned

The artifacts explain splitting versus placement/copies, range boundaries, hash remapping, hot versus oversized partitions, consistent-hash movement, SQL identity/index/partition roles, Cassandra key parentheses and clustering order, data-group creation, replica/coordinator routing, and query-specific Instagram keys.

## 3) Why this design

The supplied topics span several distinct mechanisms, so each has a focused prerequisite-ordered lesson. Two premises are corrected explicitly: consistent hashing does not split one unchanged hot key, and activity_type alone as a clustering key cannot preserve repeated events of that type per user/day. Real storage, toy tokens, paper failure models, and actual lab execution are labeled separately.

## 4) Implementation notes

No application behavior changed. [Verification](../interview/02-system-design/concepts/04-databases-and-data-modeling/partitioning-and-keys/verification.md) records semantic review, document/model checks, and an isolated PostgreSQL script exercising missing-child failure/recovery, date boundaries, tied event ordering, and rollback cleanup. Cassandra CQL was source-reviewed but not run: Docker was unavailable. No application integration/curl/browser flow changes require new backend tests or screenshots.

## 5) Code pointers

- [Post entity](../../src/main/java/com/instagram/backend/domain/Post.java): actual identity, author and createdAt fields; no Cassandra schema implied.
- [PostRepository](../../src/main/java/com/instagram/backend/repository/PostRepository.java): actual global chronological query, not a viewer-bucket feed.
- [PostService](../../src/main/java/com/instagram/backend/service/PostService.java): real feed and like methods, without a token/shard router.
- [PostgreSQL example](../interview/02-system-design/concepts/04-databases-and-data-modeling/partitioning-and-keys/examples/postgresql-partitions.sql): isolated transactional learning script.
- [Cassandra example](../interview/02-system-design/concepts/04-databases-and-data-modeling/partitioning-and-keys/examples/cassandra-activity.cql): proposed single-node experiment with summary-key overwrite comparison.

## 6) Commit pointers

The artifact commit is recorded in [commit map](../commit-map.md) after creation. Unrelated existing edits are excluded.

## 7) Pitfalls and fixes

Do not equate SQL children with Cassandra exact-key groups, a token with a partition, a coordinator with a replica, or balanced bytes with balanced request work. Toy 0–99 positions and invented capacities are explicit teaching assumptions, not production hashes/benchmarks. Actual Cassandra node routing remains a paper exercise until a cluster is run.

## 8) Next improvements

Execute the CQL example in a disposable Cassandra lab and verify actual token values/traces and failure behavior. Separately measure the existing relational query for F-016/SD-007 before proposing a read model; design F-021 ranking/pagination explicitly. Neither task is marked complete by these artifacts.
