# Choosing SQL or NoSQL

## Question

PostgreSQL becomes slower as posts and likes grow, and a teammate proposes replacing every database with NoSQL.

**“How do you choose SQL, NoSQL, or both for posts, likes, and feeds?”**


## Start with the rule a like must obey

User 7 likes post 42. Whatever database we choose, the product wants one relationship between that user and that post. A retry or two simultaneous taps must not create two likes. This always-true business rule is an **invariant**.

A relational database can enforce the pair `(post_id=42, user_id=7)` as unique. A **transaction** can commit the relationship and its related database changes together, or roll them back together. These are concrete reasons to retain PostgreSQL for this write path. “The feed is slow” does not tell us that either guarantee should be abandoned.

First inspect the slow query, its indexes and how much data it returns. Moving a query that reads every post into another database may reproduce the same excessive work with different tools.

## A feed copy has a different job

Suppose measured growth eventually justifies a separate, viewer-keyed feed store. Its job is to answer “which posts should viewer 7 see?” quickly. It need not own whether the like exists. PostgreSQL remains the **authoritative source**: when the stores disagree, its accepted business records decide what is correct.

Copying selected data into a shape optimized for a particular read is called **denormalization**. It saves work during reads by doing preparation earlier. The cost is maintaining another copy and handling the time before that copy catches up.

A ledger and a noticeboard provide a useful picture. The ledger records the accepted transaction; the noticeboard displays selected information. The analogy stops when software must handle concurrent updates, duplicate messages and a failed copying process without a person supervising each change.

## Make the copy recoverable

A proposed reliable handoff would commit the like and an event record in the same PostgreSQL transaction. A publisher can then retry delivering that recorded event, and a consumer must recognize repeated events. If the consumer stops, the like remains recorded while the read copy becomes stale. Once repaired, the consumer catches up or the copy is rebuilt.

That extra database is worthwhile only if a measured query need pays for the synchronization, backup, monitoring and operational work. SQL and NoSQL each cover many designs; choose the particular store's guarantees and access patterns, rather than treating either category as one universal architecture.

## Where this appears in our backend

[UserAccount.java](../../../../../src/main/java/com/instagram/backend/domain/UserAccount.java), [Post.java](../../../../../src/main/java/com/instagram/backend/domain/Post.java), and [PostLike.java](../../../../../src/main/java/com/instagram/backend/domain/PostLike.java) use PostgreSQL as authority. `PostLike` enforces unique `(post_id, user_id)`, and [PostService.java](../../../../../src/main/java/com/instagram/backend/service/PostService.java) updates likes transactionally. Materialized feeds, search, outbox publishing, and idempotent consumers are proposed, not implemented.

## Choosing the approach and paying its costs

- Choose SQL when transactions and constraints protect important rules; do not force it onto every scale pattern.
- Choose a key-value or wide-column store for known, partitionable high-volume queries; accept denormalization and limited joins.
- Use both only when the need justifies synchronization and operational burden.


## Paper drill: only one of two writes succeeds

Predict the result if the API saves a post in PostgreSQL, then crashes before writing its feed copy. PostgreSQL contains the post; the feed does not. Retrying a best-effort second write is possible only if we still know reliably which change needs copying.

Detect the gap by comparing authoritative posts with feed entries and checking copying progress. Stop treating the two stores as independent authorities. Serve correctness-sensitive fields from PostgreSQL while repairing the projection.

Recover by replaying recorded changes or rebuilding the affected feed range. The prevention is a durable handoff recorded with the original business change, duplicate-safe consumption and regular comparison of the copy against its source. A healthy second database alone cannot prove that it contains every accepted change.

## Two-to-three-minute interview answer

I would start with access patterns and invariants, not replace PostgreSQL because load increased. Identity, ownership, and one-like-per-user-per-post need constraints and transactional correctness, so relational storage is a strong authority. I would fix query shape, indexes, pagination, and capacity first.

For a large feed or search path, I may add a specialized read store. A viewer-keyed timeline fits a partitioned key-value or wide-column store when slight propagation delay is acceptable; a search index fits text ranking. Neither should become an accidental second authority.

Committed changes should update projections through an outbox and idempotent consumers, so partial failure creates visible lag rather than silent divergence. I would monitor that lag and rehearse rebuilding. Another database adds consistency, backup, security, capacity, and on-call cost, so I would introduce it only for a measured need whose benefit outweighs that burden.


## Before moving on

Artifact: `READY`; comprehension: `TODO`. Explain this scenario in your own words, predict the paper drill, and attempt the questions before opening their answers. Record learning only from your own explanation and predictions.

## Questions and explained answers

<details>
<summary>Why is “PostgreSQL is slow” not enough reason to choose NoSQL?</summary>

The cause may be an unbounded query, missing index, lock, or insufficient capacity. A database category change does not automatically fix a bad access pattern and may remove useful guarantees.
</details>

<details>
<summary>Which store should win if the feed projection disagrees with a post record?</summary>

The declared authoritative store—here PostgreSQL—wins. The derived feed should be repaired or rebuilt from durable authoritative changes.
</details>

<details>
<summary>What makes a dual-write approach unsafe?</summary>

The process can fail after one write succeeds and before the other does. Without one atomic commit or a durable outbox, the stores can silently diverge.
</details>
