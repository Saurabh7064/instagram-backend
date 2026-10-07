# Decomposing a Monolith

> **Question:** You are working with a large legacy monolithic Java application that needs to be broken down into microservices incrementally. The application is complex and involves tight coupling between modules.
>
> **How would you approach decomposing this monolith into microservices, and what steps would you take to minimize risk and maintain functionality during the transition?**

## Why does this problem exist?

A mature monolith usually contains more knowledge than its diagrams show:

- one screen may call several modules that share the same database tables;
- a database trigger or scheduled job may perform behavior no Java service obviously owns;
- callers may depend on undocumented response fields or error codes;
- one in-process Java call may become a slower and less reliable network call after extraction;
- a single database transaction may currently update several business areas atomically.

The goal is therefore not simply “turn classes into services.” The goal is to change **ownership and deployment boundaries without losing behavior**.

If classes are moved into separate applications but still share tables, deployment order, and internal models, the result is a distributed monolith: it has the operational cost of microservices but retains the original coupling.

## Analogy: renovate an occupied hospital one wing at a time

A hospital cannot close for a year while every room is rebuilt. The team first maps power, water, patient routes, and emergency exits. It renovates one wing, temporarily redirects people, verifies that the new wing works, and preserves a route back until it is safe.

This maps well to software:

- the existing monolith stays open;
- a gateway redirects one capability at a time;
- tests and production measurements reveal hidden behavior;
- rollback routing acts like the temporary emergency route.

### Where the analogy breaks

Software can duplicate read traffic, replay events, and run old and new implementations simultaneously. A building cannot cheaply make an exact copy of every visitor and send the copy through another wing. Software also has ambiguous network failures: a timed-out request may actually have completed, which the building analogy does not represent.

## Focus 1 — Discover what the monolith really does

Three useful terms:

- A **characterization test** records the system's current observable behavior, including behavior that is strange but relied upon. It answers, “What does the application do today?”
- A **dependency map** shows which modules, tables, jobs, and external systems call or update one another.
- A **baseline** is the current measured value for latency, errors, throughput, and business outcomes. Without it, “the new service is better” is only a guess.

### Step-by-step discovery

1. List important user journeys: register, log in, upload media, create a post, load the feed, like a post, and view a profile.
2. Trace each journey through controllers, services, repositories, database tables, background jobs, and external calls.
3. Add characterization and integration tests around those journeys before changing their internals.
4. Record response shapes, status codes, side effects, and transaction boundaries.
5. Measure existing latency, error rate, traffic, database load, and business results.
6. Identify owners and release dependencies: which teams and modules must change together?

### Instagram-backend example

The repository is currently small, but it already shows why code layers are not automatically service boundaries:

- [PostService.java](../../../../../src/main/java/com/instagram/backend/service/PostService.java) handles posts and likes, reads users, validates tokens, and builds feed responses.
- [ProfileService.java](../../../../../src/main/java/com/instagram/backend/service/ProfileService.java) reads users and calls `PostService.countPostsFor(...)`.
- [MediaStorageService.java](../../../../../src/main/java/com/instagram/backend/service/MediaStorageService.java) stores media but also validates an access token through `TokenService`.
- [AuthService.java](../../../../../src/main/java/com/instagram/backend/service/AuthService.java) owns registration and login but uses the same user repository that post and profile behavior use.

For example, extracting `ProfileService` as-is would turn its direct `PostService` call into a network dependency and leave unclear ownership of user data. The first task is to document that coupling, not hide it behind HTTP.

## Focus 2 — Choose boundaries by business capability

Three useful terms:

- A **business capability** is an outcome the business recognizes, such as identity, media storage, posting, or notifications.
- A **data owner** is the one component allowed to make authoritative changes to a particular business entity.
- A **modular monolith** is one deployable application with explicit internal module boundaries. It is often the safest intermediate state.

Do not split by technical layer into “controller service,” “database service,” and “utility service.” A change to “like a post” would still cross every service and require coordinated deployment.

Instead, ask:

1. Which behavior and data change for the same business reason?
2. Which capability needs independent scaling or release speed?
3. Can it fail without taking the core user journey down?
4. Can one team own it from API through data and operations?
5. How many synchronous calls and shared transactions would extraction introduce?

### Candidate order for this project

| Capability | First extraction? | Reason |
|---|---:|---|
| Media storage and processing | Good candidate | Clear API, different storage/scaling needs, and failure can often be isolated from feed reads |
| Notifications | Good future candidate | Naturally asynchronous and does not need to block the originating action |
| Read-only feed projection | Possible after measurement | Can be built from events and compared with the existing feed before cutover |
| Authentication | Later | Security-sensitive and used by nearly every request |
| Posts and likes | Later | They currently share user access and transactional behavior |

This order is not universal. If production evidence says media is stable but checkout blocks every release, priorities should follow the measured business pain.

## Focus 3 — Replace one path incrementally

Three useful terms:

- A **strangler migration** replaces one route or capability at a time while the old system continues serving everything else.
- An **anti-corruption layer** translates between the legacy model and the new model, preventing old assumptions from spreading into the new service.
- A **canary release** sends a small percentage of real traffic to the new path before a full cutover.

