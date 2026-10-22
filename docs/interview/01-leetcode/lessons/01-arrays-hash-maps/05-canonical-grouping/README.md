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

## First understand the idea

The key idea is to give words with the same letters the same label, then collect words under that label.

Use `words=["eat","tea","tan","ate","nat","bat"]`. Sorting the letters of eat gives aet. Sorting tea also gives aet, so both belong together. Let `wordsByKey` mean “sorted-letter label → words with those letters.” After these two words it contains `aet:[eat,tea]`; tan creates `ant:[tan]`.

Continue the same calculation: ate sorts to aet and joins `[eat,tea,ate]`; nat sorts to ant and joins `[tan,nat]`; bat sorts to abt and starts `[bat]`. The resulting groups are `[[eat,tea,ate],[tan,nat],[bat]]`; map iteration may present those groups in a different order.

A shared label of this kind is a **canonical key**: one representation for values that should be treated as equivalent. In general, sort each word's letters, look up that key, and append the original word to its group. The key is sorted; the returned word retains its original spelling.

:::simulation
type: group-anagrams
language: java
words: ["eat", "tea", "tan", "ate", "nat", "bat"]
:::

## Block-by-block code walkthrough

Original snapshots are retained. Where a value changes several times, its cell shows the sequence ending at the final value.

**Running example for the code and tables below:** Input `words = ["eat","tea","tan","ate","nat","bat"]`; initially `wordsByKey = {}`. The original array and words remain unchanged.

Tables belong to the code block immediately above them. Inputs stay unchanged unless a block explicitly creates or changes a copy. Collection order is displayed for readability; HashMap/HashSet order is unspecified.

### Block 1 — Create one list per future canonical key

```java
Map<String, List<String>> wordsByKey = new HashMap<>();
```

**What it evaluates:** an empty map is created where each sorted-letter key will point to a mutable list of original words.

**Why it is needed:** one key must retain several values; a `Map<String, String>` could keep only one word per group.

**Concrete result:** after processing `"eat"` and `"tea"`, the entry is `"aet" -> ["eat", "tea"]`.

**What fails without it:** the method has no shared place to find and extend an earlier group.

**Equivalent clearer form:** `HashMap<String, List<String>> wordsByKey` works, but the `Map` interface states the required behavior more clearly.

**Debugger state at this block:**

`wordsByKey = {}`; no word-local `characters` or `key` exists yet.

### Block 2 — Process each original word exactly once

```java
for (String word : words) {
```

**What it evaluates:** the enhanced loop visits words from left to right. For an empty input array, it runs zero times.

**Why it is needed:** every input word must appear exactly once in the output.

**What fails without it:** skipping a word omits it from the result; a nested comparison against every other word repeats work.

**Equivalent clearer form:** an index loop is equivalent but unnecessary because output does not require original indexes.

**Debugger state at this block:**

| word | characters before sort | characters after sort | key | computeIfAbsent result | wordsByKey after add |
| --- | --- | --- | --- | --- | --- |
| `eat` | `['e', 'a', 't']` | `['a', 'e', 't']` | `aet` | `new empty list` | `{'aet': ['eat']}` |
| `tea` | `['t', 'e', 'a']` | `['a', 'e', 't']` | `aet` | `existing list` | `{'aet': ['eat', 'tea']}` |
| `tan` | `['t', 'a', 'n']` | `['a', 'n', 't']` | `ant` | `new empty list` | `{'aet': ['eat', 'tea'], 'ant': ['tan']}` |
| `ate` | `['a', 't', 'e']` | `['a', 'e', 't']` | `aet` | `existing list` | `{'aet': ['eat', 'tea', 'ate'], 'ant': ['tan']}` |
| `nat` | `['n', 'a', 't']` | `['a', 'n', 't']` | `ant` | `existing list` | `{'aet': ['eat', 'tea', 'ate'], 'ant': ['tan', 'nat']}` |
| `bat` | `['b', 'a', 't']` | `['a', 'b', 't']` | `abt` | `new empty list` | `{'aet': ['eat', 'tea', 'ate'], 'ant': ['tan', 'nat'], 'abt': ['bat']}` |

