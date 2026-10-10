# Inter-Service Communication: Latency, Timeouts, and Cascading Failures

> **Question: How would you address inter-service communication issues like latency, timeouts, and cascading failures in a microservices architecture?**

> [One-page revision card](../../revision-cards/microservices/03-inter-service-communication-resilience.md)

## Why does this problem exist?

An in-process Java method call either returns or throws within the same process. A network call has more states:

- the request may never reach the server;
- the server may finish, but the response may be lost;
- the response may arrive after the caller has stopped waiting;
- the server may be healthy but overloaded;
- DNS, a load balancer, a proxy, or the network path may fail;
- one slow dependency can make caller threads and connections wait until the callers also fail.

This creates **partial failure**: some components work while another component or communication path does not. A design that assumes “the network usually works” eventually turns one local problem into a system-wide outage.

## Analogy: a restaurant kitchen with dependent stations

An order may pass through grill, salad, and plating stations. If the grill is slow and every server keeps placing duplicate urgent orders, the grill queue grows, the plating station waits, tables remain occupied, and eventually the whole restaurant stops serving—even though only one station was initially slow.

Useful responses map to software:

- stop accepting more grill-dependent orders when capacity is exhausted;
- give each station a limited work area so one backlog cannot occupy the whole kitchen;
- offer a smaller menu when one station is unavailable;
- avoid resubmitting the same paid order as if it were new.

### Where the analogy breaks

A network timeout is ambiguous: the remote service may already have committed the operation. Software also retries automatically at several invisible layers, processes work concurrently across many instances, and can queue messages durably for later processing. The kitchen analogy helps with capacity and isolation but does not capture duplicate delivery or exact data-consistency semantics.

## Focus 1 — Decide whether communication must be synchronous

Three useful terms:

- A **synchronous call** makes the caller wait for a result before it can continue.
- An **asynchronous message** records work for another component without requiring it to finish during the caller's request.
- A **partial failure** occurs when one part of the distributed workflow fails while other parts continue working.

### Use synchronous calls when

- the caller needs the answer to produce a correct immediate response;
- the operation is a query that cannot use a local read model;
- the user must know whether a rule passed before continuing;
- latency and availability requirements can tolerate the dependency.

### Use asynchronous messaging when

- work can happen after the user receives the response;
- temporary delay is acceptable;
- buffering absorbs traffic bursts;
- independent retries are safer than holding an HTTP request open.

For example, creating a post needs authentication and durable post storage before returning success. Sending follower notifications does not need to block post creation:

```text
Client -> Post service -> save post + outbox record -> 201 Created
                                      |
                                      v
                                PostCreated event
                                      |
                                      v
                             Notification service
```

If the notification service is down, the event remains available for later processing. The post still exists. This removes notification availability and latency from the critical request path.

### Do not replace every call with a queue

Asynchronous communication introduces delayed visibility, duplicate delivery, ordering questions, backlog management, and harder end-to-end debugging. State what delay the user can tolerate and what they see while processing is pending.

## Focus 2 — Bound waiting with deadlines and timeouts

Three useful terms:

- A **deadline** is the absolute time by which the entire operation must finish.
- A **connect timeout** limits how long a client waits to establish a connection.
- A **request timeout** limits how long it waits for the remote operation or response.

A timeout is not a guarantee that the remote operation failed. It only says the caller stopped waiting.

### Budget from the outside inward

Suppose the product requirement gives `GET /feed` a 1,000 ms end-to-end deadline:

```text
Total user budget                              1,000 ms
- gateway parsing/routing allowance               50 ms
- response serialization/network reserve         100 ms
--------------------------------------------------------
feed-service processing budget                   850 ms
```

If the feed service makes two sequential dependency calls, giving each a 1,000 ms timeout is wrong: the first can consume the user's entire deadline before the second starts. The service might instead reserve local work and allocate bounded dependency budgets:

```text
post query budget         250 ms
profile query budget      250 ms
ranking/local work        200 ms
remaining safety margin   150 ms
```

