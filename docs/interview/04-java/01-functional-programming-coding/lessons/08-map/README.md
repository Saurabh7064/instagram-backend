# 08 — map: transform one value into another

## Concept question

What does map do, and is it related to a Map collection?

## What you will learn

Explain the T → R type change and distinguish transforming values from selecting or indexing them.

**Time:** 10–15 minutes. **Prerequisite:** [07 — filter: keep matching values](../07-filter/README.md).
**Artifact:** READY after recorded verification. **Understanding:** TODO; no learner attempt recorded.

## Understand the concept

Suppose we need each word's length rather than the words themselves. "java" becomes 4 and "go" becomes 2, so the requested result is [4, 2]. This is a transformation: one input value describes one output value. `map` accepts a Function that describes that conversion.

The type shape is `Stream<T>` → `Stream<R>`, using a function T → R. For this example, T is String and R is Integer. The stream method named map is unrelated to creating a java.util.Map of keys and values; it does not require keys or produce a dictionary automatically.

Choose map when the result should keep the input's logical one-to-one shape but change what each value represents: a post to a response object, a String to a length, or a price to an adjusted price. A mapper returning a collection produces a stream whose elements are collections; map does not automatically open those containers. That distinction motivates flatMap in the next lesson.

## Small Java example

The snippet below is included in [FunctionalConceptExamples.java](../../../../../../src/test/java/com/instagram/backend/interview/functionalprogramming/concepts/FunctionalConceptExamples.java#L107), method `lesson08`. Run that class's `main` in IntelliJ for checked examples. The file supplies the needed imports; no coding exercise is required.

```java
List<String> words = List.of("java", "go");
List<Integer> lengths = words.stream()
        .map(String::length)
        .toList();
```

**Expected result:** lengths = [4, 2].

## Read the syntax and result

java contains four characters and go contains two, so their returned lengths are 4 and 2 in that order. String::length is the conversion Function. map describes the transformed stream; toList gathers it. The source still contains Strings, while the result contains Integers.

## Pitfalls and decisions

Returning null from a mapper does not mean “discard this value”; normal map can produce a null element. Do not rely on mapper side effects or exact invocation counts, because a terminal operation may allow optimizations. mapToInt is a primitive specialization taught later.

Official reference: [Java 24 API or language contract](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Stream.html).

## Questions and explained answers

<details>
<summary>1. Does map create key/value entries?</summary>

No. Stream.map transforms elements. java.util.Map is a different API for key/value associations.

</details>

<details>
<summary>2. Why is the result `List<Integer>`?</summary>

The conversion takes each String and returns its integer length, changing the element type.

</details>

<details>
<summary>3. What happens if a mapper returns a List?</summary>

That List becomes one mapped element. To emit its contents instead, use the flattening model in flatMap.

</details>

## Stop/go

Explain the concept question in your own words, predict this example's result, and answer the questions before opening their answers. A coding attempt is optional. Record your explanation in [learner progress](../../learner-progress.md); compiled examples do not demonstrate comprehension.

**Next:** [09 — flatMap: flatten nested results](../09-flatmap/README.md).
