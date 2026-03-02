# 11 - Persist Real Feed Media Uploads

## Goal

Replace the hardcoded placeholder post media path with a real authenticated upload flow and a servable media URL.

## What was built

- Added authenticated `POST /api/media` upload endpoint.
- Added `GET /uploads/{fileName}` media serving endpoint.
- Stored uploaded files outside the repo in a temp-backed storage directory.
- Updated UI post creation to require a selected media file.
- UI now uploads the selected file first, then creates the post using the returned `/uploads/...` URL.
- Browser proof now uploads a real image file during the demo flow.

## Feature IDs covered

- `F-015` Persist real feed media uploads

## Implementation map

### Backend

- [MediaUploadResponse.java](../../src/main/java/com/instagram/backend/dto/MediaUploadResponse.java)
- [MediaStorageService.java](../../src/main/java/com/instagram/backend/service/MediaStorageService.java)
- [MediaController.java](../../src/main/java/com/instagram/backend/controller/MediaController.java)
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
  - `POST /api/media` -> `201`
  - `POST /api/posts` using returned `/uploads/...` URL -> `201`
  - `GET /uploads/{fileName}` -> `200`
- Browser demo proof screenshots:
  - [01-login-page.png](../demo-proofs/2026-03-02/01-login-page.png)
  - [02-home-feed.png](../demo-proofs/2026-03-02/02-home-feed.png)
  - [03-post-created.png](../demo-proofs/2026-03-02/03-post-created.png)
  - [04-post-edited.png](../demo-proofs/2026-03-02/04-post-edited.png)
  - [05-post-liked.png](../demo-proofs/2026-03-02/05-post-liked.png)
  - [06-post-deleted.png](../demo-proofs/2026-03-02/06-post-deleted.png)

## Notes

- Uploaded media is intentionally stored in a temp-backed directory outside the repo so demo runs do not dirty the git worktree.
- This is still a local filesystem storage step, not a production object-storage pipeline. The full production media path remains `F-030`.

## Related system design notes

- [01 - Stateless Auth and Horizontal Scaling](../system-design/01-stateless-auth-and-horizontal-scaling.md)
