# Micro-lesson 01 — Why distributed systems have uncertain outcomes

[Module](./README.md) · [Next: CAP](./02-cap.md)

Core study: 10–15 minutes. Prerequisite: a client asks a backend to do work; a database stores records. No distributed-systems knowledge is assumed.

## You pressed Publish. Did it work? — Must understand

You write “Sunset,” press Publish, and wait. Eventually the app says “request timed out.” Your first instinct is to press Publish again. But what would that second press mean: finish the first post, or create another one?

The difficulty is that the screen tells you only that a reply did not arrive in time. It does not tell you where the work stopped. The request might never have reached the backend. Or the database might already contain your post, with only the success reply missing. We will follow that second possibility with post 42.

The browser, backend, and database are separate running programs. A running program, with its own execution and memory, is a **process**. Within one process, a function can return directly to its caller. Between processes, the request and the reply travel as separate messages. Either message can be delayed or lost.

That is enough to encounter a **distributed system**: independently running participants coordinating through messages. They need not be microservices or live on different machines. One backend talking to a separately running database already has this communication boundary.

## One part can fail while the rest keeps working

Suppose the database saves the post, but the browser connection breaks. The save and the broken connection can both be real. **Partial failure** means some participants or communication paths fail while others continue working. It is why “the system failed” is often too vague to guide recovery.

Think of a clerk ordering stock over the telephone. If the line goes dead after the order is spoken, the warehouse may have received it. Silence cannot settle the question. The analogy stops where people might find another informal way to check; a program needs recorded evidence or a defined recovery protocol.

A **timeout** is the caller deciding it has waited long enough. It is not the database saying “I saved nothing.” Waiting longer can reduce unnecessary timeouts, but cannot make a missing reply prove the absence of a save.

## Follow the save, then lose the reply

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

## What would make the second press safe?

We need a way to connect a retry to the *same intended operation*. Otherwise A can interpret the second request as a new post and create ID 43. [Lesson 14](./14-idempotency-and-delivery.md) teaches the durable request identity that makes this safe; do not assume it already exists.

A request identifier in logs helps find evidence, but logging alone does not prevent duplicates. A retry also needs a deadline and a limit so an outage does not produce endless work. Separate what the client knows from what the system has durably done.

Use a remote component when shared storage, independent capacity, or separate ownership justifies it. Prefer an ordinary local call when those benefits are unnecessary: an extra service creates extra uncertain boundaries. Distribution does not automatically make a system more reliable.

## Project mapping

[PostController.create](../../../../../src/main/java/com/instagram/backend/controller/PostController.java) accepts the request; [PostService.create](../../../../../src/main/java/com/instagram/backend/service/PostService.java) constructs and saves the post inside a transaction. Transaction completion surrounds the service invocation; `save` alone is not the same as telling the client the transaction committed. There is no request-key deduplication in these methods. The response-loss example is a proposed failure test, not an executed test result.

## Lose the reply after the save — paper exercise

Before continuing, predict what an ordinary retry does when post 42 has committed but its reply is lost. Is the backend obliged to return 42, or could it create 43?

The client timeout and the saved row can coexist. Request logs help locate the attempt; the durable database outcome tells us whether it committed. Until we know more, treat the result as unknown and avoid unlimited automatic retries of an unprotected create.

Recovery needs evidence identifying this operation. Matching caption text is insufficient because two intentional posts can share a caption. The later request-key design will connect retries to the original result and store them atomically. Test the response-loss boundary when implementing it; a log entry alone does not enforce one effect, and a broken connection can still delay the user's final answer.

## Before moving on

Artifact: `READY`; comprehension: `TODO`.

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
