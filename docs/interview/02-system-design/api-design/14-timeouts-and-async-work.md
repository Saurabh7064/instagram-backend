# 14. Timeouts, rate limits, and asynchronous work

## Question

How do you keep slow or excessive API work from exhausting the server?

[Module sequence](README.md) · Study in two passes: core model, then design decisions (15–20 minutes each) · Artifact: READY · Learning: TODO

## What you will learn

Budget end-to-end deadlines, compare admission policies, define durable job states and cancellation, and limit retries and queue amplification.

Prerequisites: Lesson 13 and its stop/go check. All later lessons remain TODO until their own checkpoints are attempted.

## Must understand: problem and model

Unlimited requests and long dependency waits consume finite capacity. A timeout bounds waiting; a rate limit bounds accepted work over time; an asynchronous job moves completion beyond the initial response. A service desk hands out a tracking receipt for long work; unlike the analogy, a server crash can lose an in-memory queue.

## Spring Boot example

Examples are proposed except where explicitly identified as current code. Imports/wiring are omitted; named new services and DTOs need implementation. Preserve current authorization rules. The project uses [Spring Boot 4.0.2 and Java 24](../../../../build.gradle).

```java
@PostMapping("/media-jobs")
ResponseEntity<JobView> submit(
        @Valid @RequestBody MediaJobRequest request) {
    var job = mediaJobs.acceptDurably(viewerId(), request);
    return ResponseEntity.accepted()
            .location(URI.create("/api/media-jobs/" + job.id()))
            .body(job);
}
```

### Walkthrough

This proposed route returns 202 only after durable acceptance. JobView exposes an ID and state; a proposed GET status route must enforce ownership. acceptDurably needs persistent state and recoverable dispatch. A background thread alone is not durable. Give failed jobs an explicit terminal state and safe retry policy.

## Request trace: predict, act, observe, explain

Before following the trace, predict the status and final stored state. Then trace these transitions in order:

Upload processing takes 30 seconds. Submit -> persist job 12 -> 202 pending -> worker processes -> GET job 12 returns succeeded with media URL. If worker crashes, job remains visible and can resume under a lease/recovery policy. Excess traffic may receive 429 with a documented retry delay.

The observable result includes the response **and** the final stored state. A successful status alone cannot prove concurrency or authorization correctness.

## Separate admission, waiting, and completion

These are related capacity decisions but different contracts. A rate limiter decides whether to admit a request. Timeouts decide how long a component waits. A job lifecycle decides what accepted work does after the initial response.

### Timeout budget with exact arithmetic

Assume an illustrative 1000 ms end-to-end budget. Reserve 100 ms for serialization/network overhead; 900 ms remains for work. If authentication consumes 100 ms, 800 remains. Reserve 100 ms to persist the final result; dependencies together have at most 700 ms. Two sequential 500 ms dependency timeouts exceed that budget: 100 + 500 + 500 + 100 + 100 = 1300 ms. Choose dependency budgets and any retries from the remaining deadline, not independent defaults.

A connect timeout bounds connection establishment, a read timeout bounds waiting for response data, and an overall deadline bounds the whole attempt. Their interaction depends on the client library. A client timeout does not guarantee the server stopped or rolled back. Avoid retrying a non-deduplicated write solely because it timed out.

### Rate-limit contract

For a fixed-window example limit 100 requests/minute, 100 at 12:00:59 and 100 at 12:01:00 can allow a burst of 200 in two seconds. A token-bucket policy accumulates tokens up to a capacity and spends one per admitted request; it allows controlled bursts but needs atomic accounting. One counter per instance does not enforce a global budget across replicas. Key policy by authenticated actor/operation, handle anonymous traffic separately, and account for shared-IP users.

A proposed 429 includes Retry-After: 10. That is a wait instruction under the policy, not a promise that capacity will exist ten seconds later. Add jitter to retries so clients do not wake together. Use bounded attempts and a global retry budget; retry storms can make an overloaded dependency less able to recover.

### A durable job state machine

