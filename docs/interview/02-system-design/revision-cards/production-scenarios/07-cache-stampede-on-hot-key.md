# Cache Stampede on a Hot Key — Interview Revision Card

> **Question:** Cache hit rate drops from 95% to 20%, database CPU spikes, and latency climbs. How do you stabilize the system and prevent recurrence?
>
> [Detailed answer](../../questions/production-scenarios/07-cache-stampede-on-hot-key.md)

## Key requirements

- Protect the database while keeping important reads available.
- Prevent synchronized misses for popular data.
- Bound how stale a fallback may be.

## Scale assumptions

- A single popular key can generate thousands of simultaneous misses when a shared TTL expires.

## Components

- A **cache stampede** is simultaneous regeneration; **TTL jitter** spreads expiry; **request coalescing** elects one refresher.
- Cache, database, refresh worker, and cache-load metrics.

## Request flow

1. Classify cache hit, miss, or acceptable stale value.
2. One miss acquires a per-key refresh lease.
3. Others wait briefly or use bounded stale data.
4. The winner stores refreshed data with jittered TTL.
5. Observe failures and release the lease safely.

## Data model

- `CacheEntry(key, value, freshUntil, staleUntil, version)`

## Three trade-offs

1. Stale-while-revalidate preserves latency but serves older data.
2. Coalescing protects the database but adds coordination and lease failure modes.
3. Longer TTL improves hit rate but increases staleness and invalidation difficulty.

## Three failures and mitigations

1. **Mass expiry:** add TTL jitter and warm known hot keys.
2. **Refresh winner crashes:** use an expiring lease; allow another winner.
3. **Cache outage:** rate-limit database fallback and shed optional reads.

## Two-minute spoken answer

The hit-rate collapse plus database spike suggests synchronized expiry or cache failure. I split misses by key class, route, and cache node, and verify that database load is amplified reads rather than an unrelated query.

I protect the database with a concurrency limit and serve bounded stale data where allowed. Per hot key, request coalescing elects one refresher while others wait or use stale data. It stores atomically with randomized TTL so keys do not expire together. I warm only predictable hot keys; authorization cannot use stale fallback. I verify hit rate, database CPU, regeneration count, and user latency, then test cache loss and synchronized expiry under load.

## Recall questions

1. Why does TTL jitter help?
2. When is stale data unacceptable?
3. How does a failed refresh lease recover?
