# Inter-Service Communication: Latency, Timeouts, and Cascading Failures

> **Question: How would you address inter-service communication issues like latency, timeouts, and cascading failures in a microservices architecture?**

> [One-page revision card](../../revision-cards/microservices/03-inter-service-communication-resilience.md)

## The profile call succeeds, but the feed still fails

Suppose the feed needs a profile name before it can respond. Profile usually answers in 50 ms, then starts taking four seconds. It still returns valid responses, so an error-only dashboard looks reassuring. Meanwhile feed requests keep arriving and holding workers while they wait. Soon a new feed request cannot get a worker at all.

Only one dependency became slow, but waiting spread the problem into its callers. This is a **partial failure**: one part of the system or its communication path behaves badly while other parts still work. The caller needs a policy for that state before it happens.

There is another complication. If a write times out, the server may have saved it and lost the response. The caller knows only that it stopped waiting. Communication policy must therefore protect both finite capacity and the correctness of repeated operations.

## Think of a kitchen waiting on one slow station

An order may pass through grill, salad, and plating stations. If the grill is slow and every server keeps placing duplicate urgent orders, the grill queue grows, the plating station waits, tables remain occupied, and eventually the whole restaurant stops serving—even though only one station was initially slow.

Useful responses map to software:

- stop accepting more grill-dependent orders when capacity is exhausted;
- give each station a limited work area so one backlog cannot occupy the whole kitchen;
- offer a smaller menu when one station is unavailable;
- avoid resubmitting the same paid order as if it were new.

### Where the analogy breaks

A network timeout is ambiguous: the remote service may already have committed the operation. Software also retries automatically at several invisible layers, processes work concurrently across many instances, and can queue messages durably for later processing. The kitchen analogy helps with capacity and isolation but does not capture duplicate delivery or exact data-consistency semantics.

## Does the user need this result before we answer?

Start by removing waiting that the product does not need. Creating a post must authenticate the user and store the post before reporting success. Sending followers a notification can often happen afterwards. Making notification delivery part of the same waiting chain makes its outage a post-creation outage too.

A **synchronous call** means the caller waits for the result before continuing. Use it when the immediate answer needs that decision and no acceptable local read copy exists. The price is that the caller's response time and availability now include the dependency.

An **asynchronous message** hands work to another component for later completion. For the post example, save the post and an event record in one local transaction, return `201 Created`, then publish the event and let a notification worker act on it. An outbox is the table holding that event record alongside the business change; the [transaction lesson](./04-distributed-transactions-and-data-consistency.md) explains why the pair must commit together.

```text
Client -> Post service -> commit post + outbox -> 201 Created
                                         |
                                         v
                               notification processing later
```

If notification processing stops, the post remains available and the durable work can wait. That is useful independence, but it is not free. Messages can repeat, arrive late, and build a backlog. State how much notification delay is acceptable and how the system detects work that never finishes.

Do not use a queue simply to avoid making a decision about freshness. If authorization must be checked now, a delayed “eventually authorized” answer is not a safe substitute.

## How long is this answer still useful?

Suppose the feed must reach the user within 1,000 ms. That entire allowance is its **deadline**. If the gateway spends 50 ms and we reserve 100 ms to build and return the response, the feed operation has `1,000 - 50 - 100 = 850 ms` left.

We cannot give each sequential dependency a fresh 1,000 ms allowance. The first could use the whole user budget before the second begins. A possible split of the remaining 850 ms is:

| Work | Maximum allowance |
|---|---:|
| Post query | 250 ms |
| Profile lookup | 250 ms |
| Ranking and other local work | 200 ms |
| Safety margin | 150 ms |

The allowances sum to `250 + 250 + 200 + 150 = 850 ms`. They are teaching values, not configuration advice: measured healthy and tail latency determine whether they are realistic.

