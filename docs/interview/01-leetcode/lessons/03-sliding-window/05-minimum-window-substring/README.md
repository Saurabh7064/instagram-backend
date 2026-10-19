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

**Concrete result in this same trace:** target `"ABC"` needs `B:1`. At `right=3`, B’s window count changes `0 → 1`, so `formedGroups++` runs. At `right=9`, B’s count changes `1 → 2`; `2 == 1` is false, so that `if` body is skipped.

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


**Narrated execution — identify the code, execute it, then check its values:**

Fixed input: `source="ADOBECODEBANC"`, `target="ABC"`; `neededCounts={A:1,B:1,C:1}` and `requiredGroups=3` throughout. Each numbered action names its exact Java statement and whether execution is in the outer `for` body, an `if` body, or the inner `while` body. “Skip the body” means its statements do not execute. The outer `for` initializer sets `right=0` once before the first check.


**Outer `for` iteration — `right=0`, incoming character `A`.**

1. **At the outer `for` condition** `right < source.length()`: evaluate `0 < 13 → true`, so enter this iteration’s `for` body. `left=0`, `formedGroups=0`, `bestStart=0`, `bestLength=2147483647` before its statements.
2. **Inside the `for` body, execute** `char incoming = source.charAt(right);`: read `source.charAt(0)` and assign `incoming='A'`.
3. **Still inside the `for` body, execute** `windowCounts.merge(incoming, 1, Integer::sum);`: the count of `A` changes `0 → 1`. The complete map is now `{'A': 1}`.
4. **Evaluate the incoming-character `if` condition** `if (neededCounts.containsKey(incoming) && windowCounts.get(incoming).equals(neededCounts.get(incoming)))`: First `neededCounts.containsKey('A') → true`; therefore evaluate the second operand: `windowCounts.get('A').equals(neededCounts.get('A')) → 1 == 1 → true`. The complete `if` condition is `true`.
5. **Enter that incoming-character `if` body and execute** `formedGroups++;`: change `0 → 1`. Then leave the `if` body and move to the inner `while` condition.
6. **Evaluate the inner `while` condition** `while (formedGroups == requiredGroups)`: `1 == 3 → false`. Skip the entire `while` body: no candidate length, best-window `if`, outgoing-character read, removal, or `left++` executes in this outer iteration.
7. **Reach the end of the outer `for` body, then execute its update** `right++`: change `0 → 1`. Next evaluate `right < source.length()` with `1 < 13`; it is true, so begin the next outer iteration. There is no `continue;` statement in this method; the `for` loop performs this update automatically at the end of its body.

| right before | incoming | incoming if result | while body passes | left after | formedGroups after | bestStart / bestLength | windowCounts at end | for update / next test |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `0` | `A` | `true` | `0` | `0` | `1` | `0 / 2147483647` | `{'A': 1}` | `right=1; 1<13 → true` |


**Outer `for` iteration — `right=1`, incoming character `D`.**

1. **At the outer `for` condition** `right < source.length()`: evaluate `1 < 13 → true`, so enter this iteration’s `for` body. `left=0`, `formedGroups=1`, `bestStart=0`, `bestLength=2147483647` before its statements.
2. **Inside the `for` body, execute** `char incoming = source.charAt(right);`: read `source.charAt(1)` and assign `incoming='D'`.
3. **Still inside the `for` body, execute** `windowCounts.merge(incoming, 1, Integer::sum);`: the count of `D` changes `0 → 1`. The complete map is now `{'A': 1, 'D': 1}`.
4. **Evaluate the incoming-character `if` condition** `if (neededCounts.containsKey(incoming) && windowCounts.get(incoming).equals(neededCounts.get(incoming)))`: First `neededCounts.containsKey('D') → false`. Because the operands use `&&`, skip the second operand completely; no required-count lookup or `.equals(...)` call runs. The complete `if` condition is `false`.
5. **Skip that incoming-character `if` body**: `formedGroups++;` does not execute; `formedGroups` stays `1`. Execution remains in the outer `for` body and moves to the inner `while` condition.
6. **Evaluate the inner `while` condition** `while (formedGroups == requiredGroups)`: `1 == 3 → false`. Skip the entire `while` body: no candidate length, best-window `if`, outgoing-character read, removal, or `left++` executes in this outer iteration.
7. **Reach the end of the outer `for` body, then execute its update** `right++`: change `1 → 2`. Next evaluate `right < source.length()` with `2 < 13`; it is true, so begin the next outer iteration. There is no `continue;` statement in this method; the `for` loop performs this update automatically at the end of its body.

| right before | incoming | incoming if result | while body passes | left after | formedGroups after | bestStart / bestLength | windowCounts at end | for update / next test |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `1` | `D` | `false` | `0` | `0` | `1` | `0 / 2147483647` | `{'A': 1, 'D': 1}` | `right=2; 2<13 → true` |


**Outer `for` iteration — `right=2`, incoming character `O`.**

1. **At the outer `for` condition** `right < source.length()`: evaluate `2 < 13 → true`, so enter this iteration’s `for` body. `left=0`, `formedGroups=1`, `bestStart=0`, `bestLength=2147483647` before its statements.
2. **Inside the `for` body, execute** `char incoming = source.charAt(right);`: read `source.charAt(2)` and assign `incoming='O'`.
3. **Still inside the `for` body, execute** `windowCounts.merge(incoming, 1, Integer::sum);`: the count of `O` changes `0 → 1`. The complete map is now `{'A': 1, 'D': 1, 'O': 1}`.
4. **Evaluate the incoming-character `if` condition** `if (neededCounts.containsKey(incoming) && windowCounts.get(incoming).equals(neededCounts.get(incoming)))`: First `neededCounts.containsKey('O') → false`. Because the operands use `&&`, skip the second operand completely; no required-count lookup or `.equals(...)` call runs. The complete `if` condition is `false`.
5. **Skip that incoming-character `if` body**: `formedGroups++;` does not execute; `formedGroups` stays `1`. Execution remains in the outer `for` body and moves to the inner `while` condition.
6. **Evaluate the inner `while` condition** `while (formedGroups == requiredGroups)`: `1 == 3 → false`. Skip the entire `while` body: no candidate length, best-window `if`, outgoing-character read, removal, or `left++` executes in this outer iteration.
7. **Reach the end of the outer `for` body, then execute its update** `right++`: change `2 → 3`. Next evaluate `right < source.length()` with `3 < 13`; it is true, so begin the next outer iteration. There is no `continue;` statement in this method; the `for` loop performs this update automatically at the end of its body.

