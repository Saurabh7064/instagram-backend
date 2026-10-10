# Cache Stampede on a Hot Key

## Question

Cache hit rate drops from **95% to 20%**, database CPU spikes, and response latency climbs. A small number of popular keys receive most of the traffic.

Your manager asks:

**“How would you stabilize the system and prevent this cache stampede from recurring?”**

> [One-page revision card](../../revision-cards/production-scenarios/07-cache-stampede-on-hot-key.md)

## Why this problem exists

A cache protects the database only while requests find usable entries. If a popular entry expires, thousands of callers can miss together and independently regenerate the same value. The database receives many copies of work that the cache previously absorbed. Slower reads then hold connections longer, creating more waiting and timeouts.

The design must protect the source of truth, coordinate regeneration per key, and state exactly how stale a fallback may be. “Add a cache” is not a complete reliability policy.

## Analogy: a popular book at a library

If one reference book is removed, every visitor may ask a librarian to fetch another copy at the same time. One librarian fetching it while others briefly wait avoids duplicated work.

The analogy stops because distributed callers can crash while holding coordination, cache nodes can disagree, and stale data may be safe for a public profile but unsafe for authorization.

## Terms to establish

- A **cache stampede** is duplicated source work caused by many concurrent misses for the same data.
- **TTL jitter** adds randomness to expiration times so many entries do not expire together.
- **Request coalescing** allows one caller to refresh a key while other callers wait briefly or use stale data.
- **Stale-while-revalidate** serves an older bounded value while a refresh runs in the background.

## Investigation and stabilization sequence

### 1. Confirm why hit rate collapsed

Break hits, misses, evictions, errors, and load time down by cache node, route, and bounded key class. Check whether the cache is unreachable, memory eviction increased, a deployment changed keys, or many TTLs aligned. Confirm that database traffic is amplified reads rather than an unrelated slow query.

### 2. Protect the database first

Limit concurrent cache-miss loads and shed optional reads before the database loses all capacity. Serve bounded stale values only where product correctness allows it. Keep authorization, ownership, and other security decisions fail-safe rather than stale.

### 3. Coalesce refresh per hot key

On a miss, one requester obtains a short expiring lease and regenerates the value. Other callers wait a small bounded time or receive a valid stale entry. The refresher stores the result atomically and releases ownership; lease expiry allows recovery if it crashes.

### 4. Spread and anticipate expiration

Add jitter to TTLs, refresh known hot keys before hard expiry, and warm only predictable critical data. Monitor regeneration count per key class. Preloading the entire database can itself create the outage.

## Concrete request walkthrough

1. The celebrity feed key `feed:user:9` expires at noon.
2. Two thousand requests arrive in the next second and all miss.
3. Without coordination, two thousand database reads start; pool wait and database CPU rise.
4. With coalescing, one caller receives a five-second refresh lease.
5. Other callers receive a feed version still within its 30-second stale allowance.
6. The winner stores the refreshed feed with a jittered TTL and releases the lease.
7. Hit rate, database load, and p99 return to baseline.

If the value were an authorization decision, the stale path would be rejected; the same availability choice does not apply to every key.

## Instagram-backend mapping

No application cache exists in this repository. [PostService.feed(...)](../../../../../src/main/java/com/instagram/backend/service/PostService.java) reads all posts from PostgreSQL on every request and checks each viewer’s like state. [PostRepository.java](../../../../../src/main/java/com/instagram/backend/repository/PostRepository.java) exposes the direct feed query. Any Redis cache, refresh lease, invalidation event, or stale window described here is **proposed**, not implemented.

A safe first candidate would be a bounded public or global feed page whose freshness requirement is explicit, not an unbounded personalized response containing security decisions. Existing [HTTP and feed observations](../../../../../src/main/resources/application.properties) could be extended with bounded `hit`, `miss`, `stale`, and `load_failure` outcomes. Review the detailed [Caching and Consistency](../../concepts/02-caching-consistency.md) lesson before choosing invalidation behavior.

## Choices and trade-offs

- **Stale-while-revalidate** when slightly old data is acceptable and availability matters. Avoid it for authorization or strict inventory; the cost is visible staleness.
- **Request coalescing** for expensive popular keys. Avoid global locks; per-key coordination adds lease and failure complexity.
- **Longer TTL or warming** for predictable read-heavy data. Avoid warming everything; longer lifetimes increase staleness and invalidation risk.

## Failure drill

**Prediction:** the refresh winner crashes while holding the hot-key lease. **Symptom and detection:** the key stays stale, no successful load completes, and callers wait or use fallback. **Containment:** continue bounded stale serving and prevent uncontrolled source loads. **Recovery:** let the short lease expire so another caller can refresh. **Prevention:** use expiring ownership, atomic conditional release, load deadlines, and a metric for lease age and refresh outcome.

## Two-to-three-minute interview answer

The hit-rate collapse plus database spike suggests requests that were previously absorbed by cache are now reaching the source together. I would split hits, misses, evictions, cache errors, and load duration by cache node, route, and bounded key class. I would check cache reachability, aligned expiry, memory eviction, and key-format changes, while confirming the database increase is amplified reads.

I protect the database first with a miss-load concurrency limit and by shedding optional reads. Where the product permits it, I serve a bounded stale value. Per hot key, request coalescing elects one refresher; other callers wait briefly or use stale data. The winner writes atomically with a randomized TTL, and an expiring lease prevents a crash from blocking the key permanently. I may refresh known hot keys early, but I do not warm every key. Recovery means hit rate and user latency recover while database CPU and loads per miss fall. Finally, I load-test synchronized expiry and complete cache loss.

## Practice status

The artifact is `READY`; comprehension remains `TODO`. Mark it practiced only after explaining why simultaneous misses amplify work, deciding where stale data is allowed, and answering the questions below before revealing them.

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
