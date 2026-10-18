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

We are executing `checkInclusion("ab", "eidbaooo")`. `pattern` is `"ab"` (length `2`); `text` is `"eidbaooo"` (length `8`). Read the following as a debugger executing one statement at a time. Each iteration's explanation is immediately followed by its values.

### Block 1 — Check whether the pattern can fit

```java
if (pattern.length() > text.length()) {
    return false;
}
```

First read the two lengths: `pattern.length()` is `2`, and `text.length()` is `8`. Evaluate `2 > 8`: **false**. Therefore skip the `if` body, including `return false`, and continue to array creation. We skip the body because the pattern is **shorter** than the text, so a matching substring could fit. This does not yet prove a match exists.

If the pattern were `"abcd"` and the text `"abc"`, the same check would be `4 > 3 → true`; the method would return `false` immediately because rearranging four characters cannot fit into three positions.

| pattern | text | length comparison | branch | next statement |
| --- | --- | --- | --- | --- |
| `ab` | `eidbaooo` | `2 > 8 → false` | `skip return false` | `create neededCounts` |

### Block 2 — Create space to remember character counts

```java
int[] neededCounts = new int[26];
int[] windowCounts = new int[26];
```

Create `neededCounts` to remember how many copies of each letter the pattern requires. Create `windowCounts` to remember how many copies are in the text window we are checking. They are separate arrays; changing one never changes the other. Java initializes every cell to zero.

There are 26 cells because the input contains only lowercase English letters. Cell `0` means `a`, cell `1` means `b`, through cell `25` meaning `z`. No character has been counted yet.