| right before | incoming | incoming if result | while body passes | left after | formedGroups after | bestStart / bestLength | windowCounts at end | for update / next test |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `2` | `O` | `false` | `0` | `0` | `1` | `0 / 2147483647` | `{'A': 1, 'D': 1, 'O': 1}` | `right=3; 3<13 → true` |


**Outer `for` iteration — `right=3`, incoming character `B`.**

1. **At the outer `for` condition** `right < source.length()`: evaluate `3 < 13 → true`, so enter this iteration’s `for` body. `left=0`, `formedGroups=1`, `bestStart=0`, `bestLength=2147483647` before its statements.
2. **Inside the `for` body, execute** `char incoming = source.charAt(right);`: read `source.charAt(3)` and assign `incoming='B'`.
3. **Still inside the `for` body, execute** `windowCounts.merge(incoming, 1, Integer::sum);`: the count of `B` changes `0 → 1`. The complete map is now `{'A': 1, 'D': 1, 'O': 1, 'B': 1}`.
4. **Evaluate the incoming-character `if` condition** `if (neededCounts.containsKey(incoming) && windowCounts.get(incoming).equals(neededCounts.get(incoming)))`: First `neededCounts.containsKey('B') → true`; therefore evaluate the second operand: `windowCounts.get('B').equals(neededCounts.get('B')) → 1 == 1 → true`. The complete `if` condition is `true`.
5. **Enter that incoming-character `if` body and execute** `formedGroups++;`: change `1 → 2`. Then leave the `if` body and move to the inner `while` condition.
6. **Evaluate the inner `while` condition** `while (formedGroups == requiredGroups)`: `2 == 3 → false`. Skip the entire `while` body: no candidate length, best-window `if`, outgoing-character read, removal, or `left++` executes in this outer iteration.
7. **Reach the end of the outer `for` body, then execute its update** `right++`: change `3 → 4`. Next evaluate `right < source.length()` with `4 < 13`; it is true, so begin the next outer iteration. There is no `continue;` statement in this method; the `for` loop performs this update automatically at the end of its body.

| right before | incoming | incoming if result | while body passes | left after | formedGroups after | bestStart / bestLength | windowCounts at end | for update / next test |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `3` | `B` | `true` | `0` | `0` | `2` | `0 / 2147483647` | `{'A': 1, 'D': 1, 'O': 1, 'B': 1}` | `right=4; 4<13 → true` |


**Outer `for` iteration — `right=4`, incoming character `E`.**

1. **At the outer `for` condition** `right < source.length()`: evaluate `4 < 13 → true`, so enter this iteration’s `for` body. `left=0`, `formedGroups=2`, `bestStart=0`, `bestLength=2147483647` before its statements.
2. **Inside the `for` body, execute** `char incoming = source.charAt(right);`: read `source.charAt(4)` and assign `incoming='E'`.
3. **Still inside the `for` body, execute** `windowCounts.merge(incoming, 1, Integer::sum);`: the count of `E` changes `0 → 1`. The complete map is now `{'A': 1, 'D': 1, 'O': 1, 'B': 1, 'E': 1}`.
4. **Evaluate the incoming-character `if` condition** `if (neededCounts.containsKey(incoming) && windowCounts.get(incoming).equals(neededCounts.get(incoming)))`: First `neededCounts.containsKey('E') → false`. Because the operands use `&&`, skip the second operand completely; no required-count lookup or `.equals(...)` call runs. The complete `if` condition is `false`.
5. **Skip that incoming-character `if` body**: `formedGroups++;` does not execute; `formedGroups` stays `2`. Execution remains in the outer `for` body and moves to the inner `while` condition.
6. **Evaluate the inner `while` condition** `while (formedGroups == requiredGroups)`: `2 == 3 → false`. Skip the entire `while` body: no candidate length, best-window `if`, outgoing-character read, removal, or `left++` executes in this outer iteration.
7. **Reach the end of the outer `for` body, then execute its update** `right++`: change `4 → 5`. Next evaluate `right < source.length()` with `5 < 13`; it is true, so begin the next outer iteration. There is no `continue;` statement in this method; the `for` loop performs this update automatically at the end of its body.

| right before | incoming | incoming if result | while body passes | left after | formedGroups after | bestStart / bestLength | windowCounts at end | for update / next test |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `4` | `E` | `false` | `0` | `0` | `2` | `0 / 2147483647` | `{'A': 1, 'D': 1, 'O': 1, 'B': 1, 'E': 1}` | `right=5; 5<13 → true` |


**Outer `for` iteration — `right=5`, incoming character `C`.**

