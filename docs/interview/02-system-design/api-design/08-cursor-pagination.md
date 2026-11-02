# 08. Cursor pagination for a live feed

## Question

How do you continue a feed without shifting offset pages?

[Module sequence](README.md) · Artifact: READY · Learning: TODO

## What you will learn

Derive the next feed boundary from the last returned post, handle equal timestamps, and explain which changes a cursor can tolerate without promising a frozen feed.

Read [offset pagination](07-offset-pagination.md) first; its shifting pages are the problem we are fixing.

## Remember the last post rather than how many you skipped

In the [offset lesson](07-offset-pagination.md), new posts could shift the next page. We can change the question from “skip two rows” to “continue after the last post I actually saw.” A new arrival above that post then does not change our starting point.

Use this fixed order, with each pair showing `(creation time, ID)`: `[(10:00,9),(10:00,8),(10:00,7),(09:59,6)]`. The first two results are 9 and 8. To continue, remember `(10:00,8)` and ask for posts older than that boundary. Post 7 qualifies because its time equals 10:00 and its ID 7 is smaller than 8. Post 6 qualifies because 09:59 is earlier than 10:00.

That saved continuation value is a **cursor**. Asking for rows after an ordered value is called **keyset pagination**. We need both time and ID because a timestamp alone cannot distinguish posts 9 and 8. The order uses larger IDs first when times tie; the continuation must use the same rule.

This resembles marking a named sentence in a book rather than remembering its page number. The limit of the comparison is that posts can disappear or become invisible. A cursor can keep a position without preserving a frozen copy of the whole collection.

The query below implements the boundary. The rest of the API still has to define what the cursor contains, how to validate it, and who may see the selected posts.

## Translate the boundary into a query

The project uses [Spring Boot 4.0.2 and Java 24](../../../../build.gradle). The excerpt below is a design proposal unless identified as current code; omitted imports and collaborators still need implementation.

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

### Read the boundary in the code

The first branch accepts times earlier than the cursor time. The second accepts equal times only when the ID is smaller. Those strict comparisons exclude the last returned row, which the client has already seen. The `order by` uses the same fields and directions as that boundary.

This is a proposed repository query. Its real authorized selection needs visibility and filter predicates around the whole boundary expression. Fetching one extra row helps decide continuation, but the cursor must come from the last row actually returned, as worked out below.

## Continue after post 8, even when post 10 arrives

Our fixed rows are `[(10:00,9),(10:00,8),(10:00,7),(09:59,6)]`. With size 2, return posts 9 and 8 and save boundary `(10:00,8)`. The next query asks for an earlier time, or the same time with a smaller ID. It returns posts 7 and 6; neither 9 nor 8 satisfies that condition.

Now insert `(10:01,10)` before the continuation request. It is newer than the boundary, so it does not qualify. The next result remains `[7,6]`, rather than shifting to include post 8 again.

This is a boundary guarantee, not a snapshot guarantee. Deleting post 7 removes it from later results; changing which posts are visible can change the selection. We must state those limits rather than describe the cursor as a copy of the feed.

## Complete a cursor protocol

The predicate alone is not a protocol. Define the first request, continuation format, validation, order, filter context, expiry policy, and backward navigation.

Proposed first response for size 2:

```json
{
  "items": [{"id":9},{"id":8}],
  "nextCursor": "opaque-boundary-for-1000-id-8",
  "hasNext": true
}
```

The token above is descriptive, not a real encoded token. A real cursor could encode format version, createdAt, id, normalized author filter, ordering, and an expiry. Base64 is encoding rather than tamper protection. A signature can reject modifications; neither encoding nor signing removes the need to recheck current visibility. Bind the cursor to its selection context, so author=mira cannot be reused with author=another without a defined restart/error policy.

### Work through size+1 precisely

The server needs to know whether more rows remain without returning an unbounded result. Ask for `size + 1 = 2 + 1 = 3` rows and receive `[9,8,7]`. Return only `[9,8]`. The third row proves another item was available at this query, so set `hasNext:true`.

