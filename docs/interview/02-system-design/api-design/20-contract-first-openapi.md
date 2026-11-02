# 20. Contract-first design and an API review rubric

## Question

How do you turn an API proposal into an implementable, reviewable contract before writing controllers?

[Module sequence](README.md) · Artifact: READY · Learning: TODO · Suggested study: 20–30 minutes plus design exercise

## What you will learn

Turn one create flow into an agreement a client and backend can both implement. Read what an OpenAPI excerpt promises, then identify the behavior it still cannot prove.

Use the [response](03-status-and-response-contracts.md), [validation](04-request-validation.md), and [contract-test](15-contract-tests-and-observability.md) lessons as supporting references.

## Agree on what Publish means before building both sides

The frontend developer treats the caption as optional and builds a form that can send `{}`. The backend developer uses `@NotBlank`, so that request fails with 400. Each implementation can pass its own tests while the Publish flow still fails for the user. The missing piece is an agreement between the two sides.

Start by writing the request and response the caller will use, including required fields and failure examples. This is **contract-first design**: settle the externally visible promises before choosing controller signatures. It does not mean every internal decision must be finished first; it means the two sides should not independently invent incompatible meanings.

**OpenAPI** is a machine-readable description of HTTP operations and data shapes. It can state that create accepts JSON with required caption, imageUrl, and locationLabel and returns 201 on completion. It helps documentation and tooling share the same structural agreement.

A description still needs checking against the application. **Provider verification** tests selected promises against the provider, the backend that serves the API. A schema can say caption is required; it cannot prove the database committed a post or that another user cannot edit it.

A blueprint is a useful analogy: it lets people coordinate before construction. The limit is runtime behavior. Permissions, races, failure recovery, and acceptable cost need implementation and evidence beyond a drawing or schema.

We will review the existing create-post shape below. The OpenAPI excerpt is a teaching proposal, not a newly installed contract file or generator.

## Proposed OpenAPI excerpt

```yaml
openapi: 3.1.1
info:
  title: Instagram API teaching contract
  version: 1.0.0
paths:
  /api/posts:
    post:
      operationId: createPost
      security:
        - bearerAuth: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required: [caption, imageUrl, locationLabel]
              properties:
                caption: {type: string, minLength: 1, maxLength: 500}
                imageUrl: {type: string, minLength: 1}
                locationLabel: {type: string, minLength: 1, maxLength: 120}
      responses:
        '201':
          description: Post created; full response schema omitted in this excerpt
        '400':
          description: Malformed or invalid request
        '401':
          description: Valid authentication required
components:
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
```

This excerpt intentionally omits response bodies and error schemas; it is not a complete production contract. `info.version` is the description’s version, not automatic route version selection. `operationId` should be stable and unique for generated tooling. The security scheme documents a requirement; it does not install Spring authentication.

Schema minLength=1 does not reproduce @NotBlank: a string of spaces has positive length. JSON Schema string-length semantics can also differ from Java UTF-16 length for supplementary characters. Record these differences in examples and implement agreed validation; a generated client alone may not enforce server rules.

## Spring Boot implementation boundary

```java
@PostMapping(path = "/api/posts", consumes = "application/json")
@ResponseStatus(HttpStatus.CREATED)
FeedPostResponse create(
        @RequestHeader("Authorization") String authorization,
        @Valid @RequestBody CreatePostRequest request) {
    return postService.create(authorization, request);
}
```

This proposed excerpt mirrors the current create shape, with explicit media-type intent. Annotations implement pieces of the contract: binding, validation, and status. Service and database rules implement ownership, normalization, and persistence. Generated interfaces can reduce drift in parameter/response shape; they cannot supply those business guarantees.

## Review a concrete operation in order

