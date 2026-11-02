# 10. Idempotency keys for create requests

## Question

How do you retry a create request after a lost response?

[Module sequence](README.md) · Artifact: READY · Learning: TODO

## What you will learn

Follow one create through a lost response and retry. Work out what must be stored together to replay the result without creating a second post.

Read the [POST retry discussion](02-http-methods.md) first. The later storage and event details build on the one-attempt model.

## The post exists, but the phone never got the answer

User 7 submits a new post. The server saves it as post 42 and commits. Just then the connection drops, so the phone never receives `201 Created`. From the phone's point of view, this looks much like a request that never reached the server.

If it simply repeats a normal POST, the server may create another post. Comparing the caption is not a sound fix: a person may intentionally publish identical content twice. We need a way for the client to say, “this is another delivery of the same attempt.”

The client attaches an **idempotency key**, such as `K`, and keeps it for that logical attempt. The server remembers which request and result belong to that key. A **request fingerprint** is a recorded summary of the relevant input, used to detect someone reusing `K` for different content. A retry with the same input can retrieve post 42's result; new content with the same key should be rejected under the documented policy.

Think of a claim ticket that retrieves one completed order. The comparison stops at concurrency and crashes: two server instances can receive the same ticket together, and durable storage must decide which one may create the post.

[HTTP methods](02-http-methods.md) explain why POST does not supply this replay guarantee itself. Adding a header gives us a label; we still have to build the guarantee behind it.

## Carry the attempt identity into the service

The project uses [Spring Boot 4.0.2 and Java 24](../../../../build.gradle). The excerpt below is a design proposal unless identified as current code; omitted imports and collaborators still need implementation.

```java
@PostMapping("/posts")
ResponseEntity<FeedPostResponse> create(
        @RequestHeader("Idempotency-Key") String key,
        @Valid @RequestBody CreatePostRequest request) {
    var result = idempotentPosts.create(viewerId(), key, request);
    return ResponseEntity.status(result.status()).body(result.body());
}
```

### Read the boundary in the code

The header carries the attempt identity and `viewerId()` stands for an identity resolved from valid credentials. `idempotentPosts` is a proposed service whose result supplies the documented status and body; merely reading the header does not deduplicate anything.

That service must scope keys by user and operation, compare the request fingerprint, and atomically commit the post with its replay record. A unique database rule coordinates competing requests across instances. A Java map or separate existence check cannot provide the same crash and concurrency guarantee.

## Retry K without publishing again

Start with no record for user 7's create attempt `K`. The first request carries caption `hi` and valid values for the other required fields. One database transaction creates post 42 and records the input fingerprint plus replay result for `K`. It commits; the response is then lost.

The client repeats `K` with the same validated input. The server finds the completed attempt and returns the promised result for post 42 without creating post 43. If the client instead reuses `K` with caption `bye`, the fingerprint differs. Under the proposed 409 policy, reject that reuse and leave the original post intact.

Two simultaneous deliveries of `K` must reach that same one-post outcome. A preliminary “does K exist?” lookup is insufficient, because both deliveries can observe absence. The unique key and transaction design below have to arbitrate the race.

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

Raw JSON bytes differ when the same caption `hi`, imageUrl, and locationLabel properties appear in a different order. Decide whether the fingerprint hashes raw bytes or a canonical validated request. Canonicalization needs rules for absent/null, whitespace normalization, defaults, and operation/version. Hash only after those semantics are defined; never use a body hash alone as the caller’s key because legitimate separate identical posts can exist.

### Commit and response-loss trace

1. T1 claims (user7,createPost,K), inserts post42, writes replay outcome, then commits.
2. The response is lost. This does not undo the transaction.
3. T2 repeats the same scoped key and fingerprint. It finds completed state and returns the documented original outcome without creating post43.
4. If T1 instead crashes before commit, the transaction rolls back; T2 can claim and create once.
5. After retention expires, a retry might create a new post. Tell clients the maximum supported retry window; “exactly once forever” is not a justified guarantee.

Authorization still runs for retries, and replay storage must not leak another user’s result. Do not persist live credential headers as part of a replay response. Define whether replay returns the original snapshot or current resource; they differ if the post is edited later. Return stable headers such as original Location according to policy.

### Useful later: a notification is another effect

For notifications, insert an outgoing event record in the same database transaction as the post. A publisher sends that event later and records progress; this is the transactional outbox pattern. Delivery can repeat, so consumers deduplicate by stable event ID. It closes a database/event dual-write gap, not every possible distributed exactly-once problem.

## Apply this to the backend

[PostService.create](../../../../src/main/java/com/instagram/backend/service/PostService.java) currently creates a post on each accepted call. There is no durable attempt-key protocol, replay table, or outbox in that method.

As a proposed follow-up to F-010, design the scoped record and commit boundary. Include simultaneous identical keys, same key with different payloads, crash before commit, and lost response after commit. For each, write the expected number of committed posts before implementing a test.

## Useful later: choices and costs

Use keys where retrying POST would duplicate an important effect. Storage, retention policy, and in-flight recovery add cost. Define what happens after expiration. Replaying a database result does not undo an already-sent external notification; use a transactionally recorded outgoing event and downstream deduplication for external effects.

## Crash between the post and a separate replay record

On paper, put post creation in one transaction and replay recording in another. Let the first commit post 42, then kill the process before the second commits key K. Predict the retry: K appears absent, so a naive implementation creates post 43.

A future controlled crash test should detect two committed posts for one attempt. Contain automatic retries of the affected operation until the stored attempts can be reconciled; do not blindly delete content that might have independent meaning. Recover by reconciling attempt and post records under a reviewed policy, then make the business write and replay record one atomic commit. Verify both pre-commit and post-commit crash cases. This proposed guarantee remains bounded by retention, rather than an “exactly once forever” claim.

## Interview-ready answer

I add idempotency to create when an uncertain response must not duplicate a logical attempt. I scope the key by actor and operation, associate a documented request fingerprint, and commit the effect with replay state atomically. A unique constraint arbitrates competing claims; transaction/error handling must be deliberate. I define in-flight and expired behavior and keep authentication on replay. The current create route has no such protocol. A database transaction does not atomically deliver external notifications, so I use an outgoing event record and downstream deduplication where required. I test commit-before-response-loss, crash-before-commit, simultaneous keys, mismatched payloads, and retries after retention.

## Before moving on

Walk K through commit, lost response, and retry. Continue when you can place the post and replay state in one commit and explain the race, changed-payload conflict, and retention boundary.

Attempt the questions before opening their answers and record the explanation in [learner progress](learner-progress.md). This revision leaves learning status unchanged; a finished document is not evidence that you have demonstrated the idea.

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
