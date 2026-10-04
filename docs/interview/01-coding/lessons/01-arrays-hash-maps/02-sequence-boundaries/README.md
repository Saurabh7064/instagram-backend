# Micro-Lesson 02 — Sequence Boundaries

- Status: `TODO`
- Time: 45–60 minutes
- Prerequisite: [HashSet Membership](../01-hashset-membership/README.md)
- Primary idea: begin scanning a run only from its first value
- New terms: predecessor, boundary, consecutive

Do not start this lesson until Micro-Lesson 01 passes its stop/go check.

## Problem

Given a non-null, unsorted integer array, return the length of its longest run of consecutive values. Input positions do not matter, and duplicates do not extend a run.

Examples:

- `[100, 4, 200, 1, 3, 2]` → `4`
- `[1, 2, 0, 1]` → `3`
- `[]` → `0`

Target: average `O(n)` time.

## Runnable code

- Write here: [LongestConsecutivePractice.java](../../../../../../src/test/java/com/instagram/backend/interview/coding/arrayshashmaps/sequenceboundaries/LongestConsecutivePractice.java)
- Reference answer: [LongestConsecutiveSolution.java](../../../../../../src/test/java/com/instagram/backend/interview/coding/arrayshashmaps/sequenceboundaries/LongestConsecutiveSolution.java)

## One idea

Putting values in a set gives fast membership checks, but starting a scan from every value can still revisit the same run many times.

A value begins a run only when its predecessor is absent. For example, `1` begins `1, 2, 3, 4` because `0` is absent. Values `2`, `3`, and `4` are skipped as starting points because each has a predecessor.

This boundary check ensures each run is walked once.

## Reference solution

```java
static int longestConsecutive(int[] values) {
    Set<Integer> unique = new HashSet<>();
    for (int value : values) {
        unique.add(value);
    }

    int longest = 0;
    for (int value : unique) {
        boolean hasPredecessor = value != Integer.MIN_VALUE
                && unique.contains(value - 1);

        if (!hasPredecessor) {
            int length = 1;
            int current = value;

            while (current != Integer.MAX_VALUE
                    && unique.contains(current + 1)) {
                current++;
                length++;
            }

            longest = Math.max(longest, length);
        }
    }

    return longest;
}
```

## Correctness and complexity

Every non-empty run has exactly one first value whose predecessor is absent. The algorithm starts there and counts every member until the first missing successor. Therefore every run is considered and its full length is measured.

- Average time: `O(n)`. Building the set is linear, and each distinct value is counted as part of one run.
- Additional space: `O(n)` for distinct values.
- Boundary guards prevent integer overflow at `Integer.MIN_VALUE` and `Integer.MAX_VALUE`.

## Run from Terminal

```bash
./gradlew testClasses
java -cp build/classes/java/test \
  com.instagram.backend.interview.coding.arrayshashmaps.sequenceboundaries.LongestConsecutivePractice
```

Reference:

```bash
java -cp build/classes/java/test \
  com.instagram.backend.interview.coding.arrayshashmaps.sequenceboundaries.LongestConsecutiveSolution
```

## Checkpoints

<details>
<summary>1. Why not start counting from every value?</summary>

That can walk the same run repeatedly and degrade toward `O(n²)`. Starting only where the predecessor is absent makes each run begin once.

</details>

<details>
<summary>2. Why do duplicates not change the answer?</summary>

The set stores each distinct value once. A run’s length is based on distinct consecutive values, so repeated input values add nothing.

</details>

<details>
<summary>3. How can a loop inside another loop still total O(n)?</summary>

The inner loop runs only at sequence starts, and a distinct value is traversed as part of only one run. Across the complete algorithm, the successful inner-loop steps are proportional to the number of distinct values.

</details>

## Stop/go

Proceed only when you can identify the sequence-start condition, implement it without the reference, explain why values are not repeatedly traversed, and pass the supplied checks.
