# Micro-lesson 10 — Paxos: preserve an already chosen value

[Module](./README.md)

- Artifact: `READY`; comprehension: `TODO`.
- Time: 10–15 minutes. Primary idea: paxos: preserve an already chosen value.
- Prerequisite: Complete the stop/go check for [Raft: ordering a replicated log](./09-raft.md).
- Original roadmap topics: 13.

## Why does this exist? — Must understand

A proposal is a numbered candidate decision. An acceptor persists promises and accepted proposals. A value is chosen when a majority has accepted the proposal. Basic Paxos chooses one value; repeated instances build a sequence.

## Analogy

A committee promises to consider only newer ballot numbers. Unlike informal votes, promises must survive restarts to prevent conflicting decisions.

## Predict before reading

Does a promise equal acceptance? Explain your prediction before expanding the checkpoints below.

## Concrete flow

1. Proposal 7 with value X is accepted by A/B.
2. a new proposer asks B/C to promise proposal 8.
3. B reports accepted X.
4. proposer must preserve the highest-numbered accepted value it learns, proposing X.
5. a majority accepts it again.

## Instagram-backend mapping

[PostService.create, feed, and transactional methods](../../../../../src/main/java/com/instagram/backend/service/PostService.java) are the real database boundary. Replication, consensus, distributed locks, durable request-key handling, and multi-service workflows in this lesson are **proposed**, not implemented by these annotations.

## Decision and trade-off

Use established implementations when consensus is required. Stable leadership can reduce repeated negotiation in Multi-Paxos. Competing proposers may prevent progress while safety remains intact.

## Failure drill — prediction, symptom, recovery, prevention

Predict replacing X with Y after learning B’s acceptance. That breaks the preservation rule. In a paper exercise, recover by carrying X into the new proposal; persist acceptor state and stabilize a proposer for progress.

These are design/paper drills, not claims that a replica cluster or fault-injection lab was executed.

## Questions and explained answers

<details>
<summary>1. Does a promise equal acceptance?</summary>

No. It restricts future ballots but does not itself choose the proposed value.

</details>

<details>
<summary>2. Why must the new proposer preserve X here?</summary>

Its prepare responses include accepted X from the earlier majority; selecting a fresh Y could contradict the chosen value.

</details>

<details>
<summary>3. Does one Paxos instance order every application command?</summary>

No. A sequence requires multiple instances and additional implementation machinery.

</details>

## Teach-back and stop/go

Explain the problem, trace the example, defend the choice, and diagnose the failure in 60–90 seconds without notes. Proceed only after a correct prediction, explanation, and failure diagnosis; reading this artifact does not complete comprehension.

## Useful later — primary reference

[Paxos Made Simple](https://lamport.azurewebsites.net/pubs/paxos-simple.pdf). Read for protocol details beyond this introductory model.
