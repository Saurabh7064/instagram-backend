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
- Docs: [Day 1 Plan](./interview/daily/2026-10-04-day-01.md), [Arrays and Hash-Based Lookup Module](./interview/01-leetcode/lessons/01-arrays-hash-maps/README.md), [Interview Progress Tracker](./interview/progress-tracker.md)

37. `ec12d0a`
- Learning fix: made runnable starter and reference Java programs mandatory for every interview coding lesson and retrofitted Lesson 01 with both artifacts.
- Concepts: executable learning materials, solution discoverability, attempt-versus-answer separation, repeatable edge-case checks, and artifact verification independent of learner mastery.
- Docs: [Interview Coding Lesson Standard](../AGENTS.md), [Sequence Boundaries Lesson](./interview/01-leetcode/lessons/01-arrays-hash-maps/02-sequence-boundaries/README.md), [Editable Practice Program](../src/test/java/com/instagram/backend/interview/leetcode/arrayshashmaps/sequenceboundaries/LongestConsecutivePractice.java), [Reference Solution](../src/test/java/com/instagram/backend/interview/leetcode/arrayshashmaps/sequenceboundaries/LongestConsecutiveSolution.java)

38. `b513f48`
- Learning fix: moved interview Java programs into IntelliJ’s recognized test source root, added matching packages, documented IDE execution, and made the unimplemented starter run with a clear readiness message.
- Concepts: Gradle/IntelliJ source sets, package-to-directory alignment, non-production learning code, IDE run configuration discovery, and friendly starter-program behavior.
- Verification: `./gradlew testClasses`, runnable practice main, and passing reference-solution checks.
- Docs: [Sequence Boundaries Lesson](./interview/01-leetcode/lessons/01-arrays-hash-maps/02-sequence-boundaries/README.md), [Editable Practice Program](../src/test/java/com/instagram/backend/interview/leetcode/arrayshashmaps/sequenceboundaries/LongestConsecutivePractice.java), [Reference Solution](../src/test/java/com/instagram/backend/interview/leetcode/arrayshashmaps/sequenceboundaries/LongestConsecutiveSolution.java)

39. `5a14ce1`
- Learning refactor: replaced the overloaded Arrays/HashMap lesson with focused micro-lessons and made HashSet membership the only active topic.
- Concepts: one-primary-idea lessons, prerequisite ordering, limited terminology, explicit stop/go gates, and matching one-problem Java packages.
- Verification: Gradle test-source compilation plus runnable starter and passing reference programs for HashSet membership and sequence boundaries.
- Docs: [Arrays and Hash-Based Lookup Module](./interview/01-leetcode/lessons/01-arrays-hash-maps/README.md), [HashSet Membership](./interview/01-leetcode/lessons/01-arrays-hash-maps/01-hashset-membership/README.md), [Day 1 Plan](./interview/daily/2026-10-04-day-01.md)

40. `65e9658`
- Learning state change: deferred HashSet Membership at the learner’s request without awarding mastery and activated Sequence Boundaries as the only current lesson.
- Concepts: explicit deferral versus completion, learner-controlled sequencing, prerequisite bridge, and preserving honest progress evidence.
- Verification: runnable Longest Consecutive starter, passing reference solution, and resolved documentation links.
- Docs: [Sequence Boundaries](./interview/01-leetcode/lessons/01-arrays-hash-maps/02-sequence-boundaries/README.md), [Session 02 Plan](./interview/daily/2026-10-04-session-02.md), [Interview Progress Tracker](./interview/progress-tracker.md)

41. `2e4b79e`
- Learning change: added a 72-problem coding micro-lesson index across 14 prerequisite-ordered modules, with one focused idea and an official LeetCode link per lesson.
- Concepts: curriculum visibility without premature content expansion, one-active-lesson discipline, core-path versus reinforcement counts, and explicit learning-state tracking.
- Verification: 72 unique lesson rows, exactly one `LEARNING` row, one `DEFERRED` row, official LeetCode links, and resolved local documentation links.
- Docs: [Coding Micro-Lesson Index](./interview/01-leetcode/lesson-index.md), [Coding and LeetCode Path](./interview/01-leetcode/README.md)

42. `a3504d0`
- Learning change: added a supplemental Java Array + HashMap cheat sheet with a focused Two Sum warm-up, editable practice program, and separate reference solution without changing the active lesson's mastery state.
- Concepts: indexed array traversal, value-to-index lookup, complements, loop invariants, and the time-space trade-off of replacing a nested scan with a hash map.
- Verification: `./gradlew testClasses`, expected starter readiness output, passing reference-solution checks across standard, duplicate-value, and negative-value cases, and resolved local documentation links.
- Docs: [Array + HashMap Refresher](./interview/01-leetcode/references/array-hashmap-cheatsheet.md), [Editable Practice Program](../src/test/java/com/instagram/backend/interview/leetcode/arrayshashmaps/refresher/TwoSumPractice.java), [Reference Solution](../src/test/java/com/instagram/backend/interview/leetcode/arrayshashmaps/refresher/TwoSumSolution.java)

43. `918ace2`
- Learning change: expanded the Array + HashMap reference into a broad Java operation catalog and added a runnable demonstration of representative operations.
- Concepts: array creation, traversal, copying, filling, sorting, binary search, reversal, streams, list conversion, map CRUD, `computeIfAbsent`, `computeIfPresent`, `compute`, `merge`, grouping, frequency counting, map iteration, collection selection, and complexity trade-offs.
- Verification: `./gradlew testClasses`, passing `ArrayHashMapOperationsDemo`, balanced Markdown code fences, resolved runnable-demo link, and clean diff checks.
- Docs: [Array and HashMap Reference Index](./interview/01-leetcode/references/array-hashmap-cheatsheet.md)

44. `a381050`
- Learning refactor: separated the combined reference and executable example into independent Array and HashMap cheat sheets with focused runnable demos.
- Concepts: single-topic reference design, array operations and patterns, map operations and patterns, independent retrieval practice, and backward-compatible documentation navigation.
- Verification: `./gradlew testClasses`, passing `ArrayOperationsDemo`, passing `HashMapOperationsDemo`, expected Two Sum starter output, passing Two Sum reference checks, balanced Markdown code fences, and resolved local links.
- Docs: [Java Array Cheat Sheet](./interview/01-leetcode/references/array-cheatsheet.md), [Array Demo](../src/test/java/com/instagram/backend/interview/leetcode/arrayshashmaps/refresher/ArrayOperationsDemo.java), [Java HashMap Cheat Sheet](./interview/01-leetcode/references/hashmap-cheatsheet.md), [HashMap Demo](../src/test/java/com/instagram/backend/interview/leetcode/arrayshashmaps/refresher/HashMapOperationsDemo.java)

45. `120a3bc`
- Learning improvement: rewrote Array and HashMap examples to show starting state, operation meaning, exact result, mutation behavior, and the reason each result occurs.
- Concepts: half-open ranges, mutating versus value-returning operations, concrete execution tracing, array aliasing and copying, binary-search insertion points, map update semantics, lazy collection creation, and frequency merging.
- Verification: `./gradlew testClasses`, passing and output-producing Array and HashMap demos, passing Two Sum reference checks, balanced Markdown code fences, and clean diff checks.
- Docs: [Java Array Cheat Sheet](./interview/01-leetcode/references/array-cheatsheet.md), [Array Demo](../src/test/java/com/instagram/backend/interview/leetcode/arrayshashmaps/refresher/ArrayOperationsDemo.java), [Java HashMap Cheat Sheet](./interview/01-leetcode/references/hashmap-cheatsheet.md), [HashMap Demo](../src/test/java/com/instagram/backend/interview/leetcode/arrayshashmaps/refresher/HashMapOperationsDemo.java)

46. `b5d2897`
- Workflow change: configured completed generated code and learning-artifact changes to be pushed to the repository's `origin` remote after they are committed.
- Concepts: remote publication as a definition-of-done step, protecting unrelated working-tree changes, and explicit reporting for authentication, network, or divergence failures.
- Verification: target GitHub repository inspected before configuration, remote found without existing branches, and unrelated Kubernetes work excluded from the workflow commit.
- Docs: [Project Workflow Rules](../AGENTS.md)

47. `7137445`
- Learning change: built an eight-week system-design interview curriculum from the November–December 2024 question history, added a dated/routed question bank, and initialized four concept micro-lessons in the learner's why/analogy/prediction/failure/teach-back format.
- Concepts: interview framing, quality attributes, caching consistency, data partitioning and replication, Cassandra routing and hot keys, incremental monolith migration, e-commerce/payment workflows, privacy, multi-region design, and evidence-based comprehension tracking.
- Verification: 11 curriculum files checked for balanced code fences, all local Markdown links resolved, supplied system-design questions extracted, adjacent Java/Spring/coding/behavioral questions explicitly routed, and diff checks passed.
- Docs: [System Design Interview Plan](./interview/02-system-design/learning-plan.md), [Question Bank](./interview/02-system-design/question-bank-nov-dec-2024.md), [Concept Lessons](./interview/02-system-design/concepts/README.md), [Learner Progress](./interview/02-system-design/learner-progress.md)

48. `6f3f441`
- Learning change: added a standalone Java HashSet refresher with explicit input/output explanations, a runnable operations demo, and navigation to the existing Contains Duplicate practice.
- Concepts: membership, uniqueness, boolean mutation results, union/intersection/difference, bulk operations, ordering variants, conversions, value equality and hashing, duplicate detection, visited sets, and average-case complexity.
- Verification: `./gradlew testClasses`, passing output-producing `HashSetOperationsDemo`, expected Contains Duplicate starter output, passing Contains Duplicate reference checks, balanced Markdown fences, resolved local links, and clean diff checks.
- Docs: [Java HashSet Cheat Sheet](./interview/01-leetcode/references/hashset-cheatsheet.md), [HashSet Operations Demo](../src/test/java/com/instagram/backend/interview/leetcode/arrayshashmaps/refresher/HashSetOperationsDemo.java), [HashSet Membership Lesson](./interview/01-leetcode/lessons/01-arrays-hash-maps/01-hashset-membership/README.md)

49. `e2f0739`
- Learner solution: recorded and reviewed the learner's passing Contains Duplicate implementation, including correctness, complexity, covered edge cases, and non-blocking clarity/performance refinements.
- Workflow change: future completed learner solutions must be executed and documented in the lesson README, while routine terminal-run sections are omitted and code completion remains separate from comprehension/mastery.
- Verification: `./gradlew testClasses`, passing learner practice checks, passing reference-solution checks, resolved lesson links, balanced Markdown fences, and no remaining `Run from Terminal` sections under coding lessons.
- Docs: [HashSet Membership Lesson and Review](./interview/01-leetcode/lessons/01-arrays-hash-maps/01-hashset-membership/README.md), [Learner Solution](../src/test/java/com/instagram/backend/interview/leetcode/arrayshashmaps/hashsetmembership/ContainsDuplicatePractice.java), [Project Workflow Rules](../AGENTS.md)

