# 15. Contract tests and observable API behavior

## Question

How do you prove that an API contract survives a release?

[Module sequence](README.md) · Study in two passes: core model, then design decisions (15–20 minutes each) · Artifact: READY · Learning: TODO

## What you will learn

Build a promise-based test matrix, verify committed state under concurrency, distinguish schema/provider/consumer evidence, and measure bounded diagnostics.

Prerequisites: Lesson 14 and its stop/go check. All later lessons remain TODO until their own checkpoints are attempted.

## Must understand: problem and model

A controller can compile while returning the wrong status, exposing fields, or breaking the database path. An integration test exercises components together; a contract assertion checks externally promised behavior; a correlation ID connects one request to diagnostics. Like checking a complete delivered order, the analogy stops because concurrent and failure paths need separate deliberate experiments.

## Spring Boot example

Examples are proposed except where explicitly identified as current code. Imports/wiring are omitted; named new services and DTOs need implementation. Preserve current authorization rules. The project uses [Spring Boot 4.0.2 and Java 24](../../../../build.gradle).

```java
// Excerpt inside the existing Testcontainers/MockMvc test fixture.
mockMvc.perform(post("/api/posts")
        .header("Authorization", bearerToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"caption":"hello","imageUrl":"/uploads/demo.png",
             "locationLabel":"Home"}
            """))
    .andExpect(status().isCreated())
    .andExpect(jsonPath("$.caption").value("hello"))
    .andExpect(jsonPath("$.id").isNumber());
```

### Walkthrough

This is an illustrative test excerpt, not an added test or a claimed test run. bearerToken must come from fixture authentication; seed permitted media if future validation requires it. perform sends a MockMvc request through Spring, status checks HTTP semantics, and jsonPath checks selected public fields. Add persistence assertions and forbidden-field checks rather than asserting implementation details.

## Request trace: predict, act, observe, explain

Before following the trace, predict the status and final stored state. Then trace these transitions in order:

Seed user -> obtain token -> POST valid content -> expect 201 and numeric ID -> query persistence or read through API -> prove one post exists. Repeat with a non-owner token for edit -> expect 403 and unchanged caption. Send X-Correlation-Id: api-lesson-15 -> current filter echoes the safe value and attaches it to diagnostics.

The observable result includes the response **and** the final stored state. A successful status alone cannot prove concurrency or authorization correctness.

## A test matrix from promises and failure boundaries

| Promise | Fixture/action | Assertion beyond HTTP status |
|---|---|---|
| Create validates boundary | 500 and 501 caption units | Exact-limit accepted, over-limit leaves no row |
| Ownership protects writes | Owner/non-owner tokens | Non-owner leaves row unchanged |
| Stable pagination | Equal timestamps; insert/delete between calls | No boundary omission under stated contract |
| Retry key deduplicates | Parallel requests with same key | One post and replay result |
| Conditional update protects edits | Two writes from same validator | One mutation, other conflict, correct final value |
| Error contract is safe | Known and unexpected failures | Stable code; no SQL, token, or stack |
| Old clients remain supported | Old request/response fixture | Old shape and required fields remain meaningful |
| Sensitive data is not cached | Identity response | Explicit intended Cache-Control |

Test concurrency with independently committed transactions and coordinated barriers; a single test transaction can hide locking/visibility behavior. Do not assert “one call to save” as proof of one durable post. Assert the final database rows and externally visible outcomes.

### Proposed negative contract assertion

```java
mockMvc.perform(put("/api/posts/{id}", postId)
        .header("Authorization", otherUsersBearerToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"caption":"stolen","locationLabel":"Elsewhere"}
            """))
    .andExpect(status().isForbidden());
assertThat(postRepository.findById(postId).orElseThrow().getCaption())
    .isEqualTo(originalCaption);
```

Illustrative excerpt, not an executed test. Setup must create two users and a post owned by the first; the second token must identify a different account. Read fresh committed state or clear any persistence context that could hide the actual row. The original caption assertion proves the rejection did not merely return a failure after changing data.

### Observable contract, not private implementation

