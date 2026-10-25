package com.instagram.backend.interview.multithreading.startandjoin;

/**
 * Runnable reference solution for Multithreading Micro-Lesson 01.
 */
public class StartAndJoinSolution {

    public static void main(String[] args) throws InterruptedException {
        assertEquals(49, squareOnWorker(7), "positive number");
        assertEquals(16, squareOnWorker(-4), "negative number");
        assertEquals(0, squareOnWorker(0), "zero");
        assertEquals(2_147_395_600, squareOnWorker(46_340), "largest safe example");
        assertEquals(2_147_395_600, squareOnWorker(-46_340), "negative boundary");

        System.out.println("All reference-solution checks passed.");
    }

    static int squareOnWorker(int number) throws InterruptedException {
        SquareTask task = new SquareTask(number);
        Thread worker = new Thread(task, "square-worker");
        worker.start();
        worker.join();
        if (task.executedBy != worker || task.executedBy == Thread.currentThread()) {
            throw new AssertionError("The calculation must run on the started worker");
        }
        return task.result;
    }

    static final class SquareTask implements Runnable {
        private final int number;
        private int result;
        private Thread executedBy;

        SquareTask(int number) {
            this.number = number;
        }

        @Override
        public void run() {
            result = number * number;
            executedBy = Thread.currentThread();
        }
    }

    private static void assertEquals(int expected, int actual, String scenario) {
        if (expected != actual) {
            throw new AssertionError(
                    scenario + ": expected " + expected + " but received " + actual);
        }
    }
}
