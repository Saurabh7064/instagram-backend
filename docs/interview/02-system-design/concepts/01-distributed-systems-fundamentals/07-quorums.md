# Micro-lesson 07 — Quorums: why overlapping copies matter

[Module](./README.md) · [Previous: transaction and observation guarantees](./06-linearizability-and-serializability.md) · [Next: consensus](./08-consensus-and-election.md)

Core study: 10–15 minutes. Prerequisites: replicas, completed writes, stale reads, and the meaning of a consistency guarantee.

## Which copies should a reader ask? — Must understand

Post 42 begins at v1 on A, B, and C. A write saves v2 on A and B, then returns success. C still has v1. If the next reader asks only C, it sees the old post. What changes if we require the reader to ask two copies?

There are only three possible pairs: A/B, A/C, and B/C. Every pair contains at least one participant in the completed write. That overlap is the useful idea. We will derive the arithmetic from these pairs, then examine why overlap alone does not tell a reader which value is safe.

A required participant count is a **quorum**. A participant's reply confirming a specified action is an **acknowledgment**; here the write replies confirm durable storage. The protocol must define that action, because “received a message” is weaker than “saved it durably.”

We also need a way to distinguish v1 from v2. A **version** identifies a data state; safely ordering concurrent updates requires additional rules. Independently chosen wall-clock timestamps do not automatically provide that order.

Use N for the fixed number of copies, W for required write acknowledgments, and R for required read responses. In our setup N=3, W=2, and we are considering R=2.

## Why two out of three cannot miss both writers

The completed write set is {A,B}; C remains at v1. Keep that membership fixed while we enumerate the read choices.

A reader waits for two responses: R=2. List every possible responding pair:

| Read responses | Overlap with write set {A,B} | Versions returned before repair |
|---|---|---|
| {A,B} | A and B | v2, v2 |
| {A,C} | A | v2, v1 |
| {B,C} | B | v2, v1 |

No two-member subset of three can avoid both A and B. Numerically, R+W=2+2=4, greater than N=3. If the sets were disjoint they would need four distinct members, but only three exist. Therefore **R+W>N** forces read/write overlap within the same membership.

Compare R=1 on this same state: a read set {C} is disjoint from {A,B} and returns only v1. Here 1+2=3, which is not greater than N; overlap is not forced.

For two write sets to overlap, each of size W, the corresponding condition is 2W>N, or **W>N/2**. With W=2, 2>1.5, so any two such write sets share a member.

## The reader still has to interpret the replies

In the {B,C} read, knowing B has v2 is useful only if the reader can safely recognize and select the appropriate version. Concurrent writers can create competing versions. A failed write can reach a subset without completing. Membership changes can make “the same three” assumption false. A temporary replacement copy may not contain the history of the unavailable member it substitutes for.

Consequently, R+W>N alone does **not** prove linearizability. Ordering, read handling, write completion, concurrency, durability, repair, and membership rules complete the protocol. The [Dynamo paper](https://www.allthingsdistributed.com/files/amazon-dynamo-sosp2007.pdf) discusses configurable participation and conflict handling; its availability-oriented mechanisms are not a proof that every quorum configuration provides a single-current-value contract.

A committee analogy explains overlap: any two groups of two from three people share someone. It stops matching when that person must compare machine versions, distinguish accepted from incomplete work, and survive crashes; intersection alone cannot interpret history.

## What do we pay for the second reply?

Waiting for two responses means the operation pays at least the time needed for its second qualifying response. If only A is reachable, W=2 cannot complete. With two suitable reachable copies, the acknowledgment count can be met, subject to the rest of the protocol. Changing W to 1 to keep accepting writes changes the guarantee; it is not cost-free recovery.

Use a documented quorum protocol when tolerating replica outages and controlling participation matter. Prefer a simpler authoritative database path when replication behavior is delegated to the database and application-level counting adds no benefit. Do not implement ad hoc “ask two servers and pick newest” logic without a version/concurrency design.

## Project mapping

[PostRepository](../../../../../src/main/java/com/instagram/backend/repository/PostRepository.java) queries one configured database interface. Neither it nor [PostService.feed](../../../../../src/main/java/com/instagram/backend/service/PostService.java) collects replica replies. The A/B/C setup is a proposed replication exercise. A future database offering quorum settings would still need its exact documented semantics checked.

## Leave only A reachable — paper exercise

B and C become unreachable before A gets a second acknowledgment. Predict whether a write requiring W=2 may now report success.

A can be alive and still unable to meet the completion rule. Inspect which participants replied and whether any stored a partial write before the timeout. Preserve the two-copy rule instead of relabeling a one-copy save as the same kind of success.

Restore connectivity and use the protocol to reconcile partial or incomplete work. Then check readable versions and the operation's retry behavior. If the acknowledgment itself was lost, its outcome remains uncertain until durable evidence resolves it. Tests should include one-member loss, two-member loss, and membership transitions; those situations exercise more than the easy case where all three copies reply.

## Before moving on

Artifact: `READY`; comprehension: `TODO`.

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
