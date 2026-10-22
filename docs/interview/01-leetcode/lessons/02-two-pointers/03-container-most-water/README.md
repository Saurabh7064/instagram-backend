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

Target: `O(n)` time and `O(1)` additional space.

## First understand the idea

The key idea is to calculate the capacity between two lines, then discard the shorter boundary because keeping it while reducing the width cannot help.

Use `heights=[1,8,6,2,5,4,8,3,7]`. Let `left` and `right` be line indexes, `width` the distance between them, and `bestArea` the largest capacity found. Begin at 0 and 8: width is `8-0=8`; water height is limited by the shorter line, `min(1,7)=1`. Capacity is `8*1=8`, so `bestArea=max(0,8)=8`.

Move left past height 1. At indexes 1 and 8, width is `8-1=7` and the limiting height is `min(8,7)=7`. Capacity is `7*7=49`, so the best becomes 49. The remaining comparisons do not beat it; return 49.

In general, `area = (right-left) * min(heights[left],heights[right])`. The subtraction counts horizontal distance; the minimum prevents water exceeding the shorter wall. Keeping that shorter wall and choosing a closer partner reduces width without increasing the limiting height, so move the shorter side inward after recording the current area.

:::simulation
type: container-water
language: java
values: [1, 8, 6, 2, 5, 4, 8, 3, 7]
:::

## Block-by-block code walkthrough

Original snapshots are retained. Where a value changes several times, its cell shows the sequence ending at the final value.

**Running example for the code and tables below:** Input `heights = [1,8,6,2,5,4,8,3,7]`; initially `left = 0`, `right = 8`, `bestArea = 0`. The array stays unchanged. Each row calculates the area before moving a pointer.

Tables belong to the code block immediately above them. Inputs stay unchanged unless a block explicitly creates or changes a copy. Collection order is displayed for readability; HashMap/HashSet order is unspecified.

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

**Debugger state at this block:**

`left=0`, `right=9-1=8`, `bestArea=0`; `width` and `limitingHeight` are not assigned.

### Block 2 — Inspect only genuine pairs

```java
while (left < right) {
```

**What it evaluates:** two distinct lines remain. For `[1, 1]`, the only iteration uses indexes `0` and `1`.

**Why it is needed:** one line cannot form a container with itself, and the loop must stop when every useful boundary was eliminated.

**What fails without it:** `left <= right` evaluates a zero-width self-pair unnecessarily; no stopping condition creates an infinite loop.

**Equivalent clearer form:** `while (right - left >= 1)` is equivalent but obscures the pointer relationship.

**Debugger state at this block:**

| left | right | heights[left] | heights[right] | width | limitingHeight | area calculation | bestArea update | movement |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `0` | `8` | `1` | `7` | `8` | `1` | `8 * 1 = 8` | `max(0,8) = 8` | `left++` |
| `1` | `8` | `8` | `7` | `7` | `7` | `7 * 7 = 49` | `max(8,49) = 49` | `right--` |
| `1` | `7` | `8` | `3` | `6` | `3` | `6 * 3 = 18` | `max(49,18) = 49` | `right--` |
| `1` | `6` | `8` | `8` | `5` | `8` | `5 * 8 = 40` | `max(49,40) = 49` | `left++` |
| `2` | `6` | `6` | `8` | `4` | `6` | `4 * 6 = 24` | `max(49,24) = 49` | `left++` |
| `3` | `6` | `2` | `8` | `3` | `2` | `3 * 2 = 6` | `max(49,6) = 49` | `left++` |
| `4` | `6` | `5` | `8` | `2` | `5` | `2 * 5 = 10` | `max(49,10) = 49` | `left++` |
| `5` | `6` | `4` | `8` | `1` | `4` | `1 * 4 = 4` | `max(49,4) = 49` | `left++` |

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

**Debugger state at this block:**

