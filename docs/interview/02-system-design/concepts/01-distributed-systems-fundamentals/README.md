# 1. Distributed Systems Fundamentals

[All concept categories](../index.md) · [Learner progress](../../learner-progress.md) · [Teaching review and verification](./verification.md)

Why can Publish time out after a post was saved? What should a disconnected reader see? Who may keep writing after a takeover? This module builds the models needed to answer those questions, then explains the protocols and their limits.

Artifact: `READY` after the teaching review recorded in verification. Comprehension: `TODO` for every lesson. The 16 lessons cover the 25 original topics; their presence does not demonstrate understanding or an executed distributed lab.

## How to study — Must understand

Start at lesson 1. A client requests work, a backend processes it, and a database stores records across requests. The lessons define processes, communication boundaries, copies, and failures before relying on them. No prior consensus, replication, transaction-isolation, or logical-clock knowledge is assumed.

Each lesson has a 10–15 minute core reading path followed by checkpoints. Allow additional time for answering and teach-back. Useful-later material is labeled. If a prerequisite is unclear, return to its linked lesson rather than memorizing the next protocol. In chat we study only one lesson and wait for your explanation/prediction before advancing; all later comprehension entries stay TODO.

The examples deliberately use fixed actors and initial values within each lesson. They derive what a reader or writer observes and explain the assumptions that make the result valid. A primary-source link is for checking or going deeper; the core explanation must be understandable without opening it.

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

## What is actually covered?

| Original topic | Lesson | What you should be able to explain |
|---|---:|---|
| 1. Distributed systems | 1 | Independent execution, messages, and partial failure |
| 2. CAP theorem | 2 | Consistency, Availability, Partition tolerance; the missing-information argument |
| 3. PACELC | 3 | Partition/Availability/Consistency; Else/Latency/Consistency; modeled waiting cost |
| 4. Consistency models | 4 | A contract over observable operation histories |
| 5. Strong consistency | 4, 6 | The precise real-time promise used in this module |
| 6. Eventual consistency | 4 | Convergence assumptions, repair, and no implicit time bound |
| 7. Causal consistency | 5 | A dependent reply cannot precede its prerequisite history |
| 8. Read-your-writes | 5 | The writer's session guarantee, including failover |
| 9. Linearizability | 6 | An operation's effective point and real-time order |
| 10. Serializability | 6 | Both possible transaction orders and a forbidden combined outcome |
| 11. Consensus algorithms | 8 | Agreement, safety, progress, and preservation of chosen history |
| 12. Raft | 9 | Append, commit, apply; term and leadership boundaries |
| 13. Paxos | 10 | Roles, promises, prepare/accept phases, and value preservation |
| 14. Leader election | 8, 9 | Selecting authority without discarding accepted history |
| 15. Split brain | 11 | Multiple active authorities and stale-owner writes |
| 16. Quorum reads/writes | 7 | Derive R+W>N and W>N/2, then state their limits |
| 17. Distributed locks | 11 | Lease expiry, fencing enforcement, and same-owner duplicates |
| 18. Distributed transactions | 12 | Atomic outcome across cooperating participants |
| 19. Two-phase commit (2PC) | 12 | Prepare, durable decision, notification, and blocking recovery |
| 20. Saga pattern | 13 | Local commits, business compensation, durable progress |
| 21. Idempotency keys | 14 | Scoped identity, fingerprint, uniqueness, atomic result recording |
| 22. Exactly/at-least/at-most once | 14 | Separate attempts, deliveries, and committed effects |
| 23. Logical clocks | 15 | Ordering metadata rather than elapsed physical time |
| 24. Vector clocks | 16 | Componentwise comparison, concurrency, and explicit content resolution |
| 25. Lamport timestamps | 15 | Derive receive=max(local,received)+1 and its one-way implication |

## How the ideas build on one another

