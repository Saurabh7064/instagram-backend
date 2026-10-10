# Microservices Interview Questions

This module answers one interview question per file. Do not memorize the architecture as a script. Study one question, close the file, explain it aloud, predict a failure, and then answer the final questions before moving on.

For interview-week retrieval after learning the material, use the [≤400-word revision cards](../../revision-cards/README.md).

## Learning sequence

| Order | Interview question | Why it matters | Artifact | Comprehension |
|---:|---|---|---|---|
| 1 | [How would you decompose a monolith incrementally?](./01-decomposing-a-monolith.md) | Safe architecture evolution | `READY` | `TODO` |
| 2 | [How would you monitor and debug microservices in production?](./02-production-observability-and-debugging.md) | Finding failures across service boundaries | `READY` | `TODO` |
| 3 | [How would you handle latency, timeouts, and cascading failures?](./03-inter-service-communication-resilience.md) | Making network calls bounded and recoverable | `READY` | `TODO` |
| 4 | [How are distributed transactions and data consistency handled?](./04-distributed-transactions-and-data-consistency.md) | Correct multi-service workflows | `READY` | `TODO` |
| 5 | [What is bulkhead isolation?](./05-bulkhead-isolation.md) | Containing resource exhaustion | `READY` | `TODO` |
| 6 | [How would you design a fault-tolerant, resilient architecture?](./06-fault-tolerant-resilient-architecture.md) | Combining patterns into one defensible design | `READY` | `TODO` |
| 7 | [How do you choose service boundaries and data ownership?](./07-service-boundaries-and-data-ownership.md) | Avoiding a distributed monolith | `READY` | `TODO` |
| 8 | [How do you make retries safe and handle duplicate requests?](./08-idempotency-retries-and-duplicate-requests.md) | Preventing duplicate writes and payments | `READY` | `TODO` |
| 9 | [How do APIs and events evolve without breaking consumers?](./09-api-event-contract-evolution.md) | Safe independent deployment | `READY` | `TODO` |
| 10 | [How do you test and deploy microservices safely?](./10-testing-and-safe-deployment.md) | Catching integration failures and limiting release risk | `READY` | `TODO` |

The repeated fault-tolerant/resilient prompt from the source list is intentionally represented once in Question 6.

## Required answer shape

For every question, practice this order:

```text
Problem → requirements → design choice → request/failure flow
        → trade-off → detection/recovery → evolution
```

An answer is not demonstrated merely because the file exists. To move a question from `TODO` to `PRACTICING`, the learner must:

1. give the interview-ready answer without reading;
2. draw or narrate the main flow;
3. correctly predict one failure;
4. answer the final questions before revealing their explanations;
5. name at least one alternative and its trade-off.

Record evidence in [System Design Learner Progress](../../learner-progress.md).
