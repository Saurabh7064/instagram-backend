# Multithreading from Scratch Backlog

Artifact readiness and understanding are separate. READY identifies authored and verified material; PLANNED identifies future coverage. TODO is the learner's unassessed state. The course is inactive, and no code submission is required for concept learning.

## Authored starting path

| Part | Concept | Artifact | Understanding |
|---|---|---|---|
| [00A](./lessons/00-foundations/00a-program-and-process.md) | Program versus process | READY | TODO |
| [00B](./lessons/00-foundations/00b-what-is-a-thread.md) | What a thread carries | READY | TODO |
| [00C](./lessons/00-foundations/00c-main-and-method-calls.md) | Main thread and ordinary calls | READY | TODO |
| [00D](./lessons/00-foundations/00d-cpu-time-and-scheduling.md) | CPU time and scheduling | READY | TODO |
| [00E](./lessons/00-foundations/00e-concurrency-and-parallelism.md) | Concurrency versus parallelism | READY | TODO |
| [00F](./lessons/00-foundations/00f-task-and-runnable.md) | Task, Runnable, and executing thread | READY | TODO |
| [00G](./lessons/00-foundations/00g-private-state-and-shared-objects.md) | Private execution state versus shared objects | READY | TODO |
| [01](./lessons/01-start-and-join/README.md) | Start and completion handoff | READY | TODO |

## Future coverage — author before activation

| Order | Primary idea | Artifact | Learning status |
|---:|---|---|---|
| 2 | Lifecycle states: new, runnable, waiting/blocking, and terminated distinctions | PLANNED | TODO |
| 3 | Interruption, cancellation, worker failure reporting, and time limits in separate parts | PLANNED | TODO |
| 4 | Race conditions and lost updates | PLANNED | TODO |
| 5 | Intrinsic locking with synchronized | PLANNED | TODO |
| 6 | Visibility and volatile, separate from compound-operation atomicity | PLANNED | TODO |
| 7 | Atomic variables and supported update operations | PLANNED | TODO |
| 8 | Executors and task/thread ownership | PLANNED | TODO |
| 9 | Futures, result retrieval, and failure propagation | PLANNED | TODO |
| 10 | CompletableFuture and asynchronous composition | PLANNED | TODO |
| 11 | Concurrent collections and atomic collection operations | PLANNED | TODO |
| 12 | Producer-consumer work and bounded queues | PLANNED | TODO |
| 13 | Deadlock diagnosis and prevention | PLANNED | TODO |
| 14 | Virtual threads and blocking-work suitability | PLANNED | TODO |

Each row is coverage to split into focused micro-lessons; multiple named ideas are not a license to teach them all at once. Keep later learner states TODO until prerequisites are demonstrated. Use [learner progress](./learner-progress.md) for the learner's own explanation, prediction, and checkpoint answers. Record code/lab evidence separately in [verification](./verification.md).
