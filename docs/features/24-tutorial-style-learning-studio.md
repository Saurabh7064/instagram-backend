# Tutorial-style Learning Studio

## 1) What I built

Main topics in a green horizontal navigation bar, a pale course sidebar with collapsible subtopic headings, and a spacious white lesson surface matching the supplied reference. Search filters the selected topic; selecting a lesson and browser history keep topic selection synchronized. Mobile retains its slide-out course outline.

## 2) Concepts learned

Topic filtering and lesson selection are separate state: browsing another course does not discard the currently open lesson. Native buttons expose expanded/current state for keyboard and assistive technology users.

## 3) Why this design

Directory-based groups expose the existing curriculum hierarchy without maintaining a second lesson catalog. Existing simulations, Markdown, source links, and numbered reference listings remain available.

## 4) Implementation notes

The catalog generator now reads `docs/code-base-system-design`, following the existing local folder rename. No backend API contract changed. Content remains generated locally; the backend does not need to run for `/learn`.

## 5) Code pointers

- [LessonApp.tsx](../../../instagram-ui/src/learning/LessonApp.tsx)
- [learning.css](../../../instagram-ui/src/learning/learning.css)
- [generate-lessons.mjs](../../../instagram-ui/scripts/generate-lessons.mjs)
- [Browser integration tests](../../../instagram-ui/tests/learning-platform.spec.ts)
- [Verification evidence](../demo-proofs/2026-10-25/learning-studio-verification.txt)
- [Verified desktop screenshot](../demo-proofs/2026-10-25/learning-studio-tutorial.png)

## 6) Commit pointers

Recorded in [commit map](../commit-map.md). UI repository has no configured origin; its commit remains local.

## 7) Pitfalls and fixes

Normal startup failed because the generator referenced the old folder. Regenerating also exposed old browser assertions for replaced lesson inputs; those assertions now check current fixed examples and sequential code-line numbers.

## 8) Next improvements

- Remember expanded groups across page reloads if useful.
- Split generated content by topic to reduce the existing large lesson bundle warning.
