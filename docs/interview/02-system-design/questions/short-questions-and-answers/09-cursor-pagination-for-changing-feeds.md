# Cursor Pagination for Changing Feeds

## Question

Users scroll a newest-first feed while new posts arrive. With page-number pagination, they sometimes see duplicates or miss posts.

**“How would you paginate a changing feed so scrolling remains stable and efficient?”**


## Continue after a post, rather than after a row number

Start with this newest-first feed: `(12:05, 105)`, `(12:04, 104)`, `(12:03, 103)`, where each pair is a timestamp and post ID. Page one contains 105 and 104. The client wants whatever follows post 104.

Before page two, post 106 arrives at 12:06. The ordered feed is now 106, 105, 104, 103. Skipping two rows returns 104 again. **Offset pagination** remembers a count of rows to skip, and that count no longer describes where the reader stopped.

Instead, remember the last returned ordering value: `(12:04, 104)`. The next request asks for posts older than that pair, so it returns 103 even after 106 arrives. This is **keyset pagination**. The client carries the saved position in a **cursor**, usually an opaque token rather than an editable database expression.

A bookmark works similarly: it remembers where you stopped reading rather than relying on the number of pages before it. The analogy needs an extra rule for data: multiple posts can have the same timestamp, and deleted posts need not remain present for the boundary to work.

## Why the cursor needs both fields

Order by `created_at DESC, id DESC`. For saved timestamp `t` and ID `k`, continue where `created_at < t`, or where `created_at = t` and `id < k`. The ID breaks timestamp ties. Using only a strict timestamp comparison would omit any remaining posts with that timestamp; using a non-strict comparison would repeat rows.

Fetch `limit + 1` rows in this same order. With a page size of two, a third row tells us there is more data. Return the first two and build the continuation from the last row actually returned, not the extra row. A matching index lets the database seek near the saved position instead of repeatedly skipping a long prefix.

## What this does and does not stabilize

Post 106 appears when the viewer refreshes from the top. Deleting an earlier post does not shift our saved key. But this is not a frozen copy of the entire feed: changed sort values, filters or permissions can change membership. Use a stable sort for this design, bind the cursor to its query context, cap the page size and validate token versions. A true snapshot requires an additional explicit mechanism.

## Where this appears in our backend

[PostController.java](../../../../../src/main/java/com/instagram/backend/controller/PostController.java) currently returns a list from `GET /api/feed`. [PostRepository.java](../../../../../src/main/java/com/instagram/backend/repository/PostRepository.java) uses `findAllByOrderByCreatedAtDesc()`, and [PostService.java](../../../../../src/main/java/com/instagram/backend/service/PostService.java) maps every post. [Post.java](../../../../../src/main/java/com/instagram/backend/domain/Post.java) already has `createdAt` and unique `id`.

**Implemented:** newest-first ordering. **Proposed:** bounded page contracts, tuple continuation query, `(created_at DESC, id DESC)` index, cursor versioning, and concurrent-insert tests. The endpoint is currently unbounded.

## Choosing the approach and paying its costs

- Choose **cursor pagination** for large, changing feeds. Do not choose it when arbitrary page jumps are essential; opaque navigation is the cost.
- Choose **offset pagination** for small, stable administrative tables. Avoid it for deep feeds; duplicates, skips, and scans are the cost.
- Choose a **snapshot boundary** only when one session must exclude later changes; retaining that context is the cost.


## Paper drill: two posts share a timestamp

Predict what a timestamp-only strict “older than” condition does to remaining rows tied at the page boundary. It skips them. Look for missing unique IDs when testing a page boundary with equal timestamps.

Contain the problem by versioning the corrected cursor and explicitly asking clients with incompatible old tokens to restart. Recover using both timestamp and ID in the ordering and continuation predicate, then verify every eligible row across the boundary.

Keep tests for equal timestamps, a new post between pages, deletions and changed filters. Verify the query plan as well: correct output on a tiny table does not prove efficient deep scrolling.

## Two-to-three-minute interview answer

I would replace page numbers with keyset pagination ordered by `created_at DESC, id DESC`; the unique ID breaks timestamp ties. The client sends a bounded limit and opaque cursor holding the final timestamp and ID. The next query selects older tuples with the same order and composite index, fetching one extra row to decide whether another cursor is needed.

New posts do not disrupt the current scroll; they appear on refresh. Deletions create gaps rather than shifts. I would version and validate cursors, bind relevant filter context, cap page size, and test equal timestamps and concurrent insertion.


## Before moving on

Artifact: `READY`; comprehension: `TODO`. Explain this scenario in your own words, predict the paper drill, and attempt the questions before opening their answers. Record learning only from your own explanation and predictions.

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
