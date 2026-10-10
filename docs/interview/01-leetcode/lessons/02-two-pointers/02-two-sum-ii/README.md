# Micro-Lesson 02 — Two Sum II

## Question

> Given a sorted integer array with exactly one valid pair, return the pair's one-based indexes when its values add to the target.

Practice source: [Two Sum II on LeetCode](https://leetcode.com/problems/two-sum-ii-input-array-is-sorted/)

## What you will learn

- how sorted order turns a sum comparison into a safe pointer decision;
- why a sum that is too small eliminates the left boundary;
- why a sum that is too large eliminates the right boundary;
- how this differs from the HashMap solution for unsorted Two Sum;
- why the result uses one-based indexes even though Java arrays are zero-based.

## Lesson status

- Learning status: `TODO`; initialized but not active
- Prerequisite: Valid Palindrome pointer movement and sorted arrays
- Primary idea: converge on a target using monotonic order
- New terms: left pointer, right pointer, monotonic order

## Inputs, examples, and target

Assumptions:

- `numbers` is sorted in nondecreasing order.
- Exactly one solution exists and one position cannot be reused.
- Values and target are between `-1,000` and `1,000`, so adding two values is safe in an `int`.
- Return one-based indexes.

Examples:

- `[2, 7, 11, 15]`, target `9` → `[1, 2]`
- `[2, 3, 4]`, target `6` → `[1, 3]`
- `[-1, 0]`, target `-1` → `[1, 2]`

Target: `O(n)` time and `O(1)` additional space.

## Concept: sorted order eliminates candidates

Start with the smallest and largest values. Their sum has three meanings:

- equal to target: the answer is found;
- below target: the left value is too small even with the largest available partner;
- above target: the right value is too large even with the smallest available partner.

If `[2, 3, 4]` targets `6`, the first sum is `2 + 4 = 6`. If it targeted `7`, a sum of `6` would eliminate `2`, because pairing `2` with any smaller right-side value cannot increase the sum.

## Reference approach

```java
int left = 0;
int right = numbers.length - 1;

while (left < right) {
    int sum = numbers[left] + numbers[right];
    if (sum == target) {
        return new int[] {left + 1, right + 1};
    }
    if (sum < target) {
        left++;
    } else {
        right--;
    }
}
```

The `+ 1` happens only when forming the answer. Pointer movement continues to use zero-based Java indexes.

## Invariant and correctness

Before each iteration, any valid solution not already ruled out must lie within `[left, right]`. A too-small sum proves `numbers[left]` cannot participate in the solution; a too-large sum proves `numbers[right]` cannot participate. Each move removes only an impossible boundary, so the guaranteed solution is eventually found.

## Complexity and trade-off

- Time: `O(n)` because one pointer moves each iteration and neither reverses.
- Additional space: `O(1)`.
- Trade-off: this elimination proof depends on sorted input. For unsorted input, use a value-to-index map or sort while preserving original indexes.

## Common pitfalls

- moving the wrong pointer after comparing the sum;
- returning zero-based indexes;
- using `left <= right` and allowing one position to pair with itself;
- applying this method to unsorted data.

## Practice after learning the concept

- Write here: [TwoSumSortedPractice.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/twopointers/twosumii/TwoSumSortedPractice.java)
- Reveal after attempting: [TwoSumSortedSolution.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/twopointers/twosumii/TwoSumSortedSolution.java)

## Questions and explained answers

<details>
<summary>1. Why can a too-small sum discard the left value?</summary>

The current right value is the largest possible partner. If even that pair is too small, pairing the same left value with anything smaller also fails.

</details>

<details>
<summary>2. Why does the algorithm require sorted input?</summary>

The pointer move relies on knowing that moving right increases the left-side candidate and moving left decreases the right-side candidate.

</details>

<details>
<summary>3. Why is the result one-based?</summary>

That is part of this problem's output contract. The algorithm uses normal zero-based Java indexes internally and adds one only when returning.

</details>

## Stop/go

Proceed only when you can prove both pointer moves, distinguish this from unsorted Two Sum, implement it, and pass all checks.
