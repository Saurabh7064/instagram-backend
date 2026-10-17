# Cursor Pagination for Changing Feeds

## Question

Users scroll a newest-first feed while new posts arrive. With page-number pagination, they sometimes see duplicates or miss posts.

**“How would you paginate a changing feed so scrolling remains stable and efficient?”**

## Why this problem exists

Offset pagination identifies rows by current position. A new row shifts every later position, so `OFFSET 20` no longer means “continue after the twentieth row I saw.” Deep offsets also make the database skip increasing amounts of data. A continuation token should name the last stable ordering position returned.

## Analogy

A bookmark records the last sentence read; “start at page 20” fails if an editor keeps inserting pages at the front.

The analogy stops because rows can share timestamps, disappear, and be filtered differently, so the bookmark needs deterministic database keys and query context.

## Terms to establish

- **Cursor:** opaque token representing an ordered position.
- **Keyset pagination:** continues by comparing with the last sort key.
- **Tie-breaker:** unique secondary field that makes ordering deterministic.
- **Composite index:** stores multiple ordered fields for an efficient continuation query.

## What I would do

I would order by `created_at DESC, id DESC`. The API accepts a bounded `limit` and optional `after` cursor containing the final item’s timestamp, ID, and cursor version. The query selects tuples older than that position, preserves the same order, and fetches `limit + 1` rows to determine whether another page exists.

The response returns items and a next cursor, not a total page count. New posts appear on refresh from the top rather than entering an in-progress scroll. Deletions create a harmless gap. Filters or ranking versions that change ordering must be encoded in the cursor or invalidate it clearly. A matching composite index keeps continuation efficient.

## Concrete walkthrough

1. The feed is `(12:05, 105)`, `(12:04, 104)`, `(12:03, 103)`.
2. Page one returns `105` and `104`, with cursor `(12:04, 104)`.
3. New post `106` arrives at `12:06`.
4. Offset two now returns `104` again because positions shifted.
5. Keyset pagination requests tuples older than `(12:04, 104)` and returns `103`; `106` appears after refresh.

The continuation remains stable without scanning shifted positions.

## Instagram-backend mapping

[PostController.java](../../../../../src/main/java/com/instagram/backend/controller/PostController.java) currently returns a list from `GET /api/feed`. [PostRepository.java](../../../../../src/main/java/com/instagram/backend/repository/PostRepository.java) uses `findAllByOrderByCreatedAtDesc()`, and [PostService.java](../../../../../src/main/java/com/instagram/backend/service/PostService.java) maps every post. [Post.java](../../../../../src/main/java/com/instagram/backend/domain/Post.java) already has `createdAt` and unique `id`.

**Implemented:** newest-first ordering. **Proposed:** bounded page contracts, tuple continuation query, `(created_at DESC, id DESC)` index, cursor versioning, and concurrent-insert tests. The endpoint is currently unbounded.

## Choices and trade-offs

- Choose **cursor pagination** for large, changing feeds. Do not choose it when arbitrary page jumps are essential; opaque navigation is the cost.
- Choose **offset pagination** for small, stable administrative tables. Avoid it for deep feeds; duplicates, skips, and scans are the cost.
- Choose a **snapshot boundary** only when one session must exclude later changes; retaining that context is the cost.

## Failure drill

**Prediction:** a timestamp-only cursor skips posts sharing one timestamp. **Detection:** tests return fewer unique IDs near equal timestamps. **Containment:** reject ambiguous old cursors after deploying a corrected version. **Recovery:** add the ID tie-breaker and restart pagination safely. **Prevention:** test equal timestamps, concurrent inserts, deletions, malformed cursors, and index usage.

## Two-to-three-minute interview answer

I would replace page numbers with keyset pagination ordered by `created_at DESC, id DESC`; the unique ID breaks timestamp ties. The client sends a bounded limit and opaque cursor holding the final timestamp and ID. The next query selects older tuples with the same order and composite index, fetching one extra row to decide whether another cursor is needed.

New posts do not disrupt the current scroll; they appear on refresh. Deletions create gaps rather than shifts. I would version and validate cursors, bind relevant filter context, cap page size, and test equal timestamps and concurrent insertion.

## Practice status

The artifact is ready; comprehension requires deriving the continuation predicate without notes.

## Questions and explained answers

<details>
<summary>Why is `createdAt` alone unsafe?</summary>

Several posts can share it. Strict comparison skips tied rows; non-strict comparison repeats them. Unique `id` creates a total order.
</details>

<details>
<summary>Why fetch `limit + 1` rows?</summary>

The extra row proves more data exists without a separate count query. Return only `limit` rows and a cursor when it appears.
</details>

<details>
<summary>Where does a post inserted after page one appear?</summary>

It is newer than the cursor, so it appears on refresh from the beginning rather than disrupting the current downward scroll.
</details>
