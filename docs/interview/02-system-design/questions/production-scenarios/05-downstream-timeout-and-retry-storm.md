# Downstream Timeout and Retry Storm

## Question

A downstream service becomes slow. Upstream timeouts rise, traffic to that dependency triples, and unrelated endpoints begin slowing down.

Your manager asks:

**“What is happening, and how would you stop the cascading failure?”**

> [One-page revision card](../../revision-cards/production-scenarios/05-downstream-timeout-and-retry-storm.md)

## Why this problem exists

A slow dependency keeps caller threads, sockets, and connections occupied while new requests continue arriving. Callers then time out and retry, which adds work to the already-slow service. If a gateway and two services each retry three times, one user request can produce up to `3 × 3 × 3 = 27` bottom-level attempts.

The goal is not to make every call eventually succeed. It is to keep waiting and retry work bounded, preserve critical paths, and handle uncertain writes safely.

## Analogy: repeatedly pressing an elevator button

When an elevator is delayed, pressing the button ten more times does not create ten elevators. It adds no useful capacity. In a distributed system, retries are worse: each press can start real duplicated work and hold another resource.

The analogy stops because some requests may already have committed before their responses were lost. Retrying a write can therefore duplicate money, posts, or notifications.

## Terms to establish

- A **deadline** is the remaining end-to-end time in which a result is still useful.
- **Backoff with jitter** increases retry delay and randomizes it so clients do not retry together.
- A **circuit breaker** temporarily rejects calls after sustained failure, then permits limited probes for recovery.
- **Idempotency** means repeating one logical request has the same intended effect as processing it once.

## Containment and design sequence

### 1. Prove where amplification begins

Compare incoming user requests with outgoing dependency attempts. Break attempts down by caller, retry number, result, and latency. Align active calls, queues, timeouts, and pool usage across the call chain. A threefold downstream rate with flat user demand is direct retry-amplification evidence.

### 2. Bound time and concurrency

Start from the user deadline and give each downstream hop a smaller timeout. Limit concurrent calls per dependency so one slow service cannot occupy every request worker. When the limit is full, reject or use an explicitly approved fallback instead of building an unlimited queue.

### 3. Assign one retry owner

Retry only transient conditions, at one layer, with a small total attempt budget, backoff, and jitter. Do not retry validation errors or an overloaded dependency blindly. A timeout says “the caller stopped waiting,” not “the callee did nothing.”

### 4. Open and recover the circuit safely

When recent failures cross the policy threshold, stop normal calls and fail fast. After a wait, admit a few half-open probes. Close the circuit only when those real calls meet the recovery rule; avoid synchronized probes from every instance.

## Concrete failure walkthrough

1. Feed Service calls Profile Service with no per-call concurrency limit.
2. Profile latency rises from `50 ms` to `4 s`.
3. Feed times out after one second and retries twice; the gateway also retries.
4. Profile continues executing attempts after callers have timed out.
5. Feed workers and database connections accumulate; unrelated feed work queues.
6. The team disables duplicate retry layers, caps profile calls, and serves a cached display name.
7. Downstream request rate and active calls fall; feed p99 recovers.

The stale display-name fallback is acceptable only if product requirements allow it. Authorization or payment decisions should fail safely rather than use stale identity or correctness data.

## Instagram-backend mapping

This repository is currently one Spring Boot process, so it has no implemented remote Profile Service or circuit breaker. [PostService.java](../../../../../src/main/java/com/instagram/backend/service/PostService.java) calls repositories locally, and [TokenService.java](../../../../../src/main/java/com/instagram/backend/service/TokenService.java) validates tokens locally. That honesty matters: adding resilience libraries now would not protect a network boundary that does not exist.

The existing [HTTP histograms and trace settings](../../../../../src/main/resources/application.properties) and [request correlation filter](../../../../../src/main/java/com/instagram/backend/observability/RequestCorrelationFilter.java) provide a starting signal for future calls.

**Proposed, not implemented:** if profile or media becomes a remote service, propagate deadlines and trace context, record attempt number, use one bounded retry layer, isolate its concurrency, and require idempotency for mutating calls. The deeper model is in [Inter-Service Communication Resilience](../microservices/03-inter-service-communication-resilience.md).

## Choices and trade-offs

- **Retry** when failures are brief, transient, and the operation is safe. Avoid it during sustained overload; it adds latency, load, and duplicate risk.
- **Circuit breaker** when repeated calls are unlikely to succeed and fast failure is useful. Avoid treating it as health discovery alone; bad thresholds can block a recovering dependency.
- **Fallback** when stale or partial data is explicitly acceptable. Do not use it for authorization, balances, or other correctness-critical facts.

## Failure drill

**Prediction:** Profile Service commits an update, but its response is lost and Feed retries. **Symptom and detection:** one operation ID has multiple attempts and the caller timed out while the server recorded success. **Containment:** stop blind retries and query operation status. **Recovery:** return the stored result for the same idempotency key or reconcile the duplicate effect. **Prevention:** persist idempotency outcome atomically, propagate deadlines, and test lost-response behavior.

## Two-to-three-minute interview answer

This is a retry-amplified slow dependency. I would compare user request rate with downstream attempt rate and trace where calls remain active after upstream deadlines. Then I would stop redundant retry layers and optional fan-out. Starting from the user deadline, each hop gets a smaller timeout and a bounded concurrency limit, so a slow dependency cannot consume every worker. When the limit is full, the caller fails quickly or uses an approved stale or partial fallback.

One layer owns a small retry budget for transient failures only, using exponential backoff and jitter. Writes also need an idempotency key because a timeout does not prove failure; the server may have committed and lost the response. A circuit breaker stops normal traffic during sustained failure and later allows limited recovery probes. I would monitor dependency latency, active calls, timeouts, attempts per request, rejections, circuit state, and fallback use. Recovery means user latency improves while downstream attempt rate and concurrency return to safe levels, not merely that errors disappear.

## Practice status

The artifact is `READY`; comprehension remains `TODO`. Mark it practiced only after explaining the 27-attempt example, deciding when a retry is safe, and answering the questions below before opening them.

## Questions and explained answers

<details>
<summary>Why can a longer timeout make availability worse?</summary>

It lets each slow call hold a worker, socket, or connection longer. More work accumulates, queues grow, and unrelated requests can lose access to the same resources. A timeout must come from the user deadline and measured healthy latency.

</details>

<details>
<summary>When is retrying a timed-out write safe?</summary>

Only when the operation is idempotent or the caller can query its outcome. The server may have committed before the response was lost, so a blind retry can duplicate the effect.

</details>

<details>
<summary>What proves a circuit breaker is recovering safely?</summary>

A bounded set of half-open probes completes within the required latency and error thresholds without recreating saturation. Time passing alone is not proof that the dependency recovered.

</details>
