# 01 — Event Streaming Foundations

## Start here

No Kafka, Docker, networking, or messaging knowledge is assumed. You only need the idea that a user can create a post; 01A defines the browser request, backend response, and database roles. This is a paper module. It does not install Kafka or modify application behavior.

The problem is concrete: a post must be saved promptly, while later reactions may be slow or unavailable. We will explain the stored facts that connect those steps, then locate ordering and reading progress. Runtime commands and Java APIs come after this model, in later modules.

## Micro-lesson path

| Part | One primary idea | Time | Prerequisite | Ready when |
|---|---|---|---|---|
| [01A](./01a-request-work-and-waiting.md) | Decide what a request waits for | 10–15 min | None | Explain the response promise and derive the waiting time |
| [01B](./01b-events-producers-consumers.md) | Exchange a completed fact | 10–15 min | 01A | Distinguish fact, writer, reader, and repeated delivery |
| [01C](./01c-broker-topic-retention.md) | Keep history for a later reader | 10–15 min | 01B | Explain why reading and deleting are separate |
| [01D](./01d-partitions-keys-offsets.md) | Locate ordering within a partition | 10–15 min | 01C | Reproduce event locations and the ordering boundary |
| [01E](./01e-groups-progress-replay.md) | Keep independent reading progress | 10–15 min | 01D | Derive restart positions and diagnose a repeated event |

Study one row at a time. In chat, stop for the learner's checkpoint attempt before moving to the next. Prepared artifacts do not imply these parts have been understood.

## One-page recap

The request decides what success promises. A completed fact lets another program react later. Kafka stores events in named topics, each divided into ordered partitions. Readers track progress separately from stored history. The event's location identifies where it sits, while the group's saved next position says where that application resumes. Event identity identifies the business occurrence independently of either position.

Kafka can separate publication from reading. Reliability still needs an explicit plan for the boundary between saving the post and publishing its event, and for the boundary between a reader's business result and saved progress. Those are proposed later exercises, not guarantees of the current application.

## Vocabulary cheat sheet

| Term | Plain meaning | Example | Do not confuse with |
|---|---|---|---|
| Event | Recorded completed fact | E1 says post 42 was created | Request to create it |
| Producer / consumer | Event writer / reader | Proposed post writer / notification reader | One necessarily calling the other |
| Broker | Kafka event server | Future local lab server | Current PostgreSQL server |
| Topic | Named stream | instagram.post-events | A database table of current posts |
| Retention | Limits on stored history | Old events eventually removed | A reader deleting on read |
| Partition | Ordered portion of a topic | P0 contains E1 before E2 | Total topic ordering |
| Key | Input used to route an event | postId 42 | Unique event ID E1 |
| Offset | Position inside a partition | P0 offset 1 holds E2 | Global event number |
| Consumer group | One logical reading application | notifications-v1 | Every member reading everything independently |
| Committed offset | Saved next restart position | P0 position 1 resumes at E2 | Proof that the business effect completed |
| Replay | Read retained history again | Read E1 again from P0 position 0 | Recovering already deleted history |

## Integrated paper exercise

Attempt this only after completing 01A–01E. No exercise completion is claimed yet.

**Fixed input:** E1 creates post 42, E2 edits post 42, E3 creates post 99. Under the exercise's fixed routing, P0 holds E1@0 and E2@1; P1 holds E3@0. Notifications has saved next positions P0=1/P1=0; analysis has P0=0/P1=0. All events remain retained, with no offset gaps.

**Predict:** Write what each group still needs, whether E3 must follow E2, and what notifications reads after stopping before its next progress save.

**Act:** Draw the two partitions and two independent sets of bookmarks. Let notifications handle E2 and E3 and then successfully save P0=2/P1=1. Leave analysis unchanged.

**Expected observation:** Notifications has no unread events in this fixed history: `(2 - 2) + (1 - 1) = 0`. Analysis still has `(2 - 0) + (1 - 0) = 3`. E3's order relative to P0 is unconstrained. Different progress does not imply separate copies of the topic.

**Controlled failure and recovery:** Repeat from the initial state, store the E2 result, then stop before saving P0=2. The saved value remains 1, so E2 repeats after restart. Explain how durable recognition of already applied E2 would avoid a second business effect. Then remove E1 to model cleanup and explain why analysis cannot reread that historical event from this topic. Name a rebuild/archive option and state when recovery is impossible.

**Why:** Stored history, event identity, business effects, and saved reading progress are separate pieces of state. The safe implementation must connect them deliberately.

## Project pointers and evidence

- Current request handling: [PostController.java](../../../src/main/java/com/instagram/backend/controller/PostController.java).
- Current post operations and database transaction boundaries: [PostService.java](../../../src/main/java/com/instagram/backend/service/PostService.java).
- Current stored post: [Post.java](../../../src/main/java/com/instagram/backend/domain/Post.java); database access: [PostRepository.java](../../../src/main/java/com/instagram/backend/repository/PostRepository.java).
- Current runtime/dependencies: [docker-compose.yml](../../../docker-compose.yml), [build.gradle](../../../build.gradle).
- Proposed notification feature: [F-028](../../features/learning-backlog.md); asynchronous-processing concept: [SD-008](../../system-design/learning-backlog.md).
- Artifact verification: [initialization evidence](../initialization-evidence.md), [Feature 18](../../features/18-kafka-learning-path-initialization.md), and [commit map](../../commit-map.md#kafka-learning-path-initialization--2026-10-24).

Runtime tests, API checks, and browser proof are not evidence for this paper exercise: no application behavior changed and no Kafka runtime was started. Actual lab outputs will be recorded in later module notes. Learner exercise/checkpoint evidence remains pending in [learner progress](../learner-progress.md).

## Teach it back

Explain why the post request may finish before notification delivery, where the event lives, what order is guaranteed, where each reading application resumes, and why a repeat can happen. Name one failure Kafka alone cannot solve. Use your own words before expanding lesson answers.

## Next module

KAFKA-002 in the [backlog](../learning-backlog.md) will turn this model into an isolated local server lab. Author and verify that lab when activated; no installation is required to finish this foundations module.
