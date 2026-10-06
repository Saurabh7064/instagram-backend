# Java Arrays + HashMap Cheat Sheet

Use this when Java syntax feels rusty. The operation catalog is a supplemental reference, followed by one focused Two Sum warm-up. It does not change the active lesson or provide evidence of mastery.

- Reference time: 15–20 minutes
- Practice time: 20–30 minutes
- Runnable operations demo: [ArrayHashMapOperationsDemo.java](../../../../src/test/java/com/instagram/backend/interview/coding/arrayshashmaps/refresher/ArrayHashMapOperationsDemo.java)

## Mental model

- An **array** stores values in order. An **index** is a value's zero-based position.
- A `HashMap<K, V>` stores a **key-value pair** so a key can retrieve associated information.
- For a current number, its **complement** is the number needed to reach the target: `target - current`.

Ask two questions before coding:

1. Do I need the value, its position, or a count?
2. What fact from an earlier iteration would save me from scanning again?

## Choose the structure first

| Need | Best starting structure | Why |
|---|---|---|
| Fast access by numeric position | Array | `values[index]` is `O(1)` |
| Fixed number of values | Array | Compact, direct storage |
| Grow or shrink a sequence | `ArrayList` | Arrays cannot change length |
| Remember whether a value appeared | `HashSet` | Stores membership only |
| Associate one value with another | `HashMap` | Stores key-value pairs |
| Count occurrences | `HashMap<T, Integer>` | Maps each value to its count |
| Group items | `HashMap<K, List<V>>` | Maps each group key to its members |
| Preserve insertion order | `LinkedHashMap` | `HashMap` order is unspecified |
| Keep keys sorted | `TreeMap` | Sorted keys with `O(log n)` operations |

## Common imports

```java
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
```

## Array operations

The `Arrays` utility class provides sorting, copying, filling, searching, comparison, printing, and stream creation.

### Create and initialize

```java
int[] empty = new int[4];          // [0, 0, 0, 0]
int[] numbers = {5, 1, 5, 2};
String[] names = new String[3];    // [null, null, null]
int[][] grid = new int[2][3];      // 2 rows, 3 columns
```

Default values are `0` for numeric primitives, `false` for `boolean`, and `null` for object references.

### Read, update, and get length

```java
int first = numbers[0];
numbers[1] = 9;
int length = numbers.length;       // field, not length()
int last = numbers[numbers.length - 1];
```

Valid indexes are `0` through `length - 1`. Any other index throws `ArrayIndexOutOfBoundsException`.

### Iterate

```java
for (int index = 0; index < numbers.length; index++) {
    int value = numbers[index];
    // Use when the index matters or you need to update the array.
}

for (int value : numbers) {
    // Use when only the value matters.
}

for (int row = 0; row < grid.length; row++) {
    for (int column = 0; column < grid[row].length; column++) {
        int value = grid[row][column];
    }
}
```

Assigning to the enhanced-loop variable does not update the array:

```java
for (int value : numbers) {
    value *= 2;       // changes only the local variable
}
```

### Print and compare

```java
String text = Arrays.toString(numbers);
boolean same = Arrays.equals(new int[] {1, 2}, new int[] {1, 2});

String gridText = Arrays.deepToString(grid);
boolean sameGrid = Arrays.deepEquals(gridA, gridB);
```

`numbers.toString()` does not print array contents. Use `Arrays.toString`, or `Arrays.deepToString` for nested arrays.

### Fill and generate values

```java
int[] values = new int[5];
Arrays.fill(values, -1);                   // [-1, -1, -1, -1, -1]
Arrays.fill(values, 1, 4, 7);              // end index 4 is excluded

int[] squares = new int[5];
Arrays.setAll(squares, index -> index * index); // [0, 1, 4, 9, 16]
```

### Copy arrays and ranges

```java
int[] original = {10, 20, 30, 40};
int[] clone = original.clone();
int[] longer = Arrays.copyOf(original, 6);       // extra slots become 0
int[] middle = Arrays.copyOfRange(original, 1, 3); // [20, 30], end excluded

int[] destination = new int[2];
System.arraycopy(original, 1, destination, 0, 2);
```

Assignment does not copy:

```java
int[] alias = original; // both variables point to the same array
```

For a nested array, `clone` and `copyOf` copy only the outer array; inner arrays are still shared.

### Sort and search

```java
int[] values = {8, 3, 5, 1};
Arrays.sort(values);                    // [1, 3, 5, 8]
Arrays.sort(values, 1, 3);              // sort a range; end is excluded

int index = Arrays.binarySearch(values, 5);
int missingResult = Arrays.binarySearch(values, 4);
```

