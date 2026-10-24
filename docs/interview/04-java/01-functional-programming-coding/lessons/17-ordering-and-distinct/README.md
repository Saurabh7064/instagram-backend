# 17 — Ordering, Comparator, and distinct

## Concept question

How do I choose result order and remove duplicates?

## What you will learn

Separate encounter order, chosen sort order, and equality-based deduplication.

**Time:** 10–15 minutes. **Prerequisite:** [16 — toMap and duplicate-key policy](../16-tomap-and-duplicates/README.md).
**Artifact:** READY after recorded verification. **Understanding:** TODO; no learner attempt recorded.

## Understand the concept

A List source has an encounter order: the order its values are presented. Sorting chooses a new order; it does not edit the original list. A Comparator is behavior comparing two values to decide their order. `Comparator.comparingInt(String::length)` orders words by length without manually subtracting integers.

For ["sql", "go", "java", "go"], removing equal duplicates leaves ["sql", "go", "java"] in first-encounter order. Sorting those three words by lengths 3, 2, and 4 produces ["go", "sql", "java"]. `distinct()` uses equality, not your sort comparator. For custom objects, correct equals/hashCode contracts matter.

With ordered streams, distinct retains the first equal occurrence, and sorted is stable for comparator ties. An unordered source does not gain an original order merely by being streamed. If a later result needs a deterministic tie order, add an explicit thenComparing rule rather than relying on unspecified upstream order.

## Small Java example

The snippet below is included in [FunctionalConceptExamples.java](../../../../../../src/test/java/com/instagram/backend/interview/functionalprogramming/concepts/FunctionalConceptExamples.java#L193), method `lesson17`. Run that class's `main` in IntelliJ for checked examples. The file supplies the needed imports; no coding exercise is required.

```java
List<String> words = List.of("sql", "go", "java", "go");
List<String> result = words.stream()
        .distinct()
        .sorted(Comparator.comparingInt(String::length))
        .toList();
```

**Expected result:** result = [go, sql, java].

## Read the syntax and result

distinct retains one equal go. The remaining lengths are go=2, sql=3, java=4, so ascending length order gives the shown list. The original words list keeps its original order and duplicate.

## Pitfalls and decisions

Sorting generally needs the relevant finite input before it can produce ordered results; it adds work and memory. distinct and sorting are stateful operations, meaning they need information beyond one isolated value. Avoid sorting an unbounded source before a limiting operation.

Official reference: [Java 24 API or language contract](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/Comparator.html).

## Questions and explained answers

<details>
<summary>1. Does sorted change words?</summary>

No. It describes a sorted result stream, leaving the source unchanged.

</details>

<details>
<summary>2. Does distinct use the length comparator?</summary>

No. It uses equality; two different words of the same length are not automatically equal.

</details>

<details>
<summary>3. Why is subtraction a poor custom comparator?</summary>

Integer subtraction can overflow and violate comparison behavior. Use comparingInt or Integer.compare for numeric fields.

</details>

## Stop/go

Explain the concept question in your own words, predict this example's result, and answer the questions before opening their answers. A coding attempt is optional. Record your explanation in [learner progress](../../learner-progress.md); compiled examples do not demonstrate comprehension.

**Next:** [18 — Primitive streams and arrays](../18-primitive-streams-and-arrays/README.md).
