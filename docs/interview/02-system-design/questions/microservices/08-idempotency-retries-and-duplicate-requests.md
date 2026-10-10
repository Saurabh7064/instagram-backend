# 08 — Idempotency, Retries, and Duplicate Requests

> **Interview question:** How do you make retries safe and prevent duplicate requests or events from applying the same business operation more than once?

> [One-page revision card](../../revision-cards/microservices/08-idempotency-retries-and-duplicate-requests.md)

- Status: `READY`; comprehension not yet demonstrated
- Time: 20–25 minutes
- Primary idea: a retry is safe only when repeated delivery produces one intended business effect

## Why does this matter?

A timeout does not prove that an operation failed. The server may have committed the write and lost the response on the network. If the client retries `POST /payments` or `POST /posts`, the system can create two charges or two posts.

Message brokers also commonly provide **at-least-once delivery**: they try not to lose a message, but may redeliver it. Duplicate delivery is therefore normal, not an exceptional corner case.

## Analogy: a numbered claim ticket

You give a repair shop ticket `R-104` with one repair request. If you ask again using `R-104`, the shop shows the original repair status instead of opening another job. A different repair request cannot reuse `R-104`.

The analogy has limits. Distributed systems process concurrent requests, records can expire, a crash can happen between several writes, and events can arrive out of order. A ticket number alone is insufficient without atomic storage and clear scope.

## Plain-language model

### Idempotency

An operation is **idempotent** when repeating the same intended operation produces the same business effect as doing it once.

- `PUT /posts/42` setting a caption to “Sunset” can naturally be idempotent.
- `DELETE /posts/42` can be treated as successful even if the post is already gone.
- `POST /payments` is not naturally idempotent because each call could create a new charge.

An **idempotency key** gives a non-idempotent operation a stable identity, for example `create-post:user-7:attempt-91`.

### Retry

A **retry** is another attempt after a failure that might be temporary. Retry only failures likely to recover, such as a connection reset or `503 Service Unavailable`. Retrying invalid input, authorization failures, or a permanent business rejection wastes resources.

Use a maximum attempt count, an overall deadline, exponential backoff, and random jitter. Jitter prevents thousands of clients from retrying at the same instant.

### Deduplication

**Deduplication** records an operation or event ID so the same item is not applied twice. The important part is making the “have I processed this?” record atomic with the business update.

## Predict before reading

The post service saves a new post, but its response is lost. The client times out and retries with the same idempotency key. What must the service return?

<details>
<summary>Reveal the prediction</summary>

It should return the outcome of the first successful operation—the same post ID and compatible response—without inserting another post. A timeout is ambiguous, so the retry must look up the durable key rather than assume the first attempt failed.

</details>

## Concrete HTTP flow

Assume the client sends:

```http
POST /api/posts
Idempotency-Key: 4a83-user7-create-post

{"caption":"Sunset","imageUrl":"/uploads/m-42","locationLabel":"LA"}
```

The service handles it as follows:

1. Authenticate the caller. Scope the key to both caller and operation; another user may coincidentally use the same text.
2. Hash the normalized request body. This detects accidental reuse of a key for different input.
3. Begin a database transaction.
4. Try to insert an idempotency record with a unique constraint on `(user_id, operation, key)`.
5. If this is the first request, create the post and store the post ID and completed response reference in that record.
6. Commit the post and idempotency record atomically.
7. If the same key and same request arrive again, return the recorded outcome.
8. If the same key arrives with different input, return a conflict such as `409` instead of guessing the caller's intent.

The unique database constraint is essential. A check-then-insert implemented only in application memory allows two concurrent instances to see “missing” and both perform the operation.

### What if work is slow?

Store a status such as `IN_PROGRESS`, `SUCCEEDED`, or `FAILED`. A duplicate can receive the completed result, poll a status URL, or receive a retryable response while processing remains in progress. Define what happens to an abandoned `IN_PROGRESS` record; do not leave it ambiguous forever.

### What about key expiry?

Retaining every key forever is expensive. Keep keys at least as long as clients and intermediaries can legitimately retry. Payment keys may need a much longer audit window than feed refresh keys. After expiry, the API can no longer guarantee deduplication for that old attempt, so document the window.

## Concrete event-consumer flow

Suppose a broker redelivers `PostPublished(eventId=e-900, postId=42)`:

1. The feed consumer begins a local database transaction.
2. It inserts `e-900` into a `processed_events` table with a unique constraint.
3. It updates the feed projection.
4. It commits both changes together.
5. Only then does it acknowledge the broker message.

On redelivery, the unique insert says the event was already applied, so the consumer acknowledges it without updating the feed again.

If the consumer records `e-900` and then crashes **before** updating the feed in a separate transaction, the event is lost logically. That is why the deduplication record and business effect must share a transaction where possible.

For publishing events after a database write, use an outbox record committed with the business change. A relay can retry publishing the outbox safely. Producers use an outbox; consumers use an inbox/processed-event record. These solve opposite sides of the delivery gap.

## Retry ownership and retry storms

Choose one layer to own retries. If the browser retries three times, the gateway retries three times, and the service client retries three times, one request can produce 27 downstream attempts.

