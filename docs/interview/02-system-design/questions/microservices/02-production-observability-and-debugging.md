# Monitoring and Debugging Microservices in Production

> **Question: How would you monitor and debug microservices in production?**

## Why does this problem exist?

In a monolith, one debugger and one stack trace may cover an entire request. In microservices, a single “load feed” request can cross a gateway, feed service, identity service, post store, cache, and message broker. Those components may run on different machines and deploy independently.

A user only sees “the feed is slow,” but the cause could be:

- one slow database query;
- a full connection pool;
- one bad service version;
- retries multiplying traffic;
- a downstream timeout;
- a queue backlog;
- a regional network problem.

Monitoring answers, “Is the system healthy from the user's perspective?” Debugging answers, “Which component and condition caused this specific failure?” A useful production design must support both.

## Analogy: diagnosing a patient in an emergency room

A doctor uses vital signs for a fast health summary, the patient's history for detailed context, and targeted tests to follow a suspected cause. In a distributed system:

- metrics are similar to vital signs;
- logs are similar to the detailed medical record;
- traces are similar to following one substance through several organs.

### Where the analogy breaks

A software request can be copied, retried, queued, or processed twice. Thousands of versions and instances can operate simultaneously, and observing every detail can itself add cost or expose sensitive data. Unlike a single patient, the system needs both aggregate population health and evidence for one request.

## Focus 1 — Use three complementary signals

Three useful terms:

- A **metric** is a numeric measurement aggregated over time, such as request count or the 95th-percentile latency.
- A **structured log** is an event recorded as named fields such as `service`, `requestId`, `postId`, and `errorType`, rather than only free-form text.
- A **distributed trace** records the timed path of one request across services.

No one signal replaces the others.

| Question | Best starting signal | Concrete example |
|---|---|---|
| Is this affecting many users? | Metric | 8% of `GET /api/feed` requests return 5xx |
| Which call is consuming the time? | Trace | 1.8 seconds of a 2-second trace is in `feed -> profile` |
| Why did this instance reject the request? | Structured log | `errorType=ConnectionPoolTimeout`, `dbPoolInUse=20` |

### Why logs alone are not enough

Suppose 300 service instances each write thousands of lines per second. Searching for “timeout” can find unrelated events, and the absence of a log does not prove health. Metrics first show when and where the failure rate changed; a trace identifies the responsible hop; logs then explain that hop in detail.

### What to record for an HTTP service

At minimum, each service should expose or derive:

- request count by route, method, result class, and service version;
- latency distributions by route, not only an average;
- error count by stable error category;
- CPU, memory, thread/worker usage, connection-pool usage, and restart count;
- dependency latency and failure rate for databases, caches, queues, and remote services;
- business outcomes such as successful registrations, posts created, and media uploads completed.

Do not attach unbounded values such as raw user ID, request ID, or full URL to metric labels. Millions of distinct label combinations create excessive storage and query cost. Those high-cardinality identifiers belong in logs and traces.

## Focus 2 — Join evidence across service boundaries

Three useful terms:

- A **trace ID** identifies one end-to-end request as it crosses services.
- A **span** represents one timed operation inside that trace, such as an HTTP call or database query.
- A **correlation ID** is an application-visible identifier used to connect related logs; it may be the trace ID or a separate business/request identifier.

### Concrete request flow

```text
Browser
  │ traceId=7f31
  v
Gateway span: GET /api/feed                  420 ms
  │ forwards trace context
  v
Feed service span                            390 ms
  ├─ PostgreSQL query span                    35 ms
  ├─ Profile service HTTP span               310 ms  <-- slow hop
  │    └─ profile database span              290 ms  <-- likely focus
  └─ response mapping span                    12 ms
```

Each structured log emitted during that request includes `traceId=7f31`. The engineer can move from the alert to a slow trace, then from the slow profile span to only the relevant profile logs.

### Propagation rules

1. The edge accepts or generates trace context.
2. Every service forwards that context on outgoing HTTP and message calls.
3. Every service adds the trace and span IDs to logs automatically.
4. Async producers attach context to message metadata; consumers create a linked processing span.
5. Service name, environment, region, instance, and deployed version are common attributes.

Do not rely on thread-local state without async-aware instrumentation. A request that moves to another thread or is placed on a queue can otherwise lose its context.

