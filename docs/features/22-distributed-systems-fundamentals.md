# Distributed systems fundamentals learning module

## 1) What I built

Rebuilt all 16 [lessons](../interview/02-system-design/concepts/01-distributed-systems-fundamentals/README.md) covering 25 original topics. Added definitions and acronym/component explanations, prerequisite models, derived examples, mechanism reasoning, guarantee boundaries, alternatives, and complete failure/recovery drills. There are 50 explained checkpoints; comprehension remains TODO.

## 2) Concepts learned

The artifacts teach partial failure, CAP/PACELC, observation contracts, serializable decisions, quorum intersection, consensus, ownership fencing, 2PC, sagas, idempotent effects, and logical/vector ordering. Names such as Raft, Paxos, and saga are explicitly distinguished from acronyms.

## 3) Why this design

The initial version compressed concepts into template slots and was called complete after structural checks. That was insufficient: CAP did not even expand the acronym, and many mechanisms were asserted rather than taught. The rebuild preserves stable lesson links while restoring the explanatory steps. The new [teaching rule](../../AGENTS.md#concept-lesson-teaching-quality-standard) requires a semantic review distinct from syntax/link checks. The [concept template](../interview/02-system-design/_concept-template.md) now asks for component meaning, observable promises, derivations, assumptions, and misconceptions.

## 4) Implementation notes

Documentation only. No backend, API, UI, or Java runtime behavior changed. Integration tests, curl, and browser screenshot gates for changed behavior are not applicable. The distributed failure drills are paper designs, not claimed executed labs. [Verification](../interview/02-system-design/concepts/01-distributed-systems-fundamentals/verification.md) records individual teaching reviews and separate structural/arithmetic checks.

## 5) Code pointers

- [PostController.create](../../src/main/java/com/instagram/backend/controller/PostController.java): current request input, without an idempotency key.
- [PostService](../../src/main/java/com/instagram/backend/service/PostService.java): real local database boundaries; no implemented replica voting or request-key result record.
- [PostRepository](../../src/main/java/com/instagram/backend/repository/PostRepository.java): single repository interface, without replica routing.
- [MediaStorageService.store](../../src/main/java/com/instagram/backend/service/MediaStorageService.java): separate file write, without distributed-transaction or fencing enforcement.
- [PostLike](../../src/main/java/com/instagram/backend/domain/PostLike.java): real user/post uniqueness constraint, narrower than generic operation deduplication.

## 6) Commit pointers

Initial artifact: `9082ac1`; initial map entry: `ca1db45`. Rebuild: `128ed36`; also recorded in [commit map](../commit-map.md). Unrelated existing changes are excluded.

## 7) Pitfalls and fixes

The rebuild derives both serial orders, enumerates quorum sets, separates Raft append/commit/apply, traces Paxos preservation, states fencing's effective boundary, compares both unsafe idempotency commit orders, and calculates every logical/vector value. Teaching evidence is reviewed separately from mechanically counting headings or words.

## 8) Next improvements

Implement F-010 retry-safe creation and F-030 fenced processing as separately tested behavior changes. Assess one lesson at a time; record understanding only after the learner explains, predicts, diagnoses, and compares without notes.
