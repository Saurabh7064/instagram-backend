package com.instagram.backend.interview.functionalprogramming.concepts;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/** Optional exercises. Run main in IntelliJ; implementation is not a concept-learning prerequisite. */
public class FunctionalConceptPractice {
    public static void main(String[] args) {
        try {
            require(6, applyToThree(value -> value * 2));
            require(List.of(4, 2), lengths(List.of("java", "go")));
            require(List.of(), lengths(List.of()));
            require(List.of("java", "go", "sql"), flatten(
                    List.of(List.of("java", "go"), List.of(), List.of("sql"))));
            require(List.of(), flatten(List.of()));
            System.out.println("Optional practice checks passed. Concept understanding is assessed separately.");
        } catch (UnsupportedOperationException unfinished) {
            if (!"TODO".equals(unfinished.getMessage())) throw unfinished;
            System.out.println("Optional practice is unfinished; no implementation pass is claimed.");
        }
    }

    // OPTIONAL TODO: invoke the supplied Function with integer 3.
    static int applyToThree(Function<Integer, Integer> operation) {
        throw new UnsupportedOperationException("TODO");
    }

    // OPTIONAL TODO: return word lengths using map; preserve the non-null source and values.
    static List<Integer> lengths(List<String> words) {
        throw new UnsupportedOperationException("TODO");
    }

    // OPTIONAL TODO: return the words in encounter order using flatMap; inputs are non-null.
    static List<String> flatten(List<List<String>> groups) {
        throw new UnsupportedOperationException("TODO");
    }

    private static void require(Object expected, Object actual) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError("Expected " + expected + " but got " + actual);
        }
    }
}
