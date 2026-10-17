# 01. Resources and URL design

## Question

How do you choose resource URLs for posts and likes?

[Module sequence](README.md) · Estimated time: 15–20 minutes · Artifact: READY · Learning: TODO

## What you will learn

Explain the problem behind resources and url design, trace its externally visible behavior, and identify one implementation risk before proposing a change.

Prerequisites: A client sends an HTTP request containing a method and URL; a server returns a status, headers, and optional body. JSON is a text format for objects and lists.. All later lessons remain TODO until their own checkpoints are attempted.

## Must understand: problem and model

A client needs stable addresses even when database tables change. An API is a contract between caller and server. A resource is something the caller can identify; its representation is the JSON description returned today. Name resources with nouns and identify individual items with stable IDs. A URL is like a library shelf address; the analogy stops because one resource can have several representations and access rules.

## Spring Boot example

Unless explicitly described as existing code, this is a proposed teaching excerpt, not an endpoint implemented by this documentation change. Application-specific collaborators and types require implementation; imports and class wiring are omitted. Preserve authentication when adapting an excerpt. This project uses Spring Boot 4.0.2 and Java 24, as recorded in [build configuration](../../../../build.gradle).

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

## Project mapping and practice

Read the actual [backend implementation](../../../../src/main/java/com/instagram/backend/controller/PostController.java) before changing it. The example above distinguishes existing behavior from a proposed extension; a linked file establishes the current starting point, not proof that the proposed design exists.

**Practice task:** Propose resource paths for F-017 comments; explain whether deleting a parent should delete or retain comments.

Keep this a design exercise until a feature change is explicitly undertaken. An implementation must follow the repository integration-test, API verification, documentation, and commit workflow.

## Useful later: choices and costs

Use nested collections for ownership or relationships, such as /posts/42/comments. Avoid deeply nested paths when a comment has its own stable identity. Flat /comments/9 is easier to relocate but must still check access. Action endpoints remain reasonable for commands such as publish when a resource model adds no clarity.

## Controlled failure drill

Predict what happens when a mobile client stores a database row position as its ID. Delete an earlier row: the position can identify a different object. Detect wrong-item reports, contain by disabling positional URLs, recover with stable identifiers, prevent with contract tests that reorder data.

Describe the prediction before reading the symptom. In a future lab, use disposable data, record the observed response and state, and verify recovery rather than assuming it succeeded.

## Interview-ready answer outline

For a two-minute answer, start with the caller’s problem, define the main concept in plain language, explain the request trace above, then justify one choice and its cost. Finish by explaining how the failure drill would be detected and prevented in this backend. Do not claim the proposed extension is deployed.

## Stop/go and learner evidence

Proceed only after explaining the request trace without notes, predicting the failure, and answering all three checks below before opening their answers. Record the attempt in [learner progress](learner-progress.md). Artifact readiness does not establish comprehension or implementation readiness.

## Questions and explained answers

<details>
<summary>1. Should the URL expose a table name or a client-visible resource?</summary>

Expose a client-visible resource. /api/posts/42 stays meaningful when storage is renamed or split. A URL does not grant access: the query service still checks visibility.

</details>

<details>
<summary>2. What exact response or state should the worked request produce, and why?</summary>

GET /api/posts/42 -> Spring matches the item route -> converts 42 -> checks visibility in the proposed query service -> returns post 42 or 404. GET /api/posts/search must have an explicit route rather than accidentally becoming a numeric ID.

This follows from the request boundary and state checks in the walkthrough, not merely from choosing a URL.

</details>

<details>
<summary>3. Which failure can remain despite a successful happy-path request, and how would you prevent it?</summary>

Predict what happens when a mobile client stores a database row position as its ID. Delete an earlier row: the position can identify a different object. Detect wrong-item reports, contain by disabling positional URLs, recover with stable identifiers, prevent with contract tests that reorder data.

A successful request proves one case. The controlled failure needs its own observation and regression check.

</details>
