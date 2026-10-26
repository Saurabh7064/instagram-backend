# Micro-lesson 08 — Consensus and leader election

[Module](./README.md)

- Artifact: `READY`; comprehension: `TODO`.
- Time: 10–15 minutes. Primary idea: consensus and leader election.
- Prerequisite: Complete the stop/go check for [Quorum reads and writes](./07-quorums.md).
- Original roadmap topics: 11, 14.

## Why does this exist? — Must understand

Consensus is agreement on a chosen decision despite allowed failures. Leader election selects a coordinator. A majority is more than half the fixed voters. Selecting someone to lead does not alone prove that their writes are safe or durable.

## Analogy

A committee appoints a chair and agrees on a recorded decision. Unlike a human meeting, servers must handle delayed messages from old chairs and retain protocol state across crashes.

## Predict before reading

Is electing a leader sufficient for safe replication? Explain your prediction before expanding the checkpoints below.

## Concrete flow

1. In a proposed three-voter group A/B/C, A coordinates a decision.
2. B and C lose contact with A.
3. B obtains two votes under the protocol.
4. A cannot form a majority alone. Safe continuation also requires preserving previously chosen decisions.

## Instagram-backend mapping

[PostService.create, feed, and transactional methods](../../../../../src/main/java/com/instagram/backend/service/PostService.java) are the real database boundary. Replication, consensus, distributed locks, durable request-key handling, and multi-service workflows in this lesson are **proposed**, not implemented by these annotations.

## Decision and trade-off

Use established consensus implementations for critical shared metadata. Keep ordinary application calls out of consensus unless agreement is needed. A majority outage stops progress; agreement does not remove network delay.

## Failure drill — prediction, symptom, recovery, prevention

Predict the one-node side of a 1–2 split. Observe rejected proposals there while the two-node side can progress under the protocol. Restore connectivity and reconcile protocol state; never force two independent majorities by unsafe membership edits.

These are design/paper drills, not claims that a replica cluster or fault-injection lab was executed.

## Questions and explained answers

<details>
<summary>1. Is electing a leader sufficient for safe replication?</summary>

No. The protocol must also preserve already chosen decisions.

</details>

<details>
<summary>2. How many voters form a majority of three?</summary>

Two: floor(3/2)+1=2.

</details>

<details>
<summary>3. Can safety survive loss of progress?</summary>

Yes. Refusing a decision can preserve correctness while availability is reduced.

</details>

## Teach-back and stop/go

Explain the problem, trace the example, defend the choice, and diagnose the failure in 60–90 seconds without notes. Proceed only after a correct prediction, explanation, and failure diagnosis; reading this artifact does not complete comprehension.
