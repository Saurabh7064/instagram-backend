# Micro-Lesson 01 — HashSet Membership

## Question

> Given a non-null integer array, return `true` if any value appears at least twice. Return `false` when every value is distinct.

Practice source: [Contains Duplicate on LeetCode](https://leetcode.com/problems/contains-duplicate/)

## What you will learn

- why remembering earlier values removes the need to compare every pair;
- how a `HashSet` models the question “have I seen this value?”;
- the loop invariant that makes an early `true` return correct;
- why the solution is average `O(n)` time with `O(n)` additional space;
- when `Set.add` can combine lookup and insertion.

## Lesson status

- Learning status: `DEFERRED` at learner request on 2026-10-04; checkpoint explanation still pending
- Learner implementation: `PASSING` on 2026-10-06
- Time: 35–45 minutes
- Primary idea: remember whether a value has appeared
- New terms: membership, duplicate, invariant

Need a syntax and operations review first? Use the supplemental [Java HashSet Cheat Sheet](../../../references/hashset-cheatsheet.md). Returning to the reference does not change this lesson's `DEFERRED` status.

## Inputs, examples, and target

Examples:

- `[1, 2, 3, 1]` → `true`
- `[1, 2, 3, 4]` → `false`
- `[]` → `false`

Target: average `O(n)` time.

## One idea

A nested-loop solution compares every pair and takes `O(n²)` time. The repeated question is:

> Have I already seen this value?

A `HashSet` stores membership. Before processing index `i`, the set contains every distinct value from indexes `0` through `i - 1`. That statement is the loop invariant.

For each value:

1. If it is already in the set, a duplicate exists.
2. Otherwise, add it and continue.
3. If the loop ends, every value was distinct.

## Reference solution

```java
static boolean containsDuplicate(int[] values) {
    Set<Integer> seen = new HashSet<>();

    for (int value : values) {
        if (!seen.add(value)) {
            return true;
        }
    }

    return false;
}
```

`Set.add` returns `false` when the value was already present, so one operation performs the membership check and insertion.

## Block-by-block code walkthrough

### Block 1 — Create the memory of earlier values

```java
Set<Integer> seen = new HashSet<>();
```

**What it evaluates:** `new HashSet<>()` creates an empty set, and `seen` exposes only the `Set` operations needed by this method.

**Why it is needed:** the algorithm must remember values from earlier positions. For `[1, 2, 1]`, the set changes from `{}` to `{1}` to `{1, 2}` before the last `1` is processed.

**What fails without it:** nothing records earlier values, so the method would need to rescan the array with a nested loop.

**Equivalent clearer form:** `HashSet<Integer> seen = new HashSet<>();` behaves the same, but declaring the `Set` interface communicates that only membership behavior matters.

### Block 2 — Process every array value once

```java
for (int value : values) {
```

**What it evaluates:** the enhanced loop assigns each array element to `value` from left to right. For `[4, 7]`, the iterations use `value = 4`, then `value = 7`.

**Why it is needed:** every value must either be remembered or identified as a duplicate. The index is unnecessary because the answer is only a boolean.

**What fails without it:** examining only part of the array misses a duplicate near the end, such as the final `1` in `[1, 2, 3, 1]`.

**Equivalent clearer form:** `for (int index = 0; index < values.length; index++) { int value = values[index]; }` is equivalent but adds an unused index.

### Block 3 — Let insertion detect the duplicate

```java
if (!seen.add(value)) {
    return true;
}
```

**What it evaluates:** `seen.add(value)` returns `true` when the value was absent and is inserted. It returns `false` when the value already exists. The `!` reverses that result, so the `if` body runs only for a duplicate.

**Concrete result:** while processing the last value of `[1, 2, 1]`, the set already contains `1`; `seen.add(1)` returns `false`, `!false` becomes `true`, and the method returns `true`.

**Why it is needed:** a failed insertion proves the current value matches an earlier value, so no later work can change the answer.

**What fails without it:** blindly adding every value loses the moment when repetition is discovered and would make every input reach `false`.

**Equivalent clearer form:**

```java
if (seen.contains(value)) {
    return true;
}
seen.add(value);
```

This is easier to read initially but performs a lookup followed by a separate insertion for new values.

### Block 4 — Reject duplication only after the scan finishes

```java
return false;
```

**What it evaluates:** reaching this line means every `add` succeeded, so every input value was new when encountered.

**Concrete results:** `[]`, `[7]`, and `[1, 2, 3]` all return `false`.

**Why it is needed:** the method must report the all-distinct case, including an empty array where the loop runs zero times.

**What fails without it:** the method would not compile because not every control path returns a boolean.

**Equivalent clearer form:** a separate `boolean duplicateFound` flag is possible, but the early return expresses the proof more directly.

## Complexity

- Average time: `O(n)` because each element performs one average constant-time set operation.
- Additional space: `O(n)` in the all-distinct case.
- Trade-off: extra memory replaces repeated pair comparisons.


## Learner solution review — 2026-10-06

Submitted implementation: [ContainsDuplicatePractice.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/arrayshashmaps/hashsetmembership/ContainsDuplicatePractice.java)

```java
HashSet<Integer> set = new HashSet<>();

for (int i = 0; i < values.length; i++) {
    if (set.contains(values[i])) {
        return true;
    }
    set.add(values[i]);
}
return false;
```

Verification result: all supplied checks pass for a duplicate, all-distinct values, an empty array, one value, and a duplicate negative value.

### What is correct

- The set contains values from earlier indexes before the current value is checked.
- Returning immediately after finding a prior value is correct and avoids unnecessary work.
- Empty and single-value arrays correctly reach `false`.
- Average time is `O(n)` and worst-case additional space is `O(n)`.

### Issues and refinements

There is no correctness bug for the problem's non-null input assumption. The improvements are about clarity and avoiding repeated work:

1. `contains` followed by `add` performs two hash-table operations for every new value. `if (!seen.add(value))` combines the membership check and insertion into one operation.
2. `Set<Integer> seen = new HashSet<>();` communicates both the interface being used and the purpose of the variable more clearly than `HashSet<Integer> set`.
3. An enhanced loop is enough because the index is never used for the answer.
4. The completed file still contains the starter `TODO` comment, and the loop spacing does not follow normal Java formatting. These do not affect correctness but should be cleaned in production-quality code.

These are refinements, not a reason to reject the solution in an interview. Explain the invariant and complexity confidently before optimizing the syntax.

## Practice after learning the concept

- Write here: [ContainsDuplicatePractice.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/arrayshashmaps/hashsetmembership/ContainsDuplicatePractice.java)
- Reference answer: [ContainsDuplicateSolution.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/arrayshashmaps/hashsetmembership/ContainsDuplicateSolution.java)

In IntelliJ, open the practice file and click the green triangle beside `main`. Change only `containsDuplicate`.

## Checkpoints

<details>
<summary>1. Why is HashSet the correct structure instead of HashMap?</summary>

The problem needs only membership—whether a value was previously seen. There is no associated count, index, or object to retrieve.

</details>

<details>
<summary>2. What is true about the set before processing each value?</summary>

It contains every distinct value processed earlier and no value from the unprocessed suffix. This invariant makes a failed `add` proof of a duplicate.

</details>

<details>
<summary>3. Why do we call the time complexity average O(n)?</summary>

There are `n` elements and each set insertion is average `O(1)`. Hash operations can degrade with collisions, so ordinary interview analysis states average rather than guaranteed linear time.

</details>

## Stop/go

Proceed only when you can:

- implement the method without viewing the reference;
- explain the invariant in one sentence;
- explain the `O(n)` time and `O(n)` space trade-off;
- answer all three checkpoints before revealing their answers.

If any item is unclear, repeat this one problem during the deferred review. This lesson remains incomplete and does not count toward mastery.
