# 12 — Optional: a result may be absent

## Concept question

What is Optional, and how do map, flatMap, orElse, and orElseGet apply to it?

## What you will learn

Model presence explicitly, transform a present value, and select a suitable absence policy.

**Time:** 10–15 minutes. **Prerequisite:** [11 — Terminal operations: questions and results](../11-terminal-operations/README.md).
**Artifact:** READY after recorded verification. **Understanding:** TODO; no learner attempt recorded.

## Understand the concept

A lookup may have a result or no result. `Optional<T>` makes that possibility explicit in a return value. `Optional.of(value)` requires a non-null value, `ofNullable(value)` accepts a possibly null value, and empty() represents no value. An Optional variable itself should not be null.

For Optional.of("java"), map(String::length) produces Optional containing 4. If the Optional were empty, there would be no String to transform. If your transformation already returns an Optional, use flatMap to avoid `Optional<Optional<R>>`. The same flatten-one-layer idea appears in streams, although the container and API are different.

Use orElse for an already available fallback value, orElseGet for a Supplier used only when absent. The argument expression to orElse is evaluated before the call, even when a value is present. Choose orElseThrow when absence should fail explicitly. Avoid get() unless presence is established; unconditional get turns a modeled absence back into an exception surprise.

## Small Java example

The snippet below is included in [FunctionalConceptExamples.java](../../../../../../src/test/java/com/instagram/backend/interview/functionalprogramming/concepts/FunctionalConceptExamples.java#L145), method `lesson12`. Run that class's `main` in IntelliJ for checked examples. The file supplies the needed imports; no coding exercise is required.

```java
Optional<String> found = Optional.of("java");
int length = found.map(String::length).orElse(0);
String fallback = Optional.<String>empty().orElseGet(() -> "guest");
Optional<Integer> nestedResult = found.flatMap(word -> Optional.of(word.length()));
```

**Expected result:** length = 4; fallback = "guest"; nestedResult = Optional[4].

## Read the syntax and result

The present word java contains four characters, so map wraps 4 and orElse selects it. The separate empty value needs a fallback, so its supplier provides guest. The final mapper already returns `Optional<Integer>`; flatMap keeps one Optional layer.

## Pitfalls and decisions

Optional is mainly a return-type tool for meaningful absence, not a universal replacement for every field or parameter. Optional.map converts a null mapping result into empty; do not assume every container API handles null the same way.

Official reference: [Java 24 API or language contract](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/Optional.html).

## Questions and explained answers

<details>
<summary>1. Why not call get immediately?</summary>

get throws when absent. Transform or choose an explicit fallback/failure policy instead.

</details>

<details>
<summary>2. When does orElseGet compute its fallback?</summary>

Only when the Optional is empty. orElse evaluates its argument expression before the method call regardless of presence.

</details>

<details>
<summary>3. What would map do with an Optional-returning function?</summary>

It would produce nested Optional wrappers. flatMap accepts that Optional-bearing function and keeps one layer.

</details>

## Stop/go

Explain the concept question in your own words, predict this example's result, and answer the questions before opening their answers. A coding attempt is optional. Record your explanation in [learner progress](../../learner-progress.md); compiled examples do not demonstrate comprehension.

**Next:** [13 — reduce: combine values into one result](../13-reduce/README.md).
