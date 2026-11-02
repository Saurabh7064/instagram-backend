# 05. Consistent error bodies

## Question

How do you design errors that clients can handle safely?

[Module sequence](README.md) · Artifact: READY · Learning: TODO

## What you will learn

Design an error the app can act on and a person can understand, while keeping server diagnostics private. Explain why a database outage must not look like a bad form field.

Builds on [request validation](04-request-validation.md).

## An error message has two readers

The app requests post 42 and receives “Post is unavailable.” Mira can understand that sentence. The app still needs to decide whether to show a missing-post screen, ask her to sign in, or offer a retry. Parsing the English sentence would make that decision depend on wording that might change.

Give the two readers different parts of the response. The HTTP status gives the broad outcome; a stable application code such as `POST_NOT_FOUND` gives the app a documented decision key. A readable detail explains the problem to the person. Changing “unavailable” to “could not be found” should not break client logic.

**Problem Details** is a standard shape for describing HTTP errors. You can use its status, title, and detail fields and add a safe application code. The value is consistency: a form, mobile screen, and monitoring tool do not each need to interpret a different error structure.

A repair ticket is a helpful analogy. The category helps route it and the description helps a person understand it. The limit is that software needs stable, exact categories; it cannot infer that two differently worded messages mean the same thing.

[Validation](04-request-validation.md) tells us why an input should fail. This lesson follows that failure across the API boundary without exposing internal diagnostics.

## Map one known domain failure

The project uses [Spring Boot 4.0.2 and Java 24](../../../../build.gradle). The excerpt below is a design proposal unless identified as current code; omitted imports and collaborators still need implementation.

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

### Read the boundary in the code

The proposed advice catches a specific proposed domain exception, `PostMissing`. It creates a 404 Problem Details object, adds a readable title, and supplies stable code `POST_NOT_FOUND`. The exception category is what makes that mapping justified.

It deliberately does not catch every exception and call it missing or invalid. Framework parsing and validation failures need their own handling, often through `ResponseEntityExceptionHandler`; an unexpected storage failure must retain its server-failure classification.

## Follow one missing-post response

The client asks for post 42. The proposed query service finds it unavailable and throws the specific `PostMissing` exception. The advice maps that known situation to a 404 Problem Details body with code `POST_NOT_FOUND`. The app can show its missing-post state without comparing the English detail.

Contrast that with a failure to reach the database while looking for the same post. The server has not established that the post is missing. Calling it `POST_NOT_FOUND` would hide the real failure and tell the user the wrong thing. Return a sanitized server error, and retain the diagnostic cause internally.

A correlation ID lets support connect the safe public response to those internal diagnostics. It is a lookup aid, not permission to send stack traces or database messages to the client.

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

## Apply this to the backend

[PostService](../../../../src/main/java/com/instagram/backend/service/PostService.java) currently uses `ResponseStatusException` for missing posts and owner-only denials. The proposed `PostMissing` advice above is a possible evolution, not a claim that this backend already emits that error contract.

For F-017, draft safe codes for invalid comment content, an unavailable target post, and denied deletion. Write one client action for each, then check that no response needs to expose table names, SQL, or an exception class.

## Useful later: choices and costs

Choose a standard shape when multiple clients need predictable errors. A small custom DTO can work but needs equivalent documentation. Do not include stack traces, SQL, token values, or rejected secret fields. Field-level errors help forms, but only expose fields the caller is entitled to know.

## Make the database fail while reading post 42

On paper, assume the query fails before it can establish whether post 42 exists. Predict the public response: a sanitized server failure, not `POST_NOT_FOUND`, and no raw database message. Returning 404 would turn an inability to look up the post into a claim that it is absent.

A future fault test should detect leaked SQL, exception classes, or wrong status/code combinations while retaining an internal correlation trail. Contain a faulty error handler by removing raw exception text and restoring the correct classification. Recover the dependency separately, then verify the same lookup returns its actual domain result. Prevent recurrence with deliberate known-failure mappings and a negative assertion that unexpected diagnostics never enter the public body.

## Interview-ready answer

I use a stable machine-readable error contract and keep human detail separate from client branching. HTTP status and body status agree; problem type/code identifies the category; a safe correlation ID helps diagnosis. I map known domain and validation failures explicitly and leave unexpected faults as sanitized server failures. Controller advice does not automatically cover every filter rejection. I never expose SQL, stack traces, or rejected secret values. In this backend I would coordinate token/filter failures with domain advice and test malformed bodies, known conflicts, missing posts, and unexpected database outages. A friendly error response is not a substitute for transactional correctness: a rejected mutation must also leave the expected stored state.

## Before moving on

Explain why a missing post and an unavailable database need different classifications. Continue when you can design one safe public response and show where its detailed internal cause belongs.

Attempt the questions before opening their answers and record the explanation in [learner progress](learner-progress.md). This revision leaves learning status unchanged; a finished document is not evidence that you have demonstrated the idea.

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
