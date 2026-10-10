# Micro-Lesson 04 — Frequency Counting

## Question

> Given two non-null strings, return whether the second string is an anagram of the first.

Practice source: [Valid Anagram on LeetCode](https://leetcode.com/problems/valid-anagram/)

## What you will learn

- why membership alone is insufficient when duplicate counts matter;
- how to build a frequency map from one input and consume it with another;
- how the remaining-count invariant proves correctness;
- when removing a zero-count key makes the final condition clearer;
- when a fixed-size array is a reasonable alternative to a map.

## Lesson status

- Learning status: `TODO`; initialized on 2026-10-09 but not yet active
- Time: 35–45 minutes
- Prerequisite: HashMap lookup and insertion
- Primary idea: map each character to how many unmatched copies remain
- New terms: frequency, anagram, balance

Do not begin this lesson while HashMap Value-to-Index Lookup remains `LEARNING`.

## Inputs, examples, and target

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

## Block-by-block code walkthrough

### Block 1 — Reject different lengths

```java
if (first.length() != second.length()) {
    return false;
}
```

**What it evaluates:** it compares the number of characters before building any counts. `"ab"` has length `2`; `"a"` has length `1`, so the condition is true.

**Why it is needed:** anagrams use exactly the same number of characters. The early return also prevents unnecessary map work.

**What fails without it:** the counting algorithm can still detect most differences, but it performs avoidable work and makes the final proof less direct.

**Equivalent clearer form:** store both lengths in named variables first; the boolean decision is identical.

### Block 2 — Create the remaining-count map

```java
Map<Character, Integer> remainingByCharacter = new HashMap<>();
```

**What it evaluates:** an empty mapping is created from each character to its unmatched count.

**Why it is needed:** a set cannot distinguish one `a` from two `a` characters.

**Concrete result:** after counting `"aab"`, the map will be `{a=2, b=1}`.

**What fails without it:** the method has nowhere to retain counts between the two scans.

**Equivalent clearer form:** under the lowercase-English constraint, `int[] counts = new int[26]` can use `character - 'a'` as the index, but it teaches a less general representation.

### Block 3 — Count every character in the first string

```java
for (char character : first.toCharArray()) {
    int currentCount = remainingByCharacter.getOrDefault(character, 0);
    remainingByCharacter.put(character, currentCount + 1);
}
```

**What it evaluates:** `toCharArray()` supplies each character. `getOrDefault` returns its saved count or `0`; `put` writes one more.

**Concrete result:** the second `a` in `"aab"` reads `1` and stores `2`.

**Why it is needed:** the map must represent every copy available for matching.

**What fails without it:** calling `get(character)` for the first occurrence returns `null`; unboxing or adding one would throw `NullPointerException`.

**Equivalent clearer form:** `remainingByCharacter.merge(character, 1, Integer::sum);` performs the same frequency update, but `getOrDefault` makes the arithmetic visible to a learner.

### Block 4 — Request one available copy for each second-string character

```java
for (char character : second.toCharArray()) {
    Integer currentCount = remainingByCharacter.get(character);
    if (currentCount == null) {
        return false;
    }
```

**What it evaluates:** the loop retrieves the unmatched count. `Integer` is required because absence is represented by `null`.

**Concrete result:** with first `"rat"` and second `"car"`, lookup for `c` returns `null`, so the method returns `false` immediately.

**Why it is needed:** a missing key means the second string requests a character copy the first string does not have left.

**What fails without it:** unboxing a missing result into primitive `int` throws, and ignoring absence could accept unequal strings.

**Equivalent clearer form:** `if (!remainingByCharacter.containsKey(character)) return false;` followed by `get` is correct but performs a second lookup.

### Block 5 — Remove the last copy or decrement the balance

```java
if (currentCount == 1) {
    remainingByCharacter.remove(character);
} else {
    remainingByCharacter.put(character, currentCount - 1);
}
```

**What it evaluates:** count `1` means the current character consumes the final copy, so the key disappears. Larger counts are reduced by one.

**Concrete result:** `{a=2, b=1}` becomes `{a=1, b=1}` after `a`, then `{a=1}` after `b`.

**Why it is needed:** the map must continue to mean “unmatched copies remaining.” Removing zero-count keys makes an empty map a complete final proof.

**What fails without it:** leaving counts unchanged lets repeated characters in the second string reuse the same copy. Storing zero instead of removing is correct only if the final check verifies every value is zero rather than calling `isEmpty()`.

**Equivalent clearer form:** always store `currentCount - 1`, then run a final loop checking for zero balances; that keeps more keys and needs more code.

### Block 6 — Accept only a completely consumed map

```java
return remainingByCharacter.isEmpty();
```

**What it evaluates:** `isEmpty()` is true only when every key was removed after its final copy was matched.

**Concrete results:** `"aab"`/`"aba"` ends with `{}` and returns `true`; a leftover count would return `false`.

**What fails without it:** an unconditional `true` could accept a first string with unmatched copies if the length guard or future contract changed.

**Equivalent clearer form:** under the current equal-length guard and immediate missing-key failure, `return true` is logically sufficient, but `isEmpty()` directly checks the invariant and resists later changes.

## Sort-based alternative

A simpler but slower approach sorts both strings and compares the resulting arrays.

```java
static boolean isAnagramBySort(String first, String second) {
    if (first.length() != second.length()) {
        return false;
    }

    char[] firstCharacters = first.toCharArray();
    char[] secondCharacters = second.toCharArray();

    Arrays.sort(firstCharacters);
    Arrays.sort(secondCharacters);

    return Arrays.equals(firstCharacters, secondCharacters);
}
```

This preserves the exact same character multiset, but the work is `O(n log n)` because sorting dominates the runtime. It is still valid when a simpler implementation is preferred and the problem does not require the faster linear-time strategy.

The alternative’s length guard has the same purpose as Block 1. `toCharArray()` creates mutable copies because Java strings cannot be sorted in place; `Arrays.sort(...)` puts equal multisets into identical order; `Arrays.equals(...)` compares character contents rather than array identities. Using `firstCharacters == secondCharacters` would compare whether both variables reference the same array object and would incorrectly return `false` for separate equal arrays.

## Additional trace: count the first string

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

## Additional trace: consume counts with the second string

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


## Practice after learning the concept

- Write here: [ValidAnagramPractice.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/arrayshashmaps/frequencycounting/ValidAnagramPractice.java)
- Reveal after attempting: [ValidAnagramSolution.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/arrayshashmaps/frequencycounting/ValidAnagramSolution.java)

When this lesson becomes active, change only `isAnagram` and run the practice class from IntelliJ.

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