1. **At the outer `for` condition** `right < source.length()`: evaluate `5 < 13 → true`, so enter this iteration’s `for` body. `left=0`, `formedGroups=2`, `bestStart=0`, `bestLength=2147483647` before its statements.
2. **Inside the `for` body, execute** `char incoming = source.charAt(right);`: read `source.charAt(5)` and assign `incoming='C'`.
3. **Still inside the `for` body, execute** `windowCounts.merge(incoming, 1, Integer::sum);`: the count of `C` changes `0 → 1`. The complete map is now `{'A': 1, 'D': 1, 'O': 1, 'B': 1, 'E': 1, 'C': 1}`.
4. **Evaluate the incoming-character `if` condition** `if (neededCounts.containsKey(incoming) && windowCounts.get(incoming).equals(neededCounts.get(incoming)))`: First `neededCounts.containsKey('C') → true`; therefore evaluate the second operand: `windowCounts.get('C').equals(neededCounts.get('C')) → 1 == 1 → true`. The complete `if` condition is `true`.
5. **Enter that incoming-character `if` body and execute** `formedGroups++;`: change `2 → 3`. Then leave the `if` body and move to the inner `while` condition.
6. **At the inner `while` condition** `while (formedGroups == requiredGroups)`: evaluate `3 == 3 → true`, so enter its body with `left=0`. This is contraction pass 1 of the current outer iteration; the pass label is explanatory, not a Java variable.
7. **Inside the `while` body, execute** `int candidateLength = right - left + 1;`: calculate `5 - 0 + 1 = 6` and assign `candidateLength=6`.
8. **Still inside the `while`, evaluate the best-window `if` condition** `if (candidateLength < bestLength)`: `6 < 2147483647 → true`.
9. **Enter the best-window `if` body**: execute `bestStart = left;`, changing `0 → 0`, then `bestLength = candidateLength;`, changing `2147483647 → 6`. Leave that `if` body; execution is still inside the same `while` body.
10. **Inside the `while` body, execute** `char outgoing = source.charAt(left);`: read `source.charAt(0)` and assign `outgoing='A'`.
11. **Inside the `while` body, execute** `windowCounts.merge(outgoing, -1, Integer::sum);`: decrement `A` from `1` to `0`. The map becomes `{'A': 0, 'D': 1, 'O': 1, 'B': 1, 'E': 1, 'C': 1}`; a zero-count entry is retained rather than removed.
12. **Inside the `while` body, execute** `left++;`: change `left` from `0` to `1`. Do not change `right`; it remains `5`.
13. **Inside the `while`, evaluate the outgoing-character `if` condition** `if (neededCounts.containsKey(outgoing) && windowCounts.get(outgoing) < neededCounts.get(outgoing))`: `neededCounts.containsKey('A') → true`, then `0 < 1 → true`. Thus this `if` condition is `true`.
14. **Enter the outgoing-character `if` body and execute** `formedGroups--;`: change `3 → 2`. Leave that `if` body, reach the closing brace of the `while` body, and go back to its condition.
15. **Re-evaluate the inner `while` condition** `while (formedGroups == requiredGroups)`: `2 == 3 → false`. Exit this inner `while`; do not run another contraction pass.
16. **Reach the end of the outer `for` body, then execute its update** `right++`: change `5 → 6`. Next evaluate `right < source.length()` with `6 < 13`; it is true, so begin the next outer iteration. There is no `continue;` statement in this method; the `for` loop performs this update automatically at the end of its body.

| right before | incoming | incoming if result | while body passes | left after | formedGroups after | bestStart / bestLength | windowCounts at end | for update / next test |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `5` | `C` | `true` | `1` | `1` | `2` | `0 / 6` | `{'A': 0, 'D': 1, 'O': 1, 'B': 1, 'E': 1, 'C': 1}` | `right=6; 6<13 → true` |


**Outer `for` iteration — `right=6`, incoming character `O`.**

1. **At the outer `for` condition** `right < source.length()`: evaluate `6 < 13 → true`, so enter this iteration’s `for` body. `left=1`, `formedGroups=2`, `bestStart=0`, `bestLength=6` before its statements.
2. **Inside the `for` body, execute** `char incoming = source.charAt(right);`: read `source.charAt(6)` and assign `incoming='O'`.
3. **Still inside the `for` body, execute** `windowCounts.merge(incoming, 1, Integer::sum);`: the count of `O` changes `1 → 2`. The complete map is now `{'A': 0, 'D': 1, 'O': 2, 'B': 1, 'E': 1, 'C': 1}`.
4. **Evaluate the incoming-character `if` condition** `if (neededCounts.containsKey(incoming) && windowCounts.get(incoming).equals(neededCounts.get(incoming)))`: First `neededCounts.containsKey('O') → false`. Because the operands use `&&`, skip the second operand completely; no required-count lookup or `.equals(...)` call runs. The complete `if` condition is `false`.
5. **Skip that incoming-character `if` body**: `formedGroups++;` does not execute; `formedGroups` stays `2`. Execution remains in the outer `for` body and moves to the inner `while` condition.
6. **Evaluate the inner `while` condition** `while (formedGroups == requiredGroups)`: `2 == 3 → false`. Skip the entire `while` body: no candidate length, best-window `if`, outgoing-character read, removal, or `left++` executes in this outer iteration.
7. **Reach the end of the outer `for` body, then execute its update** `right++`: change `6 → 7`. Next evaluate `right < source.length()` with `7 < 13`; it is true, so begin the next outer iteration. There is no `continue;` statement in this method; the `for` loop performs this update automatically at the end of its body.

| right before | incoming | incoming if result | while body passes | left after | formedGroups after | bestStart / bestLength | windowCounts at end | for update / next test |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `6` | `O` | `false` | `0` | `1` | `2` | `0 / 6` | `{'A': 0, 'D': 1, 'O': 2, 'B': 1, 'E': 1, 'C': 1}` | `right=7; 7<13 → true` |


**Outer `for` iteration — `right=7`, incoming character `D`.**

