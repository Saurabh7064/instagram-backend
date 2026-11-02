# Micro-Lesson 08 — Backpressure and Load Shedding

Core study: 15 minutes. Prerequisite: [Latency, Throughput, and Saturation](../15-advanced-architecture-and-performance/06-latency-throughput-and-saturation.md).

## Two hundred requests arrive; only one hundred finish — Must understand

The feed receives 200 requests each second while its constrained path can finish only 100. Suppose the waiting queue starts empty, these rates remain fixed for 30 seconds, and we ignore discrete arrival timing, retries, cancellations, and failures for the calculation. What happens if we simply keep accepting everything?

Each second adds `200 − 100 = 100` unfinished requests. After 30 seconds, arrivals total `200 × 30 = 6,000` and completions total `100 × 30 = 3,000`. The difference is **3,000 waiting requests** in this simplified model. The queue created waiting, not capacity.

If new arrivals now stop and service stays at 100 per second, draining that backlog takes `3,000 / 100 = 30` seconds. If 200 per second keep arriving, it never catches up under these assumptions. Real queues also have memory costs, caller deadlines, and retries that can make overload worse.

## Decide how much waiting is still useful

A short line can absorb a brief burst. A line that keeps growing turns a capacity shortfall into long latency and timeouts. A **bounded queue** sets a maximum number of waiting items and defines what happens when that space is full.

The bound does not make the 200/100 shortfall vanish. Once full, the service must cause arrivals to slow, reject some work, or reduce how much work each accepted request needs. The correct response depends on whether the caller can slow down and which work remains valuable after waiting.

Think of a venue with limited safe occupancy and a short entrance line. Once both are full, admission stops. Software has a further complication: rejected clients can immediately retry and crowd the entrance again, so the response and retry contract are part of the control.

## Slow the sender, or decline work locally

**Backpressure** is a signal or control that makes upstream senders slow down when downstream capacity is full. It works only when the upstream side can and does respond to it. Sending a retry delay that clients ignore does not reduce arrivals.

**Load shedding** deliberately rejects or disables work to preserve useful service during overload. That can mean refusing excess feed requests or pausing optional enrichment. Merely moving everything into another unlimited queue postpones the same problem.

Choose the admission limit to protect the actual constrained dependency. A backend allowing far more concurrent queries than PostgreSQL can use safely has bounded the wrong resource at the wrong level. Reserve capacity for critical paths only when the product and dependency layout make that reservation effective.

## Follow the next request after the line fills

```text
Client
  → admission check
      ├─ capacity available → bounded active work → PostgreSQL → response
      ├─ short queue space  → wait briefly ──────────┘
      └─ no safe capacity   → reject or degrade clearly
```

An arrival with available active capacity starts work. If only short waiting space remains, it waits within the permitted deadline. When neither exists, the service returns its explicit overload response or a defined reduced result rather than allowing an unbounded wait.

Clients then need a delay and a bounded retry policy. As active requests finish, waiting requests move forward. Admission can reopen when capacity recovers. Adding instances helps only if their work can run in parallel and the shared dependency has headroom; otherwise they add more callers to the same bottleneck.

## Instagram-backend mapping

The current [PostController.java](../../../../../src/main/java/com/instagram/backend/controller/PostController.java) accepts synchronous requests, while [PostService.java](../../../../../src/main/java/com/instagram/backend/service/PostService.java) performs database work. The feed reads all posts, so its cost grows with stored data.

[application.properties](../../../../../src/main/resources/application.properties) configures latency buckets and database-aware readiness. [JourneyMetricsInterceptor.java](../../../../../src/main/java/com/instagram/backend/observability/JourneyMetricsInterceptor.java) records business outcomes. These provide some evidence about latency and dependency readiness, but do not implement admission control. A shared-database overload also needs demand control: marking every dependent instance unready provides no alternative serving path, and database-aware probes can themselves compete for connections.

A gateway limit, maximum active feed requests, cursor pagination, reserved authentication capacity, and an overload response are **proposed**. Any future media-processing queue should also be bounded.

## Choose the control the caller and product can support

| Choice | Choose it when | Cost or limitation |
|---|---|---|
| Small bounded queue | Brief bursts are common and work remains valuable after a short wait | Adds some latency and needs a clear full-queue policy |
| Backpressure to callers | Upstream can reduce its production or retry rate | Requires a contract callers actually honor |
| Shed optional work | Critical journeys must survive overload | Users receive reduced functionality or explicit rejection |
| Add instances | Work is parallel and the downstream dependency has capacity | Startup is not instant and shared bottlenecks remain |

Priority follows business correctness. Omitting recommendation enrichment may be safe; dropping an accepted payment is not. Acknowledge durable work only after it reaches storage that survives process failure.

## Every rejected client immediately tries three more times — paper exercise

The service rejects excess feed requests, but each client retries immediately three times. Predict whether the original 200-per-second user demand still describes the offered request rate.

It does not: repeated attempts add requests without adding new user intent. A high rejection count, an offered rate above user demand, and rising accepted-request latency reveal the retry feedback loop. Cap attempts, tell callers how long to wait, spread retry timing, and pause optional enrichment where safe.

Allow active and waiting work to drain, then reopen admission gradually while checking latency and saturation. Test this client behavior before release and document what an overload response means for whether work was accepted. Acknowledged durable work must not simply be dropped later; priorities and degraded responses must preserve the product's correctness contract.

## Explain where the 3,000 waiting requests came from

In 60–90 seconds, use the `200 in / 100 out` example to explain why an unlimited queue cannot solve overload, then describe what happens when the bounded queue fills.

## Before moving on

Artifact: `READY`; comprehension not yet demonstrated.

Proceed only when you can:

- calculate the queue growth in the prediction;
- distinguish controlling upstream demand from rejecting work locally;
- justify which Instagram work should degrade first;
- explain why adding instances may not protect PostgreSQL.

## Questions and explained answers

<details>
<summary>1. When is a queue useful instead of harmful?</summary>

A small queue can absorb a short burst when work remains valuable after waiting. It becomes harmful when it hides sustained overload or waits beyond the caller's deadline. Bound its size and useful waiting time.

</details>

<details>
<summary>2. Why is early rejection sometimes more reliable than accepting every request?</summary>

Early rejection preserves workers, memory, connections, and dependencies for work the service can finish. Accepting everything can make all requests time out. Rejection must be explicit so callers know work was not accepted.

</details>

<details>
<summary>3. Why is autoscaling not a complete overload strategy?</summary>

Instances take time to start, and each may increase pressure on the same database. Admission control remains necessary while capacity changes or a shared dependency is the bottleneck.

</details>
