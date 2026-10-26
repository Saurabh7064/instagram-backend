# Distributed systems fundamentals — teaching review and verification

Scope: all 16 lesson files and module/supporting learning documents, rebuilt on 2026-10-25. The previous version was too compressed; structural checks did not establish that it taught the concepts. This review separates teaching judgments from mechanical evidence. Comprehension remains TODO; no replica cluster, consensus implementation, or failure-injection lab was run.

## Teaching rule applied

[AGENTS.md: Concept Lesson Teaching Quality Standard](../../../../../AGENTS.md#concept-lesson-teaching-quality-standard) now requires definitions/components before use, causal derivations, explicit assumptions, and semantic review before READY. The [concept template](../../_concept-template.md) carries these requirements into future authoring. One-primary-idea scope is not a reason to omit necessary explanation.

## Per-lesson author review

This is an author review of the written teaching, not an independent learner assessment. For every row, the explanation, example, misconception, and failure/recovery path were checked against one another. The following are the specific reasoning paths the reader can now reconstruct.

| Lesson | Missing teaching repaired | Worked reasoning checked | Boundary/misconception checked |
|---|---|---|---|
| [1](./01-partial-failure.md) | Process, messages, distributed boundary, partial failure, timeout, commit | Same timeout with three different durable paths; post 42 remains after lost response | Logging/longer timeout is not duplicate protection; save is not proof of client success |
| [2](./02-cap.md) | Full CAP name; individual C/A/P meanings and observable promises | Initialized A/B v1; acknowledged v2; B's three possible responses | Completed versus in-flight write; unavailable error is not CAP availability; partitions cannot be wished away |
| [3](./03-pacelc.md) | Every mnemonic letter, both branches, latency | 4 ms local versus 4+80=84 ms remote; delayed confirmation 204 ms | Confirmation in the toy case is not a complete consistency protocol |
| [4](./04-consistency-models.md) | Observation contract, module's strong definition, convergence | v1/v2 state before acknowledgment, stale read, and repair | Eventual does not imply a deadline or preservation of every intent |
| [5](./05-session-and-causal-consistency.md) | Client-session promise versus dependency promise | Lea creates 42; Noor reads it before reply 9; delayed delivery/failover | Stickiness alone does not prove freshness; independent events need no common order |
| [6](./06-linearizability-and-serializability.md) | Three guarantees and transaction/isolation prerequisites | Enumerated both serial orders on the same two featured posts | Single-record freshness is not a multi-record invariant; retry all reads/decisions |
| [7](./07-quorums.md) | N/R/W meanings and both overlap derivations | All three R=2 pairs against {A,B}; disjoint R=1 example | Fixed membership, versions, concurrency, durability, and repair are additional assumptions |
| [8](./08-consensus-and-election.md) | Agreement, coordinator selection, safety/progress | {A,B}→{B,C} preserves X through B; majority arithmetic | Leadership alone cannot discard chosen history; crash failure model is explicit |
| [9](./09-raft.md) | Name, replicated log, term, append/commit/apply | Term 7/index 5 from one persisted copy to majority to application | Current-term commitment; last-log term/index election comparison; old leader read safety; no complete implementation claimed |
| [10](./10-paxos.md) | Roles, ballots, promise/acceptance distinction, both phases | (7,X) on A/B carried into (8,X) from B/C's reports | Highest accepted report, not newest promise; chosen differs from known; progress can stall safely |
| [11](./11-split-brain-and-locks.md) | Lease, stale owner, ordered tokens, resource enforcement | Highest 40→42, followed by rejected 41 | Rejection boundary begins at resource's accepted authority unless registration is coupled; same-owner retries remain |
| [12](./12-distributed-transactions-and-2pc.md) | 2PC expansion, separate phases, prepared promise | A committed/B prepared after durable COMMIT and lost notification | Atomic outcome is not simultaneous visibility or isolation; B cannot guess abort |
| [13](./13-sagas.md) | Business contract, local commit, compensation, durable orchestration | Same publish flow branches at metadata versus notification failure | Optional notification failure need not unpublish; compensation is fallible and visible |
| [14](./14-idempotency-and-delivery.md) | Intent identity, atomic uniqueness/result boundary, three delivery claims | Concurrent K attempts, both separate-commit crash gaps, same result 42 | Scope/auth/fingerprint/expiry; outbox alone does not deduplicate external effects |
| [15](./15-lamport-clocks.md) | Dependency order before clock rules; max+1 derivation | Full send/receive 1→2→3→4; independent C 1→2 | Increasing scalar does not prove causation or elapsed time; no double receive increment |
| [16](./16-vector-clocks.md) | Components, dominance, concurrency, receive/update convention | [1,0] versus [0,1]; max=[1,1]; combined resolving event=[2,1] | Intermediate merge is not a separate receive event; vector metadata does not merge caption text |

## Source and code review

Primary references were consulted for CAP, PACELC, linearizability, transaction isolation, Raft, Paxos, locks, prepared transactions, sagas, retry safety, broker guarantees, logical clocks, and version reconciliation. Citations appear beside the relevant lesson claims. The CAP reference is Gilbert/Lynch's *Perspectives on the CAP Theorem*, not the original 2002 proof; the label reflects that distinction. Worked timings and application datasets are illustrative assumptions, not benchmarks or source quotations.

Reviewed the actual [PostController](../../../../../src/main/java/com/instagram/backend/controller/PostController.java), [PostService](../../../../../src/main/java/com/instagram/backend/service/PostService.java), [PostRepository](../../../../../src/main/java/com/instagram/backend/repository/PostRepository.java), [MediaStorageService](../../../../../src/main/java/com/instagram/backend/service/MediaStorageService.java), [PostLike](../../../../../src/main/java/com/instagram/backend/domain/PostLike.java), and [RequestCorrelationFilter](../../../../../src/main/java/com/instagram/backend/observability/RequestCorrelationFilter.java). Proposed replicas, protocol voters, fenced workers, workflow records, notification receiver, and operation-key storage are labeled as proposals. No implementation or feature-mastery upgrade is claimed.

## Mechanical and calculation verification

A task-local Python verification script resolves Markdown paths/anchors across the changed module, rule, template, feature note, and progress documents; checks answer blocks, topic coverage, and unchanged comprehension; and evaluates the fixed arithmetic below. These are document-model checks, not production integration tests.

- 16 lessons, 50 nonempty explained checkpoint answers, and all original topic numbers 1–25 are present.
- CAP has the full expansion and separate C/A/P sections before the worked conflict.
- All R/W subsets for N=3 and N=5 confirm exactly when overlap is guaranteed, including the R+W=N counterexample.
- Both featured-post serial orders produce [false,true] or [true,false], never [false,false].
- PACELC timing is 84 ms or 204 ms under the stated delays.
- Lamport counters derive [1,2,3,4]; vector clocks are incomparable before merge, merge to [1,1], and resolve to [2,1] under the stated one-event convention.
- Token 41 is rejected after storage accepts 42; both non-atomic idempotency-record orders expose the documented gap.
- Local links/anchors, balanced markup, answer counts, and Git whitespace are checked separately from teaching judgments.

Recorded check output:

```text
PASS: 16 lessons; 50 explained answer blocks; original topics 1–25; CAP expansion/components; 196 local links/anchors.
PASS: 1010 quorum read/write-set combinations (N=3,5); both serial orders; PACELC 84/204 ms; Lamport [1,2,3,4]; vector merge [1,1] and resolution [2,1]; fencing and crash-state arithmetic.
PASS: READY artifacts retain TODO comprehension; paper drills include containment/recovery; balanced Markdown/details; no empty answers.
```

`git diff --check` passed after the final content edits.

## Scope of completion

Documentation artifacts are READY. The learner still must explain, predict, diagnose, and compare one lesson at a time. No application/API/UI behavior changed, so integration tests, curl verification, and browser screenshots required for behavior changes do not apply to this rebuild. Future implementation exercises require their own integration and failure evidence.
