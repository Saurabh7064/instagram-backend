# Micro-lesson 11 — Split brain and fenced distributed locks

[Module](./README.md)

- Artifact: `READY`; comprehension: `TODO`.
- Time: 10–15 minutes. Primary idea: split brain and fenced distributed locks.
- Prerequisite: Complete the stop/go check for [Paxos: preserve an already chosen value](./10-paxos.md).
- Original roadmap topics: 15, 17.

## Why does this exist? — Must understand

Split brain means multiple nodes act as authoritative owners. A distributed lock coordinates exclusive work across processes. A fencing token is an increasing ownership number that the protected resource checks, rejecting older owners even if they resume late.

## Analogy

A workshop issues numbered keycards and the machine rejects older cards. Unlike an ordinary physical key, correctness depends on the machine checking every operation, not only on issuing the card.

## Predict before reading

Does expiration stop a paused worker? Explain your prediction before expanding the checkpoints below.

## Concrete flow

1. Worker A gets token 41.
2. pauses beyond its lock lifetime.
3. B gets token 42 and writes.
4. A resumes with 41. Storage must reject 41 after accepting 42. Lock expiry alone cannot stop A’s already-running code.

## Instagram-backend mapping

[PostService.create, feed, and transactional methods](../../../../../src/main/java/com/instagram/backend/service/PostService.java) are the real database boundary. Replication, consensus, distributed locks, durable request-key handling, and multi-service workflows in this lesson are **proposed**, not implemented by these annotations.

## Decision and trade-off

Use fenced ownership when exclusive background work crosses processes. Prefer a database constraint or conditional update for a local invariant. Token generation needs reliable ordering and the resource must enforce it atomically.

## Failure drill — prediction, symptom, recovery, prevention

Predict A’s late write after B takes over. In a proposed test, pause A, allow B’s write, then resume A. Detect stale-token rejection. Recover by rereading state under new ownership; do not let A blindly repeat its old write.

These are design/paper drills, not claims that a replica cluster or fault-injection lab was executed.

## Questions and explained answers

<details>
<summary>1. Does expiration stop a paused worker?</summary>

No. It changes ownership records, not the old process’s execution.

</details>

<details>
<summary>2. Where is fencing enforced?</summary>

At the protected resource when accepting an operation, atomically with its ownership/version check.

</details>

<details>
<summary>3. Can a random UUID establish ownership order?</summary>

No. Uniqueness alone does not identify which owner is newer.

</details>

## Teach-back and stop/go

Explain the problem, trace the example, defend the choice, and diagnose the failure in 60–90 seconds without notes. Proceed only after a correct prediction, explanation, and failure diagnosis; reading this artifact does not complete comprehension.
