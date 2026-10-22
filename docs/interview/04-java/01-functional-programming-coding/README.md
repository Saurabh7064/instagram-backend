# Java Functional Programming Coding Skills

- Track status: `INITIALIZED`
- Active lesson: none
- First lesson: [Pure Transformations](./lessons/01-pure-transformations/README.md) — `TODO`
- Progress tracker: [Learning Backlog](./learning-backlog.md)

## Goal

Build practical Java functional-programming skill by understanding the concept, writing small programs, then applying it to a suitable coding problem. Define the idea before naming its API. Functional programming can operate on arrays as well as collections; the style concerns how computation is expressed and what it changes.

## Working rules

1. Begin each micro-lesson with only its exact question, followed by explicit `What you will learn` concept outcomes.
2. Teach the mental model, data flow, invariants, trade-offs, and failure modes before directing the learner to the practice file.
3. Activate only one lesson in this track at a time.
4. Keep practice and reference programs separate and directly runnable from IntelliJ.
5. Prefer deterministic examples; do not use parallel streams until mutation and thread-safety risks have been taught.
6. State whether input is mutated and whether the returned collection is modifiable.
7. Explain when a loop is clearer than a stream instead of treating streams as automatically better.
8. Record code completion separately from checkpoint understanding and later retrieval.

## How each lesson should teach

1. Start with the conceptual question and explain the problem the concept solves.
2. Define at most three unfamiliar terms, using a small worked calculation before library syntax.
3. Give one or two directly runnable programs related to that same concept, with separate practice and reference files.
4. Interleave small code blocks, why they exist, concise execution bullets for each iteration/callback, and exact value tables. Explain guards, allocations, callback invocations, discarded values, and termination. A stream’s logical emitted values are not a claim about hidden counters or buffers.
5. Add a focused LeetCode bridge when it fits naturally, after its prerequisites. Compare with a loop and preserve algorithmic complexity; do not force a stream onto every problem.
6. Check understanding through prediction, a plain-language explanation, and an implementation attempt. Artifact readiness does not earn comprehension credit.

## Prepared introductory sequence

| Lesson | Concept first | Runnable problem | Learning status |
|---|---|---|---|
| [01 — Pure Transformations](./lessons/01-pure-transformations/README.md) | Same input values, same result values, no outside changes | Double a scalar; transform an array into a new one | `TODO` |
| [02 — Pass Behavior as a Value](./lessons/02-functions-as-values/README.md) | Supply a calculation instead of copying traversal logic | One array loop with doubling, adding three, and a method reference | `TODO` |
| [03 — Compose Word Transformations](./lessons/03-stream-transformations/README.md) | Describe independent value changes and keep/discard decisions | Trim, remove blanks, lowercase, and gather words | `TODO` |
| [04 — LeetCode 1929: Concatenation of Array](./lessons/04-array-concatenation/README.md) | Apply an input-preserving transformation to array data | Stream and loop solutions with the same O(n) algorithm | `TODO` |

The original normalization Java files remain at their old paths and now belong to lesson 03. The original first-lesson URL is preserved, but its content starts with purity rather than assuming streams, lambdas, and method references. Later lessons stay TODO; the track remains inactive until explicitly selected.

## Ordered path

1. Introductory module: purity → functions as values → stream transformations → one array LeetCode bridge
2. Reduction and aggregation
3. Collectors for grouping and partitioning
4. `Optional` as an explicit absence result
5. Function composition and reusable behavior
6. `flatMap` for nested data
7. Checked exceptions at functional boundaries
8. Side effects and debugging pipelines
9. Parallel streams: suitability, measurement, and safety

Start with the [learning backlog](./learning-backlog.md); later lessons remain `TODO` until the current lesson's stop/go criteria are met.

Keep useful debugger-table columns and full state context; remove any execution-script column that repeats the adjacent bullets. If one value changes several times within an iteration, show the intermediate values in that same cell (for example `2 → 3 → 2`); do not replace the table with a changes-only summary.

Write each execution as concise bullets: actual statement/API → concrete values and changes → evaluated condition and executed/skipped branch → final state → next action/return. Place ✅ or ❌ before evaluated conditions, retain true/false, and leave skipped checks unmarked. Keep each original value table immediately after its bullet list, including full collection context and in-cell change paths.

Before the code walkthrough, explain the idea as purpose → plain meaning of saved values → one worked calculation on the fixed input → general rule → why each term or boundary adjustment is needed. Use connected prose for this conceptual reasoning; keep the detailed execution in concise bullets with adjacent state tables. The [Range Sum explanation](../../01-leetcode/lessons/04-prefix-suffix-intervals/01-range-sum-query-immutable/README.md#first-understand-the-idea) is the concrete model.
