# 10. Idempotency keys for create requests

## Question

How do you retry a create request after a lost response?

[Module sequence](README.md) · Estimated time: 15–20 minutes · Artifact: READY · Learning: TODO

## What you will learn

Explain the problem behind idempotency keys for create requests, trace its externally visible behavior, and identify one implementation risk before proposing a change.

Prerequisites: Lesson 9 and its stop/go check. All later lessons remain TODO until their own checkpoints are attempted.

## Must understand: problem and model

A server may commit a post and lose the response, leaving the caller unsure whether retrying creates a duplicate. An idempotency key identifies one logical attempt across retries. A request fingerprint records the input associated with that key. Like a claim ticket, it retrieves the original result; unlike paper tickets, concurrent claims need atomic database coordination.

## Spring Boot example

Unless explicitly described as existing code, this is a proposed teaching excerpt, not an endpoint implemented by this documentation change. Application-specific collaborators and types require implementation; imports and class wiring are omitted. Preserve authentication when adapting an excerpt. This project uses Spring Boot 4.0.2 and Java 24, as recorded in [build configuration](../../../../build.gradle).

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

## Project mapping and practice

Read the actual [backend implementation](../../../../src/main/java/com/instagram/backend/service/PostService.java) before changing it. The example above distinguishes existing behavior from a proposed extension; a linked file establishes the current starting point, not proof that the proposed design exists.

**Practice task:** Extend F-010 with durable key replay as a proposed follow-up; test simultaneous identical keys and mismatched payloads.

Keep this a design exercise until a feature change is explicitly undertaken. An implementation must follow the repository integration-test, API verification, documentation, and commit workflow.

## Useful later: choices and costs

Use keys where retrying POST would duplicate an important effect. Storage, retention policy, and in-flight recovery add cost. Define what happens after expiration. Replaying a database result does not undo an already-sent external notification; use a transactionally recorded outgoing event and downstream deduplication for external effects.

## Controlled failure drill

Predict process death after post commit but before a separately committed key record. Retry creates a duplicate. Detect a controlled crash test and duplicate logical attempts, contain automatic retries, recover by reconciling attempts, prevent with one atomic transaction and concurrency tests.

Describe the prediction before reading the symptom. In a future lab, use disposable data, record the observed response and state, and verify recovery rather than assuming it succeeded.

## Interview-ready answer outline

For a two-minute answer, start with the caller’s problem, define the main concept in plain language, explain the request trace above, then justify one choice and its cost. Finish by explaining how the failure drill would be detected and prevented in this backend. Do not claim the proposed extension is deployed.

## Stop/go and learner evidence

Proceed only after explaining the request trace without notes, predicting the failure, and answering all three checks below before opening their answers. Record the attempt in [learner progress](learner-progress.md). Artifact readiness does not establish comprehension or implementation readiness.

## Questions and explained answers

<details>
<summary>1. Can a unique key record alone guarantee safe create replay?</summary>

No. Its scope, request fingerprint, and replay result must be correct, and the record and business write must commit atomically. Otherwise a crash can leave a post without a replay record and a retry creates a duplicate.

</details>

<details>
<summary>2. What exact response or state should the worked request produce, and why?</summary>

User 7 sends key K with caption hi -> transaction creates post 42 and stores replay -> response lost. Retry K with hi -> stored 201 and post 42 replayed. Retry K with bye -> fingerprint differs -> documented 409, no new post. Two simultaneous K requests must yield one committed effect.

This follows from the request boundary and state checks in the walkthrough, not merely from choosing a URL.

</details>

<details>
<summary>3. Which failure can remain despite a successful happy-path request, and how would you prevent it?</summary>

Predict process death after post commit but before a separately committed key record. Retry creates a duplicate. Detect a controlled crash test and duplicate logical attempts, contain automatic retries, recover by reconciling attempts, prevent with one atomic transaction and concurrency tests.

A successful request proves one case. The controlled failure needs its own observation and regression check.

</details>
