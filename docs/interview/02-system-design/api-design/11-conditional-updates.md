# 11. Conditional updates and lost writes

## Question

How do two editors avoid silently overwriting each other?

[Module sequence](README.md) · Study in two passes: core model, then design decisions (15–20 minutes each) · Artifact: READY · Learning: TODO

## What you will learn

Connect strong HTTP validators to atomic versioned writes, distinguish 412/428, and diagnose stale requests, lost responses, and validator scope.

Prerequisites: Lesson 10 and its stop/go check. All later lessons remain TODO until their own checkpoints are attempted.

## Must understand: problem and model

Two clients can read the same post and each write a replacement. A version identifies the revision read. An ETag is an HTTP representation validator; If-Match requires a matching current validator before updating. Like editing a signed document revision, the check asks whether you started from the current copy; unlike a document stamp, comparison and mutation must be atomic.

## Spring Boot example

Examples are proposed except where explicitly identified as current code. Imports/wiring are omitted; named new services and DTOs need implementation. Preserve current authorization rules. The project uses [Spring Boot 4.0.2 and Java 24](../../../../build.gradle).

```java
@PutMapping("/posts/{id}")
ResponseEntity<PostView> update(
        @PathVariable Long id,
        @RequestHeader("If-Match") String expected,
        @Valid @RequestBody UpdatePostRequest body) {
    var updated = versionedPosts.updateOwned(id, viewerId(), expected, body);
    return ResponseEntity.ok().body(updated.view());
}
```

### Walkthrough

This proposed service must evaluate a strong validator and update within concurrency control, such as a database version column with optimistic locking. Checking expected then doing an unconditional update leaves a race. The response ETag must describe the representation returned; real If-Match parsing must support the chosen documented header grammar.

## Request trace: predict, act, observe, explain

Before following the trace, predict the status and final stored state. Then trace these transitions in order:

A and B read version 7 with ETag "v7". A writes with If-Match "v7" -> atomic update to 8. B submits "v7" -> 412 -> no overwrite. B reloads version 8, shows a comparison, and explicitly resubmits a resolved edit.

The observable result includes the response **and** the final stored state. A successful status alone cannot prove concurrency or authorization correctness.

## Turn an HTTP precondition into an atomic write

A validator must cover the representation whose update is protected. A quoted strong ETag such as `"post42-v7"` asserts the relevant representation revision; `W/"post42-v7"` is weak and cannot satisfy If-Match strong comparison. A post-row version does not cover changing viewer-like or author-name fields unless your representation/validator design accounts for them. A dedicated editable representation can make that scope explicit.

Proposed database algorithm:

```sql
UPDATE posts
SET caption = :caption,
    location_label = :location,
    version = version + 1
WHERE id = :id
  AND author_id = :viewer
  AND version = :expected;
```

This requires a proposed version column/migration. The affected-row count is the concurrency decision: 1 means the matching authorized revision was changed; 0 means nothing matched. Distinguish absent, forbidden, and stale without disclosing data under your policy; do not infer every zero is stale. With JPA @Version, flush/commit can raise the conflict after setter calls, so place exception translation where it can see that failure.

### Exact interleaving

Initial row caption=old, version=7. A and B both send expected=7. A’s conditional SQL changes caption=A and version=8, affected=1. B’s SQL checks version=7 against 8, affected=0. Caption stays A. Returning 200 for B would falsely acknowledge a write. Reload plus explicit reconciliation is required, not an automatic overwrite with the new version.

| Header case | Meaning | Proposed response policy |
|---|---|---|
| No If-Match when required | Caller supplied no protection | 428 |
| Matching strong validator | Revision still matches | Mutate atomically |
| Stale validator | Supplied condition false | 412 |
| If-Match: * | Requires existence | Does not protect against a stale specific revision |
| Malformed field value | Invalid syntax | Documented 400 |

A production parser must support the admitted header grammar and not simply remove quotes and parse a number. Multiple entity tags have defined semantics; define narrower application requirements only with clear validation and client support.

