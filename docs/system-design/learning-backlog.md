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
| SD-003 | Token lifecycle (access + refresh) | TODO | Secure long-lived sessions | Access token issuance done in [Feature 02](../features/02-jwt-access-token-issuance.md); refresh pending `F-008` |
| SD-004 | API versioning and compatibility | TODO | Safe evolution of clients/services |  |
| SD-005 | Caching strategy (read paths) | TODO | Performance and latency gains | Concept artifact: [Caching and Consistency](../interview/02-system-design/concepts/02-caching-consistency.md); drill: [Cache Stampede](../interview/02-system-design/questions/production-scenarios/07-cache-stampede-on-hot-key.md) |
| SD-006 | Rate limiting and abuse protection | TODO | Reliability and security |  |
| SD-007 | Database indexing and query planning | TODO | Scalable data access | Diagnostic drill: [Database Connection-Pool Saturation](../interview/02-system-design/questions/production-scenarios/04-database-connection-pool-saturation.md) |
| SD-008 | Asynchronous processing and queues | TODO | Decoupling and throughput | Diagnostic drill: [Queue Backlog and Stale Results](../interview/02-system-design/questions/production-scenarios/06-queue-backlog-and-stale-results.md) |
| SD-009 | Observability (logs, metrics, traces) | IN_PROGRESS | Debugging and operations | Instrumentation baseline implemented in [Feature 12](../features/12-production-observability-baseline.md); use the [observability answer](../interview/02-system-design/questions/microservices/02-production-observability-and-debugging.md), [one-page card](../interview/02-system-design/revision-cards/microservices/02-production-observability-and-debugging.md), and [production scenarios](../interview/02-system-design/questions/production-scenarios/README.md); centralized backends and learner comprehension remain pending |
| SD-010 | Deployment topology and rollback strategy | TODO | Production safety | Ready practice artifact: [Post-Deployment Latency Spike](../interview/02-system-design/questions/production-scenarios/02-post-deployment-latency-spike.md); implementation and comprehension remain pending |

## Next 3 recommended

1. `SD-003` Token lifecycle (access + refresh)
2. `SD-004` API versioning and compatibility
3. `SD-006` Rate limiting and abuse protection
