# Peak-Hour Latency Spike

## Question

You’re in a technical meeting. A service is working fine under normal traffic, but during peak hours the response time increases from **200 ms to 3–4 seconds**.

Your manager asks:

**“How would you investigate this performance issue, and what would you check first?”**

> [One-page revision card](../../revision-cards/production-scenarios/01-peak-hour-latency-spike.md)

## The request may be waiting, not working harder

At 10 a.m., a feed request takes 200 ms. In the evening, the same route takes 3.5 seconds and still returns `200`. It is tempting to ask which calculation became slower. First ask a simpler question: did the request spend those extra seconds doing work, or waiting for its turn?

A request needs finite resources—a worker, a database connection, CPU time, perhaps another service. When one resource cannot keep up, requests queue behind it. The work may take the same time once it starts, but the user's total wait grows. Requests also stay active longer, so more are present at once and the queue can grow faster.

That is why peak-only slowdown suggests a capacity boundary without telling us which boundary. Adding application servers helps only if their capacity was the constraint and their dependencies can accept the extra work.

## More shoppers do not make the cashier slower

With ten shoppers and five cashiers, almost nobody waits. With fifty shoppers, service time at each cashier may still be two minutes, but total shopper time becomes much longer because of the queue. Opening another checkout helps only if cashiers are the constraint; it does not help if the payment network is slow.

The analogy stops at one store. A service request can use several shared pools and remote dependencies, retry work, or fail after its caller has already left.

## Separate the time users wait from the work we finish

**Latency** is the elapsed time for a request from the caller's perspective. **Throughput** is completed work per unit time, such as requests per second. The service can accept more requests without increasing throughput if the additional work merely joins a queue.

**Saturation** means demand is at or beyond a resource's useful capacity and work is waiting. A busy resource is not automatically saturated: a full pool completing work promptly differs from a full pool with growing acquisition waits.

Compare the typical request with the slow population. **p95 latency** is the duration at or below which 95% of observed requests finish; p99 does the same for 99%. Those percentiles help us notice that a minority is waiting seconds even when the average looks acceptable.

## Find where the extra three seconds appeared

First confirm the affected journey and time window. Is only the feed slow, or are login and post creation slow too? Compare the same route off-peak and at peak, then separate versions and instances. Check errors and completions alongside latency: many requests returning success slowly can leave an error-only dashboard green.

Next align the slowdown with a resource wait. If database acquisition time rises before HTTP latency, investigate the pool and database. If the handler starts late while SQL stays normal, inspect request-worker or executor queues. If a remote span expands, inspect that dependency. CPU, runtime memory-management pauses, locks, and disk or network waits help explain the clue; a long list of graphs without a shared timeline does not.

Compare several slow traces with fast traces from the same route. One slow request can suggest a mechanism; population metrics tell us whether it explains the incident. Also compare gateway/client and server timing so an upstream queue is not mistaken for Java execution.

If users are affected, make a reversible containment change while investigating. Bound optional feed concurrency, stop an expensive optional path, or add capacity to the measured constrained tier. Record the result. Then reproduce the load and change one factor at a time to test the explanation.

## Test a feed-specific explanation against the evidence

Consider this hypothetical load: `GET /api/feed` usually takes 200 ms, then reaches 3.5 seconds at 1,000 requests per second. The following sequence is a hypothesis, not an executed load-test result:

1. A feed request reaches the application and spends 600 ms waiting to borrow a database connection.
2. Once it gets one, authentication loads the viewer and the feed query loads all posts.
3. Response mapping checks like membership for each returned post, so query count grows with the feed result.
4. Longer database work keeps connections occupied. New arrivals join the acquisition queue instead of starting immediately.
5. The request eventually returns `200` after 3.5 seconds. Its business operation succeeded, but the user waited far longer.

To accept this explanation, we would need acquisition waits, connection hold time, and query counts to rise in the matching window. The code gives us a plausible place to measure; it does not establish the bottleneck on its own.

## Instagram-backend mapping

