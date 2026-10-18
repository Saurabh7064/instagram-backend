# 05. Consistent error bodies

## Question

How do you design errors that clients can handle safely?

[Module sequence](README.md) · Study in two passes: core model, then design decisions (15–20 minutes each) · Artifact: READY · Learning: TODO

## What you will learn

Define safe Problem Details and field-error extensions; map framework, domain, and persistence failures without misclassifying defects or exposing secrets.

Prerequisites: Lesson 4 and its stop/go check. All later lessons remain TODO until their own checkpoints are attempted.

## Must understand: problem and model

Clients need to distinguish invalid fields from missing resources without parsing human prose. Problem Details is a standard JSON error shape; a stable application error code adds a domain-specific decision key. Think of a repair ticket with a category and explanation; unlike a human ticket, machines need stable categories even when wording changes.

## Spring Boot example

Examples are proposed except where explicitly identified as current code. Imports/wiring are omitted; named new services and DTOs need implementation. Preserve current authorization rules. The project uses [Spring Boot 4.0.2 and Java 24](../../../../build.gradle).

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

## An error is a protocol object

A proposed error response for this backend could be:

```json
{
  "type": "https://api.example.test/problems/post-not-found",
  "title": "Post unavailable",
  "status": 404,
  "detail": "The requested post is unavailable.",
  "instance": "/api/posts/42",
  "code": "POST_NOT_FOUND",
  "correlationId": "api-lesson-05"
}
```

The example domain is illustrative; production needs stable owned documentation for its types. `type` identifies the problem category; `instance` identifies this occurrence. `code` and correlationId are application extensions. The HTTP status must agree with the body status. Treat detail as human text, never as a parser input. An instance URL must not echo token-bearing query strings.

### Map exceptions by responsibility

| Failure | Handling responsibility | Public outcome |
|---|---|---|
| Malformed JSON | Framework error handling | 400 with safe message |
| Invalid field constraints | Validation error handling | 400 with safe field codes under current convention |
| Known missing domain object | Domain exception advice | 404 |
| Known uniqueness race | Persistence exception translation | Documented 409 |
| Stale If-Match | Conditional update boundary | 412 |
| Unknown database connectivity failure | Unexpected failure path | Sanitized 5xx with diagnostics |

Do not write `@ExceptionHandler(Exception.class)` that returns 400 for every exception. It turns server defects into caller blame and corrupts monitoring. `ResponseEntityExceptionHandler` provides framework exception hooks; custom domain advice complements it. Filters can reject requests before controller advice runs, so authentication/filter errors need consistent output at their own boundary.

### Validation details that a form can use

A proposed extension `errors: [{"field":"caption","code":"SIZE_MAX","limit":500}]` lets a form highlight one field and translate its message. Use a deterministic ordering if clients or tests require it. Do not return binding internals, Java class names, or rejected secret values. Global/cross-field errors need a documented place rather than a fabricated field name.

**Failure trace:** a unique-like constraint violation occurs under concurrent requests. Classifying it as “post missing” is wrong; blindly returning 500 can encourage unsafe retries; returning success requires proving the desired like already exists and the stored count is correct after a safely managed transaction. Exception translation cannot repair a partially incorrect data model.

Use the returned correlation ID to find internal diagnostics. Log the cause once at the boundary responsible for diagnosis, preserve its stack internally, and redact tokens and PII. Error shape stability is part of compatibility, including code spelling and optional extensions. See [Problem Details](https://www.rfc-editor.org/rfc/rfc9457.html) and [Spring error handling](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-rest-exceptions.html).

## Project mapping and practice

Read the actual [backend implementation](../../../../src/main/java/com/instagram/backend/service/PostService.java) before changing it. The example above distinguishes existing behavior from a proposed extension; a linked file establishes the current starting point, not proof that the proposed design exists.

**Practice task:** Propose safe error codes for F-017: invalid content, missing post, and permission denied, without exposing database names.

Keep this a design exercise until a feature change is explicitly undertaken. An implementation must follow the repository integration-test, API verification, documentation, and commit workflow.

## Useful later: choices and costs

Choose a standard shape when multiple clients need predictable errors. A small custom DTO can work but needs equivalent documentation. Do not include stack traces, SQL, token values, or rejected secret fields. Field-level errors help forms, but only expose fields the caller is entitled to know.

## Controlled failure drill

Predict returning a database exception message on duplicate registration. Internal table details leak. Detect sensitive strings in test responses, contain by suppressing raw exception text, recover with safe codes, prevent with negative response assertions and reviewed error mappings.

Describe the prediction before reading the symptom. In a future lab, use disposable data, record the observed response and state, and verify recovery rather than assuming it succeeded.

## Interview-ready answer

I use a stable machine-readable error contract and keep human detail separate from client branching. HTTP status and body status agree; problem type/code identifies the category; a safe correlation ID helps diagnosis. I map known domain and validation failures explicitly and leave unexpected faults as sanitized server failures. Controller advice does not automatically cover every filter rejection. I never expose SQL, stack traces, or rejected secret values. In this backend I would coordinate token/filter failures with domain advice and test malformed bodies, known conflicts, missing posts, and unexpected database outages. A friendly error response is not a substitute for transactional correctness: a rejected mutation must also leave the expected stored state.

## Stop/go and learner evidence

Proceed only after explaining the request trace without notes, predicting the failure, and answering all three checks below before opening their answers. Record the attempt in [learner progress](learner-progress.md). Artifact readiness does not establish comprehension or implementation readiness.

## Questions and explained answers

<details>
<summary>1. Should a client branch on the English error detail?</summary>

No. Human wording can change or be localized. Use the HTTP status plus a documented stable application code. Safe human detail supplements that code; it must not expose stack traces or secrets.

</details>

<details>
<summary>2. Why not map every Exception to 400?</summary>

It misclassifies database outages and application defects as caller errors, damages monitoring, and encourages wrong remediation. Known caller failures and unexpected provider failures need different handling.

</details>

<details>
<summary>3. Can controller advice be assumed to format every authentication failure?</summary>

No. Filters can reject before controller invocation. Their response handling must be designed consistently at the filter/security boundary.

</details>

<details>
<summary>4. What should a field validation extension contain?</summary>

Safe field names, stable codes, and useful limits under a documented structure. Omit rejected passwords/tokens and internal binding/class details.

</details>

<details>
<summary>5. Why can an error response still be a failed API implementation?</summary>

It may have changed data before returning an error, leaked sensitive diagnostics, or misclassified the failure. Verify final state and safe output, not just the status.

</details>
