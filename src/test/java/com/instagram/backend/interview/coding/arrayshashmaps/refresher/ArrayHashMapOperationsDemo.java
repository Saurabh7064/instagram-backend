package com.instagram.backend.interview.coding.arrayshashmaps.refresher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Runnable examples for the Java Arrays + HashMap cheat sheet.
 */
public class ArrayHashMapOperationsDemo {

    public static void main(String[] args) {
        demonstrateArrayOperations();
        demonstrateBasicMapOperations();
        demonstrateMapComputationOperations();
        demonstrateGroupingAndCounting();

        System.out.println("All Array and HashMap operation checks passed.");
    }

    private static void demonstrateArrayOperations() {
        int[] numbers = {5, 1, 5, 2};

        assertEquals(5, numbers[0], "read array value by index");
        numbers[1] = 9;
        assertArrayEquals(new int[] {5, 9, 5, 2}, numbers, "update array value");

        int[] copy = Arrays.copyOf(numbers, numbers.length);
        int[] middle = Arrays.copyOfRange(numbers, 1, 3);
        assertTrue(copy != numbers, "copy has a different identity");
        assertArrayEquals(numbers, copy, "copy has the same values");
        assertArrayEquals(new int[] {9, 5}, middle, "copy a half-open range");

        Arrays.fill(copy, 1, 3, 7);
        assertArrayEquals(new int[] {5, 7, 7, 2}, copy, "fill a range");

        Arrays.sort(numbers);
        assertArrayEquals(new int[] {2, 5, 5, 9}, numbers, "sort ascending");
        assertTrue(Arrays.binarySearch(numbers, 9) >= 0, "find a value in sorted input");
        assertTrue(Arrays.binarySearch(numbers, 8) < 0, "report a missing value");

        int[] squares = new int[5];
        Arrays.setAll(squares, index -> index * index);
        assertArrayEquals(new int[] {0, 1, 4, 9, 16}, squares, "generate by index");

        int sum = Arrays.stream(numbers).sum();
        assertEquals(21, sum, "sum array values");
    }

    private static void demonstrateBasicMapOperations() {
        Map<String, Integer> scores = new HashMap<>();

        assertEquals(null, scores.put("Ana", 10), "put a new key");
        assertEquals(10, scores.put("Ana", 15), "put returns the replaced value");
        assertEquals(15, scores.get("Ana"), "get an existing value");
        assertEquals(0, scores.getOrDefault("Missing", 0), "read with a default");

        scores.putIfAbsent("Ana", 99);
        scores.putIfAbsent("Ben", 20);
        assertEquals(15, scores.get("Ana"), "putIfAbsent keeps an existing value");
        assertEquals(20, scores.get("Ben"), "putIfAbsent stores a missing value");

        scores.replace("Ana", 25);
        assertEquals(25, scores.get("Ana"), "replace an existing value");
        assertTrue(scores.remove("Ben", 20), "remove an exact key-value pair");
        assertTrue(!scores.containsKey("Ben"), "removed key is absent");

        Map<String, Integer> copy = new HashMap<>(scores);
        assertEquals(scores, copy, "copy a map");
    }

    private static void demonstrateMapComputationOperations() {
        Map<String, Integer> counts = new HashMap<>();

        counts.computeIfAbsent("apple", key -> key.length());
        counts.computeIfAbsent("apple", key -> 999);
        assertEquals(5, counts.get("apple"), "computeIfAbsent runs only when missing");

        counts.computeIfPresent("apple", (key, oldValue) -> oldValue + 1);
        counts.computeIfPresent("missing", (key, oldValue) -> oldValue + 1);
        assertEquals(6, counts.get("apple"), "computeIfPresent updates existing value");
        assertTrue(!counts.containsKey("missing"), "computeIfPresent skips missing key");

        counts.compute("banana", (key, oldValue) -> oldValue == null ? 1 : oldValue + 1);
        counts.compute("banana", (key, oldValue) -> oldValue == null ? 1 : oldValue + 1);
        assertEquals(2, counts.get("banana"), "compute handles absent and present values");

        counts.merge("pear", 1, Integer::sum);
        counts.merge("pear", 1, Integer::sum);
        assertEquals(2, counts.get("pear"), "merge inserts then combines values");
    }

    private static void demonstrateGroupingAndCounting() {
        List<String> words = List.of("apple", "ant", "boat", "apple");
        Map<Character, List<String>> wordsByFirstLetter = new HashMap<>();
        Map<String, Integer> frequencies = new HashMap<>();

        for (String word : words) {
            wordsByFirstLetter
                    .computeIfAbsent(word.charAt(0), key -> new ArrayList<>())
                    .add(word);
            frequencies.merge(word, 1, Integer::sum);
        }

        assertEquals(List.of("apple", "ant", "apple"), wordsByFirstLetter.get('a'),
                "group values with computeIfAbsent");
        assertEquals(List.of("boat"), wordsByFirstLetter.get('b'),
                "create another group");
        assertEquals(2, frequencies.get("apple"), "count duplicates with merge");
        assertEquals(1, frequencies.get("ant"), "count one occurrence");
    }

    private static void assertArrayEquals(int[] expected, int[] actual, String scenario) {
        if (!Arrays.equals(expected, actual)) {
            throw new AssertionError(
                    scenario + ": expected " + Arrays.toString(expected)
                            + " but received " + Arrays.toString(actual));
        }
    }

    private static void assertEquals(Object expected, Object actual, String scenario) {
        if (!java.util.Objects.equals(expected, actual)) {
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
