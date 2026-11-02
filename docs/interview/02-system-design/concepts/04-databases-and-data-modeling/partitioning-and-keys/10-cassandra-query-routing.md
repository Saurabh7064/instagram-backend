# Lesson 10 — Cassandra query routing: complete key to token to replica set

[Module](./README.md) · [Previous: partition creation](./09-cassandra-partition-creation.md) · [Next: Instagram design](./11-instagram-key-design.md)

- New core terms: partitioner, coordinator, consistency level.

Core study: 10–15 minutes. Prerequisites: complete partition key, clustering tuple, token ring, and replication as copies.

## A receives the request, but B and C hold the rows — Must understand

We ask for user 42's October 25 activity, with `LIMIT 2`. Suppose the client sends the request to A, while B and C hold copies of that user/day. Can A still serve the request? Yes: receiving a request and owning its data are different roles.

The node handling the operation is its **coordinator**. It locates the relevant copies and obtains the responses the operation requires. It need not store the selected partition itself. The complete `(user_id,activity_date)` tuple chooses the data group; the event time and ID then choose and order rows *inside* that group.

A **partitioner** turns the encoded complete key into a token. Cassandra commonly uses `Murmur3Partitioner`; our small teaching tokens are invented so we can follow placement without hiding it in real hash arithmetic. The token identifies an owned range, and topology-aware replication identifies the set of copies.

The **consistency level** says how many or which of those relevant replicas must respond for completion. It does not turn every live node into a copy and does not choose the partition key. The common shortcut “partition key → hash → token range → node” leaves out the replica set and the coordinator. We need the topology before we can count the required replies, so set that up next.

## Place the copies before counting their responses

Use the same 0–99 teaching ring from lesson 5 **before** adding D: A=20, B=50, C=90. There is one datacenter, each node is in a different rack, one token per node for this illustration. Replication factor (**RF**, number of copies) is 2 with topology-aware placement. In this simplified layout, replicas are the primary clockwise owner and the next distinct node in another rack.

The same user/day teaching hashes are 35,80,65. Therefore:

| Partition | Token | Primary range owner | Replica set |
|---|---:|---|---|
| (42,Oct25) | 35 | B | B,C |
| (84,Oct25) | 80 | C | C,A |
| (42,Oct26) | 65 | C | C,A |

The primary range owner is not a permanent write leader: Cassandra permits any appropriate replica/coordinator path under its protocol. Actual deployments can have many tokens, multiple datacenters, and rack constraints; do not treat this simplified “next node” rule as the complete placement algorithm. The [Cassandra token/replication architecture](https://cassandra.apache.org/doc/latest/cassandra/architecture/dynamo.html) explains the full setting.

## Follow A to the two replicas for token 35

```cql
SELECT activity_id, occurred_at, activity_type
FROM activity_lessons.activity_by_user_day
WHERE user_id = 42 AND activity_date = '2026-10-25'
LIMIT 2;
```

Assume the fixed writes have reached both relevant replicas and no concurrent updates or deletions occur. Use `LOCAL_QUORUM`, which requires a majority of the replicas in the local datacenter: floor(RF/2)+1=floor(2/2)+1=2. Floor means rounding down. Here both B and C must participate sufficiently; “any two of A/B/C” would be wrong because A is not a replica for this key.

1. The client provides **both** partition-key values. A Cassandra driver, the client library managing connections/routing, can encode them and use current token metadata to prefer a replica as coordinator.
2. To make the coordinator distinction visible, suppose A receives this request. A is allowed to coordinate even though it does not store this partition.
3. The complete tuple hashes to teaching token 35. Ownership metadata identifies B/C. Hashing user_id alone cannot reproduce the composite key's token.
4. A contacts B/C according to the read protocol and consistency level. Responses may involve data and comparison metadata; this does not require both nodes to send two complete copies of the result to the client.
5. Within `(42,Oct25)`, the clustering order selects 1002 then 1003. Neither occurred_at nor activity_id changes the partition hash; they narrow/sequence rows inside the selected group.
6. After the required responses and reconciliation, A returns the result. A being coordinator did not turn it into the range owner.

The quorum count is a participation condition, not an independent proof of linearizability under arbitrary concurrent writes. See [quorum reasoning](../../01-distributed-systems-fundamentals/07-quorums.md) for the distinction.

## Which predicates identify a group, and which only filter rows?

| Query shape | Routing consequence |
|---|---|
| user_id=42 AND activity_date=Oct25 | One complete logical partition key |
| Same key plus occurred_at lower bound | Same replicas, narrower clustering slice |
| Only user_id=42 | Missing date; not a direct single-partition lookup |
| Only activity_type='VIEW_POST' | No complete key; another access path/table is needed |

Some indexes, token scans, or explicitly filtered queries allow other shapes in supported versions. They do not turn an incomplete key into one known partition or guarantee inexpensive work. `ALLOW FILTERING` is permission to perform otherwise rejected filtering, not a universal performance repair. [Cassandra SELECT restrictions](https://cassandra.apache.org/doc/latest/cassandra/developing/cql/dml.html) describe the supported cases.

## Coordination is separate from ownership

A receptionist can direct a request to the two archives holding the right folder without personally storing it. The analogy stops where replica reconciliation and consistency-level failure rules need a protocol.

Use full-key bounded queries for predictable routing. A new access pattern may deserve another query-specific table, with extra write/storage consistency work. [PostService.feed](../../../../../../src/main/java/com/instagram/backend/service/PostService.java) currently reads one relational repository; it does not implement this coordinator protocol. The RF=2 three-node scenario is a paper model, separate from the single-node CQL script.

## Lose B while coordinator A stays healthy — paper exercise

For token 35, replicas are B/C and LOCAL_QUORUM requires both. B fails; C and coordinator A remain alive. Predict whether the read can finish at the requested level.

Only C is an available relevant replica. A's health does not add another copy, so the required two responses cannot be obtained. Do not silently lower the level and claim the operation retained the same guarantee.

Restore B, complete required catch-up or repair, and verify the result under the chosen level. Test coordinator and replica failures separately, keep driver metadata current, and select replication for the actual failure requirement. Counting all healthy cluster nodes would miss the placement that determines this operation's availability.

## Before moving on

Artifact: `READY`; comprehension: `TODO`.

Route token 35, distinguish A's role from B/C, derive the required count, and identify which query fields affect placement versus clustering.

## Questions and explained answers

<details>
<summary>1. Why does A not satisfy the missing second replica response?</summary>

For token 35 the replica set is B/C. A only coordinates this operation; it cannot substitute nonexistent local data merely because it is reachable.

</details>

<details>
<summary>2. Does changing LIMIT or adding a time bound choose another node?</summary>

No. The complete user/date partition key determines the token and replica set. LIMIT and clustering restrictions change how much is returned within that same partition.

</details>

<details>
<summary>3. Why is the original “token range → node” statement incomplete?</summary>

One node can be the primary owner of a range, but replication stores copies on a configured set. The client may also contact a different coordinator. Ownership, copies, and request entry are distinct roles.

</details>
