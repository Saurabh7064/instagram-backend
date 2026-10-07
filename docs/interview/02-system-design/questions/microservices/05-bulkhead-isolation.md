# Explain bulkhead isolation

> **Question:** Explain bulkhead isolation.

## Why does this pattern exist?

A service often handles critical and non-critical work with the same finite resources: request threads, database connections, memory, CPU, outbound sockets, or queue workers. If one slow dependency consumes all of a shared resource, unrelated operations fail even though their own code and dependencies are healthy.

Bulkhead isolation limits how much of a resource one workload or dependency may consume. The goal is not to stop that workload from failing. The goal is to stop its failure from sinking every other workload.

## Analogy: watertight compartments in a ship

A ship is divided into sealed compartments. If one compartment floods, closed barriers limit the water to that compartment, so the rest of the ship can remain afloat.

In software:

- a compartment is a bounded resource pool or concurrency limit;
- flooding is slow, stuck, or excessive work consuming the pool;
- closing the barrier is rejecting, shedding, or queuing work once that pool reaches its limit;
- staying afloat means critical operations keep enough independent capacity.

The analogy has limits. Software compartments still share CPU, memory, the operating system, network interfaces, and often a database. A “separate thread pool” is not full isolation if both pools can exhaust the same database connections or heap.

## Plain-language model

- A **bulkhead** is a boundary that gives one class of work a fixed or independently controlled amount of a finite resource.
- A **failure domain** is the set of operations affected by one failure. A good bulkhead makes this set smaller.
- **Saturation** means all allowed concurrent slots or resources are busy.
- **Load shedding** means refusing excess work quickly instead of accepting more work than the system can finish.

The central rule is:

> Put a bound around work that can become slow, then define what happens when that bound is reached.

## Predict before reading

One API instance has 200 request threads. Checkout and product recommendations share them. The recommendation provider starts taking 15 seconds to respond. Incoming recommendation traffic fills all 200 threads. What happens to a checkout request whose own database and payment provider are healthy?

<details>
<summary>Reveal the prediction</summary>

Checkout waits because no request thread is available. Eventually it times out or is rejected. The recommendation failure has spread into checkout through a shared thread pool, so the failure domain is the whole API instance.

</details>

## Without and with a bulkhead

### One shared pool

```text
                 shared 200-thread pool
                +-----------------------+
Checkout ------>|                       |----> Payment
Search -------->| all threads can be    |----> Search
Recommendation->| consumed by one slow  |----> Recommendation (stuck)
                | dependency            |
                +-----------------------+

Result: Recommendation fills 200 slots, so Checkout has 0.
```

### Separate, bounded pools

```text
Checkout ------> checkout pool:       80 slots ----> Payment
Search --------> search pool:         70 slots ----> Search
Recommendation-> recommendation pool: 30 slots ----> Recommendation (stuck)
Admin jobs ----> background pool:     20 slots ----> internal work

Result: Recommendation can consume at most its 30 slots.
The 31st concurrent call follows a defined reject/fallback policy.
```

The exact numbers are examples, not recommendations. Limits should come from measured latency, request rate, downstream capacity, service-level objectives, and load tests.

## What can be isolated?

### 1. Thread or executor pools

Blocking calls to different dependencies use different bounded executors.

```text
request thread
   ├── submit payment call --------> payment executor
   └── submit recommendation call -> recommendation executor
```

If the recommendation executor is full, it rejects or falls back without consuming the payment executor.

This is useful for blocking Java workloads, but every pool adds threads, context switching, queues, and tuning. Creating one pool per endpoint without understanding the call graph can waste resources and hide the real bottleneck.

### 2. Semaphore or concurrency limits

For asynchronous or reactive code, creating extra thread pools may not be useful. A semaphore-like limit can allow at most `N` in-flight calls to a dependency.

```text
Recommendation limit = 30 in-flight calls

calls 1..30  -> admitted
call 31      -> rejected, queued briefly, or served a fallback
```

This protects the dependency and the caller's memory even when no thread blocks per request.

### 3. Database connection pools

Critical and background work can have separate connection budgets. A report query should not consume every connection needed by checkout.

