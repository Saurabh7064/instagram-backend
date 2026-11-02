# Micro-lesson 13 — Sagas: recover business steps that already committed

[Module](./README.md) · [Previous: two-phase commit](./12-distributed-transactions-and-2pc.md) · [Next: idempotency and delivery](./14-idempotency-and-delivery.md)

Core study: 10–15 minutes. Prerequisites: local commits persist; 2PC requires cooperating participants and can block.

## The file was saved. The post was not. Now what? — Must understand

Publishing post 42 needs three steps: store media M42, save metadata referring to M42, then notify a follower. The file store, database, and notification provider may not share a transaction protocol. Keeping a Java database transaction open does not make the other two services roll back with it.

Before picking a recovery technique, decide what “published” means. In this proposed workflow, publication succeeds when metadata commits with an existing media reference. Notification may arrive later. If metadata creation permanently fails, unreferenced M42 should be cleaned up after checking it is safe to delete.

Those requirements imply two different responses to failure. A metadata failure leaves a file with no post, so cleanup makes sense. A notification failure leaves a valid post, so deleting it would undo successful publication for the wrong reason. We should retry the notification instead.

## Record the recovery work as carefully as the forward work

A **saga** is a workflow of locally committed steps with rules for retrying or counteracting them. Saga is a name, not an acronym. Each completed step is durable, and intermediate states can be visible while the rest of the workflow runs.

Counteracting an already completed step is **compensation**. Deleting unused M42 is a new operation; it is not the database rolling back a file save. It can fail, and it must not delete a file that has since acquired a legitimate reference. Similarly, an email already read cannot be made unread by rolling back a row.

An **orchestrator** tracks the workflow and decides its next action. That state must survive a crash. A call stack disappears when the process stops, while a durable `CLEANUP_PENDING` record lets another attempt discover unfinished work. We will start with no media, post, or notification and a workflow record of `STARTED`.

## Why metadata failure and notification failure recover differently

Follow the same publishing steps through two possible failure points:

**Metadata failure branch:** media storage succeeds and records M42, so workflow state becomes `MEDIA_STORED`. Metadata creation then fails permanently. The orchestrator records `CLEANUP_PENDING`, verifies M42 has no valid references, and requests deletion. If deletion succeeds it records `COMPENSATED`; media is absent and no post was published.

**Notification failure branch:** using the same initial state and business steps, media and metadata both succeed. The state becomes `PUBLISHED`. Notification fails. The orchestrator records `NOTIFICATION_PENDING` and retries later. Post 42 remains published because the contract explicitly permits delayed notification.

| Failure point | Durable completed work | Required next action | Wrong reaction |
|---|---|---|---|
| Metadata cannot be created | Media M42 | Safe cleanup of unreferenced media | Pretend the file vanished with a database rollback |
| Notification cannot be delivered | Media and valid post | Retry notification | Delete the published post automatically |
| Cleanup itself fails | Unreferenced media | Keep recovery pending and retry/escalate | Mark the workflow fully compensated |

A crash can occur between a remote effect and recording the next state. Therefore each step also needs safe repetition or a way to discover its prior result. [Lesson 14](./14-idempotency-and-delivery.md) explains that identity/deduplication boundary. A durable orchestrator alone does not make external effects exactly once.

## Undoing is another fallible business action

An earlier step may be visible to other users. Later actions may depend on it. Cleanup can fail, and a resource may become referenced before deletion. Recovery must verify current business state and ownership rather than undoing blindly. An operation sometimes requires manual resolution instead of a clean reversal.

A travel booking is a useful analogy: cancel an earlier reservation when a required later booking fails, while accepting that a cancellation can have a fee. The analogy stops where services must persist recovery state, correlate duplicate messages, and survive a coordinator restart.

The [Azure saga pattern description](https://learn.microsoft.com/en-us/azure/architecture/patterns/saga) distinguishes local transactions, compensation, and coordination approaches. A different approach, **choreography**, lets services react to events rather than using one central controller. It trades a central workflow view for distributed responsibility; it does not remove the need for durable recovery.

## Choice and project mapping

Use a saga when services must commit independently and the business can tolerate intermediate states and compensating recovery. Prefer one local transaction for work in one database; prefer a true atomic protocol only when supported and required. Sagas introduce state machines, pending work, duplicate messages, repair jobs, and escalation paths.

[MediaStorageService.store](../../../../../src/main/java/com/instagram/backend/service/MediaStorageService.java) writes media; [PostService.create](../../../../../src/main/java/com/instagram/backend/service/PostService.java) stores metadata. No durable workflow or notification step is implemented by those methods. F-030 processing and future notification work are possible implementation follow-ups, not completed features.

## Cleanup fails too — paper exercise

Media M42 was stored, metadata failed permanently, and deletion is temporarily unavailable. Predict which workflow state should remain.

An old `CLEANUP_PENDING` record alongside unreferenced M42 is unfinished recovery, not successful compensation. Keep invalid metadata from being published, but also check whether M42 has acquired a legitimate reference before deleting it.

Retry cleanup using its operation identity, verify actual absence and the reference rule, then record compensation. Escalate if those facts cannot be established. Persist progress and test crashes on both sides of each remote-effect/state-record boundary. Alerting on aged pending work makes the remaining business inconsistency visible; calling the workflow a saga does not make that inconsistency disappear.

## Before moving on

Artifact: `READY`; comprehension: `TODO`.

Explain the contract, derive the two different recovery actions, and state why compensation is a new fallible operation. Predict what survives an orchestrator crash.

## Questions and explained answers

<details>
<summary>1. Why does notification failure not delete post 42 here?</summary>

The contract defines publication at metadata commit and allows notification later. Retrying the optional downstream effect preserves a valid post; reversing it would violate the chosen business behavior.

</details>

<details>
<summary>2. Why is cleanup not equivalent to rolling back media storage?</summary>

Storage already committed and the file may have been observed or referenced. Cleanup is another operation with its own checks and failures. The original committed step was not atomically erased.

</details>

<details>
<summary>3. What must remain durable while cleanup is unavailable?</summary>

The workflow identity, completed steps, M42's identity, and pending recovery intent. Without them a restart can leave an orphan file with no reliable account of why it exists or whether deletion is safe.

</details>
