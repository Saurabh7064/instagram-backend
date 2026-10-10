package com.instagram.backend.interview.leetcode.arrayshashmaps.frequencycounting;

import java.util.Arrays;

/**
 * Editable practice program for Micro-Lesson 04: Frequency Counting.
 */
public class ValidAnagramPractice {

    public static void main(String[] args) {
        try {
            runChecks();
            System.out.println("All practice checks passed.");
        } catch (UnsupportedOperationException exception) {
            System.out.println("Practice program is ready.");
            System.out.println("Implement isAnagram, then run this main method again.");
        }
    }

    static boolean isAnagram(String first, String second) {
        // TODO: Compare exact character frequencies.
        // Target: average O(n) time and O(n) additional space.
        throw new UnsupportedOperationException("Implement isAnagram");
    }

    static boolean isAnagramBySort(String first, String second) {
        if (first.length() != second.length()) {
            return false;
        }

        char[] firstCharacters = first.toCharArray();
        char[] secondCharacters = second.toCharArray();

        Arrays.sort(firstCharacters);
        Arrays.sort(secondCharacters);

        return Arrays.equals(firstCharacters, secondCharacters);
    }

    private static void runChecks() {
        assertEquals(true, isAnagram("anagram", "nagaram"), "ordinary anagram");
        assertEquals(false, isAnagram("rat", "car"), "different characters");
        assertEquals(false, isAnagram("aacc", "ccac"), "different frequencies");
        assertEquals(true, isAnagram("", ""), "empty strings");
        assertEquals(true, isAnagram("a", "a"), "one character");
        assertEquals(false, isAnagram("ab", "a"), "different lengths");
    }

    private static void assertEquals(boolean expected, boolean actual, String scenario) {
        if (expected != actual) {
            throw new AssertionError(
                    scenario + ": expected " + expected + " but received " + actual);
        }
    }
}
