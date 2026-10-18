package com.instagram.backend.interview.leetcode.prefixintervals.mergeintervals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/** Directly runnable Module 04 practice; learning status is tracked separately. */
public class MergeIntervalsPractice {

    public static void main(String[] args) {
        try {
            runChecks();
            System.out.println("All practice checks passed.");
        } catch (UnsupportedOperationException exception) {
            System.out.println("Practice ready: implement the TODO method(s), then rerun main.");
        }
    }

    static int[][] merge(int[][] intervals) {
        // TODO: Sort a deep copy, then keep one unfinished interval.
        throw new UnsupportedOperationException("Implement merge");
    }

    private static void runChecks() {
        check(new int[][] {{1, 6}, {8, 10}, {15, 18}},
                merge(new int[][] {{8, 10}, {1, 3}, {2, 6}, {15, 18}}), "unsorted trace");
        check(new int[][] {{1, 5}}, merge(new int[][] {{1, 4}, {4, 5}}), "touching endpoints");
        check(new int[][] {{1, 10}}, merge(new int[][] {{1, 10}, {2, 3}, {4, 5}}), "nested intervals");
        check(new int[][] {{2, 2}}, merge(new int[][] {{2, 2}, {2, 2}}), "duplicate points");
        check(new int[][] {{3, 7}}, merge(new int[][] {{3, 7}}), "single interval");
        check(new int[][] {}, merge(new int[][] {}), "defensive empty input");
        int[][] input = {{8, 10}, {1, 3}, {2, 6}};
        int[][] output = merge(input);
        output[0][0] = -99;
        check(new int[][] {{8, 10}, {1, 3}, {2, 6}}, input, "no input mutation or alias");
    }

    private static void check(int[][] expected, int[][] actual, String scenario) {
        if (!Arrays.deepEquals(expected, actual)) throw new AssertionError(scenario + ": expected " + Arrays.deepToString(expected) + " but got " + Arrays.deepToString(actual));
    }
}
