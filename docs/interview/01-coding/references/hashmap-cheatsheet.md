# Java HashMap Cheat Sheet

Use this page when you need to refresh common `HashMap` operations and the patterns they support in coding interviews.

- Reference time: 15–20 minutes
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
Map<String, Integer> mutableCopy = new HashMap<>(scores);

Map<String, Integer> immutableSmallMap = Map.of("Ana", 10, "Ben", 20);
Map<String, Integer> immutableCopy = Map.copyOf(scores);

Map<String, Integer> destination = new HashMap<>();
destination.putAll(scores);
```

Program to the `Map` interface on the left and choose `HashMap` as the implementation on the right. `Map.of` and `Map.copyOf` return unmodifiable maps and reject null keys or values.

## Insert and update

```java
Integer oldValue = scores.put("Ana", 10); // null if no old mapping
scores.put("Ana", 15);                    // overwrite

scores.putIfAbsent("Ben", 20);            // write only when absent
scores.replace("Ana", 25);                // update only when present
scores.replace("Ana", 25, 30);            // require an exact old value
scores.replaceAll((name, score) -> score + 1);
```

## Read and test presence

```java
Integer score = scores.get("Ana");
int safeScore = scores.getOrDefault("Cara", 0);

boolean hasKey = scores.containsKey("Ana");
boolean hasValue = scores.containsValue(30);
int entries = scores.size();
boolean empty = scores.isEmpty();
```

`get` returning `null` is ambiguous when null values are allowed: the key may be absent or mapped to null. Use `containsKey` when the distinction matters.

`getOrDefault` returns a fallback but does not insert it into the map.

## Remove and clear

```java
Integer removedValue = scores.remove("Ana");
boolean removedExactPair = scores.remove("Ben", 20);
scores.clear();
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

// {a=[apple, ant], b=[boat]}
```

The mapping function runs only when the key has no non-null value. If the function returns `null`, no mapping is stored.

Adjacency-list example:

```java
Map<Integer, List<Integer>> neighbors = new HashMap<>();
neighbors.computeIfAbsent(1, key -> new ArrayList<>()).add(2);
neighbors.computeIfAbsent(1, key -> new ArrayList<>()).add(3);
```

## `computeIfPresent`: update only when present

```java
scores.computeIfPresent("Ana", (name, oldScore) -> oldScore + 5);
scores.computeIfPresent("Missing", (name, oldScore) -> oldScore + 5); // no change
```

It runs only when the key currently has a non-null value. Returning `null` removes the mapping.

## `compute`: handle present and absent

```java
scores.compute(
        "Ana",
        (name, oldScore) -> oldScore == null ? 1 : oldScore + 1);
```

`compute` always invokes the function. The old value may be `null`, and returning `null` removes or leaves absent the mapping.

## `merge`: insert or combine

`merge` is especially useful for frequency counting:

```java
Map<String, Integer> frequencies = new HashMap<>();

for (String word : List.of("red", "blue", "red")) {
    frequencies.merge(word, 1, Integer::sum);
}

// {red=2, blue=1}
```

When the key is absent, `merge` stores `1`. When present, it combines the old and supplied values. If the remapping function returns `null`, the key is removed.

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
Map<Integer, Integer> indexByValue = new HashMap<>();
indexByValue.put(numbers[index], index);
```

### Character frequency

```java
Map<Character, Integer> counts = new HashMap<>();
for (char character : text.toCharArray()) {
    counts.merge(character, 1, Integer::sum);
}
```

### Group by a derived key

```java
Map<Integer, List<String>> wordsByLength = new HashMap<>();
for (String word : words) {
    wordsByLength
            .computeIfAbsent(word.length(), key -> new ArrayList<>())
            .add(word);
}
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
