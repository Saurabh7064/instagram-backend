# Micro-Lesson 04 — 3Sum

## Question

> Given an integer array, return every unique triplet whose values sum to zero, without returning duplicate triplets.

Practice source: [3Sum on LeetCode](https://leetcode.com/problems/3sum/)

## What you will learn

- how sorting exposes ordered pointer movement and adjacent duplicates;
- how fixing one anchor reduces a three-value problem to Two Sum II;
- why duplicate anchors and duplicate pointer values require separate skipping;
- how to preserve the caller's array by sorting a copy;
- why the optimized solution is `O(n²)` rather than `O(n³)`.

## Lesson status

- Learning status: `TODO`; initialized but not active
- Prerequisite: Two Sum II and duplicate-aware iteration
- Primary idea: fix one value, converge two pointers, and skip repeated values
- New terms: anchor, triplet, duplicate skipping

## Inputs, examples, and target

Assumptions:

- `numbers` is non-null.
- Output order does not matter, but each value-combination must appear once.
- Values are within LeetCode constraints, so adding three values is safe in an `int`.

Examples:

- `[-1, 0, 1, 2, -1, -4]` → `[[-1, -1, 2], [-1, 0, 1]]`
- `[0, 1, 1]` → `[]`
- `[0, 0, 0]` → `[[0, 0, 0]]`

Target: `O(n²)` time, excluding output, after `O(n log n)` sorting.

## Concept: reduce three values to two

After sorting, choose `sorted[anchor]`. The remaining problem is:

> Find two values to the right whose sum is `-sorted[anchor]`.

That is the sorted two-pointer problem from Two Sum II. For every anchor, `left` starts just after it and `right` starts at the array end.

## Why sorting a copy matters

```java
int[] sorted = Arrays.copyOf(numbers, numbers.length);
Arrays.sort(sorted);
```

Sorting enables pointer decisions and adjacent duplicate checks. Copying first keeps the method from changing the caller's array. Sorting the original is also valid when mutation is explicitly allowed, but that contract should be stated.

## Duplicate control

Three separate rules create unique output:

1. Skip an anchor equal to the previous anchor.
2. After finding a triplet, advance both pointers.
3. Continue advancing across values equal to the values just used.

For `[0, 0, 0, 0]`, these rules return `[0, 0, 0]` once rather than four equivalent index combinations.

## Reference approach

```java
for (int anchor = 0; anchor < sorted.length - 2; anchor++) {
    if (anchor > 0 && sorted[anchor] == sorted[anchor - 1]) {
        continue;
    }

    int left = anchor + 1;
    int right = sorted.length - 1;
    while (left < right) {
        int sum = sorted[anchor] + sorted[left] + sorted[right];
        if (sum == 0) {
            result.add(List.of(sorted[anchor], sorted[left], sorted[right]));
            left++;
            right--;
            while (left < right && sorted[left] == sorted[left - 1]) {
                left++;
            }
            while (left < right && sorted[right] == sorted[right + 1]) {
                right--;
            }
        } else if (sum < 0) {
            left++;
        } else {
            right--;
        }
    }
}
```

## Invariant and correctness

For a fixed anchor, every untested candidate pair remains between `left` and `right`. Sorted-order pointer moves discard only pairs that are too small or too large. Duplicate skipping removes repeated value-combinations only after the equivalent combination has been considered.

Every possible first value is considered once, and Two Sum II finds every complementary pair for it, so every unique zero-sum triplet is returned exactly once.

## Complexity and pitfalls

- Sorting: `O(n log n)`.
- Each of `n` anchors performs an `O(n)` pointer scan: `O(n²)` total.
- Additional working space: `O(n)` because this version sorts a copy; sorting the input can reduce explicit extra array space.

Common mistakes include skipping duplicates before recording a match, skipping only anchors but not pointer values, reusing the anchor position, and returning duplicate triplets.

## Practice after learning the concept

- Write here: [ThreeSumPractice.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/twopointers/threesum/ThreeSumPractice.java)
- Reveal after attempting: [ThreeSumSolution.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/twopointers/threesum/ThreeSumSolution.java)

## Questions and explained answers

<details>
<summary>1. How does fixing an anchor reduce the problem?</summary>

Once one value is fixed, the other two must sum to its negation, which is a sorted two-pointer search.

</details>

<details>
<summary>2. Why are duplicate checks needed for both anchors and pointer values?</summary>

Repeated anchors can recreate all pairs for the same first value, while repeated left/right values can recreate the same pair for one anchor.

</details>

<details>
<summary>3. Why is the total O(n²)?</summary>

There are up to `n` anchors, and each anchor performs one linear inward scan. Sorting is asymptotically smaller than the quadratic scans.

</details>

## Stop/go

Proceed only when you can reduce 3Sum to Two Sum II, explain all duplicate skips, derive `O(n²)`, implement it, and pass all checks.
