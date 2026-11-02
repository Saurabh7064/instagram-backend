# Cache Stampede on a Hot Key

## Question

Cache hit rate drops from **95% to 20%**, database CPU spikes, and response latency climbs. A small number of popular keys receive most of the traffic.

Your manager asks:

**“How would you stabilize the system and prevent this cache stampede from recurring?”**

> [One-page revision card](../../revision-cards/production-scenarios/07-cache-stampede-on-hot-key.md)

## One expired value can create thousands of database reads

At noon, a popular feed cache entry expires. Two thousand requests arrive in the next second. Each sees the value is missing and decides to rebuild it from the database. The cache previously answered those requests cheaply; now the database receives two thousand copies of nearly the same work.

That duplicated regeneration is a **cache stampede**. Slower source reads then keep connections occupied, so even requests for unrelated data may begin waiting. A cache that improves ordinary latency can therefore expose the source to a sudden overload when a popular value disappears.

We need a policy for the missing value: who may rebuild it, what everyone else receives while waiting, and what happens if the rebuilder crashes. We also need an explicit freshness rule before promising a stale answer.

## One librarian can fetch the missing reference book

If one reference book is removed, every visitor may ask a librarian to fetch another copy at the same time. One librarian fetching it while others briefly wait avoids duplicated work.

The analogy stops because distributed callers can crash while holding coordination, cache nodes can disagree, and stale data may be safe for a public profile but unsafe for authorization.

## Let one caller refresh while the others receive an honest answer

**Request coalescing** groups concurrent work for the same key so one refresh normally serves the waiting callers. Coordination must be per key; a global lock could make one hot key delay unrelated data.

**Stale-while-revalidate** keeps an older value usable for a bounded period while a background refresh runs. In this example, the product permits the feed entry to remain usable for up to 30 seconds after its normal expiry. The cache must retain that stale copy; deleting it at hard expiry leaves nothing to serve.

**Time to live (TTL)** is how long a cache value remains fresh. **TTL jitter** varies expiry times so many different keys do not expire together. Jitter helps a population of keys; it does not prevent simultaneous misses on one extremely popular key. That key still needs controlled refresh behavior.

## Protect the source before trying to refill everything

First ask why the miss rate changed. The cache may be unavailable, evicting entries for memory, receiving new-format keys after a release, or expiring many values together. Compare cache errors, evictions, misses, and load duration with source-read traffic. A low hit rate and high database CPU can coincide without proving the same cause.

While investigating, bound concurrent source loads and shed optional reads. If the product permits the retained stale feed, use it within its stated window. Authorization, ownership, and strict reservation decisions need their own correctness policy; the feed's availability trade-off cannot be copied onto them.

For a hot key, let one caller obtain a short expiring refresh lease, a temporary right to refresh it. Others wait only within their deadline or receive an eligible stale value. A lease needs an expiry because the holder can crash. It also needs ownership-safe release and a version check or fencing mechanism so an old, delayed holder cannot overwrite a newer refresh after its lease expires.

Once stable, spread ordinary expirations with jitter and refresh a small known hot set early. Loading every cache entry at once may reproduce the same source overload we are trying to remove.

## Follow the noon expiry for feed:user:9

Keep the fixed input: `feed:user:9`, 2,000 incoming requests in one second, a retained stale feed usable for 30 seconds after normal expiry, and a five-second refresh lease.

1. At noon the value is no longer fresh, but its stale copy remains inside the 30-second allowance.
2. Without coordination, each of the 2,000 requests could initiate a source read.
3. With coalescing, one caller wins the five-second lease and starts the refresh; the others receive the permitted stale copy instead of independently reading the database.
4. The winner completes before its lease expires, conditionally stores the fresh version, and releases only the lease it still owns.
5. The new value receives a jittered TTL. Subsequent requests use it, and source reads for this refresh were reduced from roughly 2,000 to one under these assumptions.

