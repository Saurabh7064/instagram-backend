# Micro-Lesson 03 — Container With Most Water

## Question

> Given vertical-line heights, choose two lines that hold the most water between them.

Practice source: [Container With Most Water on LeetCode](https://leetcode.com/problems/container-with-most-water/)

## What you will learn

- how width and the shorter boundary determine container area;
- why moving the taller boundary cannot improve the current limitation;
- how to identify and discard a dominated boundary;
- how an elimination proof replaces testing every pair;
- why equal boundaries allow either pointer to move.

## Lesson status

- Learning status: `TODO`; initialized but not active
- Prerequisite: Two Sum II pointer-elimination reasoning
- Primary idea: discard the boundary that limits the current result
- New terms: limiting height, width, dominated boundary

## Inputs, examples, and target

Assumptions:

- `heights` is non-null and contains at least two nonnegative integers.
- The stated LeetCode constraints keep `width × height` inside Java's `int` range.

Examples:

- `[1, 8, 6, 2, 5, 4, 8, 3, 7]` → `49`
- `[1, 1]` → `1`
- `[4, 3, 2, 1, 4]` → `16`

Target: `O(n)` time and `O(1)` additional space.

## Concept: area has two factors

```text
width           = right - left
limiting height = min(height[left], height[right])
area            = width × limiting height
```

Water cannot rise above the shorter line. Moving either pointer inward always decreases width, so a future area can improve only if the limiting height increases.

## Why move the shorter boundary

Suppose the left height is shorter. Keeping it and moving the right pointer:

- makes the width smaller;
- keeps the limiting height at most the same short left height.

That cannot improve the area. Therefore every pair using that left boundary and a closer right boundary is dominated, and `left` can be advanced safely.

## Reference approach

```java
int left = 0;
int right = heights.length - 1;
int bestArea = 0;

while (left < right) {
    int width = right - left;
    int limitingHeight = Math.min(heights[left], heights[right]);
    bestArea = Math.max(bestArea, width * limitingHeight);

    if (heights[left] <= heights[right]) {
        left++;
    } else {
        right--;
    }
}
return bestArea;
```

When heights are equal, either boundary may move: both are current limiting heights, and keeping one with a smaller width cannot produce a larger area unless the other boundary changes.

## Correctness reasoning

Each iteration evaluates the current pair and removes one boundary that cannot produce a better area with any remaining partner. Because only dominated pairs are discarded, at least one optimal pair remains until it is evaluated. `bestArea` therefore records the global maximum.

## Complexity

- Time: `O(n)` because the two pointers move inward at most `n - 1` times.
- Additional space: `O(1)`.
- Baseline comparison: checking every pair takes `O(n²)`.

## Common pitfalls

- using the taller height instead of the shorter height;
- moving the taller boundary;
- forgetting that width is an index difference, not a count of included positions;
- returning the last area instead of the maximum seen.

## Practice after learning the concept

- Write here: [ContainerMostWaterPractice.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/twopointers/containermostwater/ContainerMostWaterPractice.java)
- Reveal after attempting: [ContainerMostWaterSolution.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/twopointers/containermostwater/ContainerMostWaterSolution.java)

## Questions and explained answers

<details>
<summary>1. Why is the shorter boundary the limiting height?</summary>

Water above the shorter line would spill over it, so only the smaller of the two heights can contribute to the container height.

</details>

<details>
<summary>2. Why can moving the taller boundary not help while the shorter one remains?</summary>

Width decreases, while the unchanged shorter line keeps the limiting height from increasing. The resulting area cannot be larger.

</details>

<details>
<summary>3. What makes this O(n) rather than O(n²)?</summary>

Each iteration discards one boundary permanently instead of pairing every line with every other line.

</details>

## Stop/go

Proceed only when you can derive the area formula, prove the shorter-boundary move, implement it, and pass all checks.
