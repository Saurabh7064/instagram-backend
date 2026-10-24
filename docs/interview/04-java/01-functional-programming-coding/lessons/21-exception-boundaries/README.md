# 21 — Exceptions at functional boundaries

## Concept question

Why do checked exceptions not fit every lambda?

## What you will learn

Match a lambda to its throws contract and choose a visible failure policy.

**Time:** 10–15 minutes. **Prerequisite:** [20 — Captured variables and effectively final](../20-lambda-capture/README.md).
**Artifact:** READY after recorded verification. **Understanding:** TODO; no learner attempt recorded.

## Understand the concept

A lambda must obey the abstract method it implements, including which checked exceptions it may declare. Checked exceptions are exception types Java requires callers to catch or declare. Standard Function.apply does not declare IOException, so a lambda directly calling a file operation that throws IOException cannot simply let that checked exception escape through Function.

There are several reasonable policies: catch and handle the failure meaningfully, wrap it in an appropriate unchecked exception while preserving the cause, define a custom functional interface whose method declares the checked exception, or use an ordinary loop/method where try/catch is clearer. Do not turn every failure into an empty value unless the business contract really treats failure as absence.

The example uses Integer.parseInt, which can throw NumberFormatException, an unchecked exception. It therefore fits `Function<String,Integer>`. The valid text "42" becomes integer 42; invalid text must still fail or be handled explicitly. A stream does not automatically retry or isolate a failing element.

## Small Java example

The snippet below is included in [FunctionalConceptExamples.java](../../../../../../src/test/java/com/instagram/backend/interview/functionalprogramming/concepts/FunctionalConceptExamples.java#L230), method `lesson21`. Run that class's `main` in IntelliJ for checked examples. The file supplies the needed imports; no coding exercise is required.

```java
Function<String, Integer> parse = Integer::parseInt;
int result = parse.apply("42");
```

**Expected result:** result = 42; malformed numeric input would throw NumberFormatException.

## Read the syntax and result

parseInt interprets the decimal characters 4 and 2 as the number forty-two. The Function contract allows unchecked failures without a throws declaration. That compile-time compatibility is not a promise that every input succeeds.

## Pitfalls and decisions

Wrapping checked failures in RuntimeException just to make syntax compile can hide context. Keep the original cause and identify the failed operation. Resource cleanup still matters when exceptions occur.

Official reference: [Java 24 API or language contract](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/function/Function.html).

## Questions and explained answers

<details>
<summary>1. Why can parseInt fit Function?</summary>

Its NumberFormatException is unchecked, so Function.apply does not need a checked throws declaration for it.

</details>

<details>
<summary>2. Does a Function permit an arbitrary checked IOException to escape?</summary>

No. Its abstract apply method does not declare it. Handle it or choose a compatible custom interface/ordinary method.

</details>

<details>
<summary>3. Should failure always become Optional.empty?</summary>

No. No result and an operational failure are different outcomes unless the contract deliberately combines them.

</details>

## Stop/go

Explain the concept question in your own words, predict this example's result, and answer the questions before opening their answers. A coding attempt is optional. Record your explanation in [learner progress](../../learner-progress.md); compiled examples do not demonstrate comprehension.

**Next:** [22 — Side effects, non-interference, and peek](../22-side-effects-and-peek/README.md).
