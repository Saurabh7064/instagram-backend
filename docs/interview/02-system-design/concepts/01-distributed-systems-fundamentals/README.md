# 1. Distributed Systems Fundamentals

[All concept categories](../index.md) · [Learner progress](../../learner-progress.md)

A request can succeed in one process while its caller sees a failure. This module starts with that uncertainty, then explains how services choose observations, agree on ownership, recover work, and order events.

Artifact: `READY` — all 25 original roadmap topics are covered in 16 focused micro-lessons. Comprehension: `TODO`; no lab execution or mastery is implied. Each part is intended for 10–15 minutes. Study one part at a time and answer its checkpoints before advancing.

## Start here

Prerequisite: know that a client sends a request and a backend returns a response. A database stores records across requests. Processes are independently running programs. Everything else is introduced in the sequence below. Use the existing Spring Boot project as a concrete boundary, not as evidence that a distributed protocol is already implemented.

## Lesson sequence and coverage

Topic numbers retain the original 1–25 roadmap; lesson numbers reflect prerequisite order.

| Lesson | Primary idea | Original topics | Artifact | Comprehension |
|---:|---|---|---|---|
| 1 | [Distributed systems and partial failure](./01-partial-failure.md) | 1 | `READY` | `TODO` |
| 2 | [CAP: what to do during a partition](./02-cap.md) | 2 | `READY` | `TODO` |
| 3 | [PACELC: the healthy-network trade-off](./03-pacelc.md) | 3 | `READY` | `TODO` |
| 4 | [Consistency models: strong and eventual](./04-consistency-models.md) | 4–6 | `READY` | `TODO` |
| 5 | [Session guarantees: causality and your own writes](./05-session-and-causal-consistency.md) | 7–8 | `READY` | `TODO` |
| 6 | [Linearizability versus serializability](./06-linearizability-and-serializability.md) | 9–10 | `READY` | `TODO` |
| 7 | [Quorum reads and writes](./07-quorums.md) | 16 | `READY` | `TODO` |
| 8 | [Consensus and leader election](./08-consensus-and-election.md) | 11, 14 | `READY` | `TODO` |
| 9 | [Raft: ordering a replicated log](./09-raft.md) | 12 | `READY` | `TODO` |
| 10 | [Paxos: preserve an already chosen value](./10-paxos.md) | 13 | `READY` | `TODO` |
| 11 | [Split brain and fenced distributed locks](./11-split-brain-and-locks.md) | 15, 17 | `READY` | `TODO` |
| 12 | [Distributed transactions and two-phase commit](./12-distributed-transactions-and-2pc.md) | 18–19 | `READY` | `TODO` |
| 13 | [Sagas: recover a multi-step business operation](./13-sagas.md) | 20 | `READY` | `TODO` |
| 14 | [Idempotency keys and delivery guarantees](./14-idempotency-and-delivery.md) | 21–22 | `READY` | `TODO` |
| 15 | [Logical clocks and Lamport timestamps](./15-lamport-clocks.md) | 23, 25 | `READY` | `TODO` |
| 16 | [Vector clocks: detect concurrent versions](./16-vector-clocks.md) | 24 | `READY` | `TODO` |

## How the ideas connect

First decide what an uncertain result means. Then state what readers may observe. Only then choose coordination, transaction recovery, and event-order tracking. A local transaction, a replicated read guarantee, and retry safety answer different questions; none automatically supplies the others.

## Integrated paper exercise

Proposed scenario: a user creates post 42 with caption v1, updates it to v2, then refreshes from a different replica. The update committed but its response was lost. The media file and metadata are stored separately; a later notification is another external effect.

1. Predict what the client knows after the lost response. It cannot distinguish committed from uncommitted work from the timeout alone.
2. Specify immediate author visibility separately from what other readers can see. Read-your-writes may satisfy the author without making every replica current.
3. Break communication between three proposed replicas into groups of one and two. Under a majority protocol, the isolated node cannot commit new decisions; stale reads are a separate policy choice.
4. Retry the update with the original operation identity. Explain atomic result recording and why a key reused with a different payload is rejected.
5. Fail notification delivery after metadata commit. Keep the post if the business contract permits delayed notification; retry the notification with its own deduplication scope.
6. Resume the old media-processing owner after a new owner has written. Require the storage boundary to reject its stale fencing token.
7. Restore communication. Verify convergence, resolve competing edits deliberately, drain pending work, and reconcile orphan media. Reconnection alone does not perform these repairs.

Observable success: one intended business effect, explicit read guarantees, no stale-owner overwrite, and a durable recovery record for incomplete external work. These are proposed acceptance criteria, not verified production capabilities.

## Recap and vocabulary cheat sheet

| Need | Tool or guarantee | Boundary to remember |
|---|---|---|
| Understand a missing reply | Partial-failure model | Timeout does not prove rollback |
| Choose behavior during lost communication | CAP | Error responses sacrifice formal availability |
| Account for healthy-network delay | PACELC | Communication costs exist without a partition |
| Specify reader observations | Consistency model | Convergence alone supplies no time bound |
| Let authors see their changes | Read-your-writes | Other clients may still lag |
| Preserve dependent observations | Causal consistency | Independent events need not share an order |
| Preserve real-time operation order | Linearizability | Different from transaction isolation |
| Protect multi-row transaction invariants | Serializability | May require whole-transaction retries |
| Obtain overlapping replica participation | Quorum | Arithmetic alone is not a correctness proof |
| Agree on durable decisions | Consensus; Raft; Paxos | Majority loss can stop progress |
| Reject a resumed obsolete owner | Fencing | The protected resource must enforce the token |
| Coordinate an atomic cross-participant outcome | 2PC | Prepared work can block during recovery |
| Recover separately committed business steps | Saga | Compensation can fail or be imperfect |
| Make retries safe | Idempotency key | Atomicity, scope, payload, and retention matter |
| Distinguish deliveries from effects | Delivery guarantees | Exactly-once claims need an explicit boundary |
| Track dependency order | Lamport timestamps | Increasing numbers do not prove causation |
| Detect concurrent histories | Vector clocks | Conflict detection does not merge content |

## Concrete implementation follow-up

Proposed task linked to existing F-010 posts and future F-030 background processing: persist a user/operation-scoped idempotency key, payload fingerprint, post ID, and stable response atomically with post creation. Add concurrent duplicate-request, changed-payload, and commit-before-response-loss integration tests. Define expiry and external-effect boundaries before adding an outbox. This module does not implement that change or mark its feature complete.

Current code: [PostService](../../../../../src/main/java/com/instagram/backend/service/PostService.java), [MediaStorageService](../../../../../src/main/java/com/instagram/backend/service/MediaStorageService.java). Related interview practice: [retry after a lost create-post response](../../questions/short-questions-and-answers/22-idempotency-keys-and-ambiguous-timeouts.md).

## Module teach-back

Explain one lost-response request from start to recovery. State the read promise, the partition decision, who may write, how retries avoid duplicate effects, and what happens when compensation fails. Then compare Lamport and vector clocks using the worked values in the final two lessons.

Stop/go: artifact completeness is separate from understanding. Record one correct explanation, prediction, and diagnosed failure per lesson in learner progress before moving to the next. Recheck without notes after one day and one week.
