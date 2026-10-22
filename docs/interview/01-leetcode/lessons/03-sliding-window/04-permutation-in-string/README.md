# Micro-Lesson 04 — Permutation in String

## Question

> Given lowercase strings `pattern` and `text`, return `true` if `text` contains a contiguous permutation of `pattern`; otherwise return `false`.

Practice source: [Permutation in String on LeetCode](https://leetcode.com/problems/permutation-in-string/)

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

## First understand the idea

The key idea is to compare character counts for a fixed-size substring, because changing order does not change how many copies of each letter it contains.

Use `pattern="ab"`, `text="eidbaooo"`. Let `neededCounts` store the required copies from the pattern: a:1 and b:1. Let `windowCounts` store copies in the current text window; `right` is the incoming text position. The required window length is 2, the pattern's length.

The first full window ei has e:1 and i:1, so it does not match. Sliding one place replaces e with d, producing id; next replace i with b, producing db. Then replace d with a, producing ba at indexes 3..4. It supplies a:1 and b:1, exactly the required counts, so return true. The full frequency arrays below also show the zero cells.

A **permutation** is an arrangement with the same characters and copy counts. In general, add the incoming character, remove the character that is now too far behind, and compare the full counts. The outgoing text index is `right-pattern.length()`; at right=4 it is `4-2=2`, holding d. This position in the text is different from the frequency-array cell selected by that character.

:::simulation
type: permutation-in-string
language: java
title: Permutation in String · fixed count signature
first: ab
second: eidbaooo
:::

## Block-by-block code walkthrough

Original snapshots are retained. Where a value changes several times, its cell shows the sequence ending at the final value.

We are executing `checkInclusion("ab", "eidbaooo")`. `pattern` is `"ab"` (length `2`); `text` is `"eidbaooo"` (length `8`). Read the following as a debugger executing one statement at a time. Each iteration's explanation is immediately followed by its values.

### Block 1 — Check whether the pattern can fit

```java
if (pattern.length() > text.length()) {
    return false;
}
```

- Inputs: `pattern="ab"`, `text="eidbaooo"`; lengths are `2` and `8`.
- **❌ `if (pattern.length() > text.length())`:** `2 > 8 → false`; skip `return false`.
- The pattern can fit, so create the count arrays next; fitting alone does not prove a match.
- A longer pattern cannot fit in the text; that is the early-return path this guard prevents.

| pattern | text | length comparison | branch | next statement |
| --- | --- | --- | --- | --- |
| `ab` | `eidbaooo` | `❌ 2 > 8 → false` | `skip return false` | `create neededCounts` |

### Block 2 — Create space to remember character counts

```java
int[] neededCounts = new int[26];
int[] windowCounts = new int[26];
```

- `int[] neededCounts = new int[26]` stores the copies each pattern letter requires.
- `int[] windowCounts = new int[26]` stores the copies in the current text window.
- Both arrays start with 26 zeros; cell `0` means `a`, cell `1` means `b`, through cell `25` meaning `z`. The arrays are independent.

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

- **✅ `for (index < pattern.length())`:** `0 < 2 → true`; enter the pattern loop.
- `pattern.charAt(index)` reads index `0` → `'a'`; `'a' - 'a' = 0` selects the cell.
- `neededCounts[pattern.charAt(index) - 'a']++` changes `neededCounts[0]`: `0 → 1`; require one `a`. `windowCounts` stays all zero.
- `index++`: `0 → 1`; **✅ `for (index < pattern.length())`:** `1 < 2 → true` next.

| index before | condition | character | array cell | neededCounts after | windowCounts after | index after |
| --- | --- | --- | --- | --- | --- | --- |
| `0` | `✅ 0 < 2 → true` | `a` | `0` | `[1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `1` |

**Pattern iteration 2: `index = 1`.**

- **✅ `for (index < pattern.length())`:** `1 < 2 → true`; enter the pattern loop.
- `pattern.charAt(index)` reads index `1` → `'b'`; `'b' - 'a' = 1` selects the cell.
- `neededCounts[pattern.charAt(index) - 'a']++` changes `neededCounts[1]`: `0 → 1`; require one `b`. `windowCounts` stays all zero.
- `index++`: `1 → 2`; **❌ `for (index < pattern.length())`:** `2 < 2 → false`; exit the pattern loop.

| index before | condition | character | array cell | neededCounts after | windowCounts after | index after |
| --- | --- | --- | --- | --- | --- | --- |
| `1` | `✅ 1 < 2 → true` | `b` | `1` | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `2` |

**Finish the pattern loop: `index = 2`.**

- **❌ `for (index < pattern.length())`:** `2 < 2 → false`; skip the body and never call `pattern.charAt(2)`.
- `neededCounts` requires one `a`, one `b`, and zero other letters; it stays unchanged during the text scan.
- Counting records how many copies are required; a set would record only membership.

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

- **✅ `for (right < text.length())`:** `0 < 8 → true`; enter the text loop.
- `text.charAt(right)` reads index `0` → `'e'`; `'e' - 'a' = 4` selects the cell.
- `windowCounts[text.charAt(right) - 'a']++` changes `windowCounts[4]`: `0 → 1`.
- **❌ `if (right >= pattern.length())`:** `0 >= 2 → false`; skip `outgoingIndex` creation and the decrement.
- **State at comparison:** `right=0`; `outgoingIndex` is not assigned; window `'e'`; the adjacent table shows all 26 counts.
- **❌ `if (Arrays.equals(neededCounts, windowCounts))`:** `false`; need a and b; e is extra.
- Skip `return true`; `right++`: `0 → 1`; **✅ `for (right < text.length())`:** `1 < 8 → true` next.

| right before | incoming/cell | removal condition | outgoingIndex | window at comparison | Arrays.equals | next action |
| --- | --- | --- | --- | --- | --- | --- |
| `0` | `e / 4` | `❌ 0 >= 2 → false` | `not assigned` | `e` | `❌ false` | `right++: 0 → 1; ✅ for (right < text.length()): 1 < 8 → true; continue` |

| phase | neededCounts (unchanged) | windowCounts |
| --- | --- | --- |
| `before insertion` | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| `after insertion` | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| `removal skipped; at comparison` | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |

**Text iteration 2: `right = 1`.**

- **✅ `for (right < text.length())`:** `1 < 8 → true`; enter the text loop.
- `text.charAt(right)` reads index `1` → `'i'`; `'i' - 'a' = 8` selects the cell.
- `windowCounts[text.charAt(right) - 'a']++` changes `windowCounts[8]`: `0 → 1`.
- **❌ `if (right >= pattern.length())`:** `1 >= 2 → false`; skip `outgoingIndex` creation and the decrement.
- **State at comparison:** `right=1`; `outgoingIndex` is not assigned; window `'ei'`; the adjacent table shows all 26 counts.
- **❌ `if (Arrays.equals(neededCounts, windowCounts))`:** `false`; need a and b; e and i are extra.
- Skip `return true`; `right++`: `1 → 2`; **✅ `for (right < text.length())`:** `2 < 8 → true` next.

| right before | incoming/cell | removal condition | outgoingIndex | window at comparison | Arrays.equals | next action |
| --- | --- | --- | --- | --- | --- | --- |
| `1` | `i / 8` | `❌ 1 >= 2 → false` | `not assigned` | `ei` | `❌ false` | `right++: 1 → 2; ✅ for (right < text.length()): 2 < 8 → true; continue` |

| phase | neededCounts (unchanged) | windowCounts |
| --- | --- | --- |
| `before insertion` | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| `after insertion` | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,0,0,0,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| `removal skipped; at comparison` | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,0,0,0,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |

**Text iteration 3: `right = 2`.**

- **✅ `for (right < text.length())`:** `2 < 8 → true`; enter the text loop.
- `text.charAt(right)` reads index `2` → `'d'`; `'d' - 'a' = 3` selects the cell.
- `windowCounts[text.charAt(right) - 'a']++` changes `windowCounts[3]`: `0 → 1`.
- **✅ `if (right >= pattern.length())`:** `2 >= 2 → true`; enter the removal branch.
- `int outgoingIndex = right - pattern.length()`: `2 - 2 = 0`.
- `text.charAt(outgoingIndex)` reads index `0` → `'e'`; `'e' - 'a' = 4` selects its count cell.
- `windowCounts[text.charAt(outgoingIndex) - 'a']--` changes `windowCounts[4]`: `1 → 0`.
- **State at comparison:** `right=2`, `outgoingIndex=0`; window `'id'`; the adjacent table shows all 26 counts.
- **❌ `if (Arrays.equals(neededCounts, windowCounts))`:** `false`; need a and b; i and d are extra.
- Skip `return true`; `right++`: `2 → 3`; **✅ `for (right < text.length())`:** `3 < 8 → true` next.

| right before | incoming/cell | removal condition | outgoingIndex | window at comparison | Arrays.equals | next action |
| --- | --- | --- | --- | --- | --- | --- |
| `2` | `d / 3` | `✅ 2 >= 2 → true` | `0` | `id` | `❌ false` | `right++: 2 → 3; ✅ for (right < text.length()): 3 < 8 → true; continue` |

| phase | neededCounts (unchanged) | windowCounts |
| --- | --- | --- |
| `before insertion` | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,0,0,0,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| `after insertion` | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,0,0,1,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| `after removal` | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,0,0,1,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |

**Text iteration 4: `right = 3`.**

- **✅ `for (right < text.length())`:** `3 < 8 → true`; enter the text loop.
- `text.charAt(right)` reads index `3` → `'b'`; `'b' - 'a' = 1` selects the cell.
- `windowCounts[text.charAt(right) - 'a']++` changes `windowCounts[1]`: `0 → 1`.
- **✅ `if (right >= pattern.length())`:** `3 >= 2 → true`; enter the removal branch.
- `int outgoingIndex = right - pattern.length()`: `3 - 2 = 1`.
- `text.charAt(outgoingIndex)` reads index `1` → `'i'`; `'i' - 'a' = 8` selects its count cell.
- `windowCounts[text.charAt(outgoingIndex) - 'a']--` changes `windowCounts[8]`: `1 → 0`.
- **State at comparison:** `right=3`, `outgoingIndex=1`; window `'db'`; the adjacent table shows all 26 counts.
- **❌ `if (Arrays.equals(neededCounts, windowCounts))`:** `false`; b matches; a is missing and d is extra.
- Skip `return true`; `right++`: `3 → 4`; **✅ `for (right < text.length())`:** `4 < 8 → true` next.

| right before | incoming/cell | removal condition | outgoingIndex | window at comparison | Arrays.equals | next action |
| --- | --- | --- | --- | --- | --- | --- |
| `3` | `b / 1` | `✅ 3 >= 2 → true` | `1` | `db` | `❌ false` | `right++: 3 → 4; ✅ for (right < text.length()): 4 < 8 → true; continue` |

| phase | neededCounts (unchanged) | windowCounts |
| --- | --- | --- |
| `before insertion` | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,0,0,1,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| `after insertion` | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,1,0,1,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| `after removal` | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |

**Text iteration 5: `right = 4`.**

- **✅ `for (right < text.length())`:** `4 < 8 → true`; enter the text loop.
- `text.charAt(right)` reads index `4` → `'a'`; `'a' - 'a' = 0` selects the cell.
- `windowCounts[text.charAt(right) - 'a']++` changes `windowCounts[0]`: `0 → 1`.
- **✅ `if (right >= pattern.length())`:** `4 >= 2 → true`; enter the removal branch.
- `int outgoingIndex = right - pattern.length()`: `4 - 2 = 2`.
- `text.charAt(outgoingIndex)` reads index `2` → `'d'`; `'d' - 'a' = 3` selects its count cell.
- `windowCounts[text.charAt(outgoingIndex) - 'a']--` changes `windowCounts[3]`: `1 → 0`.
- **State at comparison:** `right=4`, `outgoingIndex=2`; window `'ba'`; the adjacent table shows all 26 counts.
- **✅ `if (Arrays.equals(neededCounts, windowCounts))`:** `true`; one a and one b match; every other count is zero.
- Execute `return true`; skip `right++`, further loop checks, and the remaining `ooo`.

| right before | incoming/cell | removal condition | outgoingIndex | window at comparison | Arrays.equals | next action |
| --- | --- | --- | --- | --- | --- | --- |
| `4` | `a / 0` | `✅ 4 >= 2 → true` | `2` | `ba` | `✅ true` | `return true; stop` |

| phase | neededCounts (unchanged) | windowCounts |
| --- | --- | --- |
| `before insertion` | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[0,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| `after insertion` | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[1,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| `after removal` | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` | `[1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |

### Block 5 — Return false only if the text scan finishes without a match

```java
return false;
```

- `return false` is **not reached**: `return true` at `right=4` has already ended this execution.
- Text positions `3..4` form `"ba"`; counts `a=1`, `b=1`, others `0` match `pattern="ab"`.
- Later loop checks are `Skipped`, including the terminating check; the early return prevents them.
- In general, only a scan that exhausts the text without a match reaches `return false`; that return gives the boolean method a result on the exhausted-scan path.

## Reference approach

```java
// pattern index: 0 1
// pattern      : a b
// text index: 0 1 2 3 4 5 6 7
// text      : e i d b a o o o
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
