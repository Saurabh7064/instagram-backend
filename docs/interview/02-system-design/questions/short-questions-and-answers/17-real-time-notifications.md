# Real-Time Notifications

## Question

Users expect a like notification almost immediately when they are online, but an offline user must still see every important notification after reconnecting. Retries must not create visible duplicates.

**“How would you deliver real-time notifications without losing offline events or duplicating them?”**

## Why this problem exists

A live connection is fast but temporary. Phones sleep, browsers disconnect, networks change, and push providers retry. If the live channel is the only record, offline users lose notifications; if every retry creates a new record, users see duplicates. Delivery therefore needs durable state as well as a real-time path.

## Analogy

A hotel records each message at the front desk and may also call the guest’s room. The call is fast, but the written record lets the guest retrieve the message later. The analogy stops because distributed delivery can retry concurrently and arrive out of order across several devices.

## Terms to establish

- A **durable inbox** stores notification state independently of a live connection.
- **At-least-once delivery** may send the same event more than once, so consumers must deduplicate.
- A **cursor** identifies the last notification a client has processed.
- **WebSocket** and **server-sent events** are long-lived server-to-client delivery channels.

## What I would do

1. Turn a committed business action into a durable event, preferably through a transactional outbox.
2. Create one notification record with a stable event ID and recipient ID.
3. Deliver that record through WebSocket/SSE for active sessions and mobile push for background devices.
4. Let reconnecting clients fetch records after their last cursor and acknowledge read state separately.
5. Deduplicate by event ID, collapse noisy events when product rules allow it, and expire only by an explicit retention policy.

## Concrete walkthrough

1. User A likes post 42; the like and `PostLiked` outbox row commit together.
2. A notification worker consumes the event and inserts notification `n-900` for User B, ignoring a repeated `eventId`.
3. If User B has a live connection, the gateway sends `n-900`; otherwise the durable row remains unread.
4. User B reconnects with cursor `n-850`, fetches newer records, and receives `n-900` once in the UI.
5. The observable result is durable catch-up even if the real-time send or its acknowledgement was lost.

## Instagram-backend mapping

The current like route is defined in [PostController](../../../../../src/main/java/com/instagram/backend/controller/PostController.java), and [PostService](../../../../../src/main/java/com/instagram/backend/service/PostService.java) commits the `PostLike` and counter update synchronously. [PostLike](../../../../../src/main/java/com/instagram/backend/domain/PostLike.java) prevents the same user from liking the same post twice.

**Proposed, not implemented:** an outbox event, durable notification table, idempotent worker, WebSocket/SSE gateway, device-token service, per-user cursor, and read-state API. A broker alone must not be treated as the user-visible inbox.

## Choices and trade-offs

- Choose WebSocket/SSE when active clients need low latency. Do not rely on it for durable history; long-lived connections add gateway capacity and reconnect complexity.
- Choose polling when traffic is modest or simplicity matters. It is resilient but adds repeated requests and notification delay.
- Choose at-least-once processing with deduplication for practical reliability. Claiming exactly-once delivery across the database, broker, gateway, and device hides unavoidable failure windows.

## Failure drill

**Prediction:** a network recovery triggers a reconnect storm. **Symptom and detection:** gateway connection attempts, authentication calls, and inbox reads spike together. **Containment:** apply admission limits, exponential backoff with jitter, and polling fallback. **Recovery:** restore connections gradually while clients catch up from durable cursors. **Prevention:** load-test reconnection, stagger client retries, and reserve capacity for catch-up traffic.

## Two-to-three-minute interview answer

I would make the notification inbox durable and treat real-time delivery as an acceleration path. A committed action such as a like writes an outbox event. An idempotent worker creates one notification row keyed by a stable event ID, then attempts delivery to active WebSocket or SSE sessions and optionally to a mobile push provider. If the user is offline or the acknowledgement is lost, the row remains available.

Each client keeps a cursor and asks for notifications after that cursor when it reconnects. Because brokers and push providers commonly provide at-least-once delivery, both the worker and client deduplicate by notification or event ID. Product-approved collapsing can combine noisy likes, but important events should remain independently traceable. I would monitor oldest undelivered age, duplicate suppression, active connections, reconnect rate, and provider errors. During a gateway failure, durable catch-up and polling preserve correctness while real-time latency degrades.

## Practice status

The artifact is ready; comprehension remains `TODO` until the learner can trace offline catch-up, explain deduplication, and diagnose a reconnect storm without reading.

## Questions and explained answers

<details>
<summary>Why is a WebSocket connection not enough to guarantee notification delivery?</summary>

The connection can disappear while the user is offline, the device sleeps, or the network changes. A durable inbox preserves the notification independently of that temporary channel.
</details>

<details>
<summary>Why can an acknowledgement still lead to a duplicate?</summary>

The receiver may process the message and lose the acknowledgement before it reaches the sender. The sender then retries, so the repeated event must be recognized by its stable ID.
</details>

<details>
<summary>What should happen when the real-time gateway is unavailable?</summary>

Notification creation should continue in durable storage. Clients can poll or catch up by cursor after recovery, so delivery becomes slower without losing the underlying events.
</details>