50. `04bffa7`
- Learning improvement: expanded Sequence Boundaries into a line-by-line explanation with normal and integer-boundary dry runs, exact overflow results, boolean naming alternatives, guard-order reasoning, correctness proof, complexity accounting, and common failure modes.
- Workflow change: non-obvious code guards and API calls now require concrete inputs/results, the failure without the line, equivalent forms, and short-circuit/overflow explanations when applicable.
- Verification: `./gradlew testClasses`, expected starter readiness output, passing reference checks for ordinary, duplicate, empty, negative, lower-boundary, upper-boundary, and separated-extremes cases, balanced Markdown fences, resolved links, and clean diff checks.
- Docs: [Sequence Boundaries Lesson](./interview/01-leetcode/lessons/01-arrays-hash-maps/02-sequence-boundaries/README.md), [Practice Program](../src/test/java/com/instagram/backend/interview/leetcode/arrayshashmaps/sequenceboundaries/LongestConsecutivePractice.java), [Reference Solution](../src/test/java/com/instagram/backend/interview/leetcode/arrayshashmaps/sequenceboundaries/LongestConsecutiveSolution.java), [Project Workflow Rules](../AGENTS.md)

51. `779be3d`
- Learning change: added ten separate microservices interview answers covering incremental monolith decomposition, production observability, communication resilience, distributed transactions, bulkheads, fault-tolerant architecture, service boundaries, idempotency, contract evolution, and safe testing/deployment.
- Workflow change: system-design answers now start with why, define terms before use, include an analogy with its limits, trace a concrete flow and failure, map honestly to this project, provide trade-offs, include a genuine 2–3 minute spoken answer, and end with explained questions.
- Concepts: strangler migration, data ownership, metrics/logs/traces, timeout budgets, retry storms, circuit breakers, Saga/outbox/inbox, compensation, reconciliation, resource isolation, SLO/RTO/RPO, safe degradation, backward compatibility, contract tests, canaries, and expand-contract deployment.
- Verification: all ten files passed question/answer-structure checks, 230–360-word interview-answer checks, balanced-fence checks, local-link resolution, repository-specific technical review, and clean diff checks.
- Docs: [Microservices Interview Questions](./interview/02-system-design/questions/microservices/README.md), [November–December Question Bank](./interview/02-system-design/question-bank-nov-dec-2024.md), [Learner Progress](./interview/02-system-design/learner-progress.md), [Project Workflow Rules](../AGENTS.md)

52. `7f9140e`
- Feature: implemented a production observability baseline with safe health probes, protected diagnostics, Prometheus metrics, OpenTelemetry trace context, ECS JSON request logs, validated correlation IDs, bounded business/upload metrics, and a timed feed observation.
- Security and correctness: protected configurable Actuator paths behind a dedicated operations token, rejected unsafe separate management-port configuration, prevented duplicate ECS fields, normalized logged routes, omitted request secrets, and classified unhandled journey exceptions as failures.
- Concepts: complementary logs/metrics/traces, liveness versus readiness, W3C trace context versus correlation IDs, low-cardinality labels, histogram/SLO buckets, management endpoint isolation, and honest boundaries between in-process instrumentation and centralized observability infrastructure.
- Verification: `./gradlew test --rerun-tasks`, PostgreSQL Testcontainers integration coverage, real Boot ECS encoding, public probe and protected Prometheus `curl` checks, traced authenticated feed request, metric scrape inspection, separate-management-port fail-closed startup check, resolved documentation links, balanced Markdown fences, and clean diff checks.
- Docs: [Feature 12](./features/12-production-observability-baseline.md), [Production Observability and Debugging](./interview/02-system-design/questions/microservices/02-production-observability-and-debugging.md), [Manual HTTP Requests](../http/observability.http)

53. `2a53833`
- Learner solution: recorded and reviewed the passing Longest Consecutive implementation while preserving the learner's sequence-boundary approach.
- Learning state: code completion is `PASSING`; comprehension remains `LEARNING` until the learner explains the start condition, integer guards, and average linear-time analysis.
- Verification: `./gradlew testClasses`, all learner practice checks, all reference-solution checks, covered duplicate/empty/negative/integer-boundary cases, and clean diff checks for the committed files.
- Docs: [Sequence Boundaries Lesson and Review](./interview/01-leetcode/lessons/01-arrays-hash-maps/02-sequence-boundaries/README.md), [Learner Solution](../src/test/java/com/instagram/backend/interview/leetcode/arrayshashmaps/sequenceboundaries/LongestConsecutivePractice.java), [Interview Progress Tracker](./interview/progress-tracker.md), [Session 02 Plan](./interview/daily/2026-10-04-session-02.md)

54. `25b0ad4`
- Learning evidence: recorded the learner's first Sequence Boundaries checkpoint attempt without conflating passing code with demonstrated comprehension.
- Assessment: start identification, overflow/short-circuit reasoning, and aggregate complexity accounting each require a focused retry; the correct `length = 1` explanation was retained.
- Docs: [Sequence Boundaries Checkpoint Record](./interview/01-leetcode/lessons/01-arrays-hash-maps/02-sequence-boundaries/README.md), [Session 02 Plan](./interview/daily/2026-10-04-session-02.md)

55. `ea7e1f2`
- Learning evidence: recorded the second Sequence Boundaries checkpoint attempt.
- Assessment: aggregate average `O(n)` reasoning and `&&` evaluation order now pass; scan-start identification and the exact integer wraparound results remain for a focused retry.
- Docs: [Sequence Boundaries Checkpoint Record](./interview/01-leetcode/lessons/01-arrays-hash-maps/02-sequence-boundaries/README.md), [Session 02 Plan](./interview/daily/2026-10-04-session-02.md)

56. `5f7a8d2`
- Learning evidence: completed the initial Sequence Boundaries lesson after the learner correctly identified every scan start and both exact integer wraparound results.
- Learning state: advanced from `LEARNING` to `PRACTICING`; code and coached checkpoints pass, while mastery remains pending until successful spaced retrieval.
- Review plan: scheduled no-notes checks for 2026-10-08, 2026-10-14, and 2026-11-07.
- Docs: [Sequence Boundaries Lesson](./interview/01-leetcode/lessons/01-arrays-hash-maps/02-sequence-boundaries/README.md), [Coding Lesson Index](./interview/01-leetcode/lesson-index.md), [Interview Progress Tracker](./interview/progress-tracker.md), [Session 02 Plan](./interview/daily/2026-10-04-session-02.md)

57. `d8d8e9f`
- Learning change: activated the HashMap value-to-index micro-lesson with Two Sum as the single focused problem.
- Concepts: complement lookup, storing earlier indexes, the map invariant, distinct-index safety through lookup-before-insertion, correctness, and average `O(n)` time with `O(n)` additional space.
- Verification: `./gradlew testClasses`, expected untouched practice readiness output, passing reference checks for ordinary, duplicate, negative, and zero pairs, one active `LEARNING` coding lesson, and clean diff checks for committed files.
- Docs: [HashMap Value-to-Index Lesson](./interview/01-leetcode/lessons/01-arrays-hash-maps/03-value-to-index/README.md), [Editable Practice Program](../src/test/java/com/instagram/backend/interview/leetcode/arrayshashmaps/valuetoindex/TwoSumIndexPractice.java), [Reference Solution](../src/test/java/com/instagram/backend/interview/leetcode/arrayshashmaps/valuetoindex/TwoSumIndexSolution.java), [Session 03 Plan](./interview/daily/2026-10-07-session-03.md)

58. `6dee035`
- Learner attempt: recorded the functionally passing nested-loop Two Sum baseline without rewriting the learner's approach.
- Assessment: correctness and `O(1)` additional space pass, but worst-case `O(n²)` time does not meet the lesson's average `O(n)` HashMap target, so the lesson remains `LEARNING`.
- Verification: `./gradlew testClasses`, all learner practice checks, all reference-solution checks, and clean diff checks for committed files.
- Docs: [Two Sum Attempt Review](./interview/01-leetcode/lessons/01-arrays-hash-maps/03-value-to-index/README.md), [Learner Baseline](../src/test/java/com/instagram/backend/interview/leetcode/arrayshashmaps/valuetoindex/TwoSumIndexPractice.java), [Interview Progress Tracker](./interview/progress-tracker.md)

59. `9d29f3c`
- Learning change: verified the learner's optimized average `O(n)` Two Sum implementation and initialized the next two indexed problems without activating them early.
- Prepared lessons: Valid Anagram for frequency counting and Group Anagrams for canonical-key grouping, each with a focused lesson, editable practice program, separate reference solution, checkpoints, and direct LeetCode link.
- Navigation: added direct LeetCode links to every initialized Arrays and Hash-Based Lookup lesson while preserving Two Sum as the only `LEARNING` lesson and keeping both new lessons `TODO`.
- Verification: `./gradlew testClasses`, passing optimized Two Sum checks, expected starter output for both new practices, passing reference checks for Valid Anagram and Group Anagrams, one active `LEARNING` row, direct lesson links, and clean diff checks for committed files.
- Docs: [Frequency Counting](./interview/01-leetcode/lessons/01-arrays-hash-maps/04-frequency-counting/README.md), [Valid Anagram Practice](../src/test/java/com/instagram/backend/interview/leetcode/arrayshashmaps/frequencycounting/ValidAnagramPractice.java), [Canonical Grouping](./interview/01-leetcode/lessons/01-arrays-hash-maps/05-canonical-grouping/README.md), [Group Anagrams Practice](../src/test/java/com/instagram/backend/interview/leetcode/arrayshashmaps/canonicalgrouping/GroupAnagramsPractice.java), [Two Sum Practice](../src/test/java/com/instagram/backend/interview/leetcode/arrayshashmaps/valuetoindex/TwoSumIndexPractice.java)

60. `ea594a9`
- Curriculum organization: renamed the generic `01-coding` curriculum to `01-leetcode` and the matching Java package from `interview.coding` to `interview.leetcode`, updating all historical links, daily plans, package declarations, and workflow rules.
- New Java tracks: initialized Functional Programming Coding Skills and Multithreading from Scratch with ordered backlogs, learning rules, first micro-lessons, editable practice programs, and separate runnable references.
- Concepts: pure stream transformations without input mutation; deterministic thread lifecycle using `start` and `join` without sleep-based coordination.
- Verification: `./gradlew testClasses`, passing renamed Two Sum practice, expected readiness output for both new practices, passing functional-programming and multithreading references, zero stale old-path references, all local Markdown links resolved, and clean diff checks for committed files.
- Docs: [LeetCode Practice](./interview/01-leetcode/README.md), [Functional Programming Coding](./interview/04-java/01-functional-programming-coding/README.md), [Pure Transformations](./interview/04-java/01-functional-programming-coding/lessons/01-pure-transformations/README.md), [Multithreading from Scratch](./interview/04-java/02-multithreading-from-scratch/README.md), [Starting and Joining One Worker](./interview/04-java/02-multithreading-from-scratch/lessons/01-start-and-join/README.md)

