# 12 - Production Observability Baseline

## 1) What I built

Added a backend observability baseline that connects logs, metrics, traces, and health signals around the same request:

- Spring Boot Actuator health and diagnostic endpoints.
- Public, detail-safe health, liveness, and readiness probes.
- Prometheus-format HTTP, JVM, business-journey, feed, and media-upload metrics.
- OpenTelemetry-compatible tracing with W3C `traceparent` propagation.
- ECS JSON console logs containing trace context and a validated `X-Correlation-Id`.
- A local feed observation named `instagram.feed.load`.
- A dedicated operations token for non-health Actuator endpoints.
- PostgreSQL-backed integration tests and a repeatable HTTP request file.

This is an in-process instrumentation baseline. A Prometheus server, dashboard, alert manager, centralized log store, OpenTelemetry Collector, and trace backend are not deployed yet.

## 2) Concepts learned

- **Logs:** structured events answer what happened during one request. The completion event uses the normalized route template instead of the raw URL and does not record request bodies, authorization headers, or user identifiers.
- **Metrics:** numeric measurements reveal trends across many requests. Tag values are selected from bounded enums so a user ID, file name, or arbitrary URL cannot create an unbounded number of time series.
- **Traces:** a W3C trace ID follows the technical call path, while the optional correlation ID is a human-friendly support reference. They are related search keys, not interchangeable identifiers.
- **Liveness versus readiness:** liveness asks whether the process should be restarted; readiness asks whether it should receive traffic and includes database health.
- **Instrumentation versus observability backend:** the application now emits useful signals, but storing, querying, graphing, and alerting on those signals requires external systems.

## 3) Why this design

- Health endpoints remain public so an orchestrator can probe the process, but component details are hidden to avoid exposing internal topology.
- `/actuator/metrics`, `/actuator/prometheus`, and the other exposed diagnostic endpoints require a separate `X-Observability-Token`. The filter follows the configured Actuator base path and ignores the application context-path prefix, so changing either cannot bypass the check. A blank configured token fails closed with `503`; a missing or wrong token returns `401`. A root Actuator base path is rejected as ambiguous. A separate `management.server.port` is rejected at startup because it would create a second web context without this filter.
- Correlation IDs are accepted only when they match `[A-Za-z0-9][A-Za-z0-9._-]{0,63}`. Invalid values are replaced with a generated UUID, preventing control characters or attacker-controlled text from being injected into logs.
- Request metrics and logs use Spring's normalized route pattern, such as `/api/posts/{postId}/like`, instead of a concrete post ID. This keeps metric cardinality bounded and prevents identifiers from leaking into completion logs.
- Business counters use fixed `operation` and `outcome` values. Media upload sizes use only `image`, `video`, or `other` as the media-type tag.
- OTLP export is disabled by default. Local development therefore does not depend on a collector, while a later deployment can explicitly enable export and provide a collector endpoint.

The dedicated Actuator token is a useful project-level control, not the complete production security boundary. A real deployment should also isolate the management network, store the token in a secret manager, rotate it, and restrict access at the gateway or platform layer.

## 4) Implementation notes

- **Request flow:** Spring creates or continues the HTTP trace -> the correlation filter validates or generates `X-Correlation-Id` -> the controller and service execute -> the journey interceptor records a bounded success/failure metric -> the correlation filter writes one structured completion event and echoes the correlation ID.
- **Feed observation:** `PostService.feed` records the local `instagram.feed.load` observation with the low-cardinality `operation=feed` tag. With tracing enabled, it can become a child span inside the HTTP request trace; it also produces a timer locally.
- **Business metrics:** registration, login, feed load, post creation, and media upload record success/failure counters. Successful media storage also records payload size by the bounded media category.
- **Probe behavior:** `/actuator/health`, its liveness/readiness groups, `/livez`, and `/readyz` are public. Health details remain hidden. Readiness includes the database; liveness deliberately does not, so a temporary database failure does not automatically restart every application instance.
- **Diagnostic behavior:** only `health`, `info`, `metrics`, and `prometheus` are exposed. Non-health Actuator requests require the operations token; an application access token does not replace it.
- **Logging:** the default console format is ECS JSON and includes Spring tracing MDC values such as `traceId`/`spanId`, plus the validated `correlationId` while a request is active.
- **Verification:** the integration test runs against PostgreSQL through Testcontainers and checks safe probes, readiness failure state, diagnostic authentication (including an application context path), bounded journey/media metrics, the feed observation, correlation lifecycle, normalized safe logs through the real ECS encoder, and incoming W3C trace context. Focused tests also cover configurable Actuator base paths and exception outcome classification. The HTTP file gives repeatable local requests for probes, protected metrics, correlation, and trace propagation.
- **UI:** no UI flow changed, so browser-demo screenshots are not required for this backend-only feature.

### Verification evidence

- `./gradlew test --rerun-tasks` passed the full unit and integration suite, including PostgreSQL Testcontainers tests.
- A local H2-backed application was started on port `18082` with `APP_OBSERVABILITY_METRICS_TOKEN=local-observability-token` for manual API verification.
- `GET /actuator/health`, `/actuator/health/liveness`, and `/actuator/health/readiness` each returned `200` and `UP`, without component details.
- `GET /actuator/prometheus` returned `401` without the operations token and `200` with it.
- Login followed by a correlated `GET /api/feed` returned `200` and echoed `X-Correlation-Id: manual-feed-check-001`.
- The protected scrape showed feed/login success counts of `1`, `instagram_feed_load_seconds_count` of `1`, and the three pre-registered bounded media categories.
- The ECS console event contained `traceId=0af7651916cd43dd8448eb211c80319c`, the correlation ID, `route=/api/feed`, and `status=200`; it contained no bearer token.
- A startup attempt with `server.port=18083` and `management.server.port=19083` failed closed with the guard's explicit unsupported-separate-port error before either endpoint became available.

