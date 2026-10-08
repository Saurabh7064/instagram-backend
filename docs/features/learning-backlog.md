# Feature Learning Backlog (Build Track)

Use this file to track product/engineering features for the app.

## Status legend

- `TODO` not started
- `IN_PROGRESS` currently building
- `DONE` completed and documented

## Current scope

| ID | Feature | Status | Why it matters | Code/Doc link |
|---|---|---|---|---|
| F-001 | Register API | DONE | Basic account creation | [Feature 01](./01-authentication-and-password-security.md) |
| F-002 | Login API | DONE | Basic authentication flow | [Feature 01](./01-authentication-and-password-security.md) |
| F-003 | Signup UI | DONE | Browser registration flow | [Feature 01](./01-authentication-and-password-security.md) |
| F-004 | Login UI | DONE | Browser login flow | [Feature 01](./01-authentication-and-password-security.md) |
| F-005 | Password hashing (BCrypt) | DONE | Security baseline | [Feature 01](./01-authentication-and-password-security.md) |
| F-006 | JWT access token issuance | DONE | Stateless auth for scale | [Feature 02](./02-jwt-access-token-issuance.md) |
| F-007 | Protected endpoint (`/api/me`) | DONE | Validate auth end-to-end | [Feature 04](./04-protected-me-endpoint.md) |
| F-008 | Refresh token flow | DONE | Better session UX/security | [Feature 05](./05-refresh-token-flow.md) |
| F-009 | Basic profile page API/UI | DONE | First authenticated product feature | [Feature 06](./06-basic-profile-page-api-ui.md) |
| F-010 | Posts create/read API/UI | DONE | Core social functionality | [Feature 07](./07-posts-create-read-api-ui.md) |
| F-011 | Browser demo proof workflow | DONE | Reliable visual evidence for UI+backend integration | [Feature 03](./03-browser-demo-proof-and-visual-verification.md) |
| F-012 | Post like/unlike API/UI | DONE | First engagement action on content | [Feature 08](./08-post-like-unlike-api-ui.md) |
| F-013 | Delete own post API/UI | DONE | First ownership-based content mutation | [Feature 09](./09-delete-own-post-api-ui.md) |
| F-014 | Edit own post API/UI | DONE | First ownership-based update flow | [Feature 10](./10-edit-own-post-api-ui.md) |
| F-015 | Persist real feed media uploads | DONE | Replace SVG placeholder media with real uploaded content | [Feature 11](./11-persist-real-feed-media-uploads.md) |
| F-016 | Multi-post feed rendering | TODO | Show more than the hero card and support scrolling feed state |  |
| F-017 | Comments create/read API/UI | TODO | Core conversation layer on posts |  |
| F-018 | Save/unsave posts API/UI | TODO | Personal curation and later-viewing flow |  |
| F-019 | Follow/unfollow users API/UI | TODO | Core social graph behavior |  |
| F-020 | Followers/following lists API/UI | TODO | Inspect and navigate the social graph |  |
| F-021 | Personalized feed ranking | TODO | Move from chronological feed to relevance-based ordering |  |
| F-022 | Search users and posts API/UI | TODO | Discovery beyond the home feed |  |
| F-023 | Explore page API/UI | TODO | Dedicated discovery surface with ranked content |  |
| F-024 | Real stories API/UI | TODO | Replace mocked story circles with actual ephemeral content |  |
| F-025 | Story viewer and expiry rules | TODO | Complete the stories product loop |  |
| F-026 | Direct messages thread list API/UI | TODO | Private communication basics |  |
| F-027 | Direct message send/read flow | TODO | Actual messaging experience |  |
| F-028 | Notifications API/UI | TODO | User awareness for likes, follows, and comments |  |
| F-029 | Profile edit API/UI | TODO | Let users change name, bio, avatar, and profile metadata |  |
| F-030 | Image/video processing pipeline | TODO | Production path for media storage, resizing, and delivery |  |
| F-031 | Reels-style short video feed | TODO | Core modern Instagram engagement surface |  |
| F-032 | Hashtags and mentions parsing | TODO | Link content, search, and discovery graph |  |
| F-033 | Post reporting and moderation queue | TODO | Safety and abuse-handling baseline |  |
| F-034 | Rate limiting and abuse protection | TODO | Protect auth and content APIs under load or attack |  |
| F-035 | Production auth hardening | TODO | Session revocation, device sessions, and stronger security controls |  |
| F-036 | Production observability baseline | DONE | Correlate safe logs, bounded metrics, traces, and health signals during production debugging | [Feature 12](./12-production-observability-baseline.md) |

## Next 3 recommended

1. `F-016` Multi-post feed rendering
2. `F-017` Comments create/read API/UI
3. `F-018` Save/unsave posts API/UI
