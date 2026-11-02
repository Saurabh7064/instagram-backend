# 03. Status codes and response contracts

## Question

What should a client receive after success or failure?

[Module sequence](README.md) · Artifact: READY · Learning: TODO

## What you will learn

Choose a response that tells the client what happened and what state to trust. Then use the status table to distinguish failures that require different client actions.

Read [HTTP methods](02-http-methods.md) first, especially completed creation versus accepted work.

## The client needs to know what actually happened

Mira taps Publish. The server creates post 42, but the response says only `{"message":"Done"}`. What should the app do next? It has no post ID to keep, and a sentence is a fragile way to decide whether creation succeeded.

A useful response answers two questions: **what happened, and what values should the client now trust?** The status code answers the first at a broad level. A body can answer the second by returning the created post. Headers carry supporting information, such as the address of that post.

For completed creation, `201 Created` tells the client that a resource was created. Returning `id:42` gives it the server-assigned identity. If the server trims the submitted caption, the body should show the stored caption, not simply repeat what the client sent. This selected response object is a **data transfer object (DTO)**: fields deliberately chosen for the public contract rather than every field of the database entity.

Think of the response as a receipt. A receipt identifies what was accepted and the result you can rely on. The comparison stops at machine handling: the app needs stable field names, types, and status meanings, rather than a human interpreting varied wording.

Keep the [method distinction](02-http-methods.md) in mind: creation finished now is different from long work merely accepted for later completion.

## Build a response that identifies the created post

The project uses [Spring Boot 4.0.2 and Java 24](../../../../build.gradle). The excerpt below is a design proposal unless identified as current code; omitted imports and collaborators still need implementation.

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

### Read the boundary in the code

`postService.create` returns the server's created-post DTO. `ResponseEntity.created` chooses 201 and sets `Location`; `.body(created)` puts that same post in the response. Constructing the address from the generated ID keeps it tied to the created resource rather than an unchecked caller-supplied URL.

This is a proposed variant of the existing controller. Today's create uses a 201 annotation and returns a DTO, but it does not establish the proposed item's GET route. Add that read behavior before advertising the address as a link clients can follow.

## Read the proposed receipt for post 42

Assume the valid create commits and the server assigns ID 42. The proposed response is 201, with body `id:42` and `Location: /api/posts/42`. Those pieces tell one consistent story: a post now exists, this is its identity, and this is its address. The item read route must exist before the API promises clients can follow that address.

If parsing fails before creation, return the appropriate 400 response and leave no new post. If credentials are missing, the current authentication path rejects the request; it has not created a post that the client must somehow discover. If the database is unavailable, that is a server-side failure rather than an instruction to shorten the caption.

The response's role is to help the caller choose its next action. That is why “all responses are 200, inspect the message” undermines both client behavior and status-based monitoring.

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

## Apply this to the backend

[FeedPostResponse](../../../../src/main/java/com/instagram/backend/dto/FeedPostResponse.java) is the current public read shape. [PostService.create](../../../../src/main/java/com/instagram/backend/service/PostService.java) supplies trimmed content, a server-assigned ID, server-selected author fields, and the initial count. The current [controller](../../../../src/main/java/com/instagram/backend/controller/PostController.java) declares 201.

For F-017, write the create-comment response before its controller: completion status, returned fields, and whether an item address can be followed. Also choose the deletion response so the UI knows whether there is a body to parse.

## Useful later: choices and costs

Return 200 when a mutation returns a representation, 204 when there is no body, and 202 only when work is accepted but unfinished. Explicit DTOs protect clients from persistence changes but require mapping. Do not wrap every failure in 200: monitoring and retry logic would misclassify it.

## A successful status can tell the wrong story

On paper, let post 42's create fail validation but return 200 with an error sentence in the body. Predict what a client that trusts HTTP success will do: it may show a published state even though no post was created. Status-based monitoring also counts a success.

Detect the disagreement with a request that checks both the failure category and absence of a new row. Contain a faulty release by restoring the response mapping and, where necessary, reconciling the client's optimistic state. Re-run valid and invalid creates to verify their different outcomes. Prevent recurrence with client-visible status/body checks rather than tests that merely search for a friendly message.

## Interview-ready answer

I choose the status from the actual outcome and the boundary that failed. Completed creation is 201; accepted unfinished work is 202; a deliberate empty success is 204. I distinguish input syntax, media negotiation, credentials, authorization, missing resources, conflicts, and preconditions so callers can act correctly. Response DTOs contain authoritative server state and stable types rather than arbitrary entity fields. In this backend I preserve current validation conventions and avoid inventing a 422 contract without a deliberate change. I test not only status and fields but final stored state, and never use 200 to hide failures. I also review ID precision, timestamp units, nullability, and no-body responses before clients depend on them.

## Before moving on

Describe the status, body, and Location for the proposed create of post 42. Continue when you can distinguish completed creation, unfinished work, invalid input, and an unavailable dependency and name the caller's next action.

Attempt the questions before opening their answers and record the explanation in [learner progress](learner-progress.md). This revision leaves learning status unchanged; a finished document is not evidence that you have demonstrated the idea.

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
