# Micro-lesson 04 — Consistency models: what may a reader observe?

[Module](./README.md) · [Previous: PACELC](./03-pacelc.md) · [Next: session guarantees](./05-session-and-causal-consistency.md)

Core study: 10–15 minutes. Prerequisites: replicas may lag; a completed write has returned success; remote confirmation adds latency.

## Your edit succeeded, then the old caption came back — Must understand

You change post 42 from “Sunset” to “Evening.” The app reports success. You refresh, and “Sunset” reappears. It is tempting to conclude the database lost the edit. But there is another possibility: the refresh read an older copy.

Suppose A and B both started with “Sunset,” called v1. A accepts the one update to “Evening,” called v2. B receives changes later. There are no further edits yet. We need to decide which responses the application is allowed to show while B catches up.

That agreement is a **consistency model**: a contract about allowed observations of reads and writes. A **history** records operation starts, responses, and returned values. Looking only at the bytes currently on A and B cannot tell us whether the service broke its contract; the order of the user's completed operations matters too.

## Follow the refresh to the older copy

Follow the one edit through these three phases:

1. A saves v2=“Evening” and responds successfully to the author. B still has v1.
2. The author reads through B before it catches up. B returns v1.
3. A's v2 update reaches B and is applied. Now A and B both return v2.

If the contract says a later read must include a completed edit, step 2 is a bug. If the contract permits old reads while copies catch up, it can be allowed. Step 3 shows the copies agreeing again in this run; we still need a mechanism that will reliably make that happen.

| Phase | A | B | What a direct read of B can observe |
|---|---|---|---|
| Initial | v1 | v1 | v1 |
| A has acknowledged v2 | v2 | v1 | v1 unless routing/protocol prevents it |
| Delivery and repair finish | v2 | v2 | v2 |

A service promising later reads will include completed edits can keep B internally old while routing the read to A, waiting for B, or rejecting a read it cannot make safe. The guarantee constrains exposed results; it need not demand instantaneous physical copying.

## Name the promise the read path is making

**Strong consistency** is an overloaded phrase. In this module it means the single-copy, real-time behavior called linearizability: a read begun after a write completes must reflect that write or a later one. We state the precise meaning because a product's use of “strong” may have a narrower scope.

**Eventual consistency** says replicas converge on the same value if new updates stop and the system's communication and repair mechanisms continue to make progress. Convergence means the copies eventually agree. The promise alone supplies no maximum number of milliseconds before agreement and no guarantee that a particular immediate read includes a completed write.

These are not opposites in every mathematical sense: a stronger model can also satisfy eventual convergence. The practical comparison is between guaranteeing the stronger observations now and permitting temporary stale observations while repair catches up. Werner Vogels's [explanation of eventual consistency](https://www.allthingsdistributed.com/2008/12/eventually_consistent.html) distinguishes these observation promises and session variants.

## Why repair is part of the promise

Now consider a failure branch of the same setup: B's update receiver stays stopped. No new edits occur, but B keeps v1. “Wait longer” does not run the broken receiver. Eventual consistency assumes mechanisms that actually propagate and reconcile updates; an indefinitely broken system has not fulfilled the intended operational contract.

If the design also allows B to accept an independent caption edit while disconnected, delivery can reveal conflicting versions. A rule such as choosing one version or merging content must be defined. Merely transmitting both edits does not say which text should win. Identical final copies can still lose user intent, so convergence and conflict quality are separate concerns.

Branch noticeboards make a useful analogy: some display yesterday's announcement until the update arrives. The analogy stops where automatic retries, version comparison, and competing edits need an explicit protocol.

## How old is acceptable for this operation?

An approximate public count can tolerate temporary staleness if the product explains or bounds it. A decision granting access after a completed permission revocation cannot casually use that same policy. Ask “which operation, data scope, and reader?” before saying the whole application is consistent.

A bounded freshness requirement such as “no more than two seconds old” is an additional contract, not implied by “eventual.” Measuring the age of a replica's last contact also does not by itself prove its data is current. Stronger observations can require coordination, careful routing, and unavailable operations during partitions; weaker observations move some reasoning into application behavior and repair.

## Project mapping

[PostService.update and feed](../../../../../src/main/java/com/instagram/backend/service/PostService.java) operate through [PostRepository](../../../../../src/main/java/com/instagram/backend/repository/PostRepository.java). There is no replica routing or declared eventual-consistency mechanism in those files. `@Transactional` defines a configured database transaction boundary, not the freshness of a proposed external cache or remote read replica.

## Waiting does not restart B's stopped receiver — paper exercise

A has acknowledged v2, but B's update receiver stops. Predict whether “eventual” makes it acceptable to leave B at v1 indefinitely.

Inspect known versions and the propagation or repair backlog. Old reads beyond the expected operational recovery period signal stuck repair. Remove B from paths requiring fresh data; where stale reads are permitted, make the degraded freshness explicit.

Recovery means restarting propagation, reconciling any conflict, and verifying that both copies represent the accepted v2 history before restoring those read paths. Alerting on lag and stuck repair makes this visible sooner. Keep the promised user-facing freshness target separate from the abstract convergence condition: a contract about eventual agreement does not itself specify a two-second recovery deadline.

## Before moving on

Artifact: `READY`; comprehension: `TODO`.

Use the three phases to explain a stale read without calling it data loss. State what eventual convergence assumes, what it does not bound, and what a stronger read path must prevent.

## Questions and explained answers

<details>
<summary>1. Which exact step violates the strong contract, and why?</summary>

Step 2 returns v1 after the v2 write already completed. With no later edit, that read cannot be placed in a real-time order consistent with the single current copy.

</details>

<details>
<summary>2. If both copies eventually show the same caption, has every edit been preserved?</summary>

Not necessarily. A rule may choose one conflicting version and discard another. Agreement proves convergence, not preservation of every intended change.

</details>

<details>
<summary>3. What extra information is needed to promise “fresh within two seconds”?</summary>

A defined freshness measure, mechanisms and routing that enforce the bound, and behavior when the bound cannot be met. Eventual convergence alone supplies none of that deadline enforcement.

</details>
