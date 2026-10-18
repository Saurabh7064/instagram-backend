# Concept-First Functional Programming Verification — 2026-10-17

Artifacts: READY. Track inactive; all four lessons remain TODO. The learner has not yet demonstrated comprehension through these checks.

## Java compilation and execution

All nine functional-programming Java files compile with javac into a temporary directory. Four practice runners deliberately retain TODO methods; four reference runners and the optional cross-lesson verifier pass:

```text
FunctionalProgrammingVerification: Passed 400 cases per lesson: pure/repeatable values, callback choice, normalized order, stream/loop concatenation, input preservation, independent arrays and unmodifiable list results. Seed=20261017.
PureFunctionsSolution: All reference-solution checks passed.
PureFunctionsPractice: Practice ready: implement the TODO methods, then rerun main.
NormalizeWordsSolution: All reference-solution checks passed.
NormalizeWordsPractice: Practice program is ready.
Implement normalizeWords, then run this main method again.
FunctionsAsValuesPractice: Practice ready: implement the TODO methods, then rerun main.
FunctionsAsValuesSolution: All reference-solution checks passed.
ArrayConcatenationSolution: All reference-solution checks passed.
ArrayConcatenationPractice: Practice ready: implement the TODO methods, then rerun main.
```

The optional verifier compares scalar/array transformations, callback choices, and concatenation with simple independent expected results. It compares stream word normalization against a direct loop using the same specified trim/case rules. It checks original arrays/lists remain unchanged, array results are independent, repeated results match by value, and Stream.toList results reject mutation. Fixed random seed: 20261017; 400 cases per lesson. This validates artifacts, not learner understanding.

## Document contracts

Checked four question-first lessons with explicit outcomes, concepts before APIs, complete reference methods immediately after walkthroughs, adjacent named inputs/expected values, separately runnable practice links, 12 explained checkpoints, code fences, table columns, and local file links. The original first-lesson URL and normalization Java paths are preserved.

## UI integration and HTTP

The existing UI catalog generator discovers the three added Markdown lessons and re-reads the rewritten original. Production build passes with the existing large-chunk warning; no renderer/source behavior was changed. The UI checkout has no tracked changes.

`PROOF_DATE=2026-10-17 node tools/learning/verify-functional-lessons.mjs` passes all four lesson routes, narration checks, three expandable answers per lesson, practice links, adjacent reference inputs, and one-based reference code line numbers. It also checks a 390×844 viewport has no page overflow and records no browser page errors. [Machine-readable results](./functional-browser-checks.json).

Manual HTTP request: `curl --fail --silent --output /dev/null --write-out 'Functional learning UI HTTP %{http_code}\n' http://localhost:5173/learn`.

Observed: `Functional learning UI HTTP 200`. Browser checks additionally validate content at each actual lesson query URL. There is no new backend API to exercise in this learning-artifact change.

## Screenshot content review

All four screenshots were opened after final capture and visually checked. Narration and matching value tables are visible, calculations agree, and code/table content stays inside the lesson layout. Stream tables explicitly label conceptual values/source positions instead of claiming internal Java counters or intermediate lists.

- [Pure transformations](./functional-01-pure-transformations-trace.png)
- [Passing behavior](./functional-02-functions-as-values-trace.png)
- [Word transformations](./functional-03-stream-transformations-trace.png)
- [Array LeetCode bridge](./functional-04-array-concatenation-trace.png)

## Scope

This is a conceptual curriculum rewrite with new practice/reference learning programs. No application endpoint, database schema, or renderer implementation changed; tests cover the new Java artifacts and their integration into the existing learning UI. Existing learner mastery/active-lesson statuses are unchanged. Unrelated uncommitted Java and Kubernetes edits are excluded.
