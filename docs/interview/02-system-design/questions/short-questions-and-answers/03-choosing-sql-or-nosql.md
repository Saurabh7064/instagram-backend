# Choosing SQL or NoSQL

## Question

PostgreSQL becomes slower as posts and likes grow, and a teammate proposes replacing every database with NoSQL.

**“How do you choose SQL, NoSQL, or both for posts, likes, and feeds?”**

## Why this problem exists

Data paths need different guarantees and query shapes. Users, ownership, and unique likes benefit from transactions and constraints; a precomputed timeline may need partitioned read throughput. Choosing by brand name can trade a bottleneck for lost guarantees and harder operations.

## Analogy: a ledger and a noticeboard

A bank ledger protects authoritative records while a noticeboard provides a fast copy that can be rebuilt. The analogy stops because distributed stores may accept independent writes and serve stale data, so ownership and synchronization must be explicit.

## Terms to establish

- A **transaction** groups changes so they commit or fail together.
- An **invariant** is a rule that must remain true, such as one like per user and post.
- **Denormalization** duplicates data into a read-friendly shape.
- The **authoritative source** is the system whose value wins during disagreement.

## What I would do

1. List access patterns, volume, ordering, consistency, uniqueness, joins, and failure behavior.
2. Keep users, posts, ownership, and unique likes relational while transactions matter.
3. Add a specialized store only for a demonstrated access pattern, and make that projection rebuildable from one authority.

## Concrete walkthrough

A like inserts `(post_id=42, user_id=7)` under a uniqueness constraint and updates the count transactionally. A retry cannot create a second relationship. Later, an outbox event can update a denormalized ranking view through an idempotent consumer. If that consumer stops, authoritative data remains correct and the projection catches up.

## Instagram-backend mapping

[UserAccount.java](../../../../../src/main/java/com/instagram/backend/domain/UserAccount.java), [Post.java](../../../../../src/main/java/com/instagram/backend/domain/Post.java), and [PostLike.java](../../../../../src/main/java/com/instagram/backend/domain/PostLike.java) use PostgreSQL as authority. `PostLike` enforces unique `(post_id, user_id)`, and [PostService.java](../../../../../src/main/java/com/instagram/backend/service/PostService.java) updates likes transactionally. Materialized feeds, search, outbox publishing, and idempotent consumers are proposed, not implemented.

## Choices and trade-offs

- Choose SQL when transactions and constraints protect important rules; do not force it onto every scale pattern.
- Choose a key-value or wide-column store for known, partitionable high-volume queries; accept denormalization and limited joins.
- Use both only when the need justifies synchronization and operational burden.

## Failure drill

**Prediction:** the API best-effort writes PostgreSQL and a NoSQL feed store, then crashes after only one succeeds. **Symptom and detection:** feed entries disagree with authoritative posts, and reconciliation finds missing or orphaned records. **Containment:** stop unsafe dual writes and serve correctness-sensitive data from PostgreSQL. **Recovery:** choose the authority, reconcile projections, and replay durable changes. **Prevention:** commit an outbox record with the business transaction, consume it idempotently, and alert on projection lag.

## Two-to-three-minute interview answer

I would start with access patterns and invariants, not replace PostgreSQL because load increased. Identity, ownership, and one-like-per-user-per-post need constraints and transactional correctness, so relational storage is a strong authority. I would fix query shape, indexes, pagination, and capacity first.

For a large feed or search path, I may add a specialized read store. A viewer-keyed timeline fits a partitioned key-value or wide-column store when slight propagation delay is acceptable; a search index fits text ranking. Neither should become an accidental second authority.

Committed changes should update projections through an outbox and idempotent consumers, so partial failure creates visible lag rather than silent divergence. I would monitor that lag and rehearse rebuilding. Another database adds consistency, backup, security, capacity, and on-call cost, so I would introduce it only for a measured need whose benefit outweighs that burden.

## Practice status

The artifact is ready; comprehension is not implied. Practice by classifying one authoritative write and one rebuildable read model, then explaining how they remain consistent.

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
