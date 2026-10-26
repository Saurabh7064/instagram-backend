# Micro-lesson 04 — Consistency models: what may a reader observe?

[Module](./README.md) · [Previous: PACELC](./03-pacelc.md) · [Next: session guarantees](./05-session-and-causal-consistency.md)

- Artifact: `READY`; comprehension: `TODO`.
- Core study: 10–15 minutes. Primary idea: specify allowed observations before choosing replication settings.
- Prerequisites: replicas may lag; a completed write has returned success; remote confirmation adds latency.
- Core terms: consistency model, strong consistency, eventual consistency.

## A successful edit that seems to disappear — Must understand

The author changes post 42's caption from “Sunset” to “Evening,” sees success, and refreshes. The old caption reappears. Was data lost, or did the read use an older copy? Without an explicit promise, the application cannot distinguish a permitted delay from a correctness bug.

A **consistency model** is a contract describing which histories of reads and writes are allowed. A history is the sequence of operation starts, responses, and returned values. It is about what users can observe, not only what bytes each server currently stores.

## Two different promises

**Strong consistency** is an overloaded phrase. In this module it means the single-copy, real-time behavior called linearizability: a read begun after a write completes must reflect that write or a later one. We state the precise meaning because a product's use of “strong” may have a narrower scope.

**Eventual consistency** says replicas converge on the same value if new updates stop and the system's communication and repair mechanisms continue to make progress. Convergence means the copies eventually agree. The promise alone supplies no maximum number of milliseconds before agreement and no guarantee that a particular immediate read includes a completed write.

These are not opposites in every mathematical sense: a stronger model can also satisfy eventual convergence. The practical comparison is between guaranteeing the stronger observations now and permitting temporary stale observations while repair catches up. Werner Vogels's [explanation of eventual consistency](https://www.allthingsdistributed.com/2008/12/eventually_consistent.html) distinguishes these observation promises and session variants.

## One caption, two read policies

Fixed state: replicas A and B both have post 42, v1=“Sunset.” Only A accepts writes in the basic example. There is no later edit until the example says so.

1. A saves v2=“Evening” and responds successfully to the author. B still has v1.
2. The author reads through B before it catches up. B returns v1.
3. A's v2 update reaches B and is applied. Now A and B both return v2.

Step 2 is forbidden under the stronger contract because the write completed before the read began. It can be permitted by an eventually consistent contract. Step 3 demonstrates convergence for this particular run; it does not prove the implementation always converges.

| Phase | A | B | What a direct read of B can observe |
|---|---|---|---|
| Initial | v1 | v1 | v1 |
| A has acknowledged v2 | v2 | v1 | v1 unless routing/protocol prevents it |
| Delivery and repair finish | v2 | v2 | v2 |

A strong service can keep B internally old while routing the read to A, waiting for B, or rejecting a read it cannot make safe. The guarantee constrains exposed results; it need not demand instantaneous physical copying.

## Why repair is part of the promise

Now consider a failure branch of the same setup: B's update receiver stays stopped. No new edits occur, but B keeps v1. “Wait longer” does not run the broken receiver. Eventual consistency assumes mechanisms that actually propagate and reconcile updates; an indefinitely broken system has not fulfilled the intended operational contract.

If the design also allows B to accept an independent caption edit while disconnected, delivery can reveal conflicting versions. A rule such as choosing one version or merging content must be defined. Merely transmitting both edits does not say which text should win. Identical final copies can still lose user intent, so convergence and conflict quality are separate concerns.

Branch noticeboards make a useful analogy: some display yesterday's announcement until the update arrives. The analogy stops where automatic retries, version comparison, and competing edits need an explicit protocol.

## Choosing the contract

An approximate public count can tolerate temporary staleness if the product explains or bounds it. A decision granting access after a completed permission revocation cannot casually use that same policy. Ask “which operation, data scope, and reader?” before saying the whole application is consistent.

A bounded freshness requirement such as “no more than two seconds old” is an additional contract, not implied by “eventual.” Measuring the age of a replica's last contact also does not by itself prove its data is current. Stronger observations can require coordination, careful routing, and unavailable operations during partitions; weaker observations move some reasoning into application behavior and repair.

## Project mapping

[PostService.update and feed](../../../../../src/main/java/com/instagram/backend/service/PostService.java) operate through [PostRepository](../../../../../src/main/java/com/instagram/backend/repository/PostRepository.java). There is no replica routing or declared eventual-consistency mechanism in those files. `@Transactional` defines a configured database transaction boundary, not the freshness of a proposed external cache or remote read replica.

## Failure drill — paper exercise

- **Predict:** stop B's receiver after A acknowledges v2. Is repeated old data acceptable forever merely because the design says eventual?
- **Detect:** compare known versions and update/repair backlog; observe B returning v1 after the expected operational recovery period.
- **Contain:** remove B from paths requiring freshness; make degraded freshness explicit where stale reads are permitted.
- **Recover:** resume propagation, reconcile conflicts if any, verify A/B return the accepted v2 history, then re-enable routing.
- **Prevent:** test catch-up and alert on lag and stuck repair. Document a user-facing freshness target separately from convergence semantics.

## Recap and stop/go

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
