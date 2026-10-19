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

## First find the answer in learner language

At one position, the other values fall into two disjoint groups: those strictly to its left and those strictly to its right. Multiply each group separately, then multiply their results. A **prefix product** is a product of values at the beginning; a **suffix product** is a product of values at the end. The **identity** for multiplication is `1`, the value that leaves another value unchanged when multiplied.

1. Walk left to right with a product of the values already passed.
2. Save that product into the current answer cell **before** multiplying the current input value into it.
3. Walk right to left with a product of values already passed from that direction.
4. Multiply each saved answer by that right-side product **before** including the current input in it.

For index `2` in `[1,2,3,4,5,6,1]`, left-side product is `1×2=2`; right-side product is `4×5×6×1=120`; answer is `2×120=240`. The value `3` at index `2` never enters its own answer. No total-product division is needed, so zeros are safe.

## Block-by-block code walkthrough

Original snapshots are retained. Where a value changes several times, its cell shows the sequence ending at the final value.

Input stays `numbers=[1,2,3,4,5,6,1]` during both passes.

### Block 1 — Create the output and the empty left-side product

```java
int[] answer = new int[numbers.length];
int prefixProduct = 1;
```

Allocate seven zero-filled cells: `answer=[0,0,0,0,0,0,0]`. Initialize `prefixProduct=1` because no value is left of index zero. Starting at zero would force all later products to zero even on nonzero input. Equivalent: describe the initial state as “multiply no values yet, so use 1.” No input cell is modified.

### Block 2 — Save the product before including this position

```java
for (int index = 0; index < numbers.length; index++) {
    answer[index] = prefixProduct;
    prefixProduct *= numbers[index];
}
```

The first assignment records only values strictly left of `index`. The following multiplication prepares the value needed at the next index. Reversing these statements would include the excluded value in its own output. Equivalent clearer multiplication: `prefixProduct = prefixProduct * numbers[index]`.

**Left-pass iteration `index=0`.** Test `0<7 → true`. Before this position, the left product is `1`. Save `answer[0]=1`, making the array `[1, 0, 0, 0, 0, 0, 0]`. Then multiply `1×1=1` and store it in `prefixProduct` for the next position. Execute `index++` to `1`. No conditional branch exists inside the loop.

| index | condition | prefixProduct before | answer after assignment | prefixProduct after multiply | next |
| --- | --- | --- | --- | --- | --- |
| `0` | `0<7 → true` | `1` | `[1, 0, 0, 0, 0, 0, 0]` | `1` | `index=1` |

**Left-pass iteration `index=1`.** Test `1<7 → true`. Before this position, the left product is `1`. Save `answer[1]=1`, making the array `[1, 1, 0, 0, 0, 0, 0]`. Then multiply `1×2=2` and store it in `prefixProduct` for the next position. Execute `index++` to `2`. No conditional branch exists inside the loop.

| index | condition | prefixProduct before | answer after assignment | prefixProduct after multiply | next |
| --- | --- | --- | --- | --- | --- |
| `1` | `1<7 → true` | `1` | `[1, 1, 0, 0, 0, 0, 0]` | `2` | `index=2` |

**Left-pass iteration `index=2`.** Test `2<7 → true`. Before this position, the left product is `2`. Save `answer[2]=2`, making the array `[1, 1, 2, 0, 0, 0, 0]`. Then multiply `2×3=6` and store it in `prefixProduct` for the next position. Execute `index++` to `3`. No conditional branch exists inside the loop.

| index | condition | prefixProduct before | answer after assignment | prefixProduct after multiply | next |
| --- | --- | --- | --- | --- | --- |
| `2` | `2<7 → true` | `2` | `[1, 1, 2, 0, 0, 0, 0]` | `6` | `index=3` |

