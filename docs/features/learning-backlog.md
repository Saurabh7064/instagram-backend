# Feature Learning Backlog (Build Track)

Use this file to track product/engineering features for the app.

## Status legend

- `TODO` not started
- `IN_PROGRESS` currently building
- `DONE` completed and documented

## Current scope

| ID | Feature | Status | Why it matters | Code/Doc link |
|---|---|---|---|---|
| F-001 | Register API | DONE | Basic account creation | [Feature 01](./01-authentication-and-password-security.md) |
| F-002 | Login API | DONE | Basic authentication flow | [Feature 01](./01-authentication-and-password-security.md) |
| F-003 | Signup UI | DONE | Browser registration flow | [Feature 01](./01-authentication-and-password-security.md) |
| F-004 | Login UI | DONE | Browser login flow | [Feature 01](./01-authentication-and-password-security.md) |
| F-005 | Password hashing (BCrypt) | DONE | Security baseline | [Feature 01](./01-authentication-and-password-security.md) |
| F-006 | JWT access token issuance | DONE | Stateless auth for scale | [Feature 02](./02-jwt-access-token-issuance.md) |
| F-007 | Protected endpoint (`/api/me`) | DONE | Validate auth end-to-end | [Feature 04](./04-protected-me-endpoint.md) |
| F-008 | Refresh token flow | DONE | Better session UX/security | [Feature 05](./05-refresh-token-flow.md) |
| F-009 | Basic profile page API/UI | DONE | First authenticated product feature | [Feature 06](./06-basic-profile-page-api-ui.md) |
| F-010 | Posts create/read API/UI | DONE | Core social functionality | [Feature 07](./07-posts-create-read-api-ui.md) |
| F-011 | Browser demo proof workflow | DONE | Reliable visual evidence for UI+backend integration | [Feature 03](./03-browser-demo-proof-and-visual-verification.md) |
| F-012 | Post like/unlike API/UI | DONE | First engagement action on content | [Feature 08](./08-post-like-unlike-api-ui.md) |
| F-013 | Delete own post API/UI | DONE | First ownership-based content mutation | [Feature 09](./09-delete-own-post-api-ui.md) |
| F-014 | Edit own post API/UI | DONE | First ownership-based update flow | [Feature 10](./10-edit-own-post-api-ui.md) |
| F-015 | Persist real feed media uploads | DONE | Replace SVG placeholder media with real uploaded content | [Feature 11](./11-persist-real-feed-media-uploads.md) |
| F-016 | Multi-post feed rendering | TODO | Show more than the hero card and support scrolling feed state |  |
| F-017 | Comments create/read API/UI | TODO | Core conversation layer on posts |  |
| F-018 | Save/unsave posts API/UI | TODO | Personal curation and later-viewing flow |  |
| F-019 | Follow/unfollow users API/UI | TODO | Core social graph behavior |  |
| F-020 | Followers/following lists API/UI | TODO | Inspect and navigate the social graph |  |
| F-021 | Personalized feed ranking | TODO | Move from chronological feed to relevance-based ordering |  |
| F-022 | Search users and posts API/UI | TODO | Discovery beyond the home feed |  |
| F-023 | Explore page API/UI | TODO | Dedicated discovery surface with ranked content |  |
| F-024 | Real stories API/UI | TODO | Replace mocked story circles with actual ephemeral content |  |
| F-025 | Story viewer and expiry rules | TODO | Complete the stories product loop |  |
| F-026 | Direct messages thread list API/UI | TODO | Private communication basics |  |
| F-027 | Direct message send/read flow | TODO | Actual messaging experience |  |
| F-028 | Notifications API/UI | TODO | User awareness for likes, follows, and comments |  |
| F-029 | Profile edit API/UI | TODO | Let users change name, bio, avatar, and profile metadata |  |
| F-030 | Image/video processing pipeline | TODO | Production path for media storage, resizing, and delivery |  |
| F-031 | Reels-style short video feed | TODO | Core modern Instagram engagement surface |  |
| F-032 | Hashtags and mentions parsing | TODO | Link content, search, and discovery graph |  |
| F-033 | Post reporting and moderation queue | TODO | Safety and abuse-handling baseline |  |
| F-034 | Rate limiting and abuse protection | TODO | Protect auth and content APIs under load or attack |  |
| F-035 | Production auth hardening | TODO | Session revocation, device sessions, and stronger security controls |  |
| F-036 | Production observability baseline | DONE | Correlate safe logs, bounded metrics, traces, and health signals during production debugging | [Feature 12](./12-production-observability-baseline.md) |
| F-037 | Interactive Markdown learning studio | DONE | Turn existing lessons into deterministic, visual, testable learning experiences without replacing Markdown | [Feature 13](./13-interactive-learning-studio.md) |
| F-038 | Interactive sliding-window curriculum | DONE | Teach fixed and variable windows through four additional concept-first lessons and derived simulations | [Feature 14](./14-interactive-sliding-window-curriculum.md) |
| F-039 | Numbered reference solutions | DONE | Make every current and future multiline reference block easy to discuss without polluting copied Java | [Feature 15](./15-numbered-reference-solutions.md) |

## Next 3 recommended

1. `F-016` Multi-post feed rendering
2. `F-017` Comments create/read API/UI
3. `F-018` Save/unsave posts API/UI

## Curated interview scope — 2026-10-25

Prepared the [senior Java interview shortlist](../interview/curated-interview-guide.md) and updated entry-point priorities; see [Feature 21](./21-curated-interview-scope.md). Artifact status is READY after documentation validation. This selects interview scope rather than delivering product behavior. Existing feature status rows, the active Two Sum lesson, and learner mastery remain unchanged.

