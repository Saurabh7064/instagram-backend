# Array + HashMap Refresher — Two Sum

Use this as a warm-up when Java syntax feels rusty. It is a supplemental reference, not a change to the active lesson or evidence of mastery.

- Time: 20–30 minutes
- Primary idea: scan an array while remembering information about earlier values
- New terms: index, key-value pair, complement

## Mental model

- An **array** stores values in order. An **index** is a value's zero-based position.
- A `HashMap<K, V>` stores a **key-value pair** so a key can retrieve associated information.
- For a current number, its **complement** is the number needed to reach the target: `target - current`.

Ask two questions before coding:

1. Do I need the value, its position, or a count?
2. What fact from an earlier iteration would save me from scanning again?

## Java cheat sheet

### Arrays

```java
int[] numbers = {2, 7, 11, 15};

int first = numbers[0];       // read by index
numbers[1] = 8;               // update by index
int size = numbers.length;    // arrays use .length, not .size()

for (int index = 0; index < numbers.length; index++) {
    int value = numbers[index];
    // Use this loop when the position matters.
}

for (int value : numbers) {
    // Use this loop when only the value matters.
}
```

### HashMap

```java
Map<Integer, Integer> indexByValue = new HashMap<>();

indexByValue.put(7, 1);                 // store value 7 at index 1
boolean present = indexByValue.containsKey(7);
Integer index = indexByValue.get(7);    // null when the key is absent
int count = indexByValue.getOrDefault(7, 0);
indexByValue.put(7, count + 1);         // common frequency-count update
indexByValue.remove(7);
int entries = indexByValue.size();
```

Use a `HashSet` when you only need "have I seen this?" Use a `HashMap` when you need information associated with a value, such as its index or count.

## Focused problem: Two Sum

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

Compile both files:

```bash
./gradlew testClasses
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
