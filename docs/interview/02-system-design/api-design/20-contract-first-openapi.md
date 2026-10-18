# 20. Contract-first design and an API review rubric

## Question

How do you turn an API proposal into an implementable, reviewable contract before writing controllers?

[Module sequence](README.md) · Artifact: READY · Learning: TODO · Suggested study: 20–30 minutes plus design exercise

## What you will learn

Write an OpenAPI operation, distinguish schema checks from behavior guarantees, and apply a release-review rubric grounded in this backend.

Prerequisites: the foundational contract, method, authorization, and compatibility lessons. These are proposed teaching examples; routes, collaborators, DTOs, and policies shown here are not installed by this documentation change. Imports and application wiring are omitted. Use the existing authentication rules when implementing any extension.

## Must understand: write the promise before implementation details

A UI developer and backend developer can both finish their work yet disagree about nulls, error codes, or pagination. Contract-first design writes the external agreement before implementation. OpenAPI is a machine-readable description of HTTP operations and schemas. Provider verification checks that the running provider satisfies selected documented promises.

A building blueprint helps people coordinate before construction. The analogy stops because a schema cannot prove authorization, concurrency, latency, or runtime data correctness; those require tests and observations.

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

## Controlled failure drill

Prediction: rename response imageUrl to mediaUrl in the provider while keeping the old description. Detect with provider contract verification and an old-consumer fixture, contain rollout, recover the old field or a migration adapter, prevent with contract diff review plus behavioral tests. A documentation-only rename cannot make an incompatible provider safe.

## Project mapping and practice

[CreatePostRequest](../../../../src/main/java/com/instagram/backend/dto/CreatePostRequest.java), [FeedPostResponse](../../../../src/main/java/com/instagram/backend/dto/FeedPostResponse.java), [PostController](../../../../src/main/java/com/instagram/backend/controller/PostController.java), and [integration tests](../../../../src/test/java/com/instagram/backend/AuthIntegrationTests.java) provide actual contract evidence. No OpenAPI generator or new contract file is installed by this lesson. For F-017, draft create/read/delete operations and have a caller walkthrough before implementing. Consult the primary [OpenAPI specification](https://spec.openapis.org/oas/v3.1.1.html).

## Interview-ready answer

I start with a concrete caller flow and specify intent, identity, fields, response examples, and failure semantics before choosing controller signatures. I use OpenAPI for a shared structural description and tooling, but review normalization, permissions, retries, races, and operational bounds separately. I verify the provider against the contract and keep meaningful consumer fixtures. In this project the DTOs and tests expose current behavior; the next comments feature can begin with a reviewed contract and then prove it through Spring integration tests and API requests.

## Stop/go and learner evidence

Explain the worked request without notes, justify a competing option, predict the failure, and describe a test that checks the final state or response. Record an attempt in [learner progress](learner-progress.md). A completed artifact does not establish understanding or an implemented feature.

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
