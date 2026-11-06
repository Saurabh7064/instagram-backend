package com.instagram.backend.interview.leetcode.stackqueue.windowmaximum;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

/** Directly runnable Module 05 practice; comprehension is tracked separately. */
public class SlidingWindowMaximumPractice {
    public static void main(String[] args) {
        try {
            runChecks();
            System.out.println("All practice checks passed.");
        } catch (UnsupportedOperationException exception) {
            System.out.println("Practice ready: implement the TODO method, then rerun main.");
        }
    }

    // nums index: 0 1  2  3 4 5 6 7
    // nums      : 1 3 -1 -3 5 3 6 7
    // Input: nums=[1,3,-1,-3,5,3,6,7]; k=3
    // Expected return: [3,3,5,5,6,7]
    public static int[] maxSlidingWindow(int[] nums, int k) {
        // TODO: Write your solution here.
        throw new UnsupportedOperationException("Implement maxSlidingWindow");
    }

    private static void runChecks() {
        check(new int[] {3,3,5,5,6,7}, maxSlidingWindow(new int[] {1,3,-1,-3,5,3,6,7}, 3), "worked input");
        check(new int[] {7}, maxSlidingWindow(new int[] {7}, 1), "singleton");
        check(new int[] {2,-1,4}, maxSlidingWindow(new int[] {2,-1,4}, 1), "width one");
        check(new int[] {4}, maxSlidingWindow(new int[] {2,-1,4}, 3), "whole input");
        check(new int[] {5,4,3}, maxSlidingWindow(new int[] {5,4,3,2}, 2), "expired front");
        check(new int[] {2,2,2}, maxSlidingWindow(new int[] {2,2,2,2}, 2), "equal candidates");
        check(new int[] {-1,-2}, maxSlidingWindow(new int[] {-1,-2,-3}, 2), "all negative");
        check(new int[] {Integer.MAX_VALUE}, maxSlidingWindow(new int[] {Integer.MIN_VALUE,Integer.MAX_VALUE}, 2), "full int comparison");
        int[] input = {1,3,-1,-3,5,3,6,7};
        int[] before = input.clone();
        int[] output = maxSlidingWindow(input, 3);
        check(before, input, "input unchanged");
        output[0] = -99;
        check(before, input, "output does not alias input");
    }

    private static void check(int[] expected, int[] actual, String scenario) {
        if (!Arrays.equals(expected, actual)) {
            throw new AssertionError(scenario + ": expected " + Arrays.toString(expected)
                    + " but got " + Arrays.toString(actual));
        }
    }
}
