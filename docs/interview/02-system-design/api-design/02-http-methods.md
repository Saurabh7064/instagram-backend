# 02. HTTP methods and repeatable effects

## Question

How should GET, POST, PUT, PATCH, and DELETE behave?

[Module sequence](README.md) · Estimated time: 15–20 minutes · Artifact: READY · Learning: TODO

## What you will learn

Explain the problem behind http methods and repeatable effects, trace its externally visible behavior, and identify one implementation risk before proposing a change.

Prerequisites: Lesson 1 and its stop/go check. All later lessons remain TODO until their own checkpoints are attempted.

## Must understand: problem and model

A network can repeat a request, so clients need to know whether repetition changes the result again. Safe means the caller does not request a state change. Idempotent means repeated identical requests have the same intended server effect as one request. GET is safe; PUT and DELETE are idempotent by their defined semantics. Think of setting a thermostat to 20 versus raising it by one; the analogy stops because real requests can fail between steps.

## Spring Boot example

Unless explicitly described as existing code, this is a proposed teaching excerpt, not an endpoint implemented by this documentation change. Application-specific collaborators and types require implementation; imports and class wiring are omitted. Preserve authentication when adapting an excerpt. This project uses Spring Boot 4.0.2 and Java 24, as recorded in [build configuration](../../../../build.gradle).

```java
@DeleteMapping("/posts/{postId}")
@ResponseStatus(HttpStatus.NO_CONTENT)
public void delete(
        @RequestHeader("Authorization") String authorization,
        @PathVariable Long postId) {
    postService.delete(authorization, postId);
}
```

### Walkthrough

DELETE expresses removal. The 204 status deliberately has no response body. Authentication is passed into the existing service, which checks ownership. A second delete may return 404 without violating idempotency: the post remains absent. Logging each attempt also does not change the intended resource effect.

## Request trace: predict, act, observe, explain

Before following the trace, predict the status and final stored state. Then trace these transitions in order:

Post 42 exists -> first DELETE removes likes and post -> 204. Repeat DELETE -> service cannot find post -> 404. Final state after both attempts is still absent. A repeated create POST, by contrast, can produce posts 43 and 44.

The observable result includes the response **and** the final stored state. A successful status alone cannot prove concurrency or authorization correctness.

## Project mapping and practice

Read the actual [backend implementation](../../../../src/main/java/com/instagram/backend/controller/PostController.java) before changing it. The example above distinguishes existing behavior from a proposed extension; a linked file establishes the current starting point, not proof that the proposed design exists.

**Practice task:** For F-014, write down whether a missing location means unchanged, empty, or invalid before selecting PUT or PATCH.

Keep this a design exercise until a feature change is explicitly undertaken. An implementation must follow the repository integration-test, API verification, documentation, and commit workflow.

## Useful later: choices and costs

Use PUT for replacing the defined writable representation; define omitted-field behavior. PATCH describes a partial modification and requires an explicit patch format. This backend uses PUT for caption and location while retaining media: document that editable view or evolve the contract deliberately. Do not use GET for liking a post because prefetching could mutate state.

## Controlled failure drill

Predict a browser prefetch of GET /like. It creates an unintended like. Detect writes during read traffic, contain by disabling that route, recover affected counts, prevent by restricting writes to mutation methods and testing GET has no mutation.

Describe the prediction before reading the symptom. In a future lab, use disposable data, record the observed response and state, and verify recovery rather than assuming it succeeded.

## Interview-ready answer outline

For a two-minute answer, start with the caller’s problem, define the main concept in plain language, explain the request trace above, then justify one choice and its cost. Finish by explaining how the failure drill would be detected and prevented in this backend. Do not claim the proposed extension is deployed.

## Stop/go and learner evidence

Proceed only after explaining the request trace without notes, predicting the failure, and answering all three checks below before opening their answers. Record the attempt in [learner progress](learner-progress.md). Artifact readiness does not establish comprehension or implementation readiness.

## Questions and explained answers

<details>
<summary>1. Can a repeated DELETE return 404 and still be idempotent?</summary>

Yes. Idempotency concerns the intended stored effect, not identical response codes. First deletion removes the post; the repeat leaves it absent. POST creation needs a separate deduplication contract.

</details>

<details>
<summary>2. What exact response or state should the worked request produce, and why?</summary>

Post 42 exists -> first DELETE removes likes and post -> 204. Repeat DELETE -> service cannot find post -> 404. Final state after both attempts is still absent. A repeated create POST, by contrast, can produce posts 43 and 44.

This follows from the request boundary and state checks in the walkthrough, not merely from choosing a URL.

</details>

<details>
<summary>3. Which failure can remain despite a successful happy-path request, and how would you prevent it?</summary>

Predict a browser prefetch of GET /like. It creates an unintended like. Detect writes during read traffic, contain by disabling that route, recover affected counts, prevent by restricting writes to mutation methods and testing GET has no mutation.

A successful request proves one case. The controlled failure needs its own observation and regression check.

</details>
