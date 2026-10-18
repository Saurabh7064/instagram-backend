package com.instagram.backend.interview.functionalprogramming.foundations;

import java.util.Arrays;

/** Runnable concept-first functional-programming practice. */
public class PureFunctionsPractice {

    public static void main(String[] args) {
        try {
            runChecks();
            System.out.println("All practice checks passed.");
        } catch (UnsupportedOperationException exception) {
            System.out.println("Practice ready: implement the TODO methods, then rerun main.");
        }
    }

    static int doubleValue(int value) {
        // TODO: Return twice the argument without reading or changing external state.
        throw new UnsupportedOperationException("Implement doubleValue");
    }

    static int[] doubleNumbers(int[] numbers) {
        // TODO: Return a new array of doubled values; keep numbers unchanged.
        throw new UnsupportedOperationException("Implement doubleNumbers");
    }

    private static void runChecks() {
        if (doubleValue(3) != 6 || doubleValue(3) != 6 || doubleValue(-2) != -4)
            throw new AssertionError("scalar transformation");
        int[] input = {-2, 0, 3};
        check(new int[] {-4, 0, 6}, doubleNumbers(input), "ordinary transformation");
        check(new int[] {-2, 0, 3}, input, "source preserved");
        check(doubleNumbers(input), doubleNumbers(input), "same input values give same output values");
        int[] first = doubleNumbers(input);
        int[] second = doubleNumbers(input);
        if (first == input || first == second) throw new AssertionError("independent result arrays required");
        first[0] = 999;
        check(new int[] {-2, 0, 3}, input, "caller changing output cannot change input");
        check(new int[] {-4, 0, 6}, second, "separate outputs independent");
        check(new int[] {}, doubleNumbers(new int[] {}), "empty input");
        check(new int[] {2_000_000, -2_000_000}, doubleNumbers(new int[] {1_000_000, -1_000_000}), "documented bounds");
    }

    private static void check(int[] expected, int[] actual, String scenario) {
        if (!Arrays.equals(expected, actual)) {
            throw new AssertionError(scenario + ": expected " + Arrays.toString(expected)
                    + " but got " + Arrays.toString(actual));
        }
    }
}
