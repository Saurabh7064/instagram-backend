# Micro-lesson 11 — Distributed locks: why an expired owner can still write

[Module](./README.md) · [Previous: Paxos](./10-paxos.md) · [Next: distributed transactions](./12-distributed-transactions-and-2pc.md)

Core study: 10–15 minutes. Prerequisites: a timeout does not prove a process stopped; leader replacement must constrain old authority.

## A's lock expired, but A did not stop — Must understand

Worker A is computing the thumbnail for post 42. It pauses before saving the result. Its ownership expires, so worker B takes over and saves a fresh thumbnail. Then A wakes up and saves its old result over B's work. The ownership service may have followed every expiry rule correctly. The output is still wrong.

This is the gap we need to close: changing the ownership record does not stop instructions already running elsewhere. When multiple participants act as authoritative owners, we call the situation **split brain**. A broken network can cause it, but a long pause is enough too. Merely having two workers is harmless if stale authority cannot change the protected output.

A **distributed lock** coordinates exclusive permission across processes. A Java lock object inside one backend cannot coordinate a second process, which has different memory. A lock with a limited lifetime is a **lease**. Expiry lets B acquire ownership rather than waiting forever for A, but it cannot kill or rewind A.

## Let the output store reject the old owner

Give each new owner a larger number. A gets 41; B gets 42. Every output write carries that number. The output store remembers the highest authority it has accepted and compares the number atomically with the write. Once it has accepted B's 42, it can reject A's 41.

That increasing ownership number is a **fencing token**. Uniqueness alone is insufficient: a random owner ID does not say which owner is newer. Checking the token only in the worker is also insufficient, because ownership can change between that check and its later write. The resource accepting the effect must enforce the comparison.

Think of numbered keycards where a machine stops honoring an older card after seeing the newer one. The limit of the analogy is the mechanism: storage must actually make token comparison and output mutation one atomic decision. Attaching a number without enforcing it changes nothing.

## Watch storage accept 42 and reject 41

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

## What about repeated writes from the current owner?

Two retries within token 42 can still repeat an effect unless operations have their own duplicate protection. Fencing orders owners, not every request from one owner. Equal-token behavior must be specified. An external service that cannot validate tokens cannot be protected merely by attaching them to a request.

[Redis's distributed-lock guidance](https://redis.io/docs/latest/develop/clients/patterns/distributed-locks/) explicitly discusses fencing and why process lifetime is not evidence that a lock is still retained. This lesson does not claim a specific locking algorithm is safe under all timing assumptions.

## Choice and project mapping

Use fenced ownership for background work whose stale writers can corrupt shared output and whose resource supports enforcement. Prefer a database unique constraint or conditional update when that directly expresses the invariant; it is simpler than coordinating an external lock.

[PostLike's unique constraint](../../../../../src/main/java/com/instagram/backend/domain/PostLike.java) protects one like per user/post inside the database; it is not a distributed worker lock. [MediaStorageService.store](../../../../../src/main/java/com/instagram/backend/service/MediaStorageService.java) writes files without an ownership token check. The processing workers and fenced output boundary are proposed F-030 work, not implemented capabilities. Fencing introduces storage metadata, atomic comparison, and rejection/retry handling.

## Resume A after B's token-42 write — paper exercise

Pause A after computation, let B save its fresh output with token 42, then resume A's token-41 write. Predict what the storage resource must do.

Look for the stale-token rejection and verify B's output remains. A “lease expired” message in the lock service proves only that the ownership record changed; the decisive protection is at output storage.

A must abandon the old computation. If it needs to work again, it should reacquire legitimate authority, reread current input, and recompute. Simply obtaining a newer token and attaching it to stale output bypasses the purpose of takeover. Test long pauses and delayed requests, retain the token-ordering service's state, and document same-owner retries plus the exact point where new authority becomes effective at the resource.

## Before moving on

Artifact: `READY`; comprehension: `TODO`.

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
