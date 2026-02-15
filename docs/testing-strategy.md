# Testing Strategy (Mandatory for Every Feature)

## Rules

For each feature:

1. Add or update at least one **integration test**.
2. Verify backend API with either:
   - `curl` command, or
   - `.http` file request.
3. If feature touches UI + backend:
   - run app end-to-end,
   - verify in browser (`http://localhost:5173`),
   - record demo proof in diary/notes.

## Tech Stack

### Backend integration tests

- Spring Boot Test (`@SpringBootTest`)
- Testcontainers (PostgreSQL container)
- JUnit 5
- MockMvc for HTTP-level assertions

Why:
- Uses a real Postgres-like environment via container.
- Catches DB/config issues earlier than pure mocks.

### UI tests

- Playwright (E2E browser tests)
- Optional component tests: Vitest + React Testing Library

Why:
- Playwright validates real browser flow across UI+API.
- Component tests keep UI behavior checks fast.

## Minimum acceptance checklist

- [ ] Integration test added/updated and passing
- [ ] API verified by `curl` or `.http`
- [ ] If UI impacted: browser demo verified
- [ ] Diary updated with test evidence
- [ ] Commit map updated
