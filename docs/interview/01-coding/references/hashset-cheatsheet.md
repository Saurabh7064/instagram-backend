# Java HashSet Cheat Sheet

Use this page when you need to refresh common `HashSet` operations and the membership patterns that appear in coding interviews.

- Reference time: 25–35 minutes; review one section at a time
- Runnable examples: [HashSetOperationsDemo.java](../../../../src/test/java/com/instagram/backend/interview/coding/arrayshashmaps/refresher/HashSetOperationsDemo.java)
- Focused practice: [HashSet Membership](../lessons/01-arrays-hash-maps/01-hashset-membership/README.md)

## Mental model

A `HashSet<E>` stores unique elements. It answers one main question efficiently:

> Is this value already present?

A set stores values without a separate associated value:

```text
HashSet: {7, 12, 20}
         membership only

HashMap: 7 -> index 3
         key plus associated information
```

Choose a `HashSet` when you need membership, uniqueness, duplicate detection, visited-state tracking, or mathematical set operations. Choose a `HashMap` when each key needs an index, count, list, or other associated value.

## Common imports

```java
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
```

Program to the `Set` interface on the left and choose `HashSet` as the implementation on the right:

```java
Set<Integer> values = new HashSet<>();
```

## Create and copy

```java
Set<Integer> empty = new HashSet<>();
// empty contains no values

Set<Integer> values = new HashSet<>(List.of(3, 1, 3, 2));
// values contains 1, 2, and 3; the repeated 3 is stored once

Set<Integer> mutableCopy = new HashSet<>(values);
mutableCopy.add(4);
// mutableCopy contains 1, 2, 3, and 4
// values still contains only 1, 2, and 3

Set<Integer> immutable = Set.of(1, 2, 3);
// immutable.add(4) throws UnsupportedOperationException
```

`Set.of` creates an unmodifiable set. It rejects `null` and throws `IllegalArgumentException` if the arguments contain duplicates.

## Add values

```java
Set<Integer> values = new HashSet<>();

boolean firstAdd = values.add(10);
// firstAdd is true; values contains 10

boolean duplicateAdd = values.add(10);
// duplicateAdd is false; values still contains one 10

values.add(20);
// values contains 10 and 20
```

`add` returns `true` only when the set changed. This makes duplicate detection concise:

```java
if (!values.add(number)) {
    System.out.println("number was already present");
}
```

Unlike a list, a set does not have positions, so there is no `add(index, value)` or `get(index)`.

## Read membership and size

```java
Set<String> roles = new HashSet<>(List.of("ADMIN", "EDITOR"));

boolean isAdmin = roles.contains("ADMIN");
// isAdmin is true

boolean isViewer = roles.contains("VIEWER");
// isViewer is false

int size = roles.size();
// size is 2

boolean empty = roles.isEmpty();
// empty is false
```

`contains` does not change the set. Average lookup time is `O(1)` when hash codes are well distributed.

## Remove and clear

```java
Set<Integer> values = new HashSet<>(List.of(10, 20, 30));

boolean removed = values.remove(20);
// removed is true; values contains 10 and 30

boolean removedMissing = values.remove(99);
// removedMissing is false; values is unchanged

values.clear();
// values is now empty; clear returns nothing
```

`remove` returns whether an element was actually present and removed.

## Add several values

```java
Set<Integer> values = new HashSet<>(List.of(1, 2));

boolean changed = values.addAll(List.of(2, 3, 4));
// changed is true
// values contains 1, 2, 3, and 4

boolean changedAgain = values.addAll(List.of(3, 4));
// changedAgain is false because both values were already present
```

`addAll` performs a union-like update on the existing set and returns whether anything changed.

## Check whether every required value exists

```java
Set<String> permissions = new HashSet<>(List.of("READ", "WRITE", "DELETE"));

boolean canEdit = permissions.containsAll(List.of("READ", "WRITE"));
// canEdit is true

boolean canPublish = permissions.containsAll(List.of("READ", "PUBLISH"));
// canPublish is false because PUBLISH is absent
```

`containsAll` does not modify the set.

## Union, intersection, and difference

Start with two sets:

```java
Set<Integer> left = Set.of(1, 2, 3);
Set<Integer> right = Set.of(3, 4);
```

### Union: values in either set

```java
Set<Integer> union = new HashSet<>(left);
union.addAll(right);

// union contains 1, 2, 3, and 4
// left and right are unchanged
```

