# SLOs and Observability

## Question

Your API dashboard shows mostly `200` responses, but users report that feeds are slow and sometimes fifteen minutes stale.

**“How would you define SLOs and observability for the main user journeys?”**


## A green response can still be a bad feed

The feed returns HTTP `200` in 180 ms, but its projection is fifteen minutes behind accepted posts. The transport and timing look healthy while the useful result is stale. We need to measure the promise the person depends on.

A **service-level indicator (SLI)** is a defined measurement of that promise: for example, the fraction of eligible feed loads completed within a time limit. A **service-level objective (SLO)** is the target for that measurement over a stated window. Define where requests are counted, which ones qualify and which failures count; otherwise the percentage is ambiguous.

Freshness needs its own evidence. Comparing the newest displayed post with wall-clock time is insufficient: perhaps nobody posted recently. In our example, compare the projection's progress with a known committed event that should be visible. The fifteen-minute delay is then meaningful evidence of lag.

An airline tracks delayed arrivals and lost luggage as well as landings. Similarly, one successful API response does not describe an entire distributed journey. The analogy stops where software needs to connect asynchronous stages and partial results explicitly.

## Turn a target into an operating decision

An **error budget** is the permitted amount of unreliability before the SLO is missed. It makes a target actionable: rapidly consuming the allowance calls for investigation and possibly slowing risky releases. Set the objective from user impact and observed performance, rather than copying an impressive percentage.

Use aggregate metrics to discover how widespread the problem is. Use traces to see where a request spent time, and structured logs to inspect specific events. Correlation or trace IDs connect that detailed evidence; no single signal proves the whole cause.

## Keep the monitoring system finite

Every combination of metric labels creates another time series. This number of combinations is **cardinality**. Route templates, bounded outcome categories and application versions can be useful labels. A distinct user ID or full raw URL on every request can create an uncontrolled number of series.

Put necessary detailed identifiers into access-controlled logs or appropriate trace attributes, following the data policy. Alert on useful outcome and budget consumption, then use infrastructure measurements to diagnose the cause. For this stale feed, event-progress evidence should direct attention toward the delayed projection rather than a fast web response.

## Where this appears in our backend

[application.properties](../../../../../src/main/resources/application.properties) enables HTTP latency histograms, service-level buckets, bounded application/environment/version tags, health endpoints, and optional tracing export. [RequestCorrelationFilter](../../../../../src/main/java/com/instagram/backend/observability/RequestCorrelationFilter.java) emits normalized route, status, duration, and correlation IDs. [JourneyMetricsInterceptor](../../../../../src/main/java/com/instagram/backend/observability/JourneyMetricsInterceptor.java) and [InstagramMetrics](../../../../../src/main/java/com/instagram/backend/observability/InstagramMetrics.java) record bounded journey outcomes and media-upload sizes.

**Proposed, not implemented:** published SLOs, freshness/durability measurements, burn-rate alerts, dashboards, centralized telemetry storage, and operational runbooks.

## Choosing the approach and paying its costs

- Choose user-journey SLIs over host-only metrics for reliability decisions. Infrastructure signals remain essential for diagnosis but cannot prove user success.
- Choose metrics for cheap aggregation and alerting; do not put user IDs or raw URLs in labels. The cost of bounded labels is less per-user detail.
- Choose trace sampling to control volume, but configure an appropriate sampling strategy to retain useful errors and slow traces preferentially when supported; a sampling decision made before completion cannot know every final latency or error. Heavy sampling can hide rare failures; full tracing can be expensive.


## Paper drill: the dashboard itself becomes slow

Predict what happens after adding every user's ID as a metric label. A large set of users multiplies the time-series count, increasing storage and query work just when responders need reliable monitoring.

Detect the rising series count, ingestion pressure and dashboard latency. Remove the unbounded label and apply a controlled ingestion limit if needed. Let series expire or use the monitoring system's safe cleanup process while retaining appropriate diagnostic evidence.

Prevent recurrence by reviewing allowed labels and testing their possible combinations. Recheck the original user-journey indicators after recovery: a cheaper monitoring system is useful only if it still reveals slow, failed or stale outcomes.

## Two-to-three-minute interview answer

I would start from user journeys, not servers. For login, feed load, post creation, and media upload, I would define what success means and then choose availability, p95/p99 latency, and where necessary freshness or durability indicators. An SLO is a target over a window, and its error budget tells us how much unreliability we can spend before slowing releases or prioritizing reliability work.

For observability, bounded metrics answer how many and how bad, traces show where time went, and structured logs preserve detailed events. They should share route, version, environment, and correlation or trace context without putting user IDs into metric labels. I would alert on error-budget burn rather than every small metric movement and connect alerts to runbooks. In this project, HTTP histograms, correlation logs, journey counters, and optional tracing form a useful baseline, but freshness SLIs, dashboards, and burn-rate alerts remain proposed.


## Before moving on

Artifact: `READY`; comprehension: `TODO`. Explain this scenario in your own words, predict the paper drill, and attempt the questions before opening their answers. Record learning only from your own explanation and predictions.

## Questions and explained answers

<details>
<summary>Why is HTTP success rate insufficient for a feed SLO?</summary>

A response can be syntactically successful while returning stale, incomplete, or unauthorized content. The SLI must represent the useful outcome, including freshness when users depend on it.
</details>

<details>
<summary>Where should a user ID appear if it is needed for debugging?</summary>

Put it in access-controlled structured logs or sampled trace attributes, not an unbounded metric label. Metrics need bounded dimensions so their time-series count remains predictable.
</details>

<details>
<summary>What does an error budget change operationally?</summary>

It turns an SLO into a decision tool. Rapid budget burn justifies incident action and release restraint; healthy budget allows normal product delivery instead of treating every imperfection as an outage.
</details>