61. `ebfeef1`
- Learning-format change: converted every initialized LeetCode, functional-programming, and multithreading micro-lesson to a question-first, concept-first structure.
- Lesson contract: each lesson now opens with the exact question, immediately states concrete concept outcomes, teaches the mental model/invariant/correctness/trade-offs, and delays runnable practice until after the conceptual explanation.
- Workflow change: updated the LeetCode standard and both Java-track rules so future lessons follow the same learning-first organization.
- Verification: all seven initialized micro-lessons passed structural checks, no legacy `Problem` or early `Runnable code` headings remain, all local Markdown links resolve, and clean diff checks passed for committed files.
- Docs: [LeetCode Module](./interview/01-leetcode/lessons/01-arrays-hash-maps/README.md), [Two Sum Concepts](./interview/01-leetcode/lessons/01-arrays-hash-maps/03-value-to-index/README.md), [Pure Transformations](./interview/04-java/01-functional-programming-coding/lessons/01-pure-transformations/README.md), [Starting and Joining One Worker](./interview/04-java/02-multithreading-from-scratch/lessons/01-start-and-join/README.md), [Project Workflow Rules](../AGENTS.md)

62. `da5b292`
- Learning improvement: added a short, traced lesson on `Map.computeIfAbsent`, showing lazy creation, reuse of an existing value, grouping into lists or sets, and caching a value calculated from its key.
- Concepts: key-to-value mapping functions, append-after-retrieval, and behavior when the mapping function returns `null`.
- Docs: [Grouping by a Canonical Key](./interview/01-leetcode/lessons/01-arrays-hash-maps/05-canonical-grouping/README.md)

63. `756d1cb`
- Learning change: initialized the next five unbuilt LeetCode lessons without activating them: Valid Palindrome, Two Sum II, Container With Most Water, 3Sum, and Maximum Average Subarray I.
- Concepts: inward boundary comparison, sorted-order candidate elimination, dominated-boundary proofs, anchor-plus-two-pointer deduplication, and fixed-size rolling-window aggregation.
- Artifacts: each lesson uses the question-first/concept-first format and includes a linked LeetCode problem, conceptual outcomes, invariant/correctness reasoning, complexity and trade-offs, an editable runnable practice class, and a separate reference solution.
- Verification: `./gradlew testClasses`, expected readiness output for all five untouched starters, passing checks for all five reference solutions, one active `LEARNING` lesson, all local Markdown links resolved, lesson-structure checks, and clean diff checks for committed files.
- Docs: [Two Pointers Module](./interview/01-leetcode/lessons/02-two-pointers/README.md), [Valid Palindrome](./interview/01-leetcode/lessons/02-two-pointers/01-valid-palindrome/README.md), [Two Sum II](./interview/01-leetcode/lessons/02-two-pointers/02-two-sum-ii/README.md), [Container With Most Water](./interview/01-leetcode/lessons/02-two-pointers/03-container-most-water/README.md), [3Sum](./interview/01-leetcode/lessons/02-two-pointers/04-three-sum/README.md), [Maximum Average Subarray I](./interview/01-leetcode/lessons/03-sliding-window/01-maximum-average-subarray/README.md)

64. `6b69ff8`
- Learning change: added four focused system-design concepts covering stateless load balancing, latency/throughput/saturation, tail latency/queueing, and backpressure/load shedding.
- Practice change: added eight manager-style production scenarios for peak latency, post-deployment regression, high p99, database-pool saturation, retry storms, queue backlog, cache stampede, and regional failure.
- Revision system: converted all ten existing microservices answers and all eight new scenarios into separate interview cards capped at 400 words, with a reusable template and three unrevealed recall questions per card.
- Learning state: artifacts are `READY`; comprehension remains `TODO` until no-notes explanation, failure prediction, mitigation choice, and trade-off evidence are demonstrated.
- Verification: 18 question files end with 3–5 explained questions; all 18 cards contain the required sections, end with exactly three unrevealed questions, and are 372–400 words; local links resolve, Markdown structures balance, and diff checks pass.
- Docs: [System Design Interview Path](./interview/02-system-design/README.md), [Concept Lessons](./interview/02-system-design/concepts/README.md), [Production Scenarios](./interview/02-system-design/questions/production-scenarios/README.md), [One-Page Revision Cards](./interview/02-system-design/revision-cards/README.md), [Learner Progress](./interview/02-system-design/learner-progress.md)

65. `e104bfd`
- Learning fix: expanded Valid Palindrome with a block-by-block explanation of pointer initialization, loop boundaries, left/right skipping, case normalization, mismatch detection, pointer movement, and the final success return.
- Explanation standard: each block now states what it evaluates, why it is needed, a concrete input and result, what fails without it, and an equivalent clearer form; three dry runs and the runnable test harness are also explained.
- Learning state: the lesson remains `TODO`; artifact improvement does not imply learner comprehension.
- Verification: `./gradlew testClasses`, expected untouched-practice readiness output, passing reference-solution checks, balanced Markdown structures, resolved lesson links, and clean diff checks.
- Docs: [Valid Palindrome](./interview/01-leetcode/lessons/02-two-pointers/01-valid-palindrome/README.md), [Practice Program](../src/test/java/com/instagram/backend/interview/leetcode/twopointers/validpalindrome/ValidPalindromePractice.java), [Reference Solution](../src/test/java/com/instagram/backend/interview/leetcode/twopointers/validpalindrome/ValidPalindromeSolution.java)

66. `f8dbec8`
- Learning fix: added a block-by-block reference-code walkthrough to every other initialized LeetCode micro-lesson, bringing all ten lessons to the same explanation standard.
- Concepts: collection initialization, loop boundaries, guards, Java collection APIs, pointer and window updates, return paths, exceptions, duplicate control, and exact integer-boundary behavior.
- Workflow change: future LeetCode artifacts are incomplete until initialization, loops, guards, transformations, branches, state updates, and terminal paths are each explained with a concrete trace, failure mode, and clearer equivalent where one exists.
- Verification: `./gradlew testClasses`, all ten reference programs passed, completed learner programs passed, untouched starter programs reported readiness, all ten lesson READMEs contain the required walkthrough, Markdown fences balance, and diff checks pass.
- Docs: [LeetCode Interview Path](./interview/01-leetcode/README.md), [Arrays and Hash Maps](./interview/01-leetcode/lessons/01-arrays-hash-maps/README.md), [Two Pointers](./interview/01-leetcode/lessons/02-two-pointers/README.md), [Sliding Window](./interview/01-leetcode/lessons/03-sliding-window/README.md), [Project Workflow Rules](../AGENTS.md)

67. `b436be4`
- Learning fix: derived every Container With Most Water example instead of presenting unexplained outputs, including the exact index, width, limiting-height, and area calculation that produces `49`.
- Trace: documented every two-pointer iteration for the standard example and showed why `bestArea` remains `49`; also derived the `1` and `16` examples.
- Workflow change: future LeetCode lessons must show named intermediate values and exact arithmetic or state transitions for example outputs.
- Verification: `./gradlew testClasses`, passing Container With Most Water reference checks, balanced Markdown fences, and clean diff checks.
- Docs: [Container With Most Water](./interview/01-leetcode/lessons/02-two-pointers/03-container-most-water/README.md), [Project Workflow Rules](../AGENTS.md)

68. `8dd8cdb`
- Learning fix: added a labeled side-view diagram showing the two selected walls, the water line, the seven-unit width, the seven-unit limiting height, and the resulting `49` square units.
- Clarification: explained why the taller wall's extra unit cannot hold more water and why interior zero-thickness lines do not subtract from the selected container's area.
- Workflow change: future spatial LeetCode lessons must include a labeled text diagram rather than relying on arithmetic alone.
- Verification: `./gradlew testClasses`, passing Container With Most Water reference checks, balanced Markdown fences, and clean diff checks.
- Docs: [Container With Most Water](./interview/01-leetcode/lessons/02-two-pointers/03-container-most-water/README.md), [Project Workflow Rules](../AGENTS.md)

69. `9be820c`
- Learning correction: removed the Container With Most Water ASCII diagram because it was less clear than the worked calculation and pointer trace.
- Workflow correction: visuals are now optional and must be accurate and materially clearer than the accompanying derivation; ASCII diagrams are not forced.
- Verification: `./gradlew testClasses`, passing Container With Most Water reference checks, confirmed diagram removal, balanced Markdown fences, and clean diff checks.
- Docs: [Container With Most Water](./interview/01-leetcode/lessons/02-two-pointers/03-container-most-water/README.md), [Project Workflow Rules](../AGENTS.md)

70. `097d2c1`
- Learning fix: derived all three 3Sum example outputs from sorted input, with exact anchor, left, right, sum, and pointer-movement decisions.
- Duplicate explanation: showed why the repeated `-1` anchor is skipped and why `[0, 0, 0]` is returned once as a unique value combination rather than once per index combination.
- Verification: `./gradlew testClasses`, passing 3Sum reference checks, balanced Markdown fences, and clean diff checks.
- Docs: [3Sum](./interview/01-leetcode/lessons/02-two-pointers/04-three-sum/README.md)

71. `8049856`
- Learning improvement: moved the 3Sum mental model ahead of the execution table and explained why fixing an anchor converts three changing values into a two-value target.
- Reasoning trace: established `anchor index < left index < right index`, explained why the first sorted value is considered first, and derived why the `-4` anchor's `-3` sum requires moving `left` rather than `right`.
- Table improvement: added the required pair sum for every anchor so pointer decisions can be compared directly with the target.
- Verification: `./gradlew testClasses`, passing 3Sum reference checks, balanced Markdown fences, and clean diff checks.
- Docs: [3Sum](./interview/01-leetcode/lessons/02-two-pointers/04-three-sum/README.md)

72. `272eb8e`
- Learning correction: replaced the premature “why choose an anchor?” explanation with prerequisite-ordered steps that first choose a fixed value, define it as the anchor, calculate the remaining pair target, and then place and move the pointers.
- Workflow change: future LeetCode lessons must introduce an action and define its term before asking why it is used; answer-finding logic must precede traces and reference code.
- Verification: `./gradlew testClasses`, passing 3Sum reference checks, balanced Markdown fences, and clean diff checks.
- Docs: [3Sum](./interview/01-leetcode/lessons/02-two-pointers/04-three-sum/README.md), [Project Workflow Rules](../AGENTS.md)

73. `5e6b00d`
- Feature: documented the interactive Markdown learning studio, added trusted directives to ten algorithm lessons and three request/incident flows, and preserved the original lesson content and paths.
- Workflow: future LeetCode and system-design lessons now add deterministic simulations only when state or component flow benefits from them.
- Verification: 7 Vitest tests, 4 Playwright tests, UI production build, `./gradlew testClasses`, 10 passing Java reference programs, HTTP 200 route check, and inspected browser proof.
- Docs: [Feature 13](./features/13-interactive-learning-studio.md), [Simulation Authoring Guide](./interview/interactive-simulation-authoring.md), [Browser Proof](./demo-proofs/2026-10-11/01-interactive-two-sum-final.png)

74. `548f0a8`
- Learning feature: initialized four prerequisite-ordered sliding-window micro-lessons with concepts, exact result derivations, simulations, block walkthroughs, pitfalls, checkpoints, and stop/go gates.
- Java practice: added separate editable and reference runners for longest unique substring, character replacement, permutation matching, and minimum covering window; all reference checks pass.
- Verification: `./gradlew testClasses`, four passing Java reference programs, 11 Vitest tests, 5 Playwright tests, UI build, HTTP 200 route check, and inspected browser proof.
- Docs: [Feature 14](./features/14-interactive-sliding-window-curriculum.md), [Sliding Window Module](./interview/01-leetcode/lessons/03-sliding-window/README.md), [Browser Proof](./demo-proofs/2026-10-12/01-longest-unique-substring-final.png)

