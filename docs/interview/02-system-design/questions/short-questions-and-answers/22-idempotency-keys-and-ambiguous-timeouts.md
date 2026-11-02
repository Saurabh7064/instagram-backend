# Idempotency Keys and Ambiguous Timeouts

## Question

How should a create-post API handle a retry when the first response was lost?


## The post exists, but the client does not know

User 7 publishes caption “sunset” with media reference M. The server creates post 500 and commits, then the response is lost. A timeout appears on the phone. From the client's view, that looks the same as a request that never reached the server.

To retry safely, the client sends the same **idempotency key**, `publish-91`, for this intended publish. A deliberate second post gets a new key even if it has identical text. Caption matching cannot distinguish those intentions.

An order reference offers a useful analogy: asking about the same reference does not place another order. A server also needs enforced ownership, concurrent-claim rules and durable storage; the reference alone cannot provide those guarantees.

## Put the identity and result behind one commit

In the proposed design, scope the key to user 7 and this operation. Build a **request fingerprint** from the fields whose equality means “the same request,” using a defined comparison policy.

Save the post, fingerprint and replayable result in one **atomic commit**: either all become durable, or none does. Separate commits leave a dangerous gap where post 500 exists but its retry evidence does not.

If a matching retry arrives after commit, authenticate it, verify its scope and fingerprint, and return post 500's recorded result. A changed caption under `publish-91` is a conflict, not another create or a silent replay of unrelated content.

If two attempts arrive together, a database uniqueness rule must resolve their claim. Wait only within a defined bound or return a documented in-progress response. An in-memory check on one application copy cannot protect the claim across copies.

## Give the guarantee an expiry boundary

Retaining key records costs storage. Deleting them also ends the associated protection against late retries, so define the supported retry window and behavior beyond it.

This transaction protects post metadata. It does not automatically include the earlier file upload. Media needs its own stable identity, ownership checks and abandoned-upload policy. Resolve each boundary honestly instead of calling the whole Publish button one database transaction.

## Where this appears in our backend

[PostService](../../../../../src/main/java/com/instagram/backend/service/PostService.java) saves post metadata. [MediaStorageService](../../../../../src/main/java/com/instagram/backend/service/MediaStorageService.java) stores files separately.

**Proposed:** the key store, atomic post/result transaction, payload conflict response, and retention policy. A post-key transaction does not automatically deduplicate a separate media upload or delete an orphan file. See the broader [workflow answer](./12-correct-multi-step-workflows.md).

## Choosing the approach and paying its costs

- Choose durable keys for retried creates when duplicated effects matter. Storage, cleanup, and request comparison add complexity. The AWS discussion of [safe retries with idempotent APIs](https://aws.amazon.com/builders-library/making-retries-safe-with-idempotent-APIs/) explains caller intent and token reuse.
- A natural business identifier can enforce uniqueness when the product already has one. Caption text is not such an identifier: two intentional posts can have identical captions.
- Avoid a separate cache write as the sole guard: losing or expiring it can permit another post. Retention limits the guarantee; retrying beyond the supported window needs a documented policy.


## Paper drill: lose the response after commit

Predict what a retry with `publish-91` should return when the database contains post 500 and its completed result record. It should return that recorded result after verification, with the post count increased by one overall.

Detect the situation by correlating the operation record with the saved post, while treating the client's timeout as an unknown outcome. Contain duplication by preserving the original key rather than automatically assigning a new one.

Replay the result to recover. Prevent regressions with cases for a pre-commit crash, a lost post-commit response, parallel same-key attempts, changed input, another user's key and expired retention. Each probes a different boundary of the promise.

## Two-to-three-minute interview answer

I would treat a timeout as an unknown outcome, rather than evidence that creation failed. The client supplies one stable idempotency key for an intended post and reuses it on every retry of that operation. A genuinely new post receives a new key, even if its caption is identical.

On the server, I would scope the key to the authenticated user and the create-post operation. I would atomically commit the post, its request fingerprint, and the stored result behind a unique key constraint. This closes the crash window in which the post commits but its deduplication record does not. If another request already owns that key, I would wait only within a bounded time or return a documented in-progress response. A completed matching request replays the original result. The same key with changed input is a conflict.

I would specify how long keys are retained, since deleting a record ends the corresponding protection against late retries. I would also reauthorize every request, including result replays. In this backend, post metadata and media storage are separate, so this proposal protects post creation without pretending that it atomically covers file uploads. My verification would inject a lost response after a successful commit and show that a retry returns the same post ID with only one row created.


## Before moving on

Artifact: `READY`; comprehension: `TODO`. Explain this scenario in your own words, predict the paper drill, and attempt the questions before opening their answers. Record learning only from your own explanation and predictions.

## Questions and explained answers

<details>
<summary>Does an HTTP timeout mean the post was not committed?</summary>

No. The commit can finish before the response is lost. The caller needs a stable key or a lookup contract to resolve the ambiguity.
</details>

<details>
<summary>Why save the key result with the post?</summary>

Separate commits leave a crash window where the post exists but the retry cannot discover its result, allowing duplication.
</details>

<details>
<summary>Why reject a changed payload under the same key?</summary>

The key claims one intended operation. Replaying an unrelated result would hide a client bug and violate that operation’s meaning.
</details>