These are illustrative values, not universal defaults. Measure normal and tail latency, then choose budgets that satisfy the user-facing objective without cutting off healthy work unnecessarily.

### Propagate the remaining deadline

```text
Client deadline: 12:00:01.000
        |
Gateway at 12:00:00.080 -> 920 ms remains
        |
Feed at 12:00:00.180    -> 820 ms remains
        |
Profile call receives a timeout smaller than 820 ms
```

An absolute deadline prevents every hop from starting a fresh full timeout. Each service also needs a small response margin so it can cancel work and return a controlled response before its own caller gives up.

### Cancel abandoned work when possible

When the caller deadline expires, propagate cancellation to downstream calls and database work where the client and library support it. Otherwise, “zombie” work continues consuming resources even though nobody can use its response.

Cancellation is not rollback. If a database commit already happened, cancelling the HTTP wait cannot undo it.

## Focus 3 — Retry narrowly and safely

Three useful terms:

- **Exponential backoff** increases the wait between attempts, for example 50 ms, 100 ms, then 200 ms.
- **Jitter** adds randomness to that wait so thousands of clients do not retry at exactly the same moment.
- An **idempotent operation** can be repeated with the same logical request without applying the effect more than once.

### Retry only when all of these are true

1. The failure is plausibly transient, such as a brief connection reset, a capacity response that supplies a retry hint, or a service-unavailable response.
2. The operation is naturally idempotent or protected by an idempotency key.
3. Enough deadline remains for another useful attempt.
4. The retry occurs at one controlled layer with a small maximum attempt count.

Do not normally retry validation failures, authorization failures, not-found responses, or deterministic application errors. Retrying a slow overloaded dependency immediately increases its load precisely when it has the least spare capacity.

### Why retries can create a cascade

Assume three calling layers each make one initial attempt plus two retries. If the bottom dependency is down, attempts can multiply:

```text
gateway:       3 attempts
service A:     3 attempts for each gateway attempt
service B:     3 attempts for each service-A attempt

bottom dependency receives 3 x 3 x 3 = 27 attempts
for one original user request
```

This is why retry ownership and a total retry budget must be explicit. Often the layer closest to the dependency retries a small number of times while upstream layers respect the final failure.

### Mutating-request example

```text
PUT /posts/42/likes/me
```

This desired-state API means “ensure my like exists,” so retrying it after a lost response is naturally idempotent. A later `DELETE /posts/42/likes/me` removes the like, and a later `PUT` can add it again without being confused with the older request. If an API must retain a command-style `POST`, use an attempt-specific key such as `like-attempt-8f3c`, reuse it only across retries of that logical attempt, and issue a new key for a legitimate later like.

The current [PostService.like(...)](../../../../../src/main/java/com/instagram/backend/service/PostService.java) checks whether a like exists before creating one, and [PostLike.java](../../../../../src/main/java/com/instagram/backend/domain/PostLike.java) already adds the race-safe database uniqueness rule on `(post_id, user_id)`. Two concurrent requests can both observe “not present,” but the constraint prevents both inserts from succeeding. A future extraction must preserve that protection and translate the losing insert into the same logical “already liked” result.

## Focus 4 — Stop failure propagation

Three useful terms:

- A **circuit breaker** temporarily stops calls to a dependency that is repeatedly failing or timing out.
- A **bulkhead** gives one dependency or workload a separate, limited resource pool so it cannot consume everything.
- A **fallback** is a deliberately reduced response used when the preferred result is unavailable.

### Circuit breaker behavior

```text
CLOSED: calls flow; failures are measured
   |
   | failure threshold exceeded
   v
OPEN: calls fail fast; dependency gets recovery time
   |
   | cooldown passes
   v
HALF-OPEN: allow a few probe calls
   ├─ probes succeed -> CLOSED
   └─ probes fail    -> OPEN
```

`CLOSED` does not mean the dependency is offline; it means the circuit allows calls. `OPEN` means the electrical-style circuit is interrupted, so calls are rejected quickly.

