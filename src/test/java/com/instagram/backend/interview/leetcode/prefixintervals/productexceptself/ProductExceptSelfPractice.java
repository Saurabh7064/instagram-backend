package com.instagram.backend.interview.leetcode.prefixintervals.productexceptself;

import java.util.Arrays;

/** Directly runnable Module 04 practice; learning status is tracked separately. */
public class ProductExceptSelfPractice {

    public static void main(String[] args) {
        try {
            runChecks();
            System.out.println("All practice checks passed.");
        } catch (UnsupportedOperationException exception) {
            System.out.println("Practice ready: implement the TODO method(s), then rerun main.");
        }
    }

    static int[] productExceptSelf(int[] numbers) {
        // TODO: Save the product strictly left, then multiply by the product strictly right.
        throw new UnsupportedOperationException("Implement productExceptSelf");
    }

    private static void runChecks() {
        check(new int[] {720, 360, 240, 180, 144, 120},
                productExceptSelf(new int[] {1, 2, 3, 4, 5, 6}), "six-value trace");
        check(new int[] {24, 12, 8, 6}, productExceptSelf(new int[] {1, 2, 3, 4}), "ordinary input");
        check(new int[] {0, 0, 9, 0, 0}, productExceptSelf(new int[] {-1, 1, 0, -3, 3}), "one zero");
        check(new int[] {0, 0, 0}, productExceptSelf(new int[] {0, 2, 0}), "two zeros");
        check(new int[] {3, -2}, productExceptSelf(new int[] {-2, 3}), "minimum length");
        check(new int[] {}, productExceptSelf(new int[] {}), "defensive empty input");
        int[] original = {2, 3, 4};
        productExceptSelf(original);
        check(new int[] {2, 3, 4}, original, "input preserved");
    }

    private static void check(int[] expected, int[] actual, String scenario) {
        if (!Arrays.equals(expected, actual)) throw new AssertionError(scenario + ": expected " + Arrays.toString(expected) + " but got " + Arrays.toString(actual));
    }
}
