package com.instagram.backend.interview.leetcode.prefixintervals.mergeintervals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/** Directly runnable Module 04 solution; learning status is tracked separately. */
public class MergeIntervalsSolution {

    public static void main(String[] args) {
        runChecks();
        System.out.println("All reference-solution checks passed.");
    }

    static int[][] merge(int[][] intervals) {
        if (intervals.length == 0) {
            return new int[0][2];
        }
        int[][] sorted = new int[intervals.length][2];
        for (int index = 0; index < intervals.length; index++) {
            sorted[index] = intervals[index].clone();
        }
        Arrays.sort(sorted, Comparator.comparingInt(interval -> interval[0]));
        List<int[]> merged = new ArrayList<>();
        int start = sorted[0][0];
        int end = sorted[0][1];

        for (int index = 1; index < sorted.length; index++) {
            int[] current = sorted[index];
            if (current[0] <= end) {
                end = Math.max(end, current[1]);
            } else {
                merged.add(new int[] {start, end});
                start = current[0];
                end = current[1];
            }
        }
        merged.add(new int[] {start, end});
        return merged.toArray(new int[merged.size()][]);
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
