# 07. Offset pagination and bounded reads

## Question

How do you prevent a feed response from growing without limit?

[Module sequence](README.md) · Estimated time: 15–20 minutes · Artifact: READY · Learning: TODO

## What you will learn

Explain the problem behind offset pagination and bounded reads, trace its externally visible behavior, and identify one implementation risk before proposing a change.

Prerequisites: Lesson 6 and its stop/go check. All later lessons remain TODO until their own checkpoints are attempted.

## Must understand: problem and model

Fetching every post makes response size and database work grow with the product. Pagination splits a collection into bounded pages. An offset skips a number of ordered rows; a limit caps returned rows. It resembles reading page three of a catalog, except live inserts can shift what belongs to each page.

## Spring Boot example

Unless explicitly described as existing code, this is a proposed teaching excerpt, not an endpoint implemented by this documentation change. Application-specific collaborators and types require implementation; imports and class wiring are omitted. Preserve authentication when adapting an excerpt. This project uses Spring Boot 4.0.2 and Java 24, as recorded in [build configuration](../../../../build.gradle).

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

## Project mapping and practice

Read the actual [backend implementation](../../../../src/main/java/com/instagram/backend/repository/PostRepository.java) before changing it. The example above distinguishes existing behavior from a proposed extension; a linked file establishes the current starting point, not proof that the proposed design exists.

**Practice task:** For F-016 replace findAll feed loading with a bounded database query; measure query count and response size with 1000 posts.

Keep this a design exercise until a feature change is explicitly undertaken. An implementation must follow the repository integration-test, API verification, documentation, and commit workflow.

## Useful later: choices and costs

Offset pagination is simple for small administrative lists and jumping to a numbered page. Large offsets can require scanning many skipped rows; concurrent inserts cause drift. Fetch a Slice when only hasNext is needed; a Page can add a count query. Always impose a size cap and deterministic sort.

## Controlled failure drill

Predict size=1000000. An unbounded query increases memory and latency. Detect response-size and heap spikes, contain with hard caps, recover by reducing load, prevent with size-boundary tests and query-plan checks.

Describe the prediction before reading the symptom. In a future lab, use disposable data, record the observed response and state, and verify recovery rather than assuming it succeeded.

## Interview-ready answer outline

For a two-minute answer, start with the caller’s problem, define the main concept in plain language, explain the request trace above, then justify one choice and its cost. Finish by explaining how the failure drill would be detected and prevented in this backend. Do not claim the proposed extension is deployed.

## Stop/go and learner evidence

Proceed only after explaining the request trace without notes, predicting the failure, and answering all three checks below before opening their answers. Record the attempt in [learner progress](learner-progress.md). Artifact readiness does not establish comprehension or implementation readiness.

## Questions and explained answers

<details>
<summary>1. For page=3 and size=20, how many rows are skipped?</summary>

The zero-based offset is page × size = 3 × 20 = 60. Rows 1–60 are skipped and the next at most 20 are returned. This requires stable ordering and can be expensive at large offsets.

</details>

<details>
<summary>2. What exact response or state should the worked request produce, and why?</summary>

Order IDs 9,8,7,6,5. page=0,size=2 -> offset 0 -> [9,8]. page=1 -> offset 2 -> [7,6]. Insert 10 before the second request -> new order 10,9,8,7,6,5 -> page=1 returns [8,7], repeating 8.

This follows from the request boundary and state checks in the walkthrough, not merely from choosing a URL.

</details>

<details>
<summary>3. Which failure can remain despite a successful happy-path request, and how would you prevent it?</summary>

Predict size=1000000. An unbounded query increases memory and latency. Detect response-size and heap spikes, contain with hard caps, recover by reducing load, prevent with size-boundary tests and query-plan checks.

A successful request proves one case. The controlled failure needs its own observation and regression check.

</details>
