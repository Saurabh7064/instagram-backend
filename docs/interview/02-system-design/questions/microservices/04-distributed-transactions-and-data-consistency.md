# How are distributed transactions handled in microservices, and which patterns ensure data consistency?

> **Question:** How are distributed transactions handled in microservices, and discuss patterns for ensuring data consistency.

> [One-page revision card](../../revision-cards/microservices/04-distributed-transactions-and-data-consistency.md)

## The stock was reserved. The payment failed. What now?

Imagine checkout has reserved the last laptop for order `O-42`. Payment then declines the card. The order cannot be confirmed, but the inventory reservation is already real. Until something releases it, another shopper may be told the laptop is unavailable.

Inside one database, a transaction can make related changes succeed or roll back together. Here, Order, Inventory, and Payment each own their state. Payment may even belong to an outside company. Wrapping the calling Java method in `@Transactional` cannot make those independently committed operations one local database transaction.

We need to decide what the system may show while the work is incomplete, how it moves forward or repairs partial progress, and how it discovers work that got stuck. Choosing a pattern comes after those decisions.

## Think of booking with companies that cannot share an undo button

Imagine booking a flight, hotel, and rental car. The companies do not share one cash register or one undo button.

1. The flight is reserved.
2. The hotel rejects the request.
3. The system must cancel the flight or ask the customer to choose another hotel.

That cancellation is a **compensating action**. It is a new business operation; it does not travel backward in time and erase the first operation.

The analogy has limits. Software messages can be duplicated, arrive late or out of order, and be processed after a caller times out. Some real-world effects—an email already read, a package already shipped, or money already settled—cannot be perfectly undone.

## Each owner can commit its part; nobody has one undo button

A **local transaction** changes one service's database atomically: either all its included changes commit or none do. Order Service can save `O-42` and an outgoing event record this way. It cannot use that same local commit to control Inventory's database or the payment provider.

A **distributed transaction** is a business operation whose changes span those separately owned resources. To handle checkout, we can divide it into local steps and persist which step has completed. If a later step definitely fails, send a new business action to repair an earlier one—for example, release the stock reservation. A workflow built this way is called a **Saga**.

Other users may see the reserved stock while payment is still pending. We deliberately allow that temporary disagreement between completed business stages. This is **eventual consistency** only if the design includes a path to a valid end state: confirmation, cancellation, or an explicit unresolved case requiring review. It does not mean that the databases become correct on their own.

Two supporting ideas matter throughout the example. **Idempotency** makes repeating one logical command produce one intended effect, so a retried release does not release twice. **Reconciliation** compares authoritative records later to find and repair progress the normal workflow missed. We will see why both are needed when the payment response disappears.

## What must be true before we call the order confirmed?

For `O-42`, confirmation must mean that inventory is reserved and one payment authorization succeeded. The order must not be both confirmed and cancelled, and another delivery of the same checkout request must not create another order. These rules are the business **invariants**: conditions the workflow must preserve.

Now name the temporary states instead of hiding them. `PENDING` means the process has started but confirmation requirements are not yet satisfied. `CANCELLING` means a repair action still needs to finish. `REVIEW_REQUIRED` means the system cannot safely infer the next action.

```text
PENDING
   ├── inventory unavailable ───────────────> REJECTED
   └── inventory reserved
          ├── payment declined ─────────────> CANCELLING ──> CANCELLED
          └── payment authorized ───────────> CONFIRMED
                         unexpected outcome ─> REVIEW_REQUIRED
```

A pending order is not automatically corrupt. It is a valid intermediate state, provided it has a deadline, durable progress, and a recovery owner. The broken design is a pending order nobody can explain or resolve.

## Follow order O-42 through success and decline

Assume Order, Inventory, and Payment each own a database.

```text
Client
  |
  | POST /orders   Idempotency-Key: checkout-781
  v
Order Service ---- reserve command ----> Inventory Service
     |                                      |
     |<--- inventory-reserved event --------|
     |
     +------ authorize command ----------> Payment Service
     |                                      |
     |<--- payment-authorized event --------|
     |
     +--> mark order CONFIRMED
```

Successful flow:

1. Order Service creates order `O-42` as `PENDING` and records the idempotency key.
2. Inventory Service records reservation `R-42`; it does not immediately pretend the sale is final.
3. Payment Service authorizes payment `P-42` using `O-42` as its idempotency key.
4. Order Service marks `O-42` as `CONFIRMED`.
5. Fulfilment begins only after the confirmed event.

