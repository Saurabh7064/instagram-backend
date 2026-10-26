# Micro-lesson 01 — Distributed systems and partial failure

[Module](./README.md)

- Artifact: `READY`; comprehension: `TODO`.
- Time: 10–15 minutes. Primary idea: distributed systems and partial failure.
- Prerequisite: None; start here.
- Original roadmap topics: 1.

## Why does this exist? — Must understand

A distributed system has independently running programs that coordinate by sending messages. A node is one participating process or machine. Partial failure means one part fails while others continue. Even one Spring Boot service calling a separate database crosses a failure boundary; microservices are not required.

## Analogy

Two shop clerks communicate by phone. One can keep working when the other loses reception. Unlike clerks, programs cannot reliably distinguish a slow peer from a failed peer using silence alone.

## Predict before reading

Did the timed-out create fail? Explain your prediction before expanding the checkpoints below.

## Concrete flow

1. Client sends create-post request.
2. backend saves post 42.
3. database commits.
4. response connection breaks. The client sees a timeout, while post 42 exists. A timeout describes missing knowledge, not a proven rollback.

## Instagram-backend mapping

[PostService.create, feed, and transactional methods](../../../../../src/main/java/com/instagram/backend/service/PostService.java) are the real database boundary. Replication, consensus, distributed locks, durable request-key handling, and multi-service workflows in this lesson are **proposed**, not implemented by these annotations.

## Decision and trade-off

Use remote components when independent capacity or ownership justifies them. Keep a local call when it suffices: every network boundary introduces delay, uncertain outcomes, and recovery work.

## Failure drill — prediction, symptom, recovery, prevention

Predict whether a timeout proves failure. Drop the reply after commit in a proposed test. Observe one saved row and a client timeout. Inspect durable state before recovery; retry using a stable operation identity rather than a new create request.

These are design/paper drills, not claims that a replica cluster or fault-injection lab was executed.

## Questions and explained answers

<details>
<summary>1. Did the timed-out create fail?</summary>

Unknown to the client: the saved row proves the operation can succeed despite the timeout.

</details>

<details>
<summary>2. Is one backend plus a separate database distributed?</summary>

Yes. Their processes and communication can fail independently.

</details>

<details>
<summary>3. What evidence should recovery inspect?</summary>

The durable operation result, correlated with the original request identity; a missing HTTP response alone is insufficient.

</details>

## Teach-back and stop/go

Explain the problem, trace the example, defend the choice, and diagnose the failure in 60–90 seconds without notes. Proceed only after a correct prediction, explanation, and failure diagnosis; reading this artifact does not complete comprehension.
