# Project Workflow Rules

## Definition Of Done For Any Feature

For every new feature, refactor, or bug fix that changes behavior:

1. Update code and tests.
2. Add/update at least one integration test.
3. Verify backend API via `curl` or `.http` request file.
4. If UI + backend flow changed, verify demo in browser (`http://localhost:5173`).
5. Capture screenshot proof of the browser demo and save under `docs/demo-proofs/<YYYY-MM-DD>/`.
6. Verify screenshot content after capture before marking feature done.
7. Update or create a feature note in `docs/features/`.
8. Add exact code pointers (file paths).
9. Add commit pointers (hashes) when available.
10. Create at least one git commit for that feature/learning change.
11. Push completed commits to the configured `origin` remote after generating or changing code or learning artifacts. Never include unrelated uncommitted work; if authentication, network access, or remote divergence prevents the push, report it explicitly.
12. Append the commit in `docs/commit-map.md` with feature and concept mapping.
13. Update backlog trackers:
   - feature status in `docs/features/learning-backlog.md`
   - concept status in `docs/system-design/learning-backlog.md`

If a change does not affect behavior (for example formatting-only), diary update is optional.

## Command Protocol

Use these four commands as workflow triggers:

1. `next feature`
2. `next concept`
3. `next kubernetes`
4. `next devops`

### `next feature` behavior

Before starting:
1. Verify no previous feature is left `IN_PROGRESS` in `docs/features/learning-backlog.md`.
2. Verify docs + commit map are updated for the last completed change.
3. Verify both repos have clean git working trees.

Then:
1. Pick the next `TODO` feature from `docs/features/learning-backlog.md` (top-down unless user overrides).
2. Mark it `IN_PROGRESS`.
3. Build it end-to-end.
4. Mark it `DONE`.
5. Update feature-docs and `docs/commit-map.md`.
6. Commit all related changes.
7. Verify test evidence is present:
   - integration test run output
   - `curl`/`.http` API verification
   - browser demo proof screenshots in `docs/demo-proofs/<YYYY-MM-DD>/`

### `next concept` behavior

Before starting:
1. Verify no previous concept is left `IN_PROGRESS` in `docs/system-design/learning-backlog.md`.
2. Verify docs + commit map are updated for the last completed concept.
3. Verify both repos have clean git working trees.

Then:
1. Pick next `TODO` concept from `docs/system-design/learning-backlog.md` (top-down unless user overrides).
   - Use `docs/system-design/mastery-roadmap.md` as the concept priority reference.
2. Mark it `IN_PROGRESS`.
3. Add/expand concept note with project mapping.
4. Add at least one concrete implementation task linked to a feature.
5. Mark concept `DONE` (or keep `IN_PROGRESS` if implementation is intentionally deferred).
6. Update `docs/commit-map.md`.
7. Commit all related changes.

### `next kubernetes` behavior

Before starting:
1. Verify no previous item is left `IN_PROGRESS` in `docs/kubernetes/learning-backlog.md`.
2. Verify the previous item's note and `docs/commit-map.md` are updated.
3. Verify both repos have clean git working trees.

Then:
1. Pick the next `TODO` item from `docs/kubernetes/learning-backlog.md` (top-down unless user overrides).
2. Mark it `IN_PROGRESS`.
3. Use `docs/kubernetes/mastery-roadmap.md` and `docs/kubernetes/12-week-execution-plan.md` as guidance.
4. Implement the project exercise and perform its required success and failure verification.
5. Add or update a module landing page using `docs/kubernetes/_lesson-template.md` and micro-lessons using `docs/kubernetes/_micro-lesson-template.md`.
   - Treat each backlog item as a module, not one continuous chapter. Split it into named 10–15 minute micro-lessons in prerequisite order.
   - Assume no prior Kubernetes or container knowledge. State prerequisites and briefly teach any missing prerequisite one level earlier. Never use an unfamiliar term before defining it.
   - Give each micro-lesson one primary idea, one concrete problem, and normally no more than 3–5 new terms. Preserve depth by adding micro-lessons instead of packing concepts together.
   - Label content `Must understand`, `Useful later`, or `Optional deep dive`. The core path must be understandable without optional sections.
   - Explain commands only after teaching the idea they demonstrate. Show the prediction, command or action, expected observation, and why it occurs.
   - Put 3–6 checkpoint questions immediately after the concept they test. Make each question the summary of its own expandable `<details>` block, with its explained answer inside. Include at least 15 questions across the whole module and never omit an answer.
   - End each micro-lesson with observable stop/go criteria. End the module with a recap, vocabulary cheat sheet, integrated exercise, controlled failure/recovery, and teach-it-back prompt.
   - In chat, teach only one micro-lesson at a time. Ask 2–3 checkpoint questions and wait for the learner's attempt before advancing. If the learner is confused, step back one prerequisite level and introduce no additional terminology.
   - Track comprehension separately in `docs/kubernetes/learner-progress.md`. Never mark a micro-lesson `UNDERSTOOD` merely because its code or lab is complete; require the learner to explain the idea and answer its checkpoint.