| State | Meaning | Permitted transition |
|---|---|---|
| QUEUED | Accepted durably | RUNNING or CANCELLED |
| RUNNING | Worker owns a lease | SUCCEEDED, FAILED, or recoverable retry |
| SUCCEEDED | Output durably available | Terminal |
| FAILED | Terminal error recorded | Explicit new attempt per policy |
| CANCELLED | Cancellation completed | Terminal |

A lease is time-limited ownership used to recover abandoned jobs. Cancellation requested is not cancellation completed; a worker may already have committed output. Persist state transitions with concurrency control, cap outstanding jobs, and document status retention and result expiration.

GET a status resource must be read-only: polling should not itself launch another attempt. For F-030, keep binary upload separate from a processing request referencing authorized media. Use a durable outgoing dispatch record so a crash between job acceptance and broker publish cannot strand work silently.

## Project mapping and practice

Read the actual [backend implementation](../../../../src/main/java/com/instagram/backend/controller/MediaController.java) before changing it. The example above distinguishes existing behavior from a proposed extension; a linked file establishes the current starting point, not proof that the proposed design exists.

**Practice task:** For F-030 design durable job states, a queue limit, ownership-checked polling, timeout budget, and recovery after a worker restart.

Keep this a design exercise until a feature change is explicitly undertaken. An implementation must follow the repository integration-test, API verification, documentation, and commit workflow.

## Useful later: choices and costs

Keep short predictable work synchronous. Use jobs for long processing when clients can handle polling and delayed results. A full queue needs admission control; 202 is not permission to accept unbounded work. Timeouts do not prove remote work stopped, so retries of writes need deduplication. Return 503 for unavailable service capacity rather than blaming one caller indiscriminately.

## Controlled failure drill

Predict a worker crash after returning 202 with only an in-memory task. Job vanishes. Detect stuck-job age and recovery tests, contain new submissions, recover from durable records, prevent with persistent dispatch, bounded queues, and monitored terminal states.

Describe the prediction before reading the symptom. In a future lab, use disposable data, record the observed response and state, and verify recovery rather than assuming it succeeded.

## Interview-ready answer

I separate request admission, waiting time, and asynchronous completion. I allocate dependency and retry time from one end-to-end budget, because independent timeouts can exceed the user’s deadline. Rate limits bound admitted work; queue caps and load shedding handle overload. For long processing I accept only durably recorded work, expose an authorized job status, and define terminal failure, retry, cancellation, and retention. A timeout does not prove a remote write stopped, and 202 does not guarantee success. In the media pipeline I would test worker crashes, duplicate dispatch, stuck jobs, full queues, and failed cleanup while measuring resource recovery, not just initial acceptance.

## Stop/go and learner evidence

Proceed only after explaining the request trace without notes, predicting the failure, and answering all three checks below before opening their answers. Record the attempt in [learner progress](learner-progress.md). Artifact readiness does not establish comprehension or implementation readiness.

## Questions and explained answers

<details>
<summary>1. When is it safe to return 202 for a job?</summary>

After durable acceptance under a documented contract, with an ownership-protected status resource and recoverable execution path. It promises acceptance, not eventual success. An in-memory thread can disappear after the response.

</details>

<details>
<summary>2. Why can two 500 ms dependency calls exceed a 1000 ms budget?</summary>

They consume 1000 ms before authentication, persistence, serialization, and network overhead. Sequential stages must share the remaining overall deadline rather than each using the full user budget.

</details>

<details>
<summary>3. Does a local per-instance rate counter enforce one global quota?</summary>

No. Several replicas can each admit the full local amount. A global policy requires coordinated atomic accounting or an explicitly partitioned budget.

</details>

<details>
<summary>4. Why is cancellation requested not a terminal cancelled state?</summary>

The worker may already be running or committing output. A request to cancel must be reconciled with execution; report CANCELLED only when the contract’s cancellation condition is met.

</details>

<details>
<summary>5. Does a durable job row alone guarantee it will be dispatched?</summary>

No. A crash between row commit and a separate broker publish can strand it. Use recoverable scanning/dispatch or an outgoing record strategy and monitor stuck age.

</details>
