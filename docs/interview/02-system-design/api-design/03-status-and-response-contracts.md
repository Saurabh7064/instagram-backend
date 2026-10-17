# 03. Status codes and response contracts

## Question

What should a client receive after success or failure?

[Module sequence](README.md) · Estimated time: 15–20 minutes · Artifact: READY · Learning: TODO

## What you will learn

Explain the problem behind status codes and response contracts, trace its externally visible behavior, and identify one implementation risk before proposing a change.

Prerequisites: Lesson 2 and its stop/go check. All later lessons remain TODO until their own checkpoints are attempted.

## Must understand: problem and model

A client cannot reliably infer success from a sentence in JSON. The status code gives the broad result, headers add metadata, and the body carries the representation. A DTO is a deliberately selected data transfer object rather than a database entity. Like a receipt, the response states what happened; unlike a receipt, automated clients depend on exact machine-readable fields.

## Spring Boot example

Unless explicitly described as existing code, this is a proposed teaching excerpt, not an endpoint implemented by this documentation change. Application-specific collaborators and types require implementation; imports and class wiring are omitted. Preserve authentication when adapting an excerpt. This project uses Spring Boot 4.0.2 and Java 24, as recorded in [build configuration](../../../../build.gradle).

```java
@PostMapping("/posts")
public ResponseEntity<FeedPostResponse> create(
        @RequestHeader("Authorization") String authorization,
        @Valid @RequestBody CreatePostRequest request) {
    var created = postService.create(authorization, request);
    return ResponseEntity.created(
            URI.create("/api/posts/" + created.id()))
            .body(created);
}
```

### Walkthrough

`ResponseEntity.created` sets 201 and Location; `.body` supplies the created DTO. URI construction uses the generated ID, never an unchecked caller-supplied URL. This is a proposed variant: current create returns 201 through an annotation, and a GET item route would need implementing before promising a dereferenceable Location.

## Request trace: predict, act, observe, explain

Before following the trace, predict the status and final stored state. Then trace these transitions in order:

Valid create -> save generates ID 42 -> 201 with Location /api/posts/42 and body id 42. Invalid JSON -> 400 before creation. Missing credentials -> 401; authenticated non-owner edit -> current service 403; absent post -> 404. A temporary dependency outage is a server-side failure, not a client validation error.

The observable result includes the response **and** the final stored state. A successful status alone cannot prove concurrency or authorization correctness.

## Project mapping and practice

Read the actual [backend implementation](../../../../src/main/java/com/instagram/backend/dto/FeedPostResponse.java) before changing it. The example above distinguishes existing behavior from a proposed extension; a linked file establishes the current starting point, not proof that the proposed design exists.

**Practice task:** For F-017, specify create status, Location behavior, response fields, and deletion response before implementation.

Keep this a design exercise until a feature change is explicitly undertaken. An implementation must follow the repository integration-test, API verification, documentation, and commit workflow.

## Useful later: choices and costs

Return 200 when a mutation returns a representation, 204 when there is no body, and 202 only when work is accepted but unfinished. Explicit DTOs protect clients from persistence changes but require mapping. Do not wrap every failure in 200: monitoring and retry logic would misclassify it.

## Controlled failure drill

Predict a 200 response containing an error string. A client marks the operation successful. Detect disagreement between error-body counts and status metrics, contain by treating the documented error field defensively, recover with correct statuses, prevent with response contract tests.

Describe the prediction before reading the symptom. In a future lab, use disposable data, record the observed response and state, and verify recovery rather than assuming it succeeded.

## Interview-ready answer outline

For a two-minute answer, start with the caller’s problem, define the main concept in plain language, explain the request trace above, then justify one choice and its cost. Finish by explaining how the failure drill would be detected and prevented in this backend. Do not claim the proposed extension is deployed.

## Stop/go and learner evidence

Proceed only after explaining the request trace without notes, predicting the failure, and answering all three checks below before opening their answers. Record the attempt in [learner progress](learner-progress.md). Artifact readiness does not establish comprehension or implementation readiness.

## Questions and explained answers

<details>
<summary>1. Why does a create return 201 rather than 202?</summary>

201 reports completed resource creation. 202 reports acceptance of work that is still unfinished. A Location header should identify the created resource; implement the promised read/status route before advertising it.

</details>

<details>
<summary>2. What exact response or state should the worked request produce, and why?</summary>

Valid create -> save generates ID 42 -> 201 with Location /api/posts/42 and body id 42. Invalid JSON -> 400 before creation. Missing credentials -> 401; authenticated non-owner edit -> current service 403; absent post -> 404. A temporary dependency outage is a server-side failure, not a client validation error.

This follows from the request boundary and state checks in the walkthrough, not merely from choosing a URL.

</details>

<details>
<summary>3. Which failure can remain despite a successful happy-path request, and how would you prevent it?</summary>

Predict a 200 response containing an error string. A client marks the operation successful. Detect disagreement between error-body counts and status metrics, contain by treating the documented error field defensively, recover with correct statuses, prevent with response contract tests.

A successful request proves one case. The controlled failure needs its own observation and regression check.

</details>
