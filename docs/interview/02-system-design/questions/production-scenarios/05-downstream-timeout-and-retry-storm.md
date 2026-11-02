# Downstream Timeout and Retry Storm

## Question

A downstream service becomes slow. Upstream timeouts rise, traffic to that dependency triples, and unrelated endpoints begin slowing down.

Your manager asks:

**“What is happening, and how would you stop the cascading failure?”**

> [One-page revision card](../../revision-cards/production-scenarios/05-downstream-timeout-and-retry-storm.md)

## The users did not triple. Why did dependency traffic?

Profile normally replies in 50 ms. It begins taking four seconds, but Feed still stops waiting after one second. Feed sends another attempt while the first may still be executing. If more callers do the same thing, Profile receives additional work precisely when it is finishing work more slowly.

A retry can therefore multiply occupied resources without any increase in user demand. Feed workers and sockets remain busy; unrelated work that shares those resources starts waiting too. That spread is a **cascading failure**: the original slowdown causes other components to lose capacity.

Our immediate aim is to stop that multiplication and preserve useful work. Trying indefinitely to make every individual request succeed can make the whole service less available.

## Pressing the elevator button again does not add capacity

When an elevator is delayed, pressing the button ten more times does not create ten elevators. It adds no useful capacity. In a distributed system, retries are worse: each press can start real duplicated work and hold another resource.

The analogy stops because some requests may already have committed before their responses were lost. Retrying a write can therefore duplicate money, posts, or notifications.

## Give the original request one clock and one retry budget

A **deadline** is the point after which the end-to-end result is no longer useful. Each attempt, its connection setup, and the pause before retrying must fit inside the remaining time. Starting a fresh full timeout at every hop discards that boundary.

**Backoff with jitter** means waiting longer between attempts and adding a random variation. Backoff reduces pressure; jitter stops many callers returning together. Neither makes an unsafe write safe to repeat.

**Idempotency** means another delivery of one logical operation applies its intended effect only once. A stable operation key and durable stored result can protect a write whose first response disappeared. A **circuit breaker** stops ordinary calls after enough failure evidence and later admits a limited set of recovery probes. It protects capacity; it cannot tell us whether an earlier write committed.

## Stop amplification before tuning the retry delay

Compare original incoming requests with outgoing Profile attempts. If user demand is flat but attempts per request rise, that is direct amplification evidence. Trace the caller and attempt number, then check how long attempts remain active after upstream timeouts.

Remove redundant retry layers first. Suppose the gateway makes three total attempts, the next service makes three for each gateway attempt, and its client makes three for each of those. One original request can cause `3 × 3 × 3 = 27` bottom-level attempts. Assign one retry owner and a small total budget across the original deadline. Permanent validation or authorization failures should not retry.

Next bound concurrent Profile calls, not only their individual duration. If that limit fills, reject promptly or use an approved reduced response. An unlimited queue merely creates another place for callers to wait and consume memory.

During sustained failure, the circuit breaker can stop normal calls and allow the dependency to recover. After the cooldown, let only a few probes through and check their real latency and outcomes. A timer expiring does not establish recovery, and every instance probing at once can recreate the spike.

## Follow the slow Profile call through its callers

Use the same hypothetical feed request and four-second Profile latency:

1. Feed starts a Profile call. At one second, Feed's timeout expires, but Profile may still be working.
2. If Feed starts two retries as well as the initial call, one feed request can now cause three Profile attempts. A gateway retry layer would multiply that further.
3. More attempts occupy Feed's finite calling capacity, while old Profile work continues after its callers stopped waiting.
4. Unrelated Feed operations wait for shared workers or sockets. This is the visible spread beyond the original dependency.
5. The team removes duplicate retries, caps Profile concurrency, and uses a cached display name only within an approved freshness window.
6. Profile attempts and active calls fall. Feed latency and unrelated route health recover while the dependency is repaired.

A stale display name can be an honest reduced response. A stale authorization decision or guessed payment outcome cannot be substituted on the same reasoning. The product's correctness requirement determines whether a fallback exists.

## Instagram-backend mapping

This repository is currently one Spring Boot process, so it has no implemented remote Profile Service or circuit breaker. [PostService.java](../../../../../src/main/java/com/instagram/backend/service/PostService.java) calls repositories locally, and [TokenService.java](../../../../../src/main/java/com/instagram/backend/service/TokenService.java) validates tokens locally. That honesty matters: adding resilience libraries now would not protect a network boundary that does not exist.

The existing [HTTP histograms and trace settings](../../../../../src/main/resources/application.properties) and [request correlation filter](../../../../../src/main/java/com/instagram/backend/observability/RequestCorrelationFilter.java) provide a starting signal for future calls.

**Proposed, not implemented:** if profile or media becomes a remote service, propagate deadlines and trace context, record attempt number, use one bounded retry layer, isolate its concurrency, and require idempotency for mutating calls. The deeper model is in [Inter-Service Communication Resilience](../microservices/03-inter-service-communication-resilience.md).

## A retry, circuit breaker, and fallback do different jobs

- **Retry** when failures are brief, transient, and the operation is safe. Avoid it during sustained overload; it adds latency, load, and duplicate risk.
- **Circuit breaker** when repeated calls are unlikely to succeed and fast failure is useful. Avoid treating it as health discovery alone; bad thresholds can block a recovering dependency.
- **Fallback** when stale or partial data is explicitly acceptable. Do not use it for authorization, balances, or other correctness-critical facts.

## Rehearse slow success, then a lost response

**Prediction:** a four-second Profile response can exhaust callers even when Profile returns mostly `200`. Feed's timeouts and retries rise before the dependency's error rate tells the story.

**Detect:** compare attempts per original request, active calls, and latency on both sides. A server completion after the caller timed out confirms that abandoned work remained active.

**Contain:** stop blind retries, enforce the Profile concurrency bound, and use the approved display-name fallback or an explicit unavailable result. Protect unrelated work from the same calling pool.

**Recover:** repair Profile, admit limited probes, and restore traffic while checking that attempts per request and active calls remain within their budgets. Discard work whose deadline already expired.

**Prevent:** test both slow successful reads and, for future mutating operations, a commit followed by a lost response. Writes need a durable idempotency result or status lookup; the fact that a read fallback is safe does not make a repeated write safe.

## Two-to-three-minute interview answer

This is a retry-amplified slow dependency. I would compare user request rate with downstream attempt rate and trace where calls remain active after upstream deadlines. Then I would stop redundant retry layers and optional fan-out. Starting from the user deadline, each hop gets a smaller timeout and a bounded concurrency limit, so a slow dependency cannot consume every worker. When the limit is full, the caller fails quickly or uses an approved stale or partial fallback.

One layer owns a small retry budget for transient failures only, using exponential backoff and jitter. Writes also need an idempotency key because a timeout does not prove failure; the server may have committed and lost the response. A circuit breaker stops normal traffic during sustained failure and later allows limited recovery probes. I would monitor dependency latency, active calls, timeouts, attempts per request, rejections, circuit state, and fallback use. Recovery means user latency improves while downstream attempt rate and concurrency return to safe levels, not merely that errors disappear.

## Before you practise

The written scenario is ready for practice; comprehension remains separate. Explain the 27-attempt calculation, decide when a retry is safe, and try the questions below before opening their answers.

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
