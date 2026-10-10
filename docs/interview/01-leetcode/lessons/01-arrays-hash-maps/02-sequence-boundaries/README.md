# Micro-Lesson 02 — Sequence Boundaries

## Question

> Given a non-null, unsorted integer array, return the length of its longest run of consecutive values.

Practice source: [Longest Consecutive Sequence on LeetCode](https://leetcode.com/problems/longest-consecutive-sequence/)

## What you will learn

- how to recognize the first value of a consecutive run by testing its predecessor;
- why scanning only from run boundaries prevents repeated work;
- how a set removes duplicates and provides average constant-time membership checks;
- how Java integer wraparound can incorrectly connect `MIN_VALUE` and `MAX_VALUE`;
- why the nested loops still perform only average `O(n)` total work.

## Lesson status

- Learning status: `PRACTICING`; initial implementation and checkpoint explanation passed on 2026-10-07
- Learner implementation: `PASSING` on 2026-10-07
- Time: 45–60 minutes
- Prerequisite: [HashSet Membership](../01-hashset-membership/README.md)
- Primary idea: begin scanning a consecutive run only from its first value
- New terms: predecessor, sequence boundary, integer overflow

The HashSet practice implementation and initial explanation checkpoints pass. Spaced review is still required before this problem counts as mastered. The only prerequisite needed here is that a `HashSet` stores unique values and provides average `O(1)` membership checks.

## Inputs, examples, and target

Assumptions:

- Input positions do not matter.
- Duplicate values do not extend a run.
- Consecutive means each next distinct value is exactly one larger.
- The array can contain any Java `int`, including `Integer.MIN_VALUE` and `Integer.MAX_VALUE`.

Examples:

- `[100, 4, 200, 1, 3, 2]` → `4` because the run is `1, 2, 3, 4`
- `[1, 2, 0, 1]` → `3` because duplicates do not extend `0, 1, 2`
- `[]` → `0`
- `[Integer.MIN_VALUE, Integer.MAX_VALUE]` → `1`; the two extremes are not consecutive

Target: average `O(n)` time and `O(n)` additional space.

## Why a simple HashSet loop is not enough

Putting values in a set gives fast membership checks, but starting a forward scan from every value repeats work.

For the run `1, 2, 3, 4`:

- starting at `1` checks `2`, `3`, and `4`;
- starting at `2` checks `3` and `4` again;
- starting at `3` checks `4` again;
- starting at `4` checks nothing.

For a run containing `n` values, that repeated work can approach `n + (n - 1) + ... + 1`, or `O(n²)`.

The fix is to scan only from a sequence boundary: a value whose predecessor is absent.

```text
Run:          1  2  3  4
Predecessor:  0  1  2  3
Start?       yes no no no
```

`1` is the only start because `0` is absent. Values `2`, `3`, and `4` are not starts because their predecessors are present.

## Block-by-block code walkthrough

The nine blocks below explain what each block evaluates, why it is required, a concrete result, what breaks without it, and a clearer equivalent where one exists.

### Block 1 — Build the set

```java
Set<Integer> unique = new HashSet<>();
for (int value : values) {
    unique.add(value);
}
```

**What it evaluates:** it visits every input value and stores each distinct value once.

**Why it is needed:** the algorithm needs average `O(1)` predecessor and successor membership checks while ignoring duplicates.

For input `[1, 2, 0, 1]`, the set contains `{0, 1, 2}`. The duplicate `1` is stored once, so it cannot make the run longer.

Why use a set?

- `unique.contains(number)` is average `O(1)`.
- duplicates disappear automatically;
- input order no longer matters.

`HashSet` iteration order is unspecified, but the algorithm does not depend on order. It identifies starts by checking values, not positions.

**What fails without it:** searching the array for every predecessor and successor can repeat linear work and become `O(n²)`.

**Equivalent clearer form:** `Set<Integer> unique = new HashSet<>(values.length);` can pre-size the set but does not change the algorithm.

### Block 2 — Initialize the best completed length

```java
int longest = 0;
```

**What it evaluates:** before any run is measured, the best known length is zero.

**Why it is needed:** empty input contains no run, and later calls to `Math.max` need an initialized previous best.

**Concrete result:** for `[]`, both loops perform zero iterations and the final answer remains `0`.

**What fails without it:** Java does not allow an uninitialized local variable to be read by `Math.max` or returned.

**Equivalent clearer form:** no different initialization is clearer; starting at `1` would be wrong for empty input.

### Supporting concept — Understand Java's integer boundaries

A Java `int` can represent only this closed range:

```text
Integer.MIN_VALUE = -2,147,483,648
Integer.MAX_VALUE =  2,147,483,647
```

There is no smaller `int` before `Integer.MIN_VALUE` and no larger `int` after `Integer.MAX_VALUE`.

Java `int` arithmetic wraps when it crosses the boundary:

```java
int tooSmall = Integer.MIN_VALUE - 1;
// tooSmall becomes Integer.MAX_VALUE

int tooLarge = Integer.MAX_VALUE + 1;
// tooLarge becomes Integer.MIN_VALUE
```

This wraparound is integer overflow. Mathematically, the minimum and maximum values are extremely far apart; Java must not accidentally treat them as neighbors.

### Block 3 — Consider every distinct value as a possible start

```java
for (int value : unique) {
```

**What it evaluates:** each distinct value becomes a start candidate exactly once; set iteration order does not matter.

**Why it is needed:** every consecutive run has one start, and the method must discover all runs before choosing the longest.

**Concrete result:** for input `[1, 1, 2]`, the candidates are `1` and `2`, not three array positions.

**What fails without it:** iterating only one chosen value can miss a longer run elsewhere. Iterating the original array remains correct but repeats start checks for duplicates.

**Equivalent clearer form:** `for (Integer boxedValue : unique) { int value = boxedValue; }` is equivalent but makes Java's unboxing visible unnecessarily.

### Block 4 — Decide whether a value has a predecessor

The reference solution uses:

```java
boolean hasPredecessor = value != Integer.MIN_VALUE
        && unique.contains(value - 1);
```

Read the line from left to right:

1. `value != Integer.MIN_VALUE` asks whether subtracting `1` is safe.
2. `&&` means both conditions must be true.
3. Java stops after the first condition when it is `false`; it does not evaluate `value - 1`.
4. Only for a safe value does the set check whether `value - 1` exists.

### Why `!= Integer.MIN_VALUE`?

We are calculating `hasPredecessor`, not `isStart`.

- If `value` is **not equal** to the minimum, `value - 1` is representable and can be checked.
- If `value` **is equal** to the minimum, no smaller `int` exists, so it cannot have a predecessor inside an `int[]`. `hasPredecessor` must be `false`.

Example with `value = 5`:

```text
5 != MIN_VALUE             → true
unique contains 5 - 1 = 4  → depends on the set
```

Example with `value = Integer.MIN_VALUE`:

```text
MIN_VALUE != MIN_VALUE     → false
Java stops here            → value - 1 is never evaluated
hasPredecessor             → false
```

You could write the idea using equality if the variable represented the opposite fact:

```java
boolean isStart = value == Integer.MIN_VALUE
        || !unique.contains(value - 1);
```

Both versions are correct:

- `hasPredecessor` uses `value != MIN_VALUE && ...`.
- `isStart` uses `value == MIN_VALUE || ...`.

The comparison changes because the boolean question changes.

### Why the order of conditions matters

This is safe:

```java
value != Integer.MIN_VALUE && unique.contains(value - 1)
```

This does not protect the subtraction:

```java
unique.contains(value - 1) && value != Integer.MIN_VALUE
```

In the second version, Java evaluates `value - 1` before it reaches the guard.

Without the predecessor decision, the method would scan from every value and repeat the same run, approaching `O(n²)`. The `isStart` expression above is the equivalent clearer form when the surrounding branch is phrased positively.

### Block 5 — Scan only from starts

```java
if (!hasPredecessor) {
```

`!` means “not.” Therefore the block runs only when the value does not have a predecessor.

For `{100, 4, 200, 1, 3, 2}`:

| Value | Predecessor | Present? | Start scanning? |
|---:|---:|---|---|
| 100 | 99 | No | Yes |
| 4 | 3 | Yes | No |
| 200 | 199 | No | Yes |
| 1 | 0 | No | Yes |
| 3 | 2 | Yes | No |
| 2 | 1 | Yes | No |

Only `1` starts the four-value run. It does not matter which order the set happens to iterate.

Without this guard, `1`, `2`, `3`, and `4` would all rescan suffixes of the same run. An equivalent positive form is `if (isStart)`, using the boolean expression shown in Block 4.

### Block 6 — Count the current value first

```java
int length = 1;
int current = value;
```

Why does `length` start at `1` rather than `0`?

The starting value already belongs to the run. If the set contains only `{7}`, the run is `7`, whose length is `1`.

`current` is a cursor. It moves through the current run while `value` remains unchanged as the start chosen by the outer loop.

Without `length = 1`, a one-value run would be reported as zero; without a separate cursor, advancing would destroy the stable start value used by the outer iteration. There is no clearer equivalent; the two initializations directly state that the start is already counted and that scanning begins there.

### Block 7 — Look for the next value safely

```java
while (current != Integer.MAX_VALUE
        && unique.contains(current + 1)) {
    current++;
    length++;
}
```

Read the condition from left to right:

1. `current != Integer.MAX_VALUE` asks whether adding `1` is safe.
2. If `current` is the maximum, Java stops and does not evaluate `current + 1`.
3. Otherwise, the set checks for the next integer.
4. When it exists, both `current` and `length` advance by one.

### Why `!= Integer.MAX_VALUE`?

- If `current` is not the maximum, `current + 1` is representable.
- If `current` equals the maximum, no larger `int` exists, so the run must stop.

Without this guard, `Integer.MAX_VALUE + 1` wraps to `Integer.MIN_VALUE`.

For input `{Integer.MIN_VALUE, Integer.MAX_VALUE}`, removing both guards could incorrectly connect the two extremes:

```text
MAX_VALUE + 1 wraps to MIN_VALUE
The set contains MIN_VALUE
Incorrect conclusion: the two values are consecutive
```

The correct answer is `1`, not `2`.

Without `current++`, the same successor remains present forever; without `length++`, the traversal occurs but the reported size never grows. A clearer expanded equivalent stores `int next = current + 1`, checks `unique.contains(next)`, then assigns `current = next`.

### Block 8 — Keep the best run

```java
longest = Math.max(longest, length);
```

`longest` stores the best completed run seen so far.

Example:

```text
longest before = 1
current run     = 4
Math.max(1, 4)  = 4
longest after   = 4
```

If a later run has length `2`, `Math.max(4, 2)` keeps `4`.

Without this update, the method forgets completed runs and always returns its initial value. The equivalent explicit form is `if (length > longest) { longest = length; }`.

### Block 9 — Return the best run after every candidate is considered

```java
return longest;
```

**What it evaluates:** after every distinct start candidate has been checked, the maintained maximum becomes the answer.

**Why it is needed:** a later run may be longer than an earlier run, so returning from the first start is unsafe.

**Concrete result:** for `[100, 1, 2, 3]`, a one-value run may be encountered before the three-value run; the final result is still `3`.

**What fails without it:** the method would not compile because its successful completion path has no result.

**Equivalent clearer form:** none; `longest` is the maintained answer.

## Fully annotated reference solution

```java
static int longestConsecutive(int[] values) {
    // Remove duplicates and enable average O(1) membership checks.
    Set<Integer> unique = new HashSet<>();
    for (int value : values) {
        unique.add(value);
    }

    // Empty input correctly returns 0 because no run changes this value.
    int longest = 0;

    for (int value : unique) {
        // MIN_VALUE has no representable predecessor.
        // Java evaluates the contains call only when subtraction is safe.
        boolean hasPredecessor = value != Integer.MIN_VALUE
                && unique.contains(value - 1);

        // Scan forward only from the first value of a run.
        if (!hasPredecessor) {
            int length = 1;       // Count the start itself.
            int current = value;  // Cursor through this run.

            // MAX_VALUE has no representable successor.
            // Java evaluates current + 1 only when addition is safe.
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

## Complete dry run

Input:

```text
[100, 4, 200, 1, 3, 2]
```

Set contents:

```text
{1, 2, 3, 4, 100, 200}
```

The displayed order is only for explanation; actual `HashSet` order can differ.

| Start candidate | Has predecessor? | Values counted | Length | Longest afterward |
|---:|---|---|---:|---:|
| 1 | No (`0` absent) | `1, 2, 3, 4` | 4 | 4 |
| 2 | Yes (`1` present) | skipped | — | 4 |
| 3 | Yes (`2` present) | skipped | — | 4 |
| 4 | Yes (`3` present) | skipped | — | 4 |
| 100 | No (`99` absent) | `100` | 1 | 4 |
| 200 | No (`199` absent) | `200` | 1 | 4 |

The final result is `4`.

## Boundary dry runs

### Lower boundary

Input:

```text
[Integer.MIN_VALUE, Integer.MIN_VALUE + 1]
```

- `MIN_VALUE` has no representable predecessor, so it is a start.
- Its safe successor is `MIN_VALUE + 1`, which is present.
- The run length is `2`.

### Upper boundary

Input:

```text
[Integer.MAX_VALUE - 1, Integer.MAX_VALUE]
```

- `MAX_VALUE - 1` is a start when its predecessor is absent.
- It advances to `MAX_VALUE`.
- The maximum-value guard stops before overflow.
- The run length is `2`.

### Both extremes

Input:

```text
[Integer.MIN_VALUE, Integer.MAX_VALUE]
```

- Both values are separate one-value runs.
- Neither guard allows wraparound to join them.
- The answer is `1`.

## Why the nested loops are still average O(n)

The outer loop considers every distinct value once, but the inner loop runs only at starts.

For `1, 2, 3, 4`, only `1` enters the inner loop. Values `2`, `3`, and `4` are skipped as starts. Across all runs, each distinct value is advanced through at most once.

- Building the set: average `O(n)`.
- Checking possible starts: average `O(n)`.
- All successful forward steps combined: average `O(n)`.
- Total: average `O(n)`, not `O(n²)`.
- Additional space: `O(n)` for the distinct values.

## Correctness reasoning

Every non-empty consecutive run has exactly one first value:

- its predecessor is absent;
- every later value in that run has a predecessor and is skipped as a start;
- scanning from the first value counts every successor until the run ends.

Therefore each run is measured once. Taking the maximum of all measured run lengths returns the longest one.

## Common mistakes

- Starting a scan from every value, causing repeated work.
- Iterating the original array for start candidates, causing duplicates to repeat checks.
- Starting `length` at `0` and forgetting to count the first value.
- Removing the minimum/maximum guards and connecting integer extremes through overflow.
- Putting the overflow guard after the arithmetic expression, which is too late.
- Sorting even though the target asks for average `O(n)` time; sorting is a valid simpler alternative when `O(n log n)` is acceptable.


## Learner solution review — 2026-10-07

Submitted implementation: [LongestConsecutivePractice.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/arrayshashmaps/sequenceboundaries/LongestConsecutivePractice.java)

The submitted approach:

1. copies every input value into a `HashSet`, removing duplicates;
2. treats a value as a sequence start only when its predecessor is absent;
3. scans forward from each start while successors remain in the set;
4. tracks the longest completed run;
5. guards both ends of Java's `int` range before subtracting or adding one.

Verification result: `./gradlew testClasses` succeeded, and both the learner program and reference program passed all supplied checks. The covered cases are an unordered run, duplicates, empty input, one value, negative values, a run ending at `Integer.MAX_VALUE`, a run starting at `Integer.MIN_VALUE`, and separated minimum/maximum values that must not be joined through overflow.

### Correctness and complexity

- The solution is correct for the problem's non-null input assumption.
- A run is scanned only from its unique first value, so the same run is not repeatedly traversed.
- `length = 1` correctly counts the sequence start before successor scanning begins.
- The minimum and maximum guards prevent integer wraparound from making the two extremes appear adjacent.
- Average time is `O(n)`: set construction and all membership checks/forward steps combined are linear on average.
- Additional space is `O(n)` for the distinct input values.

### Issues and refinements

There is no correctness bug. These are optional Java-style refinements and are not reasons to reject the answer in an interview:

1. `public static` is the conventional modifier order instead of `static public`.
2. `new HashSet<>()` can use the diamond operator instead of repeating `Integer`.
3. `for (int num : unique)` avoids explicitly writing the wrapper type when the loop body uses primitive arithmetic.
4. Standard spacing around operators and after `for`/`while` improves readability.

The implementation attempt is complete. The lesson remains `LEARNING` until the learner explains the invariant, boundary guards, and average `O(n)` analysis without relying on the revealed answers.

### Checkpoint attempt — 2026-10-07

Result: `RETRY_NEEDED`; this does not change the passing code result.

- Start identification was partial: `1` starts the four-value run, but `100` and `200` also start their own one-value runs because `99` and `199` are absent. The explanation that `length` starts at `1` because the starting value already belongs to the run was correct.
- The guard purpose was partially identified, but the exact overflow results and short-circuit behavior were not yet explained. `Integer.MIN_VALUE - 1` wraps to `Integer.MAX_VALUE`, while `Integer.MAX_VALUE + 1` wraps to `Integer.MIN_VALUE`. For `a && b`, Java evaluates `a` first and evaluates `b` only when `a` is `true`.
- The complexity intuition was on the right path, but the accounting was incomplete. Only run starts enter the forward scan, so every distinct value is traversed by an inner loop at most once across the entire algorithm. The outer work is average `O(n)` and all inner-loop work combined is average `O(n)`.

Before advancing, retry the three checkpoints in plain language without reading the explained answers.

### Checkpoint retry 2 — 2026-10-07

Result: `RETRY_NEEDED`; one checkpoint now passes and two need one missing detail each.

- Start identification still needs correction: `100`, `200`, and `1` start scans. `4` does not start because its predecessor `3` is present. The rule is to scan when the predecessor is absent, not merely whenever subtraction is possible.
- The short-circuit explanation now passes: Java evaluates the right side of `&&` only when the left side is `true`. The answer still needs to state the two exact wraparound results to demonstrate both boundary guards.
- The complexity explanation passes: only sequence starts enter the inner loop, and each distinct value is traversed by an inner loop at most once across all runs.

Before advancing, retry only start identification and the two exact integer wraparound results.

### Checkpoint retry 3 — 2026-10-07

Result: `PASSING_WITH_COACHING`; advance is allowed, but spaced review remains required for mastery.

- Start identification passes: `100`, `200`, and `1` start scans; every other value has a predecessor in the set and is skipped.
- Boundary reasoning passes: `Integer.MIN_VALUE - 1` wraps to the positive `Integer.MAX_VALUE`, and `Integer.MAX_VALUE + 1` wraps to the negative `Integer.MIN_VALUE`.
- The earlier answers already established correct `&&` short-circuit behavior and the combined average `O(n)` complexity argument.

Scheduled retrieval checks: 2026-10-08, 2026-10-14, and 2026-11-07. The learner must solve or explain the idea again without these answers before the status can advance beyond `PRACTICING`.

## Practice after learning the concept

- Write here: [LongestConsecutivePractice.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/arrayshashmaps/sequenceboundaries/LongestConsecutivePractice.java)
- Reveal after attempting: [LongestConsecutiveSolution.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/arrayshashmaps/sequenceboundaries/LongestConsecutiveSolution.java)

Open the practice file in IntelliJ, change only `longestConsecutive`, and run its `main` method.

## Checkpoints

<details>
<summary>1. Why do we start counting only when the predecessor is absent?</summary>

Every run has exactly one value with no predecessor: its first value. Starting only there prevents the same run from being traversed repeatedly and keeps total scanning linear on average.

</details>

<details>
<summary>2. Why is the condition `value != Integer.MIN_VALUE && unique.contains(value - 1)`?</summary>

The code is calculating whether a predecessor exists. Subtraction is safe only when the value is not the minimum. When it is the minimum, no representable predecessor exists, so the answer is `false`. Java stops evaluating `&&` after the false guard, preventing `MIN_VALUE - 1` from wrapping to `MAX_VALUE`.

</details>

<details>
<summary>3. How can an inner while loop still produce average O(n) total time?</summary>

The inner loop runs only from sequence starts. Each distinct value is advanced through as part of exactly one run, so all inner-loop progress combined is proportional to the number of distinct values.

</details>

## Stop/go

Proceed only when you can:

- identify every start value in `{100, 4, 200, 1, 3, 2}`;
- explain why `length` starts at `1`;
- explain both integer boundary guards and what wraps without them;
- explain why Java must evaluate the guard before the arithmetic expression;
- explain why the nested loops total average `O(n)`;
- implement the method without viewing the reference and pass all checks.
