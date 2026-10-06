# Java HashMap Cheat Sheet

Use this page when you need to refresh common `HashMap` operations and the patterns they support in coding interviews.

- Reference time: 25–35 minutes; review one section at a time
- Practice time: 20–30 minutes
- Runnable operations: [HashMapOperationsDemo.java](../../../../src/test/java/com/instagram/backend/interview/coding/arrayshashmaps/refresher/HashMapOperationsDemo.java)

## Mental model

A `HashMap<K, V>` associates a unique key with a value. It is useful when an algorithm repeatedly asks for information about something already seen.

| Repeated question | Starting structure |
|---|---|
| “Have I seen this value?” | `HashSet<Value>` |
| “Where did I see this value?” | `HashMap<Value, Index>` |
| “How many times did this appear?” | `HashMap<Value, Count>` |
| “Which items belong to this group?” | `HashMap<Key, List<Value>>` |
| “What comes after this node?” | `HashMap<Node, List<Node>>` |

## Common imports

```java
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
```

## Create and copy

```java
Map<String, Integer> scores = new HashMap<>();
scores.put("Ana", 10);
// scores contains the mapping "Ana" -> 10

Map<String, Integer> mutableCopy = new HashMap<>(scores);
mutableCopy.put("Ben", 20);
// mutableCopy contains "Ana" -> 10 and "Ben" -> 20
// scores still contains only "Ana" -> 10

Map<String, Integer> immutableSmallMap = Map.of("Ana", 10, "Ben", 20);
Map<String, Integer> immutableCopy = Map.copyOf(scores);
// put, remove, or replace on either immutable map throws
// UnsupportedOperationException

Map<String, Integer> destination = new HashMap<>();
destination.putAll(scores);
// destination now contains "Ana" -> 10
```

`new HashMap<>(scores)` creates a separate mutable map containing the same mappings. Adding a new mapping to the copy does not add it to `scores`.

Program to the `Map` interface on the left and choose `HashMap` as the implementation on the right. `Map.of` and `Map.copyOf` return unmodifiable maps and reject null keys or values.

## Insert and update

```java
Map<String, Integer> scores = new HashMap<>();

Integer oldValue = scores.put("Ana", 10);
// oldValue is null; scores contains "Ana" -> 10

oldValue = scores.put("Ana", 15);
// oldValue is 10; scores now contains "Ana" -> 15

scores.putIfAbsent("Ana", 99);
// no change because "Ana" already exists; its value remains 15

scores.putIfAbsent("Ben", 20);
// scores now also contains "Ben" -> 20

scores.replace("Ana", 25);
// "Ana" -> 25 because the key exists

boolean replaced = scores.replace("Ana", 25, 30);
// replaced is true; "Ana" -> 30 because its old value matched 25

scores.replaceAll((name, score) -> score + 1);
// mappings are now "Ana" -> 31 and "Ben" -> 21
```

`put` always stores the supplied value and returns the previous value, or `null` if there was no mapping. `putIfAbsent` does not overwrite a non-null existing value. `replace` changes only an existing mapping.

## Read and test presence

```java
Map<String, Integer> scores = new HashMap<>();
scores.put("Ana", 30);

Integer score = scores.get("Ana");
// score is 30

Integer missing = scores.get("Cara");
// missing is null

int safeScore = scores.getOrDefault("Cara", 0);
// safeScore is 0, but "Cara" was NOT inserted into scores

boolean hasKey = scores.containsKey("Ana");
// hasKey is true

boolean hasValue = scores.containsValue(30);
// hasValue is true

int entries = scores.size();
// entries is 1

boolean empty = scores.isEmpty();
// empty is false
```

`get` returning `null` is ambiguous when null values are allowed: the key may be absent or mapped to null. Use `containsKey` when the distinction matters.

`getOrDefault` returns a fallback but does not insert it into the map.

## Remove and clear

```java
Map<String, Integer> scores = new HashMap<>();
scores.put("Ana", 30);
scores.put("Ben", 20);

Integer removedValue = scores.remove("Ana");
// removedValue is 30; "Ana" is no longer present

boolean removedExactPair = scores.remove("Ben", 20);
// removedExactPair is true; "Ben" is no longer present

scores.clear();
// scores is empty; clear returns nothing
```

