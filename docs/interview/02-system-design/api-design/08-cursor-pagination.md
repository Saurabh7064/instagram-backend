# 08. Cursor pagination for a live feed

## Question

How do you continue a feed without shifting offset pages?

[Module sequence](README.md) · Estimated time: 15–20 minutes · Artifact: READY · Learning: TODO

## What you will learn

Explain the problem behind cursor pagination for a live feed, trace its externally visible behavior, and identify one implementation risk before proposing a change.

Prerequisites: Lesson 7 and its stop/go check. All later lessons remain TODO until their own checkpoints are attempted.

## Must understand: problem and model

A growing feed needs a continuation point based on the last seen item. A cursor encodes that boundary. Keyset pagination asks for rows after that ordered key rather than skipping a count. Think of bookmarking a named sentence instead of a page number; the analogy stops when records are deleted or sort keys change.

## Spring Boot example

Unless explicitly described as existing code, this is a proposed teaching excerpt, not an endpoint implemented by this documentation change. Application-specific collaborators and types require implementation; imports and class wiring are omitted. Preserve authentication when adapting an excerpt. This project uses Spring Boot 4.0.2 and Java 24, as recorded in [build configuration](../../../../build.gradle).

```java
@Query("""
    select p from Post p
    where p.createdAt < :time
       or (p.createdAt = :time and p.id < :id)
    order by p.createdAt desc, p.id desc
    """)
List<Post> next(@Param("time") Instant time,
                @Param("id") Long id, Pageable limit);
```

### Walkthrough

This proposed repository query uses strict comparisons because the last row was already returned. Timestamp ties compare ID. Request size+1 rows to determine whether another page exists, return at most size, and create the next cursor from the last returned row, not the extra row. Validate cursor structure and bind its filter/order context.

## Request trace: predict, act, observe, explain

Before following the trace, predict the status and final stored state. Then trace these transitions in order:

Rows (10:00,9),(10:00,8),(09:59,7). First size=2 returns 9,8 with cursor (10:00,8). Next predicate excludes 9 and 8 and returns 7. A new (10:01,10) does not shift this boundary, so 8 is not repeated.

The observable result includes the response **and** the final stored state. A successful status alone cannot prove concurrency or authorization correctness.

## Project mapping and practice

Read the actual [backend implementation](../../../../src/main/java/com/instagram/backend/domain/Post.java) before changing it. The example above distinguishes existing behavior from a proposed extension; a linked file establishes the current starting point, not proof that the proposed design exists.

**Practice task:** For F-016 add tied-timestamp and insert-between-pages tests; reject malformed cursors with 400, never a raw decoder exception.

Keep this a design exercise until a feature change is explicitly undertaken. An implementation must follow the repository integration-test, API verification, documentation, and commit workflow.

## Useful later: choices and costs

Choose keysets for long, ordered feeds. They make arbitrary page-number jumps difficult and do not create a frozen snapshot across requests. Use immutable sort keys and an appropriate composite index. Opaque encoding hides format but is not authentication; sign the cursor if tampering affects a trusted boundary.

## Controlled failure drill

Predict sorting only by timestamp with strict time comparison. A tied row disappears between pages. Detect a fixture with equal timestamps, contain by retaining the old API until fixed, recover with timestamp-plus-ID cursors, prevent with tie and insertion tests.

Describe the prediction before reading the symptom. In a future lab, use disposable data, record the observed response and state, and verify recovery rather than assuming it succeeded.

## Interview-ready answer outline

For a two-minute answer, start with the caller’s problem, define the main concept in plain language, explain the request trace above, then justify one choice and its cost. Finish by explaining how the failure drill would be detected and prevented in this backend. Do not claim the proposed extension is deployed.

## Stop/go and learner evidence

Proceed only after explaining the request trace without notes, predicting the failure, and answering all three checks below before opening their answers. Record the attempt in [learner progress](learner-progress.md). Artifact readiness does not establish comprehension or implementation readiness.

## Questions and explained answers

<details>
<summary>1. Why must a cursor include an ID when timestamps tie?</summary>

With rows (10:00,9) and (10:00,8), time alone cannot distinguish the boundary. After returning ID 9, a strict time-only predicate skips ID 8. Comparing time then ID retains it without repeating ID 9.

</details>

<details>
<summary>2. What exact response or state should the worked request produce, and why?</summary>

Rows (10:00,9),(10:00,8),(09:59,7). First size=2 returns 9,8 with cursor (10:00,8). Next predicate excludes 9 and 8 and returns 7. A new (10:01,10) does not shift this boundary, so 8 is not repeated.

This follows from the request boundary and state checks in the walkthrough, not merely from choosing a URL.

</details>

<details>
<summary>3. Which failure can remain despite a successful happy-path request, and how would you prevent it?</summary>

Predict sorting only by timestamp with strict time comparison. A tied row disappears between pages. Detect a fixture with equal timestamps, contain by retaining the old API until fixed, recover with timestamp-plus-ID cursors, prevent with tie and insertion tests.

A successful request proves one case. The controlled failure needs its own observation and regression check.

</details>
