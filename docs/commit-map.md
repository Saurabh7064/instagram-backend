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
