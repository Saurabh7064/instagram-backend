# Idempotency Keys and Ambiguous Timeouts

## Question

How should a create-post API handle a retry when the first response was lost?

## Why this problem exists

A timeout tells the caller that no response arrived in time. It does not establish whether the database committed. Retrying a create request without a stable identity can create another post.

## Analogy and its limit

A customer keeps the same order reference when asking whether an order went through. The analogy stops because a distributed server must enforce reference ownership and atomic storage even during concurrent requests and crashes.

## Terms

- An **idempotency key** identifies one intended operation across retries.
- An **atomic commit** saves related database records together or saves none of them.
- A **request fingerprint** represents the fields that determine whether two requests mean the same operation.

## Reasoning and design

1. Require a client-generated key that stays stable for one publish attempt.
2. Scope the key to the authenticated user and operation; authenticate every retry.
3. Store the key, fingerprint, created post ID, and replayable result in the same database transaction as the post.
4. Serialize conflicting claims with a database uniqueness rule. A concurrent request either waits within a bound or receives an explicit retryable in-progress response.
5. Replay a completed result for the same fingerprint. Reject the same key with different content. Define retention and expiry before promising a deduplication window.

## Concrete trace

1. User 7 submits key `publish-91`, caption “sunset,” and media reference M.
2. The proposed transaction creates post 500 and saves the key-to-result record together.
3. The transaction commits, but the response is lost. The client observes a timeout.
4. The client repeats the same request with `publish-91`. The API verifies ownership and fingerprint, then returns the result for post 500.
5. The post count increased by one. Reusing that key with caption “sunrise” produces a conflict instead of silently replaying a different request. A crash before commit leaves neither committed record, so a retry can execute the operation.

## Instagram-backend mapping

[PostService](../../../../../src/main/java/com/instagram/backend/service/PostService.java) saves post metadata. [MediaStorageService](../../../../../src/main/java/com/instagram/backend/service/MediaStorageService.java) stores files separately.

**Proposed:** the key store, atomic post/result transaction, payload conflict response, and retention policy. A post-key transaction does not automatically deduplicate a separate media upload or delete an orphan file. See the broader [workflow answer](./12-correct-multi-step-workflows.md).

## Choices and trade-offs

- Choose durable keys for retried creates when duplicated effects matter. Storage, cleanup, and request comparison add complexity. The AWS discussion of [safe retries with idempotent APIs](https://aws.amazon.com/builders-library/making-retries-safe-with-idempotent-APIs/) explains caller intent and token reuse.
- A natural business identifier can enforce uniqueness when the product already has one. Caption text is not such an identifier: two intentional posts can have identical captions.
- Avoid a separate cache write as the sole guard: losing or expiring it can permit another post. Retention limits the guarantee; retrying beyond the supported window needs a documented policy.

## Failure drill

**Prediction:** interrupt the connection after commit but before response delivery. **Detection:** find one created post and a completed key record while the client reports a timeout. **Containment:** instruct the client to preserve the key; do not substitute a new one automatically. **Recovery:** replay the completed result after authentication. **Prevention:** test crashes before commit, lost responses after commit, parallel same-key requests, changed payloads, cross-user keys, and expiry.

## Two-to-three-minute interview answer

I would treat a timeout as an unknown outcome, rather than evidence that creation failed. The client supplies one stable idempotency key for an intended post and reuses it on every retry of that operation. A genuinely new post receives a new key, even if its caption is identical.

On the server, I would scope the key to the authenticated user and the create-post operation. I would atomically commit the post, its request fingerprint, and the stored result behind a unique key constraint. This closes the crash window in which the post commits but its deduplication record does not. If another request already owns that key, I would wait only within a bounded time or return a documented in-progress response. A completed matching request replays the original result. The same key with changed input is a conflict.

I would specify how long keys are retained, since deleting a record ends the corresponding protection against late retries. I would also reauthorize every request, including result replays. In this backend, post metadata and media storage are separate, so this proposal protects post creation without pretending that it atomically covers file uploads. My verification would inject a lost response after a successful commit and show that a retry returns the same post ID with only one row created.

## Practice status

Artifact: `READY`. Comprehension: `TODO`. Before marking practiced, explain the answer without notes, predict a failure, and answer the checkpoints before revealing their explanations.

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
