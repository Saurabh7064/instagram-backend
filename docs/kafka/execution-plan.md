# Kafka Execution Plan

Follow prerequisite order, without a fixed weekly deadline. Each row is a module to split into 10–15 minute parts before activation. Only Module 01 currently has authored lessons. Future part names below are plans, not links to missing files.

| Module | Planned micro-lesson order | Exercise gate |
|---|---|---|
| 01 | Request waiting → events and roles → retained history → ordered partitions → independent progress | Complete the paper trace and failure diagnosis in [Module 01](./01-event-streaming-foundations/README.md) |
| 02 | Process/port reset → container reset → broker versus controller → local startup → topic/write/read → restart and cleanup | Record actual runtime versions; read back an event; diagnose startup failure; preserve or deliberately discard isolated lab data |
| 03 | Host versus container addresses → first connection → advertised addresses → wrong-address diagnosis | Prove both host and container clients can reach intended advertised addresses |
| 04 | Choose an ordering requirement → key routing → inspect partition positions → test order boundaries | Show per-partition order and explain why no total topic order is promised |
| 05 | One reading application → saved progress → restart → two applications → reassignment → replay | Compare group positions before/after restart and controlled replay |
| 06 | Java values to bytes → publication outcome → timeout → retry → batching | Observe successful and failed sends; distinguish an uncertain outcome from a definite failure |
| 07 | Event contract → Spring configuration → post event publication → integration verification | Demonstrate one post event; document the remaining database/publication failure gap |
| 08 | Spring reader → business result → save progress → restart → duplicate identity | Verify a delayed notification result and one business effect after repeat delivery |
| 09 | Temporary failure → retry budget → bad payload → investigation storage → safe reprocessing | Recover a temporary failure; isolate and inspect a bad event; document ordering consequences |
| 10 | Two-write failure → atomic database outbox → polling relay → duplicate publication → interrupted recovery | Show no lost committed pending event and no duplicate business effect in crash-window tests |
| 11 | Contract compatibility → old reader/new writer → new reader/old writer → payload privacy | Run compatibility cases and remove unnecessary personal data from sample events |
| 12 | Delay and unread-work metrics → slow reader → uneven traffic → replicated storage → recovery runbook | Observe a backlog grow/shrink; practice server failure only after building a multi-server lab |
| 13 | Authentication → encrypted transport → topic permissions → denied operation | Prove allowed and denied operations without committing credentials |
| 14 | Connect versus application code → database change capture → Streams transform → late-data prerequisite → capstone tradeoffs | Compare alternatives and produce an evidence-backed architecture; optional extensions stay separate |

## Before the first runtime lab

Confirm Docker availability, available local ports and disk space, isolation from existing project services, the selected official Kafka image tag/digest, the Java/client compatibility matrix, and the cleanup plan. Teach each of those ideas before using its commands. Use local resources; this path does not require AWS.

Do not add Kafka to the project's [Compose file](../../docker-compose.yml) merely to complete documentation initialization. In Module 02, choose a separate lab configuration first. Record what survives restart, what cleanup deletes, and the exact observed results. One local server demonstrates behavior but cannot demonstrate server-failure availability.

## Evidence at activation time

Each runtime module must record prediction, exact command or request, observed output, why it occurred, controlled failure, diagnosis, recovery, and cleanup result. Link integration output and API verification for application changes; link inspected screenshots under `docs/demo-proofs/<YYYY-MM-DD>/` when a browser flow changes. Keep expected output clearly separate from actual observations.

Update [the module backlog](./learning-backlog.md), [learner evidence](./learner-progress.md), the affected feature note, [both shared trackers](../features/learning-backlog.md), [concept tracker](../system-design/learning-backlog.md), and [commit map](../commit-map.md). Commit and push only the related files.
