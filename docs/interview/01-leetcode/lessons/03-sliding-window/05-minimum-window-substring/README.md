# Micro-Lesson 05 — Minimum Window Substring

## Question

> Given strings `source` and `target`, return the shortest contiguous substring of `source` containing every character from `target`, including duplicate counts. Return `""` when no such window exists.

Practice source: [Minimum Window Substring on LeetCode](https://leetcode.com/problems/minimum-window-substring/)

Examples:

- `source = "ADOBECODEBANC"`, `target = "ABC"` → `"BANC"`.
- `source = "a"`, `target = "a"` → `"a"`.
- `source = "a"`, `target = "aa"` → `""`.

Assumptions follow LeetCode: both inputs contain English letters, `1 <= source.length, target.length <= 100,000`, and the answer is unique when it exists. Target `O(n + m)` time and `O(alphabet)` space.

## What you will learn

- how to expand until every required count is satisfied;
- how to shrink while the window remains valid;
- how `formedGroups` avoids comparing every count after every movement;
- why the best answer is recorded before removing the left character.

## Lesson status

- Learning status: `TODO`; initialized but not active
- Prerequisite: [Permutation in String](../04-permutation-in-string/README.md)
- Primary idea: expand to become valid, then shrink to become minimal
- New terms: required group, formed group, covering window

## First understand how to find the answer

A **required group** is one distinct target character with its required count. For target `"AABC"`, the groups are `A:2`, `B:1`, and `C:1`; therefore `requiredGroups = 3`, not `4`.

A group becomes **formed** only when the active window reaches its required count.

1. Count `target` and calculate its number of distinct required groups.
2. Expand `right`, updating the incoming count and `formedGroups`.
3. When `formedGroups == requiredGroups`, the window covers the target.
4. Record it if it is shorter, then remove the left character.
5. Continue shrinking while valid; when a required count falls short, resume expansion.

:::simulation
type: minimum-window-substring
language: java
title: Minimum Window · expand to valid, shrink to minimal
first: ADOBECODEBANC
second: ABC
:::

## Work through the results

For `"ADOBECODEBANC"` and `"ABC"`:

- expansion first satisfies all counts at index `5`, producing `"ADOBEC"` length `6`;
- shrinking removes the leading `A`, so the window becomes invalid and expansion resumes;
- later `"CODEBA"` is valid with length `6`, then left moves through irrelevant characters until removing `C` would break validity;
- at the final `C`, shrinking yields `"BANC"` at indexes `9..12`, length `12 - 9 + 1 = 4`;
- no length below `4` can contain three required characters here, so return `"BANC"`.

For `"a"` and `"a"`, the single character forms the only required group, producing `"a"`.

For `"a"` and `"aa"`, the window count reaches only `1` while `2` are needed. `formedGroups` never reaches `requiredGroups`, so return `""`.

## Reference approach

```java
static String minWindow(String source, String target) {
    Map<Character, Integer> neededCounts = new HashMap<>();
    for (char value : target.toCharArray()) {
        neededCounts.merge(value, 1, Integer::sum);
    }

    Map<Character, Integer> windowCounts = new HashMap<>();
    int requiredGroups = neededCounts.size();
    int formedGroups = 0;
    int left = 0;
    int bestStart = 0;
    int bestLength = Integer.MAX_VALUE;

    for (int right = 0; right < source.length(); right++) {
        char incoming = source.charAt(right);
        windowCounts.merge(incoming, 1, Integer::sum);
        if (neededCounts.containsKey(incoming)
                && windowCounts.get(incoming).equals(neededCounts.get(incoming))) {
            formedGroups++;
        }

        while (formedGroups == requiredGroups) {
            int candidateLength = right - left + 1;
            if (candidateLength < bestLength) {
                bestStart = left;
                bestLength = candidateLength;
            }

            char outgoing = source.charAt(left);
            windowCounts.merge(outgoing, -1, Integer::sum);
            left++;
            if (neededCounts.containsKey(outgoing)
                    && windowCounts.get(outgoing) < neededCounts.get(outgoing)) {
                formedGroups--;
            }
        }
    }

    return bestLength == Integer.MAX_VALUE
            ? ""
            : source.substring(bestStart, bestStart + bestLength);
}
```

## Block-by-block code walkthrough

### Block 1 — Count target requirements

```java
Map<Character, Integer> neededCounts = new HashMap<>();
for (char value : target.toCharArray()) {
    neededCounts.merge(value, 1, Integer::sum);
}
```

**What it evaluates:** `merge` inserts count `1` for a new character or adds `1` to its existing count.

**Concrete result:** target `"AABC"` becomes `{A=2, B=1, C=1}`.

**Why it is needed:** a covering window must satisfy duplicates, not merely contain each distinct character once.

**What fails without it:** a set would accept `"ABC"` for target `"AABC"`, even though one `A` is missing.

**Equivalent clearer form:** `put(value, getOrDefault(value, 0) + 1)` performs the same update.

### Block 2 — Initialize validity and best-answer state

```java
Map<Character, Integer> windowCounts = new HashMap<>();
int requiredGroups = neededCounts.size();
int formedGroups = 0;
int left = 0;
int bestStart = 0;
int bestLength = Integer.MAX_VALUE;
```

**What it evaluates:** the active window is empty, no requirement is formed, and no answer has been found.

**Concrete result:** target `"AABC"` has `requiredGroups = 3` because there are three distinct keys.

**Why `Integer.MAX_VALUE`:** it is a sentinel meaning “no candidate yet”; every real window length is smaller.

**What fails without it:** initializing `bestLength = 0` prevents any positive candidate from being shorter.

**Equivalent clearer form:** store a nullable best range, but start/length avoids creating range objects.

### Block 3 — Expand and form a group at exact equality

```java
char incoming = source.charAt(right);
windowCounts.merge(incoming, 1, Integer::sum);
if (neededCounts.containsKey(incoming)
        && windowCounts.get(incoming).equals(neededCounts.get(incoming))) {
    formedGroups++;
}
```

**What it evaluates:** the incoming count changes; a required group forms only when its count first reaches the exact requirement.

**Concrete result:** if `A:2` is needed, window count `1 → 2` increments `formedGroups`; `2 → 3` does not increment it again.

**What fails without exact equality:** using `>=` increments the same group repeatedly for surplus copies and can declare an invalid window valid.

**Equivalent clearer form:** compare primitive `intValue()` values; `equals` safely compares the boxed integer values here.

### Block 4 — Shrink every valid covering window

```java
while (formedGroups == requiredGroups) {
    int candidateLength = right - left + 1;
    if (candidateLength < bestLength) {
        bestStart = left;
        bestLength = candidateLength;
    }
```

**What it evaluates:** equality means every required count is met; the inclusive candidate length competes with the shortest known answer.

**Concrete result:** indexes `9..12` give `12 - 9 + 1 = 4`, so `bestStart = 9` and `bestLength = 4`.

**Why record before removal:** the current window is known valid; the next removal may invalidate it.

**What fails with `if`:** only one left position is tested, missing shorter valid windows obtainable by repeated shrinking.

**Equivalent clearer form:** use a helper to compare and store a `(start,length)` pair.

### Block 5 — Remove left and detect lost validity

```java
char outgoing = source.charAt(left);
windowCounts.merge(outgoing, -1, Integer::sum);
left++;
if (neededCounts.containsKey(outgoing)
        && windowCounts.get(outgoing) < neededCounts.get(outgoing)) {
    formedGroups--;
}
```

**What it evaluates:** the outgoing count decreases; if it falls below a requirement, that group is no longer formed.

**Concrete result:** removing the only active `A` for target `"ABC"` changes `A:1 → 0` and `formedGroups:3 → 2`.

**What fails without the `containsKey` guard:** comparing a character not required by the target can read a missing needed count and throw a `NullPointerException` through unboxing.

**Equivalent clearer form:** remove zero-count entries from the map for tidier state, though correctness does not require it.

### Block 6 — Distinguish no answer from a real range

```java
return bestLength == Integer.MAX_VALUE
        ? ""
        : source.substring(bestStart, bestStart + bestLength);
```

**What it evaluates:** unchanged sentinel returns empty; otherwise Java's exclusive substring end is `start + length`.

**Concrete result:** `bestStart = 9`, `bestLength = 4` calls `substring(9, 13)` and returns `"BANC"`.

**What fails without the sentinel check:** adding `Integer.MAX_VALUE` to `bestStart` overflows, producing an invalid substring boundary.

**Equivalent clearer form:** an explicit `if` is longer but behaves identically.

## Invariant and correctness

`windowCounts` always describes `source[left..right]`. `formedGroups == requiredGroups` exactly when every target count is satisfied. For each `right`, the inner loop tests every valid left boundary until the first removal makes the window invalid; therefore it records the shortest valid window ending at that `right`. Comparing across all right boundaries yields the global minimum.

## Complexity and alternatives

- Time: `O(source.length + target.length)` because each source character enters once and leaves at most once.
- Space: `O(alphabet)` for needed and active counts.
- Brute force: enumerate `O(n²)` substrings and check counts.
- Rechecking every target count after each move works but adds an avoidable alphabet factor; `formedGroups` makes validity `O(1)`.

## Common pitfalls

- counting distinct characters but ignoring duplicate target requirements;
- incrementing `formedGroups` for surplus copies;
- recording the answer only after removing left;
- shrinking only once instead of while the window remains valid;
- using `substring(start, start + length - 1)` even though Java's end index is exclusive;
- returning an arbitrary window when no valid candidate exists.

## Practice after learning the concept

- Write here: [MinimumWindowSubstringPractice.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/slidingwindow/minimumwindow/MinimumWindowSubstringPractice.java)
- Reveal after attempting: [MinimumWindowSubstringSolution.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/slidingwindow/minimumwindow/MinimumWindowSubstringSolution.java)

## Questions and explained answers

<details>
<summary>1. For target AABC, why is requiredGroups 3 rather than 4?</summary>

Groups count distinct required characters: `A`, `B`, and `C`. The count `A:2` is stored inside the `A` group.

</details>

<details>
<summary>2. Why is the best window recorded before the outgoing character is removed?</summary>

The current window is proven valid. Removing a required character may immediately make it invalid, so delaying the record can lose the best candidate.

</details>

<details>
<summary>3. Why does each source character participate only O(1) times?</summary>

The right boundary adds it once, and the left boundary removes it at most once. Neither boundary moves backward.

</details>

## Stop/go

Proceed only when you can explain required versus formed groups, derive `"BANC"`, show why the inner loop shrinks repeatedly, implement the practice method, and pass all checks.
