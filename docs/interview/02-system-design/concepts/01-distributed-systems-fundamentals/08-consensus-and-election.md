# Micro-lesson 08 — Consensus: agreeing on one decision after failures

[Module](./README.md) · [Previous: quorums](./07-quorums.md) · [Next: Raft](./09-raft.md)

- Artifact: `READY`; comprehension: `TODO`.
- Core study: 10–15 minutes. Primary idea: choosing a coordinator is only one part of preserving agreed decisions.
- Prerequisites: fixed-membership majorities overlap; an acknowledgment has a protocol-defined meaning.
- Core terms: consensus, leader election, majority.

## Why copying messages is not enough — Must understand

Three processes maintain critical configuration. Two proposals arrive: value X and value Y for the same decision. A process then crashes. We need the survivors to agree on the chosen value, rather than each choosing whichever message arrived last.

**Consensus** is a protocol by which participants agree on a decision despite the failures the protocol allows. A single decision is often one position in a longer ordered history. Accepting a proposal locally is not the same as the group choosing it.

**Leader election** selects a coordinator to organize work. A leader may propose decisions and collect acknowledgments, but its title alone cannot override previously chosen history. An old leader can still be running after others replace it.

A **majority** contains more than half the voters in a fixed membership. For N voters the minimum size is floor(N/2)+1, where floor means round down. With three voters it is floor(3/2)+1=1+1=2. With five it is floor(5/2)+1=2+1=3.

## What agreement must protect

We want no two participants to learn different chosen values for the same decision. The value must come from a valid proposal, not be invented. We also want the protocol eventually to make progress when its communication and failure assumptions permit it.

Two useful supporting words are **safety**, meaning an incorrect conflicting decision never happens, and **liveness**, meaning useful progress eventually happens under the stated conditions. A protocol can remain safe by refusing new decisions while too few voters can communicate. That is reduced availability, not necessarily corruption.

This lesson assumes crashes, recovery with required durable state, and delayed/lost messages. It does not teach tolerance of participants that deliberately lie. Different failure assumptions require different protocols and participant counts.

## One decision through a split and a replacement

Fixed group: voters A, B, C; one decision slot initially empty; candidates X and Y. Assume a correct consensus protocol, not merely a majority counter.

1. A coordinates proposal X. The protocol records sufficient durable acceptance on A and B and chooses X for this slot.
2. Communication splits into {A} and {B,C}. A is alive but alone; it cannot obtain two voters for new majority decisions.
3. B and C can form a majority. They establish new coordination authority under the protocol.
4. C suggests Y for the same slot. B carries information about chosen X. The protocol must preserve X instead of overwriting it with Y.
5. B/C can continue with later slots once safe history is established. A can catch up after reconnection; its old authority does not entitle it to replace X.

The intersection of {A,B} and {B,C} is B. This gives a path for information about X to survive the change. Durable state, election eligibility, and proposal rules turn that intersection into a safety argument. Merely seeing two “healthy” processes does not establish those rules.

## Why election alone is insufficient

Imagine selecting C because it responds fastest, without checking history. If C declares Y authoritative while B knows X was chosen, the group has elected a coordinator but lost agreement. An election protocol must connect leadership to the history and generation rules that prevent this.

A committee appointing a chair is an analogy. The chair cannot erase an already ratified decision merely by winning the next election. The analogy stops where delayed messages from old chairs, persisted machine state, and automatic failure detection require formal rules.

The [Raft authors' consensus overview](https://raft.github.io/) introduces replicated agreement and links the full protocol. Details differ between protocols; the sequence above describes required behavior, not a standalone implementation.

## When to use it

Consensus is useful for authoritative metadata, ownership, and ordered replicated decisions. It introduces communication rounds, durable protocol state, and reduced progress when a majority is unavailable. Ordinary independent work that can be safely merged may not need all participants to agree before proceeding.

Use a mature service rather than writing a voting mechanism inside the application. A database can internally use consensus without the application running its own elections. Changing voter membership carelessly can create disjoint majorities; an operator cannot safely repair quorum by simply declaring a new cluster with unrelated history.

## Project mapping

[PostService](../../../../../src/main/java/com/instagram/backend/service/PostService.java) delegates storage to its repository and has no voting or election implementation. A future coordination service could use consensus for a processing-owner decision. That is a proposed component; multiple Spring Boot instances are not automatically consensus voters.

## Failure drill — paper exercise

- **Predict:** after step 2, which side can form a majority, and may it replace X with Y?
- **Symptom:** the isolated A cannot complete new quorum decisions; B/C can proceed only while preserving chosen history.
- **Contain:** reject unsupported proposals and stale authority. Do not create an independent replacement majority by editing membership blindly.
- **Recover:** restore communication, reconcile from the accepted history, and verify the same chosen X on all participants.
- **Prevent:** test leadership changes with committed history and retain protocol state across restart. A timeout alone is not proof the old leader stopped.

## Recap and stop/go

Explain majority arithmetic, safety versus progress, and the difference between winning leadership and being allowed to change an old decision. Use B's role in the example to justify preservation.

## Questions and explained answers

<details>
<summary>1. Does choosing a leader prove consensus for the stored decisions?</summary>

No. The selection must preserve previous decisions and constrain subsequent writes. A leader with no history rule can coordinate a conflicting answer.

</details>

<details>
<summary>2. Why can {B,C} preserve X after A becomes unreachable?</summary>

B participated in the earlier deciding set and retains its required state. A correct protocol uses that overlap and its preservation rules; it cannot treat C's fresh proposal as permission to erase X.

</details>

<details>
<summary>3. Is refusing new decisions with one reachable voter a safety failure?</summary>

No. It is loss of progress under the majority requirement. Refusal can be exactly how the protocol prevents conflicting decisions while missing information.

</details>