| left | right | heights[left] | heights[right] | width | limitingHeight | area calculation | bestArea update | movement |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `0` | `8` | `1` | `7` | `8` | `1` | `8 * 1 = 8` | `max(0,8) = 8` | `left++` |
| `1` | `8` | `8` | `7` | `7` | `7` | `7 * 7 = 49` | `max(8,49) = 49` | `right--` |
| `1` | `7` | `8` | `3` | `6` | `3` | `6 * 3 = 18` | `max(49,18) = 49` | `right--` |
| `1` | `6` | `8` | `8` | `5` | `8` | `5 * 8 = 40` | `max(49,40) = 49` | `left++` |
| `2` | `6` | `6` | `8` | `4` | `6` | `4 * 6 = 24` | `max(49,24) = 49` | `left++` |
| `3` | `6` | `2` | `8` | `3` | `2` | `3 * 2 = 6` | `max(49,6) = 49` | `left++` |
| `4` | `6` | `5` | `8` | `2` | `5` | `2 * 5 = 10` | `max(49,10) = 49` | `left++` |
| `5` | `6` | `4` | `8` | `1` | `4` | `1 * 4 = 4` | `max(49,4) = 49` | `left++` |

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

**Debugger state at this block:**

| left | right | heights[left] | heights[right] | width | limitingHeight | area calculation | bestArea update | movement |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `0` | `8` | `1` | `7` | `8` | `1` | `8 * 1 = 8` | `max(0,8) = 8` | `left++` |
| `1` | `8` | `8` | `7` | `7` | `7` | `7 * 7 = 49` | `max(8,49) = 49` | `right--` |
| `1` | `7` | `8` | `3` | `6` | `3` | `6 * 3 = 18` | `max(49,18) = 49` | `right--` |
| `1` | `6` | `8` | `8` | `5` | `8` | `5 * 8 = 40` | `max(49,40) = 49` | `left++` |
| `2` | `6` | `6` | `8` | `4` | `6` | `4 * 6 = 24` | `max(49,24) = 49` | `left++` |
| `3` | `6` | `2` | `8` | `3` | `2` | `3 * 2 = 6` | `max(49,6) = 49` | `left++` |
| `4` | `6` | `5` | `8` | `2` | `5` | `2 * 5 = 10` | `max(49,10) = 49` | `left++` |
| `5` | `6` | `4` | `8` | `1` | `4` | `1 * 4 = 4` | `max(49,4) = 49` | `left++` |


**Narrated execution — follow each operation, then check its values:**

**Iteration/check 1 — left = `0`.**

- **✅ `while (left < right)`:** `0 < 8 → true`.
- `int width = right - left`: `8 - 0 = 8`.
- `int limitingHeight = Math.min(heights[left], heights[right])`: `min(1,7) = 1`.
- `bestArea = Math.max(bestArea, width * limitingHeight)`: `8 * 1 = 8`, then `max(0,8) = 8`; `bestArea` changes `0 → 8`.
- **✅ `if (heights[left] <= heights[right])`:** `1 <= 7 → true`; execute `left++`: `0 → 1`; skip `else`.
- **State:** `left=1`, `right=8`, `width=8`, `limitingHeight=1`, `bestArea=8`; `heights` is unchanged. Recheck the `while`.

| left | right | loop condition | width | limitingHeight | area | bestArea after | heights[left] <= heights[right] | next action |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `0` | `8` | `✅ 0 < 8 → true` | `8` | `1` | `8*1=8` | `8` | `✅ 1 <= 7 → true` | `left=1` |

**Iteration/check 2 — left = `1`.**

- **✅ `while (left < right)`:** `1 < 8 → true`.
- `int width = right - left`: `8 - 1 = 7`.
- `int limitingHeight = Math.min(heights[left], heights[right])`: `min(8,7) = 7`.
- `bestArea = Math.max(bestArea, width * limitingHeight)`: `7 * 7 = 49`, then `max(8,49) = 49`; `bestArea` changes `8 → 49`.
- **❌ `if (heights[left] <= heights[right])`:** `8 <= 7 → false`; skip `left++`; execute `right--` in `else`: `8 → 7`.
- **State:** `left=1`, `right=7`, `width=7`, `limitingHeight=7`, `bestArea=49`; `heights` is unchanged. Recheck the `while`.

