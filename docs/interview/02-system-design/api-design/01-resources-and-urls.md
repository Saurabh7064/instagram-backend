# 01. Resources and URL design

## Question

How do you choose resource URLs for posts and likes?

[Module sequence](README.md) · Artifact: READY · Learning: TODO

## What you will learn

Explain why a post keeps its identity when storage changes, choose paths for items and relationships, and show which checks a nested URL still requires.

Prerequisite: the request model is explained below; no REST vocabulary is assumed.

## Give the client an address it can keep

Start with the basic exchange. The client app sends an HTTP (Hypertext Transfer Protocol) request to the server. Its URL (Uniform Resource Locator) is the address; its method is the instruction, such as GET for reading. The server replies with a status and supporting headers, and usually a body. These examples use JSON (JavaScript Object Notation), a text format for named fields and lists, for that body.

Suppose Mira opens post 42 and copies its link. Tomorrow you rename the database table or move image storage to another service. Her saved link should still identify the same post. The client needs an address for the thing it cares about, rather than a description of how you happen to store it.

That identifiable thing is a **resource**. Here, post 42 is the resource; the JSON you return is its **representation**, a description of the post at the time of the request. Keeping those ideas separate matters: you can add a field to the JSON without giving the post a new identity.

Start with `GET /api/posts/42`: “show me this post.” Compare it with `GET /api/posts?author=mira.frames`: “show me posts selected by this author.” The first path identifies one item. The second identifies a collection and uses a query parameter, a named choice after `?`, to select members. Neither address needs to expose a table name.

A library catalogue provides a useful comparison: a book's catalogue number remains useful when its shelf changes. The limit of that comparison is permission. Knowing a post's address does not mean the caller may read it; the server still has to decide visibility.

## A proposed route for one identifiable post

The project uses [Spring Boot 4.0.2 and Java 24](../../../../build.gradle). The excerpt below is a design proposal unless identified as current code; omitted imports and collaborators still need implementation.

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

### Read the boundary in the code

Read the two mappings together. `@RequestMapping("/api/posts")` supplies the collection prefix and `@GetMapping("/{postId}")` adds one item's identity. `@PathVariable` turns the path text into the `Long` argument; `@RestController` makes the returned object a response body.

`postQueries.findVisible` is a proposed query service, not a repository handed directly to the caller. A real implementation must supply the authenticated viewer and enforce visibility there. The route annotation matches an address; it cannot establish that permission.

## Follow the address all the way to the post

For `GET /api/posts/42`, Spring first matches the item route and converts the path text `42` into the numeric argument. The proposed query service then asks whether this caller may see post 42. If the post is available under that policy, it returns its representation; otherwise it follows the documented 404 policy. Reading the address does not itself change the post.

Now consider `/api/posts/42/comments/9`. The address claims comment 9 belongs to post 42. If the stored comment belongs to another post, returning it anyway would ignore part of the request. The service must verify membership as well as visibility. That extra check follows from what the path says, rather than from the fact that both IDs are valid numbers.

A literal path such as `/api/posts/search` needs an explicit route. It is a different request from an item whose ID happens to be numeric.

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

Choose nouns for stable domain concepts, but do not invent fake resources merely to avoid every verb. A command that has side effects beyond replacing a representation is often clearer as POST with an explicit operation contract. Representational State Transfer (REST), the architectural style often referenced for these APIs, also includes resource representations, stateless requests, cache semantics, and discoverable controls; plural nouns alone do not make a design RESTful.

## Apply this to the backend

[PostController](../../../../src/main/java/com/instagram/backend/controller/PostController.java) currently offers create, edit, delete, like, and feed operations. It does not expose the proposed GET-by-ID or comments routes. Use that distinction when discussing what a saved post URL would promise.

For F-017 comments, sketch the list and item addresses. Explain how the service proves that a requested comment belongs to its parent, and decide what happens to comments when the parent is deleted. This remains a design exercise; implementing it would require the repository's feature verification workflow.

## Useful later: choices and costs

Use nested collections for ownership or relationships, such as /posts/42/comments. Avoid deeply nested paths when a comment has its own stable identity. Flat /comments/9 is easier to relocate but must still check access. Action endpoints remain reasonable for commands such as publish when a resource model adds no clarity.

## Try a URL that lies about membership

This is a paper exercise, not an executed lab. Suppose comment 9 is stored under post 77, but the caller requests `/posts/42/comments/9`. Predict the result if the service loads only comment ID 9: it can return a comment that is not a member of the requested collection, potentially bypassing a parent-specific access check.

A future integration test should detect the mismatch by asserting the documented unavailable result and no leaked comment body. Contain a faulty nested route until it checks both IDs. Restore correct reads by constraining membership and applying visibility, then test the legitimate parent too. The correction is not a different URL spelling; it is enforcing the relationship that the URL promised.

## Interview-ready answer

I begin with user-visible identity and actions rather than table names. I use item paths for identity, query parameters for selection, and explicit writable representations for mutations. Nesting can express collection membership, but the service must verify that membership and caller visibility. For relationships with known identity I can use desired-state operations; for a workflow I may use POST with a clear operation result. In this project I would add comments under a post for listing, give comments stable item identities, and test mismatched parent IDs as well as missing objects. Resource naming is one part of the contract; methods, permissions, errors, and bounded collection behavior complete it.

## Before moving on

Explain why post 42 keeps its address after a storage change, then walk the mismatched-parent request. Continue when you can locate identity, selection, and writable state and justify where permission is checked.

Attempt the questions before opening their answers and record the explanation in [learner progress](learner-progress.md). This revision leaves learning status unchanged; a finished document is not evidence that you have demonstrated the idea.

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
