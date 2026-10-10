package com.instagram.backend.interview.leetcode.twopointers.twosumii;

import java.util.Arrays;

/**
 * Editable practice program for Two Pointers Micro-Lesson 02.
 */
public class TwoSumSortedPractice {

    public static void main(String[] args) {
        try {
            runChecks();
            System.out.println("All practice checks passed.");
        } catch (UnsupportedOperationException exception) {
            System.out.println("Practice program is ready.");
            System.out.println("Implement twoSum, then run this main method again.");
        }
    }

    static int[] twoSum(int[] numbers, int target) {
        // TODO: Converge from both ends and return one-based indexes.
        throw new UnsupportedOperationException("Implement twoSum");
    }

    private static void runChecks() {
        assertArrayEquals(new int[] {1, 2}, twoSum(new int[] {2, 7, 11, 15}, 9),
                "pair at beginning");
        assertArrayEquals(new int[] {1, 3}, twoSum(new int[] {2, 3, 4}, 6),
                "outer pair");
        assertArrayEquals(new int[] {1, 2}, twoSum(new int[] {-1, 0}, -1),
                "negative target");
        assertArrayEquals(new int[] {2, 4}, twoSum(new int[] {-5, -2, 0, 3, 9}, 1),
                "mixed values");
    }

    private static void assertArrayEquals(int[] expected, int[] actual, String scenario) {
        if (!Arrays.equals(expected, actual)) {
            throw new AssertionError(
                    scenario + ": expected " + Arrays.toString(expected)
                            + " but received " + Arrays.toString(actual));
        }
    }
}