1. **Intent and identity:** create a post in a collection; server assigns ID. Does it reference authorized uploaded media?
2. **Inputs:** required fields, length units, blank/null rules, normalization, unknown fields, maximum bytes, and media type.
3. **Outputs:** 201 DTO, chosen fields, ID type, timestamp format, no secret fields; Location only if its promised identity/read behavior is supported.
4. **Failures:** invalid body, missing/invalid auth, inaccessible media, and unexpected storage outage; stable error shape and safe details.
5. **Repeats and races:** response lost after commit, same-key replay if offered, concurrent unique constraints, outgoing notifications.
6. **Operations:** request limits, dependency budgets, correlation, bounded metric labels, and capacity failure behavior.
7. **Evolution:** old clients, schema diff, rollout order, monitoring, and rollback-compatible storage.

## Worked review failure

A generated schema marks caption as optional, while Spring requires it nonblank. Client generator permits an empty object -> caller sends it -> 400 -> team thought both implementations were “correct.” The contract was inconsistent. Correct the schema and examples, verify provider behavior and a representative consumer, and avoid claiming a schema diff can prove the entire operation.

Similarly, a response schema can permit id:number but omit the JavaScript precision risk; it can describe status 201 but not prove a row committed. Pair structural checks with integration/state assertions. Keep examples validated against the schema where tooling supports it, and add old-consumer fixtures for changes.

## Let the description and provider disagree

On paper, leave `imageUrl` in the description but rename it to `mediaUrl` in the provider. Predict the result for an old client: the response may still be valid JSON with status 201, but the promised image field is gone.

Detect the mismatch with provider verification and an old-consumer fixture. Contain the release, restore the supported field or a migration adapter, and verify both description and running behavior agree. Prevent recurrence by reviewing contract differences together with behavioral checks. Editing the schema to bless the incompatible rename only makes the documents agree; it does not repair the caller's supported contract.

## Project mapping and practice

[CreatePostRequest](../../../../src/main/java/com/instagram/backend/dto/CreatePostRequest.java), [FeedPostResponse](../../../../src/main/java/com/instagram/backend/dto/FeedPostResponse.java), [PostController](../../../../src/main/java/com/instagram/backend/controller/PostController.java), and [integration tests](../../../../src/test/java/com/instagram/backend/AuthIntegrationTests.java) provide actual contract evidence. No OpenAPI generator or new contract file is installed by this lesson. For F-017, draft create/read/delete operations and have a caller walkthrough before implementing. Consult the primary [OpenAPI specification](https://spec.openapis.org/oas/v3.1.1.html).

## Interview-ready answer

I start with a concrete caller flow and specify intent, identity, fields, response examples, and failure semantics before choosing controller signatures. I use OpenAPI for a shared structural description and tooling, but review normalization, permissions, retries, races, and operational bounds separately. I verify the provider against the contract and keep meaningful consumer fixtures. In this project the DTOs and tests expose current behavior; the next comments feature can begin with a reviewed contract and then prove it through Spring integration tests and API requests.

## Before moving on

Review create from the caller's request through committed state. Continue when you can point to one schema promise, one behavior it cannot prove, and the evidence needed to verify each.

Attempt the questions before opening their answers and record the explanation in [learner progress](learner-progress.md). This revision leaves learning status unchanged; a finished document is not evidence that you have demonstrated the idea.

## Questions and explained answers

<details>
<summary>1. Does an OpenAPI bearerAuth scheme secure a Spring route?</summary>

No. It documents expected authentication. The application must implement and test token validation and domain authorization.

</details>

<details>
<summary>2. Why does minLength=1 not mean @NotBlank?</summary>

A whitespace string has positive length but is blank. Java and schema length units can also differ for some Unicode characters; document and test the intended rule.

</details>

<details>
<summary>3. Does info.version choose /api/v2 automatically?</summary>

No. It identifies the API description version. Actual version selection requires paths, parameters, headers, or negotiation configured by the server.

</details>

<details>
<summary>4. What can schema verification miss?</summary>

Object ownership, atomic writes, idempotency replay, query cost, timeout behavior, and many semantic compatibility changes. These require review and behavioral evidence.

</details>

<details>
<summary>5. What is the first review for a comments endpoint?</summary>

Walk a real create/read/delete flow and agree on identity, ownership, inputs, responses, pagination, errors, and repeat behavior before controller implementation.

</details>
