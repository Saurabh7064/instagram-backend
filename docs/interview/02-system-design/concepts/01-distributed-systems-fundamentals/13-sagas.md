# Micro-lesson 13 — Sagas: recover a multi-step business operation

[Module](./README.md)

- Artifact: `READY`; comprehension: `TODO`.
- Time: 10–15 minutes. Primary idea: sagas: recover a multi-step business operation.
- Prerequisite: Complete the stop/go check for [Distributed transactions and two-phase commit](./12-distributed-transactions-and-2pc.md).
- Original roadmap topics: 20.

## Why does this exist? — Must understand

A saga is a sequence of locally committed steps with recovery actions. Compensation is a business action that counteracts an earlier completed step; it is not a database rollback. An orchestrator durably tracks the workflow and decides the next action.

## Analogy

A trip booking cancels an earlier reservation when the next booking fails. Unlike undoing a draft, cancellation may have a fee or require manual resolution.

## Predict before reading

Must notification failure delete a valid post? Explain your prediction before expanding the checkpoints below.

## Concrete flow

1. Proposed publish workflow stores media.
2. creates post metadata.
3. notification step fails. If notifications are optional, retry them without deleting the post. If post creation fails permanently, schedule cleanup of unreferenced media. The business rule chooses compensation.

## Instagram-backend mapping

[PostService.create](../../../../../src/main/java/com/instagram/backend/service/PostService.java) saves database metadata; [MediaStorageService.store](../../../../../src/main/java/com/instagram/backend/service/MediaStorageService.java) writes a file separately. These methods do not form a shared distributed transaction. The coordinated workflow described here is proposed.

## Decision and trade-off

Use a saga when work spans services that cannot share a transaction and temporary intermediate states are acceptable. Prefer one database transaction when possible. Compensation adds state, retries, and potentially irreversible consequences.

## Failure drill — prediction, symptom, recovery, prevention

Predict cleanup failure after post creation fails. Observe pending recovery rather than a fictitious all-or-nothing result. Retry cleanup with durable progress and alert after bounded failures; reconcile orphan files before deleting them.

These are design/paper drills, not claims that a replica cluster or fault-injection lab was executed.

## Questions and explained answers

<details>
<summary>1. Must notification failure delete a valid post?</summary>

No. Compensation follows business requirements, not a mechanical reverse-all-steps rule.

</details>

<details>
<summary>2. Can compensation fail?</summary>

Yes. It needs durable tracking, retries, and escalation.

</details>

<details>
<summary>3. Is a saga isolated like one local transaction?</summary>

No. Other observers can see intermediate committed states; business safeguards may be needed.

</details>

## Teach-back and stop/go

Explain the problem, trace the example, defend the choice, and diagnose the failure in 60–90 seconds without notes. Proceed only after a correct prediction, explanation, and failure diagnosis; reading this artifact does not complete comprehension.
