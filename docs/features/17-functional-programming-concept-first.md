# 17 - Concept-First Functional Programming Introduction

## 1) What I built

Reworked the [functional-programming introduction](../interview/04-java/01-functional-programming-coding/README.md) into four prerequisite-ordered micro-lessons: purity, functions as values, stream transformations, and an array-based LeetCode exercise. The original first-lesson URL remains valid. Original normalization Java files remain at their existing paths; their lesson now comes after the concepts it relies on.

## 2) Concepts learned

Covered by these artifacts; comprehension is still TODO:

- Predictable calculations and outside changes before stream syntax.
- Supplying behavior using a functional interface, a lambda, and a method reference.
- Mapping/filtering as independent transformations, then joining array traversals for LeetCode 1929 without forcing streams onto unsuitable algorithms.

## 3) Why this design

The original lesson assumed several APIs before explaining functional programming. The replacement begins with ordinary Java functions and arrays, then introduces syntax only after its purpose. Small programs precede the LeetCode bridge. Loops and streams are compared on clarity, input ownership, and algorithmic complexity rather than treating shorter expressions as inherently better.

## 4) Implementation notes

- First lesson uses scalar doubling and a new array; local loop/output writes are distinguished from changing caller-owned input.
- Second lesson passes one-int-to-one-int behavior into a reusable array traversal and explains that an interface does not enforce purity.
- Third lesson preserves the original normalization method, explains when processing starts, and traces logical values per word. It does not invent intermediate lists or application-level loop variables. Blank words skip lowercase processing.
- Fourth lesson contrasts two fresh array streams with a two-half copying loop, both O(n). The source array remains unchanged and output arrays are independent.
- Stream.toList produces an unmodifiable result list; array outputs remain modifiable. Source preservation and output modifiability are separate contracts.
- Track stays inactive, all four lessons stay TODO, and no existing LeetCode mastery status changes.

## 5) Code pointers

- [Pure functions practice](../../src/test/java/com/instagram/backend/interview/functionalprogramming/foundations/PureFunctionsPractice.java) and [reference](../../src/test/java/com/instagram/backend/interview/functionalprogramming/foundations/PureFunctionsSolution.java)
- [Passing behavior practice](../../src/test/java/com/instagram/backend/interview/functionalprogramming/functionsasvalues/FunctionsAsValuesPractice.java) and [reference](../../src/test/java/com/instagram/backend/interview/functionalprogramming/functionsasvalues/FunctionsAsValuesSolution.java)
- Preserved [normalization practice](../../src/test/java/com/instagram/backend/interview/functionalprogramming/puretransformations/NormalizeWordsPractice.java) and [reference](../../src/test/java/com/instagram/backend/interview/functionalprogramming/puretransformations/NormalizeWordsSolution.java)
- [Array concatenation practice](../../src/test/java/com/instagram/backend/interview/functionalprogramming/arrayconcatenation/ArrayConcatenationPractice.java) and [reference](../../src/test/java/com/instagram/backend/interview/functionalprogramming/arrayconcatenation/ArrayConcatenationSolution.java)
- Optional [independent cross-lesson verifier](../../src/test/java/com/instagram/backend/interview/functionalprogramming/FunctionalProgrammingVerification.java)
- [Browser integration/proof script](../../tools/learning/verify-functional-lessons.mjs)
- [Persistent teaching standard](../../AGENTS.md#functional-programming-lesson-standard)

## 6) Commit pointers and verification

Hashes and outcomes are recorded in the [commit map](../commit-map.md); exact evidence and reviewed screenshot links are in the [verification record](../demo-proofs/2026-10-17/functional-verification.md).

Verification covers Java compilation, four reference runners and four intentional practice TODO states, 400 model cases per lesson, rendered content and reference line numbers for all four lesson routes, mobile layout, HTTP serving, and visual screenshot review. No application endpoint or renderer implementation is changed.

## 7) Pitfalls and fixes

- A stream is a traversal recipe, not an intermediate stored list. Explain lazy setup and logical emissions without claiming a fixed internal allocation strategy.
- A pure-looking callback may have side effects; the supplied operation’s behavior matters, not its interface alone.
- Strings need not allocate a fresh object for every unchanged transformation. Input preservation follows from no writes to the input and immutable string values, not assumed object identity.
- A program can use arrays and functional design together. An ordinary loop can preserve purity; a stream can violate it through callbacks.

## 8) Next improvements

- [ ] Activate only lesson 01 when the learner chooses this track and take a plain-language checkpoint attempt.
- [ ] Keep later reduction/grouping/Optional topics TODO until prerequisites are demonstrated.
- [ ] Use fresh coding problems for retrieval; do not equate reading these examples with mastery.
