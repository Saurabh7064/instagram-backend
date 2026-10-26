# Micro-lesson 09 — Raft: ordering a replicated log

[Module](./README.md)

- Artifact: `READY`; comprehension: `TODO`.
- Time: 10–15 minutes. Primary idea: raft: ordering a replicated log.
- Prerequisite: Complete the stop/go check for [Consensus and leader election](./08-consensus-and-election.md).
- Original roadmap topics: 12.

## Why does this exist? — Must understand

Raft uses a log, an ordered sequence of commands, and a term, an increasing election generation. Replication copies commands to followers. Followers reject stale-term leadership; voting and log rules preserve committed history.

## Analogy

A chair numbers decisions in a shared ledger. Unlike a paper ledger, a follower may hold an uncommitted suffix that is later replaced.

## Predict before reading

Is appended the same as committed? Explain your prediction before expanding the checkpoints below.

## Concrete flow

1. With three voters, leader A appends a command in its current term.
2. A and B persist it.
3. A can commit under Raft’s rule.
4. application state applies committed entries in order. A alone cannot commit a new command after losing both peers.

## Instagram-backend mapping

[PostService.create, feed, and transactional methods](../../../../../src/main/java/com/instagram/backend/service/PostService.java) are the real database boundary. Replication, consensus, distributed locks, durable request-key handling, and multi-service workflows in this lesson are **proposed**, not implemented by these annotations.

## Decision and trade-off

Use a tested Raft-backed service for coordination. Do not implement an application-level imitation from this overview. Read safety needs the implementation’s leader/quorum checks; asking an old leader is insufficient.

## Failure drill — prediction, symptom, recovery, prevention

Predict an isolated old leader’s response. In a proposed test, isolate it and send writes; verify no new majority commit. Reconnect and observe log repair. Watch leadership churn and commit latency.

These are design/paper drills, not claims that a replica cluster or fault-injection lab was executed.

## Questions and explained answers

<details>
<summary>1. Is appended the same as committed?</summary>

No. An entry can exist locally without satisfying commitment rules.

</details>

<details>
<summary>2. Can any old-term entry be committed merely by counting copies?</summary>

No. Raft directly commits by majority counting current-term entries; earlier entries become committed with the prefix.

</details>

<details>
<summary>3. Why carry a term?</summary>

It distinguishes leadership generations and lets nodes reject stale authority.

</details>

## Teach-back and stop/go

Explain the problem, trace the example, defend the choice, and diagnose the failure in 60–90 seconds without notes. Proceed only after a correct prediction, explanation, and failure diagnosis; reading this artifact does not complete comprehension.

## Useful later — primary reference

[Raft paper](https://raft.github.io/raft.pdf). Read for protocol details beyond this introductory model.