However, two application pools still reach the same database. Their combined maximum must stay below what the database can safely serve, with room for administration and failover. Two pools of 100 do not create 200 connections of database capacity.

### 4. Queues and worker pools

Use separate bounded queues for workloads with different importance or latency expectations:

```text
payment-events queue ----> dedicated payment workers
email-events queue ------> dedicated email workers
image-jobs queue --------> dedicated image workers
```

A surge in image processing then cannot consume every payment worker. Queues must be bounded or governed by retention and backpressure; an unlimited queue merely converts immediate failure into memory growth and hours of hidden delay.

### 5. Process, container, and deployment isolation

Critical workloads can run in separate service instances, containers, node pools, availability zones, or even accounts. This provides stronger isolation than an in-process pool, but costs more and adds deployment and operational complexity.

Examples:

- checkout instances do not also perform report exports;
- media transcoding has independent CPU and memory limits;
- one availability zone's failure does not remove all replicas;
- premium and free workloads have separate quotas when noisy-neighbor risk justifies it.

## Choose the boundary from the failure you want to contain

Common bulkhead dimensions are:

| Isolation boundary | Use it when | Example |
|---|---|---|
| Dependency | One downstream can become slow | Separate payment and recommendation call limits |
| Business criticality | Some paths must survive overload | Preserve checkout capacity before analytics |
| Work type | Background work competes with requests | Separate image workers from HTTP workers |
| Tenant | One customer can create a noisy-neighbor problem | Per-tenant concurrency or queue quota |
| Region or zone | Infrastructure failures must remain local | Replicas and traffic routing across zones |

Do not isolate merely because two methods have different names. Isolate when they have different failure behavior, criticality, resource use, or downstream capacity.

## What happens at the boundary?

A complete bulkhead design defines both a limit and an overflow policy:

1. **Reject quickly:** return `429 Too Many Requests`, `503 Service Unavailable`, or a domain-specific busy result.
2. **Queue briefly:** only if waiting is useful, the queue is bounded, and its maximum wait fits the caller's time budget.
3. **Degrade gracefully:** omit recommendations, serve a stale cache, or accept an asynchronous job when the product allows it.
4. **Prioritize:** reserve capacity for critical requests rather than allowing best-effort traffic to take every slot.

Never silently return incorrect business data as a fallback. Showing “recommendations unavailable” may be acceptable; reporting “payment failed” when the result is actually unknown is not.

## Why a long queue is usually dangerous

Suppose one worker pool completes 100 requests per second, but 150 per second arrive. A queue absorbs 50 extra requests each second:

```text
after 1 second:   50 waiting
after 10 seconds: 500 waiting
after 60 seconds: 3,000 waiting
```

The queue did not create capacity. It delayed the visible failure while latency and memory grew. A small bounded queue can absorb a short burst; sustained overload needs rejection, reduced arrival rate, more proven capacity, or less work per request.

## Bulkhead compared with related patterns

These patterns cooperate, but they solve different problems:

| Pattern | Question it answers |
|---|---|
| Timeout | How long will this caller wait? |
| Retry with backoff and jitter | Should a safe transient failure be attempted again? |
| Circuit breaker | Should calls temporarily stop after repeated failures? |
| Bulkhead | How much capacity may this workload consume? |
| Rate limiter | How much incoming work will be admitted over time? |
| Load shedding | Which excess work will be rejected now? |
| Cache/fallback | Can a useful response be provided without the dependency? |

Example interaction:

```text
request
  -> rate limit
  -> recommendation bulkhead
  -> timeout-bounded call
  -> circuit breaker records result
  -> bounded safe retry, if policy permits
  -> stale cache or explicit unavailable response
```

A timeout frees a slot eventually; a bulkhead limits how many slots can be trapped meanwhile. A circuit breaker reduces new calls after a dependency is known to be unhealthy. None is a substitute for the others.

## Sizing a bulkhead

Use evidence rather than copying a library default:

1. measure normal and tail latency of the protected work;
2. measure peak arrival rate and burst shape;
3. identify the downstream's tested concurrency limit;
4. reserve capacity for higher-priority operations;
5. choose a small or zero queue based on the latency budget;
6. load-test normal, slow, and unavailable dependency conditions;
7. monitor saturation, rejection, queue wait, active work, and completion latency;
8. adjust deliberately, because raising a limit can move the failure into the downstream service.

