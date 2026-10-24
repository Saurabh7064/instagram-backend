# 18 — Primitive streams and arrays

## Concept question

What are IntStream, mapToInt, boxed, and Arrays.stream?

## What you will learn

Choose primitive numeric processing and recognize the int[] varargs trap.

**Time:** 10–15 minutes. **Prerequisite:** [17 — Ordering, Comparator, and distinct](../17-ordering-and-distinct/README.md).
**Artifact:** READY after recorded verification. **Understanding:** TODO; no learner attempt recorded.

## Understand the concept

Java has primitive int values and object-wrapper Integer values. IntStream processes primitive ints, while `Stream<Integer>` processes Integer references. Primitive specializations provide numeric operations such as sum, average, and summaryStatistics. LongStream and DoubleStream serve the corresponding numeric types.

For numbers [2, 4, 6], Arrays.stream(numbers) creates an IntStream and sum gives 2 + 4 + 6 = 12. For words ["java", "go"], mapToInt(String::length) produces primitive values 4 and 2, whose sum is 6. `boxed()` converts primitive stream values into wrapper elements for APIs that need objects.

Be careful with Stream.of(intArray): it produces a `Stream<int[]>` containing one array object, not a stream of the array's individual primitive values. Object arrays behave differently because their elements are already reference values. Arrays.stream(intArray) states the primitive intent clearly.

## Small Java example

The snippet below is included in [FunctionalConceptExamples.java](../../../../../../src/test/java/com/instagram/backend/interview/functionalprogramming/concepts/FunctionalConceptExamples.java#L203), method `lesson18`. Run that class's `main` in IntelliJ for checked examples. The file supplies the needed imports; no coding exercise is required.

```java
int[] numbers = {2, 4, 6};
int total = Arrays.stream(numbers).sum();
List<Integer> boxed = Arrays.stream(numbers).boxed().toList();
int letters = List.of("java", "go").stream().mapToInt(String::length).sum();
long arrayObjects = Stream.of(numbers).count();
```

**Expected result:** total = 12; boxed = [2, 4, 6]; letters = 6; arrayObjects = 1.

## Read the syntax and result

The numeric total is 2 + 4 + 6 = 12; the word-length total is 4 + 2 = 6. boxed gathers the same numeric values as Integers. Stream.of(numbers) sees the int[] as one object, explaining count 1.

## Pitfalls and decisions

Primitive arithmetic can overflow; sum does not automatically widen int to long. Use mapToLong/asLongStream when the expected range needs it. average on empty input returns OptionalDouble, not zero.

Official reference: [Java 24 API or language contract](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/IntStream.html).

## Questions and explained answers

<details>
<summary>1. Why does Stream.of(numbers).count equal one?</summary>

numbers is one primitive-array object in that Stream, rather than three boxed elements.

</details>

<details>
<summary>2. What does mapToInt change?</summary>

It produces an IntStream of primitive ints from object elements, enabling numeric terminal operations.

</details>

<details>
<summary>3. Does boxed copy numbers into a new int array?</summary>

No. It converts primitive values to wrapper elements in a `Stream<Integer>`; toList then gathers a List.

</details>

## Stop/go

Explain the concept question in your own words, predict this example's result, and answer the questions before opening their answers. A coding attempt is optional. Record your explanation in [learner progress](../../learner-progress.md); compiled examples do not demonstrate comprehension.

**Next:** [19 — Function composition](../19-function-composition/README.md).
