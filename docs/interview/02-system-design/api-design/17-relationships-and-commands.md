# 17. Relationship resources, commands, and invariants

## Question

How do you model likes, follows, and publishing actions without ambiguous toggles?

[Module sequence](README.md) · Artifact: READY · Learning: TODO · Suggested study: 20–30 minutes plus design exercise

## What you will learn

Explain why replaying a toggle can undo a like, then separate the HTTP intention from the database rules needed to maintain one relationship and the right count.

Builds on [method repeat behavior](02-http-methods.md) and [identity](06-identity-and-ownership.md). Desired-state routes below are proposals; the current like routes are identified at the end.

## A repeated tap should not undo the user's intention

Post 42 has 10 likes from other users; user 7 has not liked it. User 7 taps Like; the server stores the like, but the response disappears. The app retries. If the operation means “toggle,” the second call sees liked and changes it back to unliked. Both calls executed correctly according to toggle semantics, yet the user ends up with the opposite of what they wanted.

Instead say “ensure my like exists.” The first request creates it; a repeat leaves it present. This is a **desired-state operation**: it names the result to achieve rather than an inversion of whatever happens to be there. The relationship has a known identity, the pair `(post 42, user 7)`, so a proposed `PUT /posts/42/likes/me` can address it directly.

An **invariant** is a rule that must remain true after every committed change. Here we need at most one relationship for that pair, and a stored count that agrees with the chosen counting policy. Expressing the intention clearly in HTTP helps the client, but the database must still uphold those rules under concurrent requests.

The everyday comparison is a light control marked “set on” instead of a switch marked “toggle.” The comparison stops at the stored relationship and count: two requests can race, and a notification is another effect to coordinate.

The table keeps that desired-state distinction visible. Publishing is a supporting comparison: some business actions need a processing contract rather than simply ensuring one relationship exists.

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

## Replay the same Like attempt twice

Use user 7's initially absent relationship with post 42. On paper, drop the response after the first toggle and repeat it. Predict the final state: absent again, although the person intended present. Detect it with a lost-response/repeat test rather than two different user actions.

Contain retries of the ambiguous command, recover by reading authoritative state and explicitly setting the desired result, then verify repetition leaves it present. Prevention needs desired-state semantics and an implementation that honors them. Also test two different users liking from count 10: both rows must exist and the exact count must become 12. The unique pair constraint cannot prevent that separate counter race.

## Project mapping and practice

Current [PostController](../../../../src/main/java/com/instagram/backend/controller/PostController.java) uses POST /posts/{postId}/like and DELETE for unlike. [PostLike](../../../../src/main/java/com/instagram/backend/domain/PostLike.java) has pair uniqueness; [PostService](../../../../src/main/java/com/instagram/backend/service/PostService.java) checks existence and mutates a stored counter. It aims for repeated-call behavior but is not proven safe under these races. For F-019 follow/unfollow, design desired-state identity, uniqueness, counter behavior, notifications, and two coordinated concurrency tests before implementation.

## Interview-ready answer

I prefer desired-state operations for relationships with a known identity: ensure my like exists or is absent. A toggle is ambiguous after an uncertain response. I still need database uniqueness and atomic count handling; HTTP idempotency does not implement those. For a workflow such as publication I choose an explicit command or attempt resource, validate legal transitions, and define replay. I test duplicate requests and different-user races because a uniqueness constraint cannot prevent a separate lost counter update.

## Before moving on

Derive the state after a repeated toggle and after repeated desired-state requests. Continue when you can explain why pair uniqueness and correct counter updates need separate coordination.

Attempt the questions before opening their answers and record the explanation in [learner progress](learner-progress.md). This revision leaves learning status unchanged; a finished document is not evidence that you have demonstrated the idea.

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