### Log enough, but not secrets

Useful fields include:

```text
timestamp, level, service, environment, version, region,
traceId, spanId, route, method, status, durationMs,
errorType, dependency, retryAttempt
```

Do not log passwords, authorization tokens, refresh tokens, complete request bodies by default, or personally identifiable data without an approved need and retention policy. Prefer an internal user reference when investigation requires correlation.

## Focus 3 — Measure what users care about

Three useful terms:

- A **service-level indicator (SLI)** is a measured reliability signal, such as the percentage of feed requests that complete successfully within 500 ms.
- A **service-level objective (SLO)** is the target for that indicator, such as 99.9% over 30 days.
- An **error budget** is the amount of unreliability permitted by the objective; it helps balance release speed against stability work.

### Example

If the SLO is 99.9% successful feed responses during a 30-day window, then up to 0.1% may fail under that definition. The exact SLI must state which requests count and what “successful” means. A fast HTTP 200 containing an empty feed caused by a backend failure should not be counted as a successful business result.

### Alert on symptoms, not every internal fluctuation

A high-value page might say:

> Feed success SLI is consuming the monthly error budget 10 times faster than sustainable for the last 15 minutes.

A low-value page might say:

> One pod used 81% CPU for two minutes.

High CPU is evidence, not necessarily user harm. It belongs on a diagnostic dashboard unless it reliably predicts imminent failure. Pages should require urgent human action; slower capacity or cost trends can create tickets instead.

### Dashboard order

1. **User outcome:** success, latency, and throughput for the journey.
2. **Service boundary:** errors and latency by route, region, and deployed version.
3. **Dependencies:** database, cache, queue, and downstream call behavior.
4. **Resources:** CPU, memory, workers, connections, storage, and restarts.
5. **Change markers:** deployments, configuration changes, and feature-flag changes.

This order prevents an engineer from staring at CPU graphs while the actual user error comes from an expired certificate on a dependency.

## Observability architecture

```text
services and gateway
   ├─ metrics ─────────> metrics store ──> dashboards + alert rules
   ├─ structured logs ─> log store ─────> searchable events
   └─ traces ──────────> trace store ───> end-to-end request timing
                                │
                                v
                    shared service/version/trace attributes

alert ─> incident owner ─> dashboard ─> exemplar/trace ─> related logs
```

An **exemplar** is a link from an unusual metric sample to an example trace. For instance, a latency graph can link directly to one trace from its slow bucket. It shortens the path from “latency is bad” to “this database span consumed 90% of the request.”

## A repeatable production-debugging method

### 1. Confirm and bound the impact

Ask:

- Which user journey is failing?
- When did it start?
- Which regions, tenants, routes, and versions are affected?
- Is the problem total failure, elevated latency, or wrong data?
- Is it still happening?

This prevents a local symptom from being mistaken for a global outage.

### 2. Check recent changes

Overlay deployments, configuration changes, schema migrations, certificate changes, and feature flags on the service dashboard. Correlation is not proof, but a sharp failure beginning with one version is a strong narrowing clue.

### 3. Follow representative traces

Compare one successful trace and one failed or slow trace for the same route. Look for:

- the first error, not merely later propagated errors;
- the span consuming most of the latency;
- unexpected fan-out or repeated retries;
- a missing span, which can reveal broken context propagation or a crash.

### 4. Inspect focused logs

Search the responsible service using trace ID, time range, version, and stable error category. Read surrounding state, but do not infer a global rate from a few logs—that is the metric's job.

### 5. Check dependencies and saturation

If database latency increased, inspect query duration, locks, connection wait time, and pool usage. If remote calls slowed, compare client-side timeout metrics with server-side received traffic. A client timeout with no server span may indicate a connection or routing failure; a completed server span after the client timed out indicates the deadline was too short or the server was too slow.

### 6. Mitigate before perfect diagnosis

Depending on evidence, roll back a version, disable a feature, reduce fan-out, shed nonessential work, route away from a region, or scale a saturated safe component. Record the action and result. Do not make several untracked changes at once; that destroys the ability to learn which one helped.

### 7. Preserve the learning

After recovery, add the missing alert, dashboard, test, capacity threshold, trace attribute, or runbook step. The objective of a post-incident review is to improve the system, not to assign blame.

## Concrete scenario: feed latency after a deployment