A rough intuition is that required concurrency grows with request rate multiplied by time spent in flight. If 20 recommendation calls arrive each second and each normally lasts 0.5 seconds, about 10 are in flight on average. If latency rises to 5 seconds, about 100 accumulate at the same arrival rate. The bulkhead prevents that slowdown from growing without a bound.

## E-commerce example

Checkout calls fraud scoring and payment. Recommendations appear on the same page but are non-critical.

```text
                    +--> fraud bulkhead -----> Fraud Service
Checkout API -------+--> payment bulkhead ---> Payment Provider
                    +--> order DB pool ------> Order DB

Product page ---------> recommendation bulkhead --> Recommendation Service
```

Design choices:

- payment gets an isolated, carefully sized concurrency limit;
- payment timeouts produce an `UNKNOWN`/pending outcome, not a blind retry with a new ID;
- recommendations have a smaller limit and may fall back to cached popular products;
- report generation uses separate workers and database connections;
- retries consume the same bulkhead and a retry budget so they cannot bypass the limit;
- saturation emits metrics and alerts before operators simply increase the limits.

## Instagram example and project mapping

Today, [`PostService.feed`](../../../../../src/main/java/com/instagram/backend/service/PostService.java) reads posts and maps them to responses in one application. Inside [`toResponse`](../../../../../src/main/java/com/instagram/backend/service/PostService.java), it also checks whether the viewer liked each post. If likes later become a remote service, making one unbounded network call per feed item would let a slow Like Service consume the feed's request capacity.

A safer proposed evolution is:

1. batch the like-status request for all visible post IDs;
2. protect that remote call with a feed-to-like concurrency limit and short timeout;
3. choose an honest product fallback, such as marking like status temporarily unavailable rather than falsely showing “not liked”;
4. keep feed request capacity separate from media processing and background fan-out workers;
5. monitor bulkhead active count, rejections, timeout count, and degraded-feed responses.

[`MediaStorageService.store`](../../../../../src/main/java/com/instagram/backend/service/MediaStorageService.java) currently performs blocking file work. If uploads become expensive media-processing jobs, use a separate bounded queue and worker deployment so an upload burst cannot starve [`PostController.feed`](../../../../../src/main/java/com/instagram/backend/controller/PostController.java).

These are proposed learning improvements, not features already present in the repository.

## Common mistakes

### One pool, several labels

Metrics may label checkout and recommendations separately, but they are not isolated if both still use the same exhaustible executor or connection pool.

### Separate executors, shared hidden bottleneck

Two executors still fail together if they allocate unbounded memory or compete for an exhausted database. Trace the entire resource path.

### Retries outside the bulkhead

If each failed request starts three unrestricted retries, retry traffic can exceed the original workload. Retries must obey the same concurrency and rate budgets.

### Oversized compartments

A limit larger than the downstream's safe capacity protects nothing. It merely relocates saturation.

### No overflow behavior

When the pool fills, callers need an explicit rejection, queue, fallback, or asynchronous response. Otherwise they wait in some accidental hidden queue.

### Treating rejection as a bug

Under overload, intentionally rejecting low-priority work can be the correct action that preserves critical service. The decision must be observable and tied to product priorities.

## Trade-offs

| Benefit | Cost |
|---|---|
| Smaller failure domain | Reserved capacity may sit idle |
| Predictable maximum concurrency | Limits require measurement and tuning |
| Critical-path protection | More executors, pools, queues, or deployments to operate |
| Faster overload response | Some requests are rejected or degraded |
| Clear dependency metrics | Incorrect boundaries can create false confidence |

Bulkheads exchange some peak utilization for predictable isolation. That is often worthwhile on critical paths, but applying them everywhere creates operational clutter.

## Failure drill

**Scenario:** Like Service becomes slow after a database index is dropped. The feed-to-like bulkhead allows 25 calls, with a queue of 10. All 25 calls are waiting and the queue is full.

