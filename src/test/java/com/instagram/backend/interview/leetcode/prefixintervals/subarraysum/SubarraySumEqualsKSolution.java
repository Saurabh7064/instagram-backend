package com.instagram.backend.interview.leetcode.prefixintervals.subarraysum;

import java.util.HashMap;
import java.util.Map;

/** Directly runnable Module 04 solution; learning status is tracked separately. */
public class SubarraySumEqualsKSolution {

    public static void main(String[] args) {
        runChecks();
        System.out.println("All reference-solution checks passed.");
    }

    static int subarraySum(int[] numbers, int target) {
        Map<Integer, Integer> countByPrefix = new HashMap<>();
        countByPrefix.put(0, 1);
        int prefixSum = 0;
        int answer = 0;

        for (int index = 0; index < numbers.length; index++) {
            prefixSum += numbers[index];
            int needed = prefixSum - target;
            answer += countByPrefix.getOrDefault(needed, 0);
            countByPrefix.merge(prefixSum, 1, Integer::sum);
        }
        return answer;
    }

    private static void runChecks() {
        check(6, subarraySum(new int[] {1, -1, 1, 1, -1, 1, 2}, 2), "mixed-sign trace");
        check(2, subarraySum(new int[] {1, 1, 1}, 2), "overlapping answers");
        check(2, subarraySum(new int[] {1, 2, 3}, 3), "two different lengths");
        check(6, subarraySum(new int[] {0, 0, 0}, 0), "repeated prefix totals");
        check(0, subarraySum(new int[] {1}, 0), "do not count empty ranges");
        check(1, subarraySum(new int[] {-1}, -1), "negative target");
        check(0, subarraySum(new int[] {}, 3), "defensive empty input");
    }

    private static void check(int expected, int actual, String scenario) {
        if (expected != actual) throw new AssertionError(scenario + ": expected " + expected + " but got " + actual);
    }
}
