# System Design Learning Backlog (Concept Track)

Use this file to track architecture/system design concepts to learn with this project.

## Status legend

- `TODO` not learned yet
- `IN_PROGRESS` currently learning/applying
- `DONE` learned + mapped to code/doc

## Concept list

| ID | Concept | Status | Why it matters | Notes |
|---|---|---|---|---|
| SD-001 | Stateless auth + horizontal scaling | DONE | Enables multi-instance backend behavior | [Concept 01](./01-stateless-auth-and-horizontal-scaling.md) |
| SD-002 | Password storage and credential security | DONE | Prevent credential compromise | [Concept 02](./02-password-storage-and-credential-security.md), Pair with `F-005` |
| SD-003 | Token lifecycle (access + refresh) | TODO | Secure long-lived sessions | Access token issuance done in [Feature 02](../features/02-jwt-access-token-issuance.md); refresh pending `F-008`; practice with [Token Lifecycle and Revocation](../interview/02-system-design/questions/short-questions-and-answers/14-token-lifecycle-and-revocation.md) |
| SD-004 | API versioning and compatibility | TODO | Safe evolution of clients/services | Practice backward-compatible change sequencing with [Zero-Downtime Schema Evolution](../interview/02-system-design/questions/short-questions-and-answers/20-zero-downtime-schema-evolution.md) |
| SD-005 | Caching strategy (read paths) | TODO | Performance and latency gains | Concept artifact: [Caching and Consistency](../interview/02-system-design/concepts/03-caching-and-content-delivery/02-caching-consistency.md); drills: [Cache Design and Invalidation](../interview/02-system-design/questions/short-questions-and-answers/07-cache-design-and-invalidation.md) and [Cache Stampede](../interview/02-system-design/questions/production-scenarios/07-cache-stampede-on-hot-key.md) |
| SD-006 | Rate limiting and abuse protection | TODO | Reliability and security | Practice with [Distributed Rate Limiting](../interview/02-system-design/questions/short-questions-and-answers/13-distributed-rate-limiting.md) |
| SD-007 | Database indexing and query planning | TODO | Scalable data access | Practice with [Indexing, Query Design, and Read Replicas](../interview/02-system-design/questions/short-questions-and-answers/04-indexing-query-design-and-read-replicas.md) and [Database Connection-Pool Saturation](../interview/02-system-design/questions/production-scenarios/04-database-connection-pool-saturation.md) |
| SD-008 | Asynchronous processing and queues | TODO | Decoupling and throughput | Practice with [Synchronous Work, Queues, and Pub/Sub](../interview/02-system-design/questions/short-questions-and-answers/11-synchronous-queues-and-pubsub.md) and [Queue Backlog and Stale Results](../interview/02-system-design/questions/production-scenarios/06-queue-backlog-and-stale-results.md) |
| SD-009 | Observability (logs, metrics, traces) | IN_PROGRESS | Debugging and operations | Instrumentation baseline implemented in [Feature 12](../features/12-production-observability-baseline.md); use the [observability answer](../interview/02-system-design/questions/microservices/02-production-observability-and-debugging.md), [SLO drill](../interview/02-system-design/questions/short-questions-and-answers/18-slos-and-observability.md), [one-page card](../interview/02-system-design/revision-cards/microservices/02-production-observability-and-debugging.md), and [production scenarios](../interview/02-system-design/questions/production-scenarios/README.md); centralized backends and learner comprehension remain pending |
| SD-010 | Deployment topology and rollback strategy | TODO | Production safety | Ready practice artifacts: [Post-Deployment Latency Spike](../interview/02-system-design/questions/production-scenarios/02-post-deployment-latency-spike.md) and [Zero-Downtime Schema Evolution](../interview/02-system-design/questions/short-questions-and-answers/20-zero-downtime-schema-evolution.md); implementation and comprehension remain pending |
| SD-011 | State-driven learning simulation architecture | IN_PROGRESS | Separates trusted lesson input, deterministic state, and reusable presentation | Implemented in [Feature 13](../features/13-interactive-learning-studio.md), expanded with algorithm-derived adapters in [Feature 14](../features/14-interactive-sliding-window-curriculum.md), and given reusable numbered-code presentation in [Feature 15](../features/15-numbered-reference-solutions.md); learner comprehension remains pending |

## Next 3 recommended

1. `SD-003` Token lifecycle (access + refresh)
2. `SD-004` API versioning and compatibility
3. `SD-006` Rate limiting and abuse protection

## Interview artifact update — 2026-10-17

Added ready interview answers on concurrent likes, idempotency keys, and deadline budgets in the [question index](../interview/02-system-design/questions/short-questions-and-answers/README.md). This documentation update does not complete an implementation milestone or demonstrate learner comprehension; existing statuses remain unchanged.

## API design lesson artifacts — 2026-10-17

Created [15 API Design with Spring Boot lessons](../interview/02-system-design/api-design/README.md), with [separate learner evidence](../interview/02-system-design/api-design/learner-progress.md). Artifacts are READY; comprehension remains TODO. Proposed exercises map to F-010/F-014/F-016/F-017/F-022/F-030 and SD-004 compatibility. No feature implementation or concept mastery status changes are claimed.
