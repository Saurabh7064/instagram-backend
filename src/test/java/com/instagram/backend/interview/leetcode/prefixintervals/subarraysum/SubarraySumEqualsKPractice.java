package com.instagram.backend.interview.leetcode.prefixintervals.subarraysum;

import java.util.HashMap;
import java.util.Map;

/** Directly runnable Module 04 practice; learning status is tracked separately. */
public class SubarraySumEqualsKPractice {

    public static void main(String[] args) {
        try {
            runChecks();
            System.out.println("All practice checks passed.");
        } catch (UnsupportedOperationException exception) {
            System.out.println("Practice ready: implement the TODO method(s), then rerun main.");
        }
    }

    static int subarraySum(int[] numbers, int target) {
        // TODO: Count earlier prefix totals before recording the current total.
        throw new UnsupportedOperationException("Implement subarraySum");
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
