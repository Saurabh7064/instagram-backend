# Micro-Lesson 04 — Monolith-to-Microservices Migration

Core study: 20 minutes. Prerequisite: [Scalability, Availability, and Reliability](../09-reliability-resilience-and-fault-tolerance/01-scalability-availability-reliability.md).

## Move media work while people keep publishing — Must understand

Suppose media processing has become the painful part of our backend. We want it to scale or deploy independently, but people still need to upload photos and publish posts every day. Copying `MediaStorageService` into a new application is easy compared with deciding who owns existing files, what a failed upload means, and which path serves the next request.

A **monolith** is one deployable application containing several capabilities. A network service is a separately running participant reached through messages. Splitting the deployment changes failure boundaries; it does not automatically separate ownership or remove coupling. Ten services all freely updating the same tables still depend on each other's data decisions.

Think of renovating an occupied building. You isolate one section, discover the pipes it shares, move use gradually, and retain a safe route back. The analogy stops at data: sending traffic back does not automatically reverse writes already accepted by the new implementation.

## Choose a capability, then draw its ownership boundary

Start by recording what publishing actually does: requests, scheduled work, file writes, database updates, and integrations. Add tests describing current behavior, even where that behavior is awkward; these are **characterization tests**. Request IDs, logs, metrics, and traces help compare the old and new paths rather than relying on a successful deployment alone.

A **domain boundary** groups behavior and data that change for the same business reason. Media storage/processing is a candidate here because its inputs, outputs, and ownership can be stated. That is a proposal, not proof that extraction is worthwhile. First compare the benefit with the simpler option of enforcing that boundary inside the current application.

Prefer a first capability with limited cross-boundary transactions, measurable pain, a clear owner, and failure behavior the product can tolerate. Correctness-sensitive settlement is usually a harder first experiment. If the team cannot say who writes each piece of state, extraction is premature.

## Keep the client's entrance stable while the route changes

```text
Client → Gateway/facade
          ├─ legacy route
          └─ new service route
```

A **facade** is the stable entry point hiding which implementation handles the request. Route only the selected capability to the new path at first. This gradual replacement is a **strangler migration**: the old application keeps serving while one capability after another moves.

Create an interface around the existing behavior before replacing it. If the new service must call the legacy path during transition, translate at a narrow boundary. That translator is an **anti-corruption layer**: it keeps old request shapes and data concepts from spreading through the new model. It is a bridge with a removal plan, not permanent permission for every service to depend on every legacy table.

## Move the authority to write, not just the code that calls it

Choose one authoritative writer for each business entity. During transition, build a new read view from its changes. An **outbox** records a pending change message in the same local transaction as the authoritative database update; a sender delivers it later. **Change data capture**, or **CDC**, is another approach that derives a change stream from the database's recorded changes. Both require recovery and duplicate handling; neither is a reason to let two services independently edit the same truth.

Two unconstrained writes leave a gap: the new store can succeed while the legacy update fails or merely loses its reply. Plan how existing data is copied, changes are caught up, and IDs/results are reconciled before switching the writer. Treat legacy shared-table access as a time-limited transition rather than the end state.

## Compare a small amount of traffic before moving everyone

Mirror only safe reads, compare results, and run contract and integration checks. A **canary** sends a small fraction of real traffic through the new path while we watch errors, latency, and business outcomes. Do not mirror a publishing write into two live systems and accidentally create two effects.

Increase traffic only while those outcomes agree with the intended contract. Keep routing rollback available, but check whether the old path can interpret new data before using it. Stop old writes, reconcile remaining differences, and remove the old implementation only after evidence shows its callers and background jobs have moved. The migration ends when obsolete ownership disappears, not when another service starts.

## Coexistence needs compatible messages and one writer

While both paths operate, clients must keep understanding responses. Prefer additive API changes, keep old fields/endpoints through a stated deprecation window, and translate at the boundary. Contract tests check whether known consumers still receive the behavior they use.

Events need equally deliberate evolution: include a schema version, preserve existing field meanings, and allow new optional fields where consumers support them. Replays and repeated deliveries must be safe, so event consumers need operation identity and idempotent processing rather than assuming a message arrives once.

For the data, keep one writer per entity and replicate changes for read views where suitable. Compare counts, identities, and checksums as appropriate; a matching row count alone can hide mismatched content. These checks tell us when it is safe to retire a path and when the new view still needs repair.

## Project mapping

This repository is still small enough to remain a modular monolith. [AuthService.java](../../../../../src/main/java/com/instagram/backend/service/AuthService.java), [PostService.java](../../../../../src/main/java/com/instagram/backend/service/PostService.java), [ProfileService.java](../../../../../src/main/java/com/instagram/backend/service/ProfileService.java), and [MediaStorageService.java](../../../../../src/main/java/com/instagram/backend/service/MediaStorageService.java) already suggest capability boundaries.

A sensible learning extraction would be media processing/storage because it can become asynchronous and independently scalable. Authentication and post ownership should remain together until there is a concrete scaling, security, or team-ownership reason to separate them.

Do not split these classes into network services merely to claim microservices. First enforce module boundaries and data ownership inside the monolith.

## Which evidence protects each part of the move?

| Risk | Control |
|---|---|
| Hidden behavior | Characterization tests and production observation |
| Contract break | Additive versioning and contract tests |
| Data divergence | One writer, outbox/CDC, reconciliation |
| New service failure | Timeout, circuit breaker, graceful fallback where safe |
| Bad release | Canary, feature flag, fast routing rollback |
| Debugging across calls | Correlation IDs, logs, metrics, tracing |
| Team confusion | Explicit service and data ownership |

## The legacy call times out after the new path saved its work — paper exercise

Stay with the proposed media extraction. The new service stores the upload, then asks the legacy application to record the media reference. The call times out. Predict whether another ordinary request is safe: the legacy save may have completed even though its reply did not arrive.

Compare the durable media identity with the legacy operation's status. Stop blind retries that could duplicate references, and avoid declaring the upload wholly failed just because the reply is missing. An idempotency key or status lookup can connect recovery to the original intended operation.

Reconcile the reference, then redesign the handoff around one owner recording a durable change and delivering it through an outbox, with identity-aware consumers. This removes the unreliable assumption that two independent writes finish together; it still needs retries and repair. Test lost replies and replayed messages before expanding traffic. Routing rollback is useful only alongside a plan for the state already written.

## Explain one complete move, including the way back

Explain the apartment-renovation analogy, choose the first capability from this project to extract, and describe traffic routing, data ownership, validation, rollback, and final retirement.

## Before moving on

Artifact: `READY`; comprehension not yet demonstrated.

Proceed only when you can:

- distinguish modularization from service extraction;
- choose and justify a first boundary;
- explain coexistence without permanent shared-database ownership;
- recover from the ambiguous timeout scenario;
- name a condition where retaining the monolith is the better decision.

## Questions and explained answers

<details>
<summary>1. How would you decompose a tightly coupled Java monolith?</summary>

Start with business capabilities and change patterns, not technical layers. Add tests and observability, identify a low-risk boundary, place a facade in front, extract behavior behind an interface, move data ownership with one writer, canary traffic, reconcile results, and retire the old path incrementally.

</details>

<details>
<summary>2. How do you maintain functionality during migration?</summary>

Keep the existing path as the control, preserve external contracts, mirror safe reads or canary explicitly routed writes, compare outputs, use feature flags and routing rollback, and migrate one user journey at a time. Characterization and contract tests protect known behavior.

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
