# Micro-lesson 16 — Vector clocks: distinguish later history from concurrent edits

[Module](./README.md) · [Previous: Lamport clocks](./15-lamport-clocks.md)

Core study: 10–15 minutes. Prerequisites: happened-before, logical counters, and why a smaller Lamport timestamp alone does not prove dependency.

## “Evening” and “Dusk”: which edit saw the other? — Must understand

A and B both store post 42 with caption “Sunset.” They lose contact. A changes it to “Evening”; B changes it to “Dusk.” When they reconnect, choosing whichever has a larger single counter can hide the important fact: neither writer saw the other's edit.

Instead of one counter, keep a separate progress count for each participant. Start with `[0,0]`, where the positions always mean `[A,B]`. A's independent edit produces `[1,0]`; B's produces `[0,1]`. The first version knows one event from A, the second knows one from B. Neither contains the other's recorded history.

This collection is a **vector clock**. Each position is a **component**, recording progress from one tracked participant rather than a caption character or a wall-clock time. Keeping the counts separate lets us ask whether one version includes everything known to the other.

If every count in V is at most its matching count in W, and at least one is smaller, W includes more recorded history. W **dominates** V. If each version has progress missing from the other, neither dominates: they are **concurrent versions**. Here “concurrent” means no represented dependency order, not necessarily simultaneous physical execution.

Comparing these histories can reveal that the two captions need a choice. It cannot make that choice for the user. We will keep the same two edits while deriving both the comparison and a later explicit resolution.

## Carry known progress forward without losing a branch

If A has made an event that B has not seen, A's component records that fact. If B has independently made another event, B's component records a different fact. Keeping both counters avoids collapsing independent knowledge into one misleading scalar.

At a local event, increment that participant's component. For a send event, carry a copy of the resulting vector. For a receive event, first take the maximum in each component across local and received history, then increment the receiver's own component. The maximum preserves known progress; the increment records the new event after that history.

For the worked storage example, the version-resolution event at A consumes both input histories and creates the new version in one event. The componentwise maximum is an intermediate calculation, not an additional separately timestamped receive event. If receiving and resolving were modeled as two separate events, A would increment for each and the final number would differ. State the event convention before comparing numbers.

The [Dynamo paper](https://www.allthingsdistributed.com/files/amazon-dynamo-sosp2007.pdf) describes vector-based version histories and reconciliation trade-offs. Here we use two fixed participants and no metadata pruning; conclusions rely on those assumptions.

## Compare the edits, then record a resolution

Fixed dataset: post 42's caption is “Sunset,” vector `[0,0]`, on replicas A/B. Component order is always `[A,B]`. They become disconnected before either edit.

1. A changes caption to “Evening.” It increments only A: `[0,0]`→`[1,0]`.
2. B independently changes caption to “Dusk.” It increments only B: `[0,0]`→`[0,1]`.
3. On reconnection compare A's `[1,0]` with B's `[0,1]`. In the A component, 1≤0 is false. Therefore A's version is not earlier than B's.
4. Compare in the other direction. In the B component, 1≤0 is false. Therefore B's version is not earlier than A's either. The histories are concurrent.
5. A receives both versions. Combine histories componentwise: A component max(1,0)=1; B component max(0,1)=1. Combined history is `[1,1]`.
6. A's chosen resolution creates a new caption “Evening at dusk.” It is a new edit, so increment A's component: `[1,1]`→`[2,1]`.
7. The resolved history dominates `[1,0]`: 1≤2 and 0≤1, with strict increases. It also dominates `[0,1]`: 0≤2 and 1≤1, with a strict increase. Both earlier edits are represented in its history.

| Version | Caption | Vector [A,B] | Relationship |
|---|---|---|---|
| Initial | Sunset | [0,0] | Precedes both edits |
| A's edit | Evening | [1,0] | Concurrent with B's edit |
| B's edit | Dusk | [0,1] | Concurrent with A's edit |
| Combined knowledge | No automatic text result | [1,1] | Includes both histories |
| New resolution at A | Evening at dusk | [2,1] | Later than both inputs |

The text in step 6 is a deliberate example resolution, not something `max` computes. The vector arithmetic merges knowledge of events; a person or domain rule merges the caption. Choosing one caption could also be a valid business policy, but it should not be misrepresented as “the clock proved this edit newer.”

## What each comparison actually tells us

Equal vectors describe the same recorded progress under the model. Ordered vectors let the service recognize that one history includes another. Incomparable vectors expose a conflict needing reconciliation. If two payloads have equal vectors but different text, the system must investigate its event-identity/version rules rather than infer that clocks alone validate both payloads.

A revision card with one tally for each branch is an analogy: each branch can see which contributions are missing. The analogy stops where participants join, restart, or disappear; real systems need stable identities and metadata-lifecycle rules.

## The history metadata also needs a lifecycle

The vector grows with the number of tracked participants. Dynamic membership, long-offline writers, and safe pruning complicate the model. Pruning can lose information needed to recognize relationships, so a bounded implementation must specify weakened assumptions instead of promising perfect knowledge of arbitrary history.

Use vector metadata when detecting concurrent versions is worth the storage, comparison, and merge complexity. Prefer an authoritative serialized writer when conflicting offline edits are not required. Prefer a smaller scalar timestamp when deterministic ordering is enough and causal conflict detection is not needed. Neither clock grants permissions or prevents conflicting writes by itself.

## Project mapping

[PostService.update](../../../../../src/main/java/com/instagram/backend/service/PostService.java) sets caption fields directly. It carries no per-replica vector or conflict response. Adding vector clocks would require protocol fields, stable participant identities, comparison rules, and a user/domain merge policy; this lesson does not claim that feature exists.

## Reconnect without silently choosing a winner — paper exercise

A returns with `[1,0]` and B with `[0,1]`. Predict whether either can be called causally newer.

Each comparison fails in a different component, so preserve both payloads as a conflict. Overwriting one because a single timestamp is larger would conceal the independent edit. Exposing unresolved text as though reconciliation had finished would also misstate the result.

Apply the documented content policy, combine history to `[1,1]`, and record A's resolving event as `[2,1]` before propagating it. Test equality, dominance, independence, restart identity, and any pruning rule. The comparison discovers the relationship between the edits; a separate product decision determines the caption the user should keep.

## Before moving on

Artifact: `READY`; comprehension: `TODO`.

Calculate `[1,0]`, `[0,1]`, `[1,1]`, and `[2,1]` without notes. Explain why the first two are incomparable and why the content result requires an explicit choice. Compare what this tells us with what Lamport counters tell us.

## Questions and explained answers

<details>
<summary>1. Why is [1,0] not earlier than [0,1], and vice versa?</summary>

The first comparison fails because A's 1 is not ≤0. The reverse fails because B's 1 is not ≤0. Each version contains progress missing from the other, so neither history includes the other.

</details>

<details>
<summary>2. How do we derive [2,1] rather than [1,1] for A's resolving edit?</summary>

Taking componentwise maxima first preserves both histories as [1,1]. A then performs a new event, so its own component increments 1→2 while B stays 1. Merely storing [1,1] would omit the new resolving event under this convention.

</details>

<details>
<summary>3. Does a vector decide that “Evening at dusk” is the correct caption?</summary>

No. It records the history relationship. The text is a separate domain or user choice; the clock cannot infer author intent from numbers.

</details>
