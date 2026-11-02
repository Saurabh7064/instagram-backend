# Post-Deployment Latency Spike

## Question

Your team deployed a new version of a Spring Boot service, and 20 minutes later production latency increased sharply. You suspect the deployment caused it, but you’re not 100% sure.

Your manager asks:

**“What do you think is happening, and what should we do next?”**

> [One-page revision card](../../revision-cards/production-scenarios/02-post-deployment-latency-spike.md)

## Twenty quiet minutes do not clear the release

A deployment ends, everything looks normal, and then latency jumps. The delay makes it easy to argue either side: “It must be the release” or “It would have failed immediately.” Neither conclusion follows from timing alone.

A new path might become expensive only after cached data expires, a queue builds, or connections slowly accumulate. The same delay could come from an unrelated traffic increase or scheduled job. We need a comparison that separates those explanations while keeping users from waiting for a perfect diagnosis.

Pause further rollout, preserve the timeline, and ask what differs between affected and unaffected requests. Containment can be justified by strong evidence before we know the exact faulty line, provided the action itself is safe.

## An unchanged production line is a useful comparison

Imagine a factory replaces one machine and output slows later. Comparing the new line with an unchanged line under the same materials is stronger evidence than comparing Tuesday with Monday. Stopping the new machine may be the safest containment even before the exact defective part is known.

The analogy stops because software versions can share databases, caches, and dependencies. A faulty version may degrade the shared system, so the old version is not always an unaffected control group.

## Use the version still running as a comparison

The **change point** is when the measured behavior shifts. Mark it separately from deployment start and finish; the difference may reveal a threshold or delayed effect.

A **canary** is a small production population on the candidate version. Keeping the old version running gives us a useful comparison under similar traffic, although both versions may still share a database the candidate can overload.

A **rollback** restores the old application. It does not undo committed data or a destructive schema migration. A **feature flag** can disable a behavior without replacing the binary, but only helps if the remaining path still understands the data. Both need a compatibility check before we rely on them.

## Compare versions, then explain their difference

Confirm the affected route, users, region, and latency distribution. Also check failures and completions so “latency improved” cannot mean callers are simply failing faster. Stop additional rollout and unrelated changes while the incident is active.

If old and new versions coexist, compare the same route under comparable traffic. Does the candidate issue more queries, wait longer for a connection, allocate more memory, or call another service more often? Follow one slow candidate trace beside a healthy baseline trace. That comparison tests a mechanism, rather than only noticing nearby timestamps.

Read the complete release change. Code may look harmless while a configuration, dependency, Java runtime, resource limit, feature flag, or database migration changes behavior. Verify the values actually deployed. At the same time, compare traffic, scheduled jobs, database health, cache behavior, and downstream timing with the change point.

If both versions slow together, inspect shared dependencies. That does not automatically clear the candidate: it may be generating enough extra work to slow the shared database for everyone. If impact is serious and old code remains data-compatible, route away from the candidate or roll it back. If compatibility prevents that, disable the path or apply a focused forward fix.

## Follow one plausible delayed regression

Assume a hypothetical deployment where version `v42` adds a repository lookup per feed item, and an existing cache initially absorbs most feed reads. The cache in this example is not implemented in this repository.

1. At 10:00, `v42` receives traffic. Warm cached results hide the extra query work.
2. At 10:20, those entries expire. Database calls per uncached feed request rise, and connections remain occupied longer.
3. Pending borrowers accumulate; feed p95 moves from 250 ms to three seconds even while a brief health check still succeeds.
4. Version and trace comparisons reveal repeated database work in `v42`; pool measurements show the matching wait.
5. After checking data compatibility, the team routes away from `v42`. The database queue drains and latency returns to baseline.
6. The team preserves representative traces, adds a realistic regression/load check, and canaries the corrected path.

The causal explanation is the added work exposed after expiry. Recovery after removing `v42` strengthens it, but queues may need time to drain. A single improved sample immediately after rollback is not enough.

