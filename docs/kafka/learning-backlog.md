# Kafka Learning Backlog

`TODO` means the exercise has not started. `IN_PROGRESS` means it is being attempted. `DONE` requires its evidence, documentation, commit, and push. Artifact status is independent: `READY` means authored material exists; `PLANNED` means only a roadmap exists. Neither proves comprehension; use [learner progress](./learner-progress.md).

## Module sequence

| ID | Module and project exercise | Exercise status | Artifact | Required completion evidence |
|---|---|---|---|---|
| KAFKA-001 | [Event streaming foundations](./01-event-streaming-foundations/README.md): trace a proposed post notification | TODO | READY | Completed paper exercise; predicted stop/resume behavior; diagnosed expired history; explain database/publication gap |
| KAFKA-002 | Run an isolated local Kafka lab and write/read the first event | TODO | PLANNED | Pinned version/config; event readback; controlled startup failure/recovery; restart persistence and cleanup result |
| KAFKA-003 | Connect host and container clients using correct addresses | TODO | PLANNED | Both connection paths; wrong advertised address symptom, diagnosis, and recovery |
| KAFKA-004 | Choose post keys and inspect partition ordering | TODO | PLANNED | Same-post event order; explicit partition/offset output; no cross-partition order claim |
| KAFKA-005 | Observe consumer groups, progress, reassignment, and replay | TODO | PLANNED | Two independent groups; restart position; work reassignment; controlled replay results |
| KAFKA-006 | Publish typed Java events and handle send outcomes | TODO | PLANNED | Real-broker tests of valid/invalid encoding and failed/uncertain sends; explicit retry/deadline policy |
| KAFKA-007 | Add an experimental Spring `PostCreated` producer, linked to F-010 | TODO | PLANNED | Kafka/PostgreSQL integration test; API request; exact event; failure-gap note; feature documentation |
| KAFKA-008 | Build a notification reader with duplicate protection, linked to F-028 | TODO | PLANNED | Restart and repeat-delivery tests produce one intended result; API verification; browser proof if UI changes |
| KAFKA-009 | Bound retries and isolate permanently failing events | TODO | PLANNED | Temporary failure recovery; bad-event isolation; inspected payload; safe retry and ordering tradeoff |
| KAFKA-010 | Close the database/publication gap using an outbox, linked to F-010/F-028 | TODO | PLANNED | Atomic post/pending-event save; relay crash tests; eventual publication; duplicate-safe effect; bounded polling |
| KAFKA-011 | Evolve event contracts and minimize payload data | TODO | PLANNED | Old/new reader-writer compatibility cases; version decision; deletion/privacy note |
| KAFKA-012 | Measure lag and practice durability/recovery, linked to SD-009 | TODO | PLANNED | Slow-reader recovery; metric definitions; multi-server failure drill; documented durability limits |
| KAFKA-013 | Restrict access and protect transport | TODO | PLANNED | Allowed/denied operations; encrypted connection; no committed credentials; teardown evidence |
| KAFKA-014 | Compare Connect/Streams and design the capstone, linked to F-030/SD-008 | TODO | PLANNED | Explicit technology choice; cost/failure analysis; optional transformation lab; teach-it-back |

## Initialization boundary — 2026-10-24

The track and Module 01 lesson artifacts were prepared. No learner attempt, Kafka server, producer, reader, outbox, or notification feature was implemented. KAFKA-001 remains `TODO` until the learner starts its exercise. Future module content is authored when activated, following the [execution plan](./execution-plan.md).

Start with [01A](./01-event-streaming-foundations/01a-request-work-and-waiting.md), then stop for its checkpoint. Do not activate another module while one is `IN_PROGRESS` without recording why.
