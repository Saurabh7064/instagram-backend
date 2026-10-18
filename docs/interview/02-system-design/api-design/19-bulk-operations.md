# 19. Bulk operations and partial success

## Question

How do you design a bulk API with explicit atomicity, per-item results, and safe retries?

[Module sequence](README.md) · Artifact: READY · Learning: TODO · Suggested study: 20–30 minutes plus design exercise

## What you will learn

Distinguish transport-level batch success from item outcomes; compare all-or-nothing with partial success; bound size, authorize every item, and define replay.

Prerequisites: the foundational contract, method, authorization, and compatibility lessons. These are proposed teaching examples; routes, collaborators, DTOs, and policies shown here are not installed by this documentation change. Imports and application wiring are omitted. Use the existing authentication rules when implementing any extension.

## Must understand: fewer round trips can hide many writes

A client wants to delete several saved posts or create comments in a batch. A bulk request combines multiple items into one call. Atomicity means either all intended database changes commit or none do. Partial success means some items can commit while others fail according to an explicit contract.

Think of submitting one basket of forms. The analogy stops because some forms can already be committed when the connection drops, and rolling back one local database cannot recall remote side effects.

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

## Controlled failure drill

Prediction: item 2 violates a constraint after item 1. Under atomic policy, item 1 must be absent from committed effects; under partial policy, item 1 remains successful with a clear item-2 failure. Detect by reading actual stored state after the response and after a forced timeout. Contain new batch submissions on ambiguity, recover by reconciling item IDs/outcomes, prevent with bounded batches, explicit transaction boundaries, and response-loss tests.

## Project mapping and practice

Current [PostController](../../../../src/main/java/com/instagram/backend/controller/PostController.java) has individual deletion and [PostService](../../../../src/main/java/com/instagram/backend/service/PostService.java) enforces ownership. There is no bulk route today. For a proposed F-018 saved-post bulk action, define maximum items, duplicate policy, atomicity, per-item denial/concealment, timeout, replay, and notification behavior before writing a controller.

## Interview-ready answer

I make the atomicity policy part of the contract. An atomic batch is appropriate only when the operations need one invariant and the transaction scope is realistic. Independent items can use partial outcomes, but the response must correlate every input with a result and the transactions must actually commit independently. I bound amplification, authorize each item, and specify response-loss recovery. A successful batch status cannot imply every item succeeded unless that is the documented all-or-nothing guarantee. Large work becomes a durable job rather than a huge synchronous transaction.

## Stop/go and learner evidence

Explain the worked request without notes, justify a competing option, predict the failure, and describe a test that checks the final state or response. Record an attempt in [learner progress](learner-progress.md). A completed artifact does not establish understanding or an implemented feature.

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