Failure flow when payment is declined:

1. Inventory has already reserved the item.
2. Payment emits `PaymentDeclined`.
3. The Saga requests `ReleaseInventory(O-42)`.
4. Inventory releases `R-42` idempotently.
5. Order becomes `CANCELLED`.

This is not global all-or-nothing isolation. Another shopper may temporarily see less available inventory while `O-42` is pending. The reservation and expiry rules make that temporary state intentional.

## Who decides which checkout step runs next?

### Choreography: services react to events

```text
OrderCreated
     |
     v
Inventory Service -- InventoryReserved --> Payment Service
     ^                                          |
     |---------- ReleaseInventory <--- PaymentDeclined
```

There is no single workflow coordinator. Each service knows which event it consumes and which event it emits.

Use choreography when the flow is short, ownership is clear, and adding a participant does not make the process difficult to understand. Its risks are hidden coupling, event cycles, scattered timeout logic, and no obvious place to answer, “Where is order `O-42` in the process?”

### Orchestration: one component directs the workflow

```text
                  +-------------------+
                  | Checkout Saga     |
                  | Orchestrator      |
                  +---------+---------+
                            |
             +--------------+--------------+
             |              |              |
          Reserve        Authorize       Confirm /
          inventory      payment         Compensate
             |              |              |
        Inventory        Payment          Order
```

The orchestrator persists Saga state, sends commands, receives results, applies deadlines, and chooses the next action. It coordinates the process but should not reach into participant databases or own their business rules.

Use orchestration when the workflow has several steps, branches, timeouts, or manual-review paths. The trade-off is an additional critical component and the risk of turning it into an oversized central service.

| Question | Choreography | Orchestration |
|---|---|---|
| Where is workflow state? | Spread across event consumers | Explicitly persisted by orchestrator |
| Coupling style | Services know events | Orchestrator knows sequence and commands |
| Best fit | Small, simple reaction chains | Multi-step workflows and compensations |
| Main failure risk | Invisible event web | Central coordinator bottleneck or “god” workflow |

## Saving the order and announcing it must not split

This code is unsafe:

```text
1. INSERT order into Order DB       succeeds
2. Publish OrderCreated to broker   fails
```

The order exists, but no other service hears about it. Reversing the operations is also unsafe: a message could be published and then the database write could fail.

With an **outbox**, Order Service writes both records in the same local database transaction:

```text
Order database transaction
  ├── INSERT orders(id='O-42', status='PENDING')
  └── INSERT outbox(event_id='E-91', type='OrderCreated', payload=...)
                     |
                     | committed rows read by a publishing relay
                     v
                 Message broker
```

If the transaction rolls back, neither record exists. If it commits, a relay can keep retrying publication. The relay may publish the same event more than once—for example, if it publishes successfully and crashes before marking the outbox row sent—so consumers must still be idempotent.

## Publishing safely still allows duplicate delivery

An **inbox** records which message IDs a consumer has already handled. The inbox row and the consumer's business update must share one local transaction.

```text
BEGIN
  INSERT inbox(event_id='E-91')       -- unique constraint
  INSERT inventory_reservation(...)
COMMIT
```

On redelivery, the unique constraint rejects or identifies `E-91`, so the business change is not repeated.

Idempotency also applies to synchronous APIs. A payment request can carry `Idempotency-Key: order-O-42`. Payment Service stores the key and result. A retry returns the stored authorization instead of charging again.

Important details:

- scope the key to the caller and operation;
- store a request fingerprint so the same key cannot silently represent different input;
- retain the key long enough to cover realistic retries and delayed messages;
- enforce uniqueness in durable storage, not only in process memory;
- define what a repeated in-progress request returns.

“Exactly once” is usually achieved at the business-effect level through durable deduplication and idempotency, not by assuming the network delivers a message exactly once.

## Releasing stock is a new action, not time travel

A compensating action applies business meaning:

| Completed action | Possible compensation | Why it is not a true rollback |
|---|---|---|
| Reserve stock | Release reservation | Another request may observe availability changes |
| Authorize card | Void authorization | Provider may return an uncertain or delayed result |
| Capture payment | Issue refund | Fees, settlement, and customer-visible history remain |
| Send email | Send correction | The original email cannot be unread |
| Ship parcel | Request return | Physical delivery may already be in progress |

