package com.instagram.backend.interview.leetcode.arrayshashmaps.refresher;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Reference answer for the Array + HashMap refresher.
 */
public class TwoSumSolution {

    public static void main(String[] args) {
        assertArrayEquals(new int[] {0, 1}, twoSum(new int[] {2, 7, 11, 15}, 9),
                "pair at the beginning");
        assertArrayEquals(new int[] {1, 2}, twoSum(new int[] {3, 2, 4}, 6),
                "pair after an unused value");
        assertArrayEquals(new int[] {0, 1}, twoSum(new int[] {3, 3}, 6),
                "duplicate values at distinct indexes");
        assertArrayEquals(new int[] {0, 2}, twoSum(new int[] {-3, 4, 3, 90}, 0),
                "negative complement");

        System.out.println("All reference-solution checks passed.");
    }

    static int[] twoSum(int[] numbers, int target) {
        Map<Integer, Integer> indexByValue = new HashMap<>();

        for (int index = 0; index < numbers.length; index++) {
            int needed = target - numbers[index];
            Integer earlierIndex = indexByValue.get(needed);

            if (earlierIndex != null) {
                return new int[] {earlierIndex, index};
            }

            indexByValue.put(numbers[index], index);
        }

        throw new IllegalArgumentException("Expected exactly one valid pair");
    }

    private static void assertArrayEquals(int[] expected, int[] actual, String scenario) {
        if (!Arrays.equals(expected, actual)) {
            throw new AssertionError(
                    scenario + ": expected " + Arrays.toString(expected)
                            + " but received " + Arrays.toString(actual));
        }
    }
}
