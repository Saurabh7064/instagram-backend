package com.instagram.backend.interview.coding.arrayshashmaps.hashsetmembership;

import java.util.HashSet;
import java.util.Set;

/**
 * Reference program for Micro-Lesson 01: HashSet Membership.
 */
public class ContainsDuplicateSolution {

    public static void main(String[] args) {
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

        System.out.println("All reference-solution checks passed.");
    }

    static boolean containsDuplicate(int[] values) {
        Set<Integer> seen = new HashSet<>();

        for (int value : values) {
            if (!seen.add(value)) {
                return true;
            }
        }

        return false;
    }

    private static void assertEquals(boolean expected, boolean actual, String scenario) {
        if (expected != actual) {
            throw new AssertionError(
                    scenario + ": expected " + expected + " but received " + actual);
        }
    }
}
