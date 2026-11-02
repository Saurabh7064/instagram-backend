# Micro-lesson 08 — Consensus: agreeing on one decision after failures

[Module](./README.md) · [Previous: quorums](./07-quorums.md) · [Next: Raft](./09-raft.md)

Core study: 10–15 minutes. Prerequisites: fixed-membership majorities overlap; an acknowledgment has a protocol-defined meaning.

## The coordinator changed. Does the decision change too? — Must understand

A, B, and C maintain one critical configuration decision. A coordinates value X, and A/B durably accept enough information for the protocol to choose X. Then communication breaks: A is isolated, while B and C can still talk. C would prefer value Y.

Should B/C be allowed to choose Y for that same decision? If they do, the system has two contradictory answers. A's disappearance cannot turn an already chosen value back into an empty slot. Replacing the coordinator must preserve accepted history.

**Consensus** is a protocol for agreeing on a decision despite specified failures. One decision may occupy one position in a longer ordered history. Receiving or accepting a proposal locally is only a step toward that group decision.

**Leader election** chooses a coordinator. It answers “who organizes the work now?” rather than “which previous decisions may we erase?” An old leader can also remain alive after losing communication, so a title or timeout cannot by itself establish exclusive authority.

## Why B is the important participant

The old agreeing group was {A,B}; the replacement group is {B,C}. B connects the two. With correct durable state and protocol rules, B can carry the knowledge needed to preserve X. Simply counting two live machines would miss that requirement.

A **majority** means more than half the fixed voters. For N voters the minimum is floor(N/2)+1, where floor rounds down. Three voters need floor(3/2)+1=1+1=2. Five need floor(5/2)+1=2+1=3. Two majorities in the same membership always overlap.

We want two outcomes from the protocol: no conflicting decisions, and eventual useful progress when conditions permit it. The first is **safety**; the second is **liveness**. When only A is reachable, refusing a new decision can preserve safety while progress stops.

Our failure model includes crashes, recovery with required durable state, and delayed or lost messages. It does not include participants deliberately lying. Agreement under that different assumption requires different protocols and counts.

## Follow X across the change of coordinator

Rewind to the empty decision slot and follow the full sequence. Assume a correct consensus protocol, including its history rules:

1. A coordinates proposal X. The protocol records sufficient durable acceptance on A and B and chooses X for this slot.
2. Communication splits into {A} and {B,C}. A is alive but alone; it cannot obtain two voters for new majority decisions.
3. B and C can form a majority. They establish new coordination authority under the protocol.
4. C suggests Y for the same slot. B carries information about chosen X. The protocol must preserve X instead of overwriting it with Y.
5. B/C can continue with later slots once safe history is established. A can catch up after reconnection; its old authority does not entitle it to replace X.

The intersection of {A,B} and {B,C} is B. This gives a path for information about X to survive the change. Durable state, election eligibility, and proposal rules turn that intersection into a safety argument. Merely seeing two “healthy” processes does not establish those rules.

## A fast response does not prove a safe history

Imagine selecting C because it responds fastest, without checking history. If C declares Y authoritative while B knows X was chosen, the group has elected a coordinator but lost agreement. An election protocol must connect leadership to the history and generation rules that prevent this.

A committee appointing a chair is an analogy. The chair cannot erase an already ratified decision merely by winning the next election. The analogy stops where delayed messages from old chairs, persisted machine state, and automatic failure detection require formal rules.

The [Raft authors' consensus overview](https://raft.github.io/) introduces replicated agreement and links the full protocol. Details differ between protocols; the sequence above describes required behavior, not a standalone implementation.

## When is agreement worth the coordination cost?

Consensus is useful for authoritative metadata, ownership, and ordered replicated decisions. It introduces communication rounds, durable protocol state, and reduced progress when a majority is unavailable. Ordinary independent work that can be safely merged may not need all participants to agree before proceeding.

Use a mature service rather than writing a voting mechanism inside the application. A database can internally use consensus without the application running its own elections. Changing voter membership carelessly can create disjoint majorities; an operator cannot safely repair quorum by simply declaring a new cluster with unrelated history.

## Project mapping

[PostService](../../../../../src/main/java/com/instagram/backend/service/PostService.java) delegates storage to its repository and has no voting or election implementation. A future coordination service could use consensus for a processing-owner decision. That is a proposed component; multiple Spring Boot instances are not automatically consensus voters.

## Isolate A after X was chosen — paper exercise

The split leaves {A} and {B,C}. Predict which side can gather a majority, then answer a separate question: may that majority replace X with Y in the same slot?

A cannot complete new majority decisions alone. B/C may progress only by preserving chosen history. Unsupported proposals or stale authority must be rejected; editing the membership to invent an independent replacement majority can destroy the overlap on which that protection depends.

Restore communication and bring participants to the accepted history. Verify that they learn the same chosen X, rather than merely checking that every process responds. Rehearse leadership changes with real committed history and retained protocol state. A timeout is evidence of missing communication, not proof that the old leader stopped running.

## Before moving on

Artifact: `READY`; comprehension: `TODO`.

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
