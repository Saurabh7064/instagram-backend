# Feature Backlog (Build Track)

Use this file to track product/engineering features for the app.

## Status legend

- `TODO` not started
- `IN_PROGRESS` currently building
- `DONE` completed and documented

## Current scope

| ID | Feature | Status | Why it matters | Code/Doc link |
|---|---|---|---|---|
| F-001 | Register API | DONE | Basic account creation | [Auth Diary](./diary/authentication.md) |
| F-002 | Login API | DONE | Basic authentication flow | [Auth Diary](./diary/authentication.md) |
| F-003 | Signup UI | DONE | Browser registration flow | [UI App.tsx](../../instagram-ui/src/App.tsx) |
| F-004 | Login UI | DONE | Browser login flow | [UI App.tsx](../../instagram-ui/src/App.tsx) |
| F-005 | Password hashing (BCrypt) | DONE | Security baseline | [AuthService.java](../src/main/java/com/instagram/backend/service/AuthService.java), [PasswordConfig.java](../src/main/java/com/instagram/backend/config/PasswordConfig.java) |
| F-006 | JWT access token issuance | TODO | Stateless auth for scale | [Concept 01](./system-design/01-stateless-auth-and-horizontal-scaling.md) |
| F-007 | Protected endpoint (`/api/me`) | TODO | Validate auth end-to-end |  |
| F-008 | Refresh token flow | TODO | Better session UX/security |  |
| F-009 | Basic profile page API/UI | TODO | First authenticated product feature |  |
| F-010 | Posts create/read API/UI | TODO | Core social functionality |  |

## Next 3 recommended

1. `F-006` JWT access token issuance
2. `F-007` Protected endpoint (`/api/me`)
3. `F-008` Refresh token flow
