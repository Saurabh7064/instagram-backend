package com.instagram.backend.interview.leetcode.twopointers.threesum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Editable practice program for Two Pointers Micro-Lesson 04.
 */
public class ThreeSumPractice {

    public static void main(String[] args) {
        try {
            runChecks();
            System.out.println("All practice checks passed.");
        } catch (UnsupportedOperationException exception) {
            System.out.println("Practice program is ready.");
            System.out.println("Implement threeSum, then run this main method again.");
        }
    }

    static List<List<Integer>> threeSum(int[] numbers) {
        // TODO: Sort a copy, choose an anchor, converge two pointers, and skip duplicates.
        throw new UnsupportedOperationException("Implement threeSum");
    }

    private static void runChecks() {
        assertTripletsEqual(
                List.of(List.of(-1, -1, 2), List.of(-1, 0, 1)),
                threeSum(new int[] {-1, 0, 1, 2, -1, -4}),
                "standard example");
        assertTripletsEqual(List.of(), threeSum(new int[] {0, 1, 1}), "no triplet");
        assertTripletsEqual(List.of(List.of(0, 0, 0)), threeSum(new int[] {0, 0, 0}),
                "one duplicate triplet");
        assertTripletsEqual(
                List.of(List.of(-2, 0, 2), List.of(-2, 1, 1)),
                threeSum(new int[] {-2, 0, 1, 1, 2}),
                "two unique triplets");
        assertTripletsEqual(List.of(), threeSum(new int[] {}), "empty input");
    }

    private static void assertTripletsEqual(
            List<List<Integer>> expected, List<List<Integer>> actual, String scenario) {
        List<String> normalizedExpected = normalize(expected);
        List<String> normalizedActual = normalize(actual);
        if (!normalizedExpected.equals(normalizedActual)) {
            throw new AssertionError(
                    scenario + ": expected " + normalizedExpected
                            + " but received " + normalizedActual);
        }
    }

    private static List<String> normalize(List<List<Integer>> triplets) {
        List<String> normalized = new ArrayList<>();
        for (List<Integer> triplet : triplets) {
            List<Integer> sorted = new ArrayList<>(triplet);
            Collections.sort(sorted);
            normalized.add(sorted.toString());
        }
        Collections.sort(normalized);
        return normalized;
    }
}
