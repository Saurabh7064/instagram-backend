# Micro-lesson 14 — Idempotency keys and delivery guarantees

[Module](./README.md)

- Artifact: `READY`; comprehension: `TODO`.
- Time: 10–15 minutes. Primary idea: idempotency keys and delivery guarantees.
- Prerequisite: Complete the stop/go check for [Sagas: recover a multi-step business operation](./13-sagas.md).
- Original roadmap topics: 21–22.

## Why does this exist? — Must understand

An idempotency key identifies one intended operation across retries. At-most-once processing avoids repeats but can lose work; at-least-once retries can repeat work. Exactly-once is a scoped guarantee about effects, requiring atomic state management rather than merely reliable delivery.

## Analogy

A ticket number lets a clerk retrieve the same completed order when asked again. Unlike paper tickets, concurrent requests require an atomic claim and durable result.

## Predict before reading

Does at-least-once mean duplicate business effects are unavoidable? Explain your prediction before expanding the checkpoints below.

## Concrete flow

1. Client submits key K.
2. proposed backend atomically stores the post and K’s result.
3. response is lost.
4. client retries K.
5. backend returns the original post ID. Store a request fingerprint, scope K by user/operation, and reject the same K with a different payload.

## Instagram-backend mapping

[PostService.create, feed, and transactional methods](../../../../../src/main/java/com/instagram/backend/service/PostService.java) are the real database boundary. Replication, consensus, distributed locks, durable request-key handling, and multi-service workflows in this lesson are **proposed**, not implemented by these annotations.

## Decision and trade-off

Use durable deduplication for retryable creates and consumers. Keep simple reads naturally retryable. Keys need retention and concurrency rules; a database record cannot alone deduplicate a separate email provider’s effect.

## Failure drill — prediction, symptom, recovery, prevention

Predict a crash between effect and dedup record when they are separate commits. Observe duplicate posts after retry. Recover through reconciliation; prevent this gap by one transaction or a durable outbox plus receiver deduplication for external effects.

These are design/paper drills, not claims that a replica cluster or fault-injection lab was executed.

## Questions and explained answers

<details>
<summary>1. Does at-least-once mean duplicate business effects are unavoidable?</summary>

No. Atomic deduplication can make repeated deliveries produce one scoped effect.

</details>

<details>
<summary>2. What happens after key expiry?</summary>

A later retry may be treated as new; retention must match the promised retry window.

</details>

<details>
<summary>3. Does a broker guarantee cover external email?</summary>

Only if that external effect participates in the documented guarantee; otherwise separate coordination is required.

</details>

## Teach-back and stop/go

Explain the problem, trace the example, defend the choice, and diagnose the failure in 60–90 seconds without notes. Proceed only after a correct prediction, explanation, and failure diagnosis; reading this artifact does not complete comprehension.
