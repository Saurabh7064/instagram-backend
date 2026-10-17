# 12. Caching and private representations

## Question

When can an API response be cached safely?

[Module sequence](README.md) · Estimated time: 15–20 minutes · Artifact: READY · Learning: TODO

## What you will learn

Explain the problem behind caching and private representations, trace its externally visible behavior, and identify one implementation risk before proposing a change.

Prerequisites: Lesson 11 and its stop/go check. All later lessons remain TODO until their own checkpoints are attempted.

## Must understand: problem and model

Repeated reads waste bandwidth, but caching the wrong viewer’s response can expose private state. Cache-Control sets storage/reuse policy. A validator lets a server check whether a cached representation is still current. Think of a dated photocopy; the analogy stops because shared caches serve multiple people and need explicit isolation.

## Spring Boot example

Unless explicitly described as existing code, this is a proposed teaching excerpt, not an endpoint implemented by this documentation change. Application-specific collaborators and types require implementation; imports and class wiring are omitted. Preserve authentication when adapting an excerpt. This project uses Spring Boot 4.0.2 and Java 24, as recorded in [build configuration](../../../../build.gradle).

```java
@GetMapping("/me")
ResponseEntity<MeResponse> me(
        @RequestHeader("Authorization") String authorization) {
    return ResponseEntity.ok()
            .cacheControl(CacheControl.noStore())
            .body(profileQueries.me(authorization));
}
```

### Walkthrough

This proposed variant demonstrates a sensitive-response policy. no-store instructs caches not to store this response. private permits private-cache storage but prohibits shared-cache storage; it does not itself require freshness validation. profileQueries is illustrative; inspect the existing profile controller/service before implementation.

## Request trace: predict, act, observe, explain

Before following the trace, predict the status and final stored state. Then trace these transitions in order:

User A loads a feed item with likedByViewer=true. A shared cache keyed only by URL gives it to B -> B sees incorrect personal state. With no-store, that response is not retained by compliant caches. A separately designed public representation could be cached without viewer-specific fields.

The observable result includes the response **and** the final stored state. A successful status alone cannot prove concurrency or authorization correctness.

## Project mapping and practice

Read the actual [backend implementation](../../../../src/main/java/com/instagram/backend/dto/FeedPostResponse.java) before changing it. The example above distinguishes existing behavior from a proposed extension; a linked file establishes the current starting point, not proof that the proposed design exists.

**Practice task:** For F-016 classify every feed field as public or viewer-specific before proposing caching; likedByViewer prevents a shared full-response cache.

Keep this a design exercise until a feature change is explicitly undertaken. An implementation must follow the repository integration-test, API verification, documentation, and commit workflow.

## Useful later: choices and costs

Use no-store for sensitive identity/token responses; private caching may help personalized reads with a deliberate freshness policy. Public immutable media can use long lifetimes if access policy allows it and content changes get new URLs. A 304 has no representation body and requires correct validator evaluation for the selected user-specific representation.

## Controlled failure drill

Predict sharing a cached /api/me across tokens. Detect two-user cache tests, contain with no-store and purge, recover exposed cache entries, prevent by separating public and personalized representations and reviewing cache keys.

Describe the prediction before reading the symptom. In a future lab, use disposable data, record the observed response and state, and verify recovery rather than assuming it succeeded.

## Interview-ready answer outline

For a two-minute answer, start with the caller’s problem, define the main concept in plain language, explain the request trace above, then justify one choice and its cost. Finish by explaining how the failure drill would be detected and prevented in this backend. Do not claim the proposed extension is deployed.

## Stop/go and learner evidence

Proceed only after explaining the request trace without notes, predicting the failure, and answering all three checks below before opening their answers. Record the attempt in [learner progress](learner-progress.md). Artifact readiness does not establish comprehension or implementation readiness.

## Questions and explained answers

<details>
<summary>1. Does Cache-Control: private mean never store the response?</summary>

No. It prevents shared-cache storage but allows a private cache to store it subject to the remaining policy. no-store instructs caches not to retain the response. Viewer-specific fields also require correct isolation.

</details>

<details>
<summary>2. What exact response or state should the worked request produce, and why?</summary>

User A loads a feed item with likedByViewer=true. A shared cache keyed only by URL gives it to B -> B sees incorrect personal state. With no-store, that response is not retained by compliant caches. A separately designed public representation could be cached without viewer-specific fields.

This follows from the request boundary and state checks in the walkthrough, not merely from choosing a URL.

</details>

<details>
<summary>3. Which failure can remain despite a successful happy-path request, and how would you prevent it?</summary>

Predict sharing a cached /api/me across tokens. Detect two-user cache tests, contain with no-store and purge, recover exposed cache entries, prevent by separating public and personalized representations and reviewing cache keys.

A successful request proves one case. The controlled failure needs its own observation and regression check.

</details>
