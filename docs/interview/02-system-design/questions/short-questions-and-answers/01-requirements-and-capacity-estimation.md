# Requirements and Capacity Estimation

## Question

Product expects an Instagram-like service to support 10 million daily active users and traffic peaks five times higher than normal next year.

**“How would you estimate capacity and define requirements before designing an Instagram-like feed?”**


## What does “ten million users” ask the system to do?

Imagine two services with ten million daily users. In one, each person opens the feed once. In the other, each person scrolls through twenty pages and uploads a photo. The user count is the same; the work is very different. Before drawing servers, we need to turn a person's actions into requests and stored bytes.

For this exercise, assume each of our ten million users requests 20 feed pages per day. That gives `10,000,000 × 20 = 200,000,000` daily feed requests. A day has `24 × 60 × 60 = 86,400` seconds, so the average is `200,000,000 ÷ 86,400 ≈ 2,315` requests per second. This request rate is often called **QPS**, short for queries per second; be clear whether you mean API requests or actual database queries.

Our stated peak is five times the average: `2,315 × 5 ≈ 11,575`, rounded to about 11,600 feed requests each second. That is a useful load-test target. It is not yet a claim that any particular server count will handle it: we have not measured the work needed for one request.

## Count the photos separately

Suppose 2% of these users upload one 4 MB photo daily. That is `10,000,000 × 0.02 = 200,000` photos, then `200,000 × 4 MB = 800,000 MB`, or 800 GB per day using decimal units. This is only the original photo data. Thumbnails, additional copies and retained history increase storage; downloads add a separate bandwidth cost.

A stadium planner similarly counts the crowd expected at the busiest event, rather than averaging attendance over a year. Software needs an extra feedback loop: request cost and user behavior change, so production measurements must replace our assumptions over time.

## Decide what those numbers must achieve

Ask how fast a feed must appear, how stale it may be, and what a successful upload promises. A **service-level objective (SLO)** is a measurable target for such a promise over a stated window. “The feed should be fast” cannot guide a capacity test; a defined latency target can.

Leave **headroom**, meaning capacity beyond expected demand, for forecast error and failures. Its size should follow measured behavior and the failure we want to survive. Buying enough capacity for an arbitrary multiplier without testing the database merely moves the uncertainty into a larger bill.

Our estimates now motivate specific questions: can bounded feed pages meet the peak target, and can photo storage grow by at least 800 GB daily? Answer those with measurements before choosing a complicated architecture.

## Where this appears in our backend

The feed in [PostService.java](../../../../../src/main/java/com/instagram/backend/service/PostService.java) uses the unbounded query in [PostRepository.java](../../../../../src/main/java/com/instagram/backend/repository/PostRepository.java), so growth increases response work. [MediaStorageService.java](../../../../../src/main/java/com/instagram/backend/service/MediaStorageService.java) writes local files, while [application.properties](../../../../../src/main/resources/application.properties) configures HTTP latency histograms. Pagination, object storage, CDN delivery, and production capacity limits are proposed, not implemented.

## Choosing the approach and paying its costs

- Keep headroom for spikes and failures, but avoid unlimited idle capacity because it has recurring cost.
- Use estimates to choose tests and architecture; do not add complexity from speculative precision.
- Scale only the constrained tier after checking downstream headroom.


## Paper drill: the daily average looked safe

Before reading on, predict what happens when a service sized for 2,315 requests per second receives the estimated peak of about 11,600.

Requests arrive faster than the constrained component can finish them. Waiting work accumulates, and users see increasing latency before every request necessarily fails. Check queue depth, database-connection wait and successful throughput together: growing queues with flat throughput point toward exhausted capacity.

Contain the incident by reducing optional work or page size and limiting admitted work. Add capacity only where a measured constraint and downstream capacity justify it. Let queued work drain, then verify that useful throughput and latency recover together. Afterwards, test the five-times peak and an instance-loss case, and revise the forecast from observed traffic.

## Two-to-three-minute interview answer

I would begin with critical journeys and define latency, availability, durability, freshness, retention, and regional expectations. Then I would state assumptions and estimate peak read/write QPS, concurrency, media storage, bandwidth, and growth. Ten million users making 20 feed requests daily is about 2,315 average QPS and 11,600 QPS at a five-times peak.

I would separate small API payloads from much larger media transfer and estimate reads, writes, and retained bytes independently. I would translate the results into budgets for workers, database connections and IOPS, cache, queues, storage, and egress. The numbers should identify decisions such as pagination, CDN delivery, or asynchronous work.

I would document uncertainty, keep justified headroom for spikes and instance loss, and check whether dependencies can absorb scaled traffic. Finally, I would validate the model with peak and failure load tests, compare results with SLOs, and replace assumptions with production measurements during regular forecast reviews.


## Before moving on

Artifact: `READY`; comprehension: `TODO`. Explain this scenario in your own words, predict the paper drill, and attempt the questions before opening their answers. Record learning only from your own explanation and predictions.

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
