# Java Functional Programming Concepts

Learn what Java functional concepts mean, why they exist, and how to use them. The course contains 28 focused concept lessons, with small runnable examples and 84 explained checkpoint questions.

## Start here

Start with [01 — Functional thinking and pure functions](./lessons/01-pure-transformations/README.md). Basic Java variables, methods, and List values are the only assumed starting point. Type shapes and unfamiliar functional terms are explained before use. A list such as `List<String>` stores String values; Integer is the object-wrapper form of int.

Study one 10–15 minute part at a time. In chat, ask to teach a lesson number; we explain that concept, ask 2–3 questions, and wait for your attempt. All learner statuses remain TODO until assessed. The course is prepared, not currently activated.

## Learning format and saved preference

Each lesson explains the problem, concept, a small Java example, its output and short derivation, syntax/type meaning, pitfalls, and checkpoints. Use whole-result and type-shape comparisons when helpful. Per-iteration narration, debugger tables, per-callback scripts, mandatory algorithm invariants, and forced LeetCode exercises are excluded unless you explicitly request them.

This preference also applies to other Java language/concept lessons, and is saved in [AGENTS.md](../../../../AGENTS.md#functional-programming-lesson-standard). Detailed algorithm traces still belong to the separate LeetCode track.

## Course sequence

Parts 01–05 teach behavior and its types; 06–12 teach the stream model, map/flatMap, terminal operations, and absence; 13–19 cover aggregation and composition; 20–24 cover correctness and execution boundaries; 25–28 are advanced extensions.

| Part | Concept | Question it answers |
|---|---|---|
| 01 | [Functional thinking and pure functions](./lessons/01-pure-transformations/README.md) | What makes a calculation functional, and what is a pure function? |
| 02 | [Lambda expressions](./lessons/02-functions-as-values/README.md) | What is a lambda, and how do I read the arrow syntax? |
| 03 | [Functional interfaces: Function and Predicate](./lessons/03-functional-interfaces/README.md) | How does Java know which inputs and result a lambda must have? |
| 04 | [Method references](./lessons/04-method-references/README.md) | What does :: mean, and when can it replace a lambda? |
| 05 | [Supplier, Consumer, and operator shapes](./lessons/05-suppliers-consumers-operators/README.md) | Which functional interface should I use when there is no input, no result, or the same type on both sides? |
| 06 | [What a stream is](./lessons/06-stream-model/README.md) | What is a Java stream, and how is it different from a collection? |
| 07 | [filter: keep matching values](./lessons/07-filter/README.md) | What does filter do, and why does it need a Predicate? |
| 08 | [map: transform one value into another](./lessons/08-map/README.md) | What does map do, and is it related to a Map collection? |
| 09 | [flatMap: flatten nested results](./lessons/09-flatmap/README.md) | What is flatMap, and when should I use it instead of map? |
| 10 | [Lazy pipelines and one-use streams](./lessons/10-laziness-and-reuse/README.md) | When does a stream run, and why can I not reuse it? |
| 11 | [Terminal operations: questions and results](./lessons/11-terminal-operations/README.md) | How do count, matching, finding, and forEach differ? |
| 12 | [Optional: a result may be absent](./lessons/12-optional/README.md) | What is Optional, and how do map, flatMap, orElse, and orElseGet apply to it? |
| 13 | [reduce: combine values into one result](./lessons/13-reduce/README.md) | What is reduce, and what makes a valid reduction? |
| 14 | [collect and result ownership](./lessons/14-collect-and-modifiability/README.md) | How does collect differ from reduce, and which returned lists can I modify? |
| 15 | [Grouping and partitioning](./lessons/15-grouping-and-partitioning/README.md) | How do groupingBy and partitioningBy build category-based results? |
| 16 | [toMap and duplicate-key policy](./lessons/16-tomap-and-duplicates/README.md) | How do I create a Map from a stream without silently losing duplicate keys? |
| 17 | [Ordering, Comparator, and distinct](./lessons/17-ordering-and-distinct/README.md) | How do I choose result order and remove duplicates? |
| 18 | [Primitive streams and arrays](./lessons/18-primitive-streams-and-arrays/README.md) | What are IntStream, mapToInt, boxed, and Arrays.stream? |
| 19 | [Function composition](./lessons/19-function-composition/README.md) | What do andThen and compose mean? |
| 20 | [Captured variables and effectively final](./lessons/20-lambda-capture/README.md) | Why can a lambda read a local variable but not reassign it? |
| 21 | [Exceptions at functional boundaries](./lessons/21-exception-boundaries/README.md) | Why do checked exceptions not fit every lambda? |
| 22 | [Side effects, non-interference, and peek](./lessons/22-side-effects-and-peek/README.md) | Why is changing shared state inside a stream risky? |
| 23 | [Parallel streams: suitability and limits](./lessons/23-parallel-streams/README.md) | Does parallelStream automatically make code faster or safer? |
| 24 | [Streams that own resources](./lessons/24-resource-streams/README.md) | Why must a file-backed stream be closed? |
| 25 | [Bounded and prefix-based pipelines](./lessons/25-bounded-and-multi-output-streams/README.md) | How do limit, skip, takeWhile, and dropWhile control which part of a stream is used? |
| 26 | [mapMulti: emit zero or more results](./lessons/26-mapmulti/README.md) | How does mapMulti differ from map and flatMap? |
| 27 | [Gatherers: custom intermediate transformations](./lessons/27-gatherers/README.md) | What are Gatherers, and how are they different from collectors? |
| 28 | [Custom collector contracts](./lessons/28-custom-collectors/README.md) | What do supplier, accumulator, combiner, and finisher mean in a collector? |

## Essential comparisons

| Distinction | What to remember | Lesson |
|---|---|---|
| Collection / stream | Stored values / description of processing | [06](./lessons/06-stream-model/README.md) |
| filter / map / flatMap | Keep values / change each value / emit contents from mapped groups | [07](./lessons/07-filter/README.md), [08](./lessons/08-map/README.md), [09](./lessons/09-flatmap/README.md) |
| map / Map | Stream transformation / key-value collection | [08](./lessons/08-map/README.md), [16](./lessons/16-tomap-and-duplicates/README.md) |
| Intermediate / terminal | Describes more stream processing / asks for final result or effect | [06](./lessons/06-stream-model/README.md), [10](./lessons/10-laziness-and-reuse/README.md) |
| reduce / collect / gather | Combine a result value / terminal accumulation recipe / intermediate transformation recipe | [13](./lessons/13-reduce/README.md), [14](./lessons/14-collect-and-modifiability/README.md), [27](./lessons/27-gatherers/README.md) |
| toList / toCollection | Explicit unmodifiable list / selected result container | [14](./lessons/14-collect-and-modifiability/README.md) |
| orElse / orElseGet | Argument evaluated before call / fallback supplier invoked only if absent | [12](./lessons/12-optional/README.md) |
| Sequential / parallel | Execution choice, not automatic performance or safety | [23](./lessons/23-parallel-streams/README.md) |

## Runnable examples and optional practice

[FunctionalConceptExamples.java](../../../../src/test/java/com/instagram/backend/interview/functionalprogramming/concepts/FunctionalConceptExamples.java) contains the exact 28 lesson snippets, imports, and contract checks. Run its main directly in IntelliJ. Each numbered method corresponds to a lesson. Java 24 is the project's configured runtime; the Gatherers lesson needs it. Most core APIs originated in Java 8, while takeWhile/dropWhile need Java 9, Stream.toList/mapMulti need Java 16, and final Gatherers need Java 24.

[FunctionalConceptPractice.java](../../../../src/test/java/com/instagram/backend/interview/functionalprogramming/concepts/FunctionalConceptPractice.java) offers optional lambda/map/flatMap tasks, separate from the example answers. Reading, explaining, and predicting the examples can demonstrate a concept without completing those tasks. Older practice/reference programs remain at their original paths for optional coding work; they do not define the new course.

Use the [coverage backlog](./learning-backlog.md) for artifact status, [learner progress](./learner-progress.md) for actual understanding, and [verification note](./verification.md) for compilation and rendered-page evidence.

## Project connections and boundaries

The current [PostService](../../../../src/main/java/com/instagram/backend/service/PostService.java) uses stream().map(...).toList() to convert stored posts into response values. That is in-memory Java processing after repository retrieval, not proof of a database query optimization. The course introduces the language concepts independently before using that project example. No backend endpoint or API contract is changed by recreating these learning artifacts.
