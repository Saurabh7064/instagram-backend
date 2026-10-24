# 02 — Lambda expressions

## Concept question

What is a lambda, and how do I read the arrow syntax?

## What you will learn

Read parameter → body, distinguish defining behavior from invoking it, and recognize expression and block bodies.

**Time:** 10–15 minutes. **Prerequisite:** [01 — Functional thinking and pure functions](../01-pure-transformations/README.md).
**Artifact:** READY after recorded verification. **Understanding:** TODO; no learner attempt recorded.

## Understand the concept

Sometimes a method needs a calculation supplied by its caller. Rather than naming a separate class for every calculation, Java lets you write a small function-like expression where behavior is required. A lambda expression supplies that behavior. The arrow separates parameters from the body: `value -> value * 2` means “receive value, produce value times two.”

The variable type tells Java which inputs and output the behavior must support. `Function<Integer, Integer>` means receive an Integer and return an Integer. Angle brackets name those types; they are not values passed to the calculation. `Function` is a functional interface, an interface describing one required operation; the next lesson explains this contract in detail.

Assigning a lambda saves behavior; it does not run its body. Calling `apply(3)` invokes this Function with 3. An expression body returns its expression automatically. A block body uses braces and an explicit return when a result is required: `value -> { return value * 2; }`. Empty parameters use `()`, and multiple parameters use parentheses. A lambda is not a thread and does not imply asynchronous execution.

## Small Java example

The snippet below is included in [FunctionalConceptExamples.java](../../../../../../src/test/java/com/instagram/backend/interview/functionalprogramming/concepts/FunctionalConceptExamples.java#L55), method `lesson02`. Run that class's `main` in IntelliJ for checked examples. The file supplies the needed imports; no coding exercise is required.

```java
Function<Integer, Integer> doubleIt = value -> value * 2;
int result = doubleIt.apply(3);
```

**Expected result:** result = 6.

## Read the syntax and result

`doubleIt` holds one-input/one-result behavior. `apply(3)` supplies 3; the body calculates 3 × 2 = 6. Java infers the lambda parameter type from `Function<Integer, Integer>`. The variable declaration alone produces no numeric result.

## Pitfalls and decisions

`return` inside a lambda returns from that lambda invocation, not from the surrounding method. A lambda requires a compatible target type; `var f = x -> x * 2` lacks one and does not compile.

Official reference: [Java 24 API or language contract](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/function/Function.html).

## Questions and explained answers

<details>
<summary>1. Does assigning doubleIt calculate six?</summary>

No. Assignment supplies behavior. The later apply(3) call calculates six.

</details>

<details>
<summary>2. What does the arrow mean?</summary>

Parameters on the left are inputs; the body on the right describes what to do with those inputs.

</details>

<details>
<summary>3. Can I replace the expression body with braces?</summary>

Yes, use value -> { return value * 2; }. A value-returning block needs a return on every normal completing path.

</details>

## Stop/go

Explain the concept question in your own words, predict this example's result, and answer the questions before opening their answers. A coding attempt is optional. Record your explanation in [learner progress](../../learner-progress.md); compiled examples do not demonstrate comprehension.

**Next:** [03 — Functional interfaces: Function and Predicate](../03-functional-interfaces/README.md).
