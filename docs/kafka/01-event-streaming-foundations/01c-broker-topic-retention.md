# 01C — Keep history for a later reader

**Time:** 10–15 minutes. **Prerequisite:** [01B](./01b-events-producers-consumers.md).

## One thing you will learn

Explain why reading an event and removing an event are different operations.

## The problem first

A future notification program is offline while posts change. When it returns, it needs the missed facts. That only works if another program has kept them long enough.

## New words

| Term | Plain meaning |
|---|---|
| Broker | A Kafka server that stores and serves event data |
| Topic | A named stream where related events are stored |
| Retention | The storage policy that limits how long or how much event history is kept |

## Must understand

For this exercise, use the proposed topic `instagram.post-events`. Its three event cards are E1: post 42 created, E2: post 42 edited, and E3: post 99 created. E1 and E2 concern the same post but are separate facts. A producer writes them to Kafka, and a broker stores their data under the topic.

A consumer reads without removing the stored record merely by reading it. A different reader can use that history later. Keeping data consumes storage, so the topic needs a retention policy. This exercise assumes a topic configured to delete old history; other cleanup modes are postponed. The [Kafka topic configuration reference](https://kafka.apache.org/43/configuration/topic-configs/) documents time and size limits and cleanup policies.

Imagine a shared notebook: reading a page does not tear it out. The analogy stops at access to history: real Kafka has configurable cleanup and multiple storage files, not a notebook promising permanent pages.

## Project connection

This proposed topic does not replace the application's [PostRepository](../../../src/main/java/com/instagram/backend/repository/PostRepository.java). The database remains the current way to save and query posts. Event history would let future notification and analysis programs react to changes. The [Compose file](../../../docker-compose.yml) does not currently run a broker.

Retained history helps when readers need to catch up or reread. It adds disk use and a recovery deadline. If a reader falls behind the available history, a restart alone cannot recreate missing events.

## Predict → act → observe → explain

**Predict:** Will reading E1 once prevent another program from reading it?

**Act:** Put E1, E2, and E3 in a shared storage area. Have one reader copy E1 into its own notes. Leave all three original cards in storage. Let a second reader read them.

**Expected observation:** Both readers can read E1. The first read did not delete the original. This is a paper model, not evidence from a running broker.

**Controlled failure:** Now remove E1 from shared storage to model cleanup after it falls outside the retention policy. A reader that still needs E1 cannot obtain it from this topic.

**Why and recovery:** Reading and storage cleanup are independent. Recovery may require rebuilding a reader's derived result from current database data or restoring history from an independently maintained archive. If neither is possible, the missing historical fact is unrecoverable through this topic. Increasing retention afterward does not resurrect removed data.

## Pause and check

<details>
<summary>1. Does reading E1 delete it from the topic?</summary>

No. A normal read does not remove its stored record. Cleanup follows the topic's policy independently of whether a particular application has finished reading it.

</details>

<details>
<summary>2. Can a late reader always recover E1 by restarting?</summary>

No. E1 must still be available, or another recovery source must exist. Restarting changes the reader's execution, not the storage policy or the existence of deleted history.

</details>

<details>
<summary>3. Is a topic a replacement for querying current posts?</summary>

Not automatically. The current database query returns stored application state. The proposed topic holds facts about changes. Turning those facts into a queryable view requires additional processing and recovery design.

</details>

## Useful later

Storage is cleaned up in groups of records, so a configured time limit is not a promise that each record disappears at an exact birthday. Configure recovery margins rather than relying on an exact deletion instant. See [retention configuration](https://kafka.apache.org/43/configuration/topic-configs/#retention.ms).

<details>
<summary>Optional deep dive — A different cleanup goal</summary>

Compaction is a cleanup mode that eventually keeps a latest value for a key rather than preserving every historical version indefinitely. It has different semantics and deserves a separate lesson. The card exercise here models deletion-based retention only.

</details>

## Stop/go

Explain where events are stored, show two readers using E1, and diagnose why cleanup can make a late reader unable to catch up. If you assume a read removes the original card, repeat the exercise before moving on.

## Next

[01D — Partitions, keys, and offsets](./01d-partitions-keys-offsets.md) explains where order and event positions exist inside a topic.
