# Micro-Lesson 03 — Longest Repeating Character Replacement

## Question

> Given an uppercase string `text` and an integer `k`, return the length of the longest contiguous substring that can be made of one repeated character after replacing at most `k` characters.

Practice source: [Longest Repeating Character Replacement on LeetCode](https://leetcode.com/problems/longest-repeating-character-replacement/)

Examples:

- `"ABAB"`, `k = 2` → `4`.
- `"AABABBA"`, `k = 1` → `4`.
- `"AAAA"`, `k = 0` → `4`.

Assumptions follow LeetCode: uppercase English letters, `1 <= text.length <= 100,000`, and `0 <= k <= text.length`. The local runner also checks an empty defensive case. Target `O(n)` time and `O(1)` extra space.

## What you will learn

- how to calculate whether a window fits a replacement limit;
- why `window length - most frequent count` equals the required replacements;
- when exceeding a budget forces the left boundary to move;
- why the historical `maxFrequency` does not need to decrease for this maximum-length problem.

## Lesson status

- Learning status: `TODO`; initialized but not active
- Prerequisite: [Longest Substring Without Repeating Characters](../02-longest-substring-without-repeating/README.md)
- Primary idea: maintain a window whose replacement cost stays within `k`
- New terms: replacement budget, dominant character, max frequency

## First understand how to find the answer

For any active window:

```text
replacements needed = window length - count of the dominant character
```

The **dominant character** is the most frequent character in that window. Keep its copies and replace every other character.

1. Expand `right` and increment the incoming character's count.
2. Update `maxFrequency`, the largest count observed for a character in the maintained candidate.
3. Calculate `right - left + 1 - maxFrequency`.
4. If that cost exceeds `k`, remove the left character and move `left` once.
5. Record the maintained candidate length.

:::simulation
type: character-replacement
language: java
title: Character replacement · maintain the budget
text: AABABBA
k: 1
:::

## Work through the results

For `"ABAB"`, `k = 2`, the full length is `4`; either `A` or `B` appears twice, so `4 - 2 = 2` replacements are enough. Result: `4`.

For `"AABABBA"`, `k = 1`:

- `"AABA"` has length `4`, dominant `A` count `3`, and cost `4 - 3 = 1`; valid;
- extending to `"AABAB"` has length `5`, maximum count `3`, and cost `2`; over budget;
- the window shifts, and no later candidate longer than `4` fits one replacement;
- result: `4`.

For `"AAAA"`, `k = 0`, every active character already matches: `4 - 4 = 0`. Result: `4`.

## Debugger trace — `AABABBA`, replacement budget `1`

This trace follows the reference method below and the runnable [reference solution](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/slidingwindow/characterreplacement/CharacterReplacementSolution.java). Indexes `0..6` contain `A, A, B, A, B, B, A`. Throughout the run, `text = "AABABBA"` and `replacementsAllowed = 1`.

Initially, `left = 0`, `bestLength = 0`, `maxFrequency = 0`, and all 26 frequency cells are zero. `right` starts at `0`; the other local variables are assigned inside the loop.

### Before the budget check

Each row is immediately after adding the incoming character and updating `maxFrequency`, then calculating `replacementsNeeded`. `bestLength` here is the value from the previous iteration.

| `right` | Incoming | `incomingIndex` | `left` | Window | A count | B count | `maxFrequency` | `replacementsNeeded = right - left + 1 - maxFrequency` | `bestLength` | `> 1`? |
|---:|:---:|---:|---:|:---|---:|---:|---:|:---|---:|:---:|
| 0 | A | 0 | 0 | `A` | 1 | 0 | 1 | `0 - 0 + 1 - 1 = 0` | 0 | false |
| 1 | A | 0 | 0 | `AA` | 2 | 0 | 2 | `1 - 0 + 1 - 2 = 0` | 1 | false |
| 2 | B | 1 | 0 | `AAB` | 2 | 1 | 2 | `2 - 0 + 1 - 2 = 1` | 2 | false |
| 3 | A | 0 | 0 | `AABA` | 3 | 1 | 3 | `3 - 0 + 1 - 3 = 1` | 3 | false |
| 4 | B | 1 | 0 | `AABAB` | 3 | 2 | 3 | `4 - 0 + 1 - 3 = 2` | 4 | true |
| 5 | B | 1 | 1 | `ABABB` | 2 | 3 | 3 | `5 - 1 + 1 - 3 = 2` | 4 | true |
| 6 | A | 0 | 2 | `BABBA` | 2 | 3 | 3 | `6 - 2 + 1 - 3 = 2` | 4 | true |

### Full frequency array after every mutation

`frequency[0]` counts `A`, `frequency[1]` counts `B`, and indexes `2..25` count `C..Z`. For example, `'A' - 'A' = 0` and `'B' - 'A' = 1`. Every row below contains all 26 cells in index order; only the first two change for this input.

| Debugger step | Exact operation | Full `frequency` array |
|:---|:---|:---|
| Initial | `new int[26]` | `[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| right=0: add A | `frequency[0]++` | `[1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| right=1: add A | `frequency[0]++` | `[2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| right=2: add B | `frequency[1]++` | `[2,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| right=3: add A | `frequency[0]++` | `[3,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| right=4: add B | `frequency[1]++` | `[3,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| Remove A at left=0 | `frequency[0]--` | `[2,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| right=5: add B | `frequency[1]++` | `[2,3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| Remove A at left=1 | `frequency[0]--` | `[1,3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| right=6: add A | `frequency[0]++` | `[2,3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |
| Remove B at left=2 | `frequency[1]--` | `[2,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]` |

### After removal and the best-length update

`outgoingIndex` exists only inside the `if` block. A dash means the block did not run; it does not mean the variable contains `-1`.

| `right` | Removed | `outgoingIndex` | `left` after | Window after | `maxFrequency` | Stored `replacementsNeeded` | `bestLength = max(previous best, right - left + 1)` |
|---:|:---|---:|---:|:---|---:|---:|:---|
| 0 | — | — | 0 | `A` | 1 | 0 | `max(0, 1) = 1` |
| 1 | — | — | 0 | `AA` | 2 | 0 | `max(1, 2) = 2` |
| 2 | — | — | 0 | `AAB` | 2 | 1 | `max(2, 3) = 3` |
| 3 | — | — | 0 | `AABA` | 3 | 1 | `max(3, 4) = 4` |
| 4 | A at index 0 | 0 | 1 | `ABAB` | 3 | 2 | `max(4, 4) = 4` |
| 5 | A at index 1 | 0 | 2 | `BABB` | 3 | 2 | `max(4, 4) = 4` |
| 6 | B at index 2 | 1 | 3 | `ABBA` | 3 | 2 | `max(4, 4) = 4` |

Two debugger details explain the apparently surprising values:

- `replacementsNeeded` is calculated **before removal**. The code does not assign it again after `left++`, so it still displays `2` at the end of iterations 4, 5, and 6.
- `frequency` always counts the current window, but `maxFrequency` is historical. After iteration 4, `ABAB` has counts A=2, B=2, so its actual replacement cost is `4 - 2 = 2`; `maxFrequency` nevertheless remains `3`. Iteration 5 ends with `BABB`, whose actual cost is `4 - 3 = 1`. Iteration 6 ends with `ABBA`, whose actual cost is `4 - 2 = 2`. The algorithm keeps the already achieved length `4`; it does not assert that every maintained window is currently valid.

After iteration 6, `right++` makes `right = 7`. The condition `7 < text.length()` becomes `7 < 7`, which is false. The method returns `bestLength = 4`. A valid length-four window was `AABA`: keep its three A characters and replace its one B to obtain `AAAA`.

## Reference approach

```java
static int characterReplacement(String text, int replacementsAllowed) {
    int[] frequency = new int[26];
    int left = 0;
    int bestLength = 0;
    int maxFrequency = 0;

    for (int right = 0; right < text.length(); right++) {
        int incomingIndex = text.charAt(right) - 'A';
        frequency[incomingIndex]++;
        maxFrequency = Math.max(maxFrequency, frequency[incomingIndex]);

        int replacementsNeeded = right - left + 1 - maxFrequency;
        if (replacementsNeeded > replacementsAllowed) {
            int outgoingIndex = text.charAt(left) - 'A';
            frequency[outgoingIndex]--;
            left++;
        }

        bestLength = Math.max(bestLength, right - left + 1);
    }
    return bestLength;
}
```

## Block-by-block code walkthrough

### Block 1 — Store uppercase frequencies and boundaries

```java
int[] frequency = new int[26];
int left = 0;
int bestLength = 0;
int maxFrequency = 0;
```

**What it evaluates:** indexes `0..25` represent `A..Z`; the active candidate and best start empty.

**Why it is needed:** replacement cost depends on the dominant character's count.

**Concrete result:** character `C` uses index `'C' - 'A' = 2`.

**What fails without it:** counting only total length cannot distinguish `AAAA` from `ABCD`.

**Equivalent clearer form:** a `Map<Character, Integer>` supports larger alphabets but has more overhead.

### Block 2 — Add the incoming character

```java
int incomingIndex = text.charAt(right) - 'A';
frequency[incomingIndex]++;
maxFrequency = Math.max(maxFrequency, frequency[incomingIndex]);
```

**What it evaluates:** the new character's window count increases, and the largest useful frequency is preserved.

**Concrete result:** after reading `"AABA"`, the `A` count and `maxFrequency` are both `3`.

**What fails without it:** replacing `Math.max` with assignment makes `maxFrequency` fall whenever a less-frequent character arrives.

**Equivalent clearer form:** name the character first, then calculate its zero-based alphabet index.

### Block 3 — Calculate exact replacement cost

```java
int replacementsNeeded = right - left + 1 - maxFrequency;
```

**What it evaluates:** every character except the dominant copies must change.

**Concrete result:** `"AABA"` has length `4` and `maxFrequency = 3`, so `4 - 3 = 1` replacement.

**What fails without it:** subtracting `k` from the length does not describe the current character distribution.

**Equivalent clearer form:** `int windowLength = right - left + 1; int replacementsNeeded = windowLength - maxFrequency;`.

### Block 4 — Move left when the budget is exceeded

```java
if (replacementsNeeded > replacementsAllowed) {
    int outgoingIndex = text.charAt(left) - 'A';
    frequency[outgoingIndex]--;
    left++;
}
```

**What it evaluates:** an over-budget candidate drops its oldest character and keeps scanning at the same maintained size.

**Concrete result:** a length-`5` window with `maxFrequency = 3` needs `2`; when `k = 1`, the left character leaves.

**Why `if` is sufficient here:** each expansion grows the previous maintained window by only one, so one left movement restores the maintained candidate size. This formulation seeks the maximum length, not every currently exact valid window.

**What fails without it:** an over-budget window could grow indefinitely and produce a false maximum.

**Equivalent clearer form:** a `while` loop can enforce strict current-window validity, but then recalculating an exact decreasing maximum requires extra work.

### Block 5 — Preserve the best length and stale maximum

```java
bestLength = Math.max(bestLength, right - left + 1);
return bestLength;
```

**What it evaluates:** the largest maintained candidate length becomes the answer.

**Why stale `maxFrequency` is safe:** a smaller current maximum cannot prove that a new longer answer exists. The historical maximum records the frequency required to support the best length; it may delay shrinking but cannot increase the answer without a window that once had enough dominant copies.

**Concrete result:** if `maxFrequency` remains `3` after an `A` leaves, the algorithm may keep a size-`4` candidate; it does not claim size `5` unless a frequency of at least `4` makes that possible with `k = 1`.

**What fails without the best variable:** returning the final candidate ignores a larger earlier answer.

## Invariant and correctness

The maintained candidate never grows beyond a length supported by some observed dominant count and the replacement budget. Whenever adding one character would require more than `k` replacements, `left` advances so the candidate cannot grow. Thus every achievable larger length is recorded, and no impossible larger length is accepted.

## Complexity and alternatives

- Time: `O(n)`; both boundaries move only right.
- Space: `O(1)` because the frequency array always has 26 cells.
- Brute force: test every substring and its frequencies, `O(n² × alphabet)`.
- Recalculating the maximum of 26 counts each iteration is still `O(n)` for a fixed alphabet, but the historical maximum avoids that repeated scan.

## Common pitfalls

- using the number of distinct characters instead of replacement cost;
- forgetting the inclusive `+ 1` in window length;
- shrinking when `replacementsNeeded == k` even though the budget allows it;
- trying to decrease `maxFrequency` without recomputing all counts;
- assuming replacements may reorder characters—they change values only.

## Practice after learning the concept

- Write here: [CharacterReplacementPractice.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/slidingwindow/characterreplacement/CharacterReplacementPractice.java)
- Reveal after attempting: [CharacterReplacementSolution.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/slidingwindow/characterreplacement/CharacterReplacementSolution.java)

## Questions and explained answers

<details>
<summary>1. Why is replacement cost window length minus max frequency?</summary>

Keep every occurrence of the dominant character. Every remaining position must be changed to that character, so those remaining positions are exactly the required replacements.

</details>

<details>
<summary>2. When must the left boundary move?</summary>

Only when the calculated replacements exceed `k`. Equality is valid because the full budget may be used.

</details>

<details>
<summary>3. Why can maxFrequency remain historical?</summary>

The task asks only for the maximum length. A stale value can postpone a shift at an already achievable size, but it cannot support growth to a new larger size without a correspondingly larger observed frequency.

</details>

## Stop/go

Proceed only when you can derive the budget formula, trace `"AABABBA"` with `k = 1`, explain the stale maximum, implement the practice method, and pass all checks.
