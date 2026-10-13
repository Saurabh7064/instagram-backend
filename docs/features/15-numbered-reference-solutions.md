# 15 - Numbered Reference Solutions

## 1) What I built

Every multiline Markdown code block in the existing `/learn` route now displays one-based line numbers. This automatically covers all current reference-solution blocks and all future lessons without manually inserting numbers into Markdown or Java source.

The Maximum Average Subarray lesson also now contains a clearly named `Complete reference solution` section with the entire method and a direct link to its runnable Java file.

## 2) Concepts learned

- Presentation metadata belongs in the renderer, not in copyable source code.
- Stable one-based lines make walkthroughs and interview discussion easier to reference.
- Decorative line numbers should be excluded from selection and screen-reader code content.

## 3) Why this design

Manually prefixing source lines with numbers would make copied Java invalid and require editing every existing lesson. The shared Markdown `pre` renderer splits multiline code into visual rows, adds non-selectable `aria-hidden` numbers, and preserves the original code text. Inline code remains unchanged, and the simulation's existing highlighted line numbers continue to use its specialized renderer.

## 4) Implementation notes

- Each fenced block is rendered as numbered rows inside its original `<pre><code>` structure.
- Line-number cells remain visible while horizontally scrolling wide code.
- Empty lines receive a visual row so later numbers remain aligned with the source.
- A browser test locates the Maximum Average complete solution and verifies lines `1` through `16`.

## 5) Code pointers

- Numbered renderer: [LessonApp.tsx](../../../instagram-ui/src/learning/LessonApp.tsx)
- Styling: [learning.css](../../../instagram-ui/src/learning/learning.css)
- Browser test: [learning-platform.spec.ts](../../../instagram-ui/tests/learning-platform.spec.ts)
- Proof automation: [demo-learning-proof.mjs](../../../instagram-ui/scripts/demo-learning-proof.mjs)
- Updated lesson: [Maximum Average Subarray I](../interview/01-leetcode/lessons/03-sliding-window/01-maximum-average-subarray/README.md)

## 6) Verification evidence

- `npm run test:e2e:learning`: 6 passing Playwright tests, including exact first/last reference line numbers.
- `npm run test:unit`: 11 passing simulation tests.
- `npm run build`: successful; 135 Markdown documents generated.
- `curl --head http://127.0.0.1:5173/learn?...`: `HTTP/1.1 200 OK`.
- Inspected proof: [Numbered reference solution](../demo-proofs/2026-10-12/01-numbered-reference-solution.png).

## 7) Commit pointers

- Backend commit: `edc676f` (`Document numbered reference solutions`)
- UI commit: `e7d8a92` (`Number Markdown reference code lines`)

## 8) Pitfalls and fixes

- Problem: numbers embedded in Java make copied code fail compilation.
- Fix: render numbers as separate, non-selectable spans.
- Problem: numbering only one lesson would be forgotten in future content.
- Fix: enforce numbering in the shared renderer and in `AGENTS.md`.

## 9) Next improvements

- [ ] Add an explicit copy-code button that copies only raw source text.
- [ ] Link walkthrough references directly to numbered lines within long blocks.
