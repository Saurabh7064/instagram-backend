# Database Connection-Pool Saturation

## Question

Application CPU is around **40%**, but requests are timing out and the database connection pool is fully utilized.

Your manager asks:

**“What is happening, what would you check first, and should we increase the pool size?”**

> [One-page revision card](../../revision-cards/production-scenarios/04-database-connection-pool-saturation.md)

## The Java process can be idle while all its connections are busy

A request needs PostgreSQL, but every reusable connection is already borrowed. The request waits. Waiting does little computation, so application CPU can remain around 40% while the user sees a timeout.

A **connection pool** is a bounded collection of open database sessions reused across requests. The limit avoids the cost of opening one per request and protects the database from unlimited concurrent work. When borrowed connections return more slowly, new work has fewer chances to start.

The question is why they are returning slowly. More demand, longer transactions, a blocked query, or a leak can all fill the same pool. Increasing the limit before finding which condition applies can simply move the queue into PostgreSQL.

## More parking spaces cannot make the destination serve faster

The pool is a parking lot with a fixed number of spaces. If cars stay longer, arriving cars line up even when the nearby road is empty. Painting more spaces helps only if the destination can serve more visitors; otherwise the lobby becomes the new queue.

The analogy stops because a database connection can hold transactions and locks that slow other connections. Adding “spaces” can therefore reduce total throughput.

## Count how long each request occupies its place

**Acquisition time** is the wait to borrow a connection. **Connection-hold time** is how long it stays borrowed. A **transaction** groups database operations into one atomic unit and may retain the connection and locks until it ends. **Lock contention** means one transaction waits for another's protected data.

A useful average relationship is `active connections ≈ database-using requests per second × average hold time`. At 80 feed requests per second and 50 ms, or 0.05 seconds, hold time, the average need is `80 × 0.05 = 4` connections. If hold time grows to 500 ms, or 0.5 seconds, it becomes `80 × 0.5 = 40`.

A ten-connection pool cannot maintain that second workload without waiting. Equivalently, if each request holds one connection for 0.5 seconds, ten connections can sustain at most roughly `10 ÷ 0.5 = 20` such requests per second under idealized steady conditions. Eighty arrivals exceed that rate. Real bursts and multiple database operations complicate the model, but the direction is clear: hold time matters as much as pool size.

## Follow the borrowed connection before changing the limit

First verify that work is waiting: compare active and idle connections with pending borrowers, acquisition time, usage duration, and pool timeouts. Ten active connections with no waiting can simply mean a well-used pool. Ten active connections and a rising queue show a capacity problem.

Then inspect PostgreSQL. Are sessions executing useful work, waiting on locks, or idle inside open transactions? Compare query duration, plans, CPU, memory, and disk activity. If the database is already overloaded, additional sessions compete for the same constrained resources and may reduce total throughput.

Finally, follow the slow request and its transaction. Did it issue more queries, fetch a much larger result, lose an index, hold a connection during a remote call, or fail to return a connection? Compare recent changes and fast requests on the same route. Distinguish a rise in request count from a rise in time occupied per request.

While users are affected, stop expensive optional database work, cap database-using concurrency, and keep acquisition waiting finite. Stop redundant retries; they are new borrowers arriving at the moment the queue is least able to handle them.

## Apply the calculation to this feed path

Use the same hypothetical load: a ten-connection pool and 80 feed requests each second. Normal hold time is 50 ms; the larger feed result pushes it to 500 ms.

1. `GET /api/feed` starts the read-only service transaction and loads the viewer.
2. The repository retrieves every post. Response mapping then checks the viewer's like state for each returned post.
3. If that work causes the measured 500 ms hold time, the average concurrency need is 40 connections, compared with the ten available.
4. All ten are borrowed and later arrivals wait. Some time out and retry, adding more borrowers.
5. HTTP time rises while CPU can stay near 40%, because much of that elapsed time is blocked waiting.

