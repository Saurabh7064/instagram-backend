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

## Debugger trace — every variable and collection

Inputs `pattern = "ab"`, `text = "eidbaooo"`. The guard `2 > 8` is false. Initially `neededCounts` and `windowCounts` each have 26 zero cells. Array indexes `0..25` map to `a..z`; every cell is shown below.

The rows follow execution order. Collection contents are shown after the named operation; `—` means a local variable has not been assigned or is outside its scope. Sets and maps are displayed in readable order, not a guaranteed Java iteration order.

| Step | operation / index | right | outgoingIndex | Arrays.equals |
| --- | --- | --- | --- | --- |
| 1 | `build needed: index=0, char=a` | `—` | `—` | `—` |
| 2 | `build needed: index=1, char=b` | `—` | `—` | `—` |
| 3 | `add e` | `0` | `—` | `not checked yet` |
| 4 | `compare arrays` | `0` | `out of scope` | `false` |
| 5 | `add i` | `1` | `—` | `not checked yet` |
| 6 | `compare arrays` | `1` | `out of scope` | `false` |
| 7 | `add d` | `2` | `—` | `not checked yet` |
| 8 | `remove e` | `2` | `0` | `not checked yet` |
| 9 | `compare arrays` | `2` | `out of scope` | `false` |
| 10 | `add b` | `3` | `—` | `not checked yet` |
| 11 | `remove i` | `3` | `1` | `not checked yet` |
| 12 | `compare arrays` | `3` | `out of scope` | `false` |
| 13 | `add a` | `4` | `—` | `not checked yet` |
| 14 | `remove d` | `4` | `2` | `not checked yet` |
| 15 | `compare arrays` | `4` | `out of scope` | `true` |

### Full frequency arrays at each step

Step numbers match the table above; each row shows the complete collection state at that same point.

| Step | neededCounts | windowCounts |
| --- | --- | --- |
| 1 | `[1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| 2 | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| 3 | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| 4 | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| 5 | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,0,0,0,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| 6 | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,0,0,0,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| 7 | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,0,0,1,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| 8 | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,0,0,1,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| 9 | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,0,0,1,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| 10 | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,1,0,1,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| 11 | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| 12 | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| 13 | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[1,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| 14 | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| 15 | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |

At `right=4`, adding `a` temporarily gives counts for `a`, `b`, and `d`. Removal uses `outgoingIndex = 4 - 2 = 2`, so the `d` count decreases to zero. Now both full arrays contain exactly one `a` and one `b`; return `true` for substring `ba` at indexes `3..4`. The loop never processes indexes `5..7`. `outgoingIndex` exists only inside its removal block; it is not a character-code index.

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

## Block-by-block code walkthrough

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

### Block 3 — Add each incoming character

```java
for (int right = 0; right < text.length(); right++) {
    windowCounts[text.charAt(right) - 'a']++;
```

**What it evaluates:** `right` expands the candidate and increases exactly one count.

**Concrete result:** reading the `b` at index `3` of `"eidbaooo"` changes the active `b` count from `0` to `1`.

**What fails without it:** comparing before insertion evaluates the previous window rather than the one ending at `right`.

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

### Block 5 — Compare exact counts and finish

```java
if (Arrays.equals(neededCounts, windowCounts)) {
    return true;
}
return false;
```

**What it evaluates:** all 26 counts must agree; an early match ends the search.

**Concrete result:** `"ba"` has the same `a:1,b:1` signature as `"ab"` even though its order differs.

**What fails without exact comparison:** checking only required characters can accept a window with an extra character and a missing count elsewhere.

**Equivalent clearer form:** maintain a matched-count total for `O(1)` comparison, but scanning 26 cells is constant work and easier to verify.

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
