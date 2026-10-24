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

Use `numbers=[1,2,3,4,5,6]`. For each position, multiply every number except the one at that position. The answer has six cells, in the same order as the input:

```text
index:    0  1  2  3  4  5
numbers:  1  2  3  4  5  6
```

| Answer index | Exclude | Multiply the other values | Result |
| --- | --- | --- | --- |
| `0` | `1` | `2 × 3 × 4 × 5 × 6` | `720` |
| `1` | `2` | `1 × 3 × 4 × 5 × 6` | `360` |
| `2` | `3` | `1 × 2 × 4 × 5 × 6` | `240` |
| `3` | `4` | `1 × 2 × 3 × 5 × 6` | `180` |
| `4` | `5` | `1 × 2 × 3 × 4 × 6` | `144` |
| `5` | `6` | `1 × 2 × 3 × 4 × 5` | `120` |

Therefore return `[720,360,240,180,144,120]`. We have derived what each output means; now we can find those same values without multiplying the other five values again for every position.

The key idea is to multiply the values strictly left of a position and strictly right of it, then combine those two products. At index 2, exclude the value 3: the left side `[1,2]` gives `1×2=2`; the right side `[4,5,6]` gives `4×5×6=120`. Combining gives `2×120=240`, the same answer as the table.

Let `prefixProduct` mean the product of values already passed from the left, and `suffixProduct` the product already passed from the right. These are a **prefix product** and **suffix product**. Start each at 1: an empty side must leave the other side's product unchanged. That makes 1 multiplication's **identity**. For index 0 the left side is empty, so its answer is `1×(2×3×4×5×6)=720`; for index 5 the right side is empty, so its answer is `(1×2×3×4×5)×1=120`.

In general, `answer[index] = product strictly left × product strictly right`. Save the left product **before** including the current number; scan backward and multiply by the right product **before** including the current number from that direction. This order excludes the current value in both passes. No division or input mutation is needed.

## Block-by-block code walkthrough

Original snapshots are retained. Where a value changes several times, its cell shows the sequence ending at the final value.

Input stays `numbers=[1,2,3,4,5,6]` during both passes.

### Block 1 — Create the output and the empty left-side product

```java
int[] answer = new int[numbers.length];
int prefixProduct = 1;
```

- `int[] answer = new int[numbers.length]` reads length `6`; allocate `answer=[0,0,0,0,0,0]`.
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

**Narrated execution — left pass: save the product before this position.**

Input: `numbers=[1,2,3,4,5,6]`. Initial state: `answer=[0,0,0,0,0,0]`, `prefixProduct=1`; initialize `index=0`.

**Left-pass iteration `index=0`.**

- **✅ `for (index < numbers.length)`:** `0 < 6 → true`; enter the body.
- `answer[index] = prefixProduct` writes `answer[0]`: `0 → 1`; the full array is below.
- `prefixProduct *= numbers[index]` reads `numbers[0]=1`; `1 × 1 = 1`, changing `prefixProduct`: `1 → 1`.
- `index++`: `0 → 1`; recheck the loop. Input stays `[1,2,3,4,5,6]`.

| index | condition | prefixProduct before | answer after assignment | prefixProduct after multiply | next |
| --- | --- | --- | --- | --- | --- |
| `0` | `✅ 0<6 → true` | `1` | `[1, 0, 0, 0, 0, 0]` | `1` | `index=1` |

**Left-pass iteration `index=1`.**

- **✅ `for (index < numbers.length)`:** `1 < 6 → true`; enter the body.
- `answer[index] = prefixProduct` writes `answer[1]`: `0 → 1`; the full array is below.
- `prefixProduct *= numbers[index]` reads `numbers[1]=2`; `1 × 2 = 2`, changing `prefixProduct`: `1 → 2`.
- `index++`: `1 → 2`; recheck the loop. Input stays `[1,2,3,4,5,6]`.

| index | condition | prefixProduct before | answer after assignment | prefixProduct after multiply | next |
| --- | --- | --- | --- | --- | --- |
| `1` | `✅ 1<6 → true` | `1` | `[1, 1, 0, 0, 0, 0]` | `2` | `index=2` |

**Left-pass iteration `index=2`.**

- **✅ `for (index < numbers.length)`:** `2 < 6 → true`; enter the body.
- `answer[index] = prefixProduct` writes `answer[2]`: `0 → 2`; the full array is below.
- `prefixProduct *= numbers[index]` reads `numbers[2]=3`; `2 × 3 = 6`, changing `prefixProduct`: `2 → 6`.
- `index++`: `2 → 3`; recheck the loop. Input stays `[1,2,3,4,5,6]`.

| index | condition | prefixProduct before | answer after assignment | prefixProduct after multiply | next |
| --- | --- | --- | --- | --- | --- |
| `2` | `✅ 2<6 → true` | `2` | `[1, 1, 2, 0, 0, 0]` | `6` | `index=3` |

**Left-pass iteration `index=3`.**