A circuit breaker should use a meaningful sample window and distinguish slow calls from application rejections. Opening after one failure causes unnecessary outages; never opening until every worker is stuck provides no protection.

### Bulkhead behavior

Suppose the feed service has 100 request workers. If slow profile calls can occupy all 100, health endpoints and simple cached requests may also fail. Give profile calls a concurrency limit of, for example, 20:

```text
Feed service capacity
├─ profile dependency: max 20 concurrent calls
├─ ranking dependency: max 15 concurrent calls
└─ remaining request work: protected capacity
```

When the profile limit is full, new profile-dependent work is rejected or degraded quickly. Twenty is only an example; set limits from load tests, downstream capacity, and latency objectives.

### Fallbacks must preserve correctness

Reasonable fallback:

- show a recent cached display name if profile service is temporarily unavailable;
- omit “suggested accounts” from a feed;
- accept notification work for later processing.

Unsafe fallback:

- assume a payment succeeded;
- bypass authorization because identity is unavailable;
- show stale inventory as guaranteed available during checkout.

A fallback is a product decision, not merely a library configuration. State its maximum staleness and how the UI communicates degraded data.

## Focus 5 — Apply backpressure before saturation becomes collapse

Three useful terms:

- **Backpressure** tells upstream producers to slow down because the consumer cannot keep up.
- **Load shedding** rejects low-priority work early to preserve capacity for essential work.
- A **bounded queue** has a fixed maximum size rather than growing until memory or latency is exhausted.

An unbounded queue can make a system look available while work waits for minutes and memory grows. By the time it crashes, every queued request is already too old to be useful.

Use bounded concurrency and queues, then define what happens at the limit:

- HTTP can return a capacity response with a safe retry hint;
- a broker consumer can pause intake while it recovers;
- optional recommendation or analytics work can be dropped;
- critical operations can receive reserved capacity.

Scaling can help when demand is legitimate and dependencies can also support the increased load. Scaling callers alone can make an overloaded database worse by creating more concurrent queries.

## End-to-end resilient feed example

### Fragile version

```text
Client
  -> Gateway: no overall deadline
      -> Feed service: 30-second timeout
          -> Post service: 30-second timeout + 3 retries
          -> Profile service once per post: 30 seconds + 3 retries each
          -> Ranking service: 30 seconds + 3 retries
```

If profile becomes slow, feed workers wait. Retried fan-out increases profile traffic, connection pools fill, gateway requests accumulate, memory grows, and unrelated routes become slow. This is a cascading failure.

### Resilient version

```text
Client: 1-second deadline
  -> Gateway: propagates remaining deadline
      -> Feed service
          ├─ local/read-model post query with bounded DB time
          ├─ one batched profile call
          │    ├─ shorter timeout
          │    ├─ profile-specific concurrency limit
          │    └─ circuit breaker + approved cached fallback
          └─ optional ranking
               └─ shed/fallback to chronological order if unavailable

PostCreated event -> notification queue -> notification service later
```

Changes that matter:

1. The client budget is propagated instead of reset.
2. One batched profile request replaces per-post fan-out.
3. Optional ranking cannot consume the entire critical path.
4. A profile failure has a bounded resource allocation.
5. Noncritical notification work leaves the synchronous path.
6. Degraded behavior is explicit rather than accidental.

## Instagram-backend mapping

Today these calls are in-process, so this is a design exercise rather than a claim that remote resilience is already implemented:

