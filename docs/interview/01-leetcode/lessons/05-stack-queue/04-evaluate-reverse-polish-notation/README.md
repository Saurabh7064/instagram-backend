# Micro-Lesson 04 — Evaluate Reverse Polish Notation

## Question

Given a valid arithmetic expression as an array of strings in reverse Polish notation, return its integer result. Each operator is one of `+`, `-`, `*`, or `/` and uses the two results immediately preceding it. Integer division truncates toward zero. The expression has no division by zero, and every intermediate result fits a Java `int`.

Problem: [Evaluate Reverse Polish Notation on LeetCode](https://leetcode.com/problems/evaluate-reverse-polish-notation/) · Editable Java: [EvaluateRpnPractice.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/stackqueue/rpn/EvaluateRpnPractice.java)

## What you will learn

- Replace two completed calculations with their combined result when an operator arrives.
- Preserve left and right operand order when removing the newest saved values.
- Explain how integer division and numeric strings affect expression evaluation.

## Lesson status and prerequisites

Learning status: `TODO`; this artifact is prepared, not activated or mastered. Prerequisites: Java integer arithmetic, arrays, loops, and the newest-first stack model from [Valid Parentheses](../01-valid-parentheses/README.md). Primary idea: keep completed values until their following operation can combine them. New terms: **token**, **postfix notation**, **operand**. Java APIs are explained at their first use below.

## Inputs and limits

Use `1 <= tokens.length <= 10,000`. Each string is a whole integer in `[-200,200]` or exactly one of the four operators. The array and its strings are nonnull. The expression is valid: every operator has two available operands, the divisor is nonzero, and exactly one result remains at the end. Intermediate results and the final answer fit the full signed 32-bit integer range even when they exceed the input-number range. These promises let the method focus on evaluation rather than syntax validation. Target `O(n)` time and `O(n)` auxiliary space for `n` tokens; input strings are read without mutation.

## First understand the idea

The fixed input is `tokens=["4","13","5","/","+","2","*"]`. Return the integer value represented by the complete expression. Work out that requested value before choosing saved-state names:

```text
index:     0    1    2    3    4    5    6
tokens:  "4" "13"  "5"  "/"  "+"  "2"  "*"
```

Read from left to right. The `/` comes after `13` and `5`, so combine those two values first: `13 ÷ 5 = 2` with remainder `3`; the integer result is `2`. The `+` then combines the earlier `4` and that completed `2`, giving `4 + 2 = 6`. The final `*` combines the completed `6` with the later `2`, giving `6 × 2 = 12`. Therefore the requested return is `12`, representing `(4 + (13 / 5)) * 2` with integer division.

One input string is a **token**: one number or one operation. Writing the operation after its two inputs is **postfix notation**, also called reverse Polish notation. An **operand** is a value used by an operation; division's left operand here is `13` and its right operand is `5`. The placement tells us which completed values belong together, so no parentheses need storing.

Keep each completed value that has not yet been combined with an earlier value. Whenever a number arrives, add it to the newest end of a stack. Whenever an operation arrives, take the two newest values, calculate their result, and put that result back at the newest end. At `/`, the values are newest-first `[5,13,4]`: the first removed value `5` is the **right** operand; the second removed value `13` is the **left** operand. Replacing them with `13/5=2` leaves `[2,4]`. The earlier `4` is waiting for the following `+`.

This explains the action order: read a token → decide number or operation → save a number, or remove right then left → calculate `left operator right` → save the result. Reversing removal names would compute `5/13=0`, losing the expression's meaning. Addition and multiplication can hide this mistake because swapping their inputs does not change their result; subtraction and division cannot.

Java integer `/` discards a fractional part toward zero. In this fixed expression, `13/5` gives `2`, not a rounded `3`. For negative results, “toward zero” still applies; do not replace this with flooring toward negative infinity. Additional negative and operand-order checks live in the runnable files, while this lesson keeps one fixed expression throughout.

## Block-by-block code walkthrough

### Block 1 — Start with no completed values

```java
Deque<Integer> values = new ArrayDeque<>();
```

`Deque<Integer>` names a Java container of integer values; `ArrayDeque` provides storage. We use its newest end only: `push(value)` adds there, `pop()` removes and returns there. Java stores `int` values as `Integer` objects for this container and converts them back when assigning to an `int`. The fixed expression begins with no completed values, so allocate `values=[]`. Using an ordinary integer variable alone would lose earlier unfinished work such as `4` while evaluating `13/5`.

The complete collection states below are **newest first**. For example, `[5,13,4]` means the next removed value is `5`, not `4`. This displayed order is distinct from the input array's left-to-right order.

| Before loop | Complete values | Next action |
| --- | --- | --- |
| Input unchanged: `["4","13","5","/","+","2","*"]` | `[]` | Initialize `index=0` |

### Block 2 — Read a number or combine two completed values

```java
for (int index = 0; index < tokens.length; index++) {
    String token = tokens[index];
    if (token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/")) {
        int right = values.pop();
        int left = values.pop();
        int result = switch (token) {
            case "+" -> left + right;
            case "-" -> left - right;
            case "*" -> left * right;
            case "/" -> left / right;
            default -> throw new IllegalStateException("Unexpected operator: " + token);
        };
        values.push(result);
    } else {
        values.push(Integer.parseInt(token));
    }
}
```

`index < tokens.length` keeps the array read in bounds: the fixed input's last legal index is `6`; index `7` stops the loop. `tokens[index]` reads one string without editing it. `String.equals` tests text contents; `==` would test object identity instead. The four `||` operands are checked left to right and later operands are skipped after the first true one. Only an exact operator string takes the operation branch, so a signed integer string is parsed as a number rather than mistaken for the subtraction operator.

`Integer.parseInt(token)` converts the numeric text to an integer; on the first token, text `"4"` becomes value `4`. The valid-token contract makes this conversion legal. In the other branch, the two `pop` calls remove right then left because the right value was most recently completed. `switch (token)` selects one arithmetic expression and assigns its result to `result`; the `default` throws if an unexpected operator somehow reaches this branch. It is unreachable with this exact `if` guard and is not a replacement for input validation. `values.push(result)` is essential: later tokens must be able to use that completed calculation.

The loop's `index++` moves to the next token; without it this expression would repeatedly read the same token. Equivalent clearer arithmetic assignments would be an `if/else` chain setting `result`; the selected operation and operand order must remain identical. Locals `right`, `left`, and `result` exist only inside an operator branch, not during number iterations.

**Narrated execution** — fixed input `tokens=["4","13","5","/","+","2","*"]`, length `7`; initial `values=[]`; `for` initializes `index=0`. `token` is a fresh loop-body local each time. In the tables, `—` means an operator-branch local was not created.

**Iteration `index=0`.**

- ✅ `for (index < tokens.length)`: `0 < 7 → true`; enter the body.
- `String token = tokens[index]` reads `tokens[0]="4"` and initializes `token="4"`.
- ❌ `if (token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/"))`: ❌ `"4".equals("+") → false`; ❌ `"4".equals("-") → false`; ❌ `"4".equals("*") → false`; ❌ `"4".equals("/") → false`; whole condition → `false`. The two removals, `switch`, and result push are `Skipped`.
- `values.push(Integer.parseInt(token))` parses `"4" → 4` and changes `values`: `[] → [4]`.
- `index++`: `0 → 1`; branch locals were not created. Recheck the loop next.

| index | token | values before | values after | right / left / result | Next |
| --- | --- | --- | --- | --- | --- |
| `0` | `"4"` | `[]` | `[4]` | `— / — / —` | `index=1` |

**Iteration `index=1`.**

- ✅ `for (index < tokens.length)`: `1 < 7 → true`; enter the body.
- `String token = tokens[index]` reads `tokens[1]="13"` and initializes `token="13"`.
- ❌ `if (token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/"))`: ❌ `"13".equals("+") → false`; ❌ `"13".equals("-") → false`; ❌ `"13".equals("*") → false`; ❌ `"13".equals("/") → false`; whole condition → `false`. The removals, `switch`, and result push are `Skipped`.
- `values.push(Integer.parseInt(token))` parses `"13" → 13` and changes `values`: `[4] → [13,4]`.
- `index++`: `1 → 2`; branch locals were not created. Recheck the loop next.

| index | token | values before | values after | right / left / result | Next |
| --- | --- | --- | --- | --- | --- |
| `1` | `"13"` | `[4]` | `[13,4]` | `— / — / —` | `index=2` |

**Iteration `index=2`.**

- ✅ `for (index < tokens.length)`: `2 < 7 → true`; enter the body.
- `String token = tokens[index]` reads `tokens[2]="5"` and initializes `token="5"`.
- ❌ `if (token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/"))`: ❌ `"5".equals("+") → false`; ❌ `"5".equals("-") → false`; ❌ `"5".equals("*") → false`; ❌ `"5".equals("/") → false`; whole condition → `false`. The removals, `switch`, and result push are `Skipped`.
- `values.push(Integer.parseInt(token))` parses `"5" → 5` and changes `values`: `[13,4] → [5,13,4]`.
- `index++`: `2 → 3`; branch locals were not created. Recheck the loop next.

| index | token | values before | values after | right / left / result | Next |
| --- | --- | --- | --- | --- | --- |
| `2` | `"5"` | `[13,4]` | `[5,13,4]` | `— / — / —` | `index=3` |

**Iteration `index=3`.**

- ✅ `for (index < tokens.length)`: `3 < 7 → true`; enter the body.
- `String token = tokens[index]` reads `tokens[3]="/"` and initializes `token="/"`.
- ✅ `if (token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/"))`: ❌ `"/".equals("+") → false`; ❌ `"/".equals("-") → false`; ❌ `"/".equals("*") → false`; ✅ `"/".equals("/") → true`; whole condition → `true`. Enter the operator branch.
- `int right = values.pop()` removes `5` and initializes `right=5`; `values`: `[5,13,4] → [13,4]`.
- `int left = values.pop()` removes `13` and initializes `left=13`; `values`: `[13,4] → [4]`.
- `int result = switch (token)` selects `case "/" -> left / right`: `13 / 5 = 2`, with remainder `3`; initialize `result=2`. The other arithmetic cases and the default exception are `Skipped`.
- `values.push(result)` changes `values`: `[4] → [2,4]`. The number-parsing `else` is `Skipped`.
- `index++`: `3 → 4`; `right=5`, `left=13`, and `result=2` leave scope after this branch. Recheck the loop next.

| index | token | before removals | after right pop | after left pop | after result push | right / left / result | Next |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `3` | `"/"` | `[5,13,4]` | `[13,4]` | `[4]` | `[2,4]` | `5 / 13 / 2` | `index=4` |

**Iteration `index=4`.**

- ✅ `for (index < tokens.length)`: `4 < 7 → true`; enter the body.
- `String token = tokens[index]` reads `tokens[4]="+"` and initializes `token="+"`.
- ✅ `if (token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/"))`: ✅ `"+".equals("+") → true`; the comparisons with `"-"`, `"*"`, and `"/"` are `Skipped` by short circuit; whole condition → `true`. Enter the operator branch.
- `int right = values.pop()` removes `2` and initializes `right=2`; `values`: `[2,4] → [4]`.
- `int left = values.pop()` removes `4` and initializes `left=4`; `values`: `[4] → []`.
- `int result = switch (token)` selects `case "+" -> left + right`: `4 + 2 = 6`; initialize `result=6`. The other arithmetic cases and the default exception are `Skipped`.
- `values.push(result)` changes `values`: `[] → [6]`. The number-parsing `else` is `Skipped`.
- `index++`: `4 → 5`; `right=2`, `left=4`, and `result=6` leave scope after this branch. Recheck the loop next.

| index | token | before removals | after right pop | after left pop | after result push | right / left / result | Next |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `4` | `"+"` | `[2,4]` | `[4]` | `[]` | `[6]` | `2 / 4 / 6` | `index=5` |

**Iteration `index=5`.**

- ✅ `for (index < tokens.length)`: `5 < 7 → true`; enter the body.
- `String token = tokens[index]` reads `tokens[5]="2"` and initializes `token="2"`.
- ❌ `if (token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/"))`: ❌ `"2".equals("+") → false`; ❌ `"2".equals("-") → false`; ❌ `"2".equals("*") → false`; ❌ `"2".equals("/") → false`; whole condition → `false`. The removals, `switch`, and result push are `Skipped`.
- `values.push(Integer.parseInt(token))` parses `"2" → 2` and changes `values`: `[6] → [2,6]`.
- `index++`: `5 → 6`; branch locals were not created. Recheck the loop next.

| index | token | values before | values after | right / left / result | Next |
| --- | --- | --- | --- | --- | --- |
| `5` | `"2"` | `[6]` | `[2,6]` | `— / — / —` | `index=6` |

**Iteration `index=6`.**

- ✅ `for (index < tokens.length)`: `6 < 7 → true`; enter the body.
- `String token = tokens[index]` reads `tokens[6]="*"` and initializes `token="*"`.
- ✅ `if (token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/"))`: ❌ `"*".equals("+") → false`; ❌ `"*".equals("-") → false`; ✅ `"*".equals("*") → true`; the comparison with `"/"` is `Skipped`; whole condition → `true`. Enter the operator branch.
- `int right = values.pop()` removes `2` and initializes `right=2`; `values`: `[2,6] → [6]`.
- `int left = values.pop()` removes `6` and initializes `left=6`; `values`: `[6] → []`.
- `int result = switch (token)` selects `case "*" -> left * right`: `6 * 2 = 12`; initialize `result=12`. The other arithmetic cases and the default exception are `Skipped`.
- `values.push(result)` changes `values`: `[] → [12]`. The number-parsing `else` is `Skipped`.
- `index++`: `6 → 7`; `right=2`, `left=6`, and `result=12` leave scope after this branch. Recheck the loop next.

| index | token | before removals | after right pop | after left pop | after result push | right / left / result | Next |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `6` | `"*"` | `[2,6]` | `[6]` | `[]` | `[12]` | `2 / 6 / 12` | `index=7` |

- ❌ `for (index < tokens.length)`: `7 < 7 → false`; the body is `Skipped`, so `tokens[7]` is never read. Finish the loop with `values=[12]`. Loop-local `index` leaves scope; the previous body's `token` has also left scope.

### Block 3 — Return the only completed result

```java
return values.pop();
```

- `return values.pop()` removes `12`, changes `values`: `[12] → []`, and returns integer `12` to the caller. The method ends immediately; this final local-container mutation does not change the input array.

| Complete values before return | Complete values after removal | Returned value |
| --- | --- | --- |
| `[12]` | `[]` | `12` |

The valid-expression contract guarantees exactly one result here. Returning an earlier saved value would miss later arithmetic. `return values.peek()` would produce the same answer under this contract; removing the last value makes the final ownership handoff explicit. The returned `12` agrees with the conceptual calculation `(4 + (13 / 5)) * 2 = (4 + 2) * 2 = 12`.

## Complete reference solution

Directly runnable reference and checks: [EvaluateRpnSolution.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/stackqueue/rpn/EvaluateRpnSolution.java). Imports are `java.util.ArrayDeque` and `java.util.Deque`. Input positions below are zero-based array indexes, not the code line numbers displayed by the learning UI.

```java
// Input: tokens = ["4", "13", "5", "/", "+", "2", "*"]
// index:            0     1    2    3    4    5    6
// tokens:          "4"  "13"  "5"  "/"  "+"  "2"  "*"
// Expected return: 12. Local values is empty after returning its final pop.
public static int evalRPN(String[] tokens) {
    Deque<Integer> values = new ArrayDeque<>();
    for (int index = 0; index < tokens.length; index++) {
        String token = tokens[index];
        if (token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/")) {
            int right = values.pop();
            int left = values.pop();
            int result = switch (token) {
                case "+" -> left + right;
                case "-" -> left - right;
                case "*" -> left * right;
                case "/" -> left / right;
                default -> throw new IllegalStateException("Unexpected operator: " + token);
            };
            values.push(result);
        } else {
            values.push(Integer.parseInt(token));
        }
    }
    return values.pop();
}
```

## Why it is correct and what it costs

The invariant is: after each processed token, the stack contains, in completion order, the values of completed subexpressions that have not yet been consumed by a following operator; its top is the most recently completed value. A numeric token adds one completed value and preserves earlier ones. For an operator, postfix notation makes the two newest completed values precisely its right and left operands. Replacing them by `left operator right` preserves the meaning of that entire processed part. A valid expression ends with one completed value, which is therefore its result.

Each token is read once, pushed at most once, and removed at most once. With fixed-size integer tokens and amortized constant-time container pushes, total time is `O(n)` and auxiliary space is `O(n)` in the worst case. Individual container growth can copy stored entries, but its total growth work over the expression is linear. Parsing arbitrary-length numeric text would also depend on text length; here input integers have a fixed bound. The method neither replaces the strings nor writes any input-array cell, and its stack is private to one call.

The exact operator comparisons prevent numeric text from being treated as an arithmetic operator. The valid-input contract rules out missing operands, extra unused results, malformed numbers, division by zero, and arithmetic outside the representable range; this evaluator does not independently diagnose all those errors. A more general parser would need separate syntax/error handling. A growable integer array with a size counter could avoid boxing, but introduces manual capacity management and the same operand-order rule; it is unnecessary for this lesson's goal.

## Try it in IntelliJ

Open [EvaluateRpnPractice.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/stackqueue/rpn/EvaluateRpnPractice.java). Implement only `evalRPN`, then run `main`. Checks include this fixed expression, one number, zero, signed numbers, division truncation, noncommutative operand order, input preservation, and intermediate results above the numeric-token bound. The initial “Practice ready” message signals an intentional TODO, not a passing implementation. Only `UnsupportedOperationException` is caught; incorrect results raise visible assertion failures.

## Checkpoints and explained answers

<details>
<summary>1. At index 3, which value must be removed first, and why is it named right?</summary>

The complete stack is newest-first `[5,13,4]`. The first removal returns `5`, which was the later operand of `/`, so name it `right`. The second returns `13`, so name it `left`. Evaluate `13/5=2`; reversing them would evaluate `5/13=0` and change the expression.
</details>

<details>
<summary>2. Why does index 4 need both the retained 4 and the saved result 2?</summary>

Index 3 combines only `13` and `5`, leaving the earlier `4` untouched: the stack becomes `[2,4]`. Index 4's `+` consumes right `2` and left `4`, replaces them with `6`, and leaves `[6]`. Discarding the earlier `4` or failing to push the completed `2` would lose an operand of this later calculation.
</details>

<details>
<summary>3. Why is the final answer 12 rather than 14, and how much work did finding it require?</summary>

Integer `13/5` gives `2`, because the remainder `3` is discarded. Then `4+2=6` and `6*2=12`. No rounding to `3` occurs. Each of the seven input tokens is processed once; in general time is `O(n)` and the pending-value stack can use `O(n)` space.
</details>

## Stop/go criteria

Stop if you cannot recover left and right from `[5,13,4]` or explain why `4` survives division. Go only after your own practice passes, you explain the invariant and complexity, and you derive the fixed expression and its operand removals without opening the answers. A compiled reference alone leaves learning status `TODO`.
