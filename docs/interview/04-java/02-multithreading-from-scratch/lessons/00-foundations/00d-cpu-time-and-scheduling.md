# 00D — How a thread gets CPU time

## Concept question

Does a thread execute continuously as soon as it exists?

## What you will learn

Distinguish a thread being ready to do work from actually receiving execution time.

**Time:** 10–15 minutes including the checkpoint. **Prerequisite:** [00C — The thread already running main](./00c-main-and-method-calls.md).
**Artifact:** READY after recorded verification. **Understanding:** TODO; no learner attempt recorded.

## Understand the concept

A **CPU core** is part of the processor that performs computations. A thread describes execution progress, but hardware must perform its instructions. Having more thread objects does not create more processor hardware.

A **scheduler** chooses which eligible execution path receives available CPU time. For the platform threads used in our introductory Java examples, the operating system normally schedules the corresponding operating-system threads. A thread may be ready to run but still wait for its turn. The scheduler can pause one path and later resume it from its saved progress.

For a simple paper model, assume one available execution slot and two ready threads A and B. A can receive a turn, then B, then A again. This **time sharing** lets both make progress without assuming both execute at the same instant. It is an illustration, not a promise of alternating turns or fixed durations. Real processors and schedulers are more complicated; one physical core is not a universal promise of exactly one software thread executing at a time.

A thread waiting for an external result, such as data from a disk or network, is different from a ready thread waiting for CPU time. It has nothing useful to compute until that result is available. The precise Java thread-state names are a later lesson.

## Predict and observe

**Predict:** If A is created before B, is A guaranteed to finish first?

**Action:** In the one-slot paper model, choose a valid order of turns A, B, B, A. Pause A without losing its place, let B finish, then resume A.

**Expected observation:** Both can make progress, and B can finish first. The model supplies no rule forcing creation order to be completion order.

**Why:** A thread's existence and saved progress are separate from scheduling opportunities. No wall-clock delay or real scheduler trace was measured here.

## Pitfalls and boundaries

Do not use a guessed delay to prove another thread completed. Available CPU time changes with machine load and other work. Virtual threads have an additional runtime scheduling layer and are deliberately postponed until the basic model is understood.

Official reference: [Java runtime or language contract](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/lang/Thread.html).

## Questions and explained answers

<details>
<summary>1. What does a scheduler choose?</summary>

Which eligible execution path gets available execution time; it does not turn every thread into its own physical processor.

</details>

<details>
<summary>2. Does creating A before B guarantee A finishes first?</summary>

No. Scheduling, waiting, and the amount of work affect completion. Creation order alone supplies no such guarantee.

</details>

<details>
<summary>3. Is the one-slot paper schedule a measured CPU trace?</summary>

No. It illustrates that turns can be shared. It promises neither round-robin order nor a particular hardware implementation.

</details>

## Stop/go

Explain the concept question in your own words, make the prediction before reading the observation, and answer the questions before expanding their answers. No coding submission is required. Record the learner's attempt in [learner progress](../../learner-progress.md). If unclear, return to the prerequisite instead of adding APIs.

**Next:** [00E — Concurrency is different from parallelism](./00e-concurrency-and-parallelism.md).
