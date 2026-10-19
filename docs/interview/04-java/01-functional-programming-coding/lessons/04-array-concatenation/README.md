# Micro-Lesson 04 — LeetCode 1929 — Concatenation of Array

## Question

Given an integer array numbers of length n, return a new array of length 2n containing all input values in order followed by the same values in order again: answer[i]=numbers[i] and answer[i+n]=numbers[i] for 0<=i<n.

## What you will learn

- Apply an input-preserving functional transformation to an array problem.
- Join two fresh ordered integer streams into one result.
- Compare the stream expression with a simple loop of equal complexity.

## Lesson status and prerequisites

- Learning status: `TODO`; prepared, not activated or understood.
- Primary idea: Apply an input-preserving functional transformation to an array problem.
- New terms: integer stream, concatenation
- Prerequisite: [Stream Transformations](../03-stream-transformations/README.md); understand source traversal and result collection before adding the integer-array source.

Problem: [LeetCode 1929 — Concatenation of Array](https://leetcode.com/problems/concatenation-of-array/) · Editable Java: [ArrayConcatenationPractice.java](../../../../../../src/test/java/com/instagram/backend/interview/functionalprogramming/arrayconcatenation/ArrayConcatenationPractice.java). Constraints: `1<=n<=1000`, values in `[1,1000]`. The local programs also handle empty input defensively. Target `O(n)` time and `O(n)` output space.

## Why this problem is a natural functional exercise

An array stores values; it does not require an in-place algorithm. The requested output is a new sequence derived from an unchanged input. An **integer stream** is Java’s stream form specialized for `int` values. **Concatenation** means joining complete sequences in order: all of the first, then all of the second. It is not alternating their elements.

Choose the idea before the API: read the input values once as the first sequence, read the same values again as a second sequence, then gather both into a new array. `Arrays.stream(numbers)` provides one ordered integer traversal; `IntStream.concat(first,second)` joins two traversals; `toArray()` executes the traversal and produces `int[]` output. No callback with outside changes is needed here. Official APIs: [Arrays.stream](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Arrays.html#stream(int%5B%5D)), [IntStream.concat and toArray](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/stream/IntStream.html).

For input `[1,2,1]`, first positions `0,1,2` receive `1,2,1`; output positions `3,4,5` receive `1,2,1` again. This derives `[1,2,1,1,2,1]`. For `[1,3,2,1]`, the first four positions receive those four values, then the next four receive them again: `[1,3,2,1,1,3,2,1]`.

## Block-by-block code walkthrough

Original snapshots are retained. Where a value changes several times, its cell shows the sequence ending at the final value.

Use `numbers=[1,2,1]`, length `3`, for both implementations. We explain the stream version first, then compare the loop rather than assuming short code is always preferable.

### Block 1 — Create two independent traversals of the same values

```java
Arrays.stream(numbers), Arrays.stream(numbers)
```

These are the first and second arguments of the concat call below, not a standalone Java statement. Java evaluates arguments from left to right: create a traversal over input `[1,2,1]`, then a second traversal over that same unchanged array. They are separate stream objects; no values have been collected yet. The array itself is not copied or changed by stream creation. Do not save one stream and pass that same consumed/linked object twice: streams are one-use traversals. Equivalent: name the fresh arguments `first` and `second` before concatenating them.

### Block 2 — Join the traversals and produce one array

```java
return IntStream.concat(Arrays.stream(numbers), Arrays.stream(numbers)).toArray();
```

`concat` describes “the first stream, then the second”; `toArray()` starts traversal and returns the resulting array. The internal buffer and traversal counters are not variables in this method. The following per-element observations show the logical output values; they do not promise a particular hidden allocation strategy. Without `toArray`, the expression would still be a stream rather than the required array. Equivalent: assign the concat result to a stream local and then call toArray once.

**Emission 0: first traversal, source position `0`.** Read `numbers[0]=1` from the unchanged input. Include `1` at logical output position `0`, so output values so far are `[1]`. Continue with the next value of this traversal. No user-written `if`, callback, or index update runs in this implementation; those are library-controlled traversal decisions.

| logical output position | traversal | source position (explanatory) | value | logical values so far | input after |
| --- | --- | --- | --- | --- | --- |
| `0` | `first` | `0` | `1` | `[1]` | `[1, 2, 1]` |

**Emission 1: first traversal, source position `1`.** Read `numbers[1]=2` from the unchanged input. Include `2` at logical output position `1`, so output values so far are `[1, 2]`. Continue with the next value of this traversal. No user-written `if`, callback, or index update runs in this implementation; those are library-controlled traversal decisions.

| logical output position | traversal | source position (explanatory) | value | logical values so far | input after |
| --- | --- | --- | --- | --- | --- |
| `1` | `first` | `1` | `2` | `[1, 2]` | `[1, 2, 1]` |

**Emission 2: first traversal, source position `2`.** Read `numbers[2]=1` from the unchanged input. Include `1` at logical output position `2`, so output values so far are `[1, 2, 1]`. The first traversal now has no next value; proceed to the second traversal. No user-written `if`, callback, or index update runs in this implementation; those are library-controlled traversal decisions.

| logical output position | traversal | source position (explanatory) | value | logical values so far | input after |
| --- | --- | --- | --- | --- | --- |
| `2` | `first` | `2` | `1` | `[1, 2, 1]` | `[1, 2, 1]` |

**Emission 3: second traversal, source position `0`.** Read `numbers[0]=1` from the unchanged input. Include `1` at logical output position `3`, so output values so far are `[1, 2, 1, 1]`. Continue with the next value of this traversal. No user-written `if`, callback, or index update runs in this implementation; those are library-controlled traversal decisions.

| logical output position | traversal | source position (explanatory) | value | logical values so far | input after |
| --- | --- | --- | --- | --- | --- |
| `3` | `second` | `0` | `1` | `[1, 2, 1, 1]` | `[1, 2, 1]` |

**Emission 4: second traversal, source position `1`.** Read `numbers[1]=2` from the unchanged input. Include `2` at logical output position `4`, so output values so far are `[1, 2, 1, 1, 2]`. Continue with the next value of this traversal. No user-written `if`, callback, or index update runs in this implementation; those are library-controlled traversal decisions.

| logical output position | traversal | source position (explanatory) | value | logical values so far | input after |
| --- | --- | --- | --- | --- | --- |
| `4` | `second` | `1` | `2` | `[1, 2, 1, 1, 2]` | `[1, 2, 1]` |

**Emission 5: second traversal, source position `2`.** Read `numbers[2]=1` from the unchanged input. Include `1` at logical output position `5`, so output values so far are `[1, 2, 1, 1, 2, 1]`. The second traversal now has no next value; finish toArray and return. No user-written `if`, callback, or index update runs in this implementation; those are library-controlled traversal decisions.

| logical output position | traversal | source position (explanatory) | value | logical values so far | input after |
| --- | --- | --- | --- | --- | --- |
| `5` | `second` | `2` | `1` | `[1, 2, 1, 1, 2, 1]` | `[1, 2, 1]` |

The method returns `[1,2,1,1,2,1]`. The result is a new modifiable array. There is no later statement or early-match branch; exhausting both sources completes the result. Empty input gives two empty traversals, zero emissions, and a returned `[]`.

### Block 3 — Allocate a loop result with both halves

```java
int[] result = new int[2 * numbers.length];
```

For the loop alternative, `2*3=6`; allocate `result=[0,0,0,0,0,0]`. This is a new independent array, not a second name for numbers. Forgetting the second half would leave no room for the repeated values. Equivalent: `int outputLength=2*numbers.length` before allocation. Under `n<=1000`, this length arithmetic cannot overflow.

### Block 4 — Write each value to both of its required positions

```java
for (int index = 0; index < numbers.length; index++) {
    result[index] = numbers[index];
    result[index + numbers.length] = numbers[index];
}
```

At each valid input index, read its value and write it first at the same result position, then at the corresponding position `n` cells later. Both writes target the new array. Using `index+1` for the second destination would overwrite neighboring first-half values instead. Equivalent: save the read value in a local, then assign both cells from that local.

**Loop iteration `index=0`.** Check `0<3 → true`. Read `1` and write `result[0]=1`, giving `[1, 0, 0, 0, 0, 0]`. Calculate the second destination `0+3=3` and write `1` there, giving `[1, 0, 0, 1, 0, 0]`. Input stays `[1, 2, 1]`. Execute `index++`, producing `1`.

| index | condition | value | after first write | second destination | after second write | next |
| --- | --- | --- | --- | --- | --- | --- |
| `0` | `0<3 → true` | `1` | `[1, 0, 0, 0, 0, 0]` | `3` | `[1, 0, 0, 1, 0, 0]` | `index=1` |

**Loop iteration `index=1`.** Check `1<3 → true`. Read `2` and write `result[1]=2`, giving `[1, 2, 0, 1, 0, 0]`. Calculate the second destination `1+3=4` and write `2` there, giving `[1, 2, 0, 1, 2, 0]`. Input stays `[1, 2, 1]`. Execute `index++`, producing `2`.

| index | condition | value | after first write | second destination | after second write | next |
| --- | --- | --- | --- | --- | --- | --- |
| `1` | `1<3 → true` | `2` | `[1, 2, 0, 1, 0, 0]` | `4` | `[1, 2, 0, 1, 2, 0]` | `index=2` |

**Loop iteration `index=2`.** Check `2<3 → true`. Read `1` and write `result[2]=1`, giving `[1, 2, 1, 1, 2, 0]`. Calculate the second destination `2+3=5` and write `1` there, giving `[1, 2, 1, 1, 2, 1]`. Input stays `[1, 2, 1]`. Execute `index++`, producing `3`.

| index | condition | value | after first write | second destination | after second write | next |
| --- | --- | --- | --- | --- | --- | --- |
| `2` | `2<3 → true` | `1` | `[1, 2, 1, 1, 2, 0]` | `5` | `[1, 2, 1, 1, 2, 1]` | `index=3` |

### Block 5 — Return the loop result after exhaustion

```java
return result;
```

The last increment makes index `3`; `3<3 → false` skips both writes. Return `[1,2,1,1,2,1]`, equal in values to the stream result. For empty input, allocate length zero, skip `0<0`, and return `[]`. Returning the input instead would have the wrong length and would share the original object. Equivalent: name the returned array `concatenated`.

## Complete reference solution

Runnable reference: [ArrayConcatenationSolution.java](../../../../../../src/test/java/com/instagram/backend/interview/functionalprogramming/arrayconcatenation/ArrayConcatenationSolution.java). Read the walkthrough immediately above before this complete listing.

```java
// Input: numbers=[1,2,1]
// Expected return: [1,2,1,1,2,1] from either method

static int[] getConcatenation(int[] numbers) {
    return IntStream.concat(Arrays.stream(numbers), Arrays.stream(numbers)).toArray();
}

static int[] getConcatenationWithLoop(int[] numbers) {
    int[] result = new int[2 * numbers.length];
    for (int index = 0; index < numbers.length; index++) {
        result[index] = numbers[index];
        result[index + numbers.length] = numbers[index];
    }
    return result;
}
```

## Correctness, complexity, and when to choose each

The stream version emits each of the `n` first-traversal values followed by each of the `n` second-traversal values. The loop version maintains that every processed index has the correct value in both halves; exhaustion fills every required output position. Both preserve the input.

Both require `O(n)` time and `O(n)` output space. The loop has `O(1)` extra scalar space beyond its output; the stream API may use internal collection buffers, so do not infer a zero-allocation implementation from the short expression. Choose the stream for a readable sequence transformation; choose the loop when direct index control or a simpler allocation story helps. Neither improves the asymptotic algorithm here.

## How this connects to other LeetCode work

| Problem shape | Is a functional expression useful? | Reason |
|---|---|---|
| This concatenation | Yes, as an alternative | Join ordered input-derived sequences without editing the source |
| Group Anagrams | Potentially, after grouping is taught | Grouping still requires a correct shared-character key; a collector does not invent that key |
| Two Sum in one pass | An ordinary loop is often clearer | The evolving value-to-index map is central to the algorithm; repeatedly scanning a stream for partners can become O(n²) |
| Running Sum | Save until accumulated-result lessons | Each output depends on the earlier total; an outside mutable counter hidden in map is not the independent transformation we just learned |

This table is a roadmap, not four additional problems to study in this micro-lesson. LeetCode tests algorithmic correctness and complexity; a functional-looking expression is not a requirement or a shortcut around either.

## Practice after learning the concept

Open [ArrayConcatenationPractice.java](../../../../../../src/test/java/com/instagram/backend/interview/functionalprogramming/arrayconcatenation/ArrayConcatenationPractice.java) in IntelliJ and implement the marked TODO methods. Run its `main` to check your work. A “Practice ready” message means the method is unfinished, not passing. Keep the [reference program](../../../../../../src/test/java/com/instagram/backend/interview/functionalprogramming/arrayconcatenation/ArrayConcatenationSolution.java) closed until you have attempted the solution.

## Questions and explained answers

<details>
<summary>1. Why create two streams rather than reuse one object?</summary>

Each stream represents a one-use traversal. Two fresh streams can read the same unchanged input independently. Passing the same stream as both concat arguments attempts to reuse a linked/consumed traversal.

</details>

<details>
<summary>2. Is the loop version impure merely because it writes result cells?</summary>

No. It writes only its freshly allocated output, leaves the supplied array unchanged, and produces the same output values for the same input contents.

</details>

<details>
<summary>3. Does the stream solution have a better big-O complexity here?</summary>

No. Both produce 2n values in O(n) time and need O(n) output space. Their differences are expression style, constant overhead, and control over allocation/indexing.

</details>

## Stop/go

Keep learning TODO until both practice implementations pass, you can narrate one emission and one loop iteration, explain input/output ownership and stream freshness, compare complexities, and answer the checkpoints. Do not attempt later LeetCode-to-stream rewrites before choosing a sound algorithm.
