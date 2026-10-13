# Micro-Lesson 02 — Longest Substring Without Repeating Characters

## Question

> Given a string `text`, return the length of the longest contiguous substring that contains no repeated character.

Practice source: [Longest Substring Without Repeating Characters on LeetCode](https://leetcode.com/problems/longest-substring-without-repeating-characters/)

Examples:

- `"abcabcbb"` → `3`, from `"abc"`.
- `"bbbbb"` → `1`, from `"b"`.
- `"pwwkew"` → `3`, from `"wke"` or `"kew"`.

Assumptions follow LeetCode: `0 <= text.length <= 50,000`; characters may include letters, digits, symbols, and spaces. Target `O(n)` time and `O(min(n, alphabet))` space.

## What you will learn

- how a variable-size window differs from the fixed-size window in Lesson 01;
- how to expand with `right` while the rule remains valid;
- how to shrink with `left` until a duplicate has been removed;
- why a `while` loop, not one removal, restores uniqueness;
- how a set represents exactly the characters in the current window.

## Lesson status

- Learning status: `TODO`; initialized but not active
- Prerequisite: [Maximum Average Subarray I](../01-maximum-average-subarray/README.md)
- Primary idea: shrink only when the incoming character duplicates an active character
- New terms: active window, expand, shrink

## First understand how to find the answer

The **active window** is the current contiguous candidate from index `left` through `right`.

1. **Expand:** move `right` to include the next character.
2. **Check:** if that character is not in the set, the window is still unique.
3. **Shrink:** if it is already present, remove characters at `left` and move `left` right until the duplicate is gone.
4. **Record:** add the incoming character, then compare `right - left + 1` with the best length.

The window never needs to move leftward. Once a prefix caused a duplicate, adding more characters cannot make that discarded prefix useful again.

:::simulation
type: longest-unique-substring
language: java
title: Longest unique substring · expand and shrink
text: pwwkewxz
:::

## Work through the results

For `"abcabcbb"`:

- indexes `0..2` form `"abc"`, so `bestLength = 3`;
- index `3` brings another `a`; remove the old `a` at index `0`, producing `"bca"`;
- later duplicates keep the window length at most `3`;
- result: `3`.

For `"bbbbb"`, every `b` after the first duplicates the active `b`. The old `b` leaves before the new one enters, so every valid window has length `1`.

For `"pwwkew"`, the second `w` forces removals of `p` and the earlier `w`. The window becomes `"w"`, then grows to `"wke"` with length `3`. The final `w` produces another valid length-`3` window, `"kew"`.

## Reference approach

```java
static int lengthOfLongestSubstring(String text) {
    Set<Character> activeCharacters = new HashSet<>();
    int left = 0;
    int bestLength = 0;

    for (int right = 0; right < text.length(); right++) {
        char incoming = text.charAt(right);
        while (activeCharacters.contains(incoming)) {
            activeCharacters.remove(text.charAt(left));
            left++;
        }
        activeCharacters.add(incoming);
        bestLength = Math.max(bestLength, right - left + 1);
    }

    return bestLength;
}
```

## Block-by-block code walkthrough

### Block 1 — Represent the current valid window

```java
Set<Character> activeCharacters = new HashSet<>();
int left = 0;
int bestLength = 0;
```

**What it evaluates:** the set starts empty, `left` marks the first active index, and no valid length has been seen yet.

**Why it is needed:** set lookup answers “is this character already active?” in average `O(1)` time.

**Concrete result:** before scanning `"abc"`, the set is `{}`, `left = 0`, and `bestLength = 0`.

**What fails without it:** searching the window character by character for every `right` can degrade to `O(n²)`.

**Equivalent clearer form:** a boolean array can replace the set only when the character range is known and small.

### Block 2 — Expand by one character

```java
for (int right = 0; right < text.length(); right++) {
    char incoming = text.charAt(right);
```

**What it evaluates:** `right` visits every valid index once and `incoming` names the new character.

**Concrete result:** for `"pww"`, `right = 2` reads the second `w`.

**What fails without it:** using `right <= text.length()` attempts `charAt(text.length())`, which is outside the string.

**Equivalent clearer form:** a `while` loop with an explicit `right++` is equivalent but easier to forget to advance.

### Block 3 — Shrink until the duplicate is gone

```java
while (activeCharacters.contains(incoming)) {
    activeCharacters.remove(text.charAt(left));
    left++;
}
```

**What it evaluates:** the loop removes the leftmost active character repeatedly while `incoming` is still duplicated.

**Concrete result:** before index `2` in `"pww"`, the set is `{p, w}`. Remove `p` and move `left` to `1`; `w` is still present, so remove it and move `left` to `2`.

**Why it is needed:** the repeated character may not be at the left boundary.

**What fails without it:** an `if` removes only `p` in that example, leaving the old `w`; adding the new `w` would still produce a duplicate.

**Equivalent clearer form:** store last-seen indexes and jump `left`, but that introduces a different state model reserved for a later optimization.

### Block 4 — Add only after validity is restored

```java
activeCharacters.add(incoming);
bestLength = Math.max(bestLength, right - left + 1);
```

**What it evaluates:** the new character enters a now-unique window, whose inclusive length is compared with the best.

**Concrete result:** `left = 2`, `right = 4` in `"pwwke"` gives `4 - 2 + 1 = 3` for `"wke"`.

**What fails without it:** omitting `+ 1` reports `2`; recording before the shrink can count an invalid duplicated window.

**Equivalent clearer form:** compute `int windowLength = right - left + 1` before `Math.max`.

### Block 5 — Return the completed maximum

```java
return bestLength;
```

**What it evaluates:** after every ending index has been considered, the largest valid length is final.

**Concrete result:** an empty string skips the loop and returns `0`.

**What fails without it:** returning the final window length misses a longer window that appeared earlier.

## Invariant and correctness

After the shrink loop and insertion, the set contains exactly the characters at indexes `left..right`, with no duplicates. Every `right` is considered, and `bestLength` records the largest valid window ending at any processed index. Therefore it is the global longest unique substring length.

## Complexity and alternatives

- Time: `O(n)` because each character enters once and leaves at most once.
- Space: `O(min(n, alphabet))` for active characters.
- Brute force: generate every substring and check uniqueness, taking `O(n²)` or worse.
- Last-seen map: can jump `left` directly, but the set version makes expand/shrink mechanics clearer.

## Common pitfalls

- treating a substring as a non-contiguous subsequence;
- removing only once when the duplicate is deeper in the window;
- adding the incoming character before removing its older copy;
- computing width as `right - left` instead of `right - left + 1`;
- resetting the entire set after a duplicate and losing reusable work.

## Practice after learning the concept

- Write here: [LongestUniqueSubstringPractice.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/slidingwindow/uniquesubstring/LongestUniqueSubstringPractice.java)
- Reveal after attempting: [LongestUniqueSubstringSolution.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/slidingwindow/uniquesubstring/LongestUniqueSubstringSolution.java)

## Questions and explained answers

<details>
<summary>1. Why can left move more than once for one incoming character?</summary>

The earlier copy may be inside the window rather than at its first position. Every character before and including that copy must leave before the incoming copy is unique.

</details>

<details>
<summary>2. What does the set contain after each iteration?</summary>

Exactly one copy of every character in the current contiguous range `left..right`.

</details>

<details>
<summary>3. Why is the scan O(n) even though it contains a nested while loop?</summary>

`right` adds each character once, while `left` removes each character at most once. The total number of boundary movements is at most `2n`.

</details>

## Stop/go

Proceed only when you can explain why `while` is required, trace `"pwwkew"`, state the invariant and complexity, implement the practice method, and pass all checks.
