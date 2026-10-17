# Short System Design Questions and Answers

These 20 manager-style prompts cover the main decisions expected in a senior backend system-design discussion. Each answer is deliberately shorter than a full design lesson while still explaining the problem, request or failure flow, project mapping, trade-offs, and recovery.

The two sample prompts about a [peak-hour latency spike](../production-scenarios/01-peak-hour-latency-spike.md) and a [post-deployment latency spike](../production-scenarios/02-post-deployment-latency-spike.md) already have detailed answers in the production-scenarios module. This set adds breadth without duplicating those two drills.

## Index

| # | Interview question | Primary topics | Answer | Comprehension |
|---:|---|---|---|---|
| 1 | How would you estimate capacity and define requirements before designing an Instagram-like feed? | Requirements, scale, headroom | [Answer](./01-requirements-and-capacity-estimation.md) | `TODO` |
| 2 | How would you scale a stateless Spring Boot API horizontally and route traffic safely? | Load balancing, statelessness, autoscaling | [Answer](./02-horizontal-scaling-and-load-balancing.md) | `TODO` |
| 3 | How do you choose SQL, NoSQL, or both for posts, likes, and feeds? | Access patterns, invariants, polyglot storage | [Answer](./03-choosing-sql-or-nosql.md) | `TODO` |
| 4 | How would you reduce database read latency with query design, indexes, and replicas? | Query plans, indexes, replica lag | [Answer](./04-indexing-query-design-and-read-replicas.md) | `TODO` |
| 5 | How would you shard data and prevent celebrity accounts from creating hot partitions? | Partitioning, hot keys, rebalancing | [Answer](./05-sharding-and-hot-partitions.md) | `TODO` |
| 6 | Which operations need strong consistency, and where is eventual consistency acceptable? | Replication, CAP, conflict handling | [Answer](./06-replication-cap-and-consistency.md) | `TODO` |
| 7 | How would you cache hot reads without serving dangerous stale data? | Cache-aside, invalidation, stampede control | [Answer](./07-cache-design-and-invalidation.md) | `TODO` |
| 8 | How would you design image upload, processing, storage, and global delivery? | Object storage, processing, CDN | [Answer](./08-media-storage-processing-and-cdn.md) | `TODO` |
| 9 | Why use cursor pagination for a feed that changes while the user scrolls? | Stable ordering, cursors, indexes | [Answer](./09-cursor-pagination-for-changing-feeds.md) | `TODO` |
| 10 | How would you generate feeds for normal users and celebrity accounts? | Fan-out, materialized timelines, hybrid reads | [Answer](./10-feed-fanout-strategy.md) | `TODO` |
| 11 | When should work be synchronous, queued, or published to multiple consumers? | Queues, pub/sub, backpressure | [Answer](./11-synchronous-queues-and-pubsub.md) | `TODO` |
| 12 | How do you keep a multi-step post-publish workflow correct when steps fail or repeat? | Idempotency, outbox, Saga, reconciliation | [Answer](./12-correct-multi-step-workflows.md) | `TODO` |
| 13 | How would you rate-limit login, uploads, and viral feed traffic fairly? | Token bucket, distributed limits, load shedding | [Answer](./13-distributed-rate-limiting.md) | `TODO` |
| 14 | How would you design access tokens, refresh tokens, logout, and revocation at scale? | Token lifecycle, rotation, reuse detection | [Answer](./14-token-lifecycle-and-revocation.md) | `TODO` |
| 15 | How would you build caption, hashtag, and username search with near-real-time updates? | Inverted index, async indexing, tombstones | [Answer](./15-search-and-near-real-time-indexing.md) | `TODO` |
| 16 | How would you rank a personalized feed, and how would you handle cold-start users? | Candidate generation, ranking, fallback | [Answer](./16-feed-ranking-and-cold-start.md) | `TODO` |
| 17 | How would you deliver real-time notifications without losing offline events or duplicating them? | Durable inbox, live delivery, deduplication | [Answer](./17-real-time-notifications.md) | `TODO` |
| 18 | How would you define SLOs and observability for the main user journeys? | SLIs, error budgets, logs/metrics/traces | [Answer](./18-slos-and-observability.md) | `TODO` |
| 19 | How would you survive a region failure, and how would you choose RTO and RPO? | Disaster recovery, fencing, failover | [Answer](./19-multi-region-disaster-recovery.md) | `TODO` |
| 20 | How would you change a large database schema without downtime? | Expand–migrate–contract, backfills, compatibility | [Answer](./20-zero-downtime-schema-evolution.md) | `TODO` |

## Practice rule

Practice one row at a time. Read only the question, answer aloud for two to three minutes, then compare your response with the file. Mark comprehension only after you can trace the concrete flow, predict the failure, choose a mitigation, and explain one trade-off before opening the final answers.

The files being `READY` does not mean the topics are understood; learner evidence remains separate in [System Design Learner Progress](../../learner-progress.md).

## Printable morning pack

For dense A4 revision, use the [System Design Morning Print Pack](./toprint/README.md). It contains 132 compact questions and answers, a separate 380-concept learning tracker, printable PDFs, and editable source files. This print pack intentionally does not use the full lesson format.
