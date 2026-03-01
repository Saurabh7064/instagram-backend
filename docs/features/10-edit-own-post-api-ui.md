# 10 - Edit Own Post API/UI

## Goal

Add the first ownership-enforced update flow by letting a user edit the caption and location label of their own post.

## What was built

- Added authenticated `PUT /api/posts/{postId}` endpoint.
- Enforced author-only editing on the backend.
- Added a dedicated `UpdatePostRequest` contract instead of overloading create.
- Updated UI to show inline edit controls for user-owned hero posts.
- Extended browser proof flow to create, edit, like, and delete the user-owned top post.

## Feature IDs covered

- `F-014` Edit own post API/UI

## Implementation map

### Backend

- [UpdatePostRequest.java](../../src/main/java/com/instagram/backend/dto/UpdatePostRequest.java)
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
  - `POST /api/posts` -> `201`
  - `PUT /api/posts/{postId}` for owned post -> `200`
  - `DELETE /api/posts/{postId}` cleanup after edit -> `204`
- Browser demo proof screenshots:
  - [01-login-page.png](../demo-proofs/2026-03-01/01-login-page.png)
  - [02-home-feed.png](../demo-proofs/2026-03-01/02-home-feed.png)
  - [03-post-created.png](../demo-proofs/2026-03-01/03-post-created.png)
  - [04-post-edited.png](../demo-proofs/2026-03-01/04-post-edited.png)
  - [05-post-liked.png](../demo-proofs/2026-03-01/05-post-liked.png)
  - [06-post-deleted.png](../demo-proofs/2026-03-01/06-post-deleted.png)

## Notes

- Edit and delete now share the same ownership enforcement pattern on the backend.
- The UI uses a small inline editor for the hero post only, which keeps the state model simple while proving the full update path.

## Related system design notes

- [01 - Stateless Auth and Horizontal Scaling](../system-design/01-stateless-auth-and-horizontal-scaling.md)