- **✅ `for (index < numbers.length)`:** `3 < 6 → true`; enter the body.
- `answer[index] = prefixProduct` writes `answer[3]`: `0 → 6`; the full array is below.
- `prefixProduct *= numbers[index]` reads `numbers[3]=4`; `6 × 4 = 24`, changing `prefixProduct`: `6 → 24`.
- `index++`: `3 → 4`; recheck the loop. Input stays `[1,2,3,4,5,6]`.

| index | condition | prefixProduct before | answer after assignment | prefixProduct after multiply | next |
| --- | --- | --- | --- | --- | --- |
| `3` | `✅ 3<6 → true` | `6` | `[1, 1, 2, 6, 0, 0]` | `24` | `index=4` |

**Left-pass iteration `index=4`.**

- **✅ `for (index < numbers.length)`:** `4 < 6 → true`; enter the body.
- `answer[index] = prefixProduct` writes `answer[4]`: `0 → 24`; the full array is below.
- `prefixProduct *= numbers[index]` reads `numbers[4]=5`; `24 × 5 = 120`, changing `prefixProduct`: `24 → 120`.
- `index++`: `4 → 5`; recheck the loop. Input stays `[1,2,3,4,5,6]`.

| index | condition | prefixProduct before | answer after assignment | prefixProduct after multiply | next |
| --- | --- | --- | --- | --- | --- |
| `4` | `✅ 4<6 → true` | `24` | `[1, 1, 2, 6, 24, 0]` | `120` | `index=5` |

**Left-pass iteration `index=5`.**

- **✅ `for (index < numbers.length)`:** `5 < 6 → true`; enter the body.
- `answer[index] = prefixProduct` writes `answer[5]`: `0 → 120`; the full array is below.
- `prefixProduct *= numbers[index]` reads `numbers[5]=6`; `120 × 6 = 720`, changing `prefixProduct`: `120 → 720`.
- `index++`: `5 → 6`; recheck the loop. Input stays `[1,2,3,4,5,6]`.

| index | condition | prefixProduct before | answer after assignment | prefixProduct after multiply | next |
| --- | --- | --- | --- | --- | --- |
| `5` | `✅ 5<6 → true` | `120` | `[1, 1, 2, 6, 24, 120]` | `720` | `index=6` |

- **❌ First `for (index < numbers.length)`:** `6 < 6 → false`; finish the left pass with `answer=[1,1,2,6,24,120]`, `prefixProduct=720`. This loop-local `index` leaves scope; the second loop declares a fresh index.

### Block 3 — Multiply by the product strictly to the right

```java
int suffixProduct = 1;
for (int index = numbers.length - 1; index >= 0; index--) {
    answer[index] *= suffixProduct;
    suffixProduct *= numbers[index];
}
```

Initialize `suffixProduct=1` because nothing is right of the last position. Start at `6-1=5`. The first multiplication combines the saved left product with the current right product. Only after writing the answer does the second multiplication include this position for the next iteration to its left. Equivalent: `answer[index] = answer[index] * suffixProduct`; the order excludes the current input from its own answer.

**Narrated execution — right pass: combine the saved left product with the right product.**

Input: `numbers=[1,2,3,4,5,6]`. Initial state: `answer=[1,1,2,6,24,120]`, `prefixProduct=720`, `suffixProduct=1`; initialize `index=6-1=5`.

**Right-pass iteration `index=5`.**

- **✅ `for (index >= 0)`:** `5 >= 0 → true`; enter the body.
- `answer[index] *= suffixProduct`: `120 × 1 = 120`, changing `answer[5]`: `120 → 120`. Full before/after arrays are below.
- `suffixProduct *= numbers[index]` reads `numbers[5]=6`; `1 × 6 = 6`, changing `suffixProduct`: `1 → 6`.
- `index--`: `5 → 4`; recheck the loop. `prefixProduct` stays `720`; input stays unchanged.

| index | condition | answer before | suffixProduct before | answer after multiplication | suffixProduct after | next |
| --- | --- | --- | --- | --- | --- | --- |
| `5` | `✅ 5>=0 → true` | `[1, 1, 2, 6, 24, 120]` | `1` | `[1, 1, 2, 6, 24, 120]` | `6` | `index=4` |

**Right-pass iteration `index=4`.**

- **✅ `for (index >= 0)`:** `4 >= 0 → true`; enter the body.
- `answer[index] *= suffixProduct`: `24 × 6 = 144`, changing `answer[4]`: `24 → 144`. Full before/after arrays are below.
- `suffixProduct *= numbers[index]` reads `numbers[4]=5`; `6 × 5 = 30`, changing `suffixProduct`: `6 → 30`.
- `index--`: `4 → 3`; recheck the loop. `prefixProduct` stays `720`; input stays unchanged.

| index | condition | answer before | suffixProduct before | answer after multiplication | suffixProduct after | next |
| --- | --- | --- | --- | --- | --- | --- |
| `4` | `✅ 4>=0 → true` | `[1, 1, 2, 6, 24, 120]` | `6` | `[1, 1, 2, 6, 144, 120]` | `30` | `index=3` |

**Right-pass iteration `index=3`.**

