# 00 — Before Creating a Thread

## Why this module exists

Starting a worker is hard to understand when “thread” is only a sentence inside the code explanation. This module first builds the model: a program is saved instructions; a process is a live running instance; a thread carries one path through those instructions. It then connects that path to CPU time, work descriptions, and object ownership.

## Prerequisites and path

Basic variables, methods, and objects are enough. No concurrency APIs or operating-system internals are assumed. Follow one part at a time:

1. [00A — A program and a running process](./00a-program-and-process.md).
2. [00B — What a thread actually is](./00b-what-is-a-thread.md).
3. [00C — The thread already running main](./00c-main-and-method-calls.md).
4. [00D — How a thread gets CPU time](./00d-cpu-time-and-scheduling.md).
5. [00E — Concurrency is different from parallelism](./00e-concurrency-and-parallelism.md).
6. [00F — A task is work; a thread executes it](./00f-task-and-runnable.md).
7. [00G — Private execution state and shared objects](./00g-private-state-and-shared-objects.md).

The seven parts contain 21 explained checkpoint questions. All learner statuses remain TODO; the paper predictions have not been attempted by the learner yet.

## Relationship cheat sheet

| Idea | What it identifies | Example | Boundary |
|---|---|---|---|
| Program | Saved instructions | Compiled Java application | Can exist without running |
| Process | One live running instance and its resources | One ordinary JVM launch | Not one process per method |
| Thread | One path carrying its own execution position/call history | The path currently executing main | Not a physical processor or independent object-memory copy |
| Task | Work to perform | Calculate a square | Can run directly on the caller or be assigned to a thread |
| CPU time | Opportunity for instructions to execute | A ready path receives a turn | Thread existence alone does not promise immediate or simultaneous execution |
| Shared object | Data reachable through multiple references | Two references identify the same list | Separate local variables do not imply separate data |

## Integrated concept exercise

**Predict:** One application has two jobs: waiting for incoming image data and preparing a small version of another image. Does that imply two processes? Does it prove simultaneous CPU execution? If both paths can access the same list, does each get a private copy?

**Action:** Draw one process, two separate execution positions and call histories, a shared object, and available execution capacity outside the process box. Label the two jobs separately from the threads that might carry them out.

**Expected explanation:** The two paths can belong to one process. They may take turns or have one waiting while another runs; simultaneous execution needs actual capacity and scheduling. A shared list remains the same underlying object. Having two local reference variables does not duplicate it.

**Misconception and correction:** If the drawing gives every thread its own copy of all objects, return to 00G. If it draws one physical core inside each Thread object, return to 00D. If every called method becomes a new thread, return to 00C. These are paper-model corrections, not claimed runtime failures.

## Teach it back

Explain what a thread remembers, what it shares, who provides execution time, and how a task differs from its executing thread. Use plain words before API names. Record an actual attempt in [learner progress](../../learner-progress.md).

## Next

[01 — Starting and joining one worker](../01-start-and-join/README.md) uses this foundation to explain a real completion handoff. Runnable examples and observed checks are linked from the [course entry](../../README.md).
