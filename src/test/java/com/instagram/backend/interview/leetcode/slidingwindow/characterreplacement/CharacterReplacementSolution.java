package com.instagram.backend.interview.leetcode.slidingwindow.characterreplacement;

/**
 * Runnable reference solution for Sliding Window Micro-Lesson 03.
 */
public class CharacterReplacementSolution {

    public static void main(String[] args) {
        assertEquals(4, characterReplacement("ABAB", 2), "replace either pair");
        assertEquals(4, characterReplacement("AABABBA", 1), "standard example");
        assertEquals(4, characterReplacement("AAAA", 0), "already uniform");
        assertEquals(1, characterReplacement("ABCDE", 0), "no replacements");
        assertEquals(0, characterReplacement("", 3), "empty text");
        System.out.println("All reference-solution checks passed.");
    }

    static int characterReplacement(String text, int replacementsAllowed) {
        int[] frequency = new int[26];
        int left = 0;
        int bestLength = 0;
        int maxFrequency = 0;

        for (int right = 0; right < text.length(); right++) {
            int incomingIndex = text.charAt(right) - 'A';
            frequency[incomingIndex]++;
            maxFrequency = Math.max(maxFrequency, frequency[incomingIndex]);

            int replacementsNeeded = right - left + 1 - maxFrequency;
            if (replacementsNeeded > replacementsAllowed) {
                int outgoingIndex = text.charAt(left) - 'A';
                frequency[outgoingIndex]--;
                left++;
            }

            bestLength = Math.max(bestLength, right - left + 1);
        }

        return bestLength;
    }

    private static void assertEquals(int expected, int actual, String scenario) {
        if (expected != actual) {
            throw new AssertionError(scenario + ": expected " + expected + " but received " + actual);
        }
    }
}
