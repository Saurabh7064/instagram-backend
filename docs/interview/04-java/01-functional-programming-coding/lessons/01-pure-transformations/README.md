# Micro-Lesson 01 — Pure Transformations

## Question

What makes a Java function pure, and how can we return doubled values without changing caller-owned data?

## What you will learn

- Explain functional programming before learning stream syntax.
- Recognize a pure function and an observable side effect.
- Write small scalar and array transformations with predictable results.

## Lesson status and prerequisites

- Learning status: `TODO`; prepared, not activated or understood.
- Primary idea: Explain functional programming before learning stream syntax.
- New terms: pure function, side effect
- Prerequisite: Basic Java methods, `int`, arrays, and a `for` loop. No lambda or Stream API knowledge is needed.

## Start with the problem functional programming addresses

Imagine a calculation that silently edits your input, depends on a setting elsewhere, or updates a shared counter. Calling it twice can become difficult to reason about: the second call may see data changed by the first. We want calculations whose result can be understood from their inputs, with changes to the outside world kept explicit.

**Functional programming** is a programming style that builds computations from functions and combines their results. In Java, you can adopt this style gradually: first make calculations predictable, later pass behavior into other operations, then combine transformations. Streams are one tool for expressing those transformations; they are not the definition of functional programming.

A **pure function** returns the same result values for the same input values and causes no observable changes outside the calculation. A **side effect** is such an outside change: editing a supplied array, writing to a file, updating a shared variable, or printing. Reading the current time or changing global settings can also make a result depend on something other than its declared arguments.

For these Java exercises, compare output values, not the identity of newly allocated objects. Two calls may return different array objects containing equal values. The caller must supply the same array contents and must not concurrently change them during a call.

## Program 1 — One value in, one value out

Choose the calculation before writing syntax: take a supplied integer, multiply it by two, return the result. With input `3`, calculate `3×2=6`; repeating the same call still gives `6`. With `-2`, calculate `-2×2=-4`. There is no dependency on earlier calls or machine settings.

## Program 2 — A new array instead of editing the old one

For input `[-2,0,3]`, preserve that input and return `[-4,0,6]`. The rule is the same scalar calculation applied to each value in order. This is useful when another caller still needs the original numbers.

The examples accept a non-null array, with values between `-1,000,000` and `1,000,000`; doubling stays within `[-2,000,000,2,000,000]`, so no integer overflow occurs. Empty arrays are allowed. Target: `O(1)` time for one scalar; `O(n)` time and `O(n)` output space for an array.

## Block-by-block code walkthrough

Original snapshots are retained. Where a value changes several times, its cell shows the sequence ending at the final value.

Keep input `value=3` for the scalar program and `numbers=[-2,0,3]` for the array program beside the code.

### Block 1 — Calculate from the argument only

```java
static int doubleValue(int value) {
    return value * 2;
}
```

The parameter receives the caller’s integer. Evaluate `value*2`; for `value=3`, the result is `6`. Return it immediately; no outside variable or supplied object is changed. There is no guard or loop in this method. Omitting the return leaves no answer for the caller; a clearer equivalent is `int doubled=value*2; return doubled;`.

This function is pure for the stated inputs because its result depends only on the argument and it changes nothing outside itself. A method named `doubleValue` that secretly increments a shared counter would fail the second condition even if it returned six every time.

- `doubleValue(3)` receives `value=3`; `return value * 2` calculates `3 × 2 = 6` and finishes the call.
- `doubleValue(3)` again receives `value=3`; the same calculation returns `6`, with no outside change.
- `doubleValue(-2)` receives `value=-2`; `return value * 2` calculates `-2 × 2 = -4` and finishes the call.

| call | read argument | calculation | return | outside changes |
| --- | --- | --- | --- | --- |
| `doubleValue(3)` | `3` | `3*2` | `6` | `none` |
| `doubleValue(3) again` | `3` | `3*2` | `6` | `none` |
| `doubleValue(-2)` | `-2` | `-2*2` | `-4` | `none` |

### Block 2 — Allocate result cells owned by this call

```java
int[] result = new int[numbers.length];
```

- `int[] result = new int[numbers.length]` reads length `3`; allocate `result=[0,0,0]`.
- `numbers=[-2,0,3]` stays unchanged; `result` is an independent array.

The array is locally created so filling it does not overwrite caller-owned input. Allocating output is different from changing an existing input object. Reusing `result=numbers` would merely give the same input array another name and later writes would mutate it. Equivalent: name the variable `doubledNumbers` to make its purpose explicit.

### Block 3 — Apply the same pure calculation to each input

```java
for (int index = 0; index < numbers.length; index++) {
    result[index] = doubleValue(numbers[index]);
}
```

Start at index zero. Check the bound before reading an element. The assignment writes a newly owned result cell, never `numbers[index]`. The loop has changing local state, but callers cannot observe it while this ordinary calculation runs; this does not make the method impure. Equivalent: inline `numbers[index]*2`, although the named helper makes the reusable calculation explicit.

**Iteration `index=0`.**

- **✅ `for` condition `index < numbers.length`:** `0 < 3 → true`; enter the body.
- `doubleValue(numbers[index])` reads `numbers[0]=-2`; the helper executes `return value * 2`: `-2 × 2 = -4`.
- `result[index] = ...` writes `result[0]`: `0 → -4`. The full result is below; `numbers=[-2,0,3]` stays unchanged.
- `index++`: `0 → 1`; continue to the next `for` check.

| index | condition | input read | helper return | result after | numbers after | next |
| --- | --- | --- | --- | --- | --- | --- |
| `0` | `✅ 0<3 → true` | `-2` | `-4` | `[-4, 0, 0]` | `[-2, 0, 3]` | `index=1` |

**Iteration `index=1`.**

