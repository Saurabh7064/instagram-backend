package com.instagram.backend.interview.leetcode.stackqueue.minstack;

import java.util.ArrayDeque;
import java.util.Deque;

/** Runnable reference; artifact readiness does not imply learner understanding. */
public class MinStackSolution {

    public static final class MinStack {
        private final Deque<int[]> entries;

        public MinStack() {
            entries = new ArrayDeque<>();
        }

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
        // Constructing and mutating a second stack must not reset or share the first.
        stack.push(3);
        MinStack other = new MinStack();
        other.push(1);
        other.push(1);
        check(3, stack.top(), "constructor gives each object its own top");
        check(3, stack.getMin(), "constructor gives each object its own minimum");
        check(1, other.getMin(), "second object keeps its duplicate minimum");
        other.pop();
        check(1, other.getMin(), "second object retains one minimum");
        check(3, stack.getMin(), "second object removal leaves first unchanged");
        stack.pop();
        check(1, other.top(), "emptying first object leaves second unchanged");
        other.pop();
        System.out.println("MinStack reference checks passed (16 assertions, including independent constructors).");
    }

    private static void check(int expected, int actual, String scenario) {
        if (expected != actual) {
            throw new AssertionError(scenario + ": expected " + expected + " but got " + actual);
        }
    }
}
