# Requirements and Capacity Estimation

## Question

Product expects an Instagram-like service to support 10 million daily active users and traffic peaks five times higher than normal next year.

**“How would you estimate capacity and define requirements before designing an Instagram-like feed?”**

## Why this problem exists

“Ten million users” does not reveal peak feed reads, upload volume, latency targets, retention, or acceptable data loss. I would state assumptions, estimate orders of magnitude, and use them to expose likely constraints rather than claim false precision.

## Analogy: planning a stadium

A stadium is designed for the expected peak crowd, not average yearly attendance. Capacity planning likewise includes peaks and safety margin. The analogy stops because software demand and request cost change continuously, so the model must be revised from production evidence.

## Terms to establish

- A **service-level objective (SLO)** is a measurable reliability or latency target.
- **Queries per second (QPS)** is the request rate a component receives.
- **Concurrency** is the number of requests simultaneously using a resource.
- **Headroom** is unused capacity reserved for spikes, failures, and forecast error.

## What I would do

1. Define critical journeys and their latency, availability, durability, freshness, retention, and regional needs.
2. Estimate peak read/write QPS, concurrency, media storage, bandwidth, and growth.
3. Turn estimates into component budgets, add justified headroom, and validate them with load tests.

## Concrete walkthrough

Ten million users requesting 20 feed pages daily produce 200 million requests: about 2,315 average QPS and 11,600 QPS at a five-times peak. If 2% upload one 4 MB photo daily, raw media grows by about 800 GB per day before replicas and thumbnails. Those estimates justify bounded feed pages, object storage/CDN delivery, and peak-load testing.

## Instagram-backend mapping

The feed in [PostService.java](../../../../../src/main/java/com/instagram/backend/service/PostService.java) uses the unbounded query in [PostRepository.java](../../../../../src/main/java/com/instagram/backend/repository/PostRepository.java), so growth increases response work. [MediaStorageService.java](../../../../../src/main/java/com/instagram/backend/service/MediaStorageService.java) writes local files, while [application.properties](../../../../../src/main/resources/application.properties) configures HTTP latency histograms. Pagination, object storage, CDN delivery, and production capacity limits are proposed, not implemented.

## Choices and trade-offs

- Keep headroom for spikes and failures, but avoid unlimited idle capacity because it has recurring cost.
- Use estimates to choose tests and architecture; do not add complexity from speculative precision.
- Scale only the constrained tier after checking downstream headroom.

## Failure drill

**Prediction:** the team sizes from daily averages, then peak traffic fills request and database queues. **Symptom and detection:** p99 latency, concurrency, connection wait, and queue depth rise while throughput stops increasing. **Containment:** shed optional work, reduce page size, and add only capacity that dependencies can support. **Recovery:** drain queued work and verify latency and successful throughput return to their SLOs. **Prevention:** use peak multipliers, dependency-level budgets, forecast reviews, and load tests above expected peak.

## Two-to-three-minute interview answer

I would begin with critical journeys and define latency, availability, durability, freshness, retention, and regional expectations. Then I would state assumptions and estimate peak read/write QPS, concurrency, media storage, bandwidth, and growth. Ten million users making 20 feed requests daily is about 2,315 average QPS and 11,600 QPS at a five-times peak.

I would separate small API payloads from much larger media transfer and estimate reads, writes, and retained bytes independently. I would translate the results into budgets for workers, database connections and IOPS, cache, queues, storage, and egress. The numbers should identify decisions such as pagination, CDN delivery, or asynchronous work.

I would document uncertainty, keep justified headroom for spikes and instance loss, and check whether dependencies can absorb scaled traffic. Finally, I would validate the model with peak and failure load tests, compare results with SLOs, and replace assumptions with production measurements during regular forecast reviews.

## Practice status

The artifact is ready; comprehension is not implied. Practice by stating assumptions aloud, deriving one peak-QPS estimate, and explaining which design decision it changes.

## Questions and explained answers

<details>
<summary>Why is daily active users not enough to size the feed?</summary>

It does not show requests per user, peak concentration, response cost, media size, or latency requirements. Those factors determine QPS, concurrency, storage, bandwidth, and dependency capacity.
</details>

<details>
<summary>Why use a peak multiplier instead of only average QPS?</summary>

Queues and saturation occur during concentrated demand. A system sized exactly for the daily average can fail during predictable peaks even though its daily total looks manageable.
</details>

<details>
<summary>When should an estimate trigger an architectural change?</summary>

When it exceeds a component's measured safe capacity or violates an SLO with reasonable headroom. The estimate should lead to a testable decision, not complexity added merely because a number sounds large.
</details>
