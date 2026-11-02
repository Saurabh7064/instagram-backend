# Correct Multi-Step Workflows

## Question

A media upload succeeds, post metadata is saved, but the response or follow-up event is lost. The client retries and may repeat some steps.

**“How do you keep a multi-step post-publish workflow correct when steps fail or repeat?”**


## One Publish button crosses several boundaries

The photo upload has completed. The client asks to create a post using it, the database saves the post, and the HTTP response disappears. The client sees a timeout. Should it try again?

It should be able to, but the server needs to recognize that retry as the same intended publish. **Idempotency** means repeated processing has the intended effect of one operation. Give this publish the stable key `publish-91`; generating a new key for the retry would describe a new operation instead.

## Save the evidence with the effect

In the proposed design, one transaction commits the post, the key's request fingerprint and stored result, and outbox event `e91`. All of those records must share the commit boundary. Saving the post first and its key result afterwards would leave another crash window in which a retry could create a second post.

If the response is lost after commit, retrying `publish-91` returns the stored post result after authenticating the caller and checking the input matches. If the transaction failed before commit, none of those changes exists and the request can execute. Concurrent claims need a database uniqueness rule and a bounded in-progress policy.

A parcel tracking number offers a useful analogy: repeating a status enquiry does not order another parcel. Its limit is that a server must enforce the identity and transaction rules automatically, including when two requests arrive together.

## What about steps outside that transaction?

A PostgreSQL transaction does not include a separately stored photo or a message broker. The outbox makes publication retryable; consumers must also commit their local effect with their duplicate-detection state where that is possible.

For a larger workflow, a **saga** records progress across separate local transactions and defines corrective actions for failures. Deleting an unused photo may be a valid cleanup; undoing an already delivered external effect may not be possible. A corrective action is not automatically a true rollback.

**Reconciliation** compares the recorded workflow with actual durable state and repairs a discrepancy. If a media outcome is unknown, inspect its stable identity before blindly repeating or deleting it. Keep a documented policy for abandoned uploads, unfinished posts and irreversible effects.

Use this machinery where the workflow needs it. When all required changes fit one database transaction, that simpler boundary avoids unnecessary intermediate states.

## Where this appears in our backend

[PostService](../../../../../src/main/java/com/instagram/backend/service/PostService.java) currently saves a post in one Spring transaction. [MediaStorageService](../../../../../src/main/java/com/instagram/backend/service/MediaStorageService.java) stores media before post creation as a separate operation. [PostLike](../../../../../src/main/java/com/instagram/backend/domain/PostLike.java) provides a concrete idempotency guard through its unique `(post_id, user_id)` constraint.

**Proposed, not implemented:** publish idempotency keys, workflow states, an outbox, consumer inbox/deduplication records, orphan-media cleanup, and reconciliation jobs.

## Choosing the approach and paying its costs

- Use idempotency keys for externally retried writes; do not generate a new key on every retry. Stored keys and responses require retention and conflict rules.
- Use a Saga when independent owners must complete a workflow over time. Do not use it when one database transaction is sufficient; Sagas expose temporary intermediate states.
- Use compensation only when the business action can be meaningfully counteracted. A refund or cleanup is a new auditable action, not erased history.


## Paper drill: the effect happened, the acknowledgement did not

Predict what a worker does after completing an effect and stopping before acknowledgement. On redelivery, it can attempt the effect again. The message appearing twice is expected; applying an unsafe effect twice is the failure to prevent.

Detect repetition by event ID and compare it with durable workflow and output records. Pause unsafe processing while deciding which effect already completed. Recover by replaying duplicate-safe transitions or applying an auditable corrective action where the business permits one.

Prevent recurrence by storing local duplicate detection with the local effect, using stable identities for remote effects, and testing crashes on both sides of every commit and acknowledgement boundary. A database transaction protects only the resources it actually owns.

## Two-to-three-minute interview answer

I would assume retries, duplicate events, and ambiguous timeouts are normal. The client supplies a stable idempotency key, and the API commits the key, request fingerprint and stored result atomically with the post or workflow record. If the response is lost, the retry returns the original result instead of creating another post.

Each component performs only its own local transaction. The post transaction also writes an outbox event, so a crash cannot leave a committed post with no durable publication record. The relay may publish more than once, so consumers record event IDs and apply transitions idempotently. A Saga coordinates later steps and defines compensation where a real business reversal exists; reconciliation repairs cases whose outcome cannot be inferred from acknowledgements alone.

For this project, post metadata and media storage are already separate operations, while likes demonstrate a database uniqueness guard. Full publish idempotency, outbox delivery, workflow status, and orphan cleanup are future work.


## Before moving on

Artifact: `READY`; comprehension: `TODO`. Explain this scenario in your own words, predict the paper drill, and attempt the questions before opening their answers. Record learning only from your own explanation and predictions.

## Questions and explained answers

<details>
<summary>Why must a retry reuse the same idempotency key?</summary>

The stable key tells the server that both attempts represent one logical action. A new key makes the retry indistinguishable from a request to create another post.
</details>

<details>
<summary>What failure does a transactional outbox prevent?</summary>

It prevents a committed business change from permanently losing its event because the process crashed between the database commit and a separate broker publish.
</details>

<details>
<summary>Why is compensation not always a rollback?</summary>

Some effects cannot be erased. Refunding a charge or deleting an orphaned object is a new recorded action that restores an acceptable business state while preserving history.
</details>
