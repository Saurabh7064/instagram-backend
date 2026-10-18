package com.instagram.backend.interview.leetcode.prefixintervals.insertinterval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Directly runnable Module 04 solution; learning status is tracked separately. */
public class InsertIntervalSolution {

    public static void main(String[] args) {
        runChecks();
        System.out.println("All reference-solution checks passed.");
    }

    static int[][] insert(int[][] intervals, int[] newInterval) {
        List<int[]> result = new ArrayList<>();
        int index = 0;
        int start = newInterval[0];
        int end = newInterval[1];

        while (index < intervals.length && intervals[index][1] < start) {
            result.add(intervals[index].clone());
            index++;
        }
        while (index < intervals.length && intervals[index][0] <= end) {
            start = Math.min(start, intervals[index][0]);
            end = Math.max(end, intervals[index][1]);
            index++;
        }
        result.add(new int[] {start, end});
        while (index < intervals.length) {
            result.add(intervals[index].clone());
            index++;
        }
        return result.toArray(new int[result.size()][]);
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
