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

## First understand the idea

The key idea is to store running totals once, so each later range sum is just a subtraction.

Use `numbers=[2,4,1,7]`. Define `prefix[k]` as **the sum of the first k numbers**. Start with `prefix[0]=0`: the sum of zero numbers is zero. Adding the values in order gives totals 2, 6, 7, then 14.

```text
numbers index: 0  1  2  3
numbers      : 2  4  1  7
prefix index : 0  1  2  3   4
prefix       : 0  2  6  7  14
```

That is what the constructor builds. For example, `prefix[3]=2+4+1=7`. An index in numbers identifies one input value; an index in prefix identifies how many values were added. This saved running total is a **prefix total**, and its index marks a **boundary** before or after input values.

Now request the sum from numbers index 1 through 3: `[4,1,7]`. Such a request is a **query**. The total through index 3 is `prefix[4]=14`. It includes the earlier value 2, whose saved total is `prefix[1]=2`. Remove that earlier part:

```text
sumRange(1,3) = prefix[4] - prefix[1]
              = 14 - 2
              = 12
```

Direct addition agrees: `4+1+7=12`. Both calculations refer to the same input and range.

In general, `prefix[right+1]` includes everything through right, while `prefix[left]` includes everything before left. Subtracting removes exactly that earlier portion: `prefix[right+1]-prefix[left]`. The +1 is needed because prefix counts the first k numbers: including numbers index 3 requires the first `3+1=4` numbers. This meaning also handles queries starting at zero, since the portion before them totals zero.

## Block-by-block code walkthrough

Original snapshots are retained. Where a value changes several times, its cell shows the sequence ending at the final value.

Keep this input beside the execution: `numbers=[2,4,1,7]`; queries `[0,2]`, `[1,2]`, `[0,3]`, `[3,3]`.

### Block 1 — Allocate totals for every boundary

```java
static final class NumArray {
    private final int[] prefix;

    NumArray(int[] numbers) {
        prefix = new int[numbers.length + 1];
```

Create an object to keep the totals available across queries. `static` lets the runnable outer class construct `new NumArray(...)` without creating an outer instance; `final` prevents extending this teaching class. `private` keeps the array internal; `final` assigns its reference once, while its cells can still be filled during construction.

- `prefix = new int[numbers.length + 1]` reads length `4`; allocate `4 + 1 = 5` zero-filled cells: `prefix=[0,0,0,0,0]`.
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

- **✅ `for` condition `index < numbers.length`:** `0 < 4 → true`; enter the body.
- `prefix[index + 1] = prefix[index] + numbers[index]` reads `prefix[0]=0` and `numbers[0]=2`; calculate `0 + 2 = 2`.
- Assign `prefix[1]`: `0 → 2`. The full saved array is below; `numbers` stays `[2,4,1,7]`.
- `index++`: `0 → 1`; continue to the next `for` check.

| index | loop condition | read total | incoming | written boundary | prefix after | next action |
| --- | --- | --- | --- | --- | --- | --- |
| `0` | `✅ 0<4 → true` | `0` | `2` | `1` | `[0, 2, 0, 0, 0]` | `index=1` |

**Construction iteration: `index=1`.**

- **✅ `for` condition `index < numbers.length`:** `1 < 4 → true`; enter the body.
- `prefix[index + 1] = prefix[index] + numbers[index]` reads `prefix[1]=2` and `numbers[1]=4`; calculate `2 + 4 = 6`.
- Assign `prefix[2]`: `0 → 6`. The full saved array is below; `numbers` stays `[2,4,1,7]`.
- `index++`: `1 → 2`; continue to the next `for` check.

| index | loop condition | read total | incoming | written boundary | prefix after | next action |
| --- | --- | --- | --- | --- | --- | --- |
| `1` | `✅ 1<4 → true` | `2` | `4` | `2` | `[0, 2, 6, 0, 0]` | `index=2` |

**Construction iteration: `index=2`.**

