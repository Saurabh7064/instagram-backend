# 14. Timeouts, rate limits, and asynchronous work

## Question

How do you keep slow or excessive API work from exhausting the server?

[Module sequence](README.md) · Estimated time: 15–20 minutes · Artifact: READY · Learning: TODO

## What you will learn

Explain the problem behind timeouts, rate limits, and asynchronous work, trace its externally visible behavior, and identify one implementation risk before proposing a change.

Prerequisites: Lesson 13 and its stop/go check. All later lessons remain TODO until their own checkpoints are attempted.

## Must understand: problem and model

Unlimited requests and long dependency waits consume finite capacity. A timeout bounds waiting; a rate limit bounds accepted work over time; an asynchronous job moves completion beyond the initial response. A service desk hands out a tracking receipt for long work; unlike the analogy, a server crash can lose an in-memory queue.

## Spring Boot example

Unless explicitly described as existing code, this is a proposed teaching excerpt, not an endpoint implemented by this documentation change. Application-specific collaborators and types require implementation; imports and class wiring are omitted. Preserve authentication when adapting an excerpt. This project uses Spring Boot 4.0.2 and Java 24, as recorded in [build configuration](../../../../build.gradle).

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

## Project mapping and practice

Read the actual [backend implementation](../../../../src/main/java/com/instagram/backend/controller/MediaController.java) before changing it. The example above distinguishes existing behavior from a proposed extension; a linked file establishes the current starting point, not proof that the proposed design exists.

**Practice task:** For F-030 design durable job states, a queue limit, ownership-checked polling, timeout budget, and recovery after a worker restart.

Keep this a design exercise until a feature change is explicitly undertaken. An implementation must follow the repository integration-test, API verification, documentation, and commit workflow.

## Useful later: choices and costs

Keep short predictable work synchronous. Use jobs for long processing when clients can handle polling and delayed results. A full queue needs admission control; 202 is not permission to accept unbounded work. Timeouts do not prove remote work stopped, so retries of writes need deduplication. Return 503 for unavailable service capacity rather than blaming one caller indiscriminately.

## Controlled failure drill

Predict a worker crash after returning 202 with only an in-memory task. Job vanishes. Detect stuck-job age and recovery tests, contain new submissions, recover from durable records, prevent with persistent dispatch, bounded queues, and monitored terminal states.

Describe the prediction before reading the symptom. In a future lab, use disposable data, record the observed response and state, and verify recovery rather than assuming it succeeded.

## Interview-ready answer outline

For a two-minute answer, start with the caller’s problem, define the main concept in plain language, explain the request trace above, then justify one choice and its cost. Finish by explaining how the failure drill would be detected and prevented in this backend. Do not claim the proposed extension is deployed.

## Stop/go and learner evidence

Proceed only after explaining the request trace without notes, predicting the failure, and answering all three checks below before opening their answers. Record the attempt in [learner progress](learner-progress.md). Artifact readiness does not establish comprehension or implementation readiness.

## Questions and explained answers

<details>
<summary>1. When is it safe to return 202 for a job?</summary>

After durable acceptance under a documented contract, with an ownership-protected status resource and recoverable execution path. It promises acceptance, not eventual success. An in-memory thread can disappear after the response.

</details>

<details>
<summary>2. What exact response or state should the worked request produce, and why?</summary>

Upload processing takes 30 seconds. Submit -> persist job 12 -> 202 pending -> worker processes -> GET job 12 returns succeeded with media URL. If worker crashes, job remains visible and can resume under a lease/recovery policy. Excess traffic may receive 429 with a documented retry delay.

This follows from the request boundary and state checks in the walkthrough, not merely from choosing a URL.

</details>

<details>
<summary>3. Which failure can remain despite a successful happy-path request, and how would you prevent it?</summary>

Predict a worker crash after returning 202 with only an in-memory task. Job vanishes. Detect stuck-job age and recovery tests, contain new submissions, recover from durable records, prevent with persistent dispatch, bounded queues, and monitored terminal states.

A successful request proves one case. The controlled failure needs its own observation and regression check.

</details>