Every compensation must itself be retryable and idempotent. For irreversible or ambiguous effects, transition to `REVIEW_REQUIRED` instead of inventing a false automatic rollback.

## What if payment succeeded but its answer disappeared?

Suppose Payment Service processes the authorization, but its response is lost:

```text
Order Service --- authorize O-42 ---> Payment Service
Order Service <--- response lost ----- Payment Service committed SUCCESS
                  timeout
```

A timeout says, “the caller did not receive an answer in time.” It does **not** prove the operation failed.

Safe recovery is:

1. retry using the same idempotency key, or query the status by that key;
2. never issue a new payment ID merely because the first call timed out;
3. keep the order pending while the result is uncertain;
4. escalate to reconciliation or manual review after a defined deadline.

## What if the normal workflow never finishes?

Suppose payment's records show that `O-42` was authorized, but Order still says `PENDING` ten minutes later. Retrying message delivery may resolve it; a bug or missing event may not. A periodic reconciliation process compares order, reservation, and provider state and asks whether the confirmation rules hold.

If payment succeeded and the reservation is still valid, the workflow may safely advance under its state-transition rules. If the order is cancelled but inventory remains reserved, it can retry the release. If payment is uncertain or inventory has already been given to someone else, it must not guess that confirmation is safe.

Each repair needs an audit trail and the same duplicate protection as the ordinary workflow. Automate only actions the observed facts justify, bound their rate, and send ambiguous cases to `REVIEW_REQUIRED`. Monitoring the number and age of those cases gives an operator an actual queue of unresolved business work, rather than a vague alert that consistency is “eventual.”

## What about two-phase commit (2PC)?

In 2PC, a coordinator asks all participants to **prepare**, then tells all of them to **commit** only when everyone is ready.

```text
Coordinator ---- PREPARE ----> DB A, DB B
Coordinator <--- READY ------- DB A, DB B
Coordinator ---- COMMIT -----> DB A, DB B
```

2PC can provide stronger atomicity across compatible transactional resources, but it introduces important costs:

- participants can hold locks while waiting;
- coordinator or network failures can block progress;
- every participant must support the protocol;
- availability and latency become coupled across services;
- external systems such as payment providers usually do not participate;
- independent deployment and operational ownership become harder.

It is not forbidden. It can be reasonable inside a tightly controlled boundary with compatible resources and a requirement for atomicity. It is usually a poor default for independently operated microservices. Prefer one local transaction per service plus Saga, outbox/inbox, idempotency, and reconciliation when temporary intermediate states are acceptable.

## Project mapping

The current application is a modular monolith, so [`PostService.create`](../../../../../src/main/java/com/instagram/backend/service/PostService.java) can use one Spring `@Transactional` boundary for post data. That is simpler and safer than introducing a Saga without a real service boundary.

[`MediaStorageService.store`](../../../../../src/main/java/com/instagram/backend/service/MediaStorageService.java) writes a file separately from the post database. If media upload and post creation later become one asynchronous cross-service workflow, a proposed design could be:

1. create the post as `MEDIA_PROCESSING` with an idempotency key;
2. write `PostMediaRequested` to an outbox in the same transaction;
3. let Media Service process the object idempotently;
4. mark the post `READY` after `MediaReady`;
5. mark it `MEDIA_FAILED` or allow retry on permanent failure;
6. reconcile posts that remain processing beyond a deadline.

Do not claim this is already implemented. It is a learning extension for the current [`PostController`](../../../../../src/main/java/com/instagram/backend/controller/PostController.java) and service boundaries.

## Choose the mechanism for the failure gap

| Need | Prefer | Trade-off |
|---|---|---|
| Atomic changes inside one service database | Local transaction | Does not span other owners |
| Multi-step business workflow | Saga | Intermediate states and compensation logic |
| Reliable database-change publication | Transactional outbox | Relay, storage, duplicate delivery |
| Safe event consumption | Inbox/deduplication + idempotent handler | Retention and key-management overhead |
| Temporary hold before confirmation | Reservation with expiry | Capacity temporarily unavailable |
| Repair missed or ambiguous work | Reconciliation | Detection delay and operational tooling |
| Strict atomicity across compatible resources | 2PC, only after careful justification | Blocking, coupled availability, limited participants |

## Failure drill

**Scenario:** Inventory reserved one laptop. Payment authorization succeeded, but the response and event were lost. The orchestrator timed out and sent both a retry and a release-inventory command.

