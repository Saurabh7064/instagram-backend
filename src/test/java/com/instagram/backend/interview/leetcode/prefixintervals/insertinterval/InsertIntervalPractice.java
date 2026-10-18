package com.instagram.backend.interview.leetcode.prefixintervals.insertinterval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Directly runnable Module 04 practice; learning status is tracked separately. */
public class InsertIntervalPractice {

    public static void main(String[] args) {
        try {
            runChecks();
            System.out.println("All practice checks passed.");
        } catch (UnsupportedOperationException exception) {
            System.out.println("Practice ready: implement the TODO method(s), then rerun main.");
        }
    }

    static int[][] insert(int[][] intervals, int[] newInterval) {
        // TODO: Copy intervals before, merge overlaps, then copy intervals after.
        throw new UnsupportedOperationException("Implement insert");
    }

    private static void runChecks() {
        check(new int[][] {{1, 2}, {3, 10}, {12, 16}},
                insert(new int[][] {{1, 2}, {3, 5}, {6, 7}, {8, 10}, {12, 16}}, new int[] {4, 8}),
                "bridge several intervals");
        check(new int[][] {{1, 5}, {6, 9}}, insert(new int[][] {{1, 3}, {6, 9}}, new int[] {2, 5}), "one overlap");
        check(new int[][] {{2, 5}}, insert(new int[][] {}, new int[] {2, 5}), "empty input");
        check(new int[][] {{1, 2}, {5, 7}}, insert(new int[][] {{5, 7}}, new int[] {1, 2}), "before all");
        check(new int[][] {{1, 2}, {5, 7}}, insert(new int[][] {{1, 2}}, new int[] {5, 7}), "after all");
        check(new int[][] {{1, 5}}, insert(new int[][] {{1, 3}}, new int[] {3, 5}), "touching endpoints");
        check(new int[][] {{1, 9}}, insert(new int[][] {{1, 9}}, new int[] {3, 4}), "contained new interval");
        int[][] input = {{1, 2}, {5, 7}};
        int[] addition = {3, 4};
        int[][] output = insert(input, addition);
        output[0][0] = -99;
        output[1][0] = -88;
        check(new int[][] {{1, 2}, {5, 7}}, input, "input rows copied");
        if (!Arrays.equals(addition, new int[] {3, 4})) throw new AssertionError("new interval mutated");
    }

    private static void check(int[][] expected, int[][] actual, String scenario) {
        if (!Arrays.deepEquals(expected, actual)) throw new AssertionError(scenario + ": expected " + Arrays.deepToString(expected) + " but got " + Arrays.deepToString(actual));
    }
}
