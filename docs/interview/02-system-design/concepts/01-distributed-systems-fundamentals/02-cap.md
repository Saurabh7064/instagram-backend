# Micro-lesson 02 — CAP: what to do during a partition

[Module](./README.md)

- Artifact: `READY`; comprehension: `TODO`.
- Time: 10–15 minutes. Primary idea: cap: what to do during a partition.
- Prerequisite: Complete the stop/go check for [Distributed systems and partial failure](./01-partial-failure.md).
- Original roadmap topics: 2.

## Why does this exist? — Must understand

A partition is a communication break between groups of nodes. CAP consistency means operations behave like one current copy, respecting completed operations. CAP availability means every request to a non-failing node eventually receives a valid operation response. During a partition, a read/write service cannot guarantee both for all requests.

## Analogy

Two disconnected clerks cannot both promise the same latest stock count while each keeps accepting sales. Unlike a shop, the formal model permits unbounded message delay; an error response does not satisfy CAP availability.

## Predict before reading

Can B return v1 and preserve CAP consistency? Explain your prediction before expanding the checkpoints below.

## Concrete flow

1. A and B start with caption v1.
2. network splits.
3. A acknowledges v2.
4. B receives a read. Returning v1 sacrifices the single-copy promise. Waiting or refusing the operation sacrifices CAP availability. B cannot learn v2 through the broken link.

## Instagram-backend mapping

[PostService.create, feed, and transactional methods](../../../../../src/main/java/com/instagram/backend/service/PostService.java) are the real database boundary. Replication, consensus, distributed locks, durable request-key handling, and multi-service workflows in this lesson are **proposed**, not implemented by these annotations.

## Decision and trade-off

For an ownership or privacy decision, fail closed when authoritative state is unreachable. A public counter may tolerate stale data. Choose per operation; “pick any two” hides the failure condition.

## Failure drill — prediction, symptom, recovery, prevention

Predict B’s read after A acknowledges v2. Isolate B in a proposed replica lab. Detect stale values or rejected reads. Restore communication and repair state; do not treat reconnection itself as conflict resolution.

These are design/paper drills, not claims that a replica cluster or fault-injection lab was executed.

## Questions and explained answers

<details>
<summary>1. Can B return v1 and preserve CAP consistency?</summary>

No: the v2 write completed before this read began.

</details>

<details>
<summary>2. Does HTTP 503 preserve CAP availability?</summary>

No. It exposes unavailability rather than completing the requested data operation.

</details>

<details>
<summary>3. Does choosing consistency mean always unavailable?</summary>

No. The sacrificed operations are those unable to meet the guarantee during the partition.

</details>

## Teach-back and stop/go

Explain the problem, trace the example, defend the choice, and diagnose the failure in 60–90 seconds without notes. Proceed only after a correct prediction, explanation, and failure diagnosis; reading this artifact does not complete comprehension.

## Useful later — primary reference

[CAP proof](https://pld.cs.luc.edu/database/gilbert_lynch_brewer_proof.pdf). Read for protocol details beyond this introductory model.