1. **Predict:** without stable IDs, the retry may charge twice and the release may make stock available for another buyer while the first order is later confirmed.
2. **Observe:** order remains pending, payment provider shows one or two authorizations, and reservation state disagrees with order state.
3. **Contain:** stop fulfilment while the outcome is ambiguous; do not guess that timeout means failure.
4. **Recover:** query/retry payment with the original idempotency key and inspect whether the conditional release already committed. If inventory was released, do not confirm against that missing reservation: cancel and void/refund the payment, or successfully reserve again before confirmation. Send an unsafe or ambiguous case to `REVIEW_REQUIRED`.
5. **Prevent:** use persisted Saga state, outbox/inbox, stable operation IDs, version-checked state transitions, idempotent reserve/release commands, reservation expiry, alerts, and reconciliation.

## A 2–3 minute interview answer

In microservices, I would avoid stretching one database transaction across independently owned services. I would first define business invariants—such as “one checkout key creates at most one order” and “a confirmed order has reserved inventory and one successful payment”—then define valid intermediate states such as `PENDING`, `CANCELLING`, and `REVIEW_REQUIRED`.

I would implement checkout as a Saga of local transactions. For a multi-step flow with branches and deadlines, I would normally choose orchestration so one durable workflow record shows the current step; a short, stable reaction chain could use choreography. Order Service would create a pending order and an outbox event in one transaction. Inventory would reserve stock idempotently, and Payment would authorize using the order ID as a durable idempotency key. Each consumer would record the event ID in an inbox, in the same local transaction as its business change, because outbox delivery is at least once and duplicates are expected.

For a concrete failure, suppose payment authorizes the card but its response is lost. The timeout does not mean payment failed. The orchestrator keeps the order pending, queries or retries with the same key, and prevents fulfilment until it knows the result. If payment definitely declined, it sends an idempotent release-inventory compensation and cancels the order. If the outcome stays ambiguous, reconciliation compares order, reservation, and provider records and moves the case to manual review rather than guessing.

The trade-off is that a Saga improves availability and service autonomy but exposes temporary states and requires compensation, deduplication, monitoring, and repair logic. Compensation is a new business action, not a true rollback; a refund cannot erase fees or history. I would use 2PC only inside a controlled group of compatible resources when strict atomicity is worth blocking, lock duration, and coupled availability. For most service and external-provider workflows, local transactions plus Saga, outbox/inbox, idempotency, and reconciliation are the safer operational choice.

Before marking this answer practiced, explain `O-42` through payment decline and lost response without notes, including what other shoppers can observe. Predict the repair, then try the questions below before opening them. The described Saga is a design exercise, not an implemented project workflow.

## Questions and explained answers

<details>
<summary>1. Why are an outbox and an idempotent consumer both necessary?</summary>

The outbox prevents a committed database change from permanently losing its event. It does not prevent duplicate publication: a relay can crash after sending but before recording success. The idempotent consumer or inbox prevents that duplicate delivery from repeating the business effect. They solve different sides of the delivery gap.

</details>

<details>
<summary>2. If a payment call times out, should the Saga compensate immediately?</summary>

Not automatically. The payment may have succeeded and only its response may be missing. The Saga should query or retry with the same idempotency key, keep the order in an explicit uncertain state, and compensate only after it obtains a trustworthy result or follows a documented provider-specific recovery rule. Otherwise it can refund or release inventory while a valid charge exists.

</details>

<details>
<summary>3. What is the difference between Saga compensation and rollback?</summary>

A database rollback makes uncommitted changes invisible inside one transaction. A Saga compensation is a later committed business action, such as refunding payment or releasing a reservation. Observers may have seen the original action, and some effects cannot be perfectly reversed, so compensation needs business rules, audit history, idempotency, and sometimes human review.

</details>

<details>
<summary>4. When would you choose orchestration over choreography?</summary>

Choose orchestration when the workflow has several participants, branching, deadlines, compensations, or a need to answer where each transaction currently stands. Choreography is attractive for a small, stable event chain, but as the chain grows its control flow and recovery rules become difficult to see. The orchestrator should coordinate state transitions without taking ownership of every service's domain logic.

</details>

<details>
<summary>5. Does eventual consistency permit incorrect data?</summary>

It permits defined temporary states, not arbitrary corruption. A pending order and a temporary inventory reservation can be valid while payment completes. The design must specify invariants, deadlines, idempotent progress, compensation, and reconciliation so every incomplete operation has a path to a valid terminal state or explicit manual review.

</details>
