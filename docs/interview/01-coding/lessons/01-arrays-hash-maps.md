# Lesson 01 — Arrays and Hash Maps in Java

- Status: `LEARNING`
- Estimated time: 90–120 minutes plus spaced review
- Prerequisites: Java loops, methods, arrays, and basic collections
- Today’s schedule: [Day 1 plan](../../daily/2026-10-04-day-01.md)

## The interview problem this pattern solves

Many array problems look difficult because a straightforward solution repeatedly asks the same question:

> “Have I seen this value before, and if so, what did I know about it?”

If you scan the array again for every element, two nested scans often produce `O(n²)` time. A set or map lets you remember useful information from earlier elements so each new element can be processed once.

The important interview skill is not memorizing `HashMap`. It is recognizing exactly what information must be remembered.

## Part 1 — Baseline assessment

Attempt this before reading Part 2.

### Prompt: longest consecutive run

Given an unsorted integer array, return the length of its longest run of consecutive values. Values in a run differ by one; their positions in the input do not matter. Your target is `O(n)` average time.

Examples:

- `[100, 4, 200, 1, 3, 2]` returns `4` because the run is `1, 2, 3, 4`.
- `[1, 2, 0, 1]` returns `3` because duplicates do not lengthen the run.
- `[]` returns `0`.

### Baseline protocol

1. Ask clarifying questions before coding.
2. Explain the brute-force or sorting approach and its complexity.
3. State what information an `O(n)` solution would need to remember.
4. Code in Java without hints for at most 30 minutes.
5. Test at least an empty case, duplicates, and separated runs.
6. Record your result; do not convert a hinted solution into a “pass.”

### Baseline evidence

- Time to understand:
- Time to choose an approach:
- Time to produce compilable code:
- Time to test/fix:
- Hint used? If yes, what kind?
- Final time and space complexity stated:
- Primary weakness exposed:

Stop here until the timer ends.

---

## Part 2 — Mental model

### Array: location is built into the structure

An array provides direct access when you already know an integer index. `values[i]` is effectively constant-time because the runtime can calculate the element’s memory position.

An array is a strong lookup structure when the key range is small and known. For lowercase English letters, an `int[26]` frequency table is usually simpler and cheaper than a `HashMap<Character, Integer>`.

The limitation is that arbitrary keys do not naturally map to safe, compact array indexes. A user ID of `9_000_000_000L` or a string such as `"saurabh"` needs another representation.

### HashSet: remember membership

Use a `HashSet<T>` when the only question is:

> “Is this key present?”

Typical uses:

- detect duplicates;
- mark visited graph nodes;
- remember allowed or blocked values;
- convert repeated membership scans into average `O(1)` lookup.

### HashMap: remember a relationship

Use a `HashMap<K, V>` when the key must retrieve information:

- value → count;
- value → index;
- key → first or latest position;
- canonical representation → group;
- prefix sum → frequency;
- identifier → object or state.

Say the relationship aloud before coding. “I need a map” is incomplete. “I need each previously seen value mapped to its index” is an implementable invariant.

## Part 3 — Complexity without hand-waving

For a Java `HashMap` or `HashSet`, `get`, `put`, `containsKey`, and `contains` are normally treated as average `O(1)` in interviews. That makes one pass through `n` elements average `O(n)`.

But hashing is not free:

- the key’s hash code must be computed;
- collisions must be resolved;
- the table may resize;
- entries consume extra memory;
- a poor or adversarial key distribution can degrade performance.

Your normal interview statement should be:

> “The algorithm uses average `O(n)` time because it performs a constant average number of hash operations per element, and `O(n)` additional space. Worst-case hash behavior can degrade, though Java’s collision handling mitigates many cases.”

Do not claim guaranteed `O(1)` unless the chosen structure and key range actually provide it.

## Part 4 — Six transformations to recognize

### 1. Membership

Signal: “duplicate,” “already seen,” “visited,” or “does the counterpart exist?”

Structure: `HashSet<T>`.

Invariant example: before processing index `i`, the set contains every distinct value from indexes `0` through `i - 1`.

### 2. Frequency

Signal: “how many,” “most common,” “same characters,” or “anagram.”

Structure: `HashMap<T, Integer>` or a fixed-size count array.

```java
Map<Integer, Integer> frequency = new HashMap<>();
for (int value : values) {
    frequency.merge(value, 1, Integer::sum);
}
```

### 3. Value to position

Signal: return indexes, preserve earliest/latest occurrence, or calculate a distance.

Structure: `HashMap<Value, Integer>`.

Choose deliberately between `put`, which replaces an old index, and `putIfAbsent`, which preserves the first one.

### 4. Complement lookup

Signal: two values must combine to a target or satisfy a relationship.

For a target sum, the complement of `x` is `target - x`. Ask whether that complement was seen before.

### 5. Grouping by canonical key

