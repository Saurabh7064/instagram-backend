# 07 — Choosing Service Boundaries and Data Ownership

> **Interview question:** How would you identify good microservice boundaries in a monolith, and how should data ownership work after the split?

> [One-page revision card](../../revision-cards/microservices/07-service-boundaries-and-data-ownership.md)

- Status: `READY`; comprehension not yet demonstrated
- Time: 20–25 minutes
- Primary idea: split around business responsibilities and invariants, then give one service authority to change each piece of data

## Why does this matter?

Turning every controller or database table into a service creates more deployments, but it does not create useful independence. A request still crosses many services, a small feature still requires coordinated releases, and several services may update the same records. This is a **distributed monolith**: monolith-level coupling plus network failures.

A useful boundary keeps behavior and the data it protects together. It should let a team make a normal business change without editing several services or joining directly across their databases.

## Analogy: departments in a store

A store may have a returns desk, warehouse, and accounting department. Each owns a clear decision:

- the returns desk decides whether a return is allowed;
- the warehouse decides how much stock is available;
- accounting records refunds.

Other departments request an outcome rather than reaching into a department's files and editing them.

The analogy has limits. Software can copy data into read models, messages may arrive late or twice, and a business operation may legitimately span several owners. The analogy explains authority, not distributed consistency.

## Plain-language model

### Business capability

A **business capability** is an outcome the business recognizes, such as publishing a post, storing media, authenticating a user, or processing a payment. Start with these behaviors, not technical layers such as “controller service” and “database service.”

### Invariant

An **invariant** is a rule that must always remain true. Examples:

- only the post owner can edit a post;
- one user can like one post at most once;
- inventory cannot be promised below the allowed threshold.

Data needed to enforce one invariant usually belongs with the behavior enforcing it. Splitting the like record and the uniqueness rule across unrelated services would make correctness harder.

### Data owner

The **data owner** is the only service allowed to make authoritative changes to that data. Other services use its API, react to its events, or keep explicitly non-authoritative copies for reads.

Ownership does **not** mean nobody else may store a post ID or username. It means those copies cannot silently become a second source of truth.

## How I find a boundary

For each candidate capability, ask:

1. **What business decision does it make?** “Post service owns publishing and editing,” is clearer than “post service owns the `posts` table.”
2. **Which rules must change together?** Code and data that protect one invariant should usually remain together.
3. **Which features change together in real work?** If every post change also modifies the proposed profile service, the boundary may be wrong.
4. **Can it fail or scale independently?** Media processing may need much more CPU and storage than profile reads.
5. **Can one team own it end to end?** A boundary with no clear operational owner will become shared infrastructure by accident.
6. **How chatty would the request path become?** A boundary that turns one local method into twenty synchronous calls is too fine-grained.

These are signals, not a formula. Begin with larger boundaries and split only when evidence justifies the added operational cost.

## Predict before reading

The team creates `PostService`, `LikeService`, and `ProfileService`, but all three update the same `users`, `posts`, and `post_likes` tables. `LikeService` changes a column name without coordinating with the other two. What happens?

<details>
<summary>Reveal the prediction</summary>

The services are independently deployed but not independently changeable. A schema change can break another service, ownership of validation is unclear, and one service can bypass another's rules. The shared database is acting as an undocumented public API.

</details>

## Concrete flow: loading an Instagram feed

Suppose this application eventually has three proven boundaries:

- **Post service** owns post text, author ID, publication state, and like membership/count rules.
- **Media service** owns uploaded objects, scanning status, transformations, and durable media URLs.
- **Profile service** owns display name, bio, and profile visibility.

A feed read could work as follows:

1. A user uploads an image to the media service.
2. The media service returns `mediaId = m-42` after storage and validation.
3. The client asks the post service to publish a post referencing `m-42`.
4. The post service verifies the media is usable, stores the post, and emits `PostPublished`.
5. A feed projection consumes `PostPublished` and stores the fields needed to render a feed card.
6. Profile changes update that projection asynchronously through `ProfileDisplayChanged` events.
7. Feed reads use the projection instead of synchronously joining three private databases.

The projection is intentionally duplicated data. It is acceptable because it is a read copy that can be rebuilt; the owning services remain authoritative.

If fresh profile data is legally or functionally required, the feed can call profile synchronously. That choice buys freshness but adds latency and another runtime dependency. The requirement—not a slogan—decides.

## Instagram-backend mapping

The current repository should remain a modular monolith until independent scaling or ownership provides a real benefit:

