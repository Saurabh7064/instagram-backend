# 01 — Functional thinking and pure functions

## Concept question

What makes a calculation functional, and what is a pure function?

## What you will learn

Explain a function as input → result, recognize outside changes, and separate calculation from effects.

**Time:** 10–15 minutes. **Prerequisite:** Basic Java variables, methods, and simple List values; no functional programming assumed.
**Artifact:** READY after recorded verification. **Understanding:** TODO; no learner attempt recorded.

## Understand the concept

Suppose a price needs to be doubled. A function receives a value and returns a result: 3 becomes 6 because 3 × 2 = 6. A pure function gives the same result for the same input values and does not change anything outside the calculation. A side effect is an observable change outside it, such as editing a caller-owned list, printing, writing a file, or sending a request.

Functional programming emphasizes describing results by combining functions. It is a style, not a requirement to use streams. Java supports this style alongside ordinary object-oriented code. A loop can implement a pure calculation; a lambda can perform side effects. Syntax alone does not decide purity.

Separate the calculation from its effects. Returning a price is different from printing or storing it. Pure calculations are easier to reuse and test because their answers do not depend on a changing clock, a hidden counter, or a database. Real programs still need effects; place them at explicit boundaries rather than pretending they disappear.

## Small Java example

The snippet below is included in [FunctionalConceptExamples.java](../../../../../../src/test/java/com/instagram/backend/interview/functionalprogramming/concepts/FunctionalConceptExamples.java#L47), method `lesson01`. Run that class's `main` in IntelliJ for checked examples. The file supplies the needed imports; no coding exercise is required.

```java
int input = 3;
int result = input * 2;
int sameResult = input * 2;
```

**Expected result:** result = 6; sameResult = 6; input remains 3.

## Read the syntax and result

`input * 2` calculates a value; assignment gives that value a local name. Both expressions calculate 3 × 2, so both results are 6. No caller-owned object or external system is changed. Printing these variables in main would be an explicit effect outside the calculation.

## Pitfalls and decisions

A method returning a new list can still modify objects inside the old list. Input preservation and deep immutability are different. Avoid hidden mutable dependencies when promising purity.

Official reference: [Java 24 API or language contract](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/package-summary.html).

## Questions and explained answers

<details>
<summary>1. Does a loop make a method impure?</summary>

No. Purity concerns observable behavior, not loop syntax. Local work that preserves caller-owned inputs can be pure.

</details>

<details>
<summary>2. Is returning the same number while printing pure?</summary>

No. Printing is an observable side effect even when the returned number is repeatable.

</details>

<details>
<summary>3. Why is reading the current time different from receiving a time argument?</summary>

A hidden clock reading changes independently of explicit inputs. Receiving time as an argument makes that dependency visible and lets a calculation be repeatable.

</details>

## Stop/go

Explain the concept question in your own words, predict this example's result, and answer the questions before opening their answers. A coding attempt is optional. Record your explanation in [learner progress](../../learner-progress.md); compiled examples do not demonstrate comprehension.

**Next:** [02 — Lambda expressions](../02-functions-as-values/README.md).