### Intersection: values in both sets

```java
Set<Integer> intersection = new HashSet<>(left);
intersection.retainAll(right);

// intersection contains only 3
// left and right are unchanged
```

### Difference: values in the first set but not the second

```java
Set<Integer> difference = new HashSet<>(left);
difference.removeAll(right);

// difference contains 1 and 2
// left and right are unchanged
```

`addAll`, `retainAll`, and `removeAll` modify the set on which they are called. Create a copy first when the original must remain unchanged.

## Remove values matching a condition

```java
Set<Integer> values = new HashSet<>(List.of(1, 2, 3, 4, 5, 6));

boolean changed = values.removeIf(value -> value % 2 == 0);

// changed is true
// values contains 1, 3, and 5
```

`removeIf` tests every element and removes those for which the condition returns `true`.

## Iterate

```java
Set<String> names = new HashSet<>(List.of("Ana", "Ben", "Cara"));

for (String name : names) {
    System.out.println(name);
}
```

All three names print once, but `HashSet` does not guarantee their order. Do not write logic that depends on its iteration order.

Use a different implementation when order is part of the requirement:

```java
Set<String> insertionOrder = new LinkedHashSet<>();
insertionOrder.add("Cara");
insertionOrder.add("Ana");
insertionOrder.add("Ben");
// iteration order: Cara, Ana, Ben

Set<String> sorted = new TreeSet<>(List.of("Cara", "Ana", "Ben"));
// iteration order: Ana, Ben, Cara
```

- `LinkedHashSet` preserves insertion order with average `O(1)` membership.
- `TreeSet` keeps values sorted with `O(log n)` membership and updates.

## Convert between a list, array, and set

### Remove duplicates from a list

```java
List<Integer> numbers = List.of(3, 1, 3, 2, 1);
Set<Integer> unique = new HashSet<>(numbers);

// unique contains 1, 2, and 3
// iteration order is unspecified
```

### Remove duplicates while preserving first-seen order

```java
List<Integer> numbers = List.of(3, 1, 3, 2, 1);
List<Integer> deduplicated = new ArrayList<>(new LinkedHashSet<>(numbers));

// deduplicated is [3, 1, 2]
```

### Convert a set to a sorted list

```java
Set<Integer> unique = new HashSet<>(List.of(3, 1, 2));
List<Integer> sorted = unique.stream().sorted().toList();

// sorted is [1, 2, 3]
```

### Convert an array to a set

```java
Integer[] numbers = {3, 1, 3, 2};
Set<Integer> unique = new HashSet<>(List.of(numbers));

// unique contains 1, 2, and 3
```

For a primitive `int[]`, loop or stream the values because `List.of(intArray)` treats the whole array as one object.

## Set equality

```java
Set<Integer> first = Set.of(1, 2, 3);
Set<Integer> second = Set.of(3, 2, 1);

boolean equal = first.equals(second);
// equal is true
```

Set equality depends on membership, not insertion or iteration order.

## Custom objects and `equals`/`hashCode`

`HashSet` uses `hashCode` to find a bucket and `equals` to confirm a match. Two logically equal objects must produce the same hash code.

A Java record provides suitable value-based methods automatically:

```java
record UserId(long value) {}

Set<UserId> ids = new HashSet<>();
ids.add(new UserId(42));

boolean present = ids.contains(new UserId(42));
// present is true because the two records are equal by value
```

Do not mutate fields used by `equals` or `hashCode` while an object is inside a `HashSet`; the object may become impossible to find in its original bucket.

## Common interview patterns

### Detect a duplicate

```java
int[] numbers = {4, 7, 2, 7};
Set<Integer> seen = new HashSet<>();
boolean duplicateFound = false;

for (int number : numbers) {
    if (!seen.add(number)) {
        duplicateFound = true;
        break;
    }
}

// duplicateFound is true when the second 7 is processed
// seen contains 4, 7, and 2
```

### Count distinct values

```java
int[] numbers = {4, 7, 4, 2, 7};
Set<Integer> unique = new HashSet<>();

for (int number : numbers) {
    unique.add(number);
}

int distinctCount = unique.size();
// distinctCount is 3 because the distinct values are 4, 7, and 2
```

### Find common values

