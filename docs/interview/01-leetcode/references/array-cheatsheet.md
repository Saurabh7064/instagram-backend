# Java Array Cheat Sheet

Use this page when you need to refresh Java array syntax and the operations that appear most often in coding interviews.

- Reference time: 25–35 minutes; review one section at a time
- Runnable examples: [ArrayOperationsDemo.java](../../../../src/test/java/com/instagram/backend/interview/leetcode/arrayshashmaps/refresher/ArrayOperationsDemo.java)

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
int[] zeros = new int[4];       // result: [0, 0, 0, 0]
int[] numbers = {5, 1, 5, 2};   // result: [5, 1, 5, 2]
String[] names = new String[3]; // result: [null, null, null]
int[][] grid = new int[2][3];   // result: 2 rows, each containing 3 zeros
```

`new int[4]` creates space for exactly four integers. Java initializes each position automatically. Numeric primitives start at `0`, `boolean` values start at `false`, and object references such as `String` start at `null`.

The length is fixed at creation. You can replace a value, but you cannot add a fifth position to `zeros`.

## Read, update, and get length

```java
int[] numbers = {5, 1, 5, 2};

int first = numbers[0];
// first is 5

numbers[1] = 9;
// numbers is now [5, 9, 5, 2]

int length = numbers.length;
// length is 4; arrays use the field .length, not .length() or .size()

int last = numbers[numbers.length - 1];
// last is 2 because numbers.length - 1 is index 3
```

An index identifies one position. For an array of length `4`, valid indexes are `0`, `1`, `2`, and `3`. Accessing index `4` or `-1` throws `ArrayIndexOutOfBoundsException`.

## Iterate

Use an indexed loop when the position matters or the array must be updated:

```java
int[] numbers = {5, 1, 5, 2};

for (int index = 0; index < numbers.length; index++) {
    int value = numbers[index];
    numbers[index] = value * 2;
}

// numbers is now [10, 2, 10, 4]
```

The loop visits indexes `0`, `1`, `2`, and `3`. Each assignment writes the doubled value back into the same position, so it modifies the original array.

Use an enhanced loop when only the value matters:

```java
int[] numbers = {5, 1, 5, 2};

for (int value : numbers) {
    System.out.println(value);
}

// output:
// 5
// 1
// 5
// 2
```

The enhanced loop gives you each value but not its index.

Assigning to the enhanced-loop variable does not update the array:

```java
int[] numbers = {5, 1, 5, 2};

for (int value : numbers) {
    value *= 2;
}

// numbers is still [5, 1, 5, 2]
```

`value` is a temporary local copy of the current integer. Reassigning it does not write back into the array.

Iterate a two-dimensional array:

```java
int[][] grid = {
        {1, 2, 3},
        {4, 5, 6}
};

for (int row = 0; row < grid.length; row++) {
    for (int column = 0; column < grid[row].length; column++) {
        System.out.print(grid[row][column] + " ");
    }
    System.out.println();
}

// output:
// 1 2 3
// 4 5 6
```

`grid.length` is the number of rows (`2`). `grid[row].length` is the number of columns in the current row (`3` here).

## Print and compare

```java
int[] numbers = {5, 1, 5, 2};
String text = Arrays.toString(numbers);
// text is "[5, 1, 5, 2]"

boolean same = Arrays.equals(new int[] {1, 2}, new int[] {1, 2});
// same is true because the arrays contain the same values in the same order

int[][] grid = {{1, 2}, {3, 4}};
String gridText = Arrays.deepToString(grid);
// gridText is "[[1, 2], [3, 4]]"

int[][] gridA = {{1, 2}, {3, 4}};
int[][] gridB = {{1, 2}, {3, 4}};
boolean sameGrid = Arrays.deepEquals(gridA, gridB);
// sameGrid is true
```

`numbers.toString()` does not show the elements. Use `Arrays.toString` for a one-dimensional array and `Arrays.deepToString` for nested arrays. Use `Arrays.equals` or `Arrays.deepEquals` to compare contents; `==` checks whether two variables refer to the exact same array object.

## Fill and generate

### Fill every position

```java
int[] values = new int[5];
// before: [0, 0, 0, 0, 0]

Arrays.fill(values, -1);
// after:  [-1, -1, -1, -1, -1]
```

`Arrays.fill(values, -1)` replaces every element in `values` with `-1`. It modifies the existing array and returns nothing.

### Fill only a range

```java
int[] values = {-1, -1, -1, -1, -1};
// indexes:       0   1   2   3   4

