# 01B — Pass a fact between independent programs

**Time:** 10–15 minutes. **Prerequisite:** [01A](./01a-request-work-and-waiting.md).

## One thing you will learn

Describe a completed change so a separate program can react to it later.

## The problem first

Post 42, written by user 7, has been saved. A future notification program needs to learn that it happened. The program creating the post should not need to call every future reader individually.

## New words

| Term | Plain meaning |
|---|---|
| Event | A recorded fact about something that happened |
| Producer | A program that writes events to Kafka |
| Consumer | A program that reads events from Kafka and handles them |

## Must understand

Begin with the fact: “post 42 was created by user 7.” Give this occurrence an identity, `E1`, so a reader can recognize another delivery of the same occurrence. Its event type is `PostCreated`, its post ID is 42, and its author ID is 7. These fields describe this one event; `E1` is not the post ID.

“Create post 42” asks for work that might fail. “Post 42 was created” reports a completed change. A reader may choose a reaction to the fact, such as creating a notification. That reaction is separate from the fact itself.

In Kafka, a producer writes events and a consumer reads them. Kafka sits between their schedules. Producers can publish while readers are stopped, subject to the server accepting the write and keeping the data. Those roles and their independence are described in the [official introduction](https://kafka.apache.org/43/getting-started/introduction/).

Think of a published notice: different people can act on the same notice. The analogy stops at delivery guarantees. Software needs storage, failure handling, and a way to remember what each reader has already handled.

## Project connection

The proposed producer would be near [PostService.create](../../../src/main/java/com/instagram/backend/service/PostService.java). A future notification consumer would serve [F-028](../../features/learning-backlog.md). Neither component exists yet. [Post.java](../../../src/main/java/com/instagram/backend/domain/Post.java) is the current stored post model; a Kafka event would be a separate contract, not automatically every field of that database object.

Publish facts when several independent reactions need the same change or when processing can be delayed. A direct call can be clearer when the caller needs one immediate result. Separating programs adds a contract to maintain and new failure cases to observe.

## Predict → act → observe → explain

**Predict:** If the notification program is stopped after `E1` is stored, does the fact “post 42 was created” become false?

**Act:** Write one card: `eventId=E1, type=PostCreated, postId=42, authorId=7`. Put it in a “stored facts” area. Cover the notification reader, then uncover it and let it read the same card.

**Expected observation:** The card remains a fact while the reader is absent. Once restored, the reader can react to it if storage still makes it available. Copying the card does not create another post; it is another delivery of `E1`.

**Why:** The completed business change and its later reaction have different lifetimes. A stopped reader does not undo the post. Recovery requires both a retained event and a reader able to resume. If publication failed before the card reached storage, restarting only the reader cannot recover a fact that was never published.

## Pause and check

<details>
<summary>1. Is “send a notification now” the same fact as PostCreated?</summary>

No. It asks for an action. `PostCreated` says a post was already created; the notification program chooses its reaction to that fact. Keeping this distinction clear avoids claiming an unfinished operation already happened.

</details>

<details>
<summary>2. Are eventId=E1 and postId=42 interchangeable?</summary>

No. E1 identifies this occurrence; 42 identifies the post. A later edit to post 42 would be a different occurrence. A repeated delivery of E1 still describes the original creation.

</details>

<details>
<summary>3. What must you check if the restored reader cannot find E1?</summary>

Check whether publication actually succeeded and whether storage still retains E1, then inspect where the reader starts reading. Restarting a consumer alone proves neither that publication happened nor that history is available.

</details>

## Useful later

A consumer may read E1 more than once after a failure. A future implementation must prevent a repeated delivery from creating an unintended second business result.

<details>
<summary>Optional deep dive — Producer success has a boundary</summary>

Even a successful Kafka send does not prove the notification was created. Conversely, a network timeout can leave the producer unsure whether storage accepted the event. These outcomes need explicit tests when the producer lab is activated.

</details>

## Stop/go

Explain the fact, producer, and consumer in your own words; distinguish event identity from post identity; predict what happens when the reader stops. Revisit the card exercise if you still equate publishing with finishing every reaction.

## Next

[01C — Broker, topic, and retention](./01c-broker-topic-retention.md) explains where those cards are kept and why a late reader can lose access to old history.