`binarySearch` requires sorted input. A missing value returns `-(insertion point) - 1`, so test `result >= 0` instead of expecting exactly `-1`.

Primitive arrays sort ascending. For object arrays, a comparator can choose the order:

```java
String[] words = {"pear", "fig", "banana"};
Arrays.sort(words, Comparator.comparingInt(String::length));

Integer[] boxedNumbers = {8, 3, 5, 1};
Arrays.sort(boxedNumbers, Comparator.reverseOrder());
```

Primitive arrays cannot use a comparator. Box the values as `Integer[]` when comparator-based ordering is genuinely needed.

### Swap and reverse in place

```java
int[] values = {1, 2, 3, 4};

for (int left = 0, right = values.length - 1; left < right; left++, right--) {
    int temporary = values[left];
    values[left] = values[right];
    values[right] = temporary;
}

// values is now [4, 3, 2, 1]
```

Java has no `Arrays.reverse` for primitive arrays, so the two-pointer swap is the common in-place approach.

### Aggregate with streams

```java
int sum = Arrays.stream(numbers).sum();
int maximum = Arrays.stream(numbers).max().orElseThrow();
long evenCount = Arrays.stream(numbers).filter(value -> value % 2 == 0).count();
int[] doubled = Arrays.stream(numbers).map(value -> value * 2).toArray();
```

In an interview, a normal loop is often easier to debug and explain. Streams are useful when the transformation remains obvious.

### Convert arrays and lists carefully

```java
Integer[] boxed = {1, 2, 3};
List<Integer> fixedSize = Arrays.asList(boxed);
List<Integer> mutable = new ArrayList<>(Arrays.asList(boxed));

int[] primitive = {1, 2, 3};
List<Integer> boxedList = Arrays.stream(primitive).boxed().toList();
```

`Arrays.asList(primitive)` produces a `List<int[]>` containing one array, not a `List<Integer>`. Arrays are fixed-length; use `ArrayList` when insertion and removal are required.

### Array cost summary

| Operation | Time |
|---|---:|
| Read or write by index | `O(1)` |
| Scan or linear search | `O(n)` |
| Copy or fill | `O(n)` |
| Sort primitive array | `O(n log n)` |
| Binary search sorted array | `O(log n)` |
| Insert/delete in the middle by shifting | `O(n)` |

## HashMap operations

```java
import java.util.HashMap;
import java.util.Map;

Map<String, Integer> scores = new HashMap<>();
```

Program to the `Map` interface on the left and choose `HashMap` as the implementation on the right.

### Create and copy

```java
Map<String, Integer> empty = new HashMap<>();
Map<String, Integer> mutableCopy = new HashMap<>(scores);
Map<String, Integer> immutableSmallMap = Map.of("Ana", 10, "Ben", 20);
Map<String, Integer> immutableCopy = Map.copyOf(scores);

empty.putAll(scores);                            // copy into an existing mutable map
```

`Map.of` and `Map.copyOf` return unmodifiable maps and reject null keys or values.

### Insert and update

```java
Integer oldValue = scores.put("Ana", 10);       // null if no old mapping
scores.put("Ana", 15);                          // overwrites the old value

scores.putIfAbsent("Ben", 20);                  // writes only when absent
scores.replace("Ana", 25);                      // updates only when present
scores.replace("Ana", 25, 30);                  // replace only if old value matches
scores.replaceAll((name, score) -> score + 1);
```

### Read and test presence

```java
Integer score = scores.get("Ana");               // null when absent
int safeScore = scores.getOrDefault("Cara", 0);
boolean hasKey = scores.containsKey("Ana");
boolean hasValue = scores.containsValue(30);
int entries = scores.size();
boolean empty = scores.isEmpty();
```

`get` returning `null` is ambiguous if null values are allowed: the key may be absent or explicitly mapped to null. Use `containsKey` when that distinction matters.

### Remove and clear

```java
Integer removed = scores.remove("Ana");
boolean removedExactPair = scores.remove("Ben", 20);
scores.clear();
```

### `computeIfAbsent`: create a value only when missing

This is the common operation for grouping, adjacency lists, and nested collections:

```java
Map<Character, List<String>> wordsByFirstLetter = new HashMap<>();

for (String word : List.of("apple", "ant", "boat")) {
    wordsByFirstLetter
            .computeIfAbsent(word.charAt(0), key -> new ArrayList<>())
            .add(word);
}

// {a=[apple, ant], b=[boat]}
```

