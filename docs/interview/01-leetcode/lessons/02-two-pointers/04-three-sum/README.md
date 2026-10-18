# Micro-Lesson 04 — 3Sum

## Question
https://chatgpt.com/c/6acc39fd-b5f8-83e8-a692-91d13640a5b6

> Given an integer array, return every unique triplet whose values sum to zero, without returning duplicate triplets.

Practice source: [3Sum on LeetCode](https://leetcode.com/problems/3sum/)

## What you will learn

- how sorting exposes ordered pointer movement and adjacent duplicates;
- how fixing one anchor reduces a three-value problem to Two Sum II;
- why duplicate anchors and duplicate pointer values require separate skipping;
- how to preserve the caller's array by sorting a copy;
- why the optimized solution is `O(n²)` rather than `O(n³)`.

## Lesson status

- Learning status: `TODO`; initialized but not active
- Prerequisite: Two Sum II and duplicate-aware iteration
- Primary idea: fix one value, converge two pointers, and skip repeated values
- New terms: anchor, triplet, duplicate skipping

## Inputs, examples, and target

Assumptions:

- `numbers` is non-null.
- Output order does not matter, but each value-combination must appear once.
- Values are within LeetCode constraints, so adding three values is safe in an `int`.

Examples:

- `[-1, 0, 1, 2, -1, -4]` → `[[-1, -1, 2], [-1, 0, 1]]`
- `[0, 1, 1]` → `[]`
- `[0, 0, 0]` → `[[0, 0, 0]]`

Target: `O(n²)` time, excluding output, after `O(n log n)` sorting.

:::simulation
type: three-sum
language: java
nums: [-1, 0, 1, 2, -1, -4, 3, -3]
:::

## How the example results are calculated

First, sort the example before fixing any value:

```text
input:  [-1, 0, 1, 2, -1, -4]
sorted: [-4, -1, -1, 0, 1, 2]
index:    0   1   2  3  4  5
```

### Step 1 — Choose one value and call it the anchor

We need three values whose sum is zero. Start by choosing one value and keeping it fixed while searching for the other two. This fixed value is called the **anchor**.

Begin with the first sorted value:

```text
anchor index = 0
anchor value = -4
```

The two other values must come from positions after the anchor:

```text
anchor index < left index < right index
```

This gives three different indexes. It also gives every sorted triplet one consistent order instead of finding the same values in several arrangements.

After checking all pairs for `-4`, move the anchor to index `1`, whose value is `-1`, and search again. Continue while at least two positions remain after the anchor.

### Step 2 — Calculate what the remaining pair must provide

Fixing the anchor changes the three-value question into a two-value question:

```text
anchor + left + right = 0

therefore:
left + right = -anchor
```

For anchor `-4`, the remaining pair must add to positive `4`:

```text
required pair sum = -(-4) = 4
```

### Step 3 — Place two pointers and compare the first candidate

Place `left` immediately after the anchor and `right` at the final index:

```text
anchor = -4
left   = -1
right  = 2

left + right = -1 + 2 = 1
full sum = -4 + -1 + 2 = -3
```

The pair provides only `1`, but the anchor needs a pair sum of `4`. The full sum is therefore too small. Because the values are sorted, move `left` rightward to obtain a value that is equal or larger. Moving `right` leftward would select a value equal or smaller than `2`, which cannot increase the sum.

The decision rule is now:

```text
full sum < 0  → move left rightward to increase the sum
full sum > 0  → move right leftward to decrease the sum
full sum = 0  → record the triplet and move both pointers
```

### Example 1 — Two valid triplets

The result contains values, not their indexes. For each anchor, the required pair sum is `-anchor`.

| Anchor | Required pair sum | Left | Right | Full sum | Decision |
|---|---:|---|---|---:|---|
| `-4` at `0` | `4` | `-1` at `1` | `2` at `5` | `-3` | Too small; move `left` right |
| `-4` at `0` | `4` | `-1` at `2` | `2` at `5` | `-3` | Too small; move `left` right |
| `-4` at `0` | `4` | `0` at `3` | `2` at `5` | `-2` | Too small; move `left` right |
| `-4` at `0` | `4` | `1` at `4` | `2` at `5` | `-1` | Too small; pointers meet, so no triplet starts with `-4` |
| `-1` at `1` | `1` | `-1` at `2` | `2` at `5` | `0` | Add `[-1, -1, 2]`; move both pointers |
| `-1` at `1` | `1` | `0` at `3` | `1` at `4` | `0` | Add `[-1, 0, 1]`; move both pointers |
| `-1` at `2` | `1` | — | — | — | Skip: it duplicates the previous anchor `-1` |
| `0` at `3` | `0` | `1` at `4` | `2` at `5` | `3` | Too large; move `right` left, then pointers meet |

Therefore:

```text
-1 + -1 + 2 = 0  → [-1, -1, 2]
-1 +  0 + 1 = 0  → [-1, 0, 1]

result = [[-1, -1, 2], [-1, 0, 1]]
```

The second `-1` is not allowed to repeat the anchor search because it would rediscover the same value combinations. The algorithm removes duplicate value triplets, even though the equal values may come from different indexes.

### Example 2 — No valid triplet

```text
input and sorted: [0, 1, 1]

anchor = 0 at index 0
left   = 1 at index 1
right  = 1 at index 2
sum    = 0 + 1 + 1 = 2
```

The sum is positive, so `right` moves left. The pointers then meet, no zero-sum triplet was recorded, and the result is `[]`.

### Example 3 — Three zeroes form one unique triplet

```text
input and sorted: [0, 0, 0]

anchor = 0 at index 0
left   = 0 at index 1
right  = 0 at index 2
sum    = 0 + 0 + 0 = 0
```

The algorithm records `[0, 0, 0]`, moves both pointers, and stops when they cross. The result is `[[0, 0, 0]]` because the answer contains unique value combinations, not every possible combination of indexes.

## Concept: reduce three values to two

After sorting, choose `sorted[anchor]`. The remaining problem is:

> Find two values to the right whose sum is `-sorted[anchor]`.

That is the sorted two-pointer problem from Two Sum II. For every anchor, `left` starts just after it and `right` starts at the array end.

## Why sorting a copy matters

```java
int[] sorted = Arrays.copyOf(numbers, numbers.length);
Arrays.sort(sorted);
```

Sorting enables pointer decisions and adjacent duplicate checks. Copying first keeps the method from changing the caller's array. Sorting the original is also valid when mutation is explicitly allowed, but that contract should be stated.

## Duplicate control

Three separate rules create unique output:

1. Skip an anchor equal to the previous anchor.
2. After finding a triplet, advance both pointers.
3. Continue advancing across values equal to the values just used.

For `[0, 0, 0, 0]`, these rules return `[0, 0, 0]` once rather than four equivalent index combinations.

## Block-by-block code walkthrough

**Running example for the code and tables below:** Input `numbers = [-1,0,1,2,-1,-4]`. The copy `sorted` initially matches it, then sorting yields `[-4,-1,-1,0,1,2]`. `numbers` remains unchanged; `result = []`. Each sum row shows pointers before its branch.

Tables belong to the code block immediately above them. Inputs stay unchanged unless a block explicitly creates or changes a copy. Collection order is displayed for readability; HashMap/HashSet order is unspecified.

### Block 1 — Sort a copy and create the result

```java
int[] sorted = Arrays.copyOf(numbers, numbers.length);
Arrays.sort(sorted);
List<List<Integer>> result = new ArrayList<>();
```

**What it evaluates:** the entire input is copied, the copy is sorted, and an empty output list is created.

**Concrete result:** `[-1, 0, 1, 2, -1, -4]` becomes `[-4, -1, -1, 0, 1, 2]`, while the caller’s array remains unchanged.

**Why it is needed:** sorted order enables pointer direction and makes duplicates adjacent.

**What fails without it:** two-pointer moves are unjustified on unsorted data. Sorting `numbers` directly unexpectedly mutates the caller’s array.

**Equivalent clearer form:** `int[] sorted = numbers.clone();` makes the same full copy.

**Debugger state at this block:**

`numbers=[-1,0,1,2,-1,-4]` stays unchanged. The new `sorted` array becomes `[-4,-1,-1,0,1,2]`; `result=[]`.

### Block 2 — Choose only anchors that leave room for two values

```java
for (int anchor = 0; anchor < sorted.length - 2; anchor++) {
```

**What it evaluates:** the final anchor index is `length - 3`, leaving positions for `left` and `right`.

**Concrete result:** length `6` uses anchors `0..3`; an empty or two-element array runs zero iterations and returns `[]`.

**What fails without it:** allowing `anchor = length - 2` leaves only one later value and makes a triplet impossible.

**Equivalent clearer form:** `anchor + 2 < sorted.length` expresses the same requirement.

**Debugger state at this block:**

| anchor | left | right | sum | action | result after |
| --- | --- | --- | --- | --- | --- |
| `0` | `1` | `5` | `—` | `initialize pointers` | `[]` |
| `1` | `2` | `5` | `—` | `initialize pointers` | `[]` |
| `2` | `—` | `—` | `—` | `skip duplicate anchor` | `[[-1, -1, 2], [-1, 0, 1]]` |
| `3` | `4` | `5` | `—` | `initialize pointers` | `[[-1, -1, 2], [-1, 0, 1]]` |

### Block 3 — Skip a repeated anchor value

```java
if (anchor > 0 && sorted[anchor] == sorted[anchor - 1]) {
    continue;
}
```

**What it evaluates:** Java first checks that a previous anchor exists, then compares adjacent values. Short-circuiting prevents `sorted[-1]` at anchor `0`.

**Concrete result:** in `[-1, -1, 0, 1, 2]`, the second `-1` is skipped because the first `-1` already searched every pair to its right.

**Why it is needed:** repeating the same anchor value would rediscover identical value triplets.

**What fails without it:** output can contain duplicate triplets. Reversing the `&&` operands attempts `sorted[-1]` on the first iteration and throws.

**Equivalent clearer form:** nested `if (anchor > 0) { if (...) continue; }` has the same safe order but is longer.

**Debugger state at this block:**

| anchor | left | right | sum | action | result after |
| --- | --- | --- | --- | --- | --- |
| `2` | `—` | `—` | `—` | `skip duplicate anchor` | `[[-1, -1, 2], [-1, 0, 1]]` |

### Block 4 — Create the two-pointer search for this anchor

```java
int left = anchor + 1;
int right = sorted.length - 1;
while (left < right) {
```

**What it evaluates:** `left` starts after the anchor so no index is reused; `right` starts at the largest value. The loop requires two distinct partner positions.

**Concrete result:** with anchor index `1` in a six-element array, `left = 2` and `right = 5`.

**What fails without it:** `left = anchor` can reuse the anchor; `left <= right` can reuse one partner index twice.

**Equivalent clearer form:** no alternative is clearer; these boundaries encode the three-distinct-index rule.

**Debugger state at this block:**

| anchor | left | right | sum | action | result after |
| --- | --- | --- | --- | --- | --- |
| `0` | `1` | `5` | `—` | `initialize pointers` | `[]` |
| `1` | `2` | `5` | `—` | `initialize pointers` | `[]` |
| `3` | `4` | `5` | `—` | `initialize pointers` | `[[-1, -1, 2], [-1, 0, 1]]` |

### Block 5 — Evaluate the ordered triplet

```java
int sum = sorted[anchor] + sorted[left] + sorted[right];
```

**What it evaluates:** it adds the fixed value and current boundary pair. Under the stated constraints, `int` arithmetic is safe.

**Concrete result:** `-1 + -1 + 2 = 0` finds `[-1, -1, 2]`.

**What fails without it:** pointer movement has no comparison basis. With wider constraints, `int` overflow could reverse the comparison; then use `long`.

**Equivalent clearer form:** `int pairTarget = -sorted[anchor]` and compare `sorted[left] + sorted[right]` to it, directly showing the Two Sum reduction.

**Debugger state at this block:**

| anchor | left | right | sum | action | result after |
| --- | --- | --- | --- | --- | --- |
| `0` | `1` | `5` | `-4 + -1 + 2 = -3` | `left++ → 2` | `[]` |
| `0` | `2` | `5` | `-4 + -1 + 2 = -3` | `left++ → 3` | `[]` |
| `0` | `3` | `5` | `-4 + 0 + 2 = -2` | `left++ → 4` | `[]` |
| `0` | `4` | `5` | `-4 + 1 + 2 = -1` | `left++ → 5` | `[]` |
| `1` | `2` | `5` | `-1 + -1 + 2 = 0` | `record; left=3, right=4` | `[[-1, -1, 2]]` |
| `1` | `3` | `4` | `-1 + 0 + 1 = 0` | `record; left=4, right=3` | `[[-1, -1, 2], [-1, 0, 1]]` |
| `3` | `4` | `5` | `0 + 1 + 2 = 3` | `right-- → 4` | `[[-1, -1, 2], [-1, 0, 1]]` |

### Block 6 — Record a match and move beyond the used pair

```java
if (sum == 0) {
    result.add(List.of(sorted[anchor], sorted[left], sorted[right]));
    left++;
    right--;
```

**What it evaluates:** the immutable three-value result is appended, then both used partner positions are consumed.

**Concrete result:** after recording `[-1, 0, 1]`, neither the same `0` nor the same `1` index is tested again for that anchor.

**What fails without it:** moving only one pointer can immediately recreate the same value combination; moving neither causes an infinite loop.

**Equivalent clearer form:** save `leftValue` and `rightValue` before moving; those names can then drive duplicate skipping.

**Debugger state at this block:**

| anchor | left | right | sum | action | result after |
| --- | --- | --- | --- | --- | --- |
| `1` | `2` | `5` | `-1 + -1 + 2 = 0` | `record; left=3, right=4` | `[[-1, -1, 2]]` |
| `1` | `3` | `4` | `-1 + 0 + 1 = 0` | `record; left=4, right=3` | `[[-1, -1, 2], [-1, 0, 1]]` |

### Block 7 — Skip duplicate partner values after recording once

```java
while (left < right && sorted[left] == sorted[left - 1]) {
    left++;
}
while (left < right && sorted[right] == sorted[right + 1]) {
    right--;
}
```

**What it evaluates:** each loop first checks that two positions remain, then compares the new boundary with the value just used.

**Concrete result:** after recording `[0, 0, 0]` from `[0, 0, 0, 0]`, repeated zeros are crossed without adding the triplet again.

**Why it is needed:** anchor deduplication alone does not prevent repeated pairs for the same anchor.

**What fails without it:** duplicate triplets appear. Comparing left with `left + 1` here is wrong because `left` already moved; the used value is at `left - 1`. The right-side used value is at `right + 1`.

**Equivalent clearer form:** store the used left/right values and skip while the new values equal them.

**Debugger state at this block:**

For this sample, no partner-duplicate loop advances. For sorted `[-2,0,0,2,2]`, recording `[-2,0,2]` moves `(left,right)` from `(1,4)` to `(2,3)`; equal zeros at indexes `2` and `1` move left to `3`. Now `3 < 3` is false, so the right-duplicate expression is not evaluated.

### Block 8 — Move toward zero when no match exists

```java
} else if (sum < 0) {
    left++;
} else {
    right--;
}
```

**What it evaluates:** a negative sum needs a larger value, so `left` moves right; a positive sum needs a smaller value, so `right` moves left.

**Concrete result:** for `-4 + -1 + 2 = -3`, advancing `left` is the only boundary move that can increase the sum.

**What fails without it:** moving the opposite boundary pushes the sum farther from zero and can skip valid triplets.

**Equivalent clearer form:** compare the pair sum with `-sorted[anchor]`; the movement is identical.

**Debugger state at this block:**

| anchor | left | right | sum | action | result after |
| --- | --- | --- | --- | --- | --- |
| `0` | `1` | `5` | `-4 + -1 + 2 = -3` | `left++ → 2` | `[]` |
| `0` | `2` | `5` | `-4 + -1 + 2 = -3` | `left++ → 3` | `[]` |
| `0` | `3` | `5` | `-4 + 0 + 2 = -2` | `left++ → 4` | `[]` |
| `0` | `4` | `5` | `-4 + 1 + 2 = -1` | `left++ → 5` | `[]` |
| `3` | `4` | `5` | `0 + 1 + 2 = 3` | `right-- → 4` | `[[-1, -1, 2], [-1, 0, 1]]` |


**Complete iterations — read each row left to right:**

The condition is evaluated at the point shown. `Skipped` means the code was not executed; it does not mean the condition evaluated to false. Each row finishes its iteration or explicitly returns.

| anchor | left | right | inner loop condition | duplicate-anchor guard | sum | sum == 0 | sum < 0 | result after | movement / duplicate checks / next action |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `0` | `1` | `5` | `1 < 5 → true` | `false` | `-4+(-1)+2=-3` | `false` | `true` | `[]` | `left=2; Skipped (no match)` |
| `0` | `2` | `5` | `2 < 5 → true` | `false` | `-4+(-1)+2=-3` | `false` | `true` | `[]` | `left=3; Skipped (no match)` |
| `0` | `3` | `5` | `3 < 5 → true` | `false` | `-4+(0)+2=-2` | `false` | `true` | `[]` | `left=4; Skipped (no match)` |
| `0` | `4` | `5` | `4 < 5 → true` | `false` | `-4+(1)+2=-1` | `false` | `true` | `[]` | `left=5; Skipped (no match)` |
| `0` | `5` | `5` | `5 < 5 → false` | `already false` | `Skipped` | `Skipped` | `Skipped` | `[]` | `exit inner loop; next anchor` |
| `1` | `2` | `5` | `2 < 5 → true` | `false` | `-1+(-1)+2=0` | `true` | `Skipped` | `[[-1, -1, 2]]` | `record; left=3; right=4; left duplicate false; right duplicate false` |
| `1` | `3` | `4` | `3 < 4 → true` | `false` | `-1+(0)+1=0` | `true` | `Skipped` | `[[-1, -1, 2], [-1, 0, 1]]` | `record; left=4; right=3; left < right false; equality skipped; left < right false; equality skipped` |
| `1` | `4` | `3` | `4 < 3 → false` | `already false` | `Skipped` | `Skipped` | `Skipped` | `[[-1, -1, 2], [-1, 0, 1]]` | `exit inner loop; next anchor` |
| `2` | `—` | `—` | `Skipped` | `true; continue` | `Skipped` | `Skipped` | `Skipped` | `[[-1, -1, 2], [-1, 0, 1]]` | `next anchor` |
| `3` | `4` | `5` | `4 < 5 → true` | `false` | `0+(1)+2=3` | `false` | `false` | `[[-1, -1, 2], [-1, 0, 1]]` | `right=4; Skipped (no match)` |
| `3` | `4` | `4` | `4 < 4 → false` | `already false` | `Skipped` | `Skipped` | `Skipped` | `[[-1, -1, 2], [-1, 0, 1]]` | `exit inner loop; next anchor` |

Outer anchors `0..3` satisfy `anchor < 6-2`. Anchor `0` short-circuits `anchor > 0 && ...` at its false first operand. Anchor `2` is a duplicate and skips pointer initialization entirely. After anchor `3`, `anchor=4` makes `4 < 4` false; return the two recorded triplets.

### Block 9 — Return every unique triplet found

```java
return result;
```

**What it evaluates:** after all anchors finish, the accumulated output is returned; no-solution and short inputs return an empty list.

**What fails without it:** the method does not compile on the successful completion path.

**Equivalent clearer form:** none; `result` is the maintained output.

**Debugger state at this block:**

After anchor `3`, the outer increment makes `anchor=4`; `4 < sorted.length-2` is `4 < 4`, false. Return `[[-1,-1,2],[-1,0,1]]`. The duplicate anchor at index `2` uses `continue`, so it creates no new `left`, `right`, or `sum`. Duplicate-pointer loops do not advance in this particular input; for sorted `[-2,0,0,2,2]`, recording `[-2,0,2]` moves `(left,right)` from `(1,4)` to `(2,3)`, then the equal left zeros move `left` to `3`; `left < right` becomes false, so the right duplicate comparison short-circuits.

## Reference approach

```java
// Input: numbers = [-1, 0, 1, 2, -1, -4]
// Expected return: [[-1, -1, 2], [-1, 0, 1]]

for (int anchor = 0; anchor < sorted.length - 2; anchor++) {
    if (anchor > 0 && sorted[anchor] == sorted[anchor - 1]) {
        continue;
    }

    int left = anchor + 1;
    int right = sorted.length - 1;
    while (left < right) {
        int sum = sorted[anchor] + sorted[left] + sorted[right];
        if (sum == 0) {
            result.add(List.of(sorted[anchor], sorted[left], sorted[right]));
            left++;
            right--;
            while (left < right && sorted[left] == sorted[left - 1]) {
                left++;
            }
            while (left < right && sorted[right] == sorted[right + 1]) {
                right--;
            }
        } else if (sum < 0) {
            left++;
        } else {
            right--;
        }
    }
}
```

The complete reference method first creates `result = new ArrayList<>()` and returns it after the anchor loop.

## Invariant and correctness

For a fixed anchor, every untested candidate pair remains between `left` and `right`. Sorted-order pointer moves discard only pairs that are too small or too large. Duplicate skipping removes repeated value-combinations only after the equivalent combination has been considered.

Every possible first value is considered once, and Two Sum II finds every complementary pair for it, so every unique zero-sum triplet is returned exactly once.

## Complexity and pitfalls

- Sorting: `O(n log n)`.
- Each of `n` anchors performs an `O(n)` pointer scan: `O(n²)` total.
- Additional working space: `O(n)` because this version sorts a copy; sorting the input can reduce explicit extra array space.

Common mistakes include skipping duplicates before recording a match, skipping only anchors but not pointer values, reusing the anchor position, and returning duplicate triplets.

## Practice after learning the concept

- Write here: [ThreeSumPractice.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/twopointers/threesum/ThreeSumPractice.java)
- Reveal after attempting: [ThreeSumSolution.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/twopointers/threesum/ThreeSumSolution.java)

## Questions and explained answers

<details>
<summary>1. How does fixing an anchor reduce the problem?</summary>

Once one value is fixed, the other two must sum to its negation, which is a sorted two-pointer search.

</details>

<details>
<summary>2. Why are duplicate checks needed for both anchors and pointer values?</summary>

Repeated anchors can recreate all pairs for the same first value, while repeated left/right values can recreate the same pair for one anchor.

</details>

<details>
<summary>3. Why is the total O(n²)?</summary>

There are up to `n` anchors, and each anchor performs one linear inward scan. Sorting is asymptotically smaller than the quadratic scans.

</details>

## Stop/go

Proceed only when you can reduce 3Sum to Two Sum II, explain all duplicate skips, derive `O(n²)`, implement it, and pass all checks.
