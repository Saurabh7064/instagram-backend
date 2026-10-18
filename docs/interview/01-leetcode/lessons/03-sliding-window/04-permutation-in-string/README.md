# Micro-Lesson 04 — Permutation in String

## Question

> Given lowercase strings `pattern` and `text`, return `true` if `text` contains a contiguous permutation of `pattern`; otherwise return `false`.

Practice source: [Permutation in String on LeetCode](https://leetcode.com/problems/permutation-in-string/)

Examples:

- `pattern = "ab"`, `text = "eidbaooo"` → `true` because `"ba"` is a permutation.
- `pattern = "ab"`, `text = "eidboaoo"` → `false`.
- `pattern = "adc"`, `text = "dcda"` → `true` because `"dca"` has the required counts.

Assumptions follow LeetCode: both strings are non-empty lowercase English letters and have length at most `10,000`. Target `O(n)` time and `O(1)` additional space for the fixed alphabet.

## What you will learn

- why a permutation is identified by counts rather than order;
- why every candidate window must have exactly `pattern.length()` characters;
- how to add one incoming count and remove one outgoing count;
- how exact frequency comparison avoids generating permutations.

## Lesson status

- Learning status: `TODO`; initialized but not active
- Prerequisite: [Longest Repeating Character Replacement](../03-longest-repeating-character-replacement/README.md)
- Primary idea: compare a fixed-size window's frequency signature with the pattern
- New terms: permutation, frequency signature, fixed-size candidate

## First understand how to find the answer

A **permutation** contains the same characters with the same counts in any order. Therefore `"ab"`, `"ba"`, and only those two length-`2` strings share the count signature `{a:1, b:1}`.

1. Count every character in `pattern` once.
2. Expand `right`, adding the incoming text character.
3. Once more than `pattern.length()` characters have entered, remove the character exactly one window-size behind `right`.
4. Compare the 26 counts. Equality means the current fixed-size window is a permutation.

:::simulation
type: permutation-in-string
language: java
title: Permutation in String · fixed count signature
first: ab
second: eidbaooo
:::

## Work through the results

For `"ab"` inside `"eidbaooo"`, examine length-`2` windows:

- `"ei"` → `{e:1,i:1}`, not `{a:1,b:1}`;
- `"id"` → `{i:1,d:1}`, not a match;
- `"db"` → `{d:1,b:1}`, not a match;
- `"ba"` → `{b:1,a:1}`, exact count match; return `true`.

For `"ab"` inside `"eidboaoo"`, every length-`2` window has a missing or extra character, so the result is `false`.

For `"adc"` inside `"dcda"`, the first complete window `"dcd"` has `d:2,c:1`; after sliding, `"cda"` has `a:1,c:1,d:1`, matching the pattern, so return `true`.

## Block-by-block code walkthrough

Follow one input throughout: `pattern="ab"`, `text="eidbaooo"`. Text positions `0..7` contain `e,i,d,b,a,o,o,o`. Each block below explains its code and then shows the state that code produces. Step numbers retain their original execution order across the loop; arrays are shown beside the operation that changes or compares them.

### Block 1 — Reject a pattern that cannot fit

```java
if (pattern.length() > text.length()) {
    return false;
}
```

**What it evaluates:** a candidate must contain exactly as many characters as the pattern.

**Concrete result:** length `4` pattern `"abcd"` cannot fit in length `3` text `"abc"`.

**What fails without it:** the main loop still returns false, but performs work even though no complete candidate can exist.

**Equivalent clearer form:** keep the guard as an early return so later code assumes fitting is possible.

**Debugger state at this block:**

A permutation has exactly the pattern’s length: reordering cannot shorten it. For our input, `pattern.length()=2` and `text.length()=8`, so `2 > 8` is `false`; execution continues. For `pattern="abcd"`, `text="abc"`, `4 > 3` is `true`, so return `false` here. No arrays have been allocated on that path. The guard avoids scanning a text that cannot hold even one candidate.

### Block 2 — Build the pattern's signature

```java
int[] neededCounts = new int[26];
int[] windowCounts = new int[26];
for (int index = 0; index < pattern.length(); index++) {
    neededCounts[pattern.charAt(index) - 'a']++;
}
```

**What it evaluates:** each lowercase character maps to an array position from `0` for `a` through `25` for `z`.

**Concrete result:** `"aab"` produces `neededCounts[0] = 2` and `neededCounts[1] = 1`.

**Why it is needed:** order is irrelevant, but duplicate counts are essential.

**What fails without it:** a set would treat `"aab"` and `"ab"` as identical even though their required lengths and counts differ.

**Equivalent clearer form:** a character-count map works for an unrestricted alphabet.

**Debugger state at this block:**

Both arrays start as `[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]`. Each cell counts one lowercase letter. `pattern.charAt(index)` reads that letter, subtracting `'a'` chooses its cell, and `++` adds one occurrence.

| index | index < 2 | pattern.charAt(index) | array index | count change |
| --- | --- | --- | --- | --- |
| `0` | `true` | `a` | `'a' - 'a' = 0` | `neededCounts[0]: 0 → 1` |
| `1` | `true` | `b` | `'b' - 'a' = 1` | `neededCounts[1]: 0 → 1` |
| `2` | `false` | `not read` | `not evaluated` | `loop exits` |

| Step | operation / index | right | outgoingIndex | Arrays.equals |
| --- | --- | --- | --- | --- |
| 1 | `build needed: index=0, char=a` | `—` | `—` | `—` |
| 2 | `build needed: index=1, char=b` | `—` | `—` | `—` |

Full arrays immediately after those same steps (`a..z`, indexes `0..25`):

| Step | neededCounts | windowCounts |
| --- | --- | --- |
| 1 | `[1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| 2 | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |

The result records exactly the required copies: one `a`, one `b`, and zero of every other letter. `windowCounts` is still all zero because no text character has entered. For `pattern="aab"`, the same loop visits indexes `0,1,2`: cell `a` changes `0 → 1 → 2`, then cell `b` changes `0 → 1`. A set would lose that second copy of `a`.

### Block 3 — Add each incoming character

```java
for (int right = 0; right < text.length(); right++) {
    windowCounts[text.charAt(right) - 'a']++;
```

**What it evaluates:** `right` expands the candidate and increases exactly one count.

**Concrete result:** reading the `b` at index `3` of `"eidbaooo"` changes the active `b` count from `0` to `1`.

**What fails without it:** comparing before insertion evaluates the previous window rather than the one ending at `right`.

**Debugger state at this block:**

Each outer iteration adds its character before removal or comparison. The incoming character indexes for `e,i,d,b,a` are `4,8,3,1,0`. At the start of `right=2`, the maintained window was `ei`; adding `d` temporarily produces `eid`. The next block removes the extra character. `neededCounts` stays fixed throughout this loop.

| Step | operation / index | right | outgoingIndex | Arrays.equals |
| --- | --- | --- | --- | --- |
| 3 | `add e` | `0` | `—` | `not checked yet` |
| 5 | `add i` | `1` | `—` | `not checked yet` |
| 7 | `add d` | `2` | `—` | `not checked yet` |
| 10 | `add b` | `3` | `—` | `not checked yet` |
| 13 | `add a` | `4` | `—` | `not checked yet` |

Full arrays immediately after those same steps (`a..z`, indexes `0..25`):

| Step | neededCounts | windowCounts |
| --- | --- | --- |
| 3 | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| 5 | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,0,0,0,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| 7 | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,0,0,1,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| 10 | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,1,0,1,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| 13 | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[1,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |

### Block 4 — Remove the value one full window behind

```java
if (right >= pattern.length()) {
    int outgoingIndex = right - pattern.length();
    windowCounts[text.charAt(outgoingIndex) - 'a']--;
}
```

**What it evaluates:** once the scan would exceed the fixed size, the oldest character leaves.

**Concrete result:** with length `2` and `right = 3`, outgoing index is `3 - 2 = 1`; `i` leaves as `b` enters, producing window `"db"` at indexes `2..3`.

**What fails without it:** counts accumulate for the whole prefix rather than one length-`pattern` window. Using `right - pattern.length() + 1` removes the new left boundary instead of the old one.

**Equivalent clearer form:** track a separate `left` pointer and move it when window length exceeds the pattern length.

**Debugger state at this block:**

For `right=0` and `1`, `right >= 2` is false; remove nothing. At `right=2`, it becomes `2 >= 2 → true`. The outgoing text position is `right - pattern.length()`, not the incoming letter’s alphabet index.

| right | outgoingIndex calculation | outgoing letter | frequency cell | change | window after removal |
| --- | --- | --- | --- | --- | --- |
| `2` | `2 - 2 = 0` | `e` | `4` | `1 → 0` | `id` |
| `3` | `3 - 2 = 1` | `i` | `8` | `1 → 0` | `db` |
| `4` | `4 - 2 = 2` | `d` | `3` | `1 → 0` | `ba` |

| Step | operation / index | right | outgoingIndex | Arrays.equals |
| --- | --- | --- | --- | --- |
| 8 | `remove e` | `2` | `0` | `not checked yet` |
| 11 | `remove i` | `3` | `1` | `not checked yet` |
| 14 | `remove d` | `4` | `2` | `not checked yet` |

Full arrays immediately after those same steps (`a..z`, indexes `0..25`):

| Step | neededCounts | windowCounts |
| --- | --- | --- |
| 8 | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,0,0,1,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| 11 | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| 14 | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |

Without removal, the array would count the full prefix `eidba`, including unwanted `e`, `i`, and `d`, rather than just the candidate `ba`.

### Block 5 — Compare exact counts and finish

```java
if (Arrays.equals(neededCounts, windowCounts)) {
    return true;
}
// The outer loop continues if the arrays differ.
```

After the outer loop finishes:

```java
return false;
```

**What it evaluates:** all 26 counts must agree; an early match ends the search.

**Concrete result:** `"ba"` has the same `a:1,b:1` signature as `"ab"` even though its order differs.

**What fails without exact comparison:** checking only required characters can accept a window with an extra character and a missing count elsewhere.

**Equivalent clearer form:** maintain a matched-count total for `O(1)` comparison, but scanning 26 cells is constant work and easier to verify.

**Debugger state at this block:**

`Arrays.equals` compares values in all 26 cells. It runs after the removal block, never between insertion and removal.

| Step | operation / index | right | outgoingIndex | Arrays.equals |
| --- | --- | --- | --- | --- |
| 4 | `compare arrays` | `0` | `out of scope` | `false` |
| 6 | `compare arrays` | `1` | `out of scope` | `false` |
| 9 | `compare arrays` | `2` | `out of scope` | `false` |
| 12 | `compare arrays` | `3` | `out of scope` | `false` |
| 15 | `compare arrays` | `4` | `out of scope` | `true` |

Full arrays immediately after those same steps (`a..z`, indexes `0..25`):

| Step | neededCounts | windowCounts |
| --- | --- | --- |
| 4 | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| 6 | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,0,0,0,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| 9 | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,0,0,1,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| 12 | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| 15 | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |

At `right=0`, a partial one-character window cannot match a two-character pattern: `a` and `b` are missing. At `right=4`, the window `ba` has exactly `[1,1,0,...,0]`, matching `neededCounts`; return `true` immediately. Indexes `5..7` are never processed. If every comparison were false (for example, `pattern="ab"`, `text="eidboaoo"`), `right` would reach `8`, `8 < 8` would be false, and execution would reach the final `return false` **outside the outer loop**.

## Reference approach

```java
static boolean checkInclusion(String pattern, String text) {
    if (pattern.length() > text.length()) {
        return false;
    }

    int[] neededCounts = new int[26];
    int[] windowCounts = new int[26];
    for (int index = 0; index < pattern.length(); index++) {
        neededCounts[pattern.charAt(index) - 'a']++;
    }

    for (int right = 0; right < text.length(); right++) {
        windowCounts[text.charAt(right) - 'a']++;
        if (right >= pattern.length()) {
            int outgoingIndex = right - pattern.length();
            windowCounts[text.charAt(outgoingIndex) - 'a']--;
        }
        if (Arrays.equals(neededCounts, windowCounts)) {
            return true;
        }
    }
    return false;
}
```

## Invariant and correctness

After removal, `windowCounts` describes exactly the last at-most-`pattern.length()` text characters ending at `right`. Count equality can occur only when the window has the full required length and contains every pattern character the required number of times. Every possible fixed-size window is checked once, so the result is correct.

## Complexity and alternatives

- Time: `O(26 × n)`, written `O(n)` because the alphabet size is constant.
- Space: `O(26)`, written `O(1)`.
- Generating all pattern permutations takes factorial time and is unnecessary.
- Sorting every candidate costs `O(n × m log m)` for pattern length `m`.

## Common pitfalls

- using a variable-size window even though a permutation length is fixed;
- comparing sets and losing duplicate counts;
- removing the wrong outgoing index;
- accepting a partial window before it reaches pattern length;
- confusing substring with non-contiguous subsequence.

## Practice after learning the concept

- Write here: [PermutationInStringPractice.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/slidingwindow/permutationinstring/PermutationInStringPractice.java)
- Reveal after attempting: [PermutationInStringSolution.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/slidingwindow/permutationinstring/PermutationInStringSolution.java)

## Questions and explained answers

<details>
<summary>1. Why is every candidate window exactly pattern.length characters?</summary>

A permutation neither adds nor removes characters. It only changes their order, so its length must equal the pattern length.

</details>

<details>
<summary>2. Why is a set insufficient for pattern aab?</summary>

A set records only that `a` and `b` exist; it loses the requirement for two copies of `a`. Frequency counts preserve multiplicity.

</details>

<details>
<summary>3. Which index leaves when right is 5 and the pattern length is 3?</summary>

Index `5 - 3 = 2` leaves. The resulting fixed window is indexes `3..5`, whose inclusive length is three.

</details>

## Stop/go

Proceed only when you can derive the outgoing index, explain why counts—not order—identify a permutation, trace the `"ba"` match, implement the practice method, and pass all checks.
