# Concurrent Writes and Database Constraints

## Question

How do you prevent two concurrent requests from creating duplicate likes?


## Let both requests pass the check

User 7 likes post 42 through two simultaneous requests, A and B. A asks whether `(42, 7)` exists and sees no row. Before A inserts, B asks the same question and also sees no row. Both checks are correct at the moment they run.

The missing piece is a rule enforced where both writes meet. “At most one like for this user and post” is the **invariant**. A **unique constraint** makes the database reject a second committed row with the same required `(post_id, user_id)` pair.

Two receptionists looking at the same empty seat illustrate the race. The booking register must resolve who reserves it. Unlike people coordinating verbally, database transactions also decide which related changes become durable together.

## Follow the winning and losing transactions

A inserts the pair. B attempts the same pair. If A commits, B cannot commit a second matching row; it may wait while A's outcome is unresolved and then encounter the conflict. If A rolls back, B may be able to succeed instead. In either case, the constraint protects the stored relationship.

A **transaction** groups the related database changes so they commit together or roll back. After a constraint failure, let the failed transaction roll back before reading again in a fresh one. Catching the exception and continuing to query inside the failed transaction is not a reliable recovery strategy.

## One row is only half the user-visible result

For a “set liked” request, the losing caller can receive the already-established liked state after safe conflict resolution. Handle only the expected duplicate case this way; a broken connection or a different constraint failure is not evidence that another request liked the post.

A “toggle like” request has different intent: two executions can undo one another. Do not borrow retry semantics from a set-state operation without defining them.

Keep the initial existence check if it simplifies the ordinary path, but let the constraint remain the authority under concurrency. Test both responses as well as the final row count. A single stored row plus an unexpected server error still leaves the API behavior unfinished.

## Where this appears in our backend

[PostLike](../../../../../src/main/java/com/instagram/backend/domain/PostLike.java) already declares the unique pair and non-null join columns. [PostService](../../../../../src/main/java/com/instagram/backend/service/PostService.java) checks existence before saving inside a transaction. These enforce storage uniqueness, but do not by themselves prove that both racing requests receive successful responses.

**Proposed:** explicit concurrent-conflict handling and a simultaneous-request integration test. Do not catch a database error and continue querying inside the same failed transaction.

## Choosing the approach and paying its costs

- Choose a unique constraint for uniqueness expressible by stored columns. It adds index maintenance and can make a racing writer wait or fail. PostgreSQL documents the combination-of-columns guarantee in its [constraints reference](https://www.postgresql.org/docs/16/ddl-constraints.html).
- Choose a row lock when several changes must coordinate through an existing row. Locking every like through the post row can serialize traffic to a popular post.
- A process-local lock cannot coordinate separate API instances. A distributed lock adds another dependency and is unnecessary for this simple database invariant.


## Paper drill: pause both requests after the read

Predict what happens when both existence checks finish before either insert. Then, in a proposed integration test, release both writers against PostgreSQL and inspect each response plus the final `(42, 7)` count.

The data should have one matching row. If the loser gets an unhandled server error, preserve the constraint and repair conflict handling rather than weakening uniqueness. Roll back the failed attempt and resolve the established state through a fresh transaction.

Keep this forced race as regression coverage. It detects a problem that repeated sequential calls may never trigger, and it proves the response contract separately from storage protection.

## Two-to-three-minute interview answer

I would start by stating the invariant: a user can have at most one like row for a post. A read followed by an insert is not sufficient, because two instances can both read the absence of the row. The database must enforce a unique pair of post ID and user ID, with neither value nullable.

The constraint protects the data, but I would separately design what the losing request returns. If another request has already committed the same like, returning the established liked state makes sense for a “set liked” operation. A “toggle like” operation has different semantics and should not be casually retried. After a constraint error, I would allow the failed transaction to roll back before reading the existing state in a new transaction. I would avoid treating every database exception as a duplicate, because connection failures and other integrity errors require different handling.

This backend already has the unique constraint and an existence check. I would not claim its current concurrent HTTP behavior is verified just from reading those lines. My next implementation task would add a database-backed concurrency test, force both requests through the race, and verify one stored row together with the agreed successful response contract. A broader workflow involving multiple tables may need additional transaction design, but a distributed lock is unnecessary for this invariant.


## Before moving on

Artifact: `READY`; comprehension: `TODO`. Explain this scenario in your own words, predict the paper drill, and attempt the questions before opening their answers. Record learning only from your own explanation and predictions.

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