The mapping function runs only when the key has no non-null value. If it returns `null`, no mapping is stored.

A graph adjacency-list example uses the same shape:

```java
Map<Integer, List<Integer>> neighbors = new HashMap<>();
neighbors.computeIfAbsent(1, key -> new ArrayList<>()).add(2);
neighbors.computeIfAbsent(1, key -> new ArrayList<>()).add(3);
```

### `computeIfPresent`: update only when present

```java
scores.computeIfPresent("Ana", (name, oldScore) -> oldScore + 5);
scores.computeIfPresent("Missing", (name, oldScore) -> oldScore + 5); // no change
```

It runs only when the key currently has a non-null value. Returning `null` removes the mapping.

### `compute`: calculate whether present or absent

```java
scores.compute("Ana", (name, oldScore) -> oldScore == null ? 1 : oldScore + 1);
```

`compute` always invokes the function. The old value may be `null`, and returning `null` removes or leaves absent the mapping.

### `merge`: the shortest frequency-counter update

```java
Map<String, Integer> frequencies = new HashMap<>();

for (String word : List.of("red", "blue", "red")) {
    frequencies.merge(word, 1, Integer::sum);
}

// {red=2, blue=1}
```

When the key is absent, `merge` stores the supplied value `1`. When present, it combines the old and supplied values. A remapping result of `null` removes the key.

The longer equivalent is:

```java
frequencies.put(word, frequencies.getOrDefault(word, 0) + 1);
```

### Iterate over keys, values, or entries

```java
for (String key : scores.keySet()) {
    System.out.println(key);
}

for (int value : scores.values()) {
    System.out.println(value);
}

for (Map.Entry<String, Integer> entry : scores.entrySet()) {
    System.out.println(entry.getKey() + " -> " + entry.getValue());
    entry.setValue(entry.getValue() + 1); // safe value update during iteration
}

scores.forEach((key, value) -> System.out.println(key + " -> " + value));
```

Use `entrySet` when both key and value are needed. Do not add or remove map entries inside an enhanced `for` loop; use an iterator's `remove` or `removeIf` on a view when removal is required.

```java
scores.entrySet().removeIf(entry -> entry.getValue() < 10);
```

### Common interview patterns

```java
// Value -> index
Map<Integer, Integer> indexByValue = new HashMap<>();
indexByValue.put(numbers[index], index);

// Character frequency
Map<Character, Integer> counts = new HashMap<>();
for (char character : text.toCharArray()) {
    counts.merge(character, 1, Integer::sum);
}

// Group values
Map<Integer, List<String>> wordsByLength = new HashMap<>();
for (String word : words) {
    wordsByLength.computeIfAbsent(word.length(), key -> new ArrayList<>()).add(word);
}

// Read, modify, write
int updated = counts.getOrDefault(character, 0) + 1;
counts.put(character, updated);
```

### Which update method should I use?

| Goal | Method |
|---|---|
| Always store/overwrite a value | `put` |
| Store only when missing | `putIfAbsent` |
| Lazily create a list/set/object | `computeIfAbsent` |
| Update only an existing value | `computeIfPresent` or `replace` |
| Handle present and absent in one function | `compute` |
| Add or combine a supplied value | `merge` |
| Read with a fallback but do not store it | `getOrDefault` |

`getOrDefault` does not insert the default into the map. `computeIfAbsent` can insert its computed result.

### HashMap cost and behavior summary

| Operation | Expected time |
|---|---:|
| `get`, `put`, `remove`, `containsKey` | Average `O(1)` |
| `containsValue` | `O(n)` |
| Iterate keys, values, or entries | `O(n)` |

Important behavior:

- `HashMap` does not guarantee iteration order.
- Mutable objects make risky keys because changing fields used by `equals` or `hashCode` can make an entry unreachable.
- Average constant time depends on a good `hashCode` distribution.
- Prefer clear loops during interviews when a clever `compute` expression becomes hard to explain.

## Fast pattern recognition

| Repeated question in the algorithm | Likely tool |
|---|---|
| “Have I seen this value?” | `HashSet` |
| “Where did I see this value?” | `HashMap<Value, Index>` |
| “How many times did this appear?” | `HashMap<Value, Count>` + `merge` |
| “Which items belong to this group?” | `HashMap<Key, List<Value>>` + `computeIfAbsent` |
| “What comes after this node?” | Adjacency-list map + `computeIfAbsent` |
| “What is at position i?” | Array indexing |
| “Do I need insertion/removal?” | Usually `ArrayList`, not an array |

Use a `HashSet` when you only need “have I seen this?” Use a `HashMap` when you need information associated with a value, such as its index, count, or group.

