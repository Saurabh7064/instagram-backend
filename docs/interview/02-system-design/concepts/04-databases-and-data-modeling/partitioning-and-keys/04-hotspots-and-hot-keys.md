# Lesson 4 — A hot key is a workload problem, not just uneven storage

[Module](./README.md) · [Previous: hashing](./03-hash-partitioning.md) · [Next: consistent hashing](./05-consistent-hashing.md)

- Artifact: `READY`; comprehension: `TODO`.
- Core study: 10–15 minutes. Primary idea: measure what is concentrated before choosing a remedy.
- Prerequisite: equal partition keys route together; many logical groups can share a server.
- New core terms: hotspot, hot key, write shard.

## Why a balanced cluster can still be slow — Must understand

Suppose every server stores roughly the same number of bytes, but one user's events dominate incoming requests. Storage balance does not imply work balance. We must distinguish the key, partition, and server before saying “add more nodes.” A **cluster** is the cooperating group of database servers.

A **hotspot** is a location receiving more work than its capacity can handle. A **hot key** is one key receiving disproportionate requests. A partition can also be **oversized**: too many stored rows/bytes even when current traffic is modest. A partition can be hot, oversized, both, or neither.

A server can be hot because it owns many moderately busy partitions rather than one extreme key. That case may improve by moving some ownership. One extreme unchanged key cannot be split merely by moving unrelated ownership ranges.

## Calculate the concentration

Use the fixed user-activity model. User 42's key `(42,2026-10-25)` contains events like 1001,1002,1003. For a workload exercise, suppose this same user/day produces 9,000 new events per second during a sustained burst, while the responsible replica set can handle 3,000 per second of this operation mix. These are invented capacities, not Cassandra benchmarks.

Demand exceeds capacity by 9,000−3,000=6,000 events each second. In ten seconds of buffering without throttling or additional effective capacity, pending work grows by 6,000×10=60,000. Adding nodes that own only other keys does not change that arithmetic.

A one-day time bucket bounds history, but all 9,000 current writes still share today's key. Yesterday being cold does not help today's overloaded group. Shortening to an hour bounds size more tightly but does not automatically split writes within the same active hour.

## Introduce several write destinations deliberately

A **write shard** adds a deterministic sub-group to the natural partition key. Suppose we choose four sub-groups:

`(user_id, activity_date, shard_number)`

For this example, compute `shard_number=activity_id % 4`. Activity 1001 goes to 1 because 1001=4×250+1; 1002 goes to 2; 1003 goes to 3; 1004 goes to 0. These examples explain the rule, not a claim that consecutive IDs are always an ideal production hash.

If the 9,000 writes distribute evenly across four groups, each gets 9,000/4=2,250 per second, below the assumed 3,000 per-group capacity. **That conclusion is conditional:** the groups must land on adequate independent capacity, the database must not share another limiting resource, and actual event IDs must distribute well. Four logical groups can still collide on the same overloaded servers.

The reader now needs all four groups to reconstruct a user's day. **Fan-out** means issuing work to multiple destinations. It must merge their ordered results, handle partial failures, and retain a stable pagination position. The distribution benefit is purchased with more read work and operational complexity.

## Match remedy to cause

| Observed problem | Candidate remedy | What it does not solve |
|---|---|---|
| Repeated identical reads | Safe caching/coalescing | Concentrated writes |
| Very large history for one key | Narrower time bucket and retention | A burst entirely within the current bucket |
| One key's current writes exceed capacity | Deterministic write sub-groups | Free single-partition reads |
| Many independent keys on one server | Ownership rebalance/add capacity | Splitting one unchanged key |
| Excess demand during an outage | Rate limits and bounded buffering | Unlimited throughput or storage |

A viral product at one checkout desk is an analogy. Adding empty desks elsewhere does not help unless customers can actually be routed to them. The analogy stops where ordered events, deduplication, and partition ownership constrain the split.

## Choice and project mapping

Choose write sharding after identifying a worst-case key and acceptable read cost. Avoid adding random salt to every request: a read cannot reconstruct a random previous routing choice unless the shard is recorded or all groups are queried. Preserve event identity across retries.

[Post.likeCount](../../../../../../src/main/java/com/instagram/backend/domain/Post.java) and [PostService.like](../../../../../../src/main/java/com/instagram/backend/service/PostService.java) illustrate a current shared post record that could receive concentrated traffic. This module does not implement sharded counters or fix concurrency in that method. A future activity store is a separate design requiring its own load tests.

## Failure drill — paper exercise

- **Predict:** change only the ring layout while the 9,000 writes retain one exact key. Has the fundamental key concentration disappeared?
- **Symptom:** the hot replica set changes location but remains overwhelmed; other servers are quiet.
- **Contain:** throttle or bound the queue; avoid an unbounded retry storm.
- **Recover:** provision a measured new key layout if justified, version the transition, migrate/catch up, and verify complete reads across old/new groups.
- **Prevent:** monitor both per-key rates and stored partition sizes, including worst-case users. Test the added read fan-out and partial failures.

## Recap and stop/go

Derive 6,000 pending events/second and the conditional 2,250/shard result. Explain why time bucketing, consistent hashing, and write sharding address different dimensions.

## Questions and explained answers

<details>
<summary>1. Can a tiny partition still be hot?</summary>

Yes. Stored size and request rate are separate. A small row repeatedly read or updated can overload its serving resources without holding much history.

</details>

<details>
<summary>2. Why does an hourly bucket not necessarily cure the active burst?</summary>

Every current event can still have the same user and hour. Historical size is reduced, but current demand remains concentrated in that one key.

</details>

<details>
<summary>3. What must be true before four write shards yield 2,250 writes/second each?</summary>

Demand must spread evenly and the physical placement/resources must provide independent usable capacity. The division is an assumed workload model, not a guarantee from adding a key column.

</details>
