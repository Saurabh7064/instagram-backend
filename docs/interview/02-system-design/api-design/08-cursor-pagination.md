# 08. Cursor pagination for a live feed

## Question

How do you continue a feed without shifting offset pages?

[Module sequence](README.md) · Study in two passes: core model, then design decisions (15–20 minutes each) · Artifact: READY · Learning: TODO

## What you will learn

Design a complete cursor protocol, derive time/ID boundaries and size+1 metadata, bind selection context, and identify ranked-feed consistency limits.

Prerequisites: Lesson 7 and its stop/go check. All later lessons remain TODO until their own checkpoints are attempted.

## Must understand: problem and model

A growing feed needs a continuation point based on the last seen item. A cursor encodes that boundary. Keyset pagination asks for rows after that ordered key rather than skipping a count. Think of bookmarking a named sentence instead of a page number; the analogy stops when records are deleted or sort keys change.

## Spring Boot example

Examples are proposed except where explicitly identified as current code. Imports/wiring are omitted; named new services and DTOs need implementation. Preserve current authorization rules. The project uses [Spring Boot 4.0.2 and Java 24](../../../../build.gradle).

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

## Complete a cursor protocol

The predicate alone is not a protocol. Define the first request, continuation format, validation, order, filter context, expiry policy, and backward navigation.

Proposed first response for size 2:

```json
{
  "items": [{"id":9},{"id":8}],
  "nextCursor": "opaque-boundary-for-time-1000-id-8",
  "hasNext": true
}
```

The token above is descriptive, not a real encoded token. A real cursor could encode format version, createdAt, id, normalized author filter, ordering, and an expiry. Base64 is encoding rather than tamper protection. A signature can reject modifications; neither encoding nor signing removes the need to recheck current visibility. Bind the cursor to its selection context, so author=mira cannot be reused with author=another without a defined restart/error policy.

### Work through size+1 precisely

Rows descending by (time,id): [(1000,9),(1000,8),(999,7),(998,6)]. Request size=2 -> fetch 3 -> [9,8,7]. Return [9,8]. Since 3 > 2, hasNext=true. Build next boundary from **8**, the final returned row. Next query selects time <1000 or time=1000 and id<8 -> [7,6]. Building the cursor from extra row 7 would skip it.

The SQL-side logical condition should be `(time < boundaryTime OR (time = boundaryTime AND id < boundaryId)) AND visibility/filter predicates`. Parentheses matter: writing `time < boundaryTime OR (tie AND visibility)` accidentally lets the first branch bypass visibility.

### Index and consistency boundaries

A proposed PostgreSQL index on (created_at DESC,id DESC) supports the order; a tenant/author-scoped query may benefit from leading equality columns. Confirm with EXPLAIN on representative data, not index naming alone. The current entity marks createdAt non-updatable, a helpful keyset property. Its lifecycle hook overwrites createdAt at persistence, so tied timestamp fixtures may need controlled database setup.

Keysets resist inserts before the boundary; they do not freeze the collection. A newly visible older post can appear later, deleted rows disappear, and a mutable ranking score can move an item across pages. A ranked feed may need a feed-session identifier and retained ranking snapshot, which costs storage and expiry handling. A time upper bound limits newer arrivals but does not solve deletions or changed visibility.

For previous-page navigation, reverse the comparison and query ordering, then reverse the selected rows into display order. This is a separate contract; do not reuse a forward cursor without a direction rule. Malformed, unsupported-version, expired, and filter-mismatched cursors need stable errors and tests.

## Project mapping and practice

Read the actual [backend implementation](../../../../src/main/java/com/instagram/backend/domain/Post.java) before changing it. The example above distinguishes existing behavior from a proposed extension; a linked file establishes the current starting point, not proof that the proposed design exists.

**Practice task:** For F-016 add tied-timestamp and insert-between-pages tests; reject malformed cursors with 400, never a raw decoder exception.

Keep this a design exercise until a feature change is explicitly undertaken. An implementation must follow the repository integration-test, API verification, documentation, and commit workflow.

## Useful later: choices and costs

Choose keysets for long, ordered feeds. They make arbitrary page-number jumps difficult and do not create a frozen snapshot across requests. Use immutable sort keys and an appropriate composite index. Opaque encoding hides format but is not authentication; sign the cursor if tampering affects a trusted boundary.

## Controlled failure drill

Predict sorting only by timestamp with strict time comparison. A tied row disappears between pages. Detect a fixture with equal timestamps, contain by retaining the old API until fixed, recover with timestamp-plus-ID cursors, prevent with tie and insertion tests.

Describe the prediction before reading the symptom. In a future lab, use disposable data, record the observed response and state, and verify recovery rather than assuming it succeeded.

## Interview-ready answer

I use keyset continuation for long ordered feeds, with immutable order fields and a deterministic tie-breaker. I fetch one extra row to determine continuation but build the cursor from the last returned row. I validate cursor version, filters, size, and expiry; encoding alone is not protection or authorization. In this project createdAt and ID form a plausible chronological boundary, supported by a reviewed query/index and current visibility predicates. Keysets resist front inserts but do not freeze visibility or ranking. I test ties, malformed tokens, inserts/deletes, and exact page boundaries, and only add a retained feed snapshot if the product requires stable ranked traversal.

## Stop/go and learner evidence

Proceed only after explaining the request trace without notes, predicting the failure, and answering all three checks below before opening their answers. Record the attempt in [learner progress](learner-progress.md). Artifact readiness does not establish comprehension or implementation readiness.

## Questions and explained answers

<details>
<summary>1. Why must a cursor include an ID when timestamps tie?</summary>

With rows (10:00,9) and (10:00,8), time alone cannot distinguish the boundary. After returning ID 9, a strict time-only predicate skips ID 8. Comparing time then ID retains it without repeating ID 9.

</details>

<details>
<summary>2. Which row supplies the cursor when fetching [9,8,7] for size 2?</summary>

Return [9,8], hasNext true, and cursor from 8. Using extra row 7 as the boundary would skip it on continuation.

</details>

<details>
<summary>3. Why bind author/filter context into a cursor?</summary>

The boundary was derived for a specific ordered selection. Reusing it with a different filter can create invalid or misleading traversal. Validate context or require a documented restart.

</details>

<details>
<summary>4. Why parenthesize the OR predicate before adding visibility?</summary>

Without parentheses, SQL precedence can apply visibility only to the tie branch while time-less-than rows bypass it. Both boundary branches must be inside the authorized selection.

</details>

<details>
<summary>5. Can this cursor freeze a ranking that changes every minute?</summary>

No. A mutable score can move items across the boundary. Stable ranked traversal may require a retained feed session/snapshot with storage and expiry costs.

</details>
