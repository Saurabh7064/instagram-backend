# Micro-lesson 10 — Paxos: a newer proposal must preserve an earlier decision

[Module](./README.md) · [Previous: Raft](./09-raft.md) · [Next: split brain and locks](./11-split-brain-and-locks.md)

Core study: 10–15 minutes. Prerequisites: consensus, durable state, majority overlap; a proposal is a candidate decision.

## The old proposer vanished. Was X already chosen? — Must understand

A, B, and C are the three voters for one decision. A previous attempt got value X accepted on A and B, enough for a majority. Then its coordinator stopped before announcing success. A replacement can reach B and C. It wants to choose Y.

The replacement did not see the old attempt finish, but that does not make X disappear. It needs a way to discover earlier acceptances and carry forward a value that might already be chosen. Otherwise a successful recovery could create a contradictory decision.

**Paxos** supplies rules for that recovery. The name is not an acronym. Basic Paxos chooses one value for one decision instance; repeating instances can support a longer ordered history, but one instance is not a complete replicated database.

The participant trying to choose a value is the **proposer**. The voters retaining the protocol's durable facts are **acceptors**. Proposals carry a uniquely ordered number, called a **ballot**. We will use 7 for the earlier attempt and 8 for the replacement. A real numbering scheme must prevent different proposers from issuing colliding ballot identities. Learning the chosen answer is another role, which can run in the same physical processes.

Before walking the replacement through B/C, we need to distinguish the two facts an acceptor can record.

## “I will ignore older attempts” differs from “I accepted X”

A promise says “I will not accept proposals below this ballot.” An acceptance says “I have accepted this ballot and value.” Those are different durable facts. Acceptors also retain the highest accepted ballot/value needed for recovery; forgetting them after a restart destroys the safety assumptions.

There are two phases:

1. **Prepare/promise:** ask a majority to promise a new ballot and report their prior accepted proposal, if any.
2. **Accept:** choose the value required by those reports, then ask a majority to accept it under the new ballot.

If any report contains an accepted value, the proposer must use the value attached to the highest accepted ballot it received. If none do, it can propose a fresh value. “Highest” refers to the reported **accepted** ballot, not simply the newest promise number.

## B carries X into ballot 8

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

## Why a safe protocol can still struggle to progress

Two proposers can repeatedly interrupt each other with higher ballots. They may prevent a decision while still obeying the rule that conflicting values cannot both be chosen. A stable active proposer and suitable communication conditions help progress. Consensus safety and progress are different properties.

**Multi-Paxos** means using repeated Paxos instances for a sequence, commonly with a stable leader to reduce repeated negotiation. Raft also maintains an agreed history but organizes the specification around leadership, log replication, and election restrictions. Neither protocol is simply “the fastest node wins.”

The analogy is a committee making durable numbered promises. A new chair must respect earlier accepted decisions. The analogy stops where machine restarts and delayed proposals require persisted promises and precise ordering.

## Project mapping and choice

[PostService](../../../../../src/main/java/com/instagram/backend/service/PostService.java) implements no acceptor or ballot state. A proposed authoritative coordination service might use Paxos internally. Use a tested implementation when agreement justifies its operational cost; ordinary mergeable updates or one-database transactions do not automatically require application-level Paxos. Building it introduces persistent protocol state, recovery, and contention among coordinators.

## Let ballot 8's proposer prefer Y — paper exercise

B reports `(7,X)`, but the replacement proposer wants Y. Predict which value it may send in its accept request.

A trace that discards B's accepted evidence exposes the bug before a contradictory majority forms. Stop that proposer rather than forcing Y into the same slot. A fresh ballot permits safe continuation under the preservation rules, not a fresh interpretation of a chosen decision.

Recover by examining durable promises and acceptances, then resume the correct protocol carrying X forward. Erasing acceptor state to make the system easier to start would erase its safety evidence. Test recovery between phases, competing proposers, and restoration of persisted state; progress after a restart matters only if it preserves the old decision.

## Before moving on

Artifact: `READY`; comprehension: `TODO`.

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