### Block 3 — Convert the word into a canonical key

```java
char[] characters = word.toCharArray();
Arrays.sort(characters);
String key = new String(characters);
```

**What it evaluates:** the immutable string becomes a mutable character copy, the copy is sorted, and a string key is created from that sorted order.

**Concrete result:** `"tea" → ['t','e','a'] → ['a','e','t'] → "aet"`.

**Why it is needed:** every anagram must produce the same hash-map key while words with different counts must produce different keys.

**What fails without it:** using `word` directly gives `"eat"` and `"tea"` separate entries. Sorting without copying is impossible because `String` is immutable.

**Equivalent clearer form:** a 26-count signature can build a linear-time key for lowercase English letters, but it is more complex than sorted text.

**Debugger state at this block:**

| word | characters before sort | characters after sort | key | computeIfAbsent result | wordsByKey after add |
| --- | --- | --- | --- | --- | --- |
| `eat` | `['e', 'a', 't']` | `['a', 'e', 't']` | `aet` | `new empty list` | `{'aet': ['eat']}` |
| `tea` | `['t', 'e', 'a']` | `['a', 'e', 't']` | `aet` | `existing list` | `{'aet': ['eat', 'tea']}` |
| `tan` | `['t', 'a', 'n']` | `['a', 'n', 't']` | `ant` | `new empty list` | `{'aet': ['eat', 'tea'], 'ant': ['tan']}` |
| `ate` | `['a', 't', 'e']` | `['a', 'e', 't']` | `aet` | `existing list` | `{'aet': ['eat', 'tea', 'ate'], 'ant': ['tan']}` |
| `nat` | `['n', 'a', 't']` | `['a', 'n', 't']` | `ant` | `existing list` | `{'aet': ['eat', 'tea', 'ate'], 'ant': ['tan', 'nat']}` |
| `bat` | `['b', 'a', 't']` | `['a', 'b', 't']` | `abt` | `new empty list` | `{'aet': ['eat', 'tea', 'ate'], 'ant': ['tan', 'nat'], 'abt': ['bat']}` |

### Block 4 — Create or reuse the group, then append

```java
wordsByKey.computeIfAbsent(key, ignored -> new ArrayList<>())
        .add(word);
```

**What it evaluates:** the mapping function runs only when `key` lacks a non-null value. The returned existing or new list then receives `word`.

**Concrete result:** `"eat"` creates the `"aet"` list; `"tea"` retrieves the same list and appends to it.

**Why it is needed:** each key needs exactly one shared mutable group.

**What fails without it:** calling `wordsByKey.get(key).add(word)` on the first word returns `null` and throws `NullPointerException`. Returning `null` from the mapping lambda would also make `.add` fail.

**Equivalent clearer form:** the longer `get`/null-check/`put` version below is identical and may be easier when group creation requires several statements.

**Debugger state at this block:**

| word | characters before sort | characters after sort | key | computeIfAbsent result | wordsByKey after add |
| --- | --- | --- | --- | --- | --- |
| `eat` | `['e', 'a', 't']` | `['a', 'e', 't']` | `aet` | `new empty list` | `{'aet': ['eat']}` |
| `tea` | `['t', 'e', 'a']` | `['a', 'e', 't']` | `aet` | `existing list` | `{'aet': ['eat', 'tea']}` |
| `tan` | `['t', 'a', 'n']` | `['a', 'n', 't']` | `ant` | `new empty list` | `{'aet': ['eat', 'tea'], 'ant': ['tan']}` |
| `ate` | `['a', 't', 'e']` | `['a', 'e', 't']` | `aet` | `existing list` | `{'aet': ['eat', 'tea', 'ate'], 'ant': ['tan']}` |
| `nat` | `['n', 'a', 't']` | `['a', 'n', 't']` | `ant` | `existing list` | `{'aet': ['eat', 'tea', 'ate'], 'ant': ['tan', 'nat']}` |
| `bat` | `['b', 'a', 't']` | `['a', 'b', 't']` | `abt` | `new empty list` | `{'aet': ['eat', 'tea', 'ate'], 'ant': ['tan', 'nat'], 'abt': ['bat']}` |


