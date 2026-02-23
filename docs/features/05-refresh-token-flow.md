# 05 - Refresh Token Flow

## Goal

Enable session continuation by exchanging a valid refresh token for a new access token (and rotated refresh token) without re-entering credentials.

## What was built

- Login/register now return both `accessToken` and `refreshToken` with separate expirations.
- Added `POST /api/auth/refresh` to issue a fresh access/refresh pair.
- Added token-type claim enforcement:
  - `type=access` for protected APIs (`/api/me`)
  - `type=refresh` for refresh endpoint.
- Updated UI to store refresh token and trigger session refresh.

## Feature IDs covered

- `F-008` Refresh token flow

## Implementation map

### Backend

- [AuthController.java](../../src/main/java/com/instagram/backend/controller/AuthController.java)
- [AuthService.java](../../src/main/java/com/instagram/backend/service/AuthService.java)
- [TokenService.java](../../src/main/java/com/instagram/backend/service/TokenService.java)
- [LoginResponse.java](../../src/main/java/com/instagram/backend/dto/LoginResponse.java)
- [RegisterResponse.java](../../src/main/java/com/instagram/backend/dto/RegisterResponse.java)
- [RefreshRequest.java](../../src/main/java/com/instagram/backend/dto/RefreshRequest.java)
- [RefreshResponse.java](../../src/main/java/com/instagram/backend/dto/RefreshResponse.java)
- [application.properties](../../src/main/resources/application.properties)
- [AuthIntegrationTests.java](../../src/test/java/com/instagram/backend/AuthIntegrationTests.java)
- [auth.http](../../http/auth.http)

### UI

- [App.tsx](../../../instagram-ui/src/App.tsx)
- [demo-auth-proof.mjs](../../../instagram-ui/scripts/demo-auth-proof.mjs)

## Test evidence

- Backend integration tests:
  - `./gradlew test`
  - includes refresh success, refresh misuse rejection, and refreshed-token `/api/me` access.
- API verification:
  - login returns refresh token (`200`)
  - `POST /api/auth/refresh` with refresh token returns new token pair (`200`)
  - `GET /api/me` with refreshed access token returns profile (`200`)
- Browser demo proof screenshots:
  - [01-login-page.png](../demo-proofs/2026-02-23/01-login-page.png)
  - [02-login-success.png](../demo-proofs/2026-02-23/02-login-success.png)
  - [03-refresh-success.png](../demo-proofs/2026-02-23/03-refresh-success.png)

## Notes

- This implementation is stateless and rotates refresh tokens on each refresh call.
- Server-side token revocation/blacklisting is not yet implemented.

## Related system design notes

- [01 - Stateless Auth and Horizontal Scaling](../system-design/01-stateless-auth-and-horizontal-scaling.md)