1. **At the outer `for` condition** `right < source.length()`: evaluate `7 < 13 → true`, so enter this iteration’s `for` body. `left=1`, `formedGroups=2`, `bestStart=0`, `bestLength=6` before its statements.
2. **Inside the `for` body, execute** `char incoming = source.charAt(right);`: read `source.charAt(7)` and assign `incoming='D'`.
3. **Still inside the `for` body, execute** `windowCounts.merge(incoming, 1, Integer::sum);`: the count of `D` changes `1 → 2`. The complete map is now `{'A': 0, 'D': 2, 'O': 2, 'B': 1, 'E': 1, 'C': 1}`.
4. **Evaluate the incoming-character `if` condition** `if (neededCounts.containsKey(incoming) && windowCounts.get(incoming).equals(neededCounts.get(incoming)))`: First `neededCounts.containsKey('D') → false`. Because the operands use `&&`, skip the second operand completely; no required-count lookup or `.equals(...)` call runs. The complete `if` condition is `false`.
5. **Skip that incoming-character `if` body**: `formedGroups++;` does not execute; `formedGroups` stays `2`. Execution remains in the outer `for` body and moves to the inner `while` condition.
6. **Evaluate the inner `while` condition** `while (formedGroups == requiredGroups)`: `2 == 3 → false`. Skip the entire `while` body: no candidate length, best-window `if`, outgoing-character read, removal, or `left++` executes in this outer iteration.
7. **Reach the end of the outer `for` body, then execute its update** `right++`: change `7 → 8`. Next evaluate `right < source.length()` with `8 < 13`; it is true, so begin the next outer iteration. There is no `continue;` statement in this method; the `for` loop performs this update automatically at the end of its body.

| right before | incoming | incoming if result | while body passes | left after | formedGroups after | bestStart / bestLength | windowCounts at end | for update / next test |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `7` | `D` | `false` | `0` | `1` | `2` | `0 / 6` | `{'A': 0, 'D': 2, 'O': 2, 'B': 1, 'E': 1, 'C': 1}` | `right=8; 8<13 → true` |


**Outer `for` iteration — `right=8`, incoming character `E`.**

1. **At the outer `for` condition** `right < source.length()`: evaluate `8 < 13 → true`, so enter this iteration’s `for` body. `left=1`, `formedGroups=2`, `bestStart=0`, `bestLength=6` before its statements.
2. **Inside the `for` body, execute** `char incoming = source.charAt(right);`: read `source.charAt(8)` and assign `incoming='E'`.
3. **Still inside the `for` body, execute** `windowCounts.merge(incoming, 1, Integer::sum);`: the count of `E` changes `1 → 2`. The complete map is now `{'A': 0, 'D': 2, 'O': 2, 'B': 1, 'E': 2, 'C': 1}`.
4. **Evaluate the incoming-character `if` condition** `if (neededCounts.containsKey(incoming) && windowCounts.get(incoming).equals(neededCounts.get(incoming)))`: First `neededCounts.containsKey('E') → false`. Because the operands use `&&`, skip the second operand completely; no required-count lookup or `.equals(...)` call runs. The complete `if` condition is `false`.
5. **Skip that incoming-character `if` body**: `formedGroups++;` does not execute; `formedGroups` stays `2`. Execution remains in the outer `for` body and moves to the inner `while` condition.
6. **Evaluate the inner `while` condition** `while (formedGroups == requiredGroups)`: `2 == 3 → false`. Skip the entire `while` body: no candidate length, best-window `if`, outgoing-character read, removal, or `left++` executes in this outer iteration.
7. **Reach the end of the outer `for` body, then execute its update** `right++`: change `8 → 9`. Next evaluate `right < source.length()` with `9 < 13`; it is true, so begin the next outer iteration. There is no `continue;` statement in this method; the `for` loop performs this update automatically at the end of its body.

| right before | incoming | incoming if result | while body passes | left after | formedGroups after | bestStart / bestLength | windowCounts at end | for update / next test |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `8` | `E` | `false` | `0` | `1` | `2` | `0 / 6` | `{'A': 0, 'D': 2, 'O': 2, 'B': 1, 'E': 2, 'C': 1}` | `right=9; 9<13 → true` |


**Outer `for` iteration — `right=9`, incoming character `B`.**

1. **At the outer `for` condition** `right < source.length()`: evaluate `9 < 13 → true`, so enter this iteration’s `for` body. `left=1`, `formedGroups=2`, `bestStart=0`, `bestLength=6` before its statements.
2. **Inside the `for` body, execute** `char incoming = source.charAt(right);`: read `source.charAt(9)` and assign `incoming='B'`.
3. **Still inside the `for` body, execute** `windowCounts.merge(incoming, 1, Integer::sum);`: the count of `B` changes `1 → 2`. The complete map is now `{'A': 0, 'D': 2, 'O': 2, 'B': 2, 'E': 2, 'C': 1}`.
4. **Evaluate the incoming-character `if` condition** `if (neededCounts.containsKey(incoming) && windowCounts.get(incoming).equals(neededCounts.get(incoming)))`: First `neededCounts.containsKey('B') → true`; therefore evaluate the second operand: `windowCounts.get('B').equals(neededCounts.get('B')) → 2 == 1 → false`. The complete `if` condition is `false`.
5. **Skip that incoming-character `if` body**: `formedGroups++;` does not execute; `formedGroups` stays `2`. Execution remains in the outer `for` body and moves to the inner `while` condition.
6. **Evaluate the inner `while` condition** `while (formedGroups == requiredGroups)`: `2 == 3 → false`. Skip the entire `while` body: no candidate length, best-window `if`, outgoing-character read, removal, or `left++` executes in this outer iteration.
7. **Reach the end of the outer `for` body, then execute its update** `right++`: change `9 → 10`. Next evaluate `right < source.length()` with `10 < 13`; it is true, so begin the next outer iteration. There is no `continue;` statement in this method; the `for` loop performs this update automatically at the end of its body.

| right before | incoming | incoming if result | while body passes | left after | formedGroups after | bestStart / bestLength | windowCounts at end | for update / next test |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `9` | `B` | `false` | `0` | `1` | `2` | `0 / 6` | `{'A': 0, 'D': 2, 'O': 2, 'B': 2, 'E': 2, 'C': 1}` | `right=10; 10<13 → true` |


**Outer `for` iteration — `right=10`, incoming character `A`.**

