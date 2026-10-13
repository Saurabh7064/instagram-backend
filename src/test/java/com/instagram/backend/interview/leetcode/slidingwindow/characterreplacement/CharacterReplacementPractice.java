package com.instagram.backend.interview.leetcode.slidingwindow.characterreplacement;

/**
 * Editable practice program for Sliding Window Micro-Lesson 03.
 */
public class CharacterReplacementPractice {

    public static void main(String[] args) {
        try {
            runChecks();
            System.out.println("All practice checks passed.");
        } catch (UnsupportedOperationException exception) {
            System.out.println("Practice program is ready.");
            System.out.println("Implement characterReplacement, then run this main method again.");
        }
    }

    static int characterReplacement(String text, int replacementsAllowed) {
        // TODO: Maintain a window whose replacement cost does not exceed k.
        throw new UnsupportedOperationException("Implement characterReplacement");
    }

    private static void runChecks() {
        assertEquals(4, characterReplacement("ABAB", 2), "replace either pair");
        assertEquals(4, characterReplacement("AABABBA", 1), "standard example");
        assertEquals(4, characterReplacement("AAAA", 0), "already uniform");
        assertEquals(1, characterReplacement("ABCDE", 0), "no replacements");
        assertEquals(0, characterReplacement("", 3), "empty text");
    }

    private static void assertEquals(int expected, int actual, String scenario) {
        if (expected != actual) {
            throw new AssertionError(scenario + ": expected " + expected + " but received " + actual);
        }
    }
}
