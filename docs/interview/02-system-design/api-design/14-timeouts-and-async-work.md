# 14. Timeouts, rate limits, and asynchronous work

## Question

How do you keep slow or excessive API work from exhausting the server?

[Module sequence](README.md) · Artifact: READY · Learning: TODO

## What you will learn

Explain what a timeout does and does not stop, follow a media job after 202, and work out how waiting and admission limits keep that promise realistic.

Builds on [response contracts](03-status-and-response-contracts.md), especially the difference between 201 and 202.

## A timeout ends waiting, not necessarily the work

Suppose processing an uploaded image takes 30 seconds. The phone waits 10 seconds, gives up, and offers Retry. The backend may still be processing the first attempt. Repeating it can start more work just when the system is already busy.

A **timeout** limits how long a component waits. It does not prove that work somewhere else stopped or rolled back. For long processing, the server can instead save a record of the work and return its ID promptly. The client asks for the result later. That tracked work is an **asynchronous job**: completion happens after the initial response.

For job 12, the initial response can be `202 Accepted` with state `QUEUED`. It means the server has accepted responsibility under the documented contract. It does not mean the image has finished, or that it will certainly succeed. A later status request must distinguish finished, failed, and still-running work.

A service desk's tracking receipt captures the idea: you leave the work and return with the receipt to check it. The comparison stops when the server restarts. An in-memory task can vanish, so the promise needs a durable record and a way to recover unfinished work.

We will keep that media job as the main example. Limits on incoming requests and a time budget for short synchronous stages support the same goal: accepting only work the system can realistically handle.

## Return a receipt after durable acceptance

The project uses [Spring Boot 4.0.2 and Java 24](../../../../build.gradle). The excerpt below is a design proposal unless identified as current code; omitted imports and collaborators still need implementation.

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

### Read the boundary in the code

`acceptDurably` is the important promise in this proposed handler: the job and a recoverable execution path must exist before the 202 response. `Location` points to a proposed status route that must check the caller owns the job. `JobView` reports the identity and current state rather than pretending the final image is ready.

A background thread alone cannot fulfill this contract. If the process stops, there must still be enough stored information to find the accepted work and reach a documented success or failure state.

## Follow job 12 beyond the initial response

A processing request arrives for authorized media that takes about 30 seconds to transform. The proposed acceptance service saves job 12 as queued and commits recoverable dispatch information. Only then does the controller return 202 with its status address. The client has a durable thing to ask about even if the connection ends.

A worker claims the job, records running state, produces the output, and durably records success with its media reference. Polling job 12 can now report `SUCCEEDED`. Polling before completion must report queued or running; a GET must not launch a second attempt.

If the worker crashes, a recovery policy has to find the abandoned job and decide whether to resume or retry it. Merely starting a background thread before returning 202 provides no such recovery. We will use a time-limited worker claim below to make that responsibility explicit.

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

## Apply this to the backend

[MediaController](../../../../src/main/java/com/instagram/backend/controller/MediaController.java) currently accepts a multipart upload and returns a media response. It has no `/media-jobs` acceptance or polling route. The proposed processing lifecycle is additional work beyond storing the uploaded bytes.

For F-030, draft job states, owner-checked polling, a queue cap, a waiting budget, and restart recovery. State what happens if acceptance succeeds but dispatch or processing fails; “the thread will run later” is not a recovery plan.

## Useful later: choices and costs

Keep short predictable work synchronous. Use jobs for long processing when clients can handle polling and delayed results. A full queue needs admission control; 202 is not permission to accept unbounded work. Timeouts do not prove remote work stopped, so retries of writes need deduplication. Return 503 for unavailable service capacity rather than blaming one caller indiscriminately.

## Restart just after accepting job 12

Before a future failure test, predict what remains after 202 if job 12 exists only as a task in memory. Restarting the process removes the task; the caller holds a receipt for work the backend can no longer find or finish.

Detect this with a restart test, missing/stuck job counts, and age since acceptance. Contain new acceptance while the execution path is unreliable. Recovery must use durable job/dispatch information to resume or explicitly fail accepted work; if no such record exists, recovery may require reconciliation with the caller rather than an invented success. Prevent recurrence by saving acceptance and recoverable dispatch before 202, bounding queues, and verifying every accepted job can reach a documented outcome after a worker crash.

## Interview-ready answer

I separate request admission, waiting time, and asynchronous completion. I allocate dependency and retry time from one end-to-end budget, because independent timeouts can exceed the user’s deadline. Rate limits bound admitted work; queue caps and load shedding handle overload. For long processing I accept only durably recorded work, expose an authorized job status, and define terminal failure, retry, cancellation, and retention. A timeout does not prove a remote write stopped, and 202 does not guarantee success. In the media pipeline I would test worker crashes, duplicate dispatch, stuck jobs, full queues, and failed cleanup while measuring resource recovery, not just initial acceptance.

## Before moving on

Explain what has to survive a restart after 202 for job 12. Continue when you can distinguish stopped waiting from stopped work and account for acceptance, bounded execution, failure, and recovery.

Attempt the questions before opening their answers and record the explanation in [learner progress](learner-progress.md). This revision leaves learning status unchanged; a finished document is not evidence that you have demonstrated the idea.

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
