# 16 - Prefix/Suffix and Interval Lesson Artifacts

## 1) What I built

Prepared the next five LeetCode lesson artifacts in [Module 04](../interview/01-leetcode/lessons/04-prefix-suffix-intervals/README.md): Range Sum Query — Immutable, Subarray Sum Equals K, Product of Array Except Self, Merge Intervals, and Insert Interval.

Each lesson has one primary idea, a conceptual model before code, operation-by-operation narration for every worked iteration, exact condition results and complete array/map states, a full reference listing with adjacent inputs, separate IntelliJ-runnable practice/reference classes, three explained checkpoints, and observable stop/go criteria. All remain `TODO`; Two Sum remains the single active lesson.

## 2) Concepts learned

Concepts covered by the artifacts, without claiming learner mastery:

- Remove an earlier cumulative total to answer a range sum, or count earlier totals to find matching subarrays.
- Combine products strictly left and right without division, including zero inputs.
- Preserve interval coverage while sorting/merging or inserting into already sorted disjoint intervals.

## 3) Why this design

The module follows the next unbuilt section of the existing index. The user explicitly requested the next lessons in advance, so artifact preparation is separate from lesson activation. Narration and tables follow the learner’s preferred execution format. Worked tables show the needed arithmetic directly; existing simulation adapters do not implement these five algorithms, so no unrelated array animation was substituted.

## 4) Implementation notes

- Range queries use a construction-time snapshot with an extra leading zero; valid query indexes follow the problem contract.
- Subarray counting seeds `{0:1}` and looks up matching earlier totals before recording the current one; repeated totals retain frequencies.
- Product output is filled with left products, then multiplied by right products; output space is distinguished from auxiliary space.
- Merge sorts independent copied rows; insertion relies on the existing sorted/disjoint promise. Returned rows do not alias inputs.
- Practice programs deliberately retain TODO methods. Their “Practice ready” message does not mean a solution passed.
- No application endpoint or renderer implementation changed. The existing generated learning catalog discovers the Markdown; generated catalog/build output stays untracked.

## 5) Code pointers

- [Module landing page](../interview/01-leetcode/lessons/04-prefix-suffix-intervals/README.md)
- [Range query practice](../../src/test/java/com/instagram/backend/interview/leetcode/prefixintervals/rangesum/RangeSumQueryPractice.java) and [reference](../../src/test/java/com/instagram/backend/interview/leetcode/prefixintervals/rangesum/RangeSumQuerySolution.java)
- [Subarray counting practice](../../src/test/java/com/instagram/backend/interview/leetcode/prefixintervals/subarraysum/SubarraySumEqualsKPractice.java) and [reference](../../src/test/java/com/instagram/backend/interview/leetcode/prefixintervals/subarraysum/SubarraySumEqualsKSolution.java)
- [Product practice](../../src/test/java/com/instagram/backend/interview/leetcode/prefixintervals/productexceptself/ProductExceptSelfPractice.java) and [reference](../../src/test/java/com/instagram/backend/interview/leetcode/prefixintervals/productexceptself/ProductExceptSelfSolution.java)
- [Merge practice](../../src/test/java/com/instagram/backend/interview/leetcode/prefixintervals/mergeintervals/MergeIntervalsPractice.java) and [reference](../../src/test/java/com/instagram/backend/interview/leetcode/prefixintervals/mergeintervals/MergeIntervalsSolution.java)
- [Insert practice](../../src/test/java/com/instagram/backend/interview/leetcode/prefixintervals/insertinterval/InsertIntervalPractice.java) and [reference](../../src/test/java/com/instagram/backend/interview/leetcode/prefixintervals/insertinterval/InsertIntervalSolution.java)
- [Independent Java model checks](../../src/test/java/com/instagram/backend/interview/leetcode/prefixintervals/PrefixIntervalsVerification.java) — optional verification infrastructure, not a lesson prerequisite
- [Playwright browser verification and screenshots](../../tools/learning/verify-prefix-interval-lessons.mjs)
- Existing UI catalog discovery: [generate-lessons.mjs](../../../instagram-ui/scripts/generate-lessons.mjs)

## 6) Commit pointers and evidence

Commit hashes are recorded in the [commit map](../commit-map.md). Exact validation output and screenshot review notes: [verification record](../demo-proofs/2026-10-17/prefix-interval-verification.md).

- Eleven Java files compile; five reference runners pass; five practice runners reach their intentional TODO messages.
- Independent fixed-seed checks pass 6,270 range queries, 5,500 subarray cases, 500 products, 500 merge cases, and 500 insert cases. Interval coverage is checked at half-unit positions to catch gaps between integer endpoints.
- UI catalog generation and production build pass. The build reports a large-chunk warning; no bundle-splitting work is part of this lesson update.
- Browser integration checks render all five lessons, verify numbered reference code, three checkpoint answers, practice links, and mobile layout. Screenshots are captured and visually reviewed.
- HTTP `curl` verifies the local learning page serves successfully. There is no new backend API to exercise in this artifact-only change.

## 7) Pitfalls and fixes

- A map of prefix membership would lose multiple valid starts; use frequencies and lookup before insertion to avoid empty-range false positives.
- Multiplying before saving a directional product would include the excluded value; the narration and traces show the order explicitly.
- Using a smaller contained interval’s end would erase coverage; retain the maximum end.
- Under Insert Interval’s disjoint-input guarantee, extending past the original new end cannot itself bridge another old interval, since the next old start is beyond the merged old end. The note distinguishes preserving the covered tail from claiming an impossible new overlap chain.
- Comparing only covered integer points could miss real gaps, such as between 2 and 3; independent checks include half-points.

## 8) Next improvements

- [ ] Complete the currently active lesson’s learner checkpoints before activating another.
- [ ] Attempt these prepared lessons one at a time and record implementation/comprehension separately.
- [ ] Consider algorithm-derived visual adapters if tables cease to be sufficient; do not hardcode animation answers.
