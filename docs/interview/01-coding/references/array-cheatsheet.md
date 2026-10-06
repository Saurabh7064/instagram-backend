# Java Array Cheat Sheet

Use this page when you need to refresh Java array syntax and the operations that appear most often in coding interviews.

- Reference time: 15–20 minutes
- Runnable examples: [ArrayOperationsDemo.java](../../../../src/test/java/com/instagram/backend/interview/coding/arrayshashmaps/refresher/ArrayOperationsDemo.java)

## Mental model

An array stores a fixed number of values in order. Each value is accessed through a zero-based index.

Choose an array when the number of positions is fixed, fast `O(1)` access by index matters, or the problem naturally uses positions, two pointers, or contiguous ranges. Use an `ArrayList` when the sequence must grow or shrink frequently.

## Common imports

```java
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
```

## Create and initialize

```java
int[] empty = new int[4];          // [0, 0, 0, 0]
int[] numbers = {5, 1, 5, 2};
String[] names = new String[3];    // [null, null, null]
int[][] grid = new int[2][3];      // 2 rows, 3 columns
```

Default values are `0` for numeric primitives, `false` for `boolean`, and `null` for object references.

## Read, update, and get length

```java
int first = numbers[0];
numbers[1] = 9;
int length = numbers.length;       // field, not length()
int last = numbers[numbers.length - 1];
```

Valid indexes are `0` through `length - 1`. Any other index throws `ArrayIndexOutOfBoundsException`.

## Iterate

Use an indexed loop when the position matters or the array must be updated:

```java
for (int index = 0; index < numbers.length; index++) {
    int value = numbers[index];
    numbers[index] = value * 2;
}
```

Use an enhanced loop when only the value matters:

```java
for (int value : numbers) {
    System.out.println(value);
}
```

Assigning to the enhanced-loop variable does not update the array:

```java
for (int value : numbers) {
    value *= 2; // changes only the local variable
}
```

Iterate a two-dimensional array:

```java
for (int row = 0; row < grid.length; row++) {
    for (int column = 0; column < grid[row].length; column++) {
        int value = grid[row][column];
    }
}
```

## Print and compare

```java
String text = Arrays.toString(numbers);
boolean same = Arrays.equals(new int[] {1, 2}, new int[] {1, 2});

String gridText = Arrays.deepToString(grid);
boolean sameGrid = Arrays.deepEquals(gridA, gridB);
```

`numbers.toString()` does not print the contents. Use `Arrays.toString`, or `Arrays.deepToString` for nested arrays.

## Fill and generate

```java
int[] values = new int[5];
Arrays.fill(values, -1);                       // [-1, -1, -1, -1, -1]
Arrays.fill(values, 1, 4, 7);                  // end index 4 is excluded

int[] squares = new int[5];
Arrays.setAll(squares, index -> index * index); // [0, 1, 4, 9, 16]
```

## Copy an array or range

```java
int[] original = {10, 20, 30, 40};
int[] clone = original.clone();
int[] longer = Arrays.copyOf(original, 6);         // [10, 20, 30, 40, 0, 0]
int[] middle = Arrays.copyOfRange(original, 1, 3); // [20, 30], end excluded

int[] destination = new int[2];
System.arraycopy(original, 1, destination, 0, 2);  // [20, 30]
```

Assignment creates an alias, not a copy:

```java
int[] alias = original; // both variables refer to the same array
```

For a nested array, `clone` and `copyOf` copy only the outer array. The inner arrays remain shared.

## Sort and search

```java
int[] values = {8, 3, 5, 1};
Arrays.sort(values);                       // [1, 3, 5, 8]
Arrays.sort(values, 1, 3);                 // end index is excluded

int foundIndex = Arrays.binarySearch(values, 5);
int missingResult = Arrays.binarySearch(values, 4);
```

`binarySearch` requires sorted input. A missing value returns `-(insertion point) - 1`, so test `result >= 0` rather than expecting exactly `-1`.

Sort object arrays with a comparator:

```java
String[] words = {"pear", "fig", "banana"};
Arrays.sort(words, Comparator.comparingInt(String::length));

Integer[] boxedNumbers = {8, 3, 5, 1};
Arrays.sort(boxedNumbers, Comparator.reverseOrder());
```

Primitive arrays cannot use a comparator. Box values as `Integer[]` only when comparator-based ordering is needed.

## Swap and reverse in place

```java
int[] values = {1, 2, 3, 4};

for (int left = 0, right = values.length - 1; left < right; left++, right--) {
    int temporary = values[left];
    values[left] = values[right];
    values[right] = temporary;
}

// [4, 3, 2, 1]
```

Java has no `Arrays.reverse` for primitive arrays. The two-pointer swap is the usual in-place approach.

## Aggregate with streams

```java
int sum = Arrays.stream(numbers).sum();
int maximum = Arrays.stream(numbers).max().orElseThrow();
long evenCount = Arrays.stream(numbers).filter(value -> value % 2 == 0).count();
int[] doubled = Arrays.stream(numbers).map(value -> value * 2).toArray();
```

In an interview, a normal loop is often easier to debug and explain. Use streams only when the transformation stays obvious.

## Convert between arrays and lists

```java
Integer[] boxed = {1, 2, 3};
List<Integer> fixedSize = Arrays.asList(boxed);
List<Integer> mutable = new ArrayList<>(Arrays.asList(boxed));

int[] primitive = {1, 2, 3};
List<Integer> boxedList = Arrays.stream(primitive).boxed().toList();
```

`Arrays.asList(primitive)` produces a `List<int[]>` containing one array, not a `List<Integer>`. Arrays cannot change length; use `ArrayList` for insertion and removal.

## Common interview shapes

```java
// Track the best value seen so far
int maximum = numbers[0];
for (int value : numbers) {
    maximum = Math.max(maximum, value);
}

// Two pointers
int left = 0;
int right = numbers.length - 1;
while (left < right) {
    // inspect numbers[left] and numbers[right]
    left++;
    right--;
}

// Prefix totals
int[] prefix = new int[numbers.length + 1];
for (int index = 0; index < numbers.length; index++) {
    prefix[index + 1] = prefix[index] + numbers[index];
}
```

## Complexity

| Operation | Time |
|---|---:|
| Read or write by index | `O(1)` |
| Scan or linear search | `O(n)` |
| Copy, fill, or reverse | `O(n)` |
| Sort primitive array | `O(n log n)` |
| Binary search sorted array | `O(log n)` |
| Insert/delete in the middle by shifting | `O(n)` |

## Common mistakes

- Using `length()` or `size()` instead of the array field `length`.
- Looping with `index <= numbers.length` instead of `index < numbers.length`.
- Expecting an enhanced-loop assignment to modify the array.
- Using binary search before sorting.
- Comparing arrays with `==` instead of `Arrays.equals`.
- Assuming assignment copies the array.
- Forgetting that range end indexes are excluded.

## Compile and run

```bash
./gradlew testClasses
java -cp build/classes/java/test \
  com.instagram.backend.interview.coding.arrayshashmaps.refresher.ArrayOperationsDemo
```

## Checkpoints

<details>
<summary>1. When should you use an indexed loop instead of an enhanced loop?</summary>

Use an indexed loop when the position matters, when another array uses the same position, or when you need to replace array elements.

</details>

<details>
<summary>2. What is the difference between `int[] copy = original` and `original.clone()`?</summary>

Assignment creates another reference to the same array. `clone()` creates a separate array containing the same primitive values.

</details>

<details>
<summary>3. What must be true before calling `Arrays.binarySearch`?</summary>

The array must already be sorted using an ordering compatible with the search.

</details>
