# High p99 Latency with a Normal Average

## Question

The service’s average response time is about **220 ms**, so the main dashboard looks healthy. However, users report occasional requests taking **8 seconds**, and p99 latency is high.

Your manager asks:

**“Why can the average look normal, and how would you investigate and reduce the tail latency?”**

> [One-page revision card](../../revision-cards/production-scenarios/03-high-p99-with-normal-average.md)

## Why this problem exists

An average compresses the entire latency distribution into one number. If 99 requests finish near 140 ms and one takes 8 seconds, the average is still roughly 219 ms. That single slow request is not statistical noise to the affected user. It can also hold a thread or connection long enough to amplify queueing.

Tail latency usually comes from an occasional condition rather than the normal code path: a cold cache, garbage-collection pause, lock wait, connection acquisition delay, retry, noisy instance, unusually large result, or slow dependency. The investigation must isolate what is different about slow requests.

## Analogy: average travel time

A road can average a 20-minute commute while one in every hundred drivers waits at a closed railway crossing for eight minutes. Improving the normal road speed barely helps those drivers; the crossing must be identified.

The analogy stops because service requests share resources. One slow request can occupy a connection, trigger retries, and delay requests that arrived later.

## Terms to establish

- **p50** is the median: half of requests are faster and half are slower.
- **p99** is the latency at or below which 99% finish; the slowest 1% exceed it.
- **Tail latency** is latency in the slow end of the distribution, commonly p95, p99, or p99.9.
- A **histogram** counts observations in latency ranges so percentiles can be estimated across many requests.

## Investigation sequence

### 1. Validate the measurement

Confirm the time window, route, status, region, instance, and version behind p99. Ensure there is enough traffic for p99 to be meaningful and compare client/gateway latency with server latency. Separate successful slow responses from timeout errors. Plot p50, p95, p99, and maximum together; do not replace one misleading aggregate with another.

### 2. Find the affected population

Break p99 down by normalized route, instance, zone, payload or result-size bucket, and application version using bounded dimensions. Look for one noisy instance, one route, or one input class. Avoid user IDs and raw URLs as metric labels because their unbounded values can overwhelm the monitoring system.

### 3. Compare slow traces with fast traces

Take traces from the tail and compare span shape and duration with fast requests on the same route. Ask whether the extra time is before handler execution, inside database acquisition or SQL, during a downstream call or retry, or in a runtime pause. Correlate with garbage collection, thread queues, connection waits, database locks, cache misses, and downstream percentiles.

### 4. Fix the conditional cause

Optimize the rare slow branch, bound result sizes, remove lock contention, warm or coalesce cache misses, or set a realistic deadline on remote work. Adding capacity can help queueing but will not fix an eight-second lock or retry. Verify that p99 improves without hiding failures or damaging throughput.

## Concrete request walkthrough

Consider 10,000 `GET /api/feed` requests in five minutes.

1. Most requests obtain a connection quickly and return in 140–250 ms.
2. A small group arrives while PostgreSQL is waiting on a lock or the pool has no idle connection.
3. Those requests wait several seconds before the same feed code executes.
4. Their completion logs show `durationMs` near 8,000, but all return `200`.
5. The overall average remains near 220 ms; p99 and the histogram’s upper bucket rise.
6. Slow traces share database-wait time, while fast traces do not.

The observable result identifies a conditional database wait. If slow requests instead cluster on one JVM with long pause time, the same method would point to garbage collection rather than SQL.

## Instagram-backend mapping

[application.properties](../../../../../src/main/resources/application.properties) enables the `http.server.requests` percentile histogram and explicit `100 ms`, `250 ms`, `500 ms`, `1 s`, and `2 s` service-level buckets. Requests above two seconds still enter the histogram’s final overflow bucket, but more upper buckets may be proposed if operators need finer resolution around an eight-second objective.

[RequestCorrelationFilter.java](../../../../../src/main/java/com/instagram/backend/observability/RequestCorrelationFilter.java) emits a completion event with normalized route, status, correlation ID, and duration. [PostService.java](../../../../../src/main/java/com/instagram/backend/service/PostService.java) wraps feed work in `instagram.feed.load`, so comparing HTTP time with feed-operation time can help distinguish time around the service method from time inside it.

Normal tracing samples 10% through [application.properties](../../../../../src/main/resources/application.properties). **Proposed, not implemented:** retain slow/error traces with tail-aware sampling, connect histogram exemplars to trace IDs, add JVM/pool/database dashboards, and alert on a sustained p99 objective. A 10% random sample can miss rare tail requests.

## Choices and trade-offs

- **More detailed histogram buckets** improve tail resolution but increase metric series and storage cost. Choose boundaries around user objectives, not every possible millisecond.
- **Tail-aware trace retention** preserves slow requests and is ideal for rare latency. It requires buffering or collector support and costs more than low-rate random sampling.
- **A shorter timeout** bounds resource occupation when a dependency is no longer useful. It does not make the dependency faster and can convert slow successes into failures if chosen below legitimate latency.

## Failure drill

**Prediction:** one database lock makes 1% of feed requests exceed five seconds while average latency stays below 300 ms. **Symptom and detection:** p99 and the overflow histogram bucket rise; slow logs return `200`; database lock-wait metrics align with them. **Containment:** stop or isolate the conflicting maintenance/write operation and bound incoming concurrency. **Recovery:** clear the lock, drain waiting requests, and verify p99—not only average—returns to baseline. **Prevention:** shorten transaction scope, monitor lock age, retain tail traces, and alert on p99 over multiple windows.

## Two-to-three-minute interview answer

The average looks normal because a small number of eight-second requests contribute little to the arithmetic mean. I would first validate p99 by route, window, traffic volume, status, instance, zone, and version, and compare server latency with gateway or client latency. Then I would segment the tail using bounded dimensions and compare slow traces with fast traces for the same route.

I am looking for a conditional difference: connection acquisition, database lock or slow query, garbage-collection pause, cold cache, retry, noisy instance, large result, or slow dependency. I would align traces with thread queues, pool waits, JVM pauses, database locks, and downstream percentiles. The fix targets that cause—such as reducing transaction scope, bounding results, eliminating a retry, or isolating a bad instance—rather than optimizing the already-fast median. I would verify p99, errors, and throughput together. Finally, I would add a sustained tail-latency alert and retain slow traces, because a 10% random trace sample may miss the exact 1% users we need to understand.

## Practice status

Artifact readiness does not prove mastery. Mark this practiced only after explaining why the mean is misleading without notes, predicting one tail-only failure, and answering the questions below before opening them.

## Questions and explained answers

<details>
<summary>How can one 8-second request among 100 requests leave the average near 220 ms?</summary>

If the other 99 requests are about 140 ms, total time is roughly `99 × 140 + 8,000 = 21,860 ms`; divided by 100, that is about 219 ms. The average hides the one severely affected user.
</details>

<details>
<summary>Why might 10% random tracing fail to explain high p99?</summary>

The slowest requests are rare and may not be selected. Tail-aware retention keeps traces because they are slow or erroneous, making the sample reflect the problem being investigated.
</details>

<details>
<summary>Why is lowering the timeout not automatically a tail-latency fix?</summary>

It limits waiting and protects resources, but it can turn slow successful requests into errors and does not remove the underlying lock, queue, or dependency delay. The timeout must reflect the caller’s deadline and expected legitimate work.
</details>
