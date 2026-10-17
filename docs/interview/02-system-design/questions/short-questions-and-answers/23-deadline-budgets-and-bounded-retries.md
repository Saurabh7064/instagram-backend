# Deadline Budgets and Bounded Retries

## Question

How do you assign timeouts and retries across a chain of dependent services?

## Why this problem exists

Each component can wait for a duration that seems reasonable locally while the whole request exceeds the user’s useful waiting time. Independent retry loops can also multiply traffic during an outage.

## Analogy and its limit

A connecting journey must fit within one arrival deadline; each leg cannot spend the entire journey allowance. The analogy stops because timed-out computing work may continue unless cancellation is supported and observed.

## Terms

- A **deadline** is the latest useful completion time for an operation.
- A **retry budget** limits extra attempts by count or load as well as remaining time.
- **Jitter** randomizes retry delays to reduce synchronized repeated traffic.

## Reasoning and design

1. Start from the user-facing completion target and reserve time for local work and returning the response.
2. Carry the remaining allowance to downstream calls, accounting for time already spent. Each call uses the smaller of its own cap and the remaining allowance.
3. Retry only failures classified as transient, and only when repeating the operation is safe.
4. Make one layer own retries and include any SDK or proxy attempts in the budget.
5. Stop when another attempt cannot fit; cancel remaining work where supported and choose a documented fallback.

## Concrete trace

1. In this proposed example, the request has 800 ms available. Local processing uses 100 ms and response handling reserves 100 ms: downstream work can use 800 − 100 − 100 = 600 ms.
2. The first safe read attempt uses its 200 ms cap and fails transiently. Remaining allowance is 600 − 200 = 400 ms.
3. A jittered delay happens to be 50 ms. Remaining allowance is 400 − 50 = 350 ms.
4. The second attempt has a 200 ms cap and succeeds in 150 ms. Remaining allowance is 350 − 150 = 200 ms.
5. Total elapsed before response reserve is 100 + 200 + 50 + 150 = 500 ms. With up to 100 ms for response handling, this example finishes within 600 ms. These are illustrative allocations, not measured production settings.

## Instagram-backend mapping

[PostService](../../../../../src/main/java/com/instagram/backend/service/PostService.java) currently serves the database-backed post flow; it is not a chain of remote ranking and recommendation services. [JourneyMetricsInterceptor](../../../../../src/main/java/com/instagram/backend/observability/JourneyMetricsInterceptor.java) is a real starting point for measuring request journeys.

**Proposed:** downstream deadline propagation, coordinated retry budgets, and optional ranking fallback if remote dependencies are introduced. Database query and connection-acquisition limits also need their own supported configuration; an HTTP timeout alone does not bound all database work.

## Choices and trade-offs

- Retry transient failures when another attempt fits and the operation is safe. Avoid retrying validation failures or ambiguous unsafe writes.
- Use jitter and bounded attempts to spread repeated traffic; extra attempts still consume capacity. AWS explains this risk in [timeouts, retries, and backoff with jitter](https://d1.awsstatic.com/builderslibrary/pdfs/timeouts-retries-and-backoff-with-jitter.pdf).
- Choose a fallback only when the product allows reduced results, such as a recent-post feed instead of personalized ranking. Do not turn authorization failure into a successful fallback.
- Aggressive timeouts reduce waiting but can reject healthy slow requests; loose timeouts tie up resources. Tune from measured distributions and the end-to-end target.

## Failure drill

**Prediction:** slow the proposed ranking dependency beyond its cap. **Detection:** observe dependency timeouts, request latency, extra-attempt counts, and whether work continues after caller expiry. **Containment:** stop retries that exceed time or load budgets and serve an allowed fallback. **Recovery:** restore the dependency and gradually reintroduce normal traffic while checking saturation. **Prevention:** test slow responses and lost responses, include hidden client retries in accounting, and verify cancellation rather than assuming it.

## Two-to-three-minute interview answer

I would allocate time from the user journey inward. If the whole request has an 800 ms target, I cannot give every dependency 800 ms independently. I would reserve time for local processing and returning the response, then pass a remaining allowance along the dependency chain. Each call gets the smaller of its configured cap and the allowance still available. The exact values would come from observed latency distributions and product requirements, rather than copying the illustrative numbers in this lesson.

Retries need both a time limit and an extra-load limit. I would give one layer ownership of retries, include automatic SDK attempts, classify transient failures, and require safe repetition. For an unsafe create with an unknown outcome, I would need idempotency before retrying. Backoff with jitter spreads attempts, but it does not create capacity; under sustained overload, retrying less can be the correct decision.

When the budget is exhausted, I would cancel work where possible and use only a product-approved fallback. A timeout does not prove that downstream processing stopped. For this backend, I would first measure the existing database-backed journey and avoid claiming remote-service policies are already implemented. In a future ranking service, I would inject slow responses and verify bounded attempts, acceptable feed fallback, latency, and absence of runaway background work.

## Practice status

Artifact: `READY`. Comprehension: `TODO`. Before marking practiced, explain the answer without notes, predict a failure, and answer the checkpoints before revealing their explanations.

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