6. Mark the item `DONE` only when its evidence is complete.
7. Update `docs/commit-map.md` and commit all related changes.

### `next devops` behavior

Use this command for the integrated Kubernetes + Terraform + AWS + DevOps path. Do not advance the standalone Kubernetes calendar separately when the learner is following this path; the integrated backlog decides the order.

Before starting:
1. Verify no previous item is left `IN_PROGRESS` in `docs/devops/learning-backlog.md`.
2. Verify the previous item's note, learner-progress entry, and `docs/commit-map.md` are current.
3. Verify both repos have clean git working trees.
4. If the milestone can change AWS resources, verify the intended AWS account and region, the budget/anomaly-alert setup, expected cost class, resource tags, and teardown plan before doing anything billable.

Then:
1. Pick the next `TODO` milestone from `docs/devops/learning-backlog.md` (top-down unless the user overrides).
2. Mark it `IN_PROGRESS`.
3. Use `docs/devops/integrated-roadmap.md`, `docs/devops/20-stage-execution-plan.md`, `docs/devops/toolchain.md`, and `docs/devops/cloud-safety.md` as guidance.
4. Split the milestone into prerequisite-ordered 10–15 minute micro-lessons. Reuse the teaching structure in `docs/kubernetes/_micro-lesson-template.md`:
   - assume no prior Kubernetes, Terraform, AWS, networking, IAM, YAML, or Linux knowledge unless it was demonstrated;
   - teach one primary idea and at most one small supporting connection at a time;
   - explain in simple terms, start with the problem, define every new term, and label optional depth;
   - use prediction -> action -> observation -> explanation;
   - include 3–6 checkpoint questions per micro-lesson, with each answer and explanation inside that question's expandable `<details>` block;
   - teach one micro-lesson at a time in chat and stop for the learner's attempt.
5. Prefer the local kind cluster and local Terraform exercises. For an AWS milestone:
   - use short-lived IAM role or IAM Identity Center credentials, never committed/static access keys;
   - run formatting, validation, lint/security checks, and show a reviewed `terraform plan` first;
   - list every potentially billable resource and obtain explicit user confirmation in the current turn before `terraform apply` creates paid resources;
   - never destroy cloud resources without explicit confirmation and a data/backup review;
   - use immutable image tags/digests, least privilege, and a documented same-session cleanup path;
   - independently verify teardown and record any remaining billable resources.
6. Implement the project exercise and perform required success, controlled-failure, recovery, cost, and teardown verification.
7. Keep tool ownership separate: Terraform owns AWS infrastructure; GitHub Actions owns CI; Argo CD owns Git-based application delivery; Kubernetes owns workload reconciliation. Do not make Terraform and Argo CD manage the same application objects.
8. Mark the project milestone `DONE` only when its artifact and evidence are complete. Track comprehension separately in `docs/devops/learner-progress.md`; never infer understanding from a successful lab. Require a plain-language explanation, a correct prediction, and a diagnosed failure before `DEMONSTRATED`, then recheck later before `RETAINED`.
9. Update `docs/commit-map.md` and commit all related changes.

## Test Stack Standard

Use this stack by default:
- Backend integration: Spring Boot Test + Testcontainers (PostgreSQL) + JUnit 5 + MockMvc
- UI E2E: Playwright
- API manual verification: `curl` and/or `http/*.http` files

Reference:
- `docs/testing-strategy.md`

## Feature Doc Rules

- Reuse `docs/features/_feature-template.md` for new entries.
- Keep each entry practical:
  - what was built
  - concepts learned
  - implementation notes
  - pitfalls/fixes
  - next improvements
