# Micro-lesson 13 — Sagas: recover business steps that already committed

[Module](./README.md) · [Previous: two-phase commit](./12-distributed-transactions-and-2pc.md) · [Next: idempotency and delivery](./14-idempotency-and-delivery.md)

- Artifact: `READY`; comprehension: `TODO`.
- Core study: 10–15 minutes. Primary idea: separate atomic rollback from deliberate business recovery.
- Prerequisites: local commits persist; 2PC requires cooperating participants and can block.
- Core terms: saga, compensation, orchestrator.

## What if one atomic transaction is unavailable? — Must understand

Publishing a post may span media storage, database metadata, and notification delivery. Those systems may not share a transaction protocol. Keeping a database transaction open while waiting for a notification provider also creates long waits and does not automatically make the provider transactional.

A **saga** is a workflow of locally committed steps with rules for retrying or counteracting earlier steps when the overall work cannot continue. “Saga” is a name, not an acronym requiring expansion. Individual steps are durable; intermediate states can be visible.

**Compensation** is a new business action that counteracts a completed step. It is not a database rollback of the original commit. Deleting unreferenced media is a possible compensation; an email someone already read cannot be unread by rolling back a row.

An **orchestrator** durably tracks workflow state and chooses the next step or recovery action. The durable part matters: a Java call stack disappears on process failure. Tracking only in memory can leave completed work with no record of what remains.

## Define the business outcome first

Fixed publish workflow for post 42:

1. Store media M42.
2. Create post metadata referring to M42.
3. Notify a follower.

Chosen contract: a post is successfully published when its metadata commits with an existing media reference. Notification may arrive later. If metadata creation fails permanently, clean up the unreferenced media after checking it is safe to delete. These are proposed requirements; other products can choose different rules.

This definition prevents a common mistake: reversing every step automatically when *any* later step fails. A failed optional notification does not make a valid published post invalid.

## Trace the same workflow through two failure points

Initial state: no M42, no post 42, no notification, workflow record `STARTED`.

**Metadata failure branch:** media storage succeeds and records M42, so workflow state becomes `MEDIA_STORED`. Metadata creation then fails permanently. The orchestrator records `CLEANUP_PENDING`, verifies M42 has no valid references, and requests deletion. If deletion succeeds it records `COMPENSATED`; media is absent and no post was published.

**Notification failure branch:** using the same initial state and business steps, media and metadata both succeed. The state becomes `PUBLISHED`. Notification fails. The orchestrator records `NOTIFICATION_PENDING` and retries later. Post 42 remains published because the contract explicitly permits delayed notification.

| Failure point | Durable completed work | Required next action | Wrong reaction |
|---|---|---|---|
| Metadata cannot be created | Media M42 | Safe cleanup of unreferenced media | Pretend the file vanished with a database rollback |
| Notification cannot be delivered | Media and valid post | Retry notification | Delete the published post automatically |
| Cleanup itself fails | Unreferenced media | Keep recovery pending and retry/escalate | Mark the workflow fully compensated |

A crash can occur between a remote effect and recording the next state. Therefore each step also needs safe repetition or a way to discover its prior result. [Lesson 14](./14-idempotency-and-delivery.md) explains that identity/deduplication boundary. A durable orchestrator alone does not make external effects exactly once.

## Why compensation is imperfect

An earlier step may be visible to other users. Later actions may depend on it. Cleanup can fail, and a resource may become referenced before deletion. Recovery must verify current business state and ownership rather than undoing blindly. An operation sometimes requires manual resolution instead of a clean reversal.

A travel booking is a useful analogy: cancel an earlier reservation when a required later booking fails, while accepting that a cancellation can have a fee. The analogy stops where services must persist recovery state, correlate duplicate messages, and survive a coordinator restart.

The [Azure saga pattern description](https://learn.microsoft.com/en-us/azure/architecture/patterns/saga) distinguishes local transactions, compensation, and coordination approaches. A different approach, **choreography**, lets services react to events rather than using one central controller. It trades a central workflow view for distributed responsibility; it does not remove the need for durable recovery.

## Choice and project mapping

Use a saga when services must commit independently and the business can tolerate intermediate states and compensating recovery. Prefer one local transaction for work in one database; prefer a true atomic protocol only when supported and required. Sagas introduce state machines, pending work, duplicate messages, repair jobs, and escalation paths.

[MediaStorageService.store](../../../../../src/main/java/com/instagram/backend/service/MediaStorageService.java) writes media; [PostService.create](../../../../../src/main/java/com/instagram/backend/service/PostService.java) stores metadata. No durable workflow or notification step is implemented by those methods. F-030 processing and future notification work are possible implementation follow-ups, not completed features.

## Failure drill — paper exercise

- **Predict:** media succeeds, metadata fails permanently, and deletion is temporarily unavailable. What state should remain?
- **Detect:** an old `CLEANUP_PENDING` record and an unreferenced M42, not a fictitious success or rollback.
- **Contain:** prevent publication of invalid metadata and prevent unsafe deletion of media now referenced elsewhere.
- **Recover:** retry the identity-aware cleanup, verify actual absence and no live references, then record compensation. Escalate if the invariant cannot be established.
- **Prevent:** persist progress, test crashes around every remote effect/state-record boundary, and alert on aged recovery work. Temporary business inconsistency remains a deliberate cost.

## Recap and stop/go

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