Signal: place equivalent items together.

Structure: `HashMap<CanonicalKey, List<Item>>`.

For anagrams, the canonical key could be a sorted string or a safely encoded character-frequency vector. The trade-off is simpler `O(k log k)` sorting versus an `O(k)` count representation with more careful key construction.

### 6. Boundary or sequence-start detection

Signal: items form runs, chains, or connected sequences regardless of input order.

Structure: usually a `HashSet`.

For a consecutive sequence, only start counting from a value `x` when `x - 1` is absent. This prevents repeatedly walking the same run.

## Part 5 — A decision process you can say aloud

When reading a problem, ask these questions in order:

1. Am I repeatedly searching earlier or later elements?
2. What exact fact would eliminate that repeated search?
3. Do I need only membership, or must I retrieve associated information?
4. Is the key range small enough for a direct array?
5. Does the algorithm need first occurrence, last occurrence, or a count?
6. What invariant is true before and after each iteration?
7. Is average-case hashing acceptable, and what extra space is used?

This is the reasoning interviewers want to hear. Naming the data structure without explaining the eliminated work is weaker.

## Part 6 — Guided example: two values reaching a target

### Problem shape

Given an integer array and a target, return indexes of two distinct elements whose values add to the target. Assume one valid pair exists.

### Brute force

Try each pair. There are roughly `n² / 2` pairs, so time is `O(n²)` and additional space is `O(1)`.

### Information to remember

At value `x`, the missing value is `target - x`. To return indexes, membership alone is insufficient; the algorithm needs:

> previously seen value → its index

### Loop invariant

Before processing index `i`, the map contains values from indexes strictly less than `i`. Therefore, finding the complement cannot reuse the same element.

### Java implementation

```java
static int[] twoSum(int[] values, int target) {
    Map<Integer, Integer> indexByValue = new HashMap<>();

    for (int i = 0; i < values.length; i++) {
        int complement = target - values[i];

        if (indexByValue.containsKey(complement)) {
            return new int[] {indexByValue.get(complement), i};
        }

        indexByValue.put(values[i], i);
    }

    throw new IllegalArgumentException("No pair reaches the target");
}
```

### Why lookup happens before insertion

If the current value is inserted first, a target such as `6` with a current value of `3` could match the current index with itself. Looking up first preserves the distinct-index requirement automatically.

### Complexity

- Average time: `O(n)`.
- Additional space: `O(n)`.
- Trade-off: memory is exchanged for avoiding repeated scans.

## Part 7 — Java-specific traps

### Missing key versus stored null

`map.get(key)` returns `null` for both a missing key and a key explicitly mapped to `null`. Use `containsKey` when that distinction matters. In interview code, avoid storing nulls unless the problem requires them.

### Mutable keys

A map key’s `equals` and `hashCode` behavior must remain stable while it is in the map. Mutating a key field used by those methods can make an entry effectively unreachable.

### Arrays as keys

Java arrays use identity-based `equals` and `hashCode`; two arrays with the same contents are not automatically equal map keys. Use a value-based key such as a `String`, immutable list, or custom record with correct equality.

### Integer overflow

`target - values[i]`, prefix sums, or counters can overflow `int`. If constraints allow totals outside the 32-bit range, use `long` keys and arithmetic.

### `getOrDefault`, `merge`, and `computeIfAbsent`

- `getOrDefault` is clear for simple reads or manual frequency updates.
- `merge(key, 1, Integer::sum)` is compact for counts.
- `computeIfAbsent(key, ignored -> new ArrayList<>())` is useful for grouping.

Choose the clearest form you can explain under pressure. Compactness is not the goal.

### Character assumptions

An `int[26]` assumes a restricted alphabet such as lowercase English letters. General Unicode text requires a different design, often based on code points rather than Java `char` values.

## Part 8 — Baseline solution analysis

Reveal this only after completing the timed attempt.

<details>
<summary>Show the longest-consecutive-sequence reasoning and Java solution</summary>

Put every value into a set. A value begins a run only if its predecessor is absent. From each run start, check successive values until the run ends.

Although the code contains a loop inside a loop, each distinct value belongs to one counted run, so the total number of successful successor checks is proportional to the number of distinct values. Average time is `O(n)` and additional space is `O(n)`.

```java
static int longestConsecutive(int[] values) {
    Set<Integer> unique = new HashSet<>();
    for (int value : values) {
        unique.add(value);
    }

    int longest = 0;

    for (int value : unique) {
        if (value == Integer.MIN_VALUE || !unique.contains(value - 1)) {
            int length = 1;
            int current = value;

            while (current != Integer.MAX_VALUE
                    && unique.contains(current + 1)) {
                current++;
                length++;
            }

            longest = Math.max(longest, length);
        }
    }

    return longest;
}
```

