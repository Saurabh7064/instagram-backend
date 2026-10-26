# Micro-lesson 12 — Two-phase commit: one outcome across transaction participants

[Module](./README.md) · [Previous: fenced ownership](./11-split-brain-and-locks.md) · [Next: sagas](./13-sagas.md)

- Artifact: `READY`; comprehension: `TODO`.
- Core study: 10–15 minutes. Primary idea: an atomic final outcome can require waiting for a durable decision after failure.
- Prerequisite: a local transaction groups changes into a commit/rollback unit; a missing reply does not prove rollback.
- Core terms: distributed transaction, coordinator, prepared state.

## Why two independent commits leave a gap — Must understand

A proposed workflow writes post metadata in database A and reserves a media record in database B. The business requirement says either both changes commit or neither does. Committing A and then B is unsafe: if B rejects its change after A commits, the final state contains only half the required outcome.

A **distributed transaction** coordinates one atomic outcome across multiple participants. **Atomic** means the required changes belong to one all-commit or all-abort decision, not that all machines update at the exact same physical instant.

A **coordinator** records and distributes the decision. Each participant must support the protocol and retain enough state to recover. An arbitrary file-write method or remote web request cannot be enlisted simply because an outer Java method has a transaction annotation.

## Expand and explain 2PC

**2PC means Two-Phase Commit.** The phases separate “can everyone commit?” from “what is the final decision?”

**Phase 1 — prepare.** The coordinator asks every required participant to get ready. A participant performs its local checks and durably records enough work to finish later. Voting “yes” places it in **prepared state**: it has promised not to independently change its mind while awaiting the final decision. If it cannot prepare, it votes “no.” Locks or other resources may remain held.

**Phase 2 — decide and notify.** If all required participants voted yes, the coordinator durably records commit and sends that decision. Otherwise it records abort and tells prepared participants to roll back. Durable recording precedes reporting the final decision; a crashed coordinator must not recover a different answer.

This protocol supplies an atomic outcome across participating resources; it does not independently supply serializable isolation or make external readers on separate paths observe every participant simultaneously.

## Fixed failure: the coordinator stops halfway through notification

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

The temporary `committed/prepared` state is precisely why recovery matters. It is not permission to choose a mixed final outcome. Reads and locks still need the databases' isolation machinery; 2PC alone is not a universal visibility contract.

Why can B not time out and roll back at step 4? Its silence is compatible with a committed decision already delivered to A. Guessing abort would make the final outcome mixed. In classic 2PC, uncertainty can block B until a decision can be recovered. Availability is a cost of preserving the atomic outcome.

## Analogy, limits, and choosing an alternative

Two branches pack reserved goods and promise to follow one supervisor's final instruction. A branch that already promised cannot independently sell the reserved goods while the supervisor is unreachable. The analogy stops at crash-safe logs and database locks, which are essential parts of real recovery.

Choose 2PC when all participants support it, the atomicity requirement is real, and blocking/operational costs are acceptable. Prefer one local database transaction if the data can live together. If separate services must commit independently and temporary states are acceptable, [sagas](./13-sagas.md) use business recovery instead. A saga does not provide the same atomicity promise.

PostgreSQL documents [PREPARE TRANSACTION](https://www.postgresql.org/docs/current/sql-prepare-transaction.html), later commit/rollback commands, and the risks of leaving prepared transactions unresolved. Its support is a participant capability, not a complete coordinator implementation for this project.

## Project mapping

[PostService.create](../../../../../src/main/java/com/instagram/backend/service/PostService.java) saves metadata in its configured database transaction. [MediaStorageService.store](../../../../../src/main/java/com/instagram/backend/service/MediaStorageService.java) writes a file separately. A/B are a proposed two-database exercise. The current file method is not a 2PC participant, and no durable coordinator is implemented here.

## Failure drill — paper exercise

- **Predict:** at step 4, should B release its prepared work by guessing abort?
- **Detect:** an old prepared transaction, blocked conflicting work, and a missing coordinator notification reveal the problem.
- **Contain:** preserve the unresolved state and stop unsafe manual decisions. Escalate the blockage rather than inventing an outcome.
- **Recover:** obtain the durable decision through the coordinator's recovery procedure, deliver commit, and verify both participants finish T42.
- **Prevent:** monitor prepared age, retain coordinator records, and rehearse crashes before/after the decision record. A generic timeout is not an authoritative abort record.

## Recap and stop/go

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