Implemented signals include HTTP latency histograms and service-level buckets in [application.properties](../../../../../src/main/resources/application.properties), normalized request duration and correlation IDs in [RequestCorrelationFilter.java](../../../../../src/main/java/com/instagram/backend/observability/RequestCorrelationFilter.java), and bounded feed success/failure metrics in [JourneyMetricsInterceptor.java](../../../../../src/main/java/com/instagram/backend/observability/JourneyMetricsInterceptor.java).

The real feed operation is wrapped by an observation in [PostService.java](../../../../../src/main/java/com/instagram/backend/service/PostService.java). That method calls `findAllByOrderByCreatedAtDesc()` and maps every result; response mapping calls `existsByPostAndUser(...)` for each post. [PostRepository.java](../../../../../src/main/java/com/instagram/backend/repository/PostRepository.java) currently has no pagination method. These are legitimate investigation targets under load, but measurement must establish their cost.

**Proposed, not implemented:** a dashboard joining HTTP percentiles with JVM, HikariCP, PostgreSQL, and instance metrics; slow-query capture; a bounded feed page; and load-shedding rules.

## Which change would relieve this constraint?

- **Scale application instances** when CPU or request concurrency is the constraint and dependencies have headroom. Do not use it for a saturated database; more instances can create more database connections and worsen overload.
- **Cache a read** when staleness is acceptable and reads repeat. Do not cache user-specific authorization or like state carelessly. Cache invalidation and stampedes become new failure modes.
- **Optimize and paginate the query** when database work grows with result size. This gives durable capacity but may require API cursor changes and additional indexes.

## Rehearse the overload without sacrificing post creation

**Prediction:** in a controlled load test, increasing feed demand fills the database pool and makes feed p95 exceed two seconds before the HTTP error rate rises.

**Detect:** compare successful-request latency with pending database borrowers and query rate. If only CPU changes while acquisition remains fast, this specific hypothesis is weakened.

**Contain:** cap optional feed concurrency or use an already-supported smaller page. Do not improvise a breaking response contract during the incident. Preserve capacity for post creation rather than letting every feed reader wait indefinitely.

**Recover:** stop the excess load, let queued work drain, and verify both feed latency and post-creation success return to baseline. Remove temporary limits gradually.

**Prevent:** implement bounded pagination and fewer repeated lookups where measurements justify them, capacity-test above the expected peak, and alert on sustained waiting before every connection is occupied.

## Two-to-three-minute interview answer

I would first confirm the scope using request rate, errors, and p50/p95/p99 latency, split by route, instance, zone, and version. A jump only at peak traffic suggests queueing near a capacity limit, but I would not assume CPU or immediately add pods. I would align latency with thread and executor queues, garbage collection, database-pool active and waiting counts, database locks and I/O, and downstream latency. Then I would compare slow and fast traces for the same route to locate queue, application, database, or network time.

If customers are affected, I would mitigate before completing the diagnosis: shed optional work, bound concurrency, disable an expensive feature, or scale the constrained tier only when its dependency has headroom. For this project I would examine the feed path because it returns all posts and performs a per-post like check, but I would prove that with query counts and connection metrics. Finally, I would reproduce peak load, make one change, verify latency and throughput together, and add a capacity test and early saturation alert.

## Before you practise

The artifact is ready; comprehension is not implied. Mark this scenario practiced only after explaining the investigation without notes, predicting the first observable signal of saturation, and answering the questions below before opening them.

## Questions and explained answers

<details>
<summary>Why is application CPU below 50% not proof that the service has spare capacity?</summary>

Requests may be blocked on a database connection, lock, executor queue, disk, or downstream call. Waiting work can produce high latency with modest CPU. Check every bounded resource and trace where time is spent.
</details>

<details>
<summary>What should be checked before adding more application instances?</summary>

Identify the constrained tier and verify downstream headroom. If PostgreSQL is already saturated, adding instances may add concurrent queries and connections, making latency worse rather than better.
</details>

<details>
<summary>Why compare p95 or p99 with p50 instead of using only average latency?</summary>

The median shows the typical request while tail percentiles show the slow population. A small but important group can wait seconds while the average still appears acceptable.
</details>
