# 19 — Java Functional Programming Concept Course

## 1) What I built

Recreated the [functional-programming path](../interview/04-java/01-functional-programming-coding/README.md) around 28 Java concepts with small runnable examples, result derivations, pitfalls, and 84 expandable explained questions. Iteration/debugger narration and mandatory coding/LeetCode progression were removed from the active course. Old URLs remain usable, and earlier Java exercises remain optional.

## 2) Concepts prepared

The path covers functional thinking, lambdas, functional interfaces, method references, Supplier/Consumer, streams, filter, map, flatMap, laziness/reuse, terminal operations, Optional, reduce, collectors and ownership, grouping/partitioning, toMap, ordering, primitives, composition, capture, exceptions, side effects, parallelism, resource streams, bounds/prefixes, mapMulti, Gatherers, and custom collectors. These are authored topics, not learner mastery claims.

## 3) Why this design

The learner wants to understand Java concepts and API differences without algorithm-style execution traces. Each part teaches the meaning and use case, shows a small example and expected result, explains the syntax, and checks understanding. Optional practice is separate. The preference is saved for future Java language/concept lessons in [AGENTS.md](../../AGENTS.md#functional-programming-lesson-standard), with an explicit exception to the general algorithm trace rules.

## 4) Implementation notes

- Twenty-eight authored parts are listed in the [coverage index](../interview/04-java/01-functional-programming-coding/concept-index.json) and [backlog](../interview/04-java/01-functional-programming-coding/learning-backlog.md).
- [Learner progress](../interview/04-java/01-functional-programming-coding/learner-progress.md) starts entirely TODO; no active lesson, coding completion, or understanding is inferred.
- Exact lesson snippets are compiled and checked in one named-package Java class, directly runnable in IntelliJ. Optional TODO tasks live in another class.
- Updated the existing browser verifier to check concept presentation, exact examples, answer expansion, navigation, compatibility routes, and mobile layout.
- Production endpoints, API contracts, renderer code, and generated-catalog ownership are unchanged. The ignored UI catalog is regenerated from Markdown.

## 5) Code pointers

- [FunctionalConceptExamples.java](../../src/test/java/com/instagram/backend/interview/functionalprogramming/concepts/FunctionalConceptExamples.java).
- [FunctionalConceptPractice.java](../../src/test/java/com/instagram/backend/interview/functionalprogramming/concepts/FunctionalConceptPractice.java).
- [Browser integration verifier](../../tools/learning/verify-functional-lessons.mjs).
- [Course entry](../interview/04-java/01-functional-programming-coding/README.md), [map](../interview/04-java/01-functional-programming-coding/lessons/08-map/README.md), [flatMap](../interview/04-java/01-functional-programming-coding/lessons/09-flatmap/README.md).
- Existing project mapping: [PostService.java](../../src/main/java/com/instagram/backend/service/PostService.java) uses map/toList to turn posts into responses.

## 6) Commit pointers and verification

The [verification note](../interview/04-java/01-functional-programming-coding/verification.md) records Java compilation/output, browser checks, HTTP serving, screenshot proof, and scope. The [commit map](../commit-map.md#java-functional-concept-course--2026-10-24) records the artifact hash after commit creation.

## 7) Pitfalls and fixes

- Existing generic trace instructions would reintroduce the unwanted format. The saved Java concept exception overrides them while retaining the separate algorithm standards.
- map, flatMap, and Map can be confused. Dedicated lessons compare their type/result shapes.
- Stream optimization makes callback-count teaching unreliable. Explain observable results without promising a fixed internal schedule.
- New APIs differ by Java release. The course states version requirements and compiles the exact examples with Java 24.

## 8) Next improvements

- [ ] Teach one selected concept and record the learner's explanation/prediction/checkpoint attempt.
- [ ] Use optional exercises only if the learner wants coding practice.
- [ ] Revisit concepts later to assess retention independently of artifact readiness.
