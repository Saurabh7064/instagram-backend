# Micro-Lesson 04 — Monolith-to-Microservices Migration

- Status: `READY`; comprehension not yet demonstrated
- Time: 20 minutes
- Primary idea: replace one business capability at a time behind stable contracts instead of rewriting the whole system
- New terms: domain boundary, strangler migration, anti-corruption layer
- Prerequisite: [Scalability, Availability, and Reliability](./01-scalability-availability-reliability.md)

## Why does this exist?

A legacy monolith may have tightly coupled modules, one shared database, slow deployments, obsolete frameworks, and little automated testing. A big-bang rewrite appears clean but asks a new system to reproduce years of hidden behavior before delivering value.

The migration problem is therefore not “How do I create services?” It is “How do I change ownership safely while users and developers continue operating the product?”

## Analogy: renovating an occupied apartment building

You do not demolish the building while residents are inside. You isolate one section, provide temporary routing, inspect hidden pipes, move residents safely, and keep a rollback route until the new section proves stable.

The analogy stops at database transactions, duplicated events, API versioning, and software requests that can be mirrored to both implementations.

## Plain-language model

- A **domain boundary** groups behavior and data that change for the same business reason, such as catalog, order, or payment.
- A **strangler migration** routes one capability at a time from the old system to a new implementation until the old path can be removed.
- An **anti-corruption layer** translates between the legacy model and the new model so legacy concepts do not leak everywhere.

## Predict before reading

A team extracts ten services at once from a pre-Java-6 Spring/Struts/JSP monolith, gives every service direct access to the original database, and switches all traffic on one release night. What risks remain even though the code is now deployed as services?

<details>
<summary>Reveal the prediction</summary>

The shared database preserves tight coupling and unclear ownership. The simultaneous cutover creates a large blast radius, hidden legacy behavior may be missing, rollback becomes difficult, and network calls add new latency/failure modes. This is a distributed monolith, not safe decomposition.

</details>

## Step-by-step migration

### 1. Establish safety before extraction

- inventory user journeys, integrations, scheduled jobs, and database writes;
- add characterization tests around current behavior, even when the behavior is imperfect;
- add request IDs, logs, metrics, and traces;
- identify rollback and data-recovery procedures.

### 2. Find a first boundary

Prefer a capability with:

- clear inputs and outputs;
- limited transaction coupling;
- measurable value or pain;
- a failure that can degrade safely;
- a team able to own it.

Media upload or read-only catalog search is usually safer than payment settlement as a first extraction.

### 3. Put a stable entry point in front

```text
Client → Gateway/facade
          ├─ legacy route
          └─ new service route
```

The client contract stays stable while routing changes behind it. This enables gradual traffic movement and rollback.

### 4. Separate behavior before data

Create an interface around legacy behavior. The new service initially may call the legacy system through an anti-corruption adapter. Avoid giving new services permanent write access to shared tables.

### 5. Move data ownership deliberately

Use one authoritative writer per business entity. During transition, publish changes with an outbox or change-data-capture flow to build a new read model. Avoid unconstrained dual writes because one write can succeed while the other fails.

### 6. Compare before switching

- mirror safe read traffic;
- compare old and new results;
- run contract and integration tests;
- send a small canary percentage to the new path;
- measure errors, latency, and business outcomes.

### 7. Cut over and remove the old path

Increase traffic gradually, retain a rollback window, stop old writes, reconcile data, and delete the legacy path only after evidence shows it is unused.

## Maintaining backward compatibility

### API compatibility

- prefer additive changes;
- keep old fields and endpoints during a deprecation window;
- translate old request/response shapes at the boundary;
- use consumer-driven contract tests for known clients.

### Event compatibility

- include schema versions;
- make consumers tolerant of new optional fields;
- do not change the meaning of an existing field silently;
- support replay and idempotent consumption.

### Database coexistence

- one writer owns each entity;
- replicate changes asynchronously for reads when possible;
- reconcile counts and checksums;
- never treat permanent shared-table access as service independence.

## Project mapping

This repository is still small enough to remain a modular monolith. [AuthService.java](../../../../src/main/java/com/instagram/backend/service/AuthService.java), [PostService.java](../../../../src/main/java/com/instagram/backend/service/PostService.java), [ProfileService.java](../../../../src/main/java/com/instagram/backend/service/ProfileService.java), and [MediaStorageService.java](../../../../src/main/java/com/instagram/backend/service/MediaStorageService.java) already suggest capability boundaries.

A sensible learning extraction would be media processing/storage because it can become asynchronous and independently scalable. Authentication and post ownership should remain together until there is a concrete scaling, security, or team-ownership reason to separate them.

Do not split these classes into network services merely to claim microservices. First enforce module boundaries and data ownership inside the monolith.

## How to minimize risk

| Risk | Control |
|---|---|
| Hidden behavior | Characterization tests and production observation |
| Contract break | Additive versioning and contract tests |
| Data divergence | One writer, outbox/CDC, reconciliation |
| New service failure | Timeout, circuit breaker, graceful fallback where safe |
| Bad release | Canary, feature flag, fast routing rollback |
| Debugging across calls | Correlation IDs, logs, metrics, tracing |
| Team confusion | Explicit service and data ownership |

## Failure drill

Scenario: the new order service writes its database, then synchronously calls the legacy monolith to update a shared customer record. The call times out, but the monolith may have completed the update.

1. **Prediction:** retrying blindly can apply the update twice, while not retrying can leave uncertainty.
2. **Symptom:** order and customer views disagree; operators cannot tell whether the timed-out call committed.
3. **Recovery:** use an idempotency key/status query, reconcile the entity, and avoid assuming timeout means failure.
4. **Redesign:** one owner records the transaction and publishes a durable event through an outbox; consumers process idempotently.

## Interview questions

<details>
<summary>1. How would you decompose a tightly coupled Java monolith?</summary>

Start with business capabilities and change patterns, not technical layers. Add tests and observability, identify a low-risk boundary, place a facade in front, extract behavior behind an interface, move data ownership with one writer, canary traffic, reconcile results, and retire the old path incrementally.

</details>

<details>
<summary>2. How do you maintain functionality during migration?</summary>

Keep the existing path as the control, preserve external contracts, mirror or canary traffic, compare outputs, use feature flags and routing rollback, and migrate one user journey at a time. Characterization and contract tests protect known behavior.

</details>

<details>
<summary>3. How can the monolith and services coexist without permanent coupling?</summary>

Use a stable gateway/facade, explicit versioned APIs/events, one writer per entity, translation adapters, asynchronous replication for read models, and a dated deprecation plan. Shared tables may be a short transition, but they should not be the target architecture.

</details>

<details>
<summary>4. Why not start by extracting payment?</summary>

Payment is correctness-sensitive, externally integrated, difficult to compensate, and costly to duplicate. It may eventually deserve isolation, but it is usually a poor first experiment unless payment pain is the primary business driver and the team already has strong safety controls.

</details>

<details>
<summary>5. When should you not move to microservices?</summary>

Avoid the move when one team can deploy the modular monolith effectively, scaling needs are uniform, boundaries are unclear, operational maturity is low, or the expected benefit does not justify network, data consistency, deployment, and observability complexity.

</details>

## Teach-back

Explain the apartment-renovation analogy, choose the first capability from this project to extract, and describe traffic routing, data ownership, validation, rollback, and final retirement.

## Stop/go

Proceed only when you can:

- distinguish modularization from service extraction;
- choose and justify a first boundary;
- explain coexistence without permanent shared-database ownership;
- recover from the ambiguous timeout scenario;
- name a condition where retaining the monolith is the better decision.
