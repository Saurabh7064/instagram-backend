package com.instagram.backend.interview.leetcode.twopointers.validpalindrome;

/**
 * Editable practice program for Two Pointers Micro-Lesson 01.
 */
public class ValidPalindromePractice {

    public static void main(String[] args) {
        try {
            runChecks();
            System.out.println("All practice checks passed.");
        } catch (UnsupportedOperationException exception) {
            System.out.println("Practice program is ready.");
            System.out.println("Implement isPalindrome, then run this main method again.");
        }
    }

    static boolean isPalindrome(String text) {
        // TODO: Compare normalized characters by moving two pointers inward.
        throw new UnsupportedOperationException("Implement isPalindrome");
    }

    private static void runChecks() {
        assertEquals(true, isPalindrome("A man, a plan, a canal: Panama"),
                "phrase with punctuation");
        assertEquals(false, isPalindrome("race a car"), "mismatch");
        assertEquals(true, isPalindrome(" "), "no alphanumeric characters");
        assertEquals(false, isPalindrome("0P"), "digit and letter mismatch");
        assertEquals(true, isPalindrome("ab_a"), "ignored separator");
    }

    private static void assertEquals(boolean expected, boolean actual, String scenario) {
        if (expected != actual) {
            throw new AssertionError(
                    scenario + ": expected " + expected + " but received " + actual);
        }
    }
}
