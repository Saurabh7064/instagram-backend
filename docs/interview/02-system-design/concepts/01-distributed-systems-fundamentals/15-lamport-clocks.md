# Micro-lesson 15 — Logical clocks and Lamport timestamps

[Module](./README.md)

- Artifact: `READY`; comprehension: `TODO`.
- Time: 10–15 minutes. Primary idea: logical clocks and lamport timestamps.
- Prerequisite: Complete the stop/go check for [Idempotency keys and delivery guarantees](./14-idempotency-and-delivery.md).
- Original roadmap topics: 23, 25.

## Why does this exist? — Must understand

A logical clock records event ordering without claiming wall-clock time. Happened-before means local program order or message-send before receipt, including chains of those relations. A Lamport timestamp is a counter that increases on each local event and becomes max(local, received)+1 on receipt.

## Analogy

A numbered correspondence chain ensures a reply number exceeds the message it answers. Unlike a real clock, the number says nothing about elapsed seconds.

## Predict before reading

Does timestamp(a)<timestamp(b) prove a caused b? Explain your prediction before expanding the checkpoints below.

## Concrete flow

1. A starts at 0.
2. sends at 1.
3. B starts at 0 and receives timestamp 1, so max(0,1)+1=2.
4. B replies at 3.
5. A receives at max(1,3)+1=4. Independent C can reach 2 without communicating: a smaller number alone does not prove causation.

## Instagram-backend mapping

[PostService.create, feed, and transactional methods](../../../../../src/main/java/com/instagram/backend/service/PostService.java) are the real database boundary. Replication, consensus, distributed locks, durable request-key handling, and multi-service workflows in this lesson are **proposed**, not implemented by these annotations.

## Decision and trade-off

Use logical ordering where clock drift makes physical timestamps misleading. Pair counter with node identity for deterministic tie-breaking, but do not interpret that arbitrary total order as a causal relationship.

## Failure drill — prediction, symptom, recovery, prevention

Predict sorting logs by machine time after clock skew. A reply may appear before its request. In a paper drill, preserve send/receive dependencies using logical counters; retain wall time separately for human diagnostics.

These are design/paper drills, not claims that a replica cluster or fault-injection lab was executed.

## Questions and explained answers

<details>
<summary>1. Does timestamp(a)<timestamp(b) prove a caused b?</summary>

No. Happened-before implies increasing timestamps, but the converse does not hold.

</details>

<details>
<summary>2. Why max(local, received)+1?</summary>

The receipt must follow both the receiver’s prior event and the received send event.

</details>

<details>
<summary>3. Can timestamp 4 mean four seconds?</summary>

No. It counts logical progress under these rules, not physical duration.

</details>

## Teach-back and stop/go

Explain the problem, trace the example, defend the choice, and diagnose the failure in 60–90 seconds without notes. Proceed only after a correct prediction, explanation, and failure diagnosis; reading this artifact does not complete comprehension.

## Useful later — primary reference

[Lamport: Time, Clocks, and Ordering](https://lamport.azurewebsites.net/pubs/time-clocks.pdf). Read for protocol details beyond this introductory model.
