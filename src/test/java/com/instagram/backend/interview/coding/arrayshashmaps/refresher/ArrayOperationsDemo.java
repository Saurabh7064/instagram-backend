package com.instagram.backend.interview.coding.arrayshashmaps.refresher;

import java.util.Arrays;

/**
 * Runnable examples for the Java Array cheat sheet.
 */
public class ArrayOperationsDemo {

    public static void main(String[] args) {
        demonstrateReadWriteAndCopy();
        demonstrateFillSortAndSearch();
        demonstrateGenerateReverseAndAggregate();

        System.out.println("All Array operation checks passed.");
    }

    private static void demonstrateReadWriteAndCopy() {
        int[] numbers = {5, 1, 5, 2};

        assertEquals(5, numbers[0], "read by index");
        numbers[1] = 9;
        assertArrayEquals(new int[] {5, 9, 5, 2}, numbers, "update by index");

        int[] copy = Arrays.copyOf(numbers, numbers.length);
        int[] middle = Arrays.copyOfRange(numbers, 1, 3);
        assertTrue(copy != numbers, "copy has a different identity");
        assertArrayEquals(numbers, copy, "copy has the same values");
        assertArrayEquals(new int[] {9, 5}, middle, "copy a half-open range");

        int[] destination = new int[2];
        System.arraycopy(numbers, 1, destination, 0, 2);
        assertArrayEquals(new int[] {9, 5}, destination, "copy into another array");
    }

    private static void demonstrateFillSortAndSearch() {
        int[] numbers = {5, 9, 5, 2};
        int[] filled = Arrays.copyOf(numbers, numbers.length);

        Arrays.fill(filled, 1, 3, 7);
        assertArrayEquals(new int[] {5, 7, 7, 2}, filled, "fill a range");

        Arrays.sort(numbers);
        assertArrayEquals(new int[] {2, 5, 5, 9}, numbers, "sort ascending");
        assertTrue(Arrays.binarySearch(numbers, 9) >= 0, "find in sorted input");
        assertTrue(Arrays.binarySearch(numbers, 8) < 0, "report a missing value");
    }

    private static void demonstrateGenerateReverseAndAggregate() {
        int[] squares = new int[5];
        Arrays.setAll(squares, index -> index * index);
        assertArrayEquals(new int[] {0, 1, 4, 9, 16}, squares, "generate by index");

        reverse(squares);
        assertArrayEquals(new int[] {16, 9, 4, 1, 0}, squares, "reverse in place");
        assertEquals(30, Arrays.stream(squares).sum(), "sum values");
    }

    private static void reverse(int[] values) {
        for (int left = 0, right = values.length - 1;
                left < right;
                left++, right--) {
            int temporary = values[left];
            values[left] = values[right];
            values[right] = temporary;
        }
    }

    private static void assertArrayEquals(int[] expected, int[] actual, String scenario) {
        if (!Arrays.equals(expected, actual)) {
            throw new AssertionError(
                    scenario + ": expected " + Arrays.toString(expected)
                            + " but received " + Arrays.toString(actual));
        }
    }

    private static void assertEquals(int expected, int actual, String scenario) {
        if (expected != actual) {
            throw new AssertionError(
                    scenario + ": expected " + expected + " but received " + actual);
        }
    }

    private static void assertTrue(boolean condition, String scenario) {
        if (!condition) {
            throw new AssertionError(scenario);
        }
    }
}