1. **At the outer `for` condition** `right < source.length()`: evaluate `10 < 13 → true`, so enter this iteration’s `for` body. `left=1`, `formedGroups=2`, `bestStart=0`, `bestLength=6` before its statements.
2. **Inside the `for` body, execute** `char incoming = source.charAt(right);`: read `source.charAt(10)` and assign `incoming='A'`.
3. **Still inside the `for` body, execute** `windowCounts.merge(incoming, 1, Integer::sum);`: the count of `A` changes `0 → 1`. The complete map is now `{'A': 1, 'D': 2, 'O': 2, 'B': 2, 'E': 2, 'C': 1}`.
4. **Evaluate the incoming-character `if` condition** `if (neededCounts.containsKey(incoming) && windowCounts.get(incoming).equals(neededCounts.get(incoming)))`: First `neededCounts.containsKey('A') → true`; therefore evaluate the second operand: `windowCounts.get('A').equals(neededCounts.get('A')) → 1 == 1 → true`. The complete `if` condition is `true`.
5. **Enter that incoming-character `if` body and execute** `formedGroups++;`: change `2 → 3`. Then leave the `if` body and move to the inner `while` condition.
6. **At the inner `while` condition** `while (formedGroups == requiredGroups)`: evaluate `3 == 3 → true`, so enter its body with `left=1`. This is contraction pass 1 of the current outer iteration; the pass label is explanatory, not a Java variable.
7. **Inside the `while` body, execute** `int candidateLength = right - left + 1;`: calculate `10 - 1 + 1 = 10` and assign `candidateLength=10`.
8. **Still inside the `while`, evaluate the best-window `if` condition** `if (candidateLength < bestLength)`: `10 < 6 → false`.
9. **Skip the best-window `if` body**: neither `bestStart = left;` nor `bestLength = candidateLength;` executes. They stay `0` and `6`. Continue with the remaining statements in this `while` body.
10. **Inside the `while` body, execute** `char outgoing = source.charAt(left);`: read `source.charAt(1)` and assign `outgoing='D'`.
11. **Inside the `while` body, execute** `windowCounts.merge(outgoing, -1, Integer::sum);`: decrement `D` from `2` to `1`. The map becomes `{'A': 1, 'D': 1, 'O': 2, 'B': 2, 'E': 2, 'C': 1}`; a zero-count entry is retained rather than removed.
12. **Inside the `while` body, execute** `left++;`: change `left` from `1` to `2`. Do not change `right`; it remains `10`.
13. **Inside the `while`, evaluate the outgoing-character `if` condition** `if (neededCounts.containsKey(outgoing) && windowCounts.get(outgoing) < neededCounts.get(outgoing))`: `neededCounts.containsKey('D') → false`, so `&&` skips `windowCounts.get(outgoing) < neededCounts.get(outgoing)` entirely. Thus this `if` condition is `false`.
14. **Skip the outgoing-character `if` body**: `formedGroups--;` does not execute; `formedGroups` stays `3`. Reach the closing brace of the `while` body, then go back to the `while` condition.
15. **At the inner `while` condition** `while (formedGroups == requiredGroups)`: evaluate `3 == 3 → true`, so enter its body with `left=2`. This is contraction pass 2 of the current outer iteration; the pass label is explanatory, not a Java variable.
16. **Inside the `while` body, execute** `int candidateLength = right - left + 1;`: calculate `10 - 2 + 1 = 9` and assign `candidateLength=9`.
17. **Still inside the `while`, evaluate the best-window `if` condition** `if (candidateLength < bestLength)`: `9 < 6 → false`.
18. **Skip the best-window `if` body**: neither `bestStart = left;` nor `bestLength = candidateLength;` executes. They stay `0` and `6`. Continue with the remaining statements in this `while` body.
19. **Inside the `while` body, execute** `char outgoing = source.charAt(left);`: read `source.charAt(2)` and assign `outgoing='O'`.
20. **Inside the `while` body, execute** `windowCounts.merge(outgoing, -1, Integer::sum);`: decrement `O` from `2` to `1`. The map becomes `{'A': 1, 'D': 1, 'O': 1, 'B': 2, 'E': 2, 'C': 1}`; a zero-count entry is retained rather than removed.
21. **Inside the `while` body, execute** `left++;`: change `left` from `2` to `3`. Do not change `right`; it remains `10`.
22. **Inside the `while`, evaluate the outgoing-character `if` condition** `if (neededCounts.containsKey(outgoing) && windowCounts.get(outgoing) < neededCounts.get(outgoing))`: `neededCounts.containsKey('O') → false`, so `&&` skips `windowCounts.get(outgoing) < neededCounts.get(outgoing)` entirely. Thus this `if` condition is `false`.
23. **Skip the outgoing-character `if` body**: `formedGroups--;` does not execute; `formedGroups` stays `3`. Reach the closing brace of the `while` body, then go back to the `while` condition.
24. **At the inner `while` condition** `while (formedGroups == requiredGroups)`: evaluate `3 == 3 → true`, so enter its body with `left=3`. This is contraction pass 3 of the current outer iteration; the pass label is explanatory, not a Java variable.
25. **Inside the `while` body, execute** `int candidateLength = right - left + 1;`: calculate `10 - 3 + 1 = 8` and assign `candidateLength=8`.
26. **Still inside the `while`, evaluate the best-window `if` condition** `if (candidateLength < bestLength)`: `8 < 6 → false`.
27. **Skip the best-window `if` body**: neither `bestStart = left;` nor `bestLength = candidateLength;` executes. They stay `0` and `6`. Continue with the remaining statements in this `while` body.
28. **Inside the `while` body, execute** `char outgoing = source.charAt(left);`: read `source.charAt(3)` and assign `outgoing='B'`.
29. **Inside the `while` body, execute** `windowCounts.merge(outgoing, -1, Integer::sum);`: decrement `B` from `2` to `1`. The map becomes `{'A': 1, 'D': 1, 'O': 1, 'B': 1, 'E': 2, 'C': 1}`; a zero-count entry is retained rather than removed.
30. **Inside the `while` body, execute** `left++;`: change `left` from `3` to `4`. Do not change `right`; it remains `10`.
31. **Inside the `while`, evaluate the outgoing-character `if` condition** `if (neededCounts.containsKey(outgoing) && windowCounts.get(outgoing) < neededCounts.get(outgoing))`: `neededCounts.containsKey('B') → true`, then `1 < 1 → false`. Thus this `if` condition is `false`.
32. **Skip the outgoing-character `if` body**: `formedGroups--;` does not execute; `formedGroups` stays `3`. Reach the closing brace of the `while` body, then go back to the `while` condition.
33. **At the inner `while` condition** `while (formedGroups == requiredGroups)`: evaluate `3 == 3 → true`, so enter its body with `left=4`. This is contraction pass 4 of the current outer iteration; the pass label is explanatory, not a Java variable.
34. **Inside the `while` body, execute** `int candidateLength = right - left + 1;`: calculate `10 - 4 + 1 = 7` and assign `candidateLength=7`.
35. **Still inside the `while`, evaluate the best-window `if` condition** `if (candidateLength < bestLength)`: `7 < 6 → false`.
36. **Skip the best-window `if` body**: neither `bestStart = left;` nor `bestLength = candidateLength;` executes. They stay `0` and `6`. Continue with the remaining statements in this `while` body.
37. **Inside the `while` body, execute** `char outgoing = source.charAt(left);`: read `source.charAt(4)` and assign `outgoing='E'`.
38. **Inside the `while` body, execute** `windowCounts.merge(outgoing, -1, Integer::sum);`: decrement `E` from `2` to `1`. The map becomes `{'A': 1, 'D': 1, 'O': 1, 'B': 1, 'E': 1, 'C': 1}`; a zero-count entry is retained rather than removed.
39. **Inside the `while` body, execute** `left++;`: change `left` from `4` to `5`. Do not change `right`; it remains `10`.
40. **Inside the `while`, evaluate the outgoing-character `if` condition** `if (neededCounts.containsKey(outgoing) && windowCounts.get(outgoing) < neededCounts.get(outgoing))`: `neededCounts.containsKey('E') → false`, so `&&` skips `windowCounts.get(outgoing) < neededCounts.get(outgoing)` entirely. Thus this `if` condition is `false`.
41. **Skip the outgoing-character `if` body**: `formedGroups--;` does not execute; `formedGroups` stays `3`. Reach the closing brace of the `while` body, then go back to the `while` condition.
42. **At the inner `while` condition** `while (formedGroups == requiredGroups)`: evaluate `3 == 3 → true`, so enter its body with `left=5`. This is contraction pass 5 of the current outer iteration; the pass label is explanatory, not a Java variable.
43. **Inside the `while` body, execute** `int candidateLength = right - left + 1;`: calculate `10 - 5 + 1 = 6` and assign `candidateLength=6`.
44. **Still inside the `while`, evaluate the best-window `if` condition** `if (candidateLength < bestLength)`: `6 < 6 → false`.
45. **Skip the best-window `if` body**: neither `bestStart = left;` nor `bestLength = candidateLength;` executes. They stay `0` and `6`. Continue with the remaining statements in this `while` body.
46. **Inside the `while` body, execute** `char outgoing = source.charAt(left);`: read `source.charAt(5)` and assign `outgoing='C'`.
47. **Inside the `while` body, execute** `windowCounts.merge(outgoing, -1, Integer::sum);`: decrement `C` from `1` to `0`. The map becomes `{'A': 1, 'D': 1, 'O': 1, 'B': 1, 'E': 1, 'C': 0}`; a zero-count entry is retained rather than removed.
48. **Inside the `while` body, execute** `left++;`: change `left` from `5` to `6`. Do not change `right`; it remains `10`.
49. **Inside the `while`, evaluate the outgoing-character `if` condition** `if (neededCounts.containsKey(outgoing) && windowCounts.get(outgoing) < neededCounts.get(outgoing))`: `neededCounts.containsKey('C') → true`, then `0 < 1 → true`. Thus this `if` condition is `true`.
50. **Enter the outgoing-character `if` body and execute** `formedGroups--;`: change `3 → 2`. Leave that `if` body, reach the closing brace of the `while` body, and go back to its condition.
51. **Re-evaluate the inner `while` condition** `while (formedGroups == requiredGroups)`: `2 == 3 → false`. Exit this inner `while`; do not run another contraction pass.
52. **Reach the end of the outer `for` body, then execute its update** `right++`: change `10 → 11`. Next evaluate `right < source.length()` with `11 < 13`; it is true, so begin the next outer iteration. There is no `continue;` statement in this method; the `for` loop performs this update automatically at the end of its body.

