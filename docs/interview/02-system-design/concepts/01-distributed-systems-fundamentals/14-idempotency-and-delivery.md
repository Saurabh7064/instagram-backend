# Micro-lesson 14 — Safe retries: count business effects, not just deliveries

[Module](./README.md) · [Previous: sagas](./13-sagas.md) · [Next: logical clocks](./15-lamport-clocks.md)

Core study: 10–15 minutes; external-effect boundaries are Useful later. Prerequisites: a commit can succeed before its response is lost; separate service commits leave failure gaps.

## One publish, two attempts, one post — Must understand

User 7 wants to publish “Sunset” once. The database creates post 42, but the success reply is lost. The client retries. If the backend treats that request as a fresh create, it may produce post 43. Refusing every retry is no solution either: sometimes the original request never arrived.

We need to preserve the caller's intent across attempts. Give this one publish an identity K, and reuse K whenever retrying it. A separate intentional publish, even with the same caption, gets a separate identity. This caller-supplied identity is an **idempotency key**.

**Idempotency** means repeating the same operation has the same intended effect as doing it once. Logs and metrics may still record several attempts. **Deduplication** is recognizing the repeated identity and preventing a repeated effect, including when two attempts arrive concurrently.

Recognition needs durable evidence. If we remember K only in one backend's memory, another instance or a restart can forget it. If we save the post and K separately, a crash can occur between those saves. The useful design is therefore a single database transaction containing both the post and its completed operation record.

## Store the identity and post as one outcome

Fixed example: user 7 requests one create with key K and caption “Sunset.” No post or record for K exists initially. Scope the identity as `(user 7, create-post, K)`. Store a fingerprint of the relevant request payload so K cannot silently mean two different operations.

Proposed database design: one transaction stores both the post and a completed operation record containing the key, fingerprint, post ID, and stable result. Enforce uniqueness of the scoped key in the database. That constraint is the arbiter when two backend instances receive K together; an in-memory “have I seen this?” check is insufficient.

1. Attempt A claims the unique scoped key inside the transaction. Attempt B arrives concurrently with the same key and payload; it cannot independently win the same claim.
2. A inserts post 42 and records result `postId=42`. Both changes commit together.
3. A's response is lost. The client still does not know the outcome.
4. The retry looks up K. It verifies the fingerprint matches and returns the stored result for 42 instead of creating 43.
5. If another request sends K with a different caption, reject the conflicting reuse. The caller must choose a new key for a genuinely new intent.

If A rolls back before committing, neither the post nor its completed key record should remain from that transaction. Another attempt can safely try. Handling B's pending claim requires an explicit policy: wait, retry the lookup, or return a documented in-progress response. Do not return success before the original result is committed.

## Put a crash between the two saves

On this same create, compare the two unsafe record orders:

| Order | Crash point | What retry sees | Failure |
|---|---|---|---|
| Save post, then record K | After post 42 commits, before K is stored | No K | Can create duplicate post 43 |
| Mark K complete, then save post | After K commits, before post exists | K marked complete | Can suppress the operation without creating a post |
| Commit post and completed K together | Before or after the transaction commit | Neither, or both | Retry can determine one consistent database outcome |

The atomic boundary closes these database gaps. Key expiry reopens the possibility of treating a late retry as new, so retention must match the advertised retry window. Preserve authorization checks when returning stored results; one user must not retrieve another user's response by guessing K.