- **✅ `for` condition `index < numbers.length`:** `2 < 4 → true`; enter the body.
- `prefix[index + 1] = prefix[index] + numbers[index]` reads `prefix[2]=6` and `numbers[2]=1`; calculate `6 + 1 = 7`.
- Assign `prefix[3]`: `0 → 7`. The full saved array is below; `numbers` stays `[2,4,1,7]`.
- `index++`: `2 → 3`; continue to the next `for` check.

| index | loop condition | read total | incoming | written boundary | prefix after | next action |
| --- | --- | --- | --- | --- | --- | --- |
| `2` | `✅ 2<4 → true` | `6` | `1` | `3` | `[0, 2, 6, 7, 0]` | `index=3` |

**Construction iteration: `index=3`.**

- **✅ `for` condition `index < numbers.length`:** `3 < 4 → true`; enter the body.
- `prefix[index + 1] = prefix[index] + numbers[index]` reads `prefix[3]=7` and `numbers[3]=7`; calculate `7 + 7 = 14`.
- Assign `prefix[4]`: `0 → 14`. The full saved array is below; `numbers` stays `[2,4,1,7]`.
- `index++`: `3 → 4`; continue to the next `for` check.

| index | loop condition | read total | incoming | written boundary | prefix after | next action |
| --- | --- | --- | --- | --- | --- | --- |
| `3` | `✅ 3<4 → true` | `7` | `7` | `4` | `[0, 2, 6, 7, 14]` | `index=4` |

- **❌ `for` condition `index < numbers.length`:** `4 < 4 → false`; skip the body and finish construction with `prefix=[0,2,6,7,14]`. The loop-local `index` leaves scope. Without saving every boundary, later queries would need to sum values again. Equivalent clearer form: name `nextBoundary=index+1` and `nextTotal=prefix[index]+numbers[index]` before assigning.

### Block 3 — Subtract the unwanted earlier portion

```java
int sumRange(int left, int right) {
    return prefix[right + 1] - prefix[left];
}
```

The total through the inclusive right endpoint lives at `right+1`; the unwanted total strictly before left lives at `left`. Evaluate both reads and subtract. No loop, mutation, or guard runs for a valid query. Using `prefix[right]` would omit the rightmost value; using `prefix[left+1]` would remove the leftmost requested value. Equivalent: `int throughRight=prefix[right+1]; int beforeLeft=prefix[left]; return throughRight-beforeLeft;`.

**Query `left=0`, `right=2`.**

- `prefix[right + 1]` reads boundary `2 + 1 = 3` → `7`.
- `prefix[left]` reads boundary `0` → `0`.
- `return prefix[right + 1] - prefix[left]`: `7 - 0 = 7`; direct addition: `2 + 4 + 1 = 7`. Finish this query; `prefix=[0,2,6,7,14]` stays unchanged.

| left | right | right+1 | throughRight | beforeLeft | return |
| --- | --- | --- | --- | --- | --- |
| `0` | `2` | `3` | `7` | `0` | `7` |

**Query `left=1`, `right=2`.**

- `prefix[right + 1]` reads boundary `2 + 1 = 3` → `7`.
- `prefix[left]` reads boundary `1` → `2`.
- `return prefix[right + 1] - prefix[left]`: `7 - 2 = 5`; direct addition: `4 + 1 = 5`. Finish this query; `prefix=[0,2,6,7,14]` stays unchanged.

| left | right | right+1 | throughRight | beforeLeft | return |
| --- | --- | --- | --- | --- | --- |
| `1` | `2` | `3` | `7` | `2` | `5` |

**Query `left=0`, `right=3`.**

- `prefix[right + 1]` reads boundary `3 + 1 = 4` → `14`.
- `prefix[left]` reads boundary `0` → `0`.
- `return prefix[right + 1] - prefix[left]`: `14 - 0 = 14`; direct addition: `2 + 4 + 1 + 7 = 14`. Finish this query; `prefix=[0,2,6,7,14]` stays unchanged.

