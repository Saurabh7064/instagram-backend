# Micro-Lesson 01 — Range Sum Query — Immutable

## Question

Given a fixed integer array, implement `NumArray(numbers)` and `sumRange(left, right)`. Each query must return the sum from index `left` through index `right`, inclusive. The stored values do not change between queries.

Problem: [Range Sum Query — Immutable on LeetCode](https://leetcode.com/problems/range-sum-query-immutable/) · Editable Java: [RangeSumQueryPractice.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/prefixintervals/rangesum/RangeSumQueryPractice.java)

## What you will learn

- Answer a range query by subtracting two saved cumulative totals.
- Explain the extra leading zero and the inclusive right endpoint.
- Separate one-time construction from repeated constant-time queries.

## Lesson status and prerequisites

- Learning status: `TODO`; this artifact is prepared, not activated or mastered.
- Prerequisite: Array indexes and addition; revisit [fixed-size sums](../../03-sliding-window/01-maximum-average-subarray/README.md) if needed.
- Primary idea: Answer a range query by subtracting two saved cumulative totals.
- New terms (at most three): prefix total, boundary, query

## Inputs and limits

`1 <= numbers.length <= 10,000`, each value is between `-100,000` and `100,000`; valid queries satisfy `0 <= left <= right < numbers.length`. At most 10,000 queries. Target: `O(n)` construction, `O(1)` per query, `O(n)` saved space. Under these bounds any stored total has magnitude at most `10,000 × 100,000 = 1,000,000,000`, so Java `int` is sufficient. Invalid queries are outside this lesson’s contract.

## First find the answer in learner language

The problem is repeated work: adding the same positions for many requests. A **query** is one request for a range sum. Save the total seen so far once, then subtract away the part before the requested range. A **prefix total** is the sum of the values before a position. A **boundary** is a position between values: boundary `0` is before the first value, and boundary `n` is after the last.

Choose these steps before coding:

1. Save zero at boundary `0`: nothing has been added yet.
2. For each value, add it to the previous total and store that result at the next boundary.
3. For a query `[left,right]`, take the total at boundary `right+1`, which includes `right`.
4. Subtract the total at boundary `left`, removing exactly the values before `left`.

For `[2,5]` in `[-2,0,3,-5,2,-1,4]`, the sum through index `5` is `-3`; the sum before index `2` is `-2`. Subtracting gives `-3 - (-2) = -1`. Direct addition agrees: `3 - 5 + 2 - 1 = -1`. The subtraction removes an earlier portion; it does not assume positive values.

## Block-by-block code walkthrough

Original snapshots are retained. Where a value changes several times, its cell shows the sequence ending at the final value.

Keep this input beside the execution: `numbers=[-2,0,3,-5,2,-1,4]`; queries `[0,2]`, `[2,5]`, `[0,6]`, `[3,3]`.

### Block 1 — Allocate totals for every boundary

```java
static final class NumArray {
    private final int[] prefix;

    NumArray(int[] numbers) {
        prefix = new int[numbers.length + 1];
```

Create an object to keep the totals available across queries. `static` lets the runnable outer class construct `new NumArray(...)` without creating an outer instance; `final` prevents extending this teaching class. `private` keeps the array internal; `final` assigns its reference once, while its cells can still be filled during construction.

- `prefix = new int[numbers.length + 1]` reads length `7`; allocate `7 + 1 = 8` zero-filled cells: `prefix=[0,0,0,0,0,0,0,0]`.
- `prefix[0]=0` already represents the empty total; the loop has not declared `index` yet.

Without the extra cell, storing the boundary after the last value would go past the array. A clearer equivalent is `int boundaryCount = numbers.length + 1` before allocation. This constructor snapshots totals rather than retaining the caller’s array reference.

### Block 2 — Build each next boundary total

```java
for (int index = 0; index < numbers.length; index++) {
    prefix[index + 1] = prefix[index] + numbers[index];
}
```

Start `index=0`. Each iteration reads one input value and the already-built total immediately before it, then saves their sum at the next boundary. No branch exists inside this loop; the loop condition still decides whether the body runs. The input stays unchanged.

**Construction iteration: `index=0`.**

- **✅ `for` condition `index < numbers.length`:** `0 < 7 → true`; enter the body.
- `prefix[index + 1] = prefix[index] + numbers[index]` reads `prefix[0]=0` and `numbers[0]=-2`; calculate `0 + (-2) = -2`.
- Assign `prefix[1]`: `0 → -2`. The full saved array is below; `numbers` stays unchanged.
- `index++`: `0 → 1`; continue to the next `for` check.

| index | loop condition | read total | incoming | written boundary | prefix after | next action |
| --- | --- | --- | --- | --- | --- | --- |
| `0` | `✅ 0<7 → true` | `0` | `-2` | `1` | `[0, -2, 0, 0, 0, 0, 0, 0]` | `index=1` |

**Construction iteration: `index=1`.**

- **✅ `for` condition `index < numbers.length`:** `1 < 7 → true`; enter the body.
- `prefix[index + 1] = prefix[index] + numbers[index]` reads `prefix[1]=-2` and `numbers[1]=0`; calculate `-2 + (0) = -2`.
- Assign `prefix[2]`: `0 → -2`. The full saved array is below; `numbers` stays unchanged.
- `index++`: `1 → 2`; continue to the next `for` check.

| index | loop condition | read total | incoming | written boundary | prefix after | next action |
| --- | --- | --- | --- | --- | --- | --- |
| `1` | `✅ 1<7 → true` | `-2` | `0` | `2` | `[0, -2, -2, 0, 0, 0, 0, 0]` | `index=2` |

**Construction iteration: `index=2`.**

- **✅ `for` condition `index < numbers.length`:** `2 < 7 → true`; enter the body.
- `prefix[index + 1] = prefix[index] + numbers[index]` reads `prefix[2]=-2` and `numbers[2]=3`; calculate `-2 + (3) = 1`.
- Assign `prefix[3]`: `0 → 1`. The full saved array is below; `numbers` stays unchanged.
- `index++`: `2 → 3`; continue to the next `for` check.

| index | loop condition | read total | incoming | written boundary | prefix after | next action |
| --- | --- | --- | --- | --- | --- | --- |
| `2` | `✅ 2<7 → true` | `-2` | `3` | `3` | `[0, -2, -2, 1, 0, 0, 0, 0]` | `index=3` |

**Construction iteration: `index=3`.**

- **✅ `for` condition `index < numbers.length`:** `3 < 7 → true`; enter the body.
- `prefix[index + 1] = prefix[index] + numbers[index]` reads `prefix[3]=1` and `numbers[3]=-5`; calculate `1 + (-5) = -4`.
- Assign `prefix[4]`: `0 → -4`. The full saved array is below; `numbers` stays unchanged.
- `index++`: `3 → 4`; continue to the next `for` check.

| index | loop condition | read total | incoming | written boundary | prefix after | next action |
| --- | --- | --- | --- | --- | --- | --- |
| `3` | `✅ 3<7 → true` | `1` | `-5` | `4` | `[0, -2, -2, 1, -4, 0, 0, 0]` | `index=4` |

**Construction iteration: `index=4`.**

- **✅ `for` condition `index < numbers.length`:** `4 < 7 → true`; enter the body.
- `prefix[index + 1] = prefix[index] + numbers[index]` reads `prefix[4]=-4` and `numbers[4]=2`; calculate `-4 + (2) = -2`.
- Assign `prefix[5]`: `0 → -2`. The full saved array is below; `numbers` stays unchanged.
- `index++`: `4 → 5`; continue to the next `for` check.

| index | loop condition | read total | incoming | written boundary | prefix after | next action |
| --- | --- | --- | --- | --- | --- | --- |
| `4` | `✅ 4<7 → true` | `-4` | `2` | `5` | `[0, -2, -2, 1, -4, -2, 0, 0]` | `index=5` |

**Construction iteration: `index=5`.**

- **✅ `for` condition `index < numbers.length`:** `5 < 7 → true`; enter the body.
- `prefix[index + 1] = prefix[index] + numbers[index]` reads `prefix[5]=-2` and `numbers[5]=-1`; calculate `-2 + (-1) = -3`.
- Assign `prefix[6]`: `0 → -3`. The full saved array is below; `numbers` stays unchanged.
- `index++`: `5 → 6`; continue to the next `for` check.

| index | loop condition | read total | incoming | written boundary | prefix after | next action |
| --- | --- | --- | --- | --- | --- | --- |
| `5` | `✅ 5<7 → true` | `-2` | `-1` | `6` | `[0, -2, -2, 1, -4, -2, -3, 0]` | `index=6` |

**Construction iteration: `index=6`.**

- **✅ `for` condition `index < numbers.length`:** `6 < 7 → true`; enter the body.
- `prefix[index + 1] = prefix[index] + numbers[index]` reads `prefix[6]=-3` and `numbers[6]=4`; calculate `-3 + (4) = 1`.
- Assign `prefix[7]`: `0 → 1`. The full saved array is below; `numbers` stays unchanged.
- `index++`: `6 → 7`; continue to the next `for` check.

| index | loop condition | read total | incoming | written boundary | prefix after | next action |
| --- | --- | --- | --- | --- | --- | --- |
| `6` | `✅ 6<7 → true` | `-3` | `4` | `7` | `[0, -2, -2, 1, -4, -2, -3, 1]` | `index=7` |

- **❌ `for` condition `index < numbers.length`:** `7 < 7 → false`; skip the body and finish construction. The loop-local `index` leaves scope. Without saving every boundary, later queries would need to sum values again. Equivalent clearer form: name `nextBoundary=index+1` and `nextTotal=prefix[index]+numbers[index]` before assigning.

### Block 3 — Subtract the unwanted earlier portion

```java
int sumRange(int left, int right) {
    return prefix[right + 1] - prefix[left];
}
```

The total through the inclusive right endpoint lives at `right+1`; the unwanted total strictly before left lives at `left`. Evaluate both reads and subtract. No loop, mutation, or guard runs for a valid query. Using `prefix[right]` would omit the rightmost value; using `prefix[left+1]` would remove the leftmost requested value. Equivalent: `int throughRight=prefix[right+1]; int beforeLeft=prefix[left]; return throughRight-beforeLeft;`.

**Query `left=0`, `right=2`.**

- `prefix[right + 1]` reads boundary `2 + 1 = 3` → `1`.
- `prefix[left]` reads boundary `0` → `0`.
- `return prefix[right + 1] - prefix[left]`: `1 - (0) = 1`; finish this query. The saved `prefix` array stays unchanged.

| left | right | right+1 | throughRight | beforeLeft | return |
| --- | --- | --- | --- | --- | --- |
| `0` | `2` | `3` | `1` | `0` | `1` |

**Query `left=2`, `right=5`.**

- `prefix[right + 1]` reads boundary `5 + 1 = 6` → `-3`.
- `prefix[left]` reads boundary `2` → `-2`.
- `return prefix[right + 1] - prefix[left]`: `-3 - (-2) = -1`; finish this query. The saved `prefix` array stays unchanged.

| left | right | right+1 | throughRight | beforeLeft | return |
| --- | --- | --- | --- | --- | --- |
| `2` | `5` | `6` | `-3` | `-2` | `-1` |

**Query `left=0`, `right=6`.**

- `prefix[right + 1]` reads boundary `6 + 1 = 7` → `1`.
- `prefix[left]` reads boundary `0` → `0`.
- `return prefix[right + 1] - prefix[left]`: `1 - (0) = 1`; finish this query. The saved `prefix` array stays unchanged.

| left | right | right+1 | throughRight | beforeLeft | return |
| --- | --- | --- | --- | --- | --- |
| `0` | `6` | `7` | `1` | `0` | `1` |

**Query `left=3`, `right=3`.**

- `prefix[right + 1]` reads boundary `3 + 1 = 4` → `-4`.
- `prefix[left]` reads boundary `3` → `1`.
- `return prefix[right + 1] - prefix[left]`: `-4 - (1) = -5`; finish this query. The saved `prefix` array stays unchanged.

| left | right | right+1 | throughRight | beforeLeft | return |
| --- | --- | --- | --- | --- | --- |
| `3` | `3` | `4` | `-4` | `1` | `-5` |

## Complete reference solution

Runnable reference: [RangeSumQuerySolution.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/prefixintervals/rangesum/RangeSumQuerySolution.java). The walkthrough immediately above explains the complete algorithm below; the `main` runner and assertions are in the linked Java file.

```java
// numbers index:  0 1 2  3 4  5 6
// numbers      : -2 0 3 -5 2 -1 4
// Input: numbers=[-2,0,3,-5,2,-1,4]; queries=[0,2],[2,5],[0,6],[3,3]
// Expected return: 1, -1, 1, -5

static final class NumArray {
    private final int[] prefix;

    NumArray(int[] numbers) {
        prefix = new int[numbers.length + 1];
        for (int index = 0; index < numbers.length; index++) {
            prefix[index + 1] = prefix[index] + numbers[index];
        }
    }

    int sumRange(int left, int right) {
        return prefix[right + 1] - prefix[left];
    }
}
```

## Why it works and what it costs

Before construction iteration `index`, `prefix[index]` equals the sum of exactly the first `index` values. Adding `numbers[index]` establishes the same fact for boundary `index+1`; zero establishes the first case. Therefore each query subtracts a shared earlier portion and leaves exactly the desired range.

Construction visits `n` values once: `O(n)` time and `O(n)` space. A query performs two reads and one subtraction: `O(1)` time and no additional saved space. Direct summation is a reasonable alternative for one query, taking `O(right-left+1)` per request without a prefix array. Updating the original values would invalidate saved totals; this immutable problem intentionally excludes updates.

## Pitfalls and other example results

- For `[-2,0,3,-5,2,-1]`, the whole range sums to `-2+0+3-5+2-1=-3`; the extra final `4` in our trace changes it to `1`.
- For `[7]`, saved totals are `[0,7]`; query `[0,0]` returns `7-0=7`.
- For `[0,0]`, totals are `[0,0,0]`; query `[0,1]` returns `0-0=0`.
- A caller changing its original array after construction does not change this object’s already-saved totals. Rebuilding would be needed to reflect that change.

## Practice after learning the concept

Open [RangeSumQueryPractice.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/prefixintervals/rangesum/RangeSumQueryPractice.java) in IntelliJ, implement the marked TODO method(s), then run its `main`. Implement both the constructor and `sumRange`; checks cover prefix, middle, whole, singleton, zero, and caller-mutation cases. The unfinished program reports that practice is ready; that message is not a passing solution.

Reveal only after attempting: [RangeSumQuerySolution.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/prefixintervals/rangesum/RangeSumQuerySolution.java). Its runner checks sample and edge cases and throws if an answer is wrong.

## Questions and explained answers

<details>
<summary>1. Why is the saved array one cell longer?</summary>

There is a boundary before any value. With `n` values there are `n+1` boundaries; `prefix[0]=0` also lets a query starting at zero use the same formula.

</details>

<details>
<summary>2. For query [2,5], why subtract prefix[2], not prefix[3]?</summary>

`prefix[2]` removes values at indexes 0 and 1. `prefix[3]` would also remove index 2, which belongs in the answer. Here `prefix[6]-prefix[2]=-3-(-2)=-1`.

</details>

<details>
<summary>3. Can changing a number after construction keep these queries correct?</summary>

Not for the changed data: later prefix totals would need updating. This object answers its construction-time snapshot. A structure for mutable sums is a separate lesson.

</details>

## Stop/go

Artifact readiness requires compilation and passing reference checks. Learning remains `TODO`. Go only after you implement both methods, derive `[2,5]` without notes, explain the boundary invariant and complexity, and answer the three checkpoints. Otherwise stop here; do not activate another lesson.
