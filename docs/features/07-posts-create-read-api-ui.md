# 07 - Posts Create/Read API/UI

## Goal

Implement the first persisted social-content feature: authenticated feed read + authenticated post creation, then wire the UI to it.

## What was built

- Added persisted `Post` entity and `PostRepository`.
- Added authenticated `GET /api/feed` endpoint.
- Added authenticated `POST /api/posts` endpoint.
- Seeded posts for two users (`a`, `mira.frames`).
- Updated profile page counts to use real post counts.
- Updated UI home screen to load the real backend feed and create posts through the API when backend is available.
- Kept local demo fallback for `a / a` if backend is not reachable.

## Feature IDs covered

- `F-010` Posts create/read API/UI

## Implementation map

### Backend

- [Post.java](../../src/main/java/com/instagram/backend/domain/Post.java)
- [PostRepository.java](../../src/main/java/com/instagram/backend/repository/PostRepository.java)
- [CreatePostRequest.java](../../src/main/java/com/instagram/backend/dto/CreatePostRequest.java)
- [FeedPostResponse.java](../../src/main/java/com/instagram/backend/dto/FeedPostResponse.java)
- [PostService.java](../../src/main/java/com/instagram/backend/service/PostService.java)
- [PostController.java](../../src/main/java/com/instagram/backend/controller/PostController.java)
- [ProfileService.java](../../src/main/java/com/instagram/backend/service/ProfileService.java)
- [DemoUserInitializer.java](../../src/main/java/com/instagram/backend/config/DemoUserInitializer.java)
- [auth.http](../../http/auth.http)
- [AuthIntegrationTests.java](../../src/test/java/com/instagram/backend/AuthIntegrationTests.java)

### UI

- [App.tsx](../../../instagram-ui/src/App.tsx)
- [index.css](../../../instagram-ui/src/index.css)
- [demo-auth-proof.mjs](../../../instagram-ui/scripts/demo-auth-proof.mjs)
- [post-canyon.svg](../../../instagram-ui/public/mock/post-canyon.svg)

## Test evidence

- Backend compile:
  - `./gradlew classes`
- Backend integration tests:
  - `./gradlew test`
  - blocked on this machine because Docker/Testcontainers is unavailable.
- API verification (run against local H2 fallback backend):
  - `POST /api/auth/login` -> `200`
  - `GET /api/feed` without token -> `401`
  - `GET /api/feed` with token -> `200`
  - `POST /api/posts` with token -> `201`
  - `GET /api/feed` after create shows new post first.
- Browser demo proof screenshots:
  - [01-login-page.png](../demo-proofs/2026-03-01/01-login-page.png)
  - [02-home-feed.png](../demo-proofs/2026-03-01/02-home-feed.png)
  - [03-post-created.png](../demo-proofs/2026-03-01/03-post-created.png)

## Notes

- Proper Testcontainers integration tests remain in place but could not execute here because Docker was unavailable.
- Local runtime verification used H2 in PostgreSQL-compatibility mode to keep momentum and still validate the feature end-to-end.
- Local login was relaxed to `a / a` for fast manual testing, while the existing JWT flow remains enforced after authentication.

## Related system design notes

- [01 - Stateless Auth and Horizontal Scaling](../system-design/01-stateless-auth-and-horizontal-scaling.md)
