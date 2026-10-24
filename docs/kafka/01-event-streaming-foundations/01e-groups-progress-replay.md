# 01E — Save each application's reading progress

**Time:** 10–15 minutes. **Prerequisite:** [01D](./01d-partitions-keys-offsets.md).

## One thing you will learn

Explain how separate reading applications resume from separate saved positions.

## The problem first

The notification application and an analysis application both need the same post history. One should not lose access merely because the other has read faster.

Keep the exact layout from 01D: P0 has E1 at offset 0 and E2 at offset 1; P1 has E3 at offset 0. There are no other records, and all three are retained and readable.

## New words

| Term | Plain meaning |
|---|---|
| Consumer group | Consumers cooperating as one logical reading application |
| Committed offset | A group's saved restart position for a particular topic-partition; the next offset from which to resume |
| Replay | Deliberately reading retained history again |

## Must understand

Give notifications the group name `notifications-v1` and analysis the different name `analysis-v1`. Their saved progress is independent. Within a normal partition-assigned consumer group, each partition is assigned to one consumer at a time. Two consumers in that same group share the reading work; they are not two independent subscriptions. See [KafkaConsumer's group model](https://kafka.apache.org/43/javadoc/org/apache/kafka/clients/consumer/KafkaConsumer.html).

For P0, suppose notifications has successfully handled E1 and saved restart position 1. It should next resume at E2, whose offset is 1. Analysis has saved position 0, so it should start at E1. Each group also needs its own P1 position.

| Group | P0 saved next offset | P1 saved next offset | Work remaining in this fixed history |
|---|---:|---:|---|
| notifications-v1 | 1 | 0 | E2 in P0; E3 in P1 |
| analysis-v1 | 0 | 0 | E1, E2 in P0; E3 in P1 |

For notifications, P0 has `2 - 1 = 1` unread event and P1 has `1 - 0 = 1`; total `1 + 1 = 2`. For analysis, P0 has `2 - 0 = 2` and P1 has `1 - 0 = 1`; total `2 + 1 = 3`. The end positions 2 and 1 are the positions after each partition's final record. These counts are exact for this explicitly gap-free exercise, not a universal formula for business work remaining.

The consumer's current in-memory reading position can move ahead of its saved restart position. Saving progress records where to resume; it does not itself prove the business result was stored. The [KafkaConsumer position documentation](https://kafka.apache.org/43/javadoc/org/apache/kafka/clients/consumer/KafkaConsumer.html#position%28org.apache.kafka.common.TopicPartition%29) distinguishes those positions.

Think of two reading teams sharing notebooks while keeping separate bookmarks. The analogy stops at business effects: moving a bookmark does not prove a notification was saved or delivered.

## Project connection

The proposed notification consumer would create a result for [F-028](../../features/learning-backlog.md). There is no consumer implementation yet. When implemented, its database result and progress-saving order need tests. The existing [Gradle build](../../../build.gradle) currently has no Kafka client or Kafka integration-test dependency.

Separate groups are useful for independent applications. More readers in one group help distribute partitions but do not create more independent copies of every record, and consumers beyond the available partitions can be idle in this model.

## Predict → act → observe → explain

**Predict:** Notifications reads E2, stores its notification result, then stops before saving P0 progress 2. Which event will it read after restart?

**Act:** Start with the table above. Mark E2's business result as stored, but leave the notifications P0 bookmark at 1. Restart the paper reader using that bookmark.

**Expected observation:** It reads E2 again because its saved restart position is still 1. Analysis still starts P0 at 0. Neither restart deletes or rearranges the topic.

**Why and recovery:** Handling E2 and saving progress were separate actions. A future reader must recognize already applied E2 so that repeating it does not create an unintended second result; then it can safely save position 2. If it saved position 2 before storing the result and stopped, the result could instead be missing after restart.

For deliberate replay, stop the paper reader and move its P0 bookmark back to 0. It can now reread E1 and E2 while leaving P1 at 0. Real offset-reset commands and their preconditions belong in the group lab; existing valid saved progress is not reset simply by choosing an initial-start preference. The [consumer configuration reference](https://kafka.apache.org/43/configuration/consumer-configs/#auto.offset.reset) defines when the reset policy applies.

## Pause and check

<details>
<summary>1. Why does notifications save 1 after successfully handling E1 at offset 0?</summary>

The saved value identifies the next position to resume from. In this gap-free example, after offset 0 comes offset 1, which holds E2. Saving 0 would make a restart reread E1.

</details>

<details>
<summary>2. Would two consumers both named notifications-v1 each independently read all three events?</summary>

No. As members of the same normal partition-assigned group, they share the assigned partitions. Independent applications use different group identities. A member can receive work again after failure or reassignment, so sharing work is not a promise of one business effect.

</details>

<details>
<summary>3. Why is E2 read again after the described failure, and what protects the result?</summary>

Its saved P0 position stayed at 1 even though the result was stored. Restart therefore resumes with E2. The application needs durable recognition of E2's already applied effect; saving progress alone cannot prevent duplicate effects across that failure window.

</details>

## Useful later

The retry, duplicate-protection, and outbox modules address different failure windows. Kafka transactions do not automatically include an ordinary external database result; their scope is explained in [Kafka delivery semantics](https://kafka.apache.org/43/design/design/#message-delivery-semantics).

<details>
<summary>Optional deep dive — Other reading models</summary>

This lesson uses Kafka's normal partition-assigned consumer groups. Kafka also documents share groups with different assignment and acknowledgment behavior. They are outside this introductory path; do not transfer the one-consumer-per-partition rule to every reading API. See [Kafka's share consumer design](https://kafka.apache.org/43/design/design/#the-share-consumer).

</details>

## Stop/go

Recreate both groups' positions, derive the two remaining-work counts, predict the E2 repeat, and explain what would go wrong if progress were saved before the result. If unclear, physically move the two bookmarks separately and revisit 01D's event locations.

## Next

Complete the [module's integrated exercise](./README.md#integrated-paper-exercise). Module 02's local server lab is [planned](../execution-plan.md); it has not been authored or run by this initialization.
