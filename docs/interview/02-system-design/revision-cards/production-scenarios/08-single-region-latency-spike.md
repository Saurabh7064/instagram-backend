# Single-Region Latency Spike — Interview Revision Card

> **Question:** Global latency is healthy, but one region becomes slow and intermittent. How would you isolate the fault and decide whether to shift traffic?
>
> [Detailed answer](../../questions/production-scenarios/08-single-region-latency-spike.md)

## Key requirements

- Protect users in the affected region.
- Distinguish compute, network, dependency, and data-layer failures.
- Shift traffic without violating write correctness or capacity.

## Scale assumptions

- A healthy destination region must absorb diverted peak traffic, and replicated data may lag behind the write owner.

## Components

- **Traffic shifting** changes serving region; **replication lag** delays remote writes; **RTO** is target recovery time.
- Global router, regional services/dependencies, replicas, and regional telemetry.

## Request flow

1. Compare one route and version across regions.
2. Trace the regional hop, dependency, or cross-region call.
3. Verify destination capacity, freshness, and write ownership.
4. Shift reads gradually; preserve safe write routing.
5. Confirm latency, correctness, and destination saturation.

## Data model

- `RegionalSignal(time, region, route, version, dependency, status, durationMs)`

## Three trade-offs

1. Traffic shift restores service but may overload healthy regions.
2. Local reads lower latency but can return replicated stale data.
3. Active-active writes reduce failover time but require conflict handling.

## Three failures and mitigations

1. **Destination overload:** shift gradually with stop thresholds.
2. **Stale read:** expose freshness rules or route critical reads to the owner.
3. **Conflicting writes:** keep a single write owner unless conflicts are designed explicitly.

## Two-minute spoken answer

I prove the issue is regional by comparing the same route, version, traffic class, and dependency across regions. A slow trace distinguishes service work, regional storage, network transit, and accidental cross-region calls. I also split by availability zone and deployment cohort.

Before shifting traffic, I verify destination capacity and data. Reads may tolerate lag; payments, authorization, or ordered writes may require the write owner. I shift gradually with stop thresholds while watching latency, errors, saturation, and correctness. A stale regional replica may be safer for optional reads than full failover. I keep writes single-owner unless conflict resolution exists. Recovery requires user latency, safe destination saturation, and reconciliation showing no lost or conflicting operations.

## Recall questions

1. What must be true before shifting traffic?
2. Which operations cannot safely use a lagging replica?
3. Why can active-active writes create a new incident?
