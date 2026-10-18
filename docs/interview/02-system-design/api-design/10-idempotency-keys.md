# 10. Idempotency keys for create requests

## Question

How do you retry a create request after a lost response?

[Module sequence](README.md) · Study in two passes: core model, then design decisions (15–20 minutes each) · Artifact: READY · Learning: TODO

## What you will learn

Design scoped attempt keys, request fingerprints, durable replay states, atomic commit, crash recovery, retention, and outgoing event handling.

Prerequisites: Lesson 9 and its stop/go check. All later lessons remain TODO until their own checkpoints are attempted.

## Must understand: problem and model

A server may commit a post and lose the response, leaving the caller unsure whether retrying creates a duplicate. An idempotency key identifies one logical attempt across retries. A request fingerprint records the input associated with that key. Like a claim ticket, it retrieves the original result; unlike paper tickets, concurrent claims need atomic database coordination.

## Spring Boot example

Examples are proposed except where explicitly identified as current code. Imports/wiring are omitted; named new services and DTOs need implementation. Preserve current authorization rules. The project uses [Spring Boot 4.0.2 and Java 24](../../../../build.gradle).

```java
@PostMapping("/posts")
ResponseEntity<FeedPostResponse> create(
        @RequestHeader("Idempotency-Key") String key,
        @Valid @RequestBody CreatePostRequest request) {
    var result = idempotentPosts.create(viewerId(), key, request);
    return ResponseEntity.status(result.status()).body(result.body());
}
```

### Walkthrough

This proposed handler delegates the hard part. The service must scope keys by authenticated user and operation, compare canonical request fingerprints, and atomically commit the post with its replay result. Use a unique database constraint to arbitrate concurrent attempts. A HashMap or check-then-insert does not supply this guarantee across instances.

## Request trace: predict, act, observe, explain

Before following the trace, predict the status and final stored state. Then trace these transitions in order:

User 7 sends key K with caption hi -> transaction creates post 42 and stores replay -> response lost. Retry K with hi -> stored 201 and post 42 replayed. Retry K with bye -> fingerprint differs -> documented 409, no new post. Two simultaneous K requests must yield one committed effect.

The observable result includes the response **and** the final stored state. A successful status alone cannot prove concurrency or authorization correctness.

## Durable state machine for one logical attempt

A proposed table key is `(actor_id, operation, idempotency_key)`. Store request fingerprint, state, response status/body or a durable resource pointer, creation time, and expiry. A unique constraint decides which concurrent request owns a key. Use a bounded, validated key length; never let callers create unlimited retained records.

| Observed key state | Same request fingerprint | Different fingerprint |
|---|---|---|
| Absent | Atomically claim and execute | Same initial claim rule |
| In progress | Wait for bounded time or documented conflict/pending | Reject reuse |
| Completed | Replay promised outcome | Reject, e.g. documented 409 |
| Expired | Defined policy; may count as a new attempt | Same expiry policy |

“In progress” stored in a separate transaction needs lease/owner/crash recovery semantics. A simpler local-database design keeps claim, post insert, and replay result in one transaction; competitors may wait for commit or observe a uniqueness conflict. Do not catch a constraint exception inside a rollback-only transaction and continue querying as if it were healthy. Recover outside that transaction or use a deliberate database conflict-handling strategy.

### Which bytes identify the request?

Raw JSON bytes differ for `{"caption":"hi","location":"Home"}` and the same fields in reverse order. Decide whether the fingerprint hashes raw bytes or a canonical validated request. Canonicalization needs rules for absent/null, whitespace normalization, defaults, and operation/version. Hash only after those semantics are defined; never use a body hash alone as the caller’s key because legitimate separate identical posts can exist.

### Commit and response-loss trace

1. T1 claims (user7,createPost,K), inserts post42, writes replay outcome, then commits.
2. The TCP response is lost. This does not undo the transaction.
3. T2 repeats the same scoped key and fingerprint. It finds completed state and returns the documented original outcome without creating post43.
4. If T1 instead crashes before commit, the transaction rolls back; T2 can claim and create once.
5. After retention expires, a retry might create a new post. Tell clients the maximum supported retry window; “exactly once forever” is not a justified guarantee.

