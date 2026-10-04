package com.instagram.backend.interview.coding.arrayshashmaps.hashsetmembership;

/**
 * Editable program for Micro-Lesson 01: HashSet Membership.
 */
public class ContainsDuplicatePractice {

    public static void main(String[] args) {
        try {
            runChecks();
            System.out.println("All practice checks passed.");
        } catch (UnsupportedOperationException exception) {
            System.out.println("Practice program is ready.");
            System.out.println("Implement containsDuplicate, then run this main method again.");
        }
    }

    static boolean containsDuplicate(int[] values) {
        // TODO: Write your solution here.
        throw new UnsupportedOperationException("Implement containsDuplicate");
    }

    private static void runChecks() {
        assertEquals(true, containsDuplicate(new int[] {1, 2, 3, 1}),
                "duplicate present");
        assertEquals(false, containsDuplicate(new int[] {1, 2, 3, 4}),
                "all values distinct");
        assertEquals(false, containsDuplicate(new int[] {}),
                "empty input");
        assertEquals(false, containsDuplicate(new int[] {7}),
                "single value");
        assertEquals(true, containsDuplicate(new int[] {-3, 0, -3}),
                "duplicate negative value");
    }

    private static void assertEquals(boolean expected, boolean actual, String scenario) {
        if (expected != actual) {
            throw new AssertionError(
                    scenario + ": expected " + expected + " but received " + actual);
        }
    }
}
