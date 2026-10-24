# 13 — reduce: combine values into one result

## Concept question

What is reduce, and what makes a valid reduction?

## What you will learn

Choose a neutral starting value and an associative combination rule.

**Time:** 10–15 minutes. **Prerequisite:** [12 — Optional: a result may be absent](../12-optional/README.md).
**Artifact:** READY after recorded verification. **Understanding:** TODO; no learner attempt recorded.

## Understand the concept

To add [2, 4, 6], combine the values into one answer: 2 + 4 + 6 = 12. A reduction combines many values into one value. `reduce(identity, accumulator)` starts with a neutral identity and combines a partial result with another value. For addition, the identity is 0 because adding 0 changes nothing. A `BinaryOperator<T>` supplies the same-type two-input combination.

The combination must be associative: regrouping must not change the result. For these bounded integer values, (2 + 4) + 6 and 2 + (4 + 6) both equal 12. This matters when work is split into partial results. Subtraction is not associative, so replacing the addition operator with subtraction is not a valid general parallel reduction.

The overload without an identity returns Optional because empty input has no first value to start from. The three-argument overload separates result type and combination of partial results; use it only when you can explain how its accumulator and combiner agree. For mutable collection construction, collect is usually the clearer abstraction.

## Small Java example

The snippet below is included in [FunctionalConceptExamples.java](../../../../../../src/test/java/com/instagram/backend/interview/functionalprogramming/concepts/FunctionalConceptExamples.java#L154), method `lesson13`. Run that class's `main` in IntelliJ for checked examples. The file supplies the needed imports; no coding exercise is required.

```java
List<Integer> values = List.of(2, 4, 6);
int total = values.stream().reduce(0, Integer::sum);
Optional<Integer> withoutIdentity = values.stream().reduce(Integer::sum);
```

**Expected result:** total = 12; withoutIdentity = Optional[12].

## Read the syntax and result

0 is neutral, and 0 + 2 + 4 + 6 = 12. Integer::sum supplies addition of two integers. The no-identity form wraps the answer to represent the possibility of an empty source.

## Pitfalls and decisions

Do not use a shared mutable ArrayList as reduce identity. Parallel partitions may share it, violating reduction assumptions. Floating-point addition can change slightly with grouping, so numerical reproducibility deserves a separate policy.

Official reference: [Java 24 API or language contract](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Stream.html).

## Questions and explained answers

<details>
<summary>1. Why is 0 the identity for addition?</summary>

Adding 0 leaves a value unchanged, so starting or merging with that identity does not bias the sum.

</details>

<details>
<summary>2. Why is subtraction unsuitable?</summary>

Regrouping changes the answer. Parallel reduction may combine partial results in different groupings.

</details>

<details>
<summary>3. Why can the no-identity result be empty?</summary>

An empty source supplies no value from which to begin, and no identity was supplied.

</details>

## Stop/go

Explain the concept question in your own words, predict this example's result, and answer the questions before opening their answers. A coding attempt is optional. Record your explanation in [learner progress](../../learner-progress.md); compiled examples do not demonstrate comprehension.

**Next:** [14 — collect and result ownership](../14-collect-and-modifiability/README.md).