Build the next cursor from **8**, the last returned row at 10:00. The next boundary admits 7 because it has the same time and `7 < 8`, and admits 6 because 09:59 is earlier. Building the cursor from extra row 7 would instead tell the next request to continue after an item the caller never saw, skipping it.

The SQL-side logical condition should be `(time < boundaryTime OR (time = boundaryTime AND id < boundaryId)) AND visibility/filter predicates`. Parentheses matter: writing `time < boundaryTime OR (tie AND visibility)` accidentally lets the first branch bypass visibility.

### Index and consistency boundaries

A proposed PostgreSQL index on (created_at DESC,id DESC) supports the order; a tenant/author-scoped query may benefit from leading equality columns. Confirm with EXPLAIN on representative data, not index naming alone. The current entity marks createdAt non-updatable, a helpful keyset property. Its lifecycle hook overwrites createdAt at persistence, so tied timestamp fixtures may need controlled database setup.

Keysets resist inserts before the boundary; they do not freeze the collection. A newly visible older post can appear later, deleted rows disappear, and a mutable ranking score can move an item across pages. A ranked feed may need a feed-session identifier and retained ranking snapshot, which costs storage and expiry handling. A time upper bound limits newer arrivals but does not solve deletions or changed visibility.

For previous-page navigation, reverse the comparison and query ordering, then reverse the selected rows into display order. This is a separate contract; do not reuse a forward cursor without a direction rule. Malformed, unsupported-version, expired, and filter-mismatched cursors need stable errors and tests.

## Apply this to the backend

[Post.createdAt](../../../../src/main/java/com/instagram/backend/domain/Post.java) is marked non-updatable and initialized on persistence, making it a plausible chronological sort field. The current feed has no cursor protocol or matching query/index guarantee.

For F-016, design fixtures with equal timestamps, a new post arriving between pages, malformed tokens, and changed filters. State each expected page before running the eventual test; a decoding exception must become the documented client error, not leak as an internal failure.

## Useful later: choices and costs

Choose keysets for long, ordered feeds. They make arbitrary page-number jumps difficult and do not create a frozen snapshot across requests. Use immutable sort keys and an appropriate composite index. Opaque encoding hides format but is not authentication; sign the cursor if tampering affects a trusted boundary.

## Remove the ID from the cursor on paper

Use the same first page `[9,8]` at 10:00, with post 7 also at 10:00 waiting for the next page. If continuation asks only for `createdAt < 10:00`, predict what happens: post 7 is skipped because its time is equal, even though the client has never seen it.

A future tied-timestamp test detects that omission by deriving the expected next IDs. Keep a faulty new cursor contract out of service while correcting the format and predicate; existing issued cursors need a defined restart or compatibility policy. Recover by continuing with the time-and-ID boundary and verifying `[7,6]`. Prevent recurrence with exact ties, extra-row handling, and insertion-between-pages tests. No cursor implementation or test run is supplied by this document.

## Interview-ready answer

I use keyset continuation for long ordered feeds, with immutable order fields and a deterministic tie-breaker. I fetch one extra row to determine continuation but build the cursor from the last returned row. I validate cursor version, filters, size, and expiry; encoding alone is not protection or authorization. In this project createdAt and ID form a plausible chronological boundary, supported by a reviewed query/index and current visibility predicates. Keysets resist front inserts but do not freeze visibility or ranking. I test ties, malformed tokens, inserts/deletes, and exact page boundaries, and only add a retained feed snapshot if the product requires stable ranked traversal.

## Before moving on

Derive both pages and the exact cursor source row. Continue when you can explain the tied-time omission, protect the full query with visibility, and say why this is not a frozen ranked feed.

Attempt the questions before opening their answers and record the explanation in [learner progress](learner-progress.md). This revision leaves learning status unchanged; a finished document is not evidence that you have demonstrated the idea.

## Questions and explained answers

<details>
<summary>1. Why must a cursor include an ID when timestamps tie?</summary>

After returning IDs 9 and 8 at 10:00, ID 7 at that same time is still unseen. A strict time-only condition excludes it because 10:00 is not less than 10:00. Adding the ID condition admits it because 7 < 8, while excluding returned rows 9 and 8.

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
