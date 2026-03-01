# 08 - Post Like/Unlike API/UI

## Goal

Add the first engagement interaction on content by allowing an authenticated user to like and unlike a post.

## What was built

- Added persisted `PostLike` entity with one-like-per-user-per-post uniqueness.
- Added authenticated `POST /api/posts/{postId}/like` endpoint.
- Added authenticated `DELETE /api/posts/{postId}/like` endpoint.
- Extended feed payload with `likedByViewer`.
- Updated UI heart button to reflect liked state and call the real API.
- Tightened login fallback so `a / a` tries the backend first, then falls back to local demo only if the backend is unreachable.

## Feature IDs covered

- `F-012` Post like/unlike API/UI

## Implementation map

### Backend

- [PostLike.java](../../src/main/java/com/instagram/backend/domain/PostLike.java)
- [PostLikeRepository.java](../../src/main/java/com/instagram/backend/repository/PostLikeRepository.java)
- [FeedPostResponse.java](../../src/main/java/com/instagram/backend/dto/FeedPostResponse.java)
- [PostService.java](../../src/main/java/com/instagram/backend/service/PostService.java)
- [PostController.java](../../src/main/java/com/instagram/backend/controller/PostController.java)
- [AuthIntegrationTests.java](../../src/test/java/com/instagram/backend/AuthIntegrationTests.java)
- [auth.http](../../http/auth.http)

### UI

- [App.tsx](../../../instagram-ui/src/App.tsx)
- [index.css](../../../instagram-ui/src/index.css)
- [demo-auth-proof.mjs](../../../instagram-ui/scripts/demo-auth-proof.mjs)

## Test evidence

- Backend compile:
  - `./gradlew classes`
- Backend integration tests:
  - `./gradlew test`
  - blocked on this machine because Docker/Testcontainers is unavailable.
- API verification (run against local H2 fallback backend):
  - `POST /api/auth/login` -> `200`
  - `POST /api/posts/1/like` -> `200`
  - `GET /api/feed` after like shows `likedByViewer: true`
  - `DELETE /api/posts/1/like` -> `200`
  - `GET /api/feed` after unlike shows `likedByViewer: false`
- Browser demo proof screenshots:
  - [01-login-page.png](../demo-proofs/2026-03-01/01-login-page.png)
  - [02-home-feed.png](../demo-proofs/2026-03-01/02-home-feed.png)
  - [03-post-created.png](../demo-proofs/2026-03-01/03-post-created.png)
  - [04-post-liked.png](../demo-proofs/2026-03-01/04-post-liked.png)

## Notes

- `likedByViewer` is intentionally part of the feed read model so the UI can render the heart state without guessing.
- The current design uses a simple join table and a materialized `likeCount` on `Post`, which is a practical stepping stone before introducing counters, denormalization jobs, or high-scale fanout concerns.

## Related system design notes

- [01 - Stateless Auth and Horizontal Scaling](../system-design/01-stateless-auth-and-horizontal-scaling.md)
