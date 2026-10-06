package com.instagram.backend.interview.coding.arrayshashmaps.refresher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Runnable examples for the Java HashMap cheat sheet.
 */
public class HashMapOperationsDemo {

    public static void main(String[] args) {
        demonstrateBasicOperations();
        demonstrateComputationOperations();
        demonstrateGroupingAndCounting();

        System.out.println("All HashMap operation checks passed.");
    }

    private static void demonstrateBasicOperations() {
        Map<String, Integer> scores = new HashMap<>();

        assertEquals(null, scores.put("Ana", 10), "put a new key");
        assertEquals(10, scores.put("Ana", 15), "put returns replaced value");
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

        Map<String, Integer> copy = new HashMap<>();
        copy.putAll(scores);
        assertEquals(scores, copy, "copy all entries");
    }

    private static void demonstrateComputationOperations() {
        Map<String, Integer> counts = new HashMap<>();

        counts.computeIfAbsent("apple", key -> key.length());
        counts.computeIfAbsent("apple", key -> 999);
        assertEquals(5, counts.get("apple"), "computeIfAbsent runs when missing");

        counts.computeIfPresent("apple", (key, oldValue) -> oldValue + 1);
        counts.computeIfPresent("missing", (key, oldValue) -> oldValue + 1);
        assertEquals(6, counts.get("apple"), "computeIfPresent updates existing value");
        assertTrue(!counts.containsKey("missing"), "computeIfPresent skips missing key");

        counts.compute("banana", (key, oldValue) -> oldValue == null ? 1 : oldValue + 1);
        counts.compute("banana", (key, oldValue) -> oldValue == null ? 1 : oldValue + 1);
        assertEquals(2, counts.get("banana"), "compute handles absent and present");

        counts.merge("pear", 1, Integer::sum);
        counts.merge("pear", 1, Integer::sum);
        assertEquals(2, counts.get("pear"), "merge inserts then combines");
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
