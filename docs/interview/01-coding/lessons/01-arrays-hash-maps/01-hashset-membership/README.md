# Micro-Lesson 01 — HashSet Membership

- Status: `LEARNING`
- Time: 35–45 minutes
- Primary idea: remember whether a value has appeared
- New terms: membership, duplicate, invariant

## Problem

Given a non-null integer array, return `true` if any value appears at least twice. Return `false` when every value is distinct.

Examples:

- `[1, 2, 3, 1]` → `true`
- `[1, 2, 3, 4]` → `false`
- `[]` → `false`

Target: average `O(n)` time.

## Runnable code

- Write here: [ContainsDuplicatePractice.java](../../../../../../src/test/java/com/instagram/backend/interview/coding/arrayshashmaps/hashsetmembership/ContainsDuplicatePractice.java)
- Reference answer: [ContainsDuplicateSolution.java](../../../../../../src/test/java/com/instagram/backend/interview/coding/arrayshashmaps/hashsetmembership/ContainsDuplicateSolution.java)

In IntelliJ, open the practice file and click the green triangle beside `main`. Change only `containsDuplicate`.

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

## Complexity

- Average time: `O(n)` because each element performs one average constant-time set operation.
- Additional space: `O(n)` in the all-distinct case.
- Trade-off: extra memory replaces repeated pair comparisons.

## Run from Terminal

```bash
./gradlew testClasses
java -cp build/classes/java/test \
  com.instagram.backend.interview.coding.arrayshashmaps.hashsetmembership.ContainsDuplicatePractice
```

After your attempt, run the reference:

```bash
java -cp build/classes/java/test \
  com.instagram.backend.interview.coding.arrayshashmaps.hashsetmembership.ContainsDuplicateSolution
```

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

If any item is unclear, repeat this one problem tomorrow. Do not open Micro-Lesson 02 yet.
