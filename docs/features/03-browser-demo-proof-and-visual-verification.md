# 03 - Browser Demo Proof and Visual Verification

## Goal

Make demo verification evidence mandatory and repeatable for every UI + backend feature by capturing and checking browser screenshots.

## What was built

- Added workflow rules requiring screenshot proof before feature sign-off.
- Added browser automation script to run login demo and capture proof images.
- Saved screenshot artifacts in dated proof folders.
- Added verification step: screenshots must be reviewed after capture.

## Feature IDs covered

- `F-011` Browser demo proof workflow

## Implementation map

### Backend

- [AGENTS.md](../../AGENTS.md)
- [testing-strategy.md](../testing-strategy.md)
- [demo-proofs README](../demo-proofs/README.md)

### UI

- [AGENTS.md](../../../instagram-ui/AGENTS.md)
- [demo-auth-proof.mjs](../../../instagram-ui/scripts/demo-auth-proof.mjs)
- [package.json](../../../instagram-ui/package.json)

## Test evidence

- Browser demo proof screenshots:
  - [01-login-page.png](../demo-proofs/2026-02-23/01-login-page.png)
  - [02-login-success.png](../demo-proofs/2026-02-23/02-login-success.png)
- Visual verification result:
  - Login page loads at `http://localhost:5173`.
  - Success state shows `Logged in as demo.user (Bearer token issued)`.

## Notes

- This feature standardizes evidence collection; it does not add a new product API.
- Future features should add their own dated screenshot folder under `docs/demo-proofs/`.

## Related system design notes

- [01 - Stateless Auth and Horizontal Scaling](../system-design/01-stateless-auth-and-horizontal-scaling.md)
