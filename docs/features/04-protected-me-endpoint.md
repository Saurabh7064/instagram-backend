# 04 - Protected `/api/me` Endpoint

## Goal

Add a JWT-protected endpoint so the app can prove authenticated identity end-to-end after login.

## What was built

- Added `GET /api/me` endpoint that requires `Authorization: Bearer <token>`.
- Added token parsing/verification flow in backend to validate bearer tokens.
- Added profile service that maps token subject to a real user record.
- Updated UI to call `/api/me` after login/register and show current profile data.

## Feature IDs covered

- `F-007` Protected endpoint (`/api/me`)

## Implementation map

### Backend

- [ProfileController.java](../../src/main/java/com/instagram/backend/controller/ProfileController.java)
- [ProfileService.java](../../src/main/java/com/instagram/backend/service/ProfileService.java)
- [TokenService.java](../../src/main/java/com/instagram/backend/service/TokenService.java)
- [MeResponse.java](../../src/main/java/com/instagram/backend/dto/MeResponse.java)
- [AuthIntegrationTests.java](../../src/test/java/com/instagram/backend/AuthIntegrationTests.java)
- [auth.http](../../http/auth.http)

### UI

- [App.tsx](../../../instagram-ui/src/App.tsx)
- [index.css](../../../instagram-ui/src/index.css)
- [demo-auth-proof.mjs](../../../instagram-ui/scripts/demo-auth-proof.mjs)

## Test evidence

- Backend integration tests:
  - `./gradlew test`
  - includes `/api/me` cases for missing token, invalid token, and valid token.
- API verification:
  - `curl http://localhost:8080/api/me` returns `401` without token.
  - login + bearer token call to `/api/me` returns `200` with user profile JSON.
- Browser demo proof screenshots:
  - [01-login-page.png](../demo-proofs/2026-02-23/01-login-page.png)
  - [02-login-success.png](../demo-proofs/2026-02-23/02-login-success.png)

## Notes

- `/api/me` returns identity/profile fields only (`id`, `fullName`, `username`, `email`, `createdAt`).
- Token validation is stateless and based on JWT signature + claims.

## Related system design notes

- [01 - Stateless Auth and Horizontal Scaling](../system-design/01-stateless-auth-and-horizontal-scaling.md)
