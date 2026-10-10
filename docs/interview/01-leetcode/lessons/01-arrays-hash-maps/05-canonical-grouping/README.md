# Micro-Lesson 05 — Grouping by a Canonical Key

## Question

> Given an array of lowercase English words, group all anagrams together. Group order and word order inside groups may vary.

Practice source: [Group Anagrams on LeetCode](https://leetcode.com/problems/group-anagrams/)

## What you will learn

- what a canonical key is and why equivalent values need the same representation;
- why sorting letters produces the same key for every anagram in a group;
- how a map changes from one-value lookup to one-key/many-values grouping;
- how `computeIfAbsent` creates a group only when needed;
- how key construction determines the overall time complexity.

## Lesson status

- Learning status: `TODO`; initialized on 2026-10-09 but not yet active
- Time: 45–60 minutes
- Prerequisite: frequency counting and HashMap value storage
- Primary idea: transform equivalent values into the same key, then group by that key
- New terms: canonical key, equivalence group, `computeIfAbsent`

Do not begin this lesson while an earlier module lesson remains `LEARNING`.

## Inputs, examples, and target

Assumptions:

- Every array element is non-null.
- Words contain lowercase English letters only.
- Empty strings are valid.
- Every input word must appear exactly once in the output.

Examples:

- `["eat", "tea", "tan", "ate", "nat", "bat"]` → groups equivalent to `[["eat", "tea", "ate"], ["tan", "nat"], ["bat"]]`
- `[""]` → `[[""]]`
- `["a"]` → `[["a"]]`

## Why the problem exists

The input may contain many words, and directly comparing every word with every other word repeats work. A grouping map needs a key that is identical for anagrams and different for non-anagrams.

A **canonical key** is one consistent representation chosen for all equivalent values. Sorting a word's letters provides that key:

```text
eat -> aet
tea -> aet
ate -> aet
tan -> ant
nat -> ant
```

## Reference approach

```java
static List<List<String>> groupAnagrams(String[] words) {
    Map<String, List<String>> wordsByKey = new HashMap<>();

    for (String word : words) {
        char[] characters = word.toCharArray();
        Arrays.sort(characters);
        String key = new String(characters);

        wordsByKey.computeIfAbsent(key, ignored -> new ArrayList<>())
                .add(word);
    }

    return new ArrayList<>(wordsByKey.values());
}
```

## Build the canonical key

```java
char[] characters = word.toCharArray();
Arrays.sort(characters);
String key = new String(characters);
```

Strings are immutable, so `toCharArray` creates sortable characters. Sorting places equal letters in the same order, and `new String(characters)` creates the map key.

For `"tea"`:

```text
['t', 'e', 'a'] -> sort -> ['a', 'e', 't'] -> "aet"
```

Without a consistent transformation, the map would treat `"eat"` and `"tea"` as different keys.

## Short lesson: how `computeIfAbsent` works

```java
V value = map.computeIfAbsent(key, mappingKey -> createValueFor(mappingKey));
```

`computeIfAbsent` means: “For this key, return its current non-null value; if it has none, use this function to create and store one.” The lambda receives the key and supplies the value. The method returns either the existing value or the newly created one.

In the Group Anagrams solution, the value is a list:

```java
wordsByKey.computeIfAbsent(key, ignored -> new ArrayList<>())
        .add(word);
```

The first word with key `"aet"` creates and stores an empty list, then appends `"eat"`. When `"tea"` arrives with the same key, the map returns that existing list; no new list is created, and `"tea"` is appended to it.

### Example 1: trace the existing group

Suppose the map starts empty and these words are processed:

| Word | Key | What `computeIfAbsent` returns | Map after appending |
|---|---|---|---|
| `"eat"` | `"aet"` | Creates and stores a new list | `"aet" -> ["eat"]` |
| `"tea"` | `"aet"` | Reuses that list | `"aet" -> ["eat", "tea"]` |
| `"tan"` | `"ant"` | Creates and stores a different list | `"ant" -> ["tan"]`, `"aet" -> ["eat", "tea"]` |

`add(word)` runs after `computeIfAbsent` returns, so it appends to whichever list was returned.

### Example 2: group values by a different key

The value does not have to be a `List`. For example, collect tags by post ID:

```java
Map<Long, Set<String>> tagsByPost = new HashMap<>();

tagsByPost.computeIfAbsent(postId, ignored -> new HashSet<>())
        .add(tag);
```

The first tag for a post creates its set; later tags for that post reuse the same set. A different post ID gets a different set.

### Example 3: cache a value calculated from its key

The method can also create one non-collection value:

```java
Map<String, Integer> lengthByWord = new HashMap<>();
int length = lengthByWord.computeIfAbsent("instagram", word -> word.length());
```

If `"instagram"` has no value yet, the lambda receives that string, calculates `9`, and stores `"instagram" -> 9`. A later call for `"instagram"` returns the stored `9` without calculating it again.

### Compare with the longer form

An equivalent version for the list example is:

```java
List<String> group = wordsByKey.get(key);
if (group == null) {
    group = new ArrayList<>();
    wordsByKey.put(key, group);
}
group.add(word);
```

Use `computeIfAbsent` when the missing value can be created in one clear expression. The longer form can be easier to read when creation needs several steps.

### Important detail

The mapping function runs only when the key is absent or currently maps to `null`. It should return a non-null value: if it returns `null`, nothing is stored and `computeIfAbsent` returns `null`. In the examples above, each function returns a new collection or a calculated length.

## Loop invariant

Before each word is processed, every earlier word appears exactly once in the list associated with its sorted-letter key.

Adding the current word to the list for its key preserves that statement.

## Correctness

- Anagrams have the same letters with the same counts, so sorting produces the same key and places them in the same group.
- Non-anagrams differ in at least one letter count, so their sorted strings differ and they cannot share a group.
- Each input word is processed once and appended once, so no word is omitted or duplicated.

## Complexity

For `w` words of maximum length `k`:

- Time: `O(w × k log k)` because each word's characters are sorted.
- Additional space: `O(w × k)` for keys and grouped output, excluding the required output when stated separately.

## Pitfalls and alternatives

- Using the original word as the key does not group reordered letters.
- Using only a `HashSet<Character>` as the key loses duplicate counts.
- A 26-count signature can reduce key construction to `O(k)` per word under the lowercase-English constraint, but sorted strings are the clearer first canonical-key technique.
- Output order is unspecified, so tests must compare normalized groups rather than raw list order.


## Practice after learning the concept

- Write here: [GroupAnagramsPractice.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/arrayshashmaps/canonicalgrouping/GroupAnagramsPractice.java)
- Reveal after attempting: [GroupAnagramsSolution.java](../../../../../../src/test/java/com/instagram/backend/interview/leetcode/arrayshashmaps/canonicalgrouping/GroupAnagramsSolution.java)

When this lesson becomes active, change only `groupAnagrams` and run the practice class from IntelliJ.

## Questions and explained answers

<details>
<summary>1. Why do all anagrams produce the same sorted key?</summary>

Anagrams contain the same letters with the same counts. Sorting those letters places them in one identical order, regardless of the original order.

</details>

<details>
<summary>2. What does <code>computeIfAbsent</code> accomplish here?</summary>

It returns the existing list for a key or creates, stores, and returns a new list when the key has no group yet.

</details>

<details>
<summary>3. Why is the time O(w × k log k)?</summary>

Each of `w` words has at most `k` characters, and sorting one word costs `O(k log k)`. The average map work is smaller than the sorting term.

</details>

## Stop/go

Proceed only when you can build a canonical key, explain `computeIfAbsent`, justify correctness and complexity, and pass every practice check.
