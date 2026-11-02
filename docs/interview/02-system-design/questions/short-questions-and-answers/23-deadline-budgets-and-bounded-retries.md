# Deadline Budgets and Bounded Retries

## Question

How do you assign timeouts and retries across a chain of dependent services?


## There is one user deadline, even when there are many calls

Suppose a feed has an illustrative 800 ms completion budget. Local work uses 100 ms, and we reserve 100 ms to return the response. That leaves `800 − 100 − 100 = 600 ms` for dependent work. Giving every dependency its own 800 ms would lose the connection to the user's limit.

A **deadline** is the latest useful completion time. Pass the remaining allowance through the call chain, subtracting elapsed time. Each dependency uses the smaller of its configured cap and that remaining allowance.

Think of a connecting journey with one arrival time. Each leg cannot spend the whole trip allowance. Computing adds a complication the journey analogy misses: work may continue after its caller stops waiting.

## Spend the retry time explicitly

Our first safe read attempt has a 200 ms cap and fails transiently after using it. We have `600 − 200 = 400 ms` left. A retry delay takes 50 ms, leaving `400 − 50 = 350 ms`. The second attempt has a 200 ms cap and succeeds in 150 ms, leaving `350 − 150 = 200 ms` of the downstream allowance.

Elapsed time before the response reserve is `100 + 200 + 50 + 150 = 500 ms`. Using up to the reserved 100 ms yields at most 600 ms in this example. These numbers explain the accounting; production values must come from observed latency and the product target.

Random variation in a retry delay is **jitter**. It spreads retries so many callers do not repeat together. It does not reduce the total work each retry creates.

## Bound extra work as well as waiting

A **retry budget** limits extra attempts by count or load, as well as remaining time. Let one layer own the policy and count hidden client-library or proxy attempts. Three layers each making up to three attempts can produce `3 × 3 × 3 = 27` calls to the final dependency for one operation.

Retry only a suitable transient failure when repetition is safe and another useful attempt fits. A create with an ambiguous outcome needs an idempotency contract first. A validation failure will not improve by retrying.

When the budget ends, use only an approved fallback and cancel outstanding work where cancellation is supported. A caller timeout does not establish that a downstream query or side effect stopped. Measure that behavior instead of assuming it.

## Where this appears in our backend

[PostService](../../../../../src/main/java/com/instagram/backend/service/PostService.java) currently serves the database-backed post flow; it is not a chain of remote ranking and recommendation services. [JourneyMetricsInterceptor](../../../../../src/main/java/com/instagram/backend/observability/JourneyMetricsInterceptor.java) is a real starting point for measuring request journeys.

**Proposed:** downstream deadline propagation, coordinated retry budgets, and optional ranking fallback if remote dependencies are introduced. Database query and connection-acquisition limits also need their own supported configuration; an HTTP timeout alone does not bound all database work.

## Choosing the approach and paying its costs

- Retry transient failures when another attempt fits and the operation is safe. Avoid retrying validation failures or ambiguous unsafe writes.
- Use jitter and bounded attempts to spread repeated traffic; extra attempts still consume capacity. AWS explains this risk in [timeouts, retries, and backoff with jitter](https://d1.awsstatic.com/builderslibrary/pdfs/timeouts-retries-and-backoff-with-jitter.pdf).
- Choose a fallback only when the product allows reduced results, such as a recent-post feed instead of personalized ranking. Do not turn authorization failure into a successful fallback.
- Aggressive timeouts reduce waiting but can reject healthy slow requests; loose timeouts tie up resources. Tune from measured distributions and the end-to-end target.


## Paper drill: ranking stays slow after the caller leaves

Predict the capacity effect if the caller times out but each ranking attempt keeps running, while clients start new attempts. Abandoned work and retries can consume capacity together.

Observe request deadlines, dependency latency, attempt counts and work that remains active after expiry. Contain the incident by stopping attempts that exceed time or load budgets and selecting the approved feed fallback. Authorization failures must still follow the security contract.

Recover the dependency and restore traffic gradually while watching saturation. Prevent recurrence with slow-response and lost-response tests, complete retry accounting and verified cancellation behavior. A short timeout alone cannot prove that resources were released.

## Two-to-three-minute interview answer

I would allocate time from the user journey inward. If the whole request has an 800 ms target, I cannot give every dependency 800 ms independently. I would reserve time for local processing and returning the response, then pass a remaining allowance along the dependency chain. Each call gets the smaller of its configured cap and the allowance still available. The exact values would come from observed latency distributions and product requirements, rather than copying the illustrative numbers in this lesson.

Retries need both a time limit and an extra-load limit. I would give one layer ownership of retries, include automatic SDK attempts, classify transient failures, and require safe repetition. For an unsafe create with an unknown outcome, I would need idempotency before retrying. Backoff with jitter spreads attempts, but it does not create capacity; under sustained overload, retrying less can be the correct decision.

When the budget is exhausted, I would cancel work where possible and use only a product-approved fallback. A timeout does not prove that downstream processing stopped. For this backend, I would first measure the existing database-backed journey and avoid claiming remote-service policies are already implemented. In a future ranking service, I would inject slow responses and verify bounded attempts, acceptable feed fallback, latency, and absence of runaway background work.


## Before moving on

Artifact: `READY`; comprehension: `TODO`. Explain this scenario in your own words, predict the paper drill, and attempt the questions before opening their answers. Record learning only from your own explanation and predictions.

## Questions and explained answers

<details>
<summary>Why is an 800 ms timeout at every hop insufficient?</summary>

Sequential hops and retries can each consume their own allowance. A shared remaining budget bounds the whole journey.
</details>

<details>
<summary>How many final calls can three layers each making three attempts create?</summary>

At worst 3 × 3 × 3 = 27 attempts at the final dependency for one top-level operation, if failures trigger every loop.
</details>

<details>
<summary>Does a caller timeout cancel the downstream operation?</summary>

Not necessarily. Cancellation must propagate and be supported; otherwise work or side effects can continue after the caller stops waiting.
</details>
