# Micro-lesson 02 — CAP: Consistency, Availability, Partition tolerance

[Module](./README.md) · [Previous: partial failure](./01-partial-failure.md) · [Next: PACELC](./03-pacelc.md)

- Artifact: `READY`; comprehension: `TODO`.
- Core study: 10–15 minutes. Primary idea: during lost communication, a read/write service cannot promise both one current view and successful operations everywhere.
- Prerequisite: a missing message does not prove a remote process stopped. A read fetches a value; a write changes it.
- Core terms: consistency, availability, partition tolerance.

## Why keep copies at all? — Must understand

Suppose post 42 is stored on one database machine. If that machine becomes unreachable, users cannot read it. We might maintain a second copy elsewhere so another machine can answer. A maintained copy is a **replica**; copying updates between replicas is **replication**. Call our two participants A and B; a participating process or machine is often called a **node**.

Copies help only if we decide how they behave when they disagree. Sending an update takes time, and communication can stop. “We have two copies” alone says nothing about which answers are legal.

**CAP stands for Consistency, Availability, and Partition tolerance.** Each letter names a specific promise. They are not three independent switches that can all remain guaranteed under every failure.

## C — Consistency: one logical current value

Here **Consistency** means reads and writes behave as if there were one up-to-date copy. If a write completes before a read begins, that read must reflect that write or a later one. Operations overlapping in time may be placed in either order if the observed results fit that order. The formal name for this behavior is **linearizability**, developed in [lesson 6](./06-linearizability-and-serializability.md).

For post 42, if updating caption from “Sunset” to “Evening” has already returned success, a later read must not return “Sunset” unless another write legitimately restored it. The important boundary is a **completed** write. A request still in progress has not made the same promise.

This does not mean that every physical copy contains identical bytes at every instant. Some copies may lag internally, provided the service does not expose an answer that violates its promise. It also does not mean database constraints such as “caption cannot be null”; those are a different use of “consistency.”

## A — Availability: operations complete at reachable, non-failing nodes

**Availability** in CAP means every request received by a non-failing node eventually completes with a valid result for the requested operation. The model does not impose a particular millisecond deadline. Production availability targets normally add such deadlines; they are related operational measures, not identical definitions.

A read of our existing post must eventually return a permitted value. A write must complete according to the service's contract. Returning an unavailable response such as status 503 tells the user the operation is unavailable; the fact that an error packet arrived does not satisfy this promise. A disconnected client that cannot reach any node is outside the narrower question of what a non-failing node does with a request it receives.

## P — Partition tolerance: account for broken inter-node communication

A **network partition** prevents messages passing between groups of nodes, even though the nodes themselves may still run and receive clients. Here A can serve its local clients, B can serve its local clients, but A and B cannot communicate.

**Partition tolerance** means the design accounts for that failure instead of assuming every message eventually arrives. It does not promise every operation continues successfully during the split. To preserve one current view, the system may have to refuse or delay some operations; to keep completing operations on both sides, it may have to permit divergent observations.

Two shop branches with disconnected order ledgers are a useful analogy. Each branch can talk to local customers but cannot know the other's new entries. The analogy stops at informal human reconciliation: a real service needs explicit rules for when to answer and how to repair data later.

## Derive the conflict with one fixed example

Initial state: A and B both store post 42 with caption “Sunset,” version v1. Version v2 means the later caption “Evening.” Assume the partition can last indefinitely; A and B continue running. No concurrent third update occurs.

1. Communication between A and B breaks. Both still contain v1.
2. A receives a write of v2. To explore the availability-preserving branch, suppose A commits locally and tells its client “success.” B receives no update.
3. **After that success**, another client asks B to read post 42. B holds only v1.
4. If B returns v1, it completes the read but violates C: the acknowledged v2 write preceded this read.
5. If B waits until it can learn a safe answer, the read may never finish while the split persists. If B refuses it, it also sacrifices A for that operation.
6. B cannot simply guess “Evening.” From its perspective A might have accepted any caption or no update at all. No local calculation reveals the missing information.

