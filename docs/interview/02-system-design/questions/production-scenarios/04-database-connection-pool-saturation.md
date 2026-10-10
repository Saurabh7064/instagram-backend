# Database Connection-Pool Saturation

## Question

Application CPU is around **40%**, but requests are timing out and the database connection pool is fully utilized.

Your manager asks:

**“What is happening, what would you check first, and should we increase the pool size?”**

> [One-page revision card](../../revision-cards/production-scenarios/04-database-connection-pool-saturation.md)

## Why this problem exists

A service normally reuses a bounded set of database connections because opening one for every request is expensive and an unbounded number can overwhelm the database. When every connection is borrowed, new database work waits. Longer queries or transactions reduce how quickly connections return; waiting requests remain active; timeouts and retries add pressure. CPU can remain low because threads are blocked rather than computing.

Increasing the pool treats the queue location, not necessarily the cause. It helps only when the database has headroom and the existing limit is artificially low.

## Analogy: a parking lot

The pool is a parking lot with a fixed number of spaces. If cars stay longer, arriving cars line up even when the nearby road is empty. Painting more spaces helps only if the destination can serve more visitors; otherwise the lobby becomes the new queue.

The analogy stops because a database connection can hold transactions and locks that slow other connections. Adding “spaces” can therefore reduce total throughput.

## Terms to establish

- A **connection pool** is a reusable, bounded collection of open database sessions.
- **Acquisition time** is how long application work waits to borrow a connection.
- A **transaction** groups database operations into one atomic unit and may retain a connection and locks until completion.
- **Lock contention** occurs when one transaction waits for another to release protected data.

A useful capacity relationship is `concurrent database work ≈ arrival rate × connection-hold time`. At 100 database-using requests per second and 100 ms hold time, about 10 connections are active on average. If hold time becomes one second, roughly 100 are needed for the same arrival rate—usually a signal to reduce hold time, not allocate blindly.

## What I would check first

### 1. Prove that callers are waiting for connections

Inspect active, idle, pending, acquisition-time, usage-duration, and timeout metrics for the pool. Align their change with HTTP latency. A fully utilized pool with no pending borrowers may simply be well used; saturation means demand waits or times out.

### 2. Inspect the database before changing the pool

Check database CPU, memory, I/O, maximum sessions, active queries, lock waits, and slow-query plans. If PostgreSQL is already saturated, more connections add context switching, memory, and concurrent I/O. If many sessions are idle inside transactions, find why transactions remain open.

### 3. Find why connections are held longer

Compare fast and slow traces, query counts per request, transaction duration, result sizes, and recent changes. Look for missing indexes, N+1 queries, network delay, lock contention, long external calls inside a transaction, and leaked connections. Separate a traffic-volume increase from a service-time increase.

### 4. Stabilize without creating a retry storm

Bound database-using concurrency, reject or degrade low-priority reads, stop expensive batch work, and use a finite acquisition timeout. Do not let every layer retry: retries create more borrowers while the pool is already congested.

## Concrete request walkthrough

Suppose the pool contains ten connections and peak traffic sends 80 feed requests per second.

1. `GET /api/feed` starts a read-only transaction.
2. Authentication loads the viewer, then the repository loads every post.
3. Mapping checks like state for each post, increasing query count as the feed grows.
4. Each transaction now holds database resources for 500 ms instead of 50 ms.
5. Ten requests occupy all connections; later requests wait for acquisition.
6. HTTP duration rises although application CPU stays near 40%; some callers time out and retry.
7. Pending acquisition and query count rise before database CPU necessarily reaches 100%.

The remedy might be pagination plus a set-based like lookup, not a larger pool. Query metrics must confirm this particular mechanism.

## Instagram-backend mapping

The real datasource points to PostgreSQL in [application.properties](../../../../../src/main/resources/application.properties), but the project does not explicitly configure HikariCP pool size or acquisition timeout, so framework defaults currently apply. Database readiness is included, but readiness only proves a check can reach the database; it does not prove spare pool capacity.

