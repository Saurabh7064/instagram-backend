# 27 — Gatherers: custom intermediate transformations

## Concept question

What are Gatherers, and how are they different from collectors?

## What you will learn

Distinguish an intermediate transformation recipe from a terminal result-building recipe.

**Time:** 10–15 minutes. **Prerequisite:** [26 — mapMulti: emit zero or more results](../26-mapmulti/README.md).
**Artifact:** READY after recorded verification. **Understanding:** TODO; no learner attempt recorded.

## Understand the concept

Some transformations need related neighboring values rather than an isolated mapping. A gatherer is a recipe for an intermediate stream transformation that can carry state and emit outputs. A collector is a recipe for the terminal collect operation. The similar names do not make them interchangeable.

Java 24 supplies Gatherers.windowFixed(size), which groups incoming values into non-overlapping lists of that size. For [1, 2, 3, 4, 5] and size 2, the first two values form [1, 2], the next two form [3, 4], and the remaining value forms [5]. The result therefore has three window lists, not five scalar values.

`gather(...)` returns another stream, so further stream operations remain possible before a terminal operation. This is useful when a standard intermediate recipe fits the problem; it avoids hiding cross-element state in a shared external variable. A custom gatherer needs a careful state/lifecycle design and is advanced material after using standard ones.

## Small Java example

The snippet below is included in [FunctionalConceptExamples.java](../../../../../../src/test/java/com/instagram/backend/interview/functionalprogramming/concepts/FunctionalConceptExamples.java#L290), method `lesson27`. Run that class's `main` in IntelliJ for checked examples. The file supplies the needed imports; no coding exercise is required.

```java
List<List<Integer>> windows = List.of(1, 2, 3, 4, 5).stream()
        .gather(Gatherers.windowFixed(2))
        .toList();
```

**Expected result:** windows = [[1, 2], [3, 4], [5]].

## Read the syntax and result

Fixed windows group pairs without overlap; five values make two complete pairs and one final partial window. gather produces a stream of lists. toList then gathers those emitted windows into the outer result list.

## Pitfalls and decisions

Gatherers became a final API in Java 24; code using it will not compile on Java 17 or 21. Fixed windows and sliding windows have different overlap semantics. Do not claim every gatherer can run efficiently in parallel.

Official reference: [Java 24 API or language contract](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Gatherers.html).

## Questions and explained answers

<details>
<summary>1. Is gather terminal like collect?</summary>

No. gather is intermediate and returns a stream; collect is terminal.

</details>

<details>
<summary>2. Why is the final window only [5]?</summary>

There are five values and non-overlapping windows of size two, leaving one value in the final partial window.

</details>

<details>
<summary>3. Can this run unchanged on Java 17?</summary>

No. The final Gatherers API is available in Java 24; choose an implementation matching the target runtime.

</details>

## Stop/go

Explain the concept question in your own words, predict this example's result, and answer the questions before opening their answers. A coding attempt is optional. Record your explanation in [learner progress](../../learner-progress.md); compiled examples do not demonstrate comprehension.

**Next:** [28 — Custom collector contracts](../28-custom-collectors/README.md).
