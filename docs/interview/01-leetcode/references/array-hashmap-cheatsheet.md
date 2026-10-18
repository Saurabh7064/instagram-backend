# Java Array, HashSet, and HashMap References

The combined reference has been split so each data structure can be reviewed independently:

1. [Java Array Cheat Sheet](./array-cheatsheet.md) — creation, indexing, iteration, copying, filling, sorting, searching, reversing, streams, conversions, patterns, complexity, and pitfalls.
2. [Java HashSet Cheat Sheet](./hashset-cheatsheet.md) — membership, uniqueness, bulk operations, set algebra, conversions, ordering variants, duplicate detection, complexity, and pitfalls.
3. [Java HashMap Cheat Sheet](./hashmap-cheatsheet.md) — CRUD, `computeIfAbsent`, `computeIfPresent`, `compute`, `merge`, iteration, grouping, counting, Two Sum, complexity, and pitfalls.

The active coding lesson remains unchanged. These pages are supplemental refreshers and do not represent mastery progress.

## Two Sum debugger trace

Use the [complete Two Sum variable and map trace](../lessons/01-arrays-hash-maps/03-value-to-index/README.md#debugger-trace--every-variable-and-collection) for input `[3,2,4]`, target `6`. The [refresher reference](../../../../src/test/java/com/instagram/backend/interview/leetcode/arrayshashmaps/refresher/TwoSumSolution.java) uses `indexByValue` where the lesson uses `earlierIndexByValue`, and `needed` where the lesson uses `complement`; all other algorithm state and the returned `[1,2]` are identical. Initially `indexByValue={}`; index 0 computes `needed=3`, misses, and stores `{3:0}`; index 1 computes `needed=4`, misses, and stores `{3:0,2:1}`; index 2 computes `needed=2`, finds `earlierIndex=1`, and returns `[1,2]` before insertion.
