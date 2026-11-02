# 04. Request validation and write boundaries

## Question

Where should API input be validated?

[Module sequence](README.md) · Artifact: READY · Learning: TODO

## What you will learn

Follow one caption from submitted text to stored text, decide which layer owns each check, and explain why concurrent writes need database rules as well as validation.

Builds on [response contracts](03-status-and-response-contracts.md).

## A request can look sensible and still be invalid

Mira submits a caption containing 501 letters. The editor normally stops her at 500, but a caller can send an HTTP request without using that editor. The backend must enforce the rule itself before writing a post.

Start with the cheapest question: can we read the request at all? Next ask whether its individual values are acceptable, such as a nonblank caption no longer than 500. These are **field constraints**: rules you can usually check from the submitted value alone. Then ask questions that need knowledge of the application, such as whether the caller may use the referenced image. That is **business validation**.

Passing the first set does not imply passing the second. A nonblank image URL tells us only that some text was supplied; it says nothing about who owns the image or whether it exists. This is why one collection of annotations cannot settle every input rule.

It is similar to a clerk first checking that a form is filled in and then checking whether the applicant is eligible. The comparison stops when requests run together: two individually acceptable requests can conflict, so the database must enforce rules that must survive concurrent writes.

The [response lesson](03-status-and-response-contracts.md) explains why rejected input needs a predictable status. Here we work out where rejection should happen and what must remain unwritten.

## The current caption rules in Java

The project uses [Spring Boot 4.0.2 and Java 24](../../../../build.gradle). The excerpt below is a design proposal unless identified as current code; omitted imports and collaborators still need implementation.

```java
public record CreatePostRequest(
        @NotBlank @Size(max = 500) String caption,
        @NotBlank String imageUrl,
        @NotBlank @Size(max = 120) String locationLabel) {
}

// Controller parameter:
// @Valid @RequestBody CreatePostRequest request
```

### Read the boundary in the code

The record matches the current create request. `@NotBlank` rules out null, empty, and whitespace-only text. `@Size(max=500)` adds the caption length bound; it does not reject null on its own. The controller's `@Valid` is what asks Spring to evaluate these object constraints.

Those annotations describe properties of the supplied strings. A nonblank `imageUrl` can still refer to media the caller should not use. That needs a separate, explicit policy rather than a stronger-sounding string annotation.

## What happens to the caption before storage?

Keep the current maximum at 500. A submitted caption of 501 letters fails `@Size(max=500)`, so validation rejects it before the service creates a post. Three spaces fail `@NotBlank` even though their length is small. Neither request should leave a new row.

Now submit `" hi "`. It is nonblank and its original length is 4, so these checks pass. The current service trims it and stores `"hi"`, length 2. That order matters: 499 letters surrounded by two spaces have submitted length `499 + 2 = 501`, so they fail before trimming could reduce them to 499.

After these field checks, a referenced image still needs whatever existence and ownership policy the API declares. Do not infer permission from the fact that its URL is nonblank.

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

## Apply this to the backend

Compare the existing [CreatePostRequest](../../../../src/main/java/com/instagram/backend/dto/CreatePostRequest.java) constraints with [PostService.create](../../../../src/main/java/com/instagram/backend/service/PostService.java), which trims accepted strings. They establish the validation-before-normalization behavior worked through above; they do not establish a media-ownership protocol.

For F-017, write a comment's blank and length rules separately from permission to comment on its target post. State where each check runs and what stored state must remain unchanged when it fails.

## Useful later: choices and costs

Keep cheap format checks in DTOs and ownership or uniqueness checks in the service/database. Validating only in the UI cannot protect against curl. Excessively strict URL rules can block legitimate future media hosts; unrestricted URLs can allow unwanted external content. Decide permitted origins explicitly.

## Bypass the editor and send the 501-letter caption

Before a future lab, predict what should happen when the caller bypasses the editor and sends the same 501-letter caption. The backend must reject it, with no new post. If only the UI enforces the bound, the database may reject the oversized value later, producing a less useful error; a permissive storage column might accept it instead.

Detect either broken path with a direct API request and a fresh persistence check. Contain invalid writes at the backend boundary, then make its safe error match the declared rule. Recovery includes verifying no rejected row remains; do not assume an error response proves that. Prevent recurrence by testing exactly 500 and 501 under the intended normalization order. This describes verification to perform, not a run recorded by this lesson.

## Interview-ready answer

I validate by increasing cost: format and bounds, local DTO constraints, identity and domain policy, then durable invariants. @Valid and @NotBlank do not prove ownership or uniqueness. I keep server-managed fields out of writable DTOs and define normalization before comparing or hashing requests. This project trims caption after validation, so boundary-length behavior must be tested as it actually runs. For patch I preserve presence separately from null. A preliminary uniqueness lookup improves user feedback but cannot win a race, so the database must enforce the invariant and the application must translate known failures safely. I test missing, null, blank, limit boundaries, bypassed UI, and concurrent writes with no unexpected persisted state.

## Before moving on

Explain the stored result for `" hi "` and the rejection of 499 letters plus two surrounding spaces. Continue when you can separate field checks, permission, and a database rule that concurrent writers cannot bypass.

Attempt the questions before opening their answers and record the explanation in [learner progress](learner-progress.md). This revision leaves learning status unchanged; a finished document is not evidence that you have demonstrated the idea.

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
