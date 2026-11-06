# Module 05 — Stack and Queue

## What this module teaches

Nested brackets, undoing a minimum, unfinished temperature waits, arithmetic operands, and maxima that expire with a moving window all need saved values to be removed in a particular order. These five micro-lessons teach one use at a time, starting with the newest saved item and ending with a container whose two ends have different jobs.

## Prerequisites and study order

Know Java loops, zero-based array/string positions, and basic comparisons. [Earlier modules](../../lesson-index.md) supply those foundations; no prior stack knowledge is assumed. The first lesson defines a stack (newest item handled first) and its push/pop actions. The final lesson defines a deque (a container with access at both ends); an ordinary queue handles the oldest saved item first. Each lesson is a separate 10–15 minute concept-and-trace session; implementation and retrieval can take longer. Preparing this module does not advance the active daily plan.

| Order | Micro-lesson | Primary idea | Artifact | Learning |
|---:|---|---|---|---|
| C05.01 | [Valid Parentheses](./01-valid-parentheses/README.md) | Close the newest outstanding opening first | READY | TODO |
| C05.02 | [Min Stack](./02-min-stack/README.md) | Save the minimum with each stack entry | READY | TODO |
| C05.03 | [Daily Temperatures](./03-daily-temperatures/README.md) | Resolve older waiting indexes when a warmer day arrives | READY | TODO |
| C05.04 | [Evaluate Reverse Polish Notation](./04-evaluate-reverse-polish-notation/README.md) | Combine the two newest unfinished values in operand order | READY | TODO |
| C05.05 | [Sliding Window Maximum](./05-sliding-window-maximum/README.md) | Remove expired and dominated candidates from opposite ends | READY | TODO |

## How to work through one lesson

Read the question and derive its fixed example before introducing saved state. Follow the small code blocks, narrated execution and adjacent full-state tables; a container's printed orientation is stated explicitly. Try the separate Java practice file in IntelliJ, then consult the full reference only after attempting the problem. Three expandable checkpoint answers and observable stop/go criteria finish each lesson. The UI adds one-based code line numbers without changing the source or copied code.

The existing simulation adapters do not calculate these five algorithms. The worked state tables follow actual reference execution instead of embedding precomputed animation answers. Adding an algorithm adapter is a separate UI feature.

## Artifact readiness and learner evidence

All ten practice/reference Java files compile; the five reference runners and independent model checks pass. Practice methods intentionally remain TODO. No lesson is marked understood or mastered because its artifact works. The single active lesson remains [Two Sum](../01-arrays-hash-maps/03-value-to-index/README.md), with its existing pending checkpoints. All five new lessons remain TODO until explicitly activated in order.

Verification output, catalog-preview limits, reviewed browser proofs and code pointers are recorded in [Feature 27](../../../../features/27-stack-and-queue-curriculum.md). Commit pointers are in the [commit map](../../../../commit-map.md).
