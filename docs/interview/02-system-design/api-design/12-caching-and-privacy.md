# 12. Caching and private representations

## Question

When can an API response be cached safely?

[Module sequence](README.md) · Artifact: READY · Learning: TODO

## What you will learn

Decide who may reuse a response, then decide when it becomes stale and how to check it. Use two viewers to spot a cache policy that returns personal state to the wrong person.

Builds on [representation validators](11-conditional-updates.md).

## The same URL can mean different data for two people

User A reads post 42 and the JSON includes `likedByViewer:true`. User B reads the same URL but has never liked it, so their correct value is false. A cache that stores A's entire response by URL and hands it to B will return personal state belonging to the wrong viewer.

A **cache** retains a response so a later request can reuse it. That can save network traffic and server work, but only when the saved response is appropriate for the later caller. Before asking how long a response should live, ask who may reuse it. A personalized feed and a genuinely public post view need different policies.

**Cache-Control** is the HTTP header that states storage and reuse rules. `no-store` tells compliant caches not to retain this response. `private` permits storage by a private cache, such as the browser, but prohibits shared-cache storage. Neither phrase means “make the data inaccessible after the user has received it.”

A photocopy is a useful starting analogy: keeping it avoids asking for the original again. The limit is that an HTTP cache may serve many users automatically, and a copy can become out of date. We need separate decisions about who may store it, how long it may be reused, and when to check the original.

The [ETag lesson](11-conditional-updates.md) introduced a representation identifier. Here we use validators to check a saved read rather than protect an edit.

## State a sensitive response's storage policy

The project uses [Spring Boot 4.0.2 and Java 24](../../../../build.gradle). The excerpt below is a design proposal unless identified as current code; omitted imports and collaborators still need implementation.

```java
@GetMapping("/me")
ResponseEntity<MeResponse> me(
        @RequestHeader("Authorization") String authorization) {
    return ResponseEntity.ok()
            .cacheControl(CacheControl.noStore())
            .body(profileQueries.me(authorization));
}
```

### Read the boundary in the code

This proposed variant returns sensitive identity data with `Cache-Control: no-store`. It tells compliant caches not to retain the response. `profileQueries` is illustrative; the existing profile service remains the place to inspect current behavior.

Changing this to `private` would permit private-cache storage. It would not also define how long that copy stays fresh or how account switching is isolated. Those are separate decisions, so choose a policy from the data's reuse requirements rather than the apparent convenience of one header.

## First decide whose copy this is

A requests post 42 and receives `likedByViewer:true`. B's correct representation has `likedByViewer:false`. If a shared cache ignores that distinction and looks up only `/api/posts/42`, it can serve A's saved body to B. Even when the post itself is public, that full representation is personalized.

A `no-store` policy prevents compliant caches from retaining the new response. A private cache policy can support a different design, but still needs correct viewer separation and rules for logout or account switching. A public view that omits viewer-specific fields is another deliberate contract, not something a cache may infer by deleting a field.

Once reuse is safe for the intended caller, ask how long an old copy may be used. That is a separate freshness decision; the timed example below shows why an ETag does not itself require checking on every read.

## Freshness and validation are separate decisions

| Directive | Meaning | Example fit |
|---|---|---|
| no-store | Do not store response | Tokens, sensitive identity data |
| private | Shared caches must not store | Personalized feed in a browser cache |
| no-cache | May store, must validate before reuse | Private content needing frequent correctness checks |
| max-age=60 | Fresh for 60 seconds under cache rules | Deliberately slightly stale representation |
| s-maxage=60 | Shared-cache freshness override | Public shared representation |

`no-cache` does not mean “never cache.” A private response with `max-age=60` can remain stale for that interval; an ETag is not automatically checked on every request while a stored response remains fresh. Authorization-related shared caching has additional HTTP rules; avoid using a public directive on personalized payloads just to get a CDN hit.

### Work a conditional read

At t=0, GET a proposed public post representation -> 200, body caption=old, ETag="p42-v7", Cache-Control: public,max-age=60. At t=30 a compliant cache may reuse it without contacting origin. At t=61 the client revalidates with If-None-Match: "p42-v7". If unchanged, origin returns 304 with appropriate metadata and no body; the client reuses the stored body. If edited to version 8, return 200 with the new body and validator. That 30-second stale window is a deliberate product choice, not a cache bug.

A personalized representation could instead use private,no-cache with a validator scoped to the selected viewer representation. Authenticate and check visibility before returning 304; otherwise a user whose access was revoked might keep treating protected content as valid. Network cache invalidation also does not revoke a copy already downloaded or saved by the caller.