- **✅ `for (index >= 0)`:** `3 >= 0 → true`; enter the body.
- `answer[index] *= suffixProduct`: `6 × 30 = 180`, changing `answer[3]`: `6 → 180`. Full before/after arrays are below.
- `suffixProduct *= numbers[index]` reads `numbers[3]=4`; `30 × 4 = 120`, changing `suffixProduct`: `30 → 120`.
- `index--`: `3 → 2`; recheck the loop. `prefixProduct` stays `720`; input stays unchanged.

| index | condition | answer before | suffixProduct before | answer after multiplication | suffixProduct after | next |
| --- | --- | --- | --- | --- | --- | --- |
| `3` | `✅ 3>=0 → true` | `[1, 1, 2, 6, 144, 120]` | `30` | `[1, 1, 2, 180, 144, 120]` | `120` | `index=2` |

**Right-pass iteration `index=2`.**

- **✅ `for (index >= 0)`:** `2 >= 0 → true`; enter the body.
- `answer[index] *= suffixProduct`: `2 × 120 = 240`, changing `answer[2]`: `2 → 240`. Full before/after arrays are below.
- `suffixProduct *= numbers[index]` reads `numbers[2]=3`; `120 × 3 = 360`, changing `suffixProduct`: `120 → 360`.
- `index--`: `2 → 1`; recheck the loop. `prefixProduct` stays `720`; input stays unchanged.

| index | condition | answer before | suffixProduct before | answer after multiplication | suffixProduct after | next |
| --- | --- | --- | --- | --- | --- | --- |
| `2` | `✅ 2>=0 → true` | `[1, 1, 2, 180, 144, 120]` | `120` | `[1, 1, 240, 180, 144, 120]` | `360` | `index=1` |

**Right-pass iteration `index=1`.**

- **✅ `for (index >= 0)`:** `1 >= 0 → true`; enter the body.
- `answer[index] *= suffixProduct`: `1 × 360 = 360`, changing `answer[1]`: `1 → 360`. Full before/after arrays are below.
- `suffixProduct *= numbers[index]` reads `numbers[1]=2`; `360 × 2 = 720`, changing `suffixProduct`: `360 → 720`.
- `index--`: `1 → 0`; recheck the loop. `prefixProduct` stays `720`; input stays unchanged.

| index | condition | answer before | suffixProduct before | answer after multiplication | suffixProduct after | next |
| --- | --- | --- | --- | --- | --- | --- |
| `1` | `✅ 1>=0 → true` | `[1, 1, 240, 180, 144, 120]` | `360` | `[1, 360, 240, 180, 144, 120]` | `720` | `index=0` |

**Right-pass iteration `index=0`.**

- **✅ `for (index >= 0)`:** `0 >= 0 → true`; enter the body.
- `answer[index] *= suffixProduct`: `1 × 720 = 720`, changing `answer[0]`: `1 → 720`. Full before/after arrays are below.
- `suffixProduct *= numbers[index]` reads `numbers[0]=1`; `720 × 1 = 720`, changing `suffixProduct`: `720 → 720`.
- `index--`: `0 → -1`; recheck the loop. `prefixProduct` stays `720`; input stays unchanged.

| index | condition | answer before | suffixProduct before | answer after multiplication | suffixProduct after | next |
| --- | --- | --- | --- | --- | --- | --- |
| `0` | `✅ 0>=0 → true` | `[1, 360, 240, 180, 144, 120]` | `720` | `[720, 360, 240, 180, 144, 120]` | `720` | `index=-1` |

### Block 4 — Return the constructed array

```java
return answer;
```

- **❌ Second `for` condition `index >= 0`:** `-1 >= 0 → false`; skip the body, avoiding `answer[-1]`.
- `return answer` returns `[720,360,240,180,144,120]`; both running products finish at `720`. This is the constructed result, not either running product (both end at `720`). Without returning the array the caller gets no answer; a clearer equivalent variable name is `productsExceptIndex`.

## Complete reference solution

Runnable reference: [ProductExceptSelfSolution.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/prefixintervals/productexceptself/ProductExceptSelfSolution.java). The walkthrough immediately above explains the complete algorithm below; the `main` runner and assertions are in the linked Java file.

```java
// numbers index: 0 1 2 3 4 5
// numbers      : 1 2 3 4 5 6
// Input: numbers=[1,2,3,4,5,6]
// Expected return: [720,360,240,180,144,120]

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

## Pitfalls in this example

- At index 2, saving the left product after multiplying 3 would store `1×2×3=6` instead of `1×2=2`. Combining with the right product 120 would give `6×120=720`, incorrectly including the excluded 3.
- In the right pass at index 2, multiply the saved 2 by the existing right product 120 **before** multiplying that right product by 3. Updating first would again give the incorrect `2×360=720`.
- At the last index 5, the right side is empty, so use 1: `120×1=120`. Starting the right product at zero would erase that answer and every later one.
- Both running products end at 720, but return the six-cell answer array, not one running product.
- Additional zero, negative, minimum-length, input-preservation and defensive-empty cases remain in the runnable checks; this explanation uses only `[1,2,3,4,5,6]`.

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
