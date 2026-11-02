# System-design editorial review — 2026-11-01

## What changed

Revised 96 existing documents: 34 concept lessons plus one partitioning module entry, 20 API-design lessons, 10 microservices answers, eight production scenarios, and 23 short interview answers. The revision cards, print packs, future-topic roadmaps, runnable programs and lab files remain separate reference material.

The problem was the teaching voice and sequence. Repeated glossaries, compressed instruction lists and generic section slots made the learner reconstruct the missing reasoning. The revised explanations start with a specific situation, carry its state and consequences forward, and introduce technical names when the reader has a reason to need them. Concept lessons now open with a brief prerequisite/time note; readiness bookkeeping follows the explanation.

The entry points and author template now describe this approach. The preference is saved in [AGENTS.md](../../../AGENTS.md#writing-voice-for-system-design-lessons). Learning records are unchanged: editing an explanation is not evidence of comprehension.

## Semantic review, separate from structural checks

The author review checked the explanation's causal order, prerequisites, example derivation, guarantee boundaries, alternatives, recovery reasoning, project claims and final answers. All 35 concept/entry pages received a second read-only review; the 23 short answers also received a separate technical review. API lessons were checked against the real controller, service, entity and response definitions. Microservices and production answers retain detailed mechanisms while replacing opening glossaries and instruction lists with scenario-led explanations.

Specific reviewed cases:

- **CAP and quorums:** B's missing caption update precedes the formal promises. A failed operation does not satisfy CAP availability; quorum intersection alone is not claimed to establish linearizability.
- **Replication and recovery:** disconnected copies cannot promise a permanent freshness bound. Failover needs verified write authority and a known data gap, not only healthy application instances.
- **Hashing and Cassandra:** five-event grouping, hash remainders, ring ownership, clustering ties, replica response requirements and query fan-out arithmetic remain consistent. Consistent hashing does not split one unchanged hot key.
- **Retries and workflow:** post, key/fingerprint/result and outbox share the proposed commit boundary. A local transaction does not include separately uploaded media or an external effect. Failed uniqueness transactions finish rollback before a fresh recovery read.
- **Caching and sessions:** after-commit invalidation still has a racing-fill boundary; jitter across keys differs from coalescing one hot key. Refresh replay can indicate theft or legitimate retry/concurrency, and reissuing a JWT is not one-time rotation.
- **Pagination and migrations:** cursor examples derive both timestamp and ID comparisons. Mixed-version schema changes account for rows written during backfill and do not silently expose new private states to old readers.
- **Latency and overload:** the 94-fast/six-slow example yields 428 ms mean and 4,000 ms nearest-rank p95. The production p99 example uses a 2% slow group, with 218.8 ms mean, avoiding the 1% boundary ambiguity. The 200-in/100-out queue calculation declares its assumptions. Readiness cannot create capacity in a database shared by every instance.
- **Telemetry:** explicit service-level bucket boundaries are distinguished from generated histogram buckets, following [Micrometer's histogram documentation](https://docs.micrometer.io/micrometer/reference/concepts/histogram-quantiles.html). Percentiles describe populations; breakdowns and traces locate affected groups and time.

No new PostgreSQL, Cassandra, distributed-protocol, failure-injection or cloud lab execution is claimed. Existing laboratory evidence remains attached to its original scope.

## Document-by-document review index

Each row identifies the revised explanation and a reasoning question checked against its content. These review prompts are an index to the semantic read, not a claim that headings or question counts establish teaching quality. The learner must still attempt the actual checkpoints separately.

| Revised document | Reasoning checked |
|---|---|
| [01. Resources and URL design](api-design/01-resources-and-urls.md) | 1. Should the URL expose a table name or a client-visible resource? |
| [02. HTTP methods: GET, HEAD, POST, PUT, PATCH, DELETE, and OPTIONS](api-design/02-http-methods.md) | 1. Why is “POST creates, PUT updates, PATCH updates one field” incomplete? |
| [03. Status codes and response contracts](api-design/03-status-and-response-contracts.md) | 1. Why does a create return 201 rather than 202? |
| [04. Request validation and write boundaries](api-design/04-request-validation.md) | 1. Does @NotBlank prove an image URL belongs to the caller? |
| [05. Consistent error bodies](api-design/05-error-contracts.md) | 1. Should a client branch on the English error detail? |
| [06. Identity and ownership checks](api-design/06-identity-and-ownership.md) | 1. Why is a valid token insufficient for updating a post? |
| [07. Offset pagination and bounded reads](api-design/07-offset-pagination.md) | 1. For page=3 and size=20, how many rows are skipped? |
| [08. Cursor pagination for a live feed](api-design/08-cursor-pagination.md) | 1. Why must a cursor include an ID when timestamps tie? |
| [09. Filtering and sorting contracts](api-design/09-filtering-and-sorting.md) | 1. Why use an enum for sorting instead of arbitrary SQL text? |
| [10. Idempotency keys for create requests](api-design/10-idempotency-keys.md) | 1. Can a unique key record alone guarantee safe create replay? |
| [11. Conditional updates and lost writes](api-design/11-conditional-updates.md) | 1. What should happen when two clients submit If-Match "v7"? |
| [12. Caching and private representations](api-design/12-caching-and-privacy.md) | 1. Does Cache-Control: private mean never store the response? |
| [13. Compatibility, API versioning, and client migration](api-design/13-compatibility-and-versioning.md) | 1. Why can adding an enum value break an API that kept every existing field? |
| [14. Timeouts, rate limits, and asynchronous work](api-design/14-timeouts-and-async-work.md) | 1. When is it safe to return 202 for a job? |
| [15. Contract tests and observable API behavior](api-design/15-contract-tests-and-observability.md) | 1. What can a service unit test miss that an API integration test catches? |
| [16. Content negotiation and representation design](api-design/16-content-negotiation.md) | 1. Is Content-Type the same as Accept? |
| [17. Relationship resources, commands, and invariants](api-design/17-relationships-and-commands.md) | 1. Why is toggle unsafe to replay? |
| [18. API aggregation and Backend for Frontend](api-design/18-aggregation-and-bff.md) | 1. Does aggregation require microservices? |
| [19. Bulk operations and partial success](api-design/19-bulk-operations.md) | 1. Why is a 200 batch response insufficient to infer all items succeeded? |
| [20. Contract-first design and an API review rubric](api-design/20-contract-first-openapi.md) | 1. Does an OpenAPI bearerAuth scheme secure a Spring route? |
| [Micro-lesson 01 — Why distributed systems have uncertain outcomes](concepts/01-distributed-systems-fundamentals/01-partial-failure.md) | 1. Why is backend plus database already a distributed concern? |
| [Micro-lesson 02 — CAP: Consistency, Availability, Partition tolerance](concepts/01-distributed-systems-fundamentals/02-cap.md) | 1. What does each letter promise for post 42? |
| [Micro-lesson 03 — PACELC: why coordination costs time even without failure](concepts/01-distributed-systems-fundamentals/03-pacelc.md) | 1. Why do we need the “Else” branch when no node has failed? |
| [Micro-lesson 04 — Consistency models: what may a reader observe?](concepts/01-distributed-systems-fundamentals/04-consistency-models.md) | 1. Which exact step violates the strong contract, and why? |
| [Micro-lesson 05 — See your own writes and preserve dependencies](concepts/01-distributed-systems-fundamentals/05-session-and-causal-consistency.md) | 1. Must an unrelated reader see 42 immediately because Lea has read-your-writes? |
| [Micro-lesson 06 — Linearizability and serializability answer different questions](concepts/01-distributed-systems-fundamentals/06-linearizability-and-serializability.md) | 1. Why is [false,false] forbidden for the two committed transactions? |
| [Micro-lesson 07 — Quorums: why overlapping copies matter](concepts/01-distributed-systems-fundamentals/07-quorums.md) | 1. Why must {B,C} encounter this completed write? |
| [Micro-lesson 08 — Consensus: agreeing on one decision after failures](concepts/01-distributed-systems-fundamentals/08-consensus-and-election.md) | 1. Does choosing a leader prove consensus for the stored decisions? |
| [Micro-lesson 09 — Raft: turn agreed commands into one replicated history](concepts/01-distributed-systems-fundamentals/09-raft.md) | 1. What is the difference between appended, committed, and applied? |
| [Micro-lesson 10 — Paxos: a newer proposal must preserve an earlier decision](concepts/01-distributed-systems-fundamentals/10-paxos.md) | 1. Is X chosen only when every acceptor knows the result? |
| [Micro-lesson 11 — Distributed locks: why an expired owner can still write](concepts/01-distributed-systems-fundamentals/11-split-brain-and-locks.md) | 1. If the lock service worked correctly, why could a stale overwrite still happen? |
| [Micro-lesson 12 — Two-phase commit: one outcome across transaction participants](concepts/01-distributed-systems-fundamentals/12-distributed-transactions-and-2pc.md) | 1. Why not just commit A, then commit B? |
| [Micro-lesson 13 — Sagas: recover business steps that already committed](concepts/01-distributed-systems-fundamentals/13-sagas.md) | 1. Why does notification failure not delete post 42 here? |
| [Micro-lesson 14 — Safe retries: count business effects, not just deliveries](concepts/01-distributed-systems-fundamentals/14-idempotency-and-delivery.md) | 1. Why must a retry use K instead of generating another key? |
| [Micro-lesson 15 — Logical clocks: order dependencies without trusting wall time](concepts/01-distributed-systems-fundamentals/15-lamport-clocks.md) | 1. Why does B receive at 2 rather than 1? |
| [Micro-lesson 16 — Vector clocks: distinguish later history from concurrent edits](concepts/01-distributed-systems-fundamentals/16-vector-clocks.md) | 1. Why is [1,0] not earlier than [0,1], and vice versa? |
| [Micro-Lesson 05 — Load Balancing and Stateless Services](concepts/02-scalability-and-load-balancing/05-load-balancing-and-stateless-services.md) | 1. Why must a horizontally scaled application usually be stateless? |
| [Micro-Lesson 08 — Backpressure and Load Shedding](concepts/02-scalability-and-load-balancing/08-backpressure-and-load-shedding.md) | 1. When is a queue useful instead of harmful? |
| [Micro-Lesson 02 — Caching and Consistency](concepts/03-caching-and-content-delivery/02-caching-consistency.md) | 1. Why does adding a cache create a consistency problem? |
| [Partitioning, Replication, and Hot Keys — module entry](concepts/04-databases-and-data-modeling/03-partitioning-replication-hot-keys.md) | Why are the detailed lessons split into separate placement, table and routing decisions? |
| [Lesson 1 — A partition is a data boundary; a shard is a distribution boundary](concepts/04-databases-and-data-modeling/partitioning-and-keys/01-partitions-shards-and-replicas.md) | 1. Are two date partitions on one PostgreSQL server two shards? |
| [Lesson 2 — Range partitioning: give each interval an owner](concepts/04-databases-and-data-modeling/partitioning-and-keys/02-range-partitioning.md) | 1. Why is the October 26 row excluded from P25? |
| [Lesson 3 — Hash partitioning: spread different keys, preserve equal keys](concepts/04-databases-and-data-modeling/partitioning-and-keys/03-hash-partitioning.md) | 1. Why do all user 42 events choose one bucket? |
| [Lesson 4 — A hot key is a workload problem, not just uneven storage](concepts/04-databases-and-data-modeling/partitioning-and-keys/04-hotspots-and-hot-keys.md) | 1. Can a tiny partition still be hot? |
| [Lesson 5 — Consistent hashing reduces remapping when membership changes](concepts/04-databases-and-data-modeling/partitioning-and-keys/05-consistent-hashing.md) | 1. Does adding D change H(42,Oct26)=65? |
| [Lesson 6 — Choose a SQL partition key from operations, not just identity](concepts/04-databases-and-data-modeling/partitioning-and-keys/06-sql-partition-key-and-table-creation.md) | 1. Why is user_id not automatically the partition key just because the query filters it? |
| [Lesson 7 — Cassandra PRIMARY KEY: parentheses define grouping and row identity](concepts/04-databases-and-data-modeling/partitioning-and-keys/07-cassandra-primary-key-syntax.md) | 1. What changes when the inner parentheses are removed? |
| [Lesson 8 — Clustering columns order rows inside one Cassandra partition](concepts/04-databases-and-data-modeling/partitioning-and-keys/08-cassandra-clustering-columns.md) | 1. Why include activity_id if occurred_at already orders events? |
| [Lesson 9 — Cassandra CREATE TABLE defines the grouping rule, not one table per user](concepts/04-databases-and-data-modeling/partitioning-and-keys/09-cassandra-partition-creation.md) | 1. Does CREATE TABLE pre-create an empty partition for every user/date? |
| [Lesson 10 — Cassandra query routing: complete key to token to replica set](concepts/04-databases-and-data-modeling/partitioning-and-keys/10-cassandra-query-routing.md) | 1. Why does A not satisfy the missing second replica response? |
| [Lesson 11 — Instagram has query-specific partition keys, not one universal key](concepts/04-databases-and-data-modeling/partitioning-and-keys/11-instagram-key-design.md) | 1. Why is user_id alone not automatically the best key? |
| [Micro-Lesson 04 — Monolith-to-Microservices Migration](concepts/07-microservices-and-api-design/04-monolith-to-microservices-migration.md) | 1. How would you decompose a tightly coupled Java monolith? |
| [Micro-Lesson 01 — Scalability, Availability, and Reliability](concepts/09-reliability-resilience-and-fault-tolerance/01-scalability-availability-reliability.md) | 1. Does horizontal scaling automatically make a system highly available? |
| [Micro-Lesson 07 — Tail Latency, Percentiles, and Queueing](concepts/10-observability-and-production-monitoring/07-tail-latency-percentiles-and-queueing.md) | 1. Why can average latency stay acceptable while users report a severe slowdown? |
| [Micro-Lesson 06 — Latency, Throughput, and Saturation](concepts/15-advanced-architecture-and-performance/06-latency-throughput-and-saturation.md) | 1. Why can latency rise sharply while throughput stays flat? |
| [Decomposing a Monolith](questions/microservices/01-decomposing-a-monolith.md) | 1. Why should a team modularize the monolith before extracting a service? |
| [Monitoring and Debugging Microservices in Production](questions/microservices/02-production-observability-and-debugging.md) | 1. What is the difference between metrics, logs, and traces? |
| [Inter-Service Communication: Latency, Timeouts, and Cascading Failures](questions/microservices/03-inter-service-communication-resilience.md) | 1. Why is a timeout not proof that an operation failed? |
| [How are distributed transactions handled in microservices, and which patterns ensure data consistency?](questions/microservices/04-distributed-transactions-and-data-consistency.md) | 1. Why are an outbox and an idempotent consumer both necessary? |
| [Explain bulkhead isolation](questions/microservices/05-bulkhead-isolation.md) | 1. How is a bulkhead different from a circuit breaker? |
| [Design a fault-tolerant and resilient microservices architecture](questions/microservices/06-fault-tolerant-resilient-architecture.md) | 1. Does running three instances make a service fault tolerant? |
| [07 — Choosing Service Boundaries and Data Ownership](questions/microservices/07-service-boundaries-and-data-ownership.md) | 1. Why is one service per database table usually a weak boundary? |
| [08 — Idempotency, Retries, and Duplicate Requests](questions/microservices/08-idempotency-retries-and-duplicate-requests.md) | 1. Why is checking for an idempotency key and then inserting not enough? |
| [09 — Evolving API and Event Contracts Safely](questions/microservices/09-api-event-contract-evolution.md) | 1. Is adding a response field always backward-compatible? |
| [10 — Testing and Deploying Microservices Safely](questions/microservices/10-testing-and-safe-deployment.md) | 1. Why not test every scenario end to end? |
| [Peak-Hour Latency Spike](questions/production-scenarios/01-peak-hour-latency-spike.md) | Why is application CPU below 50% not proof that the service has spare capacity? |
| [Post-Deployment Latency Spike](questions/production-scenarios/02-post-deployment-latency-spike.md) | Why does a twenty-minute delay not rule out the deployment? |
| [High p99 Latency with a Normal Average](questions/production-scenarios/03-high-p99-with-normal-average.md) | How can 200 eight-second requests coexist with an average near 220 ms? |
| [Database Connection-Pool Saturation](questions/production-scenarios/04-database-connection-pool-saturation.md) | Why can CPU stay at 40% while requests time out? |
| [Downstream Timeout and Retry Storm](questions/production-scenarios/05-downstream-timeout-and-retry-storm.md) | Why can a longer timeout make availability worse? |
| [Queue Backlog and Stale Results](questions/production-scenarios/06-queue-backlog-and-stale-results.md) | When will adding more consumers fail to reduce lag? |
| [Cache Stampede on a Hot Key](questions/production-scenarios/07-cache-stampede-on-hot-key.md) | Why does TTL jitter reduce stampede risk? |
| [Single-Region Latency Spike](questions/production-scenarios/08-single-region-latency-spike.md) | What must be checked before shifting regional traffic? |
| [Requirements and Capacity Estimation](questions/short-questions-and-answers/01-requirements-and-capacity-estimation.md) | Why is daily active users not enough to size the feed? |
| [Horizontal Scaling and Load Balancing](questions/short-questions-and-answers/02-horizontal-scaling-and-load-balancing.md) | Why can adding application replicas make database latency worse? |
| [Choosing SQL or NoSQL](questions/short-questions-and-answers/03-choosing-sql-or-nosql.md) | Why is “PostgreSQL is slow” not enough reason to choose NoSQL? |
| [Indexing, Query Design, and Read Replicas](questions/short-questions-and-answers/04-indexing-query-design-and-read-replicas.md) | Why should query shape be fixed before adding replicas? |
| [Sharding and Hot Partitions](questions/short-questions-and-answers/05-sharding-and-hot-partitions.md) | Why does hashing by post ID not solve a celebrity-post hotspot? |
| [Replication, CAP, and Consistency](questions/short-questions-and-answers/06-replication-cap-and-consistency.md) | Does CAP force a permanent choice between consistency and availability? |
| [Cache Design and Invalidation](questions/short-questions-and-answers/07-cache-design-and-invalidation.md) | Why not store `likedByViewer` in a shared post cache entry? |
| [Media Storage, Processing, and CDN Delivery](questions/short-questions-and-answers/08-media-storage-processing-and-cdn.md) | Why not keep uploads on each Spring Boot instance’s local disk? |
| [Cursor Pagination for Changing Feeds](questions/short-questions-and-answers/09-cursor-pagination-for-changing-feeds.md) | Why is `createdAt` alone unsafe? |
| [Feed Fan-Out Strategy](questions/short-questions-and-answers/10-feed-fanout-strategy.md) | Why store post IDs instead of full post objects in every feed? |
| [Synchronous Calls, Queues, and Pub/Sub](questions/short-questions-and-answers/11-synchronous-queues-and-pubsub.md) | 1. Why not publish after committing with an ordinary broker call? |
| [Correct Multi-Step Workflows](questions/short-questions-and-answers/12-correct-multi-step-workflows.md) | Why must a retry reuse the same idempotency key? |
| [Distributed Rate Limiting](questions/short-questions-and-answers/13-distributed-rate-limiting.md) | Why is IP address alone an unfair rate limit key? |
| [Token Lifecycle and Revocation](questions/short-questions-and-answers/14-token-lifecycle-and-revocation.md) | Why store a hash of the refresh token instead of its plaintext value? |
| [Search and Near-Real-Time Indexing](questions/short-questions-and-answers/15-search-and-near-real-time-indexing.md) | Why should the search index not be the source of truth? |
| [Feed Ranking and Cold Start](questions/short-questions-and-answers/16-feed-ranking-and-cold-start.md) | Why should privacy and blocking filters run before ranking? |
| [Real-Time Notifications](questions/short-questions-and-answers/17-real-time-notifications.md) | Why is a WebSocket connection not enough to guarantee notification delivery? |
| [SLOs and Observability](questions/short-questions-and-answers/18-slos-and-observability.md) | Why is HTTP success rate insufficient for a feed SLO? |
| [Multi-Region Disaster Recovery](questions/short-questions-and-answers/19-multi-region-disaster-recovery.md) | Why must RTO and RPO be chosen before the topology? |
| [Zero-Downtime Schema Evolution](questions/short-questions-and-answers/20-zero-downtime-schema-evolution.md) | Why not add the new column, backfill it, enforce `NOT NULL`, and deploy code in one step? |
| [Concurrent Writes and Database Constraints](questions/short-questions-and-answers/21-concurrent-writes-and-database-constraints.md) | Why can two existence checks both return false? |
| [Idempotency Keys and Ambiguous Timeouts](questions/short-questions-and-answers/22-idempotency-keys-and-ambiguous-timeouts.md) | Does an HTTP timeout mean the post was not committed? |
| [Deadline Budgets and Bounded Retries](questions/short-questions-and-answers/23-deadline-budgets-and-bounded-retries.md) | Why is an 800 ms timeout at every hop insufficient? |

## Verification evidence

- Source checks: all 96 selected documents changed, 347 final explained checkpoints, 504 local links, balanced fences/disclosures and no content after the final question section. Inbound links from other documentation into changed headings also resolve.
- Browser checks use an isolated temporary copy of the existing Learning Studio with a freshly generated catalog, leaving the UI repository unchanged. The browser run checks all 96 documents, answer disclosures and numbered Markdown code blocks, plus desktop and phone screenshots. See [browser results](../../demo-proofs/2026-11-01/system-design-editorial-browser.json).
- Screenshot review checks the actual rendered scenario text and readable layout, including [CAP](../../demo-proofs/2026-11-01/system-design-cap-human-explanation.png), [API design](../../demo-proofs/2026-11-01/system-design-api-human-explanation.png), [scaling](../../demo-proofs/2026-11-01/system-design-scaling-human-explanation.png), [diagnosis](../../demo-proofs/2026-11-01/system-design-diagnosis-human-explanation.png) and [phone layout](../../demo-proofs/2026-11-01/system-design-human-explanation-mobile.png).
- Exact HTTP/source/browser outputs are recorded in [verification evidence](../../demo-proofs/2026-11-01/system-design-editorial-verification.txt).

This is a documentation change. Backend/API contracts and UI implementation do not change, so backend integration tests and API mutation calls are not applicable. The browser checks verify publication through the existing reader; they do not verify the proposed distributed designs.
