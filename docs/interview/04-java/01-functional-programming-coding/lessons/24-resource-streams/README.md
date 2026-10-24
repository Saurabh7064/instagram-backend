# 24 — Streams that own resources

## Concept question

Why must a file-backed stream be closed?

## What you will learn

Distinguish in-memory sources from open resources and use try-with-resources.

**Time:** 10–15 minutes. **Prerequisite:** [23 — Parallel streams: suitability and limits](../23-parallel-streams/README.md).
**Artifact:** READY after recorded verification. **Understanding:** TODO; no learner attempt recorded.

## Understand the concept

Some streams read an external resource rather than an ordinary in-memory list. Files.lines opens a file-backed stream, whose lifetime must be closed. Reaching a terminal operation does not replace the documented requirement to close it. A stream from a normal collection usually has no such external resource to release.

Try-with-resources binds a resource to a block and closes it when the block exits, including exceptional exits. Perform the file processing inside that block; returning the open Stream would move its lifetime problem to another owner, while returning an already closed Stream would be unusable. Returning the materialized result is often a simpler boundary.

Our example creates an isolated temporary file with lines java and go. Reading those two lines and gathering them produces [java, go]. The finally block removes only that temporary file. This illustrates ownership and cleanup; the temporary path and file-writing work are not required for ordinary collection pipelines.

## Small Java example

The snippet below is included in [FunctionalConceptExamples.java](../../../../../../src/test/java/com/instagram/backend/interview/functionalprogramming/concepts/FunctionalConceptExamples.java#L254), method `lesson24`. Run that class's `main` in IntelliJ for checked examples. The file supplies the needed imports; no coding exercise is required.

```java
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
```

**Expected result:** lines = [java, go]; the opened file stream is closed and the temporary file is removed.

## Read the syntax and result

Files.write creates the two text lines, Files.lines exposes them, and toList gathers their String values while the stream is open. The resource block closes opened before finally deletes the isolated file. IOException is declared by the runnable example method.

## Pitfalls and decisions

Closing a file stream is different from deleting its file. Do not use broad cleanup paths. Avoid leaking a Files.lines stream out of a service method without a clearly documented owner responsible for closing it.

Official reference: [Java 24 API or language contract](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/nio/file/Files.html).

## Questions and explained answers

<details>
<summary>1. Does toList alone discharge the close requirement?</summary>

No. File-backed stream ownership still requires closing according to the API contract.

</details>

<details>
<summary>2. What does try-with-resources guarantee here?</summary>

It closes opened when its block exits, including when processing throws.

</details>

<details>
<summary>3. Must every list stream be closed to release a file?</summary>

No. A normal collection source has no opened file resource. The source and API contract determine the responsibility.

</details>

## Stop/go

Explain the concept question in your own words, predict this example's result, and answer the questions before opening their answers. A coding attempt is optional. Record your explanation in [learner progress](../../learner-progress.md); compiled examples do not demonstrate comprehension.

**Next:** [25 — Bounded and prefix-based pipelines](../25-bounded-and-multi-output-streams/README.md).
