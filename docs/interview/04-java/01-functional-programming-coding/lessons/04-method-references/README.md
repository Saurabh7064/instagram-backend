# 04 — Method references

## Concept question

What does :: mean, and when can it replace a lambda?

## What you will learn

Read a reference to an existing method or constructor and check its input/output compatibility.

**Time:** 10–15 minutes. **Prerequisite:** [03 — Functional interfaces: Function and Predicate](../03-functional-interfaces/README.md).
**Artifact:** READY after recorded verification. **Understanding:** TODO; no learner attempt recorded.

## Understand the concept

A lambda can simply call an existing method. When it does, a method reference can name that method directly instead of repeating a forwarding lambda. `String::length` can supply the same one-String-to-one-integer behavior as `text -> text.length()` in a compatible context. The `::` syntax refers to behavior; it does not call it immediately.

There are four useful forms. `Integer::parseInt` names a static method. `prefix::concat` names an instance method on one already chosen object. `String::length` names an instance method where the incoming String supplies the receiver. `ArrayList::new` names a constructor. Match each form to the required parameter and return types rather than mechanically replacing every lambda.

A method reference is useful when the existing name communicates intent. Keep a lambda when it makes a condition, arithmetic expression, or argument adaptation clearer. For example, `text -> text.length() > 3` adds a comparison and cannot be reduced to `String::length` as a Predicate.

## Small Java example

The snippet below is included in [FunctionalConceptExamples.java](../../../../../../src/test/java/com/instagram/backend/interview/functionalprogramming/concepts/FunctionalConceptExamples.java#L71), method `lesson04`. Run that class's `main` in IntelliJ for checked examples. The file supplies the needed imports; no coding exercise is required.

```java
Function<String, Integer> length = String::length;
Function<String, Integer> parse = Integer::parseInt;
int size = length.apply("java");
int number = parse.apply("42");
```

**Expected result:** size = 4; number = 42.

## Read the syntax and result

For length, the incoming "java" is the String whose length is requested, giving 4. For parse, the incoming "42" is an argument to the static parseInt method, giving integer 42. The shared Function contract does not make those receiver rules identical.

## Pitfalls and decisions

Bound references such as prefix::concat require a usable receiver when the reference is created. Method overloading still needs type resolution. Neither method references nor lambdas automatically provide null checks.

Official reference: [Java 24 API or language contract](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/function/package-summary.html).

## Questions and explained answers

<details>
<summary>1. Is String::length an immediate call?</summary>

No. It supplies behavior to a compatible target. Calling apply later invokes it with a String receiver.

</details>

<details>
<summary>2. Why does Integer::parseInt fit `Function<String,Integer>`?</summary>

It takes a String and returns int, which can be boxed to Integer for the Function result.

</details>

<details>
<summary>3. Should every lambda become a method reference?</summary>

No. Use the form that explains the behavior clearly. Comparisons and argument transformations often read better as lambdas.

</details>

## Stop/go

Explain the concept question in your own words, predict this example's result, and answer the questions before opening their answers. A coding attempt is optional. Record your explanation in [learner progress](../../learner-progress.md); compiled examples do not demonstrate comprehension.

**Next:** [05 — Supplier, Consumer, and operator shapes](../05-suppliers-consumers-operators/README.md).
