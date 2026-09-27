# Kubernetes Learning Path

This track teaches Kubernetes by progressively deploying and operating this Instagram project. Each concept must produce a working project artifact, verification evidence, and a short learning note.

## How to use this track

1. Pick the first `TODO` item from the [Kubernetes Learning Backlog](./learning-backlog.md).
2. Mark it `IN_PROGRESS` before making changes.
3. Read the relevant Kubernetes concepts from the [Mastery Roadmap](./mastery-roadmap.md).
4. Implement the exercise against this backend, PostgreSQL, and, where relevant, the sibling UI.
5. Verify the result using the evidence required by the backlog item.
6. Write or update a numbered note under this directory.
7. Mark the item `DONE`, update [Commit Map](../commit-map.md), and commit the learning change.

Use the [12-Week Execution Plan](./12-week-execution-plan.md) for the recommended sequence and pace.

## Definition of done for a Kubernetes item

- The manifest, configuration, script, or runbook is committed.
- The deployed resource reaches its expected state on a local cluster.
- Relevant backend API calls succeed from outside the cluster.
- Automated application tests still pass.
- Evidence commands and their observed results are recorded in the item note.
- UI-impacting work is verified in the browser with screenshot proof.
- Failure and recovery behavior is exercised where the item calls for it.
- The backlog, note, and commit map are updated.

## Teaching standard for every lesson

Every numbered Kubernetes lesson must be understandable without assuming prior Kubernetes knowledge. Use the [Lesson Template](./_lesson-template.md) and include:

1. A simple explanation using ordinary language.
2. The concrete problem the concept solves.
3. A small mental model or analogy, plus where that analogy stops being accurate.
4. How the concept maps to this Instagram project.
5. A guided hands-on exercise with expected observations.
6. A substantial comprehension set—normally at least 15 questions—covering foundations, application, prediction, and troubleshooting. Foundational lessons should contain more when several new terms are introduced.
7. A clearly separated, always-visible answer key with explanations for every question, not only final answers. Do not hide answers in collapsible sections.
8. A short "teach it back" prompt so the learner can explain the idea in their own words.

Questions should be attempted before reading the answer key, but the answers must remain present and directly visible in the document. A lesson is understood when the learner can explain why the resource exists, predict its behavior, and diagnose one simple failure—not merely reproduce a command.

## Evidence conventions

Store compact text evidence in the numbered learning note. Store UI screenshots under `docs/demo-proofs/<YYYY-MM-DD>/`. Useful evidence includes:

- `kubectl get` output showing readiness and rollout state
- `kubectl describe` or events for a diagnosed failure
- `kubectl logs` output confirming application startup
- `curl` results for health and product APIs
- rollout history and rollback results
- persistence checks before and after Pod replacement

Do not commit credentials, rendered Secret values, kubeconfig files, or cluster-specific private data.
