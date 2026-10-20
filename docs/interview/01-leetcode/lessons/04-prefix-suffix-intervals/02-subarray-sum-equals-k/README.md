# Micro-Lesson 02 — Subarray Sum Equals K

## Question

Given an integer array and an integer `target` (LeetCode calls it `k`), count all non-empty contiguous ranges whose sum is exactly `target`. Different start/end positions count separately, and ranges may overlap.

Problem: [Subarray Sum Equals K on LeetCode](https://leetcode.com/problems/subarray-sum-equals-k/) · Editable Java: [SubarraySumEqualsKPractice.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/prefixintervals/subarraysum/SubarraySumEqualsKPractice.java)

## What you will learn

- Count valid ranges by finding earlier cumulative totals.
- Explain why a map stores frequencies rather than just membership.
- Look up earlier totals before recording the current one.

## Lesson status and prerequisites

- Learning status: `TODO`; this artifact is prepared, not activated or mastered.
- Prerequisite: [Range Sum Query](../01-range-sum-query-immutable/README.md) and [frequency counting](../../01-arrays-hash-maps/04-frequency-counting/README.md).
- Primary idea: Count valid ranges by finding earlier cumulative totals.
- New terms (at most three): prefix sum, prefix frequency, complement

## Inputs and limits

`1 <= numbers.length <= 20,000`, values in `[-1000,1000]`, `target` in `[-10,000,000,10,000,000]`. Target average `O(n)` time and `O(n)` extra space. A prefix has magnitude at most `20,000,000`; subtracting target has magnitude at most `30,000,000`. At most `20,000×20,001/2=200,010,000` non-empty ranges exist, so the shown `int` arithmetic is safe under these bounds. The runner additionally checks empty input defensively.

## First find the answer in learner language

We cannot know each starting position in advance. After reading a new value, ask: “Which earlier total could I remove to leave `target`?” A **prefix sum** is the cumulative total from index zero through the current index; an earlier boundary may be before any value. Its **complement** here is `current total - target`, the earlier total we need. A **prefix frequency** records how many earlier boundaries had the same total.

1. Remember that total `0` occurred once before reading any values.
2. Read the next value and update the running total.
3. Calculate `needed = prefixSum - target`.
4. Add the number of earlier occurrences of `needed` to the answer.
5. Only then record this current total for future positions.

The algebra comes from `current total - earlier total = target`, rearranged to `earlier total = current total - target`. For example, if current total is `2` and target is `2`, an earlier total `0` leaves `2-0=2`. If zero was seen twice at different boundaries, those are two different valid starting points.

## Block-by-block code walkthrough

Original snapshots are retained. Where a value changes several times, its cell shows the sequence ending at the final value.

Use `numbers=[1,-1,1,1,-1,1,2]`, `target=2`. Every row below is one complete outer iteration, including the map update after lookup.

### Block 1 — Remember the empty beginning and initialize totals

```java
Map<Integer, Integer> countByPrefix = new HashMap<>();
countByPrefix.put(0, 1);
int prefixSum = 0;
int answer = 0;
```

- `new HashMap<>()` creates `countByPrefix={}` to store each earlier total’s frequency.
- `countByPrefix.put(0, 1)` changes `{}` → `{0:1}`: the boundary before index zero has total zero once.
- `int prefixSum = 0` and `int answer = 0` initialize the running total and matching-range count. No input-length guard runs.

An empty array simply executes no iterations and returns zero.

Without the seed, a range starting at index zero would have no matching earlier boundary. For `[2]`, target `2`, the desired lookup is zero; `{0:1}` contributes one answer. Equivalent: handle `prefixSum == target` separately, but the seed avoids that extra branch.

### Block 2 — Read, find earlier totals, then record this one

```java
for (int index = 0; index < numbers.length; index++) {
    prefixSum += numbers[index];
    int needed = prefixSum - target;
    answer += countByPrefix.getOrDefault(needed, 0);
    countByPrefix.merge(prefixSum, 1, Integer::sum);
}
```

`getOrDefault(needed,0)` returns the frequency if the key is present, or zero if absent; it does not add a key. `merge(prefixSum,1,Integer::sum)` stores one on the first occurrence, or adds one to the existing count. `Integer::sum` names the addition operation used when the key already exists. Equivalent clearer form: read the count with `getOrDefault(prefixSum,0)`, then `put(prefixSum,oldCount+1)`.

The map must contain only earlier boundaries during lookup. Recording the current total first would allow matching a boundary with itself: `[1]`, target `0` would incorrectly count an empty range (`1-1=0`). The current code looks up key `1` before inserting it, correctly contributing zero. A set cannot replace the map because it would lose multiple starts with the same total.

**Iteration `index=0`.**

- **✅ `for` condition `index < numbers.length`:** `0 < 7 → true`; enter the body.
- `prefixSum += numbers[index]` reads `numbers[0]=1`; `0 + (1) = 1`, so `prefixSum`: `0 → 1`.
- `int needed = prefixSum - target`: `1 - 2 = -1`.
- `countByPrefix.getOrDefault(needed, 0)` looks up key `-1` → `0` (absent, use the default).
- `answer += ...`: `0 + 0 = 0`, so `answer`: `0 → 0`.
- `countByPrefix.merge(prefixSum, 1, Integer::sum)` records total `1`; its frequency changes `0 → 1`. The map before/after is below.
- `index++`: `0 → 1`; continue to the next `for` check.

| index | loop test | value | prefixSum | needed | frequency found | answer after | map before lookup | map after recording | next |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `0` | `✅ 0<7 → true` | `1` | `1` | `-1` | `0` | `0` | `{0: 1}` | `{0: 1, 1: 1}` | `index=1` |

**Iteration `index=1`.**

- **✅ `for` condition `index < numbers.length`:** `1 < 7 → true`; enter the body.
- `prefixSum += numbers[index]` reads `numbers[1]=-1`; `1 + (-1) = 0`, so `prefixSum`: `1 → 0`.
- `int needed = prefixSum - target`: `0 - 2 = -2`.
- `countByPrefix.getOrDefault(needed, 0)` looks up key `-2` → `0` (absent, use the default).
- `answer += ...`: `0 + 0 = 0`, so `answer`: `0 → 0`.
- `countByPrefix.merge(prefixSum, 1, Integer::sum)` records total `0`; its frequency changes `1 → 2`. The map before/after is below.
- `index++`: `1 → 2`; continue to the next `for` check.

| index | loop test | value | prefixSum | needed | frequency found | answer after | map before lookup | map after recording | next |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `1` | `✅ 1<7 → true` | `-1` | `0` | `-2` | `0` | `0` | `{0: 1, 1: 1}` | `{0: 2, 1: 1}` | `index=2` |

**Iteration `index=2`.**

- **✅ `for` condition `index < numbers.length`:** `2 < 7 → true`; enter the body.
- `prefixSum += numbers[index]` reads `numbers[2]=1`; `0 + (1) = 1`, so `prefixSum`: `0 → 1`.
- `int needed = prefixSum - target`: `1 - 2 = -1`.
- `countByPrefix.getOrDefault(needed, 0)` looks up key `-1` → `0` (absent, use the default).
- `answer += ...`: `0 + 0 = 0`, so `answer`: `0 → 0`.
- `countByPrefix.merge(prefixSum, 1, Integer::sum)` records total `1`; its frequency changes `1 → 2`. The map before/after is below.
- `index++`: `2 → 3`; continue to the next `for` check.

| index | loop test | value | prefixSum | needed | frequency found | answer after | map before lookup | map after recording | next |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `2` | `✅ 2<7 → true` | `1` | `1` | `-1` | `0` | `0` | `{0: 2, 1: 1}` | `{0: 2, 1: 2}` | `index=3` |

**Iteration `index=3`.**

- **✅ `for` condition `index < numbers.length`:** `3 < 7 → true`; enter the body.
- `prefixSum += numbers[index]` reads `numbers[3]=1`; `1 + (1) = 2`, so `prefixSum`: `1 → 2`.
- `int needed = prefixSum - target`: `2 - 2 = 0`.
- `countByPrefix.getOrDefault(needed, 0)` looks up key `0` → `2` earlier boundaries.
- `answer += ...`: `0 + 2 = 2`, so `answer`: `0 → 2`.
- `countByPrefix.merge(prefixSum, 1, Integer::sum)` records total `2`; its frequency changes `0 → 1`. The map before/after is below.
- `index++`: `3 → 4`; continue to the next `for` check.

| index | loop test | value | prefixSum | needed | frequency found | answer after | map before lookup | map after recording | next |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `3` | `✅ 3<7 → true` | `1` | `2` | `0` | `2` | `2` | `{0: 2, 1: 2}` | `{0: 2, 1: 2, 2: 1}` | `index=4` |

**Iteration `index=4`.**

- **✅ `for` condition `index < numbers.length`:** `4 < 7 → true`; enter the body.
- `prefixSum += numbers[index]` reads `numbers[4]=-1`; `2 + (-1) = 1`, so `prefixSum`: `2 → 1`.
- `int needed = prefixSum - target`: `1 - 2 = -1`.
- `countByPrefix.getOrDefault(needed, 0)` looks up key `-1` → `0` (absent, use the default).
- `answer += ...`: `2 + 0 = 2`, so `answer`: `2 → 2`.
- `countByPrefix.merge(prefixSum, 1, Integer::sum)` records total `1`; its frequency changes `2 → 3`. The map before/after is below.
- `index++`: `4 → 5`; continue to the next `for` check.

| index | loop test | value | prefixSum | needed | frequency found | answer after | map before lookup | map after recording | next |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `4` | `✅ 4<7 → true` | `-1` | `1` | `-1` | `0` | `2` | `{0: 2, 1: 2, 2: 1}` | `{0: 2, 1: 3, 2: 1}` | `index=5` |

**Iteration `index=5`.**

- **✅ `for` condition `index < numbers.length`:** `5 < 7 → true`; enter the body.
- `prefixSum += numbers[index]` reads `numbers[5]=1`; `1 + (1) = 2`, so `prefixSum`: `1 → 2`.
- `int needed = prefixSum - target`: `2 - 2 = 0`.
- `countByPrefix.getOrDefault(needed, 0)` looks up key `0` → `2` earlier boundaries.
- `answer += ...`: `2 + 2 = 4`, so `answer`: `2 → 4`.
- `countByPrefix.merge(prefixSum, 1, Integer::sum)` records total `2`; its frequency changes `1 → 2`. The map before/after is below.
- `index++`: `5 → 6`; continue to the next `for` check.

| index | loop test | value | prefixSum | needed | frequency found | answer after | map before lookup | map after recording | next |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `5` | `✅ 5<7 → true` | `1` | `2` | `0` | `2` | `4` | `{0: 2, 1: 3, 2: 1}` | `{0: 2, 1: 3, 2: 2}` | `index=6` |

**Iteration `index=6`.**

- **✅ `for` condition `index < numbers.length`:** `6 < 7 → true`; enter the body.
- `prefixSum += numbers[index]` reads `numbers[6]=2`; `2 + (2) = 4`, so `prefixSum`: `2 → 4`.
- `int needed = prefixSum - target`: `4 - 2 = 2`.
- `countByPrefix.getOrDefault(needed, 0)` looks up key `2` → `2` earlier boundaries.
- `answer += ...`: `4 + 2 = 6`, so `answer`: `4 → 6`.
- `countByPrefix.merge(prefixSum, 1, Integer::sum)` records total `4`; its frequency changes `0 → 1`. The map before/after is below.
- `index++`: `6 → 7`; continue to the next `for` check.

| index | loop test | value | prefixSum | needed | frequency found | answer after | map before lookup | map after recording | next |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `6` | `✅ 6<7 → true` | `2` | `4` | `2` | `2` | `6` | `{0: 2, 1: 3, 2: 2}` | `{0: 2, 1: 3, 2: 2, 4: 1}` | `index=7` |

### Block 3 — Finish only after every endpoint has been counted

```java
return answer;
```

- **❌ `for` condition `index < numbers.length`:** `7 < 7 → false`; skip the body.
- `return answer` returns `6`; final `prefixSum=4` is not the answer. There is no early return when a match appears: more endpoints and more starts can contribute. Returning `prefixSum` would return `4`, a total, not the number of qualifying ranges. Equivalent: name the variable `matchingRangeCount` to emphasize what is returned.

The six matching ranges are `[0,3]`, `[2,3]`, `[0,5]`, `[2,5]`, `[4,6]`, and `[6,6]`. Their exact sums are `1-1+1+1=2`, `1+1=2`, `1-1+1+1-1+1=2`, `1+1-1+1=2`, `-1+1+2=2`, and `2`. These positions explain the three pairs of contributions at endpoints `3`, `5`, and `6`.

## Complete reference solution

Runnable reference: [SubarraySumEqualsKSolution.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/prefixintervals/subarraysum/SubarraySumEqualsKSolution.java). The walkthrough immediately above explains the complete algorithm below; the `main` runner and assertions are in the linked Java file.

```java
// numbers index: 0  1 2 3  4 5 6
// numbers      : 1 -1 1 1 -1 1 2
// Input: numbers=[1,-1,1,1,-1,1,2]; target=2
// Expected return: 6

static int subarraySum(int[] numbers, int target) {
    Map<Integer, Integer> countByPrefix = new HashMap<>();
    countByPrefix.put(0, 1);
    int prefixSum = 0;
    int answer = 0;

    for (int index = 0; index < numbers.length; index++) {
        prefixSum += numbers[index];
        int needed = prefixSum - target;
        answer += countByPrefix.getOrDefault(needed, 0);
        countByPrefix.merge(prefixSum, 1, Integer::sum);
    }
    return answer;
}
```

## Invariant, correctness, and complexity

Before each iteration, the map counts every boundary strictly before the new endpoint. Subtracting a needed earlier total gives exactly the target sum, and every stored occurrence identifies a distinct start. Lookup counts all and only valid starts for this endpoint. Recording afterward maintains the invariant for the next endpoint. Every non-empty range has one endpoint, so it is counted once.

There are `n` iterations with average constant-time HashMap operations: average `O(n)` time and at most `n+1` distinct map keys, `O(n)` space. A baseline tries every start and extends a running sum to every end, taking `O(n²)` time and `O(1)` extra space. Use it as a small-input checker. A moving positive-only window is unsuitable here because adding a negative value can decrease the sum rather than increase it.

## Edge cases and their derivations

- `[1,1,1]`, target `2`: ranges `[0,1]` and `[1,2]` each sum `1+1=2`, so result `2`.
- `[1,2,3]`, target `3`: `[0,1]` sums `1+2=3`; `[2,2]` sums `3`; result `2`.
- `[0,0,0]`, target `0`: each endpoint finds respectively `1`, `2`, then `3` earlier zero totals, so answer `1+2+3=6`. Seed zero is a boundary, not an empty counted range.
- `[-1]`, target `-1`: running total `-1`, needed `-1-(-1)=0`, seed frequency `1`; answer `1`.
- Empty input executes no body and returns the initial answer `0`.

## Practice after learning the concept

Open [SubarraySumEqualsKPractice.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/prefixintervals/subarraysum/SubarraySumEqualsKPractice.java) in IntelliJ, implement the marked TODO method(s), then run its `main`. Checks include overlapping matches, negative values and targets, repeated zero totals, and avoiding empty ranges. The unfinished program reports that practice is ready; that message is not a passing solution.

Reveal only after attempting: [SubarraySumEqualsKSolution.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/prefixintervals/subarraysum/SubarraySumEqualsKSolution.java). Its runner checks sample and edge cases and throws if an answer is wrong.

## Questions and explained answers

<details>
<summary>1. Why store frequencies instead of a set?</summary>

Different boundaries can have the same total. With three zeros and target zero, the contributions are 1, 2, 3, not merely one per endpoint; frequencies preserve all six ranges.

</details>

<details>
<summary>2. Why look up before recording the current total?</summary>

For target zero, recording first makes the current boundary match itself, creating an empty range. `[1]`, target zero must return 0; inserting total 1 before lookup would falsely contribute 1.

</details>

<details>
<summary>3. What exactly does map entry 0:2 mean?</summary>

Two earlier boundaries had cumulative total zero. Either can be the boundary before a valid range when the current total equals the target; the key is not an array index.

</details>

## Stop/go

Keep learning `TODO` until you implement and pass the practice runner, explain the earlier-boundary invariant and average complexity, predict the zero-target bug, and answer the checkpoints. Passing artifact checks alone does not activate this lesson or complete comprehension.
