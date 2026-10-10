# Micro-Lesson 07 — Tail Latency, Percentiles, and Queueing

- Status: `READY`; comprehension not yet demonstrated
- Time: 15 minutes
- Primary idea: a small group of slow requests can harm users even when average latency looks acceptable, so inspect percentiles and waiting time
- New terms: tail latency, percentile, queueing
- Prerequisite: [Latency, Throughput, and Saturation](./06-latency-throughput-and-saturation.md)

## Why does this exist?

An average compresses every request into one number. Many fast requests can hide users who wait several seconds. The slowest requests often spend time waiting for a database connection, worker, lock, or dependency. Finding that wait changes the fix: adding instances will not repair a database lock.

## Analogy: supermarket checkout lines

Most shoppers may finish in two minutes while a few wait twenty minutes behind a price check. The store's average can still look reasonable; the slow end exposes the problem.

The analogy stops matching when one request fans out, is retried, and competes for several resources. Its slowest branch can determine completion time.

## Plain-language model

- **Tail latency** is the response time experienced by the slowest portion of requests.
- A **percentile** is a boundary in an ordered set of measurements. If p95 is four seconds, 95% completed in four seconds or less and 5% took at least that long.
- **Queueing** is time spent waiting for limited capacity before useful processing begins.

Percentiles need a route and time window. Combining unrelated operations can hide which path is slow; a five-second media upload and feed read do not mean the same thing.

## Predict before reading

In one minute, 94 feed requests finish in `200 ms` and six finish in `4,000 ms`. What do the average and p95 show?

<details>
<summary>Reveal the prediction</summary>

The average is `(94 × 200 + 6 × 4,000) / 100 = 428 ms`. After ordering the durations, the 95th value is a `4,000 ms` request, so p95 is `4,000 ms`. The percentile exposes the slow group hidden by the average.

</details>

## Concrete feed flow

```text
Client
  → HTTP request waits for an available worker
  → feed transaction waits for a database connection
  → PostgreSQL executes the query
  → application maps posts to the response
  → Client
```

During a peak, all database connections become busy:

1. New requests continue arriving.
2. They wait for a connection while earlier requests hold the available connections.
3. Some complete near the normal time; requests arriving behind the queue take seconds.
4. The error rate may remain low because the requests eventually succeed.
5. p95 rises before the average or error count looks alarming.
6. Stage timing reveals whether delay occurs before, during, or after the query.

Compare the same route by time, version, instance, and region, then inspect one representative slow request.

## Instagram-backend mapping

The current [application.properties](../../../../src/main/resources/application.properties) records HTTP latency in fixed buckets from `100 ms` through `2 s`. Central collection could use them to answer percentile questions; one local scrape is not a historical production view.

[RequestCorrelationFilter.java](../../../../src/main/java/com/instagram/backend/observability/RequestCorrelationFilter.java) logs route, status, and total `durationMs`. [PostService.java](../../../../src/main/java/com/instagram/backend/service/PostService.java) measures feed work with `instagram.feed.load`.

The feed loads every post. Pagination, connection-wait metrics, retained dashboards, and database child spans are **proposed**, not implemented. They would separate growing result work from PostgreSQL wait time.

## Decision and trade-off

| Choice | Choose it when | Cost or limitation |
|---|---|---|
| Track route-level percentiles | User experience varies within the same endpoint | Needs enough samples and a defined time window |
| Split time by processing stage | Total latency says where to look but not what caused it | More instrumentation and storage |
| Bound waiting time | A shared resource can become saturated | Some requests fail quickly instead of eventually succeeding |
| Reduce per-request work | Large results or fan-out create a slow tail | May require pagination, batching, or a changed contract |

Do not optimize a high percentile from a handful of samples. Confirm enough traffic and compare the same population. A fast fallback returning incorrect data is not a success.

## Failure drill

**Scenario:** p95 feed latency rises from `250 ms` to `3.5 s` during peak traffic, while errors remain near zero.

1. **Prediction:** requests are waiting on a limited resource and eventually completing.
2. **Observable symptom:** high p95/p99, rising active requests, and increased database-connection wait time, with a smaller change in query execution time.
3. **Containment:** bound new work, reduce expensive feed size, and stop automatic retries that add more demand.
4. **Recovery:** let the waiting work drain, then verify percentiles and active-request counts return to baseline.
5. **Prevention:** use pagination, capacity tests, bounded admission, and an alert on both user latency and the resource wait that predicts it.

## Teach-back

In 60–90 seconds, explain why `428 ms` average and `4,000 ms` p95 can describe the same traffic, then trace where a feed request might wait.

## Stop/go

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
