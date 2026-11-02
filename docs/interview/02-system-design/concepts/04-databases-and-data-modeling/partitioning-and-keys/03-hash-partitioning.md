# Lesson 3 — Hash partitioning: spread different keys, preserve equal keys

[Module](./README.md) · [Previous: ranges](./02-range-partitioning.md) · [Next: hotspots](./04-hotspots-and-hot-keys.md)

- New core terms: hash function, bucket, modulo.

Core study: 10–15 minutes. Prerequisite: range partitioning groups neighboring values and can concentrate current writes.

## Put the same user's events in the same calculated destination — Must understand

A date split can put everyone's newest events in one busy day. Suppose we instead choose a destination from the user ID. Different users could spread across destinations, while a reader who knows the user ID could calculate where to look.

The calculation must repeat. Sending each request to a randomly selected server would leave a later read guessing where the earlier write went. A **hash function** turns an encoded key into a repeatable number: equal inputs produce equal outputs. We then turn that number into one of a fixed number of numbered destination groups, called **buckets**.

With three buckets numbered 0, 1, and 2, take the remainder after dividing the hash by 3. The remainder can only be 0, 1, or 2, so it selects a valid bucket. This operation is **modulo**, written `%` in many languages. For a nonnegative hash h and B buckets, the rule is `h % B`.

We will use tiny invented hashes so the arithmetic is visible: user 42 hashes to 35 and user 84 to 80. Their actual events and identities stay the same as in the module dataset. These teaching numbers are neither PostgreSQL hash results nor Cassandra tokens.

## Calculate the bucket, then change the bucket count

User 42 owns events 1001,1002,1003,1004; user 84 owns 2001. Apply the three-bucket calculation to each user:

- For user 42: 35=3×11+2, so 35%3=2. All four events go to bucket 2 under a user-only key.
- For user 84: 80=3×26+2, so 80%3=2. Its one event also goes to bucket 2.
- Buckets 0 and 1 receive none of these five rows. A good distribution function is a statistical tool across many keys, not a guarantee that every tiny dataset uses every bucket evenly.

Different inputs can select the same destination. That is a **collision**, not a declaration that the users are equal. The database retains the real keys, so user 42 and user 84 remain distinguishable inside bucket 2. A production rule must also define how it normalizes signed hashes; some languages produce negative remainders, which are not valid bucket numbers under our 0-to-B−1 convention.

Now add a fourth bucket and naively recompute `%4` on the same hashes:

| User | Hash | Old calculation | New calculation | Movement |
|---|---:|---|---|---|
| 42 | 35 | 35%3=2 | 35=4×8+3, so bucket 3 | 2→3 |
| 84 | 80 | 80%3=2 | 80=4×20+0, so bucket 0 | 2→0 |

All five sample rows need a new bucket mapping. This is a deterministic calculation, not a general claim that every key always moves. It demonstrates why using the physical node count directly as the divisor can cause widespread remapping when membership changes.

## All of one user still follows one unchanged key

A hash of `user_id` distributes **distinct users**. It does not divide one user's events across groups. If user 42 accounts for nearly all traffic, every request still computes the same bucket. Adding the event ID to the key could spread individual events, but then “read all of user 42's activity” no longer names one destination. Changing the key changes the query cost.

Using fixed logical buckets plus a separate bucket-to-node map is another design: the hash need not change just because a server is added, but the ownership map and movement still need management. [Consistent hashing](./05-consistent-hashing.md) tackles membership movement through ordered token ownership instead of direct modulo by node count.

PostgreSQL also supports `PARTITION BY HASH`, but it uses its own hashing implementation and child modulus/remainder definitions. Never claim `user_id % n` predicts its actual child assignment unless that is explicitly your implemented rule. Cassandra's actual placement uses a partitioner and token ring, not the toy modulo model. The [PostgreSQL CREATE TABLE syntax](https://www.postgresql.org/docs/current/sql-createtable.html) documents actual hash-partition declarations.

## Which queries become easier, and which become harder?

A repeatable sorter puts the same mailing address into the same tray. The analogy stops where hash collisions, ownership changes, and historical data migration require explicit handling. Use hashing for many independent keys when ordered range locality matters less. Avoid it if your dominant query must scan adjacent original values and would now need many destinations.

[PostRepository](../../../../../../src/main/java/com/instagram/backend/repository/PostRepository.java) has no hash router. A proposed distributed activity store would need a documented key encoding and ownership scheme. The arithmetic above is a learning model, not current backend behavior.

## Change the writer's divisor before the reader's — paper exercise

Let the writer adopt `%4` while the reader still computes `%3`. Predict whether user 42's events can be found reliably.

The writer selects bucket 3 while the reader selects bucket 2. A missing result may therefore be a routing disagreement rather than deleted data. Stop the inconsistent rollout and preserve the rows; deleting supposedly missing data would make recovery harder.

Restore a shared routing version, then migrate or dual-route deliberately and verify all five event identities. Version routing transitions and test reads while ownership changes. A repeatable calculation chooses an address; it does not copy old data into that new address.

## Before moving on

Artifact: `READY`; comprehension: `TODO`.

Derive each remainder and explain why the two different keys remain distinct. Compare distribution of many keys with splitting one hot key and with moving keys during a node change.

## Questions and explained answers

<details>
<summary>1. Why do all user 42 events choose one bucket?</summary>

The input is user_id=42 for every event. Deterministic hashing must return the same 35, so the remainder is always 2 with three buckets. Distinct event IDs do not matter unless included in the partition key.

</details>

<details>
<summary>2. Does a collision merge both users' records?</summary>

No. A shared computed destination is not equality of actual keys. The storage layer still distinguishes user 42 from 84 within that destination.

</details>

<details>
<summary>3. What is dangerous about directly using the live server count as B?</summary>

Changing the count changes many remainder calculations. Readers, writers, ownership metadata, and stored rows must transition together or requests can be misrouted.

</details>
