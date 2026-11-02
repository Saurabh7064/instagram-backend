# Decomposing a Monolith

> **Question:** You are working with a large legacy monolithic Java application that needs to be broken down into microservices incrementally. The application is complex and involves tight coupling between modules.
>
> **How would you approach decomposing this monolith into microservices, and what steps would you take to minimize risk and maintain functionality during the transition?**

> [One-page revision card](../../revision-cards/microservices/01-decomposing-a-monolith.md)

## What could break if we move the upload code tomorrow?

Imagine moving this backend's media code into a separate application. The upload endpoint still accepts the same file, so the extraction looks straightforward. Then you notice that media storage also validates access tokens, old posts contain URLs pointing to the original file location, and profile code still calls post code directly. Moving one class did not move all the assumptions around it.

A mature application has many such agreements: a scheduled job updates a table, a mobile client depends on an error code, or two operations rely on the same database transaction. They may be absent from the architecture diagram and still be part of the behavior users depend on.

Our first job is to discover those agreements. Then we can move one business responsibility while keeping its callers, data, and failure behavior working. If we merely deploy the classes separately while they continue sharing tables and release schedules, we have created a **distributed monolith**: the old coupling now has network failures added to it.

## Think of renovating a hospital that must stay open

A hospital cannot close for a year while every room is rebuilt. The team first maps power, water, patient routes, and emergency exits. It renovates one wing, temporarily redirects people, verifies that the new wing works, and preserves a route back until it is safe.

This maps well to software:

- the existing monolith stays open;
- a gateway redirects one capability at a time;
- tests and production measurements reveal hidden behavior;
- rollback routing acts like the temporary emergency route.

### Where the analogy breaks

Software can duplicate read traffic, replay events, and run old and new implementations simultaneously. A building cannot cheaply make an exact copy of every visitor and send the copy through another wing. Software also has ambiguous network failures: a timed-out request may actually have completed, which the building analogy does not represent.

## Find the behavior before choosing a service

Start with an ordinary user journey: upload a photo, create a post, then open the feed. Follow it from the controller through the Java calls, repository operations, files, and any scheduled jobs. Write down the response as well as the side effects. “The upload returns a URL” is incomplete if that URL stops working when traffic reaches another application instance.

Next, put tests around the behavior that callers can observe. A **characterization test** records what the existing system does today, including surprising behavior someone may rely on. This gives us a way to notice when an apparently internal refactor changes the contract. We can decide deliberately which odd behavior to preserve and which needs a separate migration.

Draw a **dependency map** of the modules, tables, jobs, and outside services the journey touches. Record transaction boundaries too: replacing a local call with HTTP may separate changes that previously succeeded or rolled back together. Finally, measure the current latency, errors, throughput, and business outcome. This is the **baseline** we will compare the new path against; without it, a healthy-looking new deployment tells us very little.

### What that investigation reveals in this backend

The repository is small enough to read directly, and it already shows why Java layers do not automatically make good service boundaries:

- [PostService.java](../../../../../src/main/java/com/instagram/backend/service/PostService.java) handles posts and likes, reads users, validates tokens, and builds feed responses.
- [ProfileService.java](../../../../../src/main/java/com/instagram/backend/service/ProfileService.java) reads users and calls `PostService.countPostsFor(...)`.
- [MediaStorageService.java](../../../../../src/main/java/com/instagram/backend/service/MediaStorageService.java) stores media but also validates an access token through `TokenService`.
- [AuthService.java](../../../../../src/main/java/com/instagram/backend/service/AuthService.java) owns registration and login but uses the same user repository that post and profile behavior use.

If we extract Profile Service as-is, its post-count call becomes a network dependency and user-data ownership is still unresolved. The map tells us which decisions to make before that new failure point exists.

## Which responsibility deserves its own deployment?

A good starting point is an outcome someone recognizes: store media, publish a post, or send a notification. We call that a **business capability**. It includes the behavior and data that change together for that purpose. Splitting into “controller service” and “database service” usually leaves one ordinary feature crossing every deployment.

For each candidate, name the **data owner**, the component allowed to make authoritative changes. If several future services will still write the same private tables, independent deployment will remain unsafe. Ask also which rules need one transaction and which work can tolerate a delayed result.

Then look for a reason to pay the cost of a new service. Media may need different storage or processing capacity. Notifications may need independent workers and can be delayed without losing the post. Authentication touches nearly every request and is security-sensitive, so extracting it first can expose a much larger part of the product to a mistake.

We can establish the boundaries inside one application first. That is a **modular monolith**: one deployable program with explicit internal interfaces and ownership. It lets us learn whether the boundary works before every call also needs network deadlines, authentication, and recovery.

### A possible extraction order for this project

| Capability | First extraction? | Reason |
|---|---:|---|
| Media storage and processing | Good candidate | Clear API, different storage/scaling needs, and failure can often be isolated from feed reads |
| Notifications | Good future candidate | Naturally asynchronous and need not block the originating action |
| Read-only feed projection | Possible after measurement | Can be compared with the existing feed before cutover, if an event pipeline is justified |
| Authentication | Later | Security-sensitive and used by nearly every request |
| Posts and likes | Later | Their rules currently benefit from shared local transactions |

