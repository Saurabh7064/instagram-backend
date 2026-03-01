# 09 - Delete Own Post API/UI

## Goal

Add the first ownership-enforced destructive action by allowing a user to delete only their own posts.

## What was built

- Added authenticated `DELETE /api/posts/{postId}` endpoint.
- Enforced author-only deletion on the backend.
- Removed likes for a post before deletion to keep the join table clean.
- Updated UI to show a `Delete` action only for posts owned by the logged-in user.
- Extended browser proof flow to create, like, and then delete the user-owned top post.

## Feature IDs covered

- `F-013` Delete own post API/UI

## Implementation map

### Backend

- [PostService.java](../../src/main/java/com/instagram/backend/service/PostService.java)
- [PostController.java](../../src/main/java/com/instagram/backend/controller/PostController.java)
- [PostLikeRepository.java](../../src/main/java/com/instagram/backend/repository/PostLikeRepository.java)
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
  - `POST /api/posts` -> `201`
  - `DELETE /api/posts/{postId}` for owned post -> `204`
  - subsequent `GET /api/feed` confirms the deleted caption is gone
- Browser demo proof screenshots:
  - [01-login-page.png](../demo-proofs/2026-03-01/01-login-page.png)
  - [02-home-feed.png](../demo-proofs/2026-03-01/02-home-feed.png)
  - [03-post-created.png](../demo-proofs/2026-03-01/03-post-created.png)
  - [04-post-liked.png](../demo-proofs/2026-03-01/04-post-liked.png)
  - [05-post-deleted.png](../demo-proofs/2026-03-01/05-post-deleted.png)

## Notes

- Ownership is enforced server-side. The UI hiding the delete button is only a convenience layer.
- This is the first destructive content action, so it establishes the pattern for future edit/delete moderation rules.

## Related system design notes

- [01 - Stateless Auth and Horizontal Scaling](../system-design/01-stateless-auth-and-horizontal-scaling.md)
