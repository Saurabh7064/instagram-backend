# 17. Relationship resources, commands, and invariants

## Question

How do you model likes, follows, and publishing actions without ambiguous toggles?

[Module sequence](README.md) · Artifact: READY · Learning: TODO · Suggested study: 20–30 minutes plus design exercise

## What you will learn

Choose desired-state resources versus commands, derive retry behavior, and protect relationship invariants under concurrent requests.

Prerequisites: the foundational contract, method, authorization, and compatibility lessons. These are proposed teaching examples; routes, collaborators, DTOs, and policies shown here are not installed by this documentation change. Imports and application wiring are omitted. Use the existing authentication rules when implementing any extension.

## Must understand: say the desired result

A mobile user taps Like while the network is unreliable. “Toggle” means invert whatever state exists now, so a repeated request can undo the first. A desired-state operation says ensure liked or ensure unliked, making retries easier to reason about. An invariant is a condition that must hold after every commit, such as at most one like per user/post pair. A command requests a business transition or action with its own rules.

A light switch that toggles is different from a control labelled “set on.” The analogy stops because concurrent database writers, stale counts, and notifications need coordinated durable state beyond a physical switch.

| Intent | Proposed API | Retry expectation |
|---|---|---|
| Ensure my like exists | PUT /posts/42/likes/me | Relationship exists after repetition |
| Ensure my like absent | DELETE /posts/42/likes/me | Relationship absent after repetition |
| Create a publication attempt | POST /posts/42/publications | New attempt unless keyed/deduplicated |
| Toggle like | POST /posts/42/toggle-like | Repetition may undo first attempt; avoid for this use |

## Spring Boot example and walkthrough

```java
@PutMapping("/api/posts/{postId}/likes/me")
@ResponseStatus(HttpStatus.NO_CONTENT)
void ensureLike(@RequestHeader("Authorization") String authorization,
                @PathVariable Long postId) {
    likes.ensurePresent(authorization, postId);
}
```

Proposed route and service: `me` derives identity from the authenticated caller, never from a body userId. ensurePresent must atomically uphold the relationship and count invariants. 204 intentionally returns no body; if the client needs a representation, a documented 200 is another choice. The resource is the caller’s relationship with a post, so its identity is known before creation.

## Request trace and concurrency problem

Initial: no like by user7 on post42, likeCount=10. Two requests race. Both application checks can report absent. The unique `(post_id,user_id)` constraint permits at most one row; the losing request must be handled under a transaction policy that leaves a valid count and returns a documented outcome.

A second race exists with two different users: both load likeCount=10; each adds a distinct relationship and sets count=11. Both relationships can commit while count should be 12. Uniqueness of the pair does not prevent this lost counter increment. Options include atomically incrementing only on a successful insertion, locking/versioned updates with correct retries, or calculating/maintaining count through another explicitly consistent design. Do not treat `if (!exists) save` as concurrency proof.

For a proposed atomic insert strategy, the successful insertion reports affected=1 -> increment count once; a repeat reports affected=0 -> no increment. Those decisions must belong to one transaction. Deletion mirrors it: decrement only when a relationship was actually removed. A database constraint alone does not update the denormalized counter for you.

## When a command is clearer

Publishing might check moderation, media readiness, schedule, and policy, then start processing. A blind PATCH status=PUBLISHED should not bypass those rules. A publication resource can record attempts, progress, and errors; a simpler POST /posts/42/publish command can also be defensible if its retry and result contract is clear. Do not force a multi-step workflow into a generic CRUD update that permits arbitrary status transitions.

Version checks protect competing transitions; attempt keys protect replay; outgoing events protect the database/notification boundary. Those mechanisms address different failures. A successful relationship PUT can still emit duplicate notifications if each retry publishes a new event without deduplication.

## Controlled failure drill

Prediction: submit the same toggle twice after dropping the first response. Symptom: the visible like is removed although the user intended like. Contain retry of that command, recover by reading authoritative state and setting the intended value, prevent with desired-state operations and duplicate-attempt tests. Separately race two different users and assert both relationship rows plus exact count; sequential tests miss that bug.

## Project mapping and practice

Current [PostController](../../../../src/main/java/com/instagram/backend/controller/PostController.java) uses POST /posts/{postId}/like and DELETE for unlike. [PostLike](../../../../src/main/java/com/instagram/backend/domain/PostLike.java) has pair uniqueness; [PostService](../../../../src/main/java/com/instagram/backend/service/PostService.java) checks existence and mutates a stored counter. It aims for repeated-call behavior but is not proven safe under these races. For F-019 follow/unfollow, design desired-state identity, uniqueness, counter behavior, notifications, and two coordinated concurrency tests before implementation.

## Interview-ready answer

I prefer desired-state operations for relationships with a known identity: ensure my like exists or is absent. A toggle is ambiguous after an uncertain response. I still need database uniqueness and atomic count handling; HTTP idempotency does not implement those. For a workflow such as publication I choose an explicit command or attempt resource, validate legal transitions, and define replay. I test duplicate requests and different-user races because a uniqueness constraint cannot prevent a separate lost counter update.

## Stop/go and learner evidence

Explain the worked request without notes, justify a competing option, predict the failure, and describe a test that checks the final state or response. Record an attempt in [learner progress](learner-progress.md). A completed artifact does not establish understanding or an implemented feature.

## Questions and explained answers

<details>
<summary>1. Why is toggle unsafe to replay?</summary>

Starting unliked, the first toggle makes liked; the second makes unliked. After response loss, replay can undo the intended result. Setting desired state avoids that ambiguity.

</details>

<details>
<summary>2. Does the existing unique pair constraint prevent lost count increments?</summary>

No. Two different users have different valid pairs and can each overwrite likeCount from 10 to 11. Counter maintenance needs its own atomic strategy.

</details>

<details>
<summary>3. When should an insert increment count?</summary>

Only when the transaction actually created a new relationship, not merely when a preliminary read reported absent. The insertion decision and increment must commit together.

</details>

<details>
<summary>4. Why might publishing deserve a command resource?</summary>

It has legal transition rules, prerequisites, work progress, and replay semantics beyond replacing a simple field. A generic update must not bypass those rules.

</details>

<details>
<summary>5. Does changing POST to PUT fix concurrency by itself?</summary>

No. It expresses the intended repeatable contract. The service, constraints, atomic writes, and outgoing events must actually uphold it.

</details>
