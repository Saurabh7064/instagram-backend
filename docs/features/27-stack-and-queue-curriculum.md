# 27 — Stack and Queue LeetCode Lesson Artifacts

## 1) What I built

Prepared the next five problems in [Module 05](../interview/01-leetcode/lessons/05-stack-queue/README.md): Valid Parentheses, Min Stack, Daily Temperatures, Evaluate Reverse Polish Notation, and Sliding Window Maximum. Each lesson derives a fixed example before implementation state, explains each code block, narrates actual operations with complete collection state, and provides a complete reference, separate IntelliJ-runnable practice, three explained checkpoints and stop/go criteria.

All five learning entries remain TODO. Two Sum remains the single active lesson with its existing pending checkpoints. Preparing future artifacts does not imply comprehension, code completion or mastery.

## 2) Concepts learned

Covered concepts, without claiming learner understanding: newest-first nesting and operands; saved minimum history; ordered unfinished temperature waits; expired and dominated window candidates; index/value/result distinctions; amortized versus worst-case operation costs.

## 3) Why this design

Module 05 is the next unprepared section of the existing index. Each problem stays in its own micro-lesson and uses one fixed worked input throughout conceptual reasoning, code, state tables and checkpoints. Existing simulation adapters do not calculate these algorithms; no precomputed declarative animation substitutes for a derived execution. New lesson preparation leaves the current daily plan intact.

## 4) Implementation notes

- Parentheses saves expected closers; the empty-before-pop condition uses left-to-right short circuit.
- Min Stack stores independent value/minimum pairs, preserving duplicate minima and the full integer range. ArrayDeque push is amortized constant time; the lesson states that qualification.
- Temperatures stores unresolved day indexes; a strictly warmer arrival resolves each older index once.
- Reverse Polish evaluation pops right before left. Java integer division truncates toward zero; signed numeric tokens are distinguished from operator tokens.
- Window maxima uses input indexes for expiration and removes older candidates whose values are no larger. Each index is added and removed at most once.
- Practice methods deliberately remain TODO. Their readiness messages do not indicate passing learner solutions.
- These are test-source learning programs. No production class, application endpoint, renderer or UI repository source changes.

## 5) Code pointers

- [Module and study order](../interview/01-leetcode/lessons/05-stack-queue/README.md)
- [Parentheses practice](../../src/test/java/com/instagram/backend/interview/leetcode/stackqueue/parentheses/ValidParenthesesPractice.java) and [reference](../../src/test/java/com/instagram/backend/interview/leetcode/stackqueue/parentheses/ValidParenthesesSolution.java)
- [Min Stack practice](../../src/test/java/com/instagram/backend/interview/leetcode/stackqueue/minstack/MinStackPractice.java) and [reference](../../src/test/java/com/instagram/backend/interview/leetcode/stackqueue/minstack/MinStackSolution.java)
- [Temperature practice](../../src/test/java/com/instagram/backend/interview/leetcode/stackqueue/temperatures/DailyTemperaturesPractice.java) and [reference](../../src/test/java/com/instagram/backend/interview/leetcode/stackqueue/temperatures/DailyTemperaturesSolution.java)
- [RPN practice](../../src/test/java/com/instagram/backend/interview/leetcode/stackqueue/rpn/EvaluateRpnPractice.java) and [reference](../../src/test/java/com/instagram/backend/interview/leetcode/stackqueue/rpn/EvaluateRpnSolution.java)
- [Window maximum practice](../../src/test/java/com/instagram/backend/interview/leetcode/stackqueue/windowmaximum/SlidingWindowMaximumPractice.java) and [reference](../../src/test/java/com/instagram/backend/interview/leetcode/stackqueue/windowmaximum/SlidingWindowMaximumSolution.java)
- [Independent model verification](../../src/test/java/com/instagram/backend/interview/leetcode/stackqueue/StackQueueVerification.java)
- [Browser integration and proof capture](../../tools/learning/verify-stack-queue-lessons.mjs)

## 6) Commit pointers and evidence

Artifact commit: `51964ce` — `docs: initialize stack and queue LeetCode lessons`.

Exact checks, screenshots, limitations and commit pointers are recorded in the [verification record](../demo-proofs/2026-11-06/stack-queue-verification.md) and [commit map](../commit-map.md). No UI commit is needed for the authored lessons.

## 7) Pitfalls and fixes

The workspace already contains a staged move from `docs/interview` to `docs/learn`, with other unrelated edits. The unchanged sibling UI generator still reads the earlier paths. Browser verification uses a temporary UI copy with catalog roots adjusted to the working tree; its caches and build output stay in `/tmp`. This verifies the actual lesson rendering without claiming the pending repository move or UI catalog repair is complete. New commits preserve that pending migration and exclude unrelated changes.

## 8) Next improvements

- Activate one lesson only after finishing or explicitly deferring the current Two Sum checkpoints.
- Record the learner's own implementation and explanation separately from artifact checks.
- Complete the existing documentation move and matching UI catalog update as their own change.
