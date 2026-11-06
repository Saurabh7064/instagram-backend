# Valid Parentheses — Close the Most Recent Opening First

## Question

Given a string `text` containing only `(`, `)`, `[`, `]`, `{`, and `}`, return whether every opening bracket is closed by the same type in the correct nesting order. Every closing bracket must have an earlier unmatched opening. [Editable Java practice](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/stackqueue/parentheses/ValidParenthesesPractice.java) · [Problem](https://leetcode.com/problems/valid-parentheses/).

Our fixed input is `text="{[()()]}"`; the expected return is `true`. Positions 2–3 form `()`, positions 4–5 form `()`, positions 1–6 enclose both in `[]`, and positions 0–7 enclose everything in `{}`. Every pair has the correct type and no pair crosses another, so all eight characters are accounted for. These are positions in the string, separate from the code-line numbers shown by the learning UI.

Assume a non-null string of at most 10,000 characters, containing only those six bracket characters. The runner also supports the empty string as valid: there are no unmatched brackets. The method reads the immutable string and returns a boolean; it changes no caller-owned data. Aim for `O(n)` time and `O(n)` worst-case extra space.

## What you will learn

- Remember the closer required by each unmatched opening, with the newest requirement handled first.
- Recognize an unmatched closer, a wrong type, and an opening left over at the end.
- Explain why a last-in-first-out stack captures nesting and why counting pairs alone loses order.

## Start with what the brackets require

Read the fixed input from left to right. Seeing `{` creates a requirement to see `}` later. Seeing `[` inside it creates a newer requirement: finish `]` before finishing `}`. At `(` the newest requirement becomes `)`. The actual `)` at position 3 satisfies only that newest requirement. The earlier `]` and `}` requirements remain. Positions 4 and 5 add and satisfy another `)` requirement; then `]` and `}` satisfy the two older ones. Nothing remains outstanding.

The saved sequence must let us add and remove at the same end. That structure is a **stack**. Its **push** action adds an item at the top; its **pop** action removes and returns the top item. These are the three new terms in this lesson. We save the required closing characters rather than the openings themselves. With the top written on the left, the requirements after the first three characters are `[')', ']', '}']`; the next `)` can satisfy only the leftmost item.

Choose → record → check → remove → compare:

1. For an opening bracket, record its required closer on top of the previous requirements.
2. For a closing bracket, first check that a requirement exists; then remove the top requirement and compare its type with the current character.
3. Stop immediately on a missing requirement or a wrong type. After the last character, accept only when no requirement remains.

A count per bracket type cannot enforce this order. Even if the numbers of openings and closings balance, accepting a closer while another type is still on top would cross the nesting. Here the required order is `)` before `]` before `}`.

## Block-by-block code walkthrough

### Block 1 — Start with no pending requirements

```java
Deque<Character> expected = new ArrayDeque<>();
```

`Deque<Character>` is the Java interface type of the saved container; `ArrayDeque<>` supplies its implementation. We use only its stack operations. `Character` lets the container hold single bracket values; Java converts between `char` and `Character` automatically. `expected` starts as `[]` because no input has been read. Without the container, the method could not remember nested requirements. An equivalent more explicit declaration is `ArrayDeque<Character> expected = new ArrayDeque<Character>();`.

| Input read | expected (top → bottom) |
|---|---|
| none | `[]` |

There is no null guard: non-null input is the contract. `isEmpty()` tests whether the container has zero entries without removing anything; `pop()` needs at least one entry and would throw on an empty container.

### Block 2 — Read a character and handle its requirement

```java
for (int index = 0; index < text.length(); index++) {
    char current = text.charAt(index);
    if (current == '(') {
        expected.push(')');
    } else if (current == '[') {
        expected.push(']');
    } else if (current == '{') {
        expected.push('}');
    } else {
        if (expected.isEmpty() || expected.pop() != current) {
            return false;
        }
    }
}
```

`index=0` starts at the first character. `index < text.length()` permits exactly positions 0 through 7 here; using `<=` would read position 8, outside the string. `text.charAt(index)` reads that position without modifying the string. `index++` advances by one after handling the character; omitting it would process the same position forever. An equivalent loop can use an enhanced `for` over `text.toCharArray()`, but that allocates a copy and hides the input index we want to discuss.

The first three branches translate an opening into its closer and push that requirement. An earlier true branch skips all later branches in the chain. In the final branch the six-character contract means the current character is a closer. `||` evaluates left to right: if `expected.isEmpty()` is true, Java skips `expected.pop() != current`. This prevents removing from an empty container. Otherwise `pop()` removes and returns the required closer; `!=` compares its character with `current`. A mismatch returns `false`; no later character can repair the wrong nesting already seen. An equivalent clearer form splits this into an empty check, a local `char required = expected.pop()`, and a mismatch check. Without the empty check the method could throw instead of returning false; without the comparison it would accept the wrong bracket types. A failed comparison consumes a requirement, but the immediate return makes that local state irrelevant to further work.

**Narrated execution.** Fixed input: `text="{[()()]}"`, length 8. Initial values: `expected=[]`, `index=0`; `current` is declared inside each iteration and leaves scope after its body. Top is always on the left.

### Iteration index=0

- ✅ `for (index < text.length())`: `0 < 8 → true`; enter the body.
- `char current = text.charAt(index)` reads `text[0]`: `current='{'`.
- ❌ `if (current == '(')`: `'{' == '(' → false`; skip `expected.push(')')`.
- ❌ `else if (current == '[')`: `'{' == '[' → false`; skip `expected.push(']')`.
- ✅ `else if (current == '{')`: `'{' == '{' → true`; execute this branch.
- `expected.push('}')` adds the required closer: `[]` → `['}']`. Later opening checks and the closing branch are `Skipped`.
- `index++`: `0 → 1`; the next action is the loop check at index 1.

| Input index | current | index after increment | expected before (top → bottom) | expected after (top → bottom) | Next |
|---:|---|---:|---|---|---|
| 0 | `{` | 1 | `[]` | `['}']` | Check `1 < 8` |

### Iteration index=1

- ✅ `for (index < text.length())`: `1 < 8 → true`; enter the body.
- `char current = text.charAt(index)` reads `text[1]`: `current='['`.
- ❌ `if (current == '(')`: `'[' == '(' → false`; skip `expected.push(')')`.
- ✅ `else if (current == '[')`: `'[' == '[' → true`; execute this branch.
- `expected.push(']')` adds the required closer: `['}']` → `[']', '}']`. Later opening checks and the closing branch are `Skipped`.
- `index++`: `1 → 2`; the next action is the loop check at index 2.

| Input index | current | index after increment | expected before (top → bottom) | expected after (top → bottom) | Next |
|---:|---|---:|---|---|---|
| 1 | `[` | 2 | `['}']` | `[']', '}']` | Check `2 < 8` |

### Iteration index=2

- ✅ `for (index < text.length())`: `2 < 8 → true`; enter the body.
- `char current = text.charAt(index)` reads `text[2]`: `current='('`.
- ✅ `if (current == '(')`: `'(' == '(' → true`; execute this branch.
- `expected.push(')')` adds the required closer: `[']', '}']` → `[')', ']', '}']`. Later opening checks and the closing branch are `Skipped`.
- `index++`: `2 → 3`; the next action is the loop check at index 3.

| Input index | current | index after increment | expected before (top → bottom) | expected after (top → bottom) | Next |
|---:|---|---:|---|---|---|
| 2 | `(` | 3 | `[']', '}']` | `[')', ']', '}']` | Check `3 < 8` |

### Iteration index=3

- ✅ `for (index < text.length())`: `3 < 8 → true`; enter the body.
- `char current = text.charAt(index)` reads `text[3]`: `current=')'`.
- ❌ `if (current == '(')`: `')' == '(' → false`; skip `expected.push(')')`.
- ❌ `else if (current == '[')`: `')' == '[' → false`; skip `expected.push(']')`.
- ❌ `else if (current == '{')`: `')' == '{' → false`; skip `expected.push('}')`.
- Enter `else`; ❌ `expected.isEmpty()`: `[')', ']', '}']` has entries → `false`, so evaluate the second `||` operand.
- `expected.pop()` removes and returns `')'`: `[')', ']', '}']` → `[']', '}']`; ❌ `expected.pop() != current`: `')' != ')' → false`.
- ❌ `if (expected.isEmpty() || expected.pop() != current)`: `false || false → false`; skip `return false` and keep processing.
- `index++`: `3 → 4`; the next action is the loop check at index 4.

| Input index | current | index after increment | expected before (top → bottom) | expected after (top → bottom) | Next |
|---:|---|---:|---|---|---|
| 3 | `)` | 4 | `[')', ']', '}']` | `[']', '}']` | Check `4 < 8` |

### Iteration index=4

- ✅ `for (index < text.length())`: `4 < 8 → true`; enter the body.
- `char current = text.charAt(index)` reads `text[4]`: `current='('`.
- ✅ `if (current == '(')`: `'(' == '(' → true`; execute this branch.
- `expected.push(')')` adds the required closer: `[']', '}']` → `[')', ']', '}']`. Later opening checks and the closing branch are `Skipped`.
- `index++`: `4 → 5`; the next action is the loop check at index 5.

| Input index | current | index after increment | expected before (top → bottom) | expected after (top → bottom) | Next |
|---:|---|---:|---|---|---|
| 4 | `(` | 5 | `[']', '}']` | `[')', ']', '}']` | Check `5 < 8` |

### Iteration index=5

- ✅ `for (index < text.length())`: `5 < 8 → true`; enter the body.
- `char current = text.charAt(index)` reads `text[5]`: `current=')'`.
- ❌ `if (current == '(')`: `')' == '(' → false`; skip `expected.push(')')`.
- ❌ `else if (current == '[')`: `')' == '[' → false`; skip `expected.push(']')`.
- ❌ `else if (current == '{')`: `')' == '{' → false`; skip `expected.push('}')`.
- Enter `else`; ❌ `expected.isEmpty()`: `[')', ']', '}']` has entries → `false`, so evaluate the second `||` operand.
- `expected.pop()` removes and returns `')'`: `[')', ']', '}']` → `[']', '}']`; ❌ `expected.pop() != current`: `')' != ')' → false`.
- ❌ `if (expected.isEmpty() || expected.pop() != current)`: `false || false → false`; skip `return false` and keep processing.
- `index++`: `5 → 6`; the next action is the loop check at index 6.

| Input index | current | index after increment | expected before (top → bottom) | expected after (top → bottom) | Next |
|---:|---|---:|---|---|---|
| 5 | `)` | 6 | `[')', ']', '}']` | `[']', '}']` | Check `6 < 8` |

### Iteration index=6

- ✅ `for (index < text.length())`: `6 < 8 → true`; enter the body.
- `char current = text.charAt(index)` reads `text[6]`: `current=']'`.
- ❌ `if (current == '(')`: `']' == '(' → false`; skip `expected.push(')')`.
- ❌ `else if (current == '[')`: `']' == '[' → false`; skip `expected.push(']')`.
- ❌ `else if (current == '{')`: `']' == '{' → false`; skip `expected.push('}')`.
- Enter `else`; ❌ `expected.isEmpty()`: `[']', '}']` has entries → `false`, so evaluate the second `||` operand.
- `expected.pop()` removes and returns `']'`: `[']', '}']` → `['}']`; ❌ `expected.pop() != current`: `']' != ']' → false`.
- ❌ `if (expected.isEmpty() || expected.pop() != current)`: `false || false → false`; skip `return false` and keep processing.
- `index++`: `6 → 7`; the next action is the loop check at index 7.

| Input index | current | index after increment | expected before (top → bottom) | expected after (top → bottom) | Next |
|---:|---|---:|---|---|---|
| 6 | `]` | 7 | `[']', '}']` | `['}']` | Check `7 < 8` |

### Iteration index=7

- ✅ `for (index < text.length())`: `7 < 8 → true`; enter the body.
- `char current = text.charAt(index)` reads `text[7]`: `current='}'`.
- ❌ `if (current == '(')`: `'}' == '(' → false`; skip `expected.push(')')`.
- ❌ `else if (current == '[')`: `'}' == '[' → false`; skip `expected.push(']')`.
- ❌ `else if (current == '{')`: `'}' == '{' → false`; skip `expected.push('}')`.
- Enter `else`; ❌ `expected.isEmpty()`: `['}']` has entries → `false`, so evaluate the second `||` operand.
- `expected.pop()` removes and returns `'}'`: `['}']` → `[]`; ❌ `expected.pop() != current`: `'}' != '}' → false`.
- ❌ `if (expected.isEmpty() || expected.pop() != current)`: `false || false → false`; skip `return false` and keep processing.
- `index++`: `7 → 8`; the next action is the loop check at index 8.

| Input index | current | index after increment | expected before (top → bottom) | expected after (top → bottom) | Next |
|---:|---|---:|---|---|---|
| 7 | `}` | 8 | `['}']` | `[]` | Check `8 < 8` |

- ❌ `for (index < text.length())`: `8 < 8 → false`; skip the body and leave the loop. Loop-local `index` leaves scope; the saved container is `expected=[]`.

### Block 3 — Reject any requirement left outstanding

```java
return expected.isEmpty();
```

`expected.isEmpty()` evaluates `[]` to `true`, so `return` produces `true` and exits the method. This is a value returned to the caller, not a mutation of the container. Replacing it with unconditional `true` would accept an unfinished opening. A more explicit equivalent is `boolean allClosed = expected.isEmpty(); return allClosed;`.

| Input consumed | expected | Returned value |
|---|---|---|
| all 8 characters | `[]` | `true` |

## Complete reference solution

Runnable reference: [ValidParenthesesSolution.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/stackqueue/parentheses/ValidParenthesesSolution.java). Imports are `java.util.Deque` and `java.util.ArrayDeque`. The full algorithm is below; the linked class supplies `main` and ten checks.

```java
// text index: 0 1 2 3 4 5 6 7
// text      : { [ ( ) ( ) ] }
// Input: text="{[()()]}"
// Expected return: true.
public static boolean isValid(String text) {
        Deque<Character> expected = new ArrayDeque<>();
        for (int index = 0; index < text.length(); index++) {
            char current = text.charAt(index);
            if (current == '(') {
                expected.push(')');
            } else if (current == '[') {
                expected.push(']');
            } else if (current == '{') {
                expected.push('}');
            } else {
                if (expected.isEmpty() || expected.pop() != current) {
                    return false;
                }
            }
        }
        return expected.isEmpty();
    }
```

## Why it works and what it costs

Before each iteration, `expected` contains exactly the required closers for the unmatched openings in the already-read portion, newest first. Initially there are none. Reading an opening adds exactly its required closer. Reading a closer can preserve correct nesting only by satisfying the newest requirement; a missing requirement or mismatched type proves the input invalid immediately. After every valid character the same fact holds. At the end an empty stack proves every opening was closed, while a nonempty stack proves at least one was not. This always-held statement is the algorithm's invariant.

Each of `n` characters is read once, and each opening causes one push and at most one pop. ArrayDeque's end operations take amortized constant time (occasional storage growth is spread over all insertions), giving `O(n)` total time. Up to `n` requirements can be saved, so worst-case extra space is `O(n)`. Repeatedly removing adjacent matching pairs until no pair remains is a useful small-input alternative, but repeatedly scanning/copying the remaining string can take `O(n²)` time. A recursive nested parser can express the grammar but adds Java call-stack depth limits; it is not needed for this one idea.

## Pitfalls and boundaries

- Save the required closer on an opening, not the opening itself, or the direct comparison at positions 3, 5, 6, and 7 would be against the wrong character.
- Check empty before pop. Java's left-to-right short circuit makes an initial unmatched closer return false safely.
- A count cannot choose which bracket must close next; the saved order does.
- Empty input runs zero iterations and returns true. Null and characters outside the six-character alphabet are excluded by the stated contract; supporting arbitrary text would require a separate policy.
- `ArrayDeque` rejects null entries. Here only concrete closing characters are added. Caller input is immutable; all saved state belongs to this call.

## Practice after learning the concept

Open [ValidParenthesesPractice.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/stackqueue/parentheses/ValidParenthesesPractice.java) in IntelliJ. Implement the marked `isValid` TODO and run its `main`. Checks cover the fixed nested input, adjacent pairs, empty, incomplete openings, wrong or crossing types, extra closers, and deep nesting. The initial readiness message means the TODO has not been implemented; it is not a passing implementation. Reveal the separate reference only after your attempt.

## Questions and explained answers

<details>
<summary>1. At index 3, why does ')' remove only the top requirement?</summary>

The saved state is `[')', ']', '}']`. The opening at index 2 is inside both earlier openings, so it must close first. Removing the top `)` leaves two entries: `']'` followed by `'}'`. Neither outer requirement has been satisfied yet.

</details>

<details>
<summary>2. Why test empty before comparing the result of pop?</summary>

An unmatched closer has no corresponding required closer. `expected.isEmpty()` then returns true, and `||` skips `pop()`, so the method returns false without throwing. Reversing these checks could remove from an empty container.

</details>

<details>
<summary>3. Why is reaching the end insufficient by itself?</summary>

Reaching the end proves only that no invalid closer was found. An opening can still be waiting for its closer, so the final result must test whether the saved requirements are empty. For the fixed input they are empty after the final `}`, giving true.

</details>

## Stop/go

Artifact readiness requires both Java files to compile, the reference checks to pass, and browser proof to be reviewed. Learning remains `TODO`; code attempt and comprehension are separate. Go only after your practice passes, you explain the saved-state invariant and `O(n)` bounds, predict what happens at index 3 without reading the reference, and answer all three checkpoints. Otherwise remain on this lesson when it becomes active; preparing it does not replace the current Two Sum session.
