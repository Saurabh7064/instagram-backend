# Search and Near-Real-Time Indexing

## Question

Users need caption, hashtag, and username search, but newly created or deleted content is not reflected consistently.

**“How would you build caption, hashtag, and username search with near-real-time updates?”**


## A search result is another copy of the post

Post 42 contains `#sunset`. Finding it by ID is a direct lookup; finding every eligible post containing that term requires a different access path. For small exact or prefix username searches, an appropriate database index may be enough. More extensive text matching and relevance can justify a dedicated search system.

An **inverted index** records which documents contain each searchable term. It resembles a book index pointing from a topic to pages. In an online service those documents keep changing, so the index can temporarily disagree with the authoritative post records. The book analogy does not address that synchronization problem.

An **analyzer** turns source text into searchable terms according to defined rules, such as case handling. Choose those rules for each field: a username and a caption do not necessarily need the same matching behavior.

## Follow post 42 from creation through deletion

In the proposed design, creating post 42 commits its content and a version-1 event together. An indexer later builds the search document. During that delay, a direct post read may work while search does not yet find it. This accepted propagation delay is the practical meaning of near-real-time here; define and measure its target.

Deleting the post records version 2 and a **tombstone**, durable evidence that the document should remain absent. If a delayed version-1 event arrives afterwards, the consumer must retain enough ordering information to reject it. Deleting both the document and all evidence of its newer version can allow an old event to recreate it.

## Search relevance does not establish permission

The search engine returns candidate IDs and scores. Before showing post 42, verify the current eligibility rules and fetch trusted display data through an authorized path. A stale index must not decide that deleted or newly private content is safe to reveal. Filter before returning snippets or other protected fields too; hiding an ID after exposing the caption is too late.

A versioned change stream, duplicate-safe indexing and periodic comparison make the index repairable. Keep a rebuild path from authoritative records. Another system adds operations and temporary disagreement; add it when the required query behavior and measured load justify that cost.

## Where this appears in our backend

[Post](../../../../../src/main/java/com/instagram/backend/domain/Post.java) stores captions and post metadata, while [UserAccount](../../../../../src/main/java/com/instagram/backend/domain/UserAccount.java) stores usernames. [PostRepository](../../../../../src/main/java/com/instagram/backend/repository/PostRepository.java) currently supports feed ordering and author counts, not text search.

**Proposed, not implemented:** versioned outbox events, a search engine, analyzers for captions/hashtags/usernames, idempotent indexers, tombstones, authorization-aware hydration, lag alerts, and rebuild tooling.

## Choosing the approach and paying its costs

- Use database prefix lookup for small, exact username workloads when it meets the SLO. Do not stretch wildcard scans into full relevance search at scale.
- Use a search engine for text relevance, filters, typo tolerance, and ranking. It adds a derived system with short staleness and operational cost.
- Hydrate every result from the authority for strongest freshness when volume permits; a replicated projection is faster but must enforce deletions and privacy correctly.


## Paper drill: an old create follows a delete

Predict the result if version 1 reaches the indexer after the version-2 deletion. A consumer applying events only by arrival order can resurrect post 42.

Detect the mismatch by comparing the document and stored version evidence with authoritative deletion state. Contain exposure through current eligibility checks and purge the stale document. Recover by replaying changes in a version-safe way or rebuilding the affected range.

Prevent recurrence by keeping deletion/version evidence for the supported replay window, recording change events with the source transaction and testing delayed events. Monitor event age as well as indexing errors: a running consumer can still be too far behind.

## Two-to-three-minute interview answer

I would keep PostgreSQL as the source of truth and build search as a versioned projection. Post and user changes commit with outbox events. Idempotent indexers transform captions, hashtags, and usernames into documents for an inverted index, applying only the newest version so delayed events cannot resurrect old content.

The query service sends text and filters to the search cluster, receives ranked IDs, then enforces current privacy, blocking, and deletion rules before returning hydrated results. New content may take a short documented time to appear, but deleted or forbidden content needs a stricter safety path using tombstones and authoritative filtering. I would monitor oldest unpublished and unindexed event age, dead letters, query latency, and zero-result changes.

For small exact username queries, a database index may remain enough. A search engine is justified when relevance, tokenization, filters, and scale exceed that simpler option, and it must always be rebuildable from authoritative data.


## Before moving on

Artifact: `READY`; comprehension: `TODO`. Explain this scenario in your own words, predict the paper drill, and attempt the questions before opening their answers. Record learning only from your own explanation and predictions.

## Questions and explained answers

<details>
<summary>Why should the search index not be the source of truth?</summary>

It is asynchronously derived and may lag, drop updates, or be rebuilt. Authoritative ownership, visibility, and deletion state belong in durable transactional storage.
</details>

<details>
<summary>How does a version prevent stale event resurrection?</summary>

The indexer records the newest applied version and ignores older events. A delayed create or update cannot overwrite a later deletion tombstone.
</details>

<details>
<summary>What must be checked before returning a search result?</summary>

Current deletion, privacy, blocking, and authorization rules must still allow the viewer to see it. Search relevance never grants access.
</details>
