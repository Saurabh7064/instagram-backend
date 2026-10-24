# 16 — toMap and duplicate-key policy

## Concept question

How do I create a Map from a stream without silently losing duplicate keys?

## What you will learn

Name key/value functions, specify a merge policy, and choose map ordering deliberately.

**Time:** 10–15 minutes. **Prerequisite:** [15 — Grouping and partitioning](../15-grouping-and-partitioning/README.md).
**Artifact:** READY after recorded verification. **Understanding:** TODO; no learner attempt recorded.

## Understand the concept

A Map associates each key with one value. Suppose words is ["java", "go", "java"], and the desired map associates each distinct word with its length. Choose a key function that keeps the word and a value function String::length. The repeated java produces a duplicate key.

The two-argument toMap collector throws for duplicate keys; it does not silently choose a winner. An overload accepts a merge function to resolve the old and incoming values. Here `(existing, incoming) -> existing` explicitly keeps the first value. Both java lengths are 4, so the result is still java=4, but the policy is necessary even when the duplicate values happen to be equal.

The fourth argument can choose the Map implementation. LinkedHashMap preserves insertion order, making java then go the display order for this sequential example. When multiple values per key are meaningful, groupingBy is often a better representation than throwing away all but one.

## Small Java example

The snippet below is included in [FunctionalConceptExamples.java](../../../../../../src/test/java/com/instagram/backend/interview/functionalprogramming/concepts/FunctionalConceptExamples.java#L184), method `lesson16`. Run that class's `main` in IntelliJ for checked examples. The file supplies the needed imports; no coding exercise is required.

```java
List<String> words = List.of("java", "go", "java");
Map<String, Integer> lengths = words.stream().collect(Collectors.toMap(
        Function.identity(), String::length,
        (existing, incoming) -> existing, LinkedHashMap::new));
```

**Expected result:** lengths = {java=4, go=2}, with java before go in this LinkedHashMap.

## Read the syntax and result

Function.identity() returns its input unchanged for the key. The lengths are 4, 2, and 4; the repeated java key invokes the merge policy and keeps its existing 4. LinkedHashMap::new selects an insertion-ordered result container.

## Pitfalls and decisions

Choose merge behavior from the business meaning, not just to suppress an exception. toMap rejects null mapped values; if null keys or special ordering matter, check the selected collector/map contracts rather than assuming all Map implementations behave alike.

Official reference: [Java 24 API or language contract](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Collectors.html).

## Questions and explained answers

<details>
<summary>1. What happens without the merge function?</summary>

The duplicate java key makes the two-argument collector throw IllegalStateException.

</details>

<details>
<summary>2. What does Function.identity do?</summary>

It returns the supplied value unchanged, so each word becomes its own key.

</details>

<details>
<summary>3. When should this be groupingBy instead?</summary>

When all occurrences or multiple distinct values per key are meaningful and must be preserved.

</details>

## Stop/go

Explain the concept question in your own words, predict this example's result, and answer the questions before opening their answers. A coding attempt is optional. Record your explanation in [learner progress](../../learner-progress.md); compiled examples do not demonstrate comprehension.

**Next:** [17 — Ordering, Comparator, and distinct](../17-ordering-and-distinct/README.md).
