package com.instagram.backend.interview.leetcode.prefixintervals.productexceptself;

import java.util.Arrays;

/** Directly runnable Module 04 solution; learning status is tracked separately. */
public class ProductExceptSelfSolution {

    public static void main(String[] args) {
        runChecks();
        System.out.println("All reference-solution checks passed.");
    }

    static int[] productExceptSelf(int[] numbers) {
        int[] answer = new int[numbers.length];
        int prefixProduct = 1;
        for (int index = 0; index < numbers.length; index++) {
            answer[index] = prefixProduct;
            prefixProduct *= numbers[index];
        }

        int suffixProduct = 1;
        for (int index = numbers.length - 1; index >= 0; index--) {
            answer[index] *= suffixProduct;
            suffixProduct *= numbers[index];
        }
        return answer;
    }

    private static void runChecks() {
        check(new int[] {720, 360, 240, 180, 144, 120, 720},
                productExceptSelf(new int[] {1, 2, 3, 4, 5, 6, 1}), "seven-value trace");
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
