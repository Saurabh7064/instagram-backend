package com.instagram.backend.interview.functionalprogramming.concepts;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/** Java 24 concept examples. Run main in IntelliJ; no learner implementation required. */
public class FunctionalConceptExamples {
    public static void main(String[] args) throws IOException {
        lesson01();
        lesson02();
        lesson03();
        lesson04();
        lesson05();
        lesson06();
        lesson07();
        lesson08();
        lesson09();
        lesson10();
        lesson11();
        lesson12();
        lesson13();
        lesson14();
        lesson15();
        lesson16();
        lesson17();
        lesson18();
        lesson19();
        lesson20();
        lesson21();
        lesson22();
        lesson23();
        lesson24();
        lesson25();
        lesson26();
        lesson27();
        lesson28();
        System.out.println("Passed 28 Java functional concept examples and contract checks.");
    }

    // 01: Functional thinking and pure functions
    static void lesson01() throws IOException {
        int input = 3;
        int result = input * 2;
        int sameResult = input * 2;
        expect(6, result); expect(6, sameResult); expect(3, input);
    }

    // 02: Lambda expressions
    static void lesson02() throws IOException {
        Function<Integer, Integer> doubleIt = value -> value * 2;
        int result = doubleIt.apply(3);
        expect(6, result); expect(0, doubleIt.apply(0));
    }

    // 03: Functional interfaces: Function and Predicate
    static void lesson03() throws IOException {
        Function<String, Integer> length = text -> text.length();
        Predicate<String> nonBlank = text -> !text.isBlank();
        int size = length.apply("java");
        boolean accepted = nonBlank.test("java");
        expect(4, size); expect(true, accepted); expect(false, nonBlank.test("   "));
    }

    // 04: Method references
    static void lesson04() throws IOException {
        Function<String, Integer> length = String::length;
        Function<String, Integer> parse = Integer::parseInt;
        int size = length.apply("java");
        int number = parse.apply("42");
        expect(4, size); expect(42, number);
    }

    // 05: Supplier, Consumer, and operator shapes
    static void lesson05() throws IOException {
        Supplier<String> defaultName = () -> "guest";
        StringBuilder buffer = new StringBuilder();
        Consumer<String> append = buffer::append;
        UnaryOperator<String> clean = String::trim;
        append.accept(clean.apply(" java "));
        String name = defaultName.get();
        expect("java", buffer.toString()); expect("guest", name);
    }

    // 06: What a stream is
    static void lesson06() throws IOException {
        List<String> words = List.of("java", "streams");
        List<String> result = words.stream().toList();
        expect(List.of("java", "streams"), result); expect(List.of("java", "streams"), words); expectUnsupported(() -> result.add("extra"));
    }

    // 07: filter: keep matching values
    static void lesson07() throws IOException {
        List<String> words = List.of("java", "go", "streams");
        List<String> result = words.stream()
                .filter(word -> word.length() > 3)
                .toList();
        expect(List.of("java", "streams"), result); expect(3, words.size());
    }

    // 08: map: transform one value into another
    static void lesson08() throws IOException {
        List<String> words = List.of("java", "go");
        List<Integer> lengths = words.stream()
                .map(String::length)
                .toList();
        expect(List.of(4, 2), lengths); expect(List.of("java", "go"), words);
    }

    // 09: flatMap: flatten nested results
    static void lesson09() throws IOException {
        List<List<String>> groups = List.of(
                List.of("java", "go"), List.of(), List.of("sql"));
        List<String> words = groups.stream()
                .flatMap(List::stream)
                .toList();
        expect(List.of("java", "go", "sql"), words); expect(List.of(List.of("java", "go"), List.of(), List.of("sql")), groups);
    }

    // 10: Lazy pipelines and one-use streams
    static void lesson10() throws IOException {
        List<String> source = List.of("java", "go");
        Stream<String> pending = source.stream()
                .filter(word -> word.length() > 3);
        List<String> result = pending.toList();
        long freshCount = source.stream().count();
        expect(List.of("java"), result); expect(2L, freshCount); expectIllegalState(() -> pending.count());
    }

