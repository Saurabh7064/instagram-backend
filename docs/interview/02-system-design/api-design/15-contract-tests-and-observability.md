# 15. Contract tests and observable API behavior

## Question

How do you prove that an API contract survives a release?

[Module sequence](README.md) · Estimated time: 15–20 minutes · Artifact: READY · Learning: TODO

## What you will learn

Explain the problem behind contract tests and observable api behavior, trace its externally visible behavior, and identify one implementation risk before proposing a change.

Prerequisites: Lesson 14 and its stop/go check. All later lessons remain TODO until their own checkpoints are attempted.

## Must understand: problem and model

A controller can compile while returning the wrong status, exposing fields, or breaking the database path. An integration test exercises components together; a contract assertion checks externally promised behavior; a correlation ID connects one request to diagnostics. Like checking a complete delivered order, the analogy stops because concurrent and failure paths need separate deliberate experiments.

## Spring Boot example

Unless explicitly described as existing code, this is a proposed teaching excerpt, not an endpoint implemented by this documentation change. Application-specific collaborators and types require implementation; imports and class wiring are omitted. Preserve authentication when adapting an excerpt. This project uses Spring Boot 4.0.2 and Java 24, as recorded in [build configuration](../../../../build.gradle).

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

## Project mapping and practice

Read the actual [backend implementation](../../../../src/main/java/com/instagram/backend/observability/RequestCorrelationFilter.java) before changing it. The example above distinguishes existing behavior from a proposed extension; a linked file establishes the current starting point, not proof that the proposed design exists.

**Practice task:** For F-017 add successful create, invalid content, missing authentication, unauthorized deletion, persistence, and correlation-header integration cases.

Keep this a design exercise until a feature change is explicitly undertaken. An implementation must follow the repository integration-test, API verification, documentation, and commit workflow.

## Useful later: choices and costs

Use MockMvc plus PostgreSQL Testcontainers for backend integration; browser tests cover UI behavior separately. Mock-only controller tests are faster but cannot prove database constraints. Avoid brittle full-response snapshots when only a few fields are contractual. Never put bearer tokens or user IDs into high-cardinality metric labels.

## Controlled failure drill

Predict a renamed JSON property while all service unit tests pass. Old clients fail. Detect contract tests and release comparison, contain rollout, recover by restoring the field, prevent with old-consumer fixtures plus negative/security cases.

Describe the prediction before reading the symptom. In a future lab, use disposable data, record the observed response and state, and verify recovery rather than assuming it succeeded.

## Interview-ready answer outline

For a two-minute answer, start with the caller’s problem, define the main concept in plain language, explain the request trace above, then justify one choice and its cost. Finish by explaining how the failure drill would be detected and prevented in this backend. Do not claim the proposed extension is deployed.

## Stop/go and learner evidence

Proceed only after explaining the request trace without notes, predicting the failure, and answering all three checks below before opening their answers. Record the attempt in [learner progress](learner-progress.md). Artifact readiness does not establish comprehension or implementation readiness.

## Questions and explained answers

<details>
<summary>1. What can a service unit test miss that an API integration test catches?</summary>

Request binding, validation activation, response serialization, status selection, filter behavior, and real database constraints. Add identity and persistence assertions; one happy-path test does not prove race safety or prevent sensitive-field exposure.

</details>

<details>
<summary>2. What exact response or state should the worked request produce, and why?</summary>

Seed user -> obtain token -> POST valid content -> expect 201 and numeric ID -> query persistence or read through API -> prove one post exists. Repeat with a non-owner token for edit -> expect 403 and unchanged caption. Send X-Correlation-Id: api-lesson-15 -> current filter echoes the safe value and attaches it to diagnostics.

This follows from the request boundary and state checks in the walkthrough, not merely from choosing a URL.

</details>

<details>
<summary>3. Which failure can remain despite a successful happy-path request, and how would you prevent it?</summary>

Predict a renamed JSON property while all service unit tests pass. Old clients fail. Detect contract tests and release comparison, contain rollout, recover by restoring the field, prevent with old-consumer fixtures plus negative/security cases.

A successful request proves one case. The controlled failure needs its own observation and regression check.

</details>
