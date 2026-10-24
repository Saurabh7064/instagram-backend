# 20 — Captured variables and effectively final

## Concept question

Why can a lambda read a local variable but not reassign it?

## What you will learn

Explain captured local values and distinguish final references from immutable objects.

**Time:** 10–15 minutes. **Prerequisite:** [19 — Function composition](../19-function-composition/README.md).
**Artifact:** READY after recorded verification. **Understanding:** TODO; no learner attempt recorded.

## Understand the concept

A lambda sometimes uses a value declared outside its body. That is capture: the behavior refers to an enclosing local variable. Java requires a captured local variable to be final or effectively final. Effectively final means it is not reassigned after its initial value is established.

Here minimumLength is 4 and is never reassigned. The predicate can use that captured threshold to test "java". Its length is 4, and 4 >= 4 is true. Writing minimumLength++ later would make this local variable unsuitable for capture and cause a compile error; it would not silently update a shared captured integer cell.

A final reference is different from an immutable object. A captured ArrayList reference can remain unchanged while the list contents are edited. That may compile, but it introduces shared state and possible concurrency problems. Do not wrap a counter in an array merely to bypass the rule when a reduction expresses the result.

## Small Java example

The snippet below is included in [FunctionalConceptExamples.java](../../../../../../src/test/java/com/instagram/backend/interview/functionalprogramming/concepts/FunctionalConceptExamples.java#L222), method `lesson20`. Run that class's `main` in IntelliJ for checked examples. The file supplies the needed imports; no coding exercise is required.

```java
int minimumLength = 4;
Predicate<String> longEnough = word -> word.length() >= minimumLength;
boolean accepted = longEnough.test("java");
```

**Expected result:** accepted = true.

## Read the syntax and result

The predicate reads the captured threshold 4. java has length 4, so the comparison 4 >= 4 succeeds. The local reference is not reassigned; no mutable counter is needed.

## Pitfalls and decisions

The rule for captured local variables is not the same as a blanket ban on accessing object fields. Mutable fields can be accessed but need explicit ownership and thread-safety reasoning. Capturing this can also keep an owning object reachable longer than expected.

Official reference: [Java 24 API or language contract](https://docs.oracle.com/javase/specs/jls/se24/html/jls-15.html#jls-15.27).

## Questions and explained answers

<details>
<summary>1. Must I write the final keyword?</summary>

No. A local that is not reassigned is effectively final and can be captured.

</details>

<details>
<summary>2. Does a final List reference make its contents immutable?</summary>

No. It stops reassignment of the reference; the referenced object may still allow mutation.

</details>

<details>
<summary>3. Why avoid an array-wrapped counter in map?</summary>

It disguises shared mutable state, depends on callbacks being invoked, and can fail under parallel execution or optimization.

</details>

## Stop/go

Explain the concept question in your own words, predict this example's result, and answer the questions before opening their answers. A coding attempt is optional. Record your explanation in [learner progress](../../learner-progress.md); compiled examples do not demonstrate comprehension.

**Next:** [21 — Exceptions at functional boundaries](../21-exception-boundaries/README.md).