A defensible policy is:

- propagate one end-to-end deadline;
- reserve time for downstream work;
- retry only idempotent operations or operations protected by a key;
- use backoff and jitter;
- stop retrying when the remaining deadline cannot fit another attempt;
- expose retry counts and duplicate-key hits as metrics.

## Instagram-backend mapping

The current [PostController.java](../../../../../src/main/java/com/instagram/backend/controller/PostController.java) accepts `POST /api/posts`, and [PostService.java](../../../../../src/main/java/com/instagram/backend/service/PostService.java) inserts a new row for each call. It does not currently accept an idempotency key, so a client retry after an ambiguous timeout could create another post.

A future implementation could add the request header at the controller, preserve the existing validation in [CreatePostRequest.java](../../../../../src/main/java/com/instagram/backend/dto/CreatePostRequest.java), and atomically store the key beside the created post. This is a proposed learning improvement, not current behavior.

The existing `like` behavior checks whether the user already liked the post, and [PostLike.java](../../../../../src/main/java/com/instagram/backend/domain/PostLike.java) already declares the database-level uniqueness rule on `(post_id, user_id)`. That constraint is the reliable protection when two requests reach separate application instances; a future service boundary must preserve it and return a consistent “already liked” outcome for the losing insert.

## Decision and trade-off

| Choice | Prefer it when | Cost or limitation |
|---|---|---|
| Naturally idempotent API semantics | Repeating “set this state” expresses the operation correctly | Not every action can be modeled as replacement |
| Idempotency key plus stored result | A client may retry a write with financial or user-visible effect | Storage, expiry rules, request hashing, and concurrent-state handling |
| Database uniqueness constraint | A duplicate can be identified by a stable business key | Must decide how to return the original result |
| At-least-once event plus deduplicating consumer | Losing an event is worse than occasionally redelivering it | Consumer state and replay-aware processing |
| No automatic retry | The action is unsafe to repeat or the failure is permanent | Caller or operator must resolve ambiguity explicitly |

## Failure drill

**Scenario:** payment succeeds at the provider, the response times out, and the order service retries with a new key.

1. **Prediction:** the provider may create a second charge because it sees a new operation.
2. **Observable symptom:** two provider charge IDs refer to one order.
3. **Immediate recovery:** stop automated retries, query the provider by merchant/order reference, refund the duplicate if policy allows, and reconcile the order.
4. **Design correction:** generate one stable key per checkout attempt, persist it before calling the provider, reuse it across retries, and query status before creating another attempt.
5. **Monitoring:** alert on multiple successful charge IDs for one order and graph retries by dependency and outcome.

## Two-to-three-minute interview-ready answer

“I assume every request or message can arrive more than once, especially after a timeout. A timeout is ambiguous: the server may have committed the operation and only lost the response. For naturally idempotent operations, I prefer set-style `PUT` or delete semantics. For a non-idempotent action such as creating a payment or post, the client sends one stable idempotency key for that business attempt, scoped to the authenticated caller and operation.

The service stores the key under a database unique constraint, records a hash of the normalized request, and commits both the business change and idempotency result in one transaction. The first call creates the resource. A retry with the same key and payload returns the original resource ID and outcome without creating another. Reusing the key with different input returns a conflict. I also define what duplicates receive while work is in progress and how long keys are retained.

For asynchronous delivery, every event has a stable event ID. The consumer writes that ID to a processed-events table in the same local transaction as its business update, so redelivery becomes a no-op. The producer uses an outbox committed with its domain change, allowing publication to retry without losing the event.

I retry only transient failures, within an end-to-end deadline, using exponential backoff and jitter. One layer owns retries so browser, gateway, and service retries do not multiply into a storm. For example, if a payment provider succeeds but its response is lost, retrying with a new key may charge twice. I would reuse the original key, query payment status, contain duplicate automation, and reconcile provider charge IDs against the order. I monitor retry attempts, exhausted retries, duplicate-key hits, stuck in-progress records, and consumer age. The trade-off is extra durable state and cleanup, but it is cheaper than duplicate money movement or user-visible writes.”

## Questions and explained answers

<details>
<summary>1. Why is checking for an idempotency key and then inserting not enough?</summary>

Two instances can check at the same time, both see no row, and both perform the write. A database unique constraint makes the race resolve to one winner. The idempotency record and business effect should then commit atomically.

</details>

<details>
<summary>2. Should a client retry every timeout?</summary>

No. It should retry only when the operation is safe to repeat or protected by idempotency, the error is plausibly transient, and enough deadline remains. An ambiguous non-idempotent operation may require a status query rather than another creation request.

</details>

<details>
<summary>3. Why store a request hash with the key?</summary>

The hash detects a caller reusing one key for two different commands. Returning the first response for different input would hide a bug; executing the second would violate the one-operation guarantee. A conflict makes the mistake visible.

</details>

<details>
<summary>4. What is the difference between an outbox and consumer deduplication?</summary>

An outbox ensures a producer can eventually publish an event after committing its data. Consumer deduplication ensures a redelivered event does not apply the local effect twice. Reliable flows commonly need both because they protect different failure gaps.

</details>
