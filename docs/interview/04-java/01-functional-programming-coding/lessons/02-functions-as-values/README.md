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

## First understand the idea

The key idea is to keep the array traversal once and pass in the calculation that changes from task to task.

Use `numbers=[-2,0,3]`. Doubling produces `[-4,0,6]` because `-2*2=-4`, `0*2=0`, `3*2=6`. Adding three produces `[1,3,6]` because `-2+3=1`, `0+3=3`, `3+3=6`. Both tasks read the same positions in order and write a separate output array; only the per-value calculation differs.

Let `operation` mean that supplied calculation, `index` the next input position, and `result` the new output array. At index 0, the doubling operation receives -2 and returns -4; the adding-three operation instead returns 1. The common traversal stores whichever value its supplied operation returns.

A **functional interface** describes the input/output shape of behavior Java can receive. `IntUnaryOperator` accepts one int and returns one int, through its method `applyAsInt`. A **lambda**, such as `value -> value*2`, supplies the calculation: receive value on the left, return the expression on the right. A **method reference**, such as `FunctionsAsValuesSolution::doubleValue`, supplies an existing compatible method instead. Naming/passing either does not execute the traversal yet.

In general, `result[index]=operation.applyAsInt(numbers[index])` means read one input, invoke the chosen calculation, save its returned value. Reuse that traversal while changing the supplied behavior. The callback must itself avoid outside changes for the complete transformation to be pure; its type alone does not guarantee purity. The array and operation are non-null. For constant-time callbacks, target `O(n)` time and `O(n)` output space with no input writes. Official API: [IntUnaryOperator](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/function/IntUnaryOperator.html).

## Block-by-block code walkthrough

Original snapshots are retained. Where a value changes several times, its cell shows the sequence ending at the final value.

Input `numbers=[-2,0,3]`; first call supplies `operation = value -> value*2`, second supplies `operation = value -> value+3`.

### Block 1 — Receive the behavior without executing it yet

```java
static int[] transform(int[] numbers, IntUnaryOperator operation) {
    int[] result = new int[numbers.length];
```

- `transform(numbers, operation)` receives `numbers=[-2,0,3]` and the chosen behavior.
- `int[] result = new int[numbers.length]` reads length `3`; allocate `result=[0,0,0]`. `operation.applyAsInt` has not run yet.

There is no null guard; non-null arguments are part of the practice contract. Hardcoding the calculation here would prevent reuse; receiving `operation` makes that choice explicit. Equivalent: the caller can assign the lambda to `IntUnaryOperator chosenOperation` before passing it.

### Block 2 — Invoke the supplied operation once for each element

```java
for (int index = 0; index < numbers.length; index++) {
    result[index] = operation.applyAsInt(numbers[index]);
}
```

`applyAsInt` is the interface’s invocation method. Read the input value, hand it to the supplied behavior, receive its result, then store that result in the new array. It is not an array conversion API. Equivalent: save `int current=numbers[index]` and `int transformed=operation.applyAsInt(current)` before assignment. Forgetting to invoke the behavior would leave it unused; writing into numbers instead would violate the contract.

**Call using doubling: `operation = value -> value*2`.** Start a fresh result `[0,0,0]` and a fresh loop index `0`.

**Iteration `index=0`.**

- **✅ `for` condition `index < numbers.length`:** `0 < 3 → true`; enter the body.
- `operation.applyAsInt(numbers[index])` reads `numbers[0]=-2` and invokes `value -> value * 2`: `-2 * 2 = -4`.
- `result[index] = ...` writes `result[0]`: `0 → -4`. The full output is below; `numbers=[-2,0,3]` stays unchanged.
- `index++`: `0 → 1`; continue to the next `for` check.

| index | condition | operation | input | returned value | result after | input after | next |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `0` | `✅ 0<3 → true` | `value*2` | `-2` | `-4` | `[-4, 0, 0]` | `[-2, 0, 3]` | `index=1` |

**Iteration `index=1`.**

