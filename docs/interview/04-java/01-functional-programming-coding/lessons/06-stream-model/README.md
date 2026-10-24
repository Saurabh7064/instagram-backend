# 06 — What a stream is

## Concept question

What is a Java stream, and how is it different from a collection?

## What you will learn

Identify source, intermediate operations, and terminal operation without imagining intermediate lists.

**Time:** 10–15 minutes. **Prerequisite:** [05 — Supplier, Consumer, and operator shapes](../05-suppliers-consumers-operators/README.md).
**Artifact:** READY after recorded verification. **Understanding:** TODO; no learner attempt recorded.

## Understand the concept

A collection stores values; a stream describes processing values from a source. It is not another list and does not itself store a copy of all the source elements. A pipeline connects a source, optional intermediate operations, and a terminal operation. An intermediate operation returns a stream describing the next part of the work. A terminal operation produces a final result or performs the final action.

In the smallest example, the source list already holds "java" and "streams". `stream()` obtains a sequential stream. `toList()` is terminal: it gathers the stream elements into a result list. No intermediate operation is needed. Later lessons add filtering and mapping, one operation at a time.

Streams can come from collections, arrays, generated values, and files. The processing model does not require every source to be stored in memory or finite. A stream is intended for one use; ask the collection for a fresh stream when processing it again.

## Small Java example

The snippet below is included in [FunctionalConceptExamples.java](../../../../../../src/test/java/com/instagram/backend/interview/functionalprogramming/concepts/FunctionalConceptExamples.java#L91), method `lesson06`. Run that class's `main` in IntelliJ for checked examples. The file supplies the needed imports; no coding exercise is required.

```java
List<String> words = List.of("java", "streams");
List<String> result = words.stream().toList();
```

**Expected result:** result = [java, streams]; words remains [java, streams].

## Read the syntax and result

The pipeline requests the source values and gathers them in their existing encounter order. No rule changes the strings, so the result contains the same two values. Stream.toList() returns an unmodifiable list; it does not promise deeply immutable contained objects.

## Pitfalls and decisions

Do not confuse java.util.stream.Stream with byte-oriented input/output streams. Calling stream() does not make work parallel, and using streams does not imply database-side processing.

Official reference: [Java 24 API or language contract](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/package-summary.html).

## Questions and explained answers

<details>
<summary>1. Where are the original words stored?</summary>

In words, the source List. The stream describes processing those values rather than becoming their new storage container.

</details>

<details>
<summary>2. Which call finishes this pipeline?</summary>

toList() is the terminal operation that obtains the result.

</details>

<details>
<summary>3. Can I add an element to result?</summary>

Stream.toList() returns an unmodifiable list, so add throws UnsupportedOperationException. That is separate from whether objects inside it can change.

</details>

## Stop/go

Explain the concept question in your own words, predict this example's result, and answer the questions before opening their answers. A coding attempt is optional. Record your explanation in [learner progress](../../learner-progress.md); compiled examples do not demonstrate comprehension.

**Next:** [07 — filter: keep matching values](../07-filter/README.md).
