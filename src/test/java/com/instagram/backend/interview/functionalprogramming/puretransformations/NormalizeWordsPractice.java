package com.instagram.backend.interview.functionalprogramming.puretransformations;

import java.util.ArrayList;
import java.util.List;

/**
 * Editable practice program for Functional Programming Micro-Lesson 01.
 */
public class NormalizeWordsPractice {

    public static void main(String[] args) {
        try {
            runChecks();
            System.out.println("All practice checks passed.");
        } catch (UnsupportedOperationException exception) {
            System.out.println("Practice program is ready.");
            System.out.println("Implement normalizeWords, then run this main method again.");
        }
    }

    static List<String> normalizeWords(List<String> words) {
        // TODO: Trim, remove blanks, lowercase, and preserve order without mutating words.
        throw new UnsupportedOperationException("Implement normalizeWords");
    }

    private static void runChecks() {
        List<String> input = new ArrayList<>(List.of(" Alice ", "", "BOB", "   ", "Chloë"));
        List<String> original = new ArrayList<>(input);

        assertEquals(List.of("alice", "bob", "chloë"), normalizeWords(input),
                "ordinary normalization");
        assertEquals(original, input, "input remains unchanged");
        assertEquals(List.of(), normalizeWords(List.of()), "empty input");
        assertEquals(List.of("already"), normalizeWords(List.of("already")),
                "already normalized");
    }

    private static void assertEquals(Object expected, Object actual, String scenario) {
        if (!expected.equals(actual)) {
            throw new AssertionError(
                    scenario + ": expected " + expected + " but received " + actual);
        }
    }
}