## `computeIfAbsent`: create only when missing

This is the common operation for grouping, adjacency lists, caches, and nested collections:

```java
Map<Character, List<String>> wordsByFirstLetter = new HashMap<>();

for (String word : List.of("apple", "ant", "boat")) {
    wordsByFirstLetter
            .computeIfAbsent(word.charAt(0), key -> new ArrayList<>())
            .add(word);
}

// wordsByFirstLetter maps:
// 'a' -> ["apple", "ant"]
// 'b' -> ["boat"]
```

On the first word beginning with `'a'`, `computeIfAbsent` creates an empty list, stores it under `'a'`, and returns that list so `add("apple")` can run. For `"ant"`, the existing list is returned; no second list is created.

The mapping function runs only when the key has no non-null value. If the function returns `null`, no mapping is stored.

Adjacency-list example:

```java
Map<Integer, List<Integer>> neighbors = new HashMap<>();
neighbors.computeIfAbsent(1, key -> new ArrayList<>()).add(2);
neighbors.computeIfAbsent(1, key -> new ArrayList<>()).add(3);

// neighbors maps 1 -> [2, 3]
```

## `computeIfPresent`: update only when present

```java
Map<String, Integer> scores = new HashMap<>();
scores.put("Ana", 20);

scores.computeIfPresent("Ana", (name, oldScore) -> oldScore + 5);
// "Ana" now maps to 25

scores.computeIfPresent("Missing", (name, oldScore) -> oldScore + 5);
// no change because "Missing" is absent
```

It runs only when the key currently has a non-null value. Returning `null` removes the mapping.

## `compute`: handle present and absent

```java
Map<String, Integer> scores = new HashMap<>();

scores.compute(
        "Ana",
        (name, oldScore) -> oldScore == null ? 1 : oldScore + 1);
// "Ana" was absent, so oldScore was null and "Ana" now maps to 1

scores.compute(
        "Ana",
        (name, oldScore) -> oldScore == null ? 1 : oldScore + 1);
// "Ana" was present with 1, so it now maps to 2
```

`compute` always invokes the function. The old value may be `null`, and returning `null` removes or leaves absent the mapping.

## `merge`: insert or combine

`merge` is especially useful for frequency counting:

```java
Map<String, Integer> frequencies = new HashMap<>();

for (String word : List.of("red", "blue", "red")) {
    frequencies.merge(word, 1, Integer::sum);
}

// frequencies maps "red" -> 2 and "blue" -> 1
```

For the first `"red"`, the key is absent, so `merge` stores `1`. For the second `"red"`, `Integer::sum` combines the old value `1` with the supplied value `1`, producing `2`. If a remapping function returns `null`, the key is removed.

The longer equivalent is:

```java
frequencies.put(word, frequencies.getOrDefault(word, 0) + 1);
```

## Which update method should I use?

| Goal | Method |
|---|---|
| Always store or overwrite | `put` |
| Store a supplied value only when missing | `putIfAbsent` |
| Lazily create a list, set, or object | `computeIfAbsent` |
| Update only an existing value | `computeIfPresent` or `replace` |
| Handle present and absent in one function | `compute` |
| Insert or combine a supplied value | `merge` |
| Read with a fallback without storing it | `getOrDefault` |

## Iterate

```java
for (String key : scores.keySet()) {
    System.out.println(key);
}

for (int value : scores.values()) {
    System.out.println(value);
}

for (Map.Entry<String, Integer> entry : scores.entrySet()) {
    System.out.println(entry.getKey() + " -> " + entry.getValue());
    entry.setValue(entry.getValue() + 1);
}

scores.forEach((key, value) -> System.out.println(key + " -> " + value));
```

Use `entrySet` when both key and value are needed. Do not structurally modify a map inside its enhanced loop.

Remove entries through a collection view when appropriate:

```java
scores.entrySet().removeIf(entry -> entry.getValue() < 10);
```

## Common interview patterns

### Value to index

```java
int[] numbers = {4, 7, 4};
Map<Integer, Integer> indexByValue = new HashMap<>();

for (int index = 0; index < numbers.length; index++) {
    indexByValue.put(numbers[index], index);
}

// indexByValue maps 4 -> 2 and 7 -> 1
```

