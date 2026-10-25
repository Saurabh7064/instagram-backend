# 00B — What a thread actually is

## Concept question

What is a thread, and why is it different from a whole running program?

## What you will learn

Explain a thread as a path through instructions with its own current position and method-call history.

**Time:** 10–15 minutes including the checkpoint. **Prerequisite:** [00A — A program and a running process](./00a-program-and-process.md).
**Artifact:** READY after recorded verification. **Understanding:** TODO; no learner attempt recorded.

## Understand the concept

A **thread** is one execution path inside a running process. It follows instructions, calls methods, and returns from them. To continue correctly after it pauses, it needs to remember where it was and which method calls have not finished. A thread is not a source file, a method, a separate application process, or a physical CPU core.

Its **execution position** means the instruction it is currently working on or will continue from. Its **call stack** is the record of active method calls: which method is currently executing and where it should return. If main calls loadFeed, which calls readPosts, those calls belong to that thread's history. Returning from readPosts continues loadFeed on the same thread.

One process can have more than one thread. Each thread has its own execution position and call stack, so two threads can be at different places in the same program. They can refer to the same application objects, so they are not isolated copies of the entire program. We will teach the sharing rules in 00G before any shared-result exercise.

Imagine two bookmarks in the same instruction book. Each bookmark records a different place to continue. The analogy helps explain separate progress through shared code; it stops at execution: bookmarks do not perform work, and two bookmarks do not prove two CPU resources are available. The scheduling lessons explain that distinction.

## Predict and observe

**Predict:** If two execution paths are currently in different methods of the same application, must there be two processes?

**Action:** Draw one process box containing shared program instructions. Inside it, draw thread A with current method loadFeed and thread B with current method prepareThumbnail. Give each its own small method-call history.

**Expected observation:** Two places in the code and two call histories fit inside one running application. Do not draw two independent copies of every object.

**Why:** A thread carries execution progress; the process is the broader running instance that provides shared resources. This diagram does not claim the two threads are physically executing simultaneously.

## Pitfalls and boundaries

A method call alone does not create a thread. A thread can execute many different methods over its lifetime, and the same method can be executed by different threads. A Java Thread object represents a thread; merely constructing that object is different from starting its execution.

Official reference: [Java runtime or language contract](https://docs.oracle.com/javase/specs/jvms/se24/html/jvms-2.html#jvms-2.5).

## Questions and explained answers

<details>
<summary>1. What must a paused thread remember to continue?</summary>

Its position in the instructions and the active method-call history, including where calls should return. These are separate for each thread.

</details>

<details>
<summary>2. Is one method the same thing as one thread?</summary>

No. A thread can call many methods, and an ordinary method call continues on the caller thread.

</details>

<details>
<summary>3. Do two threads require two processes or prove simultaneous execution?</summary>

Neither. Multiple threads can belong to one process. Whether they physically execute at the same time depends on available execution resources and scheduling.

</details>

## Stop/go

Explain the concept question in your own words, make the prediction before reading the observation, and answer the questions before expanding their answers. No coding submission is required. Record the learner's attempt in [learner progress](../../learner-progress.md). If unclear, return to the prerequisite instead of adding APIs.

**Next:** [00C — The thread already running main](./00c-main-and-method-calls.md).
