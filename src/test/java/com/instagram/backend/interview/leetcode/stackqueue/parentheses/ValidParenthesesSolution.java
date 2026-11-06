package com.instagram.backend.interview.leetcode.stackqueue.parentheses;

import java.util.ArrayDeque;
import java.util.Deque;

/** Runnable Module 05 solution; learning progress is tracked separately. */
public class ValidParenthesesSolution {

    public static void main(String[] args) {
        runChecks();
        System.out.println("All reference-solution checks passed (10 parentheses scenarios).");
    }

    // text index: 0 1 2 3 4 5 6 7
    // text      : { [ ( ) ( ) ] }
    // Input: text="{[()()]}"; expected return: true.
    public static boolean isValid(String text) {
        Deque<Character> expected = new ArrayDeque<>();
        for (int index = 0; index < text.length(); index++) {
            char current = text.charAt(index);
            if (current == '(') {
                expected.push(')');
            } else if (current == '[') {
                expected.push(']');
            } else if (current == '{') {
                expected.push('}');
            } else {
                if (expected.isEmpty() || expected.pop() != current) {
                    return false;
                }
            }
        }
        return expected.isEmpty();
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
