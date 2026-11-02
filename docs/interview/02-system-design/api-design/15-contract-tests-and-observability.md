# 15. Contract tests and observable API behavior

## Question

How do you prove that an API contract survives a release?

[Module sequence](README.md) · Artifact: READY · Learning: TODO

## What you will learn

Turn a caller promise into an integration check, verify the stored result of a denied edit, and choose diagnostics that help find failures after release.

Builds on the failure boundaries in the previous API lessons. Examples here describe tests to write; they are not test-run evidence.

## The endpoint compiled. Did its promise survive?

A release compiles and the service tests pass. Yet a client that previously read `imageUrl` now receives `mediaUrl`, or an edit returns 403 after it has already changed the row. Neither problem is ruled out by checking that Java methods return the expected objects in isolation.

An **integration test** sends a request through connected parts of the application: request conversion, validation, service logic, persistence, and response serialization. A **contract assertion** checks something a caller has been promised, such as the create status or the spelling of a response field. We need both externally visible output and the stored effect.

Take one fixed flow: an authenticated user creates a post with caption `hello`, then a different user tries to edit it. Creation should return 201 and a server-assigned ID. The second request should return the current service's 403, and a fresh read must still show `hello`. The rejection is correct only if the write did not happen.

Testing a delivered order is a useful analogy: inspect what arrived, rather than trusting that each kitchen station worked. The comparison stops at timing. Concurrent requests and crashes need deliberately arranged experiments; one successful order cannot establish every failure guarantee.

The [job lesson](14-timeouts-and-async-work.md) makes the same distinction: an initial success status is only one part of a larger promise.

## Assert the public create response

MockMvc drives an HTTP-style request through Spring inside the test process. Testcontainers supplies a real PostgreSQL database for the fixture. Together they exercise more of the actual boundary than a controller backed only by mocks.

The project uses [Spring Boot 4.0.2 and Java 24](../../../../build.gradle). The excerpt below is a design proposal unless identified as current code; omitted imports and collaborators still need implementation.

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

### Read the boundary in the code

`perform` sends the request through MockMvc and the configured Spring application. The assertions check 201 and selected public fields; they do not yet prove a committed post exists. Obtain `bearerToken` from fixture authentication, and seed permitted media if the feature adds such validation.

This is an illustrative excerpt in the existing Testcontainers/MockMvc fixture, not an added or executed test. Add a fresh persistence check and assertions that protected fields are absent. Those test the public promise more directly than counting calls to a mocked repository.

## Check the response, then check the saved effect

Create an authenticated user and submit the fixed caption `hello` with valid image and location fields. Assert 201, a numeric server-assigned ID, and returned caption `hello`. Then inspect fresh persisted state to prove one post was committed. This distinguishes an attractive response from an actual completed creation.

Next obtain another user's token and edit that post. Assert the current contract's 403 and read the stored caption again. It must remain `hello`. If it changed, the API has violated ownership even though the status assertion passes.

The current correlation filter can attach a safe `X-Correlation-Id`, such as `api-lesson-15`, to diagnostics and echo it in the response. That helps connect a failed check to logs; it is supporting evidence rather than a substitute for either response or state assertions.

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

## Apply this to the backend

[AuthIntegrationTests](../../../../src/test/java/com/instagram/backend/AuthIntegrationTests.java) is the existing real-database test starting point. [RequestCorrelationFilter](../../../../src/main/java/com/instagram/backend/observability/RequestCorrelationFilter.java) supplies the current correlation behavior. They demonstrate different boundaries: durable API behavior and diagnostic association.

For F-017, derive checks for successful creation, invalid content, missing authentication, unauthorized deletion, committed state, and correlation headers. Run the actual tests and API requests when implementing the feature; these written examples are not execution evidence.

## Useful later: choices and costs

Use MockMvc plus PostgreSQL Testcontainers for backend integration; browser tests cover UI behavior separately. Mock-only controller tests are faster but cannot prove database constraints. Avoid brittle full-response snapshots when only a few fields are contractual. Never put bearer tokens or user IDs into high-cardinality metric labels.

## Rename a response field without changing service tests

On paper, change `imageUrl` to `mediaUrl` in the public DTO while service tests continue to check only saved posts. Predict the outcome: the backend can create correctly while an old client can no longer render the image.

Detect it with a caller-facing contract assertion and a representative old-consumer fixture, then contain the rollout. Restore the promised field or a compatible adapter and verify the old client path again. Prevent recurrence by deriving checks from both the public shape and durable behavior. The ownership test in this lesson covers a different promise; it cannot substitute for checking serialization. These are complementary checks, not a claim that the sample tests have run.

## Interview-ready answer

I derive tests from external promises, not controller internals. I combine Spring integration tests with a real PostgreSQL database, meaningful identity fixtures, boundary inputs, and final-state assertions. Concurrency needs independent transactions and controlled interleavings; one service mock or sequential repeat cannot prove it. OpenAPI validates shape, consumer fixtures expose real compatibility expectations, and provider tests cover domain/security behavior. I keep route-template labels bounded and use correlation IDs in diagnostics rather than metrics. This backend’s existing Boot 4 Testcontainers/MockMvc fixture is the correct version-specific starting point. For a release I require observed API behavior and the repository’s verification workflow, while documentation examples remain explicitly unexecuted.

## Before moving on

Explain what each create and denied-edit assertion proves and leaves unproven. Continue when you can design a meaningful persisted-state check and a separate old-client shape check.

Attempt the questions before opening their answers and record the explanation in [learner progress](learner-progress.md). This revision leaves learning status unchanged; a finished document is not evidence that you have demonstrated the idea.

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