**Symptom:** the 95th-percentile latency for `GET /api/feed` rises from 180 ms to 1.7 seconds, but the average changes only slightly.

Why the average hides the problem: if most requests remain fast while a smaller group waits several seconds, fast requests pull the arithmetic average down. A percentile directly states how slow the tail is: the 95th percentile is the duration at or below which 95% of requests finish.

Debugging flow:

1. The SLO alert identifies feed latency in one region.
2. The version breakdown shows only version `v42` is affected.
3. Slow traces contain 30 profile calls per feed; successful `v41` traces contain one batched call.
4. Logs show no individual profile error, which explains why an error-only dashboard remained green.
5. Dependency metrics show a 25-fold increase in profile request traffic.
6. Roll back `v42`; latency and traffic return to baseline.
7. Add a load test and an alert for unexpected downstream calls per feed request.

This example shows why metrics locate the affected population, traces reveal call shape, and logs alone would not have exposed the fan-out pattern quickly.

## Instagram-backend mapping

The project now has an implemented single-service observability baseline. It produces local metrics, structured logs, health signals, and spans; it does **not** yet run the centralized stores shown in the architecture diagram above.

### What is implemented

| Concern | Code or configuration | Actual behavior |
|---|---|---|
| Dependencies | [build.gradle](../../../../../build.gradle) | Adds Spring Boot Actuator, the OpenTelemetry starter, and the Prometheus Micrometer registry. |
| Health and probes | [application.properties](../../../../../src/main/resources/application.properties) | Exposes overall health, liveness, and readiness. `/livez` and `/readyz` are short aliases. Readiness includes the database; liveness deliberately does not, so a database outage does not cause endless application restarts. Health details are hidden. |
| HTTP and trace instrumentation | [application.properties](../../../../../src/main/resources/application.properties) | Records `http.server.requests` with a latency histogram and `100 ms`, `250 ms`, `500 ms`, `1 s`, and `2 s` service-level buckets. The OpenTelemetry bridge accepts W3C `traceparent` context. Normal trace sampling defaults to `10%`; OTLP export is disabled by default. |
| Correlation and request log | [RequestCorrelationFilter.java](../../../../../src/main/java/com/instagram/backend/observability/RequestCorrelationFilter.java) | Accepts a safe `X-Correlation-Id` or generates a UUID, echoes it in the response, puts it in MDC while the request runs, and emits one completion event with method, normalized route, status, and duration. It does not record query strings, request bodies, authorization headers, or tokens. |
| Structured console output | [application.properties](../../../../../src/main/resources/application.properties) | Writes ECS JSON to the console with service name, version, environment, node name, MDC fields, and a bounded stack-trace length. When tracing is active, the tracing integration also puts `traceId` and `spanId` in MDC. |
| Business journey metrics | [InstagramMetrics.java](../../../../../src/main/java/com/instagram/backend/observability/InstagramMetrics.java), [JourneyMetricsInterceptor.java](../../../../../src/main/java/com/instagram/backend/observability/JourneyMetricsInterceptor.java), and [ObservabilityWebConfig.java](../../../../../src/main/java/com/instagram/backend/observability/ObservabilityWebConfig.java) | Records `instagram.business.operations` with only the bounded `operation` and `outcome` labels. Operations are registration, login, feed, post creation, and media upload; outcomes are success or failure. A response below `400` is counted as success. |
| Feed timing | [PostService.feed(...)](../../../../../src/main/java/com/instagram/backend/service/PostService.java) | Wraps feed loading in the `instagram.feed.load` observation with the low-cardinality value `operation=feed`. The observation becomes a timer and, for sampled requests, a child span inside the incoming HTTP trace. |
| Upload size | [MediaStorageService.java](../../../../../src/main/java/com/instagram/backend/service/MediaStorageService.java) | After a file is stored successfully, records `instagram.media.upload.size` in bytes. The only label is bounded to `image`, `video`, or `other`; file names and user IDs are not metric labels. |
| Diagnostic endpoint protection | [ObservabilityEndpointAuthenticationFilter.java](../../../../../src/main/java/com/instagram/backend/observability/ObservabilityEndpointAuthenticationFilter.java) and [ObservabilityManagementPortGuard.java](../../../../../src/main/java/com/instagram/backend/observability/ObservabilityManagementPortGuard.java) | Keeps health endpoints public but requires `X-Observability-Token` for other endpoints below the configured Actuator base path (default `/actuator`). It matches inside any application context path, so `/instagram/actuator/prometheus` cannot bypass the check. Missing or wrong tokens return `401`; a blank server-side token fails closed with `503`; an ambiguous root base path is rejected. A separate management port is rejected at startup because it would create another web context without the filter. The comparison is constant-time. Only `health`, `info`, `metrics`, and `prometheus` are exposed. |
| Executable verification | [ObservabilityIntegrationTests.java](../../../../../src/test/java/com/instagram/backend/ObservabilityIntegrationTests.java), [JourneyMetricsInterceptorTests.java](../../../../../src/test/java/com/instagram/backend/observability/JourneyMetricsInterceptorTests.java), [ObservabilityEndpointAuthenticationFilterTests.java](../../../../../src/test/java/com/instagram/backend/observability/ObservabilityEndpointAuthenticationFilterTests.java), [ObservabilityManagementPortGuardTests.java](../../../../../src/test/java/com/instagram/backend/observability/ObservabilityManagementPortGuardTests.java), and [observability.http](../../../../../http/observability.http) | Tests PostgreSQL-backed health/readiness behavior, endpoint protection including context/base-path/management-port cases, journey and media metric increments, exception outcome classification, correlation-ID rules, the real ECS encoding path, normalized safe logs, and incoming W3C trace context. The HTTP file provides manual probe, scrape, and traced-feed requests. |