    // 11: Terminal operations: questions and results
    static void lesson11() throws IOException {
        List<String> words = List.of("java", "go");
        long count = words.stream().count();
        boolean hasLongWord = words.stream().anyMatch(word -> word.length() > 3);
        String first = words.stream().findFirst().orElse("missing");
        expect(2L, count); expect(true, hasLongWord); expect("java", first); expect(true, Stream.<String>empty().allMatch(s -> s.length() > 3));
    }

    // 12: Optional: a result may be absent
    static void lesson12() throws IOException {
        Optional<String> found = Optional.of("java");
        int length = found.map(String::length).orElse(0);
        String fallback = Optional.<String>empty().orElseGet(() -> "guest");
        Optional<Integer> nestedResult = found.flatMap(word -> Optional.of(word.length()));
        expect(4, length); expect("guest", fallback); expect(Optional.of(4), nestedResult); expect(Optional.empty(), Optional.of("java").map(s -> null));
    }

    // 13: reduce: combine values into one result
    static void lesson13() throws IOException {
        List<Integer> values = List.of(2, 4, 6);
        int total = values.stream().reduce(0, Integer::sum);
        Optional<Integer> withoutIdentity = values.stream().reduce(Integer::sum);
        expect(12, total); expect(Optional.of(12), withoutIdentity); expect(0, Stream.<Integer>empty().reduce(0, Integer::sum));
    }

    // 14: collect and result ownership
    static void lesson14() throws IOException {
        List<String> words = List.of("java", "go");
        List<String> fixed = words.stream().toList();
        ArrayList<String> editable = words.stream()
                .collect(Collectors.toCollection(ArrayList::new));
        editable.add("sql");
        expect(List.of("java", "go"), fixed); expect(List.of("java", "go", "sql"), editable); expect(List.of("java", "go"), words); expectUnsupported(() -> fixed.add("extra"));
    }

    // 15: Grouping and partitioning
    static void lesson15() throws IOException {
        List<String> words = List.of("java", "go", "java");
        Map<Integer, List<String>> byLength = words.stream()
                .collect(Collectors.groupingBy(String::length));
        Map<Integer, Long> counts = words.stream()
                .collect(Collectors.groupingBy(String::length, Collectors.counting()));
        Map<Boolean, List<String>> split = words.stream()
                .collect(Collectors.partitioningBy(word -> word.length() > 3));
        expect(Map.of(4, List.of("java", "java"), 2, List.of("go")), byLength); expect(Map.of(4, 2L, 2, 1L), counts); expect(List.of("go"), split.get(false)); expect(2, Stream.<String>empty().collect(Collectors.partitioningBy(s -> true)).size());
    }

    // 16: toMap and duplicate-key policy
    static void lesson16() throws IOException {
        List<String> words = List.of("java", "go", "java");
        Map<String, Integer> lengths = words.stream().collect(Collectors.toMap(
                Function.identity(), String::length,
                (existing, incoming) -> existing, LinkedHashMap::new));
        expect(Map.of("java", 4, "go", 2), lengths); expect(List.of("java", "go"), new ArrayList<>(lengths.keySet())); expectIllegalState(() -> words.stream().collect(Collectors.toMap(Function.identity(), String::length)));
    }

    // 17: Ordering, Comparator, and distinct
    static void lesson17() throws IOException {
        List<String> words = List.of("sql", "go", "java", "go");
        List<String> result = words.stream()
                .distinct()
                .sorted(Comparator.comparingInt(String::length))
                .toList();
        expect(List.of("go", "sql", "java"), result); expect(4, words.size());
    }

    // 18: Primitive streams and arrays
    static void lesson18() throws IOException {
        int[] numbers = {2, 4, 6};
        int total = Arrays.stream(numbers).sum();
        List<Integer> boxed = Arrays.stream(numbers).boxed().toList();
        int letters = List.of("java", "go").stream().mapToInt(String::length).sum();
        long arrayObjects = Stream.of(numbers).count();
        expect(12, total); expect(List.of(2, 4, 6), boxed); expect(6, letters); expect(1L, arrayObjects); expect(false, IntStream.empty().average().isPresent());
    }

    // 19: Function composition
    static void lesson19() throws IOException {
        Function<Integer, Integer> doubleIt = value -> value * 2;
        Function<Integer, Integer> addThree = value -> value + 3;
        int after = doubleIt.andThen(addThree).apply(3);
        int before = doubleIt.compose(addThree).apply(3);
        expect(9, after); expect(12, before);
    }

