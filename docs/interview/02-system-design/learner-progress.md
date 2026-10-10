# System Design Learner Progress

Artifact completion and learner understanding are separate. A lesson can be ready while comprehension remains unproven.

## Status definitions

- `TODO`: lesson artifact or learner attempt has not started.
- `READY`: lesson artifact exists; learner has not yet demonstrated it.
- `LEARNING`: learner is currently attempting this one lesson.
- `PRACTICING`: initial teach-back passed; spaced retrieval remains.
- `DEMONSTRATED`: learner gave a correct explanation, prediction, and failure diagnosis without notes.
- `RETAINED`: demonstrated again after at least one week.

## Concept progress

| Order | Concept | Artifact | Comprehension | Required evidence |
|---:|---|---|---|---|
| 1 | [Scalability, availability, reliability](./concepts/01-scalability-availability-reliability.md) | `READY` | `TODO` | Explain differences and diagnose one failure |
| 2 | [Caching and consistency](./concepts/02-caching-consistency.md) | `READY` | `TODO` | Trace hit/miss/update and stale-read trade-off |
| 3 | [Partitioning, replication, hot keys](./concepts/03-partitioning-replication-hot-keys.md) | `READY` | `TODO` | Route a query and repair a hot-key design |
| 4 | [Monolith-to-microservices migration](./concepts/04-monolith-to-microservices-migration.md) | `READY` | `TODO` | Choose first extraction, coexistence, and rollback |
| 5 | [Load balancing and stateless services](./concepts/05-load-balancing-and-stateless-services.md) | `READY` | `TODO` | Predict routing and instance failure behavior |
| 6 | [Latency, throughput, and saturation](./concepts/06-latency-throughput-and-saturation.md) | `READY` | `TODO` | Locate a constrained resource from rate, latency, and queue evidence |
| 7 | [Tail latency, percentiles, and queueing](./concepts/07-tail-latency-percentiles-and-queueing.md) | `READY` | `TODO` | Explain a high percentile hidden by an average and locate waiting time |
| 8 | [Backpressure and load shedding](./concepts/08-backpressure-and-load-shedding.md) | `READY` | `TODO` | Bound overload and choose which work degrades first |
| 9 | Saga, compensation, and idempotency | `TODO` | `TODO` | Recover a partially completed checkout |
| 10 | Privacy and anonymized analytics | `TODO` | `TODO` | Separate identity, billing, and analytics data |
| 11 | Multi-region operation | `TODO` | `TODO` | State RTO/RPO and resolve one write conflict |

## Mock progress

| Date | Prompt | Score / 14 | Main miss | Repair lesson | Review date |
|---|---|---:|---|---|---|
|  |  |  |  |  |  |

## Microservices question progress

Use the detailed [Microservices Interview Questions](./questions/microservices/README.md) module. All ten answer artifacts are `READY`; comprehension remains `TODO` until the learner gives the answer, predicts a failure, and completes the final questions without reading.

| Question | Answer artifact | Project implementation | Comprehension |
|---|---|---|---|
| [Question 2: production observability and debugging](./questions/microservices/02-production-observability-and-debugging.md) | `READY` | `COMPLETE` — the in-process baseline is documented in [Feature 12](../../features/12-production-observability-baseline.md); centralized storage, dashboards, and alerting remain future work | `TODO` — implementation completion does not prove the learner can explain or diagnose it |

## Production scenario progress

All detailed answers and [one-page revision cards](./revision-cards/README.md) are ready. Comprehension remains `TODO` until the learner answers without notes, predicts the first failure signal, chooses a mitigation, and explains one trade-off.

| Scenario | Answer | Card | Comprehension |
|---|---|---|---|
| Peak-hour latency spike | [Detailed](./questions/production-scenarios/01-peak-hour-latency-spike.md) | [≤400 words](./revision-cards/production-scenarios/01-peak-hour-latency-spike.md) | `TODO` |
| Post-deployment latency spike | [Detailed](./questions/production-scenarios/02-post-deployment-latency-spike.md) | [≤400 words](./revision-cards/production-scenarios/02-post-deployment-latency-spike.md) | `TODO` |
| High p99 with normal average | [Detailed](./questions/production-scenarios/03-high-p99-with-normal-average.md) | [≤400 words](./revision-cards/production-scenarios/03-high-p99-with-normal-average.md) | `TODO` |
| Database pool saturation | [Detailed](./questions/production-scenarios/04-database-connection-pool-saturation.md) | [≤400 words](./revision-cards/production-scenarios/04-database-connection-pool-saturation.md) | `TODO` |
| Retry storm | [Detailed](./questions/production-scenarios/05-downstream-timeout-and-retry-storm.md) | [≤400 words](./revision-cards/production-scenarios/05-downstream-timeout-and-retry-storm.md) | `TODO` |
| Queue backlog | [Detailed](./questions/production-scenarios/06-queue-backlog-and-stale-results.md) | [≤400 words](./revision-cards/production-scenarios/06-queue-backlog-and-stale-results.md) | `TODO` |
| Cache stampede | [Detailed](./questions/production-scenarios/07-cache-stampede-on-hot-key.md) | [≤400 words](./revision-cards/production-scenarios/07-cache-stampede-on-hot-key.md) | `TODO` |
| Single-region latency | [Detailed](./questions/production-scenarios/08-single-region-latency-spike.md) | [≤400 words](./revision-cards/production-scenarios/08-single-region-latency-spike.md) | `TODO` |

## Current lesson

- Active lesson: none selected yet.
- Recommended start: [Scalability, Availability, and Reliability](./concepts/01-scalability-availability-reliability.md).
- Rule: select one lesson, attempt its checkpoints aloud, and record the result before advancing.
