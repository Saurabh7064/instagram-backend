package com.instagram.backend.interview.leetcode.twopointers.containermostwater;

/**
 * Editable practice program for Two Pointers Micro-Lesson 03.
 */
public class ContainerMostWaterPractice {

    public static void main(String[] args) {
        try {
            runChecks();
            System.out.println("All practice checks passed.");
        } catch (UnsupportedOperationException exception) {
            System.out.println("Practice program is ready.");
            System.out.println("Implement maxArea, then run this main method again.");
        }
    }

    static int maxArea(int[] heights) {
        // TODO: Evaluate both boundaries and discard the dominated one.
        throw new UnsupportedOperationException("Implement maxArea");
    }

    private static void runChecks() {
        assertEquals(49, maxArea(new int[] {1, 8, 6, 2, 5, 4, 8, 3, 7}),
                "standard example");
        assertEquals(1, maxArea(new int[] {1, 1}), "minimum input");
        assertEquals(16, maxArea(new int[] {4, 3, 2, 1, 4}), "equal tall boundaries");
        assertEquals(2, maxArea(new int[] {1, 2, 1}), "middle taller");
        assertEquals(0, maxArea(new int[] {0, 2}), "zero limiting height");
    }

    private static void assertEquals(int expected, int actual, String scenario) {
        if (expected != actual) {
            throw new AssertionError(
                    scenario + ": expected " + expected + " but received " + actual);
        }
    }
}
