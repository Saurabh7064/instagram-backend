package com.instagram.backend.interview.leetcode.twopointers.containermostwater;

/**
 * Runnable reference solution for Two Pointers Micro-Lesson 03.
 */
public class ContainerMostWaterSolution {

    public static void main(String[] args) {
        assertEquals(49, maxArea(new int[] {1, 8, 6, 2, 5, 4, 8, 3, 7}),
                "standard example");
        assertEquals(1, maxArea(new int[] {1, 1}), "minimum input");
        assertEquals(16, maxArea(new int[] {4, 3, 2, 1, 4}), "equal tall boundaries");
        assertEquals(2, maxArea(new int[] {1, 2, 1}), "middle taller");
        assertEquals(0, maxArea(new int[] {0, 2}), "zero limiting height");

        System.out.println("All reference-solution checks passed.");
    }

    static int maxArea(int[] heights) {
        int left = 0;
        int right = heights.length - 1;
        int bestArea = 0;

        while (left < right) {
            int width = right - left;
            int limitingHeight = Math.min(heights[left], heights[right]);
            bestArea = Math.max(bestArea, width * limitingHeight);

            if (heights[left] <= heights[right]) {
                left++;
            } else {
                right--;
            }
        }

        return bestArea;
    }

    private static void assertEquals(int expected, int actual, String scenario) {
        if (expected != actual) {
            throw new AssertionError(
                    scenario + ": expected " + expected + " but received " + actual);
        }
    }
}
