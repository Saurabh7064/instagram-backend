# 11. Conditional updates and lost writes

## Question

How do two editors avoid silently overwriting each other?

[Module sequence](README.md) · Artifact: READY · Learning: TODO

## What you will learn

Follow two edits from revision 7, show why only one may succeed, and connect the HTTP condition to the database write that actually enforces it.

Compare with [idempotency keys](10-idempotency-keys.md): replay identity and revision protection solve different problems.

## Both editors started from the same old caption

Two clients open post 42 when its caption is `old`. Both read revision 7. Client A changes the caption to `A` and saves; client B, still looking at the old copy, saves `B`. With unconditional replacements, B can silently erase A's change.

The server needs to know which revision B edited. It can return an **entity tag (ETag)** with the read response: an HTTP value identifying the selected representation's revision. The client sends that value back in **If-Match**, meaning “perform this write only if that is still the current representation.” For our example, the tag is `"post42-v7"`.

After A saves, revision 7 becomes revision 8. B still supplies the revision-7 tag, so the condition is false. The server returns `412 Precondition Failed` and preserves A's edit. B can then reload and decide how to reconcile the difference. Automatically attaching revision 8 and overwriting would defeat the protection.

This is like editing a document with a revision stamp. The limit of the comparison is timing: the check and write must be one indivisible database decision. Checking the stamp and then writing unconditionally leaves another gap for competing edits.

The previous lesson's [attempt key](10-idempotency-keys.md) identifies a repeated request. This revision check answers a different question: whether a new write is based on current state.

## Carry the read condition into the write

The project uses [Spring Boot 4.0.2 and Java 24](../../../../build.gradle). The excerpt below is a design proposal unless identified as current code; omitted imports and collaborators still need implementation.

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

### Read the boundary in the code

The proposed route passes the supplied condition into a service that must check ownership and enforce the revision atomically. An optimistic version column or a conditional SQL write can do that; an ordinary Java comparison followed by an unconditional save cannot.

The excerpt returns the updated view but deliberately omits a PUT-response ETag. This backend trims inputs, and HTTP restricts validators returned after a transformed PUT. A subsequent GET can provide the authoritative read validator. Header parsing and conflict translation still need implementation.

## Make revision 7 usable by only one edit

Initially post 42 has caption `old`, version 7. A and B both read that representation and retain its strong tag. A submits caption `A` with the revision-7 condition. The database changes the caption and advances version 7 to 8 in one conditional update.

B now submits caption `B` with the same revision-7 condition. The current version is 8, so B's update changes zero rows. Return the stale-condition response, 412, and keep caption `A` at version 8. A 200 for B would acknowledge a write that did not occur; an unconditional retry would erase the protection.

The important property is the indivisible check-and-write. Reading 7, comparing it in Java, and later saving without a version condition allows both clients to pass the comparison before either writes.

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

This requires a proposed version column/migration. The affected-row count is the concurrency decision: 1 means the matching authorized revision was changed; 0 means nothing matched. Distinguish absent, forbidden, and stale without disclosing data under your policy; do not infer every zero is stale. With Java Persistence API (JPA) `@Version`, flush/commit can raise the conflict after setter calls, so place exception translation where it can see that failure.

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

## Apply this to the backend

[Post](../../../../src/main/java/com/instagram/backend/domain/Post.java) has no version field, and [PostController.update](../../../../src/main/java/com/instagram/backend/controller/PostController.java) has no If-Match handling. Both the persistence strategy and the HTTP boundary therefore remain proposed.

For F-014, plan the migration, read validator, conditional write, and conflict screen together. A version column alone is not a client protocol, and a header alone is not concurrency control. The two-client test must finish with one accepted edit and the other preserved as an unresolved client choice.

## Useful later: choices and costs

Choose conditional updates when silent loss is unacceptable. Last-write-wins is simpler but discards earlier changes. A stale precondition maps to 412; an absent required precondition can use 428. The current entity and update route do not implement this protocol. @Version alone does not define the HTTP contract.

## Let both clients pass a separate Java check

Keep A and B at revision 7. On paper, A reads 7 and passes a Java comparison; B also reads 7 and passes; A saves `A`; B saves `B` unconditionally. Predict the final value: B silently hides A despite both requests having checked a version.

A future coordinated two-client test detects the defect by asserting one accepted mutation and one stale-condition response. Contain stale overwrites by restoring an enforced conditional-write path; a conflict screen alone cannot protect an unconditional backend. Recover the user's intended edit by reloading and explicitly reconciling it. Prevent recurrence with a version predicate or correct optimistic locking, checked at the actual write/commit boundary. The expected result is caption `A` at revision 8 when A wins, not two successful replacements.

## Interview-ready answer

I expose a validator for the selected editable representation and require a matching revision where lost writes are unacceptable. The comparison and write must be one concurrency decision, through a versioned update or correct optimistic locking, not a separate read check. A missing required precondition and a supplied stale one are different failures. I scope the validator to what actually changes in the representation, and remember a weak validator cannot satisfy If-Match strong comparison. This project has neither the version field nor precondition handler today. I would implement the migration, read and write boundaries, exception translation, and two-client tests together, then handle ambiguous response-loss recovery separately from concurrency prevention.

## Before moving on

Walk A and B from revision 7 through one accepted write. Continue when you can explain why separate comparison is racy, why a retry may get 412 after success, and how the user recovers.

Attempt the questions before opening their answers and record the explanation in [learner progress](learner-progress.md). This revision leaves learning status unchanged; a finished document is not evidence that you have demonstrated the idea.

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
