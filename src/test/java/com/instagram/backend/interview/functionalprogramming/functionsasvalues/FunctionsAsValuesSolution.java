package com.instagram.backend.interview.functionalprogramming.functionsasvalues;

import java.util.Arrays;
import java.util.function.IntUnaryOperator;

/** Runnable concept-first functional-programming solution. */
public class FunctionsAsValuesSolution {

    public static void main(String[] args) {
        runChecks();
        System.out.println("All reference-solution checks passed.");
    }

    static int[] transform(int[] numbers, IntUnaryOperator operation) {
        int[] result = new int[numbers.length];
        for (int index = 0; index < numbers.length; index++) {
            result[index] = operation.applyAsInt(numbers[index]);
        }
        return result;
    }

    static int doubleValue(int value) {
        return value * 2;
    }

    private static void runChecks() {
        int[] input = {-2, 0, 3};
        check(new int[] {-4, 0, 6}, transform(input, value -> value * 2), "doubling lambda");
        check(new int[] {1, 3, 6}, transform(input, value -> value + 3), "different behavior, same traversal");
        check(new int[] {-4, 0, 6}, transform(input, FunctionsAsValuesSolution::doubleValue), "method reference");
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
