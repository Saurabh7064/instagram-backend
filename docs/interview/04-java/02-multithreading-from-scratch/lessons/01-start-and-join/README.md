# Micro-Lesson 01 — Starting and Joining One Worker

## Question

> How can a Java method calculate a square on a named worker thread and wait for the worker's result without guessing with `Thread.sleep`?

## What you will learn

- what a thread represents inside one running Java program;
- why constructing a `Thread` does not start its task;
- the difference between calling `start()` and calling `run()` directly;
- how `join()` coordinates completion before the caller reads a result;
- why sleeping is a timing guess rather than a correctness mechanism.

## Lesson status

- Learning status: `TODO`; initialized but not active
- Primary idea: `start` begins work on another thread and `join` waits for that work to finish
- New terms: thread, `start`, `join`

## Inputs, examples, and target

Implement `squareOnWorker(int number)` under these conditions:

Assumptions:

- `number` is between `-46,340` and `46,340`, so the square fits in a Java `int`.
- The method may declare `InterruptedException`.
- Tests must not depend on arbitrary sleeps.

Examples:

- `squareOnWorker(7)` → `49`
- `squareOnWorker(-4)` → `16`
- `squareOnWorker(0)` → `0`

## Reference approach

```java
AtomicInteger result = new AtomicInteger();
Thread worker = new Thread(
        () -> result.set(number * number),
        "square-worker");

worker.start();
worker.join();
return result.get();
```

`new Thread(...)` creates a thread object but does not run the task. `start()` schedules the task to execute on the worker. Calling `run()` directly would execute on the caller and would not create concurrent work.

`join()` blocks the caller until the worker terminates. Without `join`, the caller could read the result before the worker writes it and return the initial `0`.

`AtomicInteger` supplies a small shared result holder. Later lessons will explain atomic operations in depth; here it avoids introducing an unsafe shared primitive while the focus stays on lifecycle and waiting.

## Narrated execution — `number=7`

Expected return: `49`. This is one valid execution: the caller reaches `join()` before the worker writes. The worker can also finish earlier; every valid execution must finish the worker before `join()` returns.

- `AtomicInteger result = new AtomicInteger()` creates the result holder with value `0`.
- `new Thread(...)` stores the task and name `"square-worker"`; constructing it does not run the task.
- `worker.start()` makes the worker eligible to run; the caller continues.
- `worker.join()` makes the caller wait while this worker is unfinished.
- The worker calculates `number * number`: `7 * 7 = 49`; `result.set(49)` changes the holder `0 → 49`.
- The worker finishes; `worker.join()` returns. **State:** `number=7`, `result.get()=49`, worker finished.
- `return result.get()` reads `49` and returns it to the caller.

## Prediction and observation

Prediction: after `join` returns, the worker has finished and the square is available.

Observation: every supplied input produces a deterministic result without `Thread.sleep`.

Explanation: completion is coordinated explicitly by `join`; correctness does not depend on the worker happening to run quickly.

## Useful later: interruption

`join` declares `InterruptedException` because code waiting for another thread may be asked to stop waiting. This first exercise propagates the exception. A later lesson will cover cancellation and restoring interrupt status.

## Complexity

- Work: `O(1)` for one multiplication.
- Additional space: `O(1)`.
- Coordination cost: one thread creation and one wait; real applications normally reuse executor-managed threads for many tasks.


## Practice after learning the concept

- Write here: [StartAndJoinPractice.java](../../../../../../src/test/java/com/instagram/backend/interview/multithreading/startandjoin/StartAndJoinPractice.java)
- Reveal after attempting: [StartAndJoinSolution.java](../../../../../../src/test/java/com/instagram/backend/interview/multithreading/startandjoin/StartAndJoinSolution.java)

## Questions and explained answers

<details>
<summary>1. What is the difference between calling <code>start()</code> and <code>run()</code>?</summary>

`start()` asks the JVM to execute `run` on another thread. Calling `run()` directly is an ordinary method call on the current thread.

</details>

<details>
<summary>2. Why is <code>join()</code> required before reading the result?</summary>

It guarantees that the worker has completed. Without it, the caller may read the initial value before the worker writes the square.

</details>

<details>
<summary>3. Why should a correctness test avoid <code>Thread.sleep</code>?</summary>

Sleep guesses how long work needs. Machine load and scheduling vary, so the guess can make tests slow or flaky. `join` waits for the actual completion event.

</details>

## Stop/go

Proceed only when the practice checks pass and you can predict the result of removing `join`, explain `start` versus `run`, and state why sleeps are not coordination.
