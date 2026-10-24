# 22 — Side effects, non-interference, and peek

## Concept question

Why is changing shared state inside a stream risky?

## What you will learn

Keep transformation separate from effects and understand the limits of peek-based debugging.

**Time:** 10–15 minutes. **Prerequisite:** [21 — Exceptions at functional boundaries](../21-exception-boundaries/README.md).
**Artifact:** READY after recorded verification. **Understanding:** TODO; no learner attempt recorded.

## Understand the concept

A pipeline's behavior functions should not modify its data source while the pipeline is reading it. This is non-interference. They should normally avoid depending on changing external state as well. A mapper describing a value conversion is easier to reason about than a mapper that also edits a shared list or increments a counter.

For ["java", "go"], transforming each String to its length asks for [4, 2]. map(String::length).toList() states that result directly. Building an external ArrayList with forEach(list::add) adds shared state that becomes unsafe if the stream becomes parallel. A collector can manage result accumulation without exposing that shared mutable buffer.

`peek` is an intermediate operation intended mainly for observing values during debugging. It is not a dependable business-action hook: work may be skipped because of short circuiting or optimization. Logging with peek can help investigation, but missing log lines are not proof a value never existed. Use explicit final actions where a documented effect is actually required.

## Small Java example

The snippet below is included in [FunctionalConceptExamples.java](../../../../../../src/test/java/com/instagram/backend/interview/functionalprogramming/concepts/FunctionalConceptExamples.java#L237), method `lesson22`. Run that class's `main` in IntelliJ for checked examples. The file supplies the needed imports; no coding exercise is required.

```java
List<String> words = List.of("java", "go");
List<Integer> lengths = words.stream()
        .map(String::length)
        .toList();
```

**Expected result:** lengths = [4, 2]; words remains [java, go].

## Read the syntax and result

The mapper computes word lengths, and the terminal gathers the returned values. No external result list or counter is edited by the mapper. The output follows from java having four characters and go having two.

## Pitfalls and decisions

Do not use peek to save database rows, enforce validation that must always run, or count exact processing actions. A terminal count may use known source size without traversing every mapper. Use tests and explicit boundaries for required effects.

Official reference: [Java 24 API or language contract](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/package-summary.html).

## Questions and explained answers

<details>
<summary>1. What is non-interference?</summary>

Do not modify the stream source while processing it, unless a source explicitly supports the relevant concurrent behavior.

</details>

<details>
<summary>2. Why collect rather than add to an external list?</summary>

A collector owns its accumulation strategy; an external list adds shared mutable state and may be unsafe under parallel execution.

</details>

<details>
<summary>3. Is peek guaranteed for every source value?</summary>

No. Short circuiting and permitted optimizations can omit callbacks. It is unsuitable for mandatory business work.

</details>

## Stop/go

Explain the concept question in your own words, predict this example's result, and answer the questions before opening their answers. A coding attempt is optional. Record your explanation in [learner progress](../../learner-progress.md); compiled examples do not demonstrate comprehension.

**Next:** [23 — Parallel streams: suitability and limits](../23-parallel-streams/README.md).
