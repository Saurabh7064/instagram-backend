package com.instagram.backend.interview.leetcode.stackqueue.minstack;

import java.util.ArrayDeque;
import java.util.Deque;

/** Runnable reference; artifact readiness does not imply learner understanding. */
public class MinStackSolution {

    public static final class MinStack {
        private final Deque<int[]> entries = new ArrayDeque<>();

        public void push(int value) {
            int minimum = entries.isEmpty() ? value : Math.min(value, entries.peek()[1]);
            entries.push(new int[] {value, minimum});
        }

        public void pop() {
            entries.pop();
        }

        public int top() {
            return entries.peek()[0];
        }

        public int getMin() {
            return entries.peek()[1];
        }
    }

    public static void main(String[] args) {
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
        System.out.println("MinStack reference checks passed (10 assertions).");
    }

    private static void check(int expected, int actual, String scenario) {
        if (expected != actual) {
            throw new AssertionError(scenario + ": expected " + expected + " but got " + actual);
        }
    }
}
