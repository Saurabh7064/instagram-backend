# Micro-Lesson 04 — 3Sum

## Question

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

## Reference approach

```java
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

## Block-by-block code walkthrough

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

### Block 2 — Choose only anchors that leave room for two values

```java
for (int anchor = 0; anchor < sorted.length - 2; anchor++) {
```

**What it evaluates:** the final anchor index is `length - 3`, leaving positions for `left` and `right`.

**Concrete result:** length `6` uses anchors `0..3`; an empty or two-element array runs zero iterations and returns `[]`.

**What fails without it:** allowing `anchor = length - 2` leaves only one later value and makes a triplet impossible.

**Equivalent clearer form:** `anchor + 2 < sorted.length` expresses the same requirement.

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

### Block 5 — Evaluate the ordered triplet

```java
int sum = sorted[anchor] + sorted[left] + sorted[right];
```

**What it evaluates:** it adds the fixed value and current boundary pair. Under the stated constraints, `int` arithmetic is safe.

**Concrete result:** `-1 + -1 + 2 = 0` finds `[-1, -1, 2]`.

**What fails without it:** pointer movement has no comparison basis. With wider constraints, `int` overflow could reverse the comparison; then use `long`.

**Equivalent clearer form:** `int pairTarget = -sorted[anchor]` and compare `sorted[left] + sorted[right]` to it, directly showing the Two Sum reduction.

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

### Block 9 — Return every unique triplet found

```java
return result;
```

**What it evaluates:** after all anchors finish, the accumulated output is returned; no-solution and short inputs return an empty list.

**What fails without it:** the method does not compile on the successful completion path.

**Equivalent clearer form:** none; `result` is the maintained output.

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
