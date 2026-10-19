# Micro-Lesson 02 — Two Sum II

## Question

> Given a sorted integer array with exactly one valid pair, return the pair's one-based indexes when its values add to the target.

Practice source: [Two Sum II on LeetCode](https://leetcode.com/problems/two-sum-ii-input-array-is-sorted/)

## What you will learn

- how sorted order turns a sum comparison into a safe pointer decision;
- why a sum that is too small eliminates the left boundary;
- why a sum that is too large eliminates the right boundary;
- how this differs from the HashMap solution for unsorted Two Sum;
- why the result uses one-based indexes even though Java arrays are zero-based.

## Lesson status

- Learning status: `TODO`; initialized but not active
- Prerequisite: Valid Palindrome pointer movement and sorted arrays
- Primary idea: converge on a target using monotonic order
- New terms: left pointer, right pointer, monotonic order

## Inputs, examples, and target

Assumptions:

- `numbers` is sorted in nondecreasing order.
- Exactly one solution exists and one position cannot be reused.
- Values and target are between `-1,000` and `1,000`, so adding two values is safe in an `int`.
- Return one-based indexes.

Examples:

- `[2, 7, 11, 15]`, target `9` → `[1, 2]`
- `[2, 3, 4]`, target `6` → `[1, 3]`
- `[-1, 0]`, target `-1` → `[1, 2]`

Target: `O(n)` time and `O(1)` additional space.

:::simulation
type: two-sum-sorted
language: java
nums: [1, 2, 3, 4, 6, 8, 11]
target: 10
:::

## Concept: sorted order eliminates candidates

Start with the smallest and largest values. Their sum has three meanings:

- equal to target: the answer is found;
- below target: the left value is too small even with the largest available partner;
- above target: the right value is too large even with the smallest available partner.

If `[2, 3, 4]` targets `6`, the first sum is `2 + 4 = 6`. If it targeted `7`, a sum of `6` would eliminate `2`, because pairing `2` with any smaller right-side value cannot increase the sum.

## Block-by-block code walkthrough

Original snapshots are retained. Where a value changes several times, its cell shows the sequence ending at the final value.

**Running example for the code and tables below:** Input `numbers = [-5,-2,0,3,9]`, `target = 1`; initially `left = 0`, `right = 4`. The sorted input array does not change. Rows show pointers before the comparison.

Tables belong to the code block immediately above them. Inputs stay unchanged unless a block explicitly creates or changes a copy. Collection order is displayed for readability; HashMap/HashSet order is unspecified.

### Block 1 — Start with the widest candidate range

```java
int left = 0;
int right = numbers.length - 1;
```

**What it evaluates:** `left` selects the first value and `right` selects the final value. For `[2, 7, 11, 15]`, they start at indexes `0` and `3`.

**Why it is needed:** the smallest and largest remaining values provide a sum whose comparison safely eliminates one boundary.

**What fails without it:** `right = numbers.length` would make the first `numbers[right]` access invalid. For four elements, index `4` is outside valid indexes `0..3`.

**Equivalent clearer form:** `int lastIndex = numbers.length - 1; int right = lastIndex;` behaves identically.

**Debugger state at this block:**

For `numbers=[-5,-2,0,3,9]`, `target=1`, initialize `left=0`, `right=5-1=4`; `sum` is not assigned.

### Block 2 — Continue while two distinct positions remain

```java
while (left < right) {
```

**What it evaluates:** the body runs only when the pointers name two different indexes.

**Concrete result:** in `[-1, 0]`, `0 < 1` is true for the only pair. After either pointer moves, the condition becomes false.

**Why it is needed:** the same array position cannot be used twice.

**What fails without it:** `left <= right` permits the pointers to meet and may test one value against itself.

**Equivalent clearer form:** `while (right - left > 0)` is equivalent but less direct and introduces unnecessary arithmetic.

**Debugger state at this block:**

| left | right | numbers[left] | numbers[right] | sum | action |
| --- | --- | --- | --- | --- | --- |
| `0` | `4` | `-5` | `9` | `-5 + 9 = 4` | `right--` |
| `0` | `3` | `-5` | `3` | `-5 + 3 = -2` | `left++` |
| `1` | `3` | `-2` | `3` | `-2 + 3 = 1` | `return [2,4]` |

### Block 3 — Evaluate the current pair

```java
int sum = numbers[left] + numbers[right];
```

**What it evaluates:** it adds the boundary values. For `[2, 3, 4]`, the first sum is `2 + 4 = 6`.

**Why it is needed:** the comparison with `target` decides whether to return or which boundary is impossible.

**What fails without it:** the algorithm has no evidence for pointer movement. Recomputing the expression in every branch is correct but repeats the same work.

**Equivalent clearer form:** `int leftValue = numbers[left]; int rightValue = numbers[right]; int sum = leftValue + rightValue;` names the operands explicitly.

**Debugger state at this block:**

| left | right | numbers[left] | numbers[right] | sum | action |
| --- | --- | --- | --- | --- | --- |
| `0` | `4` | `-5` | `9` | `-5 + 9 = 4` | `right--` |
| `0` | `3` | `-5` | `3` | `-5 + 3 = -2` | `left++` |
| `1` | `3` | `-2` | `3` | `-2 + 3 = 1` | `return [2,4]` |

### Block 4 — Return the required one-based indexes

```java
if (sum == target) {
    return new int[] {left + 1, right + 1};
}
```

**What it evaluates:** equality proves the current pair is the promised answer. Java indexes are converted only while creating the result.

**Concrete result:** indexes `0` and `2` become `[1, 3]` for `[2, 3, 4]`, target `6`.

**What fails without it:** returning `{left, right}` violates the problem’s one-based output contract; moving a pointer after equality can discard the answer.

**Equivalent clearer form:** assign `int firstAnswerIndex = left + 1` and `int secondAnswerIndex = right + 1` before returning when the indexing conversion needs emphasis.

**Debugger state at this block:**

| left | right | numbers[left] | numbers[right] | sum | action |
| --- | --- | --- | --- | --- | --- |
| `1` | `3` | `-2` | `3` | `-2 + 3 = 1` | `return [2,4]` |

### Block 5 — Eliminate exactly one impossible boundary

```java
if (sum < target) {
    left++;
} else {
    right--;
}
```

**What it evaluates:** after equality was handled, the sum is either too small or too large. A small sum advances to a value no smaller than the current left value; a large sum retreats to a value no larger than the current right value.

**Concrete result:** for `[1, 2, 4, 8]`, target `6`, `1 + 8 = 9` is too large, so `right--` removes `8`. Moving `left` would only increase the sum further.

**Why it is needed:** sorted order proves which boundary cannot participate in any valid remaining pair.

**What fails without it:** moving the wrong pointer can skip the solution; moving neither pointer creates an infinite loop.

**Equivalent clearer form:** `else if (sum > target) { right--; }` is more explicit, but the final `else` is safe because equality already returned.

**Debugger state at this block:**

| left | right | numbers[left] | numbers[right] | sum | action |
| --- | --- | --- | --- | --- | --- |
| `0` | `4` | `-5` | `9` | `-5 + 9 = 4` | `right--` |
| `0` | `3` | `-5` | `3` | `-5 + 3 = -2` | `left++` |


**Narrated execution — follow each operation, then check its values:**

**Iteration/check 1 — left = `0`.**

Begin with left index `0` and right index `4`; distinct-position check: `0 < 4 → true`. Add their values: `-5 + 9 = 4`. Equality with the target is `false`. Only if equality is false do we evaluate the smaller-than-target check: `false`. Next, `right=3`. A sum that is too small needs a larger left value; one that is too large needs a smaller right value.

| left | right | loop condition | sum | sum == target | sum < target | next action |
| --- | --- | --- | --- | --- | --- | --- |
| `0` | `4` | `0 < 4 → true` | `-5 + 9 = 4` | `false` | `false` | `right=3` |

**Iteration/check 2 — left = `0`.**

Begin with left index `0` and right index `3`; distinct-position check: `0 < 3 → true`. Add their values: `-5 + 3 = -2`. Equality with the target is `false`. Only if equality is false do we evaluate the smaller-than-target check: `true`. Next, `left=1`. A sum that is too small needs a larger left value; one that is too large needs a smaller right value.

| left | right | loop condition | sum | sum == target | sum < target | next action |
| --- | --- | --- | --- | --- | --- | --- |
| `0` | `3` | `0 < 3 → true` | `-5 + 3 = -2` | `false` | `true` | `left=1` |

**Iteration/check 3 — left = `1`.**

Begin with left index `1` and right index `3`; distinct-position check: `1 < 3 → true`. Add their values: `-2 + 3 = 1`. Equality with the target is `true`. Only if equality is false do we evaluate the smaller-than-target check: `Skipped (returned)`. Next, `return [2,4]; stop`. A sum that is too small needs a larger left value; one that is too large needs a smaller right value.

| left | right | loop condition | sum | sum == target | sum < target | next action |
| --- | --- | --- | --- | --- | --- | --- |
| `1` | `3` | `1 < 3 → true` | `-2 + 3 = 1` | `true` | `Skipped (returned)` | `return [2,4]; stop` |

The equality branch returns before the smaller-than-target condition is evaluated. Return indexes `left+1=2`, `right+1=4`.

### Block 6 — Signal a violated input promise

```java
throw new IllegalArgumentException("Expected exactly one valid pair");
```

**What it evaluates:** this line runs only after every candidate range was eliminated without finding the guaranteed pair.

**Why it is needed:** it prevents a fabricated answer such as `[0, 0]` from hiding invalid input or a bug.

**Concrete result:** `[1, 2]` with target `10` reaches the exception.

**Equivalent clearer form:** returning an empty array is possible only if the method contract explicitly defines that result for “no pair.”

**Debugger state at this block:**

The sample returns `[2,4]` inside the loop; it never reaches the exception. With invalid input `[1,2]`, target `9`, sum `3 < 9` moves left to `1`; `1 < 1` is false and the method throws.

The pointer pairs are `(0,4) → (0,3) → (1,3)`. At `(1,3)`, `-2 + 3 = 1`; return `[left+1,right+1] = [2,4]`. The method returns before another loop-condition check. If no pair exists, crossing pointers reaches the exception instead.

## Reference approach

```java
// Input: numbers = [-5, -2, 0, 3, 9]; target = 1
// Expected return: [2, 4] (one-based indexes)

int left = 0;
int right = numbers.length - 1;

while (left < right) {
    int sum = numbers[left] + numbers[right];
    if (sum == target) {
        return new int[] {left + 1, right + 1};
    }
    if (sum < target) {
        left++;
    } else {
        right--;
    }
}
```

The `+ 1` happens only when forming the answer. Pointer movement continues to use zero-based Java indexes.

The reference class throws `IllegalArgumentException` after this block if the promised pair does not exist. Under the lesson assumptions that line is unreachable, but it makes a broken caller assumption explicit.

## Invariant and correctness

Before each iteration, any valid solution not already ruled out must lie within `[left, right]`. A too-small sum proves `numbers[left]` cannot participate in the solution; a too-large sum proves `numbers[right]` cannot participate. Each move removes only an impossible boundary, so the guaranteed solution is eventually found.

## Complexity and trade-off

- Time: `O(n)` because one pointer moves each iteration and neither reverses.
- Additional space: `O(1)`.
- Trade-off: this elimination proof depends on sorted input. For unsorted input, use a value-to-index map or sort while preserving original indexes.

## Common pitfalls

- moving the wrong pointer after comparing the sum;
- returning zero-based indexes;
- using `left <= right` and allowing one position to pair with itself;
- applying this method to unsorted data.

## Practice after learning the concept

- Write here: [TwoSumSortedPractice.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/twopointers/twosumii/TwoSumSortedPractice.java)
- Reveal after attempting: [TwoSumSortedSolution.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/twopointers/twosumii/TwoSumSortedSolution.java)

## Questions and explained answers

<details>
<summary>1. Why can a too-small sum discard the left value?</summary>

The current right value is the largest possible partner. If even that pair is too small, pairing the same left value with anything smaller also fails.

</details>

<details>
<summary>2. Why does the algorithm require sorted input?</summary>

The pointer move relies on knowing that moving right increases the left-side candidate and moving left decreases the right-side candidate.

</details>

<details>
<summary>3. Why is the result one-based?</summary>

That is part of this problem's output contract. The algorithm uses normal zero-based Java indexes internally and adds one only when returning.

</details>

## Stop/go

Proceed only when you can prove both pointer moves, distinguish this from unsorted Two Sum, implement it, and pass all checks.
