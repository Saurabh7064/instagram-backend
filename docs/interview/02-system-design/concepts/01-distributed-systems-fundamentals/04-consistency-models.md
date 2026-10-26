# Micro-lesson 04 — Consistency models: strong and eventual

[Module](./README.md)

- Artifact: `READY`; comprehension: `TODO`.
- Time: 10–15 minutes. Primary idea: consistency models: strong and eventual.
- Prerequisite: Complete the stop/go check for [PACELC: the healthy-network trade-off](./03-pacelc.md).
- Original roadmap topics: 4–6.

## Why does this exist? — Must understand

A consistency model states which observations clients may see. Here, strong consistency means the single-copy, real-time guarantee formalized as linearizability in lesson 6. Eventual consistency promises convergence when updates stop and communication/repair continues; by itself it supplies no fixed staleness deadline.

## Analogy

Branch noticeboards can briefly show different announcements before updates arrive. Unlike paper boards, automatic repair needs explicit rules for competing edits.

## Predict before reading

Does eventual mean within one second? Explain your prediction before expanding the checkpoints below.

## Concrete flow

1. A and B hold v1.
2. A accepts v2.
3. B briefly returns v1.
4. replication and repair deliver v2.
5. both return v2. If both accept different updates, a conflict rule must decide or merge the outcome; waiting alone cannot define it.

## Instagram-backend mapping

[PostService.create, feed, and transactional methods](../../../../../src/main/java/com/instagram/backend/service/PostService.java) are the real database boundary. Replication, consensus, distributed locks, durable request-key handling, and multi-service workflows in this lesson are **proposed**, not implemented by these annotations.

## Decision and trade-off

Tolerate eventual reads for approximate public like counts. Require stronger behavior for a decision that must use a completed revocation. Stronger coordination can increase delay and reduce availability during failures.

## Failure drill — prediction, symptom, recovery, prevention

Predict what happens when repair stops permanently. Pause delivery in a proposed lab and observe growing version lag. Resume and verify convergence. Alert on age/version lag; “eventual” is not permission for indefinitely broken replication.

These are design/paper drills, not claims that a replica cluster or fault-injection lab was executed.

## Questions and explained answers

<details>
<summary>1. Does eventual mean within one second?</summary>

No. A time bound is an extra guarantee.

</details>

<details>
<summary>2. Does convergence preserve every user intent?</summary>

No. A conflict policy can discard an edit while still making copies agree.

</details>

<details>
<summary>3. Is a transaction annotation a replica freshness contract?</summary>

No. Local transaction boundaries and cross-node observations are different guarantees.

</details>

## Teach-back and stop/go

Explain the problem, trace the example, defend the choice, and diagnose the failure in 60–90 seconds without notes. Proceed only after a correct prediction, explanation, and failure diagnosis; reading this artifact does not complete comprehension.