**Narrated execution — follow each operation, then check its values:**

**Iteration/check 1 — word = `eat`.**

- **✅ `for (String word : words)`:** read index `0` → `"eat"`.
- `char[] characters = word.toCharArray()` creates `['e', 'a', 't']`; `Arrays.sort(characters)` changes the copy to `['a', 'e', 't']`.
- `String key = new String(characters)` assigns `key="aet"`; original `word="eat"` is unchanged.
- **✅ Key absent inside `wordsByKey.computeIfAbsent`:** `true`; execute `ignored -> new ArrayList<>()`, store the new empty list, then return it.
- `.add(word)` changes that group `[] → ['eat']`; the table shows the full map.
- Continue to the next word.

| word | characters before sort | characters after sort | key | key absent? (inside computeIfAbsent) | action | wordsByKey after | next action |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `eat` | `['e', 'a', 't']` | `['a', 'e', 't']` | `aet` | `✅ true` | `create [] then append` | `{'aet': ['eat']}` | `next word` |

**Iteration/check 2 — word = `tea`.**

- **✅ `for (String word : words)`:** read index `1` → `"tea"`.
- `char[] characters = word.toCharArray()` creates `['t', 'e', 'a']`; `Arrays.sort(characters)` changes the copy to `['a', 'e', 't']`.
- `String key = new String(characters)` assigns `key="aet"`; original `word="tea"` is unchanged.
- **❌ Key absent inside `wordsByKey.computeIfAbsent`:** `false`; skip the creation lambda and return existing group `['eat']`.
- `.add(word)` changes that group `['eat'] → ['eat', 'tea']`; the table shows the full map.
- Continue to the next word.

| word | characters before sort | characters after sort | key | key absent? (inside computeIfAbsent) | action | wordsByKey after | next action |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `tea` | `['t', 'e', 'a']` | `['a', 'e', 't']` | `aet` | `❌ false` | `reuse list then append` | `{'aet': ['eat', 'tea']}` | `next word` |

**Iteration/check 3 — word = `tan`.**

- **✅ `for (String word : words)`:** read index `2` → `"tan"`.
- `char[] characters = word.toCharArray()` creates `['t', 'a', 'n']`; `Arrays.sort(characters)` changes the copy to `['a', 'n', 't']`.
- `String key = new String(characters)` assigns `key="ant"`; original `word="tan"` is unchanged.
- **✅ Key absent inside `wordsByKey.computeIfAbsent`:** `true`; execute `ignored -> new ArrayList<>()`, store the new empty list, then return it.
- `.add(word)` changes that group `[] → ['tan']`; the table shows the full map.
- Continue to the next word.

| word | characters before sort | characters after sort | key | key absent? (inside computeIfAbsent) | action | wordsByKey after | next action |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `tan` | `['t', 'a', 'n']` | `['a', 'n', 't']` | `ant` | `✅ true` | `create [] then append` | `{'aet': ['eat', 'tea'], 'ant': ['tan']}` | `next word` |

**Iteration/check 4 — word = `ate`.**

- **✅ `for (String word : words)`:** read index `3` → `"ate"`.
- `char[] characters = word.toCharArray()` creates `['a', 't', 'e']`; `Arrays.sort(characters)` changes the copy to `['a', 'e', 't']`.
- `String key = new String(characters)` assigns `key="aet"`; original `word="ate"` is unchanged.
- **❌ Key absent inside `wordsByKey.computeIfAbsent`:** `false`; skip the creation lambda and return existing group `['eat', 'tea']`.
- `.add(word)` changes that group `['eat', 'tea'] → ['eat', 'tea', 'ate']`; the table shows the full map.
- Continue to the next word.

