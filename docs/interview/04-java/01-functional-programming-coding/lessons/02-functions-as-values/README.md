# Micro-Lesson 02 — Pass Behavior as a Value

## Question

How can the same Java array traversal double values in one call and add three in another, without rewriting the loop?

## What you will learn

- Pass a calculation into a method as an argument.
- Read a lambda and invoke the behavior it represents.
- Recognize a method reference as a named-function alternative.

## Lesson status and prerequisites

- Learning status: `TODO`; prepared, not activated or understood.
- Primary idea: Pass a calculation into a method as an argument.
- New terms: functional interface, lambda, method reference
- Prerequisite: [Pure Transformations](../01-pure-transformations/README.md): understand a pure scalar calculation and input-preserving array output.

## Start with the duplication problem

The first lesson’s array loop always doubles. If the next task adds three, copying the entire loop would duplicate traversal and input-preservation logic. Keep that loop once and supply the part that changes: the calculation for a single element.

A **functional interface** is a Java contract specifying one operation whose implementation you supply. Java calls that operation an abstract method: its declaration names the inputs and output but provides no method body. It describes the shape of behavior a method can receive. `IntUnaryOperator` is an existing example: its operation accepts one `int` and returns one `int`, invoked through `applyAsInt(value)`. “Unary” in its class name simply means one input.

A **lambda** is a compact way to supply that behavior. In `value -> value * 2`, the left side names the received input and the right side computes the returned result. Passing the lambda does not double an array yet; the receiving method decides when to invoke it.

A **method reference** supplies an existing compatible method rather than writing its body inline: `FunctionsAsValuesSolution::doubleValue` names the same one-int-to-one-int calculation. These are two ways to supply behavior; neither guarantees that the supplied code is pure. Use side-effect-free calculations for this exercise. Official API: [IntUnaryOperator](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/function/IntUnaryOperator.html).

## Programs and expected results

For non-null input `[-2,0,3]`, the same traversal with `value -> value*2` gives `[-4,0,6]`; with `value -> value+3` it gives `[1,3,6]`. Each output is derived element by element below. Accept a non-null operation; values in these examples are small enough that arithmetic stays in int range. Target `O(n)` time when the supplied calculation is `O(1)`, `O(n)` output space, and no input writes.

## Block-by-block code walkthrough

Original snapshots are retained. Where a value changes several times, its cell shows the sequence ending at the final value.

Input `numbers=[-2,0,3]`; first call supplies `operation = value -> value*2`, second supplies `operation = value -> value+3`.

### Block 1 — Receive the behavior without executing it yet

```java
static int[] transform(int[] numbers, IntUnaryOperator operation) {
    int[] result = new int[numbers.length];
```

The caller supplies two arguments: an array and behavior matching the interface. Create `result=[0,0,0]`. The operation has not been called yet. There is no null guard; non-null arguments are part of the practice contract. Hardcoding the calculation here would prevent reuse; receiving `operation` makes that choice explicit. Equivalent: the caller can assign the lambda to `IntUnaryOperator chosenOperation` before passing it.

### Block 2 — Invoke the supplied operation once for each element

```java
for (int index = 0; index < numbers.length; index++) {
    result[index] = operation.applyAsInt(numbers[index]);
}
```

`applyAsInt` is the interface’s invocation method. Read the input value, hand it to the supplied behavior, receive its result, then store that result in the new array. It is not an array conversion API. Equivalent: save `int current=numbers[index]` and `int transformed=operation.applyAsInt(current)` before assignment. Forgetting to invoke the behavior would leave it unused; writing into numbers instead would violate the contract.

**Call using doubling: `operation = value -> value*2`.** Start a fresh result `[0,0,0]` and a fresh loop index `0`.

**Iteration `index=0`.** Evaluate `0<3 → true`. Read `-2`. `applyAsInt(-2)` runs the supplied expression, calculating `-2*2=-4`. Store `-4` at `result[0]`; input stays `[-2, 0, 3]`. Execute `index++` to `1`.

| index | condition | operation | input | returned value | result after | input after | next |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `0` | `0<3 → true` | `value*2` | `-2` | `-4` | `[-4, 0, 0]` | `[-2, 0, 3]` | `index=1` |

**Iteration `index=1`.** Evaluate `1<3 → true`. Read `0`. `applyAsInt(0)` runs the supplied expression, calculating `0*2=0`. Store `0` at `result[1]`; input stays `[-2, 0, 3]`. Execute `index++` to `2`.

| index | condition | operation | input | returned value | result after | input after | next |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `1` | `1<3 → true` | `value*2` | `0` | `0` | `[-4, 0, 0]` | `[-2, 0, 3]` | `index=2` |

**Iteration `index=2`.** Evaluate `2<3 → true`. Read `3`. `applyAsInt(3)` runs the supplied expression, calculating `3*2=6`. Store `6` at `result[2]`; input stays `[-2, 0, 3]`. Execute `index++` to `3`.

| index | condition | operation | input | returned value | result after | input after | next |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `2` | `2<3 → true` | `value*2` | `3` | `6` | `[-4, 0, 6]` | `[-2, 0, 3]` | `index=3` |

At `index=3`, `3<3 → false`; no further operation call runs. The completed result for this call is `[-4, 0, 6]`.

**Call using adding three: `operation = value -> value+3`.** Start a fresh result `[0,0,0]` and a fresh loop index `0`.

**Iteration `index=0`.** Evaluate `0<3 → true`. Read `-2`. `applyAsInt(-2)` runs the supplied expression, calculating `-2+3=1`. Store `1` at `result[0]`; input stays `[-2, 0, 3]`. Execute `index++` to `1`.

