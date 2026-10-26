# Micro-lesson 07 — Quorum reads and writes

[Module](./README.md)

- Artifact: `READY`; comprehension: `TODO`.
- Time: 10–15 minutes. Primary idea: quorum reads and writes.
- Prerequisite: Complete the stop/go check for [Linearizability versus serializability](./06-linearizability-and-serializability.md).
- Original roadmap topics: 16.

## Why does this exist? — Must understand

A quorum is the required number of participating copies. Let N be the fixed replica count, W the write acknowledgments, and R the read responses. With the same replica membership, R+W>N forces read/write overlap; W>N/2 forces two write sets to overlap. Overlap alone is not a complete consistency protocol.

## Analogy

Asking enough members of a fixed committee ensures two groups share someone. Unlike people, replicas need explicit version comparison and conflict handling; a shared member does not interpret intent.

## Predict before reading

Why does 2+2>3 matter? Explain your prediction before expanding the checkpoints below.

## Concrete flow

1. N=3, copies A/B/C.
2. write v2 reaches A/B, so W=2.
3. read B/C with R=2 overlaps at B.
4. B returns v2 and C returns v1. A version-aware reader can choose v2. With R=1, reading C can miss the completed write.

## Instagram-backend mapping

[PostService.create, feed, and transactional methods](../../../../../src/main/java/com/instagram/backend/service/PostService.java) are the real database boundary. Replication, consensus, distributed locks, durable request-key handling, and multi-service workflows in this lesson are **proposed**, not implemented by these annotations.

## Decision and trade-off

Use quorum protocols when their documented ordering and repair behavior fit the requirement. Two-of-three participation tolerates one unavailable member; waiting for a second reply adds latency. Substitute temporary members only with explicit protocol analysis.

## Failure drill — prediction, symptom, recovery, prevention

Predict W=2 when only A is reachable. The write cannot meet its acknowledgment rule. Observe failed/uncertain writes, restore a second member, then reconcile. Do not declare the write absent simply because quorum acknowledgment was lost.

These are design/paper drills, not claims that a replica cluster or fault-injection lab was executed.

## Questions and explained answers

<details>
<summary>1. Why does 2+2>3 matter?</summary>

Two sets of size two within the same three members cannot be disjoint.

</details>

<details>
<summary>2. Does overlap prove linearizability?</summary>

No. Concurrent writes, versions, reads, membership, and failure rules still need a correct protocol.

</details>

<details>
<summary>3. Can one reachable copy satisfy W=2?</summary>

No. Reducing W changes the contract rather than repairing availability for free.

</details>

## Teach-back and stop/go

Explain the problem, trace the example, defend the choice, and diagnose the failure in 60–90 seconds without notes. Proceed only after a correct prediction, explanation, and failure diagnosis; reading this artifact does not complete comprehension.

## Useful later — primary reference

[Dynamo paper](https://www.allthingsdistributed.com/files/amazon-dynamo-sosp2007.pdf). Read for protocol details beyond this introductory model.
