# Decomposing a Monolith — Interview Revision Card

> **Question:** How would you decompose a legacy monolith incrementally while preserving functionality?
>
> [Detailed source](../../questions/microservices/01-decomposing-a-monolith.md)

## Key requirements

- Preserve current behavior and rollback capability.
- Extract around business ownership, not technical layers.
- Measure whether independence improves delivery or scaling.

## Scale assumptions

- Extract only where ownership or uneven load justifies distributed cost.

## Components

- A **strangler migration** routes selected behavior to a new component while the monolith remains live.
- Gateway/router, modular seam, anti-corruption adapter, service-owned data, and observability.

## Request flow

1. Map journeys, dependencies, data writes, and current baselines.
2. Modularize one low-risk business capability inside the monolith.
3. Keep a stable external contract and route a small cohort to the new service.
4. Compare outputs and business metrics; expand traffic gradually.
5. Move data ownership, reconcile, then remove the legacy path.

## Data model

- One service owns writes to each entity; cross-boundary copies carry source ID and version.

## Three trade-offs

1. Synchronous calls preserve freshness but couple latency and availability.
2. Events reduce runtime coupling but add lag, duplicates, and reconciliation.
3. Shared databases ease transition but preserve ownership coupling.

## Three failures and mitigations

1. **Missing legacy behavior:** shadow safe reads and compare results.
2. **Dual-write divergence:** use one owner plus outbox and reconciliation.
3. **Unsafe cutover:** canary behind reversible routing with clear thresholds.

## Two-minute spoken answer

I begin with business journeys and dependency/data maps, not with controllers or tables. I measure today’s latency, errors, throughput, and release pain, then choose one capability with clear ownership and manageable risk. I first create a clean module boundary inside the monolith. Behind a stable gateway contract, the new service translates legacy models through an adapter.

I shadow safe reads, compare results, and canary a small cohort with rollback thresholds. One component owns each write; changes reach other components through APIs or an outbox event, with idempotency and reconciliation. I expand traffic only when technical and business outcomes match baseline, then migrate data and remove the old path. This costs operational complexity and network failure modes, so I stop extracting when independent scaling, ownership, or release speed does not repay that cost.

## Recall questions

1. Why modularize before extracting?
2. What makes a service boundary useful?
3. How do you reverse a bad cutover?