    // 20: Captured variables and effectively final
    static void lesson20() throws IOException {
        int minimumLength = 4;
        Predicate<String> longEnough = word -> word.length() >= minimumLength;
        boolean accepted = longEnough.test("java");
        expect(true, accepted); expect(false, longEnough.test("go"));
    }

    // 21: Exceptions at functional boundaries
    static void lesson21() throws IOException {
        Function<String, Integer> parse = Integer::parseInt;
        int result = parse.apply("42");
        expect(42, result); expectNumberFormat(() -> parse.apply("invalid"));
    }

    // 22: Side effects, non-interference, and peek
    static void lesson22() throws IOException {
        List<String> words = List.of("java", "go");
        List<Integer> lengths = words.stream()
                .map(String::length)
                .toList();
        expect(List.of(4, 2), lengths); expect(List.of("java", "go"), words);
    }

    // 23: Parallel streams: suitability and limits
    static void lesson23() throws IOException {
        List<Integer> numbers = List.of(2, 4, 6);
        int sequential = numbers.stream().mapToInt(value -> value * 2).sum();
        int parallel = numbers.parallelStream().mapToInt(value -> value * 2).sum();
        expect(24, sequential); expect(24, parallel);
    }

    // 24: Streams that own resources
    static void lesson24() throws IOException {
        Path file = Files.createTempFile("functional-concept-", ".txt");
        List<String> lines;
        try {
            Files.write(file, List.of("java", "go"), StandardCharsets.UTF_8);
            try (Stream<String> opened = Files.lines(file, StandardCharsets.UTF_8)) {
                lines = opened.toList();
            }
        } finally {
            Files.deleteIfExists(file);
        }
        expect(List.of("java", "go"), lines); expect(false, Files.exists(file));
    }

    // 25: Bounded and prefix-based pipelines
    static void lesson25() throws IOException {
        List<Integer> numbers = IntStream.iterate(1, value -> value + 1)
                .limit(4).boxed().toList();
        List<Integer> prefix = numbers.stream().takeWhile(value -> value < 3).toList();
        int total = numbers.stream().mapToInt(Integer::intValue).sum();
        expect(List.of(1, 2, 3, 4), numbers); expect(List.of(1, 2), prefix); expect(10, total); expect(List.of(3, 4), numbers.stream().dropWhile(v -> v < 3).toList());
    }

    // 26: mapMulti: emit zero or more results
    static void lesson26() throws IOException {
        List<String> result = List.of("java", "go").stream()
                .<String>mapMulti((word, output) -> {
                    if (word.length() > 3) {
                        output.accept(word);
                        output.accept(word + "!");
                    }
                }).toList();
        expect(List.of("java", "java!"), result);
    }

    // 27: Gatherers: custom intermediate transformations
    static void lesson27() throws IOException {
        List<List<Integer>> windows = List.of(1, 2, 3, 4, 5).stream()
                .gather(Gatherers.windowFixed(2))
                .toList();
        expect(List.of(List.of(1, 2), List.of(3, 4), List.of(5)), windows);
    }

    // 28: Custom collector contracts
    static void lesson28() throws IOException {
        Collector<String, StringBuilder, String> joining = Collector.of(
                StringBuilder::new,
                (buffer, word) -> buffer.append(word),
                (left, right) -> left.append(right),
                StringBuilder::toString);
        String result = List.of("java", "go").stream().collect(joining);
        expect("javago", result); expect("javago", List.of("java", "go").parallelStream().collect(joining));
    }

    private static void expect(Object expected, Object actual) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError("Expected " + expected + " but got " + actual);
        }
    }

    private static void expectUnsupported(Runnable action) {
        try { action.run(); }
        catch (UnsupportedOperationException expected) { return; }
        throw new AssertionError("Expected UnsupportedOperationException");
    }

    private static void expectIllegalState(Runnable action) {
        try { action.run(); }
        catch (IllegalStateException expected) { return; }
        throw new AssertionError("Expected IllegalStateException");
    }

    private static void expectNumberFormat(Runnable action) {
        try { action.run(); }
        catch (NumberFormatException expected) { return; }
        throw new AssertionError("Expected NumberFormatException");
    }
}
