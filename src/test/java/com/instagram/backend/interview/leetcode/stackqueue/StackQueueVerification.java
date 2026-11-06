package com.instagram.backend.interview.leetcode.stackqueue;

import com.instagram.backend.interview.leetcode.stackqueue.minstack.MinStackSolution.MinStack;
import com.instagram.backend.interview.leetcode.stackqueue.parentheses.ValidParenthesesSolution;
import com.instagram.backend.interview.leetcode.stackqueue.rpn.EvaluateRpnSolution;
import com.instagram.backend.interview.leetcode.stackqueue.temperatures.DailyTemperaturesSolution;
import com.instagram.backend.interview.leetcode.stackqueue.windowmaximum.SlidingWindowMaximumSolution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Independent deterministic models; verification infrastructure, not a lesson prerequisite. */
public final class StackQueueVerification {
    private static final long SEED = 20261106L;
    private static final char[] BRACKETS = {'(', ')', '[', ']', '{', '}'};
    private static int bracketCases;
    private static int minStackOperations;
    private static int temperatureCases;
    private static int windowCases;
    private static int expressionCases;

    public static void main(String[] args) {
        for (int length = 0; length <= 6; length++) {
            verifyBrackets(new char[length], 0);
        }
        Random random = new Random(SEED);
        verifyMinStack(random);
        verifyTemperatureAndWindows(random);
        verifyExpressions(random);
        System.out.println("Independent model checks passed: " + bracketCases + " bracket strings; "
                + minStackOperations + " Min Stack operations; " + temperatureCases + " temperature arrays; "
                + windowCases + " sliding-window cases; " + expressionCases + " RPN expressions. Seed=" + SEED + ".");
    }

    private static void verifyBrackets(char[] input, int index) {
        if (index < input.length) {
            for (char bracket : BRACKETS) {
                input[index] = bracket;
                verifyBrackets(input, index + 1);
            }
            return;
        }
        String source = new String(input);
        // Any balanced sequence can be reduced to empty by removing adjacent matching pairs.
        // This oracle never uses the reference implementation's stack representation.
        String reduced = source;
        int previousLength;
        do {
            previousLength = reduced.length();
            reduced = reduced.replace("()", "").replace("[]", "").replace("{}", "");
        } while (reduced.length() != previousLength);
        require(ValidParenthesesSolution.isValid(source) == reduced.isEmpty(), "bracket mismatch: " + source);
        bracketCases++;
    }

    private static void verifyMinStack(Random random) {
        for (int trial = 0; trial < 200; trial++) {
            MinStack stack = new MinStack();
            List<Integer> model = new ArrayList<>();
            // Force duplicate minima and both extreme values before random operations.
            for (int value : new int[] {Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE}) {
                stack.push(value);
                model.add(value);
                minStackOperations++;
                verifyStackReads(stack, model);
            }
            for (int operation = 0; operation < 300; operation++) {
                if (model.isEmpty() || random.nextBoolean()) {
                    int value = random.nextInt();
                    stack.push(value);
                    model.add(value);
                } else {
                    stack.pop();
                    model.remove(model.size() - 1);
                }
                minStackOperations++;
                if (!model.isEmpty()) verifyStackReads(stack, model);
            }
            while (!model.isEmpty()) {
                verifyStackReads(stack, model);
                stack.pop();
                model.remove(model.size() - 1);
                minStackOperations++;
            }
        }
    }

    private static void verifyStackReads(MinStack stack, List<Integer> model) {
        require(stack.top() == model.get(model.size() - 1), "Min Stack top mismatch");
        require(stack.getMin() == Collections.min(model), "Min Stack minimum mismatch");
        minStackOperations += 2;
    }

