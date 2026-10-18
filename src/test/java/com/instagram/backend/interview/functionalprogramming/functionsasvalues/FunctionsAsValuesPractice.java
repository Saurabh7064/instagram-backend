package com.instagram.backend.interview.functionalprogramming.functionsasvalues;

import java.util.Arrays;
import java.util.function.IntUnaryOperator;

/** Runnable concept-first functional-programming practice. */
public class FunctionsAsValuesPractice {

    public static void main(String[] args) {
        try {
            runChecks();
            System.out.println("All practice checks passed.");
        } catch (UnsupportedOperationException exception) {
            System.out.println("Practice ready: implement the TODO methods, then rerun main.");
        }
    }

    static int[] transform(int[] numbers, IntUnaryOperator operation) {
        // TODO: Apply the supplied behavior to every input value into a new array.
        throw new UnsupportedOperationException("Implement transform");
    }

    static int doubleValue(int value) {
        return value * 2;
    }

    private static void runChecks() {
        int[] input = {-2, 0, 3};
        check(new int[] {-4, 0, 6}, transform(input, value -> value * 2), "doubling lambda");
        check(new int[] {1, 3, 6}, transform(input, value -> value + 3), "different behavior, same traversal");
        check(new int[] {-4, 0, 6}, transform(input, FunctionsAsValuesPractice::doubleValue), "method reference");
        check(new int[] {-2, 0, 3}, input, "input preserved");
        check(new int[] {}, transform(new int[] {}, value -> value * 2), "empty input");
        check(new int[] {5}, transform(new int[] {5}, value -> value), "identity behavior");
        int[] output = transform(input, value -> value * 2);
        output[0] = 99;
        check(new int[] {-2, 0, 3}, input, "no output alias");
    }

    private static void check(int[] expected, int[] actual, String scenario) {
        if (!Arrays.equals(expected, actual)) {
            throw new AssertionError(scenario + ": expected " + Arrays.toString(expected)
                    + " but got " + Arrays.toString(actual));
        }
    }
}
