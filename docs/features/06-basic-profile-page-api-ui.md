# 06 - Basic Profile Page API/UI

## Goal

Ship the first authenticated product screen: a basic profile page backed by an authenticated API.

## What was built

- Added `GET /api/profile/me` endpoint for profile-page data.
- Extended user model with profile `bio` field.
- Seeded demo user bio for realistic profile rendering.
- Updated UI to show a dedicated profile page panel with:
  - avatar
  - username/full name
  - bio
  - joined date
  - placeholder counters (posts/followers/following)

## Feature IDs covered

- `F-009` Basic profile page API/UI

## Implementation map

### Backend

- [UserAccount.java](../../src/main/java/com/instagram/backend/domain/UserAccount.java)
- [DemoUserInitializer.java](../../src/main/java/com/instagram/backend/config/DemoUserInitializer.java)
- [ProfileController.java](../../src/main/java/com/instagram/backend/controller/ProfileController.java)
- [ProfileService.java](../../src/main/java/com/instagram/backend/service/ProfileService.java)
- [ProfilePageResponse.java](../../src/main/java/com/instagram/backend/dto/ProfilePageResponse.java)
- [AuthIntegrationTests.java](../../src/test/java/com/instagram/backend/AuthIntegrationTests.java)
- [auth.http](../../http/auth.http)

### UI

- [App.tsx](../../../instagram-ui/src/App.tsx)
- [index.css](../../../instagram-ui/src/index.css)
- [demo-auth-proof.mjs](../../../instagram-ui/scripts/demo-auth-proof.mjs)

## Test evidence

- Backend integration tests:
  - `./gradlew test`
  - includes `/api/profile/me` authorized and unauthorized coverage.
- API verification:
  - `GET /api/profile/me` without token -> `401`
  - login + bearer token + `GET /api/profile/me` -> `200` with profile page payload.
- Browser demo proof screenshots:
  - [01-login-page.png](../demo-proofs/2026-02-24/01-login-page.png)
  - [02-login-success.png](../demo-proofs/2026-02-24/02-login-success.png)
  - [03-refresh-success.png](../demo-proofs/2026-02-24/03-refresh-success.png)
  - [04-profile-page.png](../demo-proofs/2026-02-24/04-profile-page.png)

## Notes

- Counts are currently placeholders (`0`) and will be sourced from posts/follow relationships in later features.

## Related system design notes

- [01 - Stateless Auth and Horizontal Scaling](../system-design/01-stateless-auth-and-horizontal-scaling.md)