| left | right | right+1 | throughRight | beforeLeft | return |
| --- | --- | --- | --- | --- | --- |
| `0` | `3` | `4` | `14` | `0` | `14` |

**Query `left=3`, `right=3`.**

- `prefix[right + 1]` reads boundary `3 + 1 = 4` → `14`.
- `prefix[left]` reads boundary `3` → `7`.
- `return prefix[right + 1] - prefix[left]`: `14 - 7 = 7`; direct addition: `7 = 7`. Finish this query; `prefix=[0,2,6,7,14]` stays unchanged.

| left | right | right+1 | throughRight | beforeLeft | return |
| --- | --- | --- | --- | --- | --- |
| `3` | `3` | `4` | `14` | `7` | `7` |

## Complete reference solution

Runnable reference: [RangeSumQuerySolution.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/prefixintervals/rangesum/RangeSumQuerySolution.java). The walkthrough immediately above explains the complete algorithm below; the `main` runner and assertions are in the linked Java file.

```java
// numbers index: 0 1 2 3
// numbers      : 2 4 1 7
// prefix index : 0 1 2 3  4
// prefix built : 0 2 6 7 14
// Input: numbers=[2,4,1,7]; queries=[0,2],[1,2],[0,3],[3,3]
// Expected return: 7, 5, 14, 7

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

## Pitfalls in this example

- `prefix[0]=0` represents no values; it is not the first input value. `numbers[0]=2` is included in `prefix[1]=2`.
- For `[1,2]`, using `prefix[right]` gives `prefix[2]-prefix[1]=6-2=4`, omitting the requested `numbers[2]=1`.
- Using `prefix[left+1]` instead gives `prefix[3]-prefix[2]=7-6=1`, incorrectly removing the requested `numbers[1]=4`.
- The whole range `[0,3]` returns `prefix[4]-prefix[0]=14-0=14`; the last value `[3,3]` returns `prefix[4]-prefix[3]=14-7=7`.
- A caller changing its original array after construction does not change this object's already-saved totals. Rebuilding would be needed to reflect that change. Additional negative/singleton/zero checks stay in the runnable files.

## Practice after learning the concept

Open [RangeSumQueryPractice.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/prefixintervals/rangesum/RangeSumQueryPractice.java) in IntelliJ, implement the marked TODO method(s), then run its `main`. Implement both the constructor and `sumRange`; checks cover the worked prefix/middle/whole/last-value queries, negative values, singleton, zero, and caller-mutation cases. The unfinished program reports that practice is ready; that message is not a passing solution.

Reveal only after attempting: [RangeSumQuerySolution.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/prefixintervals/rangesum/RangeSumQuerySolution.java). Its runner checks sample and edge cases and throws if an answer is wrong.

## Questions and explained answers

<details>
<summary>1. Why is the saved array one cell longer?</summary>

There is a boundary before any value. With `n` values there are `n+1` boundaries; `prefix[0]=0` also lets a query starting at zero use the same formula.

</details>

<details>
<summary>2. For query [1,2], why subtract prefix[1], not prefix[2]?</summary>

`prefix[1]=2` removes only numbers index 0. `prefix[2]=6` would also remove the requested value at index 1. Here `prefix[3]-prefix[1]=7-2=5`, matching `4+1=5`; the incorrect subtraction gives `7-6=1`.

</details>

<details>
<summary>3. Can changing a number after construction keep these queries correct?</summary>

Not for the changed data: later prefix totals would need updating. This object answers its construction-time snapshot. A structure for mutable sums is a separate lesson.

</details>

## Stop/go

Artifact readiness requires compilation and passing reference checks. Learning remains `TODO`. Go only after you implement both methods, derive `[1,2]` without notes, explain the boundary invariant and complexity, and answer the three checkpoints. Otherwise stop here; do not activate another lesson.
