package com.instagram.backend.interview.leetcode.stackqueue.rpn;

/** Implement the TODO method, then run main in IntelliJ. */
public class EvaluateRpnPractice {

    public static int evalRPN(String[] tokens) {
        // TODO: Read tokens, preserve operand order, and save each partial result.
        throw new UnsupportedOperationException("Implement evalRPN");
    }

    public static void main(String[] args) {
        try {
            runChecks();
            System.out.println("EvaluateRpn practice checks passed (11 results plus input preservation).");
        } catch (UnsupportedOperationException exception) {
            System.out.println("Practice ready: implement the TODO method, then rerun main.");
        }
    }

    private static void runChecks() {
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
    }

    private static void check(int expected, int actual, String scenario) {
        if (expected != actual) {
            throw new AssertionError(scenario + ": expected " + expected + " but got " + actual);
        }
    }
}
