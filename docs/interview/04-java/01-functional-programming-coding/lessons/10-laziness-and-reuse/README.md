# 10 — Lazy pipelines and one-use streams

## Concept question

When does a stream run, and why can I not reuse it?

## What you will learn

Separate a pipeline recipe from a terminal request and obtain a fresh stream for another use.

**Time:** 10–15 minutes. **Prerequisite:** [09 — flatMap: flatten nested results](../09-flatmap/README.md).
**Artifact:** READY after recorded verification. **Understanding:** TODO; no learner attempt recorded.

## Understand the concept

Writing intermediate operations describes work without requesting the final result. This is laziness: the pipeline waits for a terminal operation before computing its result. For ["java", "go"], a filter for length greater than three describes which words should survive; toList() asks for the actual list ["java"].

A stream is intended to be consumed once. Keeping the source collection is different from keeping an unused stream. After a terminal operation, do not call another terminal operation on that stream or reuse an earlier linked pipeline stage. Ask the source for a new stream instead.

Laziness also allows unnecessary work to be avoided. A short-circuiting terminal operation can finish without needing every value; an implementation may omit transformations that cannot affect a result. Therefore logging or counting inside map is not a dependable way to count business actions. Stream behavior promises the result and its documented semantics, not one fixed internal execution schedule.

## Small Java example

The snippet below is included in [FunctionalConceptExamples.java](../../../../../../src/test/java/com/instagram/backend/interview/functionalprogramming/concepts/FunctionalConceptExamples.java#L126), method `lesson10`. Run that class's `main` in IntelliJ for checked examples. The file supplies the needed imports; no coding exercise is required.

```java
List<String> source = List.of("java", "go");
Stream<String> pending = source.stream()
        .filter(word -> word.length() > 3);
List<String> result = pending.toList();
long freshCount = source.stream().count();
```

**Expected result:** result = [java]; freshCount = 2.

## Read the syntax and result

The first declaration of pending describes the keep rule. toList() consumes that pipeline to obtain java, because length 4 exceeds 3 while length 2 does not. source.stream() creates another stream for counting the two source elements; it does not reuse pending.

## Pitfalls and decisions

A reused stream may throw IllegalStateException, but not every invalid reuse can be detected. Never depend on the absence of an exception as permission to reuse it. Do not assert a fixed number of peek/map callbacks to demonstrate laziness.

Official reference: [Java 24 API or language contract](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/package-summary.html).

## Questions and explained answers

<details>
<summary>1. Is pending a stored filtered List?</summary>

No. It describes an unconsumed stream pipeline. The result List is obtained with toList().

</details>

<details>
<summary>2. Can pending.count() safely follow pending.toList()?</summary>

No. The stream has already been consumed. Obtain a fresh stream from source.

</details>

<details>
<summary>3. Why avoid business updates in map?</summary>

The pipeline may short circuit or optimize away work. Business effects need explicit contracts rather than assumed callback counts.

</details>

## Stop/go

Explain the concept question in your own words, predict this example's result, and answer the questions before opening their answers. A coding attempt is optional. Record your explanation in [learner progress](../../learner-progress.md); compiled examples do not demonstrate comprehension.

**Next:** [11 — Terminal operations: questions and results](../11-terminal-operations/README.md).
