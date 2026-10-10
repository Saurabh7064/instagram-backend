package com.instagram.backend.interview.functionalprogramming.puretransformations;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Runnable reference solution for Functional Programming Micro-Lesson 01.
 */
public class NormalizeWordsSolution {

    public static void main(String[] args) {
        List<String> input = new ArrayList<>(List.of(" Alice ", "", "BOB", "   ", "Chloë"));
        List<String> original = new ArrayList<>(input);

        assertEquals(List.of("alice", "bob", "chloë"), normalizeWords(input),
                "ordinary normalization");
        assertEquals(original, input, "input remains unchanged");
        assertEquals(List.of(), normalizeWords(List.of()), "empty input");
        assertEquals(List.of("already"), normalizeWords(List.of("already")),
                "already normalized");

        System.out.println("All reference-solution checks passed.");
    }

    static List<String> normalizeWords(List<String> words) {
        return words.stream()
                .map(String::trim)
                .filter(word -> !word.isEmpty())
                .map(word -> word.toLowerCase(Locale.ROOT))
                .toList();
    }

    private static void assertEquals(Object expected, Object actual, String scenario) {
        if (!expected.equals(actual)) {
            throw new AssertionError(
                    scenario + ": expected " + expected + " but received " + actual);
        }
    }
}