For PUT, do not emit a success-response validator when the submitted representation was transformed before storage; HTTP places conditions on returning validators for PUT. This backend trims input, so use a subsequent GET for the authoritative read validator unless the representation contract satisfies those conditions. The example deliberately omits an ETag on its PUT response.

A lost response adds another wrinkle: A’s first write commits version 8, then A retries If-Match version 7. A 412 is compatible with a first attempt that succeeded. The client can reload and compare; if the operation needs exact response replay, combine with a separate attempt key. Conditional updates prevent stale writes; they do not by themselves identify retried attempts.

Current [Post entity](../../../../src/main/java/com/instagram/backend/domain/Post.java) has no @Version, and the update controller has no If-Match handling. This design is proposed, including the migration, read validator, atomic persistence strategy, and conflict UX.

## Project mapping and practice

Read the actual [backend implementation](../../../../src/main/java/com/instagram/backend/domain/Post.java) before changing it. The example above distinguishes existing behavior from a proposed extension; a linked file establishes the current starting point, not proof that the proposed design exists.

**Practice task:** For F-014 propose a version migration, read ETag, If-Match write requirement, and stale-edit UX before changing the endpoint.

Keep this a design exercise until a feature change is explicitly undertaken. An implementation must follow the repository integration-test, API verification, documentation, and commit workflow.

## Useful later: choices and costs

Choose conditional updates when silent loss is unacceptable. Last-write-wins is simpler but discards earlier changes. A stale precondition maps to 412; an absent required precondition can use 428. The current entity and update route do not implement this protocol. @Version alone does not define the HTTP contract.

## Controlled failure drill

Predict two updates after both read version 7. Without a check, the later write hides the first. Detect a two-client integration test, contain with conflict UI, recover by reloading and merging, prevent with atomic version enforcement and stale-write tests.

Describe the prediction before reading the symptom. In a future lab, use disposable data, record the observed response and state, and verify recovery rather than assuming it succeeded.

## Interview-ready answer

I expose a validator for the selected editable representation and require a matching revision where lost writes are unacceptable. The comparison and write must be one concurrency decision, through a versioned update or correct optimistic locking, not a separate read check. A missing required precondition and a supplied stale one are different failures. I scope the validator to what actually changes in the representation, and remember a weak validator cannot satisfy If-Match strong comparison. This project has neither the version field nor precondition handler today. I would implement the migration, read and write boundaries, exception translation, and two-client tests together, then handle ambiguous response-loss recovery separately from concurrency prevention.

## Stop/go and learner evidence

Proceed only after explaining the request trace without notes, predicting the failure, and answering all three checks below before opening their answers. Record the attempt in [learner progress](learner-progress.md). Artifact readiness does not establish comprehension or implementation readiness.

## Questions and explained answers

<details>
<summary>1. What should happen when two clients submit If-Match "v7"?</summary>

Only one update may atomically advance version 7 to 8. The other stale write fails with 412 and leaves the first edit intact. A separate read-compare-unconditional-write sequence is racy.

</details>

<details>
<summary>2. Why can a post-row version be insufficient for a full feed DTO ETag?</summary>

The DTO also contains author and viewer-like fields that may change independently. A validator must cover the selected representation, or the API should define a narrower editable view.

</details>

<details>
<summary>3. Does If-Match:* prevent overwriting an older revision?</summary>

It requires an existing selected representation, not a match to a specific previously read revision. Use the appropriate strong entity tag for revision protection.

</details>

<details>
<summary>4. What does an affected-row count of zero prove?</summary>

Only that the full conditional predicate matched nothing. Missing, unauthorized, and stale states can all cause it; map them under a deliberate policy rather than assuming stale every time.

</details>

<details>
<summary>5. Why can a retry receive 412 even if the user’s edit succeeded?</summary>

The first request may have committed a new version and lost its response. The identical retry carries the old validator and now fails. Reload/reconcile or a separate attempt-key protocol resolves ambiguity.

</details>
