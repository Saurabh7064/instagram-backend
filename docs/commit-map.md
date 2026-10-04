# Commit Map (Feature -> Concept)

This file maps each commit to:
- feature delivered
- system/design concept learned
- where to read more

## Backend Repo: `instagram-backend`

1. `5a1d99c`
- Feature: register/login API, PostgreSQL Docker setup, CORS, demo user seed.
- Concepts: API contract design, persistence with JPA, local infra with Docker, cross-origin integration.
- Docs: [Feature 01](./features/01-authentication-and-password-security.md)

2. `c00cd8e`
- Feature: learning docs system (diary + system design path + docs index).
- Concepts: documentation-as-code, feature-to-concept traceability.
- Docs: [Docs Index](./index.md)

3. `5ed562a`
- Feature: enforced commit-per-feature workflow and added shared commit map navigation.
- Concepts: process governance, traceable engineering workflow.
- Docs: [Docs Index](./index.md)

4. `db2aeb7`
- Feature: added separate backlog trackers for build work and system design learning.
- Concepts: iterative planning, parallel tracks (implementation vs architecture learning).
- Docs: [Feature Learning Backlog](./features/learning-backlog.md), [System Design Learning Backlog](./system-design/learning-backlog.md)

5. `b76ca3d`
- Feature: added strict `next feature` and `next concept` command protocol with pre-check and completion gates.
- Concepts: reliable iterative delivery, no-skip workflow control.
- Docs: [Feature Learning Backlog](./features/learning-backlog.md), [System Design Learning Backlog](./system-design/learning-backlog.md)

6. `305b6b4`
- Feature: implemented BCrypt hashing for registration/login with legacy password migration path.
- Concepts: secure credential storage, backward-safe authentication upgrades.
- Docs: [Feature 01](./features/01-authentication-and-password-security.md), [Concept 02](./system-design/02-password-storage-and-credential-security.md)

7. `ef032c9`
- Feature: added mandatory testing policy, testing strategy doc, and executable HTTP request file.
- Concepts: testability gates, repeatable verification workflow.
- Docs: [Testing Strategy](./testing-strategy.md), [Feature 01](./features/01-authentication-and-password-security.md)

8. `c583c1a`
- Feature: reorganized feature documentation into numbered `docs/features` learning path.
- Concepts: documentation architecture consistency with system-design track.
- Docs: [Feature Learning Path](./features/README.md)

9. `39fffe3`
- Feature: fully mirrored feature track to system-design layout using `docs/features/learning-backlog.md`.
- Concepts: symmetrical documentation information architecture.
- Docs: [Feature Learning Path](./features/README.md), [Feature Learning Backlog](./features/learning-backlog.md)

10. `bbffbe5`
- Feature: added comprehensive project-driven system design mastery roadmap and linked it into the concept workflow.
- Concepts: structured tiered learning plan mapped to project execution.
- Docs: [System Design Mastery Roadmap](./system-design/mastery-roadmap.md)

11. `c57a2a9`
- Feature: added a strict 12-week execution plan mapping weekly features to system-design concepts.
- Concepts: milestone-driven learning cadence and execution discipline.
- Docs: [12-Week Execution Plan](./system-design/12-week-execution-plan.md)

12. `caa2c48`
- Feature: implemented JWT access token issuance for register/login, added token service, and Testcontainers-based integration tests.
- Concepts: stateless auth token issuance, testable auth contracts with real Postgres container.
- Docs: [Feature 02](./features/02-jwt-access-token-issuance.md), [Testing Strategy](./testing-strategy.md)

13. `7c2a703`
- Feature: enforced screenshot-proof workflow, added demo proof archives, and documented Feature 03 for visual verification.
- Concepts: evidence-based delivery, reproducible UI+backend demo verification.
- Docs: [Feature 03](./features/03-browser-demo-proof-and-visual-verification.md), [Demo Proofs](./demo-proofs/README.md), [Testing Strategy](./testing-strategy.md)

14. `8950425`
- Feature: implemented protected `GET /api/me` endpoint with JWT bearer validation, profile lookup, integration tests, and UI/API proof updates.
- Concepts: token-based endpoint protection, identity resolution from JWT subject claim.
- Docs: [Feature 04](./features/04-protected-me-endpoint.md), [Testing Strategy](./testing-strategy.md)

