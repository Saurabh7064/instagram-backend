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

Target: average `O(n)` time.

## First understand the idea

The key idea is to remember each value once, so the next value can be checked against what we have already seen.

Use `values=[1,2,3,1]`. Let `seen` mean “the distinct values from the part already read.” It starts empty: `{}`. Reading `1`, `2`, then `3` grows it to `{1,2,3}`. The final `1` is already there, so two input positions contain the same value and the answer is true.

This memory is a **set**: a collection that stores each distinct value once. Java's `HashSet` also tells us whether adding a value succeeded. At the final `1`, **❌ `seen.add(1)`:** false; **✅ `!seen.add(1)`:** true, so return true. The negation turns “not newly inserted” into “duplicate found.”

In general, check each value against the earlier values, remember new ones, and return as soon as one repeats. If every value is new, the loop finishes and returns false. The input positions are unnecessary because this problem asks whether a duplicate exists.

:::simulation
type: contains-duplicate-set
language: java
values: [1, 2, 3, 1]
:::

## Block-by-block code walkthrough

Original snapshots are retained. Where a value changes several times, its cell shows the sequence ending at the final value.

**Running example for the code and tables below:** Input `values = [1,2,3,1]`. Initially `seen = {}`. The enhanced `for` has no index variable; `value` is its loop variable.

Tables belong to the code block immediately above them. Inputs stay unchanged unless a block explicitly creates or changes a copy. Collection order is displayed for readability; HashMap/HashSet order is unspecified.

### Block 1 — Create the memory of earlier values

```java
Set<Integer> seen = new HashSet<>();
```

**What it evaluates:** `new HashSet<>()` creates an empty set, and `seen` exposes only the `Set` operations needed by this method.

**Why it is needed:** the algorithm must remember values from earlier positions. For `[1, 2, 1]`, the set changes from `{}` to `{1}` to `{1, 2}` before the last `1` is processed.

**What fails without it:** nothing records earlier values, so the method would need to rescan the array with a nested loop.

**Equivalent clearer form:** `HashSet<Integer> seen = new HashSet<>();` behaves the same, but declaring the `Set` interface communicates that only membership behavior matters.

**Debugger state at this block:**

`seen = {}`; `value` has not been assigned yet.

### Block 2 — Process every array value once

```java
for (int value : values) {
```

**What it evaluates:** the enhanced loop assigns each array element to `value` from left to right. For `[4, 7]`, the iterations use `value = 4`, then `value = 7`.

**Why it is needed:** every value must either be remembered or identified as a duplicate. The index is unnecessary because the answer is only a boolean.

**What fails without it:** examining only part of the array misses a duplicate near the end, such as the final `1` in `[1, 2, 3, 1]`.

**Equivalent clearer form:** `for (int index = 0; index < values.length; index++) { int value = values[index]; }` is equivalent but adds an unused index.

**Debugger state at this block:**

| value | seen before | seen.add(value) | seen after | branch |
| --- | --- | --- | --- | --- |
| `1` | `[]` | `✅ true` | `[1]` | `continue` |
| `2` | `[1]` | `✅ true` | `[1, 2]` | `continue` |
| `3` | `[1, 2]` | `✅ true` | `[1, 2, 3]` | `continue` |
| `1` | `[1, 2, 3]` | `❌ false` | `[1, 2, 3]` | `return true` |

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

**Debugger state at this block:**

| value | seen before | seen.add(value) | seen after | branch |
| --- | --- | --- | --- | --- |
| `1` | `[]` | `✅ true` | `[1]` | `continue` |
| `2` | `[1]` | `✅ true` | `[1, 2]` | `continue` |
| `3` | `[1, 2]` | `✅ true` | `[1, 2, 3]` | `continue` |
| `1` | `[1, 2, 3]` | `❌ false` | `[1, 2, 3]` | `return true` |


**Narrated execution — follow each operation, then check its values:**

**Iteration/check 1 — value = `1`.**

- **✅ `for (int value : values)`:** next value exists → true; assign `value=1` (input index `0`).
- **✅ `seen.add(value)`** with `1` changes `seen`: `[] → [1]`; insertion returns `true`.
- **❌ `if (!seen.add(value))`:** `!true → false`; skip `return true`.
- Continue to the next value; `seen` stays `[1]`.

| value | seen.add(value) | if (!seen.add(value)) | seen after | next action |
| --- | --- | --- | --- | --- |
| `1` | `✅ true` | `❌ !true → false` | `[1]` | `continue` |

**Iteration/check 2 — value = `2`.**

- **✅ `for (int value : values)`:** next value exists → true; assign `value=2` (input index `1`).
- **✅ `seen.add(value)`** with `2` changes `seen`: `[1] → [1, 2]`; insertion returns `true`.
- **❌ `if (!seen.add(value))`:** `!true → false`; skip `return true`.
- Continue to the next value; `seen` stays `[1, 2]`.

| value | seen.add(value) | if (!seen.add(value)) | seen after | next action |
| --- | --- | --- | --- | --- |
| `2` | `✅ true` | `❌ !true → false` | `[1, 2]` | `continue` |

**Iteration/check 3 — value = `3`.**

- **✅ `for (int value : values)`:** next value exists → true; assign `value=3` (input index `2`).
- **✅ `seen.add(value)`** with `3` changes `seen`: `[1, 2] → [1, 2, 3]`; insertion returns `true`.
- **❌ `if (!seen.add(value))`:** `!true → false`; skip `return true`.
- Continue to the next value; `seen` stays `[1, 2, 3]`.

| value | seen.add(value) | if (!seen.add(value)) | seen after | next action |
| --- | --- | --- | --- | --- |
| `3` | `✅ true` | `❌ !true → false` | `[1, 2, 3]` | `continue` |

**Iteration/check 4 — value = `1`.**

- **✅ `for (int value : values)`:** next value exists → true; assign `value=1` (input index `3`).
- **❌ `seen.add(value)`** with `1` keeps `seen`: `[1, 2, 3] → [1, 2, 3]`; insertion returns `false`.
- **✅ `if (!seen.add(value))`:** `!false → true`; execute `return true`; stop.
- `for` advancement and the final `return false` are `Skipped` after the return.

| value | seen.add(value) | if (!seen.add(value)) | seen after | next action |
| --- | --- | --- | --- | --- |
| `1` | `❌ false` | `✅ !false → true` | `[1, 2, 3]` | `return true; stop` |

The duplicate makes the fourth condition true, so the final return outside the loop is skipped. For all-distinct input, exhaustion instead reaches `return false`.

### Block 4 — Reject duplication only after the scan finishes

```java
return false;
```

**What it evaluates:** reaching this line means every `add` succeeded, so every input value was new when encountered.

**Concrete results:** `[]`, `[7]`, and `[1, 2, 3]` all return `false`.

**Why it is needed:** the method must report the all-distinct case, including an empty array where the loop runs zero times.

**What fails without it:** the method would not compile because not every control path returns a boolean.

**Equivalent clearer form:** a separate `boolean duplicateFound` flag is possible, but the early return expresses the proof more directly.

**Debugger state at this block:**

For `[1,2,3,1]`, the duplicate branch already returned `true`, so this terminal `false` is not reached. For `[1,2,3,4]`, the completed set is `{1,2,3,4}` and this line returns `false`.

The fourth addition returns `false`, so `!false` is `true` and the method returns `true` immediately. No later loop step runs. For `[1,2,3,4]`, all four additions return `true`; after the loop, the terminal return is `false`.

## Reference solution

```java
// Input: values = [1, 2, 3, 1]
// Expected return: true

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
