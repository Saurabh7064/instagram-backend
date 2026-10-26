# Micro-lesson 16 — Vector clocks: detect concurrent versions

[Module](./README.md)

- Artifact: `READY`; comprehension: `TODO`.
- Time: 10–15 minutes. Primary idea: vector clocks: detect concurrent versions.
- Prerequisite: Complete the stop/go check for [Logical clocks and Lamport timestamps](./15-lamport-clocks.md).
- Original roadmap topics: 24.

## Why does this exist? — Must understand

A vector clock records one logical counter per tracked participant. V happens before W when every component of V is at most W and at least one is smaller. Incomparable vectors indicate concurrency: neither version includes the other’s history.

## Analogy

A shared revision card tracks contributions by each branch. Unlike a fixed paper form, real systems must manage changing participants and metadata growth.

## Predict before reading

Which version is newer: [1,0] or [0,1]? Explain your prediction before expanding the checkpoints below.

## Concrete flow

1. A/B start [0,0].
2. A edits to [1,0] while disconnected.
3. B edits to [0,1]. Comparing components: 1>0 but 0<1, so neither dominates. Merge histories by componentwise max to [1,1]; if A resolves with a new edit, increment A to [2,1].

## Instagram-backend mapping

[PostService.create, feed, and transactional methods](../../../../../src/main/java/com/instagram/backend/service/PostService.java) are the real database boundary. Replication, consensus, distributed locks, durable request-key handling, and multi-service workflows in this lesson are **proposed**, not implemented by these annotations.

## Decision and trade-off

Use vector metadata when detecting concurrent versions matters and participant tracking is manageable. Lamport counters are smaller but cannot identify concurrency. Detecting a conflict does not choose a business merge policy.

## Failure drill — prediction, symptom, recovery, prevention

Predict blindly choosing the larger scalar timestamp for the two edits. One intent can disappear. In a paper exercise, preserve both versions, resolve deliberately, and record merged history. Bound metadata without silently inventing ordering guarantees.

These are design/paper drills, not claims that a replica cluster or fault-injection lab was executed.

## Questions and explained answers

<details>
<summary>1. Which version is newer: [1,0] or [0,1]?</summary>

Neither causally includes the other. They represent concurrent histories.

</details>

<details>
<summary>2. Does max produce the merged caption text?</summary>

No. [1,1] merges causal history; content resolution is a separate decision.

</details>

<details>
<summary>3. Why increment after a resolving edit?</summary>

Resolution creates a new event beyond the histories being combined, giving [2,1] at A.

</details>

## Teach-back and stop/go

Explain the problem, trace the example, defend the choice, and diagnose the failure in 60–90 seconds without notes. Proceed only after a correct prediction, explanation, and failure diagnosis; reading this artifact does not complete comprehension.

## Useful later — primary reference

[Dynamo paper](https://www.allthingsdistributed.com/files/amazon-dynamo-sosp2007.pdf). Read for protocol details beyond this introductory model.
