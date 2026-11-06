package com.instagram.backend.interview.leetcode.stackqueue.minstack;

/** Implement the TODO methods, then run main in IntelliJ. */
public class MinStackPractice {

    public static final class MinStack {
        // TODO: Add the saved state needed by the four operations.

        public void push(int value) {
            // TODO: Add value and remember the minimum at this depth.
            throw new UnsupportedOperationException("Implement push");
        }

        public void pop() {
            // TODO: Remove the newest value and its saved state together.
            throw new UnsupportedOperationException("Implement pop");
        }

        public int top() {
            // TODO: Return the newest value without removing it.
            throw new UnsupportedOperationException("Implement top");
        }

        public int getMin() {
            // TODO: Return the minimum without scanning or removing values.
            throw new UnsupportedOperationException("Implement getMin");
        }
    }

    public static void main(String[] args) {
        try {
            runChecks();
            System.out.println("MinStack practice checks passed (10 assertions).");
        } catch (UnsupportedOperationException exception) {
            System.out.println("Practice ready: implement the TODO methods, then rerun main.");
        }
    }

    private static void runChecks() {
        MinStack stack = new MinStack();
        stack.push(3);
        stack.push(1);
        stack.push(1);
        check(1, stack.getMin(), "fixed trace: duplicate minimum");
        stack.pop();
        check(1, stack.getMin(), "fixed trace: one minimum remains");
        stack.pop();
        check(3, stack.top(), "fixed trace: restored top");
        check(3, stack.getMin(), "fixed trace: restored minimum");
        stack.pop();
        stack.push(Integer.MAX_VALUE);
        stack.push(Integer.MIN_VALUE);
        stack.push(Integer.MIN_VALUE);
        check(Integer.MIN_VALUE, stack.top(), "full int range: top");
        check(Integer.MIN_VALUE, stack.getMin(), "full int range: minimum");
        stack.pop();
        check(Integer.MIN_VALUE, stack.getMin(), "full int range: duplicate remains");
        stack.pop();
        check(Integer.MAX_VALUE, stack.getMin(), "full int range: restored minimum");
        stack.pop();
        stack.push(-7);
        check(-7, stack.top(), "reuse after empty: top");
        check(-7, stack.getMin(), "reuse after empty: minimum");
        stack.pop();
    }

    private static void check(int expected, int actual, String scenario) {
        if (expected != actual) {
            throw new AssertionError(scenario + ": expected " + expected + " but got " + actual);
        }
    }
}