75. `bb9bc5d`
- Learning fix: added the complete Maximum Average Subarray reference method directly to the lesson instead of showing only separate fragments.
- Navigation: added a direct clickable link to the runnable Java reference file immediately above the full method.
- Verification: `./gradlew testClasses`, passing Maximum Average reference checks, regenerated 134-document catalog, and 5 passing Playwright tests.
- Docs: [Maximum Average Subarray I](./interview/01-leetcode/lessons/03-sliding-window/01-maximum-average-subarray/README.md)

76. `edc676f`
- Learning standard: every lesson must show a complete linked reference method, while the shared UI renders one-based line numbers without changing copyable Markdown or Java.
- Verification: 11 Vitest tests, 6 Playwright tests including exact lines 1–16, successful 135-document build, HTTP 200 route check, and inspected browser proof.
- Docs: [Feature 15](./features/15-numbered-reference-solutions.md), [Project Workflow Rules](../AGENTS.md), [Browser Proof](./demo-proofs/2026-10-12/01-numbered-reference-solution.png)

77. `7e23dec`
- Learning change: added 20 indexed, manager-style system-design questions with concise but complete answers spanning requirements, scaling, data, caching, media, feeds, messaging, correctness, security, search, ranking, notifications, observability, disaster recovery, and schema evolution.
- Answer standard: every drill includes the problem, bounded vocabulary, analogy and limitation, concrete flow, honest Instagram-backend mapping, decision trade-offs, a five-part failure drill, a two-to-three-minute answer, and three explained checkpoints.
- Learning state: all 20 artifacts are `READY`; comprehension remains `TODO` until the learner answers without notes, traces the flow, predicts the failure, and defends a trade-off.
- Verification: 20 numbered answers and 20 index rows, all required sections present, exactly three final checkpoint blocks per answer, all local Markdown links resolved, and clean diff checks.
- Docs: [Short System Design Questions and Answers](./interview/02-system-design/questions/short-questions-and-answers/README.md), [System Design Interview Path](./interview/02-system-design/README.md), [Learner Progress](./interview/02-system-design/learner-progress.md), [System Design Learning Backlog](./system-design/learning-backlog.md)

78. `4b1ab3c`
- Learning change: added a dense print-first system-design revision pack with 132 concise questions and answers across 13 categories.
- Coverage tracking: added a separate printable tracker for all 380 concepts in the master index, with `Learned`, `+1`, `+7`, and `Explain` recall checkpoints plus a morning review log.
- Print artifacts: generated a five-page A4 cheat sheet and a nine-page A4 learning tracker from editable Markdown using a deterministic ReportLab generator.
- Verification: validated sequential question and concept IDs, exact PDF coverage, A4 dimensions, page counts, extracted PDF text, ASCII-safe source, clean diff checks, and visually inspected every generated page.
- Docs: [System Design Morning Print Pack](./interview/02-system-design/questions/short-questions-and-answers/toprint/README.md), [Morning Cheat Sheet](./interview/02-system-design/questions/short-questions-and-answers/toprint/system-design-morning-cheat-sheet.md), [Learning Tracker](./interview/02-system-design/questions/short-questions-and-answers/toprint/system-design-learning-tracker.md)

79. `7f1e822`
- Learning change: added 100 production-meeting system-design scenarios with exact interview prompts and 87-102 word spoken-answer models across 10 categories.
- Interview practice: each answer prioritizes evidence, customer-impact containment, bottleneck isolation, recovery, durable prevention, and an explicit trade-off rather than an unsupported first guess.
- Print artifact: generated a nine-page two-column A4 booklet with first-attempt, next-day, and seven-day recall boxes, a 60-second answer framework, and a morning review log.
- Verification: validated sequential `S001`-`S100` coverage, category count, answer length, A4 dimensions, page count, extracted PDF text, local links, ASCII-safe source, clean diff checks, and visually inspected every generated page.
- Docs: [Scenario Drills](./interview/02-system-design/questions/short-questions-and-answers/toprint/system-design-scenario-drills.md), [System Design Morning Print Pack](./interview/02-system-design/questions/short-questions-and-answers/toprint/README.md), [Learner Progress](./interview/02-system-design/learner-progress.md)

80. `90dbe0a`
- Learning change: expanded the production-meeting scenario booklet from 100 to 200 questions with new drills on coordination, service discovery, storage lifecycle, stream processing, search and ML, networking, multi-region systems, cost engineering, architecture migration, and incident leadership.
- Interview practice: added sequential `S101`-`S200` prompts whose answers consistently cover the first evidence to inspect, immediate containment, correctness boundary, durable fix, and operational trade-off.
- Print artifact: regenerated the booklet as 18 dense A4 pages and converted the final page into a full-width 22-entry morning review log with the reusable 60-second answer framework.
- Verification: validated 200 sequential IDs, 20 categories, 84-102 word answers, exact extracted PDF coverage, A4 dimensions, page count, local links, ASCII-safe source, source diff checks, and visually inspected all 18 rendered pages.
- Docs: [Scenario Drills](./interview/02-system-design/questions/short-questions-and-answers/toprint/system-design-scenario-drills.md), [System Design Morning Print Pack](./interview/02-system-design/questions/short-questions-and-answers/toprint/README.md), [Learner Progress](./interview/02-system-design/learner-progress.md)

81. `1c1a03f`
- Learning change: added a 120-concept printable system-design handbook covering foundations, consistency, transaction isolation, database and distributed lock types, data scaling, caching, messaging, resilience, APIs, coordination, observability, architecture, product-scale patterns, security, and deployments.
- Explanation standard: every concept has a 51-78 word explanation of purpose and trade-offs plus a separate 21-38 word concrete example; included dedicated entries for eventual consistency, optimistic and pessimistic locking, shared and exclusive locks, row and table locks, distributed locks, leases, and fencing tokens.
- Print artifact: generated nine dense A4 pages with two-column concept content, three recall boxes per item, pale example callouts, a concept-answer framework, and a full-page 22-entry morning review log.
- Verification: validated sequential `C001`-`C120` coverage, 12 categories, answer and example depth, exact extracted PDF content, A4 dimensions, page count, local links, ASCII-safe source, source diff checks, and visually inspected all nine rendered pages.
- Docs: [Concept Handbook](./interview/02-system-design/questions/short-questions-and-answers/toprint/system-design-concepts-with-examples.md), [System Design Morning Print Pack](./interview/02-system-design/questions/short-questions-and-answers/toprint/README.md), [Learner Progress](./interview/02-system-design/learner-progress.md)

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

22. `79974c9`, `e56ff7e`, `a9560db`
- Feature: added the `/learn` route to the existing React application, a build-time Markdown catalog, reusable deterministic simulation adapters and visualizers, responsive accessible controls, and screenshot-proof automation.
- Tests: 7 Vitest tests and 4 Playwright tests cover exact Two Sum behavior, duplicate and edge cases, every configured directive, backward/restart/keyboard navigation, existing Markdown rendering, and mobile catalog behavior.
- Generated catalog: runs automatically before development, build, lint, and test commands and is intentionally untracked so Markdown changes cannot leave a stale snapshot in Git.
- Docs: [Feature 13](./features/13-interactive-learning-studio.md), [UI Docs Index](../../instagram-ui/docs/index.md), [UI Feature Roadmap](../../instagram-ui/docs/ui-feature-roadmap.md)

23. `73496dd`
- Feature: added four algorithm-derived sliding-window adapters, trusted directive types, simulation edge tests, browser integration coverage, and reusable proof automation.
- Verification: 11 Vitest tests, 5 Playwright tests, successful production build, and zero new lint errors.
- Docs: [Feature 14](./features/14-interactive-sliding-window-curriculum.md), [UI Docs Index](../../instagram-ui/docs/index.md), [UI Feature Roadmap](../../instagram-ui/docs/ui-feature-roadmap.md)

24. `e7d8a92`
- Feature: the shared Markdown renderer now adds stable, non-selectable one-based line numbers to every multiline code block while preserving raw source for copying.
- Verification: 6 Playwright tests, successful production build, and a proof script that asserts the complete Maximum Average reference method has lines 1–16.
- Docs: [Feature 15](./features/15-numbered-reference-solutions.md), [UI Docs Index](../../instagram-ui/docs/index.md), [UI Feature Roadmap](../../instagram-ui/docs/ui-feature-roadmap.md)

## Concept folder organization — 2026-10-17

- Commit: `95dca28` — `docs: organize system design concepts by index categories`.
- Change: organized eight existing lessons under the 16 categories from the supplied index, retaining all 380 roadmap topics and updating lesson, progress, and backlog links.
- Docs: [Concept index](./interview/02-system-design/concepts/index.md), [existing lesson sequence](./interview/02-system-design/concepts/README.md).
- Verification: 120 local concept links resolve; all 380 topic numbers remain ordered and eight lesson artifacts remain. Documentation-only change; application behavior and learner statuses are unchanged.

## Rule

For every new feature or learning concept:
1. commit code changes
2. update docs
3. append an entry to this map

## Additional system-design interview answers — 2026-10-17

- Change: questions 21–23 cover concurrent writes, ambiguous create timeouts, and deadline/retry budgets.
- Concepts: database invariants, atomic idempotency records, and bounded dependency work.
- Docs: [Question index](./interview/02-system-design/questions/short-questions-and-answers/README.md).
- Verification: three complete answers, nine explained checkpoints, correct final-section ordering, and resolving local code links. Documentation only; no API or application behavior changed.
- Commit: `26fce39` — additional concurrency, idempotency, and deadline interview answers.

## Character replacement debugger trace — 2026-10-17

- Commit: `b2ab870` — full `AABABBA`, `k = 1` debugger trace.
- Concepts: sliding-window boundaries, all 26 frequency cells after every mutation, historical maximum, and local-variable timing.
- Docs and code pointers: [lesson and trace](./interview/01-leetcode/lessons/03-sliding-window/03-longest-repeating-character-replacement/README.md), [reference method](../src/test/java/com/instagram/backend/interview/leetcode/slidingwindow/characterreplacement/CharacterReplacementSolution.java).
- Verification: compiled practice and reference Java files; ran the reference runner successfully; `git diff --check` passed. Documentation-only change; no API or UI behavior changed. Feature/concept backlog and learner status remain unchanged because this expands an existing lesson without completing a new feature or demonstrating comprehension.

## API design with Spring Boot — 2026-10-17

