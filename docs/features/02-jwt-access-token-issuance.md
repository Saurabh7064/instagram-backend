# 02 - JWT Access Token Issuance

## Goal

Issue a signed JWT access token from authentication endpoints so clients can call protected APIs in a stateless way.

## What was built

- Login now returns:
  - `accessToken`
  - `tokenType`
  - `expiresAt`
- Register now also returns JWT token payload.
- Token is signed with HMAC secret and includes user identity claims.

## Feature IDs covered

- `F-006` JWT access token issuance

## Implementation map

### Backend

- [TokenService.java](../../src/main/java/com/instagram/backend/service/TokenService.java)
- [AuthService.java](../../src/main/java/com/instagram/backend/service/AuthService.java)
- [LoginResponse.java](../../src/main/java/com/instagram/backend/dto/LoginResponse.java)
- [RegisterResponse.java](../../src/main/java/com/instagram/backend/dto/RegisterResponse.java)
- [application.properties](../../src/main/resources/application.properties)
- [AuthIntegrationTests.java](../../src/test/java/com/instagram/backend/AuthIntegrationTests.java)

### UI

- [App.tsx](../../../instagram-ui/src/App.tsx)

## Test evidence

- Integration test suite (with Testcontainers Postgres):
  - `./gradlew test`
- API verification:
  - Login with curl returns JWT fields (`accessToken`, `tokenType`, `expiresAt`)
  - [auth.http](../../http/auth.http)
- UI demo:
  - UI runs at `http://localhost:5173`
  - Login flow consumes token payload from backend response

## Notes

- This feature provides access token issuance only.
- Refresh token lifecycle is planned under `F-008` / `SD-003`.

## Related system design notes

- [01 - Stateless Auth and Horizontal Scaling](../system-design/01-stateless-auth-and-horizontal-scaling.md)
- [02 - Password Storage and Credential Security](../system-design/02-password-storage-and-credential-security.md)