| left | right | loop condition | width | limitingHeight | area | bestArea after | heights[left] <= heights[right] | next action |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `1` | `8` | `✅ 1 < 8 → true` | `7` | `7` | `7*7=49` | `49` | `❌ 8 <= 7 → false` | `right=7` |

**Iteration/check 3 — left = `1`.**

- **✅ `while (left < right)`:** `1 < 7 → true`.
- `int width = right - left`: `7 - 1 = 6`.
- `int limitingHeight = Math.min(heights[left], heights[right])`: `min(8,3) = 3`.
- `bestArea = Math.max(bestArea, width * limitingHeight)`: `6 * 3 = 18`, then `max(49,18) = 49`; `bestArea` changes `49 → 49`.
- **❌ `if (heights[left] <= heights[right])`:** `8 <= 3 → false`; skip `left++`; execute `right--` in `else`: `7 → 6`.
- **State:** `left=1`, `right=6`, `width=6`, `limitingHeight=3`, `bestArea=49`; `heights` is unchanged. Recheck the `while`.

| left | right | loop condition | width | limitingHeight | area | bestArea after | heights[left] <= heights[right] | next action |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `1` | `7` | `✅ 1 < 7 → true` | `6` | `3` | `6*3=18` | `49` | `❌ 8 <= 3 → false` | `right=6` |

**Iteration/check 4 — left = `1`.**

- **✅ `while (left < right)`:** `1 < 6 → true`.
- `int width = right - left`: `6 - 1 = 5`.
- `int limitingHeight = Math.min(heights[left], heights[right])`: `min(8,8) = 8`.
- `bestArea = Math.max(bestArea, width * limitingHeight)`: `5 * 8 = 40`, then `max(49,40) = 49`; `bestArea` changes `49 → 49`.
- **✅ `if (heights[left] <= heights[right])`:** `8 <= 8 → true`; execute `left++`: `1 → 2`; skip `else`.
- **State:** `left=2`, `right=6`, `width=5`, `limitingHeight=8`, `bestArea=49`; `heights` is unchanged. Recheck the `while`.

| left | right | loop condition | width | limitingHeight | area | bestArea after | heights[left] <= heights[right] | next action |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `1` | `6` | `✅ 1 < 6 → true` | `5` | `8` | `5*8=40` | `49` | `✅ 8 <= 8 → true` | `left=2` |

**Iteration/check 5 — left = `2`.**

- **✅ `while (left < right)`:** `2 < 6 → true`.
- `int width = right - left`: `6 - 2 = 4`.
- `int limitingHeight = Math.min(heights[left], heights[right])`: `min(6,8) = 6`.
- `bestArea = Math.max(bestArea, width * limitingHeight)`: `4 * 6 = 24`, then `max(49,24) = 49`; `bestArea` changes `49 → 49`.
- **✅ `if (heights[left] <= heights[right])`:** `6 <= 8 → true`; execute `left++`: `2 → 3`; skip `else`.
- **State:** `left=3`, `right=6`, `width=4`, `limitingHeight=6`, `bestArea=49`; `heights` is unchanged. Recheck the `while`.

| left | right | loop condition | width | limitingHeight | area | bestArea after | heights[left] <= heights[right] | next action |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `2` | `6` | `✅ 2 < 6 → true` | `4` | `6` | `4*6=24` | `49` | `✅ 6 <= 8 → true` | `left=3` |

**Iteration/check 6 — left = `3`.**

- **✅ `while (left < right)`:** `3 < 6 → true`.
- `int width = right - left`: `6 - 3 = 3`.
- `int limitingHeight = Math.min(heights[left], heights[right])`: `min(2,8) = 2`.
- `bestArea = Math.max(bestArea, width * limitingHeight)`: `3 * 2 = 6`, then `max(49,6) = 49`; `bestArea` changes `49 → 49`.
- **✅ `if (heights[left] <= heights[right])`:** `2 <= 8 → true`; execute `left++`: `3 → 4`; skip `else`.
- **State:** `left=4`, `right=6`, `width=3`, `limitingHeight=2`, `bestArea=49`; `heights` is unchanged. Recheck the `while`.

