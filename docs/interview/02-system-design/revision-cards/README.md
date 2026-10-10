# System Design Revision Cards

Use these cards in the final days before an interview. They compress a detailed lesson into a retrieval prompt; they do not replace the lesson when a concept is new.

## How to revise

1. Read only the question.
2. Give the two-minute answer aloud without looking below it.
3. Reconstruct the request flow and all three failures.
4. Answer the three recall questions without revealing notes.
5. Return to the detailed source only for a missed idea.

Every card is at most 400 words and keeps only the problem, requirements, scale, components, flow, data, trade-offs, failures, spoken answer, and unrevealed recall questions. Artifact creation does not mark the topic understood.

## Cards

### Existing detailed lessons

| Card | Detailed source | Artifact | Comprehension |
|---|---|---|---|
| [Decomposing a monolith](./microservices/01-decomposing-a-monolith.md) | [Full lesson](../questions/microservices/01-decomposing-a-monolith.md) | `READY` | `TODO` |
| [Production observability and debugging](./microservices/02-production-observability-and-debugging.md) | [Full lesson](../questions/microservices/02-production-observability-and-debugging.md) | `READY` | `TODO` |
| [Inter-service communication resilience](./microservices/03-inter-service-communication-resilience.md) | [Full lesson](../questions/microservices/03-inter-service-communication-resilience.md) | `READY` | `TODO` |
| [Distributed transactions and consistency](./microservices/04-distributed-transactions-and-data-consistency.md) | [Full lesson](../questions/microservices/04-distributed-transactions-and-data-consistency.md) | `READY` | `TODO` |
| [Bulkhead isolation](./microservices/05-bulkhead-isolation.md) | [Full lesson](../questions/microservices/05-bulkhead-isolation.md) | `READY` | `TODO` |
| [Fault-tolerant microservices](./microservices/06-fault-tolerant-resilient-architecture.md) | [Full lesson](../questions/microservices/06-fault-tolerant-resilient-architecture.md) | `READY` | `TODO` |
| [Service boundaries and data ownership](./microservices/07-service-boundaries-and-data-ownership.md) | [Full lesson](../questions/microservices/07-service-boundaries-and-data-ownership.md) | `READY` | `TODO` |
| [Idempotency and duplicate requests](./microservices/08-idempotency-retries-and-duplicate-requests.md) | [Full lesson](../questions/microservices/08-idempotency-retries-and-duplicate-requests.md) | `READY` | `TODO` |
| [API and event contract evolution](./microservices/09-api-event-contract-evolution.md) | [Full lesson](../questions/microservices/09-api-event-contract-evolution.md) | `READY` | `TODO` |
| [Testing and safe deployment](./microservices/10-testing-and-safe-deployment.md) | [Full lesson](../questions/microservices/10-testing-and-safe-deployment.md) | `READY` | `TODO` |

### Production scenarios

| Order | Revision card | Detailed answer |
|---:|---|---|
| 1 | [Peak-hour latency spike](./production-scenarios/01-peak-hour-latency-spike.md) | [Answer](../questions/production-scenarios/01-peak-hour-latency-spike.md) |
| 2 | [Post-deployment latency spike](./production-scenarios/02-post-deployment-latency-spike.md) | [Answer](../questions/production-scenarios/02-post-deployment-latency-spike.md) |
| 3 | [High p99 with a normal average](./production-scenarios/03-high-p99-with-normal-average.md) | [Answer](../questions/production-scenarios/03-high-p99-with-normal-average.md) |
| 4 | [Database connection-pool saturation](./production-scenarios/04-database-connection-pool-saturation.md) | [Answer](../questions/production-scenarios/04-database-connection-pool-saturation.md) |
| 5 | [Downstream timeout and retry storm](./production-scenarios/05-downstream-timeout-and-retry-storm.md) | [Answer](../questions/production-scenarios/05-downstream-timeout-and-retry-storm.md) |
| 6 | [Queue backlog and stale results](./production-scenarios/06-queue-backlog-and-stale-results.md) | [Answer](../questions/production-scenarios/06-queue-backlog-and-stale-results.md) |
| 7 | [Cache stampede on a hot key](./production-scenarios/07-cache-stampede-on-hot-key.md) | [Answer](../questions/production-scenarios/07-cache-stampede-on-hot-key.md) |
| 8 | [Single-region latency spike](./production-scenarios/08-single-region-latency-spike.md) | [Answer](../questions/production-scenarios/08-single-region-latency-spike.md) |

## Reusable format

Use the [revision-card template](./_template.md) for future conversions.
