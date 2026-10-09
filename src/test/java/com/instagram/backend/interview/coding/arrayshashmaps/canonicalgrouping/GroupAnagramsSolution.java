package com.instagram.backend.interview.coding.arrayshashmaps.canonicalgrouping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Runnable reference solution for Micro-Lesson 05: Grouping by a Canonical Key.
 */
public class GroupAnagramsSolution {

    public static void main(String[] args) {
        assertGroupsEqual(
                List.of(List.of("eat", "tea", "ate"), List.of("tan", "nat"), List.of("bat")),
                groupAnagrams(new String[] {"eat", "tea", "tan", "ate", "nat", "bat"}),
                "ordinary groups");
        assertGroupsEqual(List.of(List.of("")), groupAnagrams(new String[] {""}),
                "empty word");
        assertGroupsEqual(List.of(List.of("a")), groupAnagrams(new String[] {"a"}),
                "single word");
        assertGroupsEqual(
                List.of(List.of("ab", "ba"), List.of("abc", "cab", "bca"), List.of("xy")),
                groupAnagrams(new String[] {"ab", "ba", "abc", "cab", "bca", "xy"}),
                "several group sizes");
        assertGroupsEqual(List.of(), groupAnagrams(new String[] {}), "empty input");

        System.out.println("All reference-solution checks passed.");
    }

    static List<List<String>> groupAnagrams(String[] words) {
        Map<String, List<String>> wordsByKey = new HashMap<>();

        for (String word : words) {
            char[] characters = word.toCharArray();
            Arrays.sort(characters);
            String key = new String(characters);

            wordsByKey.computeIfAbsent(key, ignored -> new ArrayList<>())
                    .add(word);
        }

        return new ArrayList<>(wordsByKey.values());
    }

    private static void assertGroupsEqual(
            List<List<String>> expected, List<List<String>> actual, String scenario) {
        List<String> normalizedExpected = normalize(expected);
        List<String> normalizedActual = normalize(actual);
        if (!normalizedExpected.equals(normalizedActual)) {
            throw new AssertionError(
                    scenario + ": expected " + normalizedExpected
                            + " but received " + normalizedActual);
        }
    }

    private static List<String> normalize(List<List<String>> groups) {
        List<String> normalized = new ArrayList<>();
        for (List<String> group : groups) {
            List<String> sortedGroup = new ArrayList<>(group);
            Collections.sort(sortedGroup);
            normalized.add(String.join(",", sortedGroup));
        }
        Collections.sort(normalized);
        return normalized;
    }
}
