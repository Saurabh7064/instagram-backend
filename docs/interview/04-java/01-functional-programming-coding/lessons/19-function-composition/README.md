# 19 — Function composition

## Concept question

What do andThen and compose mean?

## What you will learn

Combine reusable transformations in a deliberate order and check compatible types.

**Time:** 10–15 minutes. **Prerequisite:** [18 — Primitive streams and arrays](../18-primitive-streams-and-arrays/README.md).
**Artifact:** READY after recorded verification. **Understanding:** TODO; no learner attempt recorded.

## Understand the concept

Two small functions may be useful alone and together. Composition builds new behavior by feeding one function's result into another. If doubleIt multiplies by two and addThree adds three, then doubling 3 and adding three gives (3 × 2) + 3 = 9. Adding three first and doubling gives (3 + 3) × 2 = 12. Function order matters.

`f.andThen(g)` means apply f, then g. `f.compose(g)` means apply g first, then f. Both create composed behavior; apply(input) invokes it. The intermediate result type must match the next input type. You can therefore compose a String-to-length function with an integer-to-label function if their types connect.

Predicates also compose: and, or, and negate build boolean rules. and/or short circuit rather than invoking both predicates unconditionally. Keep actions and transformations explicit; composing a function that secretly mutates shared data preserves its side effect, not purity.

## Small Java example

The snippet below is included in [FunctionalConceptExamples.java](../../../../../../src/test/java/com/instagram/backend/interview/functionalprogramming/concepts/FunctionalConceptExamples.java#L213), method `lesson19`. Run that class's `main` in IntelliJ for checked examples. The file supplies the needed imports; no coding exercise is required.

```java
Function<Integer, Integer> doubleIt = value -> value * 2;
Function<Integer, Integer> addThree = value -> value + 3;
int after = doubleIt.andThen(addThree).apply(3);
int before = doubleIt.compose(addThree).apply(3);
```

**Expected result:** after = 9; before = 12.

## Read the syntax and result

andThen calculates 3 × 2 = 6, then 6 + 3 = 9. compose calculates 3 + 3 = 6, then 6 × 2 = 12. Naming which function runs first prevents confusing the identical-looking intermediate 6 with identical behavior.

## Pitfalls and decisions

Changing order can change meaning even when both functions accept the same type. Avoid long composed expressions when named local functions make the intended stages easier to understand.

Official reference: [Java 24 API or language contract](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/function/Function.html).

## Questions and explained answers

<details>
<summary>1. Which function runs first in f.andThen(g)?</summary>

f runs first; its returned value becomes g input.

</details>

<details>
<summary>2. Which runs first in f.compose(g)?</summary>

g runs first, then f receives its result.

</details>

<details>
<summary>3. Can any two Functions be composed?</summary>

Their types must connect: the earlier result must be acceptable to the later input. Shared naming alone does not make incompatible types fit.

</details>

## Stop/go

Explain the concept question in your own words, predict this example's result, and answer the questions before opening their answers. A coding attempt is optional. Record your explanation in [learner progress](../../learner-progress.md); compiled examples do not demonstrate comprehension.

**Next:** [20 — Captured variables and effectively final](../20-lambda-capture/README.md).
