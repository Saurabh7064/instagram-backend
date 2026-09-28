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

## Lessons

- [01 - Containerize the Spring Boot Backend](./01-containerize-spring-boot-backend.md)
- [02 - Local Cluster, kubectl, Pods, Namespaces, Labels, and Selectors](./02-local-cluster-kubectl-pods-labels.md)

## Depth without density

A backlog item is a **module**, not one large chapter to consume in a sitting. Each module is divided into 10–15 minute micro-lessons. Read and practice one micro-lesson at a time.

The course assumes no Kubernetes, Docker, networking, YAML, or Linux knowledge unless a prerequisite is explicitly stated and refreshed. Calendar weeks and backlog status track project progress; they do not prove understanding.

For each micro-lesson:

1. Start with the one outcome and the concrete problem being solved.
2. State what is assumed and what will be taught from scratch.
3. Introduce normally no more than 3–5 new terms.
4. Separate `Must understand`, `Useful later`, and `Optional deep dive` material.
5. Use one project example and one small exercise.
6. Predict first, then act, observe, and explain why the result occurred.
7. Place 3–6 expandable question-and-answer checkpoints directly after the concept.
8. Stop until the learner can explain the problem, predict one behavior, and diagnose one simple failure.

## Interactive learning in chat

Say `teach 01A` (or another part ID) to work through exactly one micro-lesson interactively. The teaching loop is:

1. I explain one idea with one project example.
2. I ask 2–3 short checkpoint questions and stop.
3. You answer in your own words.
4. I identify what is correct, fix one misunderstanding at a time, and use a fresh example.
5. We continue only when you ask or the checkpoint is clear.

If a concept is confusing, we move one prerequisite level earlier instead of introducing more terminology. Project completion and personal understanding are tracked separately in [Learner Progress](./learner-progress.md).

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

Every numbered Kubernetes module must be understandable without assuming prior Kubernetes knowledge. Use the [Module Template](./_lesson-template.md) and [Micro-Lesson Template](./_micro-lesson-template.md).

1. A short landing page with starting assumptions and a prerequisite-ordered micro-lesson map.
2. One main idea per micro-lesson, introduced through the problem it solves.
3. A vocabulary budget of normally 3–5 new terms per micro-lesson.
4. A small mental model or diagram, including where the analogy stops being accurate.
5. A project example and a tiny exercise with expected observations.
6. Distributed checkpoints rather than a large question wall at the end.
7. An expandable, explained answer inside every question.
8. A clear stop/go mastery check before the next micro-lesson.
9. A module recap, vocabulary cheat sheet, integrated practice, and teach-it-back prompt.

Questions should be attempted before expanding their answers. A micro-lesson is understood when the learner can explain its problem in plain language, predict one behavior, and diagnose one simple failure—not merely reproduce a command.

## Evidence conventions

Store compact text evidence in the numbered learning note. Store UI screenshots under `docs/demo-proofs/<YYYY-MM-DD>/`. Useful evidence includes:

- `kubectl get` output showing readiness and rollout state
- `kubectl describe` or events for a diagnosed failure
- `kubectl logs` output confirming application startup
- `curl` results for health and product APIs
- rollout history and rollback results
- persistence checks before and after Pod replacement

Do not commit credentials, rendered Secret values, kubeconfig files, or cluster-specific private data.
