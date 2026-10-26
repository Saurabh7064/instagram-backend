# Micro-lesson 12 — Distributed transactions and two-phase commit

[Module](./README.md)

- Artifact: `READY`; comprehension: `TODO`.
- Time: 10–15 minutes. Primary idea: distributed transactions and two-phase commit.
- Prerequisite: Complete the stop/go check for [Split brain and fenced distributed locks](./11-split-brain-and-locks.md).
- Original roadmap topics: 18–19.

## Why does this exist? — Must understand

A distributed transaction coordinates one atomic decision across participants. Two-phase commit (2PC) first asks participants to prepare durable work, then delivers a commit or rollback decision. A coordinator records and distributes that decision.

## Analogy

Two branches reserve their changes before a supervisor gives a final instruction. Unlike a casual reservation, prepared database work can retain locks while awaiting recovery.

## Predict before reading

Can a prepared participant unilaterally time out and roll back? Explain your prediction before expanding the checkpoints below.

## Concrete flow

1. Coordinator asks databases A/B to prepare.
2. both report ready.
3. coordinator durably records commit.
4. tells A, then crashes before telling B. A commits; B remains prepared until recovery retrieves and delivers the durable decision. Final atomic outcome may require waiting.

## Instagram-backend mapping

[PostService.create](../../../../../src/main/java/com/instagram/backend/service/PostService.java) saves database metadata; [MediaStorageService.store](../../../../../src/main/java/com/instagram/backend/service/MediaStorageService.java) writes a file separately. These methods do not form a shared distributed transaction. The coordinated workflow described here is proposed.

## Decision and trade-off

Choose 2PC for supported participants when atomic outcome outweighs blocking and operational costs. A local Spring transaction does not automatically enlist file storage, HTTP services, or another database.

## Failure drill — prediction, symptom, recovery, prevention

Predict B after the coordinator crash. Observe an unresolved prepared transaction and blocked conflicting work. Recover the recorded decision; do not guess rollback. Monitor prepared-transaction age and maintain a coordinator recovery procedure.

These are design/paper drills, not claims that a replica cluster or fault-injection lab was executed.

## Questions and explained answers

<details>
<summary>1. Can a prepared participant unilaterally time out and roll back?</summary>

Not safely after voting ready: a commit decision may already exist.

</details>

<details>
<summary>2. Does 2PC eliminate blocking?</summary>

No. Decision uncertainty can hold resources until recovery.

</details>

<details>
<summary>3. Is @Transactional proof of distributed atomicity?</summary>

No. It defines a configured transaction boundary; external participants need explicit supported coordination.

</details>

## Teach-back and stop/go

Explain the problem, trace the example, defend the choice, and diagnose the failure in 60–90 seconds without notes. Proceed only after a correct prediction, explanation, and failure diagnosis; reading this artifact does not complete comprehension.

## Useful later — primary reference

[PostgreSQL prepared transactions](https://www.postgresql.org/docs/current/sql-prepare-transaction.html). Read for protocol details beyond this introductory model.
