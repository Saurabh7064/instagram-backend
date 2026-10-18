package com.instagram.backend.interview.functionalprogramming.arrayconcatenation;

import java.util.Arrays;
import java.util.stream.IntStream;

/** Runnable concept-first functional-programming solution. */
public class ArrayConcatenationSolution {

    public static void main(String[] args) {
        runChecks();
        System.out.println("All reference-solution checks passed.");
    }

    static int[] getConcatenation(int[] numbers) {
        return IntStream.concat(Arrays.stream(numbers), Arrays.stream(numbers)).toArray();
    }

    static int[] getConcatenationWithLoop(int[] numbers) {
        int[] result = new int[2 * numbers.length];
        for (int index = 0; index < numbers.length; index++) {
            result[index] = numbers[index];
            result[index + numbers.length] = numbers[index];
        }
        return result;
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
