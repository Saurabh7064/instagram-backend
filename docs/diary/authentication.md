# Authentication

## 1) What I built

I built signup and login for the Instagram clone flow:
- `POST /api/auth/register`
- `POST /api/auth/login`
- Password storage upgraded to BCrypt hashing (`F-005`).

The backend stores users in PostgreSQL (Docker), and the UI calls these APIs.

## 2) Concepts learned

- API contract design:
  - Separate DTOs for register/login requests and responses.
- Validation:
  - Bean validation (`@NotBlank`, `@Email`, length/pattern rules) at controller boundary.
- Persistence:
  - JPA entity + repository queries for uniqueness checks and lookup by username/email.
- Error handling:
  - `409 CONFLICT` for duplicate register data, `401 UNAUTHORIZED` for invalid login.
- Cross-origin communication:
  - CORS configuration so UI (`localhost:5173`) can call backend (`localhost:8080`).
- Dev environment reliability:
  - Use Docker Postgres on a known host port (`55432`) to avoid local DB conflicts.

## 3) Why this design

I kept this version intentionally simple:
- Plain-text password for learning flow speed (not production-safe).
- Stateless login response without tokens/sessions yet.
- Single-user table and direct service logic for readability.

This made the end-to-end feature easy to understand and debug.

## 4) Implementation notes

- Register flow:
  - Validate request body.
  - Check existing email and username.
  - Save `UserAccount`.
  - Return user summary (no password).
- Login flow:
  - Accept `identifier` (email or username) + password.
  - Lookup by email first, then username.
  - Compare password.
  - Return user summary on success.
- Demo account:
  - `CommandLineRunner` seeds `demo.user/password123` if missing.

## 5) Code pointers

- Backend API and logic:
  - [AuthController.java](../../src/main/java/com/instagram/backend/controller/AuthController.java)
  - [AuthService.java](../../src/main/java/com/instagram/backend/service/AuthService.java)
  - [PasswordConfig.java](../../src/main/java/com/instagram/backend/config/PasswordConfig.java)
  - [UserAccountRepository.java](../../src/main/java/com/instagram/backend/repository/UserAccountRepository.java)
  - [UserAccount.java](../../src/main/java/com/instagram/backend/domain/UserAccount.java)
  - [RegisterRequest.java](../../src/main/java/com/instagram/backend/dto/RegisterRequest.java)
  - [RegisterResponse.java](../../src/main/java/com/instagram/backend/dto/RegisterResponse.java)
  - [LoginRequest.java](../../src/main/java/com/instagram/backend/dto/LoginRequest.java)
  - [LoginResponse.java](../../src/main/java/com/instagram/backend/dto/LoginResponse.java)
  - [CorsConfig.java](../../src/main/java/com/instagram/backend/config/CorsConfig.java)
  - [DemoUserInitializer.java](../../src/main/java/com/instagram/backend/config/DemoUserInitializer.java)
  - [application.properties](../../src/main/resources/application.properties)
  - [docker-compose.yml](../../docker-compose.yml)

- UI integration:
  - [App.tsx](../../../instagram-ui/src/App.tsx)
  - [index.css](../../../instagram-ui/src/index.css)
  - [.env.example](../../../instagram-ui/.env.example)

## 6) Commit pointers

- Backend commit:
  - `5a1d99c` - register/login API with PostgreSQL Docker setup.
- UI commit:
  - `1ff70f7` - instagram-style signup/login UI wired to backend APIs.

Full mapping:
- [Commit Map](../commit-map.md)

## 7) Pitfalls and fixes

- Problem:
  - Port collisions with local PostgreSQL on `5432` and `5433`.
- Fix:
  - Mapped Docker Postgres to `55432` and pointed backend datasource URL there.

- Problem:
  - UI was signup-only, so testing login from browser was blocked.
- Fix:
  - Added login tab and wired it to `/api/auth/login`.

## 8) Next improvements

- [ ] Hash passwords with BCrypt.
- [ ] Add JWT-based authentication.
- [ ] Add unit/integration tests for auth service and controller.
- [ ] Add rate limiting / lockout for repeated failed logins.

Related system design note:

- [01 - Stateless Auth and Horizontal Scaling](../system-design/01-stateless-auth-and-horizontal-scaling.md)
- [02 - Password Storage and Credential Security](../system-design/02-password-storage-and-credential-security.md)