- [PostService.java](../../../../../src/main/java/com/instagram/backend/service/PostService.java) currently keeps post creation, ownership checks, likes, and persistence rules in one post capability, with each public operation using its own local transaction. Those rules form a sensible boundary.
- [PostRepository.java](../../../../../src/main/java/com/instagram/backend/repository/PostRepository.java) and [PostLikeRepository.java](../../../../../src/main/java/com/instagram/backend/repository/PostLikeRepository.java) should remain behind that capability rather than being called by a future feed service.
- [MediaStorageService.java](../../../../../src/main/java/com/instagram/backend/service/MediaStorageService.java) has different storage and scaling needs, so media is a plausible later extraction.
- [ProfileService.java](../../../../../src/main/java/com/instagram/backend/service/ProfileService.java) suggests a profile boundary, but the current shared `UserAccount` data means ownership must be clarified before extraction.

The first useful step is enforcing these module boundaries in-process. A network boundary should come later.

## Decision and trade-off

| Choice | Prefer it when | Cost or warning |
|---|---|---|
| Keep a modular monolith | One team owns the product and one deployment scales adequately | Less deployment independence, but much lower operational complexity |
| Extract a coarse service | A capability has a clear owner, different scaling/failure needs, and a stable contract | Network calls, deployment, observability, and data-consistency work |
| Keep an asynchronous read copy | Reads need data from several owners and slight staleness is acceptable | Event handling, replay, deduplication, and visible staleness |
| Call the owner synchronously | The answer must be fresh before proceeding | Caller availability and latency now depend on the callee |
| Share tables temporarily | A short migration requires coexistence | Must have one writer and an explicit removal date; never make this the target |

## Failure drill

**Scenario:** a new feed service reads the post database directly because creating an API seems slow.

1. **Prediction:** feed development appears faster at first.
2. **Failure:** the post team changes how deleted posts are represented. Feed continues showing deleted content because it bypasses the post service's rule.
3. **Observable symptom:** API responses from the post service and feed service disagree for the same post ID.
4. **Immediate recovery:** stop the unsafe feed query, invalidate affected feed entries, and rebuild them from authoritative post data.
5. **Prevention:** expose a contract or event that includes publication state, test that contract, and deny cross-service database credentials.

## Two-to-three-minute interview-ready answer

“I would begin by clarifying what problem the split must solve, such as independent scaling, team ownership, release speed, or security isolation. I would not create one service per Java package or table. Instead, I would map business capabilities, the code that changes together, and the invariants that must remain consistent. I prefer coarse boundaries because overly small services turn one local operation into many slow, failure-prone network calls.

For each boundary, I name one service as the authoritative writer for its data. Other services cannot update that database directly. They call the owner synchronously when they need an immediate decision, or consume versioned events and maintain read projections when temporary staleness is acceptable. Reporting across owners uses projections or an analytics pipeline, not cross-service database joins.

In this Instagram project, posts and like membership belong together initially because one transaction enforces rules such as one like per user and the displayed like count. Media is a stronger extraction candidate because file storage, scanning, transformation, and scaling differ from post metadata. I would first enforce these boundaries inside the modular monolith, add contract tests and observability, and measure actual coupling. Then I would extract one capability behind a stable interface, move its data to one owner, canary traffic, reconcile results, and retain a routing rollback.

A failure I specifically prevent is a feed service reading post tables directly. If the post team later changes deletion representation, the feed can show deleted content with no API error. I would contain that by revoking cross-database access and rebuilding the feed from authoritative events. The trade-off is that clean ownership introduces event delivery, stale reads, reconciliation, and operational overhead. I split only when independent change or scaling is worth those new failure modes.”

## Questions and explained answers

<details>
<summary>1. Why is one service per database table usually a weak boundary?</summary>

A table describes storage, not a complete business responsibility. A rule such as “one user may like a post once” spans behavior, the post, and like records. Splitting each table can turn one local transaction into several network calls and leave no service responsible for the complete invariant.

</details>

<details>
<summary>2. Can another service store a copy of data it does not own?</summary>

Yes. A feed service can store an author's display name for fast reads. It must treat that value as a derived copy, update it from an owner's API or events, tolerate temporary staleness, and be able to rebuild it. It must not accept profile edits as if it were the profile authority.

</details>

<details>
<summary>3. How do you enforce a rule that spans two genuine service owners?</summary>

First ask whether the rule proves the boundary is wrong. If the boundary is still justified, choose one workflow owner, reserve or confirm resources through explicit APIs, and use a Saga with compensating actions where immediate atomicity is impossible. State the temporary states and user-visible consistency rather than pretending a cross-service ACID transaction exists.

</details>

<details>
<summary>4. What evidence says a module is ready to become a service?</summary>

It has a clear business contract and owner, few direct dependencies, explicit data authority, tests around its boundary, observable traffic and failure behavior, and a reason for independent deployment or scaling. Class count alone is not evidence.

</details>
