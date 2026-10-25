# 00A — A program and a running process

## Concept question

What is running when I press Run in IntelliJ?

## What you will learn

Distinguish saved instructions from one live execution of those instructions.

**Time:** 10–15 minutes including the checkpoint. **Prerequisite:** Basic sequential Java: variables, method calls, and objects. No threading knowledge assumed.
**Artifact:** READY after recorded verification. **Understanding:** TODO; no learner attempt recorded.

## Understand the concept

Before running anything, the computer has instructions stored in files. A **program** is those instructions. In Java, source code describes them, and compiled class files contain instructions the Java runtime can execute. A program file can exist while nothing from it is running.

A **process** is one running instance of a program, with resources such as memory and open files. Starting an ordinary Java application launches a process. Starting the same application again normally launches a second process; it does not turn the existing process into a second copy of itself. Both processes can use the same compiled files while having separate application memory.

The **Java Virtual Machine (JVM)** is the runtime that executes Java instructions. In our ordinary command-line/IntelliJ model, one launched JVM runs inside one operating-system process. Operating system means the computer's software that manages running programs and hardware. You do not need its internal details yet.

Think of a saved recipe and an active cooking session. The recipe is the program; one cooking session is the running instance. This analogy explains stored instructions versus live activity, but it does not explain threads or how the computer schedules work. That comes next.

## Predict and observe

**Predict:** Does opening a .java file mean its program is running?

**Action:** On paper, draw one saved Java program and two separate Run actions. Give each resulting running instance its own memory box.

**Expected observation:** One set of saved instructions can have two running instances. Opening an editor alone creates no running instance of that application.

**Why:** Files describe what could run; a process is the live execution with its own resources. This is a model exercise, not a claim that two application processes were launched during documentation verification.

## Pitfalls and boundaries

Do not equate one source file, one Java object, or one method with one process. A single application process can use many files, objects, and methods. Sharing the same files does not automatically share ordinary in-memory Java objects between separate processes.

Official reference: [Java runtime or language contract](https://docs.oracle.com/javase/specs/jvms/se24/html/jvms-2.html).

## Questions and explained answers

<details>
<summary>1. What exists when the program is not running?</summary>

The stored program instructions can still exist in files; there is no live application process just because those files exist.

</details>

<details>
<summary>2. Why can two runs use the same code but different values?</summary>

They are separate process instances with separate ordinary application memory, even when both execute the same stored program.

</details>

<details>
<summary>3. Is the JVM the same thing as a source file?</summary>

No. The JVM is the running Java execution machinery; the source file is saved text describing code.

</details>

## Stop/go

Explain the concept question in your own words, make the prediction before reading the observation, and answer the questions before expanding their answers. No coding submission is required. Record the learner's attempt in [learner progress](../../learner-progress.md). If unclear, return to the prerequisite instead of adding APIs.

**Next:** [00B — What a thread actually is](./00b-what-is-a-thread.md).
