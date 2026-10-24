# 14 — collect and result ownership

## Concept question

How does collect differ from reduce, and which returned lists can I modify?

## What you will learn

Choose a collection-building recipe with an explicit modifiability contract.

**Time:** 10–15 minutes. **Prerequisite:** [13 — reduce: combine values into one result](../13-reduce/README.md).
**Artifact:** READY after recorded verification. **Understanding:** TODO; no learner attempt recorded.

## Understand the concept

Sometimes the desired result is a container holding values rather than one combined scalar. `collect` can build that result using a collection recipe called a collector. The recipe describes how result containers are created, filled, and combined. The Collectors utility class supplies common recipes.

`Stream.toList()` returns an unmodifiable List. `collect(Collectors.toList())` makes no guarantee about the returned List type or its modifiability. If a mutable ArrayList is required, say so with `Collectors.toCollection(ArrayList::new)`. Do not infer API guarantees from whichever implementation happens to appear on one machine.

Unmodifiable means you cannot change that list's structure through its API. It does not mean contained objects are deeply immutable. Building a result list also does not authorize editing the original source. Use reduce for a suitable value combination; use collect for managed mutable accumulation or a richer collection recipe.

## Small Java example

The snippet below is included in [FunctionalConceptExamples.java](../../../../../../src/test/java/com/instagram/backend/interview/functionalprogramming/concepts/FunctionalConceptExamples.java#L162), method `lesson14`. Run that class's `main` in IntelliJ for checked examples. The file supplies the needed imports; no coding exercise is required.

```java
List<String> words = List.of("java", "go");
List<String> fixed = words.stream().toList();
ArrayList<String> editable = words.stream()
        .collect(Collectors.toCollection(ArrayList::new));
editable.add("sql");
```

**Expected result:** fixed = [java, go]; editable = [java, go, sql]; words remains [java, go].

## Read the syntax and result

The collector creates a separate ArrayList, so adding sql changes only editable. fixed gathers the same original two values under an unmodifiable-list contract. ArrayList::new is the constructor supplier taught in method references.

## Pitfalls and decisions

Collectors.toUnmodifiableList rejects null elements, while Stream.toList allows them. A result containing mutable objects can still expose changes to those objects; structural ownership is not a deep-copy guarantee.

Official reference: [Java 24 API or language contract](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Collectors.html).

## Questions and explained answers

<details>
<summary>1. Is Collectors.toList guaranteed mutable?</summary>

No. Its contract does not guarantee type or modifiability. Request a specific collection factory when that matters.

</details>

<details>
<summary>2. Why does editable.add not change words?</summary>

The collector constructed a separate result container rather than editing the source list.

</details>

<details>
<summary>3. Does unmodifiable imply deep immutability?</summary>

No. It blocks changes through the list API, but objects stored in it may still be mutable.

</details>

## Stop/go

Explain the concept question in your own words, predict this example's result, and answer the questions before opening their answers. A coding attempt is optional. Record your explanation in [learner progress](../../learner-progress.md); compiled examples do not demonstrate comprehension.

**Next:** [15 — Grouping and partitioning](../15-grouping-and-partitioning/README.md).