```java
int[] first = {1, 2, 2, 3};
int[] second = {2, 2, 4};

Set<Integer> firstValues = new HashSet<>();
for (int value : first) {
    firstValues.add(value);
}

Set<Integer> common = new HashSet<>();
for (int value : second) {
    if (firstValues.contains(value)) {
        common.add(value);
    }
}

// common contains only 2
```

### Track visited graph nodes

```java
Set<Integer> visited = new HashSet<>();

if (visited.add(nodeId)) {
    // first visit: process the node and add its neighbors
} else {
    // already visited: skip it to avoid repeated work or a cycle
}
```

## Does the operation modify the set?

| Operation | Modifies the set? | Result |
|---|---|---|
| `add(value)` | Yes, when absent | `true` if changed |
| `contains(value)` | No | membership boolean |
| `remove(value)` | Yes, when present | `true` if changed |
| `addAll(values)` | Yes | `true` if changed |
| `retainAll(values)` | Yes | `true` if changed |
| `removeAll(values)` | Yes | `true` if changed |
| `removeIf(predicate)` | Yes | `true` if changed |
| `containsAll(values)` | No | containment boolean |
| `size()` / `isEmpty()` | No | size or boolean |
| `new HashSet<>(existing)` | No | new mutable set |

## Complexity

| Operation | Expected time |
|---|---:|
| `add`, `contains`, `remove` | Average `O(1)` |
| Iterate all values | `O(n)` |
| Copy a set | `O(n)` |
| Union/intersection/difference | Proportional to the sets examined |

HashSet operations are described as average `O(1)` because collisions and adversarial hash behavior can make individual operations more expensive.

## Common mistakes

- Expecting a stable iteration order from `HashSet`.
- Trying to retrieve a value by index.
- Using a set when a count or original index is required.
- Ignoring the boolean returned by `add` when detecting duplicates.
- Calling `retainAll` or `removeAll` on an original set that should remain unchanged.
- Mutating a custom object's equality/hash fields after insertion.
- Assuming converting a list to `HashSet` preserves first-seen order.
- Passing duplicates or `null` to `Set.of`.

## Focused warm-up: Contains Duplicate

Given a non-null integer array, return `true` when any value occurs at least twice and `false` when every value is distinct.

- Edit: [ContainsDuplicatePractice.java](../../../../src/test/java/com/instagram/backend/interview/coding/arrayshashmaps/hashsetmembership/ContainsDuplicatePractice.java)
- Reveal after attempting: [ContainsDuplicateSolution.java](../../../../src/test/java/com/instagram/backend/interview/coding/arrayshashmaps/hashsetmembership/ContainsDuplicateSolution.java)

Invariant: before processing the current number, `seen` contains every distinct number from earlier array positions. If `seen.add(number)` returns `false`, the current number must have appeared earlier.

- Average time: `O(n)`.
- Additional space: `O(n)`.

## Compile and run

```bash
./gradlew testClasses

java -cp build/classes/java/test \
  com.instagram.backend.interview.coding.arrayshashmaps.refresher.HashSetOperationsDemo

java -cp build/classes/java/test \
  com.instagram.backend.interview.coding.arrayshashmaps.hashsetmembership.ContainsDuplicatePractice

java -cp build/classes/java/test \
  com.instagram.backend.interview.coding.arrayshashmaps.hashsetmembership.ContainsDuplicateSolution
```

## Checkpoints

<details>
<summary>1. Why does `add` return `false` for a duplicate?</summary>

A set stores each equal value once. Adding an already present value does not change the set, so `add` returns `false`.

</details>

<details>
<summary>2. How do union, intersection, and difference map to Java methods?</summary>

On a mutable copy, use `addAll` for union, `retainAll` for intersection, and `removeAll` for difference.

</details>

<details>
<summary>3. When should you use a HashMap instead of a HashSet?</summary>

Use a `HashMap` when each key needs associated information such as a count, index, object, or collection. Use a `HashSet` when membership alone is sufficient.

</details>

<details>
<summary>4. Why can mutating a set key make it unreachable?</summary>

If fields used by `hashCode` change, lookup searches a different bucket from the one used during insertion. The set can still contain the object internally but fail to find it normally.

</details>

## Stop/go

Return to the active lesson only when you can:

- predict the return values of `add`, `contains`, and `remove`;
- compute union, intersection, and difference without looking;
- choose between `HashSet`, `LinkedHashSet`, `TreeSet`, and `HashMap`;
- implement Contains Duplicate without viewing the reference;
- answer all checkpoints before revealing their answers.
