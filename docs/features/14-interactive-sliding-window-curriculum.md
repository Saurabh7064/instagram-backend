# 14 - Interactive Sliding Window Curriculum

## 1) What I built

Initialized the remaining four lessons in the existing Sliding Window module:

1. [Longest Substring Without Repeating Characters](../interview/01-leetcode/lessons/03-sliding-window/02-longest-substring-without-repeating/README.md)
2. [Longest Repeating Character Replacement](../interview/01-leetcode/lessons/03-sliding-window/03-longest-repeating-character-replacement/README.md)
3. [Permutation in String](../interview/01-leetcode/lessons/03-sliding-window/04-permutation-in-string/README.md)
4. [Minimum Window Substring](../interview/01-leetcode/lessons/03-sliding-window/05-minimum-window-substring/README.md)

Each lesson starts with the exact question, teaches one primary concept before code, derives example results, provides an inline deterministic simulation, explains every reference block, links separate editable/reference Java runners, and ends with explained checkpoints plus observable stop/go criteria. All remain `TODO`; initialized artifacts do not imply learner comprehension.

## 2) Concepts learned

- Variable window: expand to gain information and shrink only when a constraint requires it.
- Fixed count window: add one incoming count and remove the value one window-size behind it.
- Coverage window: expand until valid, then shrink repeatedly to find the minimum.

## 3) Why this design

The sequence grows one idea at a time: uniqueness after fixed-window mechanics, then a replacement budget, then exact fixed-window counts, and finally minimum coverage. Algorithm adapters derive every visual state from lesson inputs instead of storing a narrated answer. Arrays and count maps use the existing visual components, so no separate lesson application or arbitrary Markdown JavaScript was introduced.

## 4) Implementation notes

- Longest unique substring uses a `HashSet` so the learner sees repeated shrinking before learning index-jump optimization.
- Character replacement uses a 26-cell frequency array and explains why a historical `maxFrequency` is safe for maximum length.
- Permutation matching compares two 26-cell signatures for a conceptually direct `O(n)` solution.
- Minimum window tracks required versus formed character groups so validity checks remain constant time.
- Each practice runner catches the intentional `UnsupportedOperationException`; each reference runner asserts standard and edge cases.

## 5) Code pointers

- Simulation adapters: [simulationEngine.ts](../../../instagram-ui/src/learning/simulationEngine.ts)
- Directive whitelist: [generate-lessons.mjs](../../../instagram-ui/scripts/generate-lessons.mjs)
- Unit tests: [simulationEngine.test.ts](../../../instagram-ui/src/learning/simulationEngine.test.ts)
- Browser integration test: [learning-platform.spec.ts](../../../instagram-ui/tests/learning-platform.spec.ts)
- Java sources: [slidingwindow package](../../src/test/java/com/instagram/backend/interview/leetcode/slidingwindow/)

## 6) Verification evidence

- `./gradlew testClasses`: successful.
- Four new Java reference `main` programs: all checks passed.
- `npm run test:unit`: 11 passing tests, including match and no-match edge cases.
- `npm run test:e2e:learning`: 5 passing Playwright tests, including completion of the new longest-unique simulation.
- `npm run build`: successful; the learning route now generates 134 Markdown documents.
- `curl --head http://127.0.0.1:5173/learn`: `HTTP/1.1 200 OK`.
- Inspected proof: [Longest unique substring final state](../demo-proofs/2026-10-12/01-longest-unique-substring-final.png).

## 7) Commit pointers

- Backend commit: `548f0a8` (`Initialize sliding window micro-lessons`)
- UI commit: `73496dd` (`Add interactive sliding window lessons`)

## 8) Pitfalls and fixes

- Problem: the first minimum-window edge test expected `"ab"` from source `"bba"`.
- Fix: corrected the expected contiguous suffix to `"ba"`; the failing assertion demonstrated that the test was checking actual substring order.
- Problem: a lesson artifact can be mistaken for mastered material.
- Fix: all four statuses remain `TODO`, with explicit prerequisite and stop/go gates.

## 9) Next improvements

- [ ] Activate only Lesson 01 when the learner chooses to begin this module.
- [ ] Persist simulation position and learner checkpoint attempts.
- [ ] Split the lazy learning catalog by document to reduce the route chunk.
