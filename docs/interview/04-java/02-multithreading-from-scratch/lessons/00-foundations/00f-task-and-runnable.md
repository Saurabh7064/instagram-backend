# 00F — A task is work; a thread executes it

## Concept question

What is the difference between a task, Runnable, and Thread?

## What you will learn

Separate the description of a job from the execution path carrying it out.

**Time:** 10–15 minutes including the checkpoint. **Prerequisite:** [00E — Concurrency is different from parallelism](./00e-concurrency-and-parallelism.md).
**Artifact:** READY after recorded verification. **Understanding:** TODO; no learner attempt recorded.

## Understand the concept

A **task** is a piece of work to perform, such as calculating a square or recording which thread performed an action. A thread is the execution path that carries out instructions. The work and the execution path are different things: the same task can be called directly by the current thread or supplied to another thread.

Java's **Runnable** is an interface describing a no-argument operation named run that returns no value. An interface is a type-level contract; a class implements it by providing the required method. `implements Runnable` therefore says the class supplies `public void run()`. It does not say that constructing its object starts concurrent work.

Calling task.run() directly is an ordinary method call, so the caller executes it. In the later start/join lesson, a Thread object receives a Runnable and start requests a separate execution path for it. Keeping these roles separate avoids the common mistake of treating a method named run as a magic thread-creation operation.

The example uses a named Java class rather than assuming lambda syntax. A lambda is merely another way to supply compatible behavior once that syntax has been learned; no lambda is required to understand a task.

## Small Java example

Runnable file: [ThreadFoundationsExamples.java](../../../../../../src/test/java/com/instagram/backend/interview/multithreading/foundations/ThreadFoundationsExamples.java). Run `ThreadFoundationsExamples.TaskOwnershipExample.main` in IntelliJ using its own main entry point; implementation is optional.

```java
RecordingTask task = new RecordingTask();
task.run();
```

The constructor creates a task object. Calling run executes that task on the current caller. The linked class defines RecordingTask with a public run method that records the actual executing Thread; this field is checked immediately on the same thread.

## Predict and observe

**Predict:** If we construct RecordingTask and call run directly, which thread records itself?

**Action:** Optionally run ThreadFoundationsExamples.TaskOwnershipExample.main and read the direct-run check. Its RecordingTask implements Runnable and stores Thread.currentThread() when run is called.

**Expected observation:** Direct run() used the caller thread. The example compares actual Thread objects, so this observation is not a guess based on matching names.

**Why:** The Runnable describes the work. The ordinary call executes it on the caller. No worker has been started in this example.

## Pitfalls and boundaries

Runnable.run returns void, so it does not directly return a calculated result to its caller through the Runnable contract. A shared result field requires completion and visibility reasoning before another thread reads it; that is why the later join lesson follows the memory foundation.

Official reference: [Java runtime or language contract](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/lang/Runnable.html).

## Questions and explained answers

<details>
<summary>1. Does implements Runnable create another execution path?</summary>

No. It provides a run method fulfilling a work-description contract. An object can exist without its task being executed.

</details>

<details>
<summary>2. What executes task.run when called directly?</summary>

The current calling thread, just as for any ordinary method call.

</details>

<details>
<summary>3. Why use a named class in this introduction?</summary>

It exposes the task contract without requiring unexplained lambda syntax or implying that an arrow creates a thread.

</details>

## Stop/go

Explain the concept question in your own words, make the prediction before reading the observation, and answer the questions before expanding their answers. No coding submission is required. Record the learner's attempt in [learner progress](../../learner-progress.md). If unclear, return to the prerequisite instead of adding APIs.

**Next:** [00G — Private execution state and shared objects](./00g-private-state-and-shared-objects.md).
