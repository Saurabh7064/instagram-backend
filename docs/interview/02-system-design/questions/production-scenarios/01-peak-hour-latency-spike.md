# Peak-Hour Latency Spike

## Question

You’re in a technical meeting. A service is working fine under normal traffic, but during peak hours the response time increases from **200 ms to 3–4 seconds**.

Your manager asks:

**“How would you investigate this performance issue, and what would you check first?”**

> [One-page revision card](../../revision-cards/production-scenarios/01-peak-hour-latency-spike.md)

## Why this problem exists

Latency often rises nonlinearly near a capacity limit. At low traffic, a request immediately gets a thread, database connection, CPU time, and downstream capacity. At peak traffic, one constrained resource develops a queue. Each request then spends more time waiting, remains in the system longer, and consumes concurrency for longer. That can create an accelerating slowdown even before error rates rise.

The first task is therefore not “add servers.” It is to determine **where time is spent and which resource first becomes saturated**.

## Analogy: supermarket checkouts

With ten shoppers and five cashiers, almost nobody waits. With fifty shoppers, service time at each cashier may still be two minutes, but total shopper time becomes much longer because of the queue. Opening another checkout helps only if cashiers are the constraint; it does not help if the payment network is slow.

The analogy stops at one store. A service request can use several shared pools and remote dependencies, retry work, or fail after its caller has already left.

## Terms to establish

- **Latency** is the time one request takes from the caller’s perspective.
- **Throughput** is completed work per unit of time, such as requests per second.
- **Saturation** means a finite resource is at or near its useful capacity and work is waiting.
- **p95 latency** is the value at or below which 95% of requests finish; it exposes slow users that an average can hide.

## What I would check first

### 1. Confirm and bound user impact

Compare request rate, p50/p95/p99 latency, error rate, and timeout rate between normal and peak windows. Break them down by route, instance, availability zone, application version, and response status. This answers whether every request is slower or one route or instance is responsible. Verify from server and client or gateway measurements so a network queue is not mistaken for application execution time.

### 2. Find the first saturated resource

Align the latency rise on one timeline with CPU, memory pressure and garbage-collection pauses, request threads, executor queues, database-pool active and waiting counts, database CPU/I/O/locks, and downstream latency. Low application CPU does not mean spare capacity: requests may be waiting for connections or remote responses.

### 3. Follow representative slow requests

Inspect several traces from the slow percentile and compare them with fast traces for the same route. Separate queue time, application work, database time, and downstream time. Use the correlation ID to find focused logs, but do not infer a population-wide cause from one log line.

### 4. Mitigate, then prove the cause

If users are actively affected, apply a reversible control: shed low-priority work, cap concurrency, disable an expensive optional feature, or scale the actually saturated tier when it has downstream headroom. Then reproduce the load and change one variable at a time.

## Concrete request walkthrough

Suppose `GET /api/feed` is 200 ms off-peak and 3.5 seconds at 1,000 requests per second.

1. The gateway accepts a request, but an application request thread waits 600 ms for a database connection.
2. Authentication loads the viewer from PostgreSQL.
3. The feed query returns all posts.
4. Response mapping checks whether that viewer liked each returned post, producing more repository calls as the result grows.
5. Each request keeps database work active longer; the connection wait queue grows.
6. The response returns successfully after 3.5 seconds, so an error-only dashboard remains green.

The observable signature would be rising HTTP tail latency followed by database acquisition wait, high pool utilization, and increasing query volume per feed request. This is a hypothesis to verify with query and pool measurements, not a conclusion from latency alone.

## Instagram-backend mapping

Implemented signals include HTTP latency histograms and service-level buckets in [application.properties](../../../../../src/main/resources/application.properties), normalized request duration and correlation IDs in [RequestCorrelationFilter.java](../../../../../src/main/java/com/instagram/backend/observability/RequestCorrelationFilter.java), and bounded feed success/failure metrics in [JourneyMetricsInterceptor.java](../../../../../src/main/java/com/instagram/backend/observability/JourneyMetricsInterceptor.java).

The real feed operation is wrapped by an observation in [PostService.java](../../../../../src/main/java/com/instagram/backend/service/PostService.java). That method calls `findAllByOrderByCreatedAtDesc()` and maps every result; response mapping calls `existsByPostAndUser(...)` for each post. [PostRepository.java](../../../../../src/main/java/com/instagram/backend/repository/PostRepository.java) currently has no pagination method. These are legitimate investigation targets under load, but measurement must establish their cost.

**Proposed, not implemented:** a dashboard joining HTTP percentiles with JVM, HikariCP, PostgreSQL, and instance metrics; slow-query capture; a bounded feed page; and load-shedding rules.

## Choices and trade-offs

- **Scale application instances** when CPU or request concurrency is the constraint and dependencies have headroom. Do not use it for a saturated database; more instances can create more database connections and worsen overload.
- **Cache a read** when staleness is acceptable and reads repeat. Do not cache user-specific authorization or like state carelessly. Cache invalidation and stampedes become new failure modes.
- **Optimize and paginate the query** when database work grows with result size. This gives durable capacity but may require API cursor changes and additional indexes.

## Failure drill

**Prediction:** peak feed traffic exhausts database connections and p95 crosses two seconds. **Symptom and detection:** successful requests become slow; HTTP histograms, pool waiting count, and database query rate rise together. **Containment:** cap feed concurrency and return a smaller page while preserving post creation. **Recovery:** drain queued work, verify latency returns to baseline, then remove the temporary cap gradually. **Prevention:** paginate, eliminate repeated lookups, capacity-test above forecast peak, and alert before the pool is fully occupied.

## Two-to-three-minute interview answer

I would first confirm the scope using request rate, errors, and p50/p95/p99 latency, split by route, instance, zone, and version. A jump only at peak traffic suggests queueing near a capacity limit, but I would not assume CPU or immediately add pods. I would align latency with thread and executor queues, garbage collection, database-pool active and waiting counts, database locks and I/O, and downstream latency. Then I would compare slow and fast traces for the same route to locate queue, application, database, or network time.

If customers are affected, I would mitigate before completing the diagnosis: shed optional work, bound concurrency, disable an expensive feature, or scale the constrained tier only when its dependency has headroom. For this project I would examine the feed path because it returns all posts and performs a per-post like check, but I would prove that with query counts and connection metrics. Finally, I would reproduce peak load, make one change, verify latency and throughput together, and add a capacity test and early saturation alert.

## Practice status

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
