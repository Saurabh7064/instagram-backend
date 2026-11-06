# Stack and Queue Artifact Verification — 2026-11-06

## Scope and status

Five future [Module 05 lessons](../../interview/01-leetcode/lessons/05-stack-queue/README.md) are prepared. Artifact READY; all learning TODO. Two Sum, pending checkpoints, learner implementation status and mastery counts are unchanged. No production API or renderer behavior changed.

## Java checks

JDK: OpenJDK 24.0.1. All 11 Java sources under [stackqueue](../../../src/test/java/com/instagram/backend/interview/leetcode/stackqueue/) compiled together into `/tmp/stack-queue-final-classes`. All five reference mains passed; the five practice mains reported their intentional TODO readiness, not implementation success. Exact output: [Java check log](./stack-queue-java-checks.txt).

The [independent model runner](../../../src/test/java/com/instagram/backend/interview/leetcode/stackqueue/StackQueueVerification.java) passed with seed `20261106`:

| Check | Cases/operations | Independent expected result |
|---|---:|---|
| Bracket strings, length 0 through 6 | 55,987 | Repeatedly remove adjacent matching pairs |
| Min Stack | 185,612 operations | Ordinary retained-value list and direct minimum scan |
| Temperature arrays | 1,000 | Scan later days until the first strictly warmer one |
| Window maxima | 6,695 | Compare every value in every complete window |
| RPN expressions | 2,001 | Build an expression tree with its value, then serialize tokens |

Checks include duplicate minima, both integer extremes, strict temperature equality, every valid width on generated arrays, input preservation, right/left operand order and negative division toward zero. Generated expressions avoid division by zero and stay within an exact bounded integer range. These checks exercise learning programs; a Spring Boot/Testcontainers run would not exercise these test-source algorithms or the Markdown and is not claimed.

## Catalog and browser integration

The backend working tree already has a staged documentation migration to `docs/learn`; the sibling UI generator still reads `docs/interview`. Neither migration nor UI source was committed by this change. A temporary copy at `/tmp/leetcode-preview/instagram-ui` uses the current UI source and installed dependencies, with only catalog roots adjusted to the working tree. Caches, generated catalog and build output stay in `/tmp`. This is a rendering check of authored lessons, not proof that the pending catalog migration is complete in the sibling repo.

Catalog generation and the TypeScript/Vite production build passed. Vite reports its existing large-bundle warning. The temporary preview runs at `http://127.0.0.1:5173`.

[Browser integration script](../../../tools/learning/verify-stack-queue-lessons.mjs) passed all five lesson routes: correct selected title; Question then What you will learn; all 15 expandable answers with explanations; existing Java/Markdown targets; stable one-based numbers on every multiline code block; full reference inputs/results; state tables; mobile catalog control; no document overflow at 390×844; no browser page errors. Exact results: [browser checks JSON](./stack-queue-browser-checks.json).

Command used: `PROOF_DATE=2026-11-06 node tools/learning/verify-stack-queue-lessons.mjs`. The script detects either the migrated or previously committed curriculum directory and supports `LEARNING_BASE_URL` and `UI_ROOT` overrides.

Manual HTTP verification used `curl -fsS -o /tmp/stack-queue-http.html -w 'Learning Studio HTTP %{http_code}\n' 'http://127.0.0.1:5173/learn?lesson=docs%2Flearn%2F01-leetcode%2Flessons%2F05-stack-queue%2F01-valid-parentheses%2FREADME'` and returned `Learning Studio HTTP 200`. Browser assertions confirm the selected lesson content; the HTTP response alone is only the SPA shell. No new backend API exists in this artifact-only change.

## Screenshot review

Each final PNG was opened and visually inspected after capture. Desktop proofs show the fixed input, actual narrated checks/mutations and adjacent collection state in the current reading layout. The mobile proof shows the correct Sliding Window Maximum question, wrapped heading and Browse lessons control without horizontal document overflow. Screenshots supplement the complete text and automated DOM checks rather than proving learner understanding.

- [Parentheses trace](./stack-queue-01-valid-parentheses-trace.png)
- [Min Stack trace](./stack-queue-02-min-stack-trace.png)
- [Temperature trace](./stack-queue-03-daily-temperatures-trace.png)
- [RPN trace](./stack-queue-04-evaluate-reverse-polish-notation-trace.png)
- [Window maximum trace](./stack-queue-05-sliding-window-maximum-trace.png)
- [Mobile proof](./stack-queue-mobile.png)

## Semantic review and commit scope

Artifact commit: `51964ce` — five lessons, Java runners, independent models and reviewed browser proof.

The fixed examples derive all outputs before optimized state; narration follows the actual reference operations, with full container mutations, exact comparisons, short circuits and termination. Complete reference blocks match runnable Java methods. Each lesson includes correctness/invariant reasoning, qualified complexity, relevant alternatives, pitfalls, separate editable practice and three explained checkpoints. No unsupported simulation or precomputed animation is introduced.

The isolated artifact commit uses the repository's previously committed curriculum paths, preserving the already-staged `docs/interview` → `docs/learn` move and extending that move to the new lessons. Only this module's additions and its navigation/tracking deltas are committed. Unrelated staged and unstaged work is excluded. Commit pointers are recorded in the [commit map](../../commit-map.md).
