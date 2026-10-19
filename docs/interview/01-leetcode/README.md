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
3. [Sliding Window](./lessons/03-sliding-window/README.md) — initialized module
   - [Micro-Lesson 01: Maximum Average Subarray I](./lessons/03-sliding-window/01-maximum-average-subarray/README.md) — `TODO`
   - [Micro-Lesson 02: Longest Substring Without Repeating Characters](./lessons/03-sliding-window/02-longest-substring-without-repeating/README.md) — `TODO`
   - [Micro-Lesson 03: Longest Repeating Character Replacement](./lessons/03-sliding-window/03-longest-repeating-character-replacement/README.md) — `TODO`
   - [Micro-Lesson 04: Permutation in String](./lessons/03-sliding-window/04-permutation-in-string/README.md) — `TODO`
   - [Micro-Lesson 05: Minimum Window Substring](./lessons/03-sliding-window/05-minimum-window-substring/README.md) — `TODO`

4. [Prefix/Suffix State and Intervals](./lessons/04-prefix-suffix-intervals/README.md) — prepared module
   - [Micro-Lesson 01: Range Sum Query — Immutable](./lessons/04-prefix-suffix-intervals/01-range-sum-query-immutable/README.md) — `TODO`
   - [Micro-Lesson 02: Subarray Sum Equals K](./lessons/04-prefix-suffix-intervals/02-subarray-sum-equals-k/README.md) — `TODO`
   - [Micro-Lesson 03: Product of Array Except Self](./lessons/04-prefix-suffix-intervals/03-product-of-array-except-self/README.md) — `TODO`
   - [Micro-Lesson 04: Merge Intervals](./lessons/04-prefix-suffix-intervals/04-merge-intervals/README.md) — `TODO`
   - [Micro-Lesson 05: Insert Interval](./lessons/04-prefix-suffix-intervals/05-insert-interval/README.md) — `TODO`

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

## Debugger trace format

Every existing problem lesson teaches code in execution order: one small block, why it exists, then its concrete debugger state directly beside it. Initial values, branch decisions, arithmetic, and complete collection mutations appear at the block that produces them. Arrays and maps use adjacent separate tables when needed; the complete reference listing follows the integrated walkthrough. Future lessons must follow the same format; see the [character replacement example](./lessons/03-sliding-window/03-longest-repeating-character-replacement/README.md#block-by-block-code-walkthrough) and the project [coding explanation standard](../../../AGENTS.md#coding-explanation-and-debugger-trace-standard). A trace documents the reference implementation; it does not advance learner status.

The main debugger table stays compact: one row per iteration, with only changed values and entries. Preserve intermediate updates as paths in the same cell, such as `2 → 3 → 2`; `—` means unchanged. Conditions, skipped branches, repeated checks and the next action remain in the concise narration above. Keep full-state reference snapshots separate when needed.

Each worked execution also narrates what the program does before presenting its values. Start with the input, guard and allocations, then explain every iteration's reads, calculations, mutations, condition results, skipped/executed branches, and next action. Put that iteration's table immediately after its explanation. The [Permutation in String walkthrough](./lessons/03-sliding-window/04-permutation-in-string/README.md#block-by-block-code-walkthrough) demonstrates the format.
