# Lesson 5 — Consistent hashing reduces remapping when membership changes

[Module](./README.md) · [Previous: hotspots](./04-hotspots-and-hot-keys.md) · [Next: SQL keys](./06-sql-partition-key-and-table-creation.md)

- Artifact: `READY`; comprehension: `TODO`.
- Core study: 10–15 minutes. Primary idea: change ownership of a portion of a stable hash space instead of changing every key's divisor.
- Prerequisites: deterministic hashing, modulo remapping, and why an unchanged hot key stays concentrated.
- New core terms: token, token ring, virtual node.

## What should happen when we add a server? — Must understand

Directly computing `hash(key) % live_node_count` makes membership part of every routing calculation. As lesson 3 showed, adding a destination can move keys even when their original servers remain healthy. We want a stable hash space with an ownership rule that changes only selected ranges.

A **token** is a numeric position produced by hashing the complete partition key. A **token ring** treats the ordered token space as wrapping from its largest position back to its smallest. **Consistent hashing** assigns ownership using positions/ranges in this space so adding a member moves the affected ownership ranges rather than recomputing every key modulo a new count.

The word “consistent” here describes placement under membership change. It is not the read-consistency promise from CAP and does not mean all replicas immediately show the same value.

## Walk a tiny ring with real boundaries

Teaching ring: integer positions 0–99. These are **toy tokens**, not Cassandra Murmur3 outputs. Nodes have endpoints A=20, B=50, C=90. A node owns the interval after its predecessor through its own endpoint: `(previous,current]`. The right endpoint is included.

- A owns `(90,20]`, which wraps: 91–99 and 0–20.
- B owns `(20,50]`, positions 21–50.
- C owns `(50,90]`, positions 51–90.

Use the same three active user/day keys as the activity dataset. Assume teaching hashes H(42,Oct25)=35, H(84,Oct25)=80, H(42,Oct26)=65.

1. Token 35: moving clockwise first reaches B=50, so B is the primary range owner.
2. Token 80: first reaches C=90, so C owns it.
3. Token 65: also first reaches C=90.

Now insert D=70. D takes `(50,70]` from C; C retains `(70,90]`.

| Key | Token | Owner before | Owner after D=70 | Why |
|---|---:|---|---|---|
| (42,Oct25) | 35 | B | B | Outside changed range |
| (84,Oct25) | 80 | C | C | Remains in (70,90] |
| (42,Oct26) | 65 | C | D | Inside newly assigned (50,70] |

Only one of these three logical partitions moves. Its token did not change. This movement is placement, not splitting the contents of a logical partition. Data transfer and routing-metadata updates must still happen safely; drawing D on the ring does not copy its rows.

## What virtual nodes add

A **virtual node**, often shortened to vnode, is an ownership position/range assigned to a physical node. Giving each physical node several positions can spread ownership and let a new server take smaller pieces from multiple existing owners. It is not another process and does not create a new copy of each partition.

The [Cassandra architecture documentation](https://cassandra.apache.org/doc/latest/cassandra/architecture/dynamo.html) describes token rings, ranges, and multiple tokens per physical node. Actual replica placement also observes configured topology; this single-owner ring deliberately isolates placement before lesson 10 adds copies.

## Why this does not cure the hot key

Every request for `(42,Oct25)` still computes 35. It still routes to one logical partition's replica set, regardless of how often that request repeats. More balanced range ownership may improve many-key distribution, but it cannot divide that unchanged key across independent partitions. Even if 35 were moved, its load would move with it.

A circular set of mailboxes illustrates stable addresses with changing ownership signs. The analogy stops where actual data must be streamed, copied, repaired, and queried during a membership transition.

Use consistent hashing when incremental ownership changes and many distributed keys justify a ring. Avoid implementing a custom ring for a small relational app just to cure a slow query; an index or cache may address the actual issue. A correct ring still needs membership coordination, migration, topology-aware copies, and hot-key handling.

## Project mapping

[PostRepository](../../../../../../src/main/java/com/instagram/backend/repository/PostRepository.java) does not implement token ownership. A future Cassandra store would delegate it to Cassandra and its driver, not reimplement this 0–99 ring inside `PostService`. The exact production token type and replica policy must be checked against the selected configuration.

## Failure drill — paper exercise

- **Predict:** D is advertised as owner of 65 before its data is available. Could a read appear to lose user 42's October 26 event?
- **Symptom:** routing reaches D but event 1004 is absent; C may still have it.
- **Contain:** use the database's supported membership/streaming transition, not a manual instantaneous map swap.
- **Recover:** complete transfer and reconcile state; verify 1004 and the two unaffected keys before treating movement as complete.
- **Prevent:** test reads/writes during ownership change and monitor transfer progress. Ring balance is neither data migration nor a hot-key guarantee.

## Recap and stop/go

Place tokens 35,65,80 before and after adding D. Explain why only `(50,70]` changes owner and why the unchanged hot key remains concentrated.

## Questions and explained answers

<details>
<summary>1. Does adding D change H(42,Oct26)=65?</summary>

No. The hash space is stable; ownership changes. Token 65 now falls in D's interval instead of C's.

</details>

<details>
<summary>2. Why is consistent hashing not a complete hotspot fix?</summary>

One exact key still maps to one token and one replica set. Rebalancing many ranges can improve overall placement, but repeated work on that key remains together unless the data/query design changes.

</details>

<details>
<summary>3. Is a vnode a new storage server or a replica?</summary>

Neither. It is an ownership position/range assigned to a physical node. Multiple positions can help distribute ranges; replication is a separate decision about copies.

</details>
