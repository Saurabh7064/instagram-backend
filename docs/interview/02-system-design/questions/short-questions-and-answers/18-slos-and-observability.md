# SLOs and Observability

## Question

Your API dashboard shows mostly `200` responses, but users report that feeds are slow and sometimes fifteen minutes stale.

**“How would you define SLOs and observability for the main user journeys?”**

## Why this problem exists

Transport success is not the same as user success. A feed can return `200` with old data, an upload can succeed but never appear in a post, and a retry can hide dependency errors while increasing latency. Teams need explicit measures of useful outcomes, plus enough correlated evidence to locate a failure.

## Analogy

An airline does not measure success only by whether a plane landed; it also tracks safety, delay, and lost luggage. The analogy stops because distributed requests split across services and asynchronous work, so one user journey may require several connected signals.

## Terms to establish

- An **SLI** is a measured indicator such as successful feed loads or p99 latency.
- An **SLO** is the target for an SLI over a time window.
- An **error budget** is the allowed unreliability before the SLO is missed.
- **Cardinality** is the number of distinct metric label combinations; unbounded values can overwhelm monitoring.

## What I would do

1. List critical journeys—login, feed load, post creation, and media upload—and define success from the user’s view.
2. Add latency, availability, and where relevant freshness or durability SLIs, with a clear measurement point and window.
3. Set achievable SLOs from business impact and current performance, not a default number of nines.
4. Use bounded metrics for alerting, traces for request paths, and structured logs for detailed events, joined by correlation or trace IDs.
5. Alert on fast and slow error-budget burn, then maintain runbooks tied to likely saturation and dependency failures.

## Concrete walkthrough

1. A feed request returns in 180 ms with HTTP `200`.
2. Its newest visible post is fifteen minutes behind the committed feed event.
3. The availability and latency SLIs pass, but the freshness SLI fails.
4. A trace shows the API reading a stale projection while queue-age metrics show consumer lag.
5. The observable result is a freshness-budget alert that points responders toward the asynchronous pipeline rather than the web tier.

## Instagram-backend mapping

[application.properties](../../../../../src/main/resources/application.properties) enables HTTP latency histograms, service-level buckets, bounded application/environment/version tags, health endpoints, and optional tracing export. [RequestCorrelationFilter](../../../../../src/main/java/com/instagram/backend/observability/RequestCorrelationFilter.java) emits normalized route, status, duration, and correlation IDs. [JourneyMetricsInterceptor](../../../../../src/main/java/com/instagram/backend/observability/JourneyMetricsInterceptor.java) and [InstagramMetrics](../../../../../src/main/java/com/instagram/backend/observability/InstagramMetrics.java) record bounded journey outcomes and media-upload sizes.

**Proposed, not implemented:** published SLOs, freshness/durability measurements, burn-rate alerts, dashboards, centralized telemetry storage, and operational runbooks.

## Choices and trade-offs

- Choose user-journey SLIs over host-only metrics for reliability decisions. Infrastructure signals remain essential for diagnosis but cannot prove user success.
- Choose metrics for cheap aggregation and alerting; do not put user IDs or raw URLs in labels. The cost of bounded labels is less per-user detail.
- Choose trace sampling to control volume, but retain errors and unusually slow traces preferentially. Heavy sampling can hide rare failures; full tracing can be expensive.

## Failure drill

**Prediction:** a developer adds `userId` as a metric label and the time-series count explodes. **Symptom and detection:** monitoring memory, ingestion cost, and query latency rise. **Containment:** drop the offending label and temporarily restrict ingestion. **Recovery:** expire or delete excessive series while preserving sampled logs and traces. **Prevention:** enforce a label allowlist, review cardinality in tests, and put high-cardinality identifiers only in logs or traces.

## Two-to-three-minute interview answer

I would start from user journeys, not servers. For login, feed load, post creation, and media upload, I would define what success means and then choose availability, p95/p99 latency, and where necessary freshness or durability indicators. An SLO is a target over a window, and its error budget tells us how much unreliability we can spend before slowing releases or prioritizing reliability work.

For observability, bounded metrics answer how many and how bad, traces show where time went, and structured logs preserve detailed events. They should share route, version, environment, and correlation or trace context without putting user IDs into metric labels. I would alert on error-budget burn rather than every small metric movement and connect alerts to runbooks. In this project, HTTP histograms, correlation logs, journey counters, and optional tracing form a useful baseline, but freshness SLIs, dashboards, and burn-rate alerts remain proposed.

## Practice status

The artifact is ready; comprehension remains unproven until the learner defines one complete journey SLO, explains signal roles, and diagnoses one misleading green dashboard.

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
