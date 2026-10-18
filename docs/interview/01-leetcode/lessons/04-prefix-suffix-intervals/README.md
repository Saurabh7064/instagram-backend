# Module 04 — Prefix/Suffix State and Intervals

## What this module teaches

The first three micro-lessons reuse earlier or later calculations instead of recomputing every range. The last two combine inclusive intervals by preserving covered points. Each lesson teaches one focused problem and includes a separate editable Java practice program and runnable reference solution.

## Prerequisites and study order

Use array indexes and Java loops comfortably; revisit the earlier array and sliding-window lessons when needed. Lesson 2 additionally uses HashMap counting; lesson 4 introduces the specific sorting and copying APIs it uses. Follow the sequence below as individual 10–15 minute concept-and-trace sessions; coding and retrieval may take longer. Teach and attempt only one lesson at a time.

| Order | Micro-lesson | Primary idea | Learning status |
|---:|---|---|---|
| 1 | [Range Sum Query — Immutable](./01-range-sum-query-immutable/README.md) | Saved totals at boundaries | `TODO` |
| 2 | [Subarray Sum Equals K](./02-subarray-sum-equals-k/README.md) | Frequencies of earlier totals | `TODO` |
| 3 | [Product of Array Except Self](./03-product-of-array-except-self/README.md) | Products strictly left and right | `TODO` |
| 4 | [Merge Intervals](./04-merge-intervals/README.md) | One unfinished interval after sorting | `TODO` |
| 5 | [Insert Interval](./05-insert-interval/README.md) | Copy before, combine overlaps, copy after | `TODO` |

## Artifact readiness versus learning progress

These five artifacts are prepared at the user’s request. Each practice/reference pair must compile and the reference checks must pass before delivery. Checkpoint answers, an invariant/complexity explanation, and the learner’s own passing implementation are still required before advancing comprehension. The active lesson remains [Two Sum](../01-arrays-hash-maps/03-value-to-index/README.md); creating these future artifacts does not promote its status or activate this module.

## How to read an execution

Read the named inputs first, then follow the guard, allocations, and each narrated iteration. Each iteration's value table sits immediately after its explanation; arrays and maps show their complete current state. Read the loop’s final false check or the reason an early return prevents it. Complete reference listings repeat the input and expected return at the top so they remain visible beside the code.

## Verification

Validation results and commit pointers are recorded in the [commit map](../../../../commit-map.md). Return to the [lesson index](../../lesson-index.md) for the larger sequence. No new UI rendering behavior or API endpoint is introduced by these lesson artifacts.