A consumer-driven contract records an actual caller’s expectations; provider verification checks those expectations against the API. It helps catch field/behavior mismatches but cannot replace provider security, load, or database tests. An OpenAPI schema documents shape, but does not prove ownership or transactional atomicity. Build the matrix from both types of promise.

For observability, compare error rate, latency percentiles, and saturation against a target. A low average can hide a bad p99. Route labels should use templates such as /posts/{id}; putting actual IDs in metric labels creates unbounded distinct series. Keep correlation IDs in diagnostics, not metric dimensions.

The current [integration fixture](../../../../src/test/java/com/instagram/backend/AuthIntegrationTests.java) uses Spring Boot Test, MockMvc, PostgreSQL Testcontainers, and Boot 4’s `org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc`. Reuse that version-correct starting point; a random Boot 3 import can fail here. Executable feature work still needs real test runs plus curl/.http evidence. This lesson revision itself changes documentation only.

## Project mapping and practice

Read the actual [backend implementation](../../../../src/main/java/com/instagram/backend/observability/RequestCorrelationFilter.java) before changing it. The example above distinguishes existing behavior from a proposed extension; a linked file establishes the current starting point, not proof that the proposed design exists.

**Practice task:** For F-017 add successful create, invalid content, missing authentication, unauthorized deletion, persistence, and correlation-header integration cases.

Keep this a design exercise until a feature change is explicitly undertaken. An implementation must follow the repository integration-test, API verification, documentation, and commit workflow.

## Useful later: choices and costs

Use MockMvc plus PostgreSQL Testcontainers for backend integration; browser tests cover UI behavior separately. Mock-only controller tests are faster but cannot prove database constraints. Avoid brittle full-response snapshots when only a few fields are contractual. Never put bearer tokens or user IDs into high-cardinality metric labels.

## Controlled failure drill

Predict a renamed JSON property while all service unit tests pass. Old clients fail. Detect contract tests and release comparison, contain rollout, recover by restoring the field, prevent with old-consumer fixtures plus negative/security cases.

Describe the prediction before reading the symptom. In a future lab, use disposable data, record the observed response and state, and verify recovery rather than assuming it succeeded.

## Interview-ready answer

I derive tests from external promises, not controller internals. I combine Spring integration tests with a real PostgreSQL database, meaningful identity fixtures, boundary inputs, and final-state assertions. Concurrency needs independent transactions and controlled interleavings; one service mock or sequential repeat cannot prove it. OpenAPI validates shape, consumer fixtures expose real compatibility expectations, and provider tests cover domain/security behavior. I keep route-template labels bounded and use correlation IDs in diagnostics rather than metrics. This backend’s existing Boot 4 Testcontainers/MockMvc fixture is the correct version-specific starting point. For a release I require observed API behavior and the repository’s verification workflow, while documentation examples remain explicitly unexecuted.

## Stop/go and learner evidence

Proceed only after explaining the request trace without notes, predicting the failure, and answering all three checks below before opening their answers. Record the attempt in [learner progress](learner-progress.md). Artifact readiness does not establish comprehension or implementation readiness.

## Questions and explained answers

<details>
<summary>1. What can a service unit test miss that an API integration test catches?</summary>

Request binding, validation activation, response serialization, status selection, filter behavior, and real database constraints. Add identity and persistence assertions; one happy-path test does not prove race safety or prevent sensitive-field exposure.

</details>

<details>
<summary>2. Why does verifying one call to save not prove exactly one durable post?</summary>

A mock interaction does not exercise transaction rollback, retries, uniqueness, or the committed database. Inspect real final rows and externally visible responses.

</details>

<details>
<summary>3. How can one test transaction hide a race?</summary>

It can keep operations in one persistence/visibility context rather than independent committed requests. Coordinate separate transaction boundaries and read fresh final state.

</details>

<details>
<summary>4. Can an OpenAPI schema prove owner-only edits?</summary>

No. Shape descriptions cannot establish which stored user owns an object or whether a rejected request mutated it. Security and state assertions are necessary.

</details>

<details>
<summary>5. Why not use correlation IDs as metric labels?</summary>

Each request can have a distinct value, causing unbounded metric-series cardinality. Put correlation IDs in logs/traces and use bounded route/operation labels for metrics.

</details>
