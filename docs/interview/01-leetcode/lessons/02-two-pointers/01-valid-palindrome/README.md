# Micro-Lesson 01 — Valid Palindrome

## Question

> After ignoring non-alphanumeric characters and letter case, is a given string the same forward and backward?

Practice source: [Valid Palindrome on LeetCode](https://leetcode.com/problems/valid-palindrome/)

## What you will learn

- how two pointers compare matching boundaries without building another string;
- why irrelevant characters are skipped independently on each side;
- how the `left < right` guard keeps every `charAt` access safe;
- the invariant that everything outside the pointers has already matched;
- when a preprocessing solution is simpler but uses additional space.

## Lesson status

- Learning status: `TODO`; initialized but not active
- Prerequisite: array/string indexing and loop boundaries
- Primary idea: move two boundaries inward while a condition holds
- New terms: two pointers, normalization, invariant

## Inputs, examples, and target

Assumptions:

- `text` is non-null and contains printable English letters, digits, spaces, and punctuation.
- A string with zero relevant characters is a palindrome.

Examples:

- `"A man, a plan, a canal: Panama"` → `true`
- `"race a car"` → `false`
- `" "` → `true`
- `"0P"` → `false`

Target: `O(n)` time and `O(1)` additional space.

## Concept: compare only meaningful boundaries

Start `left` at the first character and `right` at the last. Before comparing:

- move `left` past punctuation and spaces;
- move `right` past punctuation and spaces;
- lowercase both relevant characters;
- fail immediately when they differ;
- otherwise move both inward.

For `"a,b:a"`, the useful comparisons are `a` with `a`, then `b` with `b`. The comma and colon never need to be copied or removed.

## Why the guard appears first

```java
while (left < right && !Character.isLetterOrDigit(text.charAt(left))) {
    left++;
}
```

Java evaluates `&&` left to right. When the pointers meet, `left < right` is false, so `charAt(left)` is not evaluated by this loop. Reversing the conditions would inspect a character before confirming that another comparison is still needed.

## Reference approach

```java
int left = 0;
int right = text.length() - 1;

while (left < right) {
    while (left < right && !Character.isLetterOrDigit(text.charAt(left))) {
        left++;
    }
    while (left < right && !Character.isLetterOrDigit(text.charAt(right))) {
        right--;
    }

    char leftCharacter = Character.toLowerCase(text.charAt(left));
    char rightCharacter = Character.toLowerCase(text.charAt(right));
    if (leftCharacter != rightCharacter) {
        return false;
    }

    left++;
    right--;
}
return true;
```

## Invariant and correctness

Before each outer-loop iteration, every relevant character strictly outside `[left, right]` has a matching character on the opposite side. Skipping irrelevant characters preserves that fact. A mismatch proves the string cannot be a palindrome; reaching the middle proves every required pair matched.

## Complexity and alternatives

- Time: `O(n)` because each pointer crosses each character at most once.
- Additional space: `O(1)`.
- Alternative: construct a normalized string and reverse it. That is easy to read but requires `O(n)` additional space.

## Common pitfalls

- comparing punctuation instead of skipping it;
- lowercasing only one side;
- using `left <= right` and performing an unnecessary middle comparison;
- rebuilding the whole string when constant space is the intended concept.

## Practice after learning the concept

- Write here: [ValidPalindromePractice.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/twopointers/validpalindrome/ValidPalindromePractice.java)
- Reveal after attempting: [ValidPalindromeSolution.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/twopointers/validpalindrome/ValidPalindromeSolution.java)

## Questions and explained answers

<details>
<summary>1. What is true about characters outside the two pointers?</summary>

Every relevant character outside the current range has already been matched with its counterpart on the opposite side.

</details>

<details>
<summary>2. Why can punctuation be skipped instead of removed first?</summary>

The palindrome definition ignores it. Advancing a pointer past it reaches the next meaningful comparison without allocating a normalized copy.

</details>

<details>
<summary>3. Why is the total time O(n) despite nested while loops?</summary>

Neither pointer ever moves backward. Across all loops, `left` and `right` together advance across the string only once.

</details>

## Stop/go

Proceed only when you can state the invariant, explain the guard order, predict punctuation-only input, implement the method, and pass all checks.
