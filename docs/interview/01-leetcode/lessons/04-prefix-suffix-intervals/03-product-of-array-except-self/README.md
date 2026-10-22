# Micro-Lesson 03 — Product of Array Except Self

## Question

Given an integer array, return an array whose value at each index is the product of every input value except the one at that index. Use `O(n)` time and no division. The optional space target is `O(1)` auxiliary space, excluding the returned array.

Problem: [Product of Array Except Self on LeetCode](https://leetcode.com/problems/product-of-array-except-self/) · Editable Java: [ProductExceptSelfPractice.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/prefixintervals/productexceptself/ProductExceptSelfPractice.java)

## What you will learn

- Combine products strictly left and strictly right of each index.
- Explain why the running product starts at one and is updated after writing.
- Handle zeros without division or special zero branches.

## Lesson status and prerequisites

- Learning status: `TODO`; this artifact is prepared, not activated or mastered.
- Prerequisite: Array indexing, multiplication, and the idea of saved partial results in [Range Sum Query](../01-range-sum-query-immutable/README.md).
- Primary idea: Combine products strictly left and strictly right of each index.
- New terms (at most three): prefix product, suffix product, identity

## Inputs and limits

`2 <= numbers.length <= 100,000`, values in `[-30,30]`. The product of any prefix or suffix and every requested output fits a signed 32-bit integer; this guarantee is required for the shown `int` products. Target `O(n)` time and `O(1)` auxiliary space, excluding the `O(n)` output. The local runner also accepts empty input defensively.

## First understand the idea

The key idea is to multiply the values on each side of a position, then combine those two products without ever including the position itself.

Use `numbers=[1,2,3,4,5,6,1]`. For answer index 2, the value to exclude is 3. The left side is `[1,2]`, giving `1*2=2`. The right side is `[4,5,6,1]`, giving `4*5*6*1=120`. Therefore `answer[2]=2*120=240`.

Let `prefixProduct` mean the product of values already passed from the left, and `suffixProduct` the product already passed from the right. These are a **prefix product** and **suffix product**. Start each at 1: the empty side must leave the other side's product unchanged. That makes 1 multiplication's **identity**.

In general, save prefixProduct into the current answer before including the current number; then scan backward, multiplying that saved answer by suffixProduct before including the number from that direction. The order excludes the current value in both passes. No division by a total product is needed, so a zero never becomes a divisor. The resulting array is `[720,360,240,180,144,120,720]`, with each cell combining its own two sides as the trace shows.

## Block-by-block code walkthrough

Original snapshots are retained. Where a value changes several times, its cell shows the sequence ending at the final value.

Input stays `numbers=[1,2,3,4,5,6,1]` during both passes.

### Block 1 — Create the output and the empty left-side product

```java
int[] answer = new int[numbers.length];
int prefixProduct = 1;
```

- `int[] answer = new int[numbers.length]` reads length `7`; allocate `answer=[0,0,0,0,0,0,0]`.
- `int prefixProduct = 1` starts with the empty left-side product, before index zero.

Starting at zero would force all later products to zero even on nonzero input. Equivalent: describe the initial state as “multiply no values yet, so use 1.” No input cell is modified.

### Block 2 — Save the product before including this position

```java
for (int index = 0; index < numbers.length; index++) {
    answer[index] = prefixProduct;
    prefixProduct *= numbers[index];
}
```

The first assignment records only values strictly left of `index`. The following multiplication prepares the value needed at the next index. Reversing these statements would include the excluded value in its own output. Equivalent clearer multiplication: `prefixProduct = prefixProduct * numbers[index]`.

**Left-pass iteration `index=0`.**

- **✅ `for` condition `index < numbers.length`:** `0 < 7 → true`; enter the body.
- `answer[index] = prefixProduct` writes `answer[0]`: `0 → 1`; the array after this write is below.
- `prefixProduct *= numbers[index]` reads `numbers[0]=1`; `1 × 1 = 1`, so `prefixProduct`: `1 → 1`.
- `index++`: `0 → 1`; continue to the next `for` check.

| index | condition | prefixProduct before | answer after assignment | prefixProduct after multiply | next |
| --- | --- | --- | --- | --- | --- |
| `0` | `✅ 0<7 → true` | `1` | `[1, 0, 0, 0, 0, 0, 0]` | `1` | `index=1` |

**Left-pass iteration `index=1`.**

- **✅ `for` condition `index < numbers.length`:** `1 < 7 → true`; enter the body.
- `answer[index] = prefixProduct` writes `answer[1]`: `0 → 1`; the array after this write is below.
- `prefixProduct *= numbers[index]` reads `numbers[1]=2`; `1 × 2 = 2`, so `prefixProduct`: `1 → 2`.
- `index++`: `1 → 2`; continue to the next `for` check.

| index | condition | prefixProduct before | answer after assignment | prefixProduct after multiply | next |
| --- | --- | --- | --- | --- | --- |
| `1` | `✅ 1<7 → true` | `1` | `[1, 1, 0, 0, 0, 0, 0]` | `2` | `index=2` |

**Left-pass iteration `index=2`.**

- **✅ `for` condition `index < numbers.length`:** `2 < 7 → true`; enter the body.
- `answer[index] = prefixProduct` writes `answer[2]`: `0 → 2`; the array after this write is below.
- `prefixProduct *= numbers[index]` reads `numbers[2]=3`; `2 × 3 = 6`, so `prefixProduct`: `2 → 6`.
- `index++`: `2 → 3`; continue to the next `for` check.

| index | condition | prefixProduct before | answer after assignment | prefixProduct after multiply | next |
| --- | --- | --- | --- | --- | --- |
| `2` | `✅ 2<7 → true` | `2` | `[1, 1, 2, 0, 0, 0, 0]` | `6` | `index=3` |

**Left-pass iteration `index=3`.**

- **✅ `for` condition `index < numbers.length`:** `3 < 7 → true`; enter the body.
- `answer[index] = prefixProduct` writes `answer[3]`: `0 → 6`; the array after this write is below.
- `prefixProduct *= numbers[index]` reads `numbers[3]=4`; `6 × 4 = 24`, so `prefixProduct`: `6 → 24`.
- `index++`: `3 → 4`; continue to the next `for` check.

| index | condition | prefixProduct before | answer after assignment | prefixProduct after multiply | next |
| --- | --- | --- | --- | --- | --- |
| `3` | `✅ 3<7 → true` | `6` | `[1, 1, 2, 6, 0, 0, 0]` | `24` | `index=4` |

**Left-pass iteration `index=4`.**

- **✅ `for` condition `index < numbers.length`:** `4 < 7 → true`; enter the body.
- `answer[index] = prefixProduct` writes `answer[4]`: `0 → 24`; the array after this write is below.
- `prefixProduct *= numbers[index]` reads `numbers[4]=5`; `24 × 5 = 120`, so `prefixProduct`: `24 → 120`.
- `index++`: `4 → 5`; continue to the next `for` check.

| index | condition | prefixProduct before | answer after assignment | prefixProduct after multiply | next |
| --- | --- | --- | --- | --- | --- |
| `4` | `✅ 4<7 → true` | `24` | `[1, 1, 2, 6, 24, 0, 0]` | `120` | `index=5` |

**Left-pass iteration `index=5`.**

- **✅ `for` condition `index < numbers.length`:** `5 < 7 → true`; enter the body.
- `answer[index] = prefixProduct` writes `answer[5]`: `0 → 120`; the array after this write is below.
- `prefixProduct *= numbers[index]` reads `numbers[5]=6`; `120 × 6 = 720`, so `prefixProduct`: `120 → 720`.
- `index++`: `5 → 6`; continue to the next `for` check.

| index | condition | prefixProduct before | answer after assignment | prefixProduct after multiply | next |
| --- | --- | --- | --- | --- | --- |
| `5` | `✅ 5<7 → true` | `120` | `[1, 1, 2, 6, 24, 120, 0]` | `720` | `index=6` |

**Left-pass iteration `index=6`.**

- **✅ `for` condition `index < numbers.length`:** `6 < 7 → true`; enter the body.
- `answer[index] = prefixProduct` writes `answer[6]`: `0 → 720`; the array after this write is below.
- `prefixProduct *= numbers[index]` reads `numbers[6]=1`; `720 × 1 = 720`, so `prefixProduct`: `720 → 720`.
- `index++`: `6 → 7`; continue to the next `for` check.

| index | condition | prefixProduct before | answer after assignment | prefixProduct after multiply | next |
| --- | --- | --- | --- | --- | --- |
| `6` | `✅ 6<7 → true` | `720` | `[1, 1, 2, 6, 24, 120, 720]` | `720` | `index=7` |

- **❌ First `for` condition `index < numbers.length`:** `7 < 7 → false`; skip its body. This local `index` leaves scope; the second loop declares a fresh index.

### Block 3 — Multiply by the product strictly to the right

```java
int suffixProduct = 1;
for (int index = numbers.length - 1; index >= 0; index--) {
    answer[index] *= suffixProduct;
    suffixProduct *= numbers[index];
}
```

Initialize `suffixProduct=1` because nothing is right of the last position. Start at `7-1=6`. The first multiplication combines the saved left product with the current right product. Only after writing the answer does the second multiplication include this position for the next iteration to its left. Equivalent: `answer[index] = answer[index] * suffixProduct`; the order excludes the current input from its own answer.

**Right-pass iteration `index=6`.**

- **✅ `for` condition `index >= 0`:** `6 >= 0 → true`; enter the body.
- `answer[index] *= suffixProduct`: `720 × 1 = 720`, so `answer[6]`: `720 → 720`. The full array before/after is below.
- `suffixProduct *= numbers[index]` reads `numbers[6]=1`; `1 × 1 = 1`, so `suffixProduct`: `1 → 1`.
- `index--`: `6 → 5`; continue to the next `for` check.

| index | condition | answer before | suffixProduct before | answer after multiplication | suffixProduct after | next |
| --- | --- | --- | --- | --- | --- | --- |
| `6` | `✅ 6>=0 → true` | `[1, 1, 2, 6, 24, 120, 720]` | `1` | `[1, 1, 2, 6, 24, 120, 720]` | `1` | `index=5` |

**Right-pass iteration `index=5`.**

- **✅ `for` condition `index >= 0`:** `5 >= 0 → true`; enter the body.
- `answer[index] *= suffixProduct`: `120 × 1 = 120`, so `answer[5]`: `120 → 120`. The full array before/after is below.
- `suffixProduct *= numbers[index]` reads `numbers[5]=6`; `1 × 6 = 6`, so `suffixProduct`: `1 → 6`.
- `index--`: `5 → 4`; continue to the next `for` check.

| index | condition | answer before | suffixProduct before | answer after multiplication | suffixProduct after | next |
| --- | --- | --- | --- | --- | --- | --- |
| `5` | `✅ 5>=0 → true` | `[1, 1, 2, 6, 24, 120, 720]` | `1` | `[1, 1, 2, 6, 24, 120, 720]` | `6` | `index=4` |

**Right-pass iteration `index=4`.**

- **✅ `for` condition `index >= 0`:** `4 >= 0 → true`; enter the body.
- `answer[index] *= suffixProduct`: `24 × 6 = 144`, so `answer[4]`: `24 → 144`. The full array before/after is below.
- `suffixProduct *= numbers[index]` reads `numbers[4]=5`; `6 × 5 = 30`, so `suffixProduct`: `6 → 30`.
- `index--`: `4 → 3`; continue to the next `for` check.

| index | condition | answer before | suffixProduct before | answer after multiplication | suffixProduct after | next |
| --- | --- | --- | --- | --- | --- | --- |
| `4` | `✅ 4>=0 → true` | `[1, 1, 2, 6, 24, 120, 720]` | `6` | `[1, 1, 2, 6, 144, 120, 720]` | `30` | `index=3` |

**Right-pass iteration `index=3`.**

- **✅ `for` condition `index >= 0`:** `3 >= 0 → true`; enter the body.
- `answer[index] *= suffixProduct`: `6 × 30 = 180`, so `answer[3]`: `6 → 180`. The full array before/after is below.
- `suffixProduct *= numbers[index]` reads `numbers[3]=4`; `30 × 4 = 120`, so `suffixProduct`: `30 → 120`.
- `index--`: `3 → 2`; continue to the next `for` check.

| index | condition | answer before | suffixProduct before | answer after multiplication | suffixProduct after | next |
| --- | --- | --- | --- | --- | --- | --- |
| `3` | `✅ 3>=0 → true` | `[1, 1, 2, 6, 144, 120, 720]` | `30` | `[1, 1, 2, 180, 144, 120, 720]` | `120` | `index=2` |

**Right-pass iteration `index=2`.**

- **✅ `for` condition `index >= 0`:** `2 >= 0 → true`; enter the body.
- `answer[index] *= suffixProduct`: `2 × 120 = 240`, so `answer[2]`: `2 → 240`. The full array before/after is below.
- `suffixProduct *= numbers[index]` reads `numbers[2]=3`; `120 × 3 = 360`, so `suffixProduct`: `120 → 360`.
- `index--`: `2 → 1`; continue to the next `for` check.

| index | condition | answer before | suffixProduct before | answer after multiplication | suffixProduct after | next |
| --- | --- | --- | --- | --- | --- | --- |
| `2` | `✅ 2>=0 → true` | `[1, 1, 2, 180, 144, 120, 720]` | `120` | `[1, 1, 240, 180, 144, 120, 720]` | `360` | `index=1` |

**Right-pass iteration `index=1`.**

- **✅ `for` condition `index >= 0`:** `1 >= 0 → true`; enter the body.
- `answer[index] *= suffixProduct`: `1 × 360 = 360`, so `answer[1]`: `1 → 360`. The full array before/after is below.
- `suffixProduct *= numbers[index]` reads `numbers[1]=2`; `360 × 2 = 720`, so `suffixProduct`: `360 → 720`.
- `index--`: `1 → 0`; continue to the next `for` check.

| index | condition | answer before | suffixProduct before | answer after multiplication | suffixProduct after | next |
| --- | --- | --- | --- | --- | --- | --- |
| `1` | `✅ 1>=0 → true` | `[1, 1, 240, 180, 144, 120, 720]` | `360` | `[1, 360, 240, 180, 144, 120, 720]` | `720` | `index=0` |

**Right-pass iteration `index=0`.**

- **✅ `for` condition `index >= 0`:** `0 >= 0 → true`; enter the body.
- `answer[index] *= suffixProduct`: `1 × 720 = 720`, so `answer[0]`: `1 → 720`. The full array before/after is below.
- `suffixProduct *= numbers[index]` reads `numbers[0]=1`; `720 × 1 = 720`, so `suffixProduct`: `720 → 720`.
- `index--`: `0 → -1`; continue to the next `for` check.

| index | condition | answer before | suffixProduct before | answer after multiplication | suffixProduct after | next |
| --- | --- | --- | --- | --- | --- | --- |
| `0` | `✅ 0>=0 → true` | `[1, 360, 240, 180, 144, 120, 720]` | `720` | `[720, 360, 240, 180, 144, 120, 720]` | `720` | `index=-1` |

### Block 4 — Return the constructed array

```java
return answer;
```

- **❌ Second `for` condition `index >= 0`:** `-1 >= 0 → false`; skip the body, avoiding `answer[-1]`.
- `return answer` returns `[720,360,240,180,144,120,720]`; both running products finish at `720`. This is the constructed result, not either running product (both end at `720`). Without returning the array the caller gets no answer; a clearer equivalent variable name is `productsExceptIndex`.

## Complete reference solution

Runnable reference: [ProductExceptSelfSolution.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/prefixintervals/productexceptself/ProductExceptSelfSolution.java). The walkthrough immediately above explains the complete algorithm below; the `main` runner and assertions are in the linked Java file.

```java
// numbers index: 0 1 2 3 4 5 6
// numbers      : 1 2 3 4 5 6 1
// Input: numbers=[1,2,3,4,5,6,1]
// Expected return: [720,360,240,180,144,120,720]

static int[] productExceptSelf(int[] numbers) {
    int[] answer = new int[numbers.length];
    int prefixProduct = 1;
    for (int index = 0; index < numbers.length; index++) {
        answer[index] = prefixProduct;
        prefixProduct *= numbers[index];
    }

    int suffixProduct = 1;
    for (int index = numbers.length - 1; index >= 0; index--) {
        answer[index] *= suffixProduct;
        suffixProduct *= numbers[index];
    }
    return answer;
}
```

## Invariant, correctness, and complexity

Before left-pass index `i`, `prefixProduct` contains exactly positions `< i`; that is what gets saved. Before right-pass index `i`, `suffixProduct` contains exactly positions `> i`. Multiplying these two disjoint groups includes every input exactly once except index `i`, proving the result. Identity one supplies the empty group at each end.

Each pass visits `n` positions: `O(n)` time. Two scalars and loop indexes use `O(1)` auxiliary space; the returned array uses `O(n)` output space. A simpler alternative stores separate left and right arrays, costing `O(n)` auxiliary space; it teaches the same idea but retains more state. Brute force multiplies all other values per index in `O(n²)` time. Division by the full product violates the problem and also fails on zero.

## Other results and pitfalls

- `[1,2,3,4]`: outputs derive as `2×3×4=24`, `1×3×4=12`, `1×2×4=8`, `1×2×3=6`.
- `[-1,1,0,-3,3]`: every output except index `2` still includes zero, giving zero. Excluding that zero gives `(-1)×1×(-3)×3=9`; result `[0,0,9,0,0]`.
- `[0,2,0]`: excluding either zero still leaves the other zero; excluding `2` leaves both zeros. Result `[0,0,0]`.
- `[-2,3]`: excluding `-2` leaves `3`; excluding `3` leaves `-2`, giving `[3,-2]`.
- Defensive empty input creates `[]`; the first `0<0` and second `-1>=0` are false, so both bodies are skipped and `[]` is returned.

## Practice after learning the concept

Open [ProductExceptSelfPractice.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/prefixintervals/productexceptself/ProductExceptSelfPractice.java) in IntelliJ, implement the marked TODO method(s), then run its `main`. Do not use division. Checks cover ordinary values, one/two zeros, negatives, input preservation, and defensive emptiness. The unfinished program reports that practice is ready; that message is not a passing solution.

Reveal only after attempting: [ProductExceptSelfSolution.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/prefixintervals/productexceptself/ProductExceptSelfSolution.java). Its runner checks sample and edge cases and throws if an answer is wrong.

## Questions and explained answers

<details>
<summary>1. Why assign the left product before multiplying the current value?</summary>

At index 2, the product must be 1×2=2, excluding the current 3. Updating first would save 6 and incorrectly include 3.

</details>

<details>
<summary>2. Why start both running products at one?</summary>

One represents an empty multiplication and does not alter a product. Zero would erase every answer even when all inputs are nonzero.

</details>

<details>
<summary>3. Does the returned array make auxiliary space O(n)?</summary>

The output itself has n cells. The optional bound excludes output storage; beyond that array only a fixed number of scalar variables are used, so auxiliary space is O(1).

</details>

## Stop/go

Learning remains `TODO`. Continue only after implementing both passes, narrating an index in both directions, explaining the invariant and zero cases, passing the checks, and answering the questions without notes. Compiled artifacts do not demonstrate that understanding.
