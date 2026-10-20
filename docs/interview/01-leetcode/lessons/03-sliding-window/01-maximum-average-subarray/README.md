# Micro-Lesson 01 — Maximum Average Subarray I

## Question

> Given an integer array and a fixed window size `k`, what is the maximum average among all contiguous subarrays of length `k`?

Practice source: [Maximum Average Subarray I on LeetCode](https://leetcode.com/problems/maximum-average-subarray-i/)

## What you will learn

- what a fixed-size sliding window represents;
- how neighboring windows overlap and therefore share most of their sum;
- how to update a rolling sum by adding the incoming value and removing the outgoing value;
- why the first complete window must initialize the best result;
- why maximizing sums is equivalent to maximizing averages when every window has equal size.

## Lesson status

- Learning status: `TODO`; initialized but not active
- Prerequisite: array indexing and running totals
- Primary idea: reuse the previous fixed-size window's sum
- New terms: fixed window, incoming value, outgoing value

## Inputs, examples, and target

Assumptions:

- `numbers` is non-null and non-empty.
- `1 <= windowSize <= numbers.length`.
- Values are within LeetCode constraints.

Examples:

- `[1, 12, -5, -6, 50, 3]`, `k = 4` → `12.75`
- `[5]`, `k = 1` → `5.0`
- `[-1, -12, -5, -6, -50, -3]`, `k = 2` → `-5.5`

Target: `O(n)` time and `O(1)` additional space.

:::simulation
type: fixed-window-average
language: java
nums: [1, 12, -5, -6, 50, 3, 7]
k: 4
:::

## Why recomputing every window repeats work

For `k = 4`, adjacent windows overlap in three positions:

```text
window 1: [1, 12, -5, -6]
window 2:    [12, -5, -6, 50]
```

Recalculating both sums repeats the work for `12`, `-5`, and `-6`. Instead:

```text
new sum = old sum - outgoing value + incoming value
```

Here the outgoing value is `1` and the incoming value is `50`.

## Build the first complete window

```java
long windowSum = 0;
for (int index = 0; index < windowSize; index++) {
    windowSum += numbers[index];
}
long bestSum = windowSum;
```

`bestSum` must start from a real window, not `0`. If every number is negative, initializing to `0` would invent a nonexistent better sum.

This version uses `long` for aggregation so the technique remains safe if future input constraints become larger, even though the stated LeetCode constraints fit in `int`.

## Slide by one position

```java
for (int right = windowSize; right < numbers.length; right++) {
    int outgoingIndex = right - windowSize;
    windowSum += numbers[right];
    windowSum -= numbers[outgoingIndex];
    bestSum = Math.max(bestSum, windowSum);
}
```

At each iteration:

- `right` identifies the incoming value;
- `right - windowSize` identifies the outgoing value;
- adding one and removing one keeps the window length exactly `k`.

## Convert the best sum to an average

```java
return bestSum / (double) windowSize;
```

All windows have the same positive denominator `k`. Therefore the window with the largest sum also has the largest average. Casting the denominator to `double` prevents integer division: `51 / 4` would be `12`, while `51 / 4.0` is `12.75`.

## Block-by-block code walkthrough

Original snapshots are retained. Where a value changes several times, its cell shows the sequence ending at the final value.

**Running example for the code and tables below:** Input `numbers = [1,12,-5,-6,50,3]`, `windowSize = 4`. The array stays unchanged. Initially `windowSum = 0` (a `long`). The two loops have different local variables: `index` belongs to the first; `right` and `outgoingIndex` belong to the second.

Tables belong to the code block immediately above them. Inputs stay unchanged unless a block explicitly creates or changes a copy. Collection order is displayed for readability; HashMap/HashSet order is unspecified.

### Block 1 — Create the rolling sum

```java
long windowSum = 0;
```

**What it evaluates:** the running total starts empty before any array value is added.

**Why it is needed:** later slides reuse this variable instead of recalculating each window.

**Concrete result:** for `[1, 12, -5, -6]`, the variable changes `0 → 1 → 13 → 8 → 2` while building the first window.

**What fails without it:** Java local variables must be initialized before `+=`. Using `int` is valid for the stated constraints, but `long` prevents overflow if larger constraints are introduced.

**Equivalent clearer form:** `long firstWindowSum = 0;` is a useful temporary name, but the value later becomes the rolling sum.

**Debugger state at this block:**

- Inputs: `numbers=[1,12,-5,-6,50,3]`, `windowSize=4`; the array stays unchanged.
- `long windowSum = 0` creates the rolling sum; neither loop has started.

### Block 2 — Build exactly the first complete window

```java
for (int index = 0; index < windowSize; index++) {
    windowSum += numbers[index];
}
```

**What it evaluates:** indexes `0` through `windowSize - 1` are added. For `windowSize = 4`, the loop uses `0, 1, 2, 3` and stops when `index = 4`.

**Why it is needed:** a slide requires a previous complete window, and the first window is also a valid answer candidate.

**What fails without it:** starting the slide loop with a zero sum would subtract values that were never added. Using `index <= windowSize` reads one extra element and can go out of bounds when the window fills the array.

**Equivalent clearer form:** `for (int value : Arrays.copyOfRange(numbers, 0, windowSize))` is less efficient because it allocates a copy.

**Debugger state at this block:**

| operation | index | right | outgoingIndex | windowSum | bestSum |
| --- | --- | --- | --- | --- | --- |
| `initial window` | `0` | `—` | `—` | `0 + (1) = 1` | `—` |
| `initial window` | `1` | `—` | `—` | `1 + (12) = 13` | `—` |
| `initial window` | `2` | `—` | `—` | `13 + (-5) = 8` | `—` |
| `initial window` | `3` | `—` | `—` | `8 + (-6) = 2` | `—` |


**Narrated execution — follow each operation, then check its values:**

**Iteration/check 1 — index = `0`.**

- **✅ `for (index < windowSize)`:** `0 < 4 → true`; enter the first-window loop.
- `windowSum += numbers[index]` reads `numbers[0]=1`: `0 + (1) = 1`.
- **State:** `windowSum=1`; `bestSum`, `right`, and `outgoingIndex` are not assigned yet.
- `index++`: `0 → 1`; **✅ `for (index < windowSize)`:** `1 < 4 → true` next.

| index | index < windowSize | windowSum after addition | next action |
| --- | --- | --- | --- |
| `0` | `✅ 0 < 4 → true` | `0+(1)=1` | `index++: 0 → 1; ✅ for (index < windowSize): 1 < 4 → true; continue` |

**Iteration/check 2 — index = `1`.**

- **✅ `for (index < windowSize)`:** `1 < 4 → true`; enter the first-window loop.
- `windowSum += numbers[index]` reads `numbers[1]=12`: `1 + (12) = 13`.
- **State:** `windowSum=13`; `bestSum`, `right`, and `outgoingIndex` are not assigned yet.
- `index++`: `1 → 2`; **✅ `for (index < windowSize)`:** `2 < 4 → true` next.

| index | index < windowSize | windowSum after addition | next action |
| --- | --- | --- | --- |
| `1` | `✅ 1 < 4 → true` | `1+(12)=13` | `index++: 1 → 2; ✅ for (index < windowSize): 2 < 4 → true; continue` |

**Iteration/check 3 — index = `2`.**

- **✅ `for (index < windowSize)`:** `2 < 4 → true`; enter the first-window loop.
- `windowSum += numbers[index]` reads `numbers[2]=-5`: `13 + (-5) = 8`.
- **State:** `windowSum=8`; `bestSum`, `right`, and `outgoingIndex` are not assigned yet.
- `index++`: `2 → 3`; **✅ `for (index < windowSize)`:** `3 < 4 → true` next.

| index | index < windowSize | windowSum after addition | next action |
| --- | --- | --- | --- |
| `2` | `✅ 2 < 4 → true` | `13+(-5)=8` | `index++: 2 → 3; ✅ for (index < windowSize): 3 < 4 → true; continue` |

**Iteration/check 4 — index = `3`.**

- **✅ `for (index < windowSize)`:** `3 < 4 → true`; enter the first-window loop.
- `windowSum += numbers[index]` reads `numbers[3]=-6`: `8 + (-6) = 2`.
- **State:** `windowSum=2`; `bestSum`, `right`, and `outgoingIndex` are not assigned yet.
- `index++`: `3 → 4`; **❌ `for (index < windowSize)`:** `4 < 4 → false`; exit this loop next.

| index | index < windowSize | windowSum after addition | next action |
| --- | --- | --- | --- |
| `3` | `✅ 3 < 4 → true` | `8+(-6)=2` | `index++: 3 → 4; ❌ for (index < windowSize): 4 < 4 → false; exit loop` |

**Iteration/check 5 — index = `4`.**

- **❌ `for (index < windowSize)`:** `4 < 4 → false`; exit the first loop.
- Skip `windowSum += numbers[index]`; `windowSum=2` remains. `numbers[4]` is not read by this loop.
- `index` leaves scope; next execute `long bestSum = windowSum` → `bestSum=2`.

| index | index < windowSize | windowSum after addition | next action |
| --- | --- | --- | --- |
| `4` | `❌ 4 < 4 → false` | `unchanged: 2` | `exit; bestSum=2` |

This loop has no conditional branch in its body; its loop condition is still shown explicitly.

### Block 3 — Initialize the best from real data

```java
long bestSum = windowSum;
```

**What it evaluates:** the first complete window becomes the best known candidate.

**Concrete result:** the first window `[1,12,-5,-6]` sums to `1 + 12 - 5 - 6 = 2`, so `bestSum=2`.

**Why it is needed:** every candidate may be negative.

**What fails without it:** initializing to `0` would invent a better result whenever every real window has a negative sum.

**Equivalent clearer form:** `long maximumWindowSum = windowSum;` is the same operation with a more explicit name.

**Debugger state at this block:**

| operation | index | right | outgoingIndex | windowSum | bestSum |
| --- | --- | --- | --- | --- | --- |
| `initialize bestSum` | `—` | `—` | `—` | `2` | `2` |

### Block 4 — Identify each incoming value

```java
for (int right = windowSize; right < numbers.length; right++) {
    int outgoingIndex = right - windowSize;
```

**What it evaluates:** `right` starts at the first index outside the initial window. Subtracting the fixed window size identifies the value leaving from the left.

**Concrete result:** with `k = 4`, the first incoming index is `4` and the outgoing index is `4 - 4 = 0`.

**What fails without it:** starting `right` at `windowSize - 1` adds the final first-window value twice. Using `right - 1` removes the adjacent value rather than the left boundary.

**Equivalent clearer form:** maintain a separate `left` variable and increment both pointers, but deriving `left` from `right` prevents them drifting apart.

**Debugger state at this block:**

| operation | index | right | outgoingIndex | windowSum | bestSum |
| --- | --- | --- | --- | --- | --- |
| `add incoming` | `—` | `4` | `0` | `2 + 50 = 52` | `2` |
| `add incoming` | `—` | `5` | `1` | `51 + 3 = 54` | `51` |

### Block 5 — Slide and preserve the maximum

```java
windowSum += numbers[right];
windowSum -= numbers[outgoingIndex];
bestSum = Math.max(bestSum, windowSum);
```

**What it evaluates:** one incoming value is added, one outgoing value is removed, and the resulting complete-window sum competes with the best.

**Concrete result:** the first example changes the first sum `2` by `+50 - 1` to `51`; `bestSum` becomes `51`.

**Why it is needed:** neighboring windows share all other positions, so these two updates preserve an exact length-`k` sum.

**What fails without it:** forgetting the subtraction grows the window; updating `bestSum` before both operations compares an incomplete window.

**Equivalent clearer form:** `windowSum += numbers[right] - numbers[outgoingIndex];` combines the arithmetic, while separate lines make each movement visible.

**Debugger state at this block:**

| operation | index | right | outgoingIndex | windowSum | bestSum |
| --- | --- | --- | --- | --- | --- |
| `add incoming` | `—` | `4` | `0` | `2 + 50 = 52` | `2` |
| `subtract outgoing; update best` | `—` | `4` | `0` | `52 - (1) = 51` | `max(2,51) = 51` |
| `add incoming` | `—` | `5` | `1` | `51 + 3 = 54` | `51` |
| `subtract outgoing; update best` | `—` | `5` | `1` | `54 - (12) = 42` | `max(51,42) = 51` |


**Narrated execution — follow each operation, then check its values:**

**Iteration/check 1 — right = `4`.**

- **✅ `for (right < numbers.length)`:** `4 < 6 → true`; enter the slide loop.
- `int outgoingIndex = right - windowSize`: `4 - 4 = 0`.
- `windowSum += numbers[right]` reads `numbers[4]=50`: `2 + 50 = 52`.
- `windowSum -= numbers[outgoingIndex]` reads `numbers[0]=1`: `52 - (1) = 51`.
- `bestSum = Math.max(bestSum, windowSum)`: `max(2, 51) = 51`; `bestSum: 2 → 51`.
- **State:** `right=4, outgoingIndex=0, windowSum=51, bestSum=51, windowSize=4`; `numbers` is unchanged.
- `right++`: `4 → 5`; **✅ `for (right < numbers.length)`:** `5 < 6 → true` next.

| right | right < numbers.length | outgoingIndex | sum after add | sum after removal | bestSum after | next action |
| --- | --- | --- | --- | --- | --- | --- |
| `4` | `✅ 4 < 6 → true` | `4-4=0` | `2+50=52` | `52-(1)=51` | `51` | `right++: 4 → 5; ✅ for (right < numbers.length): 5 < 6 → true; continue` |

**Iteration/check 2 — right = `5`.**

- **✅ `for (right < numbers.length)`:** `5 < 6 → true`; enter the slide loop.
- `int outgoingIndex = right - windowSize`: `5 - 4 = 1`.
- `windowSum += numbers[right]` reads `numbers[5]=3`: `51 + 3 = 54`.
- `windowSum -= numbers[outgoingIndex]` reads `numbers[1]=12`: `54 - (12) = 42`.
- `bestSum = Math.max(bestSum, windowSum)`: `max(51, 42) = 51`; `bestSum: 51 → 51`.
- **State:** `right=5, outgoingIndex=1, windowSum=42, bestSum=51, windowSize=4`; `numbers` is unchanged.
- `right++`: `5 → 6`; **❌ `for (right < numbers.length)`:** `6 < 6 → false`; exit this loop next.

| right | right < numbers.length | outgoingIndex | sum after add | sum after removal | bestSum after | next action |
| --- | --- | --- | --- | --- | --- | --- |
| `5` | `✅ 5 < 6 → true` | `5-4=1` | `51+3=54` | `54-(12)=42` | `51` | `right++: 5 → 6; ❌ for (right < numbers.length): 6 < 6 → false; exit loop` |

**Iteration/check 3 — right = `6`.**

- **❌ `for (right < numbers.length)`:** `6 < 6 → false`; exit the slide loop.
- Skip `outgoingIndex` creation, both `windowSum` updates, and `Math.max`; `windowSum=42`, `bestSum=51` remain.
- Next execute `return bestSum / (double) windowSize`: `51 / 4.0 = 12.75`.

| right | right < numbers.length | outgoingIndex | sum after add | sum after removal | bestSum after | next action |
| --- | --- | --- | --- | --- | --- | --- |
| `6` | `❌ 6 < 6 → false` | `Skipped` | `Skipped` | `Skipped` | `51` | `exit; return 51/4.0=12.75` |

No `if` exists in this loop: `Math.max` preserves the larger sum on each completed slide.

### Block 6 — Force floating-point division

```java
return bestSum / (double) windowSize;
```

**What it evaluates:** the best sum is divided by the positive fixed size after converting the denominator to `double`.

**Concrete result:** `51 / 4.0` produces `12.75`.

**Why it is needed:** equal window sizes mean the best sum also has the best average.

**What fails without it:** integer division would truncate the fraction; `51 / 4` produces `12` before conversion to `double`.

**Equivalent clearer form:** `(double) bestSum / windowSize` is identical because either floating-point operand promotes the division.

**Debugger state at this block:**

- **❌ `for (index < windowSize)`:** `4 < 4 → false`; the first window is complete.
- **❌ `for (right < numbers.length)`:** `6 < 6 → false`; all slides are complete.
- `return bestSum / (double) windowSize`: `51 / 4.0 = 12.75`; the cast preserves the fraction.
- Winning window: `[12,-5,-6,50]`; `12 - 5 - 6 + 50 = 51`.

## Complete reference solution

Runnable file: [MaximumAverageSubarraySolution.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/slidingwindow/fixedaverage/MaximumAverageSubarraySolution.java)

```java
// Input: numbers = [1, 12, -5, -6, 50, 3]; windowSize = 4
// Expected return: 12.75

static double findMaxAverage(int[] numbers, int windowSize) {
    long windowSum = 0;
    for (int index = 0; index < windowSize; index++) {
        windowSum += numbers[index];
    }

    long bestSum = windowSum;
    for (int right = windowSize; right < numbers.length; right++) {
        int outgoingIndex = right - windowSize;
        windowSum += numbers[right];
        windowSum -= numbers[outgoingIndex];
        bestSum = Math.max(bestSum, windowSum);
    }

    return bestSum / (double) windowSize;
}
```

Read it as three operations: build the first complete window, slide by adding one value and removing one value, then convert the largest sum to an average. The next section explains every block and boundary in this method.

## Invariant and correctness

After each update, `windowSum` equals the sum of the current length-`k` window ending at `right`. `bestSum` equals the largest complete-window sum seen so far. Every valid window is initialized or reached by exactly one slide, so the final best sum—and therefore the final average—is correct.

## Complexity and alternatives

- Time: `O(n)` because initialization and all slides together visit each position a constant number of times.
- Additional space: `O(1)`.
- Baseline: summing each of roughly `n` windows from scratch takes `O(n × k)`.
- Prefix sums can answer many arbitrary range-sum queries, but one fixed-window scan needs only a rolling sum.

## Common pitfalls

- initializing `bestSum` to zero when all windows may be negative;
- forgetting to remove the outgoing value;
- removing `numbers[right - 1]` instead of `numbers[right - windowSize]`;
- dividing as integers and losing the fractional part;
- solving arbitrary subsets instead of contiguous windows.

## Practice after learning the concept

- Write here: [MaximumAverageSubarrayPractice.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/slidingwindow/fixedaverage/MaximumAverageSubarrayPractice.java)
- Reveal after attempting: [MaximumAverageSubarraySolution.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/slidingwindow/fixedaverage/MaximumAverageSubarraySolution.java)

## Questions and explained answers

<details>
<summary>1. Which values change when a fixed window moves one position?</summary>

Exactly one old value leaves and one new value enters. Every other value remains in both windows.

</details>

<details>
<summary>2. Why must bestSum start from the first complete window?</summary>

It must represent a valid candidate. Starting from zero is incorrect when every valid window has a negative sum.

</details>

<details>
<summary>3. Why can we maximize the sum instead of computing every average?</summary>

Every window is divided by the same positive `k`, so sum order and average order are identical.

</details>

## Stop/go

Proceed only when you can identify incoming/outgoing indexes, state the rolling-sum invariant, explain negative input and floating-point division, implement it, and pass all checks.
