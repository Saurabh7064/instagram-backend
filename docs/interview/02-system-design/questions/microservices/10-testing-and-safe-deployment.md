# 10 — Testing and Deploying Microservices Safely

> **Interview question:** How would you test microservices and deploy changes safely without relying on a fragile, slow end-to-end test suite?

- Status: `READY`; comprehension not yet demonstrated
- Time: 25–30 minutes
- Primary idea: test each risk at the cheapest reliable boundary, then limit and observe production exposure

## Why does this matter?

One service can pass all its unit tests and still fail because another service changed a response, a database migration is incompatible with the old binary, a message is delivered twice, or a production dependency is slower than the test substitute.

Running every possible journey through every real service is not the solution. A large end-to-end suite is slow, flaky, difficult to diagnose, and still cannot enumerate all network and rollout states. Safe delivery needs focused tests **and** a deployment strategy that assumes some defects will escape.

## Analogy: rehearsing a play and opening gradually

Actors first practice individual lines, then scenes, then a complete dress rehearsal. The theater does not replace focused practice with one giant rehearsal. A cautious production may have a preview audience before opening every seat.

The analogy has limits. Software versions coexist during rollout, databases persist after rollback, traffic is uneven, and machines can automatically measure and stop a release. The analogy explains layers and limited exposure, not the mechanics.

## Plain-language model

### Contract test

A **contract test** verifies the request, response, or event assumptions between a consumer and provider without starting the entire production topology. It catches interface disagreement early.

### Canary deployment

A **canary deployment** sends a small, controlled portion of production traffic to the new version and compares technical and business health before increasing exposure.

### Expand-contract migration

An **expand-contract migration** changes persistent data in compatible stages: add the new form, run code that tolerates both, migrate data, switch usage, and remove the old form later. It prevents old and new application versions from requiring mutually exclusive schemas during rollout.

## Match the test to the risk

### 1. Unit tests: business rules in isolation

Use them for calculations, validation, state transitions, timeout-budget logic, and mapping. They should be numerous and fast. They do not prove database behavior, serialization, or network integration.

Example: verify a post owner may edit a caption while another user is rejected.

### 2. Component tests: one deployable service with real boundaries where valuable

Start the service, use a real PostgreSQL container, exercise HTTP serialization and persistence, and replace only external services with controlled stubs. This catches mapping, transaction, query, and framework errors while keeping failures attributable to one service.

Example: call `POST /api/posts`, assert the HTTP result, then read the feed from the same running application.

### 3. Contract tests: consumer/provider compatibility

Verify supported status codes, required fields, event schemas, and meaningful examples. Run provider verification before release. Include old supported versions when consumers cannot all upgrade immediately.

Example: the feed consumer requires `PostPublished.postId`, `authorId`, and `occurredAt`; a producer change cannot remove or reinterpret them unnoticed.

### 4. Focused integration tests: real infrastructure behavior

Use real databases, brokers, caches, or object stores selectively for behavior their substitutes cannot prove: transaction boundaries, uniqueness, message redelivery, ordering, or TTL expiry.

Example: deliver the same event twice and prove the feed projection changes once.

### 5. A small end-to-end suite: critical user journeys

Keep a small set for the highest-value paths—register, login, upload media, create post, see it in feed. These prove wiring across deployed components. They should not duplicate every edge case already tested lower down.

### 6. Resilience tests: expected failure behavior

Inject a slow dependency, connection reset, duplicate event, instance termination, or broker outage in a controlled environment. Assert not merely that “an error occurs,” but that deadlines, fallbacks, queues, reconciliation, and alerts behave as designed.

Example: delay profile lookup beyond its timeout and verify feed either uses a documented stale profile projection or returns a bounded partial response instead of exhausting all request threads.

## Predict before reading

Version 2 adds a `NOT NULL` database column, removes the old column, and then begins a rolling deployment. Half the instances are still Version 1. What can happen?

<details>
<summary>Reveal the prediction</summary>