**Left-pass iteration `index=3`.** Test `3<7 → true`. Before this position, the left product is `6`. Save `answer[3]=6`, making the array `[1, 1, 2, 6, 0, 0, 0]`. Then multiply `6×4=24` and store it in `prefixProduct` for the next position. Execute `index++` to `4`. No conditional branch exists inside the loop.

| index | condition | prefixProduct before | answer after assignment | prefixProduct after multiply | next |
| --- | --- | --- | --- | --- | --- |
| `3` | `3<7 → true` | `6` | `[1, 1, 2, 6, 0, 0, 0]` | `24` | `index=4` |

**Left-pass iteration `index=4`.** Test `4<7 → true`. Before this position, the left product is `24`. Save `answer[4]=24`, making the array `[1, 1, 2, 6, 24, 0, 0]`. Then multiply `24×5=120` and store it in `prefixProduct` for the next position. Execute `index++` to `5`. No conditional branch exists inside the loop.

| index | condition | prefixProduct before | answer after assignment | prefixProduct after multiply | next |
| --- | --- | --- | --- | --- | --- |
| `4` | `4<7 → true` | `24` | `[1, 1, 2, 6, 24, 0, 0]` | `120` | `index=5` |

**Left-pass iteration `index=5`.** Test `5<7 → true`. Before this position, the left product is `120`. Save `answer[5]=120`, making the array `[1, 1, 2, 6, 24, 120, 0]`. Then multiply `120×6=720` and store it in `prefixProduct` for the next position. Execute `index++` to `6`. No conditional branch exists inside the loop.

| index | condition | prefixProduct before | answer after assignment | prefixProduct after multiply | next |
| --- | --- | --- | --- | --- | --- |
| `5` | `5<7 → true` | `120` | `[1, 1, 2, 6, 24, 120, 0]` | `720` | `index=6` |

**Left-pass iteration `index=6`.** Test `6<7 → true`. Before this position, the left product is `720`. Save `answer[6]=720`, making the array `[1, 1, 2, 6, 24, 120, 720]`. Then multiply `720×1=720` and store it in `prefixProduct` for the next position. Execute `index++` to `7`. No conditional branch exists inside the loop.

| index | condition | prefixProduct before | answer after assignment | prefixProduct after multiply | next |
| --- | --- | --- | --- | --- | --- |
| `6` | `6<7 → true` | `720` | `[1, 1, 2, 6, 24, 120, 720]` | `720` | `index=7` |

After index `6`, the increment makes `index=7`; `7<7 → false` and the first loop exits. Its local index leaves scope; the next loop declares a fresh index.

### Block 3 — Multiply by the product strictly to the right

```java
int suffixProduct = 1;
for (int index = numbers.length - 1; index >= 0; index--) {
    answer[index] *= suffixProduct;
    suffixProduct *= numbers[index];
}
```

Initialize `suffixProduct=1` because nothing is right of the last position. Start at `7-1=6`. The first multiplication combines the saved left product with the current right product. Only after writing the answer does the second multiplication include this position for the next iteration to its left. Equivalent: `answer[index] = answer[index] * suffixProduct`; the order excludes the current input from its own answer.

**Right-pass iteration `index=6`.** Check `6>=0 → true`. Read saved left product `720` and right product `1`. Multiply `720×1=720` into `answer[6]`. Then prepare the next right-side product: `1×1=1`. Execute `index--`, producing `5`.

| index | condition | answer before | suffixProduct before | answer after multiplication | suffixProduct after | next |
| --- | --- | --- | --- | --- | --- | --- |
| `6` | `6>=0 → true` | `[1, 1, 2, 6, 24, 120, 720]` | `1` | `[1, 1, 2, 6, 24, 120, 720]` | `1` | `index=5` |

**Right-pass iteration `index=5`.** Check `5>=0 → true`. Read saved left product `120` and right product `1`. Multiply `120×1=120` into `answer[5]`. Then prepare the next right-side product: `1×6=6`. Execute `index--`, producing `4`.

