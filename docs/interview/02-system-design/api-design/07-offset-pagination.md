# 07. Offset pagination and bounded reads

## Question

How do you prevent a feed response from growing without limit?

[Module sequence](README.md) · Artifact: READY · Learning: TODO

## What you will learn

Calculate which posts an offset page returns, see how an insertion changes that result, and distinguish a bounded database query from slicing an already-loaded list.

Builds on [identity and permission](06-identity-and-ownership.md).

## A feed should not grow with every post ever created

Imagine the feed currently contains five posts, ordered newest first: `[9,8,7,6,5]`. Returning all five is easy. Returning every post still looks harmless in development, but the response grows each time someone posts. Eventually a simple screen requires loading far more data than it can show.

Ask the database for only a bounded part of that ordered result. A **limit** caps the number of rows returned. An **offset** says how many matching rows to skip first. For a page size of 2, page 0 skips `0 × 2 = 0` rows and returns `[9,8]`. Page 1 skips `1 × 2 = 2` rows and returns `[7,6]`.

The page number is therefore a position in the current result, rather than an identity for a fixed group of posts. If a new post arrives at the front, everything after it moves one position. That is the source of the duplicate we will work through below.

A printed catalogue is a useful comparison for numbered pages. Its limit is that printed pages stay still; a live feed changes between requests. We need a clear order and a clear promise about what pagination can and cannot keep stable.

Keep the [permission rules](06-identity-and-ownership.md) from the previous lesson: the bounded result must contain only posts this caller may see.

## Put the page bounds in the request contract

The project uses [Spring Boot 4.0.2 and Java 24](../../../../build.gradle). The excerpt below is a design proposal unless identified as current code; omitted imports and collaborators still need implementation.

```java
@GetMapping("/feed")
FeedPage feed(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
    if (page < 0 || size < 1 || size > 100) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }
    return feedQueries.page(PageRequest.of(page, size,
            Sort.by("createdAt").descending()
                    .and(Sort.by("id").descending())));
}
```

### Read the boundary in the code

`PageRequest` uses zero-based page numbers, so the database offset is `page × size`. The guard rejects negative pages and sizes outside 1–100 before querying. Ordering by `createdAt` and then `id` makes timestamp ties deterministic.

`FeedPage` and `feedQueries` are proposed types. Authentication is omitted from this small example and must remain in the real route. The query service must pass the bounds into the database query; loading the entire feed and dividing the Java list afterwards leaves the original cost.

## Watch an insertion move the next page

Start with `[9,8,7,6,5]`, newest first. Request page 0 with size 2: offset `0 × 2 = 0`, result `[9,8]`. The client has now seen posts 9 and 8.

Before it requests page 1, post 10 arrives. The order becomes `[10,9,8,7,6,5]`. Page 1 still means skip `1 × 2 = 2` rows. Those skipped rows are now 10 and 9, so the response is `[8,7]`. Post 8 appears twice across the two responses.

Nothing was incorrectly sorted. The meaning of “two rows before this page” changed while the collection changed. An ID tie-breaker fixes ambiguous ordering between equal timestamps, but cannot stop this movement. For a modest admin list that may be acceptable; for a long scrolling feed, the next lesson gives the client a different continuation promise.

## The database must do the bounding

`findAllByOrderByCreatedAtDesc().stream().skip(...).limit(...)` still loads all rows and can trigger many author/like queries. Push order, offset, and limit into SQL. A stable tie-breaker is essential: sorting by createdAt alone does not specify the order of two posts created at the same instant.

| Choice | Returned metadata | Extra cost | Best fit |
|---|---|---|---|
| Page | totalElements, totalPages, items | May execute count query | Small admin list needing numbered navigation |
| Slice | items, hasNext | Usually fetches an extra row | Sequential browsing without exact total |
| Explicit bounded projection | Chosen fields and metadata | Manual mapping/query work | Carefully controlled public response |

Avoid serializing framework Page internals as the public contract. Their implementation shape can change. Define a DTO instead:

```java
public record PostPage(
        List<FeedPostResponse> items,
        int page,
        int size,
        boolean hasNext) {}
```

Proposed response shape; the current feed is an array and changing it in place would break consumers. With requested size 2 and query results [9,8,7], return items [9,8], size 2, hasNext true. Do not return the extra row and then use it as the next boundary.

### A deletion can skip a post instead

