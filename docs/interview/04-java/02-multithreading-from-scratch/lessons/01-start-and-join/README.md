# 01 — Starting and Joining One Worker

## Concept question

How do we ask another execution path to calculate a value, then wait until that calculation has finished?

## What you will learn

Distinguish constructing a thread representation, starting its execution, and waiting for completion before reading its result.

**Prerequisite:** [00A–00G foundations](../00-foundations/README.md), especially task versus thread and private execution state versus shared objects. **Time:** 10–15 minutes. **Understanding:** TODO; not activated.

## Understand the concept

A worker is simply an application thread assigned some work. Here the work is to calculate the square of 7: 7 × 7 = 49. The caller needs the result, so it must know when that work is complete. This tiny calculation is a teaching example; creating a thread is not a performance improvement for one multiplication.

A SquareTask object stores the input number and a result field initially 0. It implements Runnable, so its run method describes the work. The caller and worker can refer to this same task object; they do not get independent copies of its fields.

Constructing a Thread object with the task and a name does not execute the calculation. Calling start asks for a new execution path to run the task. The caller can then continue independently. Calling run directly would instead be an ordinary method call on the current thread, as taught in 00F.

Calling join waits for that worker to finish. If it already finished, join can return without waiting. After join returns normally, the caller can read the result written by the completed worker. This provides both the required completion ordering and visibility of the completed work's writes. It is more than guessing that enough time passed.

## Small Java example

Runnable reference: [StartAndJoinSolution.java](../../../../../../src/test/java/com/instagram/backend/interview/multithreading/startandjoin/StartAndJoinSolution.java). Its named SquareTask class implements Runnable and stores its input and result. The coordination portion is:

```java
SquareTask task = new SquareTask(number);
Thread worker = new Thread(task, "square-worker");
worker.start();
worker.join();
```

For number=7, the task's run method assigns `result = number * number`, calculating 7 × 7 = 49. After the normal join return, `return task.result` returns 49. The reference file also records the actual executing Thread to verify that the calculation used the started worker, not merely a matching thread name.

## Read the syntax and ownership

`new SquareTask(number)` creates the work description and its stored values. `new Thread(task, "square-worker")` associates that work with a new, not-yet-started thread representation. The String is a label, not a scheduling instruction. `worker.start()` requests execution; it is not a promise that the worker runs before the caller's next statement. `worker.join()` waits for this specific worker's termination.

No AtomicInteger or lambda is required for this restricted handoff. The input is established before start; one worker writes the task's result; the caller reads only after successful join. That is different from unsynchronized concurrent reads/writes or repeated shared updates. Ordinary int fields are not generally safe to exchange without such a coordination rule.

The method declares InterruptedException because waiting in join can be interrupted: the waiting caller can be signaled to stop waiting. This method propagates that checked exception and does not proceed to read the result on the exceptional path. It does not claim interruption stops the worker. Cancellation and interruption handling deserve their own later lesson.

## Predict and observe

**Predict:** If the worker has not calculated anything when the caller reads its result, what value is present? Does start alone rule out that observation?

**Action:** Run StartAndJoinSolution.main optionally. Its checks verify 7 → 49, -4 → 16, 0 → 0, and both safe boundary inputs ±46,340 → 2,147,395,600. Input assumptions restrict the magnitude so an int square does not overflow. The running conceptual example remains number=7.

**Expected observation:** The reference checks pass after actual worker completion; they never rely on Thread.sleep. Scheduling can differ while the returned square remains correct.

**Controlled failure and recovery:** The separate [verification helper](../../../../../../src/test/java/com/instagram/backend/interview/multithreading/foundations/ThreadFoundationsVerification.java) deliberately holds a test worker before its write, checks the unfinished value 0, then releases it and joins before verifying 49. It uses a gate so the failure is observable without timing guesses. Gate implementation is test machinery, not prerequisite lesson content.

**Why:** Reading before completion can observe unfinished state. Without join or another suitable completion mechanism, the result has no required handoff guarantee; a broken version is not guaranteed to return 0 on every run. Recover by establishing completion before the read, not by sleeping longer.

## Pitfalls and boundaries

A Thread can be started only once. join waits for termination; it does not mean a task succeeded, automatically propagate a worker exception, or impose an overall application timeout. The reference checks use a non-throwing multiplication within a documented range. Later lessons must address failure reporting, time limits, and cancellation explicitly.

Formal reference, useful later: [Java's join ordering rule](https://docs.oracle.com/javase/specs/jls/se24/html/jls-17.html#jls-17.4.5). The core idea is already explained above; no memory-model terminology is needed to attempt this checkpoint.

## Questions and explained answers

<details>
<summary>1. What is the difference between creating a Thread, calling start, and calling run directly?</summary>

Construction creates its representation and associates work; it does not run the task. start requests a separate execution path. Direct run is an ordinary call executed by the caller.

</details>

<details>
<summary>2. Why can the caller read an ordinary result field after successful join here?</summary>

One worker wrote the result, and join establishes its completion and visibility of those writes before the caller reads. This specific handoff does not make arbitrary unsynchronized shared fields safe.

</details>

<details>
<summary>3. Why not replace join with a long sleep?</summary>

A delay guesses when another thread might finish. Scheduling and work duration vary, and sleep does not establish the same completion/visibility guarantee. Wait for the actual completion mechanism.

</details>

## Stop/go

Explain task versus executing thread, predict the unfinished value without claiming a fixed broken outcome, and explain the successful completion handoff. A [separate practice file](../../../../../../src/test/java/com/instagram/backend/interview/multithreading/startandjoin/StartAndJoinPractice.java) is optional. Passing its code checks alone does not demonstrate understanding. Record checkpoint evidence in [learner progress](../../learner-progress.md).

**Next:** lifecycle states and interruption are [planned coverage](../../learning-backlog.md); they have not been authored or taught here.