- Commit: `dfb2050` — fifteen API design lesson artifacts with Spring Boot examples.
- Concepts: resource URLs, HTTP methods/statuses, validation, errors, identity/ownership, offset/cursor pagination, filtering, idempotency, conditional writes, private caching, compatibility, asynchronous work, and contract verification.
- Docs: [module and sequence](./interview/02-system-design/api-design/README.md), [learner evidence](./interview/02-system-design/api-design/learner-progress.md).
- Code mappings: [PostController](../src/main/java/com/instagram/backend/controller/PostController.java), [PostService](../src/main/java/com/instagram/backend/service/PostService.java), [PostRepository](../src/main/java/com/instagram/backend/repository/PostRepository.java), [correlation filter](../src/main/java/com/instagram/backend/observability/RequestCorrelationFilter.java). Proposed components are explicitly labelled in the lessons.
- Verification: 15 lesson files, 45 explained checkpoints, 92 resolving local module links, balanced code fences, required section checks, and `git diff --check`. Examples reviewed against source and linked primary HTTP/Spring references. Teaching excerpts were not compiled or exercised as implemented endpoints. Documentation only; application/API/UI behavior unchanged, so integration/API/browser verification is not applicable.
- Backlogs: artifact availability recorded in feature and system-design trackers. Feature implementation statuses remain unchanged; all new lesson learning statuses are TODO until demonstrated.

## API design depth review and applied patterns — 2026-10-17

- Commit: `541d19a` — reviewed and expanded all original 15 lessons; added five applied-pattern lessons for a 20-lesson curriculum.
- Concepts: full HTTP method semantics, POST/PUT/PATCH and patch-format comparison, atomic modification, four version-selection strategies and retirement/rollback, negotiated representations, relationship invariants, aggregation/BFF, bulk atomicity/partial success, and contract-first OpenAPI review. Existing validation, security, paging, replay, concurrency, caching, capacity, and testing chapters gained worked decisions and specific checkpoints.
- Docs: [curriculum index](./interview/02-system-design/api-design/README.md), [HTTP methods](./interview/02-system-design/api-design/02-http-methods.md), [versioning](./interview/02-system-design/api-design/13-compatibility-and-versioning.md), [review coverage and limits](./interview/02-system-design/api-design/review-notes.md).
- Project mappings: [current controller](../src/main/java/com/instagram/backend/controller/PostController.java), [required update fields](../src/main/java/com/instagram/backend/dto/UpdatePostRequest.java), [like uniqueness](../src/main/java/com/instagram/backend/domain/PostLike.java), [service mutations](../src/main/java/com/instagram/backend/service/PostService.java), and [Boot 4 integration fixture](../src/test/java/com/instagram/backend/AuthIntegrationTests.java).
- Verification: 20 ordered lessons, 100 explained checkpoints, 158 resolving local module links, five valid JSON blocks, 26 Java excerpts, required sections/final-question ordering, current-source behavior assertions, and clean `git diff --check`. Inspected Spring 7.0.3 class signatures for version configuration, GetMapping version, and CacheControl APIs. Examples remain teaching excerpts: not fully compiled or exercised as endpoints. Documentation only; application, API, and UI behavior did not change, so runtime integration/API/browser verification is not applicable to this revision.
- Backlogs/progress: both trackers and indexes updated; no feature completion or learner mastery inferred. All 20 lesson learning statuses remain TODO.

## LeetCode debugger trace standard — 2026-10-17

