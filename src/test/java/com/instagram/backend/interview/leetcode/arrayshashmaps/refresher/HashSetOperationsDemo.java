package com.instagram.backend.interview.leetcode.arrayshashmaps.refresher;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * Runnable examples for the Java HashSet cheat sheet.
 */
public class HashSetOperationsDemo {

    public static void main(String[] args) {
        demonstrateAddContainsAndRemove();
        demonstrateBulkOperations();
        demonstrateConversionsAndOrdering();
        demonstrateInterviewPatterns();

        System.out.println("All HashSet operation checks passed.");
    }

    private static void demonstrateAddContainsAndRemove() {
        Set<Integer> values = new HashSet<>();

        assertTrue(values.add(10), "first add changes the set");
        assertTrue(!values.add(10), "duplicate add does not change the set");
        assertTrue(values.add(20), "different value changes the set");
        assertTrue(values.contains(10), "contains existing value");
        assertTrue(!values.contains(99), "does not contain missing value");
        assertEquals(2, values.size(), "duplicates are stored once");

        assertTrue(values.remove(20), "remove existing value");
        assertTrue(!values.remove(99), "remove missing value");
        assertSetEquals(Set.of(10), values, "remaining value");

        System.out.println("add 10, add 10 again, add 20 -> " + sorted(valuesWith(10, 20)));
        System.out.println("duplicate add returned false; each value is stored once");
    }

    private static void demonstrateBulkOperations() {
        Set<Integer> left = Set.of(1, 2, 3);
        Set<Integer> right = Set.of(3, 4);

        Set<Integer> union = new HashSet<>(left);
        union.addAll(right);
        assertSetEquals(Set.of(1, 2, 3, 4), union, "union");

        Set<Integer> intersection = new HashSet<>(left);
        intersection.retainAll(right);
        assertSetEquals(Set.of(3), intersection, "intersection");

        Set<Integer> difference = new HashSet<>(left);
        difference.removeAll(right);
        assertSetEquals(Set.of(1, 2), difference, "difference");

        Set<Integer> odd = new HashSet<>(List.of(1, 2, 3, 4, 5, 6));
        odd.removeIf(value -> value % 2 == 0);
        assertSetEquals(Set.of(1, 3, 5), odd, "remove even values");

        System.out.println("union of [1, 2, 3] and [3, 4] -> " + sorted(union));
        System.out.println("intersection -> " + sorted(intersection));
        System.out.println("difference left - right -> " + sorted(difference));
        System.out.println("removeIf(even) -> " + sorted(odd));
    }

    private static void demonstrateConversionsAndOrdering() {
        List<Integer> numbers = List.of(3, 1, 3, 2, 1);
        Set<Integer> unique = new HashSet<>(numbers);
        assertSetEquals(Set.of(1, 2, 3), unique, "remove duplicates");

        List<Integer> firstSeenOrder = new ArrayList<>(new LinkedHashSet<>(numbers));
        assertEquals(List.of(3, 1, 2), firstSeenOrder,
                "remove duplicates while preserving first-seen order");

        Set<Integer> sortedSet = new TreeSet<>(unique);
        assertEquals(List.of(1, 2, 3), new ArrayList<>(sortedSet), "sorted set order");

        System.out.println("HashSet removes duplicates -> " + sorted(unique));
        System.out.println("LinkedHashSet preserves first-seen order -> " + firstSeenOrder);
        System.out.println("TreeSet sorts values -> " + sortedSet);
    }

    private static void demonstrateInterviewPatterns() {
        int[] numbers = {4, 7, 2, 7};
        Set<Integer> seen = new HashSet<>();
        boolean duplicateFound = false;

        for (int number : numbers) {
            if (!seen.add(number)) {
                duplicateFound = true;
                break;
            }
        }

        assertTrue(duplicateFound, "find repeated 7");
        assertSetEquals(Set.of(4, 7, 2), seen, "values seen before stopping");

        Set<Integer> first = Set.of(1, 2, 3);
        Set<Integer> second = Set.of(3, 2, 1);
        assertEquals(first, second, "set equality ignores order");

        System.out.println("contains duplicate in [4, 7, 2, 7] -> true");
        System.out.println("Set.of(1, 2, 3).equals(Set.of(3, 2, 1)) -> true");
    }

    private static Set<Integer> valuesWith(int... numbers) {
        Set<Integer> values = new HashSet<>();
        for (int number : numbers) {
            values.add(number);
        }
        return values;
    }

    private static List<Integer> sorted(Set<Integer> values) {
        return values.stream().sorted().toList();
    }

    private static void assertSetEquals(
            Set<Integer> expected,
            Set<Integer> actual,
            String scenario) {
        assertEquals(expected, actual, scenario);
    }

    private static void assertEquals(Object expected, Object actual, String scenario) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(
                    scenario + ": expected " + expected + " but received " + actual);
        }
    }

    private static void assertTrue(boolean condition, String scenario) {
        if (!condition) {
            throw new AssertionError(scenario);
        }
    }
}
