# Micro-lesson 12 — Two-phase commit: one outcome across transaction participants

[Module](./README.md) · [Previous: fenced ownership](./11-split-brain-and-locks.md) · [Next: sagas](./13-sagas.md)

Core study: 10–15 minutes. Prerequisite: a local transaction groups changes into a commit/rollback unit; a missing reply does not prove rollback.

## One post needs two database changes — Must understand

A proposed publish operation stores post 42's metadata in database A and reserves its media record in database B. The requirement is that both changes commit or neither does. If we commit A first and B then rejects the reservation, we have metadata without the required reservation. Reversing the order only moves the dangerous gap.

A **distributed transaction** coordinates one outcome across participants. The desired **atomicity** is an all-commit or all-abort decision. It does not mean different machines physically update at exactly the same instant.

To close the gap, each database must be able to promise it is ready before anyone announces the final decision. A **coordinator** collects those promises and records the decision. The databases must support this protocol and preserve enough state to recover. An arbitrary HTTP call or file write does not acquire those capabilities from an outer Java transaction annotation.

## First make a promise, then follow the decision

**Two-Phase Commit**, abbreviated **2PC**, separates two questions: “Can everyone commit?” and “What must everyone do?”

In **phase 1, prepare**, the coordinator asks every required participant to get ready. Each performs its local checks and durably retains enough work to finish later. A yes vote puts it in **prepared state**. It has promised to follow the eventual decision rather than changing its mind independently; locks or other resources may remain held. A participant unable to prepare votes no.

In **phase 2, decide and notify**, all yes votes permit the coordinator to durably record commit. Otherwise it records abort. It then tells participants to finish accordingly. Recording before announcing matters: a restart must recover the same decision rather than invent a different answer.

The remaining difficulty is getting that recorded answer to everyone after a failure. A participant that has promised cannot safely replace a missing decision with its own guess. Follow what happens when the coordinator fails halfway through notification.

## A committed; B is still waiting. What happens next?

Initial state: databases A/B contain no metadata/reservation for post 42. Coordinator C has no transaction decision. Both databases support prepare and recover their state durably.

1. C requests preparation. A prepares the metadata insert; B prepares the reservation. Both vote yes.
2. C records `COMMIT` durably for transaction T42. This is the recovery authority for the final outcome.
3. C tells A to commit. A completes its local commit.
4. C crashes before B receives the decision. B is still prepared, retaining its unresolved resources.
5. C's recovery process reads the durable `COMMIT` record and delivers it to B. B commits. Repeated notifications must be handled safely.

| Boundary | A | B | Durable decision at C |
|---|---|---|---|
| After preparation | Prepared | Prepared | None yet |
| After decision record | Prepared | Prepared | COMMIT |
| During notification failure | Committed | Prepared | COMMIT |
| After recovery | Committed | Committed | COMMIT |

2PC coordinates the final outcome; it does not independently provide serializable isolation or simultaneous visibility to readers on separate paths. The temporary `committed/prepared` state is precisely why recovery matters. It is not permission to choose a mixed final outcome. Reads and locks still need the databases' isolation machinery; 2PC alone is not a universal visibility contract.

Why can B not time out and roll back at step 4? Its silence is compatible with a committed decision already delivered to A. Guessing abort would make the final outcome mixed. In classic 2PC, uncertainty can block B until a decision can be recovered. Availability is a cost of preserving the atomic outcome.

## When is holding a promise worth the cost?

Two branches pack reserved goods and promise to follow one supervisor's final instruction. A branch that already promised cannot independently sell the reserved goods while the supervisor is unreachable. The analogy stops at crash-safe logs and database locks, which are essential parts of real recovery.

Choose 2PC when all participants support it, the atomicity requirement is real, and blocking/operational costs are acceptable. Prefer one local database transaction if the data can live together. If separate services must commit independently and temporary states are acceptable, [sagas](./13-sagas.md) use business recovery instead. A saga does not provide the same atomicity promise.

PostgreSQL documents [PREPARE TRANSACTION](https://www.postgresql.org/docs/current/sql-prepare-transaction.html), later commit/rollback commands, and the risks of leaving prepared transactions unresolved. Its support is a participant capability, not a complete coordinator implementation for this project.

## Project mapping

[PostService.create](../../../../../src/main/java/com/instagram/backend/service/PostService.java) saves metadata in its configured database transaction. [MediaStorageService.store](../../../../../src/main/java/com/instagram/backend/service/MediaStorageService.java) writes a file separately. A/B are a proposed two-database exercise. The current file method is not a 2PC participant, and no durable coordinator is implemented here.

## B is blocked. Is guessing abort a safe escape? — paper exercise

C has recorded commit and told A, then disappears before notifying B. Predict whether B may release its prepared work by timing out and rolling back.

An aged prepared transaction, blocked conflicting work, and the missing notification identify the blockage. Preserve B's unresolved state and escalate it rather than inventing a local outcome. Guessing abort could contradict A's completed commit.

Use the coordinator's recovery procedure to obtain the durable decision, deliver commit to B, and verify both participants finish T42. Monitor prepared-transaction age and retain the coordinator records needed to resolve it. Rehearse crashes before and after decision recording; a generic timeout is not evidence of an authoritative abort decision.

## Before moving on

Artifact: `READY`; comprehension: `TODO`.

Expand 2PC, explain why prepare is a durable promise, and walk through C's crash. Compare a local transaction, 2PC, and a saga in terms of outcome and recovery cost.

## Questions and explained answers

<details>
<summary>1. Why not just commit A, then commit B?</summary>

B may fail after A's irreversible local commit. Without a shared outcome protocol, there is no guarantee that the required changes end with the same decision.

</details>

<details>
<summary>2. What makes B's prepared state different from an ordinary unfinished local attempt?</summary>

B has durably voted that it can commit and must honor the recovered final decision. It cannot safely abandon that promise merely because the coordinator is temporarily unreachable.

</details>

<details>
<summary>3. What does the mixed state after step 4 mean?</summary>

Notification is incomplete, but the durable final decision is already COMMIT. B must recover that decision and finish. It is a blocking recovery problem, not permission for a permanently mixed outcome.

</details>
