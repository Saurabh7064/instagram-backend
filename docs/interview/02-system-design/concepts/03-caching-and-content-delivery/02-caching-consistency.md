# Micro-Lesson 02 — Caching and Consistency

Core study: 15 minutes. Prerequisite: [Scalability, Availability, and Reliability](../09-reliability-resilience-and-fault-tolerance/01-scalability-availability-reliability.md).

## The bio was saved. Why does the profile still show the old one? — Must understand

User 42 changes their bio. PostgreSQL saves the new text and the app reports success. The next profile read shows the old text. The database may be entirely correct: the read can have come from another copy kept to avoid repeating database work.

That faster reusable copy is a **cache**. It is useful when many readers request the same profile and changes are relatively rare. The design question is how long serving its old text remains acceptable after an update.

Think of the profile as a restaurant's entrance menu board. It avoids a trip to the kitchen for every price, but someone must change the board when the authoritative list changes. The analogy stops at concurrent reads and writes: software needs explicit ordering and failure handling rather than trusting someone to notice the board is wrong.

## Follow one profile read to the faster copy

Use the key `profile:user:42`. If its stored value is present and valid under the cache policy, this is a **cache hit** and the application can return it. If it is absent, this is a **cache miss**: read PostgreSQL, save the result with an expiry, then return it. This application-managed lookup is called **cache-aside**.

```text
Client → Profile service → Cache
                         ├─ hit  → return cached profile
                         └─ miss → Database → store in cache → return
```

PostgreSQL remains the **source of truth**, the authoritative place for the saved profile. Losing the cache should remove the faster copy rather than the only copy. It can still overload the database if all those previously cheap reads suddenly become misses, so correctness of storage and safety of fallback are separate concerns.

## Follow the bio update back to the old copy

The cache contains **stale data** when it still holds a bio older than the authoritative saved bio. If we update only PostgreSQL and leave the cache alone, another hit can return that old text. What should happen to the copy?

A straightforward policy is to commit the database write, then delete the cached entry. The next miss reloads the bio. That deletion is **invalidation**.

```text
Update DB → delete cache entry → next read reloads it
```

There is a race worth understanding. A reader may fetch the old bio from PostgreSQL before the update commits. The writer commits and deletes the cache entry. The earlier reader then stores its already fetched old bio, repopulating the entry after the deletion. “Write, then invalidate” is useful, but those steps alone do not prove that every concurrent read sees the new bio.

Another policy routes writes through a cache abstraction that updates both places. This is **write-through**. Keeping entries warm can help reads, but the abstraction must define what happens if one write succeeds and the other fails; giving the operation a name does not make two remote writes atomic.

```text
Update cache abstraction → cache abstraction updates DB and cache
```

## Decide what the five-minute expiry actually promises

A **time-to-live**, or **TTL**, limits how long an entry remains usable after it is populated under the expiry policy. A five-minute entry can therefore keep returning the old bio after a successful update unless another mechanism changes or invalidates it.

That is not automatically a bound of five minutes *since the database update*. A delayed refill can populate an old value later, as in the race above. State the freshness requirement first, then choose expiry, invalidation, and version handling that can enforce it.

A short stale public bio may be acceptable; stale authorization can expose data after access was revoked. Keep the policy attached to the operation rather than deciding that every value is safe to cache. Stronger freshness costs coordination, additional checks, or an unavailable read when that freshness cannot be established.

## Project mapping

[ProfileService.java](../../../../../src/main/java/com/instagram/backend/service/ProfileService.java) reads profile data that could eventually use cache-aside for popular public profiles. [PostService.java](../../../../../src/main/java/com/instagram/backend/service/PostService.java) builds feed responses where caching could reduce repeated reads.

Do not cache merely because these methods exist. First measure query load and define freshness. A viewer-specific feed response includes like state, so a shared cache key must include the viewer or separate shared post data from viewer-specific state.

## Which data can tolerate the same freshness policy?

| Data | Cache? | Freshness choice |
|---|---|---|
| Public profile bio | Often yes | Short TTL plus invalidation after update |
| Product description | Yes | Event or invalidation on catalog change |
| Search results | Often yes | Short TTL; tolerate indexing delay |
| Payment result | Do not treat cache as authority | Durable transaction record and idempotent lookup |
| Authorization/consent | Only with strict invalidation | Short lifetime and fail-safe behavior |
| Available inventory | Carefully | Reservation service remains authoritative |

## Ten thousand readers miss the same profile — paper exercise

The popular profile entry expires and ten thousand requests miss together. Predict how many database reads an unprotected cache-aside implementation could issue.

Each reader can independently fetch the same value, creating a **cache stampede**. A simultaneous rise in misses, database query time, CPU, and connection use reveals it. Bound refresh concurrency, shed excess work, or serve a prior value only where the freshness contract permits it.

One refresh serving the waiting callers is **request coalescing**, also called **single-flight**. Refreshing before expiry and spreading expiry times across keys can reduce sudden refresh demand, but callers still need bounded waiting and the database still needs protection if the cache is lost. Verify that the profile reloads and database load returns to safe levels; a restored cache is not evidence that every stale value met its contract.

## Explain the old bio without calling it a lost write

Explain caching as “a faster copy,” trace a hit and miss, then explain how a profile update can produce a stale read and how you would bound it.

## Before moving on

Artifact: `READY`; comprehension not yet demonstrated.

Proceed only when you can:

- trace hit, miss, update, and expiration paths;
- explain why the database remains authoritative;
- choose a freshness policy for profile, catalog, and payment data;
- diagnose and mitigate a cache stampede.

## Questions and explained answers

<details>
<summary>1. Why does adding a cache create a consistency problem?</summary>

There are now at least two copies: the source of truth and the cached value. A write can update one before the other, so the system must define invalidation, update ordering, and acceptable staleness.

</details>

<details>
<summary>2. What is the difference between cache-aside and write-through?</summary>

With cache-aside, application code reads the cache and loads misses from the database; writes usually update the database and invalidate the cache. With write-through, writes go through a caching layer that updates both. Cache-aside is simpler and failure-tolerant for many read-heavy workloads; write-through keeps entries warm but complicates the write path.

</details>

<details>
<summary>3. Why is a TTL not a complete invalidation strategy?</summary>

It limits an entry's usable lifetime after population under its expiry policy, rather than automatically bounding staleness from a later database write. Concurrent delayed refills can repopulate an old value after invalidation. Immediate visibility requires a stronger protocol, and simultaneous expiration can create a stampede.

</details>

<details>
<summary>4. Would you cache checkout inventory?</summary>

A cache can support browsing, but checkout must confirm or reserve against an authoritative inventory system. Otherwise two buyers can act on the same stale count. The design needs reservation, idempotency, and expiration—not only caching.

</details>
