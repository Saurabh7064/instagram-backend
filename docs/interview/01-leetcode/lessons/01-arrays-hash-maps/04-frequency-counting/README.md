# Micro-Lesson 04 — Frequency Counting

- Learning status: `TODO`; initialized on 2026-10-09 but not yet active
- Time: 35–45 minutes
- Prerequisite: HashMap lookup and insertion
- Primary idea: map each character to how many unmatched copies remain
- New terms: frequency, anagram, balance
- LeetCode: [Valid Anagram](https://leetcode.com/problems/valid-anagram/)

Do not begin this lesson while HashMap Value-to-Index Lookup remains `LEARNING`.

## Problem

Given two non-null strings `first` and `second`, return `true` when `second` is an anagram of `first` and `false` otherwise.

An anagram uses exactly the same characters with exactly the same counts, possibly in a different order.

Assumptions:

- Both strings contain lowercase English letters only.
- Empty strings are valid and are anagrams of each other.
- Character order does not matter, but character counts do.

Examples:

- `"anagram"`, `"nagaram"` → `true`
- `"rat"`, `"car"` → `false`
- `"aacc"`, `"ccac"` → `false`; both use the same distinct letters but different counts
- `""`, `""` → `true`

Target: average `O(n)` time and `O(n)` additional space, where `n` is the combined input length.

## Runnable code

- Write here: [ValidAnagramPractice.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/arrayshashmaps/frequencycounting/ValidAnagramPractice.java)
- Reveal after attempting: [ValidAnagramSolution.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/arrayshashmaps/frequencycounting/ValidAnagramSolution.java)

When this lesson becomes active, change only `isAnagram` and run the practice class from IntelliJ.

## Why the problem exists

Checking only whether both strings contain the same distinct letters is insufficient. `"aacc"` and `"ccac"` both contain `a` and `c`, but their counts differ.

The useful question is:

> How many unmatched copies of each character remain?

A frequency map stores that answer.

## Reference approach

```java
static boolean isAnagram(String first, String second) {
    if (first.length() != second.length()) {
        return false;
    }

    Map<Character, Integer> remainingByCharacter = new HashMap<>();
    for (char character : first.toCharArray()) {
        int currentCount = remainingByCharacter.getOrDefault(character, 0);
        remainingByCharacter.put(character, currentCount + 1);
    }

    for (char character : second.toCharArray()) {
        Integer currentCount = remainingByCharacter.get(character);
        if (currentCount == null) {
            return false;
        }

        if (currentCount == 1) {
            remainingByCharacter.remove(character);
        } else {
            remainingByCharacter.put(character, currentCount - 1);
        }
    }

    return remainingByCharacter.isEmpty();
}
```

## Count the first string

```java
int currentCount = remainingByCharacter.getOrDefault(character, 0);
remainingByCharacter.put(character, currentCount + 1);
```

`getOrDefault(character, 0)` returns the stored count or `0` when the character has not appeared. For `"aab"`, the map changes as follows:

```text
first a -> {a=1}
second a -> {a=2}
b -> {a=2, b=1}
```

Without the default `0`, the first lookup would return `null`, and arithmetic could not safely add one.

## Consume counts with the second string

For each character in `second`, retrieve its remaining count. A missing character immediately proves the strings are not anagrams.

When the count is `1`, remove the key because the last unmatched copy has been consumed. Otherwise, store `currentCount - 1`.

For `first = "aab"` and `second = "aba"`:

```text
start -> {a=2, b=1}
consume a -> {a=1, b=1}
consume b -> {a=1}
consume a -> {}
```

The empty map means every copy from the first string was matched exactly once.

## Loop invariant

Before each character of `second` is processed, the map contains the counts from `first` that have not yet been matched by the already-processed prefix of `second`.

This explains why a missing character fails immediately and why an empty map at the end proves the strings are anagrams.

## Correctness

- If the method returns `true`, both strings have equal length and every stored count was consumed exactly, so every character count matches.
- If the strings are anagrams, every character in `second` consumes one available copy from `first`, no lookup fails, and the map becomes empty.

## Complexity

- Average time: `O(n)` because each character causes a constant number of average `O(1)` HashMap operations.
- Additional space: `O(n)` in the general character case. Under the lowercase-English assumption, at most 26 keys are stored.

## Pitfalls and alternatives

- A `HashSet` loses counts and cannot distinguish `"aacc"` from `"ccac"`.
- Sorting both strings works in `O(n log n)` time but changes the time target.
- An `int[26]` is a good constant-space alternative under the lowercase-English constraint, but the HashMap version teaches reusable frequency counting.

## Questions and explained answers

<details>
<summary>1. What does the map represent while processing the second string?</summary>

It stores how many unmatched copies of each character from the first string remain after consuming the processed prefix of the second string.

</details>

<details>
<summary>2. Why is a HashSet insufficient?</summary>

A set records only whether a character exists. Anagrams require equal counts, so repeated characters must be counted.

</details>

<details>
<summary>3. Why can the method fail immediately when a character is absent from the map?</summary>

The second string is requesting a copy that the first string does not have left. No later character can repair that excess copy.

</details>

## Stop/go

Proceed only when you can implement the method, state the balance invariant, explain why a set fails, and pass every practice check.
