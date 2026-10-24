# 11 — Terminal operations: questions and results

## Concept question

How do count, matching, finding, and forEach differ?

## What you will learn

Choose a terminal operation according to whether the result is a count, boolean, possible value, collection, or action.

**Time:** 10–15 minutes. **Prerequisite:** [10 — Lazy pipelines and one-use streams](../10-laziness-and-reuse/README.md).
**Artifact:** READY after recorded verification. **Understanding:** TODO; no learner attempt recorded.

## Understand the concept

A terminal operation answers the final question about a stream. `count()` returns a long count. `anyMatch`, `allMatch`, and `noneMatch` answer boolean questions. `findFirst()` asks for the first value in encounter order, if one exists. Its result is Optional, a wrapper representing either a value or no value; the next lesson explains it fully.

For words ["java", "go"], there are two words. At least one has length greater than three, so anyMatch returns true. findFirst finds "java" because the List source supplies that encounter order. Each question needs a fresh stream. `findAny()` permits any element and is useful when the first is unnecessary; do not build correctness on its often-observed sequential result.

`forEach` consumes values to perform an action and returns void. Use it for a deliberate final effect, not to build a shared list when toList or collect expresses the result directly. On empty streams, anyMatch is false, allMatch is true, and noneMatch is true: there is no counterexample to either universal statement.

## Small Java example

The snippet below is included in [FunctionalConceptExamples.java](../../../../../../src/test/java/com/instagram/backend/interview/functionalprogramming/concepts/FunctionalConceptExamples.java#L136), method `lesson11`. Run that class's `main` in IntelliJ for checked examples. The file supplies the needed imports; no coding exercise is required.

```java
List<String> words = List.of("java", "go");
long count = words.stream().count();
boolean hasLongWord = words.stream().anyMatch(word -> word.length() > 3);
String first = words.stream().findFirst().orElse("missing");
```

**Expected result:** count = 2; hasLongWord = true; first = "java".

## Read the syntax and result

Two source values give count 2. java has four characters, so the existence test succeeds. findFirst obtains the first ordered value; orElse gives a fallback only as the resulting choice when no value is present. The fallback expression evaluation rules are discussed next.

## Pitfalls and decisions

Boolean matching may stop when its answer is known. forEach does not promise encounter order in a parallel pipeline; use forEachOrdered if that ordering is required, with its potential cost.

Official reference: [Java 24 API or language contract](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Stream.html).

## Questions and explained answers

<details>
<summary>1. Which terminal gives a boolean?</summary>

anyMatch, allMatch, and noneMatch answer boolean questions. count returns long and findFirst returns Optional.

</details>

<details>
<summary>2. Why three calls to words.stream()?</summary>

Each terminal consumes a different stream. The collection can supply fresh streams for independent questions.

</details>

<details>
<summary>3. Why is allMatch true on empty input?</summary>

There is no element failing the condition. It expresses that no counterexample exists, not that a matching element was found.

</details>

## Stop/go

Explain the concept question in your own words, predict this example's result, and answer the questions before opening their answers. A coding attempt is optional. Record your explanation in [learner progress](../../learner-progress.md); compiled examples do not demonstrate comprehension.

**Next:** [12 — Optional: a result may be absent](../12-optional/README.md).
