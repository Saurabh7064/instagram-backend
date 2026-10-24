# 25 — Bounded and prefix-based pipelines

## Concept question

How do limit, skip, takeWhile, and dropWhile control which part of a stream is used?

## What you will learn

Recognize finite bounds and distinguish prefix rules from filtering.

**Time:** 10–15 minutes. **Prerequisite:** [24 — Streams that own resources](../24-resource-streams/README.md).
**Artifact:** READY after recorded verification. **Understanding:** TODO; no learner attempt recorded.

## Understand the concept

A generated stream can be unbounded: its source need not have a final element. Before asking to gather all results, establish a bound. IntStream.iterate(1, value -> value + 1).limit(4) gives the finite values [1, 2, 3, 4]. Their sum is 1 + 2 + 3 + 4 = 10. Placing an unbounded sorting operation before that limit would not produce the same useful finite computation.

`takeWhile` keeps the leading prefix while a condition holds; `dropWhile` discards that leading prefix. These differ from filter, which selects matching values throughout the source. For [1, 2, 3, 4], takeWhile(value < 3) returns [1, 2]. skip(n) discards a count rather than testing a condition. These operations are easiest to predict with ordered sources.

## Small Java example

The snippet below is included in [FunctionalConceptExamples.java](../../../../../../src/test/java/com/instagram/backend/interview/functionalprogramming/concepts/FunctionalConceptExamples.java#L269), method `lesson25`. Run that class's `main` in IntelliJ for checked examples. The file supplies the needed imports; no coding exercise is required.

```java
List<Integer> numbers = IntStream.iterate(1, value -> value + 1)
        .limit(4).boxed().toList();
List<Integer> prefix = numbers.stream().takeWhile(value -> value < 3).toList();
int total = numbers.stream().mapToInt(Integer::intValue).sum();
```

**Expected result:** numbers = [1, 2, 3, 4]; prefix = [1, 2]; total = 10.

## Read the syntax and result

limit makes the generated source finite at four values. takeWhile stops its prefix before 3, the first value that fails the less-than-three condition. The total adds all four bounded values.

## Pitfalls and decisions

A limit after a filter is not a guarantee of termination: an unbounded source might never supply enough matches. Choose bounds and operation order deliberately. Newer API availability must match the target Java runtime.

Official reference: [Java 24 API or language contract](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Stream.html).

## Questions and explained answers

<details>
<summary>1. Does limit always make any pipeline terminate?</summary>

No. Upstream operations may never produce the requested values, or a preceding unbounded sort may never finish.

</details>

<details>
<summary>2. How is takeWhile different from filter?</summary>

takeWhile uses a leading prefix and stops at its boundary; filter considers matching values throughout the source.

</details>

<details>
<summary>3. How does skip differ from dropWhile?</summary>

skip discards a specified count. dropWhile discards a leading prefix satisfying a predicate. Neither means selecting all matching values throughout the stream.

</details>

## Stop/go

Explain the concept question in your own words, predict this example's result, and answer the questions before opening their answers. A coding attempt is optional. Record your explanation in [learner progress](../../learner-progress.md); compiled examples do not demonstrate comprehension.

**Next:** [26 — mapMulti: emit zero or more results](../26-mapmulti/README.md).