- [ProfileService.java](../../../../../src/main/java/com/instagram/backend/service/ProfileService.java) calls `PostService.countPostsFor(...)`. If profile and posts are separated, replace per-field chatty calls with a deliberate API or a locally maintained profile projection.
- [PostService.java](../../../../../src/main/java/com/instagram/backend/service/PostService.java) reads users, posts, and likes while constructing the feed. Extracting each repository behind a remote service would turn one database workflow into many synchronous failure points. A feed-oriented read model or batched API is usually safer.
- [MediaStorageService.java](../../../../../src/main/java/com/instagram/backend/service/MediaStorageService.java) validates access through `TokenService`. After extraction, repeatedly calling a remote auth service for every media byte is unnecessary. Validate a signed short-lived token locally, or use a carefully secured gateway identity contract.
- [AuthIntegrationTests.java](../../../../../src/test/java/com/instagram/backend/AuthIntegrationTests.java) gives a base for API behavior. A future resilience test should also inject slow dependencies, connection failures, duplicate attempts, and recovery—not only successful responses.

### Example proposed resilience test

1. Make the profile dependency take 800 ms while the feed dependency budget is 250 ms.
2. Predict that feed returns its documented degraded response before the 1-second user deadline.
3. Verify the profile call is cancelled or abandoned, the circuit records a slow failure, and worker usage stays bounded.
4. Repeat enough calls to open the circuit.
5. Restore profile health and verify limited half-open probes close the circuit.
6. Confirm a separate create-post route remains healthy throughout.

That last assertion proves isolation. Merely observing that the circuit opened does not prove the service remained useful.

## What to monitor

For each remote dependency, record:

- request rate, latency distribution, timeout count, and result category;
- current concurrent calls and queue wait time;
- retry attempts and retry success rate;
- circuit state changes and fast rejections;
- bulkhead/concurrency-limit rejections;
- fallback use, including which fallback;
- remaining deadline at call start;
- queue depth, oldest-message age, and processing rate for asynchronous paths.

Always relate technical signals to the user outcome. A circuit opening is not automatically bad if it protects the feed and the approved fallback works. A closed circuit is not automatically good if every call takes just under the timeout and users abandon the request.

## Trade-offs

| Technique | Benefit | Cost or danger |
|---|---|---|
| Short timeout | Releases resources quickly | Rejects healthy but slow requests if set below measured reality |
| Retry | Hides brief transient faults | Amplifies load, latency, and duplicates if uncontrolled |
| Circuit breaker | Stops repeated calls and supports recovery | Poor thresholds cause unnecessary fast failures or react too late |
| Bulkhead | Contains resource exhaustion | Reserved capacity may sit unused and limits need tuning |
| Cache fallback | Maintains read availability | Serves stale data and needs an explicit staleness policy |
| Async message | Decouples time and availability | Adds delayed consistency, duplicate handling, and backlog operations |
| Load shedding | Preserves critical work | Some requests are deliberately rejected, requiring product prioritization |

## Failure drill

**Scenario:** the profile service's database becomes slow. Its HTTP error rate remains low because most calls eventually succeed in 4 seconds. Feed normally receives 500 requests per second and waits up to 5 seconds for profile.

1. **Prediction:** feed workers and connections accumulate waiting calls. Queueing raises feed latency, clients retry, traffic increases, and eventually feed and gateway capacity are exhausted even though profile still returns many successful responses.
2. **Evidence:** profile p95/p99 latency and active calls rise before its error rate; feed dependency wait time, worker saturation, and client retry count rise next.
3. **Immediate protection:** enforce the shorter profile dependency budget, cap concurrent profile calls, stop retries, and use the approved cached-profile fallback. Shed optional requests if critical capacity remains threatened.
4. **Recovery check:** reduce traffic or repair the profile database, then allow a small number of half-open probes. Do not immediately release all queued and retried work at once.
5. **Prevention:** size the profile bulkhead through load tests, alert on latency and saturation rather than errors alone, batch profile reads, propagate deadlines, and test slow-success behavior regularly.

The important insight is that slow success can be more dangerous than fast failure because it holds scarce resources for longer.

## Common weak answers and how to improve them

### “Add retries for reliability”

State which failures are transient, which operation is idempotent, which layer owns retries, how many attempts fit inside the deadline, and how backoff plus jitter avoids synchronized traffic. Otherwise retries can cause the outage.

### “Use a circuit breaker”

