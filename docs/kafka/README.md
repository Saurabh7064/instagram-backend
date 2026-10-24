# Kafka Learning Path

Learn Kafka from the problem it solves, then apply it to this Instagram backend. Start with [01A — What should a post request wait for?](./01-event-streaming-foundations/01a-request-work-and-waiting.md). No Kafka, container, networking, or messaging knowledge is assumed.

## How to use this track

Read one 10–15 minute micro-lesson at a time. Explain the idea, predict an outcome, attempt the exercise, then answer the checkpoints before opening their answers. In chat, say `teach kafka 01A`; teach only that part and stop for 2–3 checkpoint answers. If a prerequisite is unclear, step back before adding vocabulary.

The [learning backlog](./learning-backlog.md) tracks exercises and evidence. [Learner progress](./learner-progress.md) tracks understanding separately. A prepared lesson or a successful lab does not demonstrate understanding.

## Navigation

- [Module 01 — Event streaming foundations](./01-event-streaming-foundations/README.md): five prepared micro-lessons, with 15 explained checkpoint questions.
- [Mastery roadmap](./mastery-roadmap.md): coverage and project connections.
- [Execution plan](./execution-plan.md): prerequisite order and future lab gates.
- [Learning backlog](./learning-backlog.md): module status, proposed exercises, required evidence.
- [Learner progress](./learner-progress.md): explanations, predictions, and failure diagnosis.
- [Module template](./_lesson-template.md) and [micro-lesson template](./_micro-lesson-template.md).
- [Initialization evidence](./initialization-evidence.md): scope and verification for this documentation change.

## Current project boundary

The existing [PostController](../../src/main/java/com/instagram/backend/controller/PostController.java) calls [PostService](../../src/main/java/com/instagram/backend/service/PostService.java), which saves posts through [PostRepository](../../src/main/java/com/instagram/backend/repository/PostRepository.java). The [Gradle build](../../build.gradle) has no Kafka dependency, and [Compose](../../docker-compose.yml) starts PostgreSQL and the backend. Kafka publication, notification workers, event storage tables, and delivery guarantees discussed here are **proposed exercises**.

The first module uses paper exercises to establish the model. It requires no installation and makes no runtime changes. Only that module is authored; later modules are planned coverage. All exercise statuses start `TODO`, and comprehension starts `NOT_ASSESSED`.

## Teaching and completion rules

Each micro-lesson starts with one concrete problem, defines at most three primary new terms, and separates `Must understand`, `Useful later`, and `Optional deep dive`. Teach the idea before commands. Use prediction → action → expected observation → explanation, with three or more expandable explained checkpoints and observable stop/go criteria.

To activate a future module, first author its prerequisite-ordered micro-lessons. Mark the exercise `IN_PROGRESS` only when starting it. Mark `DONE` only after its stated success, controlled-failure, recovery, and documentation evidence exists. Do not turn a roadmap row into a claim of implemented functionality.

When a future exercise changes application behavior, follow [project rules](../../AGENTS.md) and the [testing strategy](../testing-strategy.md): meaningful integration coverage, real API verification, browser proof when the UI flow changes, feature notes, both shared backlog updates, commit mapping, a focused commit, and push. Kafka-specific tests should use a real Kafka test container alongside PostgreSQL where needed; select compatible dependencies at activation time.

## Reference policy

Use versioned official references and record the runtime version when a lab is activated. The source set was checked on 2026-10-24: [Kafka introduction](https://kafka.apache.org/43/getting-started/introduction/), [design](https://kafka.apache.org/43/design/design/), and [quickstart](https://kafka.apache.org/43/getting-started/quickstart/). No runtime version is installed or pinned by this initialization. Avoid copying an old course's commands into a new lab without checking compatibility.
