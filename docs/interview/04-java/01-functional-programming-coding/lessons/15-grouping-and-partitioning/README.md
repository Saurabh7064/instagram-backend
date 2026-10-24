# 15 — Grouping and partitioning

## Concept question

How do groupingBy and partitioningBy build category-based results?

## What you will learn

Choose a category key or a boolean split and understand a downstream collector.

**Time:** 10–15 minutes. **Prerequisite:** [14 — collect and result ownership](../14-collect-and-modifiability/README.md).
**Artifact:** READY after recorded verification. **Understanding:** TODO; no learner attempt recorded.

## Understand the concept

Given ["java", "go", "java"], we may want words organized by length. A classifier is a function choosing a category key. String::length gives key 4 for both java values and key 2 for go. `groupingBy` gathers all values for each key: {4=[java, java], 2=[go]}.

A downstream collector describes what each group should contain. The default is a list; adding counting() asks for how many values belong to each key. The result becomes {4=2, 2=1}, with Long counts. This avoids building a separate list when the goal is only the per-category count.

`partitioningBy` is the special boolean split: the predicate separates matches from nonmatches. For length greater than three, true holds [java, java] and false holds [go]. Its Map includes both boolean keys, even if one category is empty. Map key iteration order is not promised by these default collectors.

## Small Java example

The snippet below is included in [FunctionalConceptExamples.java](../../../../../../src/test/java/com/instagram/backend/interview/functionalprogramming/concepts/FunctionalConceptExamples.java#L172), method `lesson15`. Run that class's `main` in IntelliJ for checked examples. The file supplies the needed imports; no coding exercise is required.

```java
List<String> words = List.of("java", "go", "java");
Map<Integer, List<String>> byLength = words.stream()
        .collect(Collectors.groupingBy(String::length));
Map<Integer, Long> counts = words.stream()
        .collect(Collectors.groupingBy(String::length, Collectors.counting()));
Map<Boolean, List<String>> split = words.stream()
        .collect(Collectors.partitioningBy(word -> word.length() > 3));
```

**Expected result:** byLength has 4=[java, java], 2=[go]; counts has 4=2L, 2=1L; split has true=[java, java], false=[go].

## Read the syntax and result

Both java occurrences have length 4, so that group has two values and count 2. go has length 2, so its group has one. The predicate tests membership in the greater-than-three group; it does not transform any word.

## Pitfalls and decisions

Grouping is not deduplication: repeated values remain unless you choose a set-based downstream collector. Do not depend on the default Map implementation or key order. A null classifier result is not supported by groupingBy.

Official reference: [Java 24 API or language contract](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Collectors.html).

## Questions and explained answers

<details>
<summary>1. Why are there two java values in byLength?</summary>

Grouping gathers occurrences under a key; it does not remove duplicates.

</details>

<details>
<summary>2. What changes when counting is downstream?</summary>

Each group stores a Long count instead of a List of its source values.

</details>

<details>
<summary>3. When is partitioningBy clearer?</summary>

When there are exactly two categories defined by one boolean test, rather than arbitrary keys.

</details>

## Stop/go

Explain the concept question in your own words, predict this example's result, and answer the questions before opening their answers. A coding attempt is optional. Record your explanation in [learner progress](../../learner-progress.md); compiled examples do not demonstrate comprehension.

**Next:** [16 — toMap and duplicate-key policy](../16-tomap-and-duplicates/README.md).
