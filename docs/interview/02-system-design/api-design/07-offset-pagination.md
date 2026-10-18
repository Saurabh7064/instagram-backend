# 07. Offset pagination and bounded reads

## Question

How do you prevent a feed response from growing without limit?

[Module sequence](README.md) · Study in two passes: core model, then design decisions (15–20 minutes each) · Artifact: READY · Learning: TODO

## What you will learn

Derive offset windows, insertion/deletion drift, Page versus Slice, SQL-side bounds, and query-count costs for the current feed.

Prerequisites: Lesson 6 and its stop/go check. All later lessons remain TODO until their own checkpoints are attempted.

## Must understand: problem and model

Fetching every post makes response size and database work grow with the product. Pagination splits a collection into bounded pages. An offset skips a number of ordered rows; a limit caps returned rows. It resembles reading page three of a catalog, except live inserts can shift what belongs to each page.

## Spring Boot example

Examples are proposed except where explicitly identified as current code. Imports/wiring are omitted; named new services and DTOs need implementation. Preserve current authorization rules. The project uses [Spring Boot 4.0.2 and Java 24](../../../../build.gradle).

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

### Walkthrough

This proposed excerpt omits authentication only to focus on page bounds; preserve current identity checks in a real route. PageRequest uses zero-based pages. Sorting by timestamp and then ID breaks ties deterministically. FeedPage and feedQueries are proposed application types; converting a full in-memory list into pages does not bound database work.

## Request trace: predict, act, observe, explain

Before following the trace, predict the status and final stored state. Then trace these transitions in order:

Order IDs 9,8,7,6,5. page=0,size=2 -> offset 0 -> [9,8]. page=1 -> offset 2 -> [7,6]. Insert 10 before the second request -> new order 10,9,8,7,6,5 -> page=1 returns [8,7], repeating 8.

The observable result includes the response **and** the final stored state. A successful status alone cannot prove concurrency or authorization correctness.

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

### Work through drift in both directions

Initial order [9,8,7,6,5], page 0 size 2 -> [9,8]. If 10 is inserted, page 1 offset 2 -> [8,7]: duplicate 8. If 9 is deleted instead, remaining order [8,7,6,5], offset 2 -> [6,5]: post 7 is skipped. A deterministic sort removes tie ambiguity, not this shift across separate snapshots.

Deep pages also cost work even with indexes: offset 100000 means the database may still walk past 100000 qualifying entries. Do not promise O(limit) for offset queries. Exact counts can scan many rows and can disagree with item results under concurrent changes unless the isolation policy coordinates them. “Total” is usually an observation, not a reservation of future results.

### Query budget beyond row count

The current PostService maps each post and asks `existsByPostAndUser` per item; it also accesses the lazy author relationship. A page of 20 can therefore generate more than one SQL query. A projection/join and batched viewer-like lookup can reduce this, but inspect the actual SQL and query plan. Fetching a collection join with pagination has its own pitfalls; do not add fetch joins blindly.

Cap size, reject negative page, and set a product maximum page depth if offset browsing is bounded. A successful empty page after the end is normally 200 with no items and hasNext false, not an exception.

## Project mapping and practice

Read the actual [backend implementation](../../../../src/main/java/com/instagram/backend/repository/PostRepository.java) before changing it. The example above distinguishes existing behavior from a proposed extension; a linked file establishes the current starting point, not proof that the proposed design exists.

**Practice task:** For F-016 replace findAll feed loading with a bounded database query; measure query count and response size with 1000 posts.

Keep this a design exercise until a feature change is explicitly undertaken. An implementation must follow the repository integration-test, API verification, documentation, and commit workflow.

## Useful later: choices and costs

Offset pagination is simple for small administrative lists and jumping to a numbered page. Large offsets can require scanning many skipped rows; concurrent inserts cause drift. Fetch a Slice when only hasNext is needed; a Page can add a count query. Always impose a size cap and deterministic sort.

## Controlled failure drill

Predict size=1000000. An unbounded query increases memory and latency. Detect response-size and heap spikes, contain with hard caps, recover by reducing load, prevent with size-boundary tests and query-plan checks.

Describe the prediction before reading the symptom. In a future lab, use disposable data, record the observed response and state, and verify recovery rather than assuming it succeeded.

## Interview-ready answer

I use offset pages when numbered navigation and modest data sizes justify them. I enforce bounds in the database, define a deterministic order with an ID tie-breaker, and expose my own page DTO. Exact totals and deep offsets have query cost. Deterministic ordering does not prevent separate requests from drifting when rows are inserted or deleted. The current feed loads all posts and performs per-post mapping/lookups, so merely slicing the Java list would leave the main cost intact. I would measure generated SQL and queries per page, test boundaries and data shifts, and use a cursor contract when long live-feed traversal matters more than arbitrary page jumps.

## Stop/go and learner evidence

Proceed only after explaining the request trace without notes, predicting the failure, and answering all three checks below before opening their answers. Record the attempt in [learner progress](learner-progress.md). Artifact readiness does not establish comprehension or implementation readiness.

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
