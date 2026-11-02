# Micro-Lesson 06 — Latency, Throughput, and Saturation

Core study: 15 minutes. Prerequisite: [Scalability, Availability, and Reliability](../09-reliability-resilience-and-fault-tolerance/01-scalability-availability-reliability.md).

## A four-second feed request may spend most of its time waiting — Must understand

The feed normally returns in 200 milliseconds. At peak traffic it takes four seconds. CPU use is only 35%, so the team adds backend instances. The database still completes no more work, and the extra requests wait even longer. Where did the added time go?

A request needs more than CPU. It may wait for an application worker, a database connection, a lock, or a remote response. A mostly idle processor cannot replace a database connection that is already occupied. Separate arriving demand, completed work, and waiting before choosing more hardware.

## Three measurements describe different parts of the same incident

**Latency** is one request's elapsed time, including processing and waiting. The user sees four seconds whether the query executes for four seconds or waits 3.8 seconds before a short query.

**Throughput** is the amount of completed work per unit of time. More incoming requests are increased demand, not increased throughput. If completions per second flatten while arrivals rise, the extra work is waiting, being rejected, or failing somewhere.

**Saturation** means a resource is near its safe working limit. Additional demand then waits or is rejected. A fully occupied connection pool tells us requests cannot get another connection immediately; we still need evidence of why connections stay occupied, such as query time, locks, or other work inside the transaction.

Imagine checkout lanes that can finish 90 carts per hour. An extra line of shoppers does not make cashiers complete more carts. Waiting grows even if each checkout takes the same amount of work. Software is more complicated because one request may compete for several resources, and timeouts can trigger retries that add shoppers to the line again.

## Follow the feed into the connection wait

At normal load, `GET /api/feed` reaches a worker, validates the token, obtains a database connection, reads posts, and maps the result. At peak load, earlier requests still hold the configured connection pool while later requests arrive. The newcomers wait before their queries begin.

If completed requests per second stop increasing, that wait can grow while useful throughput stays flat. Eventually clients time out. Retrying each timeout adds more work to the same path unless the retry policy is bounded and the operation is safe to repeat.

Before continuing, predict the first investigation when traffic doubles, connections are occupied, and latency rises from 200 milliseconds to four seconds while CPU stays at 35%. Adding application instances assumes the constrained dependency has spare capacity, which this evidence does not establish.

## Find the waiting stage before choosing the fix

Start with the affected route's latency and errors, then align the change with traffic, deployments, and request mix. Compare completions with incoming demand and queue or resource measurements. Split elapsed time into connection acquisition, query execution, and response work where instrumentation permits it.

A growing connection wait directs attention to the database path, but is not by itself proof that the SQL text became slower. Inspect the work holding connections and test a reversible mitigation. Reducing unnecessary feed queries can help; adding instances helps only when application capacity is the limiting resource and dependencies have headroom.

Compare typical and slow requests in the same population. An average can hide the people waiting longest; the next lesson develops percentiles so we can inspect that group directly.

## Instagram-backend mapping

**Actual visibility:** [application.properties](../../../../../src/main/resources/application.properties) enables HTTP latency histograms. [PostService.java](../../../../../src/main/java/com/instagram/backend/service/PostService.java) observes feed loading. [JourneyMetricsInterceptor.java](../../../../../src/main/java/com/instagram/backend/observability/JourneyMetricsInterceptor.java) and [InstagramMetrics.java](../../../../../src/main/java/com/instagram/backend/observability/InstagramMetrics.java) record journey outcomes.

**Actual risk:** the feed calls `findAllByOrderByCreatedAtDesc()` in [PostRepository.java](../../../../../src/main/java/com/instagram/backend/repository/PostRepository.java), so work grows with all posts. `PostService` also calls `existsByPostAndUser` from [PostLikeRepository.java](../../../../../src/main/java/com/instagram/backend/repository/PostLikeRepository.java) once per post. This repeated database work can amplify latency.

**Proposed:** paginate the feed, fetch like state in bounded work, and expose query time, connection use, and waits beside HTTP metrics. These changes are not implemented; measurements should verify the bottleneck first.

## Let the bottleneck choose the response

| Response | Choose it when evidence shows | Avoid it when | Cost or new failure mode |
|---|---|---|---|
| Optimize the expensive path | Repeated queries, scans, locks, or serialization dominate | The service is healthy and demand alone exceeds capacity | Engineering time and possible correctness regressions |
| Add parallel capacity | Application CPU or workers are constrained and dependencies have headroom | A shared database or downstream is already saturated | Higher cost and more pressure on dependencies |
| Reject excess work early | Demand exceeds safe capacity and waiting would cause timeouts | Every request must be accepted without a durable queue | Some requests fail quickly, requiring clear client behavior |
| Roll back a release | Latency aligns with one version and rollback is safe | Evidence points to an unrelated traffic or dependency event | Reverts useful changes and does not explain the root cause |

## Automatic scaling adds callers to the full database — paper exercise

Suppose a latency rule keeps adding application instances while PostgreSQL is already saturated. Predict what happens to useful completions and waiting if each new instance sends more database work.

Instance count can rise while throughput stays flat and connection waits, contention, and timeouts increase. Stop runaway scaling, reject excess work, suppress unsafe retries, and reduce expensive feed work so the constrained path can recover.

Check that waiting drains and useful request outcomes return to their target before reopening demand. Cap database concurrency and load-test realistic data, including the current feed's repeated work. Alert on the combination of rising latency and flat throughput rather than treating latency alone as proof that the application needs more replicas.

## Explain the four seconds using evidence

In 60–90 seconds, use the checkout analogy, then explain the 200-millisecond-to-four-second feed incident: what you graph together, what evidence identifies saturation, and why adding instances may fail.

## Before moving on

Artifact: `READY`; comprehension not yet demonstrated.

Proceed only when you can distinguish latency from throughput, recognize a saturated queue from the prediction, locate the likely repeated work in the feed path, and choose between optimization, capacity, early rejection, and rollback using evidence.

## Questions and explained answers

<details>
<summary>1. Why can latency rise sharply while throughput stays flat?</summary>

The constrained resource is completing work at its maximum sustainable rate. Extra requests cannot increase completions, so they accumulate and spend longer waiting. That waiting increases latency even if the useful processing time per completed request barely changes.

</details>

<details>
<summary>2. What should you check first when peak latency rises from 200 milliseconds to four seconds?</summary>

Verify user-facing timing and errors, then align the change with traffic and deployments. Compare throughput with queues and resource use. The first resource whose queue or use rises with latency is stronger evidence than CPU alone.

</details>

<details>
<summary>3. A latency spike begins 20 minutes after deployment. Should you roll back immediately?</summary>

Treat the release as a strong hypothesis, not proof. Compare versions, request mix, dependencies, and configuration. If impact is severe and rollback is safe, roll back while preserving evidence; identify the cause before redeploying.

</details>
