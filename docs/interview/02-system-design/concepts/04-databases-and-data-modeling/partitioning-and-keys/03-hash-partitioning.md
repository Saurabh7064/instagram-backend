# Lesson 3 — Hash partitioning: spread different keys, preserve equal keys

[Module](./README.md) · [Previous: ranges](./02-range-partitioning.md) · [Next: hotspots](./04-hotspots-and-hot-keys.md)

- Artifact: `READY`; comprehension: `TODO`.
- Core study: 10–15 minutes. Primary idea: deterministic hashing spreads many distinct keys but cannot divide repeated work on one unchanged key.
- Prerequisite: range partitioning groups neighboring values and can concentrate current writes.
- New core terms: hash function, bucket, modulo.

## Replace neighboring ranges with a computed group — Must understand

Suppose recent activity from many users overwhelms one date interval. Instead of grouping all users by date alone, compute a group from each user's key so different users can be spread. Both reads and writes must compute the same answer; choosing a random destination for each request would lose deterministic routing.

A **hash function** transforms a key's encoded value into a repeatable number. Equal encoded inputs produce the same output. Different inputs can produce equal outputs; that is a collision, not proof that the underlying keys are the same. The database still retains the actual partition keys.

A **bucket** is a numbered destination group. **Modulo** gives the remainder after division, written `%` in many languages. For a nonnegative hash h and B buckets, `h % B` yields a bucket from 0 through B−1. Real implementations must define how signed results are normalized; do not copy a language's negative-remainder behavior without checking.

## Calculate using deliberately small teaching hashes

Fixed dataset: user 42 owns events 1001,1002,1003,1004; user 84 owns 2001. For arithmetic only, suppose H(42)=35 and H(84)=80. These are invented hash outputs, not PostgreSQL or Cassandra tokens. There are three destination buckets, numbered 0,1,2.

- For user 42: 35=3×11+2, so 35%3=2. All four events go to bucket 2 under a user-only key.
- For user 84: 80=3×26+2, so 80%3=2. Its one event also goes to bucket 2.
- Buckets 0 and 1 receive none of these five rows. A good distribution function is a statistical tool across many keys, not a guarantee that every tiny dataset uses every bucket evenly.

The collision does not merge user 42 and 84 into one logical key. Their actual keys remain distinguishable even though they share a destination group.

Now add a fourth bucket and naively recompute `%4` on the same hashes:

| User | Hash | Old calculation | New calculation | Movement |
|---|---:|---|---|---|
| 42 | 35 | 35%3=2 | 35=4×8+3, so bucket 3 | 2→3 |
| 84 | 80 | 80%3=2 | 80=4×20+0, so bucket 0 | 2→0 |

All five sample rows need a new bucket mapping. This is a deterministic calculation, not a general claim that every key always moves. It demonstrates why using the physical node count directly as the divisor can cause widespread remapping when membership changes.

## What changed, and what did not?

A hash of `user_id` distributes **distinct users**. It does not divide one user's events across groups. If user 42 accounts for nearly all traffic, every request still computes the same bucket. Adding the event ID to the key could spread individual events, but then “read all of user 42's activity” no longer names one destination. Changing the key changes the query cost.

Using fixed logical buckets plus a separate bucket-to-node map is another design: the hash need not change just because a server is added, but the ownership map and movement still need management. [Consistent hashing](./05-consistent-hashing.md) tackles membership movement through ordered token ownership instead of direct modulo by node count.

PostgreSQL also supports `PARTITION BY HASH`, but it uses its own hashing implementation and child modulus/remainder definitions. Never claim `user_id % n` predicts its actual child assignment unless that is explicitly your implemented rule. Cassandra's actual placement uses a partitioner and token ring, not the toy modulo model. The [PostgreSQL CREATE TABLE syntax](https://www.postgresql.org/docs/current/sql-createtable.html) documents actual hash-partition declarations.

## Analogy, choice, and project mapping

A repeatable sorter puts the same mailing address into the same tray. The analogy stops where hash collisions, ownership changes, and historical data migration require explicit handling. Use hashing for many independent keys when ordered range locality matters less. Avoid it if your dominant query must scan adjacent original values and would now need many destinations.

[PostRepository](../../../../../../src/main/java/com/instagram/backend/repository/PostRepository.java) has no hash router. A proposed distributed activity store would need a documented key encoding and ownership scheme. The arithmetic above is a learning model, not current backend behavior.

## Failure drill — paper exercise

- **Predict:** a writer adopts `%4` while a reader still uses `%3`. Can user 42's existing rows be found reliably?
- **Symptom:** writes and reads reach different groups; apparently missing data can actually be misrouted data.
- **Contain:** stop the inconsistent rollout; do not delete “missing” rows based on the new map.
- **Recover:** restore a shared routing version, migrate or dual-route deliberately, and verify all five event identities.
- **Prevent:** version routing changes and test reads during movement. A repeatable hash alone does not migrate data.

## Recap and stop/go

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