- For all code references, use clickable Markdown file links (not plain text paths).

## LeetCode Lesson Standard

For every lesson under `docs/interview/01-leetcode/`:

- Put `## Question` immediately after the lesson title. Ask the exact problem there; do not place status metadata, setup instructions, or an answer before it.
- Follow it with `## What you will learn`, containing concrete concept outcomes. After that, teach the concepts and reasoning; treat the coding problem as the exercise that demonstrates the lesson, not as the lesson itself.
- Put runnable practice instructions after the learner has been given the conceptual model needed to make a meaningful attempt.

1. Organize material as module -> micro-lesson. A module landing page may list the sequence, but each micro-lesson directory must teach one primary idea through one focused problem.
2. Introduce no more than three new terms in a micro-lesson. If a second pattern, transformation, or unrelated Java pitfall appears, move it to another micro-lesson.
3. In chat and in the active daily plan, teach only one micro-lesson at a time. Keep later lessons `TODO` until the learner completes the current stop/go check.
4. Include the complete problem statement, examples, constraints or explicit assumptions, and expected complexity when applicable.
5. Provide a clickable link beside the problem to an editable Java practice file under `src/test/java/com/instagram/backend/interview/`; never place runnable Java only under `docs/`, because IntelliJ does not treat that directory as a Java source root.
6. The practice file must contain a runnable `main` method, sample/edge-case checks, and a clearly marked method where the learner writes the solution.
7. Provide a separate clickable reference-solution file so the learner can attempt the problem without accidentally reading the answer.
8. Make both files directly runnable from IntelliJ. Include terminal compile/run commands only when the learner asks for them or they add non-redundant value; do not add routine `Run from Terminal` sections.
9. Explain the reference approach, invariant, correctness reasoning, time complexity, space complexity, pitfalls, and reasonable alternatives that directly relate to the one primary idea.
10. Include 2–3 checkpoint questions with explained answers and observable stop/go criteria.
11. Compile both Java files and run the reference solution before marking the lesson artifact complete.
12. Keep learning status separate from artifact completeness. A working reference solution does not mean the learner has mastered the problem.
13. Keep the practice and reference classes in a named package matching their `src/test/java` directory so IntelliJ can run and debug each `main` method directly without adding production code to the application artifact.
14. When the learner completes their own practice solution, run its checks and update that lesson's README with a clickable solution link, the submitted approach, correctness result, complexity, edge-case coverage, and specific issues or refinements. Preserve the learner's approach unless they ask for a rewrite.
15. Record code completion separately from comprehension and mastery. Passing checks completes the implementation attempt, but checkpoint answers and a plain-language invariant/complexity explanation are still required before advancing the learning status.
16. Explain every non-obvious line, guard, boundary check, or API call with: what it evaluates, why it is needed, a concrete input and result, what fails without it, and any equivalent clearer form. For arithmetic guards, show the exact overflow or underflow value and explain Java's left-to-right short-circuit behavior.
17. Every lesson containing reference-method code must include a `## Block-by-block code walkthrough` immediately before or after that reference code. Cover initialization, loop conditions, guards, transformations, API calls, branches, state or pointer updates, and the terminal return or exception. For every block, explicitly state what it evaluates, why it is needed, a concrete input and result, what fails without it, and an equivalent clearer form when one exists. Treat the lesson artifact as incomplete if any reference block is unexplained.
18. Do not list an example output without showing how it was derived. For every distinct calculation or transformation introduced by a lesson, include at least one worked example with named intermediate values and exact arithmetic or state changes. When several listed examples behave differently, briefly derive each one. Add a visual only when it is accurate and materially clearer than the worked calculation; do not force an ASCII diagram. Do not assume the learner can infer an output from the final answer alone.
19. Teach the answer-finding logic in prerequisite order before showing a trace or reference code. Never begin with “why use X?” when `X` has not been introduced. First state the action in learner language, then name and define the term, demonstrate how it is initialized or chosen, explain its purpose, and only then justify why the approach works. Prefer numbered reasoning steps such as choose → calculate → compare → move → record.
20. When a changing algorithm state is materially easier to understand visually, add a trusted `:::simulation` directive using a supported adapter. Derive steps from the directive input rather than hardcoding the displayed answer, keep backward/forward navigation deterministic, and use at least seven array elements when that produces a useful trace. Do not force a simulation into a lesson that is clearer as prose or a worked table.
21. Place the simulation only after the learner has the minimum conceptual model needed to interpret it. The surrounding lesson must still explain the dry run, conditions, intermediate state, mistakes, complexity, and every non-obvious code block; the interactive view supplements rather than replaces those explanations.
22. Every lesson must show its complete reference method in a clearly named reference-solution section and link the directly runnable reference file. In the learning UI, every multiline Markdown code block must display stable one-based line numbers beside the code; keep those numbers out of the Markdown text and Java source so copying code remains clean.

