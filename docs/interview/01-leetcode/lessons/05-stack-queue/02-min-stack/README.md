# Micro-Lesson 02 — Min Stack

## Question

Design a stack supporting `push(value)`, `pop()`, `top()`, and `getMin()`: add a value, remove the newest value, inspect the newest value, and inspect the smallest value still stored. Aim for constant time per operation. `pop`, `top`, and `getMin` are called only when at least one value remains.

Problem: [Min Stack on LeetCode](https://leetcode.com/problems/min-stack/) · Editable Java: [MinStackPractice.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/stackqueue/minstack/MinStackPractice.java)

## What you will learn

- Save the smallest value alongside each added value so removing an entry restores the earlier minimum.
- Explain why repeated equal minima need their own saved state.
- Separate the newest value from the smallest value, and state the actual time and space costs.

## Lesson status and prerequisites

Learning status: `TODO`. This is a prepared artifact; no implementation attempt or understanding is recorded. Prerequisite: adding, inspecting, and removing the newest item in [Valid Parentheses](../01-valid-parentheses/README.md), plus Java classes and two-cell integer arrays. Primary idea: save the minimum for each stored depth. New terms: **entry**, **saved minimum**. The stack operations are reviewed below before the new state is introduced.

## Inputs and limits

Values may use the full Java `int` range, `-2,147,483,648` through `2,147,483,647`; allow up to `30,000` operation calls. No operation that reads or removes an entry is called on an empty stack. `push` and `pop` return no value; `top` and `getMin` return an `int`. Duplicate values are allowed. Target `O(1)` time per operation and `O(n)` storage for `n` retained values. The chosen Java array-backed implementation makes `push` amortized `O(1)`, as explained after the code.

## First understand the idea

Imagine adding numbers to a pile and removing only the most recently added number. This is a **stack**: the newest retained number is the **top**. The smallest retained number may be elsewhere in the pile. Finding it by inspecting every number after every removal would make a minimum query slower as the pile grows.

Use this one fixed sequence throughout the lesson. Operation positions identify calls; they are not array cell indexes or code line numbers.

| Operation position | Call | Retained values, newest first | Requested result and derivation |
| --- | --- | --- | --- |
| `0` | `push(3)` | `[3]` | No result; the only value is `3`. |
| `1` | `push(1)` | `[1,3]` | No result; among `1` and `3`, the smallest is `1`. |
| `2` | `push(1)` | `[1,1,3]` | No result; both copies of `1` are retained. |
| `3` | `getMin()` | `[1,1,3]` | Return `1`, the smallest retained value. |
| `4` | `pop()` | `[1,3]` | No result; remove only the newest `1`. |
| `5` | `getMin()` | `[1,3]` | Return `1`: the other copy remains. |
| `6` | `pop()` | `[3]` | No result; remove the remaining `1`. |
| `7` | `top()` | `[3]` | Return `3`, the newest remaining value. |
| `8` | `getMin()` | `[3]` | Return `3`: it is also the only remaining value. |
| `9` | `pop()` | `[]` | No result; all values have now been removed. |

The query results in call order are therefore `1,1,3,3`. The last two results are equal because only `3` remains, not because `top()` and `getMin()` always mean the same thing.

Now save a little more information when adding each number. One **entry** is a two-cell array: cell `[0]` holds the added value and cell `[1]` holds the smallest value among that entry and all entries below it. Call the second cell its **saved minimum**. At operation 0, store `[3,3]`. At operation 1, compare the new value `1` with the earlier minimum `3`, giving `min(1,3)=1`, and store `[1,1]`. Operation 2 compares `1` with the current minimum `1`, giving `min(1,1)=1`, and stores a separate `[1,1]`.

The saved entries, newest first, are now `[[1,1],[1,1],[3,3]]`. Removing one entry reveals another `[1,1]`, so the minimum stays `1`; removing the second reveals `[3,3]`, so it becomes `3`. Earlier entries need no edits: adding something above them never changes what is at or below their own depth.

The general rule is: on an empty stack, the new minimum is the added value; otherwise it is `min(added value, current saved minimum)`. Add `[value,new minimum]` together, and remove that pair together. This keeps value history and minimum history aligned, including equal minima.

## Block-by-block code walkthrough

### Block 1 — Keep the entry pairs together

```java
private final Deque<int[]> entries = new ArrayDeque<>();
```

`Deque<int[]>` declares a Java container of integer-array entries; `ArrayDeque` supplies its storage. We use only one end as the stack top: `push` adds there, `pop` removes there, and `peek` reads there without removing. `final` keeps the field pointing to this container while its contents remain mutable. A new `MinStack` creates `entries=[]`. An ordinary `ArrayList<int[]>` with operations at its end could express the same ownership, but the named stack operations make the intended end explicit.

Every table below shows the complete entries **newest first**, matching these API operations. Within one entry, `[0]` means its value and `[1]` means its saved minimum. These cell indexes have a different meaning from operation positions.

| Object just constructed | Complete entries | Next call |
| --- | --- | --- |
| `stack` | `[]` | Operation `0`: `push(3)` |

### Block 2 — Add a value with the minimum needed at this depth

```java
public void push(int value) {
    int minimum = entries.isEmpty() ? value : Math.min(value, entries.peek()[1]);
    entries.push(new int[] {value, minimum});
}
```

The `condition ? first : second` expression evaluates exactly one result branch. It avoids reading `peek()[1]` when no entry exists. `Math.min` compares the two integers directly; it does not subtract them, so the full `int` range does not cause an arithmetic overflow here. `new int[] {value, minimum}` creates an independent pair for this call. Reusing one mutable array for every call would make older entries change when a newer entry is written.

An equivalent clearer conditional is `int minimum; if (entries.isEmpty()) minimum=value; else minimum=Math.min(value,entries.peek()[1]);`. The variable `minimum` is local to one `push` call; each call creates a fresh local.

**Narrated execution** — fixed calls are the ten operations above; initial `entries=[]`. The methods contain no loops, so this walkthrough follows each actual call rather than inventing loop iterations.

**Operation `0`: `push(3)`.**

- `push(int value)` receives `value=3`.
- ✅ `entries.isEmpty()`: `[]` has no entries → `true`; select `value=3`. `Math.min(value, entries.peek()[1])` is `Skipped`, so no missing entry is read.
- `int minimum = ...` initializes this call's `minimum=3`.
- `entries.push(new int[] {value, minimum})` creates `[3,3]` and changes `entries`: `[] → [[3,3]]`.
- The `void` method finishes without a returned value; `value` and `minimum` leave scope. Next call is operation `1`.

| Operation | Argument | Local minimum | Entries before | Entries after | Next |
| --- | --- | --- | --- | --- | --- |
| `0` | `3` | `3` | `[]` | `[[3,3]]` | `push(1)` |

**Operation `1`: `push(1)`.**

- `push(int value)` receives `value=1`.
- ❌ `entries.isEmpty()`: `[[3,3]]` has an entry → `false`; the first ternary result is `Skipped`. `entries.peek()` reads `[3,3]` without mutation; `[1]` reads its saved minimum `3`.
- `Math.min(value, entries.peek()[1])` evaluates `min(1,3)=1`; `int minimum = ...` initializes `minimum=1`.
- `entries.push(new int[] {value, minimum})` creates `[1,1]`; `entries`: `[[3,3]] → [[1,1],[3,3]]`.
- The `void` method finishes; locals leave scope. Next call is operation `2`.

| Operation | Argument | Local minimum | Entries before | Entries after | Next |
| --- | --- | --- | --- | --- | --- |
| `1` | `1` | `1` | `[[3,3]]` | `[[1,1],[3,3]]` | `push(1)` |

**Operation `2`: `push(1)`.**

- `push(int value)` receives `value=1`.
- ❌ `entries.isEmpty()`: `[[1,1],[3,3]]` is nonempty → `false`; the first ternary result is `Skipped`. `entries.peek()[1]` reads `1` from the newest `[1,1]` without changing the container.
- `Math.min(value, entries.peek()[1])` evaluates `min(1,1)=1`; this call's `minimum=1`.
- `entries.push(new int[] {value, minimum})` creates a distinct `[1,1]`; `entries`: `[[1,1],[3,3]] → [[1,1],[1,1],[3,3]]`.
- The `void` method finishes; locals leave scope. Next call is operation `3`.

| Operation | Argument | Local minimum | Entries before | Entries after | Next |
| --- | --- | --- | --- | --- | --- |
| `2` | `1` | `1` | `[[1,1],[3,3]]` | `[[1,1],[1,1],[3,3]]` | `getMin()` |

### Block 3 — Inspect the saved minimum, then remove whole entries

```java
public int getMin() {
    return entries.peek()[1];
}

public void pop() {
    entries.pop();
}
```

`getMin` reads the top pair's second cell, whose definition includes all retained values. It does not remove the pair. Returning `[0]` would answer the newest-value question instead; the two cell meanings must remain distinct. Equivalent clearer reads are `int[] newest=entries.peek(); return newest[1];`.

`pop` removes a complete pair; Java's container returns the removed pair, but this `void` method deliberately discards it because the requested API has no return value. Removing only a value from separately maintained storage without removing its minimum history would misalign future queries. There is no empty guard because the stated call contract guarantees nonempty input to these methods. Calling them on an empty container is outside this lesson's contract; `peek` would return `null`, and dereferencing it would fail.

**Operation `3`: `getMin()`.**

- `return entries.peek()[1]` reads top entry `[1,1]`, reads cell `[1]=1`, and returns `1` immediately. `entries` stays `[[1,1],[1,1],[3,3]]`.
- The return ends this call; next call is operation `4`.

| Operation | Complete entries before and after | Returned value | Next |
| --- | --- | --- | --- |
| `3` | `[[1,1],[1,1],[3,3]]` | `1` | `pop()` |

**Operation `4`: `pop()`.**

- `entries.pop()` removes the newest `[1,1]`; `entries`: `[[1,1],[1,1],[3,3]] → [[1,1],[3,3]]`. The removed array is discarded.
- The `void` method finishes; next call is operation `5`.

| Operation | Entries before | Entries after | Next |
| --- | --- | --- | --- |
| `4` | `[[1,1],[1,1],[3,3]]` | `[[1,1],[3,3]]` | `getMin()` |

**Operation `5`: `getMin()`.**

- `return entries.peek()[1]` reads the remaining `[1,1]`, reads cell `[1]=1`, and returns `1`. `entries` stays `[[1,1],[3,3]]`.
- The return ends this call; next call is operation `6`. One equal minimum's removal did not remove the other copy's saved state.

| Operation | Complete entries before and after | Returned value | Next |
| --- | --- | --- | --- |
| `5` | `[[1,1],[3,3]]` | `1` | `pop()` |

**Operation `6`: `pop()`.**

- `entries.pop()` removes `[1,1]`; `entries`: `[[1,1],[3,3]] → [[3,3]]`. The removed pair is discarded; no minimum needs recomputing.
- The `void` method finishes; next call is operation `7`.

| Operation | Entries before | Entries after | Next |
| --- | --- | --- | --- |
| `6` | `[[1,1],[3,3]]` | `[[3,3]]` | `top()` |

### Block 4 — Read the newest value and finish the sequence

```java
public int top() {
    return entries.peek()[0];
}
```

`peek()[0]` reads the value cell of the newest retained entry without changing any entry. `peek()[1]` answers a different question, even though both happen to contain `3` in this final state. Equivalent clearer reads are `int[] newest=entries.peek(); return newest[0];`.

**Operation `7`: `top()`.**

- `return entries.peek()[0]` reads `[3,3]`, reads cell `[0]=3`, and returns `3`; `entries` stays `[[3,3]]`.
- The return ends this call; next call is operation `8`.

| Operation | Complete entries before and after | Returned value | Next |
| --- | --- | --- | --- |
| `7` | `[[3,3]]` | `3` | `getMin()` |

**Operation `8`: `getMin()`.**

- `return entries.peek()[1]` reads `[3,3]`, reads cell `[1]=3`, and returns `3`; `entries` stays `[[3,3]]`.
- The return ends this call; next call is operation `9`.

| Operation | Complete entries before and after | Returned value | Next |
| --- | --- | --- | --- |
| `8` | `[[3,3]]` | `3` | `pop()` |

**Operation `9`: `pop()`.**

- `entries.pop()` removes `[3,3]`; `entries`: `[[3,3]] → []`. Its returned pair is discarded.
- The `void` method finishes. The call sequence ends with no entries and query results `1,1,3,3`; no read is attempted on the empty container. No loop termination check exists in these four methods.

| Operation | Entries before | Entries after | Next |
| --- | --- | --- | --- |
| `9` | `[[3,3]]` | `[]` | Sequence complete |

## Complete reference solution

Directly runnable reference and checks: [MinStackSolution.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/stackqueue/minstack/MinStackSolution.java). The full class being implemented is below; the linked file wraps it in a runnable class. The operation guide describes calls, not input-array indexes. Imports are `java.util.ArrayDeque` and `java.util.Deque`.

```java
// Input: the calls below on one initially empty MinStack.
// Operation position:   0        1        2        3       4      5       6      7      8       9
// Calls:             push(3)  push(1)  push(1)  getMin()  pop()  getMin() pop()  top() getMin() pop()
// Expected query returns in order: 1, 1, 3, 3. Final retained entries: [].
public static final class MinStack {
    private final Deque<int[]> entries = new ArrayDeque<>();

    public void push(int value) {
        int minimum = entries.isEmpty() ? value : Math.min(value, entries.peek()[1]);
        entries.push(new int[] {value, minimum});
    }

    public void pop() {
        entries.pop();
    }

    public int top() {
        return entries.peek()[0];
    }

    public int getMin() {
        return entries.peek()[1];
    }
}
```

## Why it is correct and what it costs

The invariant is: every entry's second cell is the minimum of its own value and every value below it. The first entry satisfies this because its only value is its minimum. A push preserves all older entries and establishes the new entry's minimum by comparing its value with the old top's minimum. A pop discards only the newest pair; every remaining entry still has exactly the values at or below it that it had before. Therefore the exposed top's second cell is always the current global minimum, including when duplicate minima are removed one at a time.

For `n` retained values, the structure stores `n` pairs: `O(n)` space. `top`, `getMin`, and `pop` take `O(1)` time. `ArrayDeque.push` takes **amortized `O(1)`**: over many pushes the total growth/copying work is linear, but an individual capacity expansion can take `O(n)`. If the requirement literally forbids a slow individual push, linked entries avoid array resizing, with one extra object/link per entry. This implementation makes its Java container's limit explicit rather than claiming a worst-case bound it does not supply.

Keeping only one current-minimum variable loses the old minimum after operation `6`; it cannot recover `3` without scanning or saved history. A separate minimum stack can work, but it must preserve equal minimum occurrences; our paired entries make that synchronization automatic. No addition, subtraction, or sentinel value is needed, so both integer extremes remain legitimate stored values.

## Try it in IntelliJ

Open [MinStackPractice.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/stackqueue/minstack/MinStackPractice.java). Implement only its `MinStack` state and four TODO methods, then run `main`. Its checks cover the fixed sequence, repeated minima, the full integer range, and reuse after every entry was removed. The initial “Practice ready” message means the TODO remains; it is not a passing implementation. Assertion failures remain visible because only `UnsupportedOperationException` is caught.

## Checkpoints and explained answers

<details>
<summary>1. After operation 4 removes one 1, why must getMin still return 1?</summary>

There were two separate entries `[1,1]`. Removing the newest leaves `[[1,1],[3,3]]`, whose top still saves minimum `1`. Equal values represent separate retained occurrences; removing one does not remove both.
</details>

<details>
<summary>2. What invariant lets operation 6 restore minimum 3 without a search?</summary>

Each entry saves the minimum of its value and all entries below it. The `[3,3]` entry was never changed by either later push. Once both `[1,1]` entries are removed, `[3,3]` is exposed and its second cell already contains the correct minimum `3`.
</details>

<details>
<summary>3. Does ArrayDeque make every individual push worst-case constant time?</summary>

No. A capacity expansion may copy the retained entries, taking linear time for that call. Push is amortized constant time over a sequence; the other three shown operations are constant time. State that qualification, and distinguish the `O(n)` saved-state space from query time.
</details>

## Stop/go criteria

Stop when you cannot explain what the two cells mean or why the earlier minimum survives a removal. Go only after your own practice passes, you explain the invariant and time/space bounds in plain language, and you predict the two minimum queries on either side of operation `4` without opening the answers. Artifact checks alone leave learning status `TODO`.