[PostService.feed(...)](../../../../../src/main/java/com/instagram/backend/service/PostService.java) is a read-only transaction that authenticates the viewer, loads every post, and maps the response. Its response mapper invokes `existsByPostAndUser(...)` per post. [PostRepository.java](../../../../../src/main/java/com/instagram/backend/repository/PostRepository.java) exposes an unpaged feed query. These are concrete places to measure connection hold time and query count.

[RequestCorrelationFilter.java](../../../../../src/main/java/com/instagram/backend/observability/RequestCorrelationFilter.java) supplies request duration and correlation, and [application.properties](../../../../../src/main/resources/application.properties) enables HTTP histograms. **Proposed, not implemented:** HikariCP wait/usage dashboards and alerts, PostgreSQL lock and slow-query monitoring, explicit pool sizing from measured capacity, paged feed access, and a set-based like-state query.

## Should we increase the pool?

Increase it cautiously only if acquisition wait is the bottleneck, database CPU/I/O/session capacity has headroom, queries and transactions are healthy, and a load test shows higher end-to-end throughput without worse database latency. Change gradually and consider total connections across every application instance.

Do not increase it when queries are slow, locks are long, connections leak, or the database is already constrained. A pool of 30 across 20 pods is potentially 600 sessions; per-instance settings must be evaluated at fleet scale.

## Choices and trade-offs

- **Optimize queries and shorten transactions** for a durable reduction in hold time. It requires diagnosis and may need indexes or API changes, but it improves capacity without multiplying sessions.
- **Increase the pool** when the database has verified headroom. It reduces application waiting but consumes database memory and can worsen lock or I/O contention.
- **Admission control** bounds database concurrency and preserves critical paths. It intentionally rejects or degrades excess work, so product behavior and retry guidance must be explicit.

## Failure drill

**Prediction:** doubling every pod’s pool overloads PostgreSQL and makes latency worse. **Symptom and detection:** acquisition wait briefly falls, then database CPU/I/O, active sessions, lock waits, and p99 rise while throughput stops improving. **Containment:** restore the previous limit, stop retries and batch work, and cap incoming database concurrency. **Recovery:** let sessions and queues drain, then verify throughput and tail latency return. **Prevention:** size the pool at fleet level with load tests, alert on wait and hold duration, and optimize slow transaction paths first.

## Two-to-three-minute interview answer

The application is likely waiting rather than computing. I would first verify pool saturation using active, idle, pending, acquisition-time, usage-duration, and timeout metrics and align them with HTTP latency. Then I would inspect the database: CPU, I/O, session limits, active and slow queries, lock waits, and query plans. I would compare fast and slow traces and measure transaction duration and queries per request to distinguish more traffic from longer connection hold time.

I would not automatically increase the pool. It is appropriate only when callers are waiting, PostgreSQL has measured headroom, transactions are healthy, and a load test proves that more connections increase throughput. Otherwise it merely moves the queue into the database and can worsen contention. During impact I would bound database concurrency, stop expensive optional work, and prevent retries from multiplying load. For this project, I would measure the feed transaction because it loads every post and checks like state per item. The durable fix may be pagination and a set-based query, followed by fleet-level pool sizing and alerts on acquisition wait and connection hold time.

## Practice status

This file being complete does not demonstrate understanding. Mark the scenario practiced only after explaining pool saturation without notes, predicting what a harmful pool increase would do, and answering the final questions before opening them.

## Questions and explained answers

<details>
<summary>Why can CPU stay at 40% while requests time out?</summary>

Threads can spend most of their time blocked while acquiring a connection or waiting for SQL or locks. Waiting consumes latency and concurrency but little application CPU.
</details>

<details>
<summary>What evidence would justify a cautious pool-size increase?</summary>

There should be real acquisition wait, healthy query and transaction durations, spare database CPU/I/O/session capacity, and a load test showing that the change improves throughput and tail latency at total fleet connection count.
</details>

<details>
<summary>Why must pool size be calculated across all application instances?</summary>

Each instance owns its own pool. A seemingly small per-pod increase is multiplied by the number of pods and can exceed the database’s safe session capacity during normal scaling or a rollout with old and new pods together.
</details>