Old instances may continue writing the old shape or fail because their column disappeared. New instances may require a value old instances never write. A rolling deployment therefore needs a schema compatible with both versions: add the new nullable/defaulted form first, deploy dual-compatible code, backfill and verify, switch reads/writes, and remove the old form in a later release.

</details>

## Safe deployment flow

### Before production

1. Build one immutable artifact and identify it by commit plus image digest.
2. Run static checks, unit tests, component tests, provider/consumer contracts, and selected infrastructure integration tests.
3. Review database and event-schema compatibility with both the currently running and new versions.
4. Define release health signals and a rollback/roll-forward decision before deploying.

### During production rollout

1. Deploy the new version with no traffic and wait for startup to finish.
2. A **readiness check** admits traffic only when this instance can serve it. A **liveness check** should restart only a truly stuck process; using a dependency outage as liveness can restart every healthy instance at once.
3. Send a small percentage or one internal cohort to the canary.
4. Compare canary versus baseline by error rate, latency percentiles, saturation, dependency failures, and business outcomes such as successful post creation—not only CPU.
5. Hold long enough to cover meaningful traffic and background work.
6. Increase exposure in stages. Automatically pause or roll back on agreed thresholds.
7. After full rollout, continue watching delayed consumers, scheduled jobs, and data reconciliation.

### Feature flags versus deployment

Deploying code and enabling behavior are different actions. A server-side feature flag can expose a path to staff or 1% of users while retaining fast disablement. Flags need owners, expiry dates, tests for important states, and removal; otherwise they create permanent branching complexity.

### Rollback is not always safe

Application rollback is straightforward only when persistent changes remain backward-compatible. If Version 2 has already written a format Version 1 cannot read, simply redeploying Version 1 makes the incident worse. Prefer an expand-contract design and prepare a roll-forward fix when irreversible data changes occur.

## Concrete release example: idempotent post creation

Suppose the team adds an idempotency key to post creation:

1. Add the idempotency table and unique constraint without changing existing request behavior.
2. Test concurrent duplicate requests against PostgreSQL, not only an in-memory map.
3. Let the API accept the header optionally; old clients still work.
4. Deploy compatible server code behind a flag and verify metrics.
5. Enable one internal client, then a small public cohort.
6. Observe created-post count, key conflicts, duplicate attempts, latency, and database constraint errors.
7. Expand traffic only if technical and user outcomes match the baseline.
8. After supported clients send keys, decide whether and when the header becomes required—normally through a new contract rather than surprising old clients.

This flow tests correctness, compatibility, rollout mechanics, and observability as one change.

## Instagram-backend mapping

[AuthIntegrationTests.java](../../../../../src/test/java/com/instagram/backend/AuthIntegrationTests.java) already starts the Spring application, uses MockMvc at the HTTP boundary, and runs PostgreSQL through Testcontainers. It verifies important flows including registration, authentication, media upload, post creation, update, like, and delete.

That is a strong component/integration base. If this repository becomes multiple services, add:

- focused unit tests around service rules such as those in [PostService.java](../../../../../src/main/java/com/instagram/backend/service/PostService.java);
- consumer/provider contracts for the JSON returned by [PostController.java](../../../../../src/main/java/com/instagram/backend/controller/PostController.java);
- broker integration tests for duplicate delivery and replay if events are introduced;
- a deliberately small browser end-to-end path for the critical user journey;
- release metrics and a canary policy before independent production rollout.

These are proposed next steps; the repository currently deploys as one Spring Boot application.

## Decision and trade-off

| Choice | Prefer it when | Cost or limitation |
|---|---|---|
| More unit/component tests | Logic and service-local behavior can be proven without a full environment | Cannot prove all cross-service wiring |
| Consumer/provider contract tests | Teams deploy independently against a defined interface | Require ownership, version policy, and representative expectations |
| Small end-to-end suite | Proving a few critical deployed journeys | Slow and harder to diagnose; keep scope narrow |
| Canary release | Real traffic can be segmented and compared safely | Needs routing, metrics, enough volume, and automated decisions |
| Blue/green switch | Fast environment-level cutover and rollback are needed | Database and message compatibility still constrain rollback |
| Feature flag | Behavior should be enabled separately from deployment | Flag combinations and cleanup add complexity |

