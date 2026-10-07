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

## Interview Coding Lesson Standard

For every lesson under `docs/interview/01-coding/`:

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
