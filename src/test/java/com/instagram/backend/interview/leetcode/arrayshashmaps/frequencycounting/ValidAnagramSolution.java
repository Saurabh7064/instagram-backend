package com.instagram.backend.interview.leetcode.arrayshashmaps.frequencycounting;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Runnable reference solution for Micro-Lesson 04: Frequency Counting.
 */
public class ValidAnagramSolution {

    public static void main(String[] args) {
        assertEquals(true, isAnagram("anagram", "nagaram"), "ordinary anagram");
        assertEquals(false, isAnagram("rat", "car"), "different characters");
        assertEquals(false, isAnagram("aacc", "ccac"), "different frequencies");
        assertEquals(true, isAnagram("", ""), "empty strings");
        assertEquals(true, isAnagram("a", "a"), "one character");
        assertEquals(false, isAnagram("ab", "a"), "different lengths");

        assertEquals(true, isAnagramBySort("anagram", "nagaram"), "sorted anagram variant");
        assertEquals(false, isAnagramBySort("rat", "car"), "sorted different characters");
        assertEquals(true, isAnagramBySort("", ""), "sorted empty strings");

        System.out.println("All reference-solution checks passed.");
    }

    static boolean isAnagram(String first, String second) {
        if (first.length() != second.length()) {
            return false;
        }

        Map<Character, Integer> remainingByCharacter = new HashMap<>();
        for (char character : first.toCharArray()) {
            int currentCount = remainingByCharacter.getOrDefault(character, 0);
            remainingByCharacter.put(character, currentCount + 1);
        }

        for (char character : second.toCharArray()) {
            Integer currentCount = remainingByCharacter.get(character);
            if (currentCount == null) {
                return false;
            }

            if (currentCount == 1) {
                remainingByCharacter.remove(character);
            } else {
                remainingByCharacter.put(character, currentCount - 1);
            }
        }

        return remainingByCharacter.isEmpty();
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

    private static void assertEquals(boolean expected, boolean actual, String scenario) {
        if (expected != actual) {
            throw new AssertionError(
                    scenario + ": expected " + expected + " but received " + actual);
        }
    }
}