### Routing model

```text
                              ┌─────────────────────────┐
Client ──> stable gateway ────┤ /api/posts  -> monolith │
                  │           ├─────────────────────────┤
                  └───────────┤ /api/media  -> media svc│
                              └─────────────────────────┘
                                         │
                                  object storage
```

The client continues using `/api/media`. Only routing changes. That stable contract is what makes a quick rollback possible.

### Concrete media-extraction flow

1. Define the existing contract from [MediaController.java](../../../../../src/main/java/com/instagram/backend/controller/MediaController.java): multipart field name, authorization behavior, response fields, URL format, status codes, and size/type limits.
2. Put a `MediaClient` interface inside the monolith. Initially its implementation calls the existing `MediaStorageService`, so behavior does not change.
3. Build the new media service against contract and integration tests.
4. Keep token validation explicit. Either the gateway verifies tokens and forwards a signed identity, or the media service verifies them itself. Do not silently trust a plain `X-User-Id` header from the public internet.
5. For safe read requests, send a copy to the new service and compare metadata without returning its result to the user.
6. Route internal users or 1% of uploads to the new service. Compare success rate, upload latency, file integrity, and authorization failures with the baseline.
7. Increase traffic gradually: for example, 1% → 10% → 50% → 100%, with explicit stop conditions at each stage.
8. Keep the old route usable during a defined rollback window.
9. After all references and stored URLs have been reconciled, remove the legacy implementation.

### Why not extract the Java class directly?

Today `MediaStorageService.store(...)` calls `TokenService` in the same process. After extraction, that dependency must become a deliberate security contract. Directly copying the class would conceal that design decision and likely create a synchronous media-to-auth call for every upload. That call adds latency and makes an auth outage an upload outage.

## Focus 4 — Move data without unsafe dual writes

Three useful terms:

- An **outbox** stores a business change and an event record in the same local database transaction; a separate publisher later sends the event.
- An **idempotency key** identifies one logical operation so processing the same request or event twice has the effect of processing it once.
- **Reconciliation** compares old and new records or aggregates to detect and repair divergence.

### The unsafe version

```text
Monolith writes old database  ── success
Monolith calls new service    ── timeout
```

The caller does not know whether the new service failed before or after committing. Blind retry may create a duplicate; skipping retry may lose the new copy.

### Safer transition

```text
one local transaction
    ├─ update authoritative business row
    └─ insert outbox event
              │
              v
       publisher retries event
              │
              v
 new owner consumes idempotently
              │
              v
 reconciliation checks counts/checksums
```

Rules for the transition:

1. Declare one authoritative writer for each entity at every stage.
2. Replicate to the new read model asynchronously when immediate consistency is unnecessary.
3. Make consumers safe against duplicates; message systems commonly deliver more than once.
4. Backfill historical data in bounded batches and record progress.
5. Compare counts, missing IDs, important field checksums, and business totals.
6. Freeze old writes or change routing before transferring authority to the new owner.

Temporary shared-table reading can be a migration step, but it needs an owner, a removal date, and monitoring. Permanent shared writes prevent either service from changing its schema safely.

## Maintaining backward compatibility

### API changes

- Prefer adding an optional field to renaming or removing a field.
- Keep the old request/response shape during a documented deprecation window.
- Translate old shapes at the gateway or anti-corruption layer.
- Run contract tests for each known consumer.
- Measure actual use before deleting an old endpoint.

For example, if the old response contains `imageUrl`, do not suddenly replace it with `asset.location.url`. Return the old field while clients migrate, or translate the new internal model back to the old external shape.

### Event changes

- Add a schema version.
- Treat unknown optional fields as acceptable.
- Never reuse an existing field name with a different meaning.
- Retain the ability to replay safely through idempotent consumers.

## Risk controls by migration phase

| Phase | Main risk | Evidence required before advancing | Rollback |
|---|---|---|---|
| Discover | Hidden behavior | Journey map, characterization tests, production baseline | No traffic change yet |
| Modularize | Behavior accidentally changes | Existing tests plus boundary tests pass | Revert internal implementation |
| Shadow reads | New result differs | Difference rate understood and accepted | Stop copying traffic |
| Canary | Real traffic fails | Error, latency, and business metrics within threshold | Route canary back to monolith |
| Data ownership cutover | Divergent or lost data | Backfill and reconciliation complete | Stop writes; either reverse-sync and cut back only after the legacy store catches up, or roll forward on the new owner; replay the event log and reconcile |
| Retirement | Unknown consumer still depends on old path | No observed traffic for deprecation window | Temporarily restore compatibility adapter |

“Rollback” must be rehearsed. A feature flag that nobody has tested under load is a hope, not a rollback plan.

## Important trade-offs

