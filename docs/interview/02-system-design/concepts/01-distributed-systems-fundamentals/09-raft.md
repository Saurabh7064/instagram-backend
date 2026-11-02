# Micro-lesson 09 — Raft: turn agreed commands into one replicated history

[Module](./README.md) · [Previous: consensus and election](./08-consensus-and-election.md) · [Next: Paxos](./10-paxos.md)

Core study: 10–15 minutes; election and read subtleties are Useful later. Prerequisites: consensus, majority, safe replacement of a leader, and durable protocol state.

## Saved on A, agreed by the group, visible in the app — Must understand

Post 42's caption is “Sunset.” A coordinates a change to “Evening” for three copies, A/B/C. When A writes that command into its own history, is it ready to tell the user the edit succeeded?

One local save is too little: A could disappear before any other participant receives it. But the group also needs more than a pile of commands. Every copy must execute the accepted commands in the same order; “set caption,” then “hide post” cannot be arbitrarily rearranged.

**Raft** is a protocol for maintaining that agreed order through a leader. Its name is not an acronym. The ordered command history is a **replicated log**, and an entry's position is its **index**. An increasing election generation, called a **term**, distinguishes leadership attempts.

Keep three different moments separate. An entry is **stored** when a participant has saved it. It is **committed** when the protocol makes it part of authoritative history. It is **applied** when its command executes against application state. A follower can have stored an entry without knowing it is committed, or know it is committed while still catching up on application.

Those stages answer our publishing question. A must establish commitment under the protocol and perform the application's required processing before it returns the corresponding completed result. Let us follow one entry through those stages.

## Follow caption entry 5 through the three stages

Fixed group: A/B/C, majority=2. Term=7, A is leader. All have the same committed history through index 4, whose last entry is from term 6. Index 5 is initially absent. Application caption for post 42 is “Sunset.” The next command is “set caption to Evening,” not a nondeterministic action such as reading the current local clock.

1. A appends command 5 with term 7 and stores it durably. At this instant A has the entry, but one copy is insufficient to commit it.
2. A sends the entry to B and C with information identifying the preceding history. B verifies the history it extends, stores entry 5, and acknowledges. C is delayed.
3. A and B now store this **current-term** entry: 2 of 3. Under Raft's commitment rule A can advance commitment through index 5.
4. A applies the command, changing its application caption from “Sunset” to “Evening,” and can return the corresponding completed result after the application's required processing.
5. B learns the commit position and applies the same command. C later catches up, learns commitment, and applies entries in order.

| Phase | A stores index 5? | B stores index 5? | Known safe application action |
|---|---|---|---|
| After step 1 | Yes | No | Do not treat one stored copy as committed |
| After step 2 | Yes | Yes | Leader can establish current-term majority commitment |
| After steps 3–4 | Yes, committed | Yes | A applies; followers apply when they learn commitment |

The purpose of ordered application is deterministic state: the same starting state plus the same commands yields the same result. External effects still need duplicate protection; Raft does not magically turn replaying an email send into a safe operation.

## Why an incomplete follower cannot simply take over — Useful later

Each voter grants at most one vote per term and retains that choice across crashes. Two winning majorities in the same term would share a voter, which cannot grant both votes. Candidates also have to pass a log-history check: compare the last entry's term first; the larger last term is considered more up to date. If last terms match, compare the last index, with the larger index more up to date. A voter rejects a candidate whose history is less up to date than its own.

On our same dataset, after index 5 is committed on A/B, imagine A crashes before C catches up. C's last entry is (term 6,index 4), while B's is (term 7,index 5). C cannot obtain B's vote by merely starting election term 8: its **log entry's term 6** is still older than B's 7. The election term and last-log term are different fields. B can seek leadership carrying entry 5 instead. This restriction helps preserve committed history instead of electing a fast but incomplete copy.

Followers compare the history preceding new entries, and conflicting uncommitted suffixes can be replaced. The term and index are protocol metadata, not a caption's human version number.

Raft directly commits entries by majority counting only when the counted entry is in the leader's current term. Earlier-term entries can become committed as part of that committed prefix. Treating every majority-stored entry in isolation as committed omits an important safety condition. See sections 5.2–5.4 of the [Raft paper](https://raft.github.io/raft.pdf) for the actual rules; this overview is not a complete implementation specification.

The ledger analogy is a chair distributing numbered instructions. An entry penciled into one ledger is different from an accepted group decision. The analogy stops where election generations and conflicting unfinished suffixes need algorithmic handling.

## What this overview leaves to a tested implementation

Use a tested Raft-backed system for coordination or replicated metadata. It costs durable writes, network confirmation, and leadership management. Losing a majority prevents new commits even when one node is healthy. An obsolete leader's local data does not by itself justify a current read; safe read paths need evidence of current authority and sufficiently applied state.

Do not use this lesson's three-stage trace as an implementation recipe. Snapshotting history, safe membership changes, and complete read protocols are separate engineering concerns. They are optional depth after the core distinctions, not unexplained prerequisites for understanding this entry.

## Project mapping

[PostService.create](../../../../../src/main/java/com/instagram/backend/service/PostService.java) has a database transaction commit but no Raft log. The shared word “commit” does not make these identical layers. A future database or coordination service might internally use Raft; verify that service's actual contract instead of assuming Spring runs the protocol.

## Disconnect A before the second copy stores entry 5 — paper exercise

A has appended entry 5 locally, then loses B and C before a second acknowledgment. Predict whether it may report the caption change as committed.

The entry exists on A, but that is only the stored stage. A client cannot obtain a successful committed result from this evidence. A later valid history may replace the uncommitted suffix, so neither applying it as authoritative nor trusting an obsolete leader's read is safe.

Restore a quorum, establish a valid leader and history, then inspect the command's committed outcome before repeating its business effect. Test failures around append, commit, and apply separately: each leaves different evidence. A durable client-operation identity is still needed for uncertain retries; an agreed log does not make every repeated application request a new or harmless operation.

## Before moving on

Artifact: `READY`; comprehension: `TODO`.

Explain the three stages using index 5. Identify why A alone cannot commit, what the current-term condition adds, and why a follower's stored entry may not yet be visible in application state.

## Questions and explained answers

<details>
<summary>1. What is the difference between appended, committed, and applied?</summary>

Appended means the log stores the entry. Committed means protocol rules establish it as authoritative history. Applied means its command has executed in the state machine. These happen at different times, especially on followers.

</details>

<details>
<summary>2. Why is A+B sufficient in the worked example?</summary>

They are two of three fixed voters and store an entry from the current leader's term 7. Those conditions permit the leader's majority commitment rule; simply saying “two copies” without the term/history conditions is incomplete.

</details>

<details>
<summary>3. Can a client trust any read returned by the isolated old A?</summary>

No. A may have lost current leadership and may not include new committed work. A safe read must establish the protocol's authority and applied-state requirements, not merely contact a live process.

</details>
