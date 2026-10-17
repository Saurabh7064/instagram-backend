# 11. Conditional updates and lost writes

## Question

How do two editors avoid silently overwriting each other?

[Module sequence](README.md) · Estimated time: 15–20 minutes · Artifact: READY · Learning: TODO

## What you will learn

Explain the problem behind conditional updates and lost writes, trace its externally visible behavior, and identify one implementation risk before proposing a change.

Prerequisites: Lesson 10 and its stop/go check. All later lessons remain TODO until their own checkpoints are attempted.

## Must understand: problem and model

Two clients can read the same post and each write a replacement. A version identifies the revision read. An ETag is an HTTP representation validator; If-Match requires a matching current validator before updating. Like editing a signed document revision, the check asks whether you started from the current copy; unlike a document stamp, comparison and mutation must be atomic.

## Spring Boot example

Unless explicitly described as existing code, this is a proposed teaching excerpt, not an endpoint implemented by this documentation change. Application-specific collaborators and types require implementation; imports and class wiring are omitted. Preserve authentication when adapting an excerpt. This project uses Spring Boot 4.0.2 and Java 24, as recorded in [build configuration](../../../../build.gradle).

```java
@PutMapping("/posts/{id}")
ResponseEntity<PostView> update(
        @PathVariable Long id,
        @RequestHeader("If-Match") String expected,
        @Valid @RequestBody UpdatePostRequest body) {
    var updated = versionedPosts.updateOwned(id, viewerId(), expected, body);
    return ResponseEntity.ok().eTag(updated.etag()).body(updated.view());
}
```

### Walkthrough

This proposed service must evaluate a strong validator and update within concurrency control, such as a database version column with optimistic locking. Checking expected then doing an unconditional update leaves a race. The response ETag must describe the representation returned; real If-Match parsing must support the chosen documented header grammar.

## Request trace: predict, act, observe, explain

Before following the trace, predict the status and final stored state. Then trace these transitions in order:

A and B read version 7 with ETag "v7". A writes with If-Match "v7" -> atomic update to 8. B submits "v7" -> 412 -> no overwrite. B reloads version 8, shows a comparison, and explicitly resubmits a resolved edit.

The observable result includes the response **and** the final stored state. A successful status alone cannot prove concurrency or authorization correctness.

## Project mapping and practice

Read the actual [backend implementation](../../../../src/main/java/com/instagram/backend/domain/Post.java) before changing it. The example above distinguishes existing behavior from a proposed extension; a linked file establishes the current starting point, not proof that the proposed design exists.

**Practice task:** For F-014 propose a version migration, read ETag, If-Match write requirement, and stale-edit UX before changing the endpoint.

Keep this a design exercise until a feature change is explicitly undertaken. An implementation must follow the repository integration-test, API verification, documentation, and commit workflow.

## Useful later: choices and costs

Choose conditional updates when silent loss is unacceptable. Last-write-wins is simpler but discards earlier changes. A stale precondition maps to 412; an absent required precondition can use 428. The current entity and update route do not implement this protocol. @Version alone does not define the HTTP contract.

## Controlled failure drill

Predict two updates after both read version 7. Without a check, the later write hides the first. Detect a two-client integration test, contain with conflict UI, recover by reloading and merging, prevent with atomic version enforcement and stale-write tests.

Describe the prediction before reading the symptom. In a future lab, use disposable data, record the observed response and state, and verify recovery rather than assuming it succeeded.

## Interview-ready answer outline

For a two-minute answer, start with the caller’s problem, define the main concept in plain language, explain the request trace above, then justify one choice and its cost. Finish by explaining how the failure drill would be detected and prevented in this backend. Do not claim the proposed extension is deployed.

## Stop/go and learner evidence

Proceed only after explaining the request trace without notes, predicting the failure, and answering all three checks below before opening their answers. Record the attempt in [learner progress](learner-progress.md). Artifact readiness does not establish comprehension or implementation readiness.

## Questions and explained answers

<details>
<summary>1. What should happen when two clients submit If-Match "v7"?</summary>

Only one update may atomically advance version 7 to 8. The other stale write fails with 412 and leaves the first edit intact. A separate read-compare-unconditional-write sequence is racy.

</details>

<details>
<summary>2. What exact response or state should the worked request produce, and why?</summary>

A and B read version 7 with ETag "v7". A writes with If-Match "v7" -> atomic update to 8. B submits "v7" -> 412 -> no overwrite. B reloads version 8, shows a comparison, and explicitly resubmits a resolved edit.

This follows from the request boundary and state checks in the walkthrough, not merely from choosing a URL.

</details>

<details>
<summary>3. Which failure can remain despite a successful happy-path request, and how would you prevent it?</summary>

Predict two updates after both read version 7. Without a check, the later write hides the first. Detect a two-client integration test, contain with conflict UI, recover by reloading and merging, prevent with atomic version enforcement and stale-write tests.

A successful request proves one case. The controlled failure needs its own observation and regression check.

</details>
