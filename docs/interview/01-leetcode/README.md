# LeetCode Coding Practice

Full curriculum: [Coding Micro-Lesson Index](./lesson-index.md)

Syntax rusty? Use the separate [Java Array Cheat Sheet](./references/array-cheatsheet.md), [Java HashSet Cheat Sheet](./references/hashset-cheatsheet.md), and [Java HashMap Cheat Sheet](./references/hashmap-cheatsheet.md). Each has its own runnable examples, and none changes your active lesson.

## Goal

Recognize the pattern, explain a baseline approach, implement the improved solution, test it, and state time/space complexity within 30–35 minutes.

## Pattern order

1. Arrays, strings, hash maps, sets
2. Two pointers and sliding window
3. Prefix sums and intervals
4. Stack, queue, and linked list
5. Binary search
6. Trees, BST, DFS, and BFS
7. Heap and priority queue
8. Graph traversal and topological sort
9. Union-find and shortest path
10. Backtracking
11. One-dimensional dynamic programming
12. Two-dimensional dynamic programming

The pattern list above is the high-level order. The lesson index expands it into 72 focused, linked problems while keeping only one lesson active at a time.

## Lessons

1. [Arrays and Hash-Based Lookup](./lessons/01-arrays-hash-maps/README.md) — module
   - [Micro-Lesson 01: HashSet Membership](./lessons/01-arrays-hash-maps/01-hashset-membership/README.md) — `DEFERRED`
   - [Micro-Lesson 02: Sequence Boundaries](./lessons/01-arrays-hash-maps/02-sequence-boundaries/README.md) — `PRACTICING`
   - [Micro-Lesson 03: HashMap Value-to-Index Lookup](./lessons/01-arrays-hash-maps/03-value-to-index/README.md) — `LEARNING`
   - [Micro-Lesson 04: Frequency Counting](./lessons/01-arrays-hash-maps/04-frequency-counting/README.md) — `TODO`
   - [Micro-Lesson 05: Grouping by a Canonical Key](./lessons/01-arrays-hash-maps/05-canonical-grouping/README.md) — `TODO`
2. [Two Pointers](./lessons/02-two-pointers/README.md) — initialized module
   - [Micro-Lesson 01: Valid Palindrome](./lessons/02-two-pointers/01-valid-palindrome/README.md) — `TODO`
   - [Micro-Lesson 02: Two Sum II](./lessons/02-two-pointers/02-two-sum-ii/README.md) — `TODO`
   - [Micro-Lesson 03: Container With Most Water](./lessons/02-two-pointers/03-container-most-water/README.md) — `TODO`
   - [Micro-Lesson 04: 3Sum](./lessons/02-two-pointers/04-three-sum/README.md) — `TODO`
3. [Sliding Window](./lessons/03-sliding-window/README.md) — partially initialized module
   - [Micro-Lesson 01: Maximum Average Subarray I](./lessons/03-sliding-window/01-maximum-average-subarray/README.md) — `TODO`

Current session: [Session 03 — HashMap Value-to-Index Lookup](../daily/2026-10-07-session-03.md)

## Mastery rule

A problem counts as mastered only when you can solve a fresh variant without hints, explain trade-offs aloud, write runnable code, and revisit it successfully after at least one week.

## Problem log

| Date | Problem/link | Pattern | Difficulty | Time | Hint? | Main mistake | Review dates | Mastered? |
|---|---|---|---|---:|---|---|---|---|
| 2026-10-04 / 2026-10-06 | Contains duplicate | HashSet membership | Easy |  |  | Correct solution; minor clarity/duplicate-lookup refinements | Checkpoints, 2026-10-07, 2026-10-13, 2026-11-06 | NO — code passes, explanation pending |
| 2026-10-04 / 2026-10-07 | Longest consecutive run | Sequence boundary | Medium |  | Guided checkpoint retries | Correct solution; initially confused one scan start and exact overflow wording | 2026-10-08, 2026-10-14, 2026-11-07 | NO — initial lesson passed; retrieval pending |
| 2026-10-07 / 2026-10-09 | Two Sum | Value-to-index map | Easy |  |  | Nested-loop baseline replaced by passing average `O(n)` HashMap solution |  | NO — checkpoints pending |

## Per-problem note template

- Problem in one sentence:
- Clarifying questions:
- Brute-force idea and complexity:
- Pattern signal:
- Optimal approach and invariant:
- Edge cases:
- Final complexity:
- Mistake or insight:
- Follow-up variation:
