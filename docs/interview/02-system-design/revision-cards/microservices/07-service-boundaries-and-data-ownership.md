# Service Boundaries and Data Ownership — Interview Revision Card

> **Question:** How do you choose microservice boundaries and data ownership?
>
> [Detailed source](../../questions/microservices/07-service-boundaries-and-data-ownership.md)

## Key requirements

- Keep business rules and their protected data together.
- Enable normal changes without coordinated releases.
- Prevent cross-service database writes.

## Scale assumptions

- Split only where ownership, scaling, or reliability justifies distributed cost.

## Components

- A **business capability** is an outcome with cohesive rules; an **invariant** must remain true; a **data owner** alone authorizes writes.
- Service API/events, local database, read projection, and contract tests.

## Request flow

1. Map journeys, decisions, writes, and ownership.
2. Group behavior that changes together and protects one invariant.
3. Give one service authority over its records.
4. Other services call its API or consume published facts.
5. Build projections for frequent cross-service reads.

## Data model

- Identity owns `User`; Post owns `Post(authorId, ...)`; Feed owns a derived projection, not the source post.

## Three trade-offs

1. Owner calls give freshness but couple availability and latency.
2. Replicated projections improve reads but introduce lag and repair work.
3. Larger services reduce network coordination but limit independent scaling.

## Three failures and mitigations

1. **Shared-table writes:** enforce one owner and migrate callers behind contracts.
2. **Chatty boundary:** move cohesive behavior together or publish a projection.
3. **Stale copy:** expose freshness, version events, and reconcile.

## Two-minute spoken answer

I do not create one service per controller or table. I map business journeys, decisions, invariants, data writes, and team ownership, then group behavior that changes together. A useful boundary lets one team make a normal business change without coordinating several deployments.

Each record has one write owner. Other services use its API for fresh decisions or consume events into local read projections; they never update the owner’s tables directly. For this project, Post owns captions and post lifecycle, Identity owns accounts, and a future Feed service could own a derived timeline keyed by post and author IDs. Synchronous calls trade independence for freshness; projections trade freshness for read availability. I begin with modular boundaries in the monolith and extract only where scale, reliability, or ownership repays network, observability, and consistency complexity.

## Recall questions

1. Why is one service per table a weak boundary?
2. Who may change a service-owned record?
3. When is a read projection worth its lag?