15. `49d6744`
- Feature: implemented refresh-token issuance/rotation with `POST /api/auth/refresh`, token-type claim enforcement, integration tests, and browser demo proof.
- Concepts: token lifecycle management, separating access vs refresh token responsibilities.
- Docs: [Feature 05](./features/05-refresh-token-flow.md), [Testing Strategy](./testing-strategy.md)

16. `a3013f2`
- Feature: implemented authenticated basic profile-page API payload (`/api/profile/me`), demo bio field, and profile-page demo proofs.
- Concepts: product-facing authenticated read model design and API/UI contract shaping.
- Docs: [Feature 06](./features/06-basic-profile-page-api-ui.md), [Testing Strategy](./testing-strategy.md)

17. `f579e42`
- Feature: implemented persisted posts with authenticated `GET /api/feed` and `POST /api/posts`, seeded feed data, proof screenshots, and local H2 runtime fallback for end-to-end verification.
- Concepts: authenticated write/read model design, incremental persistence for a social feed, practical local-environment fallback when infra is unavailable.
- Docs: [Feature 07](./features/07-posts-create-read-api-ui.md), [Testing Strategy](./testing-strategy.md), [Demo Proofs](./demo-proofs/README.md)

18. `b84b5f3`
- Feature: implemented persisted post likes with authenticated like/unlike endpoints, viewer-specific liked state in the feed payload, and updated demo proofs.
- Concepts: engagement write paths, join-table modeling for per-user actions, and feed read-model shaping for viewer-aware UI.
- Docs: [Feature 08](./features/08-post-like-unlike-api-ui.md), [Testing Strategy](./testing-strategy.md), [Demo Proofs](./demo-proofs/README.md)

19. `ed69e0a`
- Feature: implemented author-only post deletion, cleaned up likes on delete, extended proof artifacts, and documented the first destructive content action.
- Concepts: ownership-enforced mutations, destructive-action safeguards, and cleanup of dependent records during deletes.
- Docs: [Feature 09](./features/09-delete-own-post-api-ui.md), [Testing Strategy](./testing-strategy.md), [Demo Proofs](./demo-proofs/README.md)

20. `bfb4fd8`
- Feature: implemented author-only post editing with a dedicated update contract, extended proof artifacts, and documented the first ownership-enforced update flow.
- Concepts: explicit update contracts, ownership-guarded writes, and incremental UI mutation flows for content editing.
- Docs: [Feature 10](./features/10-edit-own-post-api-ui.md), [Testing Strategy](./testing-strategy.md), [Demo Proofs](./demo-proofs/README.md)

21. `1a6ec48`
- Feature: expanded the product backlog into a concrete 20+ feature Instagram roadmap with explicit next recommended steps.
- Concepts: roadmap-driven delivery, converting ad hoc planning into a sequenced product build plan.
- Docs: [Feature Learning Backlog](./features/learning-backlog.md)

22. `d2a1df3`
- Feature: implemented authenticated media upload and media serving, wired uploaded media into post creation, and captured a new proof set using real uploaded content.
- Concepts: separating media storage from post metadata, authenticated upload pipelines, and temp-backed local storage as a stepping stone to object storage.
- Docs: [Feature 11](./features/11-persist-real-feed-media-uploads.md), [Testing Strategy](./testing-strategy.md), [Demo Proofs](./demo-proofs/README.md)

23. `8137afb`
- Learning change: added a project-driven Kubernetes track, a 15-item hands-on backlog, a 12-week execution plan, and the `next kubernetes` workflow trigger.
- Concepts: containers, Kubernetes workload primitives, configuration, state, reliability, traffic, scaling, security, packaging, delivery, and production operations.
- Docs: [Kubernetes Learning Path](./kubernetes/README.md), [Kubernetes Learning Backlog](./kubernetes/learning-backlog.md), [Kubernetes Mastery Roadmap](./kubernetes/mastery-roadmap.md), [12-Week Kubernetes Execution Plan](./kubernetes/12-week-execution-plan.md)

24. `198a021`
- Learning change: required every Kubernetes lesson to explain the concept simply, identify the problem it solves, provide a project exercise, and test understanding with questions and explained solutions.
- Concepts: progressive teaching, prediction before implementation, failure-driven learning, retrieval practice, and teach-back verification.
- Docs: [Kubernetes Learning Path](./kubernetes/README.md), [Kubernetes Lesson Template](./kubernetes/_lesson-template.md), [12-Week Kubernetes Execution Plan](./kubernetes/12-week-execution-plan.md)

