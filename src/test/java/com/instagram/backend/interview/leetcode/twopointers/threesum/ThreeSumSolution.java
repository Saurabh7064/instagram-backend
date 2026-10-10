package com.instagram.backend.interview.leetcode.twopointers.threesum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Runnable reference solution for Two Pointers Micro-Lesson 04.
 */
public class ThreeSumSolution {

    public static void main(String[] args) {
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

        System.out.println("All reference-solution checks passed.");
    }

    static List<List<Integer>> threeSum(int[] numbers) {
        int[] sorted = Arrays.copyOf(numbers, numbers.length);
        Arrays.sort(sorted);
        List<List<Integer>> result = new ArrayList<>();

        for (int anchor = 0; anchor < sorted.length - 2; anchor++) {
            if (anchor > 0 && sorted[anchor] == sorted[anchor - 1]) {
                continue;
            }

            int left = anchor + 1;
            int right = sorted.length - 1;
            while (left < right) {
                int sum = sorted[anchor] + sorted[left] + sorted[right];
                if (sum == 0) {
                    result.add(List.of(sorted[anchor], sorted[left], sorted[right]));
                    left++;
                    right--;

                    while (left < right && sorted[left] == sorted[left - 1]) {
                        left++;
                    }
                    while (left < right && sorted[right] == sorted[right + 1]) {
                        right--;
                    }
                } else if (sum < 0) {
                    left++;
                } else {
                    right--;
                }
            }
        }

        return result;
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