| right before | incoming | incoming if result | while body passes | left after | formedGroups after | bestStart / bestLength | windowCounts at end | for update / next test |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `10` | `A` | `true` | `5` | `6` | `2` | `0 / 6` | `{'A': 1, 'D': 1, 'O': 1, 'B': 1, 'E': 1, 'C': 0}` | `right=11; 11<13 → true` |


**Outer `for` iteration — `right=11`, incoming character `N`.**

1. **At the outer `for` condition** `right < source.length()`: evaluate `11 < 13 → true`, so enter this iteration’s `for` body. `left=6`, `formedGroups=2`, `bestStart=0`, `bestLength=6` before its statements.
2. **Inside the `for` body, execute** `char incoming = source.charAt(right);`: read `source.charAt(11)` and assign `incoming='N'`.
3. **Still inside the `for` body, execute** `windowCounts.merge(incoming, 1, Integer::sum);`: the count of `N` changes `0 → 1`. The complete map is now `{'A': 1, 'D': 1, 'O': 1, 'B': 1, 'E': 1, 'C': 0, 'N': 1}`.
4. **Evaluate the incoming-character `if` condition** `if (neededCounts.containsKey(incoming) && windowCounts.get(incoming).equals(neededCounts.get(incoming)))`: First `neededCounts.containsKey('N') → false`. Because the operands use `&&`, skip the second operand completely; no required-count lookup or `.equals(...)` call runs. The complete `if` condition is `false`.
5. **Skip that incoming-character `if` body**: `formedGroups++;` does not execute; `formedGroups` stays `2`. Execution remains in the outer `for` body and moves to the inner `while` condition.
6. **Evaluate the inner `while` condition** `while (formedGroups == requiredGroups)`: `2 == 3 → false`. Skip the entire `while` body: no candidate length, best-window `if`, outgoing-character read, removal, or `left++` executes in this outer iteration.
7. **Reach the end of the outer `for` body, then execute its update** `right++`: change `11 → 12`. Next evaluate `right < source.length()` with `12 < 13`; it is true, so begin the next outer iteration. There is no `continue;` statement in this method; the `for` loop performs this update automatically at the end of its body.