## Coding Explanation and Debugger Trace Standard

For LeetCode and algorithm-problem explanations, use the detailed character-replacement trace as the baseline. Java language/concept and functional-programming lessons follow the concept-first exception below; do not add iteration/debugger traces to those lessons unless the learner explicitly requests them:

- Teach concepts in connected plain-language prose before the debugger: purpose/problem → meaning of each needed saved value → one concrete calculation with the lesson's fixed input → general rule/formula → why each term or boundary adjustment is needed. Introduce the action in learner language before naming the pattern or library. Use the Range Sum explanation as the model: define prefix[k] as the sum of the first k numbers, derive a concrete total and a wanted range, subtract the unwanted earlier part, then explain prefix[right+1]-prefix[left] and why +1 includes right. Distinguish input positions from saved-state indexes and returned values. Adapt this reasoning to each algorithm rather than repeating a generic template; preserve important limits and tradeoffs. Concept prose explains why; the subsequent concise execution bullets and adjacent state tables explain what runs. Retain all existing fixed-example, condition-prefix, collection-state, aligned-input and non-duplication rules.
- Before introducing implementation-state names, maps, or an optimized formula, show the fixed input, state exactly what output is requested, and derive that output in learner language. For counting problems, enumerate the matching positions/ranges and their arithmetic (or an equally complete grouped derivation), then show how their contributions sum to the final count. Do not explain two matches and then jump to an unexplained final total. Only after the learner sees where the answer comes from, explain the saved-state meanings and faster method that finds the same answer. Keep this conceptual example distinct from the subsequent code-execution bullets.
- State the exact input, index-to-value mapping when useful, and initial values before tracing.
- Put ✅ or ❌ immediately before the evaluated condition or control-flow keyword in coding narration and debugger tables: `✅ if (candidateLength < bestLength): 6 < 2147483647 → true` and `❌ while (formedGroups == requiredGroups): 2 == 3 → false`. Do not put the emoji after true/false; retain those words in the result. Apply this to if/while/for guards, comparisons, and each evaluated operand. The marker before a compound if shows the whole condition result, even when an earlier operand is true. A short-circuited or otherwise unexecuted expression stays `Skipped` without a check/cross. Keep these annotations out of executable Java and literal worked inputs.
- Apply the same code-to-state pattern to every narrated operation and to any operation retained in a debugger cell: actual statement or condition → current values/result → executed mutation or skipped branch → next operation. This applies to reads, collection updates, pointer changes, nested conditions, and loop rechecks as well as best-answer comparisons. Do not trail off into unexplained labels such as `remove A@0`, `left=1`, `required=true`, `below required=true`, or `formed=2`. Name the original code and show each affected value before → after; evaluate compound conditions left to right, marking short-circuited expressions as skipped. Use concise execution bullets; retain useful debugger columns and remove columns that merely repeat those bullets.
- Use the [Minimum Window bullet walkthrough](./docs/interview/01-leetcode/lessons/03-sliding-window/05-minimum-window-substring/README.md#block-by-block-code-walkthrough) as the narration baseline. At right=5, the removal bullets must show `char outgoing = source.charAt(left)` reading A at index 0, `windowCounts.merge` changing A 1 → 0, `left++` 0 → 1, both true removal-if operands, `formedGroups--` 3 → 2, and the false while recheck. Preserve the code order and do not replace it with unexplained aliases.
- Name the actual condition instead of unexplained boolean shorthand: write `✅ if (candidateLength < bestLength): 6 < 2147483647 → true`, rather than `shorter=true`. Use the current values at that exact check in both narration and debugger cells. Immediately show the assignments executed by a true branch, or name the skipped assignments and unchanged values for a false branch, before continuing to the next operation.
- Label each per-iteration debugger walkthrough explicitly with **Narrated execution** before its first iteration. State the fixed input and initial values beside that label; the existence of iteration bullets alone does not replace this visible introduction.
- Write every narrated execution as concise unordered bullets, not a dense paragraph. Use one actual statement/API or a closely related check-and-action per bullet, with exact operands/results and before → after changes. Show guards, reads, mutations, each repeated nested-loop check/removal, executed/skipped branches, and next action or return in code order. End an iteration with its scalar state and full collection state (or its adjacent existing collection table), then put the original debugger table immediately after the list. Keep true/false and prefix condition markers; do not shorten by dropping state, using vague aliases, or creating a long numbered script.
- Keep one fixed worked input throughout a lesson’s conceptual explanation, code walkthrough, narrated debugging, value tables, and related checkpoints. Do not switch source, target, array, or budget to introduce a counterexample midway, even if labeled separate. Explain using another state from the same execution or state the general rule without inventing another input. Keep additional edge-case checks in the runnable test programs rather than mixing them into the running explanation.
- Follow the actual reference code in execution order; show every algorithm variable, condition result, pointer movement, and returned value with exact arithmetic.
- Narrate the actual execution in concise learner-language bullets; tables alone are insufficient. Start with the named input values, evaluate the initial guard with exact values, say which body is skipped or executed and why, then describe each allocation and what it stores. For every iteration, explain the operations in execution order (read -> choose cell/key -> calculate -> mutate -> evaluate each condition -> execute/skip branch -> compare -> advance/return). Put that iteration's value table immediately after its bullet list, including collection state before and after mutations. Repeat the explanation for every iteration rather than expecting the learner to infer later iterations from an earlier one. Include the terminating false check or explain why an early return prevents it. Use separate concise bullets for long loop bodies; do not merely recite column names or provide a table with no explanation of the actions.
- By default, make the main debugger table one complete loop iteration per row, read left to right in execution order: incoming value/state -> each actual condition with substituted values and true/false -> branch taken or skipped -> collection/pointer updates -> comparison/result -> next action (continue, exit, or return). For nested loops, put every repeated check/removal and the final false check in the execution bullets; the table retains the resulting state and useful concise checks without repeating that script. Distinguish an evaluated false condition from a skipped expression (short circuit or early return). Use actual loop indexes rather than discontinuous artificial step numbers. Keep full collection tables adjacent and align them by the same iteration and phase; show before/after phases when one iteration mutates a collection more than once. Do not leave a main iteration row at “not checked yet” when the condition is evaluated later in that same iteration. Preserve block-local explanation and mutation tables as supporting detail.
- Keep debugger tables focused on useful input/index context, concise condition results, unchanged/final values, full collection state, and next action. When the adjacent bullets already explain every check/branch/removal, remove any column that repeats that execution script; do not copy the same narration into a large table cell. Preserve the other useful columns and values. When a value changes more than once within an iteration and the existing columns hide those intermediate updates, put its ordered change path in the same existing cell (for example formedGroups: 2 → 3 → 2, ending at the final value). Do not replace the original table with a changes-only summary or expand every operation into another phase row. Keep the narration concise and do not duplicate paths when separate before/after columns already show them.
- When a reference solution uses indexes, put a zero-based index row directly above its aligned input-value row in comments at the top of that same reference code block. Include every indexed string/array argument, preserve spaces explicitly, align multi-digit indexes and negative/multi-digit values, and keep exact named inputs, scalar arguments and expected return alongside the guide. For sorted/copied working data, show its own index/value rows and say which array the pointers index. For intervals, distinguish the interval row index from endpoint indexes [0]=start and [1]=end. Never confuse these input indexes with the UI code-line numbers or returned one-based indexes. Retain all existing concise-bullet, condition-prefix, full-state and non-duplication rules.
- Keep the exact worked-example inputs and expected return immediately with every complete reference listing (including alternatives), preferably as comments at the top of the same code block. Name every argument, include array/string values and budgets/targets, and distinguish returned values from final working state. The learner must not need to scroll back to find the input.
- Interleave explanation and debugging: show one small code block, explain what it does and why, then immediately show the variable/collection table that block produces before introducing the next block. Teach guards before allocations, show frequency-building iterations beside the counting loop, and explain insertion, removal, comparison, and return beside their own code. Never place a detached full debugger trace before or after a separate explanation and expect the learner to cross-reference them. Keep the complete reference listing after this integrated walkthrough; any additional summary trace is optional.
- Show arrays, frequency tables, sets, maps, and result collections after each mutation. Use a separate table when the combined trace is too wide; never omit collection state merely to shorten the explanation. For fixed frequency arrays, label the character-to-index mapping and show all cells.
- Distinguish state before a check from state after a mutation, and explain historical values, unchanged locals, variable scope, early returns, loop termination, and implementation-dependent collection order.
- Derive the answer from the trace and check it against the runnable reference. Keep the conceptual explanation before the trace and keep learning/mastery status independent of artifact completion.
- Apply this to future lessons when created; a roadmap-only question stays TODO until activated.

## Functional Programming Lesson Standard

- Persistent learner preference (2026-10-24): Java language/concept and functional-programming lessons teach what concepts mean, why they exist, how to use them, and their important differences. Do not include per-iteration narration, debugger tables, per-callback execution scripts, mandatory algorithm invariants, or forced LeetCode exercises unless explicitly requested. This exception overrides the general coding-trace standard for these concept lessons; algorithm/LeetCode lessons retain their existing detailed-trace rules.
- Explain concept → concrete use case → small Java example → output with a short derivation → API/type/syntax meaning → pitfalls/tradeoffs → checkpoint questions. Use concise input/output or type-shape comparisons when useful, without narrating every element or loop step.
- Cover prerequisites before use: functional thinking, lambdas, functional interfaces, method references, stream model, filter, map, flatMap, laziness, terminal operations, Optional, reduction, collectors, grouping, maps, ordering, primitive streams, composition, capture, exceptions, side effects, parallelism, and resource handling. Maintain a coverage map so advanced topics do not remain vague backlog labels.
- Keep one primary idea per micro-lesson, normally no more than three new terms. Split broader subjects into focused parts and compare easily confused concepts explicitly, especially map versus flatMap, intermediate versus terminal, collection versus stream, reduce versus collect, and sequential versus parallel.
- Use named-package Java examples runnable in IntelliJ. Runnable examples support concept learning; implementation exercises are optional, with separate practice/reference files when offered. Compile examples and verify their stated outputs; do not require a coding attempt to demonstrate a Java concept unless requested.
- Explain input ownership, result modifiability, null/empty behavior, callback purity, and relevant performance limits where they matter. Do not assume a stream creates intermediate lists or guarantees callback counts/order. Prefer a loop when it expresses evolving state more clearly.
- Keep artifact readiness separate from comprehension. Teach one micro-lesson at a time in chat, ask 2–3 checkpoint questions, and wait for the learner's explanation/prediction before advancing. Never infer mastery from compiled examples or completed documentation.

## System Design Interview Answer Standard

For every question-answer file under `docs/interview/02-system-design/questions/`:

1. Keep one primary interview question per file and display the exact normalized question near the top.
2. Start with why the problem exists before naming patterns or products.
3. Use one accurate analogy and explicitly state where the analogy stops matching a distributed system.
4. Define unfamiliar terms before using them; split overloaded answers into focused sections instead of presenting a buzzword list.
5. Trace at least one concrete request, event, or failure step by step and show the observable result.
6. Map the answer to this Instagram backend with clickable code links when a real mapping exists; label proposed components rather than pretending they are implemented.
7. Explain when to choose each major option, when not to choose it, and the cost or new failure mode introduced.
8. Include a failure drill covering prediction, symptom/detection, containment, recovery, and prevention.
9. Include a concise 2–3 minute interview-ready answer after the detailed explanation.
10. End the file with a `Questions and explained answers` section containing 3–5 expandable `<details>` questions. Put the explained answer inside each question's block and place no lesson content after this section.
11. Keep artifact readiness separate from comprehension. Mark an answer practiced only after the learner explains it without notes, predicts a failure, and answers the final questions before revealing them.
12. When component order, request ownership, or a failure sequence is materially clearer as a flow, add a trusted `request-flow` simulation before the final questions. Do not use an array animation for Spring Boot, API, JPA, or architecture material, and do not let the visualization replace the required concrete trace or failure drill.
