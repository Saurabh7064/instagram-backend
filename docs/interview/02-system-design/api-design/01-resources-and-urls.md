# 01. Resources and URL design

## Question

How do you choose resource URLs for posts and likes?

[Module sequence](README.md) · Study in two passes: core model, then design decisions (15–20 minutes each) · Artifact: READY · Learning: TODO

## What you will learn

Separate resource identity, collection selection, and writable state; compare nesting, relationship resources, and commands; reject mismatched parent/child IDs.

Prerequisites: A client sends an HTTP request containing a method and URL; a server returns a status, headers, and optional body. JSON is a text format for objects and lists.. All later lessons remain TODO until their own checkpoints are attempted.

## Must understand: problem and model

A client needs stable addresses even when database tables change. An API is a contract between caller and server. A resource is something the caller can identify; its representation is the JSON description returned today. Name resources with nouns and identify individual items with stable IDs. A URL is like a library shelf address; the analogy stops because one resource can have several representations and access rules.

## Spring Boot example

Examples are proposed except where explicitly identified as current code. Imports/wiring are omitted; named new services and DTOs need implementation. Preserve current authorization rules. The project uses [Spring Boot 4.0.2 and Java 24](../../../../build.gradle).

```java
@RestController
@RequestMapping("/api/posts")
class PostRoutes {
    @GetMapping("/{postId}")
    FeedPostResponse find(@PathVariable Long postId) {
        return postQueries.findVisible(postId);
    }
}
```

### Walkthrough

`@RestController` makes method results response bodies. The class mapping supplies the collection prefix; the method mapping appends an item ID. `@PathVariable` converts the path text to a Long. `postQueries` is a proposed visibility-checking service, not a repository exposed directly to callers.

## Request trace: predict, act, observe, explain

Before following the trace, predict the status and final stored state. Then trace these transitions in order:

GET /api/posts/42 -> Spring matches the item route -> converts 42 -> checks visibility in the proposed query service -> returns post 42 or 404. GET /api/posts/search must have an explicit route rather than accidentally becoming a numeric ID.

The observable result includes the response **and** the final stored state. A successful status alone cannot prove concurrency or authorization correctness.

## Design decisions: resources, relationships, and commands

Start with the user’s intention, not the repository method. “Show a post,” “list its comments,” and “publish a draft” imply different boundaries. A resource is not necessarily one row: a feed page is a view across posts, authors, and the current viewer’s likes.

| Intention | Proposed contract | Why this shape | Cost or caution |
|---|---|---|---|
| Read one post | GET /api/posts/42 | Stable item identity | Visibility check on every read |
| List comments for it | GET /api/posts/42/comments | Collection scoped by parent | Verify parent is visible; paginate |
| Edit one comment | PATCH /api/comments/9 | Comment has its own identity | Check its actual parent and permissions |
| Ensure my like exists | PUT /api/posts/42/likes/me | Caller knows relationship identity | Uniqueness and concurrent writes still need enforcement |
| Start publishing workflow | POST /api/posts/42/publications | Creates an auditable attempt | Define lifecycle, replay, and status resource |

A nested path is a claim about membership, not proof. `/posts/42/comments/9` must reject comment 9 if it belongs to post 77. Loading only by comment ID ignores part of the contract. Conversely, nesting every ancestor creates fragile paths when content is moved or reorganized.

### Separate identity, selection, and representation

Put the stable identity in a path: `/posts/42`. Put collection selection in a query: `/posts?author=mira.frames&limit=20`. Put the proposed writable state in a body. Use headers for protocol metadata such as Authorization and If-Match. Do not put passwords or bearer tokens in URLs: URLs commonly enter browser history, proxy logs, and analytics.

Opaque public IDs can decouple external identity from database allocation; they add lookup/storage cost and do not replace authorization. Numeric IDs are acceptable when disclosure of sequence is tolerable. Never promise that IDs are contiguous or that a higher ID necessarily means a later creation timestamp.

### Spring Boot boundary worth preserving

