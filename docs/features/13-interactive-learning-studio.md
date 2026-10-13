# 13 - Interactive Markdown Learning Studio

## 1) What I built

The existing React UI now exposes `/learn`, where the repository's Markdown lessons render as a searchable learning library. Trusted `:::simulation` directives inside Markdown become inline, state-driven visualizations with deterministic Next, Previous, Restart, Play/Pause, speed, keyboard navigation, Java line highlighting, variables, conditions, intermediate structures, results, and complexity.

The source of truth remains the existing `.md` files. A build-time generator reads them from `instagram-backend`, validates directive types and keys against a whitelist, and writes typed lesson data for the existing `instagram-ui` application. No Markdown-provided JavaScript is evaluated.

## 2) Concepts learned

- **Deterministic simulation:** each screen is an immutable step derived from input, so forward and backward navigation show the same state.
- **Adapter:** one algorithm-specific function translates lesson input into reusable visual state.
- **Build-time content pipeline:** Markdown is validated before the browser receives it, keeping lesson authoring separate from presentation.

## 3) Why this design

Keeping lessons in Markdown preserves the current URLs, Git review workflow, and readable notes. Mounting `/learn` in the existing Vite application avoids a second product. Algorithm-specific adapters calculate real intermediate states, while generic array, map, pointer, stack, queue, linked-list, tree, graph, binary-search, dynamic-programming, and request-flow renderers keep the UI reusable.

The generated catalog initially rendered 129 documents and now renders **134 documents** after the sliding-window expansion and its feature note. Seventeen appropriate lessons contain simulations: fourteen algorithm traces and three architecture/request flows. Other lessons remain normal Markdown because a visualization is only added when it improves the explanation.

## 4) Implementation notes

- Request flow: Markdown → whitelist parser → generated JSON → React Markdown blocks and inline simulation players.
- Validation rules: unknown simulation types, keys, or malformed JSON values fail generation; algorithms validate required input.
- Data model: a simulation definition contains code, complexity, and ordered immutable steps; a step can expose arrays, maps, structures, variables, a condition, and a result.
- Error handling: an invalid adapter input produces a visible simulation error without taking down the surrounding lesson.

## 5) Code pointers

- Content generator: [generate-lessons.mjs](../../../instagram-ui/scripts/generate-lessons.mjs)
- Markdown route: [LessonApp.tsx](../../../instagram-ui/src/learning/LessonApp.tsx)
- Simulation controller: [SimulationPlayer.tsx](../../../instagram-ui/src/learning/SimulationPlayer.tsx)
- Algorithm adapters: [simulationEngine.ts](../../../instagram-ui/src/learning/simulationEngine.ts)
- Reusable renderers: [StructureVisualizers.tsx](../../../instagram-ui/src/learning/StructureVisualizers.tsx)
- Responsive styling: [learning.css](../../../instagram-ui/src/learning/learning.css)
- Unit tests: [simulationEngine.test.ts](../../../instagram-ui/src/learning/simulationEngine.test.ts)
- Browser tests: [learning-platform.spec.ts](../../../instagram-ui/tests/learning-platform.spec.ts)
- Authoring guide: [Interactive Simulation Authoring](../interview/interactive-simulation-authoring.md)

## 6) Verification evidence

- `npm run test:unit`: 11 passing tests, including the exact Two Sum `[3, 5]` result, duplicates, no-solution, sliding-window edges, every configured directive, and every generic structure type.
- `npm run test:e2e:learning`: 5 passing Playwright tests for Markdown/directive integration, deterministic navigation, restart, keyboard navigation, document navigation, narrow-screen behavior, and the expanded sliding-window module.
- `curl --head http://127.0.0.1:5173/learn?...`: `HTTP/1.1 200 OK`.
- Browser proof, inspected after capture: [Two Sum final state](../demo-proofs/2026-10-11/01-interactive-two-sum-final.png).

No backend API contract changed, so no new Spring endpoint or backend integration test was required. Existing Java lesson sources are still compiled and their reference solutions are run separately.

## 7) Commit pointers

- Backend commit: `5e6b00d` (`Document interactive learning simulations`)
- UI commits: `79974c9` (`Add interactive Markdown learning studio`), `e56ff7e` (`Sync generated learning catalog`), `a9560db` (`Generate lesson catalog on demand`)

## 8) Pitfalls and fixes

- Problem: a hand-authored animation can disagree with the algorithm.
- Fix: adapters derive steps from directive inputs and are unit-tested against expected results.
- Problem: a full-page proof made the simulation unreadably small.
- Fix: the proof script scrolls to the final state and captures the browser viewport.

## 9) Next improvements

- [ ] Split generated Markdown content by lesson to reduce the first `/learn` JavaScript chunk.
- [ ] Add algorithm-derived adapters as stack, queue, linked-list, tree, graph, binary-search, and dynamic-programming lessons are authored; their reusable renderers already support declarative traces.
- [ ] Add learner progress persistence and optional reduced-content revision mode.
