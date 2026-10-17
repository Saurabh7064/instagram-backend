# 04. Request validation and write boundaries

## Question

Where should API input be validated?

[Module sequence](README.md) · Estimated time: 15–20 minutes · Artifact: READY · Learning: TODO

## What you will learn

Explain the problem behind request validation and write boundaries, trace its externally visible behavior, and identify one implementation risk before proposing a change.

Prerequisites: Lesson 3 and its stop/go check. All later lessons remain TODO until their own checkpoints are attempted.

## Must understand: problem and model

Invalid input should fail before it reaches durable storage. Structural validation checks shape and local limits; business validation checks meaning using current state. A constraint is a declared rule such as a maximum length. Think of a form checking required boxes before a clerk checks eligibility; the analogy stops because concurrent database changes require atomic enforcement.

## Spring Boot example

Unless explicitly described as existing code, this is a proposed teaching excerpt, not an endpoint implemented by this documentation change. Application-specific collaborators and types require implementation; imports and class wiring are omitted. Preserve authentication when adapting an excerpt. This project uses Spring Boot 4.0.2 and Java 24, as recorded in [build configuration](../../../../build.gradle).

```java
public record CreatePostRequest(
        @NotBlank @Size(max = 500) String caption,
        @NotBlank String imageUrl,
        @NotBlank @Size(max = 120) String locationLabel) {
}

// Controller parameter:
// @Valid @RequestBody CreatePostRequest request
```

### Walkthrough

This record matches the existing request. `@NotBlank` rejects null, empty, and whitespace-only strings; `@Size` limits length but alone permits null. `@Valid` activates object validation at the controller boundary. An arbitrary nonblank imageUrl still passes these annotations: ownership, allowed media origin, and file existence need separate policies.

## Request trace: predict, act, observe, explain

Before following the trace, predict the status and final stored state. Then trace these transitions in order:

Caption containing 501 letters -> @Size fails -> 400 -> service not called. Caption containing three spaces -> @NotBlank fails. Caption " hi " passes -> current service trims -> stores "hi". A well-shaped request for another person’s media still needs business authorization.

The observable result includes the response **and** the final stored state. A successful status alone cannot prove concurrency or authorization correctness.

## Project mapping and practice

Read the actual [backend implementation](../../../../src/main/java/com/instagram/backend/dto/CreatePostRequest.java) before changing it. The example above distinguishes existing behavior from a proposed extension; a linked file establishes the current starting point, not proof that the proposed design exists.

**Practice task:** For F-017 comments, define blank/length rules and distinguish them from permission to comment on the target post.

Keep this a design exercise until a feature change is explicitly undertaken. An implementation must follow the repository integration-test, API verification, documentation, and commit workflow.

## Useful later: choices and costs

Keep cheap format checks in DTOs and ownership or uniqueness checks in the service/database. Validating only in the UI cannot protect against curl. Excessively strict URL rules can block legitimate future media hosts; unrestricted URLs can allow unwanted external content. Decide permitted origins explicitly.

## Controlled failure drill

Predict sending 501 letters with a client that bypasses UI validation. Without backend validation, oversized values reach persistence. Detect database length errors, contain with request limits, recover by rejecting invalid writes consistently, prevent with boundary tests at 500 and 501.

Describe the prediction before reading the symptom. In a future lab, use disposable data, record the observed response and state, and verify recovery rather than assuming it succeeded.

## Interview-ready answer outline

For a two-minute answer, start with the caller’s problem, define the main concept in plain language, explain the request trace above, then justify one choice and its cost. Finish by explaining how the failure drill would be detected and prevented in this backend. Do not claim the proposed extension is deployed.

## Stop/go and learner evidence

Proceed only after explaining the request trace without notes, predicting the failure, and answering all three checks below before opening their answers. Record the attempt in [learner progress](learner-progress.md). Artifact readiness does not establish comprehension or implementation readiness.

## Questions and explained answers

<details>
<summary>1. Does @NotBlank prove an image URL belongs to the caller?</summary>

No. It rejects null, empty, and whitespace-only strings. URL origin, media existence, and permission are separate rules requiring a business-policy check.

</details>

<details>
<summary>2. What exact response or state should the worked request produce, and why?</summary>

Caption containing 501 letters -> @Size fails -> 400 -> service not called. Caption containing three spaces -> @NotBlank fails. Caption " hi " passes -> current service trims -> stores "hi". A well-shaped request for another person’s media still needs business authorization.

This follows from the request boundary and state checks in the walkthrough, not merely from choosing a URL.

</details>

<details>
<summary>3. Which failure can remain despite a successful happy-path request, and how would you prevent it?</summary>

Predict sending 501 letters with a client that bypasses UI validation. Without backend validation, oversized values reach persistence. Detect database length errors, contain with request limits, recover by rejecting invalid writes consistently, prevent with boundary tests at 500 and 501.

A successful request proves one case. The controlled failure needs its own observation and regression check.

</details>