Metrics also receive the bounded common tags `application`, `environment`, and `version`. They do not use request ID, user ID, file name, or a raw URL as labels. For example, `/api/posts/1/like` and `/api/posts/999/like` are both observed as the route `/api/posts/{postId}/like` rather than two time series.

### One feed request, step by step

Assume the caller sends:

```text
GET /api/feed
Authorization: Bearer <access-token>
X-Correlation-Id: local-feed-debug-001
traceparent: 00-0af7651916cd43dd8448eb211c80319c-b7ad6b7169203331-01
```

1. The OpenTelemetry HTTP instrumentation reads the W3C `traceparent`. Its trace ID is `0af7651916cd43dd8448eb211c80319c`. Without valid incoming context, it starts new trace context.
2. [RequestCorrelationFilter.java](../../../../../src/main/java/com/instagram/backend/observability/RequestCorrelationFilter.java) validates `local-feed-debug-001`, stores it in MDC, and sets the same value on the response header. A missing value or one containing spaces is replaced by a UUID.
3. Spring maps the request to the normalized route `/api/feed`. The raw access token and query data are not copied into the completion log.
4. [PostService.feed(...)](../../../../../src/main/java/com/instagram/backend/service/PostService.java) runs the `instagram.feed.load` observation around authentication, repository loading, and response mapping. This records the operation duration and can create a child span.
5. On completion, [JourneyMetricsInterceptor.java](../../../../../src/main/java/com/instagram/backend/observability/JourneyMetricsInterceptor.java) increments the feed success or failure counter. Spring's HTTP instrumentation records the route latency and status as well.
6. The request filter writes one ECS JSON completion event. It contains the correlation ID, normalized route, status, and duration; tracing contributes the trace and span IDs through MDC.
7. An authorized monitoring client can scrape the resulting Prometheus text from `/actuator/prometheus`.

The observable result is one response carrying `X-Correlation-Id: local-feed-debug-001`, a structured completion log searchable by both correlation and trace IDs, HTTP latency metrics, one feed outcome count, and one feed-load timing observation.

### Correlation ID versus trace ID in this code

They are intentionally separate:

- `X-Correlation-Id` is an application-facing debugging handle. This project validates it, echoes it to the caller, and writes it to logs. The accepted form is 1–64 safe characters: letters, digits, `.`, `_`, or `-`, beginning with a letter or digit.
- `traceId` belongs to OpenTelemetry trace context. W3C `traceparent` can carry it, and the tracing library controls its lifecycle and span relationships. It is the ID to use when navigating a trace backend once one is connected.

Keeping them separate lets customer support quote a readable correlation ID without allowing it to redefine trace topology. A log for one request can contain both. At present this application has no outbound microservice call, so it does not yet demonstrate forwarding either value to another service.

### Metric names and bounded dimensions

