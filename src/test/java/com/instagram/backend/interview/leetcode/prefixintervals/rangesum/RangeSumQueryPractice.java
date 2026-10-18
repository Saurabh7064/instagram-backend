package com.instagram.backend.interview.leetcode.prefixintervals.rangesum;


/** Directly runnable Module 04 practice; learning status is tracked separately. */
public class RangeSumQueryPractice {

    public static void main(String[] args) {
        try {
            runChecks();
            System.out.println("All practice checks passed.");
        } catch (UnsupportedOperationException exception) {
            System.out.println("Practice ready: implement the TODO method(s), then rerun main.");
        }
    }

    static final class NumArray {
        NumArray(int[] numbers) {
            // TODO: Save one cumulative total per boundary, starting with zero.
            throw new UnsupportedOperationException("Implement NumArray construction");
        }

        int sumRange(int left, int right) {
            // TODO: Remove the total before left from the total through right.
            throw new UnsupportedOperationException("Implement sumRange");
        }
    }

    private static void runChecks() {
        int[] input = {-2, 0, 3, -5, 2, -1, 4};
        NumArray data = new NumArray(input);
        check(1, data.sumRange(0, 2), "prefix query");
        check(-1, data.sumRange(2, 5), "middle query");
        check(1, data.sumRange(0, 6), "whole array");
        check(-5, data.sumRange(3, 3), "single negative value");
        check(4, data.sumRange(6, 6), "last value");
        check(7, new NumArray(new int[] {7}).sumRange(0, 0), "singleton");
        check(0, new NumArray(new int[] {0, 0}).sumRange(0, 1), "zero totals");
        input[0] = 99;
        check(1, data.sumRange(0, 2), "snapshot unaffected by caller mutation");
    }

    private static void check(int expected, int actual, String scenario) {
        if (expected != actual) throw new AssertionError(scenario + ": expected " + expected + " but got " + actual);
    }
}
