# Micro-Lesson 07 — Tail Latency, Percentiles, and Queueing

Core study: 15 minutes. Prerequisite: [Latency, Throughput, and Saturation](../15-advanced-architecture-and-performance/06-latency-throughput-and-saturation.md).

## Six people wait four seconds while the average says 428 milliseconds — Must understand

Take one fixed minute of feed traffic: 100 requests, with 94 finishing in 200 ms and six in 4,000 ms. The six slow callers each wait four seconds. A dashboard showing only the average compresses that experience into a much smaller number.

Calculate the total measured time first. The fast group contributes `94 × 200 = 18,800 ms`; the slow group contributes `6 × 4,000 = 24,000 ms`. Together that is `42,800 ms`. Divide by 100 requests and the average is **428 ms**. No caller in this example actually experienced 428 ms.

The slow end of the response-time distribution is **tail latency**. We need a measurement that points to that end rather than spreading its cost across everyone.

## Sort the same requests and find the 95th position

A **percentile** describes a position in ordered measurements. Use the **nearest-rank convention** for this worked sample: for p95, choose position `ceil(0.95 × N)`, where N is the sample count and ceil rounds up. With N=100, that is position 95 using one-based positions.

Positions 1 through 94 contain 200 ms. Positions 95 through 100 contain 4,000 ms. Therefore the 95th value, and our sample's p95, is **4,000 ms**. It exposes the slow group that the 428 ms average hides.

Ties matter. Here six requests equal p95 and none is strictly greater than it. Do not interpret “p95 is four seconds” as “exactly 5% exceeded four seconds.” Percentile conventions and histogram estimates can differ, so state the method when deriving exact sample results.

Keep the population meaningful too: one route, a defined time window, and enough measurements. Combining a feed read with unrelated media uploads can conceal which user journey is slow.

## A successful request can spend seconds in a queue

**Queueing** is waiting for limited capacity before useful processing begins. During our feed incident, a request might wait for a worker, then for a database connection, before PostgreSQL starts executing its query.

```text
Client
  → HTTP request waits for an available worker
  → feed transaction waits for a database connection
  → PostgreSQL executes the query
  → application maps posts to the response
  → Client
```

When connections are busy, some requests arrive at a free slot while others arrive behind waiting work. The latter can take seconds even if their query execution is similar. They may still return successfully, leaving a low error rate alongside a slow tail. A percentile can reveal that experience more clearly than an average, although the average and errors can also move; there is no guaranteed order in which the signals worsen.

Picture checkout lines where most shoppers finish quickly but a few wait behind a price check. The slow shoppers are real even if the average looks acceptable. Software adds several waiting stages and sometimes several parallel dependency calls, so measure the stage rather than assuming the visible queue is the only one.

## Follow a slow request to the stage that consumed its time

Compare the same route across time, version, instance, and region. Inspect a representative slow request and separate worker wait, connection acquisition, query execution, and response construction where evidence is available. A high p95 describes the slow end of the measured population; route, version and instance breakdowns help identify affected groups. Stage timing or traces are needed to locate their extra delay.

More database connections are not automatically the remedy. They can move waiting from the application into PostgreSQL, where extra concurrent work creates more contention. Choose concurrency from measured usable capacity, and reduce unnecessary work or bound admission while investigating.

## Instagram-backend mapping

The current [application.properties](../../../../../src/main/resources/application.properties) enables an HTTP latency histogram and explicitly adds service-level boundaries from `100 ms` through `2 s`. These are not necessarily the complete exported bucket set; generated percentile-histogram boundaries may also appear. [Micrometer explains how the two sets combine](https://docs.micrometer.io/micrometer/reference/concepts/histogram-quantiles.html). Central collection could use them to answer percentile questions; one local scrape is not a historical production view.

[RequestCorrelationFilter.java](../../../../../src/main/java/com/instagram/backend/observability/RequestCorrelationFilter.java) logs route, status, and total `durationMs`. [PostService.java](../../../../../src/main/java/com/instagram/backend/service/PostService.java) measures feed work with `instagram.feed.load`.

The feed loads every post. Pagination, connection-wait metrics, retained dashboards, and database child spans are **proposed**, not implemented. They would separate growing result work from PostgreSQL wait time.

## Which measurement or limit answers the next question?

| Choice | Choose it when | Cost or limitation |
|---|---|---|
| Track route-level percentiles | User experience varies within the same endpoint | Needs enough samples and a defined time window |
| Split time by processing stage | Total latency says where to look but not what caused it | More instrumentation and storage |
| Bound waiting time | A shared resource can become saturated | Some requests fail quickly instead of eventually succeeding |
| Reduce per-request work | Large results or fan-out create a slow tail | May require pagination, batching, or a changed contract |

Do not optimize a high percentile from a handful of samples. Confirm enough traffic and compare the same population. A fast fallback returning incorrect data is not a success.

## The slow group still returns successful responses — paper exercise

Use the six slow feed requests from the same minute. Predict whether an error-rate alert alone is guaranteed to notice their four-second waits.

It is not: all six may eventually succeed. Compare high-percentile latency with active requests and database-connection wait time. If wait rises while execution time changes little, a connection queue is a stronger explanation than “the query took four seconds.” Treat that as evidence to investigate, not a conclusion from p95 alone.

Bound new work, reduce expensive feed size, and stop retries that add demand. Let waiting work drain, then verify both percentiles and active-request counts return to their target. Pagination, realistic capacity tests, and alerts connecting user latency to resource waits can prevent the same unbounded queue from recurring. A fast fallback returning incorrect data does not satisfy the user journey.

## Explain both numbers without changing the traffic

In 60–90 seconds, explain why `428 ms` average and `4,000 ms` p95 can describe the same traffic, then trace where a feed request might wait.

## Before moving on

Artifact: `READY`; comprehension not yet demonstrated.

Proceed only when you can:

- calculate the example average and identify the p95 value;
- explain why successful requests can still signal an incident;
- separate waiting for a connection from query execution;
- choose one containment action without claiming it is the root-cause fix.

## Questions and explained answers

<details>
<summary>1. Why can average latency stay acceptable while users report a severe slowdown?</summary>

Many fast requests can pull the average down while a smaller cohort waits seconds. Break measurements down by route, version, and region, then inspect p95 and a representative slow request.

</details>

<details>
<summary>2. Does a high p95 prove the database query itself is slow?</summary>

No. The request may be waiting for a worker, connection, lock, network response, or another dependency before the query runs. Measure the stages or inspect a trace before choosing a fix.

</details>

<details>
<summary>3. Why might increasing the database connection pool worsen tail latency?</summary>

More queries can exceed database capacity, increasing contention, locks, and query time. The application queue shrinks while the bottleneck moves into PostgreSQL. Tune from measured capacity rather than assuming concurrency is free.

</details>
