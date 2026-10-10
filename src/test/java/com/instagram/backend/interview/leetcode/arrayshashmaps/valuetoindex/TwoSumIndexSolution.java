package com.instagram.backend.interview.leetcode.arrayshashmaps.valuetoindex;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Runnable reference solution for Micro-Lesson 03: HashMap Value-to-Index Lookup.
 */
public class TwoSumIndexSolution {

    public static void main(String[] args) {
        assertArrayEquals(new int[] {0, 1}, twoSum(new int[] {2, 7, 11, 15}, 9),
                "pair at the beginning");
        assertArrayEquals(new int[] {1, 2}, twoSum(new int[] {3, 2, 4}, 6),
                "pair after an unused value");
        assertArrayEquals(new int[] {0, 1}, twoSum(new int[] {3, 3}, 6),
                "duplicate values at distinct indexes");
        assertArrayEquals(new int[] {0, 2}, twoSum(new int[] {-3, 4, 3, 90}, 0),
                "negative complement");
        assertArrayEquals(new int[] {0, 3}, twoSum(new int[] {0, 4, 3, 0}, 0),
                "zero pair at distinct indexes");

        System.out.println("All reference-solution checks passed.");
    }

    static int[] twoSum(int[] numbers, int target) {
        Map<Integer, Integer> earlierIndexByValue = new HashMap<>();

        for (int index = 0; index < numbers.length; index++) {
            int complement = target - numbers[index];
            Integer earlierIndex = earlierIndexByValue.get(complement);

            if (earlierIndex != null) {
                return new int[] {earlierIndex, index};
            }

            earlierIndexByValue.put(numbers[index], index);
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
