package com.instagram.backend.interview.leetcode.twopointers.twosumii;

import java.util.Arrays;

/**
 * Runnable reference solution for Two Pointers Micro-Lesson 02.
 */
public class TwoSumSortedSolution {

    public static void main(String[] args) {
        assertArrayEquals(new int[] {1, 2}, twoSum(new int[] {2, 7, 11, 15}, 9),
                "pair at beginning");
        assertArrayEquals(new int[] {1, 3}, twoSum(new int[] {2, 3, 4}, 6),
                "outer pair");
        assertArrayEquals(new int[] {1, 2}, twoSum(new int[] {-1, 0}, -1),
                "negative target");
        assertArrayEquals(new int[] {2, 4}, twoSum(new int[] {-5, -2, 0, 3, 9}, 1),
                "mixed values");

        System.out.println("All reference-solution checks passed.");
    }

    static int[] twoSum(int[] numbers, int target) {
        int left = 0;
        int right = numbers.length - 1;

        while (left < right) {
            int sum = numbers[left] + numbers[right];
            if (sum == target) {
                return new int[] {left + 1, right + 1};
            }
            if (sum < target) {
                left++;
            } else {
                right--;
            }
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
