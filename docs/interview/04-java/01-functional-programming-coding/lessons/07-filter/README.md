# 07 — filter: keep matching values

## Concept question

What does filter do, and why does it need a Predicate?

## What you will learn

Choose values using a boolean test while preserving their values and source ownership.

**Time:** 10–15 minutes. **Prerequisite:** [06 — What a stream is](../06-stream-model/README.md).
**Artifact:** READY after recorded verification. **Understanding:** TODO; no learner attempt recorded.

## Understand the concept

Suppose we want words longer than three characters. We are not changing each word into a different value; we are deciding whether each belongs in the result. `filter` accepts a Predicate, the yes/no behavior taught earlier. A true result keeps a value; false excludes it from the output.

For words ["java", "go", "streams"], the lengths are 4, 2, and 7. Only 4 and 7 exceed 3, so the requested result is ["java", "streams"]. Excluding "go" from the stream result does not remove it from the source list. filter narrows which values continue; it does not convert a String into its length.

Filtering is an intermediate operation. Chaining toList() requests a gathered result. The predicate should describe a stable test and avoid editing the source. Changing the source while it is being read can invalidate the pipeline's assumptions.

## Small Java example

The snippet below is included in [FunctionalConceptExamples.java](../../../../../../src/test/java/com/instagram/backend/interview/functionalprogramming/concepts/FunctionalConceptExamples.java#L98), method `lesson07`. Run that class's `main` in IntelliJ for checked examples. The file supplies the needed imports; no coding exercise is required.

```java
List<String> words = List.of("java", "go", "streams");
List<String> result = words.stream()
        .filter(word -> word.length() > 3)
        .toList();
```

**Expected result:** result = [java, streams].

## Read the syntax and result

`word.length() > 3` is the keep rule. java has length 4 and streams has length 7, so both qualify; go has length 2 and does not. The output elements are still Strings. toList() obtains an unmodifiable result without editing words.

## Pitfalls and decisions

A null word cannot receive a length() call. When null is permitted by the input contract, filter with Objects::nonNull before dereferencing; do not silently invent a null policy.

Official reference: [Java 24 API or language contract](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Stream.html).

## Questions and explained answers

<details>
<summary>1. Does filter shorten "streams"?</summary>

No. It either keeps the existing value or excludes it. Transformation belongs to map.

</details>

<details>
<summary>2. Why does the predicate return boolean?</summary>

filter needs a keep/discard decision, so true means keep and false means discard.

</details>

<details>
<summary>3. Is go removed from words?</summary>

No. It is absent from the result, but the original list still holds all three strings.

</details>

## Stop/go

Explain the concept question in your own words, predict this example's result, and answer the questions before opening their answers. A coding attempt is optional. Record your explanation in [learner progress](../../learner-progress.md); compiled examples do not demonstrate comprehension.

**Next:** [08 — map: transform one value into another](../08-map/README.md).
