# Micro-lesson 10 — Paxos: a newer proposal must preserve an earlier decision

[Module](./README.md) · [Previous: Raft](./09-raft.md) · [Next: split brain and locks](./11-split-brain-and-locks.md)

- Artifact: `READY`; comprehension: `TODO`.
- Core study: 10–15 minutes. Primary idea: proposal numbers and promises prevent replacing a value that may already have been chosen.
- Prerequisites: consensus, durable state, majority overlap; a proposal is a candidate decision.
- Core terms: proposer, acceptor, ballot.

## Why “let the new leader pick” is unsafe — Must understand

A coordinator stops responding after some participants accepted X. A replacement cannot necessarily know whether X reached enough participants to be chosen. Selecting a fresh Y from scratch risks contradicting a decision that already exists.

**Paxos is a proper name, not an acronym.** Basic Paxos chooses one value for one decision instance. Repeating the mechanism can support an ordered history, but one instance alone is not a complete replicated database.

A **proposer** tries to get a candidate value chosen. An **acceptor** retains protocol state and votes by accepting proposals under the rules. A **ballot** is a uniquely ordered proposal number; in this example 8 is newer than 7. Real implementations need a numbering scheme that avoids collisions between proposers. Learning the chosen result is another role, often performed by the same physical processes.

## Understand promise versus acceptance

A promise says “I will not accept proposals below this ballot.” An acceptance says “I have accepted this ballot and value.” Those are different durable facts. Acceptors also retain the highest accepted ballot/value needed for recovery; forgetting them after a restart destroys the safety assumptions.

There are two phases:

1. **Prepare/promise:** ask a majority to promise a new ballot and report their prior accepted proposal, if any.
2. **Accept:** choose the value required by those reports, then ask a majority to accept it under the new ballot.

If any report contains an accepted value, the proposer must use the value attached to the highest accepted ballot it received. If none do, it can propose a fresh value. “Highest” refers to the reported **accepted** ballot, not simply the newest promise number.

## Derive preservation from a fixed example

Three acceptors: A/B/C; majority=2. There is one decision slot. Initially none has promised or accepted anything.

1. An earlier proposer successfully gets ballot 7, value X accepted by A and B. Since two of three accepted, X is chosen, even if the proposer crashes before announcing that fact.
2. A new proposer starts ballot 8 and sends prepare requests to B and C.
3. B promises 8 and reports its acceptance `(7,X)`. C promises 8 and reports no accepted value.
4. Among the returned accepted proposals, `(7,X)` has the highest ballot. The proposer must ask for `(8,X)`, not `(8,Y)`.
5. B/C accept `(8,X)` if they have not since promised a higher ballot. A majority again accepts X. Its ballot changed; the chosen value did not.

| Acceptor | Accepted before prepare 8 | Promise after prepare 8 | Accepted after successful accept 8 |
|---|---|---|---|
| A | (7,X) | 7 in this trace | (7,X) |
| B | (7,X) | 8 | (8,X) |
| C | none | 8 | (8,X) |

Why must X be carried forward? The old majority {A,B} and new prepare majority {B,C} overlap at B. B exposes the earlier acceptance. Choosing Y despite that evidence would allow a later majority to contradict chosen X. The proposer may not know every old acceptance, so it follows a preservation rule that remains safe with incomplete knowledge.

The required rules and quorum argument are specified in Lamport's [Paxos Made Simple](https://lamport.azurewebsites.net/pubs/paxos-simple.pdf). The numerical table is a worked instance; it is not a substitute for the paper's proof.

## Progress and comparison with Raft

Two proposers can repeatedly interrupt each other with higher ballots. They may prevent a decision while still obeying the rule that conflicting values cannot both be chosen. A stable active proposer and suitable communication conditions help progress. Consensus safety and progress are different properties.

**Multi-Paxos** means using repeated Paxos instances for a sequence, commonly with a stable leader to reduce repeated negotiation. Raft also maintains an agreed history but organizes the specification around leadership, log replication, and election restrictions. Neither protocol is simply “the fastest node wins.”

The analogy is a committee making durable numbered promises. A new chair must respect earlier accepted decisions. The analogy stops where machine restarts and delayed proposals require persisted promises and precise ordering.

## Project mapping and choice

[PostService](../../../../../src/main/java/com/instagram/backend/service/PostService.java) implements no acceptor or ballot state. A proposed authoritative coordination service might use Paxos internally. Use a tested implementation when agreement justifies its operational cost; ordinary mergeable updates or one-database transactions do not automatically require application-level Paxos. Building it introduces persistent protocol state, recovery, and contention among coordinators.

## Failure drill — paper exercise

- **Predict:** ballot 8's proposer receives `(7,X)` from B but wants Y. Which value may it send?
- **Symptom of the bug:** a trace shows a new proposal discarding prior accepted evidence. If enough nodes accept it, agreement can be broken.
- **Contain:** stop the faulty proposer; do not force a new value into the same decision slot.
- **Recover:** inspect durable promises/acceptances and resume a correct protocol preserving X. Never erase acceptor state to make recovery easier.
- **Prevent:** model crashes between phases and competing proposals; test persistent state restoration. A new ballot is authority to continue safely, not permission to forget history.

## Recap and stop/go

Explain each role, both phases, and why `(8,X)` follows from the B/C reports. Distinguish promised, accepted, and chosen; then contrast safety with progress.

## Questions and explained answers

<details>
<summary>1. Is X chosen only when every acceptor knows the result?</summary>

No. In this instance A and B accepting ballot 7 already choose X. Knowledge of that outcome can lag or be lost at the proposer, so recovery must preserve decisions even when the replacement is uncertain.

</details>

<details>
<summary>2. Why may the ballot-8 proposer not choose Y?</summary>

Its prepare quorum reported accepted `(7,X)`, the highest accepted ballot in those replies. Carrying X forward is required to avoid contradicting an earlier chosen value. A newer ballot does not mean a new business value is safe.

</details>

<details>
<summary>3. If higher ballots keep interrupting attempts, has agreement necessarily failed?</summary>

No. Progress can stall while safety holds. Agreement fails if conflicting values become chosen, not merely because participants take too long to choose one.

</details>
