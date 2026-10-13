package com.instagram.backend.interview.leetcode.slidingwindow.permutationinstring;

import java.util.Arrays;

/**
 * Runnable reference solution for Sliding Window Micro-Lesson 04.
 */
public class PermutationInStringSolution {

    public static void main(String[] args) {
        assertEquals(true, checkInclusion("ab", "eidbaooo"), "contains ba");
        assertEquals(false, checkInclusion("ab", "eidboaoo"), "no matching counts");
        assertEquals(true, checkInclusion("adc", "dcda"), "match at start");
        assertEquals(false, checkInclusion("abcd", "abc"), "pattern longer than text");
        assertEquals(true, checkInclusion("a", "a"), "single character");
        System.out.println("All reference-solution checks passed.");
    }

    static boolean checkInclusion(String pattern, String text) {
        if (pattern.length() > text.length()) {
            return false;
        }

        int[] neededCounts = new int[26];
        int[] windowCounts = new int[26];
        for (int index = 0; index < pattern.length(); index++) {
            neededCounts[pattern.charAt(index) - 'a']++;
        }

        for (int right = 0; right < text.length(); right++) {
            windowCounts[text.charAt(right) - 'a']++;

            if (right >= pattern.length()) {
                int outgoingIndex = right - pattern.length();
                windowCounts[text.charAt(outgoingIndex) - 'a']--;
            }

            if (Arrays.equals(neededCounts, windowCounts)) {
                return true;
            }
        }

        return false;
    }

    private static void assertEquals(boolean expected, boolean actual, String scenario) {
        if (expected != actual) {
            throw new AssertionError(scenario + ": expected " + expected + " but received " + actual);
        }
    }
}
