# Idempotency and Duplicate Requests — Interview Revision Card

> **Question:** How do you make retries safe and prevent duplicate business effects?
>
> [Detailed source](../../questions/microservices/08-idempotency-retries-and-duplicate-requests.md)

## Key requirements

- Produce one effect per logical operation.
- Return the same known outcome after retry.
- Handle concurrent duplicates and expired records.

## Scale assumptions

- Networks lose responses and brokers redeliver; duplicates are normal even at modest traffic.

## Components

- An **idempotency key** identifies one client operation; an **inbox** records consumed events; a unique constraint arbitrates races.
- Transactional result store, request fingerprint, status lookup, and retention policy.

## Request flow

1. Client creates a stable key and sends it with the write.
2. Server inserts `IN_PROGRESS` with request fingerprint atomically.
3. The business write commits with the idempotency outcome.
4. A duplicate returns the stored result; mismatched payload is rejected.
5. Uncertain or stale records are reconciled before retry.

## Data model

- `Idempotency(key, actorId, requestHash, status, resourceId, response, expiresAt)` with a unique key.

## Three trade-offs

1. Stored responses improve retry UX but cost storage and protect sensitive data.
2. Longer retention catches late retries but increases cleanup cost.
3. Natural idempotency simplifies state but is unavailable for every operation.

## Three failures and mitigations

1. **Concurrent duplicates:** unique constraint and transactional winner.
2. **Same key, different payload:** compare fingerprint and reject.
3. **Crash after effect:** commit effect and outcome atomically or reconcile.

## Two-minute spoken answer

A timeout does not prove failure, so every retriable write needs a stable operation identity. The client sends an idempotency key scoped to its account. The server stores the key, request fingerprint, state, and result behind a unique constraint. The winning request performs the business write and records the outcome atomically; concurrent or later duplicates return that outcome. Reusing the key with another payload is an error.

Consumers use the same idea by recording event IDs in an inbox with their local state change. Retention follows the maximum retry/replay window, with safe cleanup. If the external effect cannot share a transaction, the operation remains pending and reconciliation queries the provider. This adds storage and state-machine complexity, but prevents duplicate posts, payments, and messages under normal retry behavior.

## Recall questions

1. Why is a timeout an ambiguous result?
2. What does the request fingerprint prevent?
3. How do concurrent duplicates choose one winner?
