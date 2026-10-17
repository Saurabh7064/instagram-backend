# Search and Near-Real-Time Indexing

## Question

Users need caption, hashtag, and username search, but newly created or deleted content is not reflected consistently.

**“How would you build caption, hashtag, and username search with near-real-time updates?”**

## Why this problem exists

Transactional databases are optimized for authoritative writes and known lookup paths, not large-scale text relevance, token matching, typo handling, and ranking. A search index provides those queries but is a derived copy, so updates may be delayed, duplicated, or missed unless publication and deletion are designed explicitly.

## Analogy: a book index

A book’s index maps topics to pages so readers do not scan every line. The analogy stops because an online search index is continuously rebuilt across machines and can temporarily disagree with the authoritative database.

## Terms to establish

- An **inverted index** maps normalized terms to matching documents.
- An **analyzer** converts text into searchable tokens using rules such as lowercasing.
- A **tombstone** records that a document must be removed or hidden.
- **Near-real-time** means updates appear after a small accepted delay rather than in the original transaction.

## What I would do

1. Keep PostgreSQL authoritative for users, posts, visibility, and deletion state.
2. Commit versioned create, update, and delete outbox events with those records.
3. Let idempotent indexers transform content into separate user and post documents with suitable analyzers.
4. Query the search cluster for ranked IDs, then enforce current authorization and hydrate trusted fields from the authoritative store or a safe projection.
5. Measure indexing lag, use tombstones for deletion, and run reconciliation and full rebuilds from the source of truth.

## Concrete walkthrough

1. A user creates post 42 with `#sunset`; PostgreSQL commits the post and event version 1.
2. The indexer writes document 42, and search begins returning its ID after a short delay.
3. The owner deletes the post; version 2 is a tombstone.
4. A delayed version-1 event arrives later, but the indexer rejects it because version 2 is newer.
5. Until removal propagates, the query layer checks visibility/deletion before returning the result, preventing a stale index from exposing deleted content.

## Instagram-backend mapping

[Post](../../../../../src/main/java/com/instagram/backend/domain/Post.java) stores captions and post metadata, while [UserAccount](../../../../../src/main/java/com/instagram/backend/domain/UserAccount.java) stores usernames. [PostRepository](../../../../../src/main/java/com/instagram/backend/repository/PostRepository.java) currently supports feed ordering and author counts, not text search.

**Proposed, not implemented:** versioned outbox events, a search engine, analyzers for captions/hashtags/usernames, idempotent indexers, tombstones, authorization-aware hydration, lag alerts, and rebuild tooling.

## Choices and trade-offs

- Use database prefix lookup for small, exact username workloads when it meets the SLO. Do not stretch wildcard scans into full relevance search at scale.
- Use a search engine for text relevance, filters, typo tolerance, and ranking. It adds a derived system with short staleness and operational cost.
- Hydrate every result from the authority for strongest freshness when volume permits; a replicated projection is faster but must enforce deletions and privacy correctly.

## Failure drill

**Prediction:** a delete event is missed and a private or removed post remains searchable. **Symptom and detection:** reconciliation finds an index document whose authoritative row is absent or ineligible. **Containment:** filter results through authoritative visibility checks and purge the document. **Recovery:** replay the outbox or rebuild the affected range, preserving latest-version ordering. **Prevention:** use transactional publication, tombstones, idempotent version checks, lag alerts, and routine reconciliation.

## Two-to-three-minute interview answer

I would keep PostgreSQL as the source of truth and build search as a versioned projection. Post and user changes commit with outbox events. Idempotent indexers transform captions, hashtags, and usernames into documents for an inverted index, applying only the newest version so delayed events cannot resurrect old content.

The query service sends text and filters to the search cluster, receives ranked IDs, then enforces current privacy, blocking, and deletion rules before returning hydrated results. New content may take a short documented time to appear, but deleted or forbidden content needs a stricter safety path using tombstones and authoritative filtering. I would monitor oldest unpublished and unindexed event age, dead letters, query latency, and zero-result changes.

For small exact username queries, a database index may remain enough. A search engine is justified when relevance, tokenization, filters, and scale exceed that simpler option, and it must always be rebuildable from authoritative data.

## Practice status

The artifact is `READY`; comprehension remains `TODO` until the learner can trace create and delete propagation, explain staleness, and diagnose one missed event.

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
