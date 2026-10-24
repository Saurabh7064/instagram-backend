# 03 — Functional interfaces: Function and Predicate

## Concept question

How does Java know which inputs and result a lambda must have?

## What you will learn

Explain the single abstract method contract and choose between a transformation and a boolean test.

**Time:** 10–15 minutes. **Prerequisite:** [02 — Lambda expressions](../02-functions-as-values/README.md).
**Artifact:** READY after recorded verification. **Understanding:** TODO; no learner attempt recorded.

## Understand the concept

A functional interface is an interface with one abstract operation to implement. An abstract operation declares a method without supplying its implementation. Java matches a lambda to that operation. Default methods already have implementations, and static methods belong to the interface, so they do not add another required abstract operation. The annotation `@FunctionalInterface` asks the compiler to check this intention; the annotation is helpful but not required.

Use `Function<T,R>` when behavior accepts a T and returns an R. Its invocation method is `apply`. Use `Predicate<T>` when behavior accepts a T and returns boolean. Its invocation method is `test`. Here T and R are placeholders for actual Java types: `Function<String,Integer>` receives text and returns its length, while `Predicate<String>` answers a question about text.

A custom interface can serve the same purpose: `interface WordRule { boolean test(String value); }`. The compiler checks the shape, not whether the body is pure, fast, or safe. Function and Predicate are standard reusable contracts, not special requirements that all functional code must use.

## Small Java example

The snippet below is included in [FunctionalConceptExamples.java](../../../../../../src/test/java/com/instagram/backend/interview/functionalprogramming/concepts/FunctionalConceptExamples.java#L62), method `lesson03`. Run that class's `main` in IntelliJ for checked examples. The file supplies the needed imports; no coding exercise is required.

```java
Function<String, Integer> length = text -> text.length();
Predicate<String> nonBlank = text -> !text.isBlank();
int size = length.apply("java");
boolean accepted = nonBlank.test("java");
```

**Expected result:** size = 4; accepted = true.

## Read the syntax and result

"java" has four characters, so the Function returns 4. It contains non-whitespace characters, so isBlank() returns false and ! changes that to true. The input types match, but the contracts and invocation names differ.

## Pitfalls and decisions

Functional does not mean only one declared method of any kind. The rule concerns abstract operations, with the language rules excluding methods corresponding to public Object methods. Do not assume a standard function type guarantees a pure body.

Official reference: [Java 24 API or language contract](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/function/package-summary.html).

## Questions and explained answers

<details>
<summary>1. Why is Predicate more specific than Function here?</summary>

Predicate names a yes/no decision and returns primitive boolean through test. Function describes a general returned value through apply.

</details>

<details>
<summary>2. Does a default method prevent an interface being functional?</summary>

No. It already has an implementation and is not another abstract operation to supply.

</details>

<details>
<summary>3. Does @FunctionalInterface make a callback pure?</summary>

No. It checks the interface shape, not side effects or runtime behavior.

</details>

## Stop/go

Explain the concept question in your own words, predict this example's result, and answer the questions before opening their answers. A coding attempt is optional. Record your explanation in [learner progress](../../learner-progress.md); compiled examples do not demonstrate comprehension.

**Next:** [04 — Method references](../04-method-references/README.md).
