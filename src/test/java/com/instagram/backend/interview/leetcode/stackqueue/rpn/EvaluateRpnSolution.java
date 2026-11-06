package com.instagram.backend.interview.leetcode.stackqueue.rpn;

import java.util.ArrayDeque;
import java.util.Deque;

/** Runnable reference for valid reverse Polish expressions. */
public class EvaluateRpnSolution {

    public static int evalRPN(String[] tokens) {
        Deque<Integer> values = new ArrayDeque<>();
        for (int index = 0; index < tokens.length; index++) {
            String token = tokens[index];
            if (token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/")) {
                int right = values.pop();
                int left = values.pop();
                int result = switch (token) {
                    case "+" -> left + right;
                    case "-" -> left - right;
                    case "*" -> left * right;
                    case "/" -> left / right;
                    default -> throw new IllegalStateException("Unexpected operator: " + token);
                };
                values.push(result);
            } else {
                values.push(Integer.parseInt(token));
            }
        }
        return values.pop();
    }

    public static void main(String[] args) {
        String[] fixed = {"4", "13", "5", "/", "+", "2", "*"};
        String[] before = fixed.clone();
        check(12, evalRPN(fixed), "fixed seven-token trace");
        if (!java.util.Arrays.equals(before, fixed)) throw new AssertionError("Input mutated");
        check(9, evalRPN(new String[] {"9"}), "single number");
        check(0, evalRPN(new String[] {"0"}), "zero");
        check(-2, evalRPN(new String[] {"7", "-3", "/"}), "division truncates toward zero");
        check(2, evalRPN(new String[] {"-7", "-3", "/"}), "two negative operands");
        check(-6, evalRPN(new String[] {"2", "8", "-"}), "subtraction operand order");
        check(0, evalRPN(new String[] {"2", "8", "/"}), "division operand order");
        check(-15, evalRPN(new String[] {"-3", "5", "*"}), "negative number is not an operator");
        check(200, evalRPN(new String[] {"200"}), "maximum token value");
        check(-200, evalRPN(new String[] {"-200"}), "minimum token value");
        check(80000, evalRPN(new String[] {"200", "200", "+", "200", "*"}), "intermediate exceeds token limit");
        System.out.println("EvaluateRpn reference checks passed (11 results plus input preservation).");
    }

    private static void check(int expected, int actual, String scenario) {
        if (expected != actual) {
            throw new AssertionError(scenario + ": expected " + expected + " but got " + actual);
        }
    }
}
