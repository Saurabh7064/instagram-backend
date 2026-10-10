# Micro-Lesson 08 — Backpressure and Load Shedding

- Status: `READY`; comprehension not yet demonstrated
- Time: 15 minutes
- Primary idea: when demand exceeds sustainable capacity, deliberately limit accepted work so the system keeps a bounded, useful service
- New terms: bounded queue, backpressure, load shedding
- Prerequisite: [Latency, Throughput, and Saturation](./06-latency-throughput-and-saturation.md)

## Why does this exist?

Every service has a maximum sustainable rate. If requests arrive faster than they finish, excess work waits somewhere. An unlimited line does not create capacity; it converts overload into latency, memory use, timeouts, and wider failure. A resilient service limits active and waiting work, then handles the rest explicitly.

## Analogy: a venue with a safe occupancy limit

A venue admits only as many people as it can serve safely. A short line absorbs a brief rush. When both are full, staff stop admission and preserve space for critical visitors.

The analogy stops matching because clients may retry automatically, one request can fan out, and rejected writes have correctness consequences. Outcomes and retries must be explicit.

## Plain-language model

- A **bounded queue** has a fixed maximum number of waiting items. Once full, it applies an explicit overflow policy instead of growing without limit.
- **Backpressure** is a signal or control that makes upstream senders slow down when downstream capacity is full.
- **Load shedding** deliberately rejects, delays, or disables lower-priority work to preserve critical service during overload.

These controls do not add capacity. They bound latency and resource use while the system recovers.

## Predict before reading

A feed service receives `200 requests/second` but can complete only `100 requests/second`. Its waiting queue is unlimited. What happens after 30 seconds if the rates do not change?

<details>
<summary>Reveal the prediction</summary>

The queue grows by `100 requests/second`, so about `3,000` requests wait after 30 seconds. Draining them takes at least another 30 seconds if new traffic stops. If traffic continues, the queue never catches up, and client retries can add more work.

</details>

## Concrete overload flow

```text
Client
  → admission check
      ├─ capacity available → bounded active work → PostgreSQL → response
      ├─ short queue space  → wait briefly ──────────┘
      └─ no safe capacity   → reject or degrade clearly
```

1. A traffic spike reaches the service.
2. The service admits work only up to a measured safe maximum.
3. A small queue absorbs a short burst.
4. Once full, new work receives an explicit response instead of waiting for a timeout.
5. Critical operations can receive reserved capacity while optional enrichment or background work is paused.
6. Clients wait before a bounded retry.
7. As active work completes, the queue drains and normal admission resumes.

The limit should protect the actual constrained dependency. A generous application limit does not help if PostgreSQL can safely run only a much smaller number of concurrent queries.

## Instagram-backend mapping

The current [PostController.java](../../../../src/main/java/com/instagram/backend/controller/PostController.java) accepts synchronous requests, while [PostService.java](../../../../src/main/java/com/instagram/backend/service/PostService.java) performs database work. The feed reads all posts, so its cost grows with stored data.

[application.properties](../../../../src/main/resources/application.properties) configures latency buckets and database-aware readiness. [JourneyMetricsInterceptor.java](../../../../src/main/java/com/instagram/backend/observability/JourneyMetricsInterceptor.java) records business outcomes. These detect overload but do not limit admission.

A gateway limit, maximum active feed requests, cursor pagination, reserved authentication capacity, and an overload response are **proposed**. Any future media-processing queue should also be bounded.

## Decision and trade-off

| Choice | Choose it when | Cost or limitation |
|---|---|---|
| Small bounded queue | Brief bursts are common and work remains valuable after a short wait | Adds some latency and needs a clear full-queue policy |
| Backpressure to callers | Upstream can reduce its production or retry rate | Requires a contract callers actually honor |
| Shed optional work | Critical journeys must survive overload | Users receive reduced functionality or explicit rejection |
| Add instances | Work is parallel and the downstream dependency has capacity | Startup is not instant and shared bottlenecks remain |

Priority follows business correctness. Omitting recommendation enrichment may be safe; dropping an accepted payment is not. Acknowledge durable work only after it reaches storage that survives process failure.

## Failure drill

**Scenario:** the service starts rejecting excess feed requests correctly, but every client retries immediately three times.

1. **Prediction:** one rejected request becomes several new requests, so overload persists or worsens.
2. **Observable symptom:** request rate jumps above user demand, rejection count stays high, and accepted-request latency also increases.
3. **Containment:** cap retry attempts, tell clients to wait, and temporarily disable nonessential feed enrichment.
4. **Recovery:** allow active and queued work to drain, then reopen admission gradually while watching latency and saturation.
5. **Prevention:** publish retry behavior in the contract, spread retry timing, test overload before release, and reserve capacity for critical operations.

## Teach-back

In 60–90 seconds, use the `200 in / 100 out` example to explain why an unlimited queue cannot solve overload, then describe what happens when the bounded queue fills.

## Stop/go

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