Authorization still runs for retries, and replay storage must not leak another user’s result. Do not persist live credential headers as part of a replay response. Define whether replay returns the original snapshot or current resource; they differ if the post is edited later. Return stable headers such as original Location according to policy.

For notifications, insert an outgoing event record in the same database transaction as the post. A publisher sends that event later and records progress; this is the transactional outbox pattern. Delivery can repeat, so consumers deduplicate by stable event ID. It closes a database/event dual-write gap, not every possible distributed exactly-once problem.

## Project mapping and practice

Read the actual [backend implementation](../../../../src/main/java/com/instagram/backend/service/PostService.java) before changing it. The example above distinguishes existing behavior from a proposed extension; a linked file establishes the current starting point, not proof that the proposed design exists.

**Practice task:** Extend F-010 with durable key replay as a proposed follow-up; test simultaneous identical keys and mismatched payloads.

Keep this a design exercise until a feature change is explicitly undertaken. An implementation must follow the repository integration-test, API verification, documentation, and commit workflow.

## Useful later: choices and costs

Use keys where retrying POST would duplicate an important effect. Storage, retention policy, and in-flight recovery add cost. Define what happens after expiration. Replaying a database result does not undo an already-sent external notification; use a transactionally recorded outgoing event and downstream deduplication for external effects.

## Controlled failure drill

Predict process death after post commit but before a separately committed key record. Retry creates a duplicate. Detect a controlled crash test and duplicate logical attempts, contain automatic retries, recover by reconciling attempts, prevent with one atomic transaction and concurrency tests.

Describe the prediction before reading the symptom. In a future lab, use disposable data, record the observed response and state, and verify recovery rather than assuming it succeeded.

## Interview-ready answer

I add idempotency to create when an uncertain response must not duplicate a logical attempt. I scope the key by actor and operation, associate a documented request fingerprint, and commit the effect with replay state atomically. A unique constraint arbitrates competing claims; transaction/error handling must be deliberate. I define in-flight and expired behavior and keep authentication on replay. The current create route has no such protocol. A database transaction does not atomically deliver external notifications, so I use an outgoing event record and downstream deduplication where required. I test commit-before-response-loss, crash-before-commit, simultaneous keys, mismatched payloads, and retries after retention.

## Stop/go and learner evidence

Proceed only after explaining the request trace without notes, predicting the failure, and answering all three checks below before opening their answers. Record the attempt in [learner progress](learner-progress.md). Artifact readiness does not establish comprehension or implementation readiness.

## Questions and explained answers

<details>
<summary>1. Can a unique key record alone guarantee safe create replay?</summary>

No. Its scope, request fingerprint, and replay result must be correct, and the record and business write must commit atomically. Otherwise a crash can leave a post without a replay record and a retry creates a duplicate.

</details>

<details>
<summary>2. Should identical JSON property order changes always cause key reuse conflict?</summary>

Only if the contract fingerprints raw bytes. A canonical validated request can treat them as equivalent, but canonicalization must define defaults, null, normalization, and version first.

</details>

<details>
<summary>3. Why avoid catching a uniqueness exception and continuing inside the same failed transaction?</summary>

The transaction may be rollback-only or the database may reject further work. Handle it outside the failed boundary or use a deliberate conflict-handling strategy.

</details>

<details>
<summary>4. What guarantee changes after an attempt key expires?</summary>

The provider may treat a repeat as a new attempt under its retention policy. Safe replay is bounded by the documented window, not guaranteed forever.

</details>

<details>
<summary>5. What does an outbox solve, and what does it not solve?</summary>

It commits the business change and an outgoing event record together, avoiding a local database/event dual-write gap. Publishing and delivery can still repeat, so consumers need stable event deduplication.

</details>