The ticket analogy is a clerk reusing the same order ticket instead of making another order. It stops matching when two clerks act simultaneously: uniqueness and atomic storage, not recognition alone, settle the race. The [AWS Builders' Library article on idempotent requests](https://aws.amazon.com/builders-library/making-retries-safe-with-idempotent-APIs/) discusses caller intent, parameter mismatch, and atomic recording.

## What do “at least once” and “exactly once” count?

A **delivery guarantee** describes how work reaches or is attempted by a receiver under specified failures. That count differs from the number of committed business effects. Our create can have several attempts but one post.

| Claim | Intended promise within its stated scope | Failure risk |
|---|---|---|
| At-most-once | Zero or one processing attempt/effect, depending on the stated boundary | Work may be lost if failure occurs before completion and there is no retry |
| At-least-once | Work is retried until acknowledged, assuming eventual recovery and continued retries | The receiver may perform repeated attempts |
| Exactly-once | One committed logical effect for the covered operation despite retries | Requires a specified atomic/deduplication boundary; not a claim that a network sends only once |

The words “processing” and “delivery” can be used differently by systems. Ask which stage is counted. Unlimited permanent failures cannot guarantee eventual completion; at-least-once promises need recovery and retention assumptions. Idempotent processing can turn repeated attempts into one scoped business effect.

## An email still sits outside this database transaction — Useful later

An email provider is not part of this database transaction. A crash after sending but before recording success can send twice; recording success first can lose the send. A proposed **outbox** stores an intent to deliver in the same transaction as local work; a sender later retries it. The receiver still needs an idempotency contract or reconciliation for its own effect. An outbox alone is not global exactly-once delivery.

A **broker** is a service that stores and forwards messages. [Kafka's delivery-semantics documentation](https://kafka.apache.org/41/design/design/#message-delivery-semantics) distinguishes guarantees within supported transactional processing from coordination with external destinations. A broker claim does not automatically cover an unrelated email or file API.

## Choice and project mapping

Use durable request keys for retryable creates and identity-aware consumers. Naturally repeatable reads usually need no stored result. The costs are extra storage, concurrency policy, retention, payload comparison, and client responsibilities.

[PostController.create](../../../../../src/main/java/com/instagram/backend/controller/PostController.java) accepts no idempotency-key argument, and [PostService.create](../../../../../src/main/java/com/instagram/backend/service/PostService.java) stores no key/result record. This is proposed F-010 hardening. A like-row uniqueness constraint is a narrower invariant, not a generic create-request deduplication feature.

## Lose the reply, then retry K concurrently — paper exercise

After the atomic commit, lose the reply and send K concurrently through two clients acting for user 7. Predict both the post count and the permitted client responses.

There should be exactly one post for this operation. Clients receive its same completed logical result or the documented pending behavior while it resolves. A payload mismatch must be rejected; unresolved work must not be presented as completed merely to end the wait.

Recover by reading durable K, checking authorization and the payload fingerprint, then returning 42. If no committed operation exists, retry under the uniqueness rule. Exercise rollback, simultaneous duplicates, lost replies, expiry, and external effects separately. Logs can reveal duplicate attempts, but the atomic storage rule is what prevents duplicate database effects.

## Before moving on

Artifact: `READY`; comprehension: `TODO`.

Derive both separate-commit gaps and the atomic outcome. Explain the three delivery claims with their scope, then identify why email remains a separate problem.

## Questions and explained answers

<details>
<summary>1. Why must a retry use K instead of generating another key?</summary>

K connects the request to the original intended publish. A new key describes a different operation to the receiver, so it cannot safely infer the caller meant “return post 42.” Equal captions do not prove equal intent.

</details>

<details>
<summary>2. Why are both uniqueness and one transaction needed?</summary>

Uniqueness prevents simultaneous instances claiming the same operation independently. Atomic commit prevents the key result and business effect diverging after a crash. Either alone leaves a different failure gap.

</details>

<details>
<summary>3. Can at-least-once attempts produce one committed effect?</summary>

Yes. Attempts can repeat while an atomic deduplication boundary ensures they refer to the same logical effect. Count deliveries and effects separately, and state the recovery/retention assumptions.

</details>

<details>
<summary>4. Why is a database result record insufficient for exactly one email?</summary>

The provider's effect occurs outside that transaction. A crash can leave the send outcome uncertain. The provider must participate through its own idempotency/transaction contract, or the design must accept and reconcile that uncertainty.

</details>
