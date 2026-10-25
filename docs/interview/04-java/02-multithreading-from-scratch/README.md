# Java Multithreading from Scratch

A **thread is one execution path inside a running program**. It has its own current place in the instructions and its own record of unfinished method calls. A running program can contain several such paths, which can use the same objects. More threads do not automatically mean more simultaneous CPU execution.

That definition needs a foundation, not just an API exercise. Start with [00A — A program and a running process](./lessons/00-foundations/00a-program-and-process.md), then [00B — What a thread actually is](./lessons/00-foundations/00b-what-is-a-thread.md). The [foundation module](./lessons/00-foundations/README.md) explains the relationships before introducing worker creation.

## Starting assumptions

Only ordinary sequential Java is assumed: variables, simple methods, and objects. Runnable's interface contract is refreshed before it is used. No prior threading, lambda, atomic-variable, operating-system, or memory-model knowledge is required. CPU and scheduling terms are defined before use.

**Active lesson:** none. Lesson artifacts are prepared; learner understanding remains TODO in [learner progress](./learner-progress.md). Documentation completion does not mean these concepts have been taught or understood.

## Prepared path

| Part | One primary idea | Observable understanding |
|---|---|---|
| [00A](./lessons/00-foundations/00a-program-and-process.md) | Saved instructions versus a running instance | Explain program, process, and JVM roles |
| [00B](./lessons/00-foundations/00b-what-is-a-thread.md) | A thread carries execution progress | Explain current position and method-call history |
| [00C](./lessons/00-foundations/00c-main-and-method-calls.md) | main already has an executing thread | Explain why a direct method call stays on that thread |
| [00D](./lessons/00-foundations/00d-cpu-time-and-scheduling.md) | Ready work still needs CPU time | Explain why creation order is not completion order |
| [00E](./lessons/00-foundations/00e-concurrency-and-parallelism.md) | Overlap differs from simultaneous execution | Distinguish concurrency from parallelism without a speedup claim |
| [00F](./lessons/00-foundations/00f-task-and-runnable.md) | A task is work; a thread executes it | Explain Runnable and why run() is an ordinary call |
| [00G](./lessons/00-foundations/00g-private-state-and-shared-objects.md) | Separate call stacks can reach the same object | Distinguish local reference slots from shared data |
| [01](./lessons/01-start-and-join/README.md) | Wait for actual worker completion | Explain construction, start, direct run, and join |

Take one 10–15 minute part at a time. In chat, ask to teach a part; we explain only that idea, ask 2–3 questions, and wait for your attempt. Implementation is optional. Lessons use plain-language concepts, short examples, expected observations, pitfalls, and explained checkpoints. They omit iteration/debugger narration unless explicitly requested, following the saved [Java-concept preference](../../../../AGENTS.md#functional-programming-lesson-standard).

## After the foundation

The [backlog](./learning-backlog.md) separates the eight authored parts from future coverage. Lifecycle states, interruption, races, locks, visibility, atomics, executors, futures, asynchronous composition, collections, queues, deadlocks, and virtual threads are planned; their names are coverage targets rather than prerequisites assumed by the foundation.

No unexplained concurrency helper should be inserted merely to make the first example shorter. Teach the missing concept one level earlier instead. A square-on-worker example teaches execution ownership and completion, not faster arithmetic.

## Runnable examples and evidence

- [ThreadFoundationsExamples.java](../../../../src/test/java/com/instagram/backend/interview/multithreading/foundations/ThreadFoundationsExamples.java): separate runnable main entry points for the current thread/method calls, task ownership, and shared references. Run only the entry point for your current part.
- [StartAndJoinSolution.java](../../../../src/test/java/com/instagram/backend/interview/multithreading/startandjoin/StartAndJoinSolution.java): named task and actual worker completion without lambdas or atomic variables.
- [StartAndJoinPractice.java](../../../../src/test/java/com/instagram/backend/interview/multithreading/startandjoin/StartAndJoinPractice.java): optional separate implementation exercise.
- [ThreadFoundationsVerification.java](../../../../src/test/java/com/instagram/backend/interview/multithreading/foundations/ThreadFoundationsVerification.java): deterministic checks, including controlled unfinished work and recovery. Its latch is verification machinery, not a beginner prerequisite.
- [Verification note](./verification.md): Java output, rendered lessons, HTTP serving, and reviewed screenshots.
