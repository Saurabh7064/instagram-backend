package com.instagram.backend.interview.coding.arrayshashmaps.frequencycounting;

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

    private static void assertEquals(boolean expected, boolean actual, String scenario) {
        if (expected != actual) {
            throw new AssertionError(
                    scenario + ": expected " + expected + " but received " + actual);
        }
    }
}
