# Kafka Mastery Roadmap

This is a coverage map. Each item may need several micro-lessons; a tier is not a single sitting. The [execution plan](./execution-plan.md) puts the project exercises in order.

## Tier 0 — Understand the problem

Start with [Module 01](./01-event-streaming-foundations/README.md).

- Requests and responses; work completed before a response versus work completed later.
- Facts about completed changes; writers and readers with independent schedules.
- Stored history, named streams, and retention limits.
- Ordered parts of a stream, routing keys, and positions within each part.
- Independent reading applications, saved progress, and rereading retained data.

Outcome: explain how a future notification can be delayed even though a post was saved, without claiming Kafka makes the database change and event publication indivisible.

## Tier 1 — Observe a local Kafka server

Teach missing prerequisites first: a process is a running program; a port identifies a network entry point; `localhost` refers to the machine or container making the connection. Refresh [container basics](../kubernetes/01-containerize-spring-boot-backend/01b-image-container-and-dockerfile.md) before running a container.

- Local server lifecycle, persistent versus disposable lab data, and cleanup.
- The controller role, which coordinates Kafka metadata; KRaft, Kafka's built-in metadata consensus mechanism. Keep this separate from application event processing.
- Topic creation, console writers/readers, and event inspection.
- Host/container addressing and advertised listeners: addresses Kafka tells clients to use after their first connection.
- Two partitions, same-key routing, separate reader applications, restart positions, and deliberate replay.

Outcome: produce and read identifiable events, observe saved progress, diagnose an unreachable address, and recover without deleting data blindly.

## Tier 2 — Integrate Java and Spring Boot

- Acknowledgments: what the server confirmed, what the application still has not done.
- Serialization: turning a Java value into bytes; deserialization: interpreting those bytes on reading.
- Producer outcomes, retries, delivery deadlines, batching, and key choice.
- Spring configuration, event contracts, listener lifecycle, and asynchronous test assertions.
- Consumer processing versus saving reading progress; shutdown and reassignment of work.

Project mapping: a future `PostCreated` event begins at [PostService.create](../../src/main/java/com/instagram/backend/service/PostService.java). Use a separate proposed notification result to demonstrate delayed processing for [F-028](../features/learning-backlog.md). Keep the initial Spring producer experiment explicitly limited until the database/publication gap is solved.

## Tier 3 — Make failures explicit

- At-most-once, at-least-once, and the scope of exactly-once guarantees.
- Idempotency: repeating an operation has the same intended business result as performing it once.
- Duplicate event identity versus a new event about the same post.
- Bounded retries, delay, permanently invalid events, and a separate location for events needing investigation.
- Outbox: save the business change and a pending event in one database transaction, then publish later.
- Relay failures, duplicate publication, reader deduplication, and safe restart.
- Schema evolution, event versions, deletion/privacy rules, and compatibility checks.

Outcome: demonstrate one business result despite duplicate delivery, recover an interrupted relay, and explain why [PostgreSQL transactions](../../src/main/java/com/instagram/backend/service/PostService.java) alone do not cover Kafka publication. Kafka's own transaction guarantees require careful attention to external systems; see [delivery semantics](https://kafka.apache.org/43/design/design/#message-delivery-semantics).

## Tier 4 — Operate and choose deliberately

- Measure delay and unread work; distinguish insufficient capacity from a permanently failing event.
- Replication: copies of partition data on different servers; leader failure and durability settings.
- Capacity, uneven key traffic, bounded recovery time, retention, and storage pressure.
- Authentication, encrypted transport, permissions, and safe event payloads.
- Kafka Connect: integration runtime for moving data between Kafka and other systems.
- Change data capture: observing committed database changes; compare it with polling an outbox table.
- Kafka Streams: Java library for transforming Kafka records; introduce windows and late data only in separate lessons.
- When a synchronous call or a simpler queue is sufficient; operational cost and recovery burden.

Outcome: write a failure runbook and justify Kafka for a concrete requirement rather than using it because it appears on the roadmap. Advanced integration and stream processing remain optional after the reliable notification path works.

## Coverage versus mastery

Writing these topic names completes no implementation task. Record artifact readiness in the [backlog](./learning-backlog.md) and demonstrated explanations in [learner progress](./learner-progress.md). Build-track links to F-010/F-028/F-030 and concept-track links to SD-008/SD-009 are planning connections, not status upgrades.