25. `c9007bf`
- Learning change: containerized the Spring Boot backend with a multi-stage non-root Docker image, added a Compose backend service, verified container startup, login/feed API smoke checks, and PostgreSQL recovery.
- Concepts: container images versus containers, build/runtime image separation, non-root runtime users, container-to-container networking, environment-driven configuration, and image readiness for Kubernetes Pods.
- Docs: [Kubernetes Lesson 01](./kubernetes/01-containerize-spring-boot-backend.md), [Kubernetes Learning Backlog](./kubernetes/learning-backlog.md)

26. `877ca3a`
- Learning change: expanded the first Kubernetes lesson into a beginner-focused chapter and strengthened the permanent lesson standard to require substantial question sets with explained solutions.
- Concepts: source-to-process fundamentals, JAR and JVM roles, image/container/runtime distinctions, Dockerfile layers and caching, container networking, persistent state, security boundaries, and evidence-based troubleshooting.
- Docs: [Kubernetes Lesson 01](./kubernetes/01-containerize-spring-boot-backend.md), [Kubernetes Lesson Template](./kubernetes/_lesson-template.md), [Kubernetes Learning Path](./kubernetes/README.md)

27. `8f5358d`
- Learning change: made explained answers mandatory and directly visible in every Kubernetes lesson instead of hiding them in collapsible sections.
- Concepts: accessible self-assessment, immediate feedback, and complete lesson artifacts.
- Docs: [Kubernetes Lesson 01](./kubernetes/01-containerize-spring-boot-backend.md), [Kubernetes Lesson Template](./kubernetes/_lesson-template.md), [Kubernetes Learning Path](./kubernetes/README.md)

28. `fa12b65`
- Learning change: created an isolated local kind cluster and taught Kubernetes architecture, kubectl targeting, object manifests, Pods, namespaces, labels, selectors, observation, and basic failure diagnosis through a verified disposable-Pod exercise.
- Concepts: control plane and node responsibilities, API-driven desired state, spec versus status, Pod/container lifecycle, namespace scope, label selection, `ImagePullBackOff` diagnosis, and bare-Pod ownership limitations.
- Evidence: node readiness, NGINX HTTP response, label-selector changes, invalid-image failure and recovery, bare-Pod non-recreation, server-side manifest validation, and passing backend tests.
- Docs: [Kubernetes Lesson 02](./kubernetes/02-local-cluster-kubectl-pods-labels.md), [Kubernetes Learning Backlog](./kubernetes/learning-backlog.md)

29. `ae4e310`
- Learning change: standardized Kubernetes self-checks so every question is the clickable summary of its own expandable block and its explained answer is contained inside that block.
- Concepts: active recall, optional answer reveal, question-to-explanation proximity, and consistent lesson interaction.
- Docs: [Kubernetes Lesson 01](./kubernetes/01-containerize-spring-boot-backend.md), [Kubernetes Lesson 02](./kubernetes/02-local-cluster-kubectl-pods-labels.md), [Kubernetes Lesson Template](./kubernetes/_lesson-template.md)

30. `7f1ace1`
- Learning change: redesigned the Kubernetes course for true beginners by splitting the first two modules into thirteen prerequisite-ordered micro-lessons while retaining the original long chapters as optional references.
- Concepts: progressive disclosure, vocabulary budgets, prerequisite resets, distributed retrieval practice, learner-paced stop/go gates, and separating implementation completion from demonstrated understanding.
- Structure: six container micro-lessons, seven Kubernetes-primitives micro-lessons, concise module landing pages, a reusable micro-lesson template, and a learner-progress tracker.
- Docs: [Kubernetes Module 01](./kubernetes/01-containerize-spring-boot-backend.md), [Kubernetes Module 02](./kubernetes/02-local-cluster-kubectl-pods-labels.md), [Kubernetes Learning Path](./kubernetes/README.md), [Learner Progress](./kubernetes/learner-progress.md)

