# Interactive Simulation Authoring

This guide keeps future Markdown lessons compatible with the existing `/learn` experience. A simulation supplements the concept explanation; it never replaces the worked example, code walkthrough, complexity, mistakes, or checkpoint questions.

## When to add one

Add a simulation when the learner benefits from seeing state change one operation at a time: array visits, HashMap contents, pointer movement, window boundaries, stack/queue operations, linked-list links, tree/graph traversal, binary-search range, dynamic-programming cells, or request ownership across components.

Do not add one merely to make a page animated. Static definitions, short calculations, and ideas already clear in one table should remain Markdown. For Spring Boot, API, JPA, and system-design material, prefer `request-flow` or a trusted declarative trace over an array animation.

## Directive format

Place the block after the learner knows the terms displayed by the simulation:

```markdown
:::simulation
type: two-sum-hashmap
language: java
title: Two Sum · HashMap lookup before insertion
nums: [4, 7, 2, 11, 6, 3, 9, 5]
target: 14
expectedIndices: [3, 5]
:::
```

Architecture example:

```markdown
:::simulation
type: request-flow
title: Protected request
nodes: ["Browser", "Controller", "Service", "Repository", "Response"]
edges: [["Browser", "Controller"], ["Controller", "Service"], ["Service", "Repository"], ["Repository", "Response"]]
:::
```

Supported algorithm adapters are defined in [generate-lessons.mjs](../../../instagram-ui/scripts/generate-lessons.mjs) and implemented in [simulationEngine.ts](../../../instagram-ui/src/learning/simulationEngine.ts). The parser rejects unknown types and keys. Values that begin with `[` or `{` must be valid JSON. Never put JavaScript or secrets in a directive.

## New lesson checklist

1. Teach the concept and define displayed terms first.
2. Use at least seven array elements when that creates a meaningful trace.
3. Configure input, not a list of hardcoded narration steps, when an algorithm adapter exists.
4. Confirm initial state, every condition, update order, final result, and complexity.
5. Include edge cases such as duplicates, empty/not-found behavior, and boundaries when applicable.
6. Run `npm run lessons:generate`, `npm run test:unit`, and `npm run test:e2e:learning` in `instagram-ui`.
7. Keep comprehension separate from artifact readiness; the learner must still explain the invariant and answer the lesson checkpoints.
