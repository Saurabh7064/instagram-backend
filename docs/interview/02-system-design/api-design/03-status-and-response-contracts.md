# 03. Status codes and response contracts

## Question

What should a client receive after success or failure?

[Module sequence](README.md) · Study in two passes: core model, then design decisions (15–20 minutes each) · Artifact: READY · Learning: TODO

## What you will learn

Select success/error statuses by failure boundary, design authoritative DTOs, distinguish 400/406/409/412/415/422, and identify numeric-ID compatibility risk.

Prerequisites: Lesson 2 and its stop/go check. All later lessons remain TODO until their own checkpoints are attempted.

## Must understand: problem and model

A client cannot reliably infer success from a sentence in JSON. The status code gives the broad result, headers add metadata, and the body carries the representation. A DTO is a deliberately selected data transfer object rather than a database entity. Like a receipt, the response states what happened; unlike a receipt, automated clients depend on exact machine-readable fields.

## Spring Boot example

Examples are proposed except where explicitly identified as current code. Imports/wiring are omitted; named new services and DTOs need implementation. Preserve current authorization rules. The project uses [Spring Boot 4.0.2 and Java 24](../../../../build.gradle).

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

## Choose statuses from the failure boundary

| Result | Status | Caller action | Common mistake |
|---|---|---|---|
| Existing representation returned | 200 | Render it | Returning a body with 204 |
| Resource created now | 201 | Use returned identity/Location | Using 202 after a completed synchronous create |
| Work accepted, incomplete | 202 | Inspect job state | Assuming success is guaranteed |
| Completed, no body | 204 | Update local state | Trying to parse empty JSON |
| Conditional read unchanged | 304 | Reuse stored representation | Treating 304 as a new JSON response |
| Unparseable JSON / invalid request | 400 | Correct request | Retrying the same bad bytes |
| Missing/invalid credentials | 401 | Obtain valid credentials | Using 403 for expired credentials |
| Identity lacks permission | 403 | Stop or request permission | Client retry loops |
| Missing or deliberately concealed resource | 404 | Show unavailable | Leaking existence inconsistently |
| Unsupported method for resource | 405 | Check Allow and contract | Confusing it with unknown resource |
| No acceptable output media type | 406 | Change Accept | Calling it a malformed body |
| State conflict | 409 | Resolve current domain state | Mapping every database exception to conflict |
| Failed supplied precondition | 412 | Reload and reconcile | Ignoring If-Match |
| Input too large | 413 | Reduce upload/body | Allowing allocation before checking limits |
| Unsupported request media type | 415 | Change Content-Type/body | Assuming all JSON patch formats are interchangeable |
| Semantically invalid content | 422, if adopted | Correct meaning | Using 400 and 422 unpredictably |
| Required precondition missing | 428, if adopted | Read then send validator | Silently accepting an unconditional write |
| Caller exceeds admission policy | 429 | Respect Retry-After | Retrying immediately |
| Unexpected internal failure | 500 | Report; retry only if safe | Returning SQL/stack traces |
| Dependency/gateway failure | 502/504 where applicable | Respect retry budget | Using gateway codes for every controller error |
| Temporarily unavailable service | 503 | Back off under policy | Blaming one user for global overload |

For this project, bean-validation failures currently follow Spring’s normal 400 path. Introducing 422 is a contract decision, not a reason to claim current behavior already uses it. For 401, document the applicable authentication challenge, normally a Bearer `WWW-Authenticate` header; the current custom token parsing must be inspected rather than assuming a security framework supplies it.

### Choose response fields deliberately

A create body should contain authoritative server values, not echo unchecked input. With caption `" hello "`, the service trims it: the response should say `"hello"`. IDs, ownership, creation time, and counts are server controlled. An empty collection is usually 200 with `[]` or the documented page envelope, not 404. An item lookup can be 404 even when its parent collection is empty.

Use a consistent timestamp format, ID representation, and nullability contract. JavaScript cannot represent every Java Long exactly: integer 9007199254740993 becomes unsafe as a JavaScript number. If IDs may reach that range, consider string IDs from the beginning and test consumers. Changing numeric IDs to strings later is breaking.

**Worked negotiation case:** a route accepts JSON and returns JSON. Request Content-Type=text/plain fails with 415 before its text is parsed as JSON. Valid JSON with Accept=application/xml can instead fail with 406. Those are distinct remediation steps; see [content negotiation](16-content-negotiation.md).

## Project mapping and practice

Read the actual [backend implementation](../../../../src/main/java/com/instagram/backend/dto/FeedPostResponse.java) before changing it. The example above distinguishes existing behavior from a proposed extension; a linked file establishes the current starting point, not proof that the proposed design exists.

**Practice task:** For F-017, specify create status, Location behavior, response fields, and deletion response before implementation.

Keep this a design exercise until a feature change is explicitly undertaken. An implementation must follow the repository integration-test, API verification, documentation, and commit workflow.

## Useful later: choices and costs

Return 200 when a mutation returns a representation, 204 when there is no body, and 202 only when work is accepted but unfinished. Explicit DTOs protect clients from persistence changes but require mapping. Do not wrap every failure in 200: monitoring and retry logic would misclassify it.

## Controlled failure drill

Predict a 200 response containing an error string. A client marks the operation successful. Detect disagreement between error-body counts and status metrics, contain by treating the documented error field defensively, recover with correct statuses, prevent with response contract tests.

Describe the prediction before reading the symptom. In a future lab, use disposable data, record the observed response and state, and verify recovery rather than assuming it succeeded.

## Interview-ready answer

I choose the status from the actual outcome and the boundary that failed. Completed creation is 201; accepted unfinished work is 202; a deliberate empty success is 204. I distinguish input syntax, media negotiation, credentials, authorization, missing resources, conflicts, and preconditions so callers can act correctly. Response DTOs contain authoritative server state and stable types rather than arbitrary entity fields. In this backend I preserve current validation conventions and avoid inventing a 422 contract without a deliberate change. I test not only status and fields but final stored state, and never use 200 to hide failures. I also review ID precision, timestamp units, nullability, and no-body responses before clients depend on them.

## Stop/go and learner evidence

Proceed only after explaining the request trace without notes, predicting the failure, and answering all three checks below before opening their answers. Record the attempt in [learner progress](learner-progress.md). Artifact readiness does not establish comprehension or implementation readiness.

## Questions and explained answers

<details>
<summary>1. Why does a create return 201 rather than 202?</summary>

201 reports completed resource creation. 202 reports acceptance of work that is still unfinished. A Location header should identify the created resource; implement the promised read/status route before advertising it.

</details>

<details>
<summary>2. What is the difference between 409 and 412?</summary>

409 reports a conflict with current domain state under the contract. 412 specifically means a supplied HTTP precondition failed, such as stale If-Match. A uniqueness violation is not automatically the same as a revision precondition.

</details>

<details>
<summary>3. Why is an empty collection normally not 404?</summary>

The collection resource exists but has zero matching members. Return its documented empty list/envelope with 200; an absent individual item is a different case.

</details>

<details>
<summary>4. Why could a Long JSON ID break a JavaScript consumer?</summary>

JavaScript numbers represent integers exactly only through 2^53-1. A larger ID can round, so decide the external number/string representation and test clients before changing it.

</details>

<details>
<summary>5. How should the client react to 401 versus 403?</summary>

401 calls for valid authentication according to the challenge and auth flow. 403 means permission is denied for the identity; blindly refreshing/retrying can create a loop.

</details>
