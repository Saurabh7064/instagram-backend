# Micro-lesson 06 — Linearizability versus serializability

[Module](./README.md)

- Artifact: `READY`; comprehension: `TODO`.
- Time: 10–15 minutes. Primary idea: linearizability versus serializability.
- Prerequisite: Complete the stop/go check for [Session guarantees: causality and your own writes](./05-session-and-causal-consistency.md).
- Original roadmap topics: 9–10.

## Why does this exist? — Must understand

Linearizability makes each operation appear to take effect at one instant between invocation and response, preserving real-time order. Serializability makes committed transactions equivalent to some one-at-a-time order, which need not respect wall-clock order. Strict serializability adds real-time ordering to serializability.

## Analogy

A single service desk suggests instantaneous ordered decisions, while a batch ledger can be rearranged into a valid sequential order. Real databases allow overlapping work and may abort transactions to preserve their promise.

## Predict before reading

Does serializable imply real-time order? Explain your prediction before expanding the checkpoints below.

## Concrete flow

1. Caption write v2 completes.
2. a later read returns v1: this violates linearizability. Separately, two transactions read the same state and make mutually incompatible changes: serializable execution must prevent both committing when no valid sequential order explains the result.

## Instagram-backend mapping

[PostService.create, feed, and transactional methods](../../../../../src/main/java/com/instagram/backend/service/PostService.java) are the real database boundary. Replication, consensus, distributed locks, durable request-key handling, and multi-service workflows in this lesson are **proposed**, not implemented by these annotations.

## Decision and trade-off

Use serializable transactions for multi-row invariants, with whole-transaction retries on serialization failure. Specify read freshness separately. PostgreSQL’s isolation documentation is not a promise that every external read path is linearizable.

## Failure drill — prediction, symptom, recovery, prevention

Predict whether stronger isolation removes retry handling. In a proposed concurrency test, record one serialization abort, retry the whole transaction, and verify the invariant. Bound retries and surface exhaustion; never retry only the final statement.

These are design/paper drills, not claims that a replica cluster or fault-injection lab was executed.

## Questions and explained answers

<details>
<summary>1. Does serializable imply real-time order?</summary>

No. Strict serializability includes that additional requirement.

</details>

<details>
<summary>2. Does linearizability mean all requests run sequentially internally?</summary>

No. Overlapping operations may execute concurrently if observations fit a valid instantaneous order.

</details>

<details>
<summary>3. Why retry a whole aborted transaction?</summary>

Its earlier reads informed its decisions; a fresh attempt must recompute them.

</details>

## Teach-back and stop/go

Explain the problem, trace the example, defend the choice, and diagnose the failure in 60–90 seconds without notes. Proceed only after a correct prediction, explanation, and failure diagnosis; reading this artifact does not complete comprehension.

## Useful later — primary reference

[PostgreSQL transaction isolation](https://www.postgresql.org/docs/current/transaction-iso.html). Read for protocol details beyond this introductory model.
