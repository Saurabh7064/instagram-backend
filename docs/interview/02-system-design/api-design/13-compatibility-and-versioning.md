# 13. API evolution and compatibility

## Question

How do you change an API without breaking installed clients?

[Module sequence](README.md) · Estimated time: 15–20 minutes · Artifact: READY · Learning: TODO

## What you will learn

Explain the problem behind api evolution and compatibility, trace its externally visible behavior, and identify one implementation risk before proposing a change.

Prerequisites: Lesson 12 and its stop/go check. All later lessons remain TODO until their own checkpoints are attempted.

## Must understand: problem and model

Old mobile versions keep making requests after a deployment. Backward compatibility means supported existing callers continue working under the promised contract. A breaking change invalidates an old assumption. Think of changing a power socket while old plugs remain in homes; the analogy stops because clients also depend on field meaning and error behavior.

## Spring Boot example

Unless explicitly described as existing code, this is a proposed teaching excerpt, not an endpoint implemented by this documentation change. Application-specific collaborators and types require implementation; imports and class wiring are omitted. Preserve authentication when adapting an excerpt. This project uses Spring Boot 4.0.2 and Java 24, as recorded in [build configuration](../../../../build.gradle).

```java
// Proposed new response field: preserve the old field during migration.
public record PostView(
        Long id,
        String imageUrl,
        MediaView media) {
}

public record MediaView(String url, String contentType) {
}
```

### Walkthrough

This proposed DTO retains imageUrl while adding media. Populate both consistently during the transition; accepting an extra field is only safe for clients that tolerate unknown fields. Do not return null in the old field while claiming compatibility. DTO changes need serializer and consumer checks, not merely a successful Java compilation.

## Request trace: predict, act, observe, explain

Before following the trace, predict the status and final stored state. Then trace these transitions in order:

Old app reads imageUrl -> old field remains populated -> works after deployment. New app prefers media.url -> works on the new response; during rollout it must tolerate an older server response if that is possible. Removing imageUrl before old clients retire breaks their media display.

The observable result includes the response **and** the final stored state. A successful status alone cannot prove concurrency or authorization correctness.

## Project mapping and practice

Read the actual [backend implementation](../../../../src/main/java/com/instagram/backend/dto/FeedPostResponse.java) before changing it. The example above distinguishes existing behavior from a proposed extension; a linked file establishes the current starting point, not proof that the proposed design exists.

**Practice task:** For F-016 plan an envelope migration without silently changing the existing array contract; tie it to SD-004 compatibility work.

Keep this a design exercise until a feature change is explicitly undertaken. An implementation must follow the repository integration-test, API verification, documentation, and commit workflow.

## Useful later: choices and costs

Prefer additive evolution for compatible changes, but enum expansion and strict clients can still break. Explicit /v2 routes help when semantics genuinely change, at the cost of supporting two contracts. Renaming fields, changing units, or changing a list into an envelope is a breaking change even if URLs stay the same.

## Controlled failure drill

Predict replacing the current feed array with {items:[...]}. Old UI calls array.map and fails. Detect an old-consumer fixture test, contain via parallel contract, recover by restoring old shape, prevent with staged migration and measured retirement criteria.

Describe the prediction before reading the symptom. In a future lab, use disposable data, record the observed response and state, and verify recovery rather than assuming it succeeded.

## Interview-ready answer outline

For a two-minute answer, start with the caller’s problem, define the main concept in plain language, explain the request trace above, then justify one choice and its cost. Finish by explaining how the failure drill would be detected and prevented in this backend. Do not claim the proposed extension is deployed.

## Stop/go and learner evidence

Proceed only after explaining the request trace without notes, predicting the failure, and answering all three checks below before opening their answers. Record the attempt in [learner progress](learner-progress.md). Artifact readiness does not establish comprehension or implementation readiness.

## Questions and explained answers

<details>
<summary>1. Is changing a JSON array into an items envelope additive?</summary>

No. An old consumer expecting an array can fail when calling array.map on an object. Offer a deliberate migration path or a new contract and test the old consumer before retiring the old shape.

</details>

<details>
<summary>2. What exact response or state should the worked request produce, and why?</summary>

Old app reads imageUrl -> old field remains populated -> works after deployment. New app prefers media.url -> works on the new response; during rollout it must tolerate an older server response if that is possible. Removing imageUrl before old clients retire breaks their media display.

This follows from the request boundary and state checks in the walkthrough, not merely from choosing a URL.

</details>

<details>
<summary>3. Which failure can remain despite a successful happy-path request, and how would you prevent it?</summary>

Predict replacing the current feed array with {items:[...]}. Old UI calls array.map and fails. Detect an old-consumer fixture test, contain via parallel contract, recover by restoring old shape, prevent with staged migration and measured retirement criteria.

A successful request proves one case. The controlled failure needs its own observation and regression check.

</details>
