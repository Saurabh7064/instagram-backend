# Micro-Lesson 04 — Merge Intervals

## Question

Given an array of inclusive ranges `[start,end]`, combine every overlapping range and return a set of disjoint ranges covering exactly the same positions. Input order is arbitrary. Ranges that share an endpoint overlap.

Problem: [Merge Intervals on LeetCode](https://leetcode.com/problems/merge-intervals/) · Editable Java: [MergeIntervalsPractice.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/prefixintervals/mergeintervals/MergeIntervalsPractice.java)

## What you will learn

- Keep one unfinished range while scanning by start position.
- Explain why overlap extends the end but a gap finalizes a range.
- Preserve caller-owned arrays while sorting and producing output.

## Lesson status and prerequisites

- Learning status: `TODO`; this artifact is prepared, not activated or mastered.
- Prerequisite: Array indexing, `List.add`, and sorting a copy; this lesson explains the specific Java APIs it uses.
- Primary idea: Keep one unfinished range while scanning by start position.
- New terms (at most three): interval, overlap, active range

## Inputs and limits

`1 <= intervals.length <= 10,000`, each row has two integers, and `0 <= start <= end <= 10,000`. Target `O(n log n)` time for arbitrary input order and `O(n)` extra space in this input-preserving version. The local runner additionally handles empty input. Output is ordered by start in this implementation.

## First find the answer in learner language

We want to describe the same covered positions using fewer ranges, without losing a covered point or filling a gap. An **interval** is an inclusive pair `[start,end]`, meaning every point from start through end. Two intervals **overlap** when they share at least one point. The **active range** is the unfinished combined interval held in the two local variables `start` and `end`.

1. Put ranges in increasing order of their starts, using copies so caller-owned input is preserved.
2. Keep the first range as the unfinished one.
3. If the next start is at or before the unfinished end, the ranges meet: keep the start and extend the end only as far as necessary.
4. Otherwise a real gap exists. Save the unfinished range, then start a new one.
5. After scanning, save the last unfinished range; no later gap will arrive to save it for us.

Example: sorting `[[8,10],[1,3],[2,6],[15,18]]` gives `[[1,3],[2,6],[8,10],[15,18]]`. `[1,3]` and `[2,6]` share points `2..3`, so combine them as `[1,max(3,6)]=[1,6]`. Start `8` exceeds end `6`, so there is a gap and `[1,6]` can be finalized. `[15,18]` is separate because `15>10`. The result is `[[1,6],[8,10],[15,18]]`.

## Block-by-block code walkthrough

Original snapshots are retained. Where a value changes several times, its cell shows the sequence ending at the final value.

Keep input `intervals=[[8,10],[1,3],[2,6],[15,18]]` alongside this execution.

### Block 1 — Handle no input before reading the first range

```java
if (intervals.length == 0) {
    return new int[0][2];
}
```

The trace has four ranges: evaluate `4==0 → false`; skip the return and continue. The defensive empty case evaluates `0==0 → true` and returns an array with zero rows immediately. This guard prevents a later read of `sorted[0]` when no row exists. Equivalent: place the non-empty algorithm in an `else`, but the early return keeps it less indented.

### Block 2 — Make independent rows before sorting

```java
int[][] sorted = new int[intervals.length][2];
for (int index = 0; index < intervals.length; index++) {
    sorted[index] = intervals[index].clone();
}
```

Allocate four rows, each with two zero-valued cells: `[[0,0],[0,0],[0,0],[0,0]]`. For each input row, `clone()` creates an independent two-integer array and assigns its reference into the corresponding row of `sorted`. It replaces that zero row; it does not modify the original row. Copying only the outer array would leave row references shared. Equivalent: `new int[] {intervals[index][0], intervals[index][1]}` explicitly copies the two endpoints.

**Copy iteration `index=0`.** Check `0<4 → true`. Read input row `[8, 10]`, create a new row with those same two values, and assign it at `sorted[0]`. The complete copy now contains `[[8, 10], [0, 0], [0, 0], [0, 0]]`; `intervals` remains `[[8, 10], [1, 3], [2, 6], [15, 18]]`. Execute `index++`, making `1`.

| index | condition | copied row | sorted after | original after | next |
| --- | --- | --- | --- | --- | --- |
| `0` | `0<4 → true` | `[8, 10]` | `[[8, 10], [0, 0], [0, 0], [0, 0]]` | `[[8, 10], [1, 3], [2, 6], [15, 18]]` | `index=1` |

**Copy iteration `index=1`.** Check `1<4 → true`. Read input row `[1, 3]`, create a new row with those same two values, and assign it at `sorted[1]`. The complete copy now contains `[[8, 10], [1, 3], [0, 0], [0, 0]]`; `intervals` remains `[[8, 10], [1, 3], [2, 6], [15, 18]]`. Execute `index++`, making `2`.

| index | condition | copied row | sorted after | original after | next |
| --- | --- | --- | --- | --- | --- |
| `1` | `1<4 → true` | `[1, 3]` | `[[8, 10], [1, 3], [0, 0], [0, 0]]` | `[[8, 10], [1, 3], [2, 6], [15, 18]]` | `index=2` |

**Copy iteration `index=2`.** Check `2<4 → true`. Read input row `[2, 6]`, create a new row with those same two values, and assign it at `sorted[2]`. The complete copy now contains `[[8, 10], [1, 3], [2, 6], [0, 0]]`; `intervals` remains `[[8, 10], [1, 3], [2, 6], [15, 18]]`. Execute `index++`, making `3`.

| index | condition | copied row | sorted after | original after | next |
| --- | --- | --- | --- | --- | --- |
| `2` | `2<4 → true` | `[2, 6]` | `[[8, 10], [1, 3], [2, 6], [0, 0]]` | `[[8, 10], [1, 3], [2, 6], [15, 18]]` | `index=3` |

**Copy iteration `index=3`.** Check `3<4 → true`. Read input row `[15, 18]`, create a new row with those same two values, and assign it at `sorted[3]`. The complete copy now contains `[[8, 10], [1, 3], [2, 6], [15, 18]]`; `intervals` remains `[[8, 10], [1, 3], [2, 6], [15, 18]]`. Execute `index++`, making `4`.

| index | condition | copied row | sorted after | original after | next |
| --- | --- | --- | --- | --- | --- |
| `3` | `3<4 → true` | `[15, 18]` | `[[8, 10], [1, 3], [2, 6], [15, 18]]` | `[[8, 10], [1, 3], [2, 6], [15, 18]]` | `index=4` |

After the increment to `index=4`, `4<4 → false`; skip the copy body and continue to sorting.

### Block 3 — Sort by the start and initialize the unfinished range

```java
Arrays.sort(sorted, Comparator.comparingInt(interval -> interval[0]));
List<int[]> merged = new ArrayList<>();
int start = sorted[0][0];
int end = sorted[0][1];
```

`Arrays.sort` rearranges the copied row references. `Comparator.comparingInt(...)` tells it to order rows by one integer key; the lambda `interval -> interval[0]` reads a compared row’s start. The lambda parameter exists only while the sorting library requests a key, and the library’s internal comparison order is not fixed by this lesson. An equivalent key function is a named function returning `interval[0]`.

After the call, `sorted=[[1,3],[2,6],[8,10],[15,18]]`. Create `merged=[]`, then read `start=1`, `end=3` from the first sorted row. That range is unfinished, so it is not in the output list yet. Without sorting, a later smaller start could invalidate a previously finalized range. Without saving start/end, the algorithm would have no unfinished interval to extend.

### Block 4 — Merge overlaps or finalize at a gap

```java
for (int index = 1; index < sorted.length; index++) {
    int[] current = sorted[index];
    if (current[0] <= end) {
        end = Math.max(end, current[1]);
    } else {
        merged.add(new int[] {start, end});
        start = current[0];
        end = current[1];
    }
}
```

Start at index `1` because index `0` already initialized the unfinished range. `current` refers to the copied row for this iteration. The `<=` includes touching endpoints. If ranges meet, update only the end using `Math.max`; a contained interval must not shorten it. A gap saves a new two-integer output row before replacing start/end. Equivalent: call the overlap test `boolean touches = current[0] <= end` and branch on that value. Using `<` would wrongly split `[1,4]` and `[4,5]`; assigning `end=current[1]` in the overlap branch would shrink `[1,10]` when `[2,3]` arrives.

**Merge iteration `index=1`.** Evaluate `1<4 → true`; read `current=[2, 6]`. The unfinished range before the test is `[1,3]`. Check `2 <= 3 → true`. Because the ranges meet, extend end=max(3,6)=6; skip else. The list is now `[]`, while the still-unfinished range is `[1,6]`. Execute `index++` to `2`. The sorted copy stays `[[1, 3], [2, 6], [8, 10], [15, 18]]`.

| index | loop test | current | start/end before | current[0] <= end | executed branch | merged after | start/end after | next |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `1` | `1<4 → true` | `[2, 6]` | `[1, 3]` | `true` | `extend end=max(3,6)=6; skip else` | `[]` | `[1, 6]` | `index=2` |

**Merge iteration `index=2`.** Evaluate `2<4 → true`; read `current=[8, 10]`. The unfinished range before the test is `[1,6]`. Check `8 <= 6 → false`. Because there is a gap, save [1,6]; start=8; end=10. The list is now `[[1, 6]]`, while the still-unfinished range is `[8,10]`. Execute `index++` to `3`. The sorted copy stays `[[1, 3], [2, 6], [8, 10], [15, 18]]`.

| index | loop test | current | start/end before | current[0] <= end | executed branch | merged after | start/end after | next |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `2` | `2<4 → true` | `[8, 10]` | `[1, 6]` | `false` | `save [1,6]; start=8; end=10` | `[[1, 6]]` | `[8, 10]` | `index=3` |

**Merge iteration `index=3`.** Evaluate `3<4 → true`; read `current=[15, 18]`. The unfinished range before the test is `[8,10]`. Check `15 <= 10 → false`. Because there is a gap, save [8,10]; start=15; end=18. The list is now `[[1, 6], [8, 10]]`, while the still-unfinished range is `[15,18]`. Execute `index++` to `4`. The sorted copy stays `[[1, 3], [2, 6], [8, 10], [15, 18]]`.

| index | loop test | current | start/end before | current[0] <= end | executed branch | merged after | start/end after | next |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `3` | `3<4 → true` | `[15, 18]` | `[8, 10]` | `false` | `save [8,10]; start=15; end=18` | `[[1, 6], [8, 10]]` | `[15, 18]` | `index=4` |

### Block 5 — Save the final unfinished range and return rows

```java
merged.add(new int[] {start, end});
return merged.toArray(new int[merged.size()][]);
```

After incrementing to `index=4`, `4<4 → false`; skip the loop body. The list contains `[[1,6],[8,10]]` and the unfinished range is `[15,18]`. Create and append that final row: `merged=[[1,6],[8,10],[15,18]]`. Omitting this append would lose the last range, including the only range for singleton input.

`merged.size()` is `3`. `new int[3][]` allocates an outer array with three row slots; `toArray` fills those slots with the list’s already-created independent rows. Return `[[1,6],[8,10],[15,18]]`. Input remains `[[8,10],[1,3],[2,6],[15,18]]`. Equivalent: allocate `int[][] result=new int[merged.size()][]`, then copy each list row into its result slot with a loop. The returned rows belong to the result, not the original input.

## Complete reference solution

Runnable reference: [MergeIntervalsSolution.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/prefixintervals/mergeintervals/MergeIntervalsSolution.java). The walkthrough immediately above explains the complete algorithm below; the `main` runner and assertions are in the linked Java file.

```java
// Input: intervals=[[8,10],[1,3],[2,6],[15,18]]
// Expected return: [[1,6],[8,10],[15,18]]

static int[][] merge(int[][] intervals) {
    if (intervals.length == 0) {
        return new int[0][2];
    }
    int[][] sorted = new int[intervals.length][2];
    for (int index = 0; index < intervals.length; index++) {
        sorted[index] = intervals[index].clone();
    }
    Arrays.sort(sorted, Comparator.comparingInt(interval -> interval[0]));
    List<int[]> merged = new ArrayList<>();
    int start = sorted[0][0];
    int end = sorted[0][1];

    for (int index = 1; index < sorted.length; index++) {
        int[] current = sorted[index];
        if (current[0] <= end) {
            end = Math.max(end, current[1]);
        } else {
            merged.add(new int[] {start, end});
            start = current[0];
            end = current[1];
        }
    }
    merged.add(new int[] {start, end});
    return merged.toArray(new int[merged.size()][]);
}
```

## Invariant, correctness, and complexity

Before scanning the next sorted row, `merged` contains finalized disjoint intervals and `[start,end]` covers the unfinished connected group. If the next start is at most end, taking the larger end preserves that group’s coverage. If the next start exceeds end, every later start is at least as large, so no future row can meet the old range; finalizing it is safe. The final append accounts for the unfinished last group.

Copying takes `O(n)`, comparison sorting takes `O(n log n)`, and the scan takes `O(n)`. Overall `O(n log n)` time and `O(n)` additional space for independent rows, sorting workspace, and output. If input is already sorted, a direct scan is `O(n)`; do not assume that for this problem. Sorting the original outer array avoids that particular copy but changes caller-owned order and may still use sorting workspace; this version favors clear input preservation.

## Edge cases and exact results

- `[[1,4],[4,5]]`: `4<=4 → true`, so end becomes `max(4,5)=5`; return `[[1,5]]`.
- `[[1,10],[2,3],[4,5]]`: `2<=10` and `4<=10` are true; `max(10,3)=10` and `max(10,5)=10`; result `[[1,10]]`.
- Duplicate points `[[2,2],[2,2]]` meet at `2`; `max(2,2)=2`, so output is one `[2,2]`.
- `[[3,7]]`: the scan begins at `index=1`, so `1<1` is false; final append returns `[[3,7]]`.
- Empty input takes the defensive guard and returns `[]` without sorting or reading the first row.

## Practice after learning the concept

Open [MergeIntervalsPractice.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/prefixintervals/mergeintervals/MergeIntervalsPractice.java) in IntelliJ, implement the marked TODO method(s), then run its `main`. Checks include unsorted input, touching/nested intervals, duplicate points, singleton/empty inputs, and output mutation that must not affect the input. The unfinished program reports that practice is ready; that message is not a passing solution.

Reveal only after attempting: [MergeIntervalsSolution.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/prefixintervals/mergeintervals/MergeIntervalsSolution.java). Its runner checks sample and edge cases and throws if an answer is wrong.

## Questions and explained answers

<details>
<summary>1. Why can a gap finalize the active range permanently?</summary>

After sorting, every later start is at least the current start. If the current start exceeds the active end, no later interval can reach back to overlap that active range.

</details>

<details>
<summary>2. Why take max(end,current[1]) rather than assign current[1]?</summary>

A later interval can lie inside the active one. With [1,10] then [2,3], assigning 3 would lose covered points; max(10,3)=10 preserves them.

</details>

<details>
<summary>3. Why append once after the loop?</summary>

A gap saves the previous range. The final active range has no subsequent gap to trigger saving, so a final append is necessary even for a single input interval.

</details>

## Stop/go

Keep learning `TODO`. Go only after passing the practice checks, explaining why sorting makes finalization safe, predicting the touching and nested cases, stating time/space costs, and answering the checkpoints. Do not treat the artifact’s passing reference runner as comprehension evidence.
