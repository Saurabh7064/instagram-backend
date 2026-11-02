# 19. Bulk operations and partial success

## Question

How do you design a bulk API with explicit atomicity, per-item results, and safe retries?

[Module sequence](README.md) · Artifact: READY · Learning: TODO · Suggested study: 20–30 minutes plus design exercise

## What you will learn

Derive the final state of a mixed-permission deletion batch, distinguish atomic and partial outcomes, and recover when the response disappears after some work commits.

Builds on [ownership](06-identity-and-ownership.md) and [attempt identities](10-idempotency-keys.md). This backend has no bulk route today; the examples are proposed contracts.

## One request can contain several different outcomes

User 7 submits one request to delete posts `[42,43,99]`. They own 42; user 9 owns 43; 99 does not exist. Sending these IDs together saves round trips, but it creates a question that individual requests do not: should the failure on 43 stop deletion of 42?

A **bulk request** groups multiple items into one call. Under **atomic** behavior, the intended database changes commit together or none commit. In our example, the request cannot delete every item as asked, so it rejects the batch and preserves post 42. Under **partial success**, independent items can complete separately: delete 42, retain 43, and report that 99 was absent.

Neither policy is automatically the correct one. Choose atomicity when the items need one combined guarantee in a realistic transaction scope. Choose partial outcomes when they are independent and the client can inspect and retry individual failures. The caller needs to know the policy before submitting, rather than discover it after a timeout.

A basket of forms is a useful analogy: the office might reject the whole basket or process each form. The limit is uncertain delivery and external effects. Some writes can already have committed when the response is lost, and a database rollback cannot recall a notification that was already sent.

The three implementation choices below all need explicit size bounds and item-by-item permissions. Grouping requests changes the transport; it does not grant broader authority.

| Policy | Choose when | Main cost |
|---|---|---|
| Atomic batch | Items form one invariant in one transactional store | More locking; one bad item rejects all; strict size cap |
| Partial success | Independent operations; user can retry failures | Per-item results and replay tracking |
| Async bulk job | Large/slow workload | Job state, durable dispatch, eventual results |

Use one explicit operation type or a small allowlist. A general “execute arbitrary URL/method” batch creates a complex proxy/security contract and can amplify load.

## Spring Boot example and result schema

```java
public record DeletePostsRequest(
        @NotEmpty @Size(max = 50) List<@NotNull Long> postIds) {}

@PostMapping("/api/post-deletion-batches")
DeletionBatchResult deleteMany(
        @RequestHeader("Authorization") String authorization,
        @Valid @RequestBody DeletePostsRequest request) {
    return deletionBatches.executeIndependent(authorization, request);
}
```

Proposed types/service. The list limit bounds item count, not bytes, work, or time by itself. Container element @NotNull rejects null IDs; duplicates need an explicit reject/deduplicate policy. Each item must be checked for ownership, even when another item was authorized.

A possible synchronous partial-result contract uses 200 to indicate a valid batch was processed and includes explicit item outcomes:

```json
{
  "results": [
    {"inputIndex":0,"postId":42,"outcome":"deleted"},
    {"inputIndex":1,"postId":43,"outcome":"forbidden","status":403},
    {"inputIndex":2,"postId":99,"outcome":"not_found","status":404}
  ]
}
```

This is a product choice, not a universal status convention. Do not automatically borrow WebDAV 207 without defining a compatible response format and client behavior. Malformed batch input can fail the entire request with 400 before any item executes; asynchronous acceptance can use 202 plus a job resource.

## Worked state and retry trace

User7 owns 42; user9 owns 43; 99 is absent. Partial mode processes each independent item: 42 is removed, 43 remains, 99 remains absent. Atomic mode would reject and preserve 42 as well. The caller must know which policy applies before submitting.

Now drop the response after item 42 commits. Repeating DELETE-like desired-state work can still converge to absent, though the reported outcome may change to already absent. For bulk creates or other non-idempotent commands, use item attempt identities in addition to a batch attempt key if clients may retry subsets. Reusing one batch key with a changed item list should conflict, not execute a different request under the old identity. Preserve inputIndex or a stable client item ID so the caller can correlate results even if the service processes concurrently.

Independent partial transactions should be explicit. One Spring @Transactional wrapper with a caught database error can mark the entire transaction rollback-only; swallowing exceptions does not create partial commits. An atomic local batch must also avoid sending irreversible notifications before commit.

## Lose the answer after deleting post 42

Use `[42,43,99]` with the ownership stated above. On paper, drop the response after deleting 42 in partial mode. Predict the caller's knowledge: it cannot infer from the timeout which items completed. Retrying under desired-state deletion can still leave 42 absent, but its reported outcome may now be “already absent.”

Detect recovery defects by reading actual final rows and matching every result to its input. Contain new submissions if outcomes cannot be reconciled, then recover using stable item identities and the documented transaction policy. In atomic mode, a rejected batch must preserve 42 instead. Prevent recurrence with response-loss tests for both the batch and item replay rules; swallowing an exception inside one failed transaction does not establish independent partial commits.

## Project mapping and practice

Current [PostController](../../../../src/main/java/com/instagram/backend/controller/PostController.java) has individual deletion and [PostService](../../../../src/main/java/com/instagram/backend/service/PostService.java) enforces ownership. There is no bulk route today. For a proposed F-018 saved-post bulk action, define maximum items, duplicate policy, atomicity, per-item denial/concealment, timeout, replay, and notification behavior before writing a controller.

## Interview-ready answer

I make the atomicity policy part of the contract. An atomic batch is appropriate only when the operations need one invariant and the transaction scope is realistic. Independent items can use partial outcomes, but the response must correlate every input with a result and the transactions must actually commit independently. I bound amplification, authorize each item, and specify response-loss recovery. A successful batch status cannot imply every item succeeded unless that is the documented all-or-nothing guarantee. Large work becomes a durable job rather than a huge synchronous transaction.

## Before moving on

Derive the final state of `[42,43,99]` under both policies. Continue when you can match outcomes to inputs and recover the lost-response case without assuming a whole-request status means every item succeeded.

Attempt the questions before opening their answers and record the explanation in [learner progress](learner-progress.md). This revision leaves learning status unchanged; a finished document is not evidence that you have demonstrated the idea.

## Questions and explained answers

<details>
<summary>1. Why is a 200 batch response insufficient to infer all items succeeded?</summary>

In a documented partial-success contract, it means the batch was processed; the per-item outcomes describe committed successes and failures. Clients must inspect them.

</details>

<details>
<summary>2. What changes between atomic and partial mode for IDs 42,43,99?</summary>

If 42 is owned, 43 forbidden, and 99 absent, partial mode can delete only 42. Atomic mode rejects the batch and preserves 42 too. The policy must be stated beforehand.

</details>

<details>
<summary>3. Does catching an exception inside one transaction create partial success?</summary>

No. Persistence errors can mark the transaction rollback-only. Independent item commits need deliberately separate boundaries; atomic mode should preserve all-or-nothing behavior.

</details>

<details>
<summary>4. Why keep item IDs as well as a batch key?</summary>

A caller may retry a subset after a lost response. Stable item attempt identities can deduplicate individual non-idempotent effects; a batch key alone identifies only the original whole request.

</details>

<details>
<summary>5. Why authorize every batch item?</summary>

A mixed batch can contain both owned and non-owned objects. Permission on the first or parent collection does not authorize all requested IDs.

</details>
