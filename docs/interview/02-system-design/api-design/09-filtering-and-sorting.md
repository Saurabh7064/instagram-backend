# 09. Filtering and sorting contracts

## Question

How do you expose search options without exposing the database?

[Module sequence](README.md) · Artifact: READY · Learning: TODO

## What you will learn

Define a small set of search choices, follow their order of application, and explain how the server retains control of permissions and query cost.

Builds on the ordered, bounded result from [cursor pagination](08-cursor-pagination.md).

## Let callers choose results without writing your query

Mira wants to see her recent posts, newest first. A request such as `/api/posts?author=mira.frames&order=NEWEST` expresses that need without exposing a database column or SQL fragment. The server translates the supported choices into its own query.

A **filter** decides which posts qualify. A **sort** decides their order. Those are separate decisions: selecting Mira's posts does not say whether the oldest or newest should appear first. We also need the page limit from the [pagination lessons](08-cursor-pagination.md), because a filter can still match many posts.

It is tempting to accept any column name for sorting and any expression for filtering. That makes a short controller, but transfers control over query cost and internal fields to the caller. An **allowlist** is the opposite approach: publish a small set of supported choices and translate each one deliberately.

A restaurant menu is a useful comparison: the customer chooses among options the kitchen has agreed to provide. The limit is cost. Two individually cheap filters can combine into an expensive database query, so listing supported options does not remove the need to inspect their plans.

We will use one author's posts and one time boundary below. Extra options in the reference table are proposed extensions, not current endpoint behavior.

## Expose named choices in the controller

The project uses [Spring Boot 4.0.2 and Java 24](../../../../build.gradle). The excerpt below is a design proposal unless identified as current code; omitted imports and collaborators still need implementation.

```java
enum PostOrder { NEWEST, OLDEST }

@GetMapping("/posts")
PostPage search(
        @RequestParam(required = false) String author,
        @RequestParam(defaultValue = "NEWEST") PostOrder order) {
    return postQueries.search(author, order);
}
```

### Read the boundary in the code

`PostOrder` gives the caller two named choices. The service maps them to server-owned sort expressions, instead of accepting raw database syntax. If `order` cannot bind to the enum, it should produce the documented 400 error rather than a raw exception.

An omitted author means the API's default selection, never “skip visibility checks.” The proposed query service still needs page bounds, permissions, and bound filter values; the controller signature alone supplies none of those guarantees.

## Apply selection before taking the page

Use three posts: 9 by Mira at `2026-10-17T10:00:00Z`, 8 by Lee at that same time, and 7 by Mira at `2026-10-16T10:00:00Z`. The caller asks for Mira's posts created after `2026-10-16T12:00:00Z`, newest first, limit 20.

The author condition leaves `[9,7]`. The exclusive time condition removes 7, because 10:00 is before 12:00 on October 16. The selected result is therefore `[9]`; sorting and limit 20 keep that one post. Visibility must be part of this selection too.

Taking 20 unrelated posts and only then filtering them in Java would answer a different question. It could return a short page despite more matching rows in the database. The query must select the authorized matching set before it takes the bounded window.

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

### Keep the public choice separate from storage details

For the fixed request above, the service combines author, exclusive time, and visibility in one database selection before ordering and limiting. This means a new storage layout can preserve the same public request: callers ask for NEWEST, while the server owns what fields and query plan implement that choice.

Public “contains anywhere” search may not use a normal B-tree index efficiently. Case-insensitive comparison, language normalization, relevance ordering, and full-text search need an explicit storage/query strategy. Start with exact-match contracts until the product needs more. Bound query length, repeated filters, page size, and expensive combinations; authorization remains a mandatory predicate for every plan.

## Apply this to the backend

[PostRepository](../../../../src/main/java/com/instagram/backend/repository/PostRepository.java) is the current feed query starting point. It does not support the author/time/tag language proposed here. Map future public choices to explicit repository predicates rather than exposing its Java property names directly.

For F-022, choose the smallest useful search contract: supported fields, exact-versus-pattern matching, time boundary, order, query-length cap, and result cap. Explain which combinations will need query-plan verification before implementation.

## Useful later: choices and costs

Explicit options keep contracts stable as table names change. Flexible query languages help complex internal tools but need cost controls, field permissions, and query planning. Case sensitivity, whitespace, empty filters, and repeated parameters need documented meanings. A missing filter must never bypass visibility rules.

## Let a sort option reach the query unchanged

On paper, replace the admitted `NEWEST` choice with caller-supplied query text. Predict two possible failures: SQL assembled by concatenation can admit unintended syntax, and an otherwise valid but unsupported sort can force expensive work. Binding filter values does not justify accepting arbitrary identifiers or expressions.

Detect the first with malicious-input rejection checks and the second with representative query plans and latency limits. Contain the public surface by removing arbitrary sort expressions. Restore the fixed choice-to-expression mapping and verify the existing author/time result remains `[9]`. Prevent recurrence by reviewing each new supported option's permissions and cost, rather than treating all accepted strings as equally safe.

## Interview-ready answer

I publish a small intentional query language instead of exposing database expressions. I define exact matching, repeated-parameter semantics, filter combination, order and ties, limits, and unknown-field behavior. I parameterize values and map admitted sort choices to server-owned expressions. SQL injection protection is distinct from deciding whether percent or underscore is a wildcard. Filtering and permissions must happen before pagination. For the future search feature I would start with a bounded indexed use case, inspect representative plans, and introduce full-text or flexible query syntax only when its operational and compatibility costs are justified.

## Before moving on

Derive `[9]` from the fixed author/time request. Continue when you can explain filter-before-limit, the sort allowlist, and the difference between binding a value and admitting a query expression.

Attempt the questions before opening their answers and record the explanation in [learner progress](learner-progress.md). This revision leaves learning status unchanged; a finished document is not evidence that you have demonstrated the idea.

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
