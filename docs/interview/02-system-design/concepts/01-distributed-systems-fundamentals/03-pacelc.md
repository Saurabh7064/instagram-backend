# Micro-lesson 03 — PACELC: why coordination costs time even without failure

[Module](./README.md) · [Previous: CAP](./02-cap.md) · [Next: consistency models](./04-consistency-models.md)

- Artifact: `READY`; comprehension: `TODO`.
- Core study: 10–15 minutes. Primary idea: include ordinary communication delay when choosing a read/write guarantee.
- Prerequisite: CAP's three promises; a replica is a maintained copy. This lesson reuses those meanings.
- New core term: latency.

## The question CAP leaves open — Must understand

A and B can communicate normally, but B is far away. Must every update wait for B? No machine has failed, yet the decision affects how quickly the user can publish and what a later reader can see.

**Latency** is elapsed time from starting an operation to receiving its result. Even a healthy network has nonzero latency. Waiting for an additional remote participant places some of that time on the operation's completion path. Doing work later can shorten the immediate wait, but changes what the service can safely promise meanwhile.

## Expand the whole name

**PACELC** is read as **if Partition, Availability versus Consistency; Else, Latency versus Consistency**. It is a conditional mnemonic rather than the name of one protocol.

| Letter | Meaning | Question it asks |
|---|---|---|
| P | Partition | Are participants unable to exchange messages? |
| A | Availability | During that failure, do operations still complete everywhere? |
| C | Consistency | During that failure, do observations retain the single-current-view promise? |
| E | Else | If communication is functioning, what is the normal-operation choice? |
| L | Latency | How much waiting for coordination is on the request path? |
| C | Consistency | Which observation guarantee is purchased by that coordination? |

The two C positions refer to consistency in different circumstances. The E does not introduce another correctness property. CAP explains a failure constraint; PACELC adds a question about normal operation. Daniel Abadi introduced this framing in his [discussion of CAP's limits](https://dbmsmusings.blogspot.com/2010/04/problems-with-cap-and-yahoos-little.html). It is a design lens, not a universal latency formula.

## Worked timing example

Fixed setup: post 42 begins at caption v1 on primary A and replica B. A **primary** is the participant designated to accept this example's writes. A local durable save takes 4 ms. After that save, transmitting the update to B, persisting it there, and obtaining confirmation takes another 80 ms. Ignore all other costs for this deliberately simplified calculation. There is one write, v2, and no failures.

**Policy 1: respond after the local save.** A persists v2 at t=4 ms and returns success. B receives the update later. At t=20 ms, a read at B can still return v1. The client's minimum modeled write wait is 4 ms, but a read anywhere is not guaranteed to include its completed write.

**Policy 2: respond after B confirms durable receipt.** A finishes locally at t=4 ms, then waits 80 ms more. Success returns at t=84 ms: 4+80=84. At t=20 ms the write is still in progress, so an old read then is not evidence of missing a *completed* write. Once success returns, B has the version under these assumptions.

The second policy still needs a correct read and ordering protocol. Confirmation from one replica does not make every other replica current or solve concurrent writes. This example isolates the cost of one required confirmation; it does not prove a full consistency model.

## Why the cost follows

If a result depends on information from B, A must either wait for that information, obtain an equivalent guarantee elsewhere, or weaken the result it promises. A faster CPU at A does not remove time spent awaiting B. Moving the wait from writes to reads changes which user operation pays it; it does not make coordination free.

A branch clerk answering from a local notebook is a useful analogy. Calling another branch before answering adds waiting. The analogy stops where replication protocols can confirm versions automatically and reason about ordered concurrent operations; a notebook alone supplies none of that machinery.

## Choices, limits, and common mistakes

For an approximate public count, a local answer may be valuable even if slightly old. For a permission change that must be honored before a later access, establish the required freshness through authoritative routing or coordination. Avoid making a distant replica mandatory for a low-stakes display without a requirement that justifies its cost.

Do not assign one PACELC label to an entire application and stop reasoning. A feed display and an ownership check can use different paths. **PC/EC** is shorthand for favoring consistency during a partition and consistency over latency otherwise; **PA/EL** favors availability during a partition and lower latency otherwise. These labels omit implementation details and do not prove guarantees. Specify the actual read, write, and failure behavior.

## Project mapping

[PostService.feed](../../../../../src/main/java/com/instagram/backend/service/PostService.java) queries a single repository; no distant-replica confirmation policy is implemented there. The timing values are invented for this worked calculation. A future replicated database design would require measuring actual confirmation time and stale-read frequency together. Backend method execution time alone may omit network and client waiting.

## Failure drill — paper exercise

- **Predict:** increase only the confirmation delay from 80 to 200 ms. What happens under each policy?
- **Symptom:** policy 2 now takes at least 4+200=204 ms in the model. Policy 1 still acknowledges at 4 ms, while B may remain old longer.
- **Contain:** do not quietly switch to local acknowledgment while retaining the stronger claim. Expose unavailability or explicitly downgrade only operations whose contract permits it.
- **Recover:** restore the path, confirm catch-up, then compare observed write latency and read versions.
- **Mitigate:** select a closer coordination group or revise a display's freshness requirement. Those choices change topology or contract; they do not repeal communication costs.

## Recap and stop/go

Expand all six letters, distinguish P from E, and derive 4 ms, 84 ms, and 204 ms from the setup. Explain what the acknowledgment policies do and what they do not prove.

## Questions and explained answers

<details>
<summary>1. Why do we need the “Else” branch when no node has failed?</summary>

Healthy communication still consumes time. Waiting for remote information can improve the promised observation while increasing the operation's latency. CAP's partition result alone does not describe this ordinary-operation choice.

</details>

<details>
<summary>2. Is B reading v1 at t=20 ms the same under both policies?</summary>

The returned bytes match, but the context differs. Under policy 1, v2 already completed at t=4; under policy 2, it is still pending until t=84. Completed versus overlapping operations matters to the stated guarantee.

</details>

<details>
<summary>3. Does waiting 84 ms prove all reads are linearizable?</summary>

No. It establishes B's receipt for this single write under the example's assumptions. Other replicas, routing, concurrency, and protocol ordering still require analysis.

</details>
