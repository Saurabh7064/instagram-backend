package com.instagram.backend.interview.functionalprogramming;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.IntUnaryOperator;

/** Optional cross-lesson verification, independent of the learner's practice methods. */
public class FunctionalProgrammingVerification {
    private static Method method(String owner, String name, Class<?>... args) throws Exception {
        Method method = Class.forName("com.instagram.backend.interview.functionalprogramming." + owner)
                .getDeclaredMethod(name, args);
        method.setAccessible(true);
        return method;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        Method pure = method("foundations.PureFunctionsSolution", "doubleNumbers", int[].class);
        Method behavior = method("functionsasvalues.FunctionsAsValuesSolution", "transform", int[].class, IntUnaryOperator.class);
        Method normalize = method("puretransformations.NormalizeWordsSolution", "normalizeWords", List.class);
        Method concatenate = method("arrayconcatenation.ArrayConcatenationSolution", "getConcatenation", int[].class);
        Method loop = method("arrayconcatenation.ArrayConcatenationSolution", "getConcatenationWithLoop", int[].class);
        Random random = new Random(20261017L);
        String[] choices = {"", "   ", " Alice ", "BOB", "Chloë", " I ", "İ"};
        for (int trial = 0; trial < 400; trial++) {
            int[] input = new int[random.nextInt(9)];
            for (int index = 0; index < input.length; index++) input[index] = random.nextInt(201) - 100;
            int[] original = input.clone();
            int[] doubled = new int[input.length];
            int[] increased = new int[input.length];
            int[] joined = new int[2 * input.length];
            for (int index = 0; index < input.length; index++) {
                doubled[index] = input[index] * 2;
                increased[index] = input[index] + 3;
                joined[index] = input[index];
                joined[index + input.length] = input[index];
            }
            int[] pureOutput = (int[]) pure.invoke(null, (Object) input);
            require(Arrays.equals(doubled, pureOutput), "pure values mismatch");
            require(Arrays.equals(doubled, (int[]) pure.invoke(null, (Object) input)), "repeat mismatch");
            require(Arrays.equals(increased, (int[]) behavior.invoke(null, input, (IntUnaryOperator) value -> value + 3)), "behavior mismatch");
            int[] streamOutput = (int[]) concatenate.invoke(null, (Object) input);
            require(Arrays.equals(joined, streamOutput), "concatenation mismatch");
            require(Arrays.equals(joined, (int[]) loop.invoke(null, (Object) input)), "loop mismatch");
            require(pureOutput != input && streamOutput != input, "output aliases input");
            if (input.length > 0) { pureOutput[0] = 999; streamOutput[0] = 999; }
            require(Arrays.equals(input, original), "source mutated");
            List<String> words = new ArrayList<>();
            int wordCount = random.nextInt(9);
            for (int index = 0; index < wordCount; index++) words.add(choices[random.nextInt(choices.length)]);
            List<String> before = new ArrayList<>(words);
            List<String> expected = new ArrayList<>();
            for (String word : words) {
                String trimmed = word.trim();
                if (!trimmed.isEmpty()) expected.add(trimmed.toLowerCase(Locale.ROOT));
            }
            List<String> actual = (List<String>) normalize.invoke(null, words);
            require(expected.equals(actual), "normalization/order mismatch");
            require(words.equals(before), "words mutated");
            try { actual.add("outside change"); throw new AssertionError("toList output must be unmodifiable"); }
            catch (UnsupportedOperationException expectedException) { /* documented contract */ }
        }
        System.out.println("Passed 400 cases per lesson: pure/repeatable values, callback choice, normalized order, stream/loop concatenation, input preservation, independent arrays and unmodifiable list results. Seed=20261017.");
    }
}