- **✅ `for` condition `index < numbers.length`:** `1 < 3 → true`; enter the body.
- `doubleValue(numbers[index])` reads `numbers[1]=0`; the helper executes `return value * 2`: `0 × 2 = 0`.
- `result[index] = ...` writes `result[1]`: `0 → 0`. The full result is below; `numbers=[-2,0,3]` stays unchanged.
- `index++`: `1 → 2`; continue to the next `for` check.

| index | condition | input read | helper return | result after | numbers after | next |
| --- | --- | --- | --- | --- | --- | --- |
| `1` | `✅ 1<3 → true` | `0` | `0` | `[-4, 0, 0]` | `[-2, 0, 3]` | `index=2` |

**Iteration `index=2`.**

- **✅ `for` condition `index < numbers.length`:** `2 < 3 → true`; enter the body.
- `doubleValue(numbers[index])` reads `numbers[2]=3`; the helper executes `return value * 2`: `3 × 2 = 6`.
- `result[index] = ...` writes `result[2]`: `0 → 6`. The full result is below; `numbers=[-2,0,3]` stays unchanged.
- `index++`: `2 → 3`; continue to the next `for` check.

| index | condition | input read | helper return | result after | numbers after | next |
| --- | --- | --- | --- | --- | --- | --- |
| `2` | `✅ 2<3 → true` | `3` | `6` | `[-4, 0, 6]` | `[-2, 0, 3]` | `index=3` |

### Block 4 — Return values without exposing a change to the input

```java
return result;
```

- **❌ `for` condition `index < numbers.length`:** `3 < 3 → false`; skip the helper call and assignment.
- `return result` returns `[-4,0,6]`; `numbers` stays `[-2,0,3]`. A second call creates another result with the same values. Returned arrays are modifiable by their owner, but modifying one does not change the original or a separate call’s result. Returning `numbers` would return the wrong values and share the input object. Equivalent: return a clearly named `doubledNumbers` variable.

## Complete reference solution

Runnable reference: [PureFunctionsSolution.java](../../../../../../src/test/java/com/instagram/backend/interview/functionalprogramming/foundations/PureFunctionsSolution.java). Read the walkthrough immediately above before this complete listing.

```java
// numbers index:  0 1 2
// numbers      : -2 0 3
// Input: value=3; numbers=[-2,0,3]
// Expected return: doubleValue -> 6; doubleNumbers -> [-4,0,6]

static int doubleValue(int value) {
    return value * 2;
}

static int[] doubleNumbers(int[] numbers) {
    int[] result = new int[numbers.length];
    for (int index = 0; index < numbers.length; index++) {
        result[index] = doubleValue(numbers[index]);
    }
    return result;
}
```

## Recognize the contrasting program

The statement `numbers[0]=99` changes an existing caller-owned array. Starting from `[-2,0,3]`, the caller now sees `[99,0,3]`; that is a side effect. Repeating a method on the changed input no longer repeats the same input values. Printing the answer from `main` is also a side effect, but it is explicit at the program’s outer boundary; keep the calculation itself separate from that printing.

A function can use a loop internally and still preserve the purity contract. A stream callback can secretly mutate shared data and violate it. Syntax alone does not decide purity.

## Why arrays and LeetCode still fit

An array is a way to store values; functional programming describes how we calculate with them. We just used an array without modifying its input. A later [Concatenation of Array exercise](../04-array-concatenation/README.md) returns two copies of the input values and compares an ordinary loop with a stream version. Wait until the intervening lessons explain functions-as-values and streams before attempting that stream solution.

Do not turn every LeetCode algorithm into a stream. A pointer loop or a one-pass map lookup may express the required algorithm better. Understand the algorithm and its complexity first, then choose a clear implementation.

## Correctness, costs, and alternatives

Before index `i`, result positions below `i` contain exactly twice the corresponding input value, and the input is unchanged. Assigning the doubled current value extends that property by one; loop exhaustion covers all positions. Scalar work is `O(1)`. Array work visits `n` values once: `O(n)` time and `O(n)` output space, with `O(1)` additional scalar state.

Updating an array in place could avoid the new output allocation but changes the contract: the caller loses its original values. Use it only when mutation is explicitly wanted. For empty input, allocate `[]`, evaluate `0<0 → false`, and return `[]`. That is a complete zero-iteration execution, not an error.

## Practice after learning the concept

Open [PureFunctionsPractice.java](../../../../../../src/test/java/com/instagram/backend/interview/functionalprogramming/foundations/PureFunctionsPractice.java) in IntelliJ and implement the marked TODO methods. Run its `main` to check your work. A “Practice ready” message means the method is unfinished, not passing. Keep the [reference program](../../../../../../src/test/java/com/instagram/backend/interview/functionalprogramming/foundations/PureFunctionsSolution.java) closed until you have attempted the solution.

## Questions and explained answers

<details>
<summary>1. Does a for loop automatically make a function impure?</summary>

No. This loop changes only its local index and newly allocated output. It preserves caller-owned inputs and has no external changes; the same input contents produce the same output values.

</details>

<details>
<summary>2. Does returning a modifiable array contradict purity?</summary>

Not by itself. The method produces an independent result and changes no pre-existing caller-owned data. Later changes made by the caller are separate actions. Two output arrays can have equal values without being the same object.

</details>

<details>
<summary>3. Is a method pure if it always returns six but also prints a line?</summary>

It has an observable printing side effect, so it is not pure. Put printing in main and keep the calculation separate.

</details>

## Stop/go

Artifact readiness is separate from learning. Stay here until you can define purity in plain language, identify the input-mutation side effect, predict all three loop results, pass both practice methods, and explain the costs. Answer the checkpoints before opening their explanations. Later lessons stay TODO; do not infer understanding from the reference runner.