## 5) Code pointers

### Dependencies and configuration

- [build.gradle](../../build.gradle)
- [application.properties](../../src/main/resources/application.properties)
- [test application.properties](../../src/test/resources/application.properties)

### Logs, metrics, traces, and endpoint protection

- [RequestCorrelationFilter.java](../../src/main/java/com/instagram/backend/observability/RequestCorrelationFilter.java)
- [InstagramMetrics.java](../../src/main/java/com/instagram/backend/observability/InstagramMetrics.java)
- [JourneyMetricsInterceptor.java](../../src/main/java/com/instagram/backend/observability/JourneyMetricsInterceptor.java)
- [ObservabilityWebConfig.java](../../src/main/java/com/instagram/backend/observability/ObservabilityWebConfig.java)
- [ObservabilityEndpointAuthenticationFilter.java](../../src/main/java/com/instagram/backend/observability/ObservabilityEndpointAuthenticationFilter.java)
- [ObservabilityManagementPortGuard.java](../../src/main/java/com/instagram/backend/observability/ObservabilityManagementPortGuard.java)
- [PostService.java](../../src/main/java/com/instagram/backend/service/PostService.java)
- [MediaStorageService.java](../../src/main/java/com/instagram/backend/service/MediaStorageService.java)

### Verification and interview mapping

- [ObservabilityIntegrationTests.java](../../src/test/java/com/instagram/backend/ObservabilityIntegrationTests.java)
- [JourneyMetricsInterceptorTests.java](../../src/test/java/com/instagram/backend/observability/JourneyMetricsInterceptorTests.java)
- [ObservabilityEndpointAuthenticationFilterTests.java](../../src/test/java/com/instagram/backend/observability/ObservabilityEndpointAuthenticationFilterTests.java)
- [ObservabilityManagementPortGuardTests.java](../../src/test/java/com/instagram/backend/observability/ObservabilityManagementPortGuardTests.java)
- [observability.http](../../http/observability.http)
- [Production observability and debugging interview answer](../interview/02-system-design/questions/microservices/02-production-observability-and-debugging.md)

## 6) Commit pointers

- Backend commit: `pending this change`
- UI commit: not applicable; this change is backend-only.

## 7) Pitfalls and fixes

- **Problem:** a correlation ID and a trace ID can look like duplicate concepts.
  **Fix:** keep the trace ID as the standards-based technical path identifier and the validated correlation ID as an optional support-friendly lookup key; include both in structured request logs.
- **Problem:** tagging metrics with user IDs, concrete paths, file names, or exception messages can exhaust monitoring storage.
  **Fix:** use route templates and enum-backed operation, outcome, and media-category values only.
- **Problem:** test resources override main application resources, so management settings in the main properties file were not automatically present in integration tests.
  **Fix:** declare the required probe, exposure, export, and structured-logging settings explicitly in the test properties file.
- **Problem:** adding the OpenTelemetry starter can activate exporters that expect external infrastructure.
  **Fix:** disable OTLP trace, metric, and log export by default and enable each one deliberately in a deployed environment.
- **Problem:** exposing all Actuator endpoints can leak configuration or operational details.
  **Fix:** expose only the required endpoint set, hide health details, keep safe probes public, and require a dedicated token for the remaining diagnostics.
- **Problem:** recording `correlationId` in both MDC and the structured event caused Spring Boot's ECS encoder to reject the entire request-completion log because it contained a duplicate JSON member. An in-memory appender did not expose the encoder failure.
  **Fix:** keep `correlationId` in MDC only and encode the captured event with the real `StructuredLogEncoder` in the integration test.
- **Problem:** matching diagnostics with the raw request URI allowed an application context path, such as `/instagram`, to hide the `/actuator` prefix from the authentication check. A configurable management base path created the same risk.
  **Fix:** match the path inside the application, inject and normalize `management.endpoints.web.base-path`, reject a root base path, and test context-path and custom-base-path cases.
- **Problem:** setting `management.server.port` creates a separate Spring web context where a main-context servlet filter is not registered, which would expose the diagnostic endpoint without the header check.
  **Fix:** fail startup when a separate management port is configured. Supporting one later requires authentication registered in the management context plus network isolation.
- **Problem:** an unhandled exception can reach `afterCompletion` before the response status changes from `200` to `500`.
  **Fix:** classify a journey as failed when the interceptor receives an exception, even if the current response status is still below `400`.

## 8) Next improvements

- [ ] Deploy Prometheus, Grafana, and Alertmanager, then define latency/error-rate dashboards and SLO burn alerts.
- [ ] Send ECS logs to a centralized log platform with retention and access controls.
- [ ] Deploy an OpenTelemetry Collector and a trace backend; configure authenticated OTLP export.
- [ ] Add propagation tests across a real downstream service and asynchronous message boundary.
- [ ] Add database/query instrumentation only after reviewing data exposure and overhead.
- [ ] Add management-context authentication, network isolation, and managed secrets before allowing a separate management port or replacing the startup guard.
