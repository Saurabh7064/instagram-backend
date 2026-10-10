# Single-Region Latency Spike

## Question

Global latency looks healthy, but users in one region see slow and intermittent responses. Other regions remain normal.

Your manager asks:

**“How would you isolate the regional fault, and when would you shift traffic?”**

> [One-page revision card](../../revision-cards/production-scenarios/08-single-region-latency-spike.md)

## Why this problem exists

Regional infrastructure reduces user latency and limits some failures, but each region adds its own compute, network paths, dependencies, capacity, and data freshness. A regional symptom can come from one application version, availability zone, database replica, cache, DNS route, certificate, provider, or accidental cross-region call.

Moving users is not automatically safe. The destination must absorb the traffic, required data must exist there, and write ownership must prevent lost or conflicting operations.

## Analogy: diverting flights to another airport

When one airport has severe weather, traffic can be diverted to a nearby airport only if it has runway capacity, ground staff, and the passengers’ onward connections. Redirecting everything at once can close the second airport too.

The analogy stops because replicated data can lag and two software regions can accept conflicting writes. Passenger records do not normally merge themselves after both airports change them.

## Terms to establish

- **Traffic shifting** changes which region receives new requests.
- **Replication lag** is the delay before a committed write becomes visible in another region.
- **RTO (recovery time objective)** is the target maximum time to restore a service after failure.
- A **write owner** is the region or partition allowed to serialize a particular write.

## Isolation and decision sequence

### 1. Prove the regional boundary

Compare the same route, status, version, traffic class, and percentile across regions. Split the affected region by availability zone, instance, and deployment cohort. Check whether client latency and server latency agree; a gap can indicate edge, DNS, or network transit rather than handler work.

### 2. Trace the first regional difference

Compare a healthy and slow trace. Locate time in application work, regional database/cache, a shared global service, or a cross-region dependency. Align it with packet loss, connection errors, certificate changes, cache misses, replica lag, saturation, and recent regional changes.

### 3. Verify failover safety

Before moving traffic, test destination headroom at diverted peak, quotas, credentials, configuration, and dependency capacity. Decide whether requests can use replicated data. Public feed reads may tolerate bounded staleness; authorization, payment, and ordered writes may need the write owner.

### 4. Shift gradually and observe both sides

Drain a small cohort, then increase traffic while watching latency, errors, saturation, business outcomes, and data correctness in source and destination. Use stop thresholds. Keep write routing explicit, and preserve request idempotency because clients may retry across regions.

## Concrete failure walkthrough

1. `GET /api/feed` p99 rises to four seconds only in region A.
2. Application spans in A are normal, but a profile call crosses to region B and consumes 3.5 seconds.
3. A routing change made the local profile endpoint unavailable, triggering cross-region fallback.
4. The team restores the regional endpoint and shifts 10% of A’s read traffic while validating capacity.
5. Writes remain with their existing owner; read responses expose the accepted freshness window.
6. A’s latency recovers, the destination remains below saturation, and reconciliation finds no conflicting writes.

The observation that matters is the first divergent network span, not the broad fact that region A is slow.

## Instagram-backend mapping

This repository is a single local Spring Boot deployment, not a multi-region system. [application.properties](../../../../../src/main/resources/application.properties) attaches application/environment/version tags and configures health and readiness, but it has no region tag, global router, replica-freshness signal, or failover controller.

[MediaStorageService.java](../../../../../src/main/java/com/instagram/backend/service/MediaStorageService.java) stores uploads on the instance filesystem. That is a concrete blocker even for multi-instance operation: shifting traffic could make existing media unavailable. PostgreSQL configuration is also a single endpoint.

**Proposed, not implemented:** shared object storage, regional service and dependency metrics, a tested global routing policy, replicated read data with a stated freshness bound, one write owner per record or partition, idempotency across retries, and reconciliation after failover.

## Choices and trade-offs

- **Shift reads to a healthy region** when it has capacity and bounded stale data is acceptable. Avoid it when the destination lacks required data; cost includes higher network latency and load.
- **Active-passive writes** when simpler ownership and conflict avoidance matter. It has a longer failover path and requires tested promotion.
- **Active-active writes** only when the product needs regional write availability and conflict semantics are designed. It reduces failover time but adds routing, consistency, and reconciliation failures.

## Failure drill

**Prediction:** all of region A is shifted instantly and overloads region B. **Symptom and detection:** A improves while B’s worker, connection, and database saturation rise; global latency and errors then worsen. **Containment:** stop or reverse the shift, shed optional reads, and preserve critical capacity. **Recovery:** drain destination queues and resume only a proven percentage. **Prevention:** reserve failover headroom, load-test regional evacuation, use staged routing with automatic stop thresholds, and include downstream quotas.

## Two-to-three-minute interview answer

I first prove the fault boundary by comparing the same route, version, percentile, and traffic class across regions, then split the affected region by zone and instance. I compare client and server timing and follow a slow trace against a healthy trace to find the first difference: regional storage, a shared dependency, DNS or network transit, or an accidental cross-region call.

Before shifting traffic, I verify the destination’s compute and dependency capacity, quotas, credentials, configuration, and data freshness. Reads may tolerate replication lag; authorization or ordered writes may require the existing write owner. I shift a small cohort first and watch latency, errors, saturation, business outcomes, and correctness in both regions. I stop automatically if the destination approaches its safe limit. Writes remain single-owner unless active-active conflict handling already exists, and retried operations need idempotency. Recovery requires affected-user latency to improve, destination capacity to remain safe, and reconciliation to show no missing or conflicting operations.

## Practice status

The artifact is `READY`; comprehension remains `TODO`. Mark it practiced only after identifying a safe traffic-shift gate, explaining replication lag, and answering the final questions before opening them.

## Questions and explained answers

<details>
<summary>What must be checked before shifting regional traffic?</summary>

Verify destination capacity, downstream quotas, credentials, compatible configuration, required data and freshness, write ownership, and stop thresholds. Otherwise failover can move or enlarge the outage.

</details>

<details>
<summary>Why can read failover be safer than write failover?</summary>

Some reads can tolerate a documented replica delay. Competing writes can create lost updates, ordering violations, or conflicts unless ownership and resolution rules already exist.

</details>

<details>
<summary>What proves the regional mitigation succeeded?</summary>

The original user latency and errors recover, the destination stays within tested saturation, business outcomes remain correct, and reconciliation finds no lost or conflicting data.

</details>
