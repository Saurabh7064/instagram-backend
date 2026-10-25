package com.instagram.backend.interview.multithreading.foundations;

import java.util.concurrent.CountDownLatch;

import com.instagram.backend.interview.multithreading.startandjoin.StartAndJoinSolution;

/** Verification helper; latch coordination is test machinery, not an introductory prerequisite. */
public class ThreadFoundationsVerification {
    public static void main(String[] args) throws InterruptedException {
        ThreadFoundationsExamples.main(args);
        ThreadFoundationsExamples.TaskOwnershipExample.main(args);
        ThreadFoundationsExamples.SharedReferenceExample.main(args);
        StartAndJoinSolution.main(args);
        checkReadingBeforeCompletion();
        System.out.println("Passed thread identity, task ownership, shared-reference, square/boundary, and controlled completion checks.");
    }

    private static void checkReadingBeforeCompletion() throws InterruptedException {
        CountDownLatch release = new CountDownLatch(1);
        PausedSquare task = new PausedSquare(release);
        Thread worker = new Thread(task, "paused-square-test");
        worker.start();
        try {
            // The gate forbids the write, so this observation does not depend on scheduling speed.
            if (task.result != 0) throw new AssertionError("Work must still be held back");
        } finally {
            release.countDown();
            worker.join();
        }
        if (task.result != 49) throw new AssertionError("Completed work must be visible after join");
        System.out.println("Controlled check: before release = 0; after release and join = 49.");
    }

    private static final class PausedSquare implements Runnable {
        private final CountDownLatch release;
        private int result;

        PausedSquare(CountDownLatch release) {
            this.release = release;
        }

        @Override
        public void run() {
            try {
                release.await();
                result = 7 * 7;
            } catch (InterruptedException interruption) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
