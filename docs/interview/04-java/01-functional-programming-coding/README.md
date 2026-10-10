# Java Functional Programming Coding Skills

- Track status: `INITIALIZED`
- Active lesson: none
- First lesson: [Pure Transformations](./lessons/01-pure-transformations/README.md) — `TODO`
- Progress tracker: [Learning Backlog](./learning-backlog.md)

## Goal

Build practical Java functional-programming skill through small runnable problems, not API memorization. Each lesson should make the data transformation, side-effect boundary, correctness argument, and complexity visible.

## Working rules

1. Activate only one lesson in this track at a time.
2. Keep practice and reference programs separate and directly runnable from IntelliJ.
3. Prefer deterministic examples; do not use parallel streams until mutation and thread-safety risks have been taught.
4. State whether input is mutated and whether the returned collection is modifiable.
5. Explain when a loop is clearer than a stream instead of treating streams as automatically better.
6. Record code completion separately from checkpoint understanding and later retrieval.

## Ordered path

1. Pure transformations with `map` and `filter`
2. Reduction and aggregation
3. Collectors for grouping and partitioning
4. `Optional` as an explicit absence result
5. Function composition and reusable behavior
6. `flatMap` for nested data
7. Checked exceptions at functional boundaries
8. Side effects and debugging pipelines
9. Parallel streams: suitability, measurement, and safety

Start with the [learning backlog](./learning-backlog.md); later lessons remain `TODO` until the current lesson's stop/go criteria are met.
