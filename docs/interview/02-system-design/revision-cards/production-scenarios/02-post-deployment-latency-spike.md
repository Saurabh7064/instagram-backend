# Post-Deployment Latency Spike — Interview Revision Card

> **Question:** Twenty minutes after a Spring Boot deployment, production latency rises sharply. What do you think is happening, and what should we do next?
>
> [Detailed answer](../../questions/production-scenarios/02-post-deployment-latency-spike.md)

## Key requirements

- Treat the release as a strong hypothesis, not proven causation.
- Limit user impact through a reversible action.
- Preserve evidence and verify recovery.

## Scale assumptions

- Old and new versions overlap long enough to compare similar traffic by version, route, region, and business outcome.

## Components

- A **baseline** is healthy pre-change behavior; a **canary** is a new version on limited traffic; a **feature flag** disables behavior without redeployment.
- Change markers, versioned telemetry, rollback control, and dependency dashboards.

## Request flow

1. Bound impact and align its start with recent changes.
2. Compare versions under equivalent traffic.
3. Trace a slow request and inspect changed code, configuration, queries, or dependencies.
4. Roll back or disable the path when safe.
5. Verify recovery; reproduce and repair offline.

## Data model

- `ReleaseSignal(time, version, route, region, status, durationMs, businessOutcome)`

## Three trade-offs

1. Rollback limits impact but may fail after incompatible data writes.
2. Flags act quickly but add state and cleanup.
3. Longer observation catches delayed failures but exposes users.

## Three failures and mitigations

1. **Incompatible schema:** use expand-contract or compatible roll-forward.
2. **Delayed failure:** gate on queue lag and business freshness.
3. **False correlation:** compare cohorts; reverse one change.

## Two-minute spoken answer

The deployment is my leading hypothesis, but alternatives include traffic growth, cache expiry, scheduled work, or a dependency incident. I bound affected routes, regions, tenants, and versions. Where versions overlap, I compare latency, errors, saturation, downstream calls, and business outcomes under similar traffic. Slow traces may expose a changed query, fan-out, connection wait, or remote call.

If rollback is data-compatible, I stop promotion and roll back; otherwise I disable a flagged path or roll forward. I change one variable, preserve evidence, and watch the same user percentile. No improvement weakens the release hypothesis. After containment, I reproduce the mechanism, add the missing load, contract, or migration test, and tighten canary gates.

## Recall questions

1. What evidence turns deployment correlation into causation?
2. When is rollback unsafe?
3. Why might the spike wait twenty minutes?