| right before | incoming | incoming if result | while body passes | left after | formedGroups after | bestStart / bestLength | windowCounts at end | for update / next test |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `11` | `N` | `false` | `0` | `6` | `2` | `0 / 6` | `{'A': 1, 'D': 1, 'O': 1, 'B': 1, 'E': 1, 'C': 0, 'N': 1}` | `right=12; 12<13 → true` |


**Outer `for` iteration — `right=12`, incoming character `C`.**

1. **At the outer `for` condition** `right < source.length()`: evaluate `12 < 13 → true`, so enter this iteration’s `for` body. `left=6`, `formedGroups=2`, `bestStart=0`, `bestLength=6` before its statements.
2. **Inside the `for` body, execute** `char incoming = source.charAt(right);`: read `source.charAt(12)` and assign `incoming='C'`.
3. **Still inside the `for` body, execute** `windowCounts.merge(incoming, 1, Integer::sum);`: the count of `C` changes `0 → 1`. The complete map is now `{'A': 1, 'D': 1, 'O': 1, 'B': 1, 'E': 1, 'C': 1, 'N': 1}`.
4. **Evaluate the incoming-character `if` condition** `if (neededCounts.containsKey(incoming) && windowCounts.get(incoming).equals(neededCounts.get(incoming)))`: First `neededCounts.containsKey('C') → true`; therefore evaluate the second operand: `windowCounts.get('C').equals(neededCounts.get('C')) → 1 == 1 → true`. The complete `if` condition is `true`.
5. **Enter that incoming-character `if` body and execute** `formedGroups++;`: change `2 → 3`. Then leave the `if` body and move to the inner `while` condition.
6. **At the inner `while` condition** `while (formedGroups == requiredGroups)`: evaluate `3 == 3 → true`, so enter its body with `left=6`. This is contraction pass 1 of the current outer iteration; the pass label is explanatory, not a Java variable.
7. **Inside the `while` body, execute** `int candidateLength = right - left + 1;`: calculate `12 - 6 + 1 = 7` and assign `candidateLength=7`.
8. **Still inside the `while`, evaluate the best-window `if` condition** `if (candidateLength < bestLength)`: `7 < 6 → false`.
9. **Skip the best-window `if` body**: neither `bestStart = left;` nor `bestLength = candidateLength;` executes. They stay `0` and `6`. Continue with the remaining statements in this `while` body.
10. **Inside the `while` body, execute** `char outgoing = source.charAt(left);`: read `source.charAt(6)` and assign `outgoing='O'`.
11. **Inside the `while` body, execute** `windowCounts.merge(outgoing, -1, Integer::sum);`: decrement `O` from `1` to `0`. The map becomes `{'A': 1, 'D': 1, 'O': 0, 'B': 1, 'E': 1, 'C': 1, 'N': 1}`; a zero-count entry is retained rather than removed.
12. **Inside the `while` body, execute** `left++;`: change `left` from `6` to `7`. Do not change `right`; it remains `12`.
13. **Inside the `while`, evaluate the outgoing-character `if` condition** `if (neededCounts.containsKey(outgoing) && windowCounts.get(outgoing) < neededCounts.get(outgoing))`: `neededCounts.containsKey('O') → false`, so `&&` skips `windowCounts.get(outgoing) < neededCounts.get(outgoing)` entirely. Thus this `if` condition is `false`.
14. **Skip the outgoing-character `if` body**: `formedGroups--;` does not execute; `formedGroups` stays `3`. Reach the closing brace of the `while` body, then go back to the `while` condition.
15. **At the inner `while` condition** `while (formedGroups == requiredGroups)`: evaluate `3 == 3 → true`, so enter its body with `left=7`. This is contraction pass 2 of the current outer iteration; the pass label is explanatory, not a Java variable.
16. **Inside the `while` body, execute** `int candidateLength = right - left + 1;`: calculate `12 - 7 + 1 = 6` and assign `candidateLength=6`.
17. **Still inside the `while`, evaluate the best-window `if` condition** `if (candidateLength < bestLength)`: `6 < 6 → false`.
18. **Skip the best-window `if` body**: neither `bestStart = left;` nor `bestLength = candidateLength;` executes. They stay `0` and `6`. Continue with the remaining statements in this `while` body.
19. **Inside the `while` body, execute** `char outgoing = source.charAt(left);`: read `source.charAt(7)` and assign `outgoing='D'`.
20. **Inside the `while` body, execute** `windowCounts.merge(outgoing, -1, Integer::sum);`: decrement `D` from `1` to `0`. The map becomes `{'A': 1, 'D': 0, 'O': 0, 'B': 1, 'E': 1, 'C': 1, 'N': 1}`; a zero-count entry is retained rather than removed.
21. **Inside the `while` body, execute** `left++;`: change `left` from `7` to `8`. Do not change `right`; it remains `12`.
22. **Inside the `while`, evaluate the outgoing-character `if` condition** `if (neededCounts.containsKey(outgoing) && windowCounts.get(outgoing) < neededCounts.get(outgoing))`: `neededCounts.containsKey('D') → false`, so `&&` skips `windowCounts.get(outgoing) < neededCounts.get(outgoing)` entirely. Thus this `if` condition is `false`.
23. **Skip the outgoing-character `if` body**: `formedGroups--;` does not execute; `formedGroups` stays `3`. Reach the closing brace of the `while` body, then go back to the `while` condition.
24. **At the inner `while` condition** `while (formedGroups == requiredGroups)`: evaluate `3 == 3 → true`, so enter its body with `left=8`. This is contraction pass 3 of the current outer iteration; the pass label is explanatory, not a Java variable.
25. **Inside the `while` body, execute** `int candidateLength = right - left + 1;`: calculate `12 - 8 + 1 = 5` and assign `candidateLength=5`.
26. **Still inside the `while`, evaluate the best-window `if` condition** `if (candidateLength < bestLength)`: `5 < 6 → true`.
27. **Enter the best-window `if` body**: execute `bestStart = left;`, changing `0 → 8`, then `bestLength = candidateLength;`, changing `6 → 5`. Leave that `if` body; execution is still inside the same `while` body.
28. **Inside the `while` body, execute** `char outgoing = source.charAt(left);`: read `source.charAt(8)` and assign `outgoing='E'`.
29. **Inside the `while` body, execute** `windowCounts.merge(outgoing, -1, Integer::sum);`: decrement `E` from `1` to `0`. The map becomes `{'A': 1, 'D': 0, 'O': 0, 'B': 1, 'E': 0, 'C': 1, 'N': 1}`; a zero-count entry is retained rather than removed.
30. **Inside the `while` body, execute** `left++;`: change `left` from `8` to `9`. Do not change `right`; it remains `12`.
31. **Inside the `while`, evaluate the outgoing-character `if` condition** `if (neededCounts.containsKey(outgoing) && windowCounts.get(outgoing) < neededCounts.get(outgoing))`: `neededCounts.containsKey('E') → false`, so `&&` skips `windowCounts.get(outgoing) < neededCounts.get(outgoing)` entirely. Thus this `if` condition is `false`.
32. **Skip the outgoing-character `if` body**: `formedGroups--;` does not execute; `formedGroups` stays `3`. Reach the closing brace of the `while` body, then go back to the `while` condition.
33. **At the inner `while` condition** `while (formedGroups == requiredGroups)`: evaluate `3 == 3 → true`, so enter its body with `left=9`. This is contraction pass 4 of the current outer iteration; the pass label is explanatory, not a Java variable.
34. **Inside the `while` body, execute** `int candidateLength = right - left + 1;`: calculate `12 - 9 + 1 = 4` and assign `candidateLength=4`.
35. **Still inside the `while`, evaluate the best-window `if` condition** `if (candidateLength < bestLength)`: `4 < 5 → true`.
36. **Enter the best-window `if` body**: execute `bestStart = left;`, changing `8 → 9`, then `bestLength = candidateLength;`, changing `5 → 4`. Leave that `if` body; execution is still inside the same `while` body.
37. **Inside the `while` body, execute** `char outgoing = source.charAt(left);`: read `source.charAt(9)` and assign `outgoing='B'`.
38. **Inside the `while` body, execute** `windowCounts.merge(outgoing, -1, Integer::sum);`: decrement `B` from `1` to `0`. The map becomes `{'A': 1, 'D': 0, 'O': 0, 'B': 0, 'E': 0, 'C': 1, 'N': 1}`; a zero-count entry is retained rather than removed.
39. **Inside the `while` body, execute** `left++;`: change `left` from `9` to `10`. Do not change `right`; it remains `12`.
40. **Inside the `while`, evaluate the outgoing-character `if` condition** `if (neededCounts.containsKey(outgoing) && windowCounts.get(outgoing) < neededCounts.get(outgoing))`: `neededCounts.containsKey('B') → true`, then `0 < 1 → true`. Thus this `if` condition is `true`.
41. **Enter the outgoing-character `if` body and execute** `formedGroups--;`: change `3 → 2`. Leave that `if` body, reach the closing brace of the `while` body, and go back to its condition.
42. **Re-evaluate the inner `while` condition** `while (formedGroups == requiredGroups)`: `2 == 3 → false`. Exit this inner `while`; do not run another contraction pass.
43. **Reach the end of the outer `for` body, then execute its update** `right++`: change `12 → 13`. Next evaluate `right < source.length()` with `13 < 13`; it is false, so exit the outer loop and proceed to the final no-answer `if` in Block 6. There is no `continue;` statement in this method; the `for` loop performs this update automatically at the end of its body.

| right before | incoming | incoming if result | while body passes | left after | formedGroups after | bestStart / bestLength | windowCounts at end | for update / next test |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `12` | `C` | `true` | `4` | `10` | `2` | `9 / 4` | `{'A': 1, 'D': 0, 'O': 0, 'B': 0, 'E': 0, 'C': 1, 'N': 1}` | `right=13; 13<13 → false` |

### Block 6 — Distinguish no answer from a real range

```java
if (bestLength == Integer.MAX_VALUE) {
    return "";
}
return source.substring(bestStart, bestStart + bestLength);
```

**What it evaluates:** unchanged sentinel returns empty; otherwise Java's exclusive substring end is `start + length`.

**Concrete result:** `bestStart = 9`, `bestLength = 4` calls `substring(9, 13)` and returns `"BANC"`.

**What fails without the sentinel check:** adding `Integer.MAX_VALUE` to `bestStart` overflows, producing an invalid substring boundary.

**Equivalent form:** a conditional expression can choose between the empty string and substring; the explicit `if` above matches the runnable reference and makes the branch visible.

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