The second `4` overwrites the earlier mapping `4 -> 0`, so the map retains the most recent index. Whether that is correct depends on the problem.

### Character frequency

```java
String text = "abb";
Map<Character, Integer> counts = new HashMap<>();

for (char character : text.toCharArray()) {
    counts.merge(character, 1, Integer::sum);
}

// counts maps 'a' -> 1 and 'b' -> 2
```

### Group by a derived key

```java
List<String> words = List.of("cat", "sun", "apple");
Map<Integer, List<String>> wordsByLength = new HashMap<>();

for (String word : words) {
    wordsByLength
            .computeIfAbsent(word.length(), key -> new ArrayList<>())
            .add(word);
}

// wordsByLength maps:
// 3 -> ["cat", "sun"]
// 5 -> ["apple"]
```

## Complexity and behavior

| Operation | Expected time |
|---|---:|
| `get`, `put`, `remove`, `containsKey` | Average `O(1)` |
| `containsValue` | `O(n)` |
| Iterate keys, values, or entries | `O(n)` |

Important behavior:

- `HashMap` does not guarantee iteration order.
- Use `LinkedHashMap` when insertion order matters.
- Use `TreeMap` when keys must stay sorted; its main operations are `O(log n)`.
- Mutable keys are dangerous because changing fields used by `equals` or `hashCode` can make an entry unreachable.
- Average constant time depends on a good `hashCode` distribution.

## Common mistakes

- Using `containsValue` when the algorithm needs fast key lookup.
- Assuming `getOrDefault` inserts the default.
- Calling `get` and immediately unboxing a possibly null result.
- Mutating the map structurally while iterating with an enhanced loop.
- Relying on `HashMap` iteration order.
- Making a compact `compute` expression too complicated to explain or debug.
- Storing the current Two Sum value before checking its complement, which can reuse the same element.

## Focused warm-up: Two Sum

Given a non-null integer array `numbers` and an integer `target`, return the indexes of two distinct elements whose values add to `target`.

Assumptions: the array has at least two values, exactly one valid pair exists, an element cannot be reused, and arithmetic stays within Java's `int` range.

Examples:

- `[2, 7, 11, 15]`, target `9` → `[0, 1]`
- `[3, 2, 4]`, target `6` → `[1, 2]`
- `[3, 3]`, target `6` → `[0, 1]`

Runnable files:

- Edit: [TwoSumPractice.java](../../../../src/test/java/com/instagram/backend/interview/coding/arrayshashmaps/refresher/TwoSumPractice.java)
- Reveal after attempting: [TwoSumSolution.java](../../../../src/test/java/com/instagram/backend/interview/coding/arrayshashmaps/refresher/TwoSumSolution.java)

For each index, calculate the needed complement, look for it among earlier values, return the two indexes when found, or store the current value and index.

Invariant: before processing index `i`, the map contains values from earlier indexes and an earlier index associated with each stored value.

- Average time: `O(n)`.
- Additional space: `O(n)`.

## Compile and run

```bash
./gradlew testClasses

java -cp build/classes/java/test \
  com.instagram.backend.interview.coding.arrayshashmaps.refresher.HashMapOperationsDemo

java -cp build/classes/java/test \
  com.instagram.backend.interview.coding.arrayshashmaps.refresher.TwoSumPractice

java -cp build/classes/java/test \
  com.instagram.backend.interview.coding.arrayshashmaps.refresher.TwoSumSolution
```

## Checkpoints

<details>
<summary>1. How is `getOrDefault` different from `computeIfAbsent`?</summary>

`getOrDefault` returns a fallback without modifying the map. `computeIfAbsent` can calculate and store a value when the key is missing.

</details>

<details>
<summary>2. When is `merge` a good choice?</summary>

Use it when an absent key should receive an initial value and an existing value should be combined with a new one, such as incrementing a frequency count.

</details>

<details>
<summary>3. Why does Two Sum check the complement before storing the current value?</summary>

The map must contain only earlier elements. Checking first prevents the current element from matching itself and guarantees distinct indexes.

</details>