## Failure drill

**Scenario:** canary error rate looks normal, so the release reaches 100%. Two hours later, the background feed consumer begins failing on new events.

1. **Prediction:** request dashboards remain healthy while consumer lag and feed staleness grow.
2. **Observable symptom:** published-post count rises but feed-projection count does not; queue age and dead-letter volume increase.
3. **Immediate recovery:** pause the producer feature or event version, preserve failed messages, deploy a compatible consumer, and replay.
4. **Verification:** reconcile source post IDs against feed projection IDs and measure maximum feed age.
5. **Prevention:** include asynchronous business signals in release gates, test new events against the old consumer, and hold the canary through representative background processing.

## Two-to-three-minute interview-ready answer

“I do not rely on one enormous end-to-end suite. I match each test to the risk at the cheapest reliable boundary: unit tests for business rules; service component tests with real PostgreSQL for HTTP serialization, transactions, and constraints; consumer/provider contracts for API and event compatibility; focused broker or cache tests for redelivery, ordering, and expiry; and a small end-to-end suite for critical journeys such as login, upload, publish, and feed display. I also inject latency, duplicate events, and dependency outages and assert a bounded, observable recovery rather than only an exception.

Before release, I build one immutable artifact identified by commit and image digest. Database and event changes must work with both the currently running version and the candidate. Database migrations use expand-contract: add a compatible schema, deploy code that understands old and new forms, backfill and verify, switch usage, and remove the old form in a later release.

In production, I start the candidate without traffic, verify readiness, and route a small cohort to a canary. I compare it with the baseline using error rate, latency percentiles, resource saturation, dependency failures, consumer lag, dead letters, and business outcomes such as successful post creation. I hold long enough to exercise background processing, then promote in stages with automatic stop thresholds. Feature flags can separate deployment from enablement, but each needs an owner and removal date.

A concrete failure is a producer emitting a new event that the old feed consumer cannot parse. Request metrics may remain green while queue lag grows and feeds become stale. I would pause the producer feature, preserve failed messages, deploy a compatible consumer, replay, and reconcile source versus feed IDs. Simple application rollback is unsafe if the new version already wrote data the old binary cannot read, so I pre-plan either rollback compatibility or a roll-forward fix. The trade-off is more test infrastructure and slower staged delivery, but it sharply limits blast radius and data corruption.”

## Questions and explained answers

<details>
<summary>1. Why not test every scenario end to end?</summary>

End-to-end tests exercise realistic wiring, but they are slow, flaky, combinatorial, and hard to diagnose. Most edge cases are cheaper and clearer in unit or component tests. Keep end-to-end coverage for a small number of critical journeys and use contracts plus focused integration tests for service boundaries.

</details>

<details>
<summary>2. What is the difference between readiness and liveness?</summary>

Readiness answers “should this instance receive traffic now?” Liveness answers “is this process irrecoverably stuck and should it be restarted?” A temporary downstream outage may make an instance unready, but treating it as dead can restart every instance and amplify the incident.

</details>

<details>
<summary>3. Why can a database migration make application rollback unsafe?</summary>

The new version may remove a column or write a representation the old binary cannot understand. Redeploying old code then fails or corrupts data. Expand-contract preserves a period where both versions can operate, and irreversible migrations need a tested roll-forward/recovery plan.

</details>

<details>
<summary>4. Which signals should gate a canary promotion?</summary>

Use technical and business signals: error rate, latency percentiles, resource saturation, dependency failures, queue lag, dead letters, and outcomes such as successful post creation or payment completion. Compare them with the baseline and account for low traffic; “the process is up” is not sufficient evidence.

</details>
