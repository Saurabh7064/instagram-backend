# Micro-lesson 11 — Distributed locks: why an expired owner can still write

[Module](./README.md) · [Previous: Paxos](./10-paxos.md) · [Next: distributed transactions](./12-distributed-transactions-and-2pc.md)

- Artifact: `READY`; comprehension: `TODO`.
- Core study: 10–15 minutes. Primary idea: protect the resource from an obsolete owner, not just the lock record.
- Prerequisites: a timeout does not prove a process stopped; leader replacement must constrain old authority.
- Core terms: split brain, distributed lock, fencing token.

## Two workers, one output — Must understand

Suppose media-processing workers produce the final thumbnail for post 42. We want one authorized worker at a time to replace the output. A worker can pause because its machine is overloaded, then resume after another worker has taken over. How do we prevent its late write from overwriting newer work?

**Split brain** means multiple participants act as authoritative owners at the same time. A partition can cause it, but so can unsafe takeover after a long pause. Two processes running is not itself the bug; accepting conflicting authority is.

A **distributed lock** coordinates exclusive permission across processes. A Java lock within one backend process cannot protect another process: they do not share that lock object. The distributed lock's ownership record must have a reliable coordination protocol of its own.

To avoid a crashed owner holding work forever, ownership may have a limited lifetime, called a **lease**. Expiration lets someone else acquire ownership. It changes the coordination record; it does not kill or rewind the old worker.

## The missing enforcement boundary

A **fencing token** is an increasing ownership number carried with protected operations. The storage resource tracks accepted authority and rejects operations from older ownership. It is analogous to a machine accepting only the newest numbered keycard. The keycard analogy stops where a real storage system must enforce the comparison atomically with each change.

Tokens need more than uniqueness: ownership 42 must be recognized as newer than 41. A randomly generated identifier may identify an owner but does not establish that order. Also, checking a token in the worker and then writing later leaves a race; the protected resource must enforce the rule with the mutation.

## Trace the stale-owner problem

Fixed setup: workers A/B process the same post 42 thumbnail. A coordination service issues monotonically increasing tokens. Output storage atomically rejects any token lower than its highest accepted token. The highest token stored initially is 40. A/B are well-behaved but can pause; they do not forge tokens.

1. A acquires ownership with token 41. It begins computing output but pauses before writing.
2. A's lease expires. A has not stopped; it is simply not executing while paused.
3. B acquires ownership with token 42 and computes a fresh output.
4. B submits `(42, fresh output)`. Storage checks 42 against highest=40, accepts the operation, and records highest=42 with the output.
5. A resumes and submits `(41, older output)`. Storage evaluates 41<42 and rejects it. The fresh output survives.

| Operation at storage | Highest token before | Check | Output after |
|---|---:|---|---|
| B writes with 42 | 40 | 42 is not older | B's output; highest becomes 42 |
| A writes with 41 | 42 | 41<42 | Unchanged; A is rejected |

Without the storage check, step 5 can overwrite B despite the lock service correctly expiring A and granting B. Therefore a correct lock record alone does not protect an external effect from delayed code.

An important boundary: this “highest accepted token” scheme rejects A once storage has seen the newer authority. If the requirement says A must be rejected immediately after B acquires ownership, even before B's first storage operation, ownership establishment must also register the newer token at the resource or validate current ownership there. State which guarantee the implementation provides.

## What fencing does not solve

Two retries within token 42 can still repeat an effect unless operations have their own duplicate protection. Fencing orders owners, not every request from one owner. Equal-token behavior must be specified. An external service that cannot validate tokens cannot be protected merely by attaching them to a request.

[Redis's distributed-lock guidance](https://redis.io/docs/latest/develop/clients/patterns/distributed-locks/) explicitly discusses fencing and why process lifetime is not evidence that a lock is still retained. This lesson does not claim a specific locking algorithm is safe under all timing assumptions.

## Choice and project mapping

Use fenced ownership for background work whose stale writers can corrupt shared output and whose resource supports enforcement. Prefer a database unique constraint or conditional update when that directly expresses the invariant; it is simpler than coordinating an external lock.

[PostLike's unique constraint](../../../../../src/main/java/com/instagram/backend/domain/PostLike.java) protects one like per user/post inside the database; it is not a distributed worker lock. [MediaStorageService.store](../../../../../src/main/java/com/instagram/backend/service/MediaStorageService.java) writes files without an ownership token check. The processing workers and fenced output boundary are proposed F-030 work, not implemented capabilities. Fencing introduces storage metadata, atomic comparison, and rejection/retry handling.

## Failure drill — paper exercise

- **Predict:** pause A after it computes, let B's token-42 write finish, then resume A. What should storage do?
- **Detect:** observe a stale-token rejection for A and confirm B's output remains. A mere “lock expired” log is insufficient proof.
- **Contain:** block the stale operation at storage; do not let A refresh its token and blindly write its old computation.
- **Recover:** A abandons old work, reacquires legitimate authority if needed, rereads current input, and computes again.
- **Prevent:** test long pauses and network delays; preserve the token-ordering service's state. Document same-owner retries and the point at which new ownership becomes effective at storage.

## Recap and stop/go

Explain why lease expiry cannot stop A and derive both token comparisons. Identify where the check must run, when it begins rejecting old authority, and which duplicate problem remains.

## Questions and explained answers

<details>
<summary>1. If the lock service worked correctly, why could a stale overwrite still happen?</summary>

A was paused, not dead. Correctly expiring its lease permits B to take over but cannot prevent A resuming. Without resource-side enforcement, both can submit accepted writes.

</details>

<details>
<summary>2. Why must storage compare the token with the write rather than the worker checking first?</summary>

Ownership can change between a separate check and mutation. An atomic resource-side comparison decides using the authority state at the moment the effect is accepted.

</details>

<details>
<summary>3. Does accepting token 42 once make all later token-42 operations exactly once?</summary>

No. The token distinguishes ownership generations. Repeated operations within one generation need their own operation identity or idempotent effect; fencing does not deduplicate them.

</details>
