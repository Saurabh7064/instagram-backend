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
