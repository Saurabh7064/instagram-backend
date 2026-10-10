package com.instagram.backend.interview.leetcode.slidingwindow.fixedaverage;

/**
 * Runnable reference solution for Sliding Window Micro-Lesson 01.
 */
public class MaximumAverageSubarraySolution {

    public static void main(String[] args) {
        assertEquals(12.75, findMaxAverage(new int[] {1, 12, -5, -6, 50, 3}, 4),
                "standard example");
        assertEquals(5.0, findMaxAverage(new int[] {5}, 1), "single value");
        assertEquals(-5.5, findMaxAverage(new int[] {-1, -12, -5, -6, -50, -3}, 2),
                "all negative");
        assertEquals(4.0, findMaxAverage(new int[] {0, 4, 0, 3, 2}, 1),
                "window size one");
        assertEquals(2.0, findMaxAverage(new int[] {2, 2, 2, 2}, 3),
                "equal windows");

        System.out.println("All reference-solution checks passed.");
    }

    static double findMaxAverage(int[] numbers, int windowSize) {
        long windowSum = 0;
        for (int index = 0; index < windowSize; index++) {
            windowSum += numbers[index];
        }

        long bestSum = windowSum;
        for (int right = windowSize; right < numbers.length; right++) {
            int outgoingIndex = right - windowSize;
            windowSum += numbers[right];
            windowSum -= numbers[outgoingIndex];
            bestSum = Math.max(bestSum, windowSum);
        }

        return bestSum / (double) windowSize;
    }

    private static void assertEquals(double expected, double actual, String scenario) {
        if (Math.abs(expected - actual) > 0.000000001) {
            throw new AssertionError(
                    scenario + ": expected " + expected + " but received " + actual);
        }
    }
}
