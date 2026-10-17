# 06. Identity and ownership checks

## Question

How do you stop a user from modifying somebody else’s post?

[Module sequence](README.md) · Estimated time: 15–20 minutes · Artifact: READY · Learning: TODO

## What you will learn

Explain the problem behind identity and ownership checks, trace its externally visible behavior, and identify one implementation risk before proposing a change.

Prerequisites: Lesson 5 and its stop/go check. All later lessons remain TODO until their own checkpoints are attempted.

## Must understand: problem and model

Knowing who called does not prove they may act on every ID. Authentication establishes identity; authorization establishes permission for an operation. A bearer token grants access to whoever possesses it. A building badge identifies a person, but does not open every office; unlike a badge check, API permissions may change between requests.

## Spring Boot example

Unless explicitly described as existing code, this is a proposed teaching excerpt, not an endpoint implemented by this documentation change. Application-specific collaborators and types require implementation; imports and class wiring are omitted. Preserve authentication when adapting an excerpt. This project uses Spring Boot 4.0.2 and Java 24, as recorded in [build configuration](../../../../build.gradle).

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

## Project mapping and practice

Read the actual [backend implementation](../../../../src/main/java/com/instagram/backend/service/PostService.java) before changing it. The example above distinguishes existing behavior from a proposed extension; a linked file establishes the current starting point, not proof that the proposed design exists.

**Practice task:** Extend F-017 policy: may an author delete comments on their own post, and may a commenter delete their own comment? Test both identities.

Keep this a design exercise until a feature change is explicitly undertaken. An implementation must follow the repository integration-test, API verification, documentation, and commit workflow.

## Useful later: choices and costs

403 communicates denial; 404 can intentionally hide existence but should be applied consistently. URL secrecy is not authorization. Central security filters can reduce duplicated identity parsing; service-level ownership rules still express domain policy. CORS controls browser access behavior and does not authenticate curl callers.

## Controlled failure drill

Predict changing /posts/42 to /posts/43 while authenticated. Without object checks, another user’s post changes. Detect cross-owner mutation tests, contain mutation access, restore affected data from audit/backup, prevent with owner/non-owner integration cases.

Describe the prediction before reading the symptom. In a future lab, use disposable data, record the observed response and state, and verify recovery rather than assuming it succeeded.

## Interview-ready answer outline

For a two-minute answer, start with the caller’s problem, define the main concept in plain language, explain the request trace above, then justify one choice and its cost. Finish by explaining how the failure drill would be detected and prevented in this backend. Do not claim the proposed extension is deployed.

## Stop/go and learner evidence

Proceed only after explaining the request trace without notes, predicting the failure, and answering all three checks below before opening their answers. Record the attempt in [learner progress](learner-progress.md). Artifact readiness does not establish comprehension or implementation readiness.

## Questions and explained answers

<details>
<summary>1. Why is a valid token insufficient for updating a post?</summary>

The token identifies the caller, not the owner of the requested post. Load the stored author ID and compare it with the authenticated identity before mutation; never accept a body authorId as proof.

</details>

<details>
<summary>2. What exact response or state should the worked request produce, and why?</summary>

User 7 owns post 42. Token for user 9 -> token parsed -> user 9 loaded -> post 42 loaded -> 7 differs from 9 -> 403 -> caption unchanged. Missing token -> 401 before ownership comparison. Missing post -> current service 404.

This follows from the request boundary and state checks in the walkthrough, not merely from choosing a URL.

</details>

<details>
<summary>3. Which failure can remain despite a successful happy-path request, and how would you prevent it?</summary>

Predict changing /posts/42 to /posts/43 while authenticated. Without object checks, another user’s post changes. Detect cross-owner mutation tests, contain mutation access, restore affected data from audit/backup, prevent with owner/non-owner integration cases.

A successful request proves one case. The controlled failure needs its own observation and regression check.

</details>