Pagination and a set-based like lookup might shorten the hold time. They are candidates to test, not a diagnosis established by reading the code. Query counts, transaction duration, and acquisition waits must support this mechanism.

## Instagram-backend mapping

The real datasource points to PostgreSQL in [application.properties](../../../../../src/main/resources/application.properties), but the project does not explicitly configure HikariCP pool size or acquisition timeout, so framework defaults currently apply. Database readiness is included, but readiness only proves a check can reach the database; it does not prove spare pool capacity.

[PostService.feed(...)](../../../../../src/main/java/com/instagram/backend/service/PostService.java) is a read-only transaction that authenticates the viewer, loads every post, and maps the response. Its response mapper invokes `existsByPostAndUser(...)` per post. [PostRepository.java](../../../../../src/main/java/com/instagram/backend/repository/PostRepository.java) exposes an unpaged feed query. These are concrete places to measure connection hold time and query count.

[RequestCorrelationFilter.java](../../../../../src/main/java/com/instagram/backend/observability/RequestCorrelationFilter.java) supplies request duration and correlation, and [application.properties](../../../../../src/main/resources/application.properties) enables HTTP histograms. **Proposed, not implemented:** HikariCP wait/usage dashboards and alerts, PostgreSQL lock and slow-query monitoring, explicit pool sizing from measured capacity, paged feed access, and a set-based like-state query.

## When would a larger pool actually help?

Increase it cautiously only if acquisition wait is the bottleneck, database CPU/I/O/session capacity has headroom, queries and transactions are healthy, and a load test shows higher end-to-end throughput without worse database latency. Change gradually and consider total connections across every application instance.

Do not increase it when queries are slow, locks are long, connections leak, or the database is already constrained. A pool of 30 across 20 pods is potentially 600 sessions; per-instance settings must be evaluated at fleet scale.

## Choose between less work, more concurrency, and fewer arrivals

- **Optimize queries and shorten transactions** for a durable reduction in hold time. It requires diagnosis and may need indexes or API changes, but it improves capacity without multiplying sessions.
- **Increase the pool** when the database has verified headroom. It reduces application waiting but consumes database memory and can worsen lock or I/O contention.
- **Admission control** bounds database concurrency and preserves critical paths. It intentionally rejects or degrades excess work, so product behavior and retry guidance must be explicit.

## What would prove that a bigger pool made things worse?

**Prediction:** if PostgreSQL is already constrained, doubling every instance's pool increases concurrent work without increasing completions.

**Detect:** acquisition wait may fall briefly, then database activity and query latency rise. Throughput stops improving while p99 worsens. That combination is stronger evidence than a high session count alone.

**Contain:** restore the tested concurrency limit, stop retry amplification and optional batch work, and cap new database demand.

**Recover:** let useful sessions and queues drain, then compare throughput, tail latency, and connection wait with the original baseline.

**Prevent:** size connections across the whole application fleet, including extra instances during rollout. Load-test hold time and database capacity together, and investigate slow transaction paths before treating the pool limit as the cause.

## Two-to-three-minute interview answer

The application is likely waiting rather than computing. I would first verify pool saturation using active, idle, pending, acquisition-time, usage-duration, and timeout metrics and align them with HTTP latency. Then I would inspect the database: CPU, I/O, session limits, active and slow queries, lock waits, and query plans. I would compare fast and slow traces and measure transaction duration and queries per request to distinguish more traffic from longer connection hold time.

I would not automatically increase the pool. It is appropriate only when callers are waiting, PostgreSQL has measured headroom, transactions are healthy, and a load test proves that more connections increase throughput. Otherwise it merely moves the queue into the database and can worsen contention. During impact I would bound database concurrency, stop expensive optional work, and prevent retries from multiplying load. For this project, I would measure the feed transaction because it loads every post and checks like state per item. The durable fix may be pagination and a set-based query, followed by fleet-level pool sizing and alerts on acquisition wait and connection hold time.

## Before you practise

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
