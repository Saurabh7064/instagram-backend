package com.instagram.backend.interview.multithreading.startandjoin;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Runnable reference solution for Multithreading Micro-Lesson 01.
 */
public class StartAndJoinSolution {

    public static void main(String[] args) throws InterruptedException {
        assertEquals(49, squareOnWorker(7), "positive number");
        assertEquals(16, squareOnWorker(-4), "negative number");
        assertEquals(0, squareOnWorker(0), "zero");
        assertEquals(2_147_395_600, squareOnWorker(46_340), "largest safe example");

        System.out.println("All reference-solution checks passed.");
    }

    static int squareOnWorker(int number) throws InterruptedException {
        AtomicInteger result = new AtomicInteger();
        Thread worker = new Thread(
                () -> result.set(number * number),
                "square-worker");

        worker.start();
        worker.join();
        return result.get();
    }

    private static void assertEquals(int expected, int actual, String scenario) {
        if (expected != actual) {
            throw new AssertionError(
                    scenario + ": expected " + expected + " but received " + actual);
        }
    }
}