| array | all 26 cells after allocation |
| --- | --- |
| `neededCounts` | `[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| `windowCounts` | `[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |

### Block 3 — Count the pattern, one iteration at a time

```java
for (int index = 0; index < pattern.length(); index++) {
    neededCounts[pattern.charAt(index) - 'a']++;
}
```

**Pattern iteration 1: `index = 0`.**

Check `0 < 2`: true, so enter the loop. Read `pattern.charAt(0)`, which is `'a'`. Calculate `'a' - 'a' = 0` to select that letter's array cell. Increment `neededCounts[0]` from `0` to `1`: the pattern requires 1 copy of `a`. No text character has entered, so `windowCounts` is still all zero. Finally execute `index++`, making `index = 1`.

| index before | condition | character | array cell | neededCounts after | windowCounts after | index after |
| --- | --- | --- | --- | --- | --- | --- |
| `0` | `0 < 2 → true` | `a` | `0` | `[1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `1` |

**Pattern iteration 2: `index = 1`.**

Check `1 < 2`: true, so enter the loop. Read `pattern.charAt(1)`, which is `'b'`. Calculate `'b' - 'a' = 1` to select that letter's array cell. Increment `neededCounts[1]` from `0` to `1`: the pattern requires 1 copy of `b`. No text character has entered, so `windowCounts` is still all zero. Finally execute `index++`, making `index = 2`.

| index before | condition | character | array cell | neededCounts after | windowCounts after | index after |
| --- | --- | --- | --- | --- | --- | --- |
| `1` | `1 < 2 → true` | `b` | `1` | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `2` |

**Finish the pattern loop: `index = 2`.** Evaluate `2 < 2`: false. Skip the loop body; `pattern.charAt(2)` is never called. `neededCounts` now means “one `a`, one `b`, zero other letters.” This array stays unchanged during the text scan. Counting copies rather than just membership matters: for `"aab"`, the first two iterations would change cell `a` from `0 → 1 → 2`, then the third changes cell `b` from `0 → 1`.

### Block 4 — Scan the text, completing every statement in each iteration

```java
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
```

The loop adds the incoming character, removes the oldest one if the window has grown too large, then compares all 26 counts. `outgoingIndex` is a position in the text, not a letter's array cell. These are different indexes: at `right=2`, outgoing text position `0` holds `e`, whose frequency-array cell is `4`.

**Text iteration 1: `right = 0`.**

1. Check `0 < 8`: true, so enter the loop. Read `text.charAt(0)`, which is `'e'`. Calculate `'e' - 'a' = 4` and increment `windowCounts[4]` from `0` to `1`.
2. Evaluate `right >= pattern.length()`: `0 >= 2 → false`. Skip the entire removal branch: no `outgoingIndex` is assigned and no count is decremented. The window is `e`.
3. Execute `Arrays.equals(neededCounts, windowCounts)`: **false**, because we have an e, but need one a and one b. Skip `return true`. Execute `right++`, making `right = 1`, then start the next iteration.

| right before | incoming/cell | removal condition | outgoingIndex | window at comparison | Arrays.equals | next action |
| --- | --- | --- | --- | --- | --- | --- |
| `0` | `e / 4` | `0 >= 2 → false` | `not assigned` | `e` | `false` | `right=1; continue` |

| phase | neededCounts (unchanged) | windowCounts |
| --- | --- | --- |
| `before insertion` | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| `after insertion` | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| `removal skipped; at comparison` | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |

**Text iteration 2: `right = 1`.**

1. Check `1 < 8`: true, so enter the loop. Read `text.charAt(1)`, which is `'i'`. Calculate `'i' - 'a' = 8` and increment `windowCounts[8]` from `0` to `1`.
2. Evaluate `right >= pattern.length()`: `1 >= 2 → false`. Skip the entire removal branch: no `outgoingIndex` is assigned and no count is decremented. The window is `ei`.
3. Execute `Arrays.equals(neededCounts, windowCounts)`: **false**, because we have e and i, but need a and b. Skip `return true`. Execute `right++`, making `right = 2`, then start the next iteration.

| right before | incoming/cell | removal condition | outgoingIndex | window at comparison | Arrays.equals | next action |
| --- | --- | --- | --- | --- | --- | --- |
| `1` | `i / 8` | `1 >= 2 → false` | `not assigned` | `ei` | `false` | `right=2; continue` |

| phase | neededCounts (unchanged) | windowCounts |
| --- | --- | --- |
| `before insertion` | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| `after insertion` | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,0,0,0,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| `removal skipped; at comparison` | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,0,0,0,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |

**Text iteration 3: `right = 2`.**

1. Check `2 < 8`: true, so enter the loop. Read `text.charAt(2)`, which is `'d'`. Calculate `'d' - 'a' = 3` and increment `windowCounts[3]` from `0` to `1`.
2. Evaluate `right >= pattern.length()`: `2 >= 2 → true`. Enter the removal branch. Calculate `outgoingIndex = 2 - 2 = 0`; read `text.charAt(0) = 'e'`; map it to cell `4`. Decrement `windowCounts[4]` from `1` to `0`. The window is now `id`.
3. Execute `Arrays.equals(neededCounts, windowCounts)`: **false**, because we have i and d, but need a and b. Skip `return true`. Execute `right++`, making `right = 3`, then start the next iteration.

| right before | incoming/cell | removal condition | outgoingIndex | window at comparison | Arrays.equals | next action |
| --- | --- | --- | --- | --- | --- | --- |
| `2` | `d / 3` | `2 >= 2 → true` | `0` | `id` | `false` | `right=3; continue` |

| phase | neededCounts (unchanged) | windowCounts |
| --- | --- | --- |
| `before insertion` | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,0,0,0,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| `after insertion` | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,0,0,1,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| `after removal` | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,0,0,1,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |

**Text iteration 4: `right = 3`.**

1. Check `3 < 8`: true, so enter the loop. Read `text.charAt(3)`, which is `'b'`. Calculate `'b' - 'a' = 1` and increment `windowCounts[1]` from `0` to `1`.
2. Evaluate `right >= pattern.length()`: `3 >= 2 → true`. Enter the removal branch. Calculate `outgoingIndex = 3 - 2 = 1`; read `text.charAt(1) = 'i'`; map it to cell `8`. Decrement `windowCounts[8]` from `1` to `0`. The window is now `db`.
3. Execute `Arrays.equals(neededCounts, windowCounts)`: **false**, because we have d and b; b matches, but a is missing and d is extra. Skip `return true`. Execute `right++`, making `right = 4`, then start the next iteration.

| right before | incoming/cell | removal condition | outgoingIndex | window at comparison | Arrays.equals | next action |
| --- | --- | --- | --- | --- | --- | --- |
| `3` | `b / 1` | `3 >= 2 → true` | `1` | `db` | `false` | `right=4; continue` |

| phase | neededCounts (unchanged) | windowCounts |
| --- | --- | --- |
| `before insertion` | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,0,0,1,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| `after insertion` | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,1,0,1,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| `after removal` | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |

**Text iteration 5: `right = 4`.**

1. Check `4 < 8`: true, so enter the loop. Read `text.charAt(4)`, which is `'a'`. Calculate `'a' - 'a' = 0` and increment `windowCounts[0]` from `0` to `1`.
2. Evaluate `right >= pattern.length()`: `4 >= 2 → true`. Enter the removal branch. Calculate `outgoingIndex = 4 - 2 = 2`; read `text.charAt(2) = 'd'`; map it to cell `3`. Decrement `windowCounts[3]` from `1` to `0`. The window is now `ba`.
3. Execute `Arrays.equals(neededCounts, windowCounts)`: **true**, because we have exactly one a and one b, with zero counts for all other letters. The `if` body runs and returns `true` immediately. Skip `right++`; do not read the remaining `ooo`.

| right before | incoming/cell | removal condition | outgoingIndex | window at comparison | Arrays.equals | next action |
| --- | --- | --- | --- | --- | --- | --- |
| `4` | `a / 0` | `4 >= 2 → true` | `2` | `ba` | `true` | `return true; stop` |

| phase | neededCounts (unchanged) | windowCounts |
| --- | --- | --- |
| `before insertion` | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| `after insertion` | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[1,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| `after removal` | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |

### Block 5 — Return false only if the text scan finishes without a match

```java
return false;
```

For this execution, the earlier return at `right=4` has already ended the method. This line is **not reached**. The returned value is `true`, supported by text positions `3..4`, whose substring `"ba"` contains exactly the same counts as pattern `"ab"`.

For a nonmatching text such as `"eidboaoo"`, each comparison is false. After its last iteration at `right=7`, `right++` makes `right=8`; the loop test `8 < 8` is false. Only that exhausted-loop path reaches this final `return false`. Without this terminal return, the boolean method would have a path with no result and would not compile.

## Reference approach

```java
// Input: pattern = "ab"; text = "eidbaooo"
// Expected return: true (window "ba")

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
