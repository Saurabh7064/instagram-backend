# Post-Deployment Latency Spike

## Question

Your team deployed a new version of a Spring Boot service, and 20 minutes later production latency increased sharply. You suspect the deployment caused it, but you’re not 100% sure.

Your manager asks:

**“What do you think is happening, and what should we do next?”**

> [One-page revision card](../../revision-cards/production-scenarios/02-post-deployment-latency-spike.md)

## Why this problem exists

A deployment and a latency spike close together are correlated, but correlation is not proof. The release may have added database calls, changed a query plan, leaked connections, or increased allocation until garbage collection became expensive. The twenty-minute delay could also be cache expiry, traffic growth, a downstream incident, or a scheduled job unrelated to the release.

The operational goal is twofold: reduce customer harm quickly and collect enough comparative evidence to avoid both a needless rollback and a dangerous wait for certainty.

## Analogy: two production lines

Imagine a factory replaces one machine and output slows later. Comparing the new line with an unchanged line under the same materials is stronger evidence than comparing Tuesday with Monday. Stopping the new machine may be the safest containment even before the exact defective part is known.

The analogy stops because software versions can share databases, caches, and dependencies. A faulty version may degrade the shared system, so the old version is not always an unaffected control group.

## Terms to establish

- A **change point** is the time a metric’s behavior shifts materially.
- A **canary** is a small production population receiving the new version before broad rollout.
- A **rollback** restores the previous application version; it does not automatically reverse data or schema changes.
- A **feature flag** changes behavior without replacing the deployed binary.

## Investigation and decision sequence

### 1. Declare and bound the incident

Confirm which user journeys, routes, regions, instances, and percentiles are affected. Check errors, timeouts, throughput, and saturation as well as latency. Record the deployment start/end time and the observed change point. Pause further rollout and unrelated changes so the evidence remains interpretable.

### 2. Compare new and old populations

If both versions still serve traffic, compare identical route metrics by version and instance. Ask whether the new version alone has higher latency, query count, allocation rate, garbage-collection pauses, connection wait, or downstream-call volume. Compare slow traces from both versions. This is the fastest way to strengthen or weaken the deployment hypothesis.

### 3. Inspect the complete release delta

Review code, configuration, dependency, JVM, container-resource, feature-flag, and database-migration changes. Confirm that production actually received the intended values. A harmless code diff can ship with a smaller connection pool or CPU limit.

### 4. Check competing explanations

Overlay traffic, database health, cache hit rate, downstream latency, and scheduled workload on the same timeline. If old and new versions degrade simultaneously, investigate shared dependencies—but remember that the new version may be overloading them.

### 5. Choose a reversible mitigation

Roll back or route traffic away from the new version when customer impact is serious and rollback is known to be data-compatible. Disable the suspected code path with a feature flag when that is safer. If an irreversible migration makes rollback unsafe, roll forward with a focused fix or isolate the affected route.

## Concrete event walkthrough

Assume version `v42` adds an extra repository lookup while building each feed item.

1. At 10:00, `v42` receives all traffic. Cache warmth initially hides the extra cost.
2. At 10:20, cache entries expire and database query volume per feed request rises.
3. Database connections become busy; p95 feed latency rises from 250 ms to 3 seconds while health checks still pass.
4. Metrics grouped by `version` show `v42` slowing first; traces show repeated database work.
5. The team confirms the previous version is compatible with current data and rolls back.
6. Latency and connection wait return to baseline, strengthening the causal claim.
7. The team preserves traces, adds a regression test and query-count guard, then prepares a canary release.

The rollback is both mitigation and an experiment: recovery after traffic leaves `v42` is evidence, though shared-system recovery may lag while queues drain.

## Instagram-backend mapping

The service exposes bounded `application`, `environment`, and `version` metric tags and observations through [application.properties](../../../../../src/main/resources/application.properties). The same configuration puts service version into structured ECS logs. [RequestCorrelationFilter.java](../../../../../src/main/java/com/instagram/backend/observability/RequestCorrelationFilter.java) records normalized route, status, and duration for each completed request, while [PostService.java](../../../../../src/main/java/com/instagram/backend/service/PostService.java) times feed loading.

The feed implementation also gives a realistic regression surface: [PostService.java](../../../../../src/main/java/com/instagram/backend/service/PostService.java) maps all posts and checks like state per post. A release that adds another per-item lookup could multiply database work without failing readiness.

**Proposed, not implemented:** a deployment marker on dashboards, simultaneous old/new canary comparison, automated rollback criteria, feature-flag infrastructure, and a query-count performance test. The repository provides version dimensions, but it does not contain a production rollout controller.

## Choices and trade-offs

- **Rollback** is fastest when the old binary is data-compatible. Do not choose it after a destructive schema or event change without checking compatibility; rollback can then corrupt behavior.
- **Disable a feature** when one optional path is implicated and the flag is tested. Flags add branching, ownership, and stale-flag risk.
- **Roll forward** when the cause is understood and rollback is unsafe. It avoids incompatible reversal but makes recovery depend on building and validating another release during an incident.

## Failure drill

**Prediction:** a new release slowly leaks database connections, so latency rises after twenty minutes. **Symptom and detection:** only new-version instances show increasing active connections and acquisition wait before timeouts begin. **Containment:** stop rollout and remove those instances from traffic. **Recovery:** roll back after verifying schema compatibility, wait for queues and sessions to drain, and confirm percentiles recover. **Prevention:** canary on latency and pool-wait thresholds, load-soak the release, and make connection ownership observable.

## Two-to-three-minute interview answer

The deployment is my leading hypothesis, not yet a fact. I would declare the incident, pause the rollout and unrelated changes, and compare the latency change point with the deployment timeline. I would split request rate, errors, p50/p95/p99, resource saturation, database waits, and downstream timing by route, instance, and application version. If old and new versions run together, they provide the strongest comparison. I would also inspect code, configuration, JVM, resource-limit, feature-flag, and migration deltas and check traffic or shared-dependency changes at the same time.

I would not wait for perfect proof while customers suffer. If the new version is clearly worse and the previous version remains data-compatible, I would drain or roll it back, preserve traces and logs, and verify that latency returns after queues drain. If rollback is unsafe because of a schema change, I would disable the suspected feature or roll forward with a narrow fix. After stabilization, I would reproduce the failure, identify the mechanism, add a regression or soak test, and use a canary with automatic latency and saturation guardrails next time.

## Practice status

The artifact is ready; practice requires more than reading it. Mark it practiced only after explaining the decision without notes, identifying one reason rollback could be unsafe, predicting a delayed-release failure, and answering the final questions before revealing them.

## Questions and explained answers

<details>
<summary>Why does a twenty-minute delay not rule out the deployment?</summary>

Some release defects need state to accumulate: caches expire, queues grow, connections leak, memory fills, or garbage collection changes. The delay is evidence to explain, not evidence of innocence.
</details>

<details>
<summary>When is rollback the wrong immediate action?</summary>

Rollback is unsafe when the old binary cannot understand a completed schema, data, or contract change. Verify compatibility first; otherwise disable a feature, isolate traffic, or roll forward safely.
</details>

<details>
<summary>What observation most strongly links the new version to the latency increase?</summary>

Under comparable traffic, new-version instances degrade while old-version instances remain healthy, and routing traffic away causes recovery. Traces or resource metrics should then identify the mechanism rather than relying only on timing.
</details>