## Interactive incident flow

Use **Next** to rehearse the containment sequence. The diagram is intentionally a decision-order reminder; the detailed evidence and rollback caveats remain in the surrounding answer.

:::simulation
type: request-flow
title: Deployment latency incident sequence
nodes: ["Latency alert", "Pause rollout", "Compare v41 and v42", "Check shared dependencies", "Choose reversible mitigation", "Verify recovery"]
edges: [["Latency alert", "Pause rollout"], ["Pause rollout", "Compare v41 and v42"], ["Compare v41 and v42", "Check shared dependencies"], ["Check shared dependencies", "Choose reversible mitigation"], ["Choose reversible mitigation", "Verify recovery"]]
:::

## Instagram-backend mapping

The service exposes bounded `application`, `environment`, and `version` metric tags and observations through [application.properties](../../../../../src/main/resources/application.properties). The same configuration puts service version into structured ECS logs. [RequestCorrelationFilter.java](../../../../../src/main/java/com/instagram/backend/observability/RequestCorrelationFilter.java) records normalized route, status, and duration for each completed request, while [PostService.java](../../../../../src/main/java/com/instagram/backend/service/PostService.java) times feed loading.

The feed implementation also gives a realistic regression surface: [PostService.java](../../../../../src/main/java/com/instagram/backend/service/PostService.java) maps all posts and checks like state per post. A release that adds another per-item lookup could multiply database work without failing readiness.

**Proposed, not implemented:** a deployment marker on dashboards, simultaneous old/new canary comparison, automated rollback criteria, feature-flag infrastructure, and a query-count performance test. The repository provides version dimensions, but it does not contain a production rollout controller.

## Choose containment the current data can survive

- **Rollback** is fastest when the old binary is data-compatible. Do not choose it after a destructive schema or event change without checking compatibility; rollback can then corrupt behavior.
- **Disable a feature** when one optional path is implicated and the flag is tested. Flags add branching, ownership, and stale-flag risk.
- **Roll forward** when the cause is understood and rollback is unsafe. It avoids incompatible reversal but makes recovery depend on building and validating another release during an incident.

## Rehearse the delayed regression, not only startup

**Prediction:** in the hypothetical `v42` release, the per-item repository lookup stays hidden while the cache is warm. Once entries expire, query count and connection waiting rise before feed p95 reaches three seconds.

**Detect:** compare uncached requests and the same route by version. Look for the extra query shape in traces or database evidence, then align it with cache expiry and pool wait. A passing health check does not establish spare capacity.

**Contain:** pause rollout or disable the expensive path. Route back only after confirming that the old binary can still read and write the current data.

**Recover:** let database and request queues drain. Verify feed percentiles, completions, and dependency work return to baseline instead of judging recovery from the first fast request.

**Prevent:** canary through representative expiry and background cycles, include query cost and connection wait in release checks, and reproduce the mechanism with a focused load or soak test. This is a paper exercise until those actions are actually run.

## Two-to-three-minute interview answer

The deployment is my leading hypothesis, not yet a fact. I would declare the incident, pause the rollout and unrelated changes, and compare the latency change point with the deployment timeline. I would split request rate, errors, p50/p95/p99, resource saturation, database waits, and downstream timing by route, instance, and application version. If old and new versions run together, they provide the strongest comparison. I would also inspect code, configuration, JVM, resource-limit, feature-flag, and migration deltas and check traffic or shared-dependency changes at the same time.

I would not wait for perfect proof while customers suffer. If the new version is clearly worse and the previous version remains data-compatible, I would drain or roll it back, preserve traces and logs, and verify that latency returns after queues drain. If rollback is unsafe because of a schema change, I would disable the suspected feature or roll forward with a narrow fix. After stabilization, I would reproduce the failure, identify the mechanism, add a regression or soak test, and use a canary with automatic latency and saturation guardrails next time.

## Before you practise

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