## Focused warm-up: Two Sum

Given a non-null integer array `numbers` and an integer `target`, return the indexes of the two distinct elements whose values add to `target`.

Assumptions:

- `numbers.length >= 2`.
- Exactly one valid pair exists.
- The same array element cannot be used twice.
- Addition and subtraction stay within Java's `int` range.

Examples:

- `numbers = [2, 7, 11, 15]`, `target = 9` → `[0, 1]`
- `numbers = [3, 2, 4]`, `target = 6` → `[1, 2]`
- `numbers = [3, 3]`, `target = 6` → `[0, 1]`

Expected complexity: average `O(n)` time and `O(n)` additional space.

## Runnable code

- Edit and run: [TwoSumPractice.java](../../../../src/test/java/com/instagram/backend/interview/coding/arrayshashmaps/refresher/TwoSumPractice.java)
- Reveal after attempting: [TwoSumSolution.java](../../../../src/test/java/com/instagram/backend/interview/coding/arrayshashmaps/refresher/TwoSumSolution.java)

In IntelliJ, open the practice file, change only `twoSum`, and click the green triangle beside `main`.

## Approach

The slow approach uses two loops to try every pair, which takes `O(n²)` time. The repeated question is:

> Has the number I need already appeared, and at which index?

For each current index:

1. Compute `needed = target - numbers[index]`.
2. Look for `needed` in a map of earlier values to their indexes.
3. If present, return the earlier index and the current index.
4. Otherwise, store the current value and index, then continue.

For `[2, 7, 11, 15]` and target `9`:

| Current index | Current value | Needed | Map before lookup | Result |
|---:|---:|---:|---|---|
| 0 | 2 | 7 | `{}` | store `2 → 0` |
| 1 | 7 | 2 | `{2=0}` | return `[0, 1]` |

## Invariant and correctness

Before processing index `i`, the map contains values from earlier indexes and the index associated with each stored value.

If the complement is in the map, its index is earlier than `i`, so the algorithm returns two distinct elements whose sum is the target. For the unique valid pair, when the later element is reached, the earlier element is already in the map. Therefore the pair will be found.

## Complexity

- Average time: `O(n)` because each array element performs average constant-time map operations once.
- Additional space: `O(n)` when the answer appears near the end.
- Trade-off: the map uses memory to avoid the nested loop's repeated scans.

## Common mistakes

- Storing the current value before checking can accidentally reuse the same element when `target == 2 * value`.
- Returning the values instead of their indexes does not satisfy the problem.
- Using an enhanced `for` loop makes the required index harder to obtain.
- Calling `map.get(needed)` and unboxing directly to `int` can throw when the key is absent; use `containsKey` or keep the result as `Integer`.

Sorting plus two pointers is a reasonable alternative for finding values, but preserving original indexes requires extra bookkeeping. The map is the clearest fit here.

## Compile and run

Compile all refresher programs:

```bash
./gradlew testClasses
```

Run the representative operations demo:

```bash
java -cp build/classes/java/test \
  com.instagram.backend.interview.coding.arrayshashmaps.refresher.ArrayHashMapOperationsDemo
```

Run the editable practice program:

```bash
java -cp build/classes/java/test \
  com.instagram.backend.interview.coding.arrayshashmaps.refresher.TwoSumPractice
```

Run the reference solution only after trying:

```bash
java -cp build/classes/java/test \
  com.instagram.backend.interview.coding.arrayshashmaps.refresher.TwoSumSolution
```

## Checkpoints

Try each answer aloud before expanding it.

<details>
<summary>1. Why does the map store an index instead of only recording that a value exists?</summary>

The required output is a pair of indexes. Membership alone can prove the complement appeared, but the stored index tells us which earlier position to return.

</details>

<details>
<summary>2. Why do we look for the complement before storing the current value?</summary>

The map must contain only earlier elements. Checking first prevents the current element from matching itself and guarantees the two returned indexes are distinct.

</details>

<details>
<summary>3. What is the loop invariant?</summary>

Before index `i` is processed, the map contains values seen at indexes before `i`, associated with an earlier index where each value occurred.

</details>

## Stop/go

Return to the active lesson only when you can:

- write the indexed array loop without looking;
- explain `put`, `get`, and `containsKey` in plain language;
- implement `twoSum` without viewing the reference file;
- predict the map contents before each iteration for `[3, 2, 4]` with target `6`;
- answer all three checkpoints before revealing their answers.

If not, repeat the worked example slowly. A passing program proves the artifact works; it does not by itself prove mastery.
