# Micro-Lesson 03 — HashMap Value-to-Index Lookup

- Learning status: `LEARNING`
- Learner implementation: `PASSING` with the average `O(n)` HashMap approach on 2026-10-09; checkpoints pending
- Time: 35–45 minutes
- Prerequisite: basic array indexing and `HashMap` lookup
- Primary idea: remember the index of each earlier value so its matching partner can find it
- New terms: complement, value-to-index map, loop invariant
- LeetCode: [Two Sum](https://leetcode.com/problems/two-sum/)

## Problem

Given a non-null integer array `numbers` and an integer `target`, return the indexes of the two distinct elements whose values add to `target`.

Assumptions:

- `numbers.length` is at least `2`.
- Exactly one valid pair exists.
- The same array position cannot be used twice.
- Return the earlier index first.
- Every number and the target are between `-1,000,000,000` and `1,000,000,000`. Therefore `target - numbers[index]` stays between `-2,000,000,000` and `2,000,000,000`, inside Java's `int` range.

Examples:

- `numbers = [2, 7, 11, 15]`, `target = 9` → `[0, 1]`
- `numbers = [3, 2, 4]`, `target = 6` → `[1, 2]`
- `numbers = [3, 3]`, `target = 6` → `[0, 1]`; two equal values at different indexes may form the pair
- `numbers = [-3, 4, 3, 90]`, `target = 0` → `[0, 2]`

Target: average `O(n)` time and `O(n)` additional space.

## Runnable code

- Write here: [TwoSumIndexPractice.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/arrayshashmaps/valuetoindex/TwoSumIndexPractice.java)
- Reveal after attempting: [TwoSumIndexSolution.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/arrayshashmaps/valuetoindex/TwoSumIndexSolution.java)

Open the practice file in IntelliJ, change only `twoSum`, and run its `main` method. Do not open the reference solution until your attempt passes or you have spent 30 focused minutes.

## Learner solution review — 2026-10-07

Submitted implementation: [TwoSumIndexPractice.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/arrayshashmaps/valuetoindex/TwoSumIndexPractice.java)

The submitted approach compares each index `i` with every later index `j`. It returns `[i, j]` as soon as `numbers[i] + numbers[j]` equals the target.

Verification result: `./gradlew testClasses` succeeded, and both the learner program and reference program passed all supplied checks. The learner code correctly handles a pair at the beginning, a pair after an unused value, equal values at distinct indexes, a negative complement, and two zeros at distinct indexes.

### What is correct

- `j` begins at `i + 1`, so the same array position is never used twice.
- Every unordered pair is eventually checked until the valid pair is found.
- Returning immediately after finding the guaranteed unique pair is correct.
- Under the stated constraints, adding two input values stays within Java's `int` range.
- Time is `O(n²)` and additional space is `O(1)`.

### Why the lesson is not complete

The result is functionally correct, but the problem explicitly targets average `O(n)` time and the lesson's primary idea is a value-to-index `HashMap`. For an input of length `n`, the nested loops can inspect approximately `n × (n - 1) / 2` pairs. That grows quadratically.

The next attempt must replace the inner scan with this one-pass plan:

1. Create a map from each earlier value to its index.
2. At the current index, calculate `complement = target - numbers[index]`.
3. Look up the complement in the map.
4. If found, return the earlier index and current index.
5. Otherwise, store the current value and index, then continue.

Lookup must happen before insertion so `[3, 3]` returns `[0, 1]` rather than allowing index `0` to match itself.

### Other refinements

- `public static` is the conventional modifier order instead of `static public`.
- Standard spacing around operators and after `for`/`if` improves readability.
- Because the problem guarantees a solution, returning the initially allocated `[0, 0]` is unreachable for valid input. Throwing an exception after the loop communicates a violated assumption more clearly.
- The result array can be created only when the pair is found: `return new int[] {i, j};`.

Preserve this nested-loop solution as the correct baseline in your explanation, but update the practice method yourself to meet the average `O(n)` target before attempting the checkpoints.

### Optimized learner solution review — 2026-10-09

The learner replaced the nested loops with the intended one-pass value-to-index map in [TwoSumIndexPractice.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/arrayshashmaps/valuetoindex/TwoSumIndexPractice.java).

- Each index calculates its complement and performs one average `O(1)` map lookup.
- Lookup occurs before insertion, so the current index cannot match itself.
- `[3, 3]` correctly finds index `0` while processing index `1`.
- The map contains earlier values only, which establishes the required loop invariant.
- Average time is `O(n)` and additional space is `O(n)`.
- All supplied ordinary, duplicate, negative, and zero-pair checks pass.

The implementation requirement is complete. The lesson remains `LEARNING` until the learner answers the three checkpoints without relying on the explained answers.

## Why the problem exists

At each index, the question is:

> Have I already seen the value needed to complete the target?

For current value `7` and target `9`, the needed value is `2` because `9 - 7 = 2`. That needed value is the **complement**.

A nested-loop solution searches every later position for a partner. In the worst case it checks roughly every pair, taking `O(n²)` time. A `HashMap` replaces that repeated search with an average `O(1)` lookup.

## The value-to-index map

```java
Map<Integer, Integer> earlierIndexByValue = new HashMap<>();
```

The map stores:

```text
key   = an earlier array value
value = the index where that value appeared
```

After processing index `0` of `[2, 7, 11, 15]`, the map contains:

```text
2 -> 0
```

When index `1` contains `7`, the algorithm asks for complement `2`, finds index `0`, and returns `[0, 1]`.

## Reference approach

```java
static int[] twoSum(int[] numbers, int target) {
    Map<Integer, Integer> earlierIndexByValue = new HashMap<>();

    for (int index = 0; index < numbers.length; index++) {
        int complement = target - numbers[index];
        Integer earlierIndex = earlierIndexByValue.get(complement);

        if (earlierIndex != null) {
            return new int[] {earlierIndex, index};
        }

        earlierIndexByValue.put(numbers[index], index);
    }

    throw new IllegalArgumentException("Expected exactly one valid pair");
}
```

### Why use an index loop?

```java
for (int index = 0; index < numbers.length; index++) {
```

The answer must return positions, so the loop needs the current `index`. An enhanced loop would provide only each value and would lose its position.

For a four-element array, the condition produces:

```text
index = 0: 0 < 4 -> true
index = 1: 1 < 4 -> true
index = 2: 2 < 4 -> true
index = 3: 3 < 4 -> true
index = 4: 4 < 4 -> false, stop
```

Using `index <= numbers.length` would eventually try `numbers[numbers.length]`, which is one position beyond the last valid index and would throw `ArrayIndexOutOfBoundsException`.

## Calculate the complement

```java
int complement = target - numbers[index];
```

This converts the addition question into a lookup:

```text
earlier value + current value = target
earlier value                 = target - current value
```

For target `6` and current value `4`:

```text
complement = 6 - 4 = 2
```

The stated numeric constraints make this subtraction safe in a Java `int`. Without such constraints, wider `long` arithmetic or an explicit range decision would be needed.

## Read a previously stored index

```java
Integer earlierIndex = earlierIndexByValue.get(complement);
```

`HashMap.get` returns the stored index when the key exists. It returns `null` when the key is absent. The variable uses `Integer`, not primitive `int`, because `int` cannot represent `null`.

Concrete examples:

```text
map is {2 -> 0}, complement is 2 -> get returns 0
map is {3 -> 0}, complement is 2 -> get returns null
```

This map never stores `null` values, so `null` unambiguously means the complement has not appeared.

## Return when the complement exists

```java
if (earlierIndex != null) {
    return new int[] {earlierIndex, index};
}
```

`earlierIndex != null` asks whether an earlier matching value was found. If it was, the two values add to the target by the definition of `complement`.

For `[2, 7, 11, 15]` at index `1`:

```text
earlierIndex = 0
current index = 1
return [0, 1]
```

Returning immediately is safe because the problem guarantees exactly one solution.

## Why lookup happens before insertion

```java
earlierIndexByValue.put(numbers[index], index);
```

The current value is stored only after checking for its complement. Therefore every index in the map is strictly earlier than the current index. This prevents using the same array position twice.

For `[3, 3]` with target `6`:

1. At index `0`, complement `3` is absent, so store `3 -> 0`.
2. At index `1`, complement `3` maps to index `0`.
3. Return `[0, 1]`; two distinct positions are used.

If insertion happened first, index `0` could find itself when current value `3` has complement `3`, producing the invalid answer `[0, 0]`.

## Loop invariant

Before processing `numbers[index]`, the map contains values from earlier indexes only. For each stored value, the map provides an index where that value occurred.

That statement explains both important properties:

- a found complement produces two distinct indexes;
- if a valid earlier partner exists, the map can find it in average `O(1)` time.

## Correctness reasoning

Suppose the valid pair is at indexes `i` and `j`, where `i < j`.

1. After index `i` is processed, its value is stored with index `i`.
2. Before index `j` is inserted, the algorithm calculates `target - numbers[j]`.
3. Because `numbers[i] + numbers[j] = target`, that complement equals `numbers[i]`.
4. The lookup finds index `i`, so the method returns `[i, j]`.

The lookup occurs before insertion, so `i` and `j` cannot be the same index.

## Complexity

- Average time: `O(n)` because each index performs one average `O(1)` lookup and at most one average `O(1)` insertion.
- Additional space: `O(n)` because the map may store nearly every input value before finding the pair.

## Pitfalls and reasonable alternatives

- Storing before looking can reuse the current index when a value is its own complement.
- Storing only values in a `HashSet` is insufficient because the answer requires indexes.
- Returning the values instead of their indexes answers a different question.
- A nested-loop solution is simpler but takes `O(n²)` time.
- Sorting can support a two-pointer search in `O(n log n)` time, but original indexes must be carried through the sort; it is unnecessary for this target.

## Questions and explained answers

<details>
<summary>1. What must be true about the map before processing index <code>i</code>?</summary>

It contains only values from indexes smaller than `i`, together with an earlier index for each stored value. This guarantees that a found complement belongs to a different array position.

</details>

<details>
<summary>2. Why must lookup happen before storing the current value?</summary>

Looking first prevents the current index from matching itself. It still handles `[3, 3]` correctly: the first `3` is stored before the second `3` is processed, so the answer uses indexes `0` and `1`.

</details>

<details>
<summary>3. Why does the algorithm take average O(n) time rather than O(n²)?</summary>

The array is traversed once. Each index performs a constant number of `HashMap` operations, and those operations are average `O(1)`, giving average `O(n)` total time.

</details>

## Stop/go

Proceed only when you can:

- implement `twoSum` without opening the reference file;
- state the map invariant in one sentence;
- explain why lookup must happen before insertion using `[3, 3]`;
- explain average `O(n)` time and `O(n)` additional space;
- pass every check in the practice program.
