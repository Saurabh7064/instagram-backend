package com.instagram.backend.interview.functionalprogramming.arrayconcatenation;

import java.util.Arrays;
import java.util.stream.IntStream;

/** Runnable concept-first functional-programming practice. */
public class ArrayConcatenationPractice {

    public static void main(String[] args) {
        try {
            runChecks();
            System.out.println("All practice checks passed.");
        } catch (UnsupportedOperationException exception) {
            System.out.println("Practice ready: implement the TODO methods, then rerun main.");
        }
    }

    static int[] getConcatenation(int[] numbers) {
        // TODO: Join two fresh ordered streams of the same input, then return a new array.
        throw new UnsupportedOperationException("Implement getConcatenation");
    }

    static int[] getConcatenationWithLoop(int[] numbers) {
        // TODO: Write each value to its position in both halves of a new array.
        throw new UnsupportedOperationException("Implement getConcatenationWithLoop");
    }

    private static void runChecks() {
        int[] input = {1, 2, 1};
        int[] expected = {1, 2, 1, 1, 2, 1};
        check(expected, getConcatenation(input), "stream example");
        check(expected, getConcatenationWithLoop(input), "loop example");
        check(new int[] {3, 3}, getConcatenation(new int[] {3}), "singleton");
        check(new int[] {}, getConcatenation(new int[] {}), "defensive empty input");
        check(new int[] {1, 2, 1}, input, "input preserved");
        int[] output = getConcatenation(input);
        if (output == input) throw new AssertionError("new output required");
        output[0] = 99;
        check(new int[] {1, 2, 1}, input, "output independent");
        for (int length = 0; length <= 7; length++) {
            int[] values = new int[length];
            for (int index = 0; index < length; index++) values[index] = index + 1;
            check(getConcatenationWithLoop(values), getConcatenation(values), "stream/loop agreement");
        }
    }

    private static void check(int[] expected, int[] actual, String scenario) {
        if (!Arrays.equals(expected, actual)) {
            throw new AssertionError(scenario + ": expected " + Arrays.toString(expected)
                    + " but got " + Arrays.toString(actual));
        }
    }
}
