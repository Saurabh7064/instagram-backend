# 01 - Stateless Auth and Horizontal Scaling

## Goal

Understand why stateless auth (JWT/session token style) is important when you run multiple backend instances.

## Problem (current project state)

Right now login returns user info directly and no token/session is issued:

- [AuthController.java](../../src/main/java/com/instagram/backend/controller/AuthController.java)
- [AuthService.java](../../src/main/java/com/instagram/backend/service/AuthService.java)

This works for learning, but it is not enough for real scale.

## Core concept

When traffic grows, you run many backend instances behind a load balancer.

If authentication depends on server-local memory/session state, requests can fail when the next request hits a different instance.

Stateless auth solves this:

1. User logs in once.
2. Server returns signed token.
3. Client sends token on each request.
4. Any instance can validate token and process request.

Result: easier horizontal scaling and less sticky-session dependency.

## How this maps to your project

Current:

- Login endpoint exists.
- No token generation/verification.
- Password is plain text (learning mode).

Target architecture:

1. Store password as hash (BCrypt).
2. On successful login, issue JWT.
3. For protected APIs, validate JWT in a filter/interceptor.
4. Keep backend stateless so multiple instances behave the same.

## Design tradeoffs

- JWT pros:
  - Scales well across instances.
  - No central session store required for basic flows.
- JWT cons:
  - Token revocation is harder.
  - Must handle expiry/refresh carefully.

## First implementation tasks

- [ ] Replace plain-text password with BCrypt.
- [ ] Change login response to include token + expiry.
- [ ] Add auth filter for protected routes.
- [ ] Add one protected endpoint (for example `/api/me`).

## Code pointers for this concept

- [AuthController.java](../../src/main/java/com/instagram/backend/controller/AuthController.java)
- [AuthService.java](../../src/main/java/com/instagram/backend/service/AuthService.java)
- [UserAccount.java](../../src/main/java/com/instagram/backend/domain/UserAccount.java)
- [App.tsx](../../../instagram-ui/src/App.tsx)
