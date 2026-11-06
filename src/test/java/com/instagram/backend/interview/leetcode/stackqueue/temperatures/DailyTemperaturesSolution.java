package com.instagram.backend.interview.leetcode.stackqueue.temperatures;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

/** Directly runnable Module 05 solution; comprehension is tracked separately. */
public class DailyTemperaturesSolution {
    public static void main(String[] args) {
        runChecks();
        System.out.println("All reference-solution checks passed.");
    }

    // temperatures index:  0  1  2  3  4  5  6  7
    // temperatures      : 73 74 75 71 69 72 76 73
    // Input: temperatures=[73,74,75,71,69,72,76,73]
    // Expected return: [1,1,4,2,1,1,0,0]
    public static int[] dailyTemperatures(int[] temperatures) {
        int[] waits = new int[temperatures.length];
        Deque<Integer> pending = new ArrayDeque<>();
        for (int day = 0; day < temperatures.length; day++) {
            while (!pending.isEmpty() && temperatures[pending.peekLast()] < temperatures[day]) {
                int earlier = pending.removeLast();
                waits[earlier] = day - earlier;
            }
            pending.addLast(day);
        }
        return waits;
    }

    private static void runChecks() {
        check(new int[] {1,1,4,2,1,1,0,0}, dailyTemperatures(new int[] {73,74,75,71,69,72,76,73}), "worked input");
        check(new int[] {0}, dailyTemperatures(new int[] {30}), "one day");
        check(new int[] {0,0,0}, dailyTemperatures(new int[] {70,70,70}), "equal is not warmer");
        check(new int[] {0,0,0}, dailyTemperatures(new int[] {90,80,70}), "falling temperatures");
        check(new int[] {1,1,0}, dailyTemperatures(new int[] {30,60,90}), "rising temperatures");
        check(new int[] {3,2,1,0}, dailyTemperatures(new int[] {70,69,68,71}), "resolve several earlier days");
        check(new int[] {}, dailyTemperatures(new int[] {}), "empty extension");
        int[] input = {73,74,75,71,69,72,76,73};
        int[] before = input.clone();
        int[] output = dailyTemperatures(input);
        check(before, input, "input unchanged");
        output[0] = -1;
        check(before, input, "output does not alias input");
    }

    private static void check(int[] expected, int[] actual, String scenario) {
        if (!Arrays.equals(expected, actual)) {
            throw new AssertionError(scenario + ": expected " + Arrays.toString(expected)
                    + " but got " + Arrays.toString(actual));
        }
    }
}