Micrometer code uses dotted names; Prometheus renders those names with underscores and suffixes where appropriate.

| Micrometer name | Meaning | Bounded labels |
|---|---|---|
| `http.server.requests` | HTTP count and latency distribution | normalized URI, method, status, outcome, plus common application metadata |
| `instagram.business.operations` | Completed user journeys | `operation`, `outcome` |
| `instagram.feed.load` | Time spent inside the feed service operation | `operation=feed` |
| `instagram.media.upload.size` | Successfully stored payload sizes | `media.type=image|video|other` |

For example, Prometheus exposes the business counter as `instagram_business_operations_total`. Avoid adding `userId`, `correlationId`, `traceId`, or original file name to these metrics; those values would create unbounded cardinality.

### Try the implemented debugging path

Set `APP_OBSERVABILITY_METRICS_TOKEN` when starting the application. Then compare these requests, or run the saved requests in [observability.http](../../../../../http/observability.http):

```bash
# Public and deliberately brief: expected 200 with {"status":"UP"}.
curl -i http://localhost:8080/actuator/health/readiness

# Protected diagnostic endpoint: expected 401 without the operations token.
curl -i http://localhost:8080/actuator/prometheus

# Expected 200 when this value matches APP_OBSERVABILITY_METRICS_TOKEN.
curl -s \
  -H 'X-Observability-Token: local-observability-token' \
  http://localhost:8080/actuator/prometheus \
  | grep -E 'http_server_requests|instagram_business_operations|instagram_feed_load'

# ACCESS_TOKEN must be a real login access token. Inspect the echoed correlation
# response header, then search the ECS console log for local-feed-debug-001.
curl -i \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H 'X-Correlation-Id: local-feed-debug-001' \
  -H 'traceparent: 00-0af7651916cd43dd8448eb211c80319c-b7ad6b7169203331-01' \
  http://localhost:8080/api/feed
```

The Prometheus scrape verifies that the application is producing metrics; it does not prove that a Prometheus server is collecting or retaining them. Likewise, seeing a `traceId` in a log proves context creation and correlation, not successful export to a trace store.

### Not implemented yet

The boundary matters in an interview and in an incident report:

- There is no Prometheus server, Grafana dashboard, Alertmanager rule, SLO evaluator, or long-term metric retention in this repository.
- There is no centralized log store such as OpenSearch, Elasticsearch, or Loki. ECS JSON currently goes to the process console.
- There is no OpenTelemetry Collector or trace backend such as Tempo or Jaeger. OTLP trace export is disabled by default, so spans stay local unless deployment configuration enables an exporter and supplies a destination.
- There are no outbound service or message-broker calls, so cross-service HTTP propagation, async context propagation, retry spans, and queue-linked traces are not demonstrated.
- Repository calls do not yet create explicit JDBC query spans. The `instagram.feed.load` observation surrounds the whole service operation but cannot by itself identify which SQL statement was slow.
- There are no region or instance dashboard dimensions, tail/error-based trace sampling rules, paging policies, or automated incident runbooks.
- The dedicated header token is a learning baseline, not a complete production perimeter. The application currently rejects a separate management port because its second web context would need its own authentication. A real deployment should add that management-context security together with a private management network, workload identity or stronger authentication, TLS, secret rotation, and access auditing.

The integration tests prove known behavior in a controlled environment. Production telemetry would answer the next layer of questions: which versions, regions, instances, and resource conditions cause behavior to degrade under real load.

## Trade-offs

| Decision | Benefit | Cost or danger |
|---|---|---|
| Record every trace | Maximum request detail | High storage/network cost and possible sensitive-data risk |
| Sample traces | Controls cost | Rare failures can be missed unless errors and unusual latency are retained preferentially |
| Centralize logs | Cross-service search | Storage, retention, access control, and ingestion failure must be managed |
| Add many metric labels | Flexible filtering | Unbounded labels can make the monitoring system expensive or unstable |
| Alert on internal thresholds | May detect an issue early | Creates noise if the threshold has no user impact |
| Alert on SLO symptoms | Aligns urgency with user harm | Requires careful SLI design and may need supporting early-warning signals |

## Failure drill

**Scenario:** users report intermittent `500` responses when liking posts, but no alert fired.

