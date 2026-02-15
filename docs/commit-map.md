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

## UI Repo: `instagram-ui`

1. `1ff70f7`
- Feature: Instagram-style signup/login UI connected to backend auth APIs.
- Concepts: UI/API integration, form handling, state transitions for auth flow.
- Docs: [Authentication Diary](./diary/authentication.md)

2. `837926e`
- Feature: UI docs index, diary links, and workflow rules.
- Concepts: maintainable project workflow and navigable docs.
- Docs: [UI Docs Index](../../instagram-ui/docs/index.md)

## Rule

For every new feature or learning concept:
1. commit code changes
2. update docs
3. append an entry to this map
