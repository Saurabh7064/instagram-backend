package com.instagram.backend.interview.coding.arrayshashmaps.refresher;

import java.util.Arrays;

/**
 * Editable warm-up for the Array + HashMap refresher.
 */
public class TwoSumPractice {

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
        // TODO: Return the indexes of the two distinct values that add to target.
        throw new UnsupportedOperationException("Implement twoSum");
    }

    private static void runChecks() {
        assertArrayEquals(new int[] {0, 1}, twoSum(new int[] {2, 7, 11, 15}, 9),
                "pair at the beginning");
        assertArrayEquals(new int[] {1, 2}, twoSum(new int[] {3, 2, 4}, 6),
                "pair after an unused value");
        assertArrayEquals(new int[] {0, 1}, twoSum(new int[] {3, 3}, 6),
                "duplicate values at distinct indexes");
        assertArrayEquals(new int[] {0, 2}, twoSum(new int[] {-3, 4, 3, 90}, 0),
                "negative complement");
    }

    private static void assertArrayEquals(int[] expected, int[] actual, String scenario) {
        if (!Arrays.equals(expected, actual)) {
            throw new AssertionError(
                    scenario + ": expected " + Arrays.toString(expected)
                            + " but received " + Arrays.toString(actual));
        }
    }
}
