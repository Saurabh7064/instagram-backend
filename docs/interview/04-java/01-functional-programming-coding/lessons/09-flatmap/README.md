# 09 — flatMap: flatten nested results

## Concept question

What is flatMap, and when should I use it instead of map?

## What you will learn

Distinguish one-to-one conversion from converting to zero or more values and removing one container layer.

**Time:** 10–15 minutes. **Prerequisite:** [08 — map: transform one value into another](../08-map/README.md).
**Artifact:** READY after recorded verification. **Understanding:** TODO; no learner attempt recorded.

## Understand the concept

Suppose groups contains [["java", "go"], [], ["sql"]], and we want one list ["java", "go", "sql"]. The requested result combines the contents of the three inner lists: two values, zero values, and one value, giving 2 + 0 + 1 = 3 values. We want the contained words rather than three list objects.

`map(List::stream)` converts each inner list into one Stream object, producing a `Stream<Stream<String>>`. `flatMap(List::stream)` also obtains each inner stream, then emits its contents into one `Stream<String>`. The mapper shape is T → `Stream<R>`, followed by flattening one level. Empty inner streams contribute no values.

Use map when each input becomes one returned element. Use flatMap when each input provides zero or more elements that belong directly in the output: posts to all their tags, orders to line items, or groups to members. It does not recursively flatten arbitrarily deep structures, and it does not create parallel execution.

## Small Java example

The snippet below is included in [FunctionalConceptExamples.java](../../../../../../src/test/java/com/instagram/backend/interview/functionalprogramming/concepts/FunctionalConceptExamples.java#L116), method `lesson09`. Run that class's `main` in IntelliJ for checked examples. The file supplies the needed imports; no coding exercise is required.

```java
List<List<String>> groups = List.of(
        List.of("java", "go"), List.of(), List.of("sql"));
List<String> words = groups.stream()
        .flatMap(List::stream)
        .toList();
```

**Expected result:** words = [java, go, sql].

## Read the syntax and result

List::stream exposes each inner list as a stream of its words. flatMap removes that stream layer, so the result contains Strings rather than Lists or Streams. The empty middle list supplies nothing. The ordered lists and sequential pipeline retain java, go, sql order.

## Pitfalls and decisions

If groups itself or an inner list can be null, define a policy before invoking methods on it. flatMap does not automatically remove duplicate words. Set collection or distinct is a separate decision.

Official reference: [Java 24 API or language contract](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Stream.html).

## Questions and explained answers

<details>
<summary>1. How many words does the empty inner list contribute?</summary>

Zero. Its stream has no contents to emit.

</details>

<details>
<summary>2. Why is map(List::stream) different?</summary>

It keeps each returned Stream as an element, giving nested streams. flatMap places their String contents into the outer result.

</details>

<details>
<summary>3. Does flatMap remove duplicates or flatten every depth?</summary>

Neither. It removes one mapped stream layer. Deduplication and additional nested layers require separate choices.

</details>

## Stop/go

Explain the concept question in your own words, predict this example's result, and answer the questions before opening their answers. A coding attempt is optional. Record your explanation in [learner progress](../../learner-progress.md); compiled examples do not demonstrate comprehension.

**Next:** [10 — Lazy pipelines and one-use streams](../10-laziness-and-reuse/README.md).