    private static void verifyTemperatureAndWindows(Random random) {
        for (int trial = 0; trial < 1000; trial++) {
            int length = 1 + random.nextInt(12);
            int[] temperatures = new int[length];
            int[] numbers = new int[length];
            for (int index = 0; index < length; index++) {
                temperatures[index] = 30 + random.nextInt(71);
                numbers[index] = random.nextInt(21) - 10;
            }
            int[] originalTemperatures = temperatures.clone();
            int[] expectedWaits = new int[length];
            for (int today = 0; today < length; today++) {
                for (int future = today + 1; future < length; future++) {
                    if (temperatures[future] > temperatures[today]) {
                        expectedWaits[today] = future - today;
                        break;
                    }
                }
            }
            require(Arrays.equals(DailyTemperaturesSolution.dailyTemperatures(temperatures), expectedWaits),
                    "temperature mismatch: " + Arrays.toString(temperatures));
            require(Arrays.equals(temperatures, originalTemperatures), "temperature input mutated");
            temperatureCases++;
            int[] originalNumbers = numbers.clone();
            for (int width = 1; width <= length; width++) {
                int[] expectedMaximums = new int[length - width + 1];
                for (int start = 0; start + width <= length; start++) {
                    int maximum = numbers[start];
                    for (int offset = 1; offset < width; offset++) maximum = Math.max(maximum, numbers[start + offset]);
                    expectedMaximums[start] = maximum;
                }
                require(Arrays.equals(SlidingWindowMaximumSolution.maxSlidingWindow(numbers, width), expectedMaximums),
                        "window mismatch: width=" + width + ", numbers=" + Arrays.toString(numbers));
                require(Arrays.equals(numbers, originalNumbers), "window input mutated");
                windowCases++;
            }
        }
        int[] extreme = {Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE};
        require(Arrays.equals(SlidingWindowMaximumSolution.maxSlidingWindow(extreme, 2),
                new int[] {Integer.MAX_VALUE, Integer.MAX_VALUE}), "window integer extremes mismatch");
        windowCases++;
    }

    private static void verifyExpressions(Random random) {
        for (int trial = 0; trial < 2000; trial++) {
            Expression expression = expression(random, 4);
            String[] tokens = expression.tokens.toArray(new String[0]);
            String[] original = tokens.clone();
            require(EvaluateRpnSolution.evalRPN(tokens) == expression.value,
                    "RPN mismatch: " + Arrays.toString(tokens) + ", expected=" + expression.value);
            require(Arrays.equals(tokens, original), "RPN input mutated");
            expressionCases++;
        }
        require(EvaluateRpnSolution.evalRPN(new String[] {"7", "-3", "/"}) == -2, "division must truncate toward zero");
        expressionCases++;
    }

    // Construct a tree with its value, then serialize children before the operator.
    // The expected result comes from the tree, never from a token-evaluation stack.
    private static Expression expression(Random random, int remainingDepth) {
        if (remainingDepth == 0 || random.nextInt(4) == 0) {
            int value = random.nextInt(19) - 9;
            return new Expression(new ArrayList<>(List.of(Integer.toString(value))), value);
        }
        Expression left = expression(random, remainingDepth - 1);
        Expression right = expression(random, remainingDepth - 1);
        char operator = new char[] {'+', '-', '*', '/'}[random.nextInt(4)];
        if (operator == '/' && right.value == 0) operator = '+';
        long result = switch (operator) {
            case '+' -> (long) left.value + right.value;
            case '-' -> (long) left.value - right.value;
            case '*' -> (long) left.value * right.value;
            case '/' -> (long) left.value / right.value;
            default -> throw new AssertionError("unknown operator");
        };
        // Keep intermediate results within a small exact range, including negatives and zero.
        if (result < -10000 || result > 10000) {
            return new Expression(new ArrayList<>(List.of("0")), 0);
        }
        List<String> tokens = new ArrayList<>(left.tokens);
        tokens.addAll(right.tokens);
        tokens.add(Character.toString(operator));
        return new Expression(tokens, (int) result);
    }

    private record Expression(List<String> tokens, int value) { }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