31. `7340b34`
- Learning change: added one beginner-first path that interleaves Kubernetes, Terraform, AWS, and current DevOps practices without treating them as four simultaneous full courses.
- Concepts: spiral learning, explicit tool ownership, local-first Kubernetes, preview-first infrastructure as code, short-lived EKS milestone labs, cloud cost/destruction gates, CI with OIDC, GitOps, supply-chain security, observability, and separate implementation/comprehension evidence.
- Structure: 20 dependency-ordered stages, a six-part readiness module, project backlog, learner tracker, current/core-versus-later toolchain, AWS/Terraform safety rules, and the `next devops` workflow trigger.
- Docs: [Integrated DevOps Path](./devops/README.md), [Stage 00](./devops/00-readiness-and-safety.md), [20-Stage Plan](./devops/20-stage-execution-plan.md), [Toolchain](./devops/toolchain.md), [Cloud Safety](./devops/cloud-safety.md), [Learner Progress](./devops/learner-progress.md)

32. `490111a`
- Learning fix: repaired the Kubernetes Module 02 prerequisite chain so a first-time learner checks tools, creates or reuses `instagram-learning`, and prepares its isolated kubeconfig before any node or API query.
- Concepts: explicit setup preconditions, safe create-versus-reuse branching, prerequisite ordering, readiness synchronization, and restoring state after destructive learning exercises.
- Additional fixes: aligned the 02C prerequisite, added a readiness wait before `exec`, moved bare-Pod deletion after the image recovery lab, and recorded 02A as `LEARNING` rather than inferred understanding.
- Docs: [Kubernetes Module 02](./kubernetes/02-local-cluster-kubectl-pods-labels.md), [02A Cluster Setup](./kubernetes/02-local-cluster-kubectl-pods-labels/02a-why-kubernetes-cluster-node.md), [Kubernetes Learner Progress](./kubernetes/learner-progress.md)

33. `8c92af8`
- Learning change: added a Senior Java/Backend interview preparation hub with four prioritized tracks, eleven focused work areas, a measurable progress dashboard, and a 12-week execution plan.
- Concepts: outcome-based preparation, spaced retrieval, timed practice, mock-feedback loops, senior-level behavioral evidence, project deep-dives, and overlapping preparation with the application pipeline.
- Docs: [Interview Preparation Hub](./interview/README.md), [12-Week Interview Execution Plan](./interview/12-week-execution-plan.md), [Interview Progress Tracker](./interview/progress-tracker.md)

34. `4bb6135`
- Learning refactor: separated core Java/JVM preparation from Spring and Hibernate framework/ORM preparation and renumbered the supporting interview tracks.
- Concepts: language-versus-framework learning boundaries, focused readiness measurement, and easier weakness-based study selection.
- Docs: [Java Interview Questions](./interview/04-java/README.md), [Spring and Hibernate Interview Questions](./interview/05-spring-hibernate/README.md), [Interview Preparation Hub](./interview/README.md)

35. `88dabdf`
- Learning change: added an eight-story behavioral tracker and a categorized senior-engineering behavioral question bank with reusable follow-up probes.
- Concepts: STAR-style evidence, story reuse across question variants, personal-contribution clarity, measurable outcomes, technical deep-dive separation, and spoken retrieval practice.
- Docs: [Behavioral Interview Path](./interview/03-behavioral/README.md), [Eight-Story Bank](./interview/03-behavioral/story-bank.md), [Behavioral Question Bank](./interview/03-behavioral/question-bank.md)

36. `6bf8728`
- Learning change: initialized the first deep interview lesson with a timed coding baseline, Arrays/HashMap/HashSet mental models, Java implementation guidance, deliberate practice, checkpoints, and spaced reviews.
- Concepts: repeated-search elimination, membership versus key-value lookup, loop invariants, average-case hash complexity, Java collection correctness, honest baseline measurement, and retention-based progression.
- Docs: [Day 1 Plan](./interview/daily/2026-10-04-day-01.md), [Arrays and Hash-Based Lookup Module](./interview/01-coding/lessons/01-arrays-hash-maps/README.md), [Interview Progress Tracker](./interview/progress-tracker.md)