### Proposed Spring read boundary

```java
@GetMapping("/public/posts/{id}")
ResponseEntity<PublicPostView> read(@PathVariable Long id,
                                    WebRequest request) {
    var view = publicPosts.readPublic(id);
    String etag = view.strongEtag();
    if (request.checkNotModified(etag)) {
        return null;
    }
    return ResponseEntity.ok().eTag(etag)
            .cacheControl(CacheControl.maxAge(Duration.ofSeconds(60))
                    .cachePublic())
            .body(view);
}
```

Proposed public-only route and types. `readPublic` checks the object is actually public before conditional evaluation. `checkNotModified` can complete a not-modified response; returning null here signals no body. The validator must accurately cover the public representation. Test emitted metadata on both paths rather than assuming an ETag helper supplies the entire policy.

If representation selection depends on Accept, set Vary: Accept where required so caches do not mix JSON and another format. Vary is a cache-selection instruction, not an access-control boundary. For media, content-addressed immutable URLs avoid overwriting bytes behind a supposedly immutable URL; private media needs an access and expiry strategy as well.

## Apply this to the backend

[FeedPostResponse](../../../../src/main/java/com/instagram/backend/dto/FeedPostResponse.java) contains `likedByViewer`, and [PostService.toResponse](../../../../src/main/java/com/instagram/backend/service/PostService.java) derives it using the current viewer. That is the concrete reason the same feed body cannot be assumed public for every caller.

For F-016, classify each response field before proposing caching. Decide whether to isolate the personalized response or define a separate public view, then specify a two-user check and a changed-permission check. The example policies above are proposals, not measured cache behavior.

## Useful later: choices and costs

Use no-store for sensitive identity/token responses; private caching may help personalized reads with a deliberate freshness policy. Public immutable media can use long lifetimes if access policy allows it and content changes get new URLs. A 304 has no representation body and requires correct validator evaluation for the selected user-specific representation.

## Send A's saved body to B

In a future two-user cache test, first request post 42 as A, who has liked it, then as B, who has not. Predict B's body: `likedByViewer:false`. A true value shows that the shared response crossed viewer boundaries, even if the post's other fields are public.

Contain reuse of the affected representation and purge entries where that is supported. A new no-store header does not itself erase old cached copies or revoke material already downloaded. Recovery means verifying the corrected path with both viewers and the intended storage policy. Prevent recurrence by separating genuinely public views from personalized ones and testing the cache's actual selection behavior, not only the presence of a header in a direct-origin response.

## Interview-ready answer

I classify data before caching it. Tokens and sensitive identity responses normally need no-store; personalized representations require private policy and deliberate validators. no-cache allows storage but requires validation, while max-age permits a freshness interval. I trace fresh reuse and stale revalidation separately, and check access before a protected 304. A shared feed cache is unsafe if it includes likedByViewer without correct isolation. For public media I can use immutable content identities when the access policy permits it. I test two viewers, changing access, ETags, metadata on 304, and representation selection through caches rather than assuming one header makes the whole design safe.

## Before moving on

Explain what B must receive after A's read, then distinguish freshness from validation. Continue when you can predict the 30-second reuse and later 304 without treating cache headers as access control.

Attempt the questions before opening their answers and record the explanation in [learner progress](learner-progress.md). This revision leaves learning status unchanged; a finished document is not evidence that you have demonstrated the idea.

## Questions and explained answers

<details>
<summary>1. Does Cache-Control: private mean never store the response?</summary>

No. It prevents shared-cache storage but allows a private cache to store it subject to the remaining policy. no-store instructs caches not to retain the response. Viewer-specific fields also require correct isolation.

</details>

<details>
<summary>2. What happens at 30 seconds with max-age=60?</summary>

A compliant cache may reuse the fresh response without revalidation. The ETag does not force an origin check while it remains fresh under the policy.

</details>

<details>
<summary>3. Why check visibility before a conditional 304?</summary>

Otherwise a caller who lost access can receive a response that treats their stored protected representation as still valid. Conditional optimization must not bypass authorization.

</details>

<details>
<summary>4. What does Vary:Accept protect against?</summary>

It informs caches that representation selection depends on Accept, helping prevent format/version mixing. It is not an authorization boundary or a private-data isolation policy by itself.

</details>

<details>
<summary>5. Does no-store erase content a user already downloaded?</summary>

No. It instructs compliant caches not to retain the response. It cannot revoke screenshots, saved files, malicious copies, or all previously stored content.

</details>
