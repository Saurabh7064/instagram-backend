package com.instagram.backend.interview.multithreading.foundations;

import java.util.ArrayList;
import java.util.List;

/** Small Java concept examples, without worker creation or algorithm traces. */
public class ThreadFoundationsExamples {
    public static void main(String[] args) {
        showCurrentThread();
        checkOrdinaryCall();
        System.out.println("Current-thread example checks passed.");
    }

    /** Run this main only when studying task versus thread. */
    public static class TaskOwnershipExample {
        public static void main(String[] args) {
            runTaskDirectly();
        }
    }

    /** Run this main only when studying shared references. */
    public static class SharedReferenceExample {
        public static void main(String[] args) {
            showSharedReference();
        }
    }

    static void showCurrentThread() {
        Thread current = Thread.currentThread();
        String name = current.getName();
        System.out.println("Current thread: " + name);
        if (current != Thread.currentThread()) throw new AssertionError("Current thread changed");
    }

    static Thread calledMethod() {
        return Thread.currentThread();
    }

    static void checkOrdinaryCall() {
        Thread caller = Thread.currentThread();
        Thread insideMethod = calledMethod();
        if (caller != insideMethod) throw new AssertionError("Ordinary call changed threads");
        System.out.println("Ordinary method call used the same thread.");
    }

    static final class RecordingTask implements Runnable {
        private Thread executedBy;

        @Override
        public void run() {
            executedBy = Thread.currentThread();
        }
    }

    static void runTaskDirectly() {
        RecordingTask task = new RecordingTask();
        task.run();
        if (task.executedBy != Thread.currentThread()) {
            throw new AssertionError("Direct run() should use its caller");
        }
        System.out.println("Direct run() used the caller thread.");
    }

    static void showSharedReference() {
        List<String> first = new ArrayList<>();
        List<String> second = first;
        second.add("saved");
        if (first != second || !first.equals(List.of("saved"))) {
            throw new AssertionError("References should identify the same list");
        }
        System.out.println("Both references see: " + first);
    }
}
