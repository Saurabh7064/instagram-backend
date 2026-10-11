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

## How the example results are calculated

Array positions are indexes, starting at `0`:

```text
index:   0  1  2  3  4  5  6  7  8
height:  1  8  6  2  5  4  8  3  7
            ^                    ^
          left                 right
```

The best pair uses the lines at indexes `1` and `8`:

```text
left index       = 1, height = 8
right index      = 8, height = 7
width            = right - left = 8 - 1 = 7
limiting height  = min(8, 7) = 7
area             = width × limiting height = 7 × 7 = 49
```

The width is `7`, not `8`: width is the distance between indexes `1` and `8`. The usable height is `7`, not `8`, because water would spill over the shorter right line.

### Visual: where the `49` units of water are stored

```text
height
  8      │                         ← left wall reaches height 8
  7      │~~~~~~~~~~~~~~~~~~~~~~~~│ ← water stops at right wall's height 7
  6      │~~~~~~~~~~~~~~~~~~~~~~~~│
  5      │~~~~~~~~~~~~~~~~~~~~~~~~│       ~ = water
  4      │~~~~~~~~~~~~~~~~~~~~~~~~│       │ = chosen wall
  3      │~~~~~~~~~~~~~~~~~~~~~~~~│
  2      │~~~~~~~~~~~~~~~~~~~~~~~~│
  1      │~~~~~~~~~~~~~~~~~~~~~~~~│
  0      └────────────────────────┘
         index 1     width 7      index 8
         height 8                 height 7
```

Think of the water as a rectangle between the two chosen vertical lines:

```text
7 horizontal units × 7 vertical units = 49 square units
```

The left wall has one extra unit above the water. It does not increase the stored water because the right wall is only `7` units high. The lines at indexes `2` through `7` are other possible boundaries, but they have zero width and do not subtract from the area between the selected outer walls.

The two-pointer scan proves that no checked pair has a larger area:

| Left index/height | Right index/height | Width | Limiting height | Area | Best so far | Pointer moved |
|---|---|---:|---:|---:|---:|---|
| `0 / 1` | `8 / 7` | `8` | `1` | `8 × 1 = 8` | `8` | left |
| `1 / 8` | `8 / 7` | `7` | `7` | `7 × 7 = 49` | `49` | right |
| `1 / 8` | `7 / 3` | `6` | `3` | `6 × 3 = 18` | `49` | right |
| `1 / 8` | `6 / 8` | `5` | `8` | `5 × 8 = 40` | `49` | left |
| `2 / 6` | `6 / 8` | `4` | `6` | `4 × 6 = 24` | `49` | left |
| `3 / 2` | `6 / 8` | `3` | `2` | `3 × 2 = 6` | `49` | left |
| `4 / 5` | `6 / 8` | `2` | `5` | `2 × 5 = 10` | `49` | left |
| `5 / 4` | `6 / 8` | `1` | `4` | `1 × 4 = 4` | `49` | left; stop |

`bestArea` becomes `49` on the second row and stays `49` because every later area is smaller.

The smaller examples use the same formula:

```text
[1, 1]
indexes 0 and 1: width = 1 - 0 = 1
height = min(1, 1) = 1
area = 1 × 1 = 1

[4, 3, 2, 1, 4]
indexes 0 and 4: width = 4 - 0 = 4
height = min(4, 4) = 4
area = 4 × 4 = 16
```

For the last example, no pair can beat `16`: the greatest possible width is `4`, and no line is taller than `4`.

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

## Block-by-block code walkthrough

### Block 1 — Initialize boundaries and a real lower bound

```java
int left = 0;
int right = heights.length - 1;
int bestArea = 0;
```

**What it evaluates:** the pointers select the widest possible container. `bestArea = 0` is valid because heights and therefore all areas are nonnegative.

**Concrete result:** for `[1, 8, 6]`, `left = 0`, `right = 2`, and the initial candidate width is `2`.

**What fails without it:** starting `right` at `heights.length` causes an invalid array access. Leaving `bestArea` uninitialized does not compile.

**Equivalent clearer form:** `int lastIndex = heights.length - 1;` may be introduced before assigning `right`.

### Block 2 — Inspect only genuine pairs

```java
while (left < right) {
```

**What it evaluates:** two distinct lines remain. For `[1, 1]`, the only iteration uses indexes `0` and `1`.

**Why it is needed:** one line cannot form a container with itself, and the loop must stop when every useful boundary was eliminated.

**What fails without it:** `left <= right` evaluates a zero-width self-pair unnecessarily; no stopping condition creates an infinite loop.

**Equivalent clearer form:** `while (right - left >= 1)` is equivalent but obscures the pointer relationship.

### Block 3 — Calculate the current container

```java
int width = right - left;
int limitingHeight = Math.min(heights[left], heights[right]);
bestArea = Math.max(bestArea, width * limitingHeight);
```

**What it evaluates:** width is the index distance; `Math.min` chooses the water level; `Math.max` preserves the best area seen.

**Concrete result:** for indexes `1` and `8` in the standard example, width is `7`, limiting height is `min(8, 7) = 7`, area is `49`, and `bestArea` becomes `49`.

**Why it is needed:** returning only the final pair would forget a larger container examined earlier.

**What fails without it:** using `Math.max` for the height imagines water above the shorter wall. Using `right - left + 1` counts positions rather than distance and overstates width.

**Equivalent clearer form:** compute `int area = width * limitingHeight; if (area > bestArea) bestArea = area;` instead of `Math.max`.

### Block 4 — Remove the limiting boundary

```java
if (heights[left] <= heights[right]) {
    left++;
} else {
    right--;
}
```

**What it evaluates:** the shorter boundary moves inward. With equal heights, this implementation chooses the left boundary.

**Concrete result:** if the boundary heights are `1` and `7`, moving `left` is the only move that might find a taller limiting wall. Keeping height `1` while shrinking width cannot improve area.

**Why it is needed:** each move discards a boundary whose closer pairs are dominated.

**What fails without it:** moving the taller line can discard a useful tall boundary while retaining the same short limitation. Moving neither pointer loops forever.

**Equivalent clearer form:** compare named values `leftHeight` and `rightHeight`; moving `right` on equality is also correct.

### Block 5 — Return the maximum, not the last area

```java
return bestArea;
```

**What it evaluates:** after all safe eliminations, `bestArea` contains the greatest area evaluated.

**Concrete results:** `[1, 1]` returns `1`; `[0, 2]` returns `0`.

**What fails without it:** returning the most recently calculated `area` can lose an earlier maximum and does not handle the value outside the loop cleanly.

**Equivalent clearer form:** none is simpler; `bestArea` is the maintained answer.

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