37. `ec12d0a`
- Learning fix: made runnable starter and reference Java programs mandatory for every interview coding lesson and retrofitted Lesson 01 with both artifacts.
- Concepts: executable learning materials, solution discoverability, attempt-versus-answer separation, repeatable edge-case checks, and artifact verification independent of learner mastery.
- Docs: [Interview Coding Lesson Standard](../AGENTS.md), [Sequence Boundaries Lesson](./interview/01-coding/lessons/01-arrays-hash-maps/02-sequence-boundaries/README.md), [Editable Practice Program](../src/test/java/com/instagram/backend/interview/coding/arrayshashmaps/sequenceboundaries/LongestConsecutivePractice.java), [Reference Solution](../src/test/java/com/instagram/backend/interview/coding/arrayshashmaps/sequenceboundaries/LongestConsecutiveSolution.java)

38. `b513f48`
- Learning fix: moved interview Java programs into IntelliJ’s recognized test source root, added matching packages, documented IDE execution, and made the unimplemented starter run with a clear readiness message.
- Concepts: Gradle/IntelliJ source sets, package-to-directory alignment, non-production learning code, IDE run configuration discovery, and friendly starter-program behavior.
- Verification: `./gradlew testClasses`, runnable practice main, and passing reference-solution checks.
- Docs: [Sequence Boundaries Lesson](./interview/01-coding/lessons/01-arrays-hash-maps/02-sequence-boundaries/README.md), [Editable Practice Program](../src/test/java/com/instagram/backend/interview/coding/arrayshashmaps/sequenceboundaries/LongestConsecutivePractice.java), [Reference Solution](../src/test/java/com/instagram/backend/interview/coding/arrayshashmaps/sequenceboundaries/LongestConsecutiveSolution.java)

39. `5a14ce1`
- Learning refactor: replaced the overloaded Arrays/HashMap lesson with focused micro-lessons and made HashSet membership the only active topic.
- Concepts: one-primary-idea lessons, prerequisite ordering, limited terminology, explicit stop/go gates, and matching one-problem Java packages.
- Verification: Gradle test-source compilation plus runnable starter and passing reference programs for HashSet membership and sequence boundaries.
- Docs: [Arrays and Hash-Based Lookup Module](./interview/01-coding/lessons/01-arrays-hash-maps/README.md), [HashSet Membership](./interview/01-coding/lessons/01-arrays-hash-maps/01-hashset-membership/README.md), [Day 1 Plan](./interview/daily/2026-10-04-day-01.md)

## UI Repo: `instagram-ui`

1. `1ff70f7`
- Feature: Instagram-style signup/login UI connected to backend auth APIs.
- Concepts: UI/API integration, form handling, state transitions for auth flow.
- Docs: [Authentication Diary](./diary/authentication.md)

2. `837926e`
- Feature: UI docs index, diary links, and workflow rules.
- Concepts: maintainable project workflow and navigable docs.
- Docs: [UI Docs Index](../../instagram-ui/docs/index.md)

3. `cfb1253`
- Feature: UI workflow updated to require commit-per-feature and linked shared commit map.
- Concepts: aligned multi-repo engineering conventions.
- Docs: [UI Docs Index](../../instagram-ui/docs/index.md)

4. `2c09796`
- Feature: UI docs now link directly to build and concept backlog trackers.
- Concepts: discoverability and planning visibility from either repo.
- Docs: [UI Docs Index](../../instagram-ui/docs/index.md)

5. `9e64f6e`
- Feature: UI protocol aligned with backend `next feature` / `next concept` flow.
- Concepts: multi-repo workflow consistency.
- Docs: [UI Docs Index](../../instagram-ui/docs/index.md)

6. `e3cd2cd`
- Feature: UI workflow now enforces integration/E2E + browser demo verification gates.
- Concepts: definition-of-done consistency across frontend and backend.
- Docs: [UI Docs Index](../../instagram-ui/docs/index.md)

7. `1c7ceba`
- Feature: UI docs/rules now reference backend feature-learning path instead of diary-first layout.
- Concepts: cross-repo documentation consistency.
- Docs: [UI Docs Index](../../instagram-ui/docs/index.md)

8. `da252ee`
- Feature: UI rules/index updated to new mirrored feature structure and backlog path.
- Concepts: consistent navigation between frontend and backend docs.
- Docs: [UI Docs Index](../../instagram-ui/docs/index.md)

9. `4b0ea33`
- Feature: UI docs index now links to the new system design mastery roadmap.
- Concepts: cross-repo roadmap discoverability.
- Docs: [UI Docs Index](../../instagram-ui/docs/index.md)

10. `8950e53`
- Feature: UI docs index now links to the 12-week execution plan.
- Concepts: execution-plan discoverability from frontend workflow.
- Docs: [UI Docs Index](../../instagram-ui/docs/index.md)

