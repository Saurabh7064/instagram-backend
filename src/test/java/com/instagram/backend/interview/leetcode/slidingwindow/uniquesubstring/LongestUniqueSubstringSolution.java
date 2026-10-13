package com.instagram.backend.interview.leetcode.slidingwindow.uniquesubstring;

import java.util.HashSet;
import java.util.Set;

/**
 * Runnable reference solution for Sliding Window Micro-Lesson 02.
 */
public class LongestUniqueSubstringSolution {

    public static void main(String[] args) {
        assertEquals(3, lengthOfLongestSubstring("abcabcbb"), "repeating groups");
        assertEquals(1, lengthOfLongestSubstring("bbbbb"), "all equal");
        assertEquals(3, lengthOfLongestSubstring("pwwkew"), "duplicate inside window");
        assertEquals(0, lengthOfLongestSubstring(""), "empty text");
        assertEquals(2, lengthOfLongestSubstring("abba"), "left boundary never moves backward");
        System.out.println("All reference-solution checks passed.");
    }

    static int lengthOfLongestSubstring(String text) {
        Set<Character> activeCharacters = new HashSet<>();
        int left = 0;
        int bestLength = 0;

        for (int right = 0; right < text.length(); right++) {
            char incoming = text.charAt(right);
            while (activeCharacters.contains(incoming)) {
                activeCharacters.remove(text.charAt(left));
                left++;
            }
            activeCharacters.add(incoming);
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
