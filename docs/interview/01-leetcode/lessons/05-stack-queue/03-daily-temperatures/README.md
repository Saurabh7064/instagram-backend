# Daily Temperatures — Resolve a Waiting Day Once

## Question

Given an array `temperatures` of daily temperatures, return an array where each position tells how many days you must wait for a strictly warmer temperature. Return `0` at positions with no later warmer day. Preserve the input order and do not modify the input.

Problem: [Daily Temperatures](https://leetcode.com/problems/daily-temperatures/). Editable practice: [DailyTemperaturesPractice.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/stackqueue/temperatures/DailyTemperaturesPractice.java).

Assumptions: the input is non-null; the original problem has `1 <= temperatures.length <= 100000` and `30 <= temperatures[i] <= 100`. The reference also returns an empty array for an empty input. Null validation is outside this contract. Aim for `O(n)` total time and `O(n)` auxiliary space, plus the returned array.

## What you will learn

- Keep only earlier days that still need an answer.
- Use a warmer day to answer several waiting days, while preserving their first-warmer guarantee.
- Store input indexes so both temperature comparisons and waiting distances remain available.

## Find the requested answer before choosing saved state

Use this fixed input throughout the lesson. Positions are zero-based day indexes, and the output is a distance, not a temperature or an input index.

| Day index | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 |
|---|---:|---:|---:|---:|---:|---:|---:|---:|
| Temperature | 73 | 74 | 75 | 71 | 69 | 72 | 76 | 73 |

For each day, look forward until the first strictly larger value. This gives every requested output, before introducing the faster implementation:

| Starting day and temperature | First later warmer day | Why earlier later days do not qualify | Waiting distance |
|---|---|---|---|
| `0:73` | `1:74` | The next day is already warmer. | `1 - 0 = 1` |
| `1:74` | `2:75` | The next day is already warmer. | `2 - 1 = 1` |
| `2:75` | `6:76` | Days 3–5 are `71,69,72`, all below 75. | `6 - 2 = 4` |
| `3:71` | `5:72` | Day 4 is 69, below 71. | `5 - 3 = 2` |
| `4:69` | `5:72` | The next day is already warmer. | `5 - 4 = 1` |
| `5:72` | `6:76` | The next day is already warmer. | `6 - 5 = 1` |
| `6:76` | None | The only later temperature, 73, is lower. | `0` |
| `7:73` | None | There are no later days. | `0` |

The requested return is therefore `[1,1,4,2,1,1,0,0]`. Checking each future day independently repeats comparisons. Instead, walk through the input once and let each arriving temperature answer the earlier days it can help.

## Keep unanswered days in the order they must be resolved

Prerequisite: a **stack** keeps items so the most recently added one is the first removed; review [nested delimiters](../01-valid-parentheses/README.md) for this rule. Here the items are unresolved input indexes. Index 3 means day 3, whose temperature can still be read as `temperatures[3]=71`; keeping only 71 would lose the day needed to calculate a distance.

When day 5 brings 72, the most recent waiting day is 4 at 69. We can answer it with `5-4=1`, then inspect day 3 at 71 and answer it with `5-3=2`. Day 2 at 75 must keep waiting. Processing days in order ensures that 72 is the first later warmer value for each removed day: an earlier warmer arrival would already have removed it.

The waiting temperatures run from warmer to cooler, allowing equality, from the oldest entry to the newest. This is a **monotonic stack**: its values preserve one direction of ordering. A new temperature removes smaller values from the newest end, then joins that end. Equal values stay because the question requires strictly warmer. Every removed index is complete and never needs to return.

Java's `Deque<Integer>` is the interface for a collection usable at both ends; `new ArrayDeque<>()` supplies its implementation. This lesson uses only its last end as the stack top: `addLast(day)` adds, `peekLast()` reads without removal, and `removeLast()` removes and returns the newest index. `isEmpty()` prevents reading a nonexistent top. Using `addFirst`/`peekFirst`/`removeFirst` consistently would be equivalent; mixing ends would break the stack behavior. Tables show the oldest index on the left and newest on the right, regardless of how a Java debugger formats the collection.

`waits` is a new result array initially full of zero. A zero means no warmer day has been found yet; after the scan, zeros that remain are final no-answer results. The input array is only read. The returned array is independently owned and may be edited without changing the input.

## Block-by-block code walkthrough

### Allocate one answer per day and an empty waiting collection

```java
int[] waits = new int[temperatures.length];
Deque<Integer> pending = new ArrayDeque<>();
```

`temperatures.length` evaluates to 8, so allocation creates eight zero answers. `pending` initially contains no indexes. These saved values serve different roles: `waits[2]` is the distance eventually returned for day 2, while a `2` inside `pending` is an unresolved input position. Reusing the input for results would destroy temperatures still needed by comparisons. An equivalent initialization would explicitly fill the new array with zeros, but Java already guarantees those zeros.

| State after initialization | Value |
|---|---|
| `waits` | `[0,0,0,0,0,0,0,0]` |
| `pending` oldest → newest | `[]` |

### Resolve smaller waiting temperatures, then save the current day

```java
for (int day = 0; day < temperatures.length; day++) {
    while (!pending.isEmpty() && temperatures[pending.peekLast()] < temperatures[day]) {
        int earlier = pending.removeLast();
        waits[earlier] = day - earlier;
    }
    pending.addLast(day);
}
```

`day` starts at 0 and advances by one after each complete iteration. Its guard excludes 8 because valid input indexes end at 7. The `while` condition first asks whether a waiting day exists, then reads its temperature and compares it with today's. Java evaluates `&&` left to right: when `!pending.isEmpty()` is false, the top access is skipped. Without that first operand an empty collection would supply no usable index. The equivalent clearer form is a nonempty loop with a separate `if (temperatures[pending.peekLast()] >= temperatures[day]) break;`.

When the comparison succeeds, `removeLast()` returns the waiting index and permanently removes it. `earlier` exists only in that execution of the while body. `day-earlier` is the number of day steps between the two input positions, so no `+1` belongs in it: day 2 to day 6 is four steps, not five. The assignment updates the older day's answer, not today's. After all smaller waiting temperatures are resolved, `addLast(day)` saves the current day for a future arrival. Omitting it would leave today's answer impossible to fill. The following execution shows each repeated check and each mutation beside its state.

**Narrated execution.** Input `temperatures=[73,74,75,71,69,72,76,73]`; initial `waits=[0,0,0,0,0,0,0,0]`, `pending=[]`, and `day=0`. In every collection below, entries are input indexes, oldest → newest; temperatures are still read from the unchanged input.

### Iteration day=0

- ✅ `for (day < temperatures.length)`: `0 < 8 → true`; read `temperatures[day]` at index 0 → 73.
- ❌ `while (!pending.isEmpty() && temperatures[pending.peekLast()] < temperatures[day])`: ❌ `!pending.isEmpty()`: `false`; temperature comparison and `peekLast()` are `Skipped`; whole condition → `false`. Skip `removeLast` and the answer assignment; finish this while loop.
- `pending.addLast(day)`: append index 0; `pending` [] → [0].
- `day++`: `0 → 1`; next action is the for-loop guard. The completed iteration retains `waits=[0,0,0,0,0,0,0,0]` and `pending=[0]`.

| day | Temperature | pending before | pending after | Full waits after | Next day |
|---:|---:|---|---|---|---:|
| 0 | 73 | `[]` | `[0]` | `[0,0,0,0,0,0,0,0]` | 1 |

### Iteration day=1

- ✅ `for (day < temperatures.length)`: `1 < 8 → true`; read `temperatures[day]` at index 1 → 74.
- ✅ `while (!pending.isEmpty() && temperatures[pending.peekLast()] < temperatures[day])`: ✅ `!pending.isEmpty()`: `true`; ✅ `temperatures[pending.peekLast()] < temperatures[day]`: `temperatures[0] < temperatures[1]`, or `73 < 74 → true`; whole condition → `true`. Execute the body.
- `int earlier = pending.removeLast()`: read/remove index 0; `pending` [0] → []; `earlier=0`.
- `waits[earlier] = day - earlier`: `waits[0]` 0 → `1 - 0 = 1`; full `waits=[1,0,0,0,0,0,0,0]`. Recheck the while condition; this body's `earlier` leaves scope.
- ❌ `while (!pending.isEmpty() && temperatures[pending.peekLast()] < temperatures[day])`: ❌ `!pending.isEmpty()`: `false`; temperature comparison and `peekLast()` are `Skipped`; whole condition → `false`. Skip `removeLast` and the answer assignment; finish this while loop.
- `pending.addLast(day)`: append index 1; `pending` [] → [1].
- `day++`: `1 → 2`; next action is the for-loop guard. The completed iteration retains `waits=[1,0,0,0,0,0,0,0]` and `pending=[1]`.

| day | Temperature | pending before | pending after | Full waits after | Next day |
|---:|---:|---|---|---|---:|
| 1 | 74 | `[0]` | `[1]` | `[1,0,0,0,0,0,0,0]` | 2 |

### Iteration day=2

- ✅ `for (day < temperatures.length)`: `2 < 8 → true`; read `temperatures[day]` at index 2 → 75.
- ✅ `while (!pending.isEmpty() && temperatures[pending.peekLast()] < temperatures[day])`: ✅ `!pending.isEmpty()`: `true`; ✅ `temperatures[pending.peekLast()] < temperatures[day]`: `temperatures[1] < temperatures[2]`, or `74 < 75 → true`; whole condition → `true`. Execute the body.
- `int earlier = pending.removeLast()`: read/remove index 1; `pending` [1] → []; `earlier=1`.
- `waits[earlier] = day - earlier`: `waits[1]` 0 → `2 - 1 = 1`; full `waits=[1,1,0,0,0,0,0,0]`. Recheck the while condition; this body's `earlier` leaves scope.
- ❌ `while (!pending.isEmpty() && temperatures[pending.peekLast()] < temperatures[day])`: ❌ `!pending.isEmpty()`: `false`; temperature comparison and `peekLast()` are `Skipped`; whole condition → `false`. Skip `removeLast` and the answer assignment; finish this while loop.
- `pending.addLast(day)`: append index 2; `pending` [] → [2].
- `day++`: `2 → 3`; next action is the for-loop guard. The completed iteration retains `waits=[1,1,0,0,0,0,0,0]` and `pending=[2]`.

| day | Temperature | pending before | pending after | Full waits after | Next day |
|---:|---:|---|---|---|---:|
| 2 | 75 | `[1]` | `[2]` | `[1,1,0,0,0,0,0,0]` | 3 |

### Iteration day=3

- ✅ `for (day < temperatures.length)`: `3 < 8 → true`; read `temperatures[day]` at index 3 → 71.
- ❌ `while (!pending.isEmpty() && temperatures[pending.peekLast()] < temperatures[day])`: ✅ `!pending.isEmpty()`: `true`; ❌ `temperatures[pending.peekLast()] < temperatures[day]`: `temperatures[2] < temperatures[3]`, or `75 < 71 → false`; whole condition → `false`. Skip `removeLast` and the answer assignment; finish this while loop.
- `pending.addLast(day)`: append index 3; `pending` [2] → [2,3].
- `day++`: `3 → 4`; next action is the for-loop guard. The completed iteration retains `waits=[1,1,0,0,0,0,0,0]` and `pending=[2,3]`.

| day | Temperature | pending before | pending after | Full waits after | Next day |
|---:|---:|---|---|---|---:|
| 3 | 71 | `[2]` | `[2,3]` | `[1,1,0,0,0,0,0,0]` | 4 |

### Iteration day=4

- ✅ `for (day < temperatures.length)`: `4 < 8 → true`; read `temperatures[day]` at index 4 → 69.
- ❌ `while (!pending.isEmpty() && temperatures[pending.peekLast()] < temperatures[day])`: ✅ `!pending.isEmpty()`: `true`; ❌ `temperatures[pending.peekLast()] < temperatures[day]`: `temperatures[3] < temperatures[4]`, or `71 < 69 → false`; whole condition → `false`. Skip `removeLast` and the answer assignment; finish this while loop.
- `pending.addLast(day)`: append index 4; `pending` [2,3] → [2,3,4].
- `day++`: `4 → 5`; next action is the for-loop guard. The completed iteration retains `waits=[1,1,0,0,0,0,0,0]` and `pending=[2,3,4]`.

| day | Temperature | pending before | pending after | Full waits after | Next day |
|---:|---:|---|---|---|---:|
| 4 | 69 | `[2,3]` | `[2,3,4]` | `[1,1,0,0,0,0,0,0]` | 5 |

### Iteration day=5

- ✅ `for (day < temperatures.length)`: `5 < 8 → true`; read `temperatures[day]` at index 5 → 72.
- ✅ `while (!pending.isEmpty() && temperatures[pending.peekLast()] < temperatures[day])`: ✅ `!pending.isEmpty()`: `true`; ✅ `temperatures[pending.peekLast()] < temperatures[day]`: `temperatures[4] < temperatures[5]`, or `69 < 72 → true`; whole condition → `true`. Execute the body.
- `int earlier = pending.removeLast()`: read/remove index 4; `pending` [2,3,4] → [2,3]; `earlier=4`.
- `waits[earlier] = day - earlier`: `waits[4]` 0 → `5 - 4 = 1`; full `waits=[1,1,0,0,1,0,0,0]`. Recheck the while condition; this body's `earlier` leaves scope.
- ✅ `while (!pending.isEmpty() && temperatures[pending.peekLast()] < temperatures[day])`: ✅ `!pending.isEmpty()`: `true`; ✅ `temperatures[pending.peekLast()] < temperatures[day]`: `temperatures[3] < temperatures[5]`, or `71 < 72 → true`; whole condition → `true`. Execute the body.
- `int earlier = pending.removeLast()`: read/remove index 3; `pending` [2,3] → [2]; `earlier=3`.
- `waits[earlier] = day - earlier`: `waits[3]` 0 → `5 - 3 = 2`; full `waits=[1,1,0,2,1,0,0,0]`. Recheck the while condition; this body's `earlier` leaves scope.
- ❌ `while (!pending.isEmpty() && temperatures[pending.peekLast()] < temperatures[day])`: ✅ `!pending.isEmpty()`: `true`; ❌ `temperatures[pending.peekLast()] < temperatures[day]`: `temperatures[2] < temperatures[5]`, or `75 < 72 → false`; whole condition → `false`. Skip `removeLast` and the answer assignment; finish this while loop.
- `pending.addLast(day)`: append index 5; `pending` [2] → [2,5].
- `day++`: `5 → 6`; next action is the for-loop guard. The completed iteration retains `waits=[1,1,0,2,1,0,0,0]` and `pending=[2,5]`.

| day | Temperature | pending before | pending after | Full waits after | Next day |
|---:|---:|---|---|---|---:|
| 5 | 72 | `[2,3,4]` | `[2,5]` | `[1,1,0,2,1,0,0,0]` | 6 |

### Iteration day=6

- ✅ `for (day < temperatures.length)`: `6 < 8 → true`; read `temperatures[day]` at index 6 → 76.
- ✅ `while (!pending.isEmpty() && temperatures[pending.peekLast()] < temperatures[day])`: ✅ `!pending.isEmpty()`: `true`; ✅ `temperatures[pending.peekLast()] < temperatures[day]`: `temperatures[5] < temperatures[6]`, or `72 < 76 → true`; whole condition → `true`. Execute the body.
- `int earlier = pending.removeLast()`: read/remove index 5; `pending` [2,5] → [2]; `earlier=5`.
- `waits[earlier] = day - earlier`: `waits[5]` 0 → `6 - 5 = 1`; full `waits=[1,1,0,2,1,1,0,0]`. Recheck the while condition; this body's `earlier` leaves scope.
- ✅ `while (!pending.isEmpty() && temperatures[pending.peekLast()] < temperatures[day])`: ✅ `!pending.isEmpty()`: `true`; ✅ `temperatures[pending.peekLast()] < temperatures[day]`: `temperatures[2] < temperatures[6]`, or `75 < 76 → true`; whole condition → `true`. Execute the body.
- `int earlier = pending.removeLast()`: read/remove index 2; `pending` [2] → []; `earlier=2`.
- `waits[earlier] = day - earlier`: `waits[2]` 0 → `6 - 2 = 4`; full `waits=[1,1,4,2,1,1,0,0]`. Recheck the while condition; this body's `earlier` leaves scope.
- ❌ `while (!pending.isEmpty() && temperatures[pending.peekLast()] < temperatures[day])`: ❌ `!pending.isEmpty()`: `false`; temperature comparison and `peekLast()` are `Skipped`; whole condition → `false`. Skip `removeLast` and the answer assignment; finish this while loop.
- `pending.addLast(day)`: append index 6; `pending` [] → [6].
- `day++`: `6 → 7`; next action is the for-loop guard. The completed iteration retains `waits=[1,1,4,2,1,1,0,0]` and `pending=[6]`.

| day | Temperature | pending before | pending after | Full waits after | Next day |
|---:|---:|---|---|---|---:|
| 6 | 76 | `[2,5]` | `[6]` | `[1,1,4,2,1,1,0,0]` | 7 |

### Iteration day=7

- ✅ `for (day < temperatures.length)`: `7 < 8 → true`; read `temperatures[day]` at index 7 → 73.
- ❌ `while (!pending.isEmpty() && temperatures[pending.peekLast()] < temperatures[day])`: ✅ `!pending.isEmpty()`: `true`; ❌ `temperatures[pending.peekLast()] < temperatures[day]`: `temperatures[6] < temperatures[7]`, or `76 < 73 → false`; whole condition → `false`. Skip `removeLast` and the answer assignment; finish this while loop.
- `pending.addLast(day)`: append index 7; `pending` [6] → [6,7].
- `day++`: `7 → 8`; next action is the for-loop guard. The completed iteration retains `waits=[1,1,4,2,1,1,0,0]` and `pending=[6,7]`.

| day | Temperature | pending before | pending after | Full waits after | Next day |
|---:|---:|---|---|---|---:|
| 7 | 73 | `[6]` | `[6,7]` | `[1,1,4,2,1,1,0,0]` | 8 |

- ❌ `for (day < temperatures.length)`: `8 < 8 → false`; skip the body. `day` leaves loop scope. No future warmer value exists for remaining indexes `[6,7]`, so their zero answers remain correct.

### Return the independently owned answer array

```java
return waits;
```

`return waits` yields `[1,1,4,2,1,1,0,0]`, matching the independently derived distances. It does not return `pending=[6,7]`, which is unfinished-work state rather than the requested output. No mutation or further loop check follows the return. An equivalent spelling is to assign the same array reference to a local named `answer` and return it; copying is unnecessary because the array was created inside this call and never aliases the input.

| Final result | Remaining pending indexes | Input after return |
|---|---|---|
| `[1,1,4,2,1,1,0,0]` | `[6,7]` | `[73,74,75,71,69,72,76,73]` |

## Complete reference solution

Runnable reference: [DailyTemperaturesSolution.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/stackqueue/temperatures/DailyTemperaturesSolution.java). The named input and expected return accompany the complete method; the Java file also contains its directly runnable `main` and edge-case checks.

```java
// temperatures index:  0  1  2  3  4  5  6  7
// temperatures      : 73 74 75 71 69 72 76 73
// Input: temperatures=[73,74,75,71,69,72,76,73]
// Expected return: [1,1,4,2,1,1,0,0]
public static int[] dailyTemperatures(int[] temperatures) {
        int[] waits = new int[temperatures.length];
        Deque<Integer> pending = new ArrayDeque<>();
        for (int day = 0; day < temperatures.length; day++) {
            while (!pending.isEmpty() && temperatures[pending.peekLast()] < temperatures[day]) {
                int earlier = pending.removeLast();
                waits[earlier] = day - earlier;
            }
            pending.addLast(day);
        }
        return waits;
}
```

## Why this finds the first warmer day

Before each outer iteration, `pending` contains exactly the earlier days not yet answered, in increasing index order. Their temperatures are non-increasing from oldest to newest. Every removed day has already received its first later warmer day.

At day 5, removing day 4 at 69 exposes day 3 at 71, which can also be answered by 72. Stopping at day 2 at 75 is safe: that day and all older remaining candidates have temperatures at least 75, so none can be answered by 72. Adding the new day preserves the order. A resolved day cannot have missed an earlier warmer temperature, because that earlier arrival would have removed it then. At the end, remaining entries have no later warmer arrival; their initialized zeros are correct.

Each index is added once and removed at most once. Even though one day can remove several entries, all removals across the whole method total at most `n`. Time is `O(n)`. The waiting collection needs `O(n)` auxiliary space in the worst case, and the returned array separately needs `O(n)` output space. A direct forward search for every day is easier to write and uses constant auxiliary space, but can take `O(n²)` time; choose it only when small inputs make that tradeoff acceptable.

## Common mistakes and their visible effects

- Using `<=` would treat equal temperatures as warmer. The requirement is strictly larger, so the while comparison is `<`; equal days remain waiting until a larger arrival or the end. Equal-value checks are in the runnable files rather than replacing this fixed example.
- Storing temperatures without indexes loses the starting day for the subtraction. At day 6, knowing only 75 does not tell us that the answer belongs at `waits[2]` or that its distance is `6-2=4`.
- Writing `waits[day]` would overwrite today's still-unknown answer. Day 6 answers older positions 5 and 2; its own zero stays unchanged.
- Removing only one waiting entry at day 5 would miss day 3, and removing only one at day 6 would miss day 2. The repeated while checks are part of the algorithm.
- An incorrect answer can be diagnosed by checking the first iteration where `pending` no longer means exactly the unanswered days. Restore the strict comparison, older answer index, and repeated resolution, then rerun the checks.

## Practice after learning the concept

Open [DailyTemperaturesPractice.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/stackqueue/temperatures/DailyTemperaturesPractice.java) in IntelliJ. Implement its marked TODO method and run `main`. It checks the fixed input, single/equal/falling/rising temperatures, several resolutions at one arrival, the empty-input extension, and independent input/output ownership. An intentional “Practice ready” message means the method is unfinished; it is not a passing attempt. Compare [DailyTemperaturesSolution.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/stackqueue/temperatures/DailyTemperaturesSolution.java) only after attempting the method.

## Questions and explained answers

<details>
<summary>1. At day 5, why do days 4 and 3 leave but day 2 stays?</summary>

The incoming 72 is strictly larger than 69 at day 4 and 71 at day 3, giving `5-4=1` and `5-3=2`. It is smaller than 75 at day 2, so day 2 still needs a later warmer value. The remaining older values are no smaller than the top, so stopping there is safe.

</details>

<details>
<summary>2. Why is day 2's answer 4 rather than 5 or 6?</summary>

Its first later warmer temperature is 76 at index 6. A waiting time counts the steps between indexes: `6-2=4`. Six is the destination index, while five would incorrectly count both endpoints. These are different quantities from the returned distance.

</details>

<details>
<summary>3. How can nested loops still take O(n) total time?</summary>

Every day index enters `pending` once. Once removed, its answer is final and the index never reenters. Thus there are `n` additions and at most `n` removals in the entire scan, rather than up to `n` removals for each of `n` days. Failed stopping checks add only a constant amount per outer iteration.

</details>

## Stop/go

Learning status stays `TODO`; preparing and verifying the reference does not establish comprehension. Go only after your own practice checks pass, you derive all eight distances, explain the waiting-index invariant and strict comparison, diagnose the missed-second-removal mistake, state time/space costs, and answer the three checkpoints without notes. Otherwise stop at this lesson; do not activate the next one.