- Commit: `e1cc20a` — expanded the other 13 existing problem lessons; all 14 now have detailed debugger traces, including the previously completed character-replacement trace.
- Concepts: exact scalar state, pointer movements, frequency arrays, sets/maps after mutations, historical values, scoped locals, early returns, and derived answers. Wide permutation/minimum-window traces separate scalar state from collection state using matching step numbers.
- Persistent rule: [Coding explanation and debugger trace standard](../AGENTS.md#coding-explanation-and-debugger-trace-standard).
- Lesson pointers: [Arrays/hash maps](./interview/01-leetcode/lessons/01-arrays-hash-maps/README.md), [Two pointers](./interview/01-leetcode/lessons/02-two-pointers/README.md), [Sliding window](./interview/01-leetcode/lessons/03-sliding-window/README.md), [refresher variable mapping](./interview/01-leetcode/references/array-hashmap-cheatsheet.md#two-sum-debugger-trace).
- Verification: `javac` compiled all 33 LeetCode Java files into a temporary directory; all 15 reference `main` runners reported success. Verified one trace per lesson (14 total), resolving local links, and `git diff --check`. Reviewed array and map tables against reference operations, including zero-count map entries and variable scope.
- Scope/status: documentation and teaching rules only; no API/UI behavior changed. Future roadmap-only questions remain TODO and inherit the trace standard when activated. Feature/concept backlogs and learner comprehension statuses remain unchanged; unrelated Java and Kubernetes edits are excluded.

## Integrated LeetCode explanation and debugger state — 2026-10-17

- Commit: `c59c49c` — reorganized all 14 existing problem lessons so each small code block is followed by its explanation and relevant debugger state; complete reference listings follow the integrated walkthrough.
- Teaching pattern: guard -> reason and concrete branch -> allocation -> initial state -> counting loop and per-iteration collection table -> insertion/removal/comparison states -> terminal result. Updated the persistent [coding explanation standard](../AGENTS.md#coding-explanation-and-debugger-trace-standard) and [lesson format](./interview/01-leetcode/README.md#debugger-trace-format).
- Example: [Permutation in String](./interview/01-leetcode/lessons/03-sliding-window/04-permutation-in-string/README.md#block-by-block-code-walkthrough) now shows the length guard before allocations, each pattern-count iteration and full array beside the counting loop, then text-window updates beside the code that performs them. Clarified that the terminal false return is outside the outer loop.
- Other lesson pointers: [Arrays/hash maps](./interview/01-leetcode/lessons/01-arrays-hash-maps/README.md), [Two pointers](./interview/01-leetcode/lessons/02-two-pointers/README.md), [Sliding window](./interview/01-leetcode/lessons/03-sliding-window/README.md).
- Verification: all 33 Java practice/reference files compile; all 15 reference runners pass. Checked debugger annotations beside every numbered block in all 14 lessons, balanced code fences, local file links, and `git diff --check`. Reviewed counting-array and map states, early returns, and scope explanations.
- Scope: documentation-only reorganization; no API/UI behavior changes. Existing feature/concept backlog and learner statuses remain unchanged. Unrelated uncommitted Java and Kubernetes changes are excluded.

## Inputs alongside reference code — 2026-10-17

- Commit: `289f1af` — added named example inputs and expected returns as comments at the top of the same Java code block in all 15 complete reference listings across 14 existing LeetCode lessons, including the sorting alternative.
- Concepts: keep code-reading context local; distinguish method inputs and returned values from changing algorithm state. Saved this requirement in the [coding explanation standard](../AGENTS.md#coding-explanation-and-debugger-trace-standard).
- Docs: [LeetCode lessons](./interview/01-leetcode/README.md), [Permutation in String reference](./interview/01-leetcode/lessons/03-sliding-window/04-permutation-in-string/README.md#reference-approach).
- Verification: checked all 15 listings contain input/expected-return comments inside the Java block; code fences are balanced and `git diff --check` passes. Examples match the existing worked traces. Comments/documentation only; Java sources, API/UI behavior, feature/concept backlogs, and learner statuses are unchanged. Unrelated working-tree edits are excluded.

## Complete-iteration debugger tables — 2026-10-17

- Commit: `2da32ac` — all 14 existing LeetCode lessons now include complete-iteration tables: actual condition values/results, executed or skipped branches, collection/pointer changes, comparison, and next action. Nested-loop rows include repeated checks and the terminating false check.
- Concepts: distinguish false from not evaluated, explain Java short circuits and early returns, and connect before/after collection phases to actual loop indexes. Saved the preference in the [coding explanation standard](../AGENTS.md#coding-explanation-and-debugger-trace-standard).
- Docs: [lesson format](./interview/01-leetcode/README.md#debugger-trace-format), [Permutation in String](./interview/01-leetcode/lessons/03-sliding-window/04-permutation-in-string/README.md#block-by-block-code-walkthrough), [Minimum Window Substring](./interview/01-leetcode/lessons/03-sliding-window/05-minimum-window-substring/README.md#block-by-block-code-walkthrough). Both frequency-heavy lessons use actual loop indexes and phases for supporting collection tables, replacing discontinuous step-number keys.
- Verification: all 33 Java practice/reference files compile, all 15 reference runners pass; checked complete-iteration coverage in all 14 lessons, table column counts, balanced code fences, 26-cell frequency arrays, and `git diff --check`. Reviewed worked-example calculations and branch paths against reference code.
- Scope: documentation/teaching-standard changes only; no application or Java-source behavior changes. Feature/concept backlogs and learner mastery statuses remain unchanged. Unrelated working-tree edits are excluded.

## Narrated execution beside iteration values — 2026-10-17

- Commit: `e6b21cb` — added plain-language narration for each worked iteration across all 14 existing LeetCode lessons, with that iteration's value table directly afterward. The explanation names reads, calculations, mutations, evaluated or skipped branches, and the next action rather than requiring the learner to reconstruct execution from a table.
- Example: [Permutation in String](./interview/01-leetcode/lessons/03-sliding-window/04-permutation-in-string/README.md#block-by-block-code-walkthrough) now narrates input lengths, `2 > 8` false and skipped return, the two array allocations and their purposes, each pattern-count iteration, and every text iteration using numbered actions plus full before/insertion/removal array states. It explicitly explains why the early true return skips the increment and terminal false return.
- Persistent preference: [coding explanation standard](../AGENTS.md#coding-explanation-and-debugger-trace-standard); navigation: [lesson format](./interview/01-leetcode/README.md#debugger-trace-format).
- Verification: all 14 lessons contain narrated iterations; table column counts, balanced code fences, local file links, and `git diff --check` pass. Compiled all 33 Java files and ran all 15 reference runners successfully. Checked the permutation trace against the reference's exact execution order and final result.
- Scope: documentation only; no Java-source or API/UI behavior changes. Learner statuses and feature/concept backlogs remain unchanged; unrelated working-tree edits are excluded.

## Module 04: Prefix/Suffix State and Intervals — 2026-10-17

- Commit: `32a48e7` — prepared five next LeetCode lesson artifacts: Range Sum Query — Immutable, Subarray Sum Equals K, Product of Array Except Self, Merge Intervals, and Insert Interval.
- Concepts: saved boundary totals; frequencies of earlier sums; products strictly left/right without division; sorting and preserving interval coverage; insertion using before/overlap/after regions. Each note follows the narrated-execution standard with exact calculations, complete state tables, a full reference listing with adjacent inputs, separate runnable practice/reference Java files, three answered checkpoints, and stop/go criteria.
- Docs: [Module 04](./interview/01-leetcode/lessons/04-prefix-suffix-intervals/README.md), [Feature 16](./features/16-prefix-suffix-and-interval-curriculum.md), [lesson index](./interview/01-leetcode/lesson-index.md), [progress tracker](./interview/progress-tracker.md).
- Code: [eleven Java artifact/check files](../src/test/java/com/instagram/backend/interview/leetcode/prefixintervals/), [independent model verifier](../src/test/java/com/instagram/backend/interview/leetcode/prefixintervals/PrefixIntervalsVerification.java), [Playwright integration/proof script](../tools/learning/verify-prefix-interval-lessons.mjs).
- Verification: eleven new Java files compile; all five reference mains pass; all five practice mains reach their expected TODO state. Fixed-seed model checks pass 6,270 range queries, 5,500 subarray cases, 500 products, 500 merge cases, and 500 insert cases. UI catalog generation/production build pass; five browser lesson checks verify narration, checkpoint/practice links and numbered reference code, plus mobile layout. Manual learning-page HTTP request returns 200. Five proof screenshots were captured and visually checked; [exact evidence](./demo-proofs/2026-10-17/prefix-interval-verification.md).
- Status: artifacts READY; all five learning entries remain TODO; Two Sum remains the sole active lesson with checkpoints pending. Feature/concept trackers record the artifact addition without changing mastery or product implementation statuses. No API or UI renderer behavior changed; no UI tracked files or unrelated working-tree changes are included.

## Concept-first functional-programming introduction — 2026-10-17

- Commit: `68de455` — rebuilt the original pure-transformations lesson and added three prerequisite-ordered lessons: functions as values, stream transformations, and LeetCode 1929 Concatenation of Array. The original first-lesson URL and normalization Java paths remain valid; normalization moves to lesson 03 in the teaching order.
- Concepts: pure calculations and outside changes before APIs; supplied behavior via functional interfaces/lambdas/method references; map/filter traversal and result ownership; array-based functional transformation compared with a loop. Each lesson includes concept-first reasoning, small runnable programs, narrated executions and state tables, full reference code with adjacent inputs, separate practice files, explained checkpoints, and stop/go checks.
- Docs: [module](./interview/04-java/01-functional-programming-coding/README.md), [rewritten first lesson](./interview/04-java/01-functional-programming-coding/lessons/01-pure-transformations/README.md), [backlog](./interview/04-java/01-functional-programming-coding/learning-backlog.md), [Feature 17](./features/17-functional-programming-concept-first.md), [saved teaching standard](../AGENTS.md#functional-programming-lesson-standard).
- Code: [functional-programming Java programs](../src/test/java/com/instagram/backend/interview/functionalprogramming/), [independent cross-lesson verifier](../src/test/java/com/instagram/backend/interview/functionalprogramming/FunctionalProgrammingVerification.java), [browser integration/proof script](../tools/learning/verify-functional-lessons.mjs).
- Verification: all nine Java files compile; four reference mains pass; four practice mains reach their expected TODO states; independent fixed-seed checks pass 400 cases per lesson for values, source preservation, repeatability, callback choice, array ownership, normalized order, and unmodifiable list results. Production UI build passes with its large-chunk warning; all four browser lesson checks and mobile-layout checks pass, HTTP serving returns 200, and four screenshots were captured and visually checked. [Exact evidence](./demo-proofs/2026-10-17/functional-verification.md).
- Status: artifact readiness READY; all four FP lessons stay TODO and the track stays inactive. Existing LeetCode active/mastery statuses and product/concept completion statuses are unchanged. No endpoint or renderer implementation changed; unrelated working-tree edits are excluded.

## Minimum Window example continuity — 2026-10-18

- Commit: `a7bac42` — kept the conceptual introduction and block examples on the main `ADOBECODEBANC` / `ABC` input. Removed the unexplained AABC switch and used the already-listed `source="a", target="aa"` as an explicitly separate duplicate-count counterexample, then explicitly returned to the main trace.
- Concept: sets preserve distinct membership, while counts preserve repeated requirements; group count and required copies are separate quantities. The related checkpoint now identifies its separate input explicitly.
- Docs: [Minimum Window Substring](./interview/01-leetcode/lessons/03-sliding-window/05-minimum-window-substring/README.md).
- Verification: reviewed both input/count derivations; checked main-example consistency, table columns, balanced code fences, three explained checkpoints, and `git diff --check`. Documentation clarification only; no Java/API/UI behavior or learner/backlog status changed. Unrelated working-tree edits are excluded.

## Single-input lesson explanations — 2026-10-18

- Commit: `e138f4f` — saved the learner preference that conceptual explanations, walkthroughs, debugger tables, and related checkpoints use one fixed input without switching to counterexamples.
- Updated [Minimum Window Substring](./interview/01-leetcode/lessons/03-sliding-window/05-minimum-window-substring/README.md) to explain counts using two B occurrences in the same ADOBECODEBANC / ABC execution: window 3..10 has B twice; removing index 3 leaves the B at index 9. Removed the separate duplicate-target input and kept its checkpoint on the main trace.
- Verification: checked the source slices and B counts, preserved three answered checkpoints, balanced code fences, and `git diff --check`. Documentation only; no source-code behavior, learner status, or backlog completion status changes. Additional edge-case checks remain in the runnable programs.

## Restore concise Minimum Window narration — 2026-10-18

- Revert commits: `7693fea`, `28c9cc1` — reverted the latest code-anchored narration expansion and its saved preference at the learner’s request. Restored the lesson and AGENTS.md exactly to their pre-expansion state; the single-input explanation rule remains.
- Docs: [Minimum Window Substring](./interview/01-leetcode/lessons/03-sliding-window/05-minimum-window-substring/README.md).
- Verification: both restored files match commit `2ea4617`; local lesson catalog regenerated successfully. Documentation-only revert; unrelated edits and learner/backlog statuses remain unchanged.

## Brief code names in Minimum Window narration — 2026-10-18

- Commit: `338e5cf` — retained the compact paragraph format in all 13 iterations while naming windowCounts.merge, the incoming if condition, formedGroups++, the while check, and the for update. Incoming map increments show exact before/after counts; false if branches explicitly skip the increment. Saved the concise inline-code preference in AGENTS.md.
- Docs: [Minimum Window Substring](./interview/01-leetcode/lessons/03-sliding-window/05-minimum-window-substring/README.md).
- Verification: derived count changes from the fixed ADOBECODEBANC / ABC trace; checked 13 concise anchors, table columns, code fences, and git diff --check. Local lesson catalog regenerated successfully. Documentation only; no Java/API/UI behavior or learner/backlog status changes; unrelated edits excluded.

## Within-iteration debugger state — 2026-10-19

- Commit: `9c0caf3` — replaced the 13 end-only Minimum Window iteration summaries with chronological phase tables. Each row names its phase/code and shows formedGroups before/after plus the current left boundary, best range, and full window map. Narration stays compact.
- Concept: intermediate state is distinct from final state. At right=5, the incoming if increments formedGroups 2→3; after removing A, the outgoing if decrements it 3→2. Both transitions are now visible instead of only the ending 2. Saved this table preference in AGENTS.md.
- Docs: [Minimum Window Substring](./interview/01-leetcode/lessons/03-sliding-window/05-minimum-window-substring/README.md).
- Verification: derived phase states from the fixed ADOBECODEBANC / ABC execution and verified final BANC; checked 13 phase tables, both right=5 transitions, table columns, code fences, and git diff --check. Documentation only; no Java/API/renderer behavior, learner status, or backlog completion changes. Unrelated working-tree edits excluded.

## Compact debugger change paths across coding lessons — 2026-10-19

- Commit: `7814168` — applied compact main debugger tables to all 23 existing lessons (19 LeetCode and 4 functional-programming lessons). Each table has one row per iteration and three columns: iteration, changed scalar paths, changed collection-entry paths. Intermediate changes stay visible in one cell, including Minimum Window formedGroups 2→3→2. Unchanged state is omitted/marked —, absent entries and newly assigned locals are distinguished. Concise narration and useful separate reference snapshots remain.
- Replaced the expanded phase-row preference with this compact format in [AGENTS.md](../AGENTS.md#coding-explanation-and-debugger-trace-standard) and updated [LeetCode format guidance](./interview/01-leetcode/README.md#debugger-trace-format) and [functional-programming guidance](./interview/04-java/01-functional-programming-coding/README.md). No learner/mastery statuses changed.
- Verification: checked 23 lessons and 167 compact one-row tables, table columns, code fences, local links, and the Minimum Window 2→3→2 path. Compiled all 53 LeetCode/functional Java files and ran all 26 reference/verification mains successfully. Generated the UI catalog; a browser check confirms right=5 renders with exactly three columns and one data row. Captured and visually checked [compact-table proof](./demo-proofs/2026-10-19/minimum-window-compact-change-paths.png).
- Scope: documentation/preference changes only; no Java, API, or renderer implementation changes. Feature/concept completion statuses and unrelated uncommitted work remain unchanged.

## Original debugger context with in-cell update paths — 2026-10-19

- Commit: `ac2015e` — restored the original detailed debugger-table layouts across all 23 existing coding lessons. Input/index context, conditions, actions, full values/collections, and next-action columns remain. Nine existing cells across six iteration rows now show multiple successive updates in that same cell; no extra phase rows or changes-only summary replaces the original table.
- Example: the original Minimum Window formedGroups-after cell reads 2→3→2 at right=5, while every neighboring column is retained. Other hidden multi-updates, such as repeated left-pointer movement, use the same in-cell convention; existing separate before/after columns remain unchanged when they already show the phases.
- Preference: saved in [AGENTS.md](../AGENTS.md#coding-explanation-and-debugger-trace-standard); [Minimum Window lesson](./interview/01-leetcode/lessons/03-sliding-window/05-minimum-window-substring/README.md).
- Verification: compared every restored table’s headers, rows, and cells with its original version; only nine cells differ by an ordered path. Collapsing each path to its final value reproduces the original table exactly. Checked local links, table structure, code fences, and git diff --check. Regenerated the UI catalog; browser verification confirms the original nine columns, one data row, and the 2→3→2 value. Captured and visually reviewed [original-layout proof](./demo-proofs/2026-10-19/minimum-window-original-table-with-path.png).
- Scope: documentation and teaching-preference changes only. Java/API/renderer behavior, learner/backlog statuses, and unrelated working-tree edits remain unchanged.

## Explicit Minimum Window comparison checks — 2026-10-19

- Commit: `81aa7ad` — replaced unexplained shorter=true/false shorthand in the [Minimum Window lesson](./interview/01-leetcode/lessons/03-sliding-window/05-minimum-window-substring/README.md) with the actual if (candidateLength < bestLength) condition and substituted values, in both narration and existing debugger cells. Saved the rule in [AGENTS.md](../AGENTS.md#coding-explanation-and-debugger-trace-standard).
- Concept: compare each candidate against the best length at that exact moment; equality is false, and the final comparison is 4 < 5 after saving length 5.
- Verification: replayed the fixed ADOBECODEBANC / ABC example, verified all 20 comparison occurrences and final BANC, preserved table layout/code fences, passed git diff --check, and regenerated the 216-document UI catalog. Documentation only; no behavior or learner/backlog status changes. Unrelated working-tree edits excluded.

## Show best-window branch effects before removal — 2026-10-19

- Commit: `2f33f34` — the [Minimum Window lesson](./interview/01-leetcode/lessons/03-sliding-window/05-minimum-window-substring/README.md) now follows each candidate-length comparison with its executed bestStart/bestLength assignments or the skipped body and unchanged values, before removing the outgoing character. Both concise narration and original debugger cells follow this order. Saved this rule in [AGENTS.md](../AGENTS.md#coding-explanation-and-debugger-trace-standard).
- Verification: replayed ADOBECODEBANC / ABC; checked all 20 branch descriptions (three true and seven false checks, each shown twice), exact assignment values, final BANC, unchanged table structure/code fences, and git diff --check. Regenerated the 216-document lesson catalog. Documentation only; no behavior or learner/backlog status changes; unrelated edits excluded.

## Apply explicit debugging narration to every operation — 2026-10-19

- Commit: `b57fa91` — strengthened the [coding explanation rule](../AGENTS.md#coding-explanation-and-debugger-trace-standard) to require actual statement/condition → current values/result → mutation or skipped branch → next operation throughout each iteration. Unexplained removal, requirement, and final-state aliases are insufficient. Existing concise paragraphs, table columns, and in-cell change paths remain the format.
- Example: the fixed Minimum Window right=5 removal now appears in the rule with the outgoing read, merge count 1→0, left 0→1, both actual if operands, formedGroups-- 3→2, and false while recheck. Distinguishes a skipped operand from false and inner-loop exit from outer-loop termination.
- Verification: checked the example against the reference block in the [Minimum Window lesson](./interview/01-leetcode/lessons/03-sliding-window/05-minimum-window-substring/README.md); independent read-only review confirmed execution order and map-key retention. Passed git diff --check. Saved-rule update only; lesson content, application behavior, learner/backlog statuses, and unrelated edits remain unchanged.

## Explicit outgoing reads and removal execution in Minimum Window — 2026-10-19

- Commit: `e4138f5` — applied the saved narration standard to the [Minimum Window lesson](./interview/01-leetcode/lessons/03-sliding-window/05-minimum-window-substring/README.md). All ten contraction steps now name char outgoing = source.charAt(left), its actual index and assigned character, the merge count change, left++, both removal-if operands or the short circuit, the executed/skipped formedGroups--, and the next while check. Both narration and existing debugger cells are updated; original columns and one row per outer iteration remain. The removal block also explains that outgoing retains the character read before left moves and a zero-count key remains.
- Verification: replayed the fixed ADOBECODEBANC / ABC example and checked every removal, comparison, count and final BANC. Independent review confirmed all ten steps. Original table structure and unrelated cells are preserved, old removal shorthand is absent, code fences are balanced, and git diff --check passes. Compiled both practice/reference Java files and ran all five reference checks successfully.
- Browser evidence: regenerated the 216-document UI catalog, verified the updated right=5 paragraph and original nine-column single-row table at localhost:5173, and captured/visually inspected [explicit outgoing-read proof](./demo-proofs/2026-10-19/minimum-window-explicit-outgoing-read.png).
- Scope: lesson explanation and proof only; no Java/API/renderer behavior or learner/backlog statuses changed. Unrelated uncommitted work excluded.

## Boolean check and cross markers in coding explanations — 2026-10-19

- Commit: `1fc4d62` — the [Minimum Window lesson](./interview/01-leetcode/lessons/03-sliding-window/05-minimum-window-substring/README.md) now displays true ✅ and false ❌ throughout narration, conditions, debugger tables, and final guards. Retains the boolean words, adds a brief legend, and leaves skipped expressions without an icon. Saved the convention for future coding explanations in [AGENTS.md](../AGENTS.md#coding-explanation-and-debugger-trace-standard).
- Verification: independently checked all 160 existing boolean mentions plus the two legend labels for matching icons, no mismatches or decorated skipped expressions, byte-for-byte unchanged fenced code, and identical table row/column structure. Passed git diff --check. Regenerated the 216-document UI catalog; browser checks confirm the markers in right=5 narration/table and false incoming-key checks with skipped equality. Captured and visually inspected [boolean-marker proof](./demo-proofs/2026-10-19/minimum-window-boolean-check-icons.png).
- Scope: formatting/preference only; no Java/API/renderer behavior or learner/backlog statuses changed. Unrelated work excluded.

## Prefix boolean markers before conditions — 2026-10-19

- Commit: `39e04ea` — moved boolean check/cross markers before evaluated if/while guards and comparisons in the [Minimum Window lesson](./interview/01-leetcode/lessons/03-sliding-window/05-minimum-window-substring/README.md), retaining true/false in the result. All 13 narrated iterations and their original debugger rows follow this placement. Updated the legend, supporting checks, final for/sentinel checks, and [saved rule/examples](../AGENTS.md#coding-explanation-and-debugger-trace-standard).
- Concept: the marker before a compound if reflects the whole condition; incoming B at right=9 and outgoing B at left=3 have false overall conditions despite true containsKey operands. Skipped comparisons remain unmarked.
- Verification: independent review confirmed all incoming/removal/while flags and final guards, no suffix markers, unchanged Java code blocks and table structure. Passed git diff --check; regenerated the 216-document UI catalog. Browser checks confirmed prefix placement and the incoming B compound-if result. Captured and visually inspected [prefix-marker proof](./demo-proofs/2026-10-19/minimum-window-prefix-condition-icons.png). Formatting/preference only; no application behavior, learner/backlog statuses, or unrelated work changed.

## Concise execution bullets across coding lessons — 2026-10-19

- Commit: `5db80d1` — applied the learner-approved bullet narration to all 24 existing coding lessons: 19 LeetCode, four functional-programming, and the existing start/join lesson. Each iteration or callback uses concise execution-order actions with original statements/APIs, exact values and mutations, prefix condition markers, executed/skipped branches, final state/collections, and next action or return. Minimum Window right=5 follows the approved 16-bullet example. Original debugger layouts and intermediate change paths remain.
- Saved the default in [AGENTS.md](../AGENTS.md#coding-explanation-and-debugger-trace-standard), [LeetCode guidance](./interview/01-leetcode/README.md#debugger-trace-format), and [functional-programming guidance](./interview/04-java/01-functional-programming-coding/README.md). Removed conflicting dense-paragraph/numbered-narration instructions. The [Minimum Window walkthrough](./interview/01-leetcode/lessons/03-sliding-window/05-minimum-window-substring/README.md#block-by-block-code-walkthrough) is the concrete format reference.
- Clarified evaluated versus skipped checks where older table labels were misleading, including the palindrome negated character test and Three Sum anchor guards already evaluated earlier in the same scan. Thread narration explicitly describes one valid schedule and the guaranteed completion ordering, without assuming deterministic scheduling.
- Verification: validated all 24 lesson updates, 1,043 preserved Markdown table rows and column counts, byte-identical fenced code blocks, local links, learning statuses, prefix marker placement, and git diff --check. Compiled 53 committed LeetCode/functional practice/reference files from an isolated snapshot, excluding unrelated uncommitted Java, and ran all 26 reference/verification mains successfully. Compiled both start/join files and ran its reference checks successfully. Sliding-window README methods were additionally extracted, compiled, and checked against their fixed examples.
- Browser evidence: regenerated the 216-document lesson catalog; all 24 pages render bullet lists, and iteration headings precede those lists. Verified Minimum Window right=5 has all 16 approved bullets, preserved condition markers and original table. Captured and visually inspected [concise-bullet proof](./demo-proofs/2026-10-19/minimum-window-concise-bullet-execution.png).
- Scope: learning-artifact and teaching-format changes only; no application/Java behavior, learner/backlog statuses, or unrelated work changed.

## Remove repeated inner-loop execution column — 2026-10-19

- Commit: `5989529` — removed the inner-loop every-check/branch/removal column from all 13 iteration tables in the [Minimum Window lesson](./interview/01-leetcode/lessons/03-sliding-window/05-minimum-window-substring/README.md). The adjacent concise bullets already narrate that execution. Retained every other column/cell, full collection state, next action and intermediate paths such as formedGroups 2→3→2; all bullet content remains unchanged.
- Updated [AGENTS.md](../AGENTS.md#coding-explanation-and-debugger-trace-standard) and both coding-module guides: remove table columns that repeat an adjacent execution script while retaining useful state context.
- Verification: compared all removed-column tables with their prior versions; only that column differs. Non-table lesson text and fenced code are unchanged; git diff --check passes. Regenerated the UI catalog and verified all13 eight-column tables, all16 right=5 bullets and the intermediate state path in the browser. Captured and visually inspected [table-without-duplicate-column proof](./demo-proofs/2026-10-19/minimum-window-no-repeated-execution-column.png). Documentation only; no behavior, learner/backlog statuses or unrelated work changed.

## Aligned input indexes beside reference solutions — 2026-10-19

- Commit: `6088171` — added zero-based index rows directly above aligned input-value rows in reference-code comments across 18 index-based LeetCode/functional lessons. Exact input literals, other arguments and expected returns stay in the same listing. Strings retain spaces explicitly; multi-digit indexes and negative/multi-digit values align. Three Sum and Merge Intervals also show indexed sorted working data; intervals distinguish row indexes from endpoint indexes.
- Saved the convention in [AGENTS.md](../AGENTS.md#coding-explanation-and-debugger-trace-standard), retaining concise bullets, prefix boolean markers, complete state and no repeated table-script columns. See the [Minimum Window reference](./interview/01-leetcode/lessons/03-sliding-window/05-minimum-window-substring/README.md#reference-approach).
- Verification: all changes within lesson files add only comment rows; executable statements, exact input/return comments, bullets, tables and statuses are unchanged. Passed git diff --check. Regenerated the 216-document catalog; all18 guides render in the browser with inputs/expected returns. Captured and visually inspected [input-index guide proof](./demo-proofs/2026-10-19/minimum-window-reference-input-index-guide.png). Documentation only; unrelated work excluded.

## Four-number Range Sum worked example — 2026-10-21

- Commit: `5c3ca09` — changed the [Range Sum lesson](./interview/01-leetcode/lessons/04-prefix-suffix-intervals/01-range-sum-query-immutable/README.md) to use numbers=[2,4,1,7] and prefix=[0,2,6,7,14] throughout. Updated aligned index guides, allocation, all four construction iterations, query bullets/tables, reference comments, pitfalls, checkpoint arithmetic and stop/go query. Queries [0,2],[1,2],[0,3],[3,3] return7,5,14,7. All conceptual examples stay on the same fixed input.
- Updated matching sample checks in [practice](../src/test/java/com/instagram/backend/interview/leetcode/prefixintervals/rangesum/RangeSumQueryPractice.java) and [reference](../src/test/java/com/instagram/backend/interview/leetcode/prefixintervals/rangesum/RangeSumQuerySolution.java), retaining separate negative/singleton/zero/snapshot edge checks. Algorithm and learner TODO status unchanged.
- Verification: compiled both Java files; reference checks pass and unfinished practice reports its expected TODO message. Independently checked all prefix totals/query results, four construction/four query blocks, table structure, fences, links and git diff --check. Regenerated the216-document UI catalog, verified browser values and absence of old worked input, and captured/visually inspected [four-number example proof](./demo-proofs/2026-10-21/range-sum-four-number-example.png). No backend/API behavior changed; unrelated work excluded.

## Purpose and worked reasoning before formulas — 2026-10-21

- Commit: `fcd405e` — rewrote the conceptual introductions in all24 existing coding lessons (19 LeetCode, four functional-programming, one start/join). Each introduction starts with the concrete purpose, explains named saved values in learner language, derives a result on the existing fixed input, then introduces the general rule and why its terms/position changes are needed. Connected concept prose precedes the existing concise debugger bullets.
- Saved the pattern in [AGENTS.md](../AGENTS.md#coding-explanation-and-debugger-trace-standard) and both coding-module guides. The [Range Sum concept](./interview/01-leetcode/lessons/04-prefix-suffix-intervals/01-range-sum-query-immutable/README.md#first-understand-the-idea) defines prefix[k] as the sum of the first k values, derives sumRange(1,3)=14-2=12 for numbers=[2,4,1,7], then explains the general subtraction and why right+1 is needed.
- Aligned existing simulations to each lesson's exact worked input and moved them after the minimum conceptual model. Removed alternate-input example lists from introductions; stated constraints and detailed downstream explanations remain. Detailed walkthrough/reference sections, debugger tables/bullets, code/input guides, Java files and learner statuses are unchanged.
- Verification: checked all24 introductions for purpose-before-rule order and retained walkthrough/reference tails, fences, local links, statuses and simulation placement. Independently recalculated subarray count6, product outputs, window sums2/51/42, Range Sum12 and functional outputs. Compiled53 committed practice/reference Java files in an isolated snapshot excluding unrelated user edits; all26 reference/verification mains passed. Passed git diff --check.
- Browser evidence: regenerated the216-document catalog and verified all24 pages render the new concept section. Confirmed Range Sum's definition, concrete subtraction, formula and +1 explanation; captured and visually reviewed [concept-before-formula proof](./demo-proofs/2026-10-21/range-sum-concept-before-formula.png). Documentation only; no application/Java behavior, learner/backlog statuses or unrelated work changed.

## Derive the full subarray answer before optimizing — 2026-10-22

- Commit: `e7113ea` — the [Subarray Sum lesson](./interview/01-leetcode/lessons/04-prefix-suffix-intervals/02-subarray-sum-equals-k/README.md#first-understand-the-idea) now starts its conceptual explanation with the fixed indexed input and all six matching ranges, their explicit sums, and contributions2+2+2=6. Only afterward does it introduce running totals, show why repeated earlier totals represent multiple starts, name map state, and derive needed=prefixSum-target.
- Strengthened [AGENTS.md](../AGENTS.md#coding-explanation-and-debugger-trace-standard): derive the requested output before implementation-state/maps/optimized formulas; for counts, enumerate or completely group the matches instead of explaining two and asserting an unexplained final total. Updated supporting seed explanation, pitfalls and checkpoints to keep the same fixed input. Java code and algorithm unchanged.
- Verification: independently enumerated all28 non-empty ranges in the seven-value array and confirmed exactly the six shown matches; checked explanation order, unchanged Java code blocks, tables/fences/links and git diff --check. Compiled both practice/reference files and ran reference checks successfully. Regenerated216-document catalog; browser confirms six-row derivation precedes map/formula. Captured and visually inspected [output-first proof](./demo-proofs/2026-10-22/subarray-output-before-prefix-map.png). Documentation only; no learner/backlog status or unrelated work changed.

## Explicit Subarray Narrated execution label — 2026-10-22

- Commit: `eab0039` — added the visible Narrated execution introduction and fixed input/initial state immediately before the seven existing iteration bullet/table pairs in the [Subarray Sum lesson](./interview/01-leetcode/lessons/04-prefix-suffix-intervals/02-subarray-sum-equals-k/README.md). Saved the explicit label requirement in [AGENTS.md](../AGENTS.md#coding-explanation-and-debugger-trace-standard).
- Verification: confirmed one narration label, all seven iterations retained, initial values match reference initialization, and git diff --check passes. Regenerated the216-document lesson catalog. Heading/context addition only; code, tables, learner status and unrelated work unchanged.

## Six-number Product Except Self example — 2026-10-24

- Commit: `786478e` — rewrote the [Product Except Self lesson](./interview/01-leetcode/lessons/04-prefix-suffix-intervals/03-product-of-array-except-self/README.md) to use only numbers=[1,2,3,4,5,6] as its worked input. Derives all six outputs [720,360,240,180,144,120] before the general rule, labels both Narrated execution passes, and updates allocation, six iterations per pass, full state tables, terminal checks, return and aligned reference comments. Alternate-input explanations moved out of the lesson; focused pitfalls use the same input.
- Updated the matching sample check in [practice](../src/test/java/com/instagram/backend/interview/leetcode/prefixintervals/productexceptself/ProductExceptSelfPractice.java) and [reference](../src/test/java/com/instagram/backend/interview/leetcode/prefixintervals/productexceptself/ProductExceptSelfSolution.java); additional runnable edge-case checks remain. Algorithm statements and learner TODO status unchanged.
- Verification: replayed both passes and independently derived every output; compiled both Java files and passed all reference checks. Verified twelve narrated iterations, two labels, terminal checks, unchanged reference statements, absence of old input/output, tables, fences, links and git diff --check. Regenerated the216-document catalog; browser checks confirm the six-output derivation and both passes. Captured and visually inspected [six-number example proof](./demo-proofs/2026-10-24/product-except-self-six-number-example.png). No backend/API behavior or unrelated work changed.

## Kafka learning path initialization — 2026-10-24

- Prepared [Feature 18](./features/18-kafka-learning-path-initialization.md): [Kafka entry point](./kafka/README.md), 14-module roadmap/execution backlog, separate learner progress, two templates, and five foundations micro-lessons with 15 explained checkpoints.
- Concept mapping: SD-008 asynchronous processing, partition ordering, retained history, saved reading progress, and duplicate/recovery boundaries. Proposed implementation exercises map F-010 post events, F-028 notifications, F-030 processing, and SD-009 operations; no implementation or mastery status is upgraded.
- Verification and scope: [initialization evidence](./kafka/initialization-evidence.md). No runtime/API/UI behavior changed. Unrelated pre-existing work is excluded.
- Artifact commit: `c3ec6a6` — initialize the Kafka learning path and foundations lessons. Verification includes 115 local links/anchors, 11 official reference URL/fragment combinations, 15 consistent tables, 15 explained checkpoints, fixed-example arithmetic/restart checks, unchanged shared status rows, and staged whitespace checks.

## Java functional concept course — 2026-10-24

- Recreated [Feature 19](./features/19-functional-programming-concept-course.md): [28 concept lessons](./interview/04-java/01-functional-programming-coding/README.md), 84 explained checkpoints, exact runnable examples, optional separate practice, coverage tracking, and independent learner progress. Old URLs remain valid.
- Saved the learner preference in [AGENTS.md](../AGENTS.md#functional-programming-lesson-standard): Java concept lessons explain meaning, use cases, syntax/results, comparisons, and pitfalls without iteration/debugger narration or forced LeetCode exercises. Algorithm lessons keep their own standards.
- Concept mapping: Java behavior-as-values, transformation versus flattening, lazy processing, aggregation, result ownership, failure/resource boundaries, and parallelism. The existing PostService map/toList use is the current project example; no production/API/UI implementation behavior changes or mastery upgrades are claimed.
- Verification: [Java/browser/HTTP evidence](./interview/04-java/01-functional-programming-coding/verification.md), 28 compiled examples, 28 rendered concept pages, 84 expanded-answer checks, exact numbered code, map/flatMap navigation, compatibility routes, mobile layout, and reviewed screenshots. Unrelated existing edits are excluded.
- Artifact commit: `f7321b9` — rebuild functional programming as a Java concept course and persist the Java-concept teaching preference.

## Multithreading foundations before worker APIs — 2026-10-24

- Prepared [Feature 20](./features/20-multithreading-foundations.md): [seven foundation parts](./interview/04-java/02-multithreading-from-scratch/lessons/00-foundations/README.md) teach program/process, what a thread carries, main, CPU time, concurrency/parallelism, tasks, and shared objects before the revised start/join lesson. Eight authored parts contain 24 explained checkpoints.
- Concept mapping: execution ownership, private call histories versus shared objects, work versus execution path, scheduling versus parallelism, and completion/visibility handoff. The reference uses a named task without AtomicInteger/lambda prerequisites. No application concurrency policy, API contract, or learner mastery changes.
- Verification: [Java/browser/HTTP evidence](./interview/04-java/02-multithreading-from-scratch/verification.md), actual thread identity, ordinary-call/direct-run ownership, shared references, square/boundary checks, controlled unfinished-work/recovery without sleeps, eight rendered concept pages, 24 answer expansions, prerequisite navigation, and reviewed screenshots. Unrelated pre-existing edits are excluded.
- Artifact commit: `13e9b65` — teach thread execution foundations before worker APIs; Java checks and all eight concept pages pass, with reviewed browser proofs.

## Curated senior Java interview scope — 2026-10-25

- Prepared [Feature 21](./features/21-curated-interview-scope.md) and the [curated interview guide](./interview/curated-interview-guide.md): 24 core Java/Spring/data/distributed-service questions, practical Java/SQL, conditional 24-problem DSA practice, two initial designs, and format-specific expansion.
- Concept mapping: interview-format selection, prerequisite-based gap repair, trade-off/failure explanations, and evidence-based readiness. Four profiles replace a blanket LeetCode allocation; explicit defer rules limit unrelated course work.
- Updated the hub, docs index, company template, tracker targets, and precedence notices on historical plans. Existing active lessons, learner mastery, feature status rows, and concept status rows are unchanged.
- Verification: local Markdown paths/anchors, 24 unique core IDs, 24 coding representatives, all five 10-hour allocations, source attribution, and Git whitespace. Documentation only; no application/API/UI behavior changes or executable lesson additions require integration/browser checks.
- Artifact commit: `cd1364e` — curate senior Java interview preparation by round format; 214 local links/anchors and scope/allocation checks passed.

## Distributed systems fundamentals module — 2026-10-25

- Completed [Feature 22](./features/22-distributed-systems-fundamentals.md): 16 focused lessons covering all 25 planned topics, 48 explained checkpoints, project mappings, failure drills, an integrated exercise, and vocabulary recap.
- Concept mapping: partial failure, CAP/PACELC, consistency, consensus, quorums, fenced ownership, 2PC/sagas, idempotency/delivery, and logical/vector clocks. Proposed F-010/F-030 implementation remains separate; artifact READY and comprehension TODO.
- Verification: 16 lesson structures, 48 answer blocks, local links, topic coverage, worked quorum/clock calculations and Git whitespace. Documentation only; no runtime/API/UI changes or executed distributed lab claims. Unrelated pre-existing edits excluded.

- Artifact commit: `9082ac1` — complete all 25 distributed-systems topics with 16 lessons and 48 checkpoints.

## Rebuild distributed-systems teaching from first principles — 2026-10-25

- Rebuilt all 16 [Feature 22 lessons](./features/22-distributed-systems-fundamentals.md) after the original version's shallow template-driven explanations failed the learner's review. All 25 topics now have prerequisite explanations, named components, derived fixed examples, guarantee boundaries, alternatives, and recovery reasoning; 50 explained checkpoints remain unattempted.
- Added the [concept teaching-quality rule](../AGENTS.md#concept-lesson-teaching-quality-standard) and revised the [concept template](./interview/02-system-design/_concept-template.md). READY requires a recorded semantic review; link/format/topic counts alone are insufficient. Comprehension stays TODO.
- Evidence: [per-lesson teaching review and structural/arithmetic verification](./interview/02-system-design/concepts/01-distributed-systems-fundamentals/verification.md). Documentation only; no runtime/API/UI changes or executed replica-lab claims. Existing unrelated work excluded.

- Rebuild commit: `128ed36` — rebuild every lesson and persist the semantic teaching-quality gate; all document-model checks passed.