| left | right | loop condition | width | limitingHeight | area | bestArea after | heights[left] <= heights[right] | next action |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `3` | `6` | `✅ 3 < 6 → true` | `3` | `2` | `3*2=6` | `49` | `✅ 2 <= 8 → true` | `left=4` |

**Iteration/check 7 — left = `4`.**

- **✅ `while (left < right)`:** `4 < 6 → true`.
- `int width = right - left`: `6 - 4 = 2`.
- `int limitingHeight = Math.min(heights[left], heights[right])`: `min(5,8) = 5`.
- `bestArea = Math.max(bestArea, width * limitingHeight)`: `2 * 5 = 10`, then `max(49,10) = 49`; `bestArea` changes `49 → 49`.
- **✅ `if (heights[left] <= heights[right])`:** `5 <= 8 → true`; execute `left++`: `4 → 5`; skip `else`.
- **State:** `left=5`, `right=6`, `width=2`, `limitingHeight=5`, `bestArea=49`; `heights` is unchanged. Recheck the `while`.

| left | right | loop condition | width | limitingHeight | area | bestArea after | heights[left] <= heights[right] | next action |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `4` | `6` | `✅ 4 < 6 → true` | `2` | `5` | `2*5=10` | `49` | `✅ 5 <= 8 → true` | `left=5` |

**Iteration/check 8 — left = `5`.**

- **✅ `while (left < right)`:** `5 < 6 → true`.
- `int width = right - left`: `6 - 5 = 1`.
- `int limitingHeight = Math.min(heights[left], heights[right])`: `min(4,8) = 4`.
- `bestArea = Math.max(bestArea, width * limitingHeight)`: `1 * 4 = 4`, then `max(49,4) = 49`; `bestArea` changes `49 → 49`.
- **✅ `if (heights[left] <= heights[right])`:** `4 <= 8 → true`; execute `left++`: `5 → 6`; skip `else`.
- **State:** `left=6`, `right=6`, `width=1`, `limitingHeight=4`, `bestArea=49`; `heights` is unchanged. Recheck the `while`.

| left | right | loop condition | width | limitingHeight | area | bestArea after | heights[left] <= heights[right] | next action |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `5` | `6` | `✅ 5 < 6 → true` | `1` | `4` | `1*4=4` | `49` | `✅ 4 <= 8 → true` | `left=6` |

**Iteration/check 9 — left = `6`.**

- **❌ `while (left < right)`:** `6 < 6 → false`; exit.
- Width/height calculations, `Math.max`, and pointer `if` are `Skipped`; `bestArea` stays `49`.
- **Final state:** `left=6`, `right=6`; execute `return bestArea` → `49`.

| left | right | loop condition | width | limitingHeight | area | bestArea after | heights[left] <= heights[right] | next action |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `6` | `6` | `❌ 6 < 6 → false` | `Skipped` | `Skipped` | `Skipped` | `49` | `Skipped` | `exit; return 49` |

The best is retained even as later areas decrease. Equal boundary heights take the `<=` branch and advance left.

### Block 5 — Return the maximum, not the last area

```java
return bestArea;
```

**What it evaluates:** after all safe eliminations, `bestArea` contains the greatest area evaluated.

**Concrete results:** `[1, 1]` returns `1`; `[0, 2]` returns `0`.

**What fails without it:** returning the most recently calculated `area` can lose an earlier maximum and does not handle the value outside the loop cleanly.

**Equivalent clearer form:** none is simpler; `bestArea` is the maintained answer.

**Debugger state at this block:**

The maximum is found at indexes `1` and `8`: width `8 - 1 = 7`, limiting height `min(8,7) = 7`, area `7 * 7 = 49`. The later equal-height comparison at `(1,6)` uses `<=`, so left advances. Eventually `left=6`, `right=6`; `left < right` is false and return `49`.

## Reference approach

```java
// heights index: 0 1 2 3 4 5 6 7 8
// heights      : 1 8 6 2 5 4 8 3 7
// Input: heights = [1, 8, 6, 2, 5, 4, 8, 3, 7]
// Expected return: 49

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
