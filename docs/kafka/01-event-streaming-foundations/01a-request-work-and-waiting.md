# 01A — What should a post request wait for?

**Time:** 10–15 minutes. **Prerequisite:** none. No installation is needed.

## One thing you will learn

Decide which work must finish before telling a user their post was created.

## The problem first

A user creates post 42. The application must save it. Imagine we later add a notification telling someone about it. If that notification is slow, should the user wait before seeing that the post exists?

A request is a message asking a program to do something. A response tells the caller the result. Here, the browser asks the backend to create a post, and the backend replies. The database is the program that stores the post so it can be read later. These are starting definitions; no networking knowledge is assumed.

## New words

| Term | Plain meaning |
|---|---|
| HTTP | The request/response rules used by the browser and backend here |
| Synchronous work | Work the caller waits to finish before proceeding |
| Asynchronous work | Work allowed to finish separately, after the caller proceeds |

## Must understand

Start with the promise: a successful create response means the post was saved. It need not mean every later reaction to that post has finished. The promised result determines what must happen before the response.

For this fixed teaching example, assume saving post 42 takes 40 milliseconds and delivering its notification takes 500 milliseconds. A millisecond is one thousandth of a second. Ignore network and other overhead so the arithmetic shows just the waiting decision.

If the backend saves the post and then waits for notification delivery, the work takes `40 + 500 = 540 ms` before responding. If it saves the post and durably records the need for later notification as one successful database change, the response can follow that smaller combined save. Call its total time `S`; the response waits for `S`, not for the additional 500 ms of delivery. `S` includes the extra recording work, so it is not automatically the original 40 ms.

This buys a shorter wait but introduces a second state: the post exists while the notification is still pending. Someone must process that pending work, and the application must handle failure. Asynchronous does not mean instant, free, guaranteed, or necessarily a new thread inside the same program.

Think of a receipt saying a request was accepted for later work. The analogy stops at reliability: a receipt alone cannot ensure that software saved the pending work or that another program will complete it.

## Project connection

In the current [PostService.create method](../../../src/main/java/com/instagram/backend/service/PostService.java), the application builds and saves a post. [PostController](../../../src/main/java/com/instagram/backend/controller/PostController.java) exposes the create request. There is no notification delivery step today. The 500 ms notification and combined-save design above are proposed teaching examples for [F-028](../../features/learning-backlog.md), not timings measured from this backend.

Use synchronous work when the caller needs its outcome to know whether the requested operation succeeded. Consider asynchronous work when a later reaction may be delayed and its pending state can be stored reliably. Adding a separate processing path also adds monitoring, recovery, and a more complicated user-visible state.

## Predict → act → observe → explain

**Predict:** With notification delivery unavailable, which design can still confirm a saved post?

**Act:** On paper, write two sequences for the same post 42: `save → deliver → respond` and `save post plus pending work → respond → deliver later`. Cross out delivery because it is unavailable.

**Expected observation:** The first sequence cannot reach a normal success response if it insists on successful delivery. The second can respond once its combined database save succeeds, while the pending notification remains unfinished.

**Why:** The second response promises a saved post and recorded pending work. It does not promise completed notification delivery. Recovery means restoring the later processor and completing the recorded work. Merely starting an in-memory background task would leave a loss window if the program stopped.

## Pause and check

<details>
<summary>1. Why does the sequential version take 540 ms in this example?</summary>

It waits for the 40 ms save and then the 500 ms delivery: 40 + 500 = 540 ms. The operations are sequential, and the example explicitly ignores other overhead.

</details>

<details>
<summary>2. Does a successful create response prove notification delivery finished?</summary>

Only if the API explicitly promises that. In the proposed delayed design it confirms the post and pending work were saved, while delivery may still be pending. The contract must make that distinction clear.

</details>

<details>
<summary>3. What could be lost if pending work exists only in program memory?</summary>

The need to deliver the notification could disappear when the program stops. Recoverable work must be stored durably; putting a task on another thread alone does not establish that guarantee.

</details>

## Useful later

Kafka can separate storing an event from processing its consequences. It does not by itself make a database save and event publication one successful operation. Later modules address that gap.

<details>
<summary>Optional deep dive — Timings and uncertainty</summary>

A real request can time out even after its database work succeeds. Our paper example assumes a clear response and fixed durations so we can isolate the waiting decision. Real API retries and uncertain publication outcomes need their own lessons.

</details>

## Stop/go

Continue only when you can derive 540 ms, name exactly what the proposed success response promises, and diagnose why an in-memory pending task can vanish. If unclear, revisit the two paper sequences before introducing messaging terms. Comprehension is still `NOT_ASSESSED` until you attempt the checkpoint.

## Next

[01B — Events, producers, and consumers](./01b-events-producers-consumers.md) names the completed fact and the programs that exchange it.
