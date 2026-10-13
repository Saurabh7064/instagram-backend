package com.instagram.backend.interview.leetcode.slidingwindow.uniquesubstring;

/**
 * Editable practice program for Sliding Window Micro-Lesson 02.
 */
public class LongestUniqueSubstringPractice {

    public static void main(String[] args) {
        try {
            runChecks();
            System.out.println("All practice checks passed.");
        } catch (UnsupportedOperationException exception) {
            System.out.println("Practice program is ready.");
            System.out.println("Implement lengthOfLongestSubstring, then run this main method again.");
        }
    }

    static int lengthOfLongestSubstring(String text) {
        // TODO: Expand right and shrink left until the active window is unique.
        throw new UnsupportedOperationException("Implement lengthOfLongestSubstring");
    }

    private static void runChecks() {
        assertEquals(3, lengthOfLongestSubstring("abcabcbb"), "repeating groups");
        assertEquals(1, lengthOfLongestSubstring("bbbbb"), "all equal");
        assertEquals(3, lengthOfLongestSubstring("pwwkew"), "duplicate inside window");
        assertEquals(0, lengthOfLongestSubstring(""), "empty text");
        assertEquals(2, lengthOfLongestSubstring("abba"), "left boundary never moves backward");
    }

    private static void assertEquals(int expected, int actual, String scenario) {
        if (expected != actual) {
            throw new AssertionError(scenario + ": expected " + expected + " but received " + actual);
        }
    }
}
