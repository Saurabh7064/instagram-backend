package com.instagram.backend.interview.leetcode.slidingwindow.minimumwindow;

import java.util.HashMap;
import java.util.Map;

/**
 * Runnable reference solution for Sliding Window Micro-Lesson 05.
 */
public class MinimumWindowSubstringSolution {

    public static void main(String[] args) {
        assertEquals("BANC", minWindow("ADOBECODEBANC", "ABC"), "standard example");
        assertEquals("a", minWindow("a", "a"), "exact match");
        assertEquals("", minWindow("a", "aa"), "insufficient count");
        assertEquals("aa", minWindow("aa", "aa"), "duplicate requirement");
        assertEquals("ba", minWindow("bba", "ab"), "shortest suffix");
        System.out.println("All reference-solution checks passed.");
    }

    static String minWindow(String source, String target) {
        Map<Character, Integer> neededCounts = new HashMap<>();
        for (char value : target.toCharArray()) {
            neededCounts.merge(value, 1, Integer::sum);
        }

        Map<Character, Integer> windowCounts = new HashMap<>();
        int requiredGroups = neededCounts.size();
        int formedGroups = 0;
        int left = 0;
        int bestStart = 0;
        int bestLength = Integer.MAX_VALUE;

        for (int right = 0; right < source.length(); right++) {
            char incoming = source.charAt(right);
            windowCounts.merge(incoming, 1, Integer::sum);
            if (neededCounts.containsKey(incoming)
                    && windowCounts.get(incoming).equals(neededCounts.get(incoming))) {
                formedGroups++;
            }

            while (formedGroups == requiredGroups) {
                int candidateLength = right - left + 1;
                if (candidateLength < bestLength) {
                    bestStart = left;
                    bestLength = candidateLength;
                }

                char outgoing = source.charAt(left);
                windowCounts.merge(outgoing, -1, Integer::sum);
                left++;
                if (neededCounts.containsKey(outgoing)
                        && windowCounts.get(outgoing) < neededCounts.get(outgoing)) {
                    formedGroups--;
                }
            }
        }

        if (bestLength == Integer.MAX_VALUE) {
            return "";
        }
        return source.substring(bestStart, bestStart + bestLength);
    }

    private static void assertEquals(String expected, String actual, String scenario) {
        if (!expected.equals(actual)) {
            throw new AssertionError(scenario + ": expected " + expected + " but received " + actual);
        }
    }
}
