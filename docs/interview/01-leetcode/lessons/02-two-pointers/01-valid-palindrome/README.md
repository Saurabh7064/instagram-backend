# Micro-Lesson 01 — Valid Palindrome

## Question

> After ignoring non-alphanumeric characters and letter case, is a given string the same forward and backward?

Practice source: [Valid Palindrome on LeetCode](https://leetcode.com/problems/valid-palindrome/)

## What you will learn

- how two pointers compare matching boundaries without building another string;
- why irrelevant characters are skipped independently on each side;
- how the `left < right` guard keeps every `charAt` access safe;
- the invariant that everything outside the pointers has already matched;
- what every block of the reference method evaluates, why it exists, and what fails without it;
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

## Block-by-block code walkthrough

### Block 1 — Place the pointers at both ends

```java
int left = 0;
int right = text.length() - 1;
```

**What it evaluates:** `left` is the first valid string index. `right` is the final valid index because a string of length `n` uses indexes `0` through `n - 1`.

**Why it is needed:** a palindrome compares the outermost meaningful characters first, then moves toward the middle.

**Concrete results:**

- for `"racecar"`, `left = 0` and `right = 6`, so the first pair is `r` and `r`;
- for `"A"`, both pointers are `0`;
- for `""`, `right = -1`, and the outer loop never starts because `0 < -1` is false.

**What fails without it:** starting `right` at `text.length()` would make `text.charAt(right)` invalid. For `"abc"`, the length is `3`, but the final valid index is `2`; `charAt(3)` throws `StringIndexOutOfBoundsException`.

**Equivalent clearer form:** `int lastIndex = text.length() - 1; int right = lastIndex;` is more verbose but has the same behavior.

### Block 2 — Continue only while two different positions remain

```java
while (left < right) {
```

**What it evaluates:** the body runs only while the left pointer is strictly before the right pointer.

**Why it is needed:** when the pointers meet, the middle character has no different counterpart to check. If they cross, every required pair has already matched.

**Concrete result:** for `"aba"`, after matching the outer `a` characters, `left = 1` and `right = 1`. The condition `1 < 1` is false, so `b` is not compared with itself.

**What fails without it:** an unconditional loop would eventually read beyond the string or never stop. Using `left <= right` can still be made correct, but it performs an unnecessary middle-character comparison.

**Equivalent clearer form:** `while (right - left > 0)` expresses the same relationship, but `left < right` is easier to read and avoids arithmetic.

### Block 3 — Skip irrelevant characters on the left

```java
while (left < right && !Character.isLetterOrDigit(text.charAt(left))) {
    left++;
}
```

**What it evaluates:** Java first checks `left < right`. Only when that is true does it read `text.charAt(left)` and ask whether the character is neither a letter nor a digit.

**Why it is needed:** punctuation and spaces do not participate in the palindrome comparison. This loop moves `left` to the next meaningful character without creating a cleaned copy.

**Concrete result:** for `" ,a"`, `left` starts at `0`. The space is skipped, then the comma is skipped, and `left` becomes `2`. At that point `left < right` is false because both pointers are `2`, so short-circuit evaluation prevents another condition check.

**What fails without it:** `"a,b:a"` would compare punctuation as if it were meaningful and could reject a valid normalized palindrome.

**Why guard order matters:** `&&` evaluates left to right. The boundary check decides whether another pair remains before `charAt(left)` is evaluated. A clearer equivalent is:

```java
while (left < right) {
    char current = text.charAt(left);
    if (Character.isLetterOrDigit(current)) {
        break;
    }
    left++;
}
```

The equivalent is longer but makes the stop decision explicit.

### Block 4 — Skip irrelevant characters on the right independently

```java
while (left < right && !Character.isLetterOrDigit(text.charAt(right))) {
    right--;
}
```

**What it evaluates:** while two positions remain, it inspects the right character and moves `right` leftward when that character is punctuation or whitespace.

**Why it is needed:** the two ends can contain different amounts of punctuation. Skipping only the left side does not normalize the right boundary.

**Concrete result:** for `"a!!"`, `right` moves from index `2` to `1`, then from `1` to `0`. The pointers meet, so the string is accepted as the single relevant character `a`.

**What fails without it:** for `"a,"`, the algorithm would compare `a` with `,` and incorrectly return `false`.

**Equivalent clearer form:** the expanded `while`/`break` form shown for the left pointer can be mirrored with `text.charAt(right)` and `right--`.

### Block 5 — Normalize both meaningful characters

```java
char leftCharacter = Character.toLowerCase(text.charAt(left));
char rightCharacter = Character.toLowerCase(text.charAt(right));
```

**What it evaluates:** each current character is read and converted to lowercase. Digits remain unchanged.

**Why it is needed:** the problem says letter case is irrelevant, so `A` and `a` must compare as equal.

**Concrete result:** for `"Aa"`, the first expression produces `'a'` from `'A'`, and the second produces `'a'` from `'a'`.

**What fails without it:** direct comparison would treat `'A' != 'a'` and incorrectly reject `"Aa"`. Lowercasing only one side is also wrong: `"aA"` would become `'a'` versus `'A'`.

**Equivalent clearer form:** `Character.toUpperCase(...)` on both sides works too. Converting the entire string first is simpler to visualize but uses `O(n)` additional space.

### Block 6 — Stop at the first real mismatch

```java
if (leftCharacter != rightCharacter) {
    return false;
}
```

**What it evaluates:** it compares the two normalized boundary characters.

**Why it is needed:** one mismatched mirrored pair is enough to prove the whole string is not a palindrome.

**Concrete result:** for `"0P"`, the values are `'0'` and `'p'`; `'0' != 'p'` is true, so the method returns `false` immediately.

**What fails without it:** the method would keep moving inward and eventually return `true` even for strings such as `"ab"`.

**Equivalent clearer form:** `boolean charactersMatch = leftCharacter == rightCharacter; if (!charactersMatch) return false;` has the same behavior but names the decision.

### Block 7 — Consume the pair that just matched

```java
left++;
right--;
```

**What it evaluates:** `left` moves one position right and `right` moves one position left.

**Why it is needed:** the current pair has been proven equal, so the next iteration must inspect characters inside it. The invariant expands: everything outside the new range has matched.

**Concrete result:** in `"abba"`, matching indexes `0` and `3` changes the pointers to `1` and `2`, where the next pair is `b` and `b`.

**What fails without it:** the outer loop sees the same matching pair forever and becomes an infinite loop.

**Equivalent clearer form:** `left = left + 1; right = right - 1;` is identical but more explicit.

### Block 8 — Accept only after every required pair matched

```java
return true;
```

**What it evaluates:** reaching this line means no comparison returned `false` before the pointers met or crossed.

**Why it is needed:** the method must return the successful result for normal palindromes, empty strings, one-character strings, and strings containing only ignored characters.

**Concrete results:** `""`, `" "`, `"a"`, and `"A man, a plan, a canal: Panama"` all reach this line.

**What fails without it:** Java requires a boolean result on every path, so the method would not compile.

**Equivalent clearer form:** there is no clearer different algorithmic form; `return left >= right;` is redundant here because leaving the loop already proves that condition.

## Full dry runs

### Punctuation and case: `"A,b:a"`

| Step | `left` character | `right` character | Action |
|---:|---|---|---|
| 1 | `A` | `a` | lowercase to `a` and `a`; match; move inward |
| 2 | `,` | `:` | skip comma from the left and colon from the right |
| 3 | `b` | `b` | match; pointers meet/cross |
| 4 | — | — | return `true` |

### Immediate mismatch: `"0P"`

| Step | Values | Result |
|---:|---|---|
| 1 | `left = 0`, `right = 1` | enter outer loop |
| 2 | `'0'` and lowercase `'p'` | mismatch |
| 3 | `return false` | no later code runs |

### Only punctuation: `" , "`

The left skip loop advances until the pointers meet. The algorithm may compare the same final ignored character with itself once, which is safe but unnecessary; then it exits and returns `true`. An optional `if (left >= right) break;` after both skip loops would avoid that comparison, but it is not required for correctness.

## How the runnable files work

The practice `main` catches only `UnsupportedOperationException`. That lets the untouched starter print “Practice program is ready,” while a wrong implemented answer still throws `AssertionError` instead of being hidden.

`runChecks()` calls the learner method with normal and edge inputs. `assertEquals(...)` compares the expected and actual booleans and reports the named scenario when they differ. The reference `main` uses the same style of checks, so successful execution proves those examples pass; it does not by itself prove mastery or correctness for every possible input.

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
