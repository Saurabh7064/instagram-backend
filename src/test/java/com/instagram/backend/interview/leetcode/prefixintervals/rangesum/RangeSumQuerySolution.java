package com.instagram.backend.interview.leetcode.prefixintervals.rangesum;


/** Directly runnable Module 04 solution; learning status is tracked separately. */
public class RangeSumQuerySolution {

    public static void main(String[] args) {
        runChecks();
        System.out.println("All reference-solution checks passed.");
    }

    static final class NumArray {
        private final int[] prefix;

        NumArray(int[] numbers) {
            prefix = new int[numbers.length + 1];
            for (int index = 0; index < numbers.length; index++) {
                prefix[index + 1] = prefix[index] + numbers[index];
            }
        }

        int sumRange(int left, int right) {
            return prefix[right + 1] - prefix[left];
        }
    }

    private static void runChecks() {
        int[] input = {2, 4, 1, 7};
        NumArray data = new NumArray(input);
        check(7, data.sumRange(0, 2), "worked prefix query");
        check(5, data.sumRange(1, 2), "worked middle query");
        check(14, data.sumRange(0, 3), "worked whole array");
        check(7, data.sumRange(3, 3), "worked last value");
        NumArray negativeValues = new NumArray(new int[] {-2, 0, 3, -5, 2, -1, 4});
        check(-1, negativeValues.sumRange(2, 5), "negative middle sum");
        check(-5, negativeValues.sumRange(3, 3), "single negative value");
        check(7, new NumArray(new int[] {7}).sumRange(0, 0), "singleton");
        check(0, new NumArray(new int[] {0, 0}).sumRange(0, 1), "zero totals");
        input[0] = 99;
        check(7, data.sumRange(0, 2), "snapshot unaffected by caller mutation");
    }

    private static void check(int expected, int actual, String scenario) {
        if (expected != actual) throw new AssertionError(scenario + ": expected " + expected + " but got " + actual);
    }
}