| index | condition | answer before | suffixProduct before | answer after multiplication | suffixProduct after | next |
| --- | --- | --- | --- | --- | --- | --- |
| `5` | `5>=0 → true` | `[1, 1, 2, 6, 24, 120, 720]` | `1` | `[1, 1, 2, 6, 24, 120, 720]` | `6` | `index=4` |

**Right-pass iteration `index=4`.** Check `4>=0 → true`. Read saved left product `24` and right product `6`. Multiply `24×6=144` into `answer[4]`. Then prepare the next right-side product: `6×5=30`. Execute `index--`, producing `3`.

| index | condition | answer before | suffixProduct before | answer after multiplication | suffixProduct after | next |
| --- | --- | --- | --- | --- | --- | --- |
| `4` | `4>=0 → true` | `[1, 1, 2, 6, 24, 120, 720]` | `6` | `[1, 1, 2, 6, 144, 120, 720]` | `30` | `index=3` |

**Right-pass iteration `index=3`.** Check `3>=0 → true`. Read saved left product `6` and right product `30`. Multiply `6×30=180` into `answer[3]`. Then prepare the next right-side product: `30×4=120`. Execute `index--`, producing `2`.

| index | condition | answer before | suffixProduct before | answer after multiplication | suffixProduct after | next |
| --- | --- | --- | --- | --- | --- | --- |
| `3` | `3>=0 → true` | `[1, 1, 2, 6, 144, 120, 720]` | `30` | `[1, 1, 2, 180, 144, 120, 720]` | `120` | `index=2` |

**Right-pass iteration `index=2`.** Check `2>=0 → true`. Read saved left product `2` and right product `120`. Multiply `2×120=240` into `answer[2]`. Then prepare the next right-side product: `120×3=360`. Execute `index--`, producing `1`.

| index | condition | answer before | suffixProduct before | answer after multiplication | suffixProduct after | next |
| --- | --- | --- | --- | --- | --- | --- |
| `2` | `2>=0 → true` | `[1, 1, 2, 180, 144, 120, 720]` | `120` | `[1, 1, 240, 180, 144, 120, 720]` | `360` | `index=1` |

**Right-pass iteration `index=1`.** Check `1>=0 → true`. Read saved left product `1` and right product `360`. Multiply `1×360=360` into `answer[1]`. Then prepare the next right-side product: `360×2=720`. Execute `index--`, producing `0`.

| index | condition | answer before | suffixProduct before | answer after multiplication | suffixProduct after | next |
| --- | --- | --- | --- | --- | --- | --- |
| `1` | `1>=0 → true` | `[1, 1, 240, 180, 144, 120, 720]` | `360` | `[1, 360, 240, 180, 144, 120, 720]` | `720` | `index=0` |

**Right-pass iteration `index=0`.** Check `0>=0 → true`. Read saved left product `1` and right product `720`. Multiply `1×720=720` into `answer[0]`. Then prepare the next right-side product: `720×1=720`. Execute `index--`, producing `-1`.

| index | condition | answer before | suffixProduct before | answer after multiplication | suffixProduct after | next |
| --- | --- | --- | --- | --- | --- | --- |
| `0` | `0>=0 → true` | `[1, 360, 240, 180, 144, 120, 720]` | `720` | `[720, 360, 240, 180, 144, 120, 720]` | `720` | `index=-1` |

### Block 4 — Return the constructed array

```java
return answer;
```

After the last decrement, `index=-1`; `-1>=0 → false`. Skip the body, avoiding a negative array index. Return `[720,360,240,180,144,120,720]`. This is the constructed result, not either running product (both end at `720`). Without returning the array the caller gets no answer; a clearer equivalent variable name is `productsExceptIndex`.

## Complete reference solution

Runnable reference: [ProductExceptSelfSolution.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/prefixintervals/productexceptself/ProductExceptSelfSolution.java). The walkthrough immediately above explains the complete algorithm below; the `main` runner and assertions are in the linked Java file.

```java
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
