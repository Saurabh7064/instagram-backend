# 26 — mapMulti: emit zero or more results

## Concept question

How does mapMulti differ from map and flatMap?

## What you will learn

Compare returned single values, returned inner streams, and explicit output emission.

**Time:** 10–15 minutes. **Prerequisite:** [25 — Bounded and prefix-based pipelines](../25-bounded-and-multi-output-streams/README.md).
**Artifact:** READY after recorded verification. **Understanding:** TODO; no learner attempt recorded.

## Understand the concept

map returns one mapped element for an input. flatMap returns an inner stream whose contents join the output. mapMulti gives its callback an output Consumer so the callback can emit zero, one, or multiple result values directly. Here emit means pass a value to that supplied consumer during the callback.

Suppose inputs are ["java", "go"], and the rule is to emit the original word plus an exclamation-mark version only for words longer than three characters. java qualifies and contributes "java" and "java!"; go does not qualify and contributes nothing. Thus the requested output is ["java", "java!"].

Use mapMulti when a small multi-result conversion reads more naturally with explicit emissions than with construction of inner streams. Keep flatMap when the source naturally already gives lists or streams. mapMulti does not promise better performance for every workload, and the output consumer must not be retained and used later.

## Small Java example

The snippet below is included in [FunctionalConceptExamples.java](../../../../../../src/test/java/com/instagram/backend/interview/functionalprogramming/concepts/FunctionalConceptExamples.java#L278), method `lesson26`. Run that class's `main` in IntelliJ for checked examples. The file supplies the needed imports; no coding exercise is required.

```java
List<String> result = List.of("java", "go").stream()
        .<String>mapMulti((word, output) -> {
            if (word.length() > 3) {
                output.accept(word);
                output.accept(word + "!");
            }
        }).toList();
```

**Expected result:** result = [java, java!].

## Read the syntax and result

java has four characters, so its rule emits two values. go has two characters and supplies none. The <String> type witness explicitly tells Java the emitted result element type. output is a `Consumer<String>`, and accept supplies a result value rather than returning an inner Stream.

## Pitfalls and decisions

Do not invoke the provided output consumer after the mapper returns. Prefer simple conversions; a long stateful emission body can be clearer as an ordinary method. mapMulti was added in Java 16.

Official reference: [Java 24 API or language contract](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Stream.html).

## Questions and explained answers

<details>
<summary>1. How is this different from map?</summary>

map returns one element. mapMulti can emit zero or more values into the output for an input.

</details>

<details>
<summary>2. How is this different from flatMap?</summary>

flatMap returns an inner Stream; mapMulti calls a provided Consumer to emit values directly.

</details>

<details>
<summary>3. Can output be saved for asynchronous use?</summary>

No. Using it outside the mapping callback violates its scope contract. It is not an asynchronous channel.

</details>

## Stop/go

Explain the concept question in your own words, predict this example's result, and answer the questions before opening their answers. A coding attempt is optional. Record your explanation in [learner progress](../../learner-progress.md); compiled examples do not demonstrate comprehension.

**Next:** [27 — Gatherers: custom intermediate transformations](../27-gatherers/README.md).
