# Micro-lesson 03 — PACELC: the healthy-network trade-off

[Module](./README.md)

- Artifact: `READY`; comprehension: `TODO`.
- Time: 10–15 minutes. Primary idea: pacelc: the healthy-network trade-off.
- Prerequisite: Complete the stop/go check for [CAP: what to do during a partition](./02-cap.md).
- Original roadmap topics: 3.

## Why does this exist? — Must understand

PACELC extends the partition discussion: if there is a partition, consider availability versus consistency; else, consider latency versus consistency. Latency is elapsed time to complete an operation. A replica is another maintained copy of the data.

## Analogy

A clerk can answer from a local notebook or phone a remote branch first. Calling adds waiting. Unlike notebooks, replicas can exchange updates continuously and offer several configurable guarantees.

## Predict before reading

Must a partition occur before latency matters? Explain your prediction before expanding the checkpoints below.

## Concrete flow

1. Primary saves caption v2.
2. local acknowledgment can return before a distant replica receives v2.
3. a nearby read there may see v1. Waiting for remote confirmation moves communication delay onto the write path. Safe reads still require the chosen protocol.

## Instagram-backend mapping

[PostService.create, feed, and transactional methods](../../../../../src/main/java/com/instagram/backend/service/PostService.java) are the real database boundary. Replication, consensus, distributed locks, durable request-key handling, and multi-service workflows in this lesson are **proposed**, not implemented by these annotations.

## Decision and trade-off

Use local stale reads for explicitly tolerant displays. Pay coordination latency where freshness is required. PACELC is a decision lens, not proof that a product has one universal setting.

## Failure drill — prediction, symptom, recovery, prevention

Predict the effect of adding remote delay without disconnecting anything. In a proposed lab, compare acknowledgment time and stale-read rate. Recover by restoring delay; choose routing and confirmation policy from an explicit freshness requirement.

These are design/paper drills, not claims that a replica cluster or fault-injection lab was executed.

## Questions and explained answers

<details>
<summary>1. Must a partition occur before latency matters?</summary>

No. Healthy remote communication still takes time.

</details>

<details>
<summary>2. Does one remote acknowledgment make every read current?</summary>

No. Read routing and protocol correctness still matter.

</details>

<details>
<summary>3. What should be measured together?</summary>

Latency and the actual freshness/correctness guarantee, including how often stale results occur.

</details>

## Teach-back and stop/go

Explain the problem, trace the example, defend the choice, and diagnose the failure in 60–90 seconds without notes. Proceed only after a correct prediction, explanation, and failure diagnosis; reading this artifact does not complete comprehension.

## Useful later — primary reference

[PACELC author explanation](https://dbmsmusings.blogspot.com/2010/04/problems-with-cap-and-yahoos-little.html). Read for protocol details beyond this introductory model.