Arrays.fill(values, 1, 4, 7);
// after:      [-1,  7,  7,  7, -1]
```

The four arguments mean:

```text
Arrays.fill(array, fromIndexInclusive, toIndexExclusive, replacementValue)
Arrays.fill(values, 1,                  4,                7)
```

Java starts at index `1` and replaces indexes `1`, `2`, and `3` with `7`. Index `4` is excluded, so its value remains `-1`. This call also modifies the existing array and returns nothing.

### Generate each value from its index

```java
int[] squares = new int[5];
// before: [0, 0, 0, 0, 0]

Arrays.setAll(squares, index -> index * index);
// after:  [0, 1, 4, 9, 16]
```

`Arrays.setAll` calls the function once for every index and stores the returned value at that index. For index `3`, the function returns `3 * 3`, so position `3` becomes `9`. It modifies the existing array and returns nothing.

## Copy an array or range

```java
int[] original = {10, 20, 30, 40};

int[] clone = original.clone();
// clone is [10, 20, 30, 40]

int[] longer = Arrays.copyOf(original, 6);
// longer is [10, 20, 30, 40, 0, 0]

int[] middle = Arrays.copyOfRange(original, 1, 3);
// middle is [20, 30]; index 1 is included and index 3 is excluded

int[] destination = new int[2];
System.arraycopy(original, 1, destination, 0, 2);
// destination is [20, 30]

// original is still [10, 20, 30, 40]
```

`clone`, `copyOf`, and `copyOfRange` return new arrays. Changing one of those copies does not change this primitive `original` array.

`System.arraycopy(original, 1, destination, 0, 2)` means: start at index `1` in `original`, start at index `0` in `destination`, and copy `2` values. It modifies `destination` and returns nothing.

Assignment creates an alias, not a copy:

```java
int[] original = {10, 20, 30, 40};
int[] alias = original;

alias[0] = 99;
// alias is    [99, 20, 30, 40]
// original is [99, 20, 30, 40]
```

Both variables refer to the same array object, so a change through either variable is visible through both.

For a nested array, `clone` and `copyOf` copy only the outer array. The inner arrays remain shared.

## Sort and search

### Sort the entire array

```java
int[] values = {8, 3, 5, 1};
// before: [8, 3, 5, 1]

Arrays.sort(values);
// after:  [1, 3, 5, 8]
```

`Arrays.sort(values)` rearranges the existing array into ascending order and returns nothing.

### Sort only a range

```java
int[] values = {9, 4, 3, 8};
// indexes:      0  1  2  3

Arrays.sort(values, 1, 3);
// after:       [9, 3, 4, 8]
```

Only indexes `1` and `2` are sorted. The start index `1` is included, while the end index `3` is excluded. Positions outside the range remain unchanged.

### Search a sorted array

```java
int[] sorted = {1, 3, 5, 8};

int foundIndex = Arrays.binarySearch(sorted, 5);
// foundIndex is 2 because sorted[2] is 5

int missingResult = Arrays.binarySearch(sorted, 4);
// missingResult is -3
```

`binarySearch` does not modify the array. It requires sorted input. If a value exists, the method returns one matching index. If it is absent, it returns `-(insertion point) - 1`.

For `4`, the insertion point is index `2`, between `3` and `5`, so the result is `-2 - 1`, which is `-3`. In normal code, use `result >= 0` to check whether the value was found.

Sort object arrays with a comparator:

```java
String[] words = {"pear", "fig", "banana"};
Arrays.sort(words, Comparator.comparingInt(String::length));
// words is now ["fig", "pear", "banana"]

Integer[] boxedNumbers = {8, 3, 5, 1};
Arrays.sort(boxedNumbers, Comparator.reverseOrder());
// boxedNumbers is now [8, 5, 3, 1]
```

The first comparator sorts strings by length. The second sorts boxed integers in descending order. Primitive arrays such as `int[]` cannot use a comparator; box values as `Integer[]` only when comparator-based ordering is needed.

## Swap and reverse in place

```java
int[] values = {1, 2, 3, 4};
// before: [1, 2, 3, 4]

for (int left = 0, right = values.length - 1; left < right; left++, right--) {
    int temporary = values[left];
    values[left] = values[right];
    values[right] = temporary;
}

