# 04. Request validation and write boundaries

## Question

Where should API input be validated?

[Module sequence](README.md) · Study in two passes: core model, then design decisions (15–20 minutes each) · Artifact: READY · Learning: TODO

## What you will learn

Design a staged validation pipeline, distinguish missing/null/blank/normalized values, prevent mass assignment, and enforce concurrent invariants in the database.

Prerequisites: Lesson 3 and its stop/go check. All later lessons remain TODO until their own checkpoints are attempted.

## Must understand: problem and model

Invalid input should fail before it reaches durable storage. Structural validation checks shape and local limits; business validation checks meaning using current state. A constraint is a declared rule such as a maximum length. Think of a form checking required boxes before a clerk checks eligibility; the analogy stops because concurrent database changes require atomic enforcement.

## Spring Boot example

Examples are proposed except where explicitly identified as current code. Imports/wiring are omitted; named new services and DTOs need implementation. Preserve current authorization rules. The project uses [Spring Boot 4.0.2 and Java 24](../../../../build.gradle).

```java
public record CreatePostRequest(
        @NotBlank @Size(max = 500) String caption,
        @NotBlank String imageUrl,
        @NotBlank @Size(max = 120) String locationLabel) {
}

// Controller parameter:
// @Valid @RequestBody CreatePostRequest request
```

### Walkthrough

This record matches the existing request. `@NotBlank` rejects null, empty, and whitespace-only strings; `@Size` limits length but alone permits null. `@Valid` activates object validation at the controller boundary. An arbitrary nonblank imageUrl still passes these annotations: ownership, allowed media origin, and file existence need separate policies.

## Request trace: predict, act, observe, explain

Before following the trace, predict the status and final stored state. Then trace these transitions in order:

Caption containing 501 letters -> @Size fails -> 400 -> service not called. Caption containing three spaces -> @NotBlank fails. Caption " hi " passes -> current service trims -> stores "hi". A well-shaped request for another person’s media still needs business authorization.

The observable result includes the response **and** the final stored state. A successful status alone cannot prove concurrency or authorization correctness.

## Validation is a pipeline, not one annotation

Apply checks in increasing cost order: request size/media type -> parse and bind -> local field constraints -> identity and permissions -> state-dependent business rules -> database invariants. A database invariant is a condition that must hold for every committed row, even when callers race.

| Check | Example | Boundary | What it cannot prove |
|---|---|---|---|
| Transport limit | Upload <= documented byte limit | Proxy and servlet multipart configuration | Actual safe image content |
| Parse/type | postId is a Long | Spring binding | Post exists |
| Field | caption nonblank, <=500 | DTO + @Valid | Caller owns the referenced media |
| Cross-field | start precedes end | Class-level validator/service | No competing reservation |
| Business | post permits comments | Service under transaction | Uniqueness under an unchecked race |
| Durable invariant | Unique user/post like pair | Database constraint | Safe application error translation |

### Omitted, null, blank, and normalized are different

For the current `UpdatePostRequest`, `{}` binds missing strings to null and fails @NotBlank. `{"caption":null,"locationLabel":"Home"}` also fails. `{"caption":"  ","locationLabel":"Home"}` fails because whitespace is blank. `" hi "` passes and the service trims it to `"hi"`. The annotation checks the original length: 499 letters plus two surrounding spaces has length 501 and fails even though trimming would make it 499. Decide whether that ordering is intended before adding normalization.

A patch must distinguish “not supplied” from “supplied null”; a plain nullable String often loses that information. Do not copy the full-update DTO into a PATCH and simply remove @NotBlank. See [method and patch semantics](02-http-methods.md).

### Avoid mass assignment

Mass assignment means binding caller-supplied fields directly onto a persistence object, allowing unintended writes. A proposed comment create DTO should contain content, not authorId, deleted, moderationStatus, or likeCount. Set owner from the token and initialize counters server-side. Reject or deliberately ignore unknown fields according to a documented policy; ignoring a protected property is not equivalent to allowing it to mutate state.