First understand uncertainty: a timeout does not tell you what committed. Next choose allowed observations: an author seeing their write and every reader seeing a current value are different requirements. Then consider coordination: overlapping participants provide a path for shared knowledge, but complete protocols must preserve decisions across failures. Finally choose recovery boundaries and ordering metadata; neither a clock nor a retry creates atomicity by itself.

## Integrated exercise — paper design, after the lessons

Fixed proposed system: client Lea, backend service, replicas A/B/C, media storage, and a notification receiver. Post 42 initially has caption “Sunset.” There is no implemented replica cluster or notification workflow claimed here.

1. Lea updates the caption to “Evening.” The authoritative database commits, but its reply is lost. Explain separately what the database did and what Lea knows.
2. Lea refreshes through old replica C. State whether your contract requires read-your-writes, global current reads, or allows staleness; choose the response under that contract.
3. Communication splits into {A,B} and {C}. For a fixed three-voter majority protocol, derive why A/B may continue and C cannot commit a new majority decision alone. Explain which read promises remain safe.
4. Lea retries using the original operation key. Design the unique, user/operation-scoped record and atomic effect/result boundary; reject changed-payload reuse.
5. Media and post metadata exist, but notification delivery fails. Under a delayed-notification contract, retain the published post and persist retryable notification intent. Explain why an outbox still needs receiver-side duplicate protection.
6. A media worker pauses, loses its lease, and resumes after a newer owner writes. Derive why storage must reject the older ownership token.
7. Reconnect participants. Inspect accepted history, repair copies, resolve conflicting edits under a domain policy, and verify pending work completes. Do not equate a working network with repaired data.

For each step give the predicted visible result, evidence needed to diagnose failure, containment, recovery verification, and remaining limitation. A successful paper explanation is different from running these failure injections.

## Vocabulary recap

| Question | Relevant concept | Limit you must remember |
|---|---|---|
| Did the remote operation happen? | Partial failure | Timeout cannot prove rollback |
| What may disconnected readers observe? | CAP; consistency model | State the operation and failure assumptions |
| What costs time when healthy? | PACELC | Required remote information takes communication |
| Does this client need its own update? | Read-your-writes | Other clients can still lag |
| Does one effect depend on another? | Causal consistency | Independent events need not share an order |
| Are multi-row decisions valid together? | Serializability | Retry the whole aborted transaction |
| Do enough responders overlap? | Quorum | Intersection is not a complete protocol |
| What decision survives leadership change? | Consensus; Raft; Paxos | New authority cannot discard chosen history |
| Can the old owner still mutate storage? | Fencing | Enforcement belongs at the protected resource |
| Must independent resources share one final outcome? | 2PC | Prepared work can block |
| Can the business recover already committed steps? | Saga | Compensation can fail or be imperfect |
| Can repeats produce one business effect? | Idempotency | Atomicity and guarantee scope are essential |
| Did an event depend on another? | Lamport/vector clocks | Scalar order does not prove causation; vectors do not merge text |

## Concrete implementation follow-up

Proposed F-010 hardening: add atomic request-key/result recording for post creation; test concurrent duplicates, payload mismatch, rollback, and lost response after commit. Proposed F-030 processing: enforce ordered ownership at the output resource and test a paused stale worker. Neither task is completed by these learning artifacts.

Real code boundaries: [PostService](../../../../../src/main/java/com/instagram/backend/service/PostService.java), [PostRepository](../../../../../src/main/java/com/instagram/backend/repository/PostRepository.java), and [MediaStorageService](../../../../../src/main/java/com/instagram/backend/service/MediaStorageService.java). Related practice: [lost create-post response](../../questions/short-questions-and-answers/22-idempotency-keys-and-ambiguous-timeouts.md).

## Stop/go

Start with lesson 1 and answer its checkpoints before advancing. Record an explanation, correct prediction, failure diagnosis, and comparison without notes in learner progress. Reading all files or passing documentation checks does not upgrade comprehension. Recheck after one day and one week; do not prefill recall evidence.
