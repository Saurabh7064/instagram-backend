# Commit Map (Feature -> Concept)

This file maps each commit to:
- feature delivered
- system/design concept learned
- where to read more

## Backend Repo: `instagram-backend`

1. `5a1d99c`
- Feature: register/login API, PostgreSQL Docker setup, CORS, demo user seed.
- Concepts: API contract design, persistence with JPA, local infra with Docker, cross-origin integration.
- Docs: [Authentication Diary](./diary/authentication.md)

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
- Docs: [Feature Backlog](./feature-backlog.md), [System Design Learning Backlog](./system-design/learning-backlog.md)

5. `b76ca3d`
- Feature: added strict `next feature` and `next concept` command protocol with pre-check and completion gates.
- Concepts: reliable iterative delivery, no-skip workflow control.
- Docs: [Feature Backlog](./feature-backlog.md), [System Design Learning Backlog](./system-design/learning-backlog.md)

6. `305b6b4`
- Feature: implemented BCrypt hashing for registration/login with legacy password migration path.
- Concepts: secure credential storage, backward-safe authentication upgrades.
- Docs: [Authentication Diary](./diary/authentication.md), [Concept 02](./system-design/02-password-storage-and-credential-security.md)

7. `ef032c9`
- Feature: added mandatory testing policy, testing strategy doc, and executable HTTP request file.
- Concepts: testability gates, repeatable verification workflow.
- Docs: [Testing Strategy](./testing-strategy.md)

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

## Rule

For every new feature or learning concept:
1. commit code changes
2. update docs
3. append an entry to this map
