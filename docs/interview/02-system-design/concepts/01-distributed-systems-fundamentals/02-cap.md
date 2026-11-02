# Micro-lesson 02 — CAP: Consistency, Availability, Partition tolerance

[Module](./README.md) · [Previous: partial failure](./01-partial-failure.md) · [Next: PACELC](./03-pacelc.md)

Core study: 10–15 minutes. Prerequisite: a missing message does not prove a remote process stopped. A read fetches a value; a write changes it.

## B missed the caption update. What can it answer? — Must understand

Post 42 starts with caption “Sunset” on database machines A and B. Keeping a second maintained copy, called a **replica**, gives us another place to read when one machine is unreachable. Sending changes between those copies is **replication**. A participating machine or process is often called a **node**.

Now the connection between A and B breaks. Both machines still run and can receive requests from their own clients. This is a **network partition**: the participants are alive, but messages cannot pass between them. Assume it can last indefinitely.

You change the caption through A to “Evening.” To explore what happens if both sides keep completing operations, suppose A saves the change and says “success.” Call the old version v1 and the new version v2. B still has v1. There is no third edit in this example.

After that success, another user asks B for the caption. B faces a problem no faster processor can solve: it does not know what A accepted. From B's perspective, A might have saved “Evening,” another caption, or nothing at all.

## Two reasonable responses, two different promises

B could return “Sunset.” The user gets a completed read, but an already successful edit seems to disappear. Or B could wait until it can establish a safe answer. If the connection never recovers, that read never finishes. Returning an unavailable response, such as HTTP 503, is another honest way to refuse the read; it still leaves the requested operation unavailable.

| B's response after A acknowledged v2 | What the user observes | What the service gives up |
|---|---|---|
| Return stored v1 | A stale caption | The single-current-value promise |
| Wait indefinitely for safe information | No completed read | Operation availability |
| Return an unavailable error | A failed read | Operation availability |

This is the conflict described by **CAP: Consistency, Availability, and Partition tolerance**. The names become useful once we connect them to the choice B is making.

**Consistency** here means the service behaves like one up-to-date copy. A read begun after a write completes must reflect that write or a later one. Therefore B returning v1 after A's successful v2 write violates the promise. Overlapping operations can be ordered either way if their results fit that order. This precise behavior is called **linearizability**, developed in [lesson 6](./06-linearizability-and-serializability.md).

The promise concerns visible results. B may lag internally if the read path waits, routes elsewhere, or otherwise avoids exposing the old value. It is separate from database rules such as “caption cannot be null.”

**Availability** in CAP means every request received by a non-failing node eventually completes with a valid result for its operation. An error packet is a response, but it is not a successful caption read. This formal promise has no specified millisecond deadline; production availability targets usually add one. A client unable to reach any node is outside this narrower question about requests a working node receives.

**Partition tolerance** means accounting for broken communication rather than assuming it cannot occur. It does not mean all operations must succeed during the break. Once the split exists, the service cannot guarantee both of the preceding promises for all covered operations.

## Could A have made a different choice?

Yes. A could have withheld success until it had enough coordination to preserve the current-value promise. Then the unavailable operation occurs at the write rather than B's read. Changing where we wait does not remove the missing information.

Two shop branches with disconnected order ledgers offer a useful picture: each can serve local customers, but cannot know the other's new entries. The analogy stops at recovery. A service needs explicit rules for accepted history and conflicting edits; reconnecting the cable does not decide them.

The theorem uses single-copy read/write behavior and availability in an asynchronous model, where message delays have no known bound. [Gilbert and Lynch's review of CAP](https://groups.csail.mit.edu/tds/papers/Gilbert/Brewer2.pdf) gives the precise model. The caption example illustrates the information problem within those assumptions.

## Choose the promise for the operation

**CP** means prioritizing Consistency when communication is Partitioned, accepting unavailable operations. **AP** means preserving Availability during a Partition while relaxing the single-current-view promise. These labels need a specified operation and failure model; they do not mean a CP service is normally down or that an AP design has no correctness rules.

“CA” can describe a setting where partitions are excluded from the assumptions. It cannot make a real network incapable of partitioning. Also, CAP does not say a partitioned service must stop *all* operations. An unaffected part may still serve safe operations. Choices differ between public approximate counters and security-sensitive ownership checks.

For an ownership check, returning old permission data can expose something that was revoked. Prefer an explicit failure when authoritative state cannot be established. For an approximate public count, a stale displayed number may be an acceptable product choice. That choice still needs conflict handling and a freshness policy. CAP does not choose the business requirement for you.

## Project mapping and recovery

[PostService.update and feed](../../../../../src/main/java/com/instagram/backend/service/PostService.java) save and read through one repository. [PostRepository](../../../../../src/main/java/com/instagram/backend/repository/PostRepository.java) has no replica-selection policy. A/B are proposed database participants, not the current two backend instances. Adding backend instances that share one database does not itself create the replicated data scenario taught here.

## Reconnect the copies without guessing the history — paper exercise

After A acknowledges v2, predict what B can safely return if later reads must include that completed edit. Returning v1 is not made safe by B being healthy.

Compare the acknowledged write version with the read result. Count a refused operation as unavailable rather than recording its error response as a successful read. While the split lasts, prevent operations that cannot meet their chosen contract; stale permission data must not quietly become authoritative.

After reconnection, establish the accepted write history, bring B to v2, and verify reads on both copies. If both sides accepted competing edits, apply the explicit resolution rule before claiming recovery. Partition tests and per-operation policies help expose this boundary. Extra replicas provide more options, but neither extra copies nor reconnection alone resolves missing or conflicting history.

## Before moving on

Artifact: `READY`; comprehension: `TODO`.

Expand CAP, explain each letter without the acronym, and reproduce why B cannot both return promptly and know v2 in this scenario. Compare the stale-response and refused-operation outcomes before advancing.

## Questions and explained answers

<details>
<summary>1. What does each letter promise for post 42?</summary>

C requires a later read to respect the completed v2 write. A requires valid completion at each non-failing node receiving requests. P includes the possibility that A and B cannot exchange messages. During that failure the service cannot guarantee both the first two promises for all operations.

</details>

<details>
<summary>2. Why does B returning v1 violate C even though v1 is stored correctly on B?</summary>

The contract describes the service's observations, not merely B's local file integrity. The v2 write completed before the read began, and no later write restored v1. A locally intact copy can still produce a globally forbidden stale answer.

</details>

<details>
<summary>3. Why does returning an error sacrifice A rather than satisfy it?</summary>

The requested read has not completed successfully with its data. Receiving some network response is weaker than the CAP operation-availability promise. The error is a useful honest signal of unavailability.

</details>

<details>
<summary>4. Can adding a third replica guarantee C and A under every partition?</summary>

No. A majority protocol can let a two-node group progress, but the isolated node cannot always serve the same guaranteed current operations. More replicas change tolerated failures, not the fundamental lack of information across a partition.

</details>