| Choice at B after A acknowledged v2 | User-visible result | Promise lost during this split |
|---|---|---|
| Return its stored v1 | Stale caption | Consistency |
| Wait indefinitely for safe knowledge | No completed read | Availability |
| Return an unavailable error | Failed read | Availability |

That is the conflict. A consistency-preserving design could instead have prevented A from acknowledging step 2 without sufficient coordination. It still sacrifices some operation availability; it moves where the refusal occurs.

The formal theorem concerns single-copy read/write behavior and availability under partition in an asynchronous model, where message delays have no known bound. [Gilbert and Lynch's review of CAP](https://groups.csail.mit.edu/tds/papers/Gilbert/Brewer2.pdf) provides the precise model. Our caption trace is an illustration of the missing-information argument, not a database-product classification.

## What “choose two” hides

**CP** means prioritizing Consistency when communication is Partitioned, accepting unavailable operations. **AP** means preserving Availability during a Partition while relaxing the single-current-view promise. These labels need a specified operation and failure model; they do not mean a CP service is normally down or that an AP design has no correctness rules.

“CA” can describe a setting where partitions are excluded from the assumptions. It cannot make a real network incapable of partitioning. Also, CAP does not say a partitioned service must stop *all* operations. An unaffected part may still serve safe operations. Choices differ between public approximate counters and security-sensitive ownership checks.

For an ownership check, returning old permission data can expose something that was revoked. Prefer an explicit failure when authoritative state cannot be established. For an approximate public count, a stale displayed number may be an acceptable product choice. That choice still needs conflict handling and a freshness policy. CAP does not choose the business requirement for you.

## Project mapping and recovery

[PostService.update and feed](../../../../../src/main/java/com/instagram/backend/service/PostService.java) save and read through one repository. [PostRepository](../../../../../src/main/java/com/instagram/backend/repository/PostRepository.java) has no replica-selection policy. A/B are proposed database participants, not the current two backend instances. Adding backend instances that share one database does not itself create the replicated data scenario taught here.

## Failure drill — paper exercise

- **Predict:** after step 2, what can B safely return if the single-current-view promise must hold?
- **Detect:** compare the acknowledged write's version with B's read; record unavailable operations as failures, not successful reads.
- **Contain:** stop accepting operations that cannot meet the chosen contract. Do not silently label stale data authoritative.
- **Recover:** reconnect A/B, determine the accepted write history, repair B, and verify both reads show v2. If both sides accepted competing writes, apply an explicit resolution rule first.
- **Prevent/mitigate:** test partitions and document per-operation policy. Extra replicas improve options but cannot eliminate this information limit. Reconnection alone does not merge conflicting edits.

## Recap and stop/go

Expand CAP, explain each letter without the acronym, and reproduce why B cannot both return promptly and know v2 in this scenario. Compare the stale-response and refused-operation outcomes before advancing.

## Questions and explained answers

<details>
<summary>1. What does each letter promise for post 42?</summary>

C requires a later read to respect the completed v2 write. A requires valid completion at each non-failing node receiving requests. P includes the possibility that A and B cannot exchange messages. During that failure the service cannot guarantee both the first two promises for all operations.

</details>

<details>
<summary>2. Why does B returning v1 violate C even though v1 is stored correctly on B?</summary>

The contract describes the service's observations, not merely B's local file integrity. The v2 write completed before the read began, and no later write restored v1. A locally intact copy can still produce a globally forbidden stale answer.

</details>

<details>
<summary>3. Why does returning an error sacrifice A rather than satisfy it?</summary>

The requested read has not completed successfully with its data. Receiving some network response is weaker than the CAP operation-availability promise. The error is a useful honest signal of unavailability.

</details>

<details>
<summary>4. Can adding a third replica guarantee C and A under every partition?</summary>

No. A majority protocol can let a two-node group progress, but the isolated node cannot always serve the same guaranteed current operations. More replicas change tolerated failures, not the fundamental lack of information across a partition.

</details>
