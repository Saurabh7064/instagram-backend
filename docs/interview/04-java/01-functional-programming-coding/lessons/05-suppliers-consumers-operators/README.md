# 05 — Supplier, Consumer, and operator shapes

## Concept question

Which functional interface should I use when there is no input, no result, or the same type on both sides?

## What you will learn

Distinguish producing, performing an action, and transforming within one type.

**Time:** 10–15 minutes. **Prerequisite:** [04 — Method references](../04-method-references/README.md).
**Artifact:** READY after recorded verification. **Understanding:** TODO; no learner attempt recorded.

## Understand the concept

A `Supplier<T>` receives no argument and returns a T through `get()`. It is useful when something should be created or computed on demand. A `Consumer<T>` receives a T and returns no value through `accept(T)`. It describes an action, such as adding to an explicitly owned buffer or writing output. A `UnaryOperator<T>` receives and returns the same type through `apply(T)`; it is a more specific `Function<T,T>`.

These contracts describe shape, not timing or purity. A supplier may create a fresh object or return a shared one. A consumer often has a side effect. An operator may mutate a supplied object unless its contract says otherwise. Make those ownership decisions explicit.

You also encounter two-input relatives. `BiFunction<T,U,R>` takes two inputs and returns a result; `BinaryOperator<T>` takes two T values and returns a T. Their details become useful during reduction and composition; they do not mean two threads or two automatic executions.

## Small Java example

The snippet below is included in [FunctionalConceptExamples.java](../../../../../../src/test/java/com/instagram/backend/interview/functionalprogramming/concepts/FunctionalConceptExamples.java#L80), method `lesson05`. Run that class's `main` in IntelliJ for checked examples. The file supplies the needed imports; no coding exercise is required.

```java
Supplier<String> defaultName = () -> "guest";
StringBuilder buffer = new StringBuilder();
Consumer<String> append = buffer::append;
UnaryOperator<String> clean = String::trim;
append.accept(clean.apply(" java "));
String name = defaultName.get();
```

**Expected result:** buffer contains "java"; name = "guest".

## Read the syntax and result

clean removes the surrounding ordinary spaces from " java ". append deliberately changes the owned StringBuilder to contain "java". defaultName has no input and supplies "guest" on get(). The example separates a returned transformation from an action that changes a buffer.

## Pitfalls and decisions

Consumer returning void does not imply success or thread safety. Supplier does not promise caching or a new object per call. Choose contracts for their actual inputs and result, not because their names sound functional.

Official reference: [Java 24 API or language contract](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/function/package-summary.html).

## Questions and explained answers

<details>
<summary>1. What is the input to defaultName.get()?</summary>

There is no input argument. Supplier represents no-input behavior that supplies a result.

</details>

<details>
<summary>2. Why is append a Consumer?</summary>

It accepts a String for an action and does not return a business result. Its buffer mutation is an explicit side effect.

</details>

<details>
<summary>3. How is `UnaryOperator<String>` related to `Function<String,String>`?</summary>

It is the specialized same-input-and-output-type form, still invoked with apply.

</details>

## Stop/go

Explain the concept question in your own words, predict this example's result, and answer the questions before opening their answers. A coding attempt is optional. Record your explanation in [learner progress](../../learner-progress.md); compiled examples do not demonstrate comprehension.

**Next:** [06 — What a stream is](../06-stream-model/README.md).
