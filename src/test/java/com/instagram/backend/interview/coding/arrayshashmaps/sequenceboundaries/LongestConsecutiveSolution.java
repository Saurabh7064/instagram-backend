package com.instagram.backend.interview.coding.arrayshashmaps.sequenceboundaries;

import java.util.HashSet;
import java.util.Set;

/**
 * Runnable reference solution for Lesson 01: Arrays and Hash Maps.
 */
public class LongestConsecutiveSolution {

    public static void main(String[] args) {
        assertEquals(4, longestConsecutive(new int[] {100, 4, 200, 1, 3, 2}),
                "unordered run");
        assertEquals(3, longestConsecutive(new int[] {1, 2, 0, 1}),
                "duplicates do not extend a run");
        assertEquals(0, longestConsecutive(new int[] {}),
                "empty input");
        assertEquals(1, longestConsecutive(new int[] {7}),
                "single value");
        assertEquals(3, longestConsecutive(new int[] {-1, -3, -2, 10}),
                "negative values");
        assertEquals(2,
                longestConsecutive(new int[] {Integer.MAX_VALUE - 1, Integer.MAX_VALUE}),
                "upper integer boundary");
        assertEquals(2,
                longestConsecutive(new int[] {Integer.MIN_VALUE, Integer.MIN_VALUE + 1}),
                "lower integer boundary");
        assertEquals(1,
                longestConsecutive(new int[] {Integer.MIN_VALUE, Integer.MAX_VALUE}),
                "integer extremes are not adjacent");

        System.out.println("All reference-solution checks passed.");
    }

    static int longestConsecutive(int[] values) {
        Set<Integer> unique = new HashSet<>();
        for (int value : values) {
            unique.add(value);
        }

        int longest = 0;

        for (int value : unique) {
            boolean hasPredecessor = value != Integer.MIN_VALUE
                    && unique.contains(value - 1);

            if (!hasPredecessor) {
                int length = 1;
                int current = value;

                while (current != Integer.MAX_VALUE
                        && unique.contains(current + 1)) {
                    current++;
                    length++;
                }

                longest = Math.max(longest, length);
            }
        }

        return longest;
    }

    private static void assertEquals(int expected, int actual, String scenario) {
        if (expected != actual) {
            throw new AssertionError(
                    scenario + ": expected " + expected + " but received " + actual);
        }
    }
}
