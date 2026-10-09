package com.instagram.backend.interview.coding.arrayshashmaps.canonicalgrouping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Editable practice program for Micro-Lesson 05: Grouping by a Canonical Key.
 */
public class GroupAnagramsPractice {

    public static void main(String[] args) {
        try {
            runChecks();
            System.out.println("All practice checks passed.");
        } catch (UnsupportedOperationException exception) {
            System.out.println("Practice program is ready.");
            System.out.println("Implement groupAnagrams, then run this main method again.");
        }
    }

    static List<List<String>> groupAnagrams(String[] words) {
        // TODO: Group words that share one canonical key.
        throw new UnsupportedOperationException("Implement groupAnagrams");
    }

    private static void runChecks() {
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
