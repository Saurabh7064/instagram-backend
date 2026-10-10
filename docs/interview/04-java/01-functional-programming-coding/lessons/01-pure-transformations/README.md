# Micro-Lesson 01 — Pure Transformations

## Question

> Given a list of words, how can you trim them, discard blanks, lowercase the survivors, preserve their order, and return a new list without changing the input?

## What you will learn

- what makes a transformation pure from the caller's point of view;
- how `map`, `filter`, and `toList` form a stream pipeline;
- why pipeline stage order changes the result;
- why `Locale.ROOT` makes machine-oriented case normalization predictable;
- when an ordinary loop is clearer than a stream.

## Lesson status

- Learning status: `TODO`; initialized but not active
- Primary idea: describe a sequence of transformations without changing the input list
- New terms: pure function, stream pipeline, lambda

## Inputs, examples, and target

The input is a non-null list of non-null words. The transformation must:

1. trim surrounding whitespace;
2. discard empty results;
3. convert remaining words to lowercase using `Locale.ROOT`;
4. preserve encounter order;
5. return a new list without changing the input.

Examples:

- `[" Alice ", "", "BOB", "   ", "Chloë"]` → `["alice", "bob", "chloë"]`
- `[]` → `[]`

Target: `O(c)` time for `c` total input characters and `O(c)` output space.

## Reference approach

```java
return words.stream()
        .map(String::trim)
        .filter(word -> !word.isEmpty())
        .map(word -> word.toLowerCase(Locale.ROOT))
        .toList();
```

The pipeline reads from the input and produces a new result:

- the first `map` transforms each word;
- `filter` decides which transformed words continue;
- the second `map` normalizes case;
- `toList` collects the results in encounter order.

`Locale.ROOT` makes case conversion independent of the machine's user locale. Without it, the same input can produce locale-specific results on different machines.

## Invariant and correctness

After each pipeline stage, the stream contains exactly the processed representation promised by that stage. Every surviving word is trimmed, non-empty, lowercased, and in the same relative order as the input.

The input is never modified because strings are immutable and no operation writes to the source list. `Stream.toList()` returns an unmodifiable result in modern Java; callers needing a modifiable result should explicitly collect into a new `ArrayList`.

## Alternatives and pitfalls

- A loop is valid and can be clearer when debugging or checked exceptions dominate.
- Do not use `peek` to mutate external state; that hides a side effect inside a transformation pipeline.
- Do not call `toLowerCase()` without an explicit locale for machine-independent normalization.


## Practice after learning the concept

- Write here: [NormalizeWordsPractice.java](../../../../../../src/test/java/com/instagram/backend/interview/functionalprogramming/puretransformations/NormalizeWordsPractice.java)
- Reveal after attempting: [NormalizeWordsSolution.java](../../../../../../src/test/java/com/instagram/backend/interview/functionalprogramming/puretransformations/NormalizeWordsSolution.java)

## Questions and explained answers

<details>
<summary>1. Why does this method not mutate its input?</summary>

The stream reads elements, each string transformation creates another string, and `toList` creates a separate result list. No operation calls a mutating method on the source list.

</details>

<details>
<summary>2. Why must trimming happen before filtering empty words?</summary>

A whitespace-only word such as `"   "` is not empty until trimming changes it to `""`.

</details>

<details>
<summary>3. When might a loop be preferable?</summary>

A loop may be clearer when the transformation needs step-by-step debugging, complex branching, checked exceptions, or carefully controlled mutation.

</details>

## Stop/go

Proceed only when the practice checks pass and you can explain input immutability, stage order, `Locale.ROOT`, and the time/space complexity.
