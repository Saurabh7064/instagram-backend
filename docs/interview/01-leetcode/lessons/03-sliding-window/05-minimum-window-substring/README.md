# Micro-Lesson 05 — Minimum Window Substring

## Question

> Given strings `source` and `target`, return the shortest contiguous substring of `source` containing every character from `target`, including duplicate counts. Return `""` when no such window exists.

Practice source: [Minimum Window Substring on LeetCode](https://leetcode.com/problems/minimum-window-substring/)

Examples:

- `source = "ADOBECODEBANC"`, `target = "ABC"` → `"BANC"`.

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

The main worked example throughout this walkthrough is `source="ADOBECODEBANC"`, `target="ABC"`. A **required group** is one distinct target character with its required count. Here the groups are `A:1`, `B:1`, and `C:1`, so `requiredGroups = 3`.

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

## Block-by-block code walkthrough

Original snapshots are retained. Where a value changes several times, its cell shows the sequence ending at the final value.

Use `source="ADOBECODEBANC"`, `target="ABC"`. Source indexes `0..12` hold `A,D,O,B,E,C,O,D,E,B,A,N,C`. The requirements are built first; each later block shows its own mutations and branch results. Supporting snapshots use the actual `right` index and phase to connect each mutation to its complete iteration. Map entries are displayed in insertion order for readability; Java HashMap order is unspecified.

### Block 1 — Count target requirements

```java
Map<Character, Integer> neededCounts = new HashMap<>();
for (char value : target.toCharArray()) {
    neededCounts.merge(value, 1, Integer::sum);
}
```

**What it evaluates:** `merge` inserts count `1` for a new character or adds `1` to its existing count.

**Concrete result for our main input:** target `"ABC"` becomes `{A=1, B=1, C=1}`.

**Why it is needed:** a covering window must satisfy duplicates, not merely contain each distinct character once.

**Why counts rather than a plain add/remove set?** Stay with `source="ADOBECODEBANC"`, `target="ABC"`. Later in this same execution, the window at `left=3`, `right=10` is `"BECODEBA"`. It contains two copies of `B`: source indexes `3` and `9`. Removing the left `B` must decrease its count from `2` to `1`, because the `B` at index `9` is still inside the window.

A plain set using `remove('B')` would forget `B` altogether, incorrectly suggesting that the window no longer has one. Counts let us distinguish removing one occurrence from losing the final occurrence. The target still needs `B:1`; after this removal, the window still supplies `B:1`, so the B requirement remains formed. This is why the later removal code decrements a count and checks it against the required count.

| Same running input | Window before | B positions before | Removal | B count after | Target B requirement | Is B still satisfied? |
|---|---|---|---|---|---|---|
| `ADOBECODEBANC / ABC` | `BECODEBA` at `3..10` | `3,9` | Remove B at index 3 | `2 - 1 = 1` | `1` | `1 >= 1 → true` |

**Equivalent clearer form:** `put(value, getOrDefault(value, 0) + 1)` performs the same update.

**Debugger state at this block:**

`neededCounts` begins `{}`. The target loop visits `value=A,B,C` and `merge(value,1,Integer::sum)` adds each required copy:

| value | neededCounts after merge |
| --- | --- |
| `A` | `{'A': 1}` |
| `B` | `{'A': 1, 'B': 1}` |
| `C` | `{'A': 1, 'B': 1, 'C': 1}` |
The final requirements `{A:1,B:1,C:1}` remain fixed during the source scan.

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

**Concrete result for our main input:** target `"ABC"` has `requiredGroups = 3` because `neededCounts` has the three keys `A`, `B`, and `C`.

**Why `Integer.MAX_VALUE`:** it is a sentinel meaning “no candidate yet”; every real window length is smaller.

**What fails without it:** initializing `bestLength = 0` prevents any positive candidate from being shorter.

**Equivalent clearer form:** store a nullable best range, but start/length avoids creating range objects.

**Debugger state at this block:**

`windowCounts={}`, `requiredGroups=neededCounts.size()=3`, `formedGroups=0`, `left=0`, `bestStart=0`, `bestLength=Integer.MAX_VALUE=2147483647`. No incoming character, candidate length, or outgoing character exists yet. A group means one target character’s required count, so three groups must be satisfied before shrinking.

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

**Concrete result in our ABC trace:** at `right=3`, B’s count changes `0 → 1`, matching required `B:1`, so `formedGroups++` runs. At `right=9`, B’s count changes `1 → 2`; it no longer equals `1`, so that increment is skipped.

**What fails without exact equality:** using `>=` increments the same group repeatedly for surplus copies and can declare an invalid window valid.

**Equivalent clearer form:** compare primitive `intValue()` values; `equals` safely compares the boxed integer values here.

**Debugger state at this block:**

Insert incoming first, then compare that count to its required count. Equality increases `formedGroups` only when a requirement is first met: at right `0` (A), `3` (B), `5` (C), `10` (A restored), and `12` (C restored). Irrelevant letters still appear in the map, but never form a required group. An extra B at right `9` changes B from `1` to `2`, so equality with required B=1 is false; it must not form another group.

| right / phase | operation | right | incoming | left | candidateLength | outgoing | formedGroups | bestStart | bestLength |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `0 / add; test required count` | `add; test required count` | `0` | `A` | `0` | `—` | `—` | `1` | `0` | `2147483647` |
| `1 / add; test required count` | `add; test required count` | `1` | `D` | `0` | `—` | `—` | `1` | `0` | `2147483647` |
| `2 / add; test required count` | `add; test required count` | `2` | `O` | `0` | `—` | `—` | `1` | `0` | `2147483647` |
| `3 / add; test required count` | `add; test required count` | `3` | `B` | `0` | `—` | `—` | `2` | `0` | `2147483647` |
| `4 / add; test required count` | `add; test required count` | `4` | `E` | `0` | `—` | `—` | `2` | `0` | `2147483647` |
| `5 / add; test required count` | `add; test required count` | `5` | `C` | `0` | `—` | `—` | `3` | `0` | `2147483647` |
| `6 / add; test required count` | `add; test required count` | `6` | `O` | `1` | `—` | `—` | `2` | `0` | `6` |
| `7 / add; test required count` | `add; test required count` | `7` | `D` | `1` | `—` | `—` | `2` | `0` | `6` |
| `8 / add; test required count` | `add; test required count` | `8` | `E` | `1` | `—` | `—` | `2` | `0` | `6` |
| `9 / add; test required count` | `add; test required count` | `9` | `B` | `1` | `—` | `—` | `2` | `0` | `6` |
| `10 / add; test required count` | `add; test required count` | `10` | `A` | `1` | `—` | `—` | `3` | `0` | `6` |
| `11 / add; test required count` | `add; test required count` | `11` | `N` | `6` | `—` | `—` | `2` | `0` | `6` |
| `12 / add; test required count` | `add; test required count` | `12` | `C` | `6` | `—` | `—` | `3` | `0` | `6` |

Complete `windowCounts` at those same iteration phases:

| right / phase | windowCounts after |
| --- | --- |
| `0 / add; test required count` | `{'A': 1}` |
| `1 / add; test required count` | `{'A': 1, 'D': 1}` |
| `2 / add; test required count` | `{'A': 1, 'D': 1, 'O': 1}` |
| `3 / add; test required count` | `{'A': 1, 'D': 1, 'O': 1, 'B': 1}` |
| `4 / add; test required count` | `{'A': 1, 'D': 1, 'O': 1, 'B': 1, 'E': 1}` |
| `5 / add; test required count` | `{'A': 1, 'D': 1, 'O': 1, 'B': 1, 'E': 1, 'C': 1}` |
| `6 / add; test required count` | `{'A': 0, 'D': 1, 'O': 2, 'B': 1, 'E': 1, 'C': 1}` |
| `7 / add; test required count` | `{'A': 0, 'D': 2, 'O': 2, 'B': 1, 'E': 1, 'C': 1}` |
| `8 / add; test required count` | `{'A': 0, 'D': 2, 'O': 2, 'B': 1, 'E': 2, 'C': 1}` |
| `9 / add; test required count` | `{'A': 0, 'D': 2, 'O': 2, 'B': 2, 'E': 2, 'C': 1}` |
| `10 / add; test required count` | `{'A': 1, 'D': 2, 'O': 2, 'B': 2, 'E': 2, 'C': 1}` |
| `11 / add; test required count` | `{'A': 1, 'D': 1, 'O': 1, 'B': 1, 'E': 1, 'C': 0, 'N': 1}` |
| `12 / add; test required count` | `{'A': 1, 'D': 1, 'O': 1, 'B': 1, 'E': 1, 'C': 1, 'N': 1}` |

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

**Debugger state at this block:**

Only `formedGroups == requiredGroups`, meaning `3 == 3`, enters this loop. Calculate `candidateLength` before any removal and save its start and length only if strictly shorter.

| right / phase | operation | right | incoming | left | candidateLength | outgoing | formedGroups | bestStart | bestLength |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `5 / compare candidate; save if shorter` | `compare candidate; save if shorter` | `5` | `C` | `0` | `6` | `—` | `3` | `0` | `6` |
| `10 / compare candidate; save if shorter` | `compare candidate; save if shorter` | `10` | `A` | `1` | `10` | `—` | `3` | `0` | `6` |
| `10 / compare candidate; save if shorter` | `compare candidate; save if shorter` | `10` | `A` | `2` | `9` | `—` | `3` | `0` | `6` |
| `10 / compare candidate; save if shorter` | `compare candidate; save if shorter` | `10` | `A` | `3` | `8` | `—` | `3` | `0` | `6` |
| `10 / compare candidate; save if shorter` | `compare candidate; save if shorter` | `10` | `A` | `4` | `7` | `—` | `3` | `0` | `6` |
| `10 / compare candidate; save if shorter` | `compare candidate; save if shorter` | `10` | `A` | `5` | `6` | `—` | `3` | `0` | `6` |
| `12 / compare candidate; save if shorter` | `compare candidate; save if shorter` | `12` | `C` | `6` | `7` | `—` | `3` | `0` | `6` |
| `12 / compare candidate; save if shorter` | `compare candidate; save if shorter` | `12` | `C` | `7` | `6` | `—` | `3` | `0` | `6` |
| `12 / compare candidate; save if shorter` | `compare candidate; save if shorter` | `12` | `C` | `8` | `5` | `—` | `3` | `8` | `5` |
| `12 / compare candidate; save if shorter` | `compare candidate; save if shorter` | `12` | `C` | `9` | `4` | `—` | `3` | `9` | `4` |

Complete `windowCounts` at those same iteration phases:

| right / phase | windowCounts after |
| --- | --- |
| `5 / compare candidate; save if shorter` | `{'A': 1, 'D': 1, 'O': 1, 'B': 1, 'E': 1, 'C': 1}` |
| `10 / compare candidate; save if shorter` | `{'A': 1, 'D': 2, 'O': 2, 'B': 2, 'E': 2, 'C': 1}` |
| `10 / compare candidate; save if shorter` | `{'A': 1, 'D': 1, 'O': 2, 'B': 2, 'E': 2, 'C': 1}` |
| `10 / compare candidate; save if shorter` | `{'A': 1, 'D': 1, 'O': 1, 'B': 2, 'E': 2, 'C': 1}` |
| `10 / compare candidate; save if shorter` | `{'A': 1, 'D': 1, 'O': 1, 'B': 1, 'E': 2, 'C': 1}` |
| `10 / compare candidate; save if shorter` | `{'A': 1, 'D': 1, 'O': 1, 'B': 1, 'E': 1, 'C': 1}` |
| `12 / compare candidate; save if shorter` | `{'A': 1, 'D': 1, 'O': 1, 'B': 1, 'E': 1, 'C': 1, 'N': 1}` |
| `12 / compare candidate; save if shorter` | `{'A': 1, 'D': 1, 'O': 0, 'B': 1, 'E': 1, 'C': 1, 'N': 1}` |
| `12 / compare candidate; save if shorter` | `{'A': 1, 'D': 0, 'O': 0, 'B': 1, 'E': 1, 'C': 1, 'N': 1}` |
| `12 / compare candidate; save if shorter` | `{'A': 1, 'D': 0, 'O': 0, 'B': 1, 'E': 0, 'C': 1, 'N': 1}` |

At right `10`, candidate lengths `10,9,8,7,6` never beat best length `6`; equality also does not update the best. At right `12`, length `7` and `6` fail, `5` replaces best with `(8,5)`, then `4` replaces it with `(9,4)`.

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

**Debugger state at this block:**

Read outgoing at the old left, decrement its map count, then increment left. Decrease `formedGroups` only if a required count falls below its requirement.

| right / phase | operation | right | incoming | left | candidateLength | outgoing | formedGroups | bestStart | bestLength |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `5 / remove; left++; test coverage` | `remove; left++; test coverage` | `5` | `C` | `1` | `6` | `A` | `2` | `0` | `6` |
| `10 / remove; left++; test coverage` | `remove; left++; test coverage` | `10` | `A` | `2` | `10` | `D` | `3` | `0` | `6` |
| `10 / remove; left++; test coverage` | `remove; left++; test coverage` | `10` | `A` | `3` | `9` | `O` | `3` | `0` | `6` |
| `10 / remove; left++; test coverage` | `remove; left++; test coverage` | `10` | `A` | `4` | `8` | `B` | `3` | `0` | `6` |
| `10 / remove; left++; test coverage` | `remove; left++; test coverage` | `10` | `A` | `5` | `7` | `E` | `3` | `0` | `6` |
| `10 / remove; left++; test coverage` | `remove; left++; test coverage` | `10` | `A` | `6` | `6` | `C` | `2` | `0` | `6` |
| `12 / remove; left++; test coverage` | `remove; left++; test coverage` | `12` | `C` | `7` | `7` | `O` | `3` | `0` | `6` |
| `12 / remove; left++; test coverage` | `remove; left++; test coverage` | `12` | `C` | `8` | `6` | `D` | `3` | `0` | `6` |
| `12 / remove; left++; test coverage` | `remove; left++; test coverage` | `12` | `C` | `9` | `5` | `E` | `3` | `8` | `5` |
| `12 / remove; left++; test coverage` | `remove; left++; test coverage` | `12` | `C` | `10` | `4` | `B` | `2` | `9` | `4` |

Complete `windowCounts` at those same iteration phases:

| right / phase | windowCounts after |
| --- | --- |
| `5 / remove; left++; test coverage` | `{'A': 0, 'D': 1, 'O': 1, 'B': 1, 'E': 1, 'C': 1}` |
| `10 / remove; left++; test coverage` | `{'A': 1, 'D': 1, 'O': 2, 'B': 2, 'E': 2, 'C': 1}` |
| `10 / remove; left++; test coverage` | `{'A': 1, 'D': 1, 'O': 1, 'B': 2, 'E': 2, 'C': 1}` |
| `10 / remove; left++; test coverage` | `{'A': 1, 'D': 1, 'O': 1, 'B': 1, 'E': 2, 'C': 1}` |
| `10 / remove; left++; test coverage` | `{'A': 1, 'D': 1, 'O': 1, 'B': 1, 'E': 1, 'C': 1}` |
| `10 / remove; left++; test coverage` | `{'A': 1, 'D': 1, 'O': 1, 'B': 1, 'E': 1, 'C': 0}` |
| `12 / remove; left++; test coverage` | `{'A': 1, 'D': 1, 'O': 0, 'B': 1, 'E': 1, 'C': 1, 'N': 1}` |
| `12 / remove; left++; test coverage` | `{'A': 1, 'D': 0, 'O': 0, 'B': 1, 'E': 1, 'C': 1, 'N': 1}` |
| `12 / remove; left++; test coverage` | `{'A': 1, 'D': 0, 'O': 0, 'B': 1, 'E': 0, 'C': 1, 'N': 1}` |
| `12 / remove; left++; test coverage` | `{'A': 1, 'D': 0, 'O': 0, 'B': 0, 'E': 0, 'C': 1, 'N': 1}` |

Removing A at right `5` changes A `1 → 0`, so formed groups `3 → 2` and contraction stops. Removing surplus B at right `10` changes B `2 → 1`; coverage remains satisfied. Removing C later changes C `1 → 0` and stops contraction. Zero-count keys remain in the map; they are not deleted.


**Narrated execution — follow each operation, then check its values:**

**Iteration/check 1 — right = `0`.**

Read source index `0`, character `A`. `windowCounts.merge` adds one `A` occurrence, changing its count `0 → 1`. Evaluate the incoming-group `if` condition left to right: `containsKey(A) → true; 1 == 1 → true`. The `if` is true, so `formedGroups++` changes `0 → 1`. Next check `while (formedGroups == requiredGroups)`: `1 == 3 → false; stop shrinking`. The completed outer iteration has left `0`, formed groups `1`, best `(start,length)` `(0,2147483647)`, and full window map `{'A': 1}`. The `for` loop then runs `right++` and checks its condition again.

| right | incoming | incoming group condition | inner loop: every check / branch / removal | left after | formedGroups after | bestStart,bestLength after | windowCounts after | next action |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `0` | `A` | `containsKey(A) → true; 1 == 1 → true` | `1 == 3 → false; stop shrinking` | `0` | `1` | `(0,2147483647)` | `{'A': 1}` | `right++; recheck for condition` |

**Iteration/check 2 — right = `1`.**

Read source index `1`, character `D`. `windowCounts.merge` adds one `D` occurrence, changing its count `0 → 1`. Evaluate the incoming-group `if` condition left to right: `containsKey(D) → false; equality Skipped (short circuit)`. The `if` is false, so skip `formedGroups++`; it stays `1`. Next check `while (formedGroups == requiredGroups)`: `1 == 3 → false; stop shrinking`. The completed outer iteration has left `0`, formed groups `1`, best `(start,length)` `(0,2147483647)`, and full window map `{'A': 1, 'D': 1}`. The `for` loop then runs `right++` and checks its condition again.

| right | incoming | incoming group condition | inner loop: every check / branch / removal | left after | formedGroups after | bestStart,bestLength after | windowCounts after | next action |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `1` | `D` | `containsKey(D) → false; equality Skipped (short circuit)` | `1 == 3 → false; stop shrinking` | `0` | `1` | `(0,2147483647)` | `{'A': 1, 'D': 1}` | `right++; recheck for condition` |

**Iteration/check 3 — right = `2`.**

Read source index `2`, character `O`. `windowCounts.merge` adds one `O` occurrence, changing its count `0 → 1`. Evaluate the incoming-group `if` condition left to right: `containsKey(O) → false; equality Skipped (short circuit)`. The `if` is false, so skip `formedGroups++`; it stays `1`. Next check `while (formedGroups == requiredGroups)`: `1 == 3 → false; stop shrinking`. The completed outer iteration has left `0`, formed groups `1`, best `(start,length)` `(0,2147483647)`, and full window map `{'A': 1, 'D': 1, 'O': 1}`. The `for` loop then runs `right++` and checks its condition again.

| right | incoming | incoming group condition | inner loop: every check / branch / removal | left after | formedGroups after | bestStart,bestLength after | windowCounts after | next action |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `2` | `O` | `containsKey(O) → false; equality Skipped (short circuit)` | `1 == 3 → false; stop shrinking` | `0` | `1` | `(0,2147483647)` | `{'A': 1, 'D': 1, 'O': 1}` | `right++; recheck for condition` |

**Iteration/check 4 — right = `3`.**

Read source index `3`, character `B`. `windowCounts.merge` adds one `B` occurrence, changing its count `0 → 1`. Evaluate the incoming-group `if` condition left to right: `containsKey(B) → true; 1 == 1 → true`. The `if` is true, so `formedGroups++` changes `1 → 2`. Next check `while (formedGroups == requiredGroups)`: `2 == 3 → false; stop shrinking`. The completed outer iteration has left `0`, formed groups `2`, best `(start,length)` `(0,2147483647)`, and full window map `{'A': 1, 'D': 1, 'O': 1, 'B': 1}`. The `for` loop then runs `right++` and checks its condition again.

| right | incoming | incoming group condition | inner loop: every check / branch / removal | left after | formedGroups after | bestStart,bestLength after | windowCounts after | next action |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `3` | `B` | `containsKey(B) → true; 1 == 1 → true` | `2 == 3 → false; stop shrinking` | `0` | `2` | `(0,2147483647)` | `{'A': 1, 'D': 1, 'O': 1, 'B': 1}` | `right++; recheck for condition` |

**Iteration/check 5 — right = `4`.**

Read source index `4`, character `E`. `windowCounts.merge` adds one `E` occurrence, changing its count `0 → 1`. Evaluate the incoming-group `if` condition left to right: `containsKey(E) → false; equality Skipped (short circuit)`. The `if` is false, so skip `formedGroups++`; it stays `2`. Next check `while (formedGroups == requiredGroups)`: `2 == 3 → false; stop shrinking`. The completed outer iteration has left `0`, formed groups `2`, best `(start,length)` `(0,2147483647)`, and full window map `{'A': 1, 'D': 1, 'O': 1, 'B': 1, 'E': 1}`. The `for` loop then runs `right++` and checks its condition again.

| right | incoming | incoming group condition | inner loop: every check / branch / removal | left after | formedGroups after | bestStart,bestLength after | windowCounts after | next action |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `4` | `E` | `containsKey(E) → false; equality Skipped (short circuit)` | `2 == 3 → false; stop shrinking` | `0` | `2` | `(0,2147483647)` | `{'A': 1, 'D': 1, 'O': 1, 'B': 1, 'E': 1}` | `right++; recheck for condition` |

**Iteration/check 6 — right = `5`.**

Read source index `5`, character `C`. `windowCounts.merge` adds one `C` occurrence, changing its count `0 → 1`. Evaluate the incoming-group `if` condition left to right: `containsKey(C) → true; 1 == 1 → true`. The `if` is true, so `formedGroups++` changes `2 → 3`. Next check `while (formedGroups == requiredGroups)`: `3==3 true; candidateLength=5-0+1=6; if (candidateLength < bestLength): 6 < 2147483647 → true; execute bestStart = left → 0; bestLength = candidateLength → 6; exit if body; remove A@0; left=1; required=true; below required=true; formed=2; 2 == 3 → false; stop shrinking`. The completed outer iteration has left `1`, formed groups `2`, best `(start,length)` `(0,6)`, and full window map `{'A': 0, 'D': 1, 'O': 1, 'B': 1, 'E': 1, 'C': 1}`. The `for` loop then runs `right++` and checks its condition again.

| right | incoming | incoming group condition | inner loop: every check / branch / removal | left after | formedGroups after | bestStart,bestLength after | windowCounts after | next action |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `5` | `C` | `containsKey(C) → true; 1 == 1 → true` | `3==3 true; candidateLength=5-0+1=6; if (candidateLength < bestLength): 6 < 2147483647 → true; execute bestStart = left → 0; bestLength = candidateLength → 6; exit if body; remove A@0; left=1; required=true; below required=true; formed=2; 2 == 3 → false; stop shrinking` | `1` | `2 → 3 → 2` | `(0,6)` | `{'A': 0, 'D': 1, 'O': 1, 'B': 1, 'E': 1, 'C': 1}` | `right++; recheck for condition` |

**Iteration/check 7 — right = `6`.**

Read source index `6`, character `O`. `windowCounts.merge` adds one `O` occurrence, changing its count `1 → 2`. Evaluate the incoming-group `if` condition left to right: `containsKey(O) → false; equality Skipped (short circuit)`. The `if` is false, so skip `formedGroups++`; it stays `2`. Next check `while (formedGroups == requiredGroups)`: `2 == 3 → false; stop shrinking`. The completed outer iteration has left `1`, formed groups `2`, best `(start,length)` `(0,6)`, and full window map `{'A': 0, 'D': 1, 'O': 2, 'B': 1, 'E': 1, 'C': 1}`. The `for` loop then runs `right++` and checks its condition again.

| right | incoming | incoming group condition | inner loop: every check / branch / removal | left after | formedGroups after | bestStart,bestLength after | windowCounts after | next action |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `6` | `O` | `containsKey(O) → false; equality Skipped (short circuit)` | `2 == 3 → false; stop shrinking` | `1` | `2` | `(0,6)` | `{'A': 0, 'D': 1, 'O': 2, 'B': 1, 'E': 1, 'C': 1}` | `right++; recheck for condition` |

**Iteration/check 8 — right = `7`.**

Read source index `7`, character `D`. `windowCounts.merge` adds one `D` occurrence, changing its count `1 → 2`. Evaluate the incoming-group `if` condition left to right: `containsKey(D) → false; equality Skipped (short circuit)`. The `if` is false, so skip `formedGroups++`; it stays `2`. Next check `while (formedGroups == requiredGroups)`: `2 == 3 → false; stop shrinking`. The completed outer iteration has left `1`, formed groups `2`, best `(start,length)` `(0,6)`, and full window map `{'A': 0, 'D': 2, 'O': 2, 'B': 1, 'E': 1, 'C': 1}`. The `for` loop then runs `right++` and checks its condition again.

| right | incoming | incoming group condition | inner loop: every check / branch / removal | left after | formedGroups after | bestStart,bestLength after | windowCounts after | next action |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `7` | `D` | `containsKey(D) → false; equality Skipped (short circuit)` | `2 == 3 → false; stop shrinking` | `1` | `2` | `(0,6)` | `{'A': 0, 'D': 2, 'O': 2, 'B': 1, 'E': 1, 'C': 1}` | `right++; recheck for condition` |

**Iteration/check 9 — right = `8`.**

Read source index `8`, character `E`. `windowCounts.merge` adds one `E` occurrence, changing its count `1 → 2`. Evaluate the incoming-group `if` condition left to right: `containsKey(E) → false; equality Skipped (short circuit)`. The `if` is false, so skip `formedGroups++`; it stays `2`. Next check `while (formedGroups == requiredGroups)`: `2 == 3 → false; stop shrinking`. The completed outer iteration has left `1`, formed groups `2`, best `(start,length)` `(0,6)`, and full window map `{'A': 0, 'D': 2, 'O': 2, 'B': 1, 'E': 2, 'C': 1}`. The `for` loop then runs `right++` and checks its condition again.

| right | incoming | incoming group condition | inner loop: every check / branch / removal | left after | formedGroups after | bestStart,bestLength after | windowCounts after | next action |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `8` | `E` | `containsKey(E) → false; equality Skipped (short circuit)` | `2 == 3 → false; stop shrinking` | `1` | `2` | `(0,6)` | `{'A': 0, 'D': 2, 'O': 2, 'B': 1, 'E': 2, 'C': 1}` | `right++; recheck for condition` |

**Iteration/check 10 — right = `9`.**

Read source index `9`, character `B`. `windowCounts.merge` adds one `B` occurrence, changing its count `1 → 2`. Evaluate the incoming-group `if` condition left to right: `containsKey(B) → true; 2 == 1 → false`. The `if` is false, so skip `formedGroups++`; it stays `2`. Next check `while (formedGroups == requiredGroups)`: `2 == 3 → false; stop shrinking`. The completed outer iteration has left `1`, formed groups `2`, best `(start,length)` `(0,6)`, and full window map `{'A': 0, 'D': 2, 'O': 2, 'B': 2, 'E': 2, 'C': 1}`. The `for` loop then runs `right++` and checks its condition again.

| right | incoming | incoming group condition | inner loop: every check / branch / removal | left after | formedGroups after | bestStart,bestLength after | windowCounts after | next action |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `9` | `B` | `containsKey(B) → true; 2 == 1 → false` | `2 == 3 → false; stop shrinking` | `1` | `2` | `(0,6)` | `{'A': 0, 'D': 2, 'O': 2, 'B': 2, 'E': 2, 'C': 1}` | `right++; recheck for condition` |

**Iteration/check 11 — right = `10`.**

Read source index `10`, character `A`. `windowCounts.merge` adds one `A` occurrence, changing its count `0 → 1`. Evaluate the incoming-group `if` condition left to right: `containsKey(A) → true; 1 == 1 → true`. The `if` is true, so `formedGroups++` changes `2 → 3`. Next check `while (formedGroups == requiredGroups)`: `3==3 true; candidateLength=10-1+1=10; if (candidateLength < bestLength): 10 < 6 → false; skip if body; bestStart=0, bestLength=6 unchanged; remove D@1; left=2; required=false; below required=Skipped; formed=3; 3==3 true; candidateLength=10-2+1=9; if (candidateLength < bestLength): 9 < 6 → false; skip if body; bestStart=0, bestLength=6 unchanged; remove O@2; left=3; required=false; below required=Skipped; formed=3; 3==3 true; candidateLength=10-3+1=8; if (candidateLength < bestLength): 8 < 6 → false; skip if body; bestStart=0, bestLength=6 unchanged; remove B@3; left=4; required=true; below required=false; formed=3; 3==3 true; candidateLength=10-4+1=7; if (candidateLength < bestLength): 7 < 6 → false; skip if body; bestStart=0, bestLength=6 unchanged; remove E@4; left=5; required=false; below required=Skipped; formed=3; 3==3 true; candidateLength=10-5+1=6; if (candidateLength < bestLength): 6 < 6 → false; skip if body; bestStart=0, bestLength=6 unchanged; remove C@5; left=6; required=true; below required=true; formed=2; 2 == 3 → false; stop shrinking`. The completed outer iteration has left `6`, formed groups `2`, best `(start,length)` `(0,6)`, and full window map `{'A': 1, 'D': 1, 'O': 1, 'B': 1, 'E': 1, 'C': 0}`. The `for` loop then runs `right++` and checks its condition again.

| right | incoming | incoming group condition | inner loop: every check / branch / removal | left after | formedGroups after | bestStart,bestLength after | windowCounts after | next action |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `10` | `A` | `containsKey(A) → true; 1 == 1 → true` | `3==3 true; candidateLength=10-1+1=10; if (candidateLength < bestLength): 10 < 6 → false; skip if body; bestStart=0, bestLength=6 unchanged; remove D@1; left=2; required=false; below required=Skipped; formed=3; 3==3 true; candidateLength=10-2+1=9; if (candidateLength < bestLength): 9 < 6 → false; skip if body; bestStart=0, bestLength=6 unchanged; remove O@2; left=3; required=false; below required=Skipped; formed=3; 3==3 true; candidateLength=10-3+1=8; if (candidateLength < bestLength): 8 < 6 → false; skip if body; bestStart=0, bestLength=6 unchanged; remove B@3; left=4; required=true; below required=false; formed=3; 3==3 true; candidateLength=10-4+1=7; if (candidateLength < bestLength): 7 < 6 → false; skip if body; bestStart=0, bestLength=6 unchanged; remove E@4; left=5; required=false; below required=Skipped; formed=3; 3==3 true; candidateLength=10-5+1=6; if (candidateLength < bestLength): 6 < 6 → false; skip if body; bestStart=0, bestLength=6 unchanged; remove C@5; left=6; required=true; below required=true; formed=2; 2 == 3 → false; stop shrinking` | `1 → 2 → 3 → 4 → 5 → 6` | `2 → 3 → 2` | `(0,6)` | `{'A': 1, 'D': 1, 'O': 1, 'B': 1, 'E': 1, 'C': 0}` | `right++; recheck for condition` |

**Iteration/check 12 — right = `11`.**

Read source index `11`, character `N`. `windowCounts.merge` adds one `N` occurrence, changing its count `0 → 1`. Evaluate the incoming-group `if` condition left to right: `containsKey(N) → false; equality Skipped (short circuit)`. The `if` is false, so skip `formedGroups++`; it stays `2`. Next check `while (formedGroups == requiredGroups)`: `2 == 3 → false; stop shrinking`. The completed outer iteration has left `6`, formed groups `2`, best `(start,length)` `(0,6)`, and full window map `{'A': 1, 'D': 1, 'O': 1, 'B': 1, 'E': 1, 'C': 0, 'N': 1}`. The `for` loop then runs `right++` and checks its condition again.

| right | incoming | incoming group condition | inner loop: every check / branch / removal | left after | formedGroups after | bestStart,bestLength after | windowCounts after | next action |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `11` | `N` | `containsKey(N) → false; equality Skipped (short circuit)` | `2 == 3 → false; stop shrinking` | `6` | `2` | `(0,6)` | `{'A': 1, 'D': 1, 'O': 1, 'B': 1, 'E': 1, 'C': 0, 'N': 1}` | `right++; recheck for condition` |

**Iteration/check 13 — right = `12`.**

Read source index `12`, character `C`. `windowCounts.merge` adds one `C` occurrence, changing its count `0 → 1`. Evaluate the incoming-group `if` condition left to right: `containsKey(C) → true; 1 == 1 → true`. The `if` is true, so `formedGroups++` changes `2 → 3`. Next check `while (formedGroups == requiredGroups)`: `3==3 true; candidateLength=12-6+1=7; if (candidateLength < bestLength): 7 < 6 → false; skip if body; bestStart=0, bestLength=6 unchanged; remove O@6; left=7; required=false; below required=Skipped; formed=3; 3==3 true; candidateLength=12-7+1=6; if (candidateLength < bestLength): 6 < 6 → false; skip if body; bestStart=0, bestLength=6 unchanged; remove D@7; left=8; required=false; below required=Skipped; formed=3; 3==3 true; candidateLength=12-8+1=5; if (candidateLength < bestLength): 5 < 6 → true; execute bestStart = left → 8; bestLength = candidateLength → 5; exit if body; remove E@8; left=9; required=false; below required=Skipped; formed=3; 3==3 true; candidateLength=12-9+1=4; if (candidateLength < bestLength): 4 < 5 → true; execute bestStart = left → 9; bestLength = candidateLength → 4; exit if body; remove B@9; left=10; required=true; below required=true; formed=2; 2 == 3 → false; stop shrinking`. The completed outer iteration has left `10`, formed groups `2`, best `(start,length)` `(9,4)`, and full window map `{'A': 1, 'D': 0, 'O': 0, 'B': 0, 'E': 0, 'C': 1, 'N': 1}`. The `for` loop then runs `right++` and checks its condition again.

| right | incoming | incoming group condition | inner loop: every check / branch / removal | left after | formedGroups after | bestStart,bestLength after | windowCounts after | next action |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `12` | `C` | `containsKey(C) → true; 1 == 1 → true` | `3==3 true; candidateLength=12-6+1=7; if (candidateLength < bestLength): 7 < 6 → false; skip if body; bestStart=0, bestLength=6 unchanged; remove O@6; left=7; required=false; below required=Skipped; formed=3; 3==3 true; candidateLength=12-7+1=6; if (candidateLength < bestLength): 6 < 6 → false; skip if body; bestStart=0, bestLength=6 unchanged; remove D@7; left=8; required=false; below required=Skipped; formed=3; 3==3 true; candidateLength=12-8+1=5; if (candidateLength < bestLength): 5 < 6 → true; execute bestStart = left → 8; bestLength = candidateLength → 5; exit if body; remove E@8; left=9; required=false; below required=Skipped; formed=3; 3==3 true; candidateLength=12-9+1=4; if (candidateLength < bestLength): 4 < 5 → true; execute bestStart = left → 9; bestLength = candidateLength → 4; exit if body; remove B@9; left=10; required=true; below required=true; formed=2; 2 == 3 → false; stop shrinking` | `6 → 7 → 8 → 9 → 10` | `2 → 3 → 2` | `(0,6) → (8,5) → (9,4)` | `{'A': 1, 'D': 0, 'O': 0, 'B': 0, 'E': 0, 'C': 1, 'N': 1}` | `right++; recheck for condition` |

Every outer guard `right < 13` is true for these rows. After right `12`, `13 < 13` is false; bestLength is `4`, so the no-answer guard is false and return `source.substring(9,13) = "BANC"`. A false required-key test skips its count comparison; the inner loop stops only when coverage is lost.

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

**Debugger state at this block:**

After the last contraction: `right=12`, `left=10`, `formedGroups=2`, `bestStart=9`, `bestLength=4`, `requiredGroups=3`. Final counts are `{A:1,D:0,O:0,B:0,E:0,C:1,N:1}`. The loop increments right to `13`; `13 < 13` is false. `4 == Integer.MAX_VALUE` is false, so return `source.substring(9,9+4) = source.substring(9,13) = "BANC"` (exclusive end). If no valid candidate were recorded, the unchanged sentinel would trigger the empty-string return; that branch is not taken in this execution.

## Reference approach

```java
// Input: source = "ADOBECODEBANC"; target = "ABC"
// Expected return: "BANC"

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
<summary>1. In our ABC trace, why is requiredGroups 3 even when the window ADOBEC contains six distinct characters?</summary>

Requirements come from the fixed target, not from the changing window. Target `"ABC"` has three keys with counts `A:1`, `B:1`, and `C:1`. D, O, and E in `"ADOBEC"` create no additional requirements; they are extra window characters. Thus requiredGroups stays 3 throughout this execution.

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
