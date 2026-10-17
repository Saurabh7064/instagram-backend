# Concurrent Writes and Database Constraints

## Question

How do you prevent two concurrent requests from creating duplicate likes?

## Why this problem exists

Two API instances can both check that a like does not exist before either inserts it. A check in application code is useful for the ordinary path, but cannot enforce a rule across simultaneous writers.

## Analogy and its limit

Two receptionists can both see an empty seat on their screens. A central booking register must accept only one reservation. The analogy stops at database transactions: people do not automatically undo every related action when a booking fails.

## Terms

- An **invariant** is a rule that must remain true, such as one like per user per post.
- A **unique constraint** makes the database reject a duplicate combination of values.
- A **transaction** groups database changes so that they commit together or roll back together.

## Reasoning and design

1. Define the invariant as one row per `(post_id, user_id)`.
2. Enforce that combination in the database, with both columns required.
3. Let the ordinary path check for an existing like, but treat that check as an optimization.
4. Handle an insert conflict at a safe transaction boundary and return the already-established liked state.
5. Test simultaneous requests against the real database, then assert both stored row count and HTTP outcomes.

## Concrete trace

1. Requests A and B represent user 7 liking post 42. Both read “no like.”
2. A inserts `(42, 7)`; B attempts the same pair.
3. If A commits, the unique constraint prevents B from committing a second matching row.
4. B must finish rolling back its failed transaction before a fresh read retrieves the existing like.
5. With the proposed conflict handling, both callers observe “liked,” while the database contains exactly one matching row. If A rolls back instead, B may succeed; the invariant still holds.

## Instagram-backend mapping

[PostLike](../../../../../src/main/java/com/instagram/backend/domain/PostLike.java) already declares the unique pair and non-null join columns. [PostService](../../../../../src/main/java/com/instagram/backend/service/PostService.java) checks existence before saving inside a transaction. These enforce storage uniqueness, but do not by themselves prove that both racing requests receive successful responses.

**Proposed:** explicit concurrent-conflict handling and a simultaneous-request integration test. Do not catch a database error and continue querying inside the same failed transaction.

## Choices and trade-offs

- Choose a unique constraint for uniqueness expressible by stored columns. It adds index maintenance and can make a racing writer wait or fail. PostgreSQL documents the combination-of-columns guarantee in its [constraints reference](https://www.postgresql.org/docs/16/ddl-constraints.html).
- Choose a row lock when several changes must coordinate through an existing row. Locking every like through the post row can serialize traffic to a popular post.
- A process-local lock cannot coordinate separate API instances. A distributed lock adds another dependency and is unnecessary for this simple database invariant.

## Failure drill

**Prediction:** synchronize two requests immediately after their existence checks. **Detection:** inspect final row count and each HTTP result; one row plus an unhandled server error is incomplete API behavior. **Containment:** preserve the database constraint and stop automatic unlimited retries. **Recovery:** roll back the losing transaction and resolve its result using a fresh transaction. **Prevention:** retain a concurrent integration test that checks storage and response semantics.

## Two-to-three-minute interview answer

I would start by stating the invariant: a user can have at most one like row for a post. A read followed by an insert is not sufficient, because two instances can both read the absence of the row. The database must enforce a unique pair of post ID and user ID, with neither value nullable.

The constraint protects the data, but I would separately design what the losing request returns. If another request has already committed the same like, returning the established liked state makes sense for a “set liked” operation. A “toggle like” operation has different semantics and should not be casually retried. After a constraint error, I would allow the failed transaction to roll back before reading the existing state in a new transaction. I would avoid treating every database exception as a duplicate, because connection failures and other integrity errors require different handling.

This backend already has the unique constraint and an existence check. I would not claim its current concurrent HTTP behavior is verified just from reading those lines. My next implementation task would add a database-backed concurrency test, force both requests through the race, and verify one stored row together with the agreed successful response contract. A broader workflow involving multiple tables may need additional transaction design, but a distributed lock is unnecessary for this invariant.

## Practice status

Artifact: `READY`. Comprehension: `TODO`. Before marking practiced, explain the answer without notes, predict a failure, and answer the checkpoints before revealing their explanations.

## Questions and explained answers

<details>
<summary>Why can two existence checks both return false?</summary>

Neither request has committed its insert when the checks run. A read does not reserve the missing row.
</details>

<details>
<summary>Does one stored row prove the API handles concurrency correctly?</summary>

No. One request could still receive an unexpected server error. Verify the HTTP contract as well as the data invariant.
</details>

<details>
<summary>Why read again in a fresh transaction after a constraint error?</summary>

The failed database transaction may be unusable until rollback. Finishing rollback separates the failed insert from the recovery read.
</details>
