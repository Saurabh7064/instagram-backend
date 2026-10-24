# Kafka Track Initialization Evidence

**Date:** 2026-10-24. **Scope:** Markdown curriculum initialization only.

## Prepared artifacts

- Entry point, coverage roadmap, prerequisite-ordered execution plan, 14-module exercise backlog, separate comprehension tracker, and two reusable templates.
- Module 01 landing page and five 10–15 minute micro-lessons. Each introduces at most three primary new terms and includes three expandable explained questions: 15 checkpoints in total.
- Fixed post examples with exact partition locations, independent group positions, restart/repeat behavior, retention failure, and recovery limits.
- Current code links and explicit labels for proposed Kafka/notification/outbox components.
- [Feature 18](../features/18-kafka-learning-path-initialization.md), shared tracker annotations, [docs index](../index.md), and [commit mapping](../commit-map.md#kafka-learning-path-initialization--2026-10-24).

## Validation

- Passed structural validation across 15 new Markdown files: 115 local links/anchors resolve; all 15 tables have consistent columns; expandable blocks are balanced and have explained answers.
- Verified five lesson files, three primary new terms and three numbered explained checkpoints per lesson, required teaching sections, and prerequisite/next links.
- Verified all 14 module exercise rows remain TODO, only Module 01 is READY, and all five learner rows remain NOT_ASSESSED. Existing shared feature/concept table rows are unchanged.
- Independently replayed the fixed example: 40 + 500 = 540 ms; notifications has two unread records and analysis three; successful notification completion leaves zero; failure before saving P0 position 2 causes E2 to repeat from saved position 1.
- Checked all 11 distinct official reference URLs/fragment combinations: HTTP 200 and every linked fragment exists. Corrected the consumer API method anchor during verification.
- Passed `git diff --check` for the scoped documentation changes.

These checks validate the artifacts, not a Kafka runtime or learner understanding.

## Runtime and learner boundary

No broker was installed or started. No application dependency, configuration, source, API, or UI changed. Backend integration runs, API requests, and browser screenshots are therefore not claimed for this change. The local-broker and Spring integration evidence belongs to later activated exercises.

Every module exercise remains `TODO`; all five authored micro-lessons remain `NOT_ASSESSED`. Module 01's artifact status is `READY`, and Modules 02–14 remain `PLANNED`. No checkpoint answers have been supplied by the learner.

## References checked

Checked against versioned official Apache Kafka documentation on 2026-10-24: [introduction](https://kafka.apache.org/43/getting-started/introduction/), [topic configuration](https://kafka.apache.org/43/configuration/topic-configs/), [consumer API](https://kafka.apache.org/43/javadoc/org/apache/kafka/clients/consumer/KafkaConsumer.html), [consumer configuration](https://kafka.apache.org/43/configuration/consumer-configs/), [design](https://kafka.apache.org/43/design/design/), and [quickstart](https://kafka.apache.org/43/getting-started/quickstart/). Future labs select and record their actual runtime/client versions separately.

## Commit pointers

See the [commit map entry](../commit-map.md#kafka-learning-path-initialization--2026-10-24) for the artifact commit hash and verification record.