Return `[9,8]` from the same initial order. Now consider the alternative change where 9 is deleted rather than 10 inserted. The remaining order is `[8,7,6,5]`. Page 1 skips the first two rows, now 8 and 7, and returns `[6,5]`. The client never sees 7. Inserts can therefore repeat a post, while deletion before the offset can omit one. Neither result is fixed by a deterministic tie-breaker.

Deep pages also cost work even with indexes: offset 100000 means the database may still walk past 100000 qualifying entries. Do not promise O(limit) for offset queries. Exact counts can scan many rows and can disagree with item results under concurrent changes unless the isolation policy coordinates them. “Total” is usually an observation, not a reservation of future results.

### Query budget beyond row count

The current PostService maps each post and asks `existsByPostAndUser` per item; it also accesses the lazy author relationship. A page of 20 can therefore generate more than one SQL query. A projection/join and batched viewer-like lookup can reduce this, but inspect the actual SQL and query plan. Fetching a collection join with pagination has its own pitfalls; do not add fetch joins blindly.

Cap size, reject negative page, and set a product maximum page depth if offset browsing is bounded. A successful empty page after the end is normally 200 with no items and hasNext false, not an exception.

## Apply this to the backend

[PostRepository](../../../../src/main/java/com/instagram/backend/repository/PostRepository.java) exposes the current all-posts ordered query. [PostService.feed](../../../../src/main/java/com/instagram/backend/service/PostService.java) maps the full result and looks up each viewer-like state. A response page alone would not fix either query cost.

For F-016, plan a database-bounded query and an explicit page DTO, then measure returned bytes and SQL query count with 1,000 posts when implementing the feature. This lesson proposes that work; it does not record a measurement.

## Useful later: choices and costs

Offset pagination is simple for small administrative lists and jumping to a numbered page. Large offsets can require scanning many skipped rows; concurrent inserts cause drift. Fetch a Slice when only hasNext is needed; a Page can add a count query. Always impose a size cap and deterministic sort.

## Ask for an enormous page

In a future API check, request an excessive size such as 1,000,000 against the same feed. Predict rejection under the proposed 1–100 contract before a large query runs. A slow response or memory spike shows that the bound is missing or applied after loading.

Use request validation plus SQL/query-count observation to detect where work happened. Contain the problem with a hard accepted-size cap and recover by letting excess work drain under bounded load. Then verify size 100 succeeds, 101 fails, and the rejected request does not first fetch the full result. A small test dataset can hide the cost, so representative data is needed when the feature is implemented.

## Interview-ready answer

I use offset pages when numbered navigation and modest data sizes justify them. I enforce bounds in the database, define a deterministic order with an ID tie-breaker, and expose my own page DTO. Exact totals and deep offsets have query cost. Deterministic ordering does not prevent separate requests from drifting when rows are inserted or deleted. The current feed loads all posts and performs per-post mapping/lookups, so merely slicing the Java list would leave the main cost intact. I would measure generated SQL and queries per page, test boundaries and data shifts, and use a cursor contract when long live-feed traversal matters more than arbitrary page jumps.

## Before moving on

Derive `[9,8]` and then `[8,7]` after the insertion. Continue when you can explain why a tie-breaker does not stop this drift and why slicing after loading is not a database bound.

Attempt the questions before opening their answers and record the explanation in [learner progress](learner-progress.md). This revision leaves learning status unchanged; a finished document is not evidence that you have demonstrated the idea.

## Questions and explained answers

<details>
<summary>1. For page=3 and size=20, how many rows are skipped?</summary>

The zero-based offset is page × size = 3 × 20 = 60. Rows 1–60 are skipped and the next at most 20 are returned. This requires stable ordering and can be expensive at large offsets.

</details>

<details>
<summary>2. Does sorting by timestamp and ID eliminate offset drift?</summary>

It eliminates ambiguous ties, but inserts/deletes still change how many rows precede an offset between requests. Stable ordering and stable snapshots are different properties.

</details>

<details>
<summary>3. Why is stream().skip().limit() after findAll insufficient?</summary>

The database and application may already have loaded every row and related data. Bounding must occur in the database query to control work and memory.

</details>

<details>
<summary>4. When is Slice preferable to Page?</summary>

When the UI needs sequential items and hasNext rather than exact totals. A Slice can avoid the extra count cost, while still needing a bounded query and deliberate response DTO.

</details>

<details>
<summary>5. What happens after deleting 9 from [9,8,7,6,5] between size-2 pages?</summary>

After first page [9,8], remaining data is [8,7,6,5]. Offset 2 now returns [6,5], skipping 7. That derives from count-based position shift.

</details>
