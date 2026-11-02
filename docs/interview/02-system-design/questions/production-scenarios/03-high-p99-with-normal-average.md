# High p99 Latency with a Normal Average

## Question

The service’s average response time is about **220 ms**, so the main dashboard looks healthy. However, users report occasional requests taking **8 seconds**, and p99 latency is high.

Your manager asks:

**“Why can the average look normal, and how would you investigate and reduce the tail latency?”**

> [One-page revision card](../../revision-cards/production-scenarios/03-high-p99-with-normal-average.md)

## Two users can have very different experiences behind one average

Use one fixed measurement window: 10,000 feed requests, with 9,800 completing in 60 ms and 200 taking 8,000 ms. The total observed time is `9,800 × 60 + 200 × 8,000 = 588,000 + 1,600,000 = 2,188,000 ms`. Divide by 10,000 and the mean is `218.8 ms`, close to the dashboard's 220 ms.

That average is arithmetically correct. It just does not tell us that 200 users waited eight seconds. Once the observations are sorted, the first 9,800 are fast and positions 9,801 through 10,000 are slow. The 99th-percentile position, around 9,900, lies inside the slow group. Here, p99 is eight seconds even though the mean looks normal.

The useful question is what those 200 requests have in common. Improving the already-fast branch may barely help them. They might be waiting for a connection, a lock, a runtime pause, a cold cache, or a retry. We need to isolate that conditional difference.

## A faster open road does not remove a closed crossing

A road can average a 20-minute commute while one in every hundred drivers waits at a closed railway crossing for eight minutes. Improving the normal road speed barely helps those drivers; the crossing must be identified.

The analogy stops because service requests share resources. One slow request can occupy a connection, trigger retries, and delay requests that arrived later.

## Read the distribution before choosing a fix

**p50**, the median, describes the middle observation; here it is 60 ms. **p99** is the latency at or below which 99% of observations finish; here it is eight seconds because 2% are slow. Exact percentile estimators differ at boundaries, which is why we use a slow group larger than 1% in this example.

**Tail latency** means the slow end of the distribution. A **histogram** counts requests in latency ranges, preserving information the average discards. A monitoring query can estimate percentiles from those counts across instances. Coarse ranges give coarse estimates, so the reported percentile is not necessarily an exact measured request duration.

A high p99 tells us the slow population exists. It does not tell us which route, instance, or cause produced it. We still need comparable slices and representative requests.

## What is different about the slow 200?

First make sure the measurement describes the same traffic: route, window, status, version, and location. Compare client or gateway timing with application timing. If the delay happens before the application sees the request, optimizing its handler will not address it. Check that the window contains enough observations to make a percentile meaningful.

Then split the slow population with bounded dimensions such as route, instance, version, and result-size range. Do not put a different user ID or raw URL into every metric label; that creates an unbounded number of series. Individual request identities belong in logs and traces.

Compare several eight-second traces with fast traces for the same route. Find the extra wait in code order: before the handler, borrowing a database connection, executing SQL, calling a dependency, retrying, or pausing the runtime. Align the proposed cause with its population signal—for example, connection wait or database lock age—so one anecdotal trace does not become the whole diagnosis.

Fix that conditional cause and then check p99, errors, and throughput together. A shorter timeout may make the latency graph look better by turning slow successes into failures. Extra capacity may relieve a queue, but it will not remove an eight-second lock held by another transaction.

## Test the database-wait explanation

Keep the same 10,000-request window and its 200 slow requests. Suppose those requests arrive while PostgreSQL work is blocked by a lock or while every application connection is occupied.

1. The 9,800 fast requests obtain the needed resources promptly and finish in 60 ms.
2. The other 200 wait before doing much of the same feed work. Their elapsed time reaches eight seconds.
3. All return `200`, so an error-only dashboard misses the problem.
4. The mean remains 218.8 ms, but p99 and the histogram's high-duration counts reveal the slow group.
5. Database wait evidence appears in the same window and is present in representative slow requests, while fast requests lack that wait.

That would support a database-wait diagnosis. If the slow requests instead cluster on one Java process during runtime memory-management pauses, the comparison points there. The arithmetic tells us why the average hides the experience; the telemetry tells us why the experience occurred.

## Instagram-backend mapping

