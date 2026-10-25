# 20 — Multithreading Foundations Before Worker APIs

## 1) What I built

Rebuilt the [threading course entry](../interview/04-java/02-multithreading-from-scratch/README.md) around the missing foundations: program/process, what a thread carries, the existing main thread, CPU time, concurrency/parallelism, task/Runnable, and shared references. Seven prerequisite micro-lessons now precede the revised start/join example. The eight authored parts have 24 explained checkpoints and separate learner-progress tracking.

## 2) Concepts prepared

A thread has an execution position and call history; it is not a method, task, process, or CPU core. Private local reference slots can reach shared objects. Concurrency does not prove simultaneous execution. A direct run call uses the caller; start requests another execution path; successful join establishes a completion/visibility handoff.

These are prepared topics, not demonstrated understanding. Every learner row remains TODO, and the course is inactive.

## 3) Why this design

The previous first exercise used worker, shared result holder, AtomicInteger, and coordination APIs before explaining the execution model. A definition embedded inside that exercise did not satisfy learning from scratch. The new sequence introduces one idea at a time, with no unexplained lambda/atomic prerequisite, algorithm trace, or required coding submission. The prerequisite rule is saved in [AGENTS.md](../../AGENTS.md#functional-programming-lesson-standard).

## 4) Implementation notes

- [Foundation module](../interview/04-java/02-multithreading-from-scratch/lessons/00-foundations/README.md) and [coverage index](../interview/04-java/02-multithreading-from-scratch/foundations-index.json) define the order.
- [Backlog](../interview/04-java/02-multithreading-from-scratch/learning-backlog.md) separates eight READY artifacts from planned advanced topics; [learner progress](../interview/04-java/02-multithreading-from-scratch/learner-progress.md) records future attempts independently.
- Replaced AtomicInteger/lambda in the reference with a named Runnable holding an ordinary result field, read only after successful join. The method still returns the same documented squares and propagates interruption.
- Verification records actual worker identity and uses a controlled gate for unfinished work; no scheduling-speed assumption or arbitrary sleep is used. Gate details are test machinery outside the concept prerequisite path.
- Existing [PostController](../../src/main/java/com/instagram/backend/controller/PostController.java) → [PostService](../../src/main/java/com/instagram/backend/service/PostService.java) calls illustrate that method delegation is not itself thread creation. No application worker or production concurrency policy is added.

## 5) Code pointers

- [ThreadFoundationsExamples.java](../../src/test/java/com/instagram/backend/interview/multithreading/foundations/ThreadFoundationsExamples.java).
- [ThreadFoundationsVerification.java](../../src/test/java/com/instagram/backend/interview/multithreading/foundations/ThreadFoundationsVerification.java).
- [StartAndJoinSolution.java](../../src/test/java/com/instagram/backend/interview/multithreading/startandjoin/StartAndJoinSolution.java); [optional practice](../../src/test/java/com/instagram/backend/interview/multithreading/startandjoin/StartAndJoinPractice.java).
- [Browser integration verifier](../../tools/learning/verify-thread-foundations.mjs).

## 6) Verification and commit pointers

[Verification note](../interview/04-java/02-multithreading-from-scratch/verification.md) records Java checks, HTTP serving, browser content/navigation checks, screenshots, and boundaries. The [commit map](../commit-map.md#multithreading-foundations-before-worker-apis--2026-10-24) records the artifact hash after commit creation.

## 7) Pitfalls and fixes

- Thread creation was conflated with a task object and method calls. The concepts now have separate parts and direct-run identity checks.
- Multiple threads can be mistaken for guaranteed speedup or one core per thread. The scheduling/parallelism lessons explicitly separate progress from execution capacity.
- “Local variables are private” can incorrectly imply private underlying objects. A shared-reference lesson teaches the ownership boundary first.
- An atomic holder hid what join itself guarantees. The restricted one-writer/after-join read now makes that guarantee explicit without implying arbitrary ordinary fields are safe.

## 8) Next improvements

- [ ] Teach 00A or revisit 00B with the learner's own explanation and prediction.
- [ ] Author lifecycle/interruption parts before advancing to races and locks.
- [ ] Assess later retention independently of document or code completion.
