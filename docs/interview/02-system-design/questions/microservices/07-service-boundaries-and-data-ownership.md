# 07 — Choosing Service Boundaries and Data Ownership

> **Interview question:** How would you identify good microservice boundaries in a monolith, and how should data ownership work after the split?

> [One-page revision card](../../revision-cards/microservices/07-service-boundaries-and-data-ownership.md)

## What should a service be allowed to change?

Suppose you split this backend into a Post Service, a Like Service, and a Profile Service. Each runs independently, so the deployment diagram looks promising. Then someone changes how a deleted post is stored. All three services read the same tables, and all three now need a coordinated release. The split gave you more applications to operate without making the business change easier.

That is the problem a useful boundary must solve. A service should own a recognizable decision and the data needed to make it. Other services should ask it for an outcome rather than reproducing its private rules. If they still reach directly into its tables, its database schema has become an undocumented public interface.

When independently deployed applications remain tightly dependent on each other's internals, we call the result a **distributed monolith**. The name describes the cost: the coupling remains, and calls can now fail across a network as well.

## A department owns a decision, not just a filing cabinet

A store may have a returns desk, warehouse, and accounting department. Each owns a clear decision:

- the returns desk decides whether a return is allowed;
- the warehouse decides how much stock is available;
- accounting records refunds.

Other departments request an outcome rather than reaching into a department's files and editing them.

The analogy has limits. Software can copy data into read models, messages may arrive late or twice, and a business operation may legitimately span several owners. The analogy explains authority, not distributed consistency.

## Keep the rule with the data that protects it

Think about a like. The user is asking, “Make my like on this post exist.” The backend must check that the post is usable, ensure this user cannot add two likes, and report the resulting state. Those responsibilities make sense together even though the database stores posts and likes in different tables.

A **business capability** is a recognizable outcome such as publishing a post or storing media. It gives us a starting point for grouping behavior. A table is only one way to store the facts that behavior needs; it does not automatically deserve its own service.

Within the capability, identify the rules that must remain true. We call these rules **invariants**. Here, one invariant is “a user has at most one like on a given post.” Keeping the membership record, uniqueness constraint, and operation together lets one local transaction enforce that rule. Splitting them means we must somehow preserve the same rule while services communicate and fail independently.

Then name the **data owner**: the service allowed to make authoritative changes. A feed may store a copy of the author's display name, but a profile edit goes to the Profile Service. Otherwise a user could have two competing display names with no clear answer about which one is correct. Ownership allows read copies; it tells us which copy wins when they disagree.

## How would we decide where to split this project?

Start with a change the team actually wants to make. If changing post publication rules always changes post, like, and feed code together, inspect why. Some code may belong in one capability; other code may only need a stable published fact such as “post 42 is no longer visible.” Reading change history and tracing one request reveal more than counting classes.

Next, check whether keeping the rule inside the proposed service is practical. Can one team explain, test, deploy, and operate that behavior? Can a normal operation complete without a long chain of synchronous calls? If every feed item requires a separate profile request, the boundary adds latency in proportion to the page size and makes profile availability part of every feed read.

Finally, ask what independence would buy. Media processing might need substantially more CPU and storage than profile reads. Separate deployment may help there. If everything scales adequately in one application and one team releases it safely, explicit internal modules may deliver most of the benefit without network operations.

These questions do not produce one mathematically correct diagram. They help us choose a boundary whose useful independence is worth the new communication and recovery work. Begin with larger responsibilities; split further when evidence shows a reason.

## Predict before reading

The team creates `PostService`, `LikeService`, and `ProfileService`, but all three update the same `users`, `posts`, and `post_likes` tables. `LikeService` changes a column name without coordinating with the other two. What happens?

<details>
<summary>Reveal the prediction</summary>

The services are independently deployed but not independently changeable. A schema change can break another service, ownership of validation is unclear, and one service can bypass another's rules. The shared database is acting as an undocumented public API.

</details>

## Follow one post from upload to feed

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

## Which kind of independence do we need?

| Choice | Prefer it when | Cost or warning |
|---|---|---|
| Keep a modular monolith | One team owns the product and one deployment scales adequately | Less deployment independence, but much lower operational complexity |
| Extract a coarse service | A capability has a clear owner, different scaling/failure needs, and a stable contract | Network calls, deployment, observability, and data-consistency work |
| Keep an asynchronous read copy | Reads need data from several owners and slight staleness is acceptable | Event handling, replay, deduplication, and visible staleness |
| Call the owner synchronously | The answer must be fresh before proceeding | Caller availability and latency now depend on the callee |
| Share tables temporarily | A short migration requires coexistence | Must have one writer and an explicit removal date; never make this the target |

## Failure drill

**Scenario:** a new feed service reads the post database directly because creating an API seems slow.

1. **Prediction:** the direct query works until the post owner changes a private storage rule; feed can then show content the authoritative API would hide.
2. **Failure:** the post team changes how deleted posts are represented. Feed continues showing deleted content because it bypasses the post service's rule.
3. **Observable symptom:** API responses from the post service and feed service disagree for the same post ID.
4. **Immediate recovery:** stop the unsafe feed query, invalidate affected feed entries, and rebuild them from authoritative post data.
5. **Prevention:** expose a contract or event that includes publication state, test that contract, and deny cross-service database credentials.

## Two-to-three-minute interview-ready answer

“I would begin by clarifying what problem the split must solve, such as independent scaling, team ownership, release speed, or security isolation. I would not create one service per Java package or table. Instead, I would map business capabilities, the code that changes together, and the invariants that must remain consistent. I prefer coarse boundaries because overly small services turn one local operation into many slow, failure-prone network calls.

For each boundary, I name one service as the authoritative writer for its data. Other services cannot update that database directly. They call the owner synchronously when they need an immediate decision, or consume versioned events and maintain read projections when temporary staleness is acceptable. Reporting across owners uses projections or an analytics pipeline, not cross-service database joins.

In this Instagram project, posts and like membership belong together initially because one transaction enforces rules such as one like per user and the displayed like count. Media is a stronger extraction candidate because file storage, scanning, transformation, and scaling differ from post metadata. I would first enforce these boundaries inside the modular monolith, add contract tests and observability, and measure actual coupling. Then I would extract one capability behind a stable interface, move its data to one owner, canary traffic, reconcile results, and retain a routing rollback.

A failure I specifically prevent is a feed service reading post tables directly. If the post team later changes deletion representation, the feed can show deleted content with no API error. I would contain that by revoking cross-database access and rebuilding the feed from authoritative events. The trade-off is that clean ownership introduces event delivery, stale reads, reconciliation, and operational overhead. I split only when independent change or scaling is worth those new failure modes.”

Before marking this answer practiced, explain the proposed boundary without notes, predict the deletion failure, and answer the questions below before opening them. The documentation being complete does not demonstrate understanding.

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

First ask whether the rule shows that the boundary is wrong. If the boundary is still justified, choose one workflow owner and give it explicit steps and intermediate states. A Saga is such a workflow of local transactions; later business actions, called compensations, repair earlier successful steps after failure. It does not make several databases one atomic transaction. The [distributed-transactions lesson](./04-distributed-transactions-and-data-consistency.md) develops that choice and its limits.

</details>

<details>
<summary>4. What evidence says a module is ready to become a service?</summary>

It has a clear business contract and owner, few direct dependencies, explicit data authority, tests around its boundary, observable traffic and failure behavior, and a reason for independent deployment or scaling. Class count alone is not evidence.

</details>
