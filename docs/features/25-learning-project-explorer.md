# Learning Studio project explorer

## 1) What I built
Replaced flat topic groups with an IDE-style nested document explorer: Project toolbar, actual folder/file names, disclosure chevrons, outlined folder icons, blue Markdown icons, and blue selection. Main topics still scope the displayed documents; this is a learning-document explorer, not unrestricted filesystem access.

## 2) Concepts learned
Derive a recursive tree from source paths, sort folders before files, and keep expansion separate from document selection.

## 3) Why this design
Matches the supplied IDE reference while retaining lesson search, topic navigation, history, and the mobile drawer.

## 4) Implementation notes
UI-only change; backend API contract unchanged. Production build passed; all nine Playwright integration tests passed. Explorer test passed again after toolbar alignment. Screenshot visually reviewed. API health curl returned HTTP 000 because port 8080 was unavailable.

## 5) Code pointers
- [Explorer component](../../../instagram-ui/src/learning/LessonApp.tsx)
- [Explorer styles](../../../instagram-ui/src/learning/learning.css)
- [Browser integration tests](../../../instagram-ui/tests/learning-platform.spec.ts)
- [Verified screenshot](../demo-proofs/2026-10-31/learning-project-explorer.png)

## 6) Commit pointers
UI commit: `f9a8eec`. UI push attempted and failed: no origin remote configured.

## 7) Pitfalls and fixes
Existing catalog heading styles stacked toolbar buttons; a scoped flex rule restores horizontal alignment. Filename labels preserve exact source names rather than replacing them with lesson titles.

## 8) Next improvements
Consider remembering expanded folders across reloads.