[application.properties](../../../../../src/main/resources/application.properties) enables the `http.server.requests` percentile histogram and explicit `100 ms`, `250 ms`, `500 ms`, `1 s`, and `2 s` service-level buckets. Those configured service-level boundaries are not necessarily the entire exported bucket set: percentile-histogram publication can add generated boundaries as well. Inspect the actual scrape before deciding whether an eight-second objective needs extra boundaries. [Micrometer explains how service-level and generated histogram buckets combine](https://docs.micrometer.io/micrometer/reference/concepts/histogram-quantiles.html).

[RequestCorrelationFilter.java](../../../../../src/main/java/com/instagram/backend/observability/RequestCorrelationFilter.java) emits a completion event with normalized route, status, correlation ID, and duration. [PostService.java](../../../../../src/main/java/com/instagram/backend/service/PostService.java) wraps feed work in `instagram.feed.load`, so comparing HTTP time with feed-operation time can help distinguish time around the service method from time inside it.

Normal tracing samples 10% through [application.properties](../../../../../src/main/resources/application.properties). **Proposed, not implemented:** retain slow/error traces with tail-aware sampling, connect histogram exemplars to trace IDs, add JVM/pool/database dashboards, and alert on a sustained p99 objective. A 10% random sample can miss rare tail requests.

## Better evidence and bounded waiting both have costs

- **More detailed histogram buckets** improve tail resolution but increase metric series and storage cost. Choose boundaries around user objectives, not every possible millisecond.
- **Tail-aware trace retention** preserves slow requests and is ideal for rare latency. It requires buffering or collector support and costs more than low-rate random sampling.
- **A shorter timeout** bounds resource occupation when a dependency is no longer useful. It does not make the dependency faster and can convert slow successes into failures if chosen below legitimate latency.

## Rehearse a problem the mean will hide

**Prediction:** a controlled database-lock test delays 2% of the fixed 10,000 feed requests to eight seconds while the others take 60 ms. The mean stays near 220 ms and p99 rises to the slow duration.

**Detect:** match slow successful requests and high histogram ranges to lock-wait evidence. The current repository's service-level feed observation alone cannot identify a specific SQL lock; that needs additional database evidence.

**Contain:** stop the controlled conflicting operation and bound new database work so the waiting queue does not grow indefinitely. In production, inspect the transaction owner and its data implications before cancelling it.

**Recover:** release the lock, drain useful waiting work, and verify p99, failure rate, and throughput return to baseline.

**Prevent:** reduce unnecessary transaction scope, observe lock age, retain representative slow traces, and alert over sustained tail-latency windows instead of trusting the mean.

## Two-to-three-minute interview answer

The average looks normal because a small number of eight-second requests contribute little to the arithmetic mean. I would first validate p99 by route, window, traffic volume, status, instance, zone, and version, and compare server latency with gateway or client latency. Then I would segment the tail using bounded dimensions and compare slow traces with fast traces for the same route.

I am looking for a conditional difference: connection acquisition, database lock or slow query, garbage-collection pause, cold cache, retry, noisy instance, large result, or slow dependency. I would align traces with thread queues, pool waits, JVM pauses, database locks, and downstream percentiles. The fix targets that cause—such as reducing transaction scope, bounding results, eliminating a retry, or isolating a bad instance—rather than optimizing the already-fast median. I would verify p99, errors, and throughput together. Finally, I would add a sustained tail-latency alert and retain slow traces, because a 10% random trace sample may miss the 2% slow population in this example.

## Before you practise

Artifact readiness does not prove mastery. Mark this practiced only after explaining why the mean hides the 200 slow requests without notes, predicting the database-wait failure, and answering the questions below before opening them.

## Questions and explained answers

<details>
<summary>How can 200 eight-second requests coexist with an average near 220 ms?</summary>

In the fixed 10,000-request window, 9,800 requests take 60 ms and 200 take 8,000 ms. The total is `588,000 + 1,600,000 = 2,188,000 ms`, so the mean is `2,188,000 ÷ 10,000 = 218.8 ms`. The 99th-percentile position lies inside the 200 slow requests, so p99 is eight seconds. The mean and percentile answer different questions.
</details>

<details>
<summary>Why might 10% random tracing fail to explain high p99?</summary>

The slowest requests are rare and may not be selected. Tail-aware retention keeps traces because they are slow or erroneous, making the sample reflect the problem being investigated.
</details>

<details>
<summary>Why is lowering the timeout not automatically a tail-latency fix?</summary>

It limits waiting and protects resources, but it can turn slow successful requests into errors and does not remove the underlying lock, queue, or dependency delay. The timeout must reflect the caller’s deadline and expected legitimate work.
</details>
