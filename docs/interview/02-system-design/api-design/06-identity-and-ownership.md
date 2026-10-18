# 06. Identity and ownership checks

## Question

How do you stop a user from modifying somebody else’s post?

[Module sequence](README.md) · Study in two passes: core model, then design decisions (15–20 minutes each) · Artifact: READY · Learning: TODO

## What you will learn

Separate authentication, object/collection/field authorization, CORS, and CSRF; trace a cross-owner write and design consistent denial behavior.

Prerequisites: Lesson 5 and its stop/go check. All later lessons remain TODO until their own checkpoints are attempted.

## Must understand: problem and model

Knowing who called does not prove they may act on every ID. Authentication establishes identity; authorization establishes permission for an operation. A bearer token grants access to whoever possesses it. A building badge identifies a person, but does not open every office; unlike a badge check, API permissions may change between requests.

## Spring Boot example

Examples are proposed except where explicitly identified as current code. Imports/wiring are omitted; named new services and DTOs need implementation. Preserve current authorization rules. The project uses [Spring Boot 4.0.2 and Java 24](../../../../build.gradle).

```java
private Post requireOwnedPost(UserAccount viewer, Long postId) {
    var post = requirePost(postId);
    if (!post.getAuthor().getId().equals(viewer.getId())) {
        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You can only modify your own posts");
    }
    return post;
}
```

### Walkthrough

This is the existing service logic with a shortened type name. Load the target, compare its stored author ID with the identity derived from the token, reject mismatches, and only then return an editable object. Never trust authorId from the body. The transaction wraps lookup and mutation; stricter policies may require additional race protection.

## Request trace: predict, act, observe, explain

Before following the trace, predict the status and final stored state. Then trace these transitions in order:

User 7 owns post 42. Token for user 9 -> token parsed -> user 9 loaded -> post 42 loaded -> 7 differs from 9 -> 403 -> caption unchanged. Missing token -> 401 before ownership comparison. Missing post -> current service 404.

The observable result includes the response **and** the final stored state. A successful status alone cannot prove concurrency or authorization correctness.

## Permissions apply to rows, fields, and transitions

Object authorization is the owner check already shown. Collection authorization means every returned row belongs to the viewer’s allowed scope. Field authorization means a caller may edit caption but cannot promote themselves by writing role=ADMIN. Transition authorization means permission to read a draft does not imply permission to publish it.

| Request | Identity | Required rule | Expected failure |
|---|---|---|---|
| Edit my post | Valid owner | Stored author matches viewer | Missing post -> 404 |
| Edit another user’s post | Valid non-owner | Same rule | Current service -> 403 |
| Read a private collection | Valid follower/non-follower | Scope query by visibility | Never load all then leak before filtering |
| Create like for body userId=7 | Token user 9 | Ignore/reject caller-selected actor | Actor remains user 9 |
| Poll media job | Valid non-owner | Job owner matches viewer | Consistent denial/concealment |

### Authenticate once, authorize at the business boundary

A proposed security integration could supply an authenticated principal to controllers rather than passing raw headers everywhere. Do not copy `@AuthenticationPrincipal` into this project and assume it works: build.gradle includes password crypto, not a configured full security filter chain. The current implementation calls TokenService inside services. Introducing filter-chain authentication is a separate behavior change with tests.

Even with central authentication, keep domain checks close to the mutation. A controller check followed by a reusable unprotected service method invites another caller to bypass it. Perform the ownership decision and write in an appropriate transaction; if ownership or visibility can change concurrently, define the locking/version policy.

### CORS, CSRF, and bearer tokens solve different problems

CORS is a browser rule controlling which origins may read certain responses or issue preflighted requests. It does not stop a scripted caller using curl. CSRF is a browser sending ambient credentials, commonly cookies, on a request a malicious site induced. A token supplied explicitly in an Authorization header has a different threat model from a session cookie; switching to cookies requires a CSRF design. TLS protects tokens in transit, but cannot stop a compromised client or token leakage in logs.

Sequential IDs reveal that another object may exist but guessing-resistant IDs do not establish permission. For private objects, a consistent 404 policy can reduce existence disclosure, at the cost of less explicit diagnostics. Keep internal audit evidence separate from the public denial response.

**Adversarial trace:** user 9 sends valid token plus postId 42 and authorId 7 in JSON. DTO excludes authorId -> service resolves token to 9 -> loads stored author 7 -> denies -> no write. Repeat via batch and nested routes: they require the same rule for every item. A permission check only on the collection’s first item is insufficient.

## Project mapping and practice

Read the actual [backend implementation](../../../../src/main/java/com/instagram/backend/service/PostService.java) before changing it. The example above distinguishes existing behavior from a proposed extension; a linked file establishes the current starting point, not proof that the proposed design exists.

**Practice task:** Extend F-017 policy: may an author delete comments on their own post, and may a commenter delete their own comment? Test both identities.

Keep this a design exercise until a feature change is explicitly undertaken. An implementation must follow the repository integration-test, API verification, documentation, and commit workflow.

## Useful later: choices and costs

403 communicates denial; 404 can intentionally hide existence but should be applied consistently. URL secrecy is not authorization. Central security filters can reduce duplicated identity parsing; service-level ownership rules still express domain policy. CORS controls browser access behavior and does not authenticate curl callers.

## Controlled failure drill

Predict changing /posts/42 to /posts/43 while authenticated. Without object checks, another user’s post changes. Detect cross-owner mutation tests, contain mutation access, restore affected data from audit/backup, prevent with owner/non-owner integration cases.

Describe the prediction before reading the symptom. In a future lab, use disposable data, record the observed response and state, and verify recovery rather than assuming it succeeded.

## Interview-ready answer

I derive identity from validated credentials and decide permission for each object, field, collection, and transition. Knowing a post ID or carrying a valid token does not grant edit rights. I keep ownership near the business mutation and scope collection queries to visible rows. In this backend tokens are parsed in services; adding a full security filter chain would be a real separately tested change. CORS is a browser access mechanism, not authentication, and a move to cookie credentials requires a CSRF design. I test owner/non-owner requests, attacker-supplied actor IDs, nested membership, and mixed-permission batch items, then assert denied writes leave data unchanged.

## Stop/go and learner evidence

Proceed only after explaining the request trace without notes, predicting the failure, and answering all three checks below before opening their answers. Record the attempt in [learner progress](learner-progress.md). Artifact readiness does not establish comprehension or implementation readiness.

## Questions and explained answers

<details>
<summary>1. Why is a valid token insufficient for updating a post?</summary>

The token identifies the caller, not the owner of the requested post. Load the stored author ID and compare it with the authenticated identity before mutation; never accept a body authorId as proof.

</details>

<details>
<summary>2. Can curl bypass permissions by ignoring CORS?</summary>

CORS does not impose domain permissions on curl in the first place. The API must authenticate and authorize every request independently of browser origin enforcement.

</details>

<details>
<summary>3. Can this project use @AuthenticationPrincipal without extra setup?</summary>

It is not currently configured as a full security-chain authentication model. Password crypto and custom token parsing do not automatically supply a resolved Spring Security principal.

</details>

<details>
<summary>4. What changes when credentials move from an explicit bearer header to cookies?</summary>

Browsers may attach cookies automatically, creating a CSRF threat model. Cookie policy, CSRF defenses, origins, and session behavior require explicit design.

</details>

<details>
<summary>5. Why authorize fields and transitions as well as rows?</summary>

An owner may edit caption but not authorId, counters, or an arbitrary publication status. Ownership of the object does not grant every possible mutation.

</details>
