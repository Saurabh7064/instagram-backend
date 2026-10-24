# Java Interview Questions

Prioritize explanation and production examples over trivia.

## Java learning tracks

- [Functional Programming Concepts](./01-functional-programming-coding/README.md) — 28 lessons on lambdas, interfaces, streams, map/flatMap, collectors, Optional, and execution boundaries
- [Multithreading from Scratch](./02-multithreading-from-scratch/README.md) — threads, coordination, shared-state safety, executors, futures, and concurrent design

Both tracks are initialized but inactive. Activate one micro-lesson at a time and keep artifact completeness separate from demonstrated understanding.

For Java language/concept lessons, teach the meaning, purpose, small example and result, API differences, pitfalls, and checkpoints. Per-iteration narration and debugger traces are excluded unless requested; implementation exercises are optional. This preference is saved in [AGENTS.md](../../../AGENTS.md#functional-programming-lesson-standard).

## Java and JVM priority list

- Object contracts: equality, hashing, immutability, records
- Collections and their performance trade-offs
- Generics and type erasure
- Exceptions and resource management
- Streams, lambdas, and when not to use them
- Threads, executors, futures, locks, and concurrent collections
- Java Memory Model, visibility, atomicity, and `volatile`
- JVM memory, garbage collection, profiling, and common failure symptoms
- Modern Java features used in the target company’s version
- Testing with JUnit, mocking boundaries, and integration tests

## Answer template

1. Define the idea simply.
2. Explain the problem it solves.
3. Give a concrete Java example.
4. State the important trade-off or failure mode.
5. Connect it to production experience when truthful.

Keep framework-specific preparation in [Spring and Hibernate](../05-spring-hibernate/README.md).
