package com.instagram.backend.interview.leetcode.slidingwindow.fixedaverage;

/**
 * Editable practice program for Sliding Window Micro-Lesson 01.
 */
public class MaximumAverageSubarrayPractice {

    public static void main(String[] args) {
        try {
            runChecks();
            System.out.println("All practice checks passed.");
        } catch (UnsupportedOperationException exception) {
            System.out.println("Practice program is ready.");
            System.out.println("Implement findMaxAverage, then run this main method again.");
        }
    }

    static double findMaxAverage(int[] numbers, int windowSize) {
        // TODO: Maintain one fixed-size rolling sum.
        throw new UnsupportedOperationException("Implement findMaxAverage");
    }

    private static void runChecks() {
        assertEquals(12.75, findMaxAverage(new int[] {1, 12, -5, -6, 50, 3}, 4),
                "standard example");
        assertEquals(5.0, findMaxAverage(new int[] {5}, 1), "single value");
        assertEquals(-5.5, findMaxAverage(new int[] {-1, -12, -5, -6, -50, -3}, 2),
                "all negative");
        assertEquals(4.0, findMaxAverage(new int[] {0, 4, 0, 3, 2}, 1),
                "window size one");
        assertEquals(2.0, findMaxAverage(new int[] {2, 2, 2, 2}, 3),
                "equal windows");
    }

    private static void assertEquals(double expected, double actual, String scenario) {
        if (Math.abs(expected - actual) > 0.000000001) {
            throw new AssertionError(
                    scenario + ": expected " + expected + " but received " + actual);
        }
    }
}
