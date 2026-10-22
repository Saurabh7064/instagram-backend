# Micro-Lesson 02 — Longest Substring Without Repeating Characters

## Question

> Given a string `text`, return the length of the longest contiguous substring that contains no repeated character.

Practice source: [Longest Substring Without Repeating Characters on LeetCode](https://leetcode.com/problems/longest-substring-without-repeating-characters/)

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

## First understand the idea

The key idea is to keep a current substring with no repeated character, shortening its left side only when the next character would repeat.

Use `text="abcabcbb"`. Let `left` and `right` mark the current candidate's ends; `activeCharacters` remembers its characters, and `bestLength` stores the longest valid length seen. Reading indexes 0, 1, 2 builds abc with set `{a,b,c}` and length `2-0+1=3`.

Index 3 brings another a. The set already contains a, so remove the character at left index 0, then move left to 1. Now add the new a: the candidate is bca at indexes 1..3, length `3-1+1=3`. Later repeated characters force more removals; no candidate exceeds 3, so return 3.

This current contiguous candidate is an **active window**. In general, remove characters from its left until the incoming character is absent, then add it and compare `right-left+1` with the best length. The +1 counts both endpoints: positions 1,2,3 are three characters, not two. A removed prefix cannot help a future window that still includes the repetition which made it invalid.

:::simulation
type: longest-unique-substring
language: java
title: Longest unique substring · expand and shrink
text: abcabcbb
:::

## Block-by-block code walkthrough

Original snapshots are retained. Where a value changes several times, its cell shows the sequence ending at the final value.

**Running example for the code and tables below:** Input `text = "abcabcbb"`; initially `activeCharacters = {}`, `left = 0`, `bestLength = 0`. The input string stays unchanged. `incoming` is read once per outer iteration and remains the same throughout its removal loop.

Tables belong to the code block immediately above them. Inputs stay unchanged unless a block explicitly creates or changes a copy. Collection order is displayed for readability; HashMap/HashSet order is unspecified.

### Block 1 — Represent the current valid window

```java
Set<Character> activeCharacters = new HashSet<>();
int left = 0;
int bestLength = 0;
```

**What it evaluates:** the set starts empty, `left` marks the first active index, and no valid length has been seen yet.

**Why it is needed:** set lookup answers “is this character already active?” in average `O(1)` time.

**Concrete result:** before scanning `"abcabcbb"`, the set is `{}`, `left = 0`, and `bestLength = 0`.

**What fails without it:** searching the window character by character for every `right` can degrade to `O(n²)`.

**Equivalent clearer form:** a boolean array can replace the set only when the character range is known and small.

**Debugger state at this block:**

- Input: `text="abcabcbb"`; the string stays unchanged.
- `new HashSet<>()` creates `activeCharacters={}`; `left=0`, `bestLength=0`.
- `right` and `incoming` are not assigned yet.

### Block 2 — Expand by one character

```java
for (int right = 0; right < text.length(); right++) {
    char incoming = text.charAt(right);
```

**What it evaluates:** `right` visits every valid index once and `incoming` names the new character.

**Concrete result:** for `"abcabcbb"`, `right=6` reads `'b'`, which is already in the active set.

**What fails without it:** using `right <= text.length()` attempts `charAt(text.length())`, which is outside the string.

**Equivalent clearer form:** a `while` loop with an explicit `right++` is equivalent but easier to forget to advance.

**Debugger state at this block:**

| operation | right | incoming | left | activeCharacters | bestLength |
| --- | --- | --- | --- | --- | --- |
| `read incoming` | `0` | `a` | `0` | `[]` | `0` |
| `read incoming` | `1` | `b` | `0` | `['a']` | `1` |
| `read incoming` | `2` | `c` | `0` | `['a', 'b']` | `2` |
| `read incoming` | `3` | `a` | `0` | `['a', 'b', 'c']` | `3` |
| `read incoming` | `4` | `b` | `1` | `['a', 'b', 'c']` | `3` |
| `read incoming` | `5` | `c` | `2` | `['a', 'b', 'c']` | `3` |
| `read incoming` | `6` | `b` | `3` | `['a', 'b', 'c']` | `3` |
| `read incoming` | `7` | `b` | `5` | `['b', 'c']` | `3` |

### Block 3 — Shrink until the duplicate is gone

```java
while (activeCharacters.contains(incoming)) {
    activeCharacters.remove(text.charAt(left));
    left++;
}
```

**What it evaluates:** the loop removes the leftmost active character repeatedly while `incoming` is still duplicated.

**Concrete result:** at `right=6`, the set is `{a,b,c}`, `left=3`, and `incoming='b'`. Remove `a`, move `left` to `4`; `b` is still present, so remove `b` and move `left` to `5`.

**Why it is needed:** the repeated character may not be at the left boundary.

**What fails without it:** an `if` would remove only `a` at `right=6`, leaving the old `b`; adding the incoming `b` would leave a duplicated substring.

**Equivalent clearer form:** store last-seen indexes and jump `left`, but that introduces a different state model reserved for a later optimization.

**Debugger state at this block:**

| operation | right | incoming | left | activeCharacters | bestLength |
| --- | --- | --- | --- | --- | --- |
| `remove a; left++` | `3` | `a` | `1` | `['b', 'c']` | `3` |
| `remove b; left++` | `4` | `b` | `2` | `['a', 'c']` | `3` |
| `remove c; left++` | `5` | `c` | `3` | `['a', 'b']` | `3` |
| `remove a; left++` | `6` | `b` | `4` | `['b', 'c']` | `3` |
| `remove b; left++` | `6` | `b` | `5` | `['c']` | `3` |
| `remove c; left++` | `7` | `b` | `6` | `['b']` | `3` |
| `remove b; left++` | `7` | `b` | `7` | `[]` | `3` |

### Block 4 — Add only after validity is restored

```java
activeCharacters.add(incoming);
bestLength = Math.max(bestLength, right - left + 1);
```

**What it evaluates:** the new character enters a now-unique window, whose inclusive length is compared with the best.

**Concrete result:** `left=0`, `right=2` gives `2 - 0 + 1 = 3` for `"abc"`.

**What fails without it:** omitting `+ 1` reports `2`; recording before the shrink can count an invalid duplicated window.

**Equivalent clearer form:** compute `int windowLength = right - left + 1` before `Math.max`.

**Debugger state at this block:**

| operation | right | incoming | left | activeCharacters | bestLength |
| --- | --- | --- | --- | --- | --- |
| `add incoming; update bestLength` | `0` | `a` | `0` | `['a']` | `1` |
| `add incoming; update bestLength` | `1` | `b` | `0` | `['a', 'b']` | `2` |
| `add incoming; update bestLength` | `2` | `c` | `0` | `['a', 'b', 'c']` | `3` |
| `add incoming; update bestLength` | `3` | `a` | `1` | `['a', 'b', 'c']` | `3` |
| `add incoming; update bestLength` | `4` | `b` | `2` | `['a', 'b', 'c']` | `3` |
| `add incoming; update bestLength` | `5` | `c` | `3` | `['a', 'b', 'c']` | `3` |
| `add incoming; update bestLength` | `6` | `b` | `5` | `['b', 'c']` | `3` |
| `add incoming; update bestLength` | `7` | `b` | `7` | `['b']` | `3` |


**Narrated execution — follow each operation, then check its values:**

**Iteration/check 1 — right = `0`.**

- **✅ `for (right < text.length())`:** `0 < 8 → true`; enter the outer loop.
- `char incoming = text.charAt(right)` reads index `0` → `'a'`; set before: `{}`.
- **❌ `while (activeCharacters.contains(incoming))`:** `contains('a') → false`; skip removal and `left++`; `left=0` stays.
- `activeCharacters.add(incoming)`: `{} → {'a'}`.
- `bestLength = Math.max(bestLength, right - left + 1)`: length `0 - 0 + 1 = 1`; `max(0, 1) = 1`.
- **State:** `left=0, bestLength=1, incoming='a'`; set `{'a'}`.
- `right++`: `0 → 1`; **✅ `for (right < text.length())`:** `1 < 8 → true` next.

| right | incoming | set before | while contains(incoming): every evaluation | left after | set after add | length | bestLength | next action |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `0` | `a` | `[]` | `❌ while (activeCharacters.contains(incoming)): contains('a') → false; stop shrinking` | `0` | `['a']` | `0-0+1=1` | `1` | `right++: 0 → 1; ✅ for (right < text.length()): 1 < 8 → true; continue` |

**Iteration/check 2 — right = `1`.**

- **✅ `for (right < text.length())`:** `1 < 8 → true`; enter the outer loop.
- `char incoming = text.charAt(right)` reads index `1` → `'b'`; set before: `{'a'}`.
- **❌ `while (activeCharacters.contains(incoming))`:** `contains('b') → false`; skip removal and `left++`; `left=0` stays.
- `activeCharacters.add(incoming)`: `{'a'} → {'a', 'b'}`.
- `bestLength = Math.max(bestLength, right - left + 1)`: length `1 - 0 + 1 = 2`; `max(1, 2) = 2`.
- **State:** `left=0, bestLength=2, incoming='b'`; set `{'a', 'b'}`.
- `right++`: `1 → 2`; **✅ `for (right < text.length())`:** `2 < 8 → true` next.

| right | incoming | set before | while contains(incoming): every evaluation | left after | set after add | length | bestLength | next action |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `1` | `b` | `['a']` | `❌ while (activeCharacters.contains(incoming)): contains('b') → false; stop shrinking` | `0` | `['a', 'b']` | `1-0+1=2` | `2` | `right++: 1 → 2; ✅ for (right < text.length()): 2 < 8 → true; continue` |

**Iteration/check 3 — right = `2`.**

- **✅ `for (right < text.length())`:** `2 < 8 → true`; enter the outer loop.
- `char incoming = text.charAt(right)` reads index `2` → `'c'`; set before: `{'a', 'b'}`.
- **❌ `while (activeCharacters.contains(incoming))`:** `contains('c') → false`; skip removal and `left++`; `left=0` stays.
- `activeCharacters.add(incoming)`: `{'a', 'b'} → {'a', 'b', 'c'}`.
- `bestLength = Math.max(bestLength, right - left + 1)`: length `2 - 0 + 1 = 3`; `max(2, 3) = 3`.
- **State:** `left=0, bestLength=3, incoming='c'`; set `{'a', 'b', 'c'}`.
- `right++`: `2 → 3`; **✅ `for (right < text.length())`:** `3 < 8 → true` next.

| right | incoming | set before | while contains(incoming): every evaluation | left after | set after add | length | bestLength | next action |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `2` | `c` | `['a', 'b']` | `❌ while (activeCharacters.contains(incoming)): contains('c') → false; stop shrinking` | `0` | `['a', 'b', 'c']` | `2-0+1=3` | `3` | `right++: 2 → 3; ✅ for (right < text.length()): 3 < 8 → true; continue` |

**Iteration/check 4 — right = `3`.**

- **✅ `for (right < text.length())`:** `3 < 8 → true`; enter the outer loop.
- `char incoming = text.charAt(right)` reads index `3` → `'a'`; set before: `{'a', 'b', 'c'}`.
- **✅ `while (activeCharacters.contains(incoming))`:** `contains('a') → true`; enter the removal body.
- `activeCharacters.remove(text.charAt(left))` reads `text.charAt(0)='a'`: `{'a', 'b', 'c'} → {'b', 'c'}`.
- `left++`: `0 → 1`.
- **❌ `while (activeCharacters.contains(incoming))`:** `contains('a') → false`; stop shrinking.
- `activeCharacters.add(incoming)`: `{'b', 'c'} → {'a', 'b', 'c'}`.
- `bestLength = Math.max(bestLength, right - left + 1)`: length `3 - 1 + 1 = 3`; `max(3, 3) = 3`.
- **State:** `left=1, bestLength=3, incoming='a'`; set `{'a', 'b', 'c'}`.
- `right++`: `3 → 4`; **✅ `for (right < text.length())`:** `4 < 8 → true` next.

| right | incoming | set before | while contains(incoming): every evaluation | left after | set after add | length | bestLength | next action |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `3` | `a` | `['a', 'b', 'c']` | `✅ while (activeCharacters.contains(incoming)): contains('a') → true; activeCharacters.remove(text.charAt(0)) removes 'a': {'a', 'b', 'c'} → {'b', 'c'}; left++: 0 → 1; ❌ while (activeCharacters.contains(incoming)): contains('a') → false; stop shrinking` | `1` | `['a', 'b', 'c']` | `3-1+1=3` | `3` | `right++: 3 → 4; ✅ for (right < text.length()): 4 < 8 → true; continue` |

**Iteration/check 5 — right = `4`.**

- **✅ `for (right < text.length())`:** `4 < 8 → true`; enter the outer loop.
- `char incoming = text.charAt(right)` reads index `4` → `'b'`; set before: `{'a', 'b', 'c'}`.
- **✅ `while (activeCharacters.contains(incoming))`:** `contains('b') → true`; enter the removal body.
- `activeCharacters.remove(text.charAt(left))` reads `text.charAt(1)='b'`: `{'a', 'b', 'c'} → {'a', 'c'}`.
- `left++`: `1 → 2`.
- **❌ `while (activeCharacters.contains(incoming))`:** `contains('b') → false`; stop shrinking.
- `activeCharacters.add(incoming)`: `{'a', 'c'} → {'a', 'b', 'c'}`.
- `bestLength = Math.max(bestLength, right - left + 1)`: length `4 - 2 + 1 = 3`; `max(3, 3) = 3`.
- **State:** `left=2, bestLength=3, incoming='b'`; set `{'a', 'b', 'c'}`.
- `right++`: `4 → 5`; **✅ `for (right < text.length())`:** `5 < 8 → true` next.

| right | incoming | set before | while contains(incoming): every evaluation | left after | set after add | length | bestLength | next action |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `4` | `b` | `['a', 'b', 'c']` | `✅ while (activeCharacters.contains(incoming)): contains('b') → true; activeCharacters.remove(text.charAt(1)) removes 'b': {'a', 'b', 'c'} → {'a', 'c'}; left++: 1 → 2; ❌ while (activeCharacters.contains(incoming)): contains('b') → false; stop shrinking` | `2` | `['a', 'b', 'c']` | `4-2+1=3` | `3` | `right++: 4 → 5; ✅ for (right < text.length()): 5 < 8 → true; continue` |

**Iteration/check 6 — right = `5`.**

- **✅ `for (right < text.length())`:** `5 < 8 → true`; enter the outer loop.
- `char incoming = text.charAt(right)` reads index `5` → `'c'`; set before: `{'a', 'b', 'c'}`.
- **✅ `while (activeCharacters.contains(incoming))`:** `contains('c') → true`; enter the removal body.
- `activeCharacters.remove(text.charAt(left))` reads `text.charAt(2)='c'`: `{'a', 'b', 'c'} → {'a', 'b'}`.
- `left++`: `2 → 3`.
- **❌ `while (activeCharacters.contains(incoming))`:** `contains('c') → false`; stop shrinking.
- `activeCharacters.add(incoming)`: `{'a', 'b'} → {'a', 'b', 'c'}`.
- `bestLength = Math.max(bestLength, right - left + 1)`: length `5 - 3 + 1 = 3`; `max(3, 3) = 3`.
- **State:** `left=3, bestLength=3, incoming='c'`; set `{'a', 'b', 'c'}`.
- `right++`: `5 → 6`; **✅ `for (right < text.length())`:** `6 < 8 → true` next.

| right | incoming | set before | while contains(incoming): every evaluation | left after | set after add | length | bestLength | next action |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `5` | `c` | `['a', 'b', 'c']` | `✅ while (activeCharacters.contains(incoming)): contains('c') → true; activeCharacters.remove(text.charAt(2)) removes 'c': {'a', 'b', 'c'} → {'a', 'b'}; left++: 2 → 3; ❌ while (activeCharacters.contains(incoming)): contains('c') → false; stop shrinking` | `3` | `['a', 'b', 'c']` | `5-3+1=3` | `3` | `right++: 5 → 6; ✅ for (right < text.length()): 6 < 8 → true; continue` |

**Iteration/check 7 — right = `6`.**

- **✅ `for (right < text.length())`:** `6 < 8 → true`; enter the outer loop.
- `char incoming = text.charAt(right)` reads index `6` → `'b'`; set before: `{'a', 'b', 'c'}`.
- **✅ `while (activeCharacters.contains(incoming))`:** `contains('b') → true`; enter the removal body.
- `activeCharacters.remove(text.charAt(left))` reads `text.charAt(3)='a'`: `{'a', 'b', 'c'} → {'b', 'c'}`.
- `left++`: `3 → 4`.
- **✅ `while (activeCharacters.contains(incoming))`:** `contains('b') → true`; enter the removal body.
- `activeCharacters.remove(text.charAt(left))` reads `text.charAt(4)='b'`: `{'b', 'c'} → {'c'}`.
- `left++`: `4 → 5`.
- **❌ `while (activeCharacters.contains(incoming))`:** `contains('b') → false`; stop shrinking.
- `activeCharacters.add(incoming)`: `{'c'} → {'b', 'c'}`.
- `bestLength = Math.max(bestLength, right - left + 1)`: length `6 - 5 + 1 = 2`; `max(3, 2) = 3`.
- **State:** `left=5, bestLength=3, incoming='b'`; set `{'b', 'c'}`.
- `right++`: `6 → 7`; **✅ `for (right < text.length())`:** `7 < 8 → true` next.

| right | incoming | set before | while contains(incoming): every evaluation | left after | set after add | length | bestLength | next action |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `6` | `b` | `['a', 'b', 'c']` | `✅ while (activeCharacters.contains(incoming)): contains('b') → true; activeCharacters.remove(text.charAt(3)) removes 'a': {'a', 'b', 'c'} → {'b', 'c'}; left++: 3 → 4; ✅ while (activeCharacters.contains(incoming)): contains('b') → true; activeCharacters.remove(text.charAt(4)) removes 'b': {'b', 'c'} → {'c'}; left++: 4 → 5; ❌ while (activeCharacters.contains(incoming)): contains('b') → false; stop shrinking` | `3 → 4 → 5` | `['b', 'c']` | `6-5+1=2` | `3` | `right++: 6 → 7; ✅ for (right < text.length()): 7 < 8 → true; continue` |

**Iteration/check 8 — right = `7`.**

- **✅ `for (right < text.length())`:** `7 < 8 → true`; enter the outer loop.
- `char incoming = text.charAt(right)` reads index `7` → `'b'`; set before: `{'b', 'c'}`.
- **✅ `while (activeCharacters.contains(incoming))`:** `contains('b') → true`; enter the removal body.
- `activeCharacters.remove(text.charAt(left))` reads `text.charAt(5)='c'`: `{'b', 'c'} → {'b'}`.
- `left++`: `5 → 6`.
- **✅ `while (activeCharacters.contains(incoming))`:** `contains('b') → true`; enter the removal body.
- `activeCharacters.remove(text.charAt(left))` reads `text.charAt(6)='b'`: `{'b'} → {}`.
- `left++`: `6 → 7`.
- **❌ `while (activeCharacters.contains(incoming))`:** `contains('b') → false`; stop shrinking.
- `activeCharacters.add(incoming)`: `{} → {'b'}`.
- `bestLength = Math.max(bestLength, right - left + 1)`: length `7 - 7 + 1 = 1`; `max(3, 1) = 3`.
- **State:** `left=7, bestLength=3, incoming='b'`; set `{'b'}`.
- `right++`: `7 → 8`; **❌ `for (right < text.length())`:** `8 < 8 → false`; exit the outer loop.

| right | incoming | set before | while contains(incoming): every evaluation | left after | set after add | length | bestLength | next action |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `7` | `b` | `['b', 'c']` | `✅ while (activeCharacters.contains(incoming)): contains('b') → true; activeCharacters.remove(text.charAt(5)) removes 'c': {'b', 'c'} → {'b'}; left++: 5 → 6; ✅ while (activeCharacters.contains(incoming)): contains('b') → true; activeCharacters.remove(text.charAt(6)) removes 'b': {'b'} → {}; left++: 6 → 7; ❌ while (activeCharacters.contains(incoming)): contains('b') → false; stop shrinking` | `5 → 6 → 7` | `['b']` | `7-7+1=1` | `3` | `right++: 7 → 8; ❌ for (right < text.length()): 8 < 8 → false; exit loop` |

- **❌ `for (right < text.length())`:** `8 < 8 → false`; all characters have been processed.
- `return bestLength` returns `3`; repeated removals at `right=6` and `right=7` explain why `while` is needed.

### Block 5 — Return the completed maximum

```java
return bestLength;
```

**What it evaluates:** after every ending index has been considered, the largest valid length is final.

**Concrete result:** return `3` from the earlier window `"abc"`, even though the final window `"b"` has length `1`.

**What fails without it:** returning the final window length misses a longer window that appeared earlier.

**Debugger state at this block:**

- **Final state:** `left=7`, `activeCharacters={'b'}`, `bestLength=3`; the final window is `"b"`.
- **❌ `for (right < text.length())`:** `8 < 8 → false`; exit the loop.
- `return bestLength` returns the earlier maximum `3`, rather than the final window length `1`.

## Reference approach

```java
// text index: 0 1 2 3 4 5 6 7
// text      : a b c a b c b b
// Input: text = "abcabcbb"
// Expected return: 3

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

Proceed only when you can explain why `while` is required, trace `"abcabcbb"`, state the invariant and complexity, implement the practice method, and pass all checks.
