package com.instagram.backend.interview.leetcode.slidingwindow.permutationinstring;

/**
 * Editable practice program for Sliding Window Micro-Lesson 04.
 */
public class PermutationInStringPractice {

    public static void main(String[] args) {
        try {
            runChecks();
            System.out.println("All practice checks passed.");
        } catch (UnsupportedOperationException exception) {
            System.out.println("Practice program is ready.");
            System.out.println("Implement checkInclusion, then run this main method again.");
        }
    }

    static boolean checkInclusion(String pattern, String text) {
        // TODO: Compare the pattern counts with every same-length text window.
        throw new UnsupportedOperationException("Implement checkInclusion");
    }

    private static void runChecks() {
        assertEquals(true, checkInclusion("ab", "eidbaooo"), "contains ba");
        assertEquals(false, checkInclusion("ab", "eidboaoo"), "no matching counts");
        assertEquals(true, checkInclusion("adc", "dcda"), "match at start");
        assertEquals(false, checkInclusion("abcd", "abc"), "pattern longer than text");
        assertEquals(true, checkInclusion("a", "a"), "single character");
    }

    private static void assertEquals(boolean expected, boolean actual, String scenario) {
        if (expected != actual) {
            throw new AssertionError(scenario + ": expected " + expected + " but received " + actual);
        }
    }
}
