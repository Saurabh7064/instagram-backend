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
| SD-003 | Token lifecycle (access + refresh) | TODO | Secure long-lived sessions | Pair with `F-006`/`F-008` |
| SD-004 | API versioning and compatibility | TODO | Safe evolution of clients/services |  |
| SD-005 | Caching strategy (read paths) | TODO | Performance and latency gains |  |
| SD-006 | Rate limiting and abuse protection | TODO | Reliability and security |  |
| SD-007 | Database indexing and query planning | TODO | Scalable data access |  |
| SD-008 | Asynchronous processing and queues | TODO | Decoupling and throughput |  |
| SD-009 | Observability (logs, metrics, traces) | TODO | Debugging and operations |  |
| SD-010 | Deployment topology and rollback strategy | TODO | Production safety |  |

## Next 3 recommended

1. `SD-002` Password storage and credential security
2. `SD-003` Token lifecycle (access + refresh)
3. `SD-006` Rate limiting and abuse protection
