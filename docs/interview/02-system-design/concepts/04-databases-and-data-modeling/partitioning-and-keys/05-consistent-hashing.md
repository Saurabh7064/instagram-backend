# Lesson 5 — Consistent hashing reduces remapping when membership changes

[Module](./README.md) · [Previous: hotspots](./04-hotspots-and-hot-keys.md) · [Next: SQL keys](./06-sql-partition-key-and-table-creation.md)

- New core terms: token, token ring, virtual node.

Core study: 10–15 minutes. Prerequisites: deterministic hashing, modulo remapping, and why an unchanged hot key stays concentrated.

## Adding D should not change every user's address — Must understand

Our modulo example changed destinations when we changed the divisor from three to four. A new server can help, but it would be useful to move only the data it will own rather than change every routing calculation's server count.

Keep a fixed numbered space instead. Give each key a position in that space, and give servers responsibility for intervals. When a server joins, transfer a selected interval to it. The key's position stays the same; the owner of that position changes.

A hashed numeric position is a **token**. Treating the numbered space as wrapping from its end back to its beginning creates a **token ring**. **Consistent hashing** uses this arrangement to change selected ownership ranges as membership changes, instead of recomputing every key modulo the new member count.

“Consistent” here concerns placement when servers join or leave. It does not make a promise that reads show the latest write. That is the separate consistency topic from CAP.

We will reuse three user/day keys from the activity dataset on a deliberately tiny 0–99 ring. Drawing the new ownership is only the first step: data still has to move safely before requests can rely on it at the new owner.

## Give D the interval (50,70] and follow each key

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

## Why one physical server may own several ring positions

A **virtual node**, often shortened to vnode, is an ownership position/range assigned to a physical node. Giving each physical node several positions can spread ownership and let a new server take smaller pieces from multiple existing owners. It is not another process and does not create a new copy of each partition.

The [Cassandra architecture documentation](https://cassandra.apache.org/doc/latest/cassandra/architecture/dynamo.html) describes token rings, ranges, and multiple tokens per physical node. Actual replica placement also observes configured topology; this single-owner ring deliberately isolates placement before lesson 10 adds copies.

## Moving token 35 would move its traffic too

Every request for `(42,Oct25)` still computes 35. It still routes to one logical partition's replica set, regardless of how often that request repeats. More balanced range ownership may improve many-key distribution, but it cannot divide that unchanged key across independent partitions. Even if 35 were moved, its load would move with it.

A circular set of mailboxes illustrates stable addresses with changing ownership signs. The analogy stops where actual data must be streamed, copied, repaired, and queried during a membership transition.

Use consistent hashing when incremental ownership changes and many distributed keys justify a ring. Avoid implementing a custom ring for a small relational app just to cure a slow query; an index or cache may address the actual issue. A correct ring still needs membership coordination, migration, topology-aware copies, and hot-key handling.

## Project mapping

[PostRepository](../../../../../../src/main/java/com/instagram/backend/repository/PostRepository.java) does not implement token ownership. A future Cassandra store would delegate it to Cassandra and its driver, not reimplement this 0–99 ring inside `PostService`. The exact production token type and replica policy must be checked against the selected configuration.

## Advertise D before its rows arrive — paper exercise

D becomes owner of token 65 before event 1004 has been transferred. Predict what a read directed to D might show.

The event can appear missing even while C still stores it. The new ownership map and the physical transfer are different stages. Use the database's supported streaming and membership transition rather than swapping a routing map instantly.

Complete transfer, reconcile state, and verify 1004 plus the two unchanged keys before treating the move as finished. Test reads and writes during the transition and monitor transfer progress. Balanced ownership is useful, but it is neither proof of completed migration nor a cure for one repeated hot key.

## Before moving on

Artifact: `READY`; comprehension: `TODO`.

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
