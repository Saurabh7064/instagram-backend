# 18 — Kafka Learning Path Initialization

## 1) What I built

Initialized [docs/kafka](../kafka/README.md) with a coverage roadmap, 14-module backlog and execution plan, separate learner-progress tracking, reusable module/micro-lesson templates, and a complete five-part first module. Only Module 01 is authored; future modules are planned exercises. The first module teaches request waiting, completed facts, storage/retention, partition ordering, and independent reading progress through paper exercises and 15 explained checkpoints.

## 2) Concepts learned

These are prepared learning topics, not demonstrated learner mastery:

- Separate the API's success promise from a later reaction.
- Distinguish event identity, partition location, business results, and saved reading progress.
- Predict repeated delivery and unavailable history after cleanup.
- Separate artifact readiness, exercised implementation, and demonstrated understanding.

## 3) Why this design

The existing learning tracks use modules and short prerequisite-ordered parts. Kafka follows that structure without requiring container or networking knowledge before the first lesson. A complete foundations module gives an immediate starting point, while later real-broker and Spring exercises remain explicit plans rather than unverified setup instructions.

## 4) Implementation notes

- Start at [01A](../kafka/01-event-streaming-foundations/01a-request-work-and-waiting.md) and teach one part at a time in chat.
- Keep all exercises `TODO` until attempted; keep comprehension `NOT_ASSESSED` until the learner answers.
- Examples identify current post/database behavior and label Kafka publication, notification readers, and outbox storage as proposed.
- The new path is linked from the docs index and shared backlog annotations. No lesson-catalog generator or UI behavior changes are included.
- Validation and scope are recorded in [initialization evidence](../kafka/initialization-evidence.md).

## 5) Code pointers

- Existing request entry: [PostController.java](../../src/main/java/com/instagram/backend/controller/PostController.java).
- Existing post mutation and database transactions: [PostService.java](../../src/main/java/com/instagram/backend/service/PostService.java).
- Existing persistence: [Post.java](../../src/main/java/com/instagram/backend/domain/Post.java), [PostRepository.java](../../src/main/java/com/instagram/backend/repository/PostRepository.java).
- Current dependencies and runtime: [build.gradle](../../build.gradle), [docker-compose.yml](../../docker-compose.yml).
- Curriculum: [module landing page](../kafka/01-event-streaming-foundations/README.md), [backlog](../kafka/learning-backlog.md), [learner progress](../kafka/learner-progress.md).

## 6) Commit pointers

The [commit map](../commit-map.md#kafka-learning-path-initialization--2026-10-24) records the artifact commit hash once available. No UI commit is required for this documentation-only initialization.

## 7) Pitfalls and fixes

- A roadmap can look like finished teaching material. Artifact readiness is recorded separately; only the five authored parts are READY.
- A successful lab can look like comprehension. The separate learner tracker requires explanations, predictions, and checkpoints.
- Simplified messaging explanations can imply a global order or automatic database guarantees. The fixed partition tables and failure exercises state their boundaries explicitly.
- Existing unrelated edits were present. They are excluded from this change's commits.

## 8) Next improvements

- [ ] Teach 01A and record the learner's checkpoint attempt.
- [ ] Author KAFKA-002's local lab with version selection, prerequisite resets, failure/recovery checks, and cleanup evidence when activated.
- [ ] Implement future Spring producer/reader/outbox exercises with Kafka/PostgreSQL integration tests and exact API verification.
