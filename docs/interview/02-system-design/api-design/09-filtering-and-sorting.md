# 09. Filtering and sorting contracts

## Question

How do you expose search options without exposing the database?

[Module sequence](README.md) · Study in two passes: core model, then design decisions (15–20 minutes each) · Artifact: READY · Learning: TODO

## What you will learn

Specify filter combination, empty/unknown parameter policies, allowlisted sorting, wildcard handling, and bounded query cost.

Prerequisites: Lesson 8 and its stop/go check. All later lessons remain TODO until their own checkpoints are attempted.

## Must understand: problem and model

Users need to select subsets and order results, but arbitrary query expressions can be expensive or unsafe. A filter selects matching records; a sort defines result order; an allowlist names accepted choices. A restaurant menu offers bounded options; unlike a menu, combinations can change database query cost dramatically.

## Spring Boot example

Examples are proposed except where explicitly identified as current code. Imports/wiring are omitted; named new services and DTOs need implementation. Preserve current authorization rules. The project uses [Spring Boot 4.0.2 and Java 24](../../../../build.gradle).

```java
enum PostOrder { NEWEST, OLDEST }

@GetMapping("/posts")
PostPage search(
        @RequestParam(required = false) String author,
        @RequestParam(defaultValue = "NEWEST") PostOrder order) {
    return postQueries.search(author, order);
}
```

### Walkthrough

The enum limits accepted sort choices instead of accepting raw SQL. An absent author means the documented default scope, not permission to see private data. postQueries must also enforce pagination, authorization, and parameter binding. Unknown enum values should become a documented 400 response.

## Request trace: predict, act, observe, explain

Before following the trace, predict the status and final stored state. Then trace these transitions in order:

GET /posts?author=mira.frames&order=NEWEST -> validate options -> bind author as data -> apply visibility scope -> order timestamp and ID -> bounded response. order=DROP_TABLE is not a recognized enum, so reject it before querying.

The observable result includes the response **and** the final stored state. A successful status alone cannot prove concurrency or authorization correctness.

## Specify a query language small enough to operate

A query contract needs more than optional Strings. Define how each parameter combines, how empty values behave, how ties sort, and what resource budget one request may consume.

| Parameter | Proposed meaning | Validation | Query implication |
|---|---|---|---|
| author | Exact username | Nonblank if supplied | Parameterized equality |
| createdAfter | Exclusive UTC instant | Valid ISO-8601 timestamp | Range on createdAt |
| tag, repeated | Any listed tag (OR) | Max 5 values | Membership query; deduplicate rows |
| order | NEWEST or OLDEST | Enum allowlist | Always append ID tie-breaker |
| limit | Requested maximum | 1–100 | Database LIMIT |
| fields | Optional public projection | Allowlist and max count | Never grants extra field access |

These are design examples, not supported current parameters. `tag=a&tag=b` could mean AND instead of OR in another API; either is defensible if explicit. Combining author with tags can be AND across dimensions. Unknown parameters may be rejected to catch typos, or ignored for forward compatibility; choose deliberately and test it. Silent `limti=1000` ignored as a typo may surprise clients.

### Parameterize values, allowlist identifiers

```java
Sort sort = switch (order) {
    case NEWEST -> Sort.by("createdAt").descending()
            .and(Sort.by("id").descending());
    case OLDEST -> Sort.by("createdAt").ascending()
            .and(Sort.by("id").ascending());
};
```

Proposed service fragment: each enum maps to server-owned entity property names. Bound query parameters protect values; they do not make arbitrary caller-supplied SQL column names safe. For text LIKE queries, escaping SQL injection is separate from escaping `%` and `_` wildcard semantics. Decide whether a user searching `100%` means the literal percentage sign or a wildcard.

### Work through combined filtering

Suppose posts are 9(mira,2026-10-17), 8(lee,2026-10-17), 7(mira,2026-10-16). For author=mira, createdAfter=2026-10-16T12:00:00Z, NEWEST, assume post 7 is at 10:00 that day. Filter by author -> [9,7]. Apply exclusive time boundary -> [9]. Sort -> [9]. Limit 20 -> [9]. Filtering after fetching 20 unrelated posts would produce incomplete pages and potentially false hasNext values.

Public “contains anywhere” search may not use a normal B-tree index efficiently. Case-insensitive comparison, language normalization, relevance ordering, and full-text search need an explicit storage/query strategy. Start with exact-match contracts until the product needs more. Bound query length, repeated filters, page size, and expensive combinations; authorization remains a mandatory predicate for every plan.

## Project mapping and practice

Read the actual [backend implementation](../../../../src/main/java/com/instagram/backend/repository/PostRepository.java) before changing it. The example above distinguishes existing behavior from a proposed extension; a linked file establishes the current starting point, not proof that the proposed design exists.

**Practice task:** For F-022 define supported search fields, matching rules, sort order, maximum query length, and a bounded result window.

Keep this a design exercise until a feature change is explicitly undertaken. An implementation must follow the repository integration-test, API verification, documentation, and commit workflow.

## Useful later: choices and costs

Explicit options keep contracts stable as table names change. Flexible query languages help complex internal tools but need cost controls, field permissions, and query planning. Case sensitivity, whitespace, empty filters, and repeated parameters need documented meanings. A missing filter must never bypass visibility rules.

## Controlled failure drill

Predict passing a query expression as a raw ORDER BY fragment. Concatenation can allow injection or unindexed sorts. Detect malicious-input tests and slow-query metrics, contain by disabling arbitrary fields, recover with bound values and allowlisted expressions, prevent with review and query-budget tests.

Describe the prediction before reading the symptom. In a future lab, use disposable data, record the observed response and state, and verify recovery rather than assuming it succeeded.

## Interview-ready answer

I publish a small intentional query language instead of exposing database expressions. I define exact matching, repeated-parameter semantics, filter combination, order and ties, limits, and unknown-field behavior. I parameterize values and map admitted sort choices to server-owned expressions. SQL injection protection is distinct from deciding whether percent or underscore is a wildcard. Filtering and permissions must happen before pagination. For the future search feature I would start with a bounded indexed use case, inspect representative plans, and introduce full-text or flexible query syntax only when its operational and compatibility costs are justified.

## Stop/go and learner evidence

Proceed only after explaining the request trace without notes, predicting the failure, and answering all three checks below before opening their answers. Record the attempt in [learner progress](learner-progress.md). Artifact readiness does not establish comprehension or implementation readiness.

## Questions and explained answers

<details>
<summary>1. Why use an enum for sorting instead of arbitrary SQL text?</summary>

An enum exposes a finite supported choice and prevents callers selecting arbitrary database expressions. Translate each choice to a fixed server-owned sort, bind filter values, and retain visibility checks and page bounds.

</details>

<details>
<summary>2. Is a bound parameter enough to make arbitrary ORDER BY input safe?</summary>

No. Bind values as data, but choose identifiers/expressions from a server-owned allowlist. Sort field selection is not an ordinary value substitution problem.

</details>

<details>
<summary>3. Why filter before limiting?</summary>

Limiting unrelated rows then filtering produces incomplete pages and incorrect continuation metadata. The selected authorized set must be formed before pagination.

</details>

<details>
<summary>4. Are escaped SQL values automatically literal LIKE searches?</summary>

No. Percent and underscore can retain wildcard meaning even without SQL injection. Specify and implement literal-versus-pattern semantics separately.

</details>

<details>
<summary>5. Why document repeated tag parameters?</summary>

tag=a&tag=b can mean any tag or all tags. Both are possible product rules; unstated combinations make clients and queries disagree.

</details>
