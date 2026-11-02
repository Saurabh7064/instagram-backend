# Single-Region Latency Spike

## Question

Global latency looks healthy, but users in one region see slow and intermittent responses. Other regions remain normal.

Your manager asks:

**“How would you isolate the regional fault, and when would you shift traffic?”**

> [One-page revision card](../../revision-cards/production-scenarios/08-single-region-latency-spike.md)

## Moving the traffic could move the outage

Region A's feed is slow while region B looks healthy. Sending A's users to B sounds like the obvious rescue. But B may already be near its tested capacity, its copy of data may lag, or its application may still call the same dependency that is failing for A.

The first job is to locate what differs in A. The second is to establish what the destination can safely take over. Those are separate questions: identifying the bad region does not prove that every request can be moved.

Reads with an approved freshness window may move safely before writes. Writes need explicit authority and retry handling, or the same logical operation can be accepted in two places with conflicting results.

## A diversion works only if the other airport has room

When one airport has severe weather, traffic can be diverted to a nearby airport only if it has runway capacity, ground staff, and the passengers’ onward connections. Redirecting everything at once can close the second airport too.

The analogy stops because replicated data can lag and two software regions can accept conflicting writes. Passenger records do not normally merge themselves after both airports change them.

## Moving a request is different from moving its authority

**Traffic shifting** changes where new requests go. It does not automatically copy their needed data or transfer the right to update it.

**Replication lag** is the delay before a committed write appears in another copy. A feed read in B might therefore miss a post just accepted in A. Whether that is permissible depends on the stated freshness requirement.

A **write owner** is the region or partition allowed to order and accept a particular write. Keep ownership explicit during failover; two regions independently accepting the same record's changes need a designed conflict policy.

The **recovery time objective (RTO)** states how quickly we target restoration. The **recovery point objective (RPO)** states the maximum targeted data-loss window. A fast shift that loses recent accepted writes can meet a time goal and still violate the data goal.

## Find the first difference, then test the escape route

Compare the same route, version, status, and traffic class across regions. Within A, split by zone and instance so one bad deployment or network path is not mistaken for a whole-region outage. Compare caller and server timing too; a delay before the application sees the request points toward routing or transport rather than its handler.

Follow a slow A trace beside a healthy B trace. Find the first operation whose behavior diverges: local storage, a shared service, or an unexpected cross-region call. Use the matching evidence—connection failures, packet loss, replica freshness, resource wait, or a recent regional change—to test that explanation.

Before moving users, verify B's remaining compute and dependency capacity, quotas, credentials, configuration, and data. “B is healthy at its current load” is a weaker claim than “B can safely serve both populations.” Separate read eligibility from write ownership, including where cross-region retries will be deduplicated.

Shift a small cohort with explicit stop thresholds and watch both regions. Increase only while affected-user latency improves and destination saturation and correctness remain safe. Keep a route back when data and ownership permit it. If the destination begins degrading, stop before turning a local incident into a global one.

## Follow the accidental cross-region Profile call

Assume the proposed multi-region feed system has regions A and B. The feed can tolerate a documented short read-staleness window; writes retain one established owner.

1. Feed p99 reaches four seconds only in A. Its local application work stays normal.
2. A slow trace shows a Profile call to B taking 3.5 seconds. Healthy traces normally use a local Profile endpoint.
3. A recent routing change made A's local endpoint unavailable and activated the remote fallback. The first divergent span identifies the mechanism more precisely than “A is slow.”
4. The team repairs local routing. If users still need relief, it first verifies B's spare capacity and replicated read freshness, then shifts 10% of A's eligible read traffic to B.
5. Writes keep their existing owner. Retried reads and writes retain their required operation identities rather than becoming unrelated new work.
6. The team watches A recover and B remain within its limits, then checks for missing or conflicting operations before declaring recovery.

The percentages and durations are an exercise, not evidence of an implemented global router in this repository.

## Instagram-backend mapping

This repository is a single local Spring Boot deployment, not a multi-region system. [application.properties](../../../../../src/main/resources/application.properties) attaches application/environment/version tags and configures health and readiness, but it has no region tag, global router, replica-freshness signal, or failover controller.

[MediaStorageService.java](../../../../../src/main/java/com/instagram/backend/service/MediaStorageService.java) stores uploads on the instance filesystem. That is a concrete blocker even for multi-instance operation: shifting traffic could make existing media unavailable. PostgreSQL configuration is also a single endpoint.

**Proposed, not implemented:** shared object storage, regional service and dependency metrics, a tested global routing policy, replicated read data with a stated freshness bound, one write owner per record or partition, idempotency across retries, and reconciliation after failover.

## Read diversion and write failover buy different things

- **Shift reads to a healthy region** when it has capacity and bounded stale data is acceptable. Avoid it when the destination lacks required data; cost includes higher network latency and load.
- **Active-passive writes** when simpler ownership and conflict avoidance matter. It has a longer failover path and requires tested promotion.
- **Active-active writes** only when the product needs regional write availability and conflict semantics are designed. It reduces failover time but adds routing, consistency, and reconciliation failures.

## The destination is healthy until we send it everyone

**Prediction:** shifting all of A at once can exceed B's worker or database capacity even though B was healthy before the shift.

**Detect:** A's latency improves while B's active work, connection waiting, and tail latency rise. If completions stop increasing, adding arrivals is feeding a queue rather than useful capacity.

**Contain:** halt further movement, reduce or reverse the shift where safe, and shed optional reads. Preserve capacity for critical requests and recovery operations.

**Recover:** let B's queues drain, re-establish A or add proven capacity, and resume only a percentage the full dependency path can sustain.

**Prevent:** reserve evacuation headroom, test the shifted peak load and downstream quotas, use staged routing with stop thresholds, and rehearse data promotion separately from moving read traffic.

## Two-to-three-minute interview answer

I first prove the fault boundary by comparing the same route, version, percentile, and traffic class across regions, then split the affected region by zone and instance. I compare client and server timing and follow a slow trace against a healthy trace to find the first difference: regional storage, a shared dependency, DNS or network transit, or an accidental cross-region call.

Before shifting traffic, I verify the destination’s compute and dependency capacity, quotas, credentials, configuration, and data freshness. Reads may tolerate replication lag; authorization or ordered writes may require the existing write owner. I shift a small cohort first and watch latency, errors, saturation, business outcomes, and correctness in both regions. I stop automatically if the destination approaches its safe limit. Writes remain single-owner unless active-active conflict handling already exists, and retried operations need idempotency. Recovery requires affected-user latency to improve, destination capacity to remain safe, and reconciliation to show no missing or conflicting operations.

## Before you practise

The written scenario is ready for practice; comprehension remains separate. Explain what makes the 10% read shift safe, predict what could happen to writes, and try the final questions before opening their answers.

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