| Decision | Benefit | Cost or danger |
|---|---|---|
| Modularize before extraction | Reveals boundaries without network complexity | Does not provide independent deployment yet |
| Start with low-risk capability | Builds operational skill safely | May not solve the largest business pain first |
| Asynchronous data propagation | Decouples availability and supports retries | Introduces temporary inconsistency and more debugging work |
| Stable gateway contract | Allows gradual routing and rollback | Gateway can become overloaded with business logic if translation is not controlled |
| Canary rollout | Limits blast radius | Requires representative metrics and routing support |
| Keep monolith | Lowest operational complexity | Remains unsuitable if scale, ownership, or release constraints are real and measured |

## Failure drill

**Scenario:** 10% of media uploads now go to the new service. Its database records the upload, but the response times out before reaching the gateway. The mobile client retries.

1. **Prediction:** without an idempotency key, the retry may create a second media record and a second stored object.
2. **Observable symptoms:** upload latency rises, timeout count increases, duplicate object count grows, and users may see duplicate media.
3. **Immediate response:** stop the canary or route uploads back to the old path; do not delete uncertain uploads blindly.
4. **Diagnosis:** trace the request ID through gateway and service logs, then query both attempts using the client operation ID.
5. **Recovery:** reconcile duplicate records and preserve the referenced object.
6. **Prevention:** require an idempotency key for upload creation, return the original result on a repeated key, use bounded timeouts, and test ambiguous-response failure before increasing traffic.

## Common weak answers and how to improve them

### “I would identify services and rewrite them”

This omits how behavior is discovered, how data ownership moves, how traffic is shifted, and how rollback works. Explain the transition, not only the target diagram.

### “Each database table becomes a service”

Tables are implementation details. A service boundary should contain business behavior and the data it owns. Otherwise one user action creates a chain of chatty network calls.

### “We will use events so everything is decoupled”

Events change the consistency and debugging model. State which event is published, who owns the source data, how duplicates are handled, what the user sees before consumers catch up, and how divergence is reconciled.

### “We can roll back”

Explain precisely what rolls back. Code can roll back quickly; a destructive schema change or ownership transfer may not. Use backward-compatible schema changes, retain event history, and rehearse routing rollback.

## A 2–3 minute interview answer

“I would treat decomposition as a risk-managed migration, not a rewrite. First I would inventory the important user journeys, module and table dependencies, scheduled jobs, and external integrations. I would add characterization and integration tests plus logs, metrics, traces, and a production baseline, because legacy behavior is often not fully documented.

Next I would identify boundaries around business capabilities and data ownership. I would first make those boundaries explicit inside a modular monolith. For an initial extraction I would choose a capability with a clear contract, limited shared transactions, measurable benefit, and a safe failure mode—for example media processing rather than payment or authentication.

I would use the strangler approach: keep a stable gateway contract, implement the new service behind it, and use an anti-corruption layer where legacy and new models differ. I would shadow safe reads, compare results, then canary a small traffic percentage with thresholds for errors, latency, and business outcomes. Routing remains reversible until the new path is proven.

For data, I would declare one authoritative writer per entity. I would avoid fragile dual writes; instead I would publish durable changes using an outbox, consume them idempotently, backfill in batches, and reconcile counts and key business totals. API and event changes would remain backward-compatible during a measured deprecation window.

Finally, I would increase traffic gradually, rehearse rollback and failure cases, transfer ownership, observe a stability window, and remove the old path only after traffic and data evidence show it is safe. I would repeat this one capability at a time, and I would keep the modular monolith if independent services do not provide enough value to justify their operational cost.”

## Questions and explained answers

<details>
<summary>1. Why should a team modularize the monolith before extracting a service?</summary>

Modularization makes dependencies and ownership explicit while calls are still local and easy to debug. If the team cannot separate media from identity inside one process, moving the same code across a network does not remove the coupling; it only adds latency and partial failure. A clean internal interface also creates the seam later used by a remote client.

</details>

<details>
<summary>2. Why is sharing the monolith database dangerous after extraction?</summary>

Both applications can change the same data without a clear owner. A schema change by one team can break the other at runtime, independent deployment becomes unsafe, and cross-service transactions remain hidden in SQL. Temporary read access may help migration, but authoritative writes need one owner and a removal plan for shared access.

</details>

<details>
<summary>3. What is wrong with writing to the old database and the new service in one request?</summary>

The two writes do not share one atomic transaction. If the first succeeds and the second times out, the caller cannot safely infer whether the second committed. Retrying may duplicate it; not retrying may lose it. One local write plus an outbox event makes the durable intent atomic, and an idempotent consumer makes retries safe.

</details>

<details>
<summary>4. How do you know when it is safe to retire the old path?</summary>

The new path has passed contract and integration tests, canary metrics meet explicit thresholds, old and new outputs or data have been reconciled, rollback has been rehearsed, and telemetry shows no remaining consumers during the deprecation window. “The deployment succeeded” alone is not evidence that hidden clients or background jobs are gone.

</details>

<details>
<summary>5. When is a modular monolith a better answer than microservices?</summary>

It is better when a small team can release safely, components scale similarly, strong local transactions are valuable, operational maturity is limited, and no measured bottleneck requires independent deployment or scaling. Microservices are a trade: they buy independent ownership and scaling by adding networks, distributed data, deployment coordination, and observability work.

</details>