### A check-then-save race

A and B both ask “does username mira exist?” and both get false. Both attempt insertion. An application check cannot prevent this interleaving; a unique database constraint must arbitrate it. Map the known username constraint violation to a documented conflict, without converting all integrity violations to “username taken.” Other failures may be programming bugs.

Validation failures should identify safe field names and stable codes. Never return the rejected password/token value. Tests need null, missing, blank, exactly-at-limit, one-over-limit, malformed JSON, and state-dependent rejection with no persisted write. Check Unicode policy explicitly: Java String length counts UTF-16 code units, which can differ from visible characters or code points.

## Project mapping and practice

Read the actual [backend implementation](../../../../src/main/java/com/instagram/backend/dto/CreatePostRequest.java) before changing it. The example above distinguishes existing behavior from a proposed extension; a linked file establishes the current starting point, not proof that the proposed design exists.

**Practice task:** For F-017 comments, define blank/length rules and distinguish them from permission to comment on the target post.

Keep this a design exercise until a feature change is explicitly undertaken. An implementation must follow the repository integration-test, API verification, documentation, and commit workflow.

## Useful later: choices and costs

Keep cheap format checks in DTOs and ownership or uniqueness checks in the service/database. Validating only in the UI cannot protect against curl. Excessively strict URL rules can block legitimate future media hosts; unrestricted URLs can allow unwanted external content. Decide permitted origins explicitly.

## Controlled failure drill

Predict sending 501 letters with a client that bypasses UI validation. Without backend validation, oversized values reach persistence. Detect database length errors, contain with request limits, recover by rejecting invalid writes consistently, prevent with boundary tests at 500 and 501.

Describe the prediction before reading the symptom. In a future lab, use disposable data, record the observed response and state, and verify recovery rather than assuming it succeeded.

## Interview-ready answer

I validate by increasing cost: format and bounds, local DTO constraints, identity and domain policy, then durable invariants. @Valid and @NotBlank do not prove ownership or uniqueness. I keep server-managed fields out of writable DTOs and define normalization before comparing or hashing requests. This project trims caption after validation, so boundary-length behavior must be tested as it actually runs. For patch I preserve presence separately from null. A preliminary uniqueness lookup improves user feedback but cannot win a race, so the database must enforce the invariant and the application must translate known failures safely. I test missing, null, blank, limit boundaries, bypassed UI, and concurrent writes with no unexpected persisted state.

## Stop/go and learner evidence

Proceed only after explaining the request trace without notes, predicting the failure, and answering all three checks below before opening their answers. Record the attempt in [learner progress](learner-progress.md). Artifact readiness does not establish comprehension or implementation readiness.

## Questions and explained answers

<details>
<summary>1. Does @NotBlank prove an image URL belongs to the caller?</summary>

No. It rejects null, empty, and whitespace-only strings. URL origin, media existence, and permission are separate rules requiring a business-policy check.

</details>

<details>
<summary>2. What happens to 499 letters plus one space at each end in the current caption DTO?</summary>

Its original Java String length is 501, so @Size(max=500) fails before service trimming. It does not become an accepted 499-character caption unless normalization order is deliberately changed.

</details>

<details>
<summary>3. Why cannot a username existence check replace a unique constraint?</summary>

Two transactions can both observe absence before either inserts. Only durable concurrency enforcement arbitrates the competing writes; map the known constraint deliberately.

</details>

<details>
<summary>4. What is mass assignment in a post API?</summary>

Binding caller input directly onto an entity can let them write authorId, role, counters, or moderation fields. A narrow DTO plus server-owned assignments avoids granting those capabilities.

</details>

<details>
<summary>5. Does String.length count visible characters?</summary>

It counts UTF-16 code units. Some visible characters use multiple code units, and combining sequences complicate it further. Specify the length unit and test representative Unicode.

</details>