Explain what it protects, which failures and slow calls it counts, what happens while open, how probes restore traffic, and what the user receives. A named pattern without thresholds or behavior is not a design.

### “Increase the timeout”

A larger timeout can help a legitimately long operation, but it also keeps workers and connections occupied longer. Start from the user deadline, measure the latency distribution, remove avoidable synchronous work, and allocate per-hop budgets.

### “Return cached data”

Name the cached field, maximum acceptable age, invalidation or refresh path, security implications, and UI behavior. Stale profile text may be acceptable; stale authorization or payment status is not.

## A 2–3 minute interview answer

“I start by minimizing the synchronous critical path. If the caller does not need an immediate result, such as follower notifications after a post, I publish durable work asynchronously. For required request-response calls, I define an end-to-end deadline from the user experience and propagate the remaining time. Every downstream timeout is shorter than the remaining deadline and leaves time for the caller to return a controlled response.

I retry only transient failures, only when the operation is idempotent or uses an idempotency key, and only while enough deadline remains. Retries use exponential backoff with jitter, a low attempt cap, and one clearly owned retry layer, because nested retries can multiply traffic. I remember that timeout means an unknown outcome, not guaranteed failure.

To prevent cascades, I use a circuit breaker to fail fast when a dependency is repeatedly unhealthy, a dependency-specific bulkhead or concurrency limit so it cannot consume every worker, bounded queues and backpressure, and load shedding for low-priority work. Fallbacks are product-specific: cached profile data or chronological feed ranking may be acceptable, but bypassing authorization or assuming payment success is not.

For example, if profile latency jumps to four seconds, feed can enforce its 250 ms budget, cap concurrent profile calls, open the circuit, and serve stale display names. The feed remains responsive and create-post capacity stays isolated. This trades freshness for availability; I would not make that trade for authorization or payment correctness.

I monitor each dependency's tail latency, timeouts, active calls, queue wait, retries, circuit state, rejections, and fallback usage, tied to the user-facing SLO. I test slow dependencies, lost responses after commits, duplicate delivery, circuit recovery, and isolation of unrelated routes. This design turns an unbounded wait and traffic-amplification problem into a bounded, observable degradation.”

## Questions and explained answers

<details>
<summary>1. Why is a timeout not proof that an operation failed?</summary>

The server may have received and committed the request, but the response may have been delayed or lost after the client's deadline. Therefore blindly retrying a mutation can duplicate it. Use an idempotency key, a unique business constraint, or a status lookup so an ambiguous result can be resolved safely.

</details>

<details>
<summary>2. Why should downstream timeouts be shorter than the user's total deadline?</summary>

The caller needs time for its own processing, possibly other dependencies, serialization, and returning the response. If every hop waits for the complete user deadline, an inner call can consume all available time and every outer layer will time out anyway. Propagating the remaining absolute deadline keeps the total work bounded.

</details>

<details>
<summary>3. How can retries turn one failure into a cascading failure?</summary>

Retries add load to a dependency that may already be overloaded. If several layers each retry, attempts multiply—for three total attempts at each of three layers, one user request can produce 27 bottom-level attempts. Those attempts occupy more workers and connections, increase latency, trigger more client retries, and spread saturation upstream.

</details>

<details>
<summary>4. What is the difference between a circuit breaker and a bulkhead?</summary>

A circuit breaker uses recent outcomes to decide whether new calls should flow or fail fast, giving an unhealthy dependency recovery time. A bulkhead places a hard resource boundary around calls even before failure history is known. The circuit reacts to dependency health; the bulkhead guarantees that dependency cannot consume all caller capacity. They solve related but different problems and are often used together.

</details>

<details>
<summary>5. When is asynchronous messaging better than a synchronous API call?</summary>

It is better when the caller can succeed after durably recording intent without waiting for downstream completion, such as sending notifications. It buffers bursts and decouples temporary availability. It is not automatically better when an immediate answer is required, and it introduces delayed consistency, duplicate delivery, ordering, backlog, and replay concerns that must be designed explicitly.

</details>