1. **Predict:** the next feed enrichment call is rejected immediately; it must not enter an unbounded wait.
2. **Observe:** bulkhead active count is 25, queue depth is 10, rejections and degraded-feed responses rise, while post retrieval remains healthy.
3. **Contain:** open the circuit after the configured failure evidence, stop enrichment calls temporarily, and return the approved degraded response.
4. **Recover:** restore Like Service, allow a small number of probe calls, close the circuit gradually, and drain only work whose deadlines have not expired.
5. **Learn:** confirm that feed latency stayed inside its objective and that the database, CPU, and request pool were truly isolated; revise limits if the failure leaked through a shared resource.

## Interview-ready answer

Bulkhead isolation divides finite capacity into bounded compartments so one slow dependency cannot consume everything and fail unrelated operations. I map critical paths and shared resources: threads, in-flight calls, database connections, workers, CPU, memory, and downstream concurrency. I isolate workloads when they differ in priority, resource use, or failure behavior—not simply because they have different endpoint names.

For example, checkout, recommendations, and reports should not share one unbounded executor and database budget. Payment might receive a measured concurrency limit, recommendations a smaller limit with a cached fallback, and reports a separate worker queue. Blocking Java may use bounded executors; reactive code may use an in-flight limit; stronger isolation may require separate deployments. Every compartment also needs an overflow policy: reject quickly, wait in a bounded queue only when the deadline permits, or return an honest degraded result.

Consider a concrete failure: Like Service latency rises from 50 milliseconds to 10 seconds. Its 25-call bulkhead fills and the 10-item queue reaches capacity. The next enrichment call is rejected immediately, the feed returns an approved “like status unavailable” response, and post retrieval keeps its threads. A timeout eventually releases trapped slots, while a circuit breaker stops further probes except a small half-open test set. Metrics show active calls, queue depth, rejection count, and degraded responses. Retries must pass through the same limit and retry budget so they cannot bypass the protection.

The trade-off is deliberate under-utilization and operational complexity: reserved checkout capacity may sit idle, and too many pools become hard to tune. Separate executors also give false confidence if they still exhaust one database, heap, or CPU. I therefore load-test slow and unavailable dependencies, size limits from measured arrival rate, tail latency, and downstream capacity, and verify the entire resource path. Bulkheads contain the blast radius; timeouts, circuit breakers, rate limits, backpressure, and load shedding complete the resilience policy.

## Questions and explained answers

<details>
<summary>1. How is a bulkhead different from a circuit breaker?</summary>

A bulkhead limits how much concurrent capacity a workload may consume, even before the system knows the dependency is unhealthy. A circuit breaker watches outcomes and temporarily stops new calls after failures cross a threshold. During a slow failure, the bulkhead prevents every caller from becoming trapped; the circuit breaker then reduces further pressure. They work together.

</details>

<details>
<summary>2. Why not make the bulkhead very large to avoid rejections?</summary>

A larger limit can exceed the caller's memory, threads, sockets, or the downstream's tested capacity. It may reduce visible rejections briefly while increasing queueing, tail latency, and cascading failure. Rejection is sometimes the safety mechanism. Size the limit from capacity and latency evidence, then decide which work should degrade first.

</details>

<details>
<summary>3. Are separate thread pools sufficient isolation?</summary>

Not necessarily. The pools may still share heap, CPU, network sockets, or one database connection limit. If both executors call the same saturated database, their names do not create database capacity. Isolation must be checked across the full dependency and resource path.

</details>

<details>
<summary>4. What should happen when a bulkhead is full?</summary>

The behavior must be deliberate: reject quickly, wait in a small bounded queue if the deadline permits, return an honest cached/degraded result, or accept asynchronous work. The caller should receive a clear signal, and the service should emit saturation and rejection metrics. An unbounded hidden queue defeats the pattern.

</details>

<details>
<summary>5. Where would you add the first bulkhead in a checkout design?</summary>

Start at a dependency whose slowdown can consume the critical path, often the external payment or fraud call. Give it a tested concurrency limit, deadline, idempotent retry policy, and explicit unknown-outcome handling. Also isolate non-critical work such as recommendations and reporting so it cannot take checkout capacity. The exact first boundary depends on measurements and the failure with the largest business impact.

</details>
