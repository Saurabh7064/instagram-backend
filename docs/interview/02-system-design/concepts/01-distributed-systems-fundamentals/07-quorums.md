# Micro-lesson 07 — Quorums: why overlapping copies matter

[Module](./README.md) · [Previous: transaction and observation guarantees](./06-linearizability-and-serializability.md) · [Next: consensus](./08-consensus-and-election.md)

- Artifact: `READY`; comprehension: `TODO`.
- Core study: 10–15 minutes. Primary idea: counting enough responses forces overlap, but overlap still needs a correct protocol.
- Prerequisites: replicas, completed writes, stale reads, and the meaning of a consistency guarantee.
- Core terms: quorum, acknowledgment, version.

## What if a reader asks the wrong copy? — Must understand

A write reaches two copies and returns success, but a reader asks only the third copy and gets an old value. Can asking more copies guarantee that the reader contacts at least one participant in the completed write?

A **quorum** is the required number of participants for an operation. An **acknowledgment** is a participant's confirmation of a specified action, such as durable storage; the protocol must say what it confirms. A **version** identifies a data state so a reader can distinguish or order updates. A timestamp chosen independently by each writer is not automatically a reliable version order.

Let **N** be the number of copies in one fixed membership, **W** the number of write acknowledgments required, and **R** the number of read responses required. These are symbols with defined meanings, not product settings we have already configured.

## Derive the overlap instead of memorizing a formula

Fixed replicas: A, B, C, so N=3. Post 42 begins at v1 on all three. A single write changes it to v2 and waits for durable storage on A and B: W=2. C still has v1.

A reader waits for two responses: R=2. List every possible responding pair:

| Read responses | Overlap with write set {A,B} | Versions returned before repair |
|---|---|---|
| {A,B} | A and B | v2, v2 |
| {A,C} | A | v2, v1 |
| {B,C} | B | v2, v1 |

No two-member subset of three can avoid both A and B. Numerically, R+W=2+2=4, greater than N=3. If the sets were disjoint they would need four distinct members, but only three exist. Therefore **R+W>N** forces read/write overlap within the same membership.

Compare R=1 on this same state: a read set {C} is disjoint from {A,B} and returns only v1. Here 1+2=3, which is not greater than N; overlap is not forced.

For two write sets to overlap, each of size W, the corresponding condition is 2W>N, or **W>N/2**. With W=2, 2>1.5, so any two such write sets share a member.

## What the arithmetic does not solve

In the {B,C} read, knowing B has v2 is useful only if the reader can safely recognize and select the appropriate version. Concurrent writers can create competing versions. A failed write can reach a subset without completing. Membership changes can make “the same three” assumption false. A temporary replacement copy may not contain the history of the unavailable member it substitutes for.

Consequently, R+W>N alone does **not** prove linearizability. Ordering, read handling, write completion, concurrency, durability, repair, and membership rules complete the protocol. The [Dynamo paper](https://www.allthingsdistributed.com/files/amazon-dynamo-sosp2007.pdf) discusses configurable participation and conflict handling; its availability-oriented mechanisms are not a proof that every quorum configuration provides a single-current-value contract.

A committee analogy explains overlap: any two groups of two from three people share someone. It stops matching when that person must compare machine versions, distinguish accepted from incomplete work, and survive crashes; intersection alone cannot interpret history.

## Cost and availability

Waiting for two responses means the operation pays at least the time needed for its second qualifying response. If only A is reachable, W=2 cannot complete. With two suitable reachable copies, the acknowledgment count can be met, subject to the rest of the protocol. Changing W to 1 to keep accepting writes changes the guarantee; it is not cost-free recovery.

Use a documented quorum protocol when tolerating replica outages and controlling participation matter. Prefer a simpler authoritative database path when replication behavior is delegated to the database and application-level counting adds no benefit. Do not implement ad hoc “ask two servers and pick newest” logic without a version/concurrency design.

## Project mapping

[PostRepository](../../../../../src/main/java/com/instagram/backend/repository/PostRepository.java) queries one configured database interface. Neither it nor [PostService.feed](../../../../../src/main/java/com/instagram/backend/service/PostService.java) collects replica replies. The A/B/C setup is a proposed replication exercise. A future database offering quorum settings would still need its exact documented semantics checked.

## Failure drill — paper exercise

- **Predict:** B and C become unreachable before A can obtain a second acknowledgment. Can A acknowledge W=2?
- **Symptom:** timeout or refusal despite A being alive. Inspect participant responses and whether any partial write was stored.
- **Contain:** preserve the configured completion rule; do not report a one-copy write as a two-copy success.
- **Recover:** restore connectivity, reconcile any partial/incomplete operation under the protocol, then verify readable versions and safe retry behavior.
- **Prevent:** test one- and two-member outages and membership changes. Record the operation's outcome as uncertain when the acknowledgment result is lost.

## Recap and stop/go

Enumerate all three read pairs and derive both inequalities. Explain why one reachable node cannot satisfy W=2 and name two protocol requirements beyond counting.

## Questions and explained answers

<details>
<summary>1. Why must {B,C} encounter this completed write?</summary>

The write stored v2 on {A,B}; the read set shares B. In this fixed one-write example, B contains v2. A version-aware read can distinguish it from C's v1.

</details>

<details>
<summary>2. Why does R+W=N fail to guarantee overlap?</summary>

With N=3, R=1 and W=2, {C} and {A,B} are disjoint. Their total size fits exactly without sharing a member, so the read can miss the write.

</details>

<details>
<summary>3. Why is increasing W not a universal improvement?</summary>

It adds required confirmations, waiting, and dependence on more reachable participants. It can improve participation durability/overlap within a protocol, but can reduce operation availability and does not independently solve concurrent ordering.

</details>