A **timeout** limits one wait. A connect timeout covers establishing the connection; a request timeout covers waiting for the operation or response, according to the client library. The overall deadline must still include connection setup, all attempts, and local work. Pass the remaining time to each hop instead of restarting the original budget.

For example, with a deadline of `12:00:01.000`, a gateway starting at `12:00:00.080` has 920 ms left. Feed starting at `12:00:00.180` has 820 ms. Its next call must leave a response margin inside those 820 ms.

When the result is no longer useful, cancel outstanding work where the libraries support it. Otherwise the remote operation can keep occupying capacity after its caller has left. Cancellation is still not rollback: it cannot erase a database commit that already occurred.

## Another attempt helps only under specific conditions

A brief connection reset may clear before the deadline. Trying that call once more can help. Repeating an authorization rejection will not change the user's permissions. Repeating a call into an overloaded service can make its queue worse.

Before retrying, require three things: the fault may be temporary, repeating the logical operation is safe, and enough of the original deadline remains. Choose one layer to own a small total attempt budget. Increase the pause between attempts—**exponential backoff** might use 50, 100, then 200 ms—and randomize it with **jitter** so many callers do not return together.

### Count attempts across the whole call chain

If the gateway makes three total attempts, Service A makes three for each gateway attempt, and Service B makes three for each A attempt, the bottom dependency receives `3 × 3 × 3 = 27` attempts for one user request. The local retry settings looked small; their combined effect is large. Upstream layers should respect the final result instead of starting another full retry sequence.

### A retry must repeat the intention, not create another one

An **idempotent operation** has the same intended business effect when the same logical request repeats. `PUT /posts/42/likes/me` can mean “ensure my like exists.” Sending it again after a lost response does not mean “add another like.” A later unlike and new like are legitimate new intentions and must still work.

If a command-style `POST` needs a key, generate one for the logical attempt, such as `like-attempt-8f3c`. Reuse it across that attempt's retries, then use a new key for a later deliberate action. A durable stored outcome and a uniqueness constraint make this safe across concurrent instances.

The current [PostService.like(...)](../../../../../src/main/java/com/instagram/backend/service/PostService.java) first checks whether the like exists. [PostLike.java](../../../../../src/main/java/com/instagram/backend/domain/PostLike.java) also declares uniqueness on `(post_id, user_id)`. The database constraint matters because two callers can both observe “missing” before either inserts. A future extraction must preserve it and turn the losing insert into the same logical “already liked” result.

## Keep a slow dependency from occupying the whole caller

A timeout releases a trapped call eventually. We also need to limit how many calls can be trapped at once. A **bulkhead** reserves a bounded share of capacity for one dependency or workload, so profile cannot take every feed worker.

After repeated evidence of failure, a **circuit breaker** stops normal calls temporarily rather than spending that share on attempts unlikely to work. While calls are stopped, a **fallback** may provide a deliberately reduced response if there is an honest product-approved alternative. These solve different parts of the same failure; none repairs profile data or creates spare capacity.

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

## What should happen when there is no room left?

A queue can hide overload briefly. If new work keeps arriving faster than we finish it, the queue grows and the eventual answer arrives too late to help. Making the queue unbounded postpones rejection until memory or caller patience runs out.

Use a **bounded queue**, with a maximum length and a maximum useful wait. When it fills, tell producers to slow down where the protocol allows it; that is **backpressure**. If the system must reject some work to preserve the rest, that is **load shedding**. For HTTP, return a clear capacity response with safe retry guidance. For a broker, pause intake. For optional ranking, omit the work if the product permits it.

Adding application instances helps only when the constrained tier can actually accept more work. More callers against one already-overloaded database create additional queries and connections. Find the resource limit before deciding which tier should scale.

## Put the policies together around the same feed request

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

## How would we know the protection worked?

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

## What each protection costs

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

Before marking this answer practiced, explain the four-second profile slowdown and calculate a deadline budget without notes. Predict which capacity remains available, then answer the questions below before opening them. No remote resilience lab is claimed here.

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
