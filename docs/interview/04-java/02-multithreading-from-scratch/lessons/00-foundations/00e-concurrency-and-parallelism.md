# 00E — Concurrency is different from parallelism

## Concept question

Can two threads be concurrent without literally running at the same instant?

## What you will learn

Explain overlapping progress versus simultaneous execution, and why more threads may not help.

**Time:** 10–15 minutes including the checkpoint. **Prerequisite:** [00D — How a thread gets CPU time](./00d-cpu-time-and-scheduling.md).
**Artifact:** READY after recorded verification. **Understanding:** TODO; no learner attempt recorded.

## Understand the concept

**Concurrency** means multiple activities are in progress over an overlapping period. They may take turns using execution resources. **Parallelism** means work actually executes simultaneously using enough execution capacity. A program can be concurrent without demonstrating parallel execution.

Use two jobs: waiting for an image download and preparing a small version of another image. If one execution path is waiting for incoming data, another can make progress on useful work. Both jobs are underway during the same period even if the ready work gets only one execution slot at a time. This explains why multiple execution paths can be useful without treating them as a guarantee of faster arithmetic.

For two independent image calculations to run simultaneously, enough hardware execution capacity must actually be available. Adding threads does not create it. Splitting work also adds coordination and resource costs, so a tiny multiplication is not a realistic performance reason to create a thread. Our later square example exists to learn lifecycle and completion, not to make squaring faster.

An application can stay responsive by separating waiting or long work from work that must respond promptly. That still requires a design for shared state, failures, cancellation, and resource limits. Multithreading is a tool for organizing execution, not a default optimization for every method.

## Predict and observe

**Predict:** Does seeing two Thread objects prove two image calculations executed simultaneously?

**Action:** Explain two possible schedules for the same two jobs: one where ready work takes turns, and one where independent calculations use simultaneous execution capacity.

**Expected observation:** Both jobs can be concurrent in the first explanation, while only the second explanation establishes parallel work. The number of named threads alone distinguishes neither case.

**Why:** Concurrency concerns overlapping activity and coordination; parallelism additionally requires simultaneous execution. No speedup is claimed or benchmarked in this lesson.

## Pitfalls and boundaries

Multiple threads can make a program slower through scheduling and coordination overhead. Waiting-heavy work and calculation-heavy work have different resource needs. Do not promise a worker stays on one core or that the printed order proves the entire execution schedule.

Official reference: [Java runtime or language contract](https://docs.oracle.com/javase/specs/jls/se24/html/jls-17.html).

## Questions and explained answers

<details>
<summary>1. Can concurrency exist with one available execution slot?</summary>

Yes. Activities can take turns and overlap in progress without physically executing at the same instant.

</details>

<details>
<summary>2. What additional fact is needed to claim parallelism?</summary>

That work actually executed simultaneously using enough execution capacity, not merely that two threads existed.

</details>

<details>
<summary>3. Why is a square-on-worker example not evidence of speedup?</summary>

It is a tiny calculation where thread-management overhead is unnecessary for performance. The exercise teaches completion coordination, not faster multiplication.

</details>

## Stop/go

Explain the concept question in your own words, make the prediction before reading the observation, and answer the questions before expanding their answers. No coding submission is required. Record the learner's attempt in [learner progress](../../learner-progress.md). If unclear, return to the prerequisite instead of adding APIs.

**Next:** [00F — A task is work; a thread executes it](./00f-task-and-runnable.md).
