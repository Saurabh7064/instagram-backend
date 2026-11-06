package com.instagram.backend.interview.leetcode.stackqueue.parentheses;

import java.util.ArrayDeque;
import java.util.Deque;

/** Runnable Module 05 practice; learning progress is tracked separately. */
public class ValidParenthesesPractice {

    public static void main(String[] args) {
        try {
            runChecks();
            System.out.println("All practice checks passed.");
        } catch (UnsupportedOperationException exception) {
            System.out.println("Practice ready: implement isValid, then rerun main. No solution has passed yet.");
        }
    }

    public static boolean isValid(String text) {
        // TODO: Match each closing bracket to the most recent unmatched opening.
        throw new UnsupportedOperationException("Implement isValid");
    }

    private static void runChecks() {
        check(true, isValid("{[()()]}"), "worked nested input");
        check(true, isValid("()[]{}"), "adjacent pairs");
        check(true, isValid(""), "empty extension");
        check(false, isValid("("), "unclosed opening");
        check(false, isValid("]"), "closing before opening");
        check(false, isValid("(]"), "wrong type");
        check(false, isValid("([)]"), "crossing pairs");
        check(false, isValid("())"), "extra closing");
        check(false, isValid("(("), "leftover openings");
        check(true, isValid("(".repeat(5000) + ")".repeat(5000)), "deep nesting");
    }

    private static void check(boolean expected, boolean actual, String scenario) {
        if (expected != actual) {
            throw new AssertionError(scenario + ": expected " + expected + " but got " + actual);
        }
    }
}