| index | condition | operation | input | returned value | result after | input after | next |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `0` | `0<3 → true` | `value+3` | `-2` | `1` | `[1, 0, 0]` | `[-2, 0, 3]` | `index=1` |

**Iteration `index=1`.** Evaluate `1<3 → true`. Read `0`. `applyAsInt(0)` runs the supplied expression, calculating `0+3=3`. Store `3` at `result[1]`; input stays `[-2, 0, 3]`. Execute `index++` to `2`.

| index | condition | operation | input | returned value | result after | input after | next |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `1` | `1<3 → true` | `value+3` | `0` | `3` | `[1, 3, 0]` | `[-2, 0, 3]` | `index=2` |

**Iteration `index=2`.** Evaluate `2<3 → true`. Read `3`. `applyAsInt(3)` runs the supplied expression, calculating `3+3=6`. Store `6` at `result[2]`; input stays `[-2, 0, 3]`. Execute `index++` to `3`.

| index | condition | operation | input | returned value | result after | input after | next |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `2` | `2<3 → true` | `value+3` | `3` | `6` | `[1, 3, 6]` | `[-2, 0, 3]` | `index=3` |

At `index=3`, `3<3 → false`; no further operation call runs. The completed result for this call is `[1, 3, 6]`.

### Block 3 — Return this call’s new values

```java
return result;
```

Return the completed array: `[-4,0,6]` for doubling, `[1,3,6]` for adding three. Only the supplied calculation changed. Each call owns an independent output, and input remains `[-2,0,3]`. Without this return the caller has no transformed values. Equivalent: name the local array `transformedValues`.

### Block 4 — Supply an existing function with the same shape

```java
static int doubleValue(int value) {
    return value * 2;
}
```

For `value=3`, this helper calculates `3*2=6` and returns it. It changes no external data. The caller can pass `FunctionsAsValuesSolution::doubleValue`; when index `2` is reached, `applyAsInt(3)` invokes that helper. The values and iteration trace match the doubling-lambda call exactly. Equivalent: `value -> doubleValue(value)`. The helper is present in the runnable reference and practice classes so the method reference resolves correctly in IntelliJ.

## Complete reference solution

Runnable reference: [FunctionsAsValuesSolution.java](../../../../../../src/test/java/com/instagram/backend/interview/functionalprogramming/functionsasvalues/FunctionsAsValuesSolution.java). Read the walkthrough immediately above before this complete listing.

```java
// Input: numbers=[-2,0,3]; operation=value -> value*2 OR value -> value+3
// Expected return: [-4,0,6] OR [1,3,6]

static int[] transform(int[] numbers, IntUnaryOperator operation) {
    int[] result = new int[numbers.length];
    for (int index = 0; index < numbers.length; index++) {
        result[index] = operation.applyAsInt(numbers[index]);
    }
    return result;
}

static int doubleValue(int value) {
    return value * 2;
}
```

The caller chooses behavior like this:

```java
IntUnaryOperator doubleIt = value -> value * 2;
int[] doubled = transform(new int[] {-2, 0, 3}, doubleIt);
int[] increased = transform(new int[] {-2, 0, 3}, value -> value + 3);
int[] named = transform(new int[] {-2, 0, 3}, FunctionsAsValuesSolution::doubleValue);
```

The first declaration stores behavior; it does not traverse an array. The following calls execute the traversal with that choice. Values are `doubled=[-4,0,6]`, `increased=[1,3,6]`, `named=[-4,0,6]`, as derived in the two traces. This caller fragment is demonstrated by the linked reference `main` checks; it is not an additional algorithm.

## Correctness, costs, and limits

Before index `i`, each earlier result equals the supplied operation applied once to that position’s input. The assignment extends this by one. The array itself is never written, but an arbitrary supplied callback could still print, read changing state, or edit something else; purity requires that callback’s behavior to be pure too.

For a constant-time callback, the loop is `O(n)` time and `O(n)` output space. More generally, multiply the traversal count by the callback’s cost. A dedicated doubling method may be clearer when behavior never changes; parameterizing it is useful when several calculations share traversal. An empty input skips `0<0` and makes zero callback calls; identity behavior `value -> value` on `[5]` calculates `5` and returns `[5]` in a separate array.

## Practice after learning the concept

Open [FunctionsAsValuesPractice.java](../../../../../../src/test/java/com/instagram/backend/interview/functionalprogramming/functionsasvalues/FunctionsAsValuesPractice.java) in IntelliJ and implement the marked TODO methods. Run its `main` to check your work. A “Practice ready” message means the method is unfinished, not passing. Keep the [reference program](../../../../../../src/test/java/com/instagram/backend/interview/functionalprogramming/functionsasvalues/FunctionsAsValuesSolution.java) closed until you have attempted the solution.

## Questions and explained answers

<details>
<summary>1. What does value -> value*2 create, and when is it run?</summary>

It supplies one-int-to-one-int behavior. It is run when transform calls operation.applyAsInt on an input value, not simply when the lambda is passed.

</details>

<details>
<summary>2. Does IntUnaryOperator guarantee a pure callback?</summary>

No. The interface specifies input/output types and one invocation method, not the absence of side effects. Supply pure behavior when you want the overall transformation to be pure.

</details>

<details>
<summary>3. How is the method reference related to the doubling lambda?</summary>

With the shown helper, FunctionsAsValuesSolution::doubleValue and value -> value*2 produce the same values. The method reference reuses a named implementation rather than repeating its body.

</details>

## Stop/go

Keep learning TODO until you can read the lambda aloud, explain the interface’s job, predict both calls, implement transform, pass checks, and answer the questions. Do not advance merely because the reference program works.