This is not an unconditional exactly-one guarantee. Lease expiry, a paused process, or failed coordination can briefly allow overlapping refreshes. Source-load concurrency limits still protect the database, and version checks prevent an older result replacing a newer one.

## Instagram-backend mapping

No application cache exists in this repository. [PostService.feed(...)](../../../../../src/main/java/com/instagram/backend/service/PostService.java) reads all posts from PostgreSQL on every request and checks each viewer’s like state. [PostRepository.java](../../../../../src/main/java/com/instagram/backend/repository/PostRepository.java) exposes the direct feed query. Any Redis cache, refresh lease, invalidation event, or stale window described here is **proposed**, not implemented.

A safe first candidate would be a bounded public or global feed page whose freshness requirement is explicit, not an unbounded personalized response containing security decisions. Existing [HTTP and feed observations](../../../../../src/main/resources/application.properties) could be extended with bounded `hit`, `miss`, `stale`, and `load_failure` outcomes. Review the detailed [Caching and Consistency](../../concepts/03-caching-and-content-delivery/02-caching-consistency.md) lesson before choosing invalidation behavior.

## How much staleness and coordination are justified?

- **Stale-while-revalidate** when slightly old data is acceptable and availability matters. Avoid it for authorization or strict inventory; the cost is visible staleness.
- **Request coalescing** for expensive popular keys. Avoid global locks; per-key coordination adds lease and failure complexity.
- **Longer TTL or warming** for predictable read-heavy data. Avoid warming everything; longer lifetimes increase staleness and invalidation risk.

## The refresh winner dies. Who gets to try next?

**Prediction:** the winner crashes while holding its five-second lease. No fresh value arrives, but other callers must not start two thousand independent loads.

**Detect:** refresh completion is absent while stale serving and lease age rise. Check whether the retained value remains within its 30-second allowance.

**Contain:** keep source loads bounded. Serve stale only inside that allowance; once it expires, return an explicit unavailable result or another approved fallback rather than silently extending it.

**Recover:** after the lease expires, a new holder refreshes. If the original holder later resumes, its expired ownership must not let it delete the new lease or overwrite a newer value.

**Prevent:** use expiring ownership, conditional release, refresh deadlines, version/fencing checks, and tests for crash, pause, and lease expiry. Track source-load attempts and refresh outcome as well as hit rate.

## Two-to-three-minute interview answer

The hit-rate collapse plus database spike suggests requests that were previously absorbed by cache are now reaching the source together. I would split hits, misses, evictions, cache errors, and load duration by cache node, route, and bounded key class. I would check cache reachability, aligned expiry, memory eviction, and key-format changes, while confirming the database increase is amplified reads.

I protect the database first with a miss-load concurrency limit and by shedding optional reads. Where the product permits it, I serve a bounded stale value. Per hot key, request coalescing elects one refresher; other callers wait briefly or use stale data. The winner writes atomically with a randomized TTL, and an expiring lease prevents a crash from blocking the key permanently. I may refresh known hot keys early, but I do not warm every key. Recovery means hit rate and user latency recover while database CPU and loads per miss fall. Finally, I load-test synchronized expiry and complete cache loss.

## Before you practise

The written scenario is ready for practice; comprehension remains separate. Explain the noon expiry and the crashed refresher, decide where stale data is allowed, and try the questions below before revealing their answers.

## Questions and explained answers

<details>
<summary>Why does TTL jitter reduce stampede risk?</summary>

It spreads expirations over time, so a large population of entries is less likely to regenerate in the same second. It does not protect one extremely hot key by itself; that key still needs coalescing or early refresh.

</details>

<details>
<summary>When should stale-while-revalidate not be used?</summary>

Avoid it when stale data can violate security or strict correctness, such as authorization, account balance, or inventory reservation. Those paths need a safe failure or a separately designed consistency rule.

</details>

<details>
<summary>Why is warming every cache entry dangerous?</summary>

Bulk warming can create the same database surge as a stampede, waste capacity on cold keys, and evict useful data. Warm a small predictable hot set at a controlled rate.

</details>
