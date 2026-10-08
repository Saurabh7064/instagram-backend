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
| 5 | Load balancing and stateless services | `TODO` | `TODO` | Predict routing and instance failure behavior |
| 6 | Saga, compensation, and idempotency | `TODO` | `TODO` | Recover a partially completed checkout |
| 7 | Privacy and anonymized analytics | `TODO` | `TODO` | Separate identity, billing, and analytics data |
| 8 | Multi-region operation | `TODO` | `TODO` | State RTO/RPO and resolve one write conflict |

## Mock progress

| Date | Prompt | Score / 14 | Main miss | Repair lesson | Review date |
|---|---|---:|---|---|---|
|  |  |  |  |  |  |

## Microservices question progress

Use the detailed [Microservices Interview Questions](./questions/microservices/README.md) module. All ten answer artifacts are `READY`; comprehension remains `TODO` until the learner gives the answer, predicts a failure, and completes the final questions without reading.

| Question | Answer artifact | Project implementation | Comprehension |
|---|---|---|---|
| [Question 2: production observability and debugging](./questions/microservices/02-production-observability-and-debugging.md) | `READY` | `COMPLETE` — the in-process baseline is documented in [Feature 12](../../features/12-production-observability-baseline.md); centralized storage, dashboards, and alerting remain future work | `TODO` — implementation completion does not prove the learner can explain or diagnose it |

## Current lesson

- Active lesson: none selected yet.
- Recommended start: [Scalability, Availability, and Reliability](./concepts/01-scalability-availability-reliability.md).
- Rule: select one lesson, attempt its checkpoints aloud, and record the result before advancing.
