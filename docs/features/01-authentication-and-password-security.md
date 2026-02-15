# 01 - Authentication and Password Security

## Goal

Implement a full auth baseline for this project:
- register API
- login API
- UI signup/login flow
- BCrypt password storage

## What was built

- Backend auth endpoints:
  - `POST /api/auth/register`
  - `POST /api/auth/login`
- UI auth screens (signup + login)
- BCrypt hashing for new passwords
- Legacy plain-text password migration on successful login

## Feature IDs covered

- `F-001` Register API
- `F-002` Login API
- `F-003` Signup UI
- `F-004` Login UI
- `F-005` Password hashing (BCrypt)

## Implementation map

### Backend

- [AuthController.java](../../src/main/java/com/instagram/backend/controller/AuthController.java)
- [AuthService.java](../../src/main/java/com/instagram/backend/service/AuthService.java)
- [PasswordConfig.java](../../src/main/java/com/instagram/backend/config/PasswordConfig.java)
- [UserAccount.java](../../src/main/java/com/instagram/backend/domain/UserAccount.java)
- [UserAccountRepository.java](../../src/main/java/com/instagram/backend/repository/UserAccountRepository.java)
- [DemoUserInitializer.java](../../src/main/java/com/instagram/backend/config/DemoUserInitializer.java)
- [application.properties](../../src/main/resources/application.properties)
- [docker-compose.yml](../../docker-compose.yml)

### UI

- [App.tsx](../../../instagram-ui/src/App.tsx)
- [index.css](../../../instagram-ui/src/index.css)
- [.env.example](../../../instagram-ui/.env.example)

## Test evidence

- Integration/unit suite run:
  - `./gradlew test`
- API verification:
  - `curl -X POST http://localhost:8080/api/auth/login ...`
  - [http/auth.http](../../http/auth.http)
- UI demo:
  - open `http://localhost:5173` and verify signup/login flow

## Commits

- Backend:
  - `5a1d99c` register/login API + postgres setup
  - `305b6b4` BCrypt implementation + migration path
- UI:
  - `1ff70f7` signup/login UI integration

## Related system design notes

- [01 - Stateless Auth and Horizontal Scaling](../system-design/01-stateless-auth-and-horizontal-scaling.md)
- [02 - Password Storage and Credential Security](../system-design/02-password-storage-and-credential-security.md)
