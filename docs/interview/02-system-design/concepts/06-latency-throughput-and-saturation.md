# Micro-Lesson 06 — Latency, Throughput, and Saturation

- Status: `READY`; comprehension not yet demonstrated
- Time: 15 minutes
- Primary idea: response time rises sharply when incoming work approaches the capacity of the slowest constrained resource
- New terms: latency, throughput, saturation
- Prerequisite: [Scalability, Availability, and Reliability](./01-scalability-availability-reliability.md)

## Why does this exist?

A service can take 200 milliseconds normally and four seconds at peak without doing four seconds of useful work. Most time may be spent waiting for a thread, database connection, CPU, lock, or dependency.

Separate demand, completed work, and waiting. Extra instances help only if the constrained resource accepts more work; a database at its limit may get worse.

## Analogy: supermarket checkouts

Three cashiers can finish 90 carts per hour. When arrivals approach or exceed 90, carts accumulate. Checkout time may stay constant while waiting time grows.

The analogy stops where distributed requests call several dependencies, retry after timeouts, compete for multiple pools, and may perform different amounts of work.

## Plain-language model

- **Latency** is the elapsed time for one request, including both useful processing and waiting.
- **Throughput** is the number of requests completed per unit of time, such as 500 requests per second.
- **Saturation** means a resource is near its safe working limit, so additional work waits or is rejected.

Average latency hides harm. Compare typical and slow requests, split time by component, and correlate it with rate, errors, queues, resource use, and version.

## Predict before reading

Traffic doubles. Completed requests per second stop increasing, database connections remain fully occupied, and latency grows from 200 milliseconds to four seconds. CPU is only 35%. Should you add application instances first?

<details>
<summary>Reveal the prediction</summary>

No. Throughput is flat while database connections are saturated, so requests are waiting. Low CPU does not prove spare capacity. More instances may intensify database contention. Protect and investigate that path, reduce expensive work, or limit concurrency.

</details>

## Concrete peak-traffic flow

1. A client sends `GET /api/feed`; normal traffic reaches an application thread immediately.
2. The service validates the token, gets a database connection, reads posts, and builds the response.
3. At peak traffic, more requests arrive concurrently than the database path can complete.
4. The connection pool reaches its configured limit; later requests wait even before their queries begin.
5. Completed requests per second flatten, but queued work continues growing.
6. Timeouts trigger retries, adding demand to the same constrained path.
7. Users see multi-second responses and then errors.

Investigate in this order: confirm the latency and errors; correlate them with traffic and deployment time; find the first resource whose use or queue rises with latency; inspect its slow operation; test a reversible mitigation.

## Instagram-backend mapping

**Actual visibility:** [application.properties](../../../../src/main/resources/application.properties) enables HTTP latency histograms. [PostService.java](../../../../src/main/java/com/instagram/backend/service/PostService.java) observes feed loading. [JourneyMetricsInterceptor.java](../../../../src/main/java/com/instagram/backend/observability/JourneyMetricsInterceptor.java) and [InstagramMetrics.java](../../../../src/main/java/com/instagram/backend/observability/InstagramMetrics.java) record journey outcomes.

**Actual risk:** the feed calls `findAllByOrderByCreatedAtDesc()` in [PostRepository.java](../../../../src/main/java/com/instagram/backend/repository/PostRepository.java), so work grows with all posts. `PostService` also calls `existsByPostAndUser` from [PostLikeRepository.java](../../../../src/main/java/com/instagram/backend/repository/PostLikeRepository.java) once per post. This repeated database work can amplify latency.

**Proposed:** paginate the feed, fetch like state in bounded work, and expose query time, connection use, and waits beside HTTP metrics. These changes are not implemented; measurements should verify the bottleneck first.

## Decision and trade-off

| Response | Choose it when evidence shows | Avoid it when | Cost or new failure mode |
|---|---|---|---|
| Optimize the expensive path | Repeated queries, scans, locks, or serialization dominate | The service is healthy and demand alone exceeds capacity | Engineering time and possible correctness regressions |
| Add parallel capacity | Application CPU or workers are constrained and dependencies have headroom | A shared database or downstream is already saturated | Higher cost and more pressure on dependencies |
| Reject excess work early | Demand exceeds safe capacity and waiting would cause timeouts | Every request must be accepted without a durable queue | Some requests fail quickly, requiring clear client behavior |
| Roll back a release | Latency aligns with one version and rollback is safe | Evidence points to an unrelated traffic or dependency event | Reverts useful changes and does not explain the root cause |

## Failure drill

Scenario: latency-based automatic scaling adds application instances while PostgreSQL connections are already exhausted.

1. **Prediction:** new instances add callers and connections, increasing database contention.
2. **Failure:** the database remains saturated while retries multiply work.
3. **Observable symptom:** instance count rises, throughput stays flat, and connection waits and timeouts rise.
4. **Containment and recovery:** stop runaway scaling, reject excess requests, suppress unsafe retries, and reduce expensive feed work.
5. **Prevention:** cap database concurrency, load-test realistic data, and alert when latency rises while throughput flattens.

## Teach-back

In 60–90 seconds, use the checkout analogy, then explain the 200-millisecond-to-four-second feed incident: what you graph together, what evidence identifies saturation, and why adding instances may fail.

## Stop/go

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
