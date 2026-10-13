package com.instagram.backend.interview.leetcode.slidingwindow.minimumwindow;

/**
 * Editable practice program for Sliding Window Micro-Lesson 05.
 */
public class MinimumWindowSubstringPractice {

    public static void main(String[] args) {
        try {
            runChecks();
            System.out.println("All practice checks passed.");
        } catch (UnsupportedOperationException exception) {
            System.out.println("Practice program is ready.");
            System.out.println("Implement minWindow, then run this main method again.");
        }
    }

    static String minWindow(String source, String target) {
        // TODO: Expand until all target counts are met, then shrink while valid.
        throw new UnsupportedOperationException("Implement minWindow");
    }

    private static void runChecks() {
        assertEquals("BANC", minWindow("ADOBECODEBANC", "ABC"), "standard example");
        assertEquals("a", minWindow("a", "a"), "exact match");
        assertEquals("", minWindow("a", "aa"), "insufficient count");
        assertEquals("aa", minWindow("aa", "aa"), "duplicate requirement");
        assertEquals("ba", minWindow("bba", "ab"), "shortest suffix");
    }

    private static void assertEquals(String expected, String actual, String scenario) {
        if (!expected.equals(actual)) {
            throw new AssertionError(scenario + ": expected " + expected + " but received " + actual);
        }
    }
}
