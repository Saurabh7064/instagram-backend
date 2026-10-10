# Testing and Safe Deployment — Interview Revision Card

> **Question:** How would you test and deploy microservices without relying on a fragile end-to-end suite?
>
> [Detailed source](../../questions/microservices/10-testing-and-safe-deployment.md)

## Key requirements

- Test each risk at the cheapest reliable boundary.
- Preserve compatibility while versions coexist.
- Limit production blast radius and stop automatically.

## Scale assumptions

- Defects can appear after request metrics look healthy.

## Components

- A **contract test** verifies consumer/provider assumptions; a **canary** exposes limited traffic; **expand-contract** keeps old and new schemas compatible.
- Unit/component/integration tests, immutable artifact, feature flag, release telemetry, and rollback plan.

## Request flow

1. Test logic, real persistence, contracts, and selected failure behavior.
2. Build one immutable artifact identified by commit/digest.
3. Apply backward-compatible schema expansion.
4. Send a small cohort to the canary and compare with baseline.
5. Promote in stages or stop; contract old schema later.

## Data model

- `Release(version, artifactDigest, cohort, startedAt, state)` plus technical and business health signals.

## Three trade-offs

1. Component tests are fast and attributable but miss full wiring.
2. End-to-end tests prove critical journeys but are slow and flaky.
3. Canary release limits exposure but needs traffic, routing, and trustworthy metrics.

## Three failures and mitigations

1. **Old binary rejects schema:** expand-contract migration.
2. **Async consumer fails later:** gate on lag, DLQ, and freshness.
3. **Rollback cannot read new data:** plan compatible rollback or roll-forward.

## Two-minute spoken answer

I match tests to risk: unit tests for rules, component tests with real persistence for service behavior, contract tests for APIs/events, focused broker or cache tests for infrastructure semantics, and a small end-to-end suite for critical journeys. I inject slow dependencies, duplicates, and crashes and assert bounded recovery.

I build one immutable artifact and keep schema changes compatible with both running versions through expand-contract. A small canary receives real traffic and is compared with baseline on errors, percentiles, saturation, dependency health, queue lag, and business outcomes. Promotion proceeds in stages with automatic stop thresholds. Feature flags can separate deployment from enablement. Because rollback may be unsafe after incompatible data writes, I decide rollback versus roll-forward before release. The cost is test and release infrastructure, repaid by smaller blast radius and diagnosable failures.

## Recall questions

1. Why not test every case end to end?
2. Which signals should gate a canary?
3. When is rollback unsafe?