1. **Prediction:** the global error average may be below its threshold even though one version, region, or route is badly affected.
2. **Symptom and detection:** inspect `http.server.requests` for the normalized `/api/posts/{postId}/like` route and `5xx` status. In the current project this requires a live protected scrape; there is no monitoring store or alert history yet. Region is also not a current metric tag.
3. **Focused reproduction:** repeat one like request with a known correlation ID and `traceparent`. Confirm that the response echoes the correlation ID and that the ECS completion event has the normalized route, failure status, trace ID, and correlation ID.
4. **Evidence limit:** an incoming HTTP span can identify the failed request, but the current project has no retained trace backend or JDBC child span. It cannot yet show whether existence checking, insertion, or counter update inside [PostService.like(...)](../../../../../src/main/java/com/instagram/backend/service/PostService.java) consumed the time.
5. **Containment:** if failures began with a known release, roll it back; otherwise stop or isolate the failing dependency or route. Re-run the same correlated request and verify that its status and route-level metric recover.
6. **Recovery:** confirm that new like requests succeed, the `5xx` count stops increasing, database readiness remains `UP`, and the completion log contains no new failure for the reproduced path.
7. **Prevention:** add centralized metric retention and a route-level SLI alert, retain error traces, instrument the relevant database stages, add an integration or concurrency test for the discovered failure, and document the diagnosis in a runbook.

## A 2–3 minute interview answer

“I would design observability around user journeys and make telemetry consistent across every service. I use three complementary signals: metrics show the size and trend of a problem, distributed traces show where one request spent time across services, and structured logs explain the detailed state at the failing component. Every service propagates trace context and records common fields such as service, environment, region, version, normalized route, status, duration, and trace ID. I exclude tokens and sensitive bodies.

For monitoring, I define SLIs and SLOs for user-visible outcomes—for example, successful feed responses under a latency threshold—and page on fast error-budget consumption rather than every CPU spike. Dashboards start with the user journey, then break down by route, region, and version, followed by dependencies and resource saturation. Deployment and configuration markers are shown on the same timeline.

For an incident, I follow a fixed process. I confirm impact and scope, inspect recent changes, compare successful and failing traces, use the slow or erroneous span to focus log searches, then check dependency and resource metrics. I mitigate safely—such as rollback, feature disablement, traffic shift, or load shedding—before chasing a perfect root cause. After recovery I add the missing test, alert, dashboard, instrumentation, or runbook step.

I also control observability cost and risk: normalize metric labels, sample normal traces while retaining errors and slow traces, set retention policies, restrict access, and never log credentials or unnecessary personal data. The key is that an alert must lead quickly from user symptom to a representative trace and then to focused evidence in the responsible service.”

## Questions and explained answers

<details>
<summary>1. What is the difference between metrics, logs, and traces?</summary>

Metrics aggregate numeric behavior and answer “how many, how slow, and how widespread?” Logs record detailed events and state at one component. Traces connect timed operations for one request across components. A practical investigation starts with a metric to scope impact, uses a trace to find the responsible hop, and then uses logs to explain that hop.

</details>

<details>
<summary>2. Why is average latency not enough?</summary>

A small but important set of extremely slow requests can be hidden by many fast requests. If 95 requests take 100 ms and five take 5 seconds, the average does not directly show what those five users experience. Percentiles such as p95 and p99 reveal tail latency, while a histogram preserves the distribution needed to calculate those percentiles correctly across instances.

</details>

<details>
<summary>3. Why should request IDs not be metric labels?</summary>

Each request ID is distinct. Using it as a label creates a new metric time series per request, causing unbounded cardinality, high memory/storage use, and slow queries. Put request or trace IDs in logs and traces; keep metric labels bounded to values such as normalized route, status class, region, and deployed version.

</details>

<details>
<summary>4. A client timed out, but the server log says the operation succeeded. Is either side wrong?</summary>

No. A timeout means the client did not receive a response before its deadline; it does not prove the server failed. The server may have committed just before or after the deadline and lost or delayed the response. Trace timestamps reveal the sequence. Mutating requests therefore need idempotency or a status lookup before blind retry.

</details>

<details>
<summary>5. What makes an alert actionable?</summary>

It identifies meaningful user or reliability impact, has an owner, includes enough scope to start investigation, and points to a dashboard or runbook with a safe mitigation. If no urgent human action is required, the signal should generally be a dashboard, automated response, or ticket rather than a page.

</details>
