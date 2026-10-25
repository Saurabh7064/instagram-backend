# 21 — Curated Senior Java Interview Scope

## 1) What I built

Created [What to Learn for Senior Java Backend Interviews](../interview/curated-interview-guide.md) for a learner with 10+ years of Java/Spring/backend experience. It narrows the reference library to 24 core question families, practical coding/SQL, two initial designs, two project deep-dives, and eight stories. A conditional 24-problem algorithm starting set and up to six design prompts support confirmed algorithm/design-heavy processes.

## 2) Concepts prepared

Interview-format selection, prerequisite-based gap repair, evidence-based readiness, and distinguishing artifact completeness from learner understanding. Four profiles cover Java/framework, algorithm, practical coding, and lead/architect emphasis. Official company guides substantiate format differences; the recommended scope and time budgets are editorial choices, not company pass criteria.

## 3) Why this design

The prior entry pages prescribed a uniform 40% LeetCode allocation and large problem/design counts. A Java-heavy or practical backend interview may require a different balance. The new guide provides explicit defer rules and observable stop criteria, while preserving existing lesson evidence and active learning status.

## 4) Implementation notes

- Added exact question prompts, expected depth, local source links, profile allocations, recruiter questions, and readiness gates.
- Updated the hub, current tracker targets, company template, and docs index. Marked the original Markdown/text plans and 12-week calendar as superseded for current scope.
- This is a planning/reference artifact, not a new algorithm lesson or system-design answer file. It supplies no reference method or application implementation.
- No learner mastery, active lesson, product-feature status, or concept completion is inferred. The active Two Sum explanation/checkpoint requirement remains pending.

## 5) Artifact and project pointers

- [Curated guide](../interview/curated-interview-guide.md), [interview hub](../interview/README.md), [progress tracker](../interview/progress-tracker.md), and [company template](../interview/11-company-specific/README.md).
- [Original plan](../interview/interview-plan.md), [text copy](../interview/interview-plan.txt), and [12-week calendar](../interview/12-week-execution-plan.md) carry precedence notices.
- Existing project examples to use during later selected exercises: [PostService](../../src/main/java/com/instagram/backend/service/PostService.java), [PostLike](../../src/main/java/com/instagram/backend/domain/PostLike.java), and [RequestCorrelationFilter](../../src/main/java/com/instagram/backend/observability/RequestCorrelationFilter.java). This change does not modify or newly verify their behavior.

## 6) Verification and commit pointers

Artifact commit: `cd1364e` — curate senior Java interview preparation by round format.

Documentation-only change: validate local Markdown paths/anchors, 24 unique core IDs, 24 algorithm representatives, profile budget totals, source attribution, and Git whitespace. Application integration tests, API verification, and browser screenshots are not applicable because no application/UI behavior or executable lesson changed. Verification results and the artifact commit are recorded in the [commit map](../commit-map.md#curated-senior-java-interview-scope--2026-10-25).

Verification on 2026-10-25: PASS for 214 local links/anchors, 24 unique core IDs (8 Java, 7 Spring, 4 database, 5 distributed-service), 24 algorithm representatives, five allocations totaling 10 hours each, and precedence links from all three historical plans. `git diff --check` passed. Reviewed the question/source mappings and confirmed no existing mastery counts or active-lesson requirements were advanced.

## 7) Pitfalls and fixes

- Company-wide “no LeetCode” assumptions are replaced with confirmed process profiles and primary-source examples.
- Older numerical targets could keep driving unnecessary work; navigation, tracker targets, and precedence notices now point to the curated scope.
- Topic indexes may include unauthored lessons; the guide distinguishes reference prompts, prepared artifacts, and planned coverage.

## 8) Next improvements

- [ ] Confirm the format for each active interview process and select its profile.
- [ ] Diagnose one relevant question and record the learner's own explanation before selecting the next micro-lesson.
