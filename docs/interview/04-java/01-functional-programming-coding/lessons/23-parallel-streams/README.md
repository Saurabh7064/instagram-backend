# 23 — Parallel streams: suitability and limits

## Concept question

Does parallelStream automatically make code faster or safer?

## What you will learn

Explain splitting/combining work and decide when sequential processing is the better default.

**Time:** 10–15 minutes. **Prerequisite:** [22 — Side effects, non-interference, and peek](../22-side-effects-and-peek/README.md).
**Artifact:** READY after recorded verification. **Understanding:** TODO; no learner attempt recorded.

## Understand the concept

A sequential stream processes through one sequential execution path. A parallel stream allows work to be divided and partial results combined. This adds coordination and requires compatible operations. It is not a guarantee of faster execution, a separate thread for every element, or safe use of a shared mutable object.

For [2, 4, 6], doubling gives [4, 8, 12], whose sum is 24. Pure mapping and associative addition can express that result in either sequential or parallel form. The example demonstrates equal results, not a speed advantage. Tiny inputs usually do not justify benchmarking claims about parallel benefit.

Parallel streams are candidates for substantial independent CPU work with splittable sources and appropriate reductions. Blocking database calls or HTTP requests inside them can tie up shared execution resources; request context and ordinary thread-local values do not automatically transfer as intended. Use an explicit concurrency design when you need bounded I/O concurrency, cancellation, or resource isolation.

## Small Java example

The snippet below is included in [FunctionalConceptExamples.java](../../../../../../src/test/java/com/instagram/backend/interview/functionalprogramming/concepts/FunctionalConceptExamples.java#L246), method `lesson23`. Run that class's `main` in IntelliJ for checked examples. The file supplies the needed imports; no coding exercise is required.

```java
List<Integer> numbers = List.of(2, 4, 6);
int sequential = numbers.stream().mapToInt(value -> value * 2).sum();
int parallel = numbers.parallelStream().mapToInt(value -> value * 2).sum();
```

**Expected result:** sequential = 24; parallel = 24; no performance claim is made.

## Read the syntax and result

Doubling produces 4, 8, and 12; 4 + 8 + 12 = 24. Both pipelines ask for the same associative sum. The correctness check does not depend on worker count, scheduling, or callback order.

## Pitfalls and decisions

forEach on a parallel stream does not promise encounter-order effects. forEachOrdered preserves encounter order but may reduce useful parallelism. Measure a realistic workload rather than choosing parallel from syntax alone.

Official reference: [Java 24 API or language contract](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/package-summary.html).

## Questions and explained answers

<details>
<summary>1. Does parallel mean one new thread per element?</summary>

No. Work is split and scheduled using runtime execution resources; no such element-to-thread promise exists.

</details>

<details>
<summary>2. Why is this result safe to combine?</summary>

The mapping has no external mutations and the bounded integer sum uses a suitable associative combination.

</details>

<details>
<summary>3. Should HTTP calls be parallelStream by default?</summary>

No. Blocking work needs explicit concurrency and resource limits; parallel streams do not supply a complete request-execution policy.

</details>

## Stop/go

Explain the concept question in your own words, predict this example's result, and answer the questions before opening their answers. A coding attempt is optional. Record your explanation in [learner progress](../../learner-progress.md); compiled examples do not demonstrate comprehension.

**Next:** [24 — Streams that own resources](../24-resource-streams/README.md).