// after:  [4, 3, 2, 1]
```

The first iteration swaps indexes `0` and `3`. The second swaps indexes `1` and `2`. The pointers then meet, and the loop ends. This modifies the original array using `O(1)` additional space.

Java has no `Arrays.reverse` for primitive arrays, so this two-pointer swap is the usual in-place approach.

## Aggregate with streams

```java
int[] numbers = {3, 8, 2, 7};

int sum = Arrays.stream(numbers).sum();
// sum is 20

int maximum = Arrays.stream(numbers).max().orElseThrow();
// maximum is 8

long evenCount = Arrays.stream(numbers).filter(value -> value % 2 == 0).count();
// evenCount is 2 because 8 and 2 are even

int[] doubled = Arrays.stream(numbers).map(value -> value * 2).toArray();
// doubled is [6, 16, 4, 14]

// numbers is still [3, 8, 2, 7]
```

Streams read from `numbers`. The terminal operations return a result or a new array; they do not modify `numbers` here. In an interview, a normal loop is often easier to debug and explain, so use streams only when the transformation stays obvious.

## Convert between arrays and lists

```java
Integer[] boxed = {1, 2, 3};
List<Integer> fixedSize = Arrays.asList(boxed);
// fixedSize contains [1, 2, 3]

fixedSize.set(0, 9);
// fixedSize now contains [9, 2, 3]
// boxed is also [9, 2, 3] because the list is backed by the array
// fixedSize.add(4) throws UnsupportedOperationException

List<Integer> mutable = new ArrayList<>(List.of(1, 2, 3));
mutable.add(4);
// mutable now contains [1, 2, 3, 4]

int[] primitive = {1, 2, 3};
List<Integer> boxedList = Arrays.stream(primitive).boxed().toList();
// boxedList contains [1, 2, 3]
// boxedList is unmodifiable
```

`Arrays.asList` works as expected with an object array such as `Integer[]`. With a primitive `int[]`, `Arrays.asList(primitive)` produces a `List<int[]>` containing one array, not a `List<Integer>`; the stream example performs the required boxing.

Arrays cannot change length. Use `new ArrayList<>(...)` when insertion and removal are required.

## Common interview shapes

### Track the best value seen so far

```java
int[] numbers = {3, 8, 2, 7};
int maximum = numbers[0];

for (int value : numbers) {
    maximum = Math.max(maximum, value);
}

// maximum is 8
```

Start with a real array value instead of `0`; otherwise an all-negative array would incorrectly keep `0` as its maximum.

### Move two pointers toward each other

```java
int[] sorted = {1, 2, 4, 6};
int target = 8;

int left = 0;
int right = sorted.length - 1;

while (left < right) {
    int sum = sorted[left] + sorted[right];

    if (sum == target) {
        break;
    } else if (sum < target) {
        left++;
    } else {
        right--;
    }
}

// left is 1 and right is 3
// sorted[left] + sorted[right] is 2 + 6, which is 8
```

Because the array is sorted, a sum that is too small lets us discard the current left value; a sum that is too large lets us discard the current right value.

### Build prefix totals

```java
int[] numbers = {2, 4, 1};
int[] prefix = new int[numbers.length + 1];

for (int index = 0; index < numbers.length; index++) {
    prefix[index + 1] = prefix[index] + numbers[index];
}

// prefix is [0, 2, 6, 7]

int fromIndex = 1;
int toIndexExclusive = 3;
int rangeSum = prefix[toIndexExclusive] - prefix[fromIndex];
// rangeSum is 5 because numbers[1] + numbers[2] is 4 + 1
```

The leading zero makes the sum of the half-open range `[left, right)` equal to `prefix[right] - prefix[left]`.

## Does the operation modify the input?

| Operation | Modifies existing array? | Produces |
|---|---|---|
| `numbers[index] = value` | Yes | No return value |
| `Arrays.fill` | Yes | No return value |
| `Arrays.setAll` | Yes | No return value |
| `Arrays.sort` | Yes | No return value |
| `System.arraycopy` | Yes, the destination | No return value |
| In-place reverse loop | Yes | No return value |
| `clone` | No | New array |
| `Arrays.copyOf` / `copyOfRange` | No | New array |
| Stream `map(...).toArray()` | No | New array |
| Stream `sum`, `max`, or `count` | No | One calculated value |

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
  com.instagram.backend.interview.leetcode.arrayshashmaps.refresher.ArrayOperationsDemo
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
