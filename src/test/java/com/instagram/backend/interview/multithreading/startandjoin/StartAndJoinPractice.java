package com.instagram.backend.interview.multithreading.startandjoin;

/**
 * Editable practice program for Multithreading Micro-Lesson 01.
 */
public class StartAndJoinPractice {

    public static void main(String[] args) throws InterruptedException {
        try {
            runChecks();
            System.out.println("All practice checks passed.");
        } catch (UnsupportedOperationException exception) {
            System.out.println("Practice program is ready.");
            System.out.println("Implement squareOnWorker, then run this main method again.");
        }
    }

    static int squareOnWorker(int number) throws InterruptedException {
        // TODO: Calculate on a worker thread and wait for its completion without sleeping.
        throw new UnsupportedOperationException("Implement squareOnWorker");
    }

    private static void runChecks() throws InterruptedException {
        assertEquals(49, squareOnWorker(7), "positive number");
        assertEquals(16, squareOnWorker(-4), "negative number");
        assertEquals(0, squareOnWorker(0), "zero");
        assertEquals(2_147_395_600, squareOnWorker(46_340), "largest safe example");
    }

    private static void assertEquals(int expected, int actual, String scenario) {
        if (expected != actual) {
            throw new AssertionError(
                    scenario + ": expected " + expected + " but received " + actual);
        }
    }
}
