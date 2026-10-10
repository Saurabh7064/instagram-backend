# Java Multithreading from Scratch

- Track status: `INITIALIZED`
- Assumed background: ordinary sequential Java only; no concurrency knowledge required
- Active lesson: none
- First lesson: [Starting and Joining One Worker](./lessons/01-start-and-join/README.md) — `TODO`
- Progress tracker: [Learning Backlog](./learning-backlog.md)

## Goal

Build a correct mental model of concurrent Java before using high-level frameworks. Each lesson introduces one coordination problem, predicts observable behavior, uses deterministic checks, and explains the failure caused by removing the coordination.

## Working rules

1. Begin each micro-lesson with only its exact question, followed by explicit `What you will learn` concept outcomes.
2. Teach the concurrency model and predicted behavior before presenting the runnable exercise.
3. Teach one micro-lesson at a time in prerequisite order.
4. Define each concurrency term before using it.
5. Use prediction → action → observation → explanation.
6. Do not use arbitrary `Thread.sleep` calls to make correctness tests pass.
7. Prefer `join`, latches, barriers, futures, or executor termination for deterministic coordination.
8. Include a controlled failure and recovery when the lesson introduces shared mutable state.
9. Keep code completion separate from the learner's prediction and explanation.

## Ordered path

1. Start one worker and wait with `join`
2. Observe a race condition
3. Protect a critical section with `synchronized`
4. Understand visibility and `volatile`
5. Perform atomic updates
6. Submit tasks to an executor
7. Retrieve results with futures
8. Compose asynchronous work
9. Use concurrent collections
10. Coordinate producer and consumer work
11. Diagnose deadlock and prevent it
12. Evaluate virtual threads for blocking workloads

Start with the [learning backlog](./learning-backlog.md). Later lessons remain `TODO` until their prerequisites are demonstrated.
