# 28 — Custom collector contracts

## Concept question

What do supplier, accumulator, combiner, and finisher mean in a collector?

## What you will learn

Recognize a collection recipe and the ownership rules that make partial results safe to combine.

**Time:** 10–15 minutes. **Prerequisite:** [27 — Gatherers: custom intermediate transformations](../27-gatherers/README.md).
**Artifact:** READY after recorded verification. **Understanding:** TODO; no learner attempt recorded.

## Understand the concept

Collectors such as joining already solve common tasks. A custom collector is useful only when an existing recipe does not express the needed result. Its supplier creates a fresh mutable result container; its accumulator adds one input to that container; its combiner merges independently accumulated containers; its finisher converts the container into the returned result.

Those names describe lifecycle roles in one primary abstraction, not four separate algorithms to memorize. For words ["java", "go"], a StringBuilder container can accumulate the text and finish as "javago". The standard joining collector is simpler for this actual requirement; the custom version below exists to expose the contract.

Fresh containers matter. Returning the same shared StringBuilder from the supplier breaks the isolation expected by the collector. A correct combiner must preserve the intended meaning for partial results. Characteristics are optional declarations about properties such as concurrent accumulation or an identity finisher; never declare them just to request speed.

## Small Java example

The snippet below is included in [FunctionalConceptExamples.java](../../../../../../src/test/java/com/instagram/backend/interview/functionalprogramming/concepts/FunctionalConceptExamples.java#L298), method `lesson28`. Run that class's `main` in IntelliJ for checked examples. The file supplies the needed imports; no coding exercise is required.

```java
Collector<String, StringBuilder, String> joining = Collector.of(
        StringBuilder::new,
        (buffer, word) -> buffer.append(word),
        (left, right) -> left.append(right),
        StringBuilder::toString);
String result = List.of("java", "go").stream().collect(joining);
```

**Expected result:** result = "javago".

## Read the syntax and result

The supplier creates owned builders. Appending the two words joins java and go into javago. The combiner can append a later partial builder to an earlier one; the finisher returns the String representation. The accumulator does not return a separate result value, while the combiner must return its merged container.

## Pitfalls and decisions

Prefer Collectors.joining() for this task. Incorrect characteristics or a shared supplier container can make parallel collection unsafe even if a sequential example happens to pass. Do not use mutable reduce as a shortcut around this contract.

Official reference: [Java 24 API or language contract](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Collector.html).

## Questions and explained answers

<details>
<summary>1. Why must the supplier return a fresh container?</summary>

Independent partial accumulations must not unexpectedly share the same mutable object.

</details>

<details>
<summary>2. What distinguishes accumulator from combiner?</summary>

The accumulator adds one source value to a container; the combiner merges two accumulated containers.

</details>

<details>
<summary>3. Would this custom collector be the best production choice for joining words?</summary>

No. Collectors.joining() already states the requirement clearly and supplies the tested recipe.

</details>

## Stop/go

Explain the concept question in your own words, predict this example's result, and answer the questions before opening their answers. A coding attempt is optional. Record your explanation in [learner progress](../../learner-progress.md); compiled examples do not demonstrate comprehension.

**Next:** [Course coverage and review](../../README.md).
