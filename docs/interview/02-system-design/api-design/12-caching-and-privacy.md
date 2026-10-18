# 12. Caching and private representations

## Question

When can an API response be cached safely?

[Module sequence](README.md) · Study in two passes: core model, then design decisions (15–20 minutes each) · Artifact: READY · Learning: TODO

## What you will learn

Separate storage, freshness, revalidation, and viewer isolation; derive 200/304 behavior and review cache keys for negotiated representations.

Prerequisites: Lesson 11 and its stop/go check. All later lessons remain TODO until their own checkpoints are attempted.

## Must understand: problem and model

Repeated reads waste bandwidth, but caching the wrong viewer’s response can expose private state. Cache-Control sets storage/reuse policy. A validator lets a server check whether a cached representation is still current. Think of a dated photocopy; the analogy stops because shared caches serve multiple people and need explicit isolation.

## Spring Boot example

Examples are proposed except where explicitly identified as current code. Imports/wiring are omitted; named new services and DTOs need implementation. Preserve current authorization rules. The project uses [Spring Boot 4.0.2 and Java 24](../../../../build.gradle).

```java
@GetMapping("/me")
ResponseEntity<MeResponse> me(
        @RequestHeader("Authorization") String authorization) {
    return ResponseEntity.ok()
            .cacheControl(CacheControl.noStore())
            .body(profileQueries.me(authorization));
}
```

### Walkthrough

This proposed variant demonstrates a sensitive-response policy. no-store instructs caches not to store this response. private permits private-cache storage but prohibits shared-cache storage; it does not itself require freshness validation. profileQueries is illustrative; inspect the existing profile controller/service before implementation.

## Request trace: predict, act, observe, explain

Before following the trace, predict the status and final stored state. Then trace these transitions in order:

User A loads a feed item with likedByViewer=true. A shared cache keyed only by URL gives it to B -> B sees incorrect personal state. With no-store, that response is not retained by compliant caches. A separately designed public representation could be cached without viewer-specific fields.

The observable result includes the response **and** the final stored state. A successful status alone cannot prove concurrency or authorization correctness.

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

## Project mapping and practice

Read the actual [backend implementation](../../../../src/main/java/com/instagram/backend/dto/FeedPostResponse.java) before changing it. The example above distinguishes existing behavior from a proposed extension; a linked file establishes the current starting point, not proof that the proposed design exists.

**Practice task:** For F-016 classify every feed field as public or viewer-specific before proposing caching; likedByViewer prevents a shared full-response cache.

Keep this a design exercise until a feature change is explicitly undertaken. An implementation must follow the repository integration-test, API verification, documentation, and commit workflow.

## Useful later: choices and costs

Use no-store for sensitive identity/token responses; private caching may help personalized reads with a deliberate freshness policy. Public immutable media can use long lifetimes if access policy allows it and content changes get new URLs. A 304 has no representation body and requires correct validator evaluation for the selected user-specific representation.

## Controlled failure drill

Predict sharing a cached /api/me across tokens. Detect two-user cache tests, contain with no-store and purge, recover exposed cache entries, prevent by separating public and personalized representations and reviewing cache keys.

Describe the prediction before reading the symptom. In a future lab, use disposable data, record the observed response and state, and verify recovery rather than assuming it succeeded.

## Interview-ready answer

I classify data before caching it. Tokens and sensitive identity responses normally need no-store; personalized representations require private policy and deliberate validators. no-cache allows storage but requires validation, while max-age permits a freshness interval. I trace fresh reuse and stale revalidation separately, and check access before a protected 304. A shared feed cache is unsafe if it includes likedByViewer without correct isolation. For public media I can use immutable content identities when the access policy permits it. I test two viewers, changing access, ETags, metadata on 304, and representation selection through caches rather than assuming one header makes the whole design safe.

## Stop/go and learner evidence

Proceed only after explaining the request trace without notes, predicting the failure, and answering all three checks below before opening their answers. Record the attempt in [learner progress](learner-progress.md). Artifact readiness does not establish comprehension or implementation readiness.

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