- **✅ `for` condition `index < numbers.length`:** `1 < 3 → true`; enter the body.
- `operation.applyAsInt(numbers[index])` reads `numbers[1]=0` and invokes `value -> value * 2`: `0 * 2 = 0`.
- `result[index] = ...` writes `result[1]`: `0 → 0`. The full output is below; `numbers=[-2,0,3]` stays unchanged.
- `index++`: `1 → 2`; continue to the next `for` check.

| index | condition | operation | input | returned value | result after | input after | next |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `1` | `✅ 1<3 → true` | `value*2` | `0` | `0` | `[-4, 0, 0]` | `[-2, 0, 3]` | `index=2` |

**Iteration `index=2`.**

- **✅ `for` condition `index < numbers.length`:** `2 < 3 → true`; enter the body.
- `operation.applyAsInt(numbers[index])` reads `numbers[2]=3` and invokes `value -> value * 2`: `3 * 2 = 6`.
- `result[index] = ...` writes `result[2]`: `0 → 6`. The full output is below; `numbers=[-2,0,3]` stays unchanged.
- `index++`: `2 → 3`; continue to the next `for` check.

| index | condition | operation | input | returned value | result after | input after | next |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `2` | `✅ 2<3 → true` | `value*2` | `3` | `6` | `[-4, 0, 6]` | `[-2, 0, 3]` | `index=3` |

- **❌ `for` condition `index < numbers.length`:** `3 < 3 → false`; skip `operation.applyAsInt(...)` and the assignment.
- `return result` returns `[-4, 0, 6]`; this call ends with the input unchanged.

**Call using adding three: `operation = value -> value+3`.** Start a fresh result `[0,0,0]` and a fresh loop index `0`.

**Iteration `index=0`.**

- **✅ `for` condition `index < numbers.length`:** `0 < 3 → true`; enter the body.
- `operation.applyAsInt(numbers[index])` reads `numbers[0]=-2` and invokes `value -> value + 3`: `-2 + 3 = 1`.
- `result[index] = ...` writes `result[0]`: `0 → 1`. The full output is below; `numbers=[-2,0,3]` stays unchanged.
- `index++`: `0 → 1`; continue to the next `for` check.

| index | condition | operation | input | returned value | result after | input after | next |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `0` | `✅ 0<3 → true` | `value+3` | `-2` | `1` | `[1, 0, 0]` | `[-2, 0, 3]` | `index=1` |

**Iteration `index=1`.**

- **✅ `for` condition `index < numbers.length`:** `1 < 3 → true`; enter the body.
- `operation.applyAsInt(numbers[index])` reads `numbers[1]=0` and invokes `value -> value + 3`: `0 + 3 = 3`.
- `result[index] = ...` writes `result[1]`: `0 → 3`. The full output is below; `numbers=[-2,0,3]` stays unchanged.
- `index++`: `1 → 2`; continue to the next `for` check.

| index | condition | operation | input | returned value | result after | input after | next |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `1` | `✅ 1<3 → true` | `value+3` | `0` | `3` | `[1, 3, 0]` | `[-2, 0, 3]` | `index=2` |

**Iteration `index=2`.**

- **✅ `for` condition `index < numbers.length`:** `2 < 3 → true`; enter the body.
- `operation.applyAsInt(numbers[index])` reads `numbers[2]=3` and invokes `value -> value + 3`: `3 + 3 = 6`.
- `result[index] = ...` writes `result[2]`: `0 → 6`. The full output is below; `numbers=[-2,0,3]` stays unchanged.
- `index++`: `2 → 3`; continue to the next `for` check.

| index | condition | operation | input | returned value | result after | input after | next |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `2` | `✅ 2<3 → true` | `value+3` | `3` | `6` | `[1, 3, 6]` | `[-2, 0, 3]` | `index=3` |

- **❌ `for` condition `index < numbers.length`:** `3 < 3 → false`; skip `operation.applyAsInt(...)` and the assignment.
- `return result` returns `[1, 3, 6]`; this call ends with the input unchanged.

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
// numbers index:  0 1 2
// numbers      : -2 0 3
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
