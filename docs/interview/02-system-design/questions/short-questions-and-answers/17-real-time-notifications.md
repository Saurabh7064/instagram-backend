# Real-Time Notifications

## Question

Users expect a like notification almost immediately when they are online, but an offline user must still see every important notification after reconnecting. Retries must not create visible duplicates.

**“How would you deliver real-time notifications without losing offline events or duplicating them?”**


## The user went offline just before the send

User A likes post 42. User B should receive a notification, but B's phone disconnects just before the server sends it. A live connection alone leaves nowhere reliable to retrieve that notification later.

Create a **durable inbox**: stored notification records whose lifetime does not depend on a socket. A live delivery becomes a quick way to announce a record that already exists. A hotel front desk can record a message and also phone the guest; the unanswered call does not erase the record. Software adds repeated deliveries and several devices, so identity and ordering must be explicit.

## Store once, attempt delivery as needed

In our proposed flow, the committed like has a durable `PostLiked` event. A notification worker inserts `n-900` for B under a uniqueness rule tied to the recipient and stable event identity. Reprocessing that event finds the same notification instead of inventing another one.

If B is connected, a gateway sends `n-900`. **WebSocket** supports two-way communication on a long-lived connection; **server-sent events (SSE)** send a server-to-client event stream over HTTP. Either can make the notification appear quickly, but neither replaces the inbox.

The device may display `n-900` and lose its acknowledgement. The server can then send it again. **At-least-once delivery** allows such repeats; the worker and device need duplicate detection to keep one user-visible notification.

## Reconnect from a durable position

B last recorded position `n-850`. After reconnecting, the client asks for eligible inbox records after that position and receives `n-900`. This saved position is a **cursor**. It needs a defined server ordering, pagination and retention policy; do not assume an arbitrary string ID sorts into creation order.

Keep delivery progress separate from read state. Receiving or displaying a notification is not the same as the person opening it. Multiple devices may also have different delivery positions while sharing account-level read state.

Polling is a simpler option when modest delay and request volume are acceptable. A live gateway adds connection capacity, reconnection and authentication work. Whichever channel we choose, the durable inbox remains the recovery path.

## Where this appears in our backend

The current like route is defined in [PostController](../../../../../src/main/java/com/instagram/backend/controller/PostController.java), and [PostService](../../../../../src/main/java/com/instagram/backend/service/PostService.java) commits the `PostLike` and counter update synchronously. [PostLike](../../../../../src/main/java/com/instagram/backend/domain/PostLike.java) prevents the same user from liking the same post twice.

**Proposed, not implemented:** an outbox event, durable notification table, idempotent worker, WebSocket/SSE gateway, device-token service, per-user cursor, and read-state API. A broker alone must not be treated as the user-visible inbox.

## Choosing the approach and paying its costs

- Choose WebSocket/SSE when active clients need low latency. Do not rely on it for durable history; long-lived connections add gateway capacity and reconnect complexity.
- Choose polling when traffic is modest or simplicity matters. It is resilient but adds repeated requests and notification delay.
- Choose at-least-once processing with deduplication for practical reliability. Claiming exactly-once delivery across the database, broker, gateway, and device hides unavoidable failure windows.


## Paper drill: every phone reconnects together

Predict the load after a gateway outage ends and all clients immediately reconnect and request missed notifications. Connection setup, authentication and inbox reads can spike together.

Detect that combined pattern, then limit admission and spread client retries with bounded delays and random variation. Polling can help only with its own request limits; uncontrolled polling can reproduce the same overload.

Recover by restoring connections gradually while clients catch up from retained positions. Test a repeated `n-900` during recovery and verify it appears once in the UI. Prevent recurrence with reconnect-shaped load tests, cursor-expiry behavior and reserved capacity for catch-up work.

## Two-to-three-minute interview answer

I would make the notification inbox durable and treat real-time delivery as an acceleration path. A committed action such as a like writes an outbox event. An idempotent worker creates one notification row keyed by a stable event ID, then attempts delivery to active WebSocket or SSE sessions and optionally to a mobile push provider. If the user is offline or the acknowledgement is lost, the row remains available.

Each client keeps a cursor and asks for notifications after that cursor when it reconnects. Because brokers and push providers commonly provide at-least-once delivery, both the worker and client deduplicate by notification or event ID. Product-approved collapsing can combine noisy likes, but important events should remain independently traceable. I would monitor oldest undelivered age, duplicate suppression, active connections, reconnect rate, and provider errors. During a gateway failure, durable catch-up and polling preserve correctness while real-time latency degrades.


## Before moving on

Artifact: `READY`; comprehension: `TODO`. Explain this scenario in your own words, predict the paper drill, and attempt the questions before opening their answers. Record learning only from your own explanation and predictions.

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
