# Correct Multi-Step Workflows

## Question

A media upload succeeds, post metadata is saved, but the response or follow-up event is lost. The client retries and may repeat some steps.

**“How do you keep a multi-step post-publish workflow correct when steps fail or repeat?”**

## Why this problem exists

A workflow that crosses storage, a database, and a broker cannot usually commit everything in one local transaction. Timeouts are ambiguous: the caller may not know whether a step failed or completed just before the response disappeared. The design must make retries safe and provide a way to finish or repair partial work.

## Analogy: tracked parcel delivery

A parcel has one tracking number and recorded states such as accepted, shipped, and delivered. Repeating a status message does not create another parcel. The analogy stops because some distributed side effects cannot be undone; compensation may be a new action rather than a true reversal.

## Terms to establish

- **Idempotency** means repeating the same logical request has the same intended effect as processing it once.
- A **Saga** coordinates local transactions across components using later steps and compensations.
- A **transactional outbox** commits a business change and its event record together.
- **Reconciliation** compares actual state with expected state and repairs discrepancies.

## What I would do

1. Give the publish attempt a stable idempotency key and persist its result or workflow state.
2. Keep each owner’s state change locally transactional; do not claim one transaction spans every service.
3. Commit the post and outbox record together, then publish asynchronously with retry.
4. Make every consumer deduplicate by event ID and make each transition valid only from expected prior states.
5. Define compensation for reversible work and reconciliation for ambiguous or irreversible outcomes.

## Concrete walkthrough

1. The client sends `POST /api/posts` with idempotency key `publish-91`.
2. The service finds no prior result, stores the post and `PostCreated(eventId=e91)` outbox row in one transaction, then records the response for `publish-91`.
3. The HTTP response is lost, so the client retries with the same key.
4. The service returns the stored result instead of creating a second post.
5. A relay publishes `e91`; duplicate deliveries are ignored by consumers that already recorded `e91`.
6. If media processing later fails, the workflow marks the post unavailable or deletes orphaned media according to product policy.

## Instagram-backend mapping

[PostService](../../../../../src/main/java/com/instagram/backend/service/PostService.java) currently saves a post in one Spring transaction. [MediaStorageService](../../../../../src/main/java/com/instagram/backend/service/MediaStorageService.java) stores media before post creation as a separate operation. [PostLike](../../../../../src/main/java/com/instagram/backend/domain/PostLike.java) provides a concrete idempotency guard through its unique `(post_id, user_id)` constraint.

**Proposed, not implemented:** publish idempotency keys, workflow states, an outbox, consumer inbox/deduplication records, orphan-media cleanup, and reconciliation jobs.

## Choices and trade-offs

- Use idempotency keys for externally retried writes; do not generate a new key on every retry. Stored keys and responses require retention and conflict rules.
- Use a Saga when independent owners must complete a workflow over time. Do not use it when one database transaction is sufficient; Sagas expose temporary intermediate states.
- Use compensation only when the business action can be meaningfully counteracted. A refund or cleanup is a new auditable action, not erased history.

## Failure drill

**Prediction:** a worker completes a side effect, crashes before acknowledging the event, and repeats it after restart. **Symptom and detection:** the same event ID appears twice and duplicate output is attempted. **Containment:** pause the consumer if the effect is unsafe and reject already-recorded event IDs. **Recovery:** reconcile the authoritative workflow state, apply compensation where required, and replay safely. **Prevention:** persist deduplication with the local effect and test crashes before and after every acknowledgement boundary.

## Two-to-three-minute interview answer

I would assume retries, duplicate events, and ambiguous timeouts are normal. The client supplies a stable idempotency key, and the API stores the key with the resulting post or workflow record. If the response is lost, the retry returns the original result instead of creating another post.

Each component performs only its own local transaction. The post transaction also writes an outbox event, so a crash cannot leave a committed post with no durable publication record. The relay may publish more than once, so consumers record event IDs and apply transitions idempotently. A Saga coordinates later steps and defines compensation where a real business reversal exists; reconciliation repairs cases whose outcome cannot be inferred from acknowledgements alone.

For this project, post metadata and media storage are already separate operations, while likes demonstrate a database uniqueness guard. Full publish idempotency, outbox delivery, workflow status, and orphan cleanup are future work.

## Practice status

The artifact is `READY`; comprehension remains `TODO` until the learner can explain the retry path, identify an ambiguous failure, and choose compensation or reconciliation without notes.

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
