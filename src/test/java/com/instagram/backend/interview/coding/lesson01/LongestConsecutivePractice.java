package com.instagram.backend.interview.coding.lesson01;

import java.util.HashSet;
import java.util.Set;

/**
 * Editable practice program for Lesson 01: Arrays and Hash Maps.
 *
 * Write your implementation only inside longestConsecutive. Keep the main
 * method unchanged initially so the same examples test every attempt.
 */
public class LongestConsecutivePractice {

    public static void main(String[] args) {
        try {
            runChecks();
            System.out.println("All practice checks passed.");
        } catch (UnsupportedOperationException exception) {
            System.out.println("Practice program is ready.");
            System.out.println("Implement longestConsecutive, then run this main method again.");
        }
    }

    static int longestConsecutive(int[] values) {
        // TODO: Write your solution here.
        // Target: O(n) average time and O(n) additional space.
        throw new UnsupportedOperationException("Implement longestConsecutive");
    }

    private static void runChecks() {
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
    }

    private static void assertEquals(int expected, int actual, String scenario) {
        if (expected != actual) {
            throw new AssertionError(
                    scenario + ": expected " + expected + " but received " + actual);
        }
    }
}
