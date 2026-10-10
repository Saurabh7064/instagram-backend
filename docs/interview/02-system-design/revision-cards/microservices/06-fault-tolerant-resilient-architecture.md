# Fault-Tolerant Microservices — Interview Revision Card

> **Question:** How would you design a fault-tolerant and resilient microservices architecture?
>
> [Detailed source](../../questions/microservices/06-fault-tolerant-resilient-architecture.md)

## Key requirements

- Keep critical journeys within stated SLO, RTO, and RPO.
- Contain component failures and ambiguous outcomes.
- Recover data and service through tested procedures.

## Scale assumptions

- Peak rate, failure domains, and data-loss tolerance determine redundancy; replica count alone does not.

## Components

- An **SLO** is a service target; **RTO** bounds recovery time; **RPO** bounds acceptable data loss.
- Multi-zone replicas, health-aware routing, bounded calls, durable state, backups, and observability.

## Request flow

1. Route to a ready stateless instance in another failure domain.
2. Apply deadline, concurrency limit, and idempotency at dependencies.
3. Commit authoritative state durably before acknowledging.
4. Move noncritical side effects to durable asynchronous work.
5. Degrade safely during failure; reconcile and restore afterwards.

## Data model

- `Operation(id, status, result, updatedAt)` plus replicated authoritative records and backup metadata.

## Three trade-offs

1. Redundancy improves availability but costs capacity and operations.
2. Stronger consistency reduces ambiguity but adds latency or reduces partition availability.
3. Multi-region operation improves disaster recovery but adds lag and conflict handling.

## Three failures and mitigations

1. **One instance dies:** health-aware routing and spare zone capacity.
2. **Dependency slows:** deadlines, bulkheads, circuit breaker, safe fallback.
3. **Bad delete replicates:** point-in-time backup and tested restore.

## Two-minute spoken answer

I begin with critical journeys and measurable SLO, RTO, and RPO, then map process, zone, database, broker, credential, and region failure domains. Stateless services run across zones behind readiness-aware routing with enough remaining capacity after one failure. Stateful systems use replication for availability and tested backups for corruption or deletion.

Every network call has a deadline and bounded concurrency. Retries are limited and writes are idempotent because timeout outcomes are ambiguous. Noncritical effects move to durable queues, while critical paths define explicit degraded behavior. I monitor user outcomes, dependency latency, saturation, queue age, replica lag, and business invariants. I regularly test instance loss, slow dependencies, duplicate messages, database failover, and restore. Each control adds cost or latency, so I choose it from failure impact rather than assembling a pattern checklist.

## Recall questions

1. Why is replication not a backup?
2. Which failure domain does another pod not solve?
3. How do RTO and RPO change the design?
