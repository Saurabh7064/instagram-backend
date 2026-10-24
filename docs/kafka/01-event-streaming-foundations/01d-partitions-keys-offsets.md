# 01D — Find order inside one partition

**Time:** 10–15 minutes. **Prerequisite:** [01C](./01c-broker-topic-retention.md).

## One thing you will learn

Locate the ordering boundary for related post events.

## The problem first

The proposed topic contains E1: create post 42, E2: edit post 42, and E3: create post 99. We want a reader to see post 42's creation before its edit. We also want different parts of the topic to be handled independently.

## New words

| Term | Plain meaning |
|---|---|
| Partition | One ordered part of a topic |
| Key | A value supplied with an event that a producer can use to choose its partition |
| Offset | A record's numeric position within one partition |

## Must understand

Split the topic into two ordered parts, P0 and P1. For this fixed example, explicitly assume the chosen routing sends key `42` to P0 and key `99` to P1. We are choosing an illustrative assignment, not calculating Kafka's routing from the digits in a post ID.

Append E1, then E2, using key 42. They occupy P0 offsets 0 and 1. Append E3 using key 99; it occupies P1 offset 0. Each partition has its own positions. Thus offset 0 alone cannot identify an event: you need its topic and partition too.

| Topic | Partition | Offset | Event | Key |
|---|---|---:|---|---:|
| instagram.post-events | P0 | 0 | E1: post 42 created | 42 |
| instagram.post-events | P0 | 1 | E2: post 42 edited | 42 |
| instagram.post-events | P1 | 0 | E3: post 99 created | 99 |

Kafka exposes order within a partition, not one order across the whole topic. Same-key routing assumes a consistent routing strategy and partition layout; a custom strategy or a changed partition count needs separate analysis. See the [official introduction's partition model](https://kafka.apache.org/43/getting-started/introduction/).

Imagine two numbered notebooks. Page 0 in one notebook and page 0 in the other are different pages. This analogy does not imply page numbers are times or that readers across notebooks act in one shared order.

## Project connection

A proposed event for [PostService.create and update](../../../src/main/java/com/instagram/backend/service/PostService.java) could use `postId` as its key when the requirement is per-post order. It would not automatically preserve the order of every action by one author. Choose the entity whose events need ordering before choosing the key.

This layout helps distribute independent work, but a very busy single post still sends its events to one partition. Extra partitions do not automatically parallelize that post while preserving this simple ordering model.

## Predict → act → observe → explain

**Predict:** Must E3 be read after E2? Can E2 be stored before E1 despite our stated append order?

**Act:** Draw P0 with E1 at 0 and E2 at 1, and P1 with E3 at 0. Read P1 first, then P0. Next read P0 first, then P1.

**Expected observation:** The first read sequence is E3, E1, E2; the second is E1, E2, E3. Both preserve E1 before E2 in P0. Neither establishes a total order across the topic. Under the specified append sequence, P0's stored order is E1 then E2.

**Controlled failure:** Move E2 to P1 to model an inconsistent key-routing choice. The reader can now encounter the edit before the creation when it reads P1 first.

**Why and recovery:** The original guarantee applied only to P0's stored sequence. Diagnose by inspecting the event keys and partition positions. Fix future routing and reconcile already affected reader state; changing a key later does not move old events into a new order. Reading order also does not force business effects to complete in that order if the application processes records concurrently.

## Pause and check

<details>
<summary>1. Why do E1 and E3 both have offset 0?</summary>

Their positions belong to different partitions. P0 starts with E1 and P1 starts with E3 in this exercise. A full location includes topic, partition, and offset; 0 is not a global event identity.

</details>

<details>
<summary>2. Does choosing postId as the key order all actions by author 7?</summary>

No. It groups events about a post under the stated routing assumptions. Events for different posts by the same author can go to different partitions. An author-wide ordering requirement would need a different design decision.

</details>

<details>
<summary>3. Does E3, E1, E2 violate the example's ordering rule?</summary>

No. E1 still precedes E2 within P0. E3 is in P1, so the relative position of E3 versus the two P0 records is outside this ordering guarantee.

</details>

## Useful later

A key decision trades ordering scope against how evenly work can be distributed. Keep that requirement explicit when the local partition lab is activated.

<details>
<summary>Optional deep dive — Offsets are positions, not reliable counters</summary>

Real reads need not expose a record at every numeric offset because storage cleanup and transaction visibility can leave gaps. This module uses a small gap-free example for arithmetic. Do not infer a universal record count from an offset difference.

</details>

## Stop/go

Reproduce the three locations, explain why E3 can be read first, and diagnose the wrong-routing failure. Revisit the two notebooks if you still use offset as a topic-wide event number.

## Next

[01E — Consumer groups, saved progress, and replay](./01e-groups-progress-replay.md) gives different reading applications their own positions in this history.