## Interview artifact update — 2026-10-17

Added ready interview answers on concurrent likes, idempotency keys, and deadline budgets in the [question index](../interview/02-system-design/questions/short-questions-and-answers/README.md). This documentation update does not complete an implementation milestone or demonstrate learner comprehension; existing statuses remain unchanged.

## API design lesson artifacts — 2026-10-17

Created [15 API Design with Spring Boot lessons](../interview/02-system-design/api-design/README.md), with [separate learner evidence](../interview/02-system-design/api-design/learner-progress.md). Artifacts are READY; comprehension remains TODO. Proposed exercises map to F-010/F-014/F-016/F-017/F-022/F-030 and SD-004 compatibility. No feature implementation or concept mastery status changes are claimed.

### API design depth review — 2026-10-17

Re-reviewed and expanded every original lesson and added five applied-pattern chapters: [20-lesson module](../interview/02-system-design/api-design/README.md), [coverage/evidence](../interview/02-system-design/api-design/review-notes.md). HTTP method/patch semantics and versioning were rewritten; relationship races, negotiation, aggregation/BFF, bulk operations, and contract-first design now have explicit decisions and worked examples. Artifact completion is documentation only; feature statuses and learner comprehension remain unchanged.

## Prefix/suffix and interval artifacts — 2026-10-17

Prepared [five Module 04 LeetCode lessons](../interview/01-leetcode/lessons/04-prefix-suffix-intervals/README.md), with [Feature 16 evidence](../features/16-prefix-suffix-and-interval-curriculum.md), separate runnable practice/reference programs, narrated iterations, and checks. This artifact-only addition does not complete a product implementation milestone or demonstrate learner comprehension; existing feature/concept statuses remain unchanged. All five learning entries remain TODO; the active Two Sum lesson is unchanged.

## Functional-programming introduction review — 2026-10-17

Reworked the [introductory functional-programming module](../interview/04-java/01-functional-programming-coding/README.md) into concept-first lessons with small Java programs, narrated executions, and a suitable array LeetCode bridge; see [Feature 17](../features/17-functional-programming-concept-first.md). This learning-artifact change does not complete a product implementation or demonstrate learner comprehension. Existing tracker statuses are unchanged; all four FP lessons remain TODO and inactive.

## Kafka track initialization — 2026-10-24

Prepared the [Kafka learning path](../kafka/README.md), with five foundations micro-lessons and a 14-module roadmap/backlog; see [Feature 18](./18-kafka-learning-path-initialization.md). Proposed application exercises link F-010 post events, F-028 notifications, and F-030 processing. Only Module 01 artifacts are READY; every module exercise remains TODO and comprehension remains NOT_ASSESSED. No Kafka application behavior or product-feature completion is claimed; existing feature statuses remain unchanged.

## Java functional concept course — 2026-10-24

Recreated the [functional-programming course](../interview/04-java/01-functional-programming-coding/README.md) as 28 concept lessons with small examples and 84 explained questions; see [Feature 19](./19-functional-programming-concept-course.md). Removed iteration/debugger narration and required LeetCode progression from this course. Saved the Java-concept preference in [AGENTS.md](../../AGENTS.md#functional-programming-lesson-standard). Coverage artifacts are READY; understanding remains TODO and coding is optional. Existing feature status rows remain unchanged.

## Multithreading foundations — 2026-10-24

Added [seven foundation micro-lessons](../interview/04-java/02-multithreading-from-scratch/lessons/00-foundations/README.md) before the revised start/join lesson, with [Feature 20](./20-multithreading-foundations.md) recording evidence. The course starts with program/process and what a thread is, then teaches main, CPU time, tasks, and sharing before APIs. Eight artifacts are READY; comprehension remains TODO and coding is optional. Existing feature status rows are unchanged.

## Distributed systems fundamentals artifacts — 2026-10-25

Completed [16 lessons covering 25 topics](./22-distributed-systems-fundamentals.md); artifact `READY`, comprehension `TODO`. Proposed implementation tasks map F-010 retry-safe creation, F-030 processing ownership, and SD-008 asynchronous recovery. Existing build/concept statuses remain unchanged; this documentation does not complete implementation or prove understanding.

## Distributed-systems teaching rebuild — 2026-10-25

Rebuilt [Feature 22](./22-distributed-systems-fundamentals.md) using the new concept teaching-quality rule: full terminology/components, prerequisite reasoning, derived examples, explicit boundaries, and semantic review. All 16 artifacts are READY; all comprehension remains TODO. F-010 retry-safe creation, F-030 fenced processing, and SD-008 asynchronous recovery remain proposed follow-ups; no build/concept status is upgraded by documentation.

## Partitioning and Cassandra key artifacts — 2026-10-25

Created [Feature 23](./23-partitioning-and-cassandra-key-lessons.md): 11 lessons with 33 explained checkpoints covering distribution, hotspots, key choice, table creation, and routing. Artifact READY; comprehension TODO. The PostgreSQL exercise is isolated and executed; Cassandra runtime verification remains pending. F-016/SD-007 query planning and F-021 query-specific read models are proposed follow-ups, not completed implementation. Existing status rows are unchanged.


## Learning Studio tutorial navigation — 2026-10-25

DONE: [Feature 24](./24-tutorial-style-learning-studio.md), with main-topic navigation, collapsible subtopic groups, responsive reading layout, repaired generator path, and seven passing browser integration checks. No backend build-track or learner comprehension status changed.
