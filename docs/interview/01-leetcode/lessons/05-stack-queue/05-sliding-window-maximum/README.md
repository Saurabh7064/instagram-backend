# Sliding Window Maximum — Keep Only Useful Candidates

## Question

Given an integer array `nums` and a window width `k`, move the width-`k` window from left to right by one position at a time. Return the maximum value in each complete window, in that order. Do not modify `nums`.

Problem: [Sliding Window Maximum](https://leetcode.com/problems/sliding-window-maximum/). Editable practice: [SlidingWindowMaximumPractice.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/stackqueue/windowmaximum/SlidingWindowMaximumPractice.java).

Assumptions: `nums` is non-null, `1 <= nums.length <= 100000`, and `1 <= k <= nums.length`; values may be any Java `int`. Empty/null input and invalid widths are outside this method's contract, so the reference does not add validation branches. Aim for `O(n)` total time with `O(k)` auxiliary space, plus `O(n-k+1)` output space.

## What you will learn

- Remove a saved input position when it falls outside the current window.
- Discard an older value when a newer value is at least as large and remains eligible longer.
- Keep a collection whose first entry supplies the current maximum without rescanning the window.

## Derive each window's maximum first

The fixed input is `nums=[1,3,-1,-3,5,3,6,7]`, with `k=3`. The window consists of three adjacent input positions. The return contains maximum values, not their input indexes.

| Input index | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 |
|---|---:|---:|---:|---:|---:|---:|---:|---:|
| Value | 1 | 3 | -1 | -3 | 5 | 3 | 6 | 7 |

| Window start/end | Values compared | Calculation | Returned value |
|---|---|---|---:|
| `[0,2]` | `[1,3,-1]` | `max(1,3)=3`; `max(3,-1)=3` | 3 |
| `[1,3]` | `[3,-1,-3]` | `max(3,-1)=3`; `max(3,-3)=3` | 3 |
| `[2,4]` | `[-1,-3,5]` | `max(-1,-3)=-1`; `max(-1,5)=5` | 5 |
| `[3,5]` | `[-3,5,3]` | `max(-3,5)=5`; `max(5,3)=5` | 5 |
| `[4,6]` | `[5,3,6]` | `max(5,3)=5`; `max(5,6)=6` | 6 |
| `[5,7]` | `[3,6,7]` | `max(3,6)=6`; `max(6,7)=7` | 7 |

The return is `[3,3,5,5,6,7]`. There are `8-3+1=6` complete windows: a width of three allows starts 0 through 5, inclusive. Rescanning all three values works here, but for a large width it repeats work. We need saved candidates that survive a one-position window move.

## Keep positions that can still become a maximum

Prerequisites: read [fixed-size windows](../../03-sliding-window/01-maximum-average-subarray/README.md) for adjacent-range boundaries and [Daily Temperatures](../03-daily-temperatures/README.md) for storing input indexes. The required boundary idea is that a width-three window ending at input position 4 starts at `4-3+1=2`. The `+1` includes the end: indexes 2, 3, and 4 are exactly three positions.

An older saved position can become useless in two ways. First, it can leave the window. When the end reaches index 4, index 1 is before the new start 2 and cannot supply that window's maximum, even though its value 3 was useful earlier. Second, a newer value can make an older value unnecessary. At index 1, the new value 3 is larger than the older 1 at index 0. Any future window containing that older index and the current position has both values, so 1 cannot beat 3. The newer 3 also expires later. We say the older entry is a **dominated candidate**: it can never be needed again for a future maximum. Equality is enough for this argument, because a newer equal value supplies the same maximum and lasts longer.

We need to remove expired oldest entries at one end and dominated newest entries at the other. A **deque** is a collection allowing reads, additions, and removals at both ends. The deque stores input indexes, oldest at the first end and newest at the last end. Their corresponding values decrease strictly from first to last after removing older values `<=` the incoming value. This is a **monotonic deque**: the saved values keep one direction of order. Its first value is consequently the largest remaining candidate.

Java's `Deque<Integer>` interface declares these operations and `new ArrayDeque<>()` provides their storage. `peekFirst()` and `peekLast()` read without removal; `removeFirst()` and `removeLast()` remove and return an entry; `addLast(right)` appends the new input index. `isEmpty()` protects reads. The `Integer` indexes are automatically converted to `int` for comparisons and array access. A protected nonempty read prevents a missing entry from being converted. Tables always use first → last order, not an implementation-dependent printed order.

The output array `maxima` is separate from these indexes. Its position 0 stores the maximum for input window `[0,2]`; position 1 is for `[1,3]`. For any complete window, the output index equals its starting input index `right-k+1`. Unfilled zero cells are placeholders, not answers for incomplete windows. The input is read only, and the newly allocated output can be edited independently after return.

## Block-by-block code walkthrough

### Allocate exactly one result per complete window

```java
int[] maxima = new int[nums.length - k + 1];
Deque<Integer> candidates = new ArrayDeque<>();
```

The contract guarantees that the result length is positive: `8-3+1=6`. Java fills the new array with zeros and creates an empty deque. No input guard executes because valid inputs are promised. A width larger than the array or a null input would violate that promise; this method does not silently invent an answer for them. Allocating only `n-k` results would lose the final window; allocating `n` would add cells without windows. An equivalent clearer form names `int windowCount = nums.length-k+1;` before allocation.

| State after allocation | Value |
|---|---|
| `maxima` | `[0,0,0,0,0,0]` |
| `candidates` first → last | `[]` |

### Expire old positions, remove dominated positions, then record full windows

```java
for (int right = 0; right < nums.length; right++) {
    while (!candidates.isEmpty() && candidates.peekFirst() <= right - k) {
        candidates.removeFirst();
    }
    while (!candidates.isEmpty() && nums[candidates.peekLast()] <= nums[right]) {
        candidates.removeLast();
    }
    candidates.addLast(right);
    if (right >= k - 1) {
        maxima[right - k + 1] = nums[candidates.peekFirst()];
    }
}
```

`right` scans every input index once. The first while removes indexes `<= right-k`, exactly those before the current start `right-k+1`. At `right=4`, `right-k=1`; index 1 expires, whereas index 2 is still in the window. An equivalent condition is `candidates.peekFirst() < right-k+1`. Removing only values without keeping their indexes would make this expiration test impossible.

The second while compares the newest saved candidate's value with the incoming value. An older value `<=` the incoming one is removed because it is dominated. After removals, `addLast(right)` preserves increasing input indexes and strictly decreasing candidate values. Without repeated removals, the first candidate could fail to be the largest. Java evaluates each `&&` from left to right: a false nonempty check skips the dependent peek and array access. An equivalent clearer form for either loop is a nonempty loop with a separate stopping `if` before removal.

`right >= k-1` asks whether `k` input positions have arrived. Here `k-1=2`; the first write happens at right 2. A true branch reads the first candidate's index, reads its input value, and writes to output position `right-k+1`. No complete-window result is written in a false branch. Because the current index is added before that read, the deque is nonempty. Reordering the write before addition could lose today's largest value. The next execution follows these operations in their exact order.

**Narrated execution.** Fixed `nums=[1,3,-1,-3,5,3,6,7]`, `k=3`; initial `maxima=[0,0,0,0,0,0]`, `candidates=[]`, and `right=0`. Candidate entries are input indexes in first → last order; their values remain in `nums`.

### Iteration right=0

- ✅ `for (right < nums.length)`: `0 < 8 → true`; read `nums[right]` at index 0 → 1.
- ❌ `while (!candidates.isEmpty() && candidates.peekFirst() <= right - k)`: ❌ `!candidates.isEmpty()`: `false`; `peekFirst()` and expiration comparison are `Skipped`; whole condition → `false`. Skip `removeFirst()` and finish expiration.
- ❌ `while (!candidates.isEmpty() && nums[candidates.peekLast()] <= nums[right])`: ❌ `!candidates.isEmpty()`: `false`; `peekLast()` and value comparison are `Skipped`; whole condition → `false`. Skip `removeLast()` and finish dominance removal.
- `candidates.addLast(right)`: append index 0; `candidates` [] → [0]; corresponding values are `[1]`.
- ❌ `if (right >= k - 1)`: `0 >= 3 - 1 = 2 → false`. Skip `maxima[right-k+1] = nums[candidates.peekFirst()]`; `maxima=[0,0,0,0,0,0]` stays unchanged because fewer than 3 values have arrived.
- `right++`: `0 → 1`; next action is the for-loop guard. Final iteration state is `candidates=[0]` and `maxima=[0,0,0,0,0,0]`.

| right | Incoming value | Candidates before | Candidates after | Values after | Full maxima after | Next right |
|---:|---:|---|---|---|---|---:|
| 0 | 1 | `[]` | `[0]` | `[1]` | `[0,0,0,0,0,0]` | 1 |

### Iteration right=1

- ✅ `for (right < nums.length)`: `1 < 8 → true`; read `nums[right]` at index 1 → 3.
- ❌ `while (!candidates.isEmpty() && candidates.peekFirst() <= right - k)`: ✅ `!candidates.isEmpty()`: `true`; ❌ `candidates.peekFirst() <= right - k`: `0 <= 1 - 3 = -2 → false`; whole condition → `false`. Skip `removeFirst()` and finish expiration.
- ✅ `while (!candidates.isEmpty() && nums[candidates.peekLast()] <= nums[right])`: ✅ `!candidates.isEmpty()`: `true`; ✅ `nums[candidates.peekLast()] <= nums[right]`: `nums[0] <= nums[1]`, or `1 <= 3 → true`; whole condition → `true`. Execute the body.
- `candidates.removeLast()`: remove dominated index 0 (value 1); `candidates` [0] → []; `maxima=[0,0,0,0,0,0]` unchanged. Recheck the value condition.
- ❌ `while (!candidates.isEmpty() && nums[candidates.peekLast()] <= nums[right])`: ❌ `!candidates.isEmpty()`: `false`; `peekLast()` and value comparison are `Skipped`; whole condition → `false`. Skip `removeLast()` and finish dominance removal.
- `candidates.addLast(right)`: append index 1; `candidates` [] → [1]; corresponding values are `[3]`.
- ❌ `if (right >= k - 1)`: `1 >= 3 - 1 = 2 → false`. Skip `maxima[right-k+1] = nums[candidates.peekFirst()]`; `maxima=[0,0,0,0,0,0]` stays unchanged because fewer than 3 values have arrived.
- `right++`: `1 → 2`; next action is the for-loop guard. Final iteration state is `candidates=[1]` and `maxima=[0,0,0,0,0,0]`.

| right | Incoming value | Candidates before | Candidates after | Values after | Full maxima after | Next right |
|---:|---:|---|---|---|---|---:|
| 1 | 3 | `[0]` | `[1]` | `[3]` | `[0,0,0,0,0,0]` | 2 |

### Iteration right=2

- ✅ `for (right < nums.length)`: `2 < 8 → true`; read `nums[right]` at index 2 → -1.
- ❌ `while (!candidates.isEmpty() && candidates.peekFirst() <= right - k)`: ✅ `!candidates.isEmpty()`: `true`; ❌ `candidates.peekFirst() <= right - k`: `1 <= 2 - 3 = -1 → false`; whole condition → `false`. Skip `removeFirst()` and finish expiration.
- ❌ `while (!candidates.isEmpty() && nums[candidates.peekLast()] <= nums[right])`: ✅ `!candidates.isEmpty()`: `true`; ❌ `nums[candidates.peekLast()] <= nums[right]`: `nums[1] <= nums[2]`, or `3 <= -1 → false`; whole condition → `false`. Skip `removeLast()` and finish dominance removal.
- `candidates.addLast(right)`: append index 2; `candidates` [1] → [1,2]; corresponding values are `[3,-1]`.
- ✅ `if (right >= k - 1)`: `2 >= 3 - 1 = 2 → true`. Execute the output assignment.
- `maxima[right - k + 1] = nums[candidates.peekFirst()]`: output index `2 - 3 + 1 = 0`; first candidate index 1 reads `nums[1]=3`; `maxima[0]` 0 → 3; full `maxima=[3,0,0,0,0,0]`.
- `right++`: `2 → 3`; next action is the for-loop guard. Final iteration state is `candidates=[1,2]` and `maxima=[3,0,0,0,0,0]`.

| right | Incoming value | Candidates before | Candidates after | Values after | Full maxima after | Next right |
|---:|---:|---|---|---|---|---:|
| 2 | -1 | `[1]` | `[1,2]` | `[3,-1]` | `[3,0,0,0,0,0]` | 3 |

### Iteration right=3

- ✅ `for (right < nums.length)`: `3 < 8 → true`; read `nums[right]` at index 3 → -3.
- ❌ `while (!candidates.isEmpty() && candidates.peekFirst() <= right - k)`: ✅ `!candidates.isEmpty()`: `true`; ❌ `candidates.peekFirst() <= right - k`: `1 <= 3 - 3 = 0 → false`; whole condition → `false`. Skip `removeFirst()` and finish expiration.
- ❌ `while (!candidates.isEmpty() && nums[candidates.peekLast()] <= nums[right])`: ✅ `!candidates.isEmpty()`: `true`; ❌ `nums[candidates.peekLast()] <= nums[right]`: `nums[2] <= nums[3]`, or `-1 <= -3 → false`; whole condition → `false`. Skip `removeLast()` and finish dominance removal.
- `candidates.addLast(right)`: append index 3; `candidates` [1,2] → [1,2,3]; corresponding values are `[3,-1,-3]`.
- ✅ `if (right >= k - 1)`: `3 >= 3 - 1 = 2 → true`. Execute the output assignment.
- `maxima[right - k + 1] = nums[candidates.peekFirst()]`: output index `3 - 3 + 1 = 1`; first candidate index 1 reads `nums[1]=3`; `maxima[1]` 0 → 3; full `maxima=[3,3,0,0,0,0]`.
- `right++`: `3 → 4`; next action is the for-loop guard. Final iteration state is `candidates=[1,2,3]` and `maxima=[3,3,0,0,0,0]`.

| right | Incoming value | Candidates before | Candidates after | Values after | Full maxima after | Next right |
|---:|---:|---|---|---|---|---:|
| 3 | -3 | `[1,2]` | `[1,2,3]` | `[3,-1,-3]` | `[3,3,0,0,0,0]` | 4 |

### Iteration right=4

- ✅ `for (right < nums.length)`: `4 < 8 → true`; read `nums[right]` at index 4 → 5.
- ✅ `while (!candidates.isEmpty() && candidates.peekFirst() <= right - k)`: ✅ `!candidates.isEmpty()`: `true`; ✅ `candidates.peekFirst() <= right - k`: `1 <= 4 - 3 = 1 → true`; whole condition → `true`. Execute expiration.
- `candidates.removeFirst()`: remove expired index 1 (value 3); `candidates` [1,2,3] → [2,3]; `maxima=[3,3,0,0,0,0]` unchanged. Recheck expiration.
- ❌ `while (!candidates.isEmpty() && candidates.peekFirst() <= right - k)`: ✅ `!candidates.isEmpty()`: `true`; ❌ `candidates.peekFirst() <= right - k`: `2 <= 4 - 3 = 1 → false`; whole condition → `false`. Skip `removeFirst()` and finish expiration.
- ✅ `while (!candidates.isEmpty() && nums[candidates.peekLast()] <= nums[right])`: ✅ `!candidates.isEmpty()`: `true`; ✅ `nums[candidates.peekLast()] <= nums[right]`: `nums[3] <= nums[4]`, or `-3 <= 5 → true`; whole condition → `true`. Execute the body.
- `candidates.removeLast()`: remove dominated index 3 (value -3); `candidates` [2,3] → [2]; `maxima=[3,3,0,0,0,0]` unchanged. Recheck the value condition.
- ✅ `while (!candidates.isEmpty() && nums[candidates.peekLast()] <= nums[right])`: ✅ `!candidates.isEmpty()`: `true`; ✅ `nums[candidates.peekLast()] <= nums[right]`: `nums[2] <= nums[4]`, or `-1 <= 5 → true`; whole condition → `true`. Execute the body.
- `candidates.removeLast()`: remove dominated index 2 (value -1); `candidates` [2] → []; `maxima=[3,3,0,0,0,0]` unchanged. Recheck the value condition.
- ❌ `while (!candidates.isEmpty() && nums[candidates.peekLast()] <= nums[right])`: ❌ `!candidates.isEmpty()`: `false`; `peekLast()` and value comparison are `Skipped`; whole condition → `false`. Skip `removeLast()` and finish dominance removal.
- `candidates.addLast(right)`: append index 4; `candidates` [] → [4]; corresponding values are `[5]`.
- ✅ `if (right >= k - 1)`: `4 >= 3 - 1 = 2 → true`. Execute the output assignment.
- `maxima[right - k + 1] = nums[candidates.peekFirst()]`: output index `4 - 3 + 1 = 2`; first candidate index 4 reads `nums[4]=5`; `maxima[2]` 0 → 5; full `maxima=[3,3,5,0,0,0]`.
- `right++`: `4 → 5`; next action is the for-loop guard. Final iteration state is `candidates=[4]` and `maxima=[3,3,5,0,0,0]`.

| right | Incoming value | Candidates before | Candidates after | Values after | Full maxima after | Next right |
|---:|---:|---|---|---|---|---:|
| 4 | 5 | `[1,2,3]` | `[4]` | `[5]` | `[3,3,5,0,0,0]` | 5 |

### Iteration right=5

- ✅ `for (right < nums.length)`: `5 < 8 → true`; read `nums[right]` at index 5 → 3.
- ❌ `while (!candidates.isEmpty() && candidates.peekFirst() <= right - k)`: ✅ `!candidates.isEmpty()`: `true`; ❌ `candidates.peekFirst() <= right - k`: `4 <= 5 - 3 = 2 → false`; whole condition → `false`. Skip `removeFirst()` and finish expiration.
- ❌ `while (!candidates.isEmpty() && nums[candidates.peekLast()] <= nums[right])`: ✅ `!candidates.isEmpty()`: `true`; ❌ `nums[candidates.peekLast()] <= nums[right]`: `nums[4] <= nums[5]`, or `5 <= 3 → false`; whole condition → `false`. Skip `removeLast()` and finish dominance removal.
- `candidates.addLast(right)`: append index 5; `candidates` [4] → [4,5]; corresponding values are `[5,3]`.
- ✅ `if (right >= k - 1)`: `5 >= 3 - 1 = 2 → true`. Execute the output assignment.
- `maxima[right - k + 1] = nums[candidates.peekFirst()]`: output index `5 - 3 + 1 = 3`; first candidate index 4 reads `nums[4]=5`; `maxima[3]` 0 → 5; full `maxima=[3,3,5,5,0,0]`.
- `right++`: `5 → 6`; next action is the for-loop guard. Final iteration state is `candidates=[4,5]` and `maxima=[3,3,5,5,0,0]`.

| right | Incoming value | Candidates before | Candidates after | Values after | Full maxima after | Next right |
|---:|---:|---|---|---|---|---:|
| 5 | 3 | `[4]` | `[4,5]` | `[5,3]` | `[3,3,5,5,0,0]` | 6 |

### Iteration right=6

- ✅ `for (right < nums.length)`: `6 < 8 → true`; read `nums[right]` at index 6 → 6.
- ❌ `while (!candidates.isEmpty() && candidates.peekFirst() <= right - k)`: ✅ `!candidates.isEmpty()`: `true`; ❌ `candidates.peekFirst() <= right - k`: `4 <= 6 - 3 = 3 → false`; whole condition → `false`. Skip `removeFirst()` and finish expiration.
- ✅ `while (!candidates.isEmpty() && nums[candidates.peekLast()] <= nums[right])`: ✅ `!candidates.isEmpty()`: `true`; ✅ `nums[candidates.peekLast()] <= nums[right]`: `nums[5] <= nums[6]`, or `3 <= 6 → true`; whole condition → `true`. Execute the body.
- `candidates.removeLast()`: remove dominated index 5 (value 3); `candidates` [4,5] → [4]; `maxima=[3,3,5,5,0,0]` unchanged. Recheck the value condition.
- ✅ `while (!candidates.isEmpty() && nums[candidates.peekLast()] <= nums[right])`: ✅ `!candidates.isEmpty()`: `true`; ✅ `nums[candidates.peekLast()] <= nums[right]`: `nums[4] <= nums[6]`, or `5 <= 6 → true`; whole condition → `true`. Execute the body.
- `candidates.removeLast()`: remove dominated index 4 (value 5); `candidates` [4] → []; `maxima=[3,3,5,5,0,0]` unchanged. Recheck the value condition.
- ❌ `while (!candidates.isEmpty() && nums[candidates.peekLast()] <= nums[right])`: ❌ `!candidates.isEmpty()`: `false`; `peekLast()` and value comparison are `Skipped`; whole condition → `false`. Skip `removeLast()` and finish dominance removal.
- `candidates.addLast(right)`: append index 6; `candidates` [] → [6]; corresponding values are `[6]`.
- ✅ `if (right >= k - 1)`: `6 >= 3 - 1 = 2 → true`. Execute the output assignment.
- `maxima[right - k + 1] = nums[candidates.peekFirst()]`: output index `6 - 3 + 1 = 4`; first candidate index 6 reads `nums[6]=6`; `maxima[4]` 0 → 6; full `maxima=[3,3,5,5,6,0]`.
- `right++`: `6 → 7`; next action is the for-loop guard. Final iteration state is `candidates=[6]` and `maxima=[3,3,5,5,6,0]`.

| right | Incoming value | Candidates before | Candidates after | Values after | Full maxima after | Next right |
|---:|---:|---|---|---|---|---:|
| 6 | 6 | `[4,5]` | `[6]` | `[6]` | `[3,3,5,5,6,0]` | 7 |

### Iteration right=7

- ✅ `for (right < nums.length)`: `7 < 8 → true`; read `nums[right]` at index 7 → 7.
- ❌ `while (!candidates.isEmpty() && candidates.peekFirst() <= right - k)`: ✅ `!candidates.isEmpty()`: `true`; ❌ `candidates.peekFirst() <= right - k`: `6 <= 7 - 3 = 4 → false`; whole condition → `false`. Skip `removeFirst()` and finish expiration.
- ✅ `while (!candidates.isEmpty() && nums[candidates.peekLast()] <= nums[right])`: ✅ `!candidates.isEmpty()`: `true`; ✅ `nums[candidates.peekLast()] <= nums[right]`: `nums[6] <= nums[7]`, or `6 <= 7 → true`; whole condition → `true`. Execute the body.
- `candidates.removeLast()`: remove dominated index 6 (value 6); `candidates` [6] → []; `maxima=[3,3,5,5,6,0]` unchanged. Recheck the value condition.
- ❌ `while (!candidates.isEmpty() && nums[candidates.peekLast()] <= nums[right])`: ❌ `!candidates.isEmpty()`: `false`; `peekLast()` and value comparison are `Skipped`; whole condition → `false`. Skip `removeLast()` and finish dominance removal.
- `candidates.addLast(right)`: append index 7; `candidates` [] → [7]; corresponding values are `[7]`.
- ✅ `if (right >= k - 1)`: `7 >= 3 - 1 = 2 → true`. Execute the output assignment.
- `maxima[right - k + 1] = nums[candidates.peekFirst()]`: output index `7 - 3 + 1 = 5`; first candidate index 7 reads `nums[7]=7`; `maxima[5]` 0 → 7; full `maxima=[3,3,5,5,6,7]`.
- `right++`: `7 → 8`; next action is the for-loop guard. Final iteration state is `candidates=[7]` and `maxima=[3,3,5,5,6,7]`.

| right | Incoming value | Candidates before | Candidates after | Values after | Full maxima after | Next right |
|---:|---:|---|---|---|---|---:|
| 7 | 7 | `[6]` | `[7]` | `[7]` | `[3,3,5,5,6,7]` | 8 |

- ❌ `for (right < nums.length)`: `8 < 8 → false`; skip the body and leave the loop. The loop-local `right` leaves scope. All six output positions have been filled.

### Return the maximum values, not the candidate indexes

```java
return maxima;
```

`return maxima` yields `[3,3,5,5,6,7]`, matching all six maxima derived from the actual windows. The remaining deque `[7]` identifies a possible maximum in the final working state; it is not the requested history of maxima. Returning it would lose five window results and return an index rather than its value. No mutation follows the return. An equivalent spelling stores the same newly created array reference in a local `answer` before returning it; a second copy is unnecessary.

| Returned maxima | Final candidate indexes | Input after return |
|---|---|---|
| `[3,3,5,5,6,7]` | `[7]` | `[1,3,-1,-3,5,3,6,7]` |

## Complete reference solution

Runnable reference: [SlidingWindowMaximumSolution.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/stackqueue/windowmaximum/SlidingWindowMaximumSolution.java). This complete method keeps its exact input, width, and expected return adjacent; the linked Java file contains the directly runnable `main` and assertions as well.

```java
// nums index: 0 1  2  3 4 5 6 7
// nums      : 1 3 -1 -3 5 3 6 7
// Input: nums=[1,3,-1,-3,5,3,6,7]; k=3
// Expected return: [3,3,5,5,6,7]
public static int[] maxSlidingWindow(int[] nums, int k) {
        int[] maxima = new int[nums.length - k + 1];
        Deque<Integer> candidates = new ArrayDeque<>();
        for (int right = 0; right < nums.length; right++) {
            while (!candidates.isEmpty() && candidates.peekFirst() <= right - k) {
                candidates.removeFirst();
            }
            while (!candidates.isEmpty() && nums[candidates.peekLast()] <= nums[right]) {
                candidates.removeLast();
            }
            candidates.addLast(right);
            if (right >= k - 1) {
                maxima[right - k + 1] = nums[candidates.peekFirst()];
            }
        }
        return maxima;
}
```

## Why the first candidate is the current maximum

After each insertion, candidate indexes increase from first to last, all belong to the current partial or complete window, and their values strictly decrease. Every omitted current-window index was removed because a newer retained-or-later-replaced index had at least its value and expires no earlier. This is the maintained invariant: the deque contains all candidates still needed to find current or future maxima.

Expiration removes only indexes before the current start. Removing a dominated index preserves a replacement at least as valuable that remains eligible for every future window in which the older index could matter. Repeated replacement preserves that argument even when a replacing index is itself later removed. After insertion, the first candidate has the largest saved value and no omitted eligible value can beat it. Recording that value therefore gives the window maximum. The first output happens only when a full window exists, and the formula fills each result position exactly once.

Every index enters the deque once and leaves at most once, from either end. The total work of all while bodies is therefore `O(n)`; stopping checks add `O(n)` more. The deque has at most `k` entries because expired entries leave before insertion, so auxiliary space is `O(k)`. The returned array separately occupies `O(n-k+1)` space. Directly scanning each window is a reasonable small-input alternative with constant auxiliary space, but takes `O((n-k+1)k)` time. Keeping every value in the deque would preserve data but lose the constant-time maximum read unless another structure tracked the maximum.

## Boundary mistakes to diagnose

- At `right=4`, index 1 must expire because the new window begins at 2. Using `< right-k` instead of `<= right-k` would keep that expired index one step too long; the symptom is a maximum from outside the current window.
- Comparing candidate indexes instead of `nums[index]` compares age rather than value. Index 3 is newer than index 1, but `nums[3]=-3` is smaller than `nums[1]=3`.
- A false full-window test at `right=1` must skip the output write. Writing then would use index `1-3+1=-1`, outside the result array.
- The dominance comparison intentionally uses `<=`: a newer equal value lasts longer, so the older equal entry is unnecessary. A strict `<` implementation can also be correct if it retains ties and expires each index properly; it uses non-increasing values instead of this method's strictly decreasing ones. Equality checks stay in the runnable programs, preserving this lesson's fixed input.
- Reusing zero as a running maximum would be incorrect for negative windows. The reference reads a real eligible candidate; for window `[2,4]`, it compares actual values `-1,-3,5` and obtains 5. All-negative checks are in the runner.

When a result is wrong, identify the first window with a mismatch and inspect its start, all candidate indexes, and their input values. Remove out-of-window indexes first, restore repeated `<=` dominance removal, then check that the result index equals the window start. Rerun the same fixed-input checks before checking additional edge cases.

## Practice after learning the concept

Open [SlidingWindowMaximumPractice.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/stackqueue/windowmaximum/SlidingWindowMaximumPractice.java) in IntelliJ, implement the TODO method, and run `main`. Its checks cover the fixed example, one position, width one, whole-array width, decreasing values with expiration, equal values, negative values, full-int comparisons, and independent input/output ownership. The unfinished program reports “Practice ready”; this is not a successful solution. Reveal [SlidingWindowMaximumSolution.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/stackqueue/windowmaximum/SlidingWindowMaximumSolution.java) after making your own attempt.

## Questions and explained answers

<details>
<summary>1. At right=4, which saved position expires and why?</summary>

The width-three window starts at `4-3+1=2`. Saved index 1 is before that start, equivalently `1 <= 4-3`, so it expires from the first end. Indexes 2 and 3 are still inside the window and are not expired; they subsequently leave from the last end because the new value 5 dominates their values -1 and -3.

</details>

<details>
<summary>2. Why can index 0 be discarded permanently when index 1 arrives?</summary>

Index 0 has value 1, while newer index 1 has value 3. Any later window that could still contain index 0 after index 1 arrives also includes index 1, whose value is larger. Index 1 expires later as well. Thus index 0 cannot supply a future maximum that index 1 cannot supply or improve. It is discarded for a different reason from expiration.

</details>

<details>
<summary>3. At right=6, what output position is written, and why is the whole method O(n)?</summary>

The window starts at `6-3+1=4`, so `maxima[4]` receives the value 6 from input index 6. Input position and output value remain distinct. Every input index is added once and removed at most once; across all outer iterations the two removal loops perform at most `n` removals in total. The repeated loops therefore do not create quadratic work.

</details>

## Stop/go

Learning stays `TODO`. Passing the reference runner establishes artifact correctness, not learner understanding. Go only after your own practice passes, you derive the six maxima, explain expiration versus dominance using the fixed input, predict the write at right 6, diagnose the boundary mistakes, state the invariant and time/space costs, and answer the three checkpoints without notes. Otherwise stop here and revisit the missing prerequisite.
