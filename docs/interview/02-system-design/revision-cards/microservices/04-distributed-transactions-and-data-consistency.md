# Distributed Transactions and Consistency — Interview Revision Card

> **Question:** How are distributed transactions handled in microservices, and which patterns keep data consistent?
>
> [Detailed source](../../questions/microservices/04-distributed-transactions-and-data-consistency.md)

## Key requirements

- Preserve business invariants across service-owned databases.
- Make partial progress, duplicates, and recovery visible.
- Avoid indefinite locks across network calls.

## Scale assumptions

- Services deploy independently and messages may be delayed or delivered more than once.

## Components

- A **Saga** is a sequence of local transactions with recovery actions; an **outbox** stores a state change and its event atomically; **reconciliation** repairs missed outcomes.
- Order, Inventory, and Payment services plus a durable broker.

## Request flow

1. Create order as `PENDING` with workflow ID.
2. Commit order and outbox event in one local transaction.
3. Reserve inventory, then authorize payment idempotently.
4. Confirm the order when all required steps succeed.
5. On failure, release reversible reservations; reconcile uncertain steps.

## Data model

- `Order(id, status, version)`; `Outbox(eventId, aggregateId, type, payload)`; processed `eventId` per consumer.

## Three trade-offs

1. Orchestration centralizes flow but can become a coordination bottleneck.
2. Choreography reduces a central controller but hides end-to-end state.
3. Two-phase commit offers stronger atomicity but couples availability and participants.

## Three failures and mitigations

1. **Commit before publish:** transactional outbox and relay retry.
2. **Duplicate event:** consumer inbox/idempotent state transition.
3. **Irreversible step:** reserve first; use pending state and reconciliation.

## Two-minute spoken answer

I avoid holding one transaction across services. Each service commits only its owned data, while a Saga tracks the business operation through explicit pending, confirmed, or failed states. The initiating service writes its state and an outbox event in one database transaction; a relay publishes it. Consumers deduplicate by event ID and apply valid state transitions.

For checkout, inventory is reserved before payment is captured. A later failure releases the reservation; an irreversible action may require refund or manual review rather than pretending it can be undone. Orchestration is easier to observe for complex flows, while choreography suits a few loosely coupled reactions. Timeouts leave outcomes uncertain, so status queries and reconciliation compare source records with workflow state. The trade-off is eventual completion and more operational machinery in exchange for local autonomy and failure recovery.

## Recall questions

1. What problem does the outbox solve?
2. Why is compensation not rollback?
3. When would orchestration be clearer than choreography?