The `Integer.MIN_VALUE` and `Integer.MAX_VALUE` guards prevent overflow when checking the previous and next integers. A reasonable interview solution may omit them if constraints exclude those boundaries, but state the assumption.

</details>

## Part 9 — Practice ladder

Do not solve every exercise today. Use the ladder to diagnose the exact missing skill.

### Level A — Mechanics

1. Detect whether an array contains a duplicate.
2. Build a frequency map and return the most frequent value.
3. Find the first value that occurs exactly once.

### Level B — Pattern recognition

1. Return two indexes whose values reach a target.
2. Determine whether two strings are anagrams, stating alphabet assumptions.
3. Group words that are anagrams using a canonical key.

### Level C — Interview transfer

1. Longest consecutive sequence.
2. Find the length of the longest subarray with a required property when a prefix-sum map is appropriate.
3. Explain when sorting is preferable to hashing even if sorting is asymptotically slower.

Today, complete one Level A problem and one Level B problem after the baseline. Save Level C transfer work for the scheduled review unless the baseline was already easy and fully correct.

## Part 10 — Checkpoints

Answer aloud before opening each explanation.

<details>
<summary>1. What question distinguishes HashSet from HashMap?</summary>

Use a set when only membership matters: “Have I seen this key?” Use a map when the key must retrieve associated information such as a count, index, group, or state.

</details>

<details>
<summary>2. Why can a hash-based solution be O(n) even when it contains a loop with lookups?</summary>

There are `n` iterations and each iteration performs a constant average number of hash operations. Therefore the average total is `n × O(1) = O(n)`. This depends on average hash-table behavior, not a universal guaranteed constant time.

</details>

<details>
<summary>3. When is a frequency array better than a HashMap?</summary>

When keys come from a small, known, safely indexable range. Examples include lowercase English letters or a bounded set of statuses. The array has less overhead and predictable direct access, but it is inappropriate for a large, sparse, or unknown key space.

</details>

<details>
<summary>4. What invariant prevents the two-sum solution from using the same element twice?</summary>

Before processing index `i`, the map contains only elements at indexes smaller than `i`. The complement lookup occurs before inserting the current element, so any returned pair uses two distinct indexes.

</details>

<details>
<summary>5. Why is a Java int array risky as a HashMap key?</summary>

Arrays inherit identity-based equality and hashing. Two different arrays containing identical counts are not equal keys. Use a value-based immutable representation or a custom type with correct `equals` and `hashCode`.

</details>

<details>
<summary>6. Why does longest-consecutive sequence count only from values with no predecessor?</summary>

That condition identifies the unique beginning of each run. Without it, the algorithm could walk the same run again from every member, degrading toward `O(n²)`. Starting only at boundaries makes the total traversal proportional to the distinct values.

</details>

<details>
<summary>7. What is the real time-space trade-off in these problems?</summary>

The algorithm spends up to `O(n)` extra memory to remember information that prevents repeated searches. This commonly reduces average time from `O(n²)` to `O(n)`.

</details>

<details>
<summary>8. What should you say if sorting gives O(n log n) time but O(1) extra space?</summary>

State both valid approaches and connect the choice to requirements. Hashing usually favors average linear time with extra memory and unordered processing; sorting may favor lower auxiliary space, deterministic ordering, simpler traversal, or better practical locality, but it may mutate the input unless copied.

</details>

## Part 11 — Interview narration template

Practice saying this naturally rather than memorizing it word for word:

> “The brute-force approach repeatedly searches for related values, which costs quadratic time. The fact I need from prior elements is ____. I’ll store that as ____ in a HashSet/HashMap. Before each iteration, the structure contains ____. I perform lookup before/after insertion because ____. This gives average O(n) time and O(n) extra space. I’ll test ____ because it challenges the invariant.”

## Part 12 — Stop/go criteria

Do not mark this lesson `INTERVIEW_READY` merely because you read it.

### Stop and repeat a targeted exercise if

- you cannot state the map/set relationship before coding;
- you confuse membership with value retrieval;
- your code works only after several unstructured patches;
- you cannot explain why the complexity is average `O(n)`;
- you miss duplicates, empty input, or numeric boundaries without prompting.

### Proceed to two pointers and sliding window when

- you solve one unrehearsed medium hash-based problem within 35 minutes;
- you explain the brute-force approach, invariant, and trade-off aloud;
- your Java code compiles or is syntactically credible and handles agreed edge cases;
- you answer at least six of eight checkpoints correctly before revealing answers;
- you schedule and pass the one-day and one-week reviews.

## Review record

| Review | Scheduled | Result | Mistake or evidence | Next action |
|---|---|---|---|---|
| Initial | 2026-10-04 |  |  |  |
| +1 day | 2026-10-05 |  |  |  |
| +1 week | 2026-10-11 |  |  |  |
| +1 month | 2026-11-04 |  |  |  |
