# Production Observability and Debugging — Interview Revision Card

> **Question:** How would you monitor and debug microservices in production?
>
> [Detailed source](../../questions/microservices/02-production-observability-and-debugging.md)

## Key requirements

- Detect user impact quickly.
- Isolate the responsible version, service, dependency, or region.
- Recover safely and preserve evidence.

## Scale assumptions

- At millions of requests, aggregate signals and sampled traces replace manual log reading.

## Components

- A **metric** aggregates numeric behavior, a **trace** times one request's path, and a **structured log** records searchable fields.
- SLO alerts, dashboards, telemetry stores, and change markers.

## Request flow

1. An SLO alert identifies an affected user journey.
2. Split metrics by route, region, and version.
3. Compare healthy and bad traces.
4. At the first abnormal span, inspect logs and dependency saturation.
5. Mitigate; confirm the original user signal recovers.

## Data model

- `RequestSignal(timestamp, service, version, region, route, traceId, status, durationMs, errorType)`

## Three trade-offs

1. More telemetry improves diagnosis but raises cost and privacy risk.
2. Sampling controls volume but may miss rare failures.
3. Request identifiers aid correlation but must not become metric labels.

## Three failures and mitigations

1. **Alert noise:** page on user symptoms; ticket non-urgent trends.
2. **Broken trace:** propagate IDs through HTTP and messages; test continuity.
3. **Telemetry outage:** buffer with limits; never block user traffic.

## Two-minute spoken answer

I start with the user journey: metrics show scope, traces identify the slow or failing hop, and structured logs explain local state. Signals carry bounded dimensions such as route, region, and version; request identifiers stay in logs and traces.

During an incident, I confirm impact and start time, overlay recent changes, and compare healthy and bad cohorts. I trace one bad request to its first abnormal span, then inspect that dependency and its saturation. For serious impact, I roll back, disable a feature, shift traffic, or shed optional work before completing root-cause analysis. I verify recovery using the original user-facing signal. Because detail adds cost and privacy risk, I sample normal traffic, retain errors and slow traces, restrict access, and exclude secrets. Finally, I add the missing test, alert, instrumentation, or runbook step.

## Recall questions

1. Why move from metrics to traces to logs?
2. How would you test whether a deployment caused the incident?
3. What proves that mitigation worked?