This is a reasoned starting point, not a universal order. Choose the capability that solves a measured problem and has a transition we can actually control.

## Keep the public upload route while changing what handles it

Suppose media is the first extraction. The client should keep using `/api/media` while we replace the implementation behind it. Replacing one route at a time while the rest of the old application stays running is called a **strangler migration**.

A gateway can route the upload to either implementation. If their internal models differ, a small adapter translates between them. This is an **anti-corruption layer**: it keeps the new service from adopting every legacy assumption simply to keep old callers working. Keep the translation narrow; otherwise the gateway becomes another place containing business rules.

```text
Client -> stable gateway -> /api/posts -> monolith
                         -> /api/media -> old implementation or new media service
```

The stable URL gives us a way to shift traffic gradually. A **canary release** sends a small controlled group to the new path first, so a defect affects fewer users while we compare it with the baseline.

### Follow one upload through the migration

1. Capture the existing contract from [MediaController.java](../../../../../src/main/java/com/instagram/backend/controller/MediaController.java): multipart field, authorization, response fields, URL format, status codes, and size/type limits.
2. Introduce a `MediaClient` interface in the monolith. Its first implementation calls the existing storage code; this creates a replacement point without changing behavior.
3. Build the remote implementation against the same contract and integration tests. Decide how it verifies identity: verify a signed token locally or trust only a protected gateway identity contract. A public plain `X-User-Id` header is not proof of identity.
4. Compare safe read results before returning the new path to users. Shadowing means sending a copy for comparison; do not shadow a mutating upload blindly, because the copy can create a second object.
5. Send internal users or 1% of uploads to the new service. Compare successful uploads, latency, stored file integrity, and authorization failures with the old path.
6. Increase traffic in stages, for example 1%, 10%, 50%, then 100%, with a hold and stop decision at each stage.
7. Keep the old path usable during a stated rollback window. After stored URLs, object availability, and remaining consumers are reconciled, retire it.

Routing is reversible only while the receiving path can access the right data. Once the new service accepts writes the old path cannot see, switching a flag back may return missing files. That data condition belongs in the rollback plan.

### Why copying the class would miss the difficult part

`MediaStorageService.store(...)` currently calls `TokenService` locally. In another application, identity validation becomes a security decision. Calling a remote auth service for every upload adds latency and makes an auth communication failure an upload failure. The extraction must choose that contract explicitly; moving source files does not choose it for us.

## Change one authoritative writer at a time

Now consider moving metadata. Writing the old database and then calling the new service feels like a convenient way to keep both copies current:

```text
old database write -> succeeds
new service write  -> caller times out
```

What should the caller do next? The new service may have committed before its response was lost. Retrying could create a duplicate; skipping the retry could leave the new copy missing. Those two writes do not share one all-or-nothing transaction.

An **outbox** closes the gap at the source: write the authoritative business change and an event describing it in the same local database transaction. Either both commit or neither does. A separate publisher can keep retrying the committed event.

```text
one local transaction: business change + outbox event
                                  |
                                  v
                         publisher may retry
                                  |
                                  v
                       new copy applies event safely
```

The publisher may send twice, so the receiver must recognize the same logical operation. An **idempotency key** gives it that identity; durable uniqueness and a transaction around the deduplication record and local update make the protection survive concurrent requests and crashes.

Keep the old system as the only authoritative writer while copying history in bounded batches and applying new events. Record backfill progress. Then perform **reconciliation**: compare missing IDs, important values, and business totals between the copies. Matching row counts alone can hide a missing row paired with an extra row.

Only after the new owner is caught up should we freeze or redirect old writes and transfer authority. A temporary shared-table read may help during migration, but name its owner and removal date. Permanent shared writes make future schema changes a coordination problem again.

## Old callers still need to work during the move

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

## What evidence lets us advance to the next stage?

| Phase | Main risk | Evidence required before advancing | Rollback |
|---|---|---|---|
| Discover | Hidden behavior | Journey map, characterization tests, production baseline | No traffic change yet |
| Modularize | Behavior accidentally changes | Existing tests plus boundary tests pass | Revert internal implementation |
| Shadow reads | New result differs | Difference rate understood and accepted | Stop copying traffic |
| Canary | Real traffic fails | Error, latency, and business metrics within threshold | Route canary back to monolith |
| Data ownership cutover | Divergent or lost data | Backfill and reconciliation complete | Stop writes; either reverse-sync and cut back only after the legacy store catches up, or roll forward on the new owner; replay the event log and reconcile |
| Retirement | Unknown consumer still depends on old path | No observed traffic for deprecation window | Temporarily restore compatibility adapter |

“Rollback” must be rehearsed. A feature flag that nobody has tested under load is a hope, not a rollback plan.

## When extraction is worth the extra work

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

Before marking this answer practiced, explain one extraction and its data-aware rollback without notes, then predict the lost-response failure. Answer the questions below before opening them; this written migration is a design exercise, not execution or demonstrated mastery.

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
