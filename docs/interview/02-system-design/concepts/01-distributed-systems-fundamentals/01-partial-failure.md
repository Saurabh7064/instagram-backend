# Micro-lesson 01 — Why distributed systems have uncertain outcomes

[Module](./README.md) · [Next: CAP](./02-cap.md)

- Artifact: `READY`; comprehension: `TODO`.
- Core study: 10–15 minutes. Primary idea: a missing response does not tell you whether remote work happened.
- Prerequisite: a client asks a backend to do work; a database stores records. No distributed-systems knowledge is assumed.
- Core terms: process, distributed system, partial failure.

## Start with the problem — Must understand

You press Publish. The app waits, then shows “request timed out.” Should pressing Publish again create the original post or a second post? Before choosing a retry policy, we need to understand why the first request's outcome is uncertain.

A **process** is a running program with its own execution and memory. The browser, Spring Boot backend, and database can run as separate processes. A function call inside one process can return directly to its caller. A call to another process must cross a communication boundary: the request and the response are separate messages, and either can be delayed or lost.

A **distributed system** consists of independently running participants coordinating through messages. Separate machines are common, but separate communicating processes on one machine already expose some of these failure boundaries. You do not need microservices: one backend using a separately running database must already handle communication failure.

## Why failure can be partial

**Partial failure** means some participants or communication paths fail while others continue working. Your browser can lose its connection while the database remains healthy. The backend can stop after saving a post. The database can be unreachable while the backend still answers a health request that does not query it.

Compare a shop clerk placing an order by telephone. The line goes dead after the clerk speaks. The warehouse may have received the order, or may never have heard it. The analogy captures uncertainty, but programs have no extra human judgment that can reliably distinguish a dead peer from a slow one using silence alone.

A **timeout** is a local decision to stop waiting after a chosen duration. It is not a message from the remote participant saying “nothing happened.” Extending the duration can reduce unnecessary failures, but cannot eliminate this uncertainty.

## Worked request: post 42

Fixed setup: one client, backend A, and a separate database. The client requests one new post with caption “Sunset.” A database **commit** is the point at which the transaction's changes become durable according to its storage contract. The application must not report success before its required commit.

1. The client sends the create request to A. No post exists yet.
2. A asks the database to insert the post. The database assigns ID 42.
3. The transaction commits. Post 42 now exists even if the client disappears.
4. A sends a success response containing ID 42. The connection breaks before the client receives it.
5. The client reaches its waiting limit and reports a timeout. Its screen contains no ID, but the database contains post 42.

Now compare three possible failures on this **same request**:

| Failure point | Database result | What the client may observe |
|---|---|---|
| Request never reaches A | No post | Timeout |
| Database rejects the insert and rolls it back | No post | Timeout if the error response is also lost |
| Commit succeeds, response is lost | Post 42 exists | Timeout |

The observations are identical while the durable outcomes differ. Therefore the screen's timeout cannot select a safe recovery by itself. This is an information problem, not simply a slow-server problem.

## What follows from this model

We need a way to connect a retry to the *same intended operation*. Otherwise A can interpret the second request as a new post and create ID 43. [Lesson 14](./14-idempotency-and-delivery.md) teaches the durable request identity that makes this safe; do not assume it already exists.

A request identifier in logs helps find evidence, but logging alone does not prevent duplicates. A retry also needs a deadline and a limit so an outage does not produce endless work. Separate what the client knows from what the system has durably done.

Use a remote component when shared storage, independent capacity, or separate ownership justifies it. Prefer an ordinary local call when those benefits are unnecessary: an extra service creates extra uncertain boundaries. Distribution does not automatically make a system more reliable.

## Project mapping

[PostController.create](../../../../../src/main/java/com/instagram/backend/controller/PostController.java) accepts the request; [PostService.create](../../../../../src/main/java/com/instagram/backend/service/PostService.java) constructs and saves the post inside a transaction. Transaction completion surrounds the service invocation; `save` alone is not the same as telling the client the transaction committed. There is no request-key deduplication in these methods. The response-loss example is a proposed failure test, not an executed test result.

## Failure drill — paper exercise

- **Predict:** the reply is lost after commit. Does retrying an ordinary create safely return 42?
- **Symptom:** client timeout; database has 42; a naive retry may create 43. Compare request logs with saved rows.
- **Contain:** avoid automatic unlimited retries of unprotected creates. Preserve the original request identity and outcome as unknown.
- **Recover:** inspect durable operation evidence. Do not identify an operation solely by matching caption text; two intentional posts can share it.
- **Prevent:** implement atomic request-key/result storage before promising safe retries. A broken network can still delay the user seeing the result.

## Recap and stop/go

Reconstruct the five steps without notes. Explain how a successful commit and a timeout coexist, and why neither a longer wait nor a log entry alone provides duplicate protection. Proceed after you can predict the three failure points' outcomes.

## Questions and explained answers

<details>
<summary>1. Why is backend plus database already a distributed concern?</summary>

They execute independently and communicate. A healthy backend cannot infer the database's exact outcome from a broken connection, even without multiple backend services.

</details>

<details>
<summary>2. In the worked request, did Publish fail?</summary>

The business operation succeeded: post 42 committed. The client failed to learn its result. Saying only “it failed” hides the distinction needed for recovery.

</details>

<details>
<summary>3. Why can a timeout not justify blindly creating another post?</summary>

The same timeout is compatible with no insert or a committed insert. A new create can produce post 43 after 42 already exists. Recovery needs an identity for the intended operation and durable evidence tied to it.

</details>
