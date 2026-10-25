# 00G — Private execution state and shared objects

## Concept question

If threads have separate call stacks, how can they still affect the same data?

## What you will learn

Distinguish a local reference from the object it points to before reading cross-thread results.

**Time:** 10–15 minutes including the checkpoint. **Prerequisite:** [00F — A task is work; a thread executes it](./00f-task-and-runnable.md).
**Artifact:** READY after recorded verification. **Understanding:** TODO; no learner attempt recorded.

## Understand the concept

Each thread has its own call history and method-local variables. A **reference** is a value identifying a Java object; two different local variables can refer to the same object. Separate variable slots therefore do not guarantee separate underlying data.

The **heap** is the JVM's shared object-storage area in its execution model. Objects and arrays are allocated there. A thread can have a local reference to an object in that shared area. If another thread also has a reference to that object, both can access its fields or contents. This is **shared state** when the same data is accessible through more than one execution path. You need not memorize physical memory addresses or compiler optimizations to understand the ownership relationship.

First observe sharing in ordinary sequential Java: first refers to a new list; second = first copies that reference, not the list. Adding saved through second changes the same list observed through first, so first contains [saved]. The program still has one list object even though two variables name it. The runnable check does not create threads; it isolates the aliasing idea before we combine it with concurrency.

With threads, access to shared mutable state needs rules for when changes may be read and how competing updates are coordinated. Different threads do not automatically receive independent copies of an object. A final reference prevents reassignment of that reference, not all edits to the object's contents.

## Small Java example

Runnable file: [ThreadFoundationsExamples.java](../../../../../../src/test/java/com/instagram/backend/interview/multithreading/foundations/ThreadFoundationsExamples.java). Run `ThreadFoundationsExamples.SharedReferenceExample.main` in IntelliJ using its own main entry point; implementation is optional.

```java
List<String> first = new ArrayList<>();
List<String> second = first;
second.add("saved");
```

Only new ArrayList creates a list. The assignment to second copies a reference to that same list. add changes its contents, which is why inspecting first also reveals saved. This is an ownership example without a debugger or mutation-by-mutation thread trace.

## Predict and observe

**Predict:** Will first stay empty after second adds saved in the example?

**Action:** Optionally run the shared-reference example in ThreadFoundationsExamples.main, then explain what would be different about making an actual new list copy.

**Expected observation:** Both references see: [saved]. There is one shared list object. The example does not demonstrate a concurrent update or claim a timing order between two threads.

**Why:** Assigning a reference does not copy an object. Ownership and coordination must concern the underlying data, not just whether each thread declares its own variable.

## Pitfalls and boundaries

A private call stack is not permission to mutate every object reachable from a local variable without coordination. In the following lesson, one worker writes a result and the caller waits for completion before reading it. That restricted handoff is different from two threads repeatedly updating the same value.

Official reference: [Java runtime or language contract](https://docs.oracle.com/javase/specs/jvms/se24/html/jvms-2.html#jvms-2.5.3).

## Questions and explained answers

<details>
<summary>1. Does second = first create a second list?</summary>

No. It copies the reference, so both variables identify the same list object.

</details>

<details>
<summary>2. Why can private local variables still lead to shared data?</summary>

Their values can be references to the same object. The local slots are separate, while the object contents are shared.

</details>

<details>
<summary>3. Does final make the referenced list immutable?</summary>

No. It prevents reassigning that reference; it does not prevent the list API from changing its contents.

</details>

## Stop/go

Explain the concept question in your own words, make the prediction before reading the observation, and answer the questions before expanding their answers. No coding submission is required. Record the learner's attempt in [learner progress](../../learner-progress.md). If unclear, return to the prerequisite instead of adding APIs.

**Next:** [01 — Starting and joining one worker](../01-start-and-join/README.md).