| word | characters before sort | characters after sort | key | key absent? (inside computeIfAbsent) | action | wordsByKey after | next action |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `ate` | `['a', 't', 'e']` | `['a', 'e', 't']` | `aet` | `❌ false` | `reuse list then append` | `{'aet': ['eat', 'tea', 'ate'], 'ant': ['tan']}` | `next word` |

**Iteration/check 5 — word = `nat`.**

- **✅ `for (String word : words)`:** read index `4` → `"nat"`.
- `char[] characters = word.toCharArray()` creates `['n', 'a', 't']`; `Arrays.sort(characters)` changes the copy to `['a', 'n', 't']`.
- `String key = new String(characters)` assigns `key="ant"`; original `word="nat"` is unchanged.
- **❌ Key absent inside `wordsByKey.computeIfAbsent`:** `false`; skip the creation lambda and return existing group `['tan']`.
- `.add(word)` changes that group `['tan'] → ['tan', 'nat']`; the table shows the full map.
- Continue to the next word.

| word | characters before sort | characters after sort | key | key absent? (inside computeIfAbsent) | action | wordsByKey after | next action |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `nat` | `['n', 'a', 't']` | `['a', 'n', 't']` | `ant` | `❌ false` | `reuse list then append` | `{'aet': ['eat', 'tea', 'ate'], 'ant': ['tan', 'nat']}` | `next word` |

**Iteration/check 6 — word = `bat`.**

- **✅ `for (String word : words)`:** read index `5` → `"bat"`.
- `char[] characters = word.toCharArray()` creates `['b', 'a', 't']`; `Arrays.sort(characters)` changes the copy to `['a', 'b', 't']`.
- `String key = new String(characters)` assigns `key="abt"`; original `word="bat"` is unchanged.
- **✅ Key absent inside `wordsByKey.computeIfAbsent`:** `true`; execute `ignored -> new ArrayList<>()`, store the new empty list, then return it.
- `.add(word)` changes that group `[] → ['bat']`; the table shows the full map.
- **❌ Enhanced `for`:** no next word; `return new ArrayList<>(wordsByKey.values())` → `[[eat,tea,ate],[tan,nat],[bat]]` (group order unspecified).

| word | characters before sort | characters after sort | key | key absent? (inside computeIfAbsent) | action | wordsByKey after | next action |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `bat` | `['b', 'a', 't']` | `['a', 'b', 't']` | `abt` | `✅ true` | `create [] then append` | `{'aet': ['eat', 'tea', 'ate'], 'ant': ['tan', 'nat'], 'abt': ['bat']}` | `next word` |

There is no explicit `if` in this Java loop: `computeIfAbsent` makes the missing-key decision internally. After all six words, return the grouped values in a new outer list; group order is unspecified.

### Block 5 — Return a detached outer result list

```java
return new ArrayList<>(wordsByKey.values());
```

**What it evaluates:** `values()` exposes the map’s collection of group lists; the constructor copies those group references into the required `List<List<String>>` outer container.

**Concrete result:** a map with keys `"aet"` and `"ant"` returns an outer list containing their two groups. Order is unspecified because `HashMap` order is unspecified.

**Why it is needed:** the method contract returns grouped lists, not the key-to-group map or a live `Collection` view.

**What fails without it:** returning `wordsByKey.values()` does not match the declared return type and remains backed by the map.

**Equivalent clearer form:** create an empty result list and call `result.addAll(wordsByKey.values())`; the constructor is shorter.

**Debugger state at this block:**

For `tea`, sorting `[t,e,a]` produces `[a,e,t]`, so `key = "aet"`; the existing list `[eat]` becomes `[eat,tea]`. The lambda parameter `ignored` receives the missing key only when creating a list: `aet`, `ant`, then `abt`. Return a new outer list containing `[eat,tea,ate]`, `[tan,nat]`, and `[bat]`. HashMap does not guarantee the order of those three groups.

## Reference approach

```java
// Input: words = ["eat", "tea", "tan", "ate", "nat", "bat"]
// Expected return: [[eat, tea, ate], [tan, nat], [bat]] (group order may vary)

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

## Additional trace: build the canonical key

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

## Additional API lesson: how `computeIfAbsent` works

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
