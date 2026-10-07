# Micro-Lesson 02 — Caching and Consistency

- Status: `READY`; comprehension not yet demonstrated
- Time: 15 minutes
- Primary idea: caching speeds reads by creating another copy, which creates a freshness decision
- New terms: cache hit, cache miss, stale data
- Prerequisite: [Scalability, Availability, and Reliability](./01-scalability-availability-reliability.md)

## Why does this exist?

Popular data may be read thousands of times but updated rarely. Repeating the same database work wastes capacity and increases latency. A cache keeps a faster copy closer to the application or user.

The difficulty is not putting data into a cache. The difficulty is deciding when that copy is no longer correct enough to serve.

## Analogy: a restaurant menu board

The kitchen owns the current price list. A menu board near the entrance is a fast copy customers can read without asking the kitchen.

When the kitchen changes a price, the board can temporarily show the old price. You must choose whether to update the board immediately, tolerate a short delay, or force every customer to ask the kitchen.

The analogy stops at concurrent writers, distributed eviction, network partitions, and caches that disappear on restart.

## Plain-language model

- **Cache hit:** the requested value exists in the cache and can be served.
- **Cache miss:** it is absent, so the system asks the source of truth and may store the result.
- **Stale data:** the cache contains an older value than the source of truth.

## Predict before reading

A profile is cached for five minutes. The user changes their bio, the database update succeeds, but the cache is not changed. What will the next profile read return?

<details>
<summary>Reveal the prediction</summary>

It can return the old bio until the entry expires or is invalidated. The database is correct, but the faster copy is stale.

</details>

## Cache-aside read flow

```text
Client → Profile service → Cache
                         ├─ hit  → return cached profile
                         └─ miss → Database → store in cache → return
```

1. Build a cache key such as `profile:user:42`.
2. Read from the cache.
3. On a hit, return the cached value.
4. On a miss, read PostgreSQL.
5. Store the result with an expiration time.
6. Return the result.

The database remains the source of truth. Cache loss should make the system slower, not destroy the only copy of the data.

## Update choices

### Invalidate after the database write

```text
Update DB → delete cache entry → next read reloads it
```

Simple and common. A small race can still serve stale data if a concurrent read repopulates an old value at the wrong moment.

### Write through

```text
Update cache abstraction → cache abstraction updates DB and cache
```

Keeps the cache populated but adds write-path latency and couples correctness to the cache layer's behavior.

### Accept bounded staleness

Use a time-to-live (TTL) and tolerate old data briefly. Appropriate for product descriptions or public profile metadata; dangerous for payment state, authorization, or available inventory during checkout.

## Project mapping

[ProfileService.java](../../../../src/main/java/com/instagram/backend/service/ProfileService.java) reads profile data that could eventually use cache-aside for popular public profiles. [PostService.java](../../../../src/main/java/com/instagram/backend/service/PostService.java) builds feed responses where caching could reduce repeated reads.

Do not cache merely because these methods exist. First measure query load and define freshness. A viewer-specific feed response includes like state, so a shared cache key must include the viewer or separate shared post data from viewer-specific state.

## Decision table

| Data | Cache? | Freshness choice |
|---|---|---|
| Public profile bio | Often yes | Short TTL plus invalidation after update |
| Product description | Yes | Event or invalidation on catalog change |
| Search results | Often yes | Short TTL; tolerate indexing delay |
| Payment result | Do not treat cache as authority | Durable transaction record and idempotent lookup |
| Authorization/consent | Only with strict invalidation | Short lifetime and fail-safe behavior |
| Available inventory | Carefully | Reservation service remains authoritative |

## Failure drill: cache stampede

A popular key expires. Ten thousand requests miss at nearly the same time and all query the database.

1. **Prediction:** database traffic spikes instead of being protected by the cache.
2. **Observable symptom:** miss rate, query latency, database CPU, and connection use rise together.
3. **Recovery:** shed excess load, serve a stale value if safe, and allow only limited refresh work.
4. **Mitigation:** request coalescing/single-flight, randomized TTLs, refresh-ahead, and bounded concurrency.

## Interview questions

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

It bounds how long an entry can remain stale but does not make updates immediately visible. The acceptable TTL depends on business correctness, and mass expiration can create a stampede.

</details>

<details>
<summary>4. Would you cache checkout inventory?</summary>

A cache can support browsing, but checkout must confirm or reserve against an authoritative inventory system. Otherwise two buyers can act on the same stale count. The design needs reservation, idempotency, and expiration—not only caching.

</details>

## Teach-back

Explain caching as “a faster copy,” trace a hit and miss, then explain how a profile update can produce a stale read and how you would bound it.

## Stop/go

Proceed only when you can:

- trace hit, miss, update, and expiration paths;
- explain why the database remains authoritative;
- choose a freshness policy for profile, catalog, and payment data;
- diagnose and mitigate a cache stampede.