```java
@GetMapping("/posts/{postId}/comments/{commentId}")
CommentView comment(@PathVariable Long postId,
                    @PathVariable Long commentId,
                    @RequestHeader("Authorization") String authorization) {
    return comments.readVisibleMember(authorization, postId, commentId);
}
```

Proposed excerpt: `CommentView` and `comments` are not implemented. The service must resolve the viewer, check parent visibility, and load a comment constrained by both IDs. A repository predicate such as `findByIdAndPostId` supports membership; it does not alone prove visibility. Missing/mismatched membership should use the documented missing-resource policy.

Choose nouns for stable domain concepts, but do not invent fake resources merely to avoid every verb. A command that has side effects beyond replacing a representation is often clearer as POST with an explicit operation contract. “REST” also includes resource representations, stateless requests, cache semantics, and discoverable controls; plural nouns alone do not make a design RESTful.

## Project mapping and practice

Read the actual [backend implementation](../../../../src/main/java/com/instagram/backend/controller/PostController.java) before changing it. The example above distinguishes existing behavior from a proposed extension; a linked file establishes the current starting point, not proof that the proposed design exists.

**Practice task:** Propose resource paths for F-017 comments; explain whether deleting a parent should delete or retain comments.

Keep this a design exercise until a feature change is explicitly undertaken. An implementation must follow the repository integration-test, API verification, documentation, and commit workflow.

## Useful later: choices and costs

Use nested collections for ownership or relationships, such as /posts/42/comments. Avoid deeply nested paths when a comment has its own stable identity. Flat /comments/9 is easier to relocate but must still check access. Action endpoints remain reasonable for commands such as publish when a resource model adds no clarity.

## Controlled failure drill

Predict what happens when a mobile client stores a database row position as its ID. Delete an earlier row: the position can identify a different object. Detect wrong-item reports, contain by disabling positional URLs, recover with stable identifiers, prevent with contract tests that reorder data.

Describe the prediction before reading the symptom. In a future lab, use disposable data, record the observed response and state, and verify recovery rather than assuming it succeeded.

## Interview-ready answer

I begin with user-visible identity and actions rather than table names. I use item paths for identity, query parameters for selection, and explicit writable representations for mutations. Nesting can express collection membership, but the service must verify that membership and caller visibility. For relationships with known identity I can use desired-state operations; for a workflow I may use POST with a clear operation result. In this project I would add comments under a post for listing, give comments stable item identities, and test mismatched parent IDs as well as missing objects. Resource naming is one part of the contract; methods, permissions, errors, and bounded collection behavior complete it.

## Stop/go and learner evidence

Proceed only after explaining the request trace without notes, predicting the failure, and answering all three checks below before opening their answers. Record the attempt in [learner progress](learner-progress.md). Artifact readiness does not establish comprehension or implementation readiness.

## Questions and explained answers

<details>
<summary>1. Should the URL expose a table name or a client-visible resource?</summary>

Expose a client-visible resource. /api/posts/42 stays meaningful when storage is renamed or split. A URL does not grant access: the query service still checks visibility.

</details>

<details>
<summary>2. Does /posts/42/comments/9 prove comment 9 belongs to post 42?</summary>

No. The server must check actual membership, for example through a query constrained by both IDs, and separately check visibility. Loading only by comment ID ignores part of the path contract.

</details>

<details>
<summary>3. Which pieces belong in path, query, headers, and body?</summary>

Stable identity in path; collection selection in query; protocol metadata such as Authorization/If-Match in headers; proposed writable state in body. Do not place credentials in URLs.

</details>

<details>
<summary>4. Do opaque IDs remove the need for authorization?</summary>

No. Guessing resistance can reduce enumeration but never establishes permission. Every access still needs the appropriate object/collection policy.

</details>

<details>
<summary>5. Must every business action be renamed into a noun?</summary>

No. A stable attempt resource can be useful for workflow history, but a clear POST command with defined replay/result rules may be simpler than an artificial resource model.

</details>
