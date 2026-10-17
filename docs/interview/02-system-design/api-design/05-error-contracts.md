# 05. Consistent error bodies

## Question

How do you design errors that clients can handle safely?

[Module sequence](README.md) · Estimated time: 15–20 minutes · Artifact: READY · Learning: TODO

## What you will learn

Explain the problem behind consistent error bodies, trace its externally visible behavior, and identify one implementation risk before proposing a change.

Prerequisites: Lesson 4 and its stop/go check. All later lessons remain TODO until their own checkpoints are attempted.

## Must understand: problem and model

Clients need to distinguish invalid fields from missing resources without parsing human prose. Problem Details is a standard JSON error shape; a stable application error code adds a domain-specific decision key. Think of a repair ticket with a category and explanation; unlike a human ticket, machines need stable categories even when wording changes.

## Spring Boot example

Unless explicitly described as existing code, this is a proposed teaching excerpt, not an endpoint implemented by this documentation change. Application-specific collaborators and types require implementation; imports and class wiring are omitted. Preserve authentication when adapting an excerpt. This project uses Spring Boot 4.0.2 and Java 24, as recorded in [build configuration](../../../../build.gradle).

```java
@RestControllerAdvice
class ApiErrors {
    @ExceptionHandler(PostMissing.class)
    ProblemDetail missing(PostMissing failure) {
        var problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, "Post is unavailable");
        problem.setTitle("Post unavailable");
        problem.setProperty("code", "POST_NOT_FOUND");
        return problem;
    }
}
```

### Walkthrough

This proposed advice handles a proposed domain exception, not every exception. `forStatusAndDetail` supplies 404 and safe prose. The title is readable; code is stable for client branching. Framework validation exceptions need their own deliberate handling, typically through ResponseEntityExceptionHandler; never map all exceptions to 400.

## Request trace: predict, act, observe, explain

Before following the trace, predict the status and final stored state. Then trace these transitions in order:

GET post 42 -> proposed service throws PostMissing -> advice returns 404 application/problem+json with status, title, detail, and code. Client chooses its missing-post screen from code, not from the English detail. An unexpected database failure must instead produce a sanitized 5xx and internal diagnostics.

The observable result includes the response **and** the final stored state. A successful status alone cannot prove concurrency or authorization correctness.

## Project mapping and practice

Read the actual [backend implementation](../../../../src/main/java/com/instagram/backend/service/PostService.java) before changing it. The example above distinguishes existing behavior from a proposed extension; a linked file establishes the current starting point, not proof that the proposed design exists.

**Practice task:** Propose safe error codes for F-017: invalid content, missing post, and permission denied, without exposing database names.

Keep this a design exercise until a feature change is explicitly undertaken. An implementation must follow the repository integration-test, API verification, documentation, and commit workflow.

## Useful later: choices and costs

Choose a standard shape when multiple clients need predictable errors. A small custom DTO can work but needs equivalent documentation. Do not include stack traces, SQL, token values, or rejected secret fields. Field-level errors help forms, but only expose fields the caller is entitled to know.

## Controlled failure drill

Predict returning a database exception message on duplicate registration. Internal table details leak. Detect sensitive strings in test responses, contain by suppressing raw exception text, recover with safe codes, prevent with negative response assertions and reviewed error mappings.

Describe the prediction before reading the symptom. In a future lab, use disposable data, record the observed response and state, and verify recovery rather than assuming it succeeded.

## Interview-ready answer outline

For a two-minute answer, start with the caller’s problem, define the main concept in plain language, explain the request trace above, then justify one choice and its cost. Finish by explaining how the failure drill would be detected and prevented in this backend. Do not claim the proposed extension is deployed.

## Stop/go and learner evidence

Proceed only after explaining the request trace without notes, predicting the failure, and answering all three checks below before opening their answers. Record the attempt in [learner progress](learner-progress.md). Artifact readiness does not establish comprehension or implementation readiness.

## Questions and explained answers

<details>
<summary>1. Should a client branch on the English error detail?</summary>

No. Human wording can change or be localized. Use the HTTP status plus a documented stable application code. Safe human detail supplements that code; it must not expose stack traces or secrets.

</details>

<details>
<summary>2. What exact response or state should the worked request produce, and why?</summary>

GET post 42 -> proposed service throws PostMissing -> advice returns 404 application/problem+json with status, title, detail, and code. Client chooses its missing-post screen from code, not from the English detail. An unexpected database failure must instead produce a sanitized 5xx and internal diagnostics.

This follows from the request boundary and state checks in the walkthrough, not merely from choosing a URL.

</details>

<details>
<summary>3. Which failure can remain despite a successful happy-path request, and how would you prevent it?</summary>

Predict returning a database exception message on duplicate registration. Internal table details leak. Detect sensitive strings in test responses, contain by suppressing raw exception text, recover with safe codes, prevent with negative response assertions and reviewed error mappings.

A successful request proves one case. The controlled failure needs its own observation and regression check.

</details>
