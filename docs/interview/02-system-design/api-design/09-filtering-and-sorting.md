# 09. Filtering and sorting contracts

## Question

How do you expose search options without exposing the database?

[Module sequence](README.md) · Estimated time: 15–20 minutes · Artifact: READY · Learning: TODO

## What you will learn

Explain the problem behind filtering and sorting contracts, trace its externally visible behavior, and identify one implementation risk before proposing a change.

Prerequisites: Lesson 8 and its stop/go check. All later lessons remain TODO until their own checkpoints are attempted.

## Must understand: problem and model

Users need to select subsets and order results, but arbitrary query expressions can be expensive or unsafe. A filter selects matching records; a sort defines result order; an allowlist names accepted choices. A restaurant menu offers bounded options; unlike a menu, combinations can change database query cost dramatically.

## Spring Boot example

Unless explicitly described as existing code, this is a proposed teaching excerpt, not an endpoint implemented by this documentation change. Application-specific collaborators and types require implementation; imports and class wiring are omitted. Preserve authentication when adapting an excerpt. This project uses Spring Boot 4.0.2 and Java 24, as recorded in [build configuration](../../../../build.gradle).

```java
enum PostOrder { NEWEST, OLDEST }

@GetMapping("/posts")
PostPage search(
        @RequestParam(required = false) String author,
        @RequestParam(defaultValue = "NEWEST") PostOrder order) {
    return postQueries.search(author, order);
}
```

### Walkthrough

The enum limits accepted sort choices instead of accepting raw SQL. An absent author means the documented default scope, not permission to see private data. postQueries must also enforce pagination, authorization, and parameter binding. Unknown enum values should become a documented 400 response.

## Request trace: predict, act, observe, explain

Before following the trace, predict the status and final stored state. Then trace these transitions in order:

GET /posts?author=mira.frames&order=NEWEST -> validate options -> bind author as data -> apply visibility scope -> order timestamp and ID -> bounded response. order=DROP_TABLE is not a recognized enum, so reject it before querying.

The observable result includes the response **and** the final stored state. A successful status alone cannot prove concurrency or authorization correctness.

## Project mapping and practice

Read the actual [backend implementation](../../../../src/main/java/com/instagram/backend/repository/PostRepository.java) before changing it. The example above distinguishes existing behavior from a proposed extension; a linked file establishes the current starting point, not proof that the proposed design exists.

**Practice task:** For F-022 define supported search fields, matching rules, sort order, maximum query length, and a bounded result window.

Keep this a design exercise until a feature change is explicitly undertaken. An implementation must follow the repository integration-test, API verification, documentation, and commit workflow.

## Useful later: choices and costs

Explicit options keep contracts stable as table names change. Flexible query languages help complex internal tools but need cost controls, field permissions, and query planning. Case sensitivity, whitespace, empty filters, and repeated parameters need documented meanings. A missing filter must never bypass visibility rules.

## Controlled failure drill

Predict passing a query expression as a raw ORDER BY fragment. Concatenation can allow injection or unindexed sorts. Detect malicious-input tests and slow-query metrics, contain by disabling arbitrary fields, recover with bound values and allowlisted expressions, prevent with review and query-budget tests.

Describe the prediction before reading the symptom. In a future lab, use disposable data, record the observed response and state, and verify recovery rather than assuming it succeeded.

## Interview-ready answer outline

For a two-minute answer, start with the caller’s problem, define the main concept in plain language, explain the request trace above, then justify one choice and its cost. Finish by explaining how the failure drill would be detected and prevented in this backend. Do not claim the proposed extension is deployed.

## Stop/go and learner evidence

Proceed only after explaining the request trace without notes, predicting the failure, and answering all three checks below before opening their answers. Record the attempt in [learner progress](learner-progress.md). Artifact readiness does not establish comprehension or implementation readiness.

## Questions and explained answers

<details>
<summary>1. Why use an enum for sorting instead of arbitrary SQL text?</summary>

An enum exposes a finite supported choice and prevents callers selecting arbitrary database expressions. Translate each choice to a fixed server-owned sort, bind filter values, and retain visibility checks and page bounds.

</details>

<details>
<summary>2. What exact response or state should the worked request produce, and why?</summary>

GET /posts?author=mira.frames&order=NEWEST -> validate options -> bind author as data -> apply visibility scope -> order timestamp and ID -> bounded response. order=DROP_TABLE is not a recognized enum, so reject it before querying.

This follows from the request boundary and state checks in the walkthrough, not merely from choosing a URL.

</details>

<details>
<summary>3. Which failure can remain despite a successful happy-path request, and how would you prevent it?</summary>

Predict passing a query expression as a raw ORDER BY fragment. Concatenation can allow injection or unindexed sorts. Detect malicious-input tests and slow-query metrics, contain by disabling arbitrary fields, recover with bound values and allowlisted expressions, prevent with review and query-budget tests.

A successful request proves one case. The controlled failure needs its own observation and regression check.

</details>
