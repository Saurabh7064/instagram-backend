# Micro-Lesson 05 — Insert Interval

## Question

Given disjoint inclusive intervals already sorted by start, and one additional interval, return the sorted disjoint intervals after inserting it and combining all overlaps. Sharing an endpoint counts as overlap; the input arrays need not be modified in place.

Problem: [Insert Interval on LeetCode](https://leetcode.com/problems/insert-interval/) · Editable Java: [InsertIntervalPractice.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/prefixintervals/insertinterval/InsertIntervalPractice.java)

## What you will learn

- Use sorted order to copy before, combine overlaps, then copy after.
- Preserve the full covered range when an overlap extends its end.
- Evaluate loop bounds before accessing interval rows.

## Lesson status and prerequisites

- Learning status: `TODO`; this artifact is prepared, not activated or mastered.
- Prerequisite: [Merge Intervals](../04-merge-intervals/README.md); in particular, inclusive endpoints and why an expanding end preserves coverage.
- Primary idea: Use sorted order to copy before, combine overlaps, then copy after.
- New terms (at most three): before region, overlap region, after region

## Inputs and limits

`0 <= intervals.length <= 10,000`; every row and `newInterval` have exactly two endpoints, with `0 <= start <= end <= 100,000`. Existing intervals are sorted by start and do not overlap. Target `O(n)` time and `O(n)` output storage; this implementation copies output rows so inputs remain independent. Null or unsorted input is outside the contract.

## First find the answer in learner language

Sorting again would throw away a useful promise: existing ranges are already ordered and separated. Move through them once using three parts. The **before region** contains intervals ending strictly before the new start. The **overlap region** contains intervals meeting the combined new interval. The **after region** contains everything still to its right once no more overlaps can occur.

1. Copy intervals whose end is less than the new start. They cannot overlap it.
2. For each interval whose start is at most the current combined end, expand the new range to include it.
3. Save that combined range exactly once.
4. Copy all remaining intervals; sorted order guarantees they come after it.

For `[[1,2],[3,5],[6,7],[8,10],[12,16]]`, new `[4,8]`: copy `[1,2]` because `2<4`; combining `[3,5]` changes the new range to `[3,8]`; `[6,7]` keeps `[3,8]`; `[8,10]` touches its end and expands it to `[3,10]`; `[12,16]` starts after `10` so remains separate. Return `[[1,2],[3,10],[12,16]]`.

## Block-by-block code walkthrough

Original snapshots are retained. Where a value changes several times, its cell shows the sequence ending at the final value.

Input `intervals=[[1,2],[3,5],[6,7],[8,10],[12,16]]`, `newInterval=[4,8]`; neither input is changed. Every repeated loop check appears below, including its final false check.

### Block 1 — Initialize a scan and separate editable endpoints

```java
List<int[]> result = new ArrayList<>();
int index = 0;
int start = newInterval[0];
int end = newInterval[1];
```

- `new ArrayList<>()` creates `result=[]`; `int index = 0` starts at the first row.
- `int start = newInterval[0]` reads `4`; `int end = newInterval[1]` reads `8`.

Reading endpoints into local integers lets the new combined range expand without editing `newInterval`. If we edited the provided row directly, caller-owned state would change. Equivalent: clone the new row and edit that independent copy, but two scalars expose the state clearly. Empty intervals need no early guard because every loop checks length before reading a row.

### Block 2 — Copy every interval strictly before the new one

```java
while (index < intervals.length && intervals[index][1] < start) {
    result.add(intervals[index].clone());
    index++;
}
```

Java evaluates `&&` left to right. First ensure `index` refers to an existing row. Only then read its end and compare it to `start`. Strict `<` leaves a touching endpoint for the overlap loop. `clone()` creates an independent row, `add` appends that row, and only then `index++` advances. Equivalent: separately test the bound and break when the row end is not strictly before start. Using `<=` would wrongly separate touching intervals.

**Before-loop iteration `index=0`.**

- **✅ `while (index < intervals.length && intervals[index][1] < start)`:** first **✅ `index < intervals.length`:** `0 < 5 → true`; then **✅ `intervals[index][1] < start`:** `2 < 4 → true`; enter the body.
- `result.add(intervals[index].clone())` reads `[1,2]`, copies it, and appends it: `result`: `[] → [[1,2]]`.
- `index++`: `0 → 1`; `start=4, end=8` stay unchanged. Recheck the same `while` next.

| index before | bounds check | end < start | action | result after | start/end | index after |
| --- | --- | --- | --- | --- | --- | --- |
| `0` | `✅ 0<5 → true` | `✅ 2<4 → true` | `copy [1,2]` | `[[1, 2]]` | `[4, 8]` | `1` |

**Before-loop stopping check `index=1`.**

- **❌ `while (index < intervals.length && intervals[index][1] < start)`:** first **✅ `index < intervals.length`:** `1 < 5 → true`; then **❌ `intervals[index][1] < start`:** `5 < 4 → false`.
- Skip `result.add(...)` and `index++`; `index=1, start=4, end=8, result=[[1,2]]` stay unchanged. Continue to the overlap loop at this same row.

| index | bounds | end < start | body | result | next action |
| --- | --- | --- | --- | --- | --- |
| `1` | `✅ 1<5 → true` | `❌ 5<4 → false` | `Skipped` | `[[1, 2]]` | `enter overlap loop at index=1` |

### Block 3 — Expand across every overlapping row

```java
while (index < intervals.length && intervals[index][0] <= end) {
    start = Math.min(start, intervals[index][0]);
    end = Math.max(end, intervals[index][1]);
    index++;
}
```

At this point every earlier row was strictly before the new start. Any remaining row starting at or before `end` therefore meets the combined range. `Math.min` keeps the earlier start; `Math.max` keeps the later end. The changed end describes all coverage accumulated so far and is used in the **next** condition. Under this problem’s promise that old intervals are disjoint, extending past the original new end cannot unexpectedly connect another old interval: that later interval starts beyond the extended old interval’s end. Keeping the updated end still makes the covered-range invariant explicit. Equivalent: `currentStart` and `currentEnd` locals can make the two row reads more explicit. Failing to preserve the larger end would lose the covered tail of the last merged interval.

**Overlap iteration `index=1`.**

- **✅ `while (index < intervals.length && intervals[index][0] <= end)`:** first **✅ `index < intervals.length`:** `1 < 5 → true`; then **✅ `intervals[index][0] <= end`:** `3 <= 8 → true`; enter the body.
- `start = Math.min(start, intervals[index][0])`: `min(4,3)=3`, so `start`: `4 → 3`.
- `end = Math.max(end, intervals[index][1])`: `max(8,5)=8`, so `end`: `8 → 8`.
- `index++`: `1 → 2`; recheck this `while` using the updated end. `result=[[1,2]]` stays unchanged; no append occurs in this loop.

| index | bounds | current | start <= end | start update | end update | result after | next index |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `1` | `✅ 1<5 → true` | `[3, 5]` | `✅ 3<=8 → true` | `min(4,3)=3` | `max(8,5)=8` | `[[1, 2]]` | `2` |

**Overlap iteration `index=2`.**

- **✅ `while (index < intervals.length && intervals[index][0] <= end)`:** first **✅ `index < intervals.length`:** `2 < 5 → true`; then **✅ `intervals[index][0] <= end`:** `6 <= 8 → true`; enter the body.
- `start = Math.min(start, intervals[index][0])`: `min(3,6)=3`, so `start`: `3 → 3`.
- `end = Math.max(end, intervals[index][1])`: `max(8,7)=8`, so `end`: `8 → 8`.
- `index++`: `2 → 3`; recheck this `while` using the updated end. `result=[[1,2]]` stays unchanged; no append occurs in this loop.

| index | bounds | current | start <= end | start update | end update | result after | next index |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `2` | `✅ 2<5 → true` | `[6, 7]` | `✅ 6<=8 → true` | `min(3,6)=3` | `max(8,7)=8` | `[[1, 2]]` | `3` |

**Overlap iteration `index=3`.**

- **✅ `while (index < intervals.length && intervals[index][0] <= end)`:** first **✅ `index < intervals.length`:** `3 < 5 → true`; then **✅ `intervals[index][0] <= end`:** `8 <= 8 → true`; enter the body.
- `start = Math.min(start, intervals[index][0])`: `min(3,8)=3`, so `start`: `3 → 3`.
- `end = Math.max(end, intervals[index][1])`: `max(8,10)=10`, so `end`: `8 → 10`.
- `index++`: `3 → 4`; recheck this `while` using the updated end. `result=[[1,2]]` stays unchanged; no append occurs in this loop.

| index | bounds | current | start <= end | start update | end update | result after | next index |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `3` | `✅ 3<5 → true` | `[8, 10]` | `✅ 8<=8 → true` | `min(3,8)=3` | `max(8,10)=10` | `[[1, 2]]` | `4` |

**Overlap-loop stopping check `index=4`.**

- **❌ `while (index < intervals.length && intervals[index][0] <= end)`:** first **✅ `index < intervals.length`:** `4 < 5 → true`; then **❌ `intervals[index][0] <= end`:** `12 <= 10 → false`.
- Skip both endpoint assignments and `index++`; `index=4, start=3, end=10, result=[[1,2]]` stay unchanged. Continue to append the combined range.

| index | bounds | start <= end | body | start/end after | next |
| --- | --- | --- | --- | --- | --- |
| `4` | `✅ 4<5 → true` | `❌ 12<=10 → false` | `Skipped` | `[3, 10]` | `append combined range` |

### Block 4 — Save the combined interval exactly once

```java
result.add(new int[] {start, end});
```

- `result.add(new int[] {start, end})` creates `[3,10]` and appends it: `[[1,2]] → [[1,2],[3,10]]`.
- `index=4, start=3, end=10` stay unchanged; continue to the after loop. Both input arrays stay unchanged.

Without this append the inserted interval would disappear. Appending inside the overlap loop would produce intermediate ranges or none at all when there were no overlaps. Equivalent: name the new row `combined` before adding it.

### Block 5 — Copy remaining rows and finish the scan

```java
while (index < intervals.length) {
    result.add(intervals[index].clone());
    index++;
}
```

Every remaining start is beyond the combined end, and the original rows are disjoint, so no more merging is needed. Copy each row into the result rather than storing an input row reference. There is no second condition or inner branch in this loop. Equivalent: a `for` loop starting from the current index; do not reset index to zero.

**After-loop iteration `index=4`.**

- **✅ `while (index < intervals.length)`:** `4 < 5 → true`; enter the body.
- `result.add(intervals[index].clone())` reads `[12,16]`, copies it, and appends it: `[[1,2],[3,10]] → [[1,2],[3,10],[12,16]]`.
- `index++`: `4 → 5`; `start=3, end=10` stay unchanged. Recheck this `while` next.

| index | condition | row copied | result after | next |
| --- | --- | --- | --- | --- |
| `4` | `✅ 4<5 → true` | `[12, 16]` | `[[1, 2], [3, 10], [12, 16]]` | `index=5` |

**After-loop stopping check `index=5`.**

- **❌ `while (index < intervals.length)`:** `5 < 5 → false`; skip the append and increment. `intervals[5]` is never read.
- `index=5, start=3, end=10, result=[[1,2],[3,10],[12,16]]`; continue to the return.

### Block 6 — Return an array of independent output rows

```java
return result.toArray(new int[result.size()][]);
```

The list has size `3`. Allocate three outer row slots with `new int[3][]`; `toArray` fills them with the output rows already created. Return `[[1,2],[3,10],[12,16]]`. There is no array mutation after this return. The input stays `[[1,2],[3,5],[6,7],[8,10],[12,16]]` and the new input row stays `[4,8]`. Equivalent: allocate an outer `int[][]` and assign the three list rows by a loop. Without converting, the declared `int[][]` method cannot return a `List<int[]>`.

## Complete reference solution

Runnable reference: [InsertIntervalSolution.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/prefixintervals/insertinterval/InsertIntervalSolution.java). The walkthrough immediately above explains the complete algorithm below; the `main` runner and assertions are in the linked Java file.

```java
// intervals index:     0     1     2      3       4
// intervals      : [1,2] [3,5] [6,7] [8,10] [12,16]
// Endpoint indexes inside each interval: [0]=start, [1]=end.
// newInterval index: 0 1
// newInterval      : 4 8
// Input: intervals=[[1,2],[3,5],[6,7],[8,10],[12,16]]; newInterval=[4,8]
// Expected return: [[1,2],[3,10],[12,16]]

static int[][] insert(int[][] intervals, int[] newInterval) {
    List<int[]> result = new ArrayList<>();
    int index = 0;
    int start = newInterval[0];
    int end = newInterval[1];

    while (index < intervals.length && intervals[index][1] < start) {
        result.add(intervals[index].clone());
        index++;
    }
    while (index < intervals.length && intervals[index][0] <= end) {
        start = Math.min(start, intervals[index][0]);
        end = Math.max(end, intervals[index][1]);
        index++;
    }
    result.add(new int[] {start, end});
    while (index < intervals.length) {
        result.add(intervals[index].clone());
        index++;
    }
    return result.toArray(new int[result.size()][]);
}
```

## Invariant, correctness, and complexity

After the first loop, the output contains all intervals strictly before the new interval, and index points to the first possible overlap. During the second loop, `[start,end]` covers the inserted interval plus exactly the consumed overlaps. The next row either extends that combined interval or starts beyond its end. Sorted order means every later start is also beyond that end, so the remaining rows can be copied unchanged. Appending the combined interval between the two copied parts preserves order and covers exactly the original coverage plus the new interval.

Index moves only forward, at most once per original row across all three loops: `O(n)` time, not three nested `O(n)` scans. The result has at most `n+1` rows: `O(n)` output space; endpoints and index use `O(1)` auxiliary scalar space beyond the output. Cloned rows are the output itself. An alternative appends the new interval then sorts and merges everything in `O(n log n)`, which is useful when the existing list is not already sorted/disjoint. Here it wastes an available guarantee.

## Edge cases and exact results

- Empty `intervals=[]`, new `[2,5]`: both initial bounds `0<0` are false, so endpoint tests are skipped; append `[2,5]`; final bound also false; return `[[2,5]]`.
- `[[1,3],[6,9]]`, new `[2,5]`: `3<2` is false; `1<=5` is true, producing `[1,5]`; `6<=5` false stops merging; copy `[6,9]`; return `[[1,5],[6,9]]`.
- `[[5,7]]`, new `[1,2]`: no before copy (`7<1` false) and no overlap (`5<=2` false); append new then copy old, giving `[[1,2],[5,7]]`.
- `[[1,2]]`, new `[5,7]`: before test `2<5` true copies the old row, then bounds fail; append new; return `[[1,2],[5,7]]`.
- `[[1,3]]`, new `[3,5]`: before test `3<3` false; overlap `1<=5` true; `min(3,1)=1`, `max(5,3)=5`; return `[[1,5]]`.
- `[[1,9]]`, new `[3,4]`: overlap gives `min(3,1)=1`, `max(4,9)=9`; return `[[1,9]]`.

## Practice after learning the concept

Open [InsertIntervalPractice.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/prefixintervals/insertinterval/InsertIntervalPractice.java) in IntelliJ, implement the marked TODO method(s), then run its `main`. Checks cover bridging several rows, insertion before/after all rows, empty input, containment, touching endpoints, and preserving both input arrays. The unfinished program reports that practice is ready; that message is not a passing solution.

Reveal only after attempting: [InsertIntervalSolution.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/prefixintervals/insertinterval/InsertIntervalSolution.java). Its runner checks sample and edge cases and throws if an answer is wrong.

## Questions and explained answers

<details>
<summary>1. Why is the before test strict but the overlap test inclusive?</summary>

An old end equal to the new start shares a point, so it must not be copied as separate. The overlap test includes equality because touching endpoints merge.

</details>

<details>
<summary>2. Why preserve the larger end after each overlap?</summary>

For old [1,5],[6,9] and new [2,7], the combined range becomes [1,7], then [1,9]. Keeping end at the original 7 would lose covered points through 9. Since old intervals are disjoint, a later old interval starts beyond 9; the extension itself does not create a further overlap beyond the original new end.

</details>

<details>
<summary>3. When index equals length, is intervals[index][0] read?</summary>

No. Java evaluates the bounds condition first; when false, && short-circuits and skips the row access. This also handles an empty input safely.

</details>

## Stop/go

Keep learning `TODO`. Advance only after passing practice checks, explaining all three regions and their bounds, predicting a touching-endpoint case, narrating an empty-input short circuit, stating the invariant/complexity, and answering the checkpoints. Artifact readiness and learner understanding are separate.