11. `9019d6a`
- Feature: UI login/register now consumes JWT response and stores access token for subsequent authenticated flows.
- Concepts: frontend token handling and auth-state bootstrap for protected APIs.
- Docs: [Feature 02](./features/02-jwt-access-token-issuance.md)

12. `0d40f9c`
- Feature: added Playwright-based demo proof script and UI workflow rules for mandatory screenshot evidence.
- Concepts: automated E2E evidence capture and cross-repo quality gate alignment.
- Docs: [UI Docs Index](../../instagram-ui/docs/index.md), [Feature 03](./features/03-browser-demo-proof-and-visual-verification.md)

13. `b5393e4`
- Feature: UI now loads protected `/api/me` profile after auth and displays authenticated user details.
- Concepts: frontend bearer-token API calls and protected-resource UX verification.
- Docs: [UI Docs Index](../../instagram-ui/docs/index.md), [Feature 04](./features/04-protected-me-endpoint.md)

14. `10f5c8b`
- Feature: UI now stores refresh token and supports session refresh with updated screenshot-proof automation.
- Concepts: client-side refresh-token usage and authenticated session continuity UX.
- Docs: [UI Docs Index](../../instagram-ui/docs/index.md), [Feature 05](./features/05-refresh-token-flow.md)

15. `a4ae3f9`
- Feature: UI now renders a dedicated profile-page panel backed by `/api/profile/me` response.
- Concepts: authenticated page composition and stable client model for user-profile screens.
- Docs: [UI Docs Index](../../instagram-ui/docs/index.md), [Feature 06](./features/06-basic-profile-page-api-ui.md)

16. `a4e4c00`
- Feature: UI home page now loads a real authenticated feed, supports post creation, uses screenshot-style visual assets, and updates the browser proof flow for post creation.
- Concepts: client-side feed hydration, optimistic product-facing UI composition, and frontend fallback behavior when the backend is unavailable.
- Docs: [UI Docs Index](../../instagram-ui/docs/index.md), [Feature 07](./features/07-posts-create-read-api-ui.md)

17. `340f039`
- Feature: UI now supports real like/unlike interaction on the hero post, renders liked state visually, and prefers backend login before falling back to local demo mode.
- Concepts: viewer-state-driven UI rendering, single-action mutation flows, and pragmatic frontend resilience when the API is unavailable.
- Docs: [UI Docs Index](../../instagram-ui/docs/index.md), [Feature 08](./features/08-post-like-unlike-api-ui.md)

18. `a038435`
- Feature: UI now exposes delete for user-owned hero posts and extends the browser proof run through the delete action.
- Concepts: ownership-aware action rendering and safe UX gating for destructive operations.
- Docs: [UI Docs Index](../../instagram-ui/docs/index.md), [Feature 09](./features/09-delete-own-post-api-ui.md)

19. `912b280`
- Feature: UI now supports inline editing for user-owned hero posts and extends the browser proof run through the edit step before like/delete.
- Concepts: inline content editing, localized UI state transitions, and staged mutation verification in browser automation.
- Docs: [UI Docs Index](../../instagram-ui/docs/index.md), [Feature 10](./features/10-edit-own-post-api-ui.md)

20. `7280027`
- Feature: added a dedicated UI roadmap that explicitly tracks mocked frontend areas and the steps needed to make the UI fully backend-driven.
- Concepts: frontend-specific planning, separating product roadmap from UI realism and polish work.
- Docs: [UI Docs Index](../../instagram-ui/docs/index.md), [UI Feature Roadmap](../../instagram-ui/docs/ui-feature-roadmap.md)

21. `1e07611`
- Feature: UI post creation now requires a selected media file, uploads it first, uses the returned real media URL, and updates the UI roadmap to mark media previews as no longer mocked.
- Concepts: staged frontend mutations (upload then create), file-input driven UX, and reducing mock dependencies in the product surface.
- Docs: [UI Docs Index](../../instagram-ui/docs/index.md), [UI Feature Roadmap](../../instagram-ui/docs/ui-feature-roadmap.md), [Feature 11](./features/11-persist-real-feed-media-uploads.md)

## Rule

For every new feature or learning concept:
1. commit code changes
2. update docs
3. append an entry to this map
